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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.eclipse.model.GraphicQuality
import com.example.eclipse.model.Language
import com.example.eclipse.model.SettingsState
import com.example.ui.theme.EclipseBorder
import com.example.ui.theme.EclipseCrimson
import com.example.ui.theme.EclipseEerieCyan
import com.example.ui.theme.EclipseSurface
import com.example.ui.theme.EclipseSurfaceVariant
import com.example.ui.theme.EclipseTextMuted
import com.example.ui.theme.EclipseTextPrimary
import com.example.ui.theme.EclipseTextSecondary

@Composable
fun SettingsScreen(
    currentSettings: SettingsState,
    onSaveSettings: (SettingsState) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    var language by remember { mutableStateOf(currentSettings.language) }
    var graphicQuality by remember { mutableStateOf(currentSettings.graphicQuality) }
    var targetFps by remember { mutableIntStateOf(currentSettings.targetFps) }
    var cameraSensitivity by remember { mutableFloatStateOf(currentSettings.cameraSensitivity) }
    var invertYAxis by remember { mutableStateOf(currentSettings.invertYAxis) }
    var masterVolume by remember { mutableFloatStateOf(currentSettings.masterVolume) }
    var sfxVolume by remember { mutableFloatStateOf(currentSettings.sfxVolume) }
    var hapticFeedback by remember { mutableStateOf(currentSettings.hapticFeedback) }
    var buttonScale by remember { mutableFloatStateOf(currentSettings.buttonScale) }
    var buttonLayoutPreset by remember { mutableStateOf(currentSettings.buttonLayoutPreset) }
    var buttonVerticalOffset by remember { mutableFloatStateOf(currentSettings.buttonVerticalOffset) }
    var buttonOpacity by remember { mutableFloatStateOf(currentSettings.buttonOpacity) }

    val isAr = language == Language.ARABIC

    fun buildUpdatedState() = SettingsState(
        language = language,
        graphicQuality = graphicQuality,
        targetFps = targetFps,
        cameraSensitivity = cameraSensitivity,
        invertYAxis = invertYAxis,
        masterVolume = masterVolume,
        sfxVolume = sfxVolume,
        hapticFeedback = hapticFeedback,
        buttonScale = buttonScale,
        buttonLayoutPreset = buttonLayoutPreset,
        buttonVerticalOffset = buttonVerticalOffset,
        buttonOpacity = buttonOpacity
    )

    BackHandler {
        onSaveSettings(buildUpdatedState())
        onBack()
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF08090C))
            .padding(24.dp)
            .testTag("settings_screen")
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = {
                            onSaveSettings(buildUpdatedState())
                            onBack()
                        },
                        modifier = Modifier.testTag("settings_back_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back",
                            tint = EclipseTextPrimary
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = if (isAr) "إعدادات اللعبة والتحكم والرسومات" else "GAME, CONTROLS & GRAPHICS",
                        color = EclipseTextPrimary,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Button(
                    onClick = {
                        onSaveSettings(buildUpdatedState())
                        onBack()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = EclipseEerieCyan),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.testTag("save_settings_button")
                ) {
                    Text(
                        text = if (isAr) "حفظ وتطبيق" else "Save & Apply",
                        color = Color.Black,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Scrollable Settings List
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // 1. Button Size Scaling (تغيير حجم الأزرار وتكبيرها)
                SettingCard(
                    title = if (isAr) "حجم وتكبير أزرار اللعب: ${(buttonScale * 100).toInt()}%" else "Button Scale: ${(buttonScale * 100).toInt()}%",
                    description = if (isAr) "تكبير أزرار المصباح والانحناء والركض والحقيبة لتناسب راحة يدك" else "Resize and enlarge all HUD action buttons for comfortable touch targets"
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            QualityOptionChip(
                                label = if (isAr) "صغير (80%)" else "Small (80%)",
                                selected = (buttonScale - 0.8f) in -0.05f..0.05f,
                                onClick = { buttonScale = 0.8f }
                            )
                            QualityOptionChip(
                                label = if (isAr) "افتراضي (100%)" else "Normal (100%)",
                                selected = (buttonScale - 1.0f) in -0.05f..0.05f,
                                onClick = { buttonScale = 1.0f }
                            )
                            QualityOptionChip(
                                label = if (isAr) "كبير (125%)" else "Large (125%)",
                                selected = (buttonScale - 1.25f) in -0.05f..0.05f,
                                onClick = { buttonScale = 1.25f }
                            )
                            QualityOptionChip(
                                label = if (isAr) "عملاق (145%)" else "XL (145%)",
                                selected = (buttonScale - 1.45f) in -0.05f..0.05f,
                                onClick = { buttonScale = 1.45f }
                            )
                        }

                        Slider(
                            value = buttonScale,
                            onValueChange = { buttonScale = it },
                            valueRange = 0.75f..1.50f,
                            colors = SliderDefaults.colors(
                                thumbColor = EclipseEerieCyan,
                                activeTrackColor = EclipseEerieCyan,
                                inactiveTrackColor = Color(0x33262A34)
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }

                // 2. Button Layout & Placement (تغيير موقع وتوزيع الأزرار)
                SettingCard(
                    title = if (isAr) "موقع وتوزيع أزرار التحكم" else "Button Layout & Placement",
                    description = if (isAr) "اختر الوضع الافتراضي، المرتفع للهواتف الكبيرة، المعكوس للأعسر، أو الموزع" else "Select Default, Raised (for larger screens), Swapped (Lefty mode), or Split"
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            QualityOptionChip(
                                label = if (isAr) "الافتراضي" else "Default",
                                selected = buttonLayoutPreset == "DEFAULT",
                                onClick = { buttonLayoutPreset = "DEFAULT" }
                            )
                            QualityOptionChip(
                                label = if (isAr) "مرتفع" else "Raised",
                                selected = buttonLayoutPreset == "RAISED",
                                onClick = { buttonLayoutPreset = "RAISED" }
                            )
                            QualityOptionChip(
                                label = if (isAr) "معكوس" else "Lefty",
                                selected = buttonLayoutPreset == "SWAPPED",
                                onClick = { buttonLayoutPreset = "SWAPPED" }
                            )
                            QualityOptionChip(
                                label = if (isAr) "موزع" else "Split",
                                selected = buttonLayoutPreset == "SPLIT",
                                onClick = { buttonLayoutPreset = "SPLIT" }
                            )
                        }

                        // Vertical Offset Slider (ارتفاع الأزرار عن أسفل الشاشة)
                        Text(
                            text = if (isAr) "ارتفاع الأزرار عن أسفل الشاشة: ${buttonVerticalOffset.toInt()} dp" else "Vertical Bottom Margin: ${buttonVerticalOffset.toInt()} dp",
                            color = EclipseTextSecondary,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        Slider(
                            value = buttonVerticalOffset,
                            onValueChange = { buttonVerticalOffset = it },
                            valueRange = 8f..64f,
                            colors = SliderDefaults.colors(
                                thumbColor = EclipseEerieCyan,
                                activeTrackColor = EclipseEerieCyan,
                                inactiveTrackColor = Color(0x33262A34)
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )

                        // Button Opacity Slider (شفافية الأزرار)
                        Text(
                            text = if (isAr) "شفافية الأزرار: ${(buttonOpacity * 100).toInt()}%" else "Button Opacity: ${(buttonOpacity * 100).toInt()}%",
                            color = EclipseTextSecondary,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        Slider(
                            value = buttonOpacity,
                            onValueChange = { buttonOpacity = it },
                            valueRange = 0.40f..1.0f,
                            colors = SliderDefaults.colors(
                                thumbColor = EclipseCrimson,
                                activeTrackColor = EclipseCrimson,
                                inactiveTrackColor = Color(0x33262A34)
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }

                // 3. Language Toggle
                SettingCard(
                    title = if (isAr) "اللغة / Language" else "Language / اللغة",
                    description = if (isAr) "التبديل بين العربية والإنجليزية" else "Switch between Arabic and English"
                ) {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        QualityOptionChip(
                            label = "العربية",
                            selected = language == Language.ARABIC,
                            onClick = { language = Language.ARABIC }
                        )
                        QualityOptionChip(
                            label = "English",
                            selected = language == Language.ENGLISH,
                            onClick = { language = Language.ENGLISH }
                        )
                    }
                }

                // 4. Graphic Quality
                SettingCard(
                    title = if (isAr) "جودة الرسومات والمؤثرات" else "Graphic Quality",
                    description = if (isAr) "المنخفضة للأجهزة الضعيفة، العالية لأفضل تجربة سينمائية" else "Low for performance on weaker devices, High for full fidelity"
                ) {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        QualityOptionChip(
                            label = if (isAr) "منخفضة" else "Low",
                            selected = graphicQuality == GraphicQuality.LOW,
                            onClick = { graphicQuality = GraphicQuality.LOW }
                        )
                        QualityOptionChip(
                            label = if (isAr) "متوسطة" else "Medium",
                            selected = graphicQuality == GraphicQuality.MEDIUM,
                            onClick = { graphicQuality = GraphicQuality.MEDIUM }
                        )
                        QualityOptionChip(
                            label = if (isAr) "عالية" else "High",
                            selected = graphicQuality == GraphicQuality.HIGH,
                            onClick = { graphicQuality = GraphicQuality.HIGH }
                        )
                    }
                }

                // 5. Target Framerate
                SettingCard(
                    title = if (isAr) "معدل الإطارات المستهدف" else "Target Framerate",
                    description = if (isAr) "30 إطاراً لتوفير البطارية أو 60 إطاراً لسلاسة فائقة" else "Target 30 FPS for battery saving, 60 FPS for ultra-smoothness"
                ) {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        QualityOptionChip(
                            label = "30 FPS",
                            selected = targetFps == 30,
                            onClick = { targetFps = 30 }
                        )
                        QualityOptionChip(
                            label = "60 FPS",
                            selected = targetFps == 60,
                            onClick = { targetFps = 60 }
                        )
                    }
                }

                // 6. Camera Sensitivity
                SettingCard(
                    title = if (isAr) "حساسية الكاميرا: ${(cameraSensitivity * 100).toInt()}%" else "Camera Sensitivity: ${(cameraSensitivity * 100).toInt()}%",
                    description = if (isAr) "سرعة استجابة دوران زاوية الرؤية عند السحب" else "Touch look swipe responsiveness"
                ) {
                    Slider(
                        value = cameraSensitivity,
                        onValueChange = { cameraSensitivity = it },
                        valueRange = 0.5f..2.5f,
                        colors = SliderDefaults.colors(
                            thumbColor = EclipseEerieCyan,
                            activeTrackColor = EclipseEerieCyan,
                            inactiveTrackColor = Color(0x33262A34)
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                // 7. Invert Y-Axis
                SettingCard(
                    title = if (isAr) "عكس المحور العمودي (Invert Y)" else "Invert Y-Axis",
                    description = if (isAr) "عكس اتجاه النظر لأعلى وأسفل" else "Invert vertical camera pitch"
                ) {
                    Switch(
                        checked = invertYAxis,
                        onCheckedChange = { invertYAxis = it },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = EclipseEerieCyan,
                            checkedTrackColor = EclipseEerieCyan.copy(alpha = 0.5f)
                        )
                    )
                }

                // 8. Audio Volume
                SettingCard(
                    title = if (isAr) "مستوى الصوت العام: ${(masterVolume * 100).toInt()}%" else "Master Volume: ${(masterVolume * 100).toInt()}%",
                    description = if (isAr) "التحكم في أصوات الأجواء والمطر والرعد والوحوش" else "Atmospheric horror drone, rain, thunder, heartbeat and monster screams"
                ) {
                    Slider(
                        value = masterVolume,
                        onValueChange = { masterVolume = it },
                        valueRange = 0f..1f,
                        colors = SliderDefaults.colors(
                            thumbColor = EclipseCrimson,
                            activeTrackColor = EclipseCrimson,
                            inactiveTrackColor = Color(0x33262A34)
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                // 9. Haptic Vibration
                SettingCard(
                    title = if (isAr) "الاهتزاز اللمسي (Haptic Feedback)" else "Tactile Haptics (Vibration)",
                    description = if (isAr) "نبضات الهاتف عند اقتراب الوحش وهجمات الرعب" else "Vibrate device with panic heartbeat and jumpscares"
                ) {
                    Switch(
                        checked = hapticFeedback,
                        onCheckedChange = { hapticFeedback = it },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = EclipseCrimson,
                            checkedTrackColor = EclipseCrimson.copy(alpha = 0.5f)
                        )
                    )
                }
            }
        }
    }
}

@Composable
private fun SettingCard(
    title: String,
    description: String,
    content: @Composable () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        color = EclipseSurfaceVariant,
        border = androidx.compose.foundation.BorderStroke(1.dp, EclipseBorder)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = title, color = EclipseTextPrimary, fontWeight = FontWeight.Bold, fontSize = 14.sp)
            Text(text = description, color = EclipseTextSecondary, fontSize = 11.sp, modifier = Modifier.padding(top = 2.dp, bottom = 10.dp))
            content()
        }
    }
}

@Composable
private fun QualityOptionChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .clickable { onClick() }
            .clip(RoundedCornerShape(8.dp)),
        color = if (selected) EclipseEerieCyan else Color(0x22181B22),
        border = androidx.compose.foundation.BorderStroke(1.dp, if (selected) EclipseEerieCyan else EclipseBorder)
    ) {
        Text(
            text = label,
            color = if (selected) Color.Black else EclipseTextPrimary,
            fontWeight = FontWeight.Bold,
            fontSize = 12.sp,
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 7.dp)
        )
    }
}
