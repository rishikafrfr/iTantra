package com.example.itantra

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.os.Bundle
import android.speech.RecognizerIntent
import android.view.Gravity
import android.view.View
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.Spinner
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.itantra.tts.AudioPlayer
import com.example.itantra.tts.FastPitchEngine
import com.example.itantra.tts.HiFiGanEngine
import com.example.itantra.tts.Tokenizer

class ChatActivity : AppCompatActivity() {

    private lateinit var messagesContainer: LinearLayout
    private lateinit var messagesScroll: ScrollView
    private lateinit var messageInput: EditText
    private lateinit var languageSpinner: Spinner

    private lateinit var audioPlayer: AudioPlayer

    private val speechRequestCode = 1001

    private val languages = arrayOf(
        "हिन्दी (Hindi)",
        "ગુજરાતી (Gujarati)",
        "मराठी (Marathi)",
        "ಕನ್ನಡ (Kannada)",
        "മലയാളം (Malayalam)",
        "தமிழ் (Tamil)",
        "తెలుగు (Telugu)",
        "ଓଡ଼ିଆ (Odia)",
        "বাংলা (Bengali)",
        "English"
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_chat)

        messagesContainer = findViewById(R.id.messagesContainer)
        messagesScroll = findViewById(R.id.messagesScroll)
        messageInput = findViewById(R.id.messageInput)
        languageSpinner = findViewById(R.id.languageSpinner)
        audioPlayer = AudioPlayer()

        setupLanguageSpinner()

        findViewById<View>(R.id.btnSend).setOnClickListener {

            val message = messageInput.text.toString().trim()

            if (message.isNotEmpty()) {
                sendMessage(message)
                messageInput.text.clear()
            }
        }

        findViewById<View>(R.id.btnMic).setOnClickListener {
            startSpeechRecognition()
        }
    }

    private fun setupLanguageSpinner() {

        val adapter = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_item,
            languages
        )

        adapter.setDropDownViewResource(
            android.R.layout.simple_spinner_dropdown_item
        )

        languageSpinner.adapter = adapter
    }

    private fun sendMessage(message: String) {

        addMessage(
            message = message,
            isMine = true,
            tick = "✓"
        )

        speakMessage(message)

        // Later connect this to Bluetooth/Wi-Fi transmission.
    }

    private fun speakMessage(message: String) {

        val selectedLanguage =
            languageSpinner.selectedItem.toString()

        if (!selectedLanguage.contains("English")) {
            return
        }

        Thread {

            try {

                android.util.Log.d(
                    "iTantraTTS",
                    "TTS INPUT TEXT: $message"
                )

                val tokenizer = Tokenizer()

                val tokenIds =
                    tokenizer.tokenizeEnglish(message)

                val fastPitch =
                    FastPitchEngine(this, "en")

                val (mel, melLength) =
                    fastPitch.generateMel(tokenIds)

                var melMin = Float.MAX_VALUE
                var melMax = -Float.MAX_VALUE
                var melSum = 0.0

                for (value in mel) {
                    if (value < melMin) melMin = value
                    if (value > melMax) melMax = value
                    melSum += kotlin.math.abs(value.toDouble())
                }

                android.util.Log.d(
                    "iTantraTTS",
                    "MEL MIN: $melMin"
                )

                android.util.Log.d(
                    "iTantraTTS",
                    "MEL MAX: $melMax"
                )

                android.util.Log.d(
                    "iTantraTTS",
                    "MEL AVG ABS: ${melSum / mel.size}"
                )

                val hiFiGan =
                    HiFiGanEngine(this)

                val waveform =
                    hiFiGan.synthesize(
                        mel,
                        melLength
                    )

                runOnUiThread {
                    audioPlayer.play(waveform)

                    android.util.Log.d(
                        "iTantraTTS",
                        "TTS AUDIO PLAYBACK STARTED"
                    )
                }

                fastPitch.close()
                hiFiGan.close()

            } catch (e: Exception) {

                android.util.Log.e(
                    "iTantraTTS",
                    "TTS FAILED",
                    e
                )
            }

        }.start()
    }

    fun receiveMessage(message: String) {

        runOnUiThread {

            addMessage(
                message = message,
                isMine = false,
                tick = ""
            )
        }
    }

    private fun addMessage(
        message: String,
        isMine: Boolean,
        tick: String
    ) {

        val row = LinearLayout(this)

        row.orientation = LinearLayout.HORIZONTAL
        row.gravity = if (isMine) {
            Gravity.END
        } else {
            Gravity.START
        }

        val bubble = TextView(this)

        bubble.text = message
        bubble.textSize = 17f
        bubble.setTextColor(Color.WHITE)
        bubble.setPadding(20, 14, 20, 14)

        if (isMine) {

            bubble.setBackgroundColor(
                Color.rgb(32, 85, 65)
            )

        } else {

            bubble.setBackgroundColor(
                Color.rgb(48, 48, 52)
            )
        }

        val params = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.WRAP_CONTENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )

        params.topMargin = 5
        params.bottomMargin = 5

        if (isMine) {
            params.leftMargin = 80
            params.rightMargin = 8
        } else {
            params.leftMargin = 8
            params.rightMargin = 80
        }

        row.addView(bubble, params)

        if (isMine) {

            val tickView = TextView(this)

            tickView.text = tick
            tickView.textSize = 13f
            tickView.setTextColor(
                Color.rgb(180, 220, 190)
            )

            row.addView(tickView)
        }

        messagesContainer.addView(row)

        messagesScroll.post {
            messagesScroll.fullScroll(View.FOCUS_DOWN)
        }
    }

    private fun startSpeechRecognition() {

        if (
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.RECORD_AUDIO
            ) != PackageManager.PERMISSION_GRANTED
        ) {

            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.RECORD_AUDIO),
                2001
            )

            return
        }

        val selectedLanguage =
            languageSpinner.selectedItem.toString()

        val languageCode = when {
            selectedLanguage.contains("Hindi") -> "hi-IN"
            selectedLanguage.contains("Gujarati") -> "gu-IN"
            selectedLanguage.contains("Marathi") -> "mr-IN"
            selectedLanguage.contains("Kannada") -> "kn-IN"
            selectedLanguage.contains("Malayalam") -> "ml-IN"
            selectedLanguage.contains("Tamil") -> "ta-IN"
            selectedLanguage.contains("Telugu") -> "te-IN"
            selectedLanguage.contains("Odia") -> "or-IN"
            selectedLanguage.contains("Bengali") -> "bn-IN"
            else -> "en-IN"
        }

        val intent = Intent(
            RecognizerIntent.ACTION_RECOGNIZE_SPEECH
        )

        intent.putExtra(
            RecognizerIntent.EXTRA_LANGUAGE_MODEL,
            RecognizerIntent.LANGUAGE_MODEL_FREE_FORM
        )

        intent.putExtra(
            RecognizerIntent.EXTRA_LANGUAGE,
            languageCode
        )

        intent.putExtra(
            RecognizerIntent.EXTRA_PROMPT,
            "Speak in $selectedLanguage"
        )

        startActivityForResult(
            intent,
            speechRequestCode
        )
    }

    override fun onActivityResult(
        requestCode: Int,
        resultCode: Int,
        data: Intent?
    ) {

        super.onActivityResult(
            requestCode,
            resultCode,
            data
        )

        if (
            requestCode == speechRequestCode &&
            resultCode == Activity.RESULT_OK &&
            data != null
        ) {

            val results = data.getStringArrayListExtra(
                RecognizerIntent.EXTRA_RESULTS
            )

            if (!results.isNullOrEmpty()) {

                val spokenText = results[0]

                sendMessage(spokenText)
            }
        }
    }
}