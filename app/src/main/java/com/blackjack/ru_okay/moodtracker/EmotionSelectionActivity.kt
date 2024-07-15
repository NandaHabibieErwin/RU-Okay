package com.blackjack.ru_okay.moodtracker

import android.content.Intent
import android.os.Bundle
import android.view.Gravity
import android.view.ViewGroup
import android.widget.GridLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.blackjack.ru_okay.R
import com.blackjack.ru_okay.databinding.ActivityEmotionSelectionBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import java.text.SimpleDateFormat
import java.util.*

class EmotionSelectionActivity : AppCompatActivity() {
    private lateinit var binding: ActivityEmotionSelectionBinding
    private lateinit var database: DatabaseReference
    private val selectedPositiveEmotions = mutableListOf<String>()
    private val selectedNegativeEmotions = mutableListOf<String>()
    private val selectedFactors = mutableListOf<String>()

    private val emotionMap = mapOf(
        "Enthusiast" to "Positive",
        "Happy" to "Positive",
        "Amazed" to "Positive",
        "Excited" to "Positive",
        "Proud" to "Positive",
        "Full of Love" to "Positive",
        "Relaxed" to "Positive",
        "Calm" to "Positive",
        "Satisfied" to "Positive",
        "Relieved" to "Positive",
        "Pleased" to "Positive",
        "Grateful" to "Positive",
        "Angry" to "Negative",
        "Afraid" to "Negative",
        "Stress" to "Negative",
        "Alerted" to "Negative",
        "Annoyed" to "Negative",
        "Embarrassed" to "Negative",
        "Worried" to "Negative",
        "Sluggish" to "Negative",
        "Sad" to "Negative",
        "Sorrowful" to "Negative",
        "Bored" to "Negative",
        "Apathetic" to "Negative",
        "Lonely" to "Negative",
        "Confused" to "Negative",
        "Nervous" to "Negative",
        "Gloomy" to "Negative",
        "Mourning" to "Negative",
        "Heartbroken" to "Negative",
        "Disappointed" to "Negative",
        "Hyperactive" to "Negative"
    )

    private val factors = listOf(
        "Family", "Work", "Friend", "Love", "Health", "Education",
        "Sleep", "Journey", "Relaxing", "Food", "Sports", "Arts",
        "Hobby", "Weather", "Shopping", "Money", "Prayer", "Self-Reflect",
        "Disappoint", "Hyperactive"
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEmotionSelectionBinding.inflate(layoutInflater)
        setContentView(binding.root)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        database = FirebaseDatabase.getInstance().reference
        val userId = FirebaseAuth.getInstance().currentUser?.uid
        val initialMood = intent.getStringExtra("selectedEmotion") ?: "Neutral"

        setupEmotionGrid(binding.positiveEmotionGrid, emotionMap.filterValues { it == "Positive" }.keys, selectedPositiveEmotions)
        setupEmotionGrid(binding.negativeEmotionGrid, emotionMap.filterValues { it == "Negative" }.keys, selectedNegativeEmotions)
        setupEmotionGrid(binding.sourceGrid, factors, selectedFactors)

        binding.doneButton.setOnClickListener {
            val dayOfWeek = SimpleDateFormat("EEE", Locale.ENGLISH).format(Date())
            val currentDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
            val moodKey = "$dayOfWeek-$currentDate"
            val moodData = mapOf(
                "mood" to initialMood,
                "positive" to selectedPositiveEmotions,
                "negative" to selectedNegativeEmotions,
                "factor" to selectedFactors
            )
            userId?.let {
                database.child("users").child(it).child("moods").child(moodKey).setValue(moodData)
                    .addOnSuccessListener {
                        val intent = Intent(this, WeeklyMoodSummaryActivity::class.java)
                        startActivity(intent)
                    }
                    .addOnFailureListener {
                        // Handle failure
                    }
            }
        }
    }

    private fun setupEmotionGrid(gridLayout: GridLayout, emotions: Set<String>, selectedEmotions: MutableList<String>) {
        gridLayout.removeAllViews()
        for (emotion in emotions) {
            val textView = TextView(this)
            textView.text = emotion
            textView.gravity = Gravity.CENTER
            textView.layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
            textView.setOnClickListener {
                if (selectedEmotions.contains(emotion)) {
                    selectedEmotions.remove(emotion)
                    textView.setBackgroundResource(android.R.color.transparent)
                } else {
                    selectedEmotions.add(emotion)
                    textView.setBackgroundResource(R.color.primary_500)
                }
            }
            gridLayout.addView(textView)
        }
    }

    private fun setupEmotionGrid(gridLayout: GridLayout, emotions: List<String>, selectedEmotions: MutableList<String>) {
        gridLayout.removeAllViews()
        for (emotion in emotions) {
            val textView = TextView(this)
            textView.text = emotion
            textView.gravity = Gravity.CENTER
            textView.layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
            textView.setOnClickListener {
                if (selectedEmotions.contains(emotion)) {
                    selectedEmotions.remove(emotion)
                    textView.setBackgroundResource(android.R.color.transparent)
                } else {
                    selectedEmotions.add(emotion)
                    textView.setBackgroundResource(R.color.primary_500)
                }
            }
            gridLayout.addView(textView)
        }
    }
}
