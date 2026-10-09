package com.example.eclipse.model

data class GameSaveState(
    val currentChapter: Int = 1,
    val playerHealth: Float = 100f,
    val playerBattery: Float = 90f,
    val playerSanity: Float = 100f,
    val inventoryItemTypes: List<String> = emptyList(),
    val solvedPuzzleIds: List<String> = emptyList(),
    val unlockedChapters: List<Int> = listOf(1),
    val collectedLoreIds: List<String> = emptyList(),
    val hasActiveSave: Boolean = false,
    val timestamp: Long = System.currentTimeMillis()
)

data class SettingsState(
    val language: Language = Language.ARABIC,
    val graphicQuality: GraphicQuality = GraphicQuality.HIGH,
    val targetFps: Int = 60,
    val cameraSensitivity: Float = 1.0f,
    val invertYAxis: Boolean = false,
    val masterVolume: Float = 0.85f,
    val sfxVolume: Float = 0.9f,
    val hapticFeedback: Boolean = true,
    val buttonScale: Float = 1.0f, // 0.75f to 1.50f
    val buttonLayoutPreset: String = "DEFAULT", // "DEFAULT", "RAISED", "SWAPPED", "SPLIT"
    val buttonVerticalOffset: Float = 20f, // 10dp to 60dp bottom clearance
    val buttonOpacity: Float = 0.88f // 0.4f to 1.0f
)
