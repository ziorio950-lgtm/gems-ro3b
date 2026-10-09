package com.example.eclipse.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.eclipse.gameplay.CircuitPuzzleState
import com.example.eclipse.gameplay.GameSession
import com.example.eclipse.gameplay.KeypadPuzzleState
import com.example.eclipse.gameplay.ValvePuzzleState
import com.example.eclipse.model.Language
import com.example.ui.theme.EclipseBloodRed
import com.example.ui.theme.EclipseBorder
import com.example.ui.theme.EclipseCrimson
import com.example.ui.theme.EclipseCrimsonGlow
import com.example.ui.theme.EclipseEerieCyan
import com.example.ui.theme.EclipseSurface
import com.example.ui.theme.EclipseSurfaceVariant
import com.example.ui.theme.EclipseTextMuted
import com.example.ui.theme.EclipseTextPrimary
import com.example.ui.theme.EclipseTextSecondary
import com.example.ui.theme.EclipseWarningAmber

@Composable
fun KeypadPuzzleDialog(
    session: GameSession,
    language: Language,
    onSolved: () -> Unit,
    onDismiss: () -> Unit
) {
    val isAr = language == Language.ARABIC
    var input by remember { mutableStateOf("") }
    var statusText by remember { mutableStateOf<String?>(null) }
    var isSuccess by remember { mutableStateOf(false) }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier
                .width(360.dp)
                .testTag("keypad_puzzle_dialog"),
            shape = RoundedCornerShape(16.dp),
            color = EclipseSurface,
            border = androidx.compose.foundation.BorderStroke(2.dp, if (isSuccess) EclipseEerieCyan else EclipseBorder)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (isAr) "لوحة أمان الكود الرقمي" else "SECURITY ACCESS KEYPAD",
                        color = EclipseTextPrimary,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, "Close", tint = EclipseTextSecondary)
                    }
                }

                Text(
                    text = if (isAr) "دليل السجل: عام إغلاق الجناح (1984)" else "Log Clue: Year ward was decommissioned (1984)",
                    color = EclipseTextMuted,
                    fontSize = 11.sp,
                    modifier = Modifier.padding(bottom = 12.dp)
                )

                // Digital Display Screen
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp),
                    color = Color.Black,
                    shape = RoundedCornerShape(8.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, if (isSuccess) EclipseEerieCyan else EclipseCrimson)
                ) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = if (isSuccess) "ACCESS GRANTED" else (statusText ?: (if (input.isEmpty()) "----" else input)),
                            color = if (isSuccess) EclipseEerieCyan else (if (statusText != null) EclipseCrimsonGlow else Color(0xFFFF5252)),
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace,
                            letterSpacing = 4.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Number Pad (1-9, Clear, 0, Enter)
                val buttons = listOf(
                    listOf("1", "2", "3"),
                    listOf("4", "5", "6"),
                    listOf("7", "8", "9"),
                    listOf("CLR", "0", "ENT")
                )

                for (row in buttons) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        for (btn in row) {
                            Surface(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(48.dp)
                                    .clickable {
                                        if (isSuccess) return@clickable
                                        when (btn) {
                                            "CLR" -> {
                                                input = ""
                                                statusText = null
                                            }
                                            "ENT" -> {
                                                if (input == "1984") {
                                                    isSuccess = true
                                                    statusText = null
                                                    session.audioEngine.playPickupSfx()
                                                    onSolved()
                                                } else {
                                                    statusText = "DENIED"
                                                    session.audioEngine.playMonsterGrowl()
                                                    input = ""
                                                }
                                            }
                                            else -> {
                                                if (input.length < 4) {
                                                    input += btn
                                                    statusText = null
                                                }
                                            }
                                        }
                                    }
                                    .testTag("keypad_btn_$btn"),
                                shape = RoundedCornerShape(8.dp),
                                color = EclipseSurfaceVariant,
                                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0x33262A34))
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Text(
                                        text = btn,
                                        color = if (btn == "ENT") EclipseEerieCyan else (if (btn == "CLR") EclipseCrimson else EclipseTextPrimary),
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
        }
    }
}

@Composable
fun CircuitPuzzleDialog(
    session: GameSession,
    language: Language,
    onSolved: () -> Unit,
    onDismiss: () -> Unit
) {
    val isAr = language == Language.ARABIC
    var breakerStates by remember { mutableStateOf(booleanArrayOf(false, false, false, false)) }
    var isSolved by remember { mutableStateOf(false) }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier
                .width(360.dp)
                .testTag("circuit_puzzle_dialog"),
            shape = RoundedCornerShape(16.dp),
            color = EclipseSurface,
            border = androidx.compose.foundation.BorderStroke(2.dp, if (isSolved) EclipseEerieCyan else EclipseBorder)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (isAr) "لوحة قواطع الطاقة الفائقة" else "POWER BREAKER TERMINAL",
                        color = EclipseTextPrimary,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, "Close", tint = EclipseTextSecondary)
                    }
                }

                Text(
                    text = if (isAr) "قم بتفعيل جميع القواطع الأربعة بالتوازي لتوصيل التيار" else "Align all 4 breaker switches to restore full containment grid",
                    color = EclipseTextMuted,
                    fontSize = 11.sp,
                    modifier = Modifier.padding(bottom = 14.dp)
                )

                // 4 breaker switches
                val labels = listOf("BUS-1", "BUS-2", "BUS-3", "BUS-4")
                for (i in 0..3) {
                    val isActive = breakerStates[i]
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                            .clickable {
                                if (isSolved) return@clickable
                                val updated = breakerStates.copyOf()
                                updated[i] = !updated[i]
                                breakerStates = updated
                                if (updated.all { it }) {
                                    isSolved = true
                                    session.audioEngine.playPickupSfx()
                                    onSolved()
                                }
                            }
                            .testTag("breaker_switch_$i"),
                        shape = RoundedCornerShape(8.dp),
                        color = if (isActive) Color(0x3300E5FF) else EclipseSurfaceVariant,
                        border = androidx.compose.foundation.BorderStroke(1.dp, if (isActive) EclipseEerieCyan else Color(0x33262A34))
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = 16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = labels[i],
                                color = EclipseTextPrimary,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                            Surface(
                                shape = CircleShape,
                                color = if (isActive) EclipseEerieCyan else EclipseBloodRed,
                                modifier = Modifier.size(16.dp)
                            ) {}
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                }

                if (isSolved) {
                    Text(
                        text = if (isAr) "تمت إعادة تشغيل الطاقة بنجاح!" else "CONTAINMENT POWER RESTORED!",
                        color = EclipseEerieCyan,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(top = 10.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun ValvePuzzleDialog(
    session: GameSession,
    language: Language,
    onSolved: () -> Unit,
    onDismiss: () -> Unit
) {
    val isAr = language == Language.ARABIC
    var valveA by remember { mutableIntStateOf(10) }
    var valveB by remember { mutableIntStateOf(15) }
    var valveC by remember { mutableIntStateOf(20) }
    val totalPsi = valveA + valveB + valveC
    val targetPsi = 75
    val isSolved = totalPsi == targetPsi

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier
                .width(360.dp)
                .testTag("valve_puzzle_dialog"),
            shape = RoundedCornerShape(16.dp),
            color = EclipseSurface,
            border = androidx.compose.foundation.BorderStroke(2.dp, if (isSolved) EclipseEerieCyan else EclipseBorder)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (isAr) "صمامات ضغط بوابة المياه" else "DRAINAGE PRESSURE VALVES",
                        color = EclipseTextPrimary,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, "Close", tint = EclipseTextSecondary)
                    }
                }

                // PSI Meter
                Text(
                    text = "PRESSURE: $totalPsi / $targetPsi PSI",
                    color = if (isSolved) EclipseEerieCyan else (if (totalPsi > targetPsi) EclipseCrimson else EclipseWarningAmber),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace,
                    modifier = Modifier.padding(vertical = 10.dp)
                )

                LinearProgressIndicator(
                    progress = { (totalPsi.toFloat() / targetPsi.toFloat()).coerceIn(0f, 1f) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(RoundedCornerShape(4.dp)),
                    color = if (isSolved) EclipseEerieCyan else EclipseWarningAmber,
                    trackColor = Color(0x33262A34)
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Three Valve Controls
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    ValveControl(name = "Valve 1", psi = valveA, onRotate = { valveA = (valveA + 10) % 40 })
                    ValveControl(name = "Valve 2", psi = valveB, onRotate = { valveB = (valveB + 15) % 45 })
                    ValveControl(name = "Valve 3", psi = valveC, onRotate = { valveC = (valveC + 10) % 50 })
                }

                Spacer(modifier = Modifier.height(14.dp))

                if (isSolved) {
                    Button(
                        onClick = {
                            session.audioEngine.playPickupSfx()
                            onSolved()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = EclipseEerieCyan),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = if (isAr) "تأكيد تصريف المياه وفتح البوابة" else "DRAIN FLOOD & UNLOCK",
                            color = Color.Black,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ValveControl(name: String, psi: Int, onRotate: () -> Unit) {
    Surface(
        modifier = Modifier
            .clickable { onRotate() }
            .clip(RoundedCornerShape(8.dp)),
        color = EclipseSurfaceVariant,
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0x33262A34))
    ) {
        Column(
            modifier = Modifier.padding(vertical = 12.dp, horizontal = 10.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = name, color = EclipseTextSecondary, fontSize = 11.sp)
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = "$psi PSI", color = EclipseTextPrimary, fontWeight = FontWeight.Bold, fontSize = 13.sp)
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = "[ROTATE]", color = EclipseEerieCyan, fontSize = 9.sp, fontWeight = FontWeight.SemiBold)
        }
    }
}
