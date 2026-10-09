package com.example.eclipse.data

import android.content.Context
import android.content.SharedPreferences
import com.example.eclipse.model.GameSaveState
import com.example.eclipse.model.GraphicQuality
import com.example.eclipse.model.Language
import com.example.eclipse.model.SettingsState

class GameRepository(context: Context) {

    private val prefs: SharedPreferences = context.getSharedPreferences("eclipse_horror_prefs", Context.MODE_PRIVATE)

    fun loadSettings(): SettingsState {
        val langStr = prefs.getString("setting_language", Language.ARABIC.name) ?: Language.ARABIC.name
        val qualityStr = prefs.getString("setting_quality", GraphicQuality.HIGH.name) ?: GraphicQuality.HIGH.name
        val fps = prefs.getInt("setting_fps", 60)
        val sensitivity = prefs.getFloat("setting_sensitivity", 1.0f)
        val invertY = prefs.getBoolean("setting_invert_y", false)
        val masterVol = prefs.getFloat("setting_master_vol", 0.85f)
        val sfxVol = prefs.getFloat("setting_sfx_vol", 0.9f)
        val haptic = prefs.getBoolean("setting_haptic", true)
        val btnScale = prefs.getFloat("setting_button_scale", 1.0f)
        val btnLayout = prefs.getString("setting_button_layout", "DEFAULT") ?: "DEFAULT"
        val btnOffset = prefs.getFloat("setting_button_offset", 20f)
        val btnOpacity = prefs.getFloat("setting_button_opacity", 0.88f)

        return SettingsState(
            language = try { Language.valueOf(langStr) } catch (_: Exception) { Language.ARABIC },
            graphicQuality = try { GraphicQuality.valueOf(qualityStr) } catch (_: Exception) { GraphicQuality.HIGH },
            targetFps = fps,
            cameraSensitivity = sensitivity,
            invertYAxis = invertY,
            masterVolume = masterVol,
            sfxVolume = sfxVol,
            hapticFeedback = haptic,
            buttonScale = btnScale,
            buttonLayoutPreset = btnLayout,
            buttonVerticalOffset = btnOffset,
            buttonOpacity = btnOpacity
        )
    }

    fun saveSettings(settings: SettingsState) {
        prefs.edit()
            .putString("setting_language", settings.language.name)
            .putString("setting_quality", settings.graphicQuality.name)
            .putInt("setting_fps", settings.targetFps)
            .putFloat("setting_sensitivity", settings.cameraSensitivity)
            .putBoolean("setting_invert_y", settings.invertYAxis)
            .putFloat("setting_master_vol", settings.masterVolume)
            .putFloat("setting_sfx_vol", settings.sfxVolume)
            .putBoolean("setting_haptic", settings.hapticFeedback)
            .putFloat("setting_button_scale", settings.buttonScale)
            .putString("setting_button_layout", settings.buttonLayoutPreset)
            .putFloat("setting_button_offset", settings.buttonVerticalOffset)
            .putFloat("setting_button_opacity", settings.buttonOpacity)
            .apply()
    }

    fun loadSaveState(): GameSaveState {
        val hasSave = prefs.getBoolean("has_active_save", false)
        val chapter = prefs.getInt("current_chapter", 1)
        val health = prefs.getFloat("player_health", 100f)
        val battery = prefs.getFloat("player_battery", 90f)
        val unlockedStr = prefs.getString("unlocked_chapters", "1") ?: "1"
        val unlockedList = unlockedStr.split(",").mapNotNull { it.trim().toIntOrNull() }.ifEmpty { listOf(1) }

        val loreStr = prefs.getString("collected_lore", "") ?: ""
        val loreList = if (loreStr.isNotEmpty()) loreStr.split(",") else emptyList()

        return GameSaveState(
            currentChapter = chapter,
            playerHealth = health,
            playerBattery = battery,
            unlockedChapters = unlockedList,
            collectedLoreIds = loreList,
            hasActiveSave = hasSave
        )
    }

    fun saveGameProgress(chapter: Int, unlocked: List<Int>, loreIds: List<String>) {
        val unlockedStr = unlocked.distinct().joinToString(",")
        val loreStr = loreIds.distinct().joinToString(",")
        prefs.edit()
            .putBoolean("has_active_save", true)
            .putInt("current_chapter", chapter)
            .putString("unlocked_chapters", unlockedStr)
            .putString("collected_lore", loreStr)
            .apply()
    }

    fun unlockChapter(chapterId: Int) {
        val current = loadSaveState()
        val updated = (current.unlockedChapters + chapterId).distinct().sorted()
        prefs.edit()
            .putString("unlocked_chapters", updated.joinToString(","))
            .apply()
    }
}
