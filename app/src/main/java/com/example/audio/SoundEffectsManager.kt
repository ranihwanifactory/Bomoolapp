package com.example.audio

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlin.math.PI
import kotlin.math.exp
import kotlin.math.sin

class SoundEffectsManager {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    @Volatile
    var enabled: Boolean = true

    fun playPop() {
        if (!enabled) return
        playNotes(listOf(ToneSpec(587.33, 60, 0.25)))
    }

    fun playRadarPing() {
        if (!enabled) return
        playNotes(listOf(ToneSpec(880.0, 90, 0.18), ToneSpec(1174.66, 110, 0.14)))
    }

    fun playWrongBuzz() {
        if (!enabled) return
        playNotes(listOf(ToneSpec(261.63, 95, 0.24), ToneSpec(196.0, 140, 0.24)))
    }

    fun playCollectSparkle(isHighRarity: Boolean = false) {
        if (!enabled) return
        val notes = if (isHighRarity) {
            listOf(
                ToneSpec(523.25, 75, 0.30),
                ToneSpec(659.25, 75, 0.30),
                ToneSpec(783.99, 75, 0.32),
                ToneSpec(1046.50, 150, 0.36)
            )
        } else {
            listOf(
                ToneSpec(587.33, 70, 0.28),
                ToneSpec(880.00, 130, 0.32)
            )
        }
        playNotes(notes)
    }

    fun playLevelUpFanfare() {
        if (!enabled) return
        playNotes(
            listOf(
                ToneSpec(523.25, 90, 0.32),
                ToneSpec(659.25, 90, 0.32),
                ToneSpec(783.99, 90, 0.34),
                ToneSpec(1046.50, 140, 0.38),
                ToneSpec(1318.51, 240, 0.40)
            )
        )
    }

    fun playRoundFinish() {
        if (!enabled) return
        playNotes(
            listOf(
                ToneSpec(783.99, 100, 0.30),
                ToneSpec(659.25, 100, 0.28),
                ToneSpec(1046.50, 220, 0.35)
            )
        )
    }

    private data class ToneSpec(val freqHz: Double, val durationMs: Int, val volume: Double)

    private fun playNotes(notes: List<ToneSpec>) {
        scope.launch {
            try {
                val sampleRate = 22050
                val totalSamples = notes.sumOf { (sampleRate * it.durationMs) / 1000 }
                if (totalSamples <= 0) return@launch
                val pcm = ShortArray(totalSamples)
                var cursor = 0
                for (note in notes) {
                    val count = (sampleRate * note.durationMs) / 1000
                    for (i in 0 until count) {
                        val t = i.toDouble() / sampleRate
                        val env = exp(-4.5 * i / count.coerceAtLeast(1))
                        val wave = sin(2.0 * PI * note.freqHz * t) + 0.25 * sin(4.0 * PI * note.freqHz * t)
                        val sample = (wave * env * note.volume * Short.MAX_VALUE)
                            .toInt()
                            .coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt())
                        pcm[cursor++] = sample.toShort()
                    }
                }
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
                            .setSampleRate(sampleRate)
                            .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                            .build()
                    )
                    .setBufferSizeInBytes(pcm.size * 2)
                    .setTransferMode(AudioTrack.MODE_STATIC)
                    .build()
                track.write(pcm, 0, pcm.size)
                track.play()
                val totalDurationMs = notes.sumOf { it.durationMs }.toLong() + 120L
                kotlinx.coroutines.delay(totalDurationMs)
                track.release()
            } catch (_: Throwable) {
                // Ignore audio errors on headless/muted test devices
            }
        }
    }
}
