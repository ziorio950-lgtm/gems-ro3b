package com.example.eclipse.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.eclipse.model.Language
import com.example.ui.theme.EclipseBorder
import com.example.ui.theme.EclipseCrimson
import com.example.ui.theme.EclipseEerieCyan
import com.example.ui.theme.EclipseSurface
import com.example.ui.theme.EclipseTextMuted
import com.example.ui.theme.EclipseTextPrimary

@Composable
fun PauseDialog(
    language: Language,
    onResume: () -> Unit,
    onRestartCheckpoint: () -> Unit,
    onOpenSettings: () -> Unit,
    onQuitToMenu: () -> Unit
) {
    val isAr = language == Language.ARABIC

    Dialog(onDismissRequest = onResume) {
        Surface(
            modifier = Modifier
                .width(340.dp)
                .testTag("pause_dialog"),
            shape = RoundedCornerShape(16.dp),
            color = EclipseSurface,
            border = androidx.compose.foundation.BorderStroke(1.5.dp, EclipseBorder)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = if (isAr) "إيقاف مؤقت" else "GAME PAUSED",
                    color = EclipseTextPrimary,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )

                Text(
                    text = if (isAr) "الظلام ينتظرك في كل زاوية..." else "The darkness awaits beyond the light...",
                    color = EclipseTextMuted,
                    fontSize = 11.sp
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Resume
                Button(
                    onClick = onResume,
                    colors = ButtonDefaults.buttonColors(containerColor = EclipseEerieCyan),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("resume_button")
                ) {
                    Text(
                        text = if (isAr) "متابعة اللعب" else "Resume Game",
                        color = Color.Black,
                        fontWeight = FontWeight.Bold
                    )
                }

                // Restart Checkpoint
                OutlinedButton(
                    onClick = onRestartCheckpoint,
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = EclipseCrimson),
                    border = androidx.compose.foundation.BorderStroke(1.dp, EclipseCrimson),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("restart_checkpoint_button")
                ) {
                    Text(
                        text = if (isAr) "إعادة من نقطة الحفظ" else "Restart Checkpoint",
                        fontWeight = FontWeight.SemiBold
                    )
                }

                // Settings
                OutlinedButton(
                    onClick = onOpenSettings,
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = EclipseTextPrimary),
                    border = androidx.compose.foundation.BorderStroke(1.dp, EclipseBorder),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("pause_settings_button")
                ) {
                    Text(
                        text = if (isAr) "الإعدادات" else "Settings",
                        fontWeight = FontWeight.SemiBold
                    )
                }

                // Quit to Main Menu
                OutlinedButton(
                    onClick = onQuitToMenu,
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFE57373)),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0x33E57373)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("quit_to_menu_button")
                ) {
                    Text(
                        text = if (isAr) "الخروج للقائمة الرئيسية" else "Quit to Main Menu",
                        fontWeight = FontWeight.Normal
                    )
                }
            }
        }
    }
}
