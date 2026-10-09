package com.example.eclipse.gameplay

import com.example.eclipse.engine.GameAudioEngine
import com.example.eclipse.engine.HapticsManager
import com.example.eclipse.model.BillboardSprite
import com.example.eclipse.model.Chapter
import com.example.eclipse.model.GameScreenState
import com.example.eclipse.model.HidingSpot
import com.example.eclipse.model.InventorySlot
import com.example.eclipse.model.ItemType
import com.example.eclipse.model.LoreNote
import com.example.eclipse.model.Monster
import com.example.eclipse.model.MonsterState
import com.example.eclipse.model.MonsterType
import com.example.eclipse.model.Player
import com.example.eclipse.model.WorldDoor
import com.example.eclipse.model.WorldPickup
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.hypot
import kotlin.math.sin

data class InteractionTarget(
    val type: String, // "door", "pickup", "hiding_enter", "hiding_exit", "note", "puzzle"
    val textEn: String,
    val textAr: String,
    val data: Any? = null
)

class GameSession(
    var currentChapter: Chapter,
    val audioEngine: GameAudioEngine,
    val hapticsManager: HapticsManager,
    val onGameOver: () -> Unit,
    val onChapterComplete: (Chapter) -> Unit
) {
    var levelData: ChapterLevelData = MapDefinitions.getLevelData(currentChapter)
    val player: Player = Player(
        x = levelData.playerStartX,
        y = levelData.playerStartY,
        angle = levelData.playerStartAngle
    )

    val monsterAI = MonsterAIManager()
    val inventory = mutableListOf<InventorySlot>()
    val solvedPuzzles = mutableSetOf<String>()
    val readLoreNotes = mutableSetOf<String>()

    var activeInteraction: InteractionTarget? = null
    var activeNoteToRead: LoreNote? = null
    var isKeypadOpen: Boolean = false
    var isCircuitOpen: Boolean = false
    var isValveOpen: Boolean = false

    var keypadState = KeypadPuzzleState()
    var circuitState = CircuitPuzzleState()
    var valveState = ValvePuzzleState()

    var headBobTimer: Float = 0f
    var headBobOffset: Float = 0f
    var flashlightFlicker: Float = 1.0f
    var bannerMessageEn: String? = null
    var bannerMessageAr: String? = null
    var bannerTimer: Float = 0f

    init {
        // Provide starter items if appropriate
        inventory.add(
            InventorySlot(
                id = "item_flash",
                type = ItemType.FLASHLIGHT_BATTERY,
                count = 1,
                titleEn = "Heavy Duty Torch",
                titleAr = "كشاف يدوي ثقيل",
                descriptionEn = "Essential for piercing the anomalous gloom. Toggle with the flashlight icon.",
                descriptionAr = "أداة أساسية لاختراق الظلام الكوني. يمكن تشغيله عبر أيقونة المصباح."
            )
        )
    }

    fun restartCurrentChapter() {
        levelData = MapDefinitions.getLevelData(currentChapter)
        player.x = levelData.playerStartX
        player.y = levelData.playerStartY
        player.angle = levelData.playerStartAngle
        player.pitch = 0f
        player.health = 100f
        player.stamina = 100f
        player.isHiding = false
        player.hidingSpotId = null
        player.sanity = 100f
        player.batteryPercent = 95f
        player.flashlightOn = true
    }

    fun loadChapter(chapter: Chapter) {
        currentChapter = chapter
        levelData = MapDefinitions.getLevelData(chapter)
        player.x = levelData.playerStartX
        player.y = levelData.playerStartY
        player.angle = levelData.playerStartAngle
        player.pitch = 0f
        player.health = 100f
        player.stamina = 100f
        player.isHiding = false
        player.hidingSpotId = null
        player.sanity = 100f
    }

    fun update(dt: Float, moveForward: Float, moveStrafe: Float, lookYaw: Float, lookPitch: Float) {
        if (player.health <= 0f) return

        // 1. Camera Look Rotation
        player.angle += lookYaw
        while (player.angle > 2 * PI) player.angle -= (2 * PI).toFloat()
        while (player.angle < 0) player.angle += (2 * PI).toFloat()

        player.pitch = (player.pitch + lookPitch).coerceIn(-0.45f, 0.45f)

        // 2. Movement & Stamina
        if (!player.isHiding) {
            handlePlayerMovement(dt, moveForward, moveStrafe)
        }

        // 3. Flashlight & Battery Drain
        if (player.flashlightOn && player.batteryPercent > 0) {
            player.batteryPercent = (player.batteryPercent - dt * 0.45f).coerceAtLeast(0f)
            // Occasional eerie flicker when battery is low or monster near
            val terrorDist = getClosestMonsterDistance()
            if (player.batteryPercent < 25f || terrorDist < 5.0f) {
                if (Math.random() < 0.08) {
                    flashlightFlicker = (0.3f + Math.random().toFloat() * 0.7f)
                } else {
                    flashlightFlicker = 1.0f
                }
            } else {
                flashlightFlicker = 1.0f
            }
        } else {
            flashlightFlicker = 0f
        }

        // 4. Monster AI update
        monsterAI.updateMonsters(
            monsters = levelData.monsters,
            player = player,
            map = levelData.map,
            dt = dt,
            onMonsterScreech = {
                audioEngine.playMonsterGrowl()
                hapticsManager.vibrateImpact()
            },
            onMonsterHitPlayer = { damage ->
                player.health = (player.health - damage).coerceAtLeast(0f)
                hapticsManager.vibrateImpact()
                if (player.health <= 0f) {
                    onGameOver()
                }
            }
        )

        // 5. Terror / Heartbeat Audio calculation
        val minDist = getClosestMonsterDistance()
        val terror = (1.0f - (minDist / 12.0f)).coerceIn(0f, 1f)
        audioEngine.terrorLevel = terror
        audioEngine.currentChapter = currentChapter
        audioEngine.playerStaminaRatio = (player.stamina / player.maxStamina).coerceIn(0f, 1f)
        audioEngine.isHidingInLocker = player.isHiding
        if (terror > 0.6f && Math.random() < 0.04) {
            hapticsManager.vibrateHeartbeat()
        }

        // 6. Interaction Raycast
        evaluateInteractionTarget()

        // 7. Check Level Exit Trigger
        val distToExit = hypot(player.x - levelData.exitTriggerX, player.y - levelData.exitTriggerY)
        if (distToExit < 1.2f) {
            val exitDoor = levelData.doors.firstOrNull { it.x == levelData.exitTriggerX.toInt() && it.y == levelData.exitTriggerY.toInt() }
            if (exitDoor == null || exitDoor.isOpen || !exitDoor.isLocked) {
                onChapterComplete(currentChapter)
            }
        }

        // 8. Timers
        if (bannerTimer > 0f) {
            bannerTimer -= dt
            if (bannerTimer <= 0f) {
                bannerMessageEn = null
                bannerMessageAr = null
            }
        }
    }

    private fun handlePlayerMovement(dt: Float, moveForward: Float, moveStrafe: Float) {
        val hasInput = abs(moveForward) > 0.05f || abs(moveStrafe) > 0.05f

        // Stamina drain/recovery
        if (player.isSprinting && hasInput && player.stamina > 0f && !player.isCrouching) {
            player.stamina = (player.stamina - dt * 22f).coerceAtLeast(0f)
            if (player.stamina <= 0f) player.isSprinting = false
            monsterAI.playerSprintPreferenceScore += dt * 0.1f
        } else if (!player.isSprinting) {
            player.stamina = (player.stamina + dt * 14f).coerceAtMost(player.maxStamina)
        }

        val baseSpeed = when {
            player.isCrouching -> 1.1f
            player.isSprinting && player.stamina > 0f -> 3.2f
            else -> 2.0f
        }

        // Head bobbing
        if (hasInput) {
            val bobFreq = if (player.isSprinting) 14f else 8f
            headBobTimer += dt * bobFreq
            headBobOffset = sin(headBobTimer) * (if (player.isSprinting) 12f else 6f)
            if (sin(headBobTimer) > 0.95f) {
                audioEngine.playFootstep(player.isSprinting)
            }
            player.noiseLevel = if (player.isCrouching) 0f else (if (player.isSprinting) 2.5f else 1.0f)
        } else {
            headBobOffset = 0f
            player.noiseLevel = 0f
        }

        // Forward and Strafe vectors
        val dirX = cos(player.angle)
        val dirY = sin(player.angle)
        val strafeX = -sin(player.angle)
        val strafeY = cos(player.angle)

        val totalMoveX = (dirX * moveForward + strafeX * moveStrafe) * baseSpeed * dt
        val totalMoveY = (dirY * moveForward + strafeY * moveStrafe) * baseSpeed * dt

        // Collision detection with walls & doors
        val newX = player.x + totalMoveX
        val newY = player.y + totalMoveY

        val padding = 0.28f
        if (canPlayerStandAt(newX + if (totalMoveX > 0) padding else -padding, player.y)) {
            player.x = newX
        }
        if (canPlayerStandAt(player.x, newY + if (totalMoveY > 0) padding else -padding)) {
            player.y = newY
        }
    }

    private fun canPlayerStandAt(x: Float, y: Float): Boolean {
        val ix = x.toInt()
        val iy = y.toInt()
        if (ix !in levelData.map.indices || iy !in levelData.map[0].indices) return false

        // Check if map wall
        if (levelData.map[ix][iy] > 0) return false

        // Check if closed locked door
        val door = levelData.doors.firstOrNull { it.x == ix && it.y == iy }
        if (door != null && !door.isOpen) return false

        return true
    }

    private fun evaluateInteractionTarget() {
        if (player.isHiding) {
            activeInteraction = InteractionTarget(
                type = "hiding_exit",
                textEn = "Exit Hiding Spot",
                textAr = "الخروج من المخبأ"
            )
            return
        }

        val lookAheadDist = 1.6f
        val frontX = player.x + cos(player.angle) * lookAheadDist
        val frontY = player.y + sin(player.angle) * lookAheadDist

        // 1. Check Pickups
        for (pickup in levelData.pickups) {
            if (!pickup.isCollected && hypot(player.x - pickup.x, player.y - pickup.y) < 1.6f) {
                activeInteraction = InteractionTarget(
                    type = "pickup",
                    textEn = "Pick up: ${pickup.titleEn}",
                    textAr = "التقاط: ${pickup.titleAr}",
                    data = pickup
                )
                return
            }
        }

        // 2. Check Lore Notes
        for (note in levelData.loreNotes) {
            val dist = hypot(player.x - 1.5f, player.y - 1.5f) // or note coordinate
            if (!note.isRead && dist < 1.6f) {
                activeInteraction = InteractionTarget(
                    type = "note",
                    textEn = if (note.isAudioTape) "Play Cassette Tape" else "Read Classified Note",
                    textAr = if (note.isAudioTape) "تشغيل شريط الكاسيت" else "قراءة المستند السري",
                    data = note
                )
                return
            }
        }

        // 3. Check Doors
        for (door in levelData.doors) {
            val dist = hypot(player.x - (door.x + 0.5f), player.y - (door.y + 0.5f))
            if (dist < 1.8f) {
                activeInteraction = if (door.isOpen) {
                    InteractionTarget("door_close", "Close Door", "إغلاق الباب", door)
                } else if (door.isLocked) {
                    val keyName = when (door.requiredItem) {
                        ItemType.BRASS_KEY -> "Iron Key"
                        ItemType.SURGICAL_KEYCARD -> "Keycard"
                        ItemType.VALVE_WRENCH -> "Valve Wrench"
                        ItemType.ELECTRICAL_FUSE -> "Power Fuse"
                        else -> "Key"
                    }
                    InteractionTarget("door_unlock", "Locked (Requires $keyName)", "مغلق (يتطلب مفتاحاً)", door)
                } else {
                    InteractionTarget("door_open", "Open Door", "فتح الباب", door)
                }
                return
            }
        }

        // 4. Check Hiding Spots
        for (spot in levelData.hidingSpots) {
            val dist = hypot(player.x - spot.x, player.y - spot.y)
            if (dist < 1.7f) {
                activeInteraction = InteractionTarget(
                    type = "hiding_enter",
                    textEn = "Hide in ${spot.nameEn}",
                    textAr = "اختبئ في ${spot.nameAr}",
                    data = spot
                )
                return
            }
        }

        // 5. Check Puzzle Terminals
        if (levelData.puzzleTerminalX != null && levelData.puzzleTerminalY != null) {
            val dist = hypot(player.x - levelData.puzzleTerminalX!!, player.y - levelData.puzzleTerminalY!!)
            if (dist < 1.8f) {
                val pType = when (currentChapter) {
                    Chapter.BLACK_HOSPITAL -> "keypad"
                    Chapter.ENDLESS_FOREST -> "valve"
                    Chapter.ECLIPSE_FACILITY -> "circuit"
                    else -> "keypad"
                }
                activeInteraction = InteractionTarget(
                    type = "puzzle_$pType",
                    textEn = "Operate Control Terminal",
                    textAr = "تشغيل وحدة التحكم",
                    data = pType
                )
                return
            }
        }

        activeInteraction = null
    }

    fun executeInteraction() {
        val target = activeInteraction ?: return

        when (target.type) {
            "pickup" -> {
                val pickup = target.data as? WorldPickup ?: return
                pickup.isCollected = true
                inventory.add(
                    InventorySlot(
                        id = pickup.id,
                        type = pickup.type,
                        titleEn = pickup.titleEn,
                        titleAr = pickup.titleAr,
                        descriptionEn = pickup.descriptionEn,
                        descriptionAr = pickup.descriptionAr
                    )
                )
                if (pickup.type == ItemType.FLASHLIGHT_BATTERY) {
                    player.batteryPercent = 100f
                    showBanner("Battery Replaced: 100%", "تم تبديل البطارية: 100%")
                    audioEngine.playBatteryReload()
                } else if (pickup.type == ItemType.SYRINGE_MEDKIT) {
                    player.health = (player.health + 50f).coerceAtMost(player.maxHealth)
                    showBanner("Medkit Applied: Health Restored", "تم استخدام الإسعاف واستعادة الصحة")
                } else {
                    showBanner("Acquired: ${pickup.titleEn}", "تم الحصول على: ${pickup.titleAr}")
                }
                audioEngine.playPickupSfx()
                hapticsManager.vibrateSubtle()
            }

            "hiding_enter" -> {
                val spot = target.data as? HidingSpot ?: return
                player.isHiding = true
                player.hidingSpotId = spot.id
                monsterAI.playerLockerHidesCount++
                showBanner("Hidden. Stay completely still.", "أنت في مخبأ. التزم الصمت والهدوء التام.")
                audioEngine.playDoorSfx()
                hapticsManager.vibrateSubtle()
            }

            "hiding_exit" -> {
                player.isHiding = false
                player.hidingSpotId = null
                audioEngine.playDoorSfx()
                hapticsManager.vibrateSubtle()
            }

            "door_open" -> {
                val door = target.data as? WorldDoor ?: return
                door.isOpen = true
                audioEngine.playDoorSfx()
                hapticsManager.vibrateSubtle()
            }

            "door_close" -> {
                val door = target.data as? WorldDoor ?: return
                door.isOpen = false
                audioEngine.playDoorSfx()
                hapticsManager.vibrateSubtle()
            }

            "door_unlock" -> {
                val door = target.data as? WorldDoor ?: return
                val hasKey = inventory.any { it.type == door.requiredItem }
                if (hasKey) {
                    door.isLocked = false
                    door.isOpen = true
                    audioEngine.playDoorSfx()
                    showBanner("Unlocked with key!", "تم فتح القفل بالمفتاح بنجاح!")
                    hapticsManager.vibrateImpact()
                } else {
                    showBanner("Door is firmly locked.", "الباب موصد بإحكام. ابحث عن المفتاح.")
                    hapticsManager.vibrateSubtle()
                }
            }

            "note" -> {
                val note = target.data as? LoreNote ?: return
                note.isRead = true
                readLoreNotes.add(note.id)
                activeNoteToRead = note
                audioEngine.playPickupSfx()
            }

            "puzzle_keypad" -> {
                isKeypadOpen = true
            }

            "puzzle_circuit" -> {
                isCircuitOpen = true
            }

            "puzzle_valve" -> {
                isValveOpen = true
            }
        }
    }

    fun toggleFlashlight() {
        if (player.batteryPercent > 0f) {
            player.flashlightOn = !player.flashlightOn
            audioEngine.playFlashlightClick()
            hapticsManager.vibrateSubtle()
        }
    }

    fun toggleCrouch() {
        player.isCrouching = !player.isCrouching
        hapticsManager.vibrateSubtle()
    }

    fun toggleSprint() {
        if (player.stamina > 10f && !player.isCrouching) {
            player.isSprinting = !player.isSprinting
        }
    }

    fun showBanner(en: String, ar: String) {
        bannerMessageEn = en
        bannerMessageAr = ar
        bannerTimer = 3.5f
    }

    fun getSprites(): List<BillboardSprite> {
        val list = mutableListOf<BillboardSprite>()

        // Monsters
        for (m in levelData.monsters) {
            val dist = hypot(m.x - player.x, m.y - player.y)
            val typeStr = when (m.type) {
                MonsterType.THE_OBSERVER -> "monster_observer"
                MonsterType.SOUND_HUNTER -> "monster_hunter"
                MonsterType.THE_LURKER -> "monster_lurker"
                MonsterType.FACILITY_SENTINEL -> "monster_sentinel"
                MonsterType.THE_MIMIC -> "monster_observer"
            }
            list.add(BillboardSprite(m.x, m.y, dist, typeStr, m))
        }

        // Pickups
        for (p in levelData.pickups) {
            if (!p.isCollected) {
                val dist = hypot(p.x - player.x, p.y - player.y)
                val typeStr = when (p.type) {
                    ItemType.FLASHLIGHT_BATTERY -> "pickup_battery"
                    ItemType.BRASS_KEY, ItemType.SURGICAL_KEYCARD, ItemType.VALVE_WRENCH, ItemType.ELECTRICAL_FUSE -> "pickup_key"
                    else -> "pickup_note"
                }
                list.add(BillboardSprite(p.x, p.y, dist, typeStr, p))
            }
        }

        // Lockers / Hiding Spots
        for (h in levelData.hidingSpots) {
            val dist = hypot(h.x - player.x, h.y - player.y)
            list.add(BillboardSprite(h.x, h.y, dist, "locker", h))
        }

        // Puzzle Terminal
        if (levelData.puzzleTerminalX != null && levelData.puzzleTerminalY != null) {
            val dist = hypot(levelData.puzzleTerminalX!! - player.x, levelData.puzzleTerminalY!! - player.y)
            list.add(BillboardSprite(levelData.puzzleTerminalX!!, levelData.puzzleTerminalY!!, dist, "puzzle_terminal"))
        }

        // World Props (Hospital Beds, Curtains, Carts, Signs)
        for (prop in levelData.props) {
            val dist = hypot(prop.x - player.x, prop.y - player.y)
            list.add(BillboardSprite(prop.x, prop.y, dist, prop.type, prop))
        }

        return list
    }

    private fun getClosestMonsterDistance(): Float {
        var minD = 999f
        for (m in levelData.monsters) {
            val d = hypot(m.x - player.x, m.y - player.y)
            if (d < minD) minD = d
        }
        return minD
    }
}
