package com.blackjack.ru_okay.auth

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.RadioButton
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.blackjack.ru_okay.MainActivity
import com.blackjack.ru_okay.PsychologistActivity
import com.blackjack.ru_okay.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.sendbird.android.exception.SendbirdException
import com.sendbird.android.handler.InitResultHandler
import com.sendbird.uikit.SendbirdUIKit
import com.sendbird.uikit.adapter.SendbirdUIKitAdapter
import com.sendbird.uikit.interfaces.UserInfo

class RegisterActivity : AppCompatActivity() {
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private lateinit var database: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (auth.currentUser != null) {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish()
        }
        enableEdgeToEdge()
        setContentView(R.layout.activity_register)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        database = FirebaseDatabase.getInstance().reference

        val registerButton = findViewById<Button>(R.id.btnRegister)
        registerButton.setOnClickListener {
            val emailEditText = findViewById<EditText>(R.id.registerEmail)
            val passwordEditText = findViewById<EditText>(R.id.registerPassword)
            val usernameEditText = findViewById<EditText>(R.id.registerUsername)
            val birthDateEditText = findViewById<EditText>(R.id.registerBirthdate)

            val email = emailEditText.text.toString()
            val password = passwordEditText.text.toString()
            val username = usernameEditText.text.toString()
            val birthDate = birthDateEditText.text.toString()
            val isPsychologist = findViewById<RadioButton>(R.id.radioPsychologist).isChecked

            if (email.isNotEmpty() && password.isNotEmpty() && username.isNotEmpty() && birthDate.isNotEmpty()) {
                registerUser(email, password, username, birthDate, isPsychologist)
            } else {
                Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun initializeSendBird(id: String, username: String) {
        SendbirdUIKit.init(object : SendbirdUIKitAdapter {
            override fun getAppId(): String {
                return "66356B2B-EEE4-4399-A78B-C38720145059" // Specify your Sendbird application ID.
            }

            override fun getAccessToken(): String {
                return ""
            }

            override fun getUserInfo(): UserInfo {
                return object : UserInfo {
                    override fun getUserId(): String {
                        val x = id
                        Log.d("Username", id)
                        return x
                    }

                    override fun getNickname(): String {
                        val r = username
                        Log.d("Username", id)
                        return r
                    }

                    override fun getProfileUrl(): String {
                        return ""
                    }
                }
            }

            override fun getInitResultHandler(): InitResultHandler {
                return object : InitResultHandler {
                    override fun onMigrationStarted() {}
                    override fun onInitFailed(e: SendbirdException) {}
                    override fun onInitSucceed() {}
                }
            }
        }, this)
    }

    private fun registerUser(email: String, password: String, username: String, birthDate: String, isPsychologist: Boolean) {
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { authTask ->
                if (authTask.isSuccessful) {
                    val user = auth.currentUser
                    val userId = user?.uid ?: ""
                    val userMap = mapOf(
                        "email" to email,
                        "username" to username,
                        "birthDate" to birthDate
                    )
                    Log.d("Registration", "User registered: $userId")

                    val databaseReference = if (isPsychologist) {
                        database.child("psychologists").child(userId).child("profile")
                    } else {
                        database.child("users").child(userId).child("profile")
                    }

                    databaseReference.setValue(userMap)
                        .addOnCompleteListener { dbTask ->
                            if (dbTask.isSuccessful) {
                                Log.d("Registration", "User data saved successfully")
                                initializeSendBird(userId, username)
                                val intent = if (isPsychologist) {
                                    Intent(this, PsychologistActivity::class.java)
                                } else {
                                    Intent(this, MainActivity::class.java)
                                }
                                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                                startActivity(intent)
                                finish()
                            } else {
                                Log.e("Registration", "Failed to save user data", dbTask.exception)
                                Toast.makeText(this, "Failed to save user data", Toast.LENGTH_SHORT).show()
                            }
                        }
                } else {
                    Log.e("Registration", "createUserWithEmail:failure", authTask.exception)
                    Toast.makeText(this, "Registration failed: ${authTask.exception?.message}", Toast.LENGTH_SHORT).show()
                }
            }
    }
}
