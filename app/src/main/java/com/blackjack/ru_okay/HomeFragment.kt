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

class HomeFragment : Fragment() {
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        auth = FirebaseAuth.getInstance()
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

        // Get current user and display email
        val currentUser = auth.currentUser
        displayUserEmail(view, currentUser)

        return view
    }

    private fun displayUserEmail(view: View, user: FirebaseUser?) {
        val emailTextView: TextView = view.findViewById(R.id.greeting_text)
        user?.let {
            val email = it.email
            emailTextView.text = email
        } ?: run {
            // No user is signed in
            emailTextView.text = "No user is signed in"
        }
    }
}
