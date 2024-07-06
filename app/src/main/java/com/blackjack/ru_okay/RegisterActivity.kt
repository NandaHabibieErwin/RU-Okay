package com.blackjack.ru_okay

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
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

            if (email.isNotEmpty() && password.isNotEmpty() && username.isNotEmpty() && birthDate.isNotEmpty()) {
                registerUser(email, password, username, birthDate)
            }
        }
    }

    private fun initializeSendBird(id: String, username: String) {
        SendbirdUIKit.init(object : SendbirdUIKitAdapter {
            override fun getAppId(): String {
                return "9679B48B-9707-4DFA-83E3-D4915E396D50" // Specify your Sendbird application ID.
            }

            override fun getAccessToken(): String {
                return "e8ca03277650b52bda3de01ca6e56862909e9c42"
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

    private fun registerUser(email: String, password: String, username: String, birthDate: String) {
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { authTask ->
                if (authTask.isSuccessful) {
                    val user = auth.currentUser
                    val userId = user?.uid ?: ""
                    val userEmail = user?.email ?: ""
                    val userMap = mapOf(
                        "email" to email,
                        "username" to username,
                        "birthDate" to birthDate
                    )
                    Log.d("Registration", "User registered: $userId")

                    database.child("users").child(userId).setValue(userMap)
                        .addOnCompleteListener { dbTask ->
                            if (dbTask.isSuccessful) {
                                Log.d("Registration", "User data saved successfully")
                                initializeSendBird(userId, username)
                                val intent = Intent(this, MainActivity::class.java)
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
