package com.example.eclipse.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.eclipse.gameplay.MapDefinitions
import com.example.eclipse.model.Chapter
import com.example.eclipse.model.Language
import com.example.eclipse.model.LoreNote
import com.example.eclipse.ui.components.DocumentViewerDialog
import com.example.ui.theme.EclipseBorder
import com.example.ui.theme.EclipseCrimson
import com.example.ui.theme.EclipseEerieCyan
import com.example.ui.theme.EclipseSurfaceVariant
import com.example.ui.theme.EclipseTextMuted
import com.example.ui.theme.EclipseTextPrimary
import com.example.ui.theme.EclipseTextSecondary

@Composable
fun LoreArchiveScreen(
    language: Language,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isAr = language == Language.ARABIC
    var selectedNote by remember { mutableStateOf<LoreNote?>(null) }

    val allNotes = remember {
        Chapter.values().flatMap { chapter ->
            MapDefinitions.getLevelData(chapter).loreNotes
        }
    }

    BackHandler {
        onBack()
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF08090C))
            .padding(24.dp)
            .testTag("lore_archive_screen")
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack, modifier = Modifier.testTag("lore_back_button")) {
                    Icon(Icons.Default.ArrowBack, "Back", tint = EclipseTextPrimary)
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = if (isAr) "أرشيف السجلات والتسجيلات" else "ANOMALY DOSSIERS & AUDIO LOGS",
                        color = EclipseTextPrimary,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = if (isAr) "الوثائق والتسجيلات الصوتية المستخرجة من المنشأة والمدينة" else "Classified records recovered across all 5 zones",
                        color = EclipseTextMuted,
                        fontSize = 11.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Notes List
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(allNotes) { note ->
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { selectedNote = note }
                            .testTag("lore_item_${note.id}"),
                        shape = RoundedCornerShape(10.dp),
                        color = EclipseSurfaceVariant,
                        border = androidx.compose.foundation.BorderStroke(1.dp, EclipseBorder)
                    ) {
                        Row(
                            modifier = Modifier.padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = if (note.isAudioTape) Icons.Default.GraphicEq else Icons.Default.MenuBook,
                                contentDescription = "Lore icon",
                                tint = if (note.isAudioTape) EclipseEerieCyan else EclipseCrimson,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(14.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = if (isAr) note.titleAr else note.titleEn,
                                    color = EclipseTextPrimary,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp
                                )
                                Text(
                                    text = "${note.chapter.titleEn} // ${note.dateOrSource}",
                                    color = EclipseTextSecondary,
                                    fontSize = 11.sp
                                )
                            }
                            Text(
                                text = if (isAr) "[قراءة / استماع]" else "[OPEN]",
                                color = EclipseEerieCyan,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }

        selectedNote?.let { note ->
            DocumentViewerDialog(
                note = note,
                language = language,
                onDismiss = { selectedNote = null }
            )
        }
    }
}
