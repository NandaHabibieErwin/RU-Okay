package com.blackjack.ru_okay.fragments

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.blackjack.ru_okay.auth.LandingActivity
import com.blackjack.ru_okay.setting.PsychologistProfileActivity
import com.blackjack.ru_okay.R
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.google.firebase.storage.FirebaseStorage
import com.blackjack.ru_okay.databinding.FragmentPsychologistSettingBinding

class PsychologistSettingFragment : Fragment() {
    private lateinit var auth: FirebaseAuth
    private lateinit var database: DatabaseReference
    private lateinit var storage: FirebaseStorage
    private var _binding: FragmentPsychologistSettingBinding? = null
    private val binding get() = _binding!!

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance().reference
        storage = FirebaseStorage.getInstance()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPsychologistSettingBinding.inflate(inflater, container, false)
        val view = binding.root

        val userId = auth.currentUser?.uid

        userId?.let {
            // Fetch psychologist info from Realtime Database
            database.child("psychologists").child(it).child("profile").addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (_binding == null || !isAdded) return

                    val username = snapshot.child("username").value as? String
                    val email = auth.currentUser?.email
                    val phone = snapshot.child("phoneNumber").value as? String

                    binding.userName.text = username ?: "Username not found"
                    binding.userEmail.text = email ?: "Email not found"
                    binding.userPhone.text = phone ?: "Phone not found"
                }

                override fun onCancelled(error: DatabaseError) {
                    if (_binding == null || !isAdded) return

                    binding.userName.text = "Failed to load username"
                    binding.userEmail.text = "Failed to load email"
                    binding.userPhone.text = "Failed to load phone"

                    activity?.let {
                        Toast.makeText(it, "Failed to load psychologist info", Toast.LENGTH_SHORT).show()
                    }
                }
            })

            // Fetch avatar URL from Firebase Storage
            val avatarRef = storage.reference.child("avatars/$it.jpg")
            avatarRef.downloadUrl.addOnSuccessListener { uri ->
                if (_binding == null || !isAdded) return@addOnSuccessListener

                // Load avatar image using Glide
                activity?.let { ctx ->
                    Glide.with(ctx)
                        .load(uri)
                        .placeholder(R.drawable.ic_user_temporary)
                        .error(R.drawable.ic_user_temporary)
                        .into(binding.userProfilePicture)
                }
            }.addOnFailureListener {
                if (_binding == null || !isAdded) return@addOnFailureListener

                binding.userProfilePicture.setImageResource(R.drawable.ic_user_temporary)
            }
        }

        binding.editUserProfile.setOnClickListener {
            val intent = Intent(activity, PsychologistProfileActivity::class.java)
            startActivity(intent)
        }

        binding.btnLogout.setOnClickListener {
            FirebaseAuth.getInstance().signOut()
            val intent = Intent(requireContext(), LandingActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
        }

        return view
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
