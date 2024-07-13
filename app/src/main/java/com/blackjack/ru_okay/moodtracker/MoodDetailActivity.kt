package com.blackjack.ru_okay.moodtracker

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import android.widget.TextView
import com.blackjack.ru_okay.databinding.ActivityMoodDetailBinding

class MoodDetailActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMoodDetailBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMoodDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val mood = intent.getStringExtra("mood")
        binding.moodText.text = mood

        val recommendations = getRecommendations(mood)
        binding.recommendationText.text = recommendations.joinToString("\n")
    }

    private fun getRecommendations(mood: String?): List<String> {
        return when (mood) {
            "Very Good" -> listOf("Keep up the good work!", "Share your happiness with others.")
            "Good" -> listOf("Maintain your positive attitude.", "Consider helping someone in need.")
            "Neutral" -> listOf("Try to find something that makes you happy.", "Engage in a fun activity.")
            "Bad" -> listOf("Take a deep breath and relax.", "Talk to a friend about your feelings.")
            "Very Bad" -> listOf("Seek support from loved ones.", "Consider journaling your thoughts.")
            else -> listOf("No recommendations available.")
        }
    }
}
