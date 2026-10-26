package com.example.game.audio

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.math.PI
import kotlin.math.sin
import kotlin.random.Random

/**
 * Procedural Submarine & Abyssal Audio Synthesizer
 * Generates zero-latency deep-sea sound effects and ambient sonar using AudioTrack.
 * Requires 0 external asset files, works 100% offline and online, and will never fail to load.
 */
object GameAudioManager {
    private const val TAG = "GameAudioManager"
    private const val SAMPLE_RATE = 22050

    var isMuted: Boolean = false
    private val audioScope = CoroutineScope(Dispatchers.Default)
    private var ambientJob: Job? = null

    init {
        startAmbientSonarLoop()
    }

    /**
     * Synthesize and play raw PCM audio buffer on a lightweight AudioTrack.
     */
    private fun playPcm(buffer: ShortArray) {
        if (isMuted) return
        audioScope.launch {
            try {
                val minBufferSize = AudioTrack.getMinBufferSize(
                    SAMPLE_RATE,
                    AudioFormat.CHANNEL_OUT_MONO,
                    AudioFormat.ENCODING_PCM_16BIT
                )
                val bufferSize = maxOf(buffer.size * 2, minBufferSize)

                val track = AudioTrack.Builder()
                    .setAudioAttributes(
                        AudioAttributes.Builder()
                            .setUsage(AudioAttributes.USAGE_GAME)
                            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                            .build()
                    )
                    .setAudioFormat(
                        AudioFormat.Builder()
                            .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                            .setSampleRate(SAMPLE_RATE)
                            .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                            .build()
                    )
                    .setBufferSizeInBytes(bufferSize)
                    .setTransferMode(AudioTrack.MODE_STATIC)
                    .build()

                track.write(buffer, 0, buffer.size)
                track.play()

                // Release track after playback completes
                val durationMs = (buffer.size * 1000L) / SAMPLE_RATE + 100L
                delay(durationMs)
                track.stop()
                track.release()
            } catch (e: Exception) {
                Log.w(TAG, "AudioTrack playback notice: ${e.message}")
            }
        }
    }

    /**
     * Sonar Torpedo / Harpoon launch sound (downward acoustic frequency chirp).
     */
    fun playShoot() {
        val numSamples = (SAMPLE_RATE * 0.18f).toInt()
        val buffer = ShortArray(numSamples)
        var phase = 0.0

        for (i in 0 until numSamples) {
            val progress = i.toDouble() / numSamples
            val freq = 850.0 - (progress * 550.0) // 850Hz down to 300Hz
            val envelope = (1.0 - progress) * (1.0 - progress) // decay
            phase += 2.0 * PI * freq / SAMPLE_RATE
            val sample = (sin(phase) * envelope * 24000).toInt().coerceIn(-32767, 32767)
            buffer[i] = sample.toShort()
        }
        playPcm(buffer)
    }

    /**
     * Thruster Boost sound (Low frequency cavitation rumble with white noise).
     */
    fun playBoost() {
        val numSamples = (SAMPLE_RATE * 0.25f).toInt()
        val buffer = ShortArray(numSamples)
        var phase = 0.0

        for (i in 0 until numSamples) {
            val progress = i.toDouble() / numSamples
            val freq = 90.0 + sin(progress * 10.0) * 25.0
            val noise = (Random.nextDouble() * 2.0 - 1.0) * 0.35
            val envelope = (1.0 - progress)
            phase += 2.0 * PI * freq / SAMPLE_RATE
            val sample = ((sin(phase) * 0.65 + noise) * envelope * 20000).toInt().coerceIn(-32767, 32767)
            buffer[i] = sample.toShort()
        }
        playPcm(buffer)
    }

    /**
     * Impact explosion / Hit sound.
     */
    fun playHit() {
        val numSamples = (SAMPLE_RATE * 0.22f).toInt()
        val buffer = ShortArray(numSamples)
        var phase = 0.0

        for (i in 0 until numSamples) {
            val progress = i.toDouble() / numSamples
            val freq = 220.0 - progress * 160.0
            val noise = (Random.nextDouble() * 2.0 - 1.0) * 0.6
            val envelope = (1.0 - progress) * (1.0 - progress)
            phase += 2.0 * PI * freq / SAMPLE_RATE
            val sample = ((sin(phase) * 0.4 + noise) * envelope * 26000).toInt().coerceIn(-32767, 32767)
            buffer[i] = sample.toShort()
        }
        playPcm(buffer)
    }

    /**
     * Boss Defeated / Victory Fanfare (Triumphant 4-note ascending chord C5, E5, G5, C6).
     */
    fun playVictory() {
        val notes = doubleArrayOf(523.25, 659.25, 783.99, 1046.50) // C5, E5, G5, C6
        val noteSamples = (SAMPLE_RATE * 0.16f).toInt()
        val totalSamples = noteSamples * notes.size
        val buffer = ShortArray(totalSamples)

        for ((nIndex, freq) in notes.withIndex()) {
            var phase = 0.0
            val offset = nIndex * noteSamples
            for (i in 0 until noteSamples) {
                val progress = i.toDouble() / noteSamples
                val envelope = 1.0 - (progress * 0.6)
                phase += 2.0 * PI * freq / SAMPLE_RATE
                // Add rich harmonics
                val wave = sin(phase) * 0.7 + sin(phase * 2.0) * 0.3
                val sample = (wave * envelope * 22000).toInt().coerceIn(-32767, 32767)
                buffer[offset + i] = sample.toShort()
            }
        }
        playPcm(buffer)
    }

    /**
     * Ambient Sonar ping and abyssal water atmosphere.
     */
    private fun startAmbientSonarLoop() {
        ambientJob?.cancel()
        ambientJob = audioScope.launch {
            while (isActive) {
                delay(7000) // Periodic sonar ping every 7 seconds
                if (!isMuted) {
                    playSonarPing()
                }
            }
        }
    }

    private fun playSonarPing() {
        val numSamples = (SAMPLE_RATE * 0.35f).toInt()
        val buffer = ShortArray(numSamples)
        var phase = 0.0
        val freq = 1200.0

        for (i in 0 until numSamples) {
            val progress = i.toDouble() / numSamples
            val envelope = (1.0 - progress) * (1.0 - progress) * (1.0 - progress)
            phase += 2.0 * PI * freq / SAMPLE_RATE
            val sample = (sin(phase) * envelope * 12000).toInt().coerceIn(-32767, 32767)
            buffer[i] = sample.toShort()
        }
        playPcm(buffer)
    }
}
