package com.example.eclipse.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.eclipse.model.Language
import com.example.ui.theme.EclipseBloodRed
import com.example.ui.theme.EclipseCrimson
import com.example.ui.theme.EclipseCrimsonGlow
import com.example.ui.theme.EclipseTextPrimary
import com.example.ui.theme.EclipseTextSecondary

@Composable
fun GameOverScreen(
    language: Language,
    onRetry: () -> Unit,
    onReturnToMenu: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isAr = language == Language.ARABIC

    val infiniteTransition = rememberInfiniteTransition(label = "game_over_pulse")
    val alphaPulse by infiniteTransition.animateFloat(
        initialValue = 0.5f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "go_alpha"
    )

    BackHandler {
        onReturnToMenu()
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.radialGradient(
                    colors = listOf(
                        EclipseBloodRed.copy(alpha = 0.6f),
                        Color(0xFF1B0306),
                        Color.Black
                    )
                )
            )
            .testTag("game_over_screen"),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.padding(28.dp)
        ) {
            Text(
                text = if (isAr) "ابتلعك الظلام..." else "THE DARKNESS CONSUMED YOU",
                color = EclipseCrimsonGlow,
                fontSize = 32.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = 4.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.alpha(alphaPulse)
            )

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = if (isAr) "استحوذ الكيان على آخر نبضات وعيك داخل مدينة الكسوف." else "The entity severed your consciousness inside the anomaly.",
                color = EclipseTextSecondary,
                fontSize = 14.sp,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(32.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                Button(
                    onClick = onRetry,
                    colors = ButtonDefaults.buttonColors(containerColor = EclipseCrimson),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.testTag("retry_checkpoint_button")
                ) {
                    Icon(Icons.Default.Refresh, "Retry")
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (isAr) "إعادة المحاولة" else "Retry Checkpoint",
                        fontWeight = FontWeight.Bold
                    )
                }

                OutlinedButton(
                    onClick = onReturnToMenu,
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = EclipseTextPrimary),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0x66FFFFFF)),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.testTag("game_over_menu_button")
                ) {
                    Text(
                        text = if (isAr) "القائمة الرئيسية" else "Main Menu",
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}
