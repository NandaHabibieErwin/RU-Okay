package com.blackjack.ru_okay.fragments

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.blackjack.ru_okay.R
import com.blackjack.ru_okay.databinding.FragmentHomeBinding
import com.blackjack.ru_okay.moodtracker.EmotionSelectionActivity
import com.blackjack.ru_okay.moodtracker.WeeklyMoodSummaryActivity
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import java.text.SimpleDateFormat
import java.util.*

class HomeFragment : Fragment() {
    private lateinit var auth: FirebaseAuth
    private lateinit var database: DatabaseReference
    private lateinit var storage: FirebaseStorage
    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    companion object {
        private const val ARG_USER_ID = "user_id"

        fun newInstance(userId: String): HomeFragment {
            val fragment = HomeFragment()
            val args = Bundle().apply {
                putString(ARG_USER_ID, userId)
            }
            fragment.arguments = args
            return fragment
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance("https://ru-okaydemo-default-rtdb.asia-southeast1.firebasedatabase.app/").reference
        storage = FirebaseStorage.getInstance("gs://ru-okaydemo.appspot.com")
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        val view = binding.root

        // Initialize ViewFlipper and buttons
        binding.btnNext.setOnClickListener {
            binding.viewFlipper.showNext()
        }

        binding.btnPrevious.setOnClickListener {
            binding.viewFlipper.showPrevious()
        }

        // Get current user and display username and avatar
        val userId = arguments?.getString(ARG_USER_ID)
        displayUserInfo(userId)

        // Set up mood buttons
        binding.veryGood.setOnClickListener { openEmotionSelectionActivity("Very Good") }
        binding.good.setOnClickListener { openEmotionSelectionActivity("Good") }
        binding.neutral.setOnClickListener { openEmotionSelectionActivity("Neutral") }
        binding.bad.setOnClickListener { openEmotionSelectionActivity("Bad") }
        binding.veryBad.setOnClickListener { openEmotionSelectionActivity("Very Bad") }

        // Check if user has already checked mood today
        checkMoodStatus(userId)

        // Set up click listener for mood summary card
        binding.moodSummaryCardView.setOnClickListener {
            openWeeklyMoodSummaryActivity()
        }

        return view
    }

    private fun displayUserInfo(userId: String?) {
        userId?.let {
            // Fetch username from Realtime Database
            database.child("users").child(it).child("profile").child("username").addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (!isAdded || _binding == null) return
                    val username = snapshot.value as? String
                    binding.greetingText.text = username ?: "Username not found"
                    binding.greetingTextSummary.text = username ?: "Username not found"
                }

                override fun onCancelled(error: DatabaseError) {
                    if (!isAdded || _binding == null) return
                    binding.greetingText.text = "Failed to load username"
                    binding.greetingTextSummary.text = "Failed to load username"
                }
            })

            // Fetch avatar URL from Firebase Storage
            val avatarRef: StorageReference = storage.reference.child("avatars/$it.jpg")
            avatarRef.downloadUrl.addOnSuccessListener { uri ->
                // Load avatar image using Glide
                if (!isAdded || _binding == null) return@addOnSuccessListener
                Glide.with(this@HomeFragment)
                    .load(uri)
                    .placeholder(R.drawable.ic_user_temporary) // Optional placeholder image
                    .into(binding.userProfilePicture)
                Glide.with(this@HomeFragment)
                    .load(uri)
                    .placeholder(R.drawable.ic_user_temporary) // Optional placeholder image
                    .into(binding.userProfilePictureSummary)
            }.addOnFailureListener {
                if (!isAdded || _binding == null) return@addOnFailureListener
                // Handle any errors
                binding.userProfilePicture.setImageResource(R.drawable.ic_user_temporary)
                binding.userProfilePictureSummary.setImageResource(R.drawable.ic_user_temporary)
            }
        } ?: run {
            // No user ID provided
            binding.greetingText.text = "No user ID provided"
            binding.greetingTextSummary.text = "No user ID provided"
            binding.userProfilePicture.setImageResource(R.drawable.ic_user_temporary)
            binding.userProfilePictureSummary.setImageResource(R.drawable.ic_user_temporary)
        }
    }

    private fun openEmotionSelectionActivity(emotion: String) {
        val intent = Intent(context, EmotionSelectionActivity::class.java)
        intent.putExtra("selectedEmotion", emotion)

        // Store the initial mood selection in the database
        val userId = arguments?.getString(ARG_USER_ID)
        userId?.let {
            val dayOfWeek = SimpleDateFormat("EEE", Locale.ENGLISH).format(Date())
            val currentDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
            val moodKey = "$dayOfWeek-$currentDate"

            database.child("users").child(it).child("moods").child(moodKey).child("mood").setValue(emotion)
                .addOnSuccessListener {
                    // Navigate to EmotionSelectionActivity only after storing the initial mood
                    startActivity(intent)
                }
                .addOnFailureListener {
                    // Handle failure
                }
        }
    }

    private fun checkMoodStatus(userId: String?) {
        userId?.let {
            val dayOfWeek = SimpleDateFormat("EEE", Locale.ENGLISH).format(Date())
            val currentDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
            val moodKey = "$dayOfWeek-$currentDate"

            // Set up a ValueEventListener for real-time updates
            database.child("users").child(it).child("moods").child(moodKey).addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (!isAdded || _binding == null) return
                    if (snapshot.exists()) {
                        val mood = snapshot.child("mood").getValue(String::class.java)
                        val emotions = snapshot.child("positive").getValue(object : GenericTypeIndicator<List<String>>() {})
                        val factors = snapshot.child("factor").getValue(object : GenericTypeIndicator<List<String>>() {})
                        updateMoodSummary(mood, emotions, factors)
                    } else {
                        showMoodSelection()
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    if (!isAdded || _binding == null) return
                    // Handle any errors
                    showMoodSelection()
                }
            })
        } ?: run {
            showMoodSelection()
        }
    }

    private fun updateMoodSummary(mood: String?, emotions: List<String>?, factors: List<String>?) {
        binding.moodSelectionCardView.visibility = View.GONE
        binding.moodSummaryCardView.visibility = View.VISIBLE
        binding.moodStatusSummary.text = mood ?: "No Mood"
        binding.emotionTextSummary.text = "Emotions: ${emotions?.joinToString(", ") ?: "None"}"

        val moodIconRes = when (mood) {
            "Very Good" -> R.drawable.smiling
            "Good" -> R.drawable.blushing
            "Neutral" -> R.drawable.neutral
            "Bad" -> R.drawable.sad
            "Very Bad" -> R.drawable.disappointed
            else -> R.drawable.neutral
        }
        binding.moodIconSummary.setImageResource(moodIconRes)
    }

    private fun showMoodSelection() {
        binding.moodSelectionCardView.visibility = View.VISIBLE
        binding.moodSummaryCardView.visibility = View.GONE
    }

    private fun openWeeklyMoodSummaryActivity() {
        val intent = Intent(context, WeeklyMoodSummaryActivity::class.java)
        startActivity(intent)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
