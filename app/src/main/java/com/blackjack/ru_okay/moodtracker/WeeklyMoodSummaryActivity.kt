package com.blackjack.ru_okay.moodtracker

import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.blackjack.ru_okay.MainActivity
import com.blackjack.ru_okay.databinding.ActivityWeeklyMoodSummaryBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.blackjack.ru_okay.R

class WeeklyMoodSummaryActivity : AppCompatActivity() {
    private lateinit var binding: ActivityWeeklyMoodSummaryBinding
    private lateinit var database: DatabaseReference
    private val dayMoodMap = mutableMapOf<String, Map<String, List<String>>>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityWeeklyMoodSummaryBinding.inflate(layoutInflater)
        setContentView(binding.root)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        database = FirebaseDatabase.getInstance().reference
        val userId = FirebaseAuth.getInstance().currentUser?.uid

        val days = arrayOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")
        val moodIcons = arrayOf(
            R.drawable.smiling, R.drawable.blushing, R.drawable.neutral,
            R.drawable.sad, R.drawable.smiling, R.drawable.sad, R.drawable.smiling
        )

        for ((index, day) in days.withIndex()) {
            val textView = TextView(this)
            textView.text = day
            textView.setCompoundDrawablesWithIntrinsicBounds(0, moodIcons[index], 0, 0)
            textView.setOnClickListener { displayMoodDetails(day) }
            binding.weeklyMoodGrid.addView(textView)
        }

        // Fetch mood data from Firebase
        userId?.let {
            database.child("users").child(it).child("moods").addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val moodMap = snapshot.getValue(object : GenericTypeIndicator<Map<String, Map<String, List<String>>>>() {})
                    if (moodMap != null) {
                        dayMoodMap.putAll(moodMap)
                        displayMoodDetails("Sun") // Display the most recent day by default
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    // Handle error
                }
            })
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
        val mood = dayMoodMap[day] ?: mapOf("positive" to listOf("Very Bad"), "negative" to emptyList(), "factor" to emptyList())
        binding.selectedDayText.text = day
        binding.selectedMoodText.text = mood["positive"]?.joinToString(", ") ?: "Very Bad"

        binding.emotionLayout.removeAllViews()
        val emotions = mood["negative"] ?: listOf("None")
        for (emotion in emotions) {
            val textView = TextView(this)
            textView.text = emotion
            textView.setBackgroundResource(R.color.primary_500)
            binding.emotionLayout.addView(textView)
        }

        binding.factorText.text = mood["factor"]?.joinToString(", ") ?: "None"

        // Set recommendations based on mood
        binding.journalImage1.setImageResource(R.drawable.ic_journal)
        binding.journalImage2.setImageResource(R.drawable.ic_journal)
    }
}
