package com.blackjack.ru_okay.payment

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.blackjack.ru_okay.databinding.ActivityInvoiceBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

class InvoiceActivity : AppCompatActivity() {
    private lateinit var binding: ActivityInvoiceBinding
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Initialize ViewBinding
        binding = ActivityInvoiceBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Enable the action bar back button
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        // Initialize FirebaseAuth
        auth = FirebaseAuth.getInstance()

        // Retrieve consultId from intent
        val consultId = intent.getStringExtra("consultId")

        // Fetch and display data from database
        consultId?.let {
            val currentUser = auth.currentUser
            val userId = currentUser?.uid ?: return@let

            val database = FirebaseDatabase.getInstance().reference
            database.child("users").child(userId).child("consults").child(it).get()
                .addOnSuccessListener { dataSnapshot ->
                    val datetime = dataSnapshot.child("datetime").value as Long
                    val location = dataSnapshot.child("location").value as String
                    val service = dataSnapshot.child("service").value as String

                    binding.textViewService.text = service
                    binding.textViewLocation.text = location
                    binding.textViewDatetime.text = datetime.toString() // Format datetime as needed
                }
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }
}
