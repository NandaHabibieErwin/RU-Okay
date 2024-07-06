package com.blackjack.ru_okay

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import android.widget.ViewFlipper
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.*

class HomeFragment : Fragment() {
    private lateinit var auth: FirebaseAuth
    private lateinit var database: DatabaseReference

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
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_home, container, false)

        // Initialize ViewFlipper and buttons
        val viewFlipper: ViewFlipper = view.findViewById(R.id.viewFlipper)
        val btnNext: ImageButton = view.findViewById(R.id.btnNext)
        val btnPrevious: ImageButton = view.findViewById(R.id.btnPrevious)

        btnNext.setOnClickListener {
            viewFlipper.showNext()
        }

        btnPrevious.setOnClickListener {
            viewFlipper.showPrevious()
        }

        // Get current user and display username
        val userId = arguments?.getString(ARG_USER_ID)
        displayUsername(view, userId)

        return view
    }

    private fun displayUsername(view: View, userId: String?) {
        val usernameTextView: TextView = view.findViewById(R.id.greeting_text)
        userId?.let {
            // Fetch username from Realtime Database
            database.child("users").child(it).child("username").addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val username = snapshot.value as? String
                    usernameTextView.text = username ?: "Username not found"
                }

                override fun onCancelled(error: DatabaseError) {
                    usernameTextView.text = "Failed to load username"
                }
            })
        } ?: run {
            // No user ID provided
            usernameTextView.text = "No user ID provided"
        }
    }
}
