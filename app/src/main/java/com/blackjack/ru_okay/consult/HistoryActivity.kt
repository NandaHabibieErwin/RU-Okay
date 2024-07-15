package com.blackjack.ru_okay.consult

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.blackjack.ru_okay.R
import com.blackjack.ru_okay.databinding.ActivityHistoryBinding
import com.blackjack.ru_okay.payment.InvoiceActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class HistoryActivity : AppCompatActivity() {
    private lateinit var binding: ActivityHistoryBinding
    private lateinit var database: DatabaseReference
    private lateinit var auth: FirebaseAuth
    private var userID = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Initialize ViewBinding
        binding = ActivityHistoryBinding.inflate(layoutInflater)
        setContentView(binding.root)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance("https://ru-okaydemo-default-rtdb.asia-southeast1.firebasedatabase.app/").reference
        val currentUser = auth.currentUser
        userID = currentUser?.uid ?: "null"

        // Set up RecyclerView
        binding.recyclerView.layoutManager = LinearLayoutManager(this)
        val consultsList = mutableListOf<Consult>()
        val adapter = ConsultsAdapter(consultsList) { consultId ->
            val intent = Intent(this, InvoiceActivity::class.java)
            intent.putExtra("consultId", consultId)
            startActivity(intent)
        }
        binding.recyclerView.adapter = adapter

        // Fetch consults from database
        database.child("users").child(userID).child("consults").addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                consultsList.clear()
                for (dataSnapshot in snapshot.children) {
                    val consult = dataSnapshot.getValue(Consult::class.java)
                    consult?.let {
                        it.id = dataSnapshot.key ?: ""
                        consultsList.add(it)
                    }
                }
                adapter.notifyDataSetChanged()
            }

            override fun onCancelled(error: DatabaseError) {
                // Handle database error
            }
        })
    }
    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }
}

data class Consult(
    var id: String = "",
    var datetime: Long = 0L,
    var location: String = "",
    var service: String = ""
)
