package com.example.itantra.tts

import android.content.Context
import org.tensorflow.lite.Interpreter
import java.io.File

class HiFiGanEngine(context: Context) {

    private val interpreter: Interpreter

    init {
        val modelFile = File(
            context.cacheDir,
            "hifigan_en.tflite"
        )

        if (!modelFile.exists()) {
            context.assets
                .open("tts/en/hifigan_fp16.tflite")
                .use { input ->
                    modelFile.outputStream().use { output ->
                        input.copyTo(
                            output,
                            bufferSize = 1024 * 1024
                        )
                    }
                }
        }

        interpreter = Interpreter(modelFile)

        android.util.Log.d(
            "iTantraTTS",
            "HiFi-GAN model loaded successfully"
        )
    }

    fun synthesize(
        mel: FloatArray,
        melLength: Int
    ): FloatArray {

        require(mel.size == melLength * 80)

        interpreter.resizeInput(
            0,
            intArrayOf(1, melLength, 80)
        )

        interpreter.allocateTensors()

        val input = Array(1) {
            Array(melLength) {
                FloatArray(80)
            }
        }

        for (t in 0 until melLength) {
            for (m in 0 until 80) {
                input[0][t][m] = mel[t * 80 + m]
            }
        }

        val output = Array(1) {
            Array(melLength * 256) {
                FloatArray(1)
            }
        }

        interpreter.run(input, output)

        val waveform = FloatArray(melLength * 256)

        for (i in waveform.indices) {
            waveform[i] = output[0][i][0]
        }

        android.util.Log.d(
            "iTantraTTS",
            "HiFi-GAN inference successful"
        )

        android.util.Log.d(
            "iTantraTTS",
            "Waveform samples: ${waveform.size}"
        )

        android.util.Log.d(
            "iTantraTTS",
            "WAVEFORM FIRST 100: ${
                waveform.take(100).joinToString(", ")
            }"
        )

        return waveform
    }

    fun close() {
        interpreter.close()
    }
}