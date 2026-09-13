package com.example.itantra.tts

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack

class AudioPlayer {

    private var audioTrack: AudioTrack? = null

    fun play(waveform: FloatArray) {

        stop()

        val samples = ShortArray(waveform.size)

        for (i in waveform.indices) {
            val sample = waveform[i].coerceIn(-1.0f, 1.0f)
            samples[i] = (sample * 32767).toInt().toShort()
        }

        val minBufferSize = AudioTrack.getMinBufferSize(
            22050,
            AudioFormat.CHANNEL_OUT_MONO,
            AudioFormat.ENCODING_PCM_16BIT
        )

        val bufferSize = maxOf(
            minBufferSize,
            samples.size * 2
        )

        audioTrack = AudioTrack.Builder()
            .setAudioAttributes(
                AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_MEDIA)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                    .build()
            )
            .setAudioFormat(
                AudioFormat.Builder()
                    .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                    .setSampleRate(22050)
                    .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                    .build()
            )
            .setBufferSizeInBytes(bufferSize)
            .setTransferMode(AudioTrack.MODE_STATIC)
            .build()

        val written = audioTrack?.write(
            samples,
            0,
            samples.size
        )

        android.util.Log.d(
            "iTantraTTS",
            "Samples written: $written / ${samples.size}"
        )

        android.util.Log.d(
            "iTantraTTS",
            "AudioTrack state: ${audioTrack?.state}"
        )

        audioTrack?.play()

        android.util.Log.d(
            "iTantraTTS",
            "PCM playback started"
        )
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