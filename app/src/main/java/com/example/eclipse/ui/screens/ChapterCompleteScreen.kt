package com.example.eclipse.ui.screens

import androidx.activity.compose.BackHandler
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
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.eclipse.model.Chapter
import com.example.eclipse.model.Language
import com.example.ui.theme.EclipseBorder
import com.example.ui.theme.EclipseEerieCyan
import com.example.ui.theme.EclipseSurface
import com.example.ui.theme.EclipseTextPrimary
import com.example.ui.theme.EclipseTextSecondary

@Composable
fun ChapterCompleteScreen(
    completedChapter: Chapter,
    language: Language,
    onContinueNextChapter: () -> Unit,
    onReturnToMenu: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isAr = language == Language.ARABIC

    BackHandler {
        onReturnToMenu()
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.radialGradient(
                    colors = listOf(
                        Color(0xFF003840),
                        Color(0xFF040F12),
                        Color.Black
                    )
                )
            )
            .testTag("chapter_complete_screen"),
        contentAlignment = Alignment.Center
    ) {
        Surface(
            modifier = Modifier.width(460.dp),
            shape = RoundedCornerShape(16.dp),
            color = EclipseSurface.copy(alpha = 0.95f),
            border = androidx.compose.foundation.BorderStroke(1.5.dp, EclipseEerieCyan)
        ) {
            Column(
                modifier = Modifier.padding(28.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = "Success",
                    tint = EclipseEerieCyan,
                    modifier = Modifier.height(48.dp)
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = if (isAr) "تم النجاة من الفصل!" else "SURVIVED CHAPTER",
                    color = EclipseEerieCyan,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 2.sp
                )

                Text(
                    text = if (isAr) completedChapter.titleAr else completedChapter.titleEn,
                    color = EclipseTextPrimary,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(top = 4.dp)
                )

                Text(
                    text = if (isAr) completedChapter.subtitleAr else completedChapter.subtitleEn,
                    color = EclipseTextSecondary,
                    fontSize = 12.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(top = 4.dp, bottom = 20.dp)
                )

                Row(horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                    Button(
                        onClick = onContinueNextChapter,
                        colors = ButtonDefaults.buttonColors(containerColor = EclipseEerieCyan),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.testTag("next_chapter_button")
                    ) {
                        Text(
                            text = if (isAr) "متابعة الفصل التالي" else "Next Chapter",
                            color = Color.Black,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    OutlinedButton(
                        onClick = onReturnToMenu,
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = EclipseTextPrimary),
                        border = androidx.compose.foundation.BorderStroke(1.dp, EclipseBorder),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.testTag("chapter_complete_menu_button")
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
}
