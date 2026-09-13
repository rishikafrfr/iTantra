package com.example.itantra.tts

import android.content.Context
import ai.onnxruntime.OnnxTensor
import ai.onnxruntime.OrtEnvironment
import ai.onnxruntime.OrtSession
import java.io.File

class FastPitchEngine(
    private val context: Context,
    private val language: String
) {

    private val environment = OrtEnvironment.getEnvironment()
    private val session: OrtSession

    init {
        val modelFile = File(
            context.cacheDir,
            "fastpitch_${language}.onnx"
        )

        if (!modelFile.exists()) {
            context.assets
                .open("tts/$language/fastpitch.onnx")
                .use { input ->
                    modelFile.outputStream().use { output ->
                        input.copyTo(
                            output,
                            bufferSize = 1024 * 1024
                        )
                    }
                }
        }

        session = environment.createSession(
            modelFile.absolutePath,
            OrtSession.SessionOptions()
        )

        android.util.Log.d(
            "iTantraTTS",
            "FastPitch model loaded: $language"
        )
    }

    fun generateMel(tokenIds: LongArray): Pair<FloatArray, Int> {

        val inputIds = OnnxTensor.createTensor(
            environment,
            arrayOf(tokenIds)
        )

        val inputLengths = OnnxTensor.createTensor(
            environment,
            longArrayOf(tokenIds.size.toLong())
        )

        val speakerIds = OnnxTensor.createTensor(
            environment,
            longArrayOf(0)
        )

        val inputs = mapOf(
            "input_ids" to inputIds,
            "input_lengths" to inputLengths,
            "speaker_ids" to speakerIds
        )

        val result = session.run(inputs)

        val melTensor = result[0] as OnnxTensor
        val lengthTensor = result[1] as OnnxTensor

        val melLength =
            (lengthTensor.value as LongArray)[0].toInt()

        val mel = FloatArray(melLength * 80)

        melTensor.floatBuffer.get(mel)

        result.close()
        inputIds.close()
        inputLengths.close()
        speakerIds.close()

        android.util.Log.d(
            "iTantraTTS",
            "FastPitch mel generated: $melLength frames"
        )

        return Pair(mel, melLength)
    }

    fun close() {
        session.close()
    }
}