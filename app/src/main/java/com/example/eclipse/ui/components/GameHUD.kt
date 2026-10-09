package com.example.eclipse.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BatteryChargingFull
import androidx.compose.material.icons.filled.DirectionsRun
import androidx.compose.material.icons.filled.FlashlightOff
import androidx.compose.material.icons.filled.FlashlightOn
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.TouchApp
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.Work
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.eclipse.gameplay.GameSession
import com.example.eclipse.model.Language
import com.example.eclipse.model.SettingsState
import com.example.ui.theme.EclipseBloodRed
import com.example.ui.theme.EclipseCrimson
import com.example.ui.theme.EclipseCrimsonGlow
import com.example.ui.theme.EclipseEerieCyan
import com.example.ui.theme.EclipseSurface
import com.example.ui.theme.EclipseTextPrimary
import com.example.ui.theme.EclipseWarningAmber

@Composable
fun GameHUD(
    session: GameSession,
    settings: SettingsState,
    onOpenInventory: () -> Unit,
    onOpenHandbook: () -> Unit,
    onPauseGame: () -> Unit,
    modifier: Modifier = Modifier
) {
    val player = session.player
    val isAr = settings.language == Language.ARABIC
    val scale = settings.buttonScale
    val layoutPreset = settings.buttonLayoutPreset

    val infiniteTransition = rememberInfiniteTransition(label = "hud_pulse")
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.35f,
        targetValue = 0.90f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 800),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_alpha"
    )

    Box(modifier = modifier.fillMaxSize()) {
        // 1. Horror Sanity / Terror Vignette when monster is near or health is low
        val terrorDist = session.audioEngine.terrorLevel
        if (terrorDist > 0.12f || player.health < 40f) {
            val vignetteAlpha = (terrorDist * 0.75f + if (player.health < 40f) (1f - player.health / 40f) * 0.5f else 0f).coerceIn(0f, 0.95f)
            Canvas(modifier = Modifier.fillMaxSize()) {
                drawRect(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            Color.Transparent,
                            EclipseBloodRed.copy(alpha = vignetteAlpha * 0.45f),
                            Color.Black.copy(alpha = vignetteAlpha)
                        )
                    )
                )
            }
        }

        // 2. Top Bar: Chapter Objective, Handbook button & Pause button
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Objective Banner
            val currentObj = session.levelData.objectives.firstOrNull { !it.isCompleted }
            Surface(
                color = EclipseSurface.copy(alpha = 0.85f),
                shape = RoundedCornerShape(12.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0x55262A34)),
                modifier = Modifier.weight(1f, fill = false)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Visibility,
                        contentDescription = "Objective",
                        tint = EclipseEerieCyan,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (isAr) (currentObj?.textAr ?: "استكشف المنطقة بحذر") else (currentObj?.textEn ?: "Explore with caution"),
                        color = EclipseTextPrimary,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        maxLines = 1
                    )
                }
            }

            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                // Survival Handbook Button (Paper in Hand)
                Surface(
                    color = EclipseSurface.copy(alpha = 0.88f),
                    shape = RoundedCornerShape(10.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, EclipseCrimson),
                    modifier = Modifier
                        .clickable { onOpenHandbook() }
                        .testTag("handbook_hud_button")
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.MenuBook,
                            contentDescription = "Handbook",
                            tint = EclipseCrimsonGlow,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (isAr) "مذكرة النجاة" else "FIELD GUIDE",
                            color = EclipseTextPrimary,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                // Pause Button
                Surface(
                    color = EclipseSurface.copy(alpha = 0.88f),
                    shape = CircleShape,
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0x66E53935)),
                    modifier = Modifier
                        .size(42.dp)
                        .clickable { onPauseGame() }
                        .testTag("pause_button")
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.Pause,
                            contentDescription = "Pause",
                            tint = EclipseTextPrimary,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }
            }
        }

        // 3. Dynamic Center Notification Banner
        AnimatedVisibility(
            visible = session.bannerMessageEn != null,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 64.dp)
        ) {
            Surface(
                color = Color(0xEE12151B),
                shape = RoundedCornerShape(8.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, EclipseCrimson)
            ) {
                Text(
                    text = if (isAr) (session.bannerMessageAr ?: "") else (session.bannerMessageEn ?: ""),
                    color = EclipseTextPrimary,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp)
                )
            }
        }

        // 4. Center Crosshair
        Box(
            modifier = Modifier.align(Alignment.Center),
            contentAlignment = Alignment.Center
        ) {
            Canvas(modifier = Modifier.size(14.dp)) {
                drawCircle(
                    color = if (session.activeInteraction != null) EclipseCrimson else Color(0x88FFFFFF),
                    radius = if (session.activeInteraction != null) 4.5f else 2.5f
                )
            }
        }

        // 5. Context Interaction Prompt
        session.activeInteraction?.let { target ->
            Surface(
                color = Color(0xDD1B0C11),
                shape = RoundedCornerShape(24.dp),
                border = androidx.compose.foundation.BorderStroke(1.5.dp, EclipseCrimsonGlow),
                modifier = Modifier
                    .align(Alignment.Center)
                    .padding(top = 70.dp)
                    .clickable { session.executeInteraction() }
                    .testTag("interaction_prompt_button")
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 18.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.TouchApp,
                        contentDescription = "Interact",
                        tint = EclipseCrimsonGlow,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (isAr) target.textAr else target.textEn,
                        color = Color.White,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        // 6. Bottom Vitals Status Panel (Health, Stamina, Flashlight Battery, Noise)
        val vitalsAlignment = if (layoutPreset == "SWAPPED") Alignment.BottomEnd else Alignment.BottomStart
        Column(
            modifier = Modifier
                .align(vitalsAlignment)
                .padding(
                    start = if (layoutPreset == "SWAPPED") 0.dp else 20.dp,
                    end = if (layoutPreset == "SWAPPED") 20.dp else 0.dp,
                    bottom = if (layoutPreset == "RAISED") 32.dp else 18.dp
                )
                .width(185.dp)
        ) {
            // Health Bar
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = if (isAr) "الصحة" else "HP",
                    color = EclipseCrimson,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.width(36.dp)
                )
                LinearProgressIndicator(
                    progress = { player.health / player.maxHealth },
                    modifier = Modifier
                        .height(7.dp)
                        .weight(1f)
                        .clip(RoundedCornerShape(3.dp)),
                    color = if (player.health < 30f) EclipseCrimson.copy(alpha = pulseAlpha) else EclipseCrimson,
                    trackColor = Color(0x44262A34),
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Stamina Bar
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = if (isAr) "التحمل" else "STM",
                    color = EclipseEerieCyan,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.width(36.dp)
                )
                LinearProgressIndicator(
                    progress = { player.stamina / player.maxStamina },
                    modifier = Modifier
                        .height(5.dp)
                        .weight(1f)
                        .clip(RoundedCornerShape(3.dp)),
                    color = EclipseEerieCyan,
                    trackColor = Color(0x44262A34),
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Flashlight Battery (Realistic Percentage and Voltage LED)
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = if (isAr) "البطارية" else "BAT",
                    color = EclipseWarningAmber,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.width(36.dp)
                )
                LinearProgressIndicator(
                    progress = { player.batteryPercent / 100f },
                    modifier = Modifier
                        .height(5.dp)
                        .weight(1f)
                        .clip(RoundedCornerShape(3.dp)),
                    color = if (player.batteryPercent < 20f) EclipseWarningAmber.copy(alpha = pulseAlpha) else EclipseWarningAmber,
                    trackColor = Color(0x44262A34),
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "${player.batteryPercent.toInt()}%",
                    color = if (player.batteryPercent < 20f) EclipseWarningAmber else Color(0xBBFFFFFF),
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Noise Meter
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.GraphicEq,
                    contentDescription = "Noise",
                    tint = if (player.noiseLevel > 1.5f) EclipseCrimson else Color(0xAAFFFFFF),
                    modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = when {
                        player.isHiding -> if (isAr) "مخفي داخل الخزانة" else "HIDDEN (SILENT)"
                        player.isCrouching -> if (isAr) "تسلل هادئ" else "STEALTH (0 NOISE)"
                        player.isSprinting -> if (isAr) "ركض: صوت عالٍ!" else "SPRINT (LOUD!)"
                        else -> if (isAr) "خطوات عادية" else "WALKING (NORMAL)"
                    },
                    color = if (player.isSprinting) EclipseCrimson else Color(0xBBFFFFFF),
                    fontSize = 10.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }

        // 7. Tactical Action Buttons (Flashlight, Crouch, Sprint, Bag)
        val bottomMargin = settings.buttonVerticalOffset.dp
        val opacity = settings.buttonOpacity
        val btnBaseSize = (52 * scale).dp
        val sprintBtnSize = (56 * scale).dp

        if (layoutPreset == "SPLIT") {
            // Split Layout: Left side has Flashlight and Bag; Right side has Crouch and Sprint
            // Left Group
            Row(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(start = 20.dp, bottom = bottomMargin),
                horizontalArrangement = Arrangement.spacedBy((10 * scale).dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                TacticalFlashlightButton(
                    player = player,
                    scale = scale,
                    size = btnBaseSize,
                    opacity = opacity,
                    onToggle = { session.toggleFlashlight() }
                )
                TacticalInventoryButton(
                    itemCount = session.inventory.size,
                    scale = scale,
                    size = btnBaseSize,
                    opacity = opacity,
                    onClick = onOpenInventory
                )
            }

            // Right Group
            Row(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(end = 20.dp, bottom = bottomMargin),
                horizontalArrangement = Arrangement.spacedBy((10 * scale).dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                TacticalCrouchButton(
                    player = player,
                    scale = scale,
                    size = btnBaseSize,
                    opacity = opacity,
                    isAr = isAr,
                    onToggle = { session.toggleCrouch() }
                )
                TacticalSprintButton(
                    player = player,
                    scale = scale,
                    size = sprintBtnSize,
                    opacity = opacity,
                    isAr = isAr,
                    onToggle = { session.toggleSprint() }
                )
            }
        } else {
            // Clustered Layout: Either BottomEnd (DEFAULT / RAISED) or BottomStart (SWAPPED)
            val actionsAlignment = if (layoutPreset == "SWAPPED") Alignment.BottomStart else Alignment.BottomEnd
            val extraBottom = if (layoutPreset == "RAISED") 16.dp else 0.dp

            Row(
                modifier = Modifier
                    .align(actionsAlignment)
                    .padding(
                        start = if (layoutPreset == "SWAPPED") 20.dp else 0.dp,
                        end = if (layoutPreset == "SWAPPED") 0.dp else 20.dp,
                        bottom = bottomMargin + extraBottom
                    ),
                horizontalArrangement = Arrangement.spacedBy((10 * scale).dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                TacticalFlashlightButton(
                    player = player,
                    scale = scale,
                    size = btnBaseSize,
                    opacity = opacity,
                    onToggle = { session.toggleFlashlight() }
                )
                TacticalCrouchButton(
                    player = player,
                    scale = scale,
                    size = btnBaseSize,
                    opacity = opacity,
                    isAr = isAr,
                    onToggle = { session.toggleCrouch() }
                )
                TacticalSprintButton(
                    player = player,
                    scale = scale,
                    size = sprintBtnSize,
                    opacity = opacity,
                    isAr = isAr,
                    onToggle = { session.toggleSprint() }
                )
                TacticalInventoryButton(
                    itemCount = session.inventory.size,
                    scale = scale,
                    size = btnBaseSize,
                    opacity = opacity,
                    onClick = onOpenInventory
                )
            }
        }

        // 8. Locker Exit / Peek Floating Prompt if Hiding
        if (player.isHiding) {
            Surface(
                color = Color(0xDD12151B),
                shape = RoundedCornerShape(20.dp),
                border = androidx.compose.foundation.BorderStroke(2.dp, EclipseCrimsonGlow),
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = bottomMargin + 70.dp)
                    .clickable {
                        player.isHiding = false
                        player.hidingSpotId = null
                        session.audioEngine.playDoorSfx()
                    }
                    .testTag("exit_locker_hud_button")
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 22.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.TouchApp,
                        contentDescription = "Exit Locker",
                        tint = EclipseCrimsonGlow,
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (isAr) "الخروج من الخزانة (تأكد من ابتعاد الوحش)" else "EXIT LOCKER (Ensure Coast is Clear)",
                        color = Color.White,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
private fun TacticalFlashlightButton(
    player: com.example.eclipse.model.Player,
    scale: Float,
    size: androidx.compose.ui.unit.Dp,
    opacity: Float,
    onToggle: () -> Unit
) {
    val isOn = player.flashlightOn && player.batteryPercent > 0
    val containerColor = if (isOn) Color(0xCC382808) else Color(0xBB14161C)
    val borderColor = if (isOn) EclipseWarningAmber else Color(0x555A6578)

    Surface(
        color = containerColor.copy(alpha = containerColor.alpha * opacity),
        shape = RoundedCornerShape((14 * scale).dp),
        border = androidx.compose.foundation.BorderStroke((2 * scale).dp, borderColor),
        modifier = Modifier
            .size(size)
            .clickable { onToggle() }
            .testTag("flashlight_button")
    ) {
        Box(contentAlignment = Alignment.Center) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = if (isOn) Icons.Default.FlashlightOn else Icons.Default.FlashlightOff,
                    contentDescription = "Flashlight",
                    tint = if (isOn) EclipseWarningAmber else Color(0xAAFFFFFF),
                    modifier = Modifier.size((22 * scale).dp)
                )
                Text(
                    text = if (isOn) "${player.batteryPercent.toInt()}%" else "OFF",
                    color = if (isOn) EclipseWarningAmber else Color(0x77FFFFFF),
                    fontSize = (9 * scale).sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
private fun TacticalCrouchButton(
    player: com.example.eclipse.model.Player,
    scale: Float,
    size: androidx.compose.ui.unit.Dp,
    opacity: Float,
    isAr: Boolean,
    onToggle: () -> Unit
) {
    val isCrouched = player.isCrouching
    val containerColor = if (isCrouched) Color(0xCC003840) else Color(0xBB14161C)
    val borderColor = if (isCrouched) EclipseEerieCyan else Color(0x555A6578)

    Surface(
        color = containerColor.copy(alpha = containerColor.alpha * opacity),
        shape = RoundedCornerShape((14 * scale).dp),
        border = androidx.compose.foundation.BorderStroke((2 * scale).dp, borderColor),
        modifier = Modifier
            .size(size)
            .clickable { onToggle() }
            .testTag("crouch_button")
    ) {
        Box(contentAlignment = Alignment.Center) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = if (isAr) "تسلل" else "SNEAK",
                    color = if (isCrouched) EclipseEerieCyan else Color.White,
                    fontSize = (11 * scale).sp,
                    fontWeight = FontWeight.ExtraBold
                )
                Text(
                    text = if (isCrouched) "0 dB" else "WALK",
                    color = if (isCrouched) EclipseEerieCyan else Color(0x77FFFFFF),
                    fontSize = (8 * scale).sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
private fun TacticalSprintButton(
    player: com.example.eclipse.model.Player,
    scale: Float,
    size: androidx.compose.ui.unit.Dp,
    opacity: Float,
    isAr: Boolean,
    onToggle: () -> Unit
) {
    val isSprinting = player.isSprinting
    val containerColor = if (isSprinting) Color(0xDD5A0C14) else Color(0xBB14161C)
    val borderColor = if (isSprinting) EclipseCrimsonGlow else Color(0x555A6578)

    Surface(
        color = containerColor.copy(alpha = containerColor.alpha * opacity),
        shape = RoundedCornerShape((16 * scale).dp),
        border = androidx.compose.foundation.BorderStroke((2 * scale).dp, borderColor),
        modifier = Modifier
            .size(size)
            .clickable { onToggle() }
            .testTag("sprint_button")
    ) {
        Box(contentAlignment = Alignment.Center) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = Icons.Default.DirectionsRun,
                    contentDescription = "Sprint",
                    tint = if (isSprinting) Color.White else Color(0xDDFFFFFF),
                    modifier = Modifier.size((24 * scale).dp)
                )
                Text(
                    text = if (isAr) "ركض" else "RUN",
                    color = if (isSprinting) Color.White else Color(0x99FFFFFF),
                    fontSize = (9 * scale).sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
private fun TacticalInventoryButton(
    itemCount: Int,
    scale: Float,
    size: androidx.compose.ui.unit.Dp,
    opacity: Float,
    onClick: () -> Unit
) {
    val containerColor = Color(0xBB14161C)
    val borderColor = if (itemCount > 0) EclipseEerieCyan else Color(0x555A6578)

    Surface(
        color = containerColor.copy(alpha = containerColor.alpha * opacity),
        shape = RoundedCornerShape((14 * scale).dp),
        border = androidx.compose.foundation.BorderStroke((2 * scale).dp, borderColor),
        modifier = Modifier
            .size(size)
            .clickable { onClick() }
            .testTag("inventory_button")
    ) {
        Box(contentAlignment = Alignment.Center) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Work,
                    contentDescription = "Inventory",
                    tint = EclipseEerieCyan,
                    modifier = Modifier.size((20 * scale).dp)
                )
                Text(
                    text = "$itemCount",
                    color = Color.White,
                    fontSize = (10 * scale).sp,
                    fontWeight = FontWeight.ExtraBold
                )
            }
        }
    }
}
