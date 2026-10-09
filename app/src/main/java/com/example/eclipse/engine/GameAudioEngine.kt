package com.example.eclipse.engine

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import com.example.eclipse.model.Chapter
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.math.PI
import kotlin.math.sin

/**
 * Procedural Horror Audio Synthesizer with Contextual Environmental Layers.
 * Synthesizes dynamic realistic rain, thunder, fluorescent buzz, howling wind,
 * surface-specific footsteps, ragged breathing, heartbeat BPM scaling,
 * tactical flashlight switch snaps, and monster vocalizations.
 */
class GameAudioEngine {

    private val sampleRate = 22050
    private var audioTrack: AudioTrack? = null
    private var audioJob: Job? = null
    private val scope = CoroutineScope(Dispatchers.Default)

    @Volatile var isRunning: Boolean = false
    @Volatile var masterVolume: Float = 0.8f
    @Volatile var terrorLevel: Float = 0f // 0f (calm) to 1f (extreme panic)
    @Volatile var currentChapter: Chapter = Chapter.DEAD_CITY
    @Volatile var playerStaminaRatio: Float = 1.0f
    @Volatile var isHidingInLocker: Boolean = false

    @Volatile var isFlashlightTriggered: Boolean = false
    @Volatile var isFootstepTriggered: Boolean = false
    @Volatile var isSprintStep: Boolean = false
    @Volatile var isMonsterGrowlTriggered: Boolean = false
    @Volatile var isDoorOpenTriggered: Boolean = false
    @Volatile var isPickupTriggered: Boolean = false
    @Volatile var isBatteryReloadTriggered: Boolean = false

    fun start() {
        if (isRunning) return
        isRunning = true

        val minBufferSize = AudioTrack.getMinBufferSize(
            sampleRate,
            AudioFormat.CHANNEL_OUT_MONO,
            AudioFormat.ENCODING_PCM_16BIT
        )
        val bufferSize = minBufferSize.coerceAtLeast(4096)

        try {
            audioTrack = AudioTrack.Builder()
                .setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_GAME)
                        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                        .build()
                )
                .setAudioFormat(
                    AudioFormat.Builder()
                        .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                        .setSampleRate(sampleRate)
                        .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                        .build()
                )
                .setBufferSizeInBytes(bufferSize)
                .setTransferMode(AudioTrack.MODE_STREAM)
                .build()

            audioTrack?.play()

            audioJob = scope.launch {
                val chunkSize = 1024
                val buffer = ShortArray(chunkSize)
                var phaseDrone = 0.0
                var phaseSub = 0.0
                var phaseWind = 0.0
                var phaseFluo = 0.0
                var heartbeatPhase = 0.0
                var breathingPhase = 0.0
                var sampleCounter = 0L

                var flashSfxSamples = 0
                var stepSfxSamples = 0
                var stepIsSprint = false
                var growlSfxSamples = 0
                var doorSfxSamples = 0
                var pickupSfxSamples = 0
                var batterySfxSamples = 0
                var thunderCountdown = (Math.random() * 200000 + 100000).toInt()
                var thunderDuration = 0

                while (isActive && isRunning) {
                    val terror = terrorLevel.coerceIn(0f, 1f)
                    val chapter = currentChapter
                    val hiding = isHidingInLocker
                    val stamina = playerStaminaRatio

                    // Heartbeat rate increases with terror: 1.0 Hz (60 bpm) to 2.6 Hz (156 bpm)
                    val heartRateHz = 1.0 + (terror * 1.6)
                    val heartVolume = (0.25f + terror * 0.75f)

                    // Breathing rate: fast and audible when stamina < 0.35
                    val isExhausted = stamina < 0.35f
                    val breathRateHz = if (isExhausted) 0.85 else 0.35

                    for (i in 0 until chunkSize) {
                        sampleCounter++
                        val t = sampleCounter.toDouble() / sampleRate

                        // -------------------------------------------------------------
                        // 1. CONTEXTUAL ENVIRONMENTAL AMBIENCE BY ZONE
                        // -------------------------------------------------------------
                        var ambientSample = 0.0

                        when (chapter) {
                            Chapter.DEAD_CITY -> {
                                // Rain hiss (shaped white/pink noise)
                                val rainNoise = (Math.random() - 0.5) * 0.14
                                ambientSample += rainNoise

                                // Thunder event
                                if (thunderDuration > 0) {
                                    val thunderEnv = thunderDuration.toDouble() / 15000.0
                                    val rumble = sin(sampleCounter * 0.015) * 0.35 + (Math.random() - 0.5) * 0.25
                                    ambientSample += rumble * thunderEnv * 0.6
                                    thunderDuration--
                                } else {
                                    thunderCountdown--
                                    if (thunderCountdown <= 0) {
                                        thunderDuration = 15000
                                        thunderCountdown = (Math.random() * 250000 + 120000).toInt()
                                    }
                                }

                                // Dark low city rumble
                                phaseSub += 2.0 * PI * 42.0 / sampleRate
                                ambientSample += sin(phaseSub) * 0.15
                            }

                            Chapter.BLACK_HOSPITAL -> {
                                // Fluorescent light buzz (60Hz + 120Hz harmonics + subtle flicker noise)
                                phaseFluo += 2.0 * PI * 60.0 / sampleRate
                                if (phaseFluo > 2.0 * PI) phaseFluo -= 2.0 * PI
                                val fluoBuzz = (sin(phaseFluo) * 0.15 + sin(phaseFluo * 2.0) * 0.08) * (if (Math.random() < 0.005) 0.4 else 1.0)
                                ambientSample += fluoBuzz * 0.5

                                // Low cold dread drone
                                phaseDrone += 2.0 * PI * 48.0 / sampleRate
                                ambientSample += sin(phaseDrone) * 0.2
                            }

                            Chapter.ENDLESS_FOREST -> {
                                // Wind gusts howling through dead pines (slow swept filter)
                                val windFreq = 220.0 + sin(t * 0.3) * 90.0 + sin(t * 0.08) * 60.0
                                phaseWind += 2.0 * PI * windFreq / sampleRate
                                if (phaseWind > 2.0 * PI) phaseWind -= 2.0 * PI
                                val windNoise = (Math.random() - 0.5) * 0.12
                                ambientSample += (sin(phaseWind) * 0.12 + windNoise) * (0.6 + sin(t * 0.25) * 0.4)
                            }

                            Chapter.ECLIPSE_FACILITY -> {
                                // Industrial turbine drone (88Hz) + emergency alarm pulse
                                phaseDrone += 2.0 * PI * 88.0 / sampleRate
                                if (phaseDrone > 2.0 * PI) phaseDrone -= 2.0 * PI
                                ambientSample += sin(phaseDrone) * 0.22

                                // Faint rhythmic red alarm beacon sweep
                                val alarmSweep = sin(t * 1.8)
                                if (alarmSweep > 0.4) {
                                    val alarmFreq = 440.0 + sin(t * 12.0) * 40.0
                                    ambientSample += sin(t * alarmFreq * 2.0 * PI) * 0.06 * (alarmSweep - 0.4)
                                }
                            }

                            Chapter.INVERTED_WORLD -> {
                                // Surreal cosmic dissonant chords (34Hz sub + 114Hz tritone)
                                phaseSub += 2.0 * PI * 34.0 / sampleRate
                                phaseDrone += 2.0 * PI * 114.0 / sampleRate
                                ambientSample += (sin(phaseSub) * 0.3 + sin(phaseDrone) * 0.2) * (0.8 + terror * 0.4)
                            }
                        }

                        // Muffle ambient sound if hiding in locker
                        if (hiding) {
                            ambientSample *= 0.35
                        }

                        // -------------------------------------------------------------
                        // 2. HEARTBEAT "LUB-DUB" PULSE
                        // -------------------------------------------------------------
                        heartbeatPhase += 2.0 * PI * heartRateHz / sampleRate
                        if (heartbeatPhase > 2.0 * PI) heartbeatPhase -= 2.0 * PI

                        var heartSample = 0.0
                        if (heartbeatPhase in 0.0..0.24) {
                            val env = sin(heartbeatPhase / 0.24 * PI)
                            heartSample += sin(heartbeatPhase * 16.0) * env * 0.8
                        } else if (heartbeatPhase in 0.30..0.50) {
                            val env = sin((heartbeatPhase - 0.30) / 0.20 * PI)
                            heartSample += sin(heartbeatPhase * 14.0) * env * 0.55
                        }
                        heartSample *= heartVolume

                        // -------------------------------------------------------------
                        // 3. EXHAUSTED / TENSE BREATHING
                        // -------------------------------------------------------------
                        breathingPhase += 2.0 * PI * breathRateHz / sampleRate
                        if (breathingPhase > 2.0 * PI) breathingPhase -= 2.0 * PI

                        var breathSample = 0.0
                        if (isExhausted || hiding) {
                            val inBreath = sin(breathingPhase)
                            if (inBreath > 0.2) {
                                val breathNoise = (Math.random() - 0.5) * (if (isExhausted) 0.18 else 0.06)
                                breathSample = breathNoise * inBreath
                            }
                        }

                        // -------------------------------------------------------------
                        // 4. TRIGGERED SFX (FOOTSTEPS, FLASHLIGHT, SCREECH, PICKUP)
                        // -------------------------------------------------------------
                        var sfxSample = 0.0

                        // Tactical Flashlight Dual-Click Switch Snap
                        if (isFlashlightTriggered) {
                            flashSfxSamples = 1600
                            isFlashlightTriggered = false
                        }
                        if (flashSfxSamples > 0) {
                            val progress = flashSfxSamples.toDouble() / 1600.0
                            // Two distinct mechanical snap clicks (thumb click-in and spring snap)
                            if (flashSfxSamples > 1100 || flashSfxSamples < 500) {
                                val snapNoise = (Math.random() - 0.5) * 0.6
                                sfxSample += (sin(flashSfxSamples * 0.9) * 0.4 + snapNoise) * progress
                            }
                            flashSfxSamples--
                        }

                        // Surface-Coordinated Footsteps
                        if (isFootstepTriggered) {
                            stepSfxSamples = if (isSprintStep) 2400 else 1800
                            stepIsSprint = isSprintStep
                            isFootstepTriggered = false
                        }
                        if (stepSfxSamples > 0) {
                            val stepEnv = stepSfxSamples.toDouble() / (if (stepIsSprint) 2400.0 else 1800.0)
                            val stepVol = if (stepIsSprint) 0.65 else 0.35

                            when (chapter) {
                                Chapter.DEAD_CITY -> {
                                    // Wet splash on asphalt
                                    val splashNoise = (Math.random() - 0.5) * 0.4 * stepEnv
                                    val thud = sin(stepSfxSamples * 0.06) * 0.5 * stepEnv
                                    sfxSample += (thud + splashNoise) * stepVol
                                }
                                Chapter.BLACK_HOSPITAL -> {
                                    // Resonant hard tile heel strike
                                    val click = sin(stepSfxSamples * 0.18) * 0.6 * stepEnv
                                    sfxSample += click * stepVol
                                }
                                Chapter.ENDLESS_FOREST -> {
                                    // Dead twigs and soil crunch
                                    val crunch = (Math.random() - 0.5) * 0.7 * stepEnv
                                    sfxSample += crunch * stepVol
                                }
                                Chapter.ECLIPSE_FACILITY -> {
                                    // Metal floor grating metallic clink
                                    val clang = sin(stepSfxSamples * 0.28) * 0.45 * stepEnv
                                    sfxSample += clang * stepVol
                                }
                                Chapter.INVERTED_WORLD -> {
                                    // Void displacement muffled pulse
                                    val voidThud = sin(stepSfxSamples * 0.035) * 0.7 * stepEnv
                                    sfxSample += voidThud * stepVol
                                }
                            }
                            stepSfxSamples--
                        }

                        // Monster Eldritch Screech / Static
                        if (isMonsterGrowlTriggered) {
                            growlSfxSamples = 12000
                            isMonsterGrowlTriggered = false
                        }
                        if (growlSfxSamples > 0) {
                            val growlEnv = (growlSfxSamples.toDouble() / 12000.0)
                            val noise = (Math.random() - 0.5) * 0.75
                            val pitch = sin(growlSfxSamples * 0.14 + sin(growlSfxSamples * 0.012) * 6.0)
                            sfxSample += (pitch * 0.45 + noise * 0.55) * growlEnv * 0.95
                            growlSfxSamples--
                        }

                        // Door Open Creak
                        if (isDoorOpenTriggered) {
                            doorSfxSamples = 7000
                            isDoorOpenTriggered = false
                        }
                        if (doorSfxSamples > 0) {
                            val creakEnv = doorSfxSamples.toDouble() / 7000.0
                            val creakFreq = 160.0 + sin(doorSfxSamples * 0.025) * 75.0
                            sfxSample += sin(doorSfxSamples * creakFreq * 2.0 * PI / sampleRate) * creakEnv * 0.42
                            doorSfxSamples--
                        }

                        // Item Pickup Resonance Chime
                        if (isPickupTriggered) {
                            pickupSfxSamples = 4500
                            isPickupTriggered = false
                        }
                        if (pickupSfxSamples > 0) {
                            val chimeEnv = pickupSfxSamples.toDouble() / 4500.0
                            sfxSample += (sin(pickupSfxSamples * 0.28) * 0.4 + sin(pickupSfxSamples * 0.42) * 0.25) * chimeEnv
                            pickupSfxSamples--
                        }

                        // Battery Insertion Click
                        if (isBatteryReloadTriggered) {
                            batterySfxSamples = 2200
                            isBatteryReloadTriggered = false
                        }
                        if (batterySfxSamples > 0) {
                            val battEnv = batterySfxSamples.toDouble() / 2200.0
                            sfxSample += (sin(batterySfxSamples * 0.8) * 0.5 + (Math.random() - 0.5) * 0.4) * battEnv
                            batterySfxSamples--
                        }

                        // Final mix
                        val totalMix = (ambientSample * 0.55 + heartSample + breathSample + sfxSample) * masterVolume
                        val clamped = totalMix.coerceIn(-1.0, 1.0)
                        buffer[i] = (clamped * 32767.0).toInt().toShort()
                    }

                    audioTrack?.write(buffer, 0, chunkSize)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun playFlashlightClick() {
        isFlashlightTriggered = true
    }

    fun playFootstep(isSprint: Boolean = false) {
        isFootstepTriggered = true
        isSprintStep = isSprint
    }

    fun playMonsterGrowl() {
        isMonsterGrowlTriggered = true
    }

    fun playDoorSfx() {
        isDoorOpenTriggered = true
    }

    fun playPickupSfx() {
        isPickupTriggered = true
    }

    fun playBatteryReload() {
        isBatteryReloadTriggered = true
    }

    fun stop() {
        isRunning = false
        audioJob?.cancel()
        audioJob = null
        try {
            audioTrack?.stop()
            audioTrack?.release()
        } catch (_: Exception) {}
        audioTrack = null
    }
}
