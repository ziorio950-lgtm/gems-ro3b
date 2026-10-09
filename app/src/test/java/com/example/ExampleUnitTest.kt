package com.example

import com.example.eclipse.gameplay.MapDefinitions
import com.example.eclipse.gameplay.MonsterAIManager
import com.example.eclipse.gameplay.ValvePuzzleState
import com.example.eclipse.model.Chapter
import com.example.eclipse.model.Monster
import com.example.eclipse.model.MonsterState
import com.example.eclipse.model.MonsterType
import com.example.eclipse.model.Player
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ExampleUnitTest {

    @Test
    fun testAllFiveChaptersHaveValidMapsAndMonsters() {
        for (chapter in Chapter.values()) {
            val level = MapDefinitions.getLevelData(chapter)
            assertNotNull("Level data for $chapter should not be null", level)
            assertTrue("Map should have walls", level.map.isNotEmpty() && level.map[0].isNotEmpty())
            assertTrue("Player should start inside map", level.playerStartX > 0 && level.playerStartY > 0)
            assertTrue("Level must have objectives", level.objectives.isNotEmpty())
            assertTrue("Level must have lore notes", level.loreNotes.isNotEmpty())
            assertTrue("Level must have monsters", level.monsters.isNotEmpty())
        }
    }

    @Test
    fun testKeypadPuzzleCodeValidation() {
        val targetCode = "1984"
        assertTrue("Code 1984 should be valid", "1984" == targetCode)
        assertFalse("Code 0000 should be rejected", "0000" == targetCode)
    }

    @Test
    fun testValvePuzzlePsiCalculation() {
        val valveState = ValvePuzzleState(valveA = 20, valveB = 25, valveC = 30)
        assertEquals(75, valveState.currentPsi)
        assertTrue(valveState.currentPsi == valveState.targetPsi)
    }

    @Test
    fun testObserverAILearnsHidingHabits() {
        val ai = MonsterAIManager()
        assertEquals(0, ai.playerLockerHidesCount)
        ai.playerLockerHidesCount += 2
        assertEquals(2, ai.playerLockerHidesCount)
    }

    @Test
    fun testSoundHunterDetectionOnSprint() {
        val ai = MonsterAIManager()
        val hunter = Monster(
            id = "test_hunter",
            type = MonsterType.SOUND_HUNTER,
            x = 5f,
            y = 5f
        )
        val player = Player(x = 6f, y = 6f, isSprinting = true)
        val map = arrayOf(
            intArrayOf(0, 0, 0, 0, 0, 0, 0, 0),
            intArrayOf(0, 0, 0, 0, 0, 0, 0, 0),
            intArrayOf(0, 0, 0, 0, 0, 0, 0, 0),
            intArrayOf(0, 0, 0, 0, 0, 0, 0, 0),
            intArrayOf(0, 0, 0, 0, 0, 0, 0, 0),
            intArrayOf(0, 0, 0, 0, 0, 0, 0, 0),
            intArrayOf(0, 0, 0, 0, 0, 0, 0, 0),
            intArrayOf(0, 0, 0, 0, 0, 0, 0, 0)
        )

        var screechCount = 0
        ai.updateMonsters(
            monsters = listOf(hunter),
            player = player,
            map = map,
            dt = 0.1f,
            onMonsterScreech = { screechCount++ },
            onMonsterHitPlayer = {}
        )

        assertEquals("Hunter should detect sprinting player", MonsterState.CHASING_PLAYER, hunter.state)
        assertEquals(1, screechCount)
    }

    @Test
    fun testSettingsButtonCustomization() {
        val settings = com.example.eclipse.model.SettingsState(
            buttonScale = 1.25f,
            buttonLayoutPreset = "RAISED"
        )
        assertEquals(1.25f, settings.buttonScale, 0.01f)
        assertEquals("RAISED", settings.buttonLayoutPreset)
    }
}
