package com.blackjack.ru_okay.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.blackjack.ru_okay.R
import com.blackjack.ru_okay.consult.Consultation
import com.blackjack.ru_okay.consult.ConsultationAdapter
import com.blackjack.ru_okay.databinding.FragmentPsychologistConsultBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class PsychologistConsultFragment : Fragment() {

    private var _binding: FragmentPsychologistConsultBinding? = null
    private val binding get() = _binding!!
    private lateinit var database: DatabaseReference
    private lateinit var auth: FirebaseAuth
    private lateinit var consultationAdapter: ConsultationAdapter
    private val consultationList = mutableListOf<Consultation>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPsychologistConsultBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance().reference

        setupRecyclerView()

        val psychologistId = auth.currentUser?.uid ?: return
        fetchConsultations(psychologistId)
    }

    private fun setupRecyclerView() {
        consultationAdapter = ConsultationAdapter(consultationList)
        binding.recyclerView.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = consultationAdapter
        }
    }

    private fun fetchConsultations(psychologistId: String) {
        database.child("consultations").orderByChild("psychologistId").equalTo(psychologistId)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    consultationList.clear()
                    for (dataSnapshot in snapshot.children) {
                        val consultation = dataSnapshot.getValue(Consultation::class.java)
                        consultation?.let { consultationList.add(it) }
                    }
                    consultationAdapter.notifyDataSetChanged()
                }

                override fun onCancelled(error: DatabaseError) {
                    // Handle error
                }
            })
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
