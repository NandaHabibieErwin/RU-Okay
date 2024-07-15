package com.blackjack.ru_okay.journal

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.blackjack.ru_okay.MainActivity
import com.blackjack.ru_okay.R
import com.blackjack.ru_okay.databinding.ActivityWriteJournalBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

class WriteJournalActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var database: DatabaseReference
    private lateinit var binding: ActivityWriteJournalBinding
    private var journalId: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        // Initialize ViewBinding
        binding = ActivityWriteJournalBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance("https://ru-okaydemo-default-rtdb.asia-southeast1.firebasedatabase.app/").reference

        journalId = intent.getStringExtra("journalId")
        val content = intent.getStringExtra("content")
        if (content != null) {
            viewJournal(content)
        }

        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        val inflater: MenuInflater = menuInflater
        inflater.inflate(R.menu.journal_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_send -> {
                saveJournalEntry()
                true
            }
            android.R.id.home -> {
                val intent = Intent(this, MainActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
                startActivity(intent)
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun viewJournal(content: String) {
        binding.writeJournalEditText.setText(content)
    }

    private fun saveJournalEntry() {
        val currentUser = auth.currentUser
        val userID = currentUser?.uid ?: return
        val journalText = binding.writeJournalEditText.text.toString().trim()

        if (journalId != null) {
            updateJournalEntry(userID, journalId!!, journalText)
        } else {
            val journalEntry = hashMapOf(
                "text" to journalText,
                "timestamp" to System.currentTimeMillis()
            )

            database.child("users").child(userID).child("journals").push().setValue(journalEntry)
                .addOnSuccessListener {
                    Toast.makeText(this, "Journal entry saved", Toast.LENGTH_SHORT).show()
                    binding.writeJournalEditText.text.clear()
                    finish()
                }
                .addOnFailureListener {
                    Toast.makeText(this, "Failed to save journal entry", Toast.LENGTH_SHORT).show()
                }
        }
    }

    private fun updateJournalEntry(userID: String, journalId: String, journalText: String) {
        val journalEntry = hashMapOf(
            "text" to journalText,
            "timestamp" to System.currentTimeMillis()
        )

        database.child("users").child(userID).child("journals").child(journalId).setValue(journalEntry)
            .addOnSuccessListener {
                Toast.makeText(this, "Journal entry updated", Toast.LENGTH_SHORT).show()
                binding.writeJournalEditText.text.clear()
                finish()
            }
            .addOnFailureListener {
                Toast.makeText(this, "Failed to update journal entry", Toast.LENGTH_SHORT).show()
            }
    }
}
