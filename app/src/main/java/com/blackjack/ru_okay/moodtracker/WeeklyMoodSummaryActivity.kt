package com.blackjack.ru_okay.moodtracker

import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.blackjack.ru_okay.MainActivity
import com.blackjack.ru_okay.databinding.ActivityWeeklyMoodSummaryBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.blackjack.ru_okay.R
import java.text.SimpleDateFormat
import java.util.*

class WeeklyMoodSummaryActivity : AppCompatActivity() {
    private lateinit var binding: ActivityWeeklyMoodSummaryBinding
    private lateinit var database: DatabaseReference
    private val dayMoodMap = mutableMapOf<String, Map<String, Any>>()

    // Mapping of emotions to icons
    private val emotionIconMap = mapOf(
        "Very Good" to R.drawable.smiling,
        "Good" to R.drawable.blushing,
        "Neutral" to R.drawable.neutral,
        "Bad" to R.drawable.sad,
        "Very Bad" to R.drawable.disappointed
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityWeeklyMoodSummaryBinding.inflate(layoutInflater)
        setContentView(binding.root)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        database = FirebaseDatabase.getInstance().reference
        val userId = FirebaseAuth.getInstance().currentUser?.uid

        // Initialize the past seven days
        val pastWeek = getPastWeek()

        // Add TextViews for each day to the grid and set click listeners
        for (day in pastWeek) {
            val textView = TextView(this)
            textView.text = day.split("-")[0] // Display only the day name
            textView.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.neutral, 0, 0) // Default icon
            textView.setOnClickListener { displayMoodDetails(day) }
            binding.weeklyMoodGrid.addView(textView)

            // Fetch mood data from Firebase for each day of the past week
            userId?.let { uid ->
                database.child("users").child(uid).child("moods").child(day).addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        val moodMap = snapshot.value as? Map<String, Any>
                        if (moodMap != null) {
                            dayMoodMap[day] = moodMap
                            val mood = moodMap["mood"] as? String
                            mood?.let {
                                textView.setCompoundDrawablesWithIntrinsicBounds(0, emotionIconMap[mood] ?: R.drawable.neutral, 0, 0)
                            }
                        } else {
                            textView.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.neutral, 0, 0)
                        }
                    }

                    override fun onCancelled(error: DatabaseError) {
                        // Handle error
                    }
                })
            }
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                val intent = Intent(this, MainActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
                startActivity(intent)
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun displayMoodDetails(day: String) {
        val moodData = dayMoodMap[day] ?: mapOf("mood" to "Neutral", "positive" to emptyList<String>(), "negative" to emptyList<String>(), "factor" to emptyList<String>())
        val dayDate = SimpleDateFormat("EEE-yyyy-MM-dd", Locale.getDefault()).parse(day)
        binding.selectedDayText.text = SimpleDateFormat("EEEE, MMMM dd yyyy", Locale.getDefault()).format(dayDate)

        val mood = moodData["mood"] as? String
        binding.selectedMoodText.text = mood ?: "Neutral"

        // Set mood icon
        val moodIconRes = emotionIconMap[mood] ?: R.drawable.neutral
        binding.selectedMoodIcon.setImageResource(moodIconRes)

        // Display combined emotions
        binding.emotionLayout.removeAllViews()
        val positiveEmotions = moodData["positive"] as? List<String> ?: emptyList()
        val negativeEmotions = moodData["negative"] as? List<String> ?: emptyList()
        val combinedEmotions = positiveEmotions + negativeEmotions

        for (emotion in combinedEmotions) {
            val textView = TextView(this)
            textView.text = emotion
            textView.setBackgroundResource(R.drawable.buttonborder)
            textView.setPadding(8, 4, 8, 4)
            textView.setTextColor(resources.getColor(R.color.primary_500, null))
            val layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            layoutParams.setMargins(4, 4, 4, 4)
            textView.layoutParams = layoutParams
            binding.emotionLayout.addView(textView)
        }

        // Display factors
        binding.factorLayout.removeAllViews()
        val factors = moodData["factor"] as? List<String> ?: emptyList()

        for (factor in factors) {
            val textView = TextView(this)
            textView.text = factor
            textView.setBackgroundResource(R.drawable.buttonborder)
            textView.setPadding(8, 4, 8, 4)
            textView.setTextColor(resources.getColor(R.color.primary_500, null))
            val layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            layoutParams.setMargins(4, 4, 4, 4)
            textView.layoutParams = layoutParams
            binding.factorLayout.addView(textView)
        }
    }

    private fun getPastWeek(): List<String> {
        val dateFormat = SimpleDateFormat("EEE-yyyy-MM-dd", Locale.getDefault())
        val calendar = Calendar.getInstance()
        val pastWeek = mutableListOf<String>()

        // Get today and the past six days
        for (i in 0..6) {
            pastWeek.add(dateFormat.format(calendar.time))
            calendar.add(Calendar.DAY_OF_MONTH, -1)
        }

        return pastWeek
    }
}
