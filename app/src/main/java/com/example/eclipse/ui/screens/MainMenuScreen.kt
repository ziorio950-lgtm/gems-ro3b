package com.example.eclipse.ui.screens

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.eclipse.model.GameSaveState
import com.example.eclipse.model.Language
import com.example.eclipse.model.SettingsState
import com.example.ui.theme.EclipseBloodRed
import com.example.ui.theme.EclipseBorder
import com.example.ui.theme.EclipseCrimson
import com.example.ui.theme.EclipseCrimsonGlow
import com.example.ui.theme.EclipseEerieCyan
import com.example.ui.theme.EclipseTextPrimary
import com.example.ui.theme.EclipseTextSecondary

@Composable
fun MainMenuScreen(
    saveState: GameSaveState,
    settings: SettingsState,
    onStartNewGame: () -> Unit,
    onContinueGame: () -> Unit,
    onSelectChapter: () -> Unit,
    onOpenHandbook: () -> Unit,
    onOpenSettings: () -> Unit,
    onOpenLoreArchive: () -> Unit,
    onOpenCredits: () -> Unit,
    onToggleLanguage: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isAr = settings.language == Language.ARABIC

    val infiniteTransition = rememberInfiniteTransition(label = "menu_glow")
    val titleGlow by infiniteTransition.animateFloat(
        initialValue = 0.6f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(2200, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glow_alpha"
    )

    Box(modifier = modifier.fillMaxSize()) {
        // 1. Cinematic Background Image
        Image(
            painter = painterResource(id = R.drawable.img_eclipse_menu),
            contentDescription = "Eclipse City Atmosphere",
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )

        // 2. Dark Atmospheric Horror Vignette Overlay
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.horizontalGradient(
                        colors = listOf(
                            Color(0xEE08090C),
                            Color(0xCC08090C),
                            Color(0x8808090C)
                        )
                    )
                )
        )

        // 3. Language Toggle & Version in Top Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "v1.0.0-PROD // OFFLINE READY",
                color = EclipseTextSecondary,
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium
            )

            OutlinedButton(
                onClick = onToggleLanguage,
                colors = ButtonDefaults.outlinedButtonColors(contentColor = EclipseEerieCyan),
                border = androidx.compose.foundation.BorderStroke(1.dp, EclipseEerieCyan.copy(alpha = 0.6f)),
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier.testTag("toggle_language_button")
            ) {
                Icon(
                    imageVector = Icons.Default.Language,
                    contentDescription = "Language",
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = if (isAr) "English" else "العربية",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        // 4. Main Menu Body
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(start = 36.dp, bottom = 28.dp, top = 20.dp),
            verticalArrangement = Arrangement.Center
        ) {
            // Game Titles
            Text(
                text = "ECLIPSE",
                color = EclipseCrimsonGlow,
                fontSize = 44.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = 6.sp,
                modifier = Modifier.alpha(titleGlow)
            )

            Text(
                text = if (isAr) "ما وراء الظلام — BEYOND THE DARKNESS" else "BEYOND THE DARKNESS",
                color = EclipseTextPrimary,
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                letterSpacing = 2.sp
            )

            Text(
                text = if (isAr) "لعبة رعب نفسي وبقاء من منظور الشخص الأول" else "First-Person Psychological Survival Horror",
                color = EclipseTextSecondary,
                fontSize = 12.sp,
                modifier = Modifier.padding(top = 4.dp, bottom = 24.dp)
            )

            // Buttons Column
            Column(
                modifier = Modifier.width(260.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Continue (if active save)
                if (saveState.hasActiveSave) {
                    Button(
                        onClick = onContinueGame,
                        colors = ButtonDefaults.buttonColors(containerColor = EclipseCrimson),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(44.dp)
                            .testTag("continue_game_button")
                    ) {
                        Text(
                            text = if (isAr) "متابعة التقدم (الفصل ${saveState.currentChapter})" else "Continue (Ch. ${saveState.currentChapter})",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                    }
                }

                // New Game
                Button(
                    onClick = onStartNewGame,
                    colors = ButtonDefaults.buttonColors(containerColor = if (saveState.hasActiveSave) EclipseEerieCyan else EclipseCrimson),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(44.dp)
                        .testTag("new_game_button")
                ) {
                    Text(
                        text = if (isAr) "بدء قصة جديدة" else "New Game",
                        color = if (saveState.hasActiveSave) Color.Black else Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                }

                // Chapters
                OutlinedButton(
                    onClick = onSelectChapter,
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = EclipseTextPrimary),
                    border = androidx.compose.foundation.BorderStroke(1.dp, EclipseBorder),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(42.dp)
                        .testTag("chapter_select_button")
                ) {
                    Text(
                        text = if (isAr) "المراحل والفصول (5 مناطق)" else "Chapters (5 Zones)",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                // Lore Archives
                OutlinedButton(
                    onClick = onOpenLoreArchive,
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = EclipseTextPrimary),
                    border = androidx.compose.foundation.BorderStroke(1.dp, EclipseBorder),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(42.dp)
                        .testTag("lore_archive_button")
                ) {
                    Text(
                        text = if (isAr) "الأرشيف والتسجيلات" else "Lore & Audio Logs",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                // Survival Handbook
                OutlinedButton(
                    onClick = onOpenHandbook,
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = EclipseEerieCyan),
                    border = androidx.compose.foundation.BorderStroke(1.dp, EclipseEerieCyan.copy(alpha = 0.6f)),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(42.dp)
                        .testTag("handbook_menu_button")
                ) {
                    Text(
                        text = if (isAr) "مذكرة النجاة الميدانية" else "Survival Field Guide",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                // Settings
                OutlinedButton(
                    onClick = onOpenSettings,
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = EclipseTextPrimary),
                    border = androidx.compose.foundation.BorderStroke(1.dp, EclipseBorder),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(42.dp)
                        .testTag("settings_button")
                ) {
                    Text(
                        text = if (isAr) "إعدادات اللعبة والرسومات" else "Settings & Graphics",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                // Credits & Unity Engine Architecture
                OutlinedButton(
                    onClick = onOpenCredits,
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = EclipseTextSecondary),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0x33262A34)),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(40.dp)
                        .testTag("credits_button")
                ) {
                    Text(
                        text = if (isAr) "القصة وهندسة المشروع" else "Story & Unity Engine Spec",
                        fontSize = 11.sp
                    )
                }
            }
        }
    }
}
