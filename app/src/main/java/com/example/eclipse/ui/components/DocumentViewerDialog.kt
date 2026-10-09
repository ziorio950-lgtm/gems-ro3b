package com.example.eclipse.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.eclipse.model.Language
import com.example.eclipse.model.LoreNote
import com.example.ui.theme.EclipseBorder
import com.example.ui.theme.EclipseCrimson
import com.example.ui.theme.EclipseEerieCyan
import com.example.ui.theme.EclipseSurface
import com.example.ui.theme.EclipseSurfaceVariant
import com.example.ui.theme.EclipseTextMuted
import com.example.ui.theme.EclipseTextPrimary
import com.example.ui.theme.EclipseTextSecondary

@Composable
fun DocumentViewerDialog(
    note: LoreNote,
    language: Language,
    onDismiss: () -> Unit
) {
    val isAr = language == Language.ARABIC

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .fillMaxHeight(0.85f)
                .testTag("document_viewer_dialog"),
            shape = RoundedCornerShape(16.dp),
            color = EclipseSurface,
            border = androidx.compose.foundation.BorderStroke(1.5.dp, if (note.isAudioTape) EclipseEerieCyan else EclipseBorder)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(22.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = if (note.isAudioTape) Icons.Default.GraphicEq else Icons.Default.MenuBook,
                            contentDescription = "Lore icon",
                            tint = if (note.isAudioTape) EclipseEerieCyan else EclipseCrimson,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.size(10.dp))
                        Text(
                            text = if (isAr) (if (note.isAudioTape) "تسجيل صوتي سري" else "وثيقة سرية مصنفة") else (if (note.isAudioTape) "CLASSIFIED AUDIO LOG" else "CLASSIFIED DOSSIER"),
                            color = EclipseTextPrimary,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.testTag("close_document_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = EclipseTextSecondary
                        )
                    }
                }

                Text(
                    text = "${note.chapter.titleEn} // ${note.dateOrSource}",
                    color = EclipseTextMuted,
                    fontSize = 11.sp,
                    modifier = Modifier.padding(top = 4.dp, bottom = 12.dp)
                )

                // Document Body
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    color = EclipseSurfaceVariant,
                    shape = RoundedCornerShape(12.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0x33262A34))
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(18.dp)
                            .verticalScroll(rememberScrollState())
                    ) {
                        Text(
                            text = if (isAr) note.titleAr else note.titleEn,
                            color = if (note.isAudioTape) EclipseEerieCyan else Color(0xFFFFD54F),
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        Text(
                            text = if (isAr) note.contentAr else note.contentEn,
                            color = EclipseTextPrimary,
                            fontSize = 14.sp,
                            lineHeight = 22.sp,
                            fontFamily = FontFamily.Serif
                        )

                        Spacer(modifier = Modifier.height(18.dp))

                        // English / Arabic secondary text for immersion
                        Text(
                            text = if (isAr) "--- النسخة الأصلية / Original Transcript ---" else "--- الترجمة العربية / Arabic Translation ---",
                            color = EclipseTextMuted,
                            fontSize = 11.sp
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = if (isAr) note.contentEn else note.contentAr,
                            color = EclipseTextSecondary,
                            fontSize = 12.sp,
                            lineHeight = 18.sp
                        )
                    }
                }
            }
        }
    }
}
