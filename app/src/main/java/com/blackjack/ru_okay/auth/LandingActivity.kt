package com.blackjack.ru_okay.auth

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.blackjack.ru_okay.MainActivity
import com.blackjack.ru_okay.PsychologistActivity
import com.blackjack.ru_okay.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class LandingActivity : AppCompatActivity() {
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private lateinit var database: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Initialize the database reference before checking user role
        database = FirebaseDatabase.getInstance("https://ru-okaydemo-default-rtdb.asia-southeast1.firebasedatabase.app/").reference

        if (auth.currentUser != null) {
            checkUserRoleAndRedirect()
        }

        enableEdgeToEdge()
        setContentView(R.layout.activity_landing)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val loginButton = findViewById<Button>(R.id.signin)
        loginButton.setOnClickListener {
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
        }

        val registerButton = findViewById<Button>(R.id.signup)
        registerButton.setOnClickListener {
            val intent = Intent(this, RegisterActivity::class.java)
            startActivity(intent)
        }
    }

    private fun checkUserRoleAndRedirect() {
        val userId = auth.currentUser?.uid ?: return

        // Check if the user is a psychologist
        database.child("psychologists").child(userId).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    // User is a psychologist, redirect to PsychologistActivity
                    val intent = Intent(this@LandingActivity, PsychologistActivity::class.java)
                    startActivity(intent)
                    finish()
                } else {
                    // Check if the user is a regular user
                    database.child("users").child(userId).addListenerForSingleValueEvent(object : ValueEventListener {
                        override fun onDataChange(snapshot: DataSnapshot) {
                            if (snapshot.exists()) {
                                // User is a regular user, redirect to MainActivity
                                val intent = Intent(this@LandingActivity, MainActivity::class.java)
                                startActivity(intent)
                                finish()
                            }
                        }

                        override fun onCancelled(error: DatabaseError) {
                            // Handle database error
                        }
                    })
                }
            }

            override fun onCancelled(error: DatabaseError) {
                // Handle database error
            }
        })
    }
}
