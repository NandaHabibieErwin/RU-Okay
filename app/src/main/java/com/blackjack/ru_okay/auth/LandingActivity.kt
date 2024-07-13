package com.blackjack.ru_okay.auth

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.blackjack.ru_okay.MainActivity
import com.blackjack.ru_okay.R
import com.google.firebase.auth.FirebaseAuth


class LandingActivity : AppCompatActivity() {
   private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (auth.currentUser != null){
            redirectToMain()
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

    private fun redirectToMain(){
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
    }
}