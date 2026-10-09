package com.example.eclipse.engine

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RadialGradient
import android.graphics.RectF
import android.graphics.Shader
import com.example.R
import com.example.eclipse.model.BillboardSprite
import com.example.eclipse.model.Chapter
import com.example.eclipse.model.GraphicQuality
import com.example.eclipse.model.Monster
import com.example.eclipse.model.MonsterState
import com.example.eclipse.model.MonsterType
import com.example.eclipse.model.Player
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.hypot
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sin

class Raycast3DEngine(context: Context? = null) {

    private val wallPaint = Paint().apply { isAntiAlias = false }
    private val floorPaint = Paint().apply { isAntiAlias = false }
    private val skyPaint = Paint().apply { isAntiAlias = false }
    private val spritePaint = Paint().apply { isAntiAlias = true }
    private val handPaint = Paint().apply { isAntiAlias = true }
    private val lightConePaint = Paint().apply { isAntiAlias = true }

    private val rainPaint = Paint().apply {
        color = Color.argb(130, 190, 220, 255)
        strokeWidth = 2.2f
        isAntiAlias = true
    }
    private val dustMotePaint = Paint().apply {
        color = Color.argb(160, 240, 245, 255)
        isAntiAlias = true
    }
    private val lockerSlatPaint = Paint().apply {
        color = Color.argb(245, 12, 14, 16)
        isAntiAlias = true
    }

    private var handBitmap: Bitmap? = null

    init {
        if (context != null) {
            try {
                handBitmap = BitmapFactory.decodeResource(context.resources, R.drawable.img_fp_hand)
            } catch (_: Exception) {}
        }
    }

    // Weather & volumetric dust particles
    private val rainParticles = FloatArray(180) { (Math.random() * 2000).toFloat() }
    private val dustParticles = FloatArray(60) { (Math.random() * 1000).toFloat() }
    private val voidMotes = FloatArray(90) { (Math.random() * 2000).toFloat() }

    // Z-buffer for 1D column depths
    private var zBuffer = FloatArray(1200)

    fun render(
        canvas: Canvas,
        width: Float,
        height: Float,
        player: Player,
        map: Array<IntArray>,
        sprites: List<BillboardSprite>,
        chapter: Chapter,
        quality: GraphicQuality,
        headBobOffset: Float,
        flashlightFlicker: Float = 1.0f
    ) {
        val w = width.toInt()
        val h = height.toInt()
        if (w <= 0 || h <= 0) return

        if (zBuffer.size < w) {
            zBuffer = FloatArray(w)
        }

        val step = when (quality) {
            GraphicQuality.LOW -> 3
            GraphicQuality.MEDIUM -> 2
            GraphicQuality.HIGH -> 1
        }

        val posX = player.x
        val posY = player.y
        val dirX = cos(player.angle)
        val dirY = sin(player.angle)

        // Camera plane perpendicular to direction (FOV ~ 66 degrees)
        val fovFactor = 0.66f
        val planeX = -dirY * fovFactor
        val planeY = dirX * fovFactor

        val pitchOffset = (player.pitch * h * 0.85f) + headBobOffset

        // 1. Render Sky & Floor with Realistic Wet Reflection
        renderSkyAndFloor(canvas, width, height, chapter, pitchOffset, player.flashlightOn && player.batteryPercent > 0)

        // 2. Raycast Walls
        var x = 0
        while (x < w) {
            val cameraX = 2f * x / w - 1f
            val rayDirX = dirX + planeX * cameraX
            val rayDirY = dirY + planeY * cameraX

            var mapX = posX.toInt()
            var mapY = posY.toInt()

            val deltaDistX = if (rayDirX == 0f) 1e30f else abs(1f / rayDirX)
            val deltaDistY = if (rayDirY == 0f) 1e30f else abs(1f / rayDirY)

            val stepX: Int
            var sideDistX: Float
            if (rayDirX < 0) {
                stepX = -1
                sideDistX = (posX - mapX) * deltaDistX
            } else {
                stepX = 1
                sideDistX = (mapX + 1.0f - posX) * deltaDistX
            }

            val stepY: Int
            var sideDistY: Float
            if (rayDirY < 0) {
                stepY = -1
                sideDistY = (posY - mapY) * deltaDistY
            } else {
                stepY = 1
                sideDistY = (mapY + 1.0f - posY) * deltaDistY
            }

            var hit = 0
            var side = 0
            var maxSteps = 45

            while (hit == 0 && maxSteps > 0) {
                maxSteps--
                if (sideDistX < sideDistY) {
                    sideDistX += deltaDistX
                    mapX += stepX
                    side = 0
                } else {
                    sideDistY += deltaDistY
                    mapY += stepY
                    side = 1
                }

                if (mapX in map.indices && mapY in map[0].indices) {
                    if (map[mapX][mapY] > 0) {
                        hit = map[mapX][mapY]
                    }
                } else {
                    hit = 1
                }
            }

            val perpWallDist = if (side == 0) {
                (mapX - posX + (1 - stepX) / 2f) / rayDirX
            } else {
                (mapY - posY + (1 - stepY) / 2f) / rayDirY
            }.coerceAtLeast(0.12f)

            for (col in x until min(x + step, w)) {
                zBuffer[col] = perpWallDist
            }

            val lineHeight = (h / perpWallDist).toFloat()
            val drawStart = ((-lineHeight / 2f + h / 2f) + pitchOffset).coerceAtLeast(-h.toFloat())
            val drawEnd = ((lineHeight / 2f + h / 2f) + pitchOffset).coerceAtMost(h * 2f)

            val wallHitX = if (side == 0) posY + perpWallDist * rayDirY else posX + perpWallDist * rayDirX
            val texCoordX = (wallHitX - wallHitX.toInt().toFloat()).let { if (it < 0) it + 1f else it }

            val lightFactor = calculateRealisticWallLighting(
                distance = perpWallDist,
                cameraX = cameraX,
                side = side,
                player = player,
                flicker = flashlightFlicker
            )

            val wallColor = getDetailedWallColor(hit, texCoordX, lightFactor, chapter, side)
            wallPaint.color = wallColor

            canvas.drawRect(
                x.toFloat(),
                drawStart,
                (x + step).toFloat(),
                drawEnd,
                wallPaint
            )

            x += step
        }

        // 3. Render 3D Sprites (Monsters, Hospital Beds, Props, Pickups, Lockers)
        renderSprites(canvas, width, height, player, sprites, dirX, dirY, planeX, planeY, pitchOffset)

        // 4. Volumetric Flashlight Dust Motes & Weather
        if (player.flashlightOn && player.batteryPercent > 0) {
            renderVolumetricFlashlightDust(canvas, width, height, headBobOffset)
        }
        renderWeatherParticles(canvas, width, height, chapter)

        // 5. Realistic First-Person Arm & Hand Holding Tactical Flashlight
        if (!player.isHiding) {
            renderRealisticPlayerHands(canvas, width, height, player, headBobOffset, flashlightFlicker)
        }

        // 6. Realistic Locker View (if inside locker)
        if (player.isHiding) {
            renderRealisticLockerView(canvas, width, height)
        }
    }

    private fun renderSkyAndFloor(
        canvas: Canvas,
        w: Float,
        h: Float,
        chapter: Chapter,
        pitchOffset: Float,
        flashlightOn: Boolean
    ) {
        val horizon = (h / 2f + pitchOffset).coerceIn(0f, h)

        // 1. ATMOSPHERIC CEILING / SKY
        val (skyTop, skyBottom) = when (chapter) {
            Chapter.DEAD_CITY -> Pair(Color.rgb(1, 2, 4), Color.rgb(5, 8, 14))
            Chapter.BLACK_HOSPITAL -> Pair(Color.rgb(2, 4, 3), Color.rgb(7, 11, 9))
            Chapter.ENDLESS_FOREST -> Pair(Color.rgb(1, 1, 2), Color.rgb(3, 6, 4))
            Chapter.ECLIPSE_FACILITY -> Pair(Color.rgb(6, 3, 5), Color.rgb(14, 7, 9))
            Chapter.INVERTED_WORLD -> Pair(Color.rgb(16, 2, 12), Color.rgb(32, 5, 22))
        }

        skyPaint.shader = LinearGradient(0f, 0f, 0f, horizon, skyTop, skyBottom, Shader.TileMode.CLAMP)
        canvas.drawRect(0f, 0f, w, horizon, skyPaint)

        // Hospital Drop-Ceiling Acoustic Tile Grid
        if (chapter == Chapter.BLACK_HOSPITAL && horizon > 10f) {
            val gridPaint = Paint().apply {
                color = Color.argb(40, 45, 60, 50)
                strokeWidth = 1.5f
            }
            val numGrid = 6
            for (i in 1..numGrid) {
                val gy = horizon * (i.toFloat() / numGrid)
                canvas.drawLine(0f, gy, w, gy, gridPaint)
            }
        }

        // 2. TEXTURED REALISTIC FLOOR
        val (floorFar, floorNear) = when (chapter) {
            Chapter.DEAD_CITY -> Pair(Color.rgb(4, 5, 7), Color.rgb(15, 18, 22))
            Chapter.BLACK_HOSPITAL -> Pair(Color.rgb(5, 7, 6), Color.rgb(16, 22, 20))
            Chapter.ENDLESS_FOREST -> Pair(Color.rgb(3, 4, 3), Color.rgb(10, 14, 8))
            Chapter.ECLIPSE_FACILITY -> Pair(Color.rgb(8, 8, 10), Color.rgb(24, 24, 28))
            Chapter.INVERTED_WORLD -> Pair(Color.rgb(14, 3, 10), Color.rgb(34, 6, 24))
        }

        floorPaint.shader = LinearGradient(0f, horizon, 0f, h, floorFar, floorNear, Shader.TileMode.CLAMP)
        canvas.drawRect(0f, horizon, w, h, floorPaint)

        // 3. PERSPECTIVE FLOOR TILES & TEXTURE LINES
        val floorLinesPaint = Paint().apply {
            isAntiAlias = true
            strokeWidth = 1.8f
        }

        val floorDepth = (h - horizon).coerceAtLeast(1f)
        when (chapter) {
            Chapter.BLACK_HOSPITAL -> {
                // Checkerboard Linoleum Hospital Tile Perspective
                floorLinesPaint.color = Color.argb(45, 120, 150, 135)
                for (step in 1..7) {
                    val yProgress = (step.toFloat() / 7f)
                    val ly = horizon + (yProgress * yProgress) * floorDepth
                    canvas.drawLine(0f, ly, w, ly, floorLinesPaint)
                }
                // Perspective vanishing diagonals
                for (col in -3..3) {
                    val startX = w / 2f + (col * (w * 0.18f))
                    canvas.drawLine(w / 2f, horizon, startX, h, floorLinesPaint)
                }
            }

            Chapter.DEAD_CITY -> {
                // Cracked asphalt road with center yellow road marking
                val roadLinePaint = Paint().apply {
                    color = Color.argb(60, 210, 175, 40)
                    strokeWidth = 3.5f
                    isAntiAlias = true
                }
                canvas.drawLine(w / 2f, horizon + floorDepth * 0.25f, w / 2f, h, roadLinePaint)
            }

            Chapter.ECLIPSE_FACILITY -> {
                // Industrial Steel Grating Diamond Lines
                floorLinesPaint.color = Color.argb(50, 80, 95, 115)
                for (step in 1..6) {
                    val ly = horizon + (step / 6f) * (step / 6f) * floorDepth
                    canvas.drawLine(0f, ly, w, ly, floorLinesPaint)
                }
            }

            Chapter.INVERTED_WORLD -> {
                // Glowing eldritch void cracks on the floor
                val voidCrackPaint = Paint().apply {
                    color = Color.argb(55, 230, 40, 160)
                    strokeWidth = 2.2f
                    isAntiAlias = true
                }
                canvas.drawLine(w * 0.42f, horizon, w * 0.35f, h, voidCrackPaint)
                canvas.drawLine(w * 0.58f, horizon, w * 0.68f, h, voidCrackPaint)
            }

            else -> {}
        }

        // 4. WET CORRIDOR SPECULAR FLASHLIGHT POOL
        if (flashlightOn) {
            val floorSpotPaint = Paint().apply {
                isAntiAlias = true
                shader = RadialGradient(
                    w * 0.49f, h * 0.76f, w * 0.38f,
                    Color.argb(95, 245, 248, 255),
                    Color.TRANSPARENT,
                    Shader.TileMode.CLAMP
                )
            }
            canvas.drawOval(
                RectF(w * 0.14f, h * 0.56f, w * 0.84f, h * 0.94f),
                floorSpotPaint
            )

            // Central hot specular reflection on floor
            val hotSpotPaint = Paint().apply {
                isAntiAlias = true
                shader = RadialGradient(
                    w * 0.49f, h * 0.72f, w * 0.16f,
                    Color.argb(120, 255, 252, 235),
                    Color.TRANSPARENT,
                    Shader.TileMode.CLAMP
                )
            }
            canvas.drawOval(
                RectF(w * 0.35f, h * 0.64f, w * 0.63f, h * 0.82f),
                hotSpotPaint
            )
        }

        // Hospital emergency red EXIT sign wet floor reflection
        if (chapter == Chapter.BLACK_HOSPITAL) {
            val exitGlowPaint = Paint().apply {
                isAntiAlias = true
                shader = RadialGradient(
                    w * 0.49f, horizon + 20f, 45f,
                    Color.argb(135, 255, 25, 25),
                    Color.TRANSPARENT,
                    Shader.TileMode.CLAMP
                )
            }
            canvas.drawCircle(w * 0.49f, horizon + 20f, 45f, exitGlowPaint)
        }
    }

    private fun calculateRealisticWallLighting(
        distance: Float,
        cameraX: Float,
        side: Int,
        player: Player,
        flicker: Float
    ): Float {
        // True physical darkness in the desolate anomaly (almost pitch black beyond light cone)
        val trueDarkness = 0.025f

        val flashlight = if (player.flashlightOn && player.batteryPercent > 0) {
            val angleFromCenter = abs(cameraX)

            // Realistic elliptical spotlight cone with central concentrated hotspot
            val coneSpot = (1.0f - (angleFromCenter * 0.68f).coerceIn(0f, 1f))
            val coreHotspot = if (angleFromCenter < 0.22f) 0.45f else 0f

            // Inverse-square physical attenuation
            val attenuation = 1.0f / (1.0f + 0.08f * distance + 0.020f * distance * distance)
            val battSag = (player.batteryPercent / 100f).coerceIn(0.20f, 1f)

            (coneSpot + coreHotspot) * attenuation * battSag * flicker * 2.1f
        } else {
            0f
        }

        var total = trueDarkness + flashlight
        if (side == 1) total *= 0.82f // subtle shadow on perpendicular walls
        return total.coerceIn(0.02f, 1.0f)
    }

    private fun getDetailedWallColor(
        wallType: Int,
        texX: Float,
        light: Float,
        chapter: Chapter,
        side: Int
    ): Int {
        val (rBase, gBase, bBase) = when (wallType) {
            1 -> {
                val brickRow = (texX * 12).toInt() % 2 == 0
                val mortar = (texX * 12 - (texX * 12).toInt()) < 0.08f
                if (mortar) Triple(60, 58, 55)
                else if (brickRow) Triple(115, 85, 75)
                else Triple(95, 70, 60)
            }
            2 -> {
                val tileGrout = (texX * 14 - (texX * 14).toInt()) < 0.06f
                val bloodStain = (texX in 0.45f..0.65f)
                if (tileGrout) Triple(50, 60, 55)
                else if (bloodStain) Triple(140, 50, 45)
                else Triple(130, 150, 140)
            }
            3 -> {
                val barkGroove = (texX * 16).toInt() % 3 == 0
                if (barkGroove) Triple(60, 45, 30) else Triple(85, 65, 45)
            }
            4 -> {
                val hazardStripe = ((texX * 8).toInt() % 2 == 0)
                val panelSeam = (texX * 8 - (texX * 8).toInt()) < 0.07f
                if (panelSeam) Triple(40, 42, 48)
                else if (hazardStripe) Triple(185, 155, 30)
                else Triple(85, 90, 100)
            }
            5 -> {
                val vein = (texX in 0.3f..0.38f || texX in 0.75f..0.82f)
                if (vein) Triple(180, 50, 140) else Triple(60, 15, 45)
            }
            6 -> {
                Triple(75, 82, 95)
            }
            else -> Triple(80, 80, 80)
        }

        val (cr, cg, cb) = when (chapter) {
            Chapter.DEAD_CITY -> Triple((rBase * 0.85f).toInt(), (gBase * 0.95f).toInt(), (bBase * 1.15f).toInt())
            Chapter.BLACK_HOSPITAL -> Triple((rBase * 0.8f).toInt(), (gBase * 1.2f).toInt(), (bBase * 0.9f).toInt())
            Chapter.ENDLESS_FOREST -> Triple((rBase * 0.75f).toInt(), (gBase * 0.85f).toInt(), (bBase * 0.7f).toInt())
            Chapter.ECLIPSE_FACILITY -> Triple((rBase * 1.1f).toInt(), (gBase * 0.85f).toInt(), (bBase * 0.9f).toInt())
            Chapter.INVERTED_WORLD -> Triple((rBase * 1.35f).toInt(), (gBase * 0.45f).toInt(), (bBase * 1.25f).toInt())
        }

        val r = (cr * light).toInt().coerceIn(0, 255)
        val g = (cg * light).toInt().coerceIn(0, 255)
        val b = (cb * light).toInt().coerceIn(0, 255)

        return Color.rgb(r, g, b)
    }

    private fun renderSprites(
        canvas: Canvas,
        w: Float,
        h: Float,
        player: Player,
        sprites: List<BillboardSprite>,
        dirX: Float,
        dirY: Float,
        planeX: Float,
        planeY: Float,
        pitchOffset: Float
    ) {
        val sorted = sprites.sortedByDescending { it.distance }

        for (sprite in sorted) {
            val spriteX = sprite.x - player.x
            val spriteY = sprite.y - player.y

            val invDet = 1.0f / (planeX * dirY - dirX * planeY)
            val transformX = invDet * (dirY * spriteX - dirX * spriteY)
            val transformY = invDet * (-planeY * spriteX + planeX * spriteY)

            if (transformY <= 0.2f) continue

            val spriteScreenX = ((w / 2f) * (1f + transformX / transformY)).toInt()
            val spriteHeight = abs((h / transformY).toInt())
            val spriteWidth = abs((h / transformY).toInt())

            val drawStartY = ((-spriteHeight / 2f + h / 2f) + pitchOffset).toInt()
            val drawStartX = (spriteScreenX - spriteWidth / 2f).toInt()
            val drawEndX = (spriteScreenX + spriteWidth / 2f).toInt()

            val dist = transformY

            var visibleCols = 0
            for (stripe in drawStartX until drawEndX) {
                if (stripe in 0 until w.toInt()) {
                    if (dist < zBuffer[stripe]) {
                        visibleCols++
                    }
                }
            }
            if (visibleCols == 0) continue

            val spriteLight = calculateSpriteLighting(dist, player)

            when (sprite.type) {
                "monster_observer" -> drawTheObserver(canvas, spriteScreenX.toFloat(), drawStartY.toFloat(), spriteWidth.toFloat(), spriteHeight.toFloat(), spriteLight, sprite.data as? Monster)
                "monster_hunter" -> drawSoundHunter(canvas, spriteScreenX.toFloat(), drawStartY.toFloat(), spriteWidth.toFloat(), spriteHeight.toFloat(), spriteLight, sprite.data as? Monster)
                "monster_lurker" -> drawTheLurker(canvas, spriteScreenX.toFloat(), drawStartY.toFloat(), spriteWidth.toFloat(), spriteHeight.toFloat(), spriteLight)
                "monster_sentinel" -> drawFacilitySentinel(canvas, spriteScreenX.toFloat(), drawStartY.toFloat(), spriteWidth.toFloat(), spriteHeight.toFloat(), spriteLight)
                "hospital_bed" -> drawHospitalBed(canvas, spriteScreenX.toFloat(), drawStartY.toFloat(), spriteWidth.toFloat(), spriteHeight.toFloat(), spriteLight)
                "curtain" -> drawMedicalCurtain(canvas, spriteScreenX.toFloat(), drawStartY.toFloat(), spriteWidth.toFloat(), spriteHeight.toFloat(), spriteLight)
                "supply_cart" -> drawSupplyCart(canvas, spriteScreenX.toFloat(), drawStartY.toFloat(), spriteWidth.toFloat(), spriteHeight.toFloat(), spriteLight)
                "exit_sign" -> drawExitSign(canvas, spriteScreenX.toFloat(), drawStartY.toFloat(), spriteWidth.toFloat(), spriteHeight.toFloat())
                "blood_stain" -> drawBloodStain(canvas, spriteScreenX.toFloat(), drawStartY.toFloat(), spriteWidth.toFloat(), spriteHeight.toFloat(), spriteLight)
                "pickup_key" -> drawPickupKey(canvas, spriteScreenX.toFloat(), drawStartY.toFloat(), spriteWidth.toFloat(), spriteHeight.toFloat(), spriteLight)
                "pickup_battery" -> drawPickupBattery(canvas, spriteScreenX.toFloat(), drawStartY.toFloat(), spriteWidth.toFloat(), spriteHeight.toFloat(), spriteLight)
                "pickup_note" -> drawPickupNote(canvas, spriteScreenX.toFloat(), drawStartY.toFloat(), spriteWidth.toFloat(), spriteHeight.toFloat(), spriteLight)
                "door" -> drawDoorMarker(canvas, spriteScreenX.toFloat(), drawStartY.toFloat(), spriteWidth.toFloat(), spriteHeight.toFloat(), spriteLight)
                "locker" -> drawHighQualityLocker(canvas, spriteScreenX.toFloat(), drawStartY.toFloat(), spriteWidth.toFloat(), spriteHeight.toFloat(), spriteLight)
                "puzzle_terminal" -> drawPuzzleTerminal(canvas, spriteScreenX.toFloat(), drawStartY.toFloat(), spriteWidth.toFloat(), spriteHeight.toFloat(), spriteLight)
            }
        }
    }

    private fun calculateSpriteLighting(dist: Float, player: Player): Float {
        val ambient = 0.05f
        val flash = if (player.flashlightOn && player.batteryPercent > 0) {
            (1.0f / (1.0f + 0.11f * dist + 0.03f * dist * dist)) * 1.8f
        } else 0f
        return (ambient + flash).coerceIn(0.04f, 1.0f)
    }

    // --- REALISTIC FIRST-PERSON HANDS & TACTICAL FLASHLIGHT ---

    private fun renderRealisticPlayerHands(
        canvas: Canvas,
        w: Float,
        h: Float,
        player: Player,
        headBobOffset: Float,
        flicker: Float
    ) {
        val sprintBobMul = if (player.isSprinting) 1.8f else 1.0f
        val timeNow = System.currentTimeMillis()
        val fearTremor = if (player.sanity < 45f) (sin(timeNow * 0.045f) * 4.0f) else 0f

        val bobX = (sin(headBobOffset * 0.14f) * 8f * sprintBobMul) + fearTremor
        val bobY = (headBobOffset * 1.5f * sprintBobMul) + (fearTremor * 0.5f)
        val crouchOffset = if (player.isCrouching) 38f else 0f

        // -------------------------------------------------------------
        // A. REALISTIC LEFT HAND (Survivor bracing / balance)
        // -------------------------------------------------------------
        val leftBaseX = w * 0.14f - (bobX * 0.7f)
        val leftBaseY = h * 0.82f - (bobY * 0.6f) + (if (player.isCrouching) -15f else 25f)

        // Left Sleeve
        val leftSleevePath = Path().apply {
            moveTo(-20f, h)
            lineTo(leftBaseX + 45f, h)
            lineTo(leftBaseX + 35f, leftBaseY + 40f)
            lineTo(-20f, leftBaseY + 70f)
            close()
        }
        handPaint.shader = LinearGradient(
            0f, leftBaseY,
            leftBaseX + 40f, h,
            Color.rgb(26, 28, 34),
            Color.rgb(10, 12, 16),
            Shader.TileMode.CLAMP
        )
        canvas.drawPath(leftSleevePath, handPaint)

        // Left Hand Palm / Wrist
        handPaint.shader = LinearGradient(
            leftBaseX - 10f, leftBaseY - 10f,
            leftBaseX + 35f, leftBaseY + 35f,
            Color.rgb(78, 62, 54),
            Color.rgb(44, 34, 30),
            Shader.TileMode.CLAMP
        )
        canvas.drawRoundRect(RectF(leftBaseX - 12f, leftBaseY - 5f, leftBaseX + 38f, leftBaseY + 45f), 14f, 14f, handPaint)

        // Left Hand Curled Fingers (Bracing in the dark)
        handPaint.color = Color.rgb(88, 70, 60)
        for (i in 0..3) {
            val fingerY = leftBaseY - 2f + (i * 12f)
            canvas.drawRoundRect(RectF(leftBaseX + 22f, fingerY, leftBaseX + 48f, fingerY + 10f), 5f, 5f, handPaint)
        }
        // Left Thumb
        canvas.drawRoundRect(RectF(leftBaseX + 4f, leftBaseY - 18f, leftBaseX + 24f, leftBaseY - 2f), 6f, 6f, handPaint)

        // -------------------------------------------------------------
        // B. REALISTIC RIGHT HAND & HEAVY TACTICAL FLASHLIGHT
        // -------------------------------------------------------------
        if (handBitmap != null) {
            val bmpW = handBitmap!!.width.toFloat()
            val bmpH = handBitmap!!.height.toFloat()
            val targetW = w * 0.48f
            val targetH = targetW * (bmpH / bmpW)

            val drawX = w - targetW + bobX
            val drawY = h - targetH + bobY + crouchOffset

            val lensX = drawX + targetW * 0.22f
            val lensY = drawY + targetH * 0.35f

            // Volumetric spotlight cone radiating forward
            if (player.flashlightOn && player.batteryPercent > 0) {
                val beamPath = Path().apply {
                    moveTo(lensX, lensY)
                    lineTo(-w * 0.15f, h * 0.20f)
                    lineTo(w * 0.60f, -h * 0.12f)
                    close()
                }
                val isLowBat = player.batteryPercent < 25f
                val beamColor = if (isLowBat) Color.argb((40 * flicker).toInt(), 255, 220, 160) else Color.argb((45 * flicker).toInt(), 255, 250, 230)
                lightConePaint.shader = LinearGradient(
                    lensX, lensY,
                    w * 0.35f, h * 0.20f,
                    beamColor,
                    Color.TRANSPARENT,
                    Shader.TileMode.CLAMP
                )
                canvas.drawPath(beamPath, lightConePaint)
            }

            // Draw the photorealistic hand bitmap
            canvas.drawBitmap(handBitmap!!, null, RectF(drawX, drawY, drawX + targetW, drawY + targetH), null)

            // Bezel lens flare
            if (player.flashlightOn && player.batteryPercent > 0) {
                val flarePaint = Paint().apply {
                    isAntiAlias = true
                    shader = RadialGradient(
                        lensX, lensY, 32f,
                        Color.argb((185 * flicker).toInt(), 255, 252, 220),
                        Color.TRANSPARENT,
                        Shader.TileMode.CLAMP
                    )
                }
                canvas.drawCircle(lensX, lensY, 32f, flarePaint)
            }
        } else {
            // Procedural ultra-realistic hand & flashlight
            val handBaseX = w * 0.72f + bobX
            val handBaseY = h * 0.74f + bobY + crouchOffset

            val torchLensX = handBaseX - 58f
            val torchLensY = handBaseY - 22f

            // 1. Volumetric Light Cone
            if (player.flashlightOn && player.batteryPercent > 0) {
                val beamPath = Path().apply {
                    moveTo(torchLensX, torchLensY)
                    lineTo(0f, -h * 0.25f)
                    lineTo(w * 0.85f, -h * 0.25f)
                    close()
                }
                val beamAlpha = (38 * flicker).toInt().coerceIn(10, 55)
                val isLowBat = player.batteryPercent < 25f
                val coneColor = if (isLowBat) Color.argb(beamAlpha, 255, 215, 140) else Color.argb(beamAlpha, 255, 250, 225)
                lightConePaint.shader = LinearGradient(
                    torchLensX, torchLensY,
                    w * 0.42f, 0f,
                    coneColor,
                    Color.TRANSPARENT,
                    Shader.TileMode.CLAMP
                )
                canvas.drawPath(beamPath, lightConePaint)
            }

            // 2. Tactical Sleeve (Combat Jacket)
            val sleevePath = Path().apply {
                moveTo(handBaseX + 55f, h)
                lineTo(w * 1.05f, h)
                lineTo(w * 1.05f, handBaseY + 50f)
                lineTo(handBaseX + 30f, handBaseY + 25f)
                close()
            }
            handPaint.shader = LinearGradient(
                handBaseX, handBaseY,
                w, h,
                Color.rgb(24, 26, 32),
                Color.rgb(10, 11, 14),
                Shader.TileMode.CLAMP
            )
            canvas.drawPath(sleevePath, handPaint)

            // Sleeve Cuff Border
            handPaint.color = Color.rgb(38, 42, 50)
            canvas.drawRoundRect(RectF(handBaseX + 22f, handBaseY + 20f, handBaseX + 48f, handBaseY + 70f), 6f, 6f, handPaint)

            // 3. Survivor Hand & Wrist Anatomy
            handPaint.shader = LinearGradient(
                handBaseX - 25f, handBaseY - 20f,
                handBaseX + 38f, handBaseY + 45f,
                Color.rgb(78, 62, 54),
                Color.rgb(42, 32, 28),
                Shader.TileMode.CLAMP
            )
            canvas.drawRoundRect(RectF(handBaseX - 24f, handBaseY - 14f, handBaseX + 38f, handBaseY + 44f), 12f, 12f, handPaint)

            // 4. Heavy Aircraft-Grade Aluminum Flashlight Body
            // Cylinder Body
            handPaint.shader = LinearGradient(
                handBaseX - 60f, handBaseY - 45f,
                handBaseX + 25f, handBaseY + 15f,
                Color.rgb(45, 50, 58),
                Color.rgb(16, 18, 22),
                Shader.TileMode.CLAMP
            )
            canvas.drawRoundRect(RectF(handBaseX - 58f, handBaseY - 36f, handBaseX + 22f, handBaseY + 14f), 8f, 8f, handPaint)

            // Diamond-Knurling Gripping Rings on Torch Body
            handPaint.color = Color.rgb(32, 36, 42)
            for (k in 0..4) {
                val kx = handBaseX - 44f + (k * 12f)
                canvas.drawRect(kx, handBaseY - 35f, kx + 6f, handBaseY + 13f, handPaint)
            }

            // Scalloped Stainless Bezel
            handPaint.color = Color.rgb(110, 120, 135)
            canvas.drawRoundRect(RectF(handBaseX - 74f, handBaseY - 44f, handBaseX - 48f, handBaseY + 4f), 6f, 6f, handPaint)

            // Lens Emitter / Reflector Face
            val emitterColor = if (player.flashlightOn && player.batteryPercent > 0) {
                if (player.batteryPercent < 25f) Color.rgb(255, 215, 120) else Color.rgb(255, 252, 235)
            } else {
                Color.rgb(35, 40, 48)
            }
            handPaint.color = emitterColor
            canvas.drawCircle(torchLensX, torchLensY, 14f, handPaint)

            // 5. Four Fingers Wrapped Firmly Around Torch
            handPaint.color = Color.rgb(84, 68, 58)
            for (f in 0..3) {
                val fingerY = handBaseY - 14f + (f * 14f)
                // Finger segment
                canvas.drawRoundRect(RectF(handBaseX - 42f, fingerY, handBaseX + 6f, fingerY + 12f), 6f, 6f, handPaint)
                // Fingernail shadow highlight
                handPaint.color = Color.rgb(105, 85, 75)
                canvas.drawCircle(handBaseX - 38f, fingerY + 6f, 3.2f, handPaint)
                handPaint.color = Color.rgb(84, 68, 58)
            }

            // Thumb pressed along top
            handPaint.color = Color.rgb(90, 72, 62)
            canvas.drawRoundRect(RectF(handBaseX - 22f, handBaseY - 32f, handBaseX + 16f, handBaseY - 12f), 7f, 7f, handPaint)
        }
    }

    // --- HOSPITAL CORRIDOR PROPS (MATCHING REFERENCE SCREENSHOT) ---

    private fun drawHospitalBed(canvas: Canvas, cx: Float, topY: Float, w: Float, h: Float, light: Float) {
        val bw = w * 0.95f
        val left = cx - bw / 2f
        val right = cx + bw / 2f
        val bedY = topY + h * 0.45f
        val bottomY = topY + h

        // Metal tubular bed rails
        spritePaint.color = Color.rgb((160 * light).toInt(), (170 * light).toInt(), (185 * light).toInt())
        spritePaint.strokeWidth = 4.5f
        canvas.drawRoundRect(RectF(left, bedY - h * 0.18f, left + bw * 0.15f, bottomY), 6f, 6f, spritePaint)
        canvas.drawRoundRect(RectF(right - bw * 0.15f, bedY - h * 0.18f, right, bottomY), 6f, 6f, spritePaint)

        // Mattress base
        spritePaint.color = Color.rgb((30 * light).toInt(), (42 * light).toInt(), (55 * light).toInt())
        canvas.drawRect(RectF(left, bedY, right, bedY + h * 0.22f), spritePaint)

        // White pillow on left
        spritePaint.color = Color.rgb((200 * light).toInt(), (205 * light).toInt(), (210 * light).toInt())
        canvas.drawRoundRect(RectF(left + 8f, bedY - 14f, left + bw * 0.32f, bedY + 8f), 6f, 6f, spritePaint)

        // Metal bed legs & caster wheels
        spritePaint.color = Color.rgb((90 * light).toInt(), (95 * light).toInt(), (105 * light).toInt())
        canvas.drawRect(left + 10f, bedY + h * 0.22f, left + 18f, bottomY - 8f, spritePaint)
        canvas.drawRect(right - 18f, bedY + h * 0.22f, right - 10f, bottomY - 8f, spritePaint)

        // Wheels
        spritePaint.color = Color.BLACK
        canvas.drawCircle(left + 14f, bottomY - 5f, 6f, spritePaint)
        canvas.drawCircle(right - 14f, bottomY - 5f, 6f, spritePaint)
        spritePaint.strokeWidth = 0f
    }

    private fun drawMedicalCurtain(canvas: Canvas, cx: Float, topY: Float, w: Float, h: Float, light: Float) {
        val cw = w * 0.65f
        val left = cx - cw / 2f
        val right = cx + cw / 2f

        // Blue hospital privacy curtain
        spritePaint.color = Color.rgb((35 * light).toInt(), (75 * light).toInt(), (115 * light).toInt())
        canvas.drawRect(RectF(left, topY, right, topY + h * 0.85f), spritePaint)

        // Vertical curtain folds
        spritePaint.color = Color.rgb((20 * light).toInt(), (50 * light).toInt(), (85 * light).toInt())
        for (i in 1..4) {
            val fx = left + (cw * i / 5f)
            canvas.drawLine(fx, topY, fx, topY + h * 0.85f, spritePaint)
        }
    }

    private fun drawSupplyCart(canvas: Canvas, cx: Float, topY: Float, w: Float, h: Float, light: Float) {
        val cw = w * 0.52f
        val left = cx - cw / 2f
        val right = cx + cw / 2f

        // Blue & white hospital medical cart
        spritePaint.color = Color.rgb((45 * light).toInt(), (90 * light).toInt(), (130 * light).toInt())
        canvas.drawRoundRect(RectF(left, topY + h * 0.35f, right, topY + h * 0.88f), 6f, 6f, spritePaint)

        // Drawer handles
        spritePaint.color = Color.rgb((180 * light).toInt(), (190 * light).toInt(), (200 * light).toInt())
        canvas.drawRect(cx - cw * 0.25f, topY + h * 0.48f, cx + cw * 0.25f, topY + h * 0.52f, spritePaint)
        canvas.drawRect(cx - cw * 0.25f, topY + h * 0.66f, cx + cw * 0.25f, topY + h * 0.70f, spritePaint)
    }

    private fun drawExitSign(canvas: Canvas, cx: Float, topY: Float, w: Float, h: Float) {
        val sw = w * 0.45f
        val sh = h * 0.22f

        // Red emergency EXIT sign in distance
        spritePaint.color = Color.rgb(20, 2, 2)
        canvas.drawRoundRect(RectF(cx - sw / 2, topY, cx + sw / 2, topY + sh), 4f, 4f, spritePaint)

        spritePaint.color = Color.rgb(255, 30, 30)
        canvas.drawRect(cx - sw * 0.4f, topY + sh * 0.2f, cx + sw * 0.4f, topY + sh * 0.8f, spritePaint)
    }

    private fun drawBloodStain(canvas: Canvas, cx: Float, topY: Float, w: Float, h: Float, light: Float) {
        val bw = w * 0.6f
        val bh = h * 0.3f
        spritePaint.color = Color.argb((190 * light).toInt(), 120, 15, 15)
        canvas.drawOval(RectF(cx - bw / 2, topY + h * 0.65f, cx + bw / 2, topY + h * 0.65f + bh), spritePaint)
    }

    // --- LOCKERS & OTHER PROPS ---

    private fun drawHighQualityLocker(canvas: Canvas, cx: Float, topY: Float, w: Float, h: Float, light: Float) {
        val lw = w * 0.52f
        val left = cx - lw / 2f
        val right = cx + lw / 2f
        val bottomY = topY + h

        spritePaint.color = Color.rgb((38 * light).toInt(), (44 * light).toInt(), (50 * light).toInt())
        canvas.drawRoundRect(RectF(left, topY + h * 0.04f, right, bottomY), 10f, 10f, spritePaint)

        spritePaint.color = Color.rgb((28 * light).toInt(), (32 * light).toInt(), (38 * light).toInt())
        canvas.drawRect(RectF(left + 6f, topY + h * 0.07f, right - 6f, bottomY - 6f), spritePaint)

        spritePaint.color = Color.BLACK
        val slatStartY = topY + h * 0.16f
        for (i in 0..5) {
            val sy = slatStartY + i * 14f
            canvas.drawRect(cx - lw * 0.32f, sy, cx + lw * 0.32f, sy + 5f, spritePaint)
        }

        spritePaint.color = Color.rgb((160 * light).toInt(), (170 * light).toInt(), (185 * light).toInt())
        canvas.drawRoundRect(RectF(right - lw * 0.28f, topY + h * 0.48f, right - lw * 0.12f, topY + h * 0.58f), 4f, 4f, spritePaint)
    }

    private fun renderRealisticLockerView(canvas: Canvas, w: Float, h: Float) {
        // 1. Dark Metal Locker Frame (Left, Right, Top, Bottom)
        lockerSlatPaint.color = Color.argb(250, 10, 12, 15)

        // Left Frame & Right Frame
        canvas.drawRect(0f, 0f, w * 0.16f, h, lockerSlatPaint)
        canvas.drawRect(w * 0.84f, 0f, w, h, lockerSlatPaint)

        // Top Hood & Bottom Base
        canvas.drawRect(0f, 0f, w, h * 0.22f, lockerSlatPaint)
        canvas.drawRect(0f, h * 0.78f, w, h, lockerSlatPaint)

        // 2. Horizontal Louvered Ventilation Slats with 3D Bevels
        val slitHeight = h * 0.085f
        val gapHeight = h * 0.045f

        val slatHighlightPaint = Paint().apply {
            color = Color.argb(70, 160, 180, 200)
            strokeWidth = 2.5f
            isAntiAlias = true
        }

        var curY = h * 0.22f
        for (i in 0..3) {
            val slatTop = curY + slitHeight
            val slatBottom = slatTop + gapHeight
            canvas.drawRect(w * 0.16f, slatTop, w * 0.84f, slatBottom, lockerSlatPaint)
            // Metallic top edge reflection on louver
            canvas.drawLine(w * 0.16f, slatTop, w * 0.84f, slatTop, slatHighlightPaint)
            curY += slitHeight + gapHeight
        }

        // 3. Right Inner Lock Latch Mechanism
        val latchPaint = Paint().apply {
            color = Color.rgb(45, 52, 60)
            isAntiAlias = true
        }
        canvas.drawRoundRect(RectF(w * 0.82f, h * 0.44f, w * 0.88f, h * 0.58f), 6f, 6f, latchPaint)
        latchPaint.color = Color.rgb(120, 130, 145)
        canvas.drawCircle(w * 0.85f, h * 0.51f, 7f, latchPaint)

        // 4. Claustrophobic Vignette & Condensation
        val vignette = Paint().apply {
            shader = RadialGradient(
                w / 2f, h / 2f, w * 0.55f,
                Color.TRANSPARENT,
                Color.argb(215, 6, 8, 10),
                Shader.TileMode.CLAMP
            )
        }
        canvas.drawRect(0f, 0f, w, h, vignette)
    }

    private fun renderVolumetricFlashlightDust(canvas: Canvas, w: Float, h: Float, bob: Float) {
        val time = System.currentTimeMillis() % 100000
        for (i in dustParticles.indices step 2) {
            val px = (dustParticles[i] + sin(time * 0.001f + i) * 20f) % (w * 0.5f) + w * 0.25f
            val py = (dustParticles[i + 1] + (time * 0.03f)) % (h * 0.6f) + h * 0.2f
            canvas.drawCircle(px, py + bob, 1.8f, dustMotePaint)
        }
    }

    private fun drawTheObserver(canvas: Canvas, cx: Float, topY: Float, w: Float, h: Float, light: Float, monster: Monster?) {
        val bodyW = w * 0.36f
        val left = cx - bodyW / 2f
        val right = cx + bodyW / 2f
        val bottomY = topY + h

        val isChasing = monster?.state == MonsterState.CHASING_PLAYER
        val alpha = (250 * light).toInt().coerceIn(120, 255)

        spritePaint.color = Color.argb(alpha / 2, if (isChasing) 110 else 12, 0, if (isChasing) 25 else 30)
        canvas.drawOval(RectF(left - w * 0.18f, topY, right + w * 0.18f, bottomY), spritePaint)

        spritePaint.color = Color.argb(alpha, 8, 6, 12)
        canvas.drawRoundRect(RectF(left, topY + h * 0.14f, right, bottomY), 18f, 18f, spritePaint)

        val headRadius = w * 0.17f
        val headCenterY = topY + h * 0.11f
        canvas.drawCircle(cx, headCenterY, headRadius, spritePaint)

        val eyeColor = if (isChasing) Color.rgb(255, 20, 20) else Color.rgb(240, 248, 255)
        spritePaint.color = eyeColor
        val eyeSpacing = headRadius * 0.46f
        val eyeY = headCenterY - headRadius * 0.1f
        val eyeR = headRadius * 0.2f

        canvas.drawCircle(cx - eyeSpacing, eyeY, eyeR, spritePaint)
        canvas.drawCircle(cx + eyeSpacing, eyeY, eyeR, spritePaint)

        spritePaint.color = Color.argb(alpha, 6, 4, 10)
        spritePaint.strokeWidth = w * 0.065f
        canvas.drawLine(left + w * 0.05f, topY + h * 0.22f, left - w * 0.16f, bottomY - h * 0.08f, spritePaint)
        canvas.drawLine(right - w * 0.05f, topY + h * 0.22f, right + w * 0.16f, bottomY - h * 0.08f, spritePaint)
        spritePaint.strokeWidth = 0f
    }

    private fun drawSoundHunter(canvas: Canvas, cx: Float, topY: Float, w: Float, h: Float, light: Float, monster: Monster?) {
        val left = cx - w * 0.3f
        val right = cx + w * 0.3f
        val bottomY = topY + h

        spritePaint.color = Color.argb(230, (115 * light).toInt(), (35 * light).toInt(), (35 * light).toInt())
        canvas.drawOval(RectF(left, topY + h * 0.28f, right, bottomY), spritePaint)

        spritePaint.color = Color.argb(245, (135 * light).toInt(), (25 * light).toInt(), (25 * light).toInt())
        canvas.drawCircle(cx, topY + h * 0.20f, w * 0.2f, spritePaint)

        spritePaint.color = Color.WHITE
        canvas.drawCircle(cx, topY + h * 0.23f, w * 0.07f, spritePaint)
    }

    private fun drawTheLurker(canvas: Canvas, cx: Float, topY: Float, w: Float, h: Float, light: Float) {
        val left = cx - w * 0.25f
        val right = cx + w * 0.25f
        val bottomY = topY + h
        spritePaint.color = Color.argb(240, (75 * light).toInt(), (85 * light).toInt(), (105 * light).toInt())
        canvas.drawOval(RectF(left, topY + h * 0.42f, right, bottomY), spritePaint)

        spritePaint.color = Color.argb(255, (135 * light).toInt(), (140 * light).toInt(), (150 * light).toInt())
        canvas.drawCircle(cx, topY + h * 0.39f, w * 0.17f, spritePaint)
    }

    private fun drawFacilitySentinel(canvas: Canvas, cx: Float, topY: Float, w: Float, h: Float, light: Float) {
        val left = cx - w * 0.35f
        val right = cx + w * 0.35f
        val bottomY = topY + h
        spritePaint.color = Color.argb(240, (55 * light).toInt(), (60 * light).toInt(), (75 * light).toInt())
        canvas.drawRect(RectF(left, topY + h * 0.18f, right, bottomY), spritePaint)

        spritePaint.color = Color.RED
        canvas.drawRect(cx - w * 0.22f, topY + h * 0.14f, cx + w * 0.22f, topY + h * 0.21f, spritePaint)
    }

    private fun drawPickupKey(canvas: Canvas, cx: Float, topY: Float, w: Float, h: Float, light: Float) {
        val centerY = topY + h * 0.65f
        val r = w * 0.16f
        spritePaint.color = Color.argb(130, 255, 215, 0)
        canvas.drawCircle(cx, centerY, r * 1.8f, spritePaint)

        spritePaint.color = Color.rgb((255 * light).toInt(), (210 * light).toInt(), (40 * light).toInt())
        canvas.drawCircle(cx, centerY - r * 0.6f, r * 0.7f, spritePaint)
        canvas.drawRect(cx - r * 0.2f, centerY - r * 0.4f, cx + r * 0.2f, centerY + r * 1.2f, spritePaint)
        canvas.drawRect(cx, centerY + r * 0.6f, cx + r * 0.7f, centerY + r * 0.9f, spritePaint)
    }

    private fun drawPickupBattery(canvas: Canvas, cx: Float, topY: Float, w: Float, h: Float, light: Float) {
        val centerY = topY + h * 0.65f
        val bw = w * 0.26f
        val bh = h * 0.40f
        spritePaint.color = Color.argb(110, 0, 255, 120)
        canvas.drawCircle(cx, centerY, bw * 1.4f, spritePaint)

        spritePaint.color = Color.rgb((30 * light).toInt(), (225 * light).toInt(), (95 * light).toInt())
        canvas.drawRoundRect(RectF(cx - bw / 2, centerY - bh / 2, cx + bw / 2, centerY + bh / 2), 6f, 6f, spritePaint)
        spritePaint.color = Color.WHITE
        canvas.drawRect(cx - bw * 0.2f, centerY - bh / 2 - 6f, cx + bw * 0.2f, centerY - bh / 2, spritePaint)
    }

    private fun drawPickupNote(canvas: Canvas, cx: Float, topY: Float, w: Float, h: Float, light: Float) {
        val centerY = topY + h * 0.65f
        val nw = w * 0.32f
        val nh = h * 0.36f
        spritePaint.color = Color.rgb((215 * light).toInt(), (205 * light).toInt(), (175 * light).toInt())
        canvas.drawRect(cx - nw / 2, centerY - nh / 2, cx + nw / 2, centerY + nh / 2, spritePaint)

        spritePaint.color = Color.rgb((80 * light).toInt(), (50 * light).toInt(), (40 * light).toInt())
        canvas.drawLine(cx - nw * 0.35f, centerY - nh * 0.2f, cx + nw * 0.35f, centerY - nh * 0.2f, spritePaint)
        canvas.drawLine(cx - nw * 0.35f, centerY, cx + nw * 0.35f, centerY, spritePaint)
    }

    private fun drawDoorMarker(canvas: Canvas, cx: Float, topY: Float, w: Float, h: Float, light: Float) {
        val dw = w * 0.6f
        spritePaint.color = Color.rgb((80 * light).toInt(), (90 * light).toInt(), (105 * light).toInt())
        canvas.drawRoundRect(RectF(cx - dw / 2, topY + h * 0.08f, cx + dw / 2, topY + h), 8f, 8f, spritePaint)
        spritePaint.color = Color.YELLOW
        canvas.drawCircle(cx + dw * 0.3f, topY + h * 0.55f, dw * 0.08f, spritePaint)
    }

    private fun drawPuzzleTerminal(canvas: Canvas, cx: Float, topY: Float, w: Float, h: Float, light: Float) {
        val tw = w * 0.42f
        spritePaint.color = Color.rgb((30 * light).toInt(), (35 * light).toInt(), (42 * light).toInt())
        canvas.drawRect(cx - tw / 2, topY + h * 0.3f, cx + tw / 2, topY + h * 0.85f, spritePaint)
        spritePaint.color = Color.rgb(0, 220, 255)
        canvas.drawRect(cx - tw * 0.35f, topY + h * 0.38f, cx + tw * 0.35f, topY + h * 0.55f, spritePaint)
    }

    private fun renderWeatherParticles(canvas: Canvas, w: Float, h: Float, chapter: Chapter) {
        when (chapter) {
            Chapter.DEAD_CITY, Chapter.ENDLESS_FOREST -> {
                val time = System.currentTimeMillis() % 100000
                for (i in rainParticles.indices step 2) {
                    val rx = (rainParticles[i] + (time * 0.16f)) % w
                    val ry = (rainParticles[i + 1] + (time * 0.88f)) % h
                    canvas.drawLine(rx, ry, rx - 9f, ry + 28f, rainPaint)
                }
            }
            Chapter.INVERTED_WORLD -> {
                val time = System.currentTimeMillis() % 100000
                spritePaint.color = Color.argb(165, 225, 45, 185)
                for (i in voidMotes.indices step 2) {
                    val mx = (voidMotes[i] + sin(time * 0.002f + i) * 32f) % w
                    var my = (voidMotes[i + 1] - (time * 0.11f)) % h
                    if (my < 0) my += h
                    canvas.drawCircle(mx, my, 3.8f, spritePaint)
                }
            }
            else -> {}
        }
    }
}
