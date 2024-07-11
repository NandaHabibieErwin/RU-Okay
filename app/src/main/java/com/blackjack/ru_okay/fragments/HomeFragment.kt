package com.blackjack.ru_okay.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.blackjack.ru_okay.R
import com.blackjack.ru_okay.databinding.FragmentHomeBinding
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference

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
                }

                override fun onCancelled(error: DatabaseError) {
                    if (!isAdded || _binding == null) return
                    binding.greetingText.text = "Failed to load username"
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
            }.addOnFailureListener {
                if (!isAdded || _binding == null) return@addOnFailureListener
                // Handle any errors
                binding.userProfilePicture.setImageResource(R.drawable.ic_user_temporary)
            }
        } ?: run {
            // No user ID provided
            binding.greetingText.text = "No user ID provided"
            binding.userProfilePicture.setImageResource(R.drawable.ic_user_temporary)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}