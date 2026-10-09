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
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BatteryChargingFull
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.MedicalServices
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.SettingsSuggest
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.eclipse.gameplay.GameSession
import com.example.eclipse.model.InventorySlot
import com.example.eclipse.model.ItemType
import com.example.eclipse.model.Language
import com.example.ui.theme.EclipseBloodRed
import com.example.ui.theme.EclipseBorder
import com.example.ui.theme.EclipseCrimson
import com.example.ui.theme.EclipseEerieCyan
import com.example.ui.theme.EclipseSurface
import com.example.ui.theme.EclipseSurfaceVariant
import com.example.ui.theme.EclipseTextPrimary
import com.example.ui.theme.EclipseTextSecondary

@Composable
fun InventoryDialog(
    session: GameSession,
    language: Language,
    onDismiss: () -> Unit
) {
    val isAr = language == Language.ARABIC
    var selectedItem by remember { mutableStateOf(session.inventory.firstOrNull()) }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .fillMaxHeight(0.85f)
                .testTag("inventory_dialog"),
            shape = RoundedCornerShape(16.dp),
            color = EclipseSurface,
            border = androidx.compose.foundation.BorderStroke(1.5.dp, EclipseBorder)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (isAr) "حقيبة النجاة والأدوات" else "SURVIVAL INVENTORY",
                        color = EclipseTextPrimary,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.testTag("close_inventory_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = EclipseTextSecondary
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Content: Left Grid, Right Inspection Details
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Item Slots Grid
                    Surface(
                        modifier = Modifier
                            .weight(1.2f)
                            .fillMaxHeight(),
                        color = EclipseSurfaceVariant,
                        shape = RoundedCornerShape(12.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0x33262A34))
                    ) {
                        if (session.inventory.isEmpty()) {
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = if (isAr) "الحقيبة فارغة حالياً" else "Inventory is currently empty",
                                    color = EclipseTextSecondary,
                                    fontSize = 13.sp
                                )
                            }
                        } else {
                            LazyVerticalGrid(
                                columns = GridCells.Fixed(3),
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(10.dp),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                items(session.inventory) { item ->
                                    val isSelected = selectedItem?.id == item.id
                                    ItemSlotCard(
                                        item = item,
                                        isAr = isAr,
                                        isSelected = isSelected,
                                        onClick = { selectedItem = item }
                                    )
                                }
                            }
                        }
                    }

                    // Inspection Panel
                    Surface(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight(),
                        color = EclipseSurfaceVariant,
                        shape = RoundedCornerShape(12.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0x33262A34))
                    ) {
                        selectedItem?.let { item ->
                            Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(16.dp),
                                verticalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column {
                                    Text(
                                        text = if (isAr) item.titleAr else item.titleEn,
                                        color = EclipseEerieCyan,
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = if (isAr) item.descriptionAr else item.descriptionEn,
                                        color = EclipseTextSecondary,
                                        fontSize = 13.sp,
                                        lineHeight = 18.sp
                                    )
                                }

                                // Action Buttons
                                if (item.type == ItemType.FLASHLIGHT_BATTERY) {
                                    Button(
                                        onClick = {
                                            session.player.batteryPercent = 100f
                                            session.inventory.remove(item)
                                            selectedItem = session.inventory.firstOrNull()
                                            session.showBanner("Flashlight charged to 100%", "تم شحن المصباح إلى 100%")
                                            session.audioEngine.playPickupSfx()
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = EclipseEerieCyan),
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .testTag("use_battery_button")
                                    ) {
                                        Text(
                                            text = if (isAr) "استخدام وإعادة الشحن" else "Use Battery",
                                            color = Color.Black,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                } else if (item.type == ItemType.SYRINGE_MEDKIT) {
                                    Button(
                                        onClick = {
                                            session.player.health = 100f
                                            session.inventory.remove(item)
                                            selectedItem = session.inventory.firstOrNull()
                                            session.showBanner("Health fully recovered", "تمت استعادة الصحة بالكامل")
                                            session.audioEngine.playPickupSfx()
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = EclipseCrimson),
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .testTag("use_medkit_button")
                                    ) {
                                        Text(
                                            text = if (isAr) "استخدام الإسعاف" else "Apply Medkit",
                                            color = Color.White,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }
                        } ?: run {
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = if (isAr) "حدد عنصراً لفحصه" else "Select an item to inspect",
                                    color = EclipseTextSecondary,
                                    fontSize = 12.sp
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ItemSlotCard(
    item: InventorySlot,
    isAr: Boolean,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val icon: ImageVector = when (item.type) {
        ItemType.FLASHLIGHT_BATTERY -> Icons.Default.BatteryChargingFull
        ItemType.BRASS_KEY, ItemType.SURGICAL_KEYCARD -> Icons.Default.Key
        ItemType.SYRINGE_MEDKIT -> Icons.Default.MedicalServices
        ItemType.AUDIO_CASSETTE, ItemType.RESEARCH_NOTE -> Icons.Default.MenuBook
        else -> Icons.Default.SettingsSuggest
    }

    Surface(
        modifier = Modifier
            .size(76.dp)
            .clickable { onClick() }
            .testTag("item_slot_${item.id}"),
        shape = RoundedCornerShape(8.dp),
        color = if (isSelected) Color(0x3300E5FF) else Color(0x22101217),
        border = androidx.compose.foundation.BorderStroke(
            1.5.dp,
            if (isSelected) EclipseEerieCyan else Color(0x33262A34)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(6.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = item.titleEn,
                tint = if (isSelected) EclipseEerieCyan else Color(0xCCFFFFFF),
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = if (isAr) item.titleAr else item.titleEn,
                color = EclipseTextPrimary,
                fontSize = 10.sp,
                maxLines = 1
            )
        }
    }
}
