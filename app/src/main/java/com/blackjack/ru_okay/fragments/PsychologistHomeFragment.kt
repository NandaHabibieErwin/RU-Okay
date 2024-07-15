package com.blackjack.ru_okay.fragments

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.blackjack.ru_okay.R
import com.blackjack.ru_okay.consult.Consultation
import com.blackjack.ru_okay.consult.ConsultationAdapter
import com.blackjack.ru_okay.consult.VideoCallActivity
import com.blackjack.ru_okay.databinding.FragmentPsychologistHomeBinding
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference

class PsychologistHomeFragment : Fragment() {
    private lateinit var auth: FirebaseAuth
    private lateinit var database: DatabaseReference
    private lateinit var storage: FirebaseStorage
    private var _binding: FragmentPsychologistHomeBinding? = null
    private val binding get() = _binding!!
    private val consultations = mutableListOf<Consultation>()
    private lateinit var consultationAdapter: ConsultationAdapter

    companion object {
        private const val ARG_USER_ID = "user_id"

        fun newInstance(userId: String): PsychologistHomeFragment {
            val fragment = PsychologistHomeFragment()
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
        _binding = FragmentPsychologistHomeBinding.inflate(inflater, container, false)
        val view = binding.root

        // Initialize RecyclerView
        consultationAdapter = ConsultationAdapter(consultations)
        binding.recyclerViewConsultations.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerViewConsultations.adapter = consultationAdapter

        // Get current user and display username and avatar
        val userId = arguments?.getString(ARG_USER_ID)
        displayUserInfo(userId)

        // Load consultation requests
        loadConsultationRequests(userId)

        // Listen for notifications
        listenForNotifications(userId)

        return view
    }

    private fun displayUserInfo(userId: String?) {
        userId?.let {
            // Fetch username from Realtime Database
            database.child("psychologists").child(it).child("profile").child("username").addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (!isAdded || _binding == null) return
                    val username = snapshot.value as? String
                    binding.psychologistGreetingText.text = "Hello, ${username ?: "Username not found"}"
                }

                override fun onCancelled(error: DatabaseError) {
                    if (!isAdded || _binding == null) return
                    binding.psychologistGreetingText.text = "Failed to load username"
                }
            })

            // Fetch specialty from Realtime Database
            database.child("psychologists").child(it).child("profile").child("specialty").addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (!isAdded || _binding == null) return
                    val specialty = snapshot.value as? String
                    binding.psychologistSpecialtyText.text = specialty ?: "Specialty not found"
                }

                override fun onCancelled(error: DatabaseError) {
                    if (!isAdded || _binding == null) return
                    binding.psychologistSpecialtyText.text = "Failed to load specialty"
                }
            })

            // Fetch avatar URL from Firebase Storage
            val avatarRef: StorageReference = storage.reference.child("avatars/$it.jpg")
            avatarRef.downloadUrl.addOnSuccessListener { uri ->
                // Load avatar image using Glide
                if (!isAdded || _binding == null) return@addOnSuccessListener
                Glide.with(this@PsychologistHomeFragment)
                    .load(uri)
                    .placeholder(R.drawable.ic_user_temporary) // Optional placeholder image
                    .into(binding.psychologistProfilePicture)
            }.addOnFailureListener {
                if (!isAdded || _binding == null) return@addOnFailureListener
                // Handle any errors
                binding.psychologistProfilePicture.setImageResource(R.drawable.ic_user_temporary)
            }
        } ?: run {
            // No user ID provided
            binding.psychologistGreetingText.text = "No user ID provided"
            binding.psychologistProfilePicture.setImageResource(R.drawable.ic_user_temporary)
        }
    }

    private fun loadConsultationRequests(userId: String?) {
        userId?.let {
            database.child("psychologists").child(it).child("consultations").addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    consultations.clear()
                    for (dataSnapshot in snapshot.children) {
                        val consultation = dataSnapshot.getValue(Consultation::class.java)
                        consultation?.let { consultations.add(it) }
                    }
                    consultationAdapter.notifyDataSetChanged()
                }

                override fun onCancelled(error: DatabaseError) {
                    // Handle possible errors.
                }
            })
        }
    }

    private fun listenForNotifications(userId: String?) {
        userId?.let {
            val notificationsRef = database.child("psychologists").child(it).child("notifications")
            notificationsRef.addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val userJoined = snapshot.child("userJoined").getValue(Boolean::class.java) ?: false
                    val message = snapshot.child("message").getValue(String::class.java) ?: ""

                    if (userJoined) {
                        binding.joinCallButton.visibility = View.VISIBLE
                        binding.joinCallButton.setOnClickListener {
                            onJoinCallButtonClick()
                        }
                        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
                    } else {
                        binding.joinCallButton.visibility = View.GONE
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    // Handle error
                }
            })
        }
    }

    private fun onJoinCallButtonClick() {
        val intent = Intent(requireContext(), VideoCallActivity::class.java)
        startActivity(intent)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
