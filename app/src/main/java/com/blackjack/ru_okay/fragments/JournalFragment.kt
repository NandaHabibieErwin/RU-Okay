package com.blackjack.ru_okay.fragments

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.blackjack.ru_okay.JournalInfoActivity
import com.blackjack.ru_okay.R
import com.blackjack.ru_okay.WriteJournalActivity
import com.blackjack.ru_okay.databinding.FragmentJournalBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import java.text.SimpleDateFormat
import java.util.*

class JournalFragment : Fragment() {

    private var _binding: FragmentJournalBinding? = null
    private val binding get() = _binding!!
    private lateinit var database: DatabaseReference
    private lateinit var auth: FirebaseAuth

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentJournalBinding.inflate(inflater, container, false)
        val view = binding.root

        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance("https://ru-okaydemo-default-rtdb.asia-southeast1.firebasedatabase.app/").reference

        binding.writeJournal.setOnClickListener {
            val intent = Intent(activity, JournalInfoActivity::class.java)
            startActivity(intent)
        }

        loadJournalEntries()

        return view
    }

    private fun loadJournalEntries() {
        val currentUser = auth.currentUser
        val userID = currentUser?.uid ?: return

        database.child("users").child(userID).child("journals").addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                // Check if the fragment view is still valid
                if (_binding != null) {
                    binding.journalHistory.removeAllViews()
                    for (journalSnapshot in snapshot.children) {
                        val journal = journalSnapshot.getValue(JournalEntry::class.java)
                        journal?.let { addJournalEntryView(it) }
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(activity, "Failed to load journals: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun addJournalEntryView(journal: JournalEntry) {
        // Check if the fragment view is still valid
        if (_binding != null) {
            val journalView = LayoutInflater.from(context).inflate(R.layout.journal_entry_item, binding.journalHistory, false)
            val titleTextView = journalView.findViewById<TextView>(R.id.journalTitle)
            val timeTextView = journalView.findViewById<TextView>(R.id.journalTime)
            val dateTextView = journalView.findViewById<TextView>(R.id.journalDate)

            titleTextView.text = journal.title

            val date = Date(journal.timestamp)
            val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
            val dateFormat = SimpleDateFormat("EEEE, MMMM d, yyyy", Locale.getDefault())

            timeTextView.text = "Written at ${timeFormat.format(date)}"
            dateTextView.text = dateFormat.format(date)

            journalView.setOnClickListener {
                val intent = Intent(activity, WriteJournalActivity::class.java).apply {
                    putExtra("content", journal.text.toString()) // Assuming 'journal.content' holds the content
                }
                startActivity(intent)
            }

            binding.journalHistory.addView(journalView)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

data class JournalEntry(
    val title: String = "",
    val timestamp: Long = 0L,
    val text: String = ""
)
