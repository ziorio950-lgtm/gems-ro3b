package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.example.eclipse.data.GameRepository
import com.example.eclipse.engine.GameAudioEngine
import com.example.eclipse.engine.HapticsManager
import com.example.eclipse.gameplay.GameSession
import com.example.eclipse.model.Chapter
import com.example.eclipse.model.GameScreenState
import com.example.eclipse.model.GameSaveState
import com.example.eclipse.model.Language
import com.example.eclipse.model.SettingsState
import com.example.eclipse.ui.screens.ChapterCompleteScreen
import com.example.eclipse.ui.screens.ChapterSelectScreen
import com.example.eclipse.ui.screens.CreditsScreen
import com.example.eclipse.ui.screens.GameOverScreen
import com.example.eclipse.ui.screens.GameScreen
import com.example.eclipse.ui.screens.LoreArchiveScreen
import com.example.eclipse.ui.screens.MainMenuScreen
import com.example.eclipse.ui.screens.SettingsScreen
import com.example.eclipse.ui.components.SurvivalHandbookDialog
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {

    private lateinit var repository: GameRepository
    private val audioEngine = GameAudioEngine()
    private lateinit var hapticsManager: HapticsManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        repository = GameRepository(this)
        hapticsManager = HapticsManager(this)

        setContent {
            MyApplicationTheme {
                EclipseHorrorGameApp(
                    repository = repository,
                    audioEngine = audioEngine,
                    hapticsManager = hapticsManager
                )
            }
        }
    }

    override fun onPause() {
        super.onPause()
        audioEngine.stop()
    }

    override fun onDestroy() {
        super.onDestroy()
        audioEngine.stop()
    }
}

@Composable
fun EclipseHorrorGameApp(
    repository: GameRepository,
    audioEngine: GameAudioEngine,
    hapticsManager: HapticsManager
) {
    var screenState by remember { mutableStateOf(GameScreenState.MAIN_MENU) }
    var settings by remember { mutableStateOf(repository.loadSettings()) }
    var saveState by remember { mutableStateOf(repository.loadSaveState()) }
    var completedChapter by remember { mutableStateOf(Chapter.DEAD_CITY) }

    var gameSession by remember {
        mutableStateOf<GameSession?>(null)
    }

    var isMenuHandbookOpen by remember { mutableStateOf(false) }

    DisposableEffect(Unit) {
        onDispose {
            audioEngine.stop()
        }
    }

    fun startChapter(chapter: Chapter) {
        val session = GameSession(
            currentChapter = chapter,
            audioEngine = audioEngine,
            hapticsManager = hapticsManager,
            onGameOver = {
                screenState = GameScreenState.GAME_OVER
            },
            onChapterComplete = { doneChapter ->
                completedChapter = doneChapter
                val nextChapterId = (doneChapter.id + 1).coerceAtMost(5)
                repository.unlockChapter(nextChapterId)
                repository.saveGameProgress(
                    chapter = nextChapterId,
                    unlocked = (saveState.unlockedChapters + nextChapterId).distinct(),
                    loreIds = saveState.collectedLoreIds
                )
                saveState = repository.loadSaveState()
                screenState = GameScreenState.CHAPTER_COMPLETE
            }
        )
        gameSession = session
        screenState = GameScreenState.PLAYING
    }

    Box(modifier = Modifier.fillMaxSize()) {
        when (screenState) {
            GameScreenState.MAIN_MENU -> {
                MainMenuScreen(
                    saveState = saveState,
                    settings = settings,
                    onStartNewGame = {
                        startChapter(Chapter.DEAD_CITY)
                    },
                    onContinueGame = {
                        val chapterToResume = Chapter.values().firstOrNull { it.id == saveState.currentChapter } ?: Chapter.DEAD_CITY
                        startChapter(chapterToResume)
                    },
                    onSelectChapter = {
                        screenState = GameScreenState.CHAPTER_SELECT
                    },
                    onOpenHandbook = {
                        isMenuHandbookOpen = true
                    },
                    onOpenSettings = {
                        screenState = GameScreenState.SETTINGS
                    },
                    onOpenLoreArchive = {
                        screenState = GameScreenState.LORE_ARCHIVE
                    },
                    onOpenCredits = {
                        screenState = GameScreenState.CREDITS
                    },
                    onToggleLanguage = {
                        val newLang = if (settings.language == Language.ARABIC) Language.ENGLISH else Language.ARABIC
                        val updated = settings.copy(language = newLang)
                        settings = updated
                        repository.saveSettings(updated)
                    }
                )

                if (isMenuHandbookOpen) {
                    SurvivalHandbookDialog(
                        language = settings.language,
                        onDismiss = { isMenuHandbookOpen = false }
                    )
                }
            }

            GameScreenState.PLAYING -> {
                gameSession?.let { session ->
                    GameScreen(
                        session = session,
                        settings = settings,
                        onPause = { /* In-Game Pause handled inside GameScreen */ },
                        onRestartCheckpoint = {
                            session.restartCurrentChapter()
                        },
                        onQuitToMenu = {
                            screenState = GameScreenState.MAIN_MENU
                        },
                        onOpenSettings = {
                            screenState = GameScreenState.SETTINGS
                        }
                    )
                } ?: run {
                    screenState = GameScreenState.MAIN_MENU
                }
            }

            GameScreenState.CHAPTER_SELECT -> {
                ChapterSelectScreen(
                    unlockedChapters = saveState.unlockedChapters,
                    language = settings.language,
                    onSelectChapter = { chapter ->
                        startChapter(chapter)
                    },
                    onBack = {
                        screenState = GameScreenState.MAIN_MENU
                    }
                )
            }

            GameScreenState.SETTINGS -> {
                SettingsScreen(
                    currentSettings = settings,
                    onSaveSettings = { newSettings ->
                        settings = newSettings
                        repository.saveSettings(newSettings)
                        audioEngine.masterVolume = newSettings.masterVolume
                        hapticsManager.isEnabled = newSettings.hapticFeedback
                    },
                    onBack = {
                        screenState = if (gameSession != null) GameScreenState.PLAYING else GameScreenState.MAIN_MENU
                    }
                )
            }

            GameScreenState.LORE_ARCHIVE -> {
                LoreArchiveScreen(
                    language = settings.language,
                    onBack = {
                        screenState = GameScreenState.MAIN_MENU
                    }
                )
            }

            GameScreenState.CREDITS -> {
                CreditsScreen(
                    language = settings.language,
                    onBack = {
                        screenState = GameScreenState.MAIN_MENU
                    }
                )
            }

            GameScreenState.GAME_OVER -> {
                GameOverScreen(
                    language = settings.language,
                    onRetry = {
                        gameSession?.restartCurrentChapter()
                        screenState = GameScreenState.PLAYING
                    },
                    onReturnToMenu = {
                        gameSession?.audioEngine?.stop()
                        screenState = GameScreenState.MAIN_MENU
                    }
                )
            }

            GameScreenState.CHAPTER_COMPLETE -> {
                ChapterCompleteScreen(
                    completedChapter = completedChapter,
                    language = settings.language,
                    onContinueNextChapter = {
                        val nextId = completedChapter.id + 1
                        val nextChapter = Chapter.values().firstOrNull { it.id == nextId }
                        if (nextChapter != null) {
                            startChapter(nextChapter)
                        } else {
                            screenState = GameScreenState.CREDITS
                        }
                    },
                    onReturnToMenu = {
                        gameSession?.audioEngine?.stop()
                        screenState = GameScreenState.MAIN_MENU
                    }
                )
            }

            else -> {
                screenState = GameScreenState.MAIN_MENU
            }
        }
    }
}
