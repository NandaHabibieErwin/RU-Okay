package com.blackjack.ru_okay.consult

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.blackjack.ru_okay.databinding.ActivityConsultPsychologListBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.blackjack.ru_okay.R

class ConsultPsychologListActivity : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth
    private lateinit var binding: ActivityConsultPsychologListBinding
    private lateinit var adapter: PsychologistAdapter
    private val psychologists = mutableListOf<Psychologist>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityConsultPsychologListBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        adapter = PsychologistAdapter(psychologists) { psychologist ->
            val intent = Intent(this, CallActivity::class.java).apply {
                putExtra("psychologistId", psychologist.id) // pass necessary details
                putExtra("psychologistName", psychologist.name)
                // Add other relevant information if needed
            }
            startActivity(intent)
        }
        binding.recyclerViewPsychologists.layoutManager = LinearLayoutManager(this)
        binding.recyclerViewPsychologists.adapter = adapter

        checkUserAndLoadData()
    }

    private fun checkUserAndLoadData() {
        val currentUser = auth.currentUser
        if (currentUser == null) {
            // Redirect to login activity if user is not authenticated
            Log.w("THis","Unauthetciate")
        } else {
            loadPsychologist()
        }
    }

    private fun loadPsychologist() {
        val databaseReference = FirebaseDatabase.getInstance().reference.child("psychologist")
        databaseReference.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                psychologists.clear()
                for (dataSnapshot in snapshot.children) {
                    val psychologist = dataSnapshot.getValue(Psychologist::class.java)
                    psychologist?.let { psychologists.add(it) }
                }
                adapter.notifyDataSetChanged()
            }

            override fun onCancelled(error: DatabaseError) {
                // Handle possible errors.
            }
        })
    }
}
