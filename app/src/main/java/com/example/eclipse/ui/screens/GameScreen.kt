package com.example.eclipse.ui.screens

import android.graphics.Canvas
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.example.eclipse.engine.Raycast3DEngine
import com.example.eclipse.gameplay.GameSession
import com.example.eclipse.model.Chapter
import com.example.eclipse.model.GameScreenState
import com.example.eclipse.model.GraphicQuality
import com.example.eclipse.model.Language
import com.example.eclipse.model.SettingsState
import com.example.eclipse.ui.components.CameraLookTouchPad
import com.example.eclipse.ui.components.CircuitPuzzleDialog
import com.example.eclipse.ui.components.DocumentViewerDialog
import com.example.eclipse.ui.components.GameHUD
import com.example.eclipse.ui.components.InventoryDialog
import com.example.eclipse.ui.components.KeypadPuzzleDialog
import com.example.eclipse.ui.components.PauseDialog
import com.example.eclipse.ui.components.SurvivalHandbookDialog
import com.example.eclipse.ui.components.ValvePuzzleDialog
import com.example.eclipse.ui.components.VirtualJoystick

@Composable
fun GameScreen(
    session: GameSession,
    settings: SettingsState,
    onPause: () -> Unit,
    onRestartCheckpoint: () -> Unit,
    onQuitToMenu: () -> Unit,
    onOpenSettings: () -> Unit,
    modifier: Modifier = Modifier
) {
    val engine = remember { Raycast3DEngine() }

    var moveForward by remember { mutableFloatStateOf(0f) }
    var moveStrafe by remember { mutableFloatStateOf(0f) }
    var lookYawDelta by remember { mutableFloatStateOf(0f) }
    var lookPitchDelta by remember { mutableFloatStateOf(0f) }

    var isInventoryOpen by remember { mutableStateOf(false) }
    var isHandbookOpen by remember { mutableStateOf(false) }
    var isPaused by remember { mutableStateOf(false) }

    // Start Audio Engine when game starts
    LaunchedEffect(Unit) {
        session.audioEngine.masterVolume = settings.masterVolume
        session.hapticsManager.isEnabled = settings.hapticFeedback
        session.audioEngine.start()
    }

    // Frame ticker loop for smooth physics and AI updates
    var frameTick by remember { mutableLongStateOf(0L) }
    LaunchedEffect(isPaused) {
        var lastTime = System.nanoTime()
        while (true) {
            withFrameNanos { now ->
                val dt = ((now - lastTime) / 1_000_000_000.0f).coerceIn(0.005f, 0.05f)
                lastTime = now

                if (!isPaused && !isInventoryOpen && !isHandbookOpen && session.activeNoteToRead == null &&
                    !session.isKeypadOpen && !session.isCircuitOpen && !session.isValveOpen
                ) {
                    session.update(
                        dt = dt,
                        moveForward = moveForward,
                        moveStrafe = moveStrafe,
                        lookYaw = lookYawDelta,
                        lookPitch = lookPitchDelta
                    )
                    // Reset single-frame look deltas
                    lookYawDelta = 0f
                    lookPitchDelta = 0f
                }
                frameTick = now
            }
        }
    }

    BackHandler {
        if (isHandbookOpen) isHandbookOpen = false
        else if (isInventoryOpen) isInventoryOpen = false
        else isPaused = !isPaused
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black)
            .testTag("game_viewport_screen")
    ) {
        // 1. 3D Raycasting Viewport Canvas
        Canvas(modifier = Modifier.fillMaxSize()) {
            val currentFrame = frameTick

            drawIntoCanvas { canvas ->
                val nativeCanvas = canvas.nativeCanvas
                val sprites = session.getSprites()

                engine.render(
                    canvas = nativeCanvas,
                    width = size.width,
                    height = size.height,
                    player = session.player,
                    map = session.levelData.map,
                    sprites = sprites,
                    chapter = session.currentChapter,
                    quality = settings.graphicQuality,
                    headBobOffset = session.headBobOffset,
                    flashlightFlicker = session.flashlightFlicker
                )
            }
        }

        // 2. Camera Touch Pad (covers free look swipe surface)
        val isSwapped = settings.buttonLayoutPreset == "SWAPPED"
        CameraLookTouchPad(
            modifier = Modifier
                .fillMaxSize()
                .padding(
                    start = if (isSwapped) 0.dp else 220.dp,
                    end = if (isSwapped) 220.dp else 0.dp
                ),
            sensitivity = settings.cameraSensitivity,
            invertY = settings.invertYAxis,
            onLook = { yaw, pitch ->
                lookYawDelta += yaw
                lookPitchDelta += pitch
            }
        )

        // 3. Virtual Joystick (adapted to layout preset)
        val joystickAlignment = if (isSwapped) Alignment.BottomEnd else Alignment.BottomStart
        val joystickBottomPadding = if (settings.buttonLayoutPreset == "RAISED") 42.dp else 26.dp
        VirtualJoystick(
            modifier = Modifier
                .align(joystickAlignment)
                .padding(
                    start = if (isSwapped) 0.dp else 24.dp,
                    end = if (isSwapped) 24.dp else 0.dp,
                    bottom = joystickBottomPadding
                ),
            onMove = { forward, strafe ->
                moveForward = forward
                moveStrafe = strafe
            }
        )

        // 4. In-Game HUD overlay
        GameHUD(
            session = session,
            settings = settings,
            onOpenInventory = { isInventoryOpen = true },
            onOpenHandbook = { isHandbookOpen = true },
            onPauseGame = { isPaused = true }
        )

        // 5. In-Hand Survival Handbook Dialog
        if (isHandbookOpen) {
            SurvivalHandbookDialog(
                language = settings.language,
                onDismiss = { isHandbookOpen = false }
            )
        }

        // 6. Inventory Dialog
        if (isInventoryOpen) {
            InventoryDialog(
                session = session,
                language = settings.language,
                onDismiss = { isInventoryOpen = false }
            )
        }

        // 6. Classified Document / Audio Tape Viewer Dialog
        session.activeNoteToRead?.let { note ->
            DocumentViewerDialog(
                note = note,
                language = settings.language,
                onDismiss = { session.activeNoteToRead = null }
            )
        }

        // 7. Interactive Puzzle Overlays
        if (session.isKeypadOpen) {
            KeypadPuzzleDialog(
                session = session,
                language = settings.language,
                onSolved = {
                    session.isKeypadOpen = false
                    session.showBanner("Code Accepted. Gate unlocked!", "تم قبول الكود. تم فتح البوابة!")
                    val door = session.levelData.doors.firstOrNull()
                    door?.isLocked = false
                    door?.isOpen = true
                },
                onDismiss = { session.isKeypadOpen = false }
            )
        }

        if (session.isCircuitOpen) {
            CircuitPuzzleDialog(
                session = session,
                language = settings.language,
                onSolved = {
                    session.isCircuitOpen = false
                    session.showBanner("Containment grid powered up!", "تمت تغذية شبكة الاحتواء بالطاقة!")
                    val door = session.levelData.doors.firstOrNull()
                    door?.isLocked = false
                    door?.isOpen = true
                },
                onDismiss = { session.isCircuitOpen = false }
            )
        }

        if (session.isValveOpen) {
            ValvePuzzleDialog(
                session = session,
                language = settings.language,
                onSolved = {
                    session.isValveOpen = false
                    session.showBanner("Flood drained! Bunker entrance clear.", "تم تصريف المياه وفتح مدخل الملجأ.")
                    val door = session.levelData.doors.firstOrNull()
                    door?.isLocked = false
                    door?.isOpen = true
                },
                onDismiss = { session.isValveOpen = false }
            )
        }

        // 8. In-Game Pause Dialog
        if (isPaused) {
            PauseDialog(
                language = settings.language,
                onResume = { isPaused = false },
                onRestartCheckpoint = {
                    isPaused = false
                    onRestartCheckpoint()
                },
                onOpenSettings = onOpenSettings,
                onQuitToMenu = {
                    isPaused = false
                    session.audioEngine.stop()
                    onQuitToMenu()
                }
            )
        }
    }
}
