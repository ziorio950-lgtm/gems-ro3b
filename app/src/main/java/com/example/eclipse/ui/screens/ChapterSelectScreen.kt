package com.example.eclipse.ui.screens

import androidx.activity.compose.BackHandler
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.eclipse.model.Chapter
import com.example.eclipse.model.Language
import com.example.ui.theme.EclipseBorder
import com.example.ui.theme.EclipseCrimson
import com.example.ui.theme.EclipseEerieCyan
import com.example.ui.theme.EclipseSurface
import com.example.ui.theme.EclipseSurfaceVariant
import com.example.ui.theme.EclipseTextMuted
import com.example.ui.theme.EclipseTextPrimary
import com.example.ui.theme.EclipseTextSecondary

@Composable
fun ChapterSelectScreen(
    unlockedChapters: List<Int>,
    language: Language,
    onSelectChapter: (Chapter) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isAr = language == Language.ARABIC

    BackHandler {
        onBack()
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF08090C))
            .padding(24.dp)
            .testTag("chapter_select_screen")
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onBack,
                    modifier = Modifier.testTag("chapter_select_back_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "Back",
                        tint = EclipseTextPrimary
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column {
                    Text(
                        text = if (isAr) "اختر المرحلة والمنطقة" else "SELECT CHAPTER & ZONE",
                        color = EclipseTextPrimary,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = if (isAr) "5 مناطق مترابطة تحكي سر تجارب الكسوف" else "5 Interconnected anomalous horror zones",
                        color = EclipseTextMuted,
                        fontSize = 11.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Chapters Carousel Cards
            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(Chapter.values()) { chapter ->
                    val isUnlocked = unlockedChapters.contains(chapter.id)

                    Surface(
                        modifier = Modifier
                            .width(230.dp)
                            .fillMaxSize()
                            .alpha(if (isUnlocked) 1.0f else 0.5f)
                            .clickable(enabled = isUnlocked) { onSelectChapter(chapter) }
                            .testTag("chapter_card_${chapter.id}"),
                        shape = RoundedCornerShape(14.dp),
                        color = EclipseSurfaceVariant,
                        border = androidx.compose.foundation.BorderStroke(
                            1.5.dp,
                            if (isUnlocked) EclipseEerieCyan.copy(alpha = 0.5f) else EclipseBorder
                        )
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(18.dp),
                            verticalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                // Chapter Number Badge
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "CH. 0${chapter.id}",
                                        color = if (isUnlocked) EclipseEerieCyan else EclipseTextMuted,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Black
                                    )

                                    if (!isUnlocked) {
                                        Icon(
                                            imageVector = Icons.Default.Lock,
                                            contentDescription = "Locked",
                                            tint = EclipseTextMuted,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(14.dp))

                                Text(
                                    text = if (isAr) chapter.titleAr else chapter.titleEn,
                                    color = EclipseTextPrimary,
                                    fontSize = 17.sp,
                                    fontWeight = FontWeight.Bold
                                )

                                Spacer(modifier = Modifier.height(6.dp))

                                Text(
                                    text = if (isAr) chapter.subtitleAr else chapter.subtitleEn,
                                    color = EclipseTextSecondary,
                                    fontSize = 12.sp,
                                    lineHeight = 16.sp
                                )
                            }

                            // Bottom Button / Status
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = if (isUnlocked) EclipseCrimson else Color(0x33262A34),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.padding(vertical = 8.dp),
                                    horizontalArrangement = Arrangement.Center,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    if (isUnlocked) {
                                        Icon(
                                            imageVector = Icons.Default.PlayArrow,
                                            contentDescription = "Play",
                                            tint = Color.White,
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                    }
                                    Text(
                                        text = if (isUnlocked) (if (isAr) "بدء الاستكشاف" else "ENTER ZONE") else (if (isAr) "مغلق" else "LOCKED"),
                                        color = if (isUnlocked) Color.White else EclipseTextMuted,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
