package com.example.itantra.tts

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import java.io.File

class AudioPlayer {

    private var audioTrack: AudioTrack? = null

    fun play(waveform: FloatArray) {

        stop()

        android.util.Log.d(
            "iTantraTTS",
            "WAVEFORM SIZE: ${waveform.size}"
        )

        android.util.Log.d(
            "iTantraTTS",
            "WAVEFORM MIN: ${waveform.minOrNull()}"
        )

        android.util.Log.d(
            "iTantraTTS",
            "WAVEFORM MAX: ${waveform.maxOrNull()}"
        )

        android.util.Log.d(
            "iTantraTTS",
            "WAVEFORM MEAN: ${
                waveform.average()
            }"
        )

        android.util.Log.d(
            "iTantraTTS",
            "WAVEFORM FIRST 20: ${
                waveform.take(20).joinToString(", ")
            }"
        )

        val samples = ShortArray(waveform.size)

        for (i in waveform.indices) {

            val sample =
                waveform[i].coerceIn(-1.0f, 1.0f)

            samples[i] =
                (sample * 32767).toInt().toShort()
        }

        android.util.Log.d(
            "iTantraTTS",
            "PCM MIN: ${samples.minOrNull()}"
        )

        android.util.Log.d(
            "iTantraTTS",
            "PCM MAX: ${samples.maxOrNull()}"
        )

        android.util.Log.d(
            "iTantraTTS",
            "PCM MEAN: ${samples.average()}"
        )

        android.util.Log.d(
            "iTantraTTS",
            "PCM FIRST 20: ${
                samples.take(20).joinToString(", ")
            }"
        )

        // TEMPORARY: do not play yet
    }

    private fun saveAsWav(
        samples: ShortArray,
        sampleRate: Int,
        file: File
    ) {

        val dataSize = samples.size * 2
        val fileSize = 36 + dataSize

        file.parentFile?.mkdirs()

        file.outputStream().use { output ->

            // RIFF header
            output.write("RIFF".toByteArray())
            writeIntLE(output, fileSize)
            output.write("WAVE".toByteArray())

            // fmt chunk
            output.write("fmt ".toByteArray())
            writeIntLE(output, 16)              // chunk size
            writeShortLE(output, 1)             // PCM
            writeShortLE(output, 1)             // mono
            writeIntLE(output, sampleRate)
            writeIntLE(output, sampleRate * 2)  // byte rate
            writeShortLE(output, 2)             // block align
            writeShortLE(output, 16)            // bits/sample

            // data chunk
            output.write("data".toByteArray())
            writeIntLE(output, dataSize)

            // PCM16 little-endian samples
            for (sample in samples) {
                writeShortLE(output, sample.toInt())
            }
        }
    }

    private fun writeIntLE(
        output: java.io.OutputStream,
        value: Int
    ) {
        output.write(value and 0xFF)
        output.write((value shr 8) and 0xFF)
        output.write((value shr 16) and 0xFF)
        output.write((value shr 24) and 0xFF)
    }

    private fun writeShortLE(
        output: java.io.OutputStream,
        value: Int
    ) {
        output.write(value and 0xFF)
        output.write((value shr 8) and 0xFF)
    }

    fun stop() {
        try {
            audioTrack?.stop()
        } catch (_: Exception) {
        }

        audioTrack?.release()
        audioTrack = null
    }
}