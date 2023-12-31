package com.example.clarity.sets.activities

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.graphics.Color
import android.media.*
import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.util.Log
import android.view.View
import android.view.View.GONE
import android.view.View.INVISIBLE
import android.view.View.VISIBLE
import android.widget.Button
import android.widget.ImageButton
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.core.app.ActivityCompat
import androidx.lifecycle.lifecycleScope
import com.example.clarity.sdk.ClaritySDK
import com.example.clarity.R
import com.example.clarity.SessionManager
import com.example.clarity.sdk.CreateAttemptResponse
import com.example.clarity.sets.data.Card
import com.example.clarity.sets.data.Set
import com.example.clarity.sets.data.SetCategory
import com.example.clarity.sets.audio.WavRecorder
import com.google.gson.Gson
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import okhttp3.MediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Response
import java.io.*
import java.io.File
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.charset.Charset
import java.util.Locale

class TestSetActivity() : AppCompatActivity() {

    // Recorder and Player
    private var player: TextToSpeech? = null
    private val wavRecorder = WavRecorder(this)

    // Session Manager
    private val sessionManager: SessionManager by lazy { SessionManager(this) }

    // Toggle to check if we are currently recording
    private var isRecording = false

    // Variable to restrict users to one attempt per card
    private var recordingCompleted = false

    // ClaritySDK api for endpoint calls
    private val api = ClaritySDK().apiService

    // Index that stores the current card being displayed
    private var index = 0

    // List that stores missing words
    var failedWords: List<String>? = listOf()

    // User and Set
    var userid = 0
    var set: Set = Set(0, "", 0, mutableListOf<Card>(), 0, SetCategory.COMMUNITY_SET)

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Request permission to record
        ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.RECORD_AUDIO), 0)

        // Set View
        setContentView(R.layout.activity_test_set)

        // Create TTS Object
        player = TextToSpeech(this, TextToSpeech.OnInitListener {
            if (it == TextToSpeech.SUCCESS) {
                player!!.language = Locale.ENGLISH
            } else {
                Log.d("TTS ERROR", it.toString())
            }
        })

        // Get Set that was started
        val intent = intent
        val setJson = intent.getStringExtra("set")
        val gson = Gson()
        set = gson.fromJson(setJson, Set::class.java)

        // Get all view components
        val tvTitle = findViewById<TextView>(R.id.tvTitle)
        val iBtnClose = findViewById<ImageButton>(R.id.iBtnClose)
        val iBtnMic = findViewById<ImageButton>(R.id.iBtnMic)
        val iBtnSpeaker = findViewById<ImageButton>(R.id.iBtnSpeaker)
        val iBtnNext = findViewById<ImageButton>(R.id.iBtnNext)
        val cvPopUp = findViewById<CardView>(R.id.cvPopUp)
        val cvCompletedScreen = findViewById<CardView>(R.id.cvCompletedScreen)
        val btnReturn = findViewById<Button>(R.id.btnReturn)

        // Get Session Context Variables
        lifecycleScope.launch {
            userid = sessionManager.getUserId()
        }

        // Initialize Title
        tvTitle.text = set.title

        // Handle Close button, which automatically closes test session
        iBtnClose.setOnClickListener {
            finish()
        }

        // Handle Speaker button click
        iBtnSpeaker.setOnClickListener {
            if (index < set.cards.size) {
                player!!.speak(set.cards[index].phrase, TextToSpeech.QUEUE_ADD, null, null)
            }
        }

        // Handle Mic button click
        iBtnMic.setOnClickListener {
            // Ensure we haven't already recorded an attempt for this card
            if (!recordingCompleted) {
                // CASE 1: Not Recording -> Recording
                if (!isRecording) {
                    // Change UI of button
                    iBtnMic.setBackgroundResource(R.drawable.setclosebutton)
                    iBtnMic.setImageResource(R.drawable.baseline_mic_24_white)

                    // Start recording, while storing recording in file
                    wavRecorder.startRecording("audio_test.wav", true)

                // CASE 2: Recording -> Not Recording
                } else {
                    // Change UI of button
                    iBtnMic.setBackgroundResource(R.drawable.fadedroundcorner)
                    iBtnMic.setImageResource(R.drawable.baseline_mic_24)

                    // Stop Recording
                    wavRecorder.stopRecording()

                    // Return Accuracy Score and Display Popup
                    val isComplete = getAccuracyScore(File(this.filesDir, "audio_test.wav"))
                    displayMessagePopup(isComplete)

                    if (failedWords != null) {
                        // Indicate that we have already recorded an attempt for this card
                        recordingCompleted = true
                        iBtnMic.isEnabled = false

                        // Make next button visible
                        iBtnNext.visibility = VISIBLE

                        // Increment Set Progress
                        set.progress = index + 1

                        // Update Progress Components
                        val progressBar = findViewById<ProgressBar>(R.id.progressBar)
                        val tvCompletedCount = findViewById<TextView>(R.id.tvCompletedPhrases)
                        val tvPercentComplete = findViewById<TextView>(R.id.tvPercentComplete)
                        progressBar.progress = ((index + 1) * 100) / set.cards.size
                        tvCompletedCount.text = "${index + 1} Complete"
                        tvPercentComplete.text = "${((index + 1) * 100) / set.cards.size} %"
                    } else {
                        iBtnMic.setBackgroundResource(R.drawable.roundcorner)
                    }
                }

                // Toggle isRecording Value
                isRecording = !isRecording
            }
        }

        // Handle Forward Navigation
        iBtnNext.setOnClickListener {
            iBtnMic.isEnabled = true
            // Increment Index
            index++

            if (index < set.cards.size) {
                iBtnMic.setBackgroundResource(R.drawable.roundcorner)
                iBtnMic.setImageResource(R.drawable.baseline_mic_24)
                iBtnNext.visibility = INVISIBLE
                cvPopUp.visibility = GONE
                loadCard(set.cards[index])
                recordingCompleted = false
            } else {
                cvCompletedScreen.visibility = VISIBLE
                iBtnClose.isEnabled = false
                iBtnNext.visibility = INVISIBLE
                iBtnMic.isEnabled = false
            }
        }

        // Handle Return after completion
        btnReturn.setOnClickListener {
            cvCompletedScreen.visibility = GONE
            iBtnClose.isEnabled = true
            iBtnMic.isEnabled = true
            finish()
        }

        // Load Initial Card
        loadCard(set.cards[index])
    }

    // Handles setting the UI for the current card
    private fun loadCard(card: Card) {
        val tvCardPhrase = findViewById<TextView>(R.id.tvCardPhrase)
        tvCardPhrase.text = card.phrase
    }

    // Returns accuracy score
    private fun getAccuracyScore(wavFile: File): Boolean {
        // Convert file to MultipartBody.Part
        val requestFile = RequestBody.create(MediaType.parse("audio/*"), wavFile)
        val part = MultipartBody.Part.createFormData("audio", wavFile.name, requestFile)

        // Make attempt call
        val response: Response<CreateAttemptResponse> = runBlocking {
            return@runBlocking api.attemptCard(userid, set.cards[index].id, set.id, part)
        }
        // Handle failed response case
        if (response.body() == null || response.body()!!.metadata == null) {
            failedWords = null
            return false
        }

        Log.d("response metadata", response.body()!!.metadata.toString())

        val omissions = response.body()!!.metadata?.omissions!!
        val mispronunciations = response.body()!!.metadata?.mispronunciations!!
        failedWords = omissions + mispronunciations
        // Return with isComplete
        return response.body()?.metadata!!.is_complete
    }

    // Display popup
    @SuppressLint("SetTextI18n")
    private fun displayMessagePopup(isComplete: Boolean)  {
        // Get Components
        val cvPopUp = findViewById<CardView>(R.id.cvPopUp)
        val tvResultMessage = findViewById<TextView>(R.id.tvResultMessage)

        // Set Message Properties based on Difficulty Threshold
        if (isComplete)  {
            cvPopUp.backgroundTintList = getColorStateList(R.color.passed)
            tvResultMessage.text = resources.getString(R.string.great_job)
        } else if (failedWords == null) {
            cvPopUp.backgroundTintList = getColorStateList(R.color.error)
            tvResultMessage.text = "Whoops, No audio was detected, ensure that your microphone is enabled and try again"
        } else {
            cvPopUp.backgroundTintList = getColorStateList(R.color.failed)
            tvResultMessage.text =  resources.getString(R.string.just_a_little_off_keep_practicing)
            if (failedWords!!.isNotEmpty()) {
                tvResultMessage.text = tvResultMessage.text as String + "\n The following words weren't picked up: " + getFailedWords(
                    failedWords!!
                )
            }
        }

        // Make Popup visible
        cvPopUp.visibility = View.VISIBLE
    }

    private fun getFailedWords(failedWords: List<String>): String {
        var result = ""
        for (i in failedWords.indices) {
            result += failedWords[i]
            if (i != failedWords.size - 1) {
                result += ", "
            }
        }

        return result
    }
}