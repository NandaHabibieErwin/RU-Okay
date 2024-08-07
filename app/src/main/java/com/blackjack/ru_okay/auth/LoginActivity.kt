package com.blackjack.ru_okay.auth

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
import com.blackjack.ru_okay.MainActivity
import com.blackjack.ru_okay.PsychologistActivity
import com.blackjack.ru_okay.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.sendbird.android.exception.SendbirdException
import com.sendbird.android.handler.InitResultHandler
import com.sendbird.uikit.SendbirdUIKit
import com.sendbird.uikit.adapter.SendbirdUIKitAdapter
import com.sendbird.uikit.interfaces.UserInfo

class LoginActivity : AppCompatActivity() {
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private lateinit var database: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_login)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Initialize the database reference
        database = FirebaseDatabase.getInstance("https://ru-okaydemo-default-rtdb.asia-southeast1.firebasedatabase.app/").reference

        val loginButton = findViewById<Button>(R.id.btnLogin)
        loginButton.setOnClickListener {
            val emailEditText = findViewById<EditText>(R.id.loginEmail)
            val passwordEditText = findViewById<EditText>(R.id.loginPassword)
            val email = emailEditText.text.toString()
            val password = passwordEditText.text.toString()

            if (email.isNotEmpty() && password.isNotEmpty()) {
                loginUser(email, password)
            } else {
                Toast.makeText(this, "Please enter email and password", Toast.LENGTH_SHORT).show()
            }
        }

        val psychologistLoginButton = findViewById<Button>(R.id.btnPsychologistLogin)
        psychologistLoginButton.setOnClickListener {
            val emailEditText = findViewById<EditText>(R.id.loginEmail)
            val passwordEditText = findViewById<EditText>(R.id.loginPassword)
            val email = emailEditText.text.toString()
            val password = passwordEditText.text.toString()

            if (email.isNotEmpty() && password.isNotEmpty()) {
                loginPsychologist(email, password)
            } else {
                Toast.makeText(this, "Please enter email and password", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun initializeSendBird(uid: String, username: String) {
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
                        val x = uid
                        Log.d("SendBird", "UserId: $uid")
                        return x
                    }

                    override fun getNickname(): String {
                        val r = username
                        Log.d("SendBird", "Username: $username")
                        return r
                    }

                    override fun getProfileUrl(): String {
                        return ""
                    }
                }
            }

            override fun getInitResultHandler(): InitResultHandler {
                return object : InitResultHandler {
                    override fun onMigrationStarted() {
                        // DB migration has started.
                    }

                    override fun onInitFailed(e: SendbirdException) {
                        Log.e("SendBird", "Initialization failed: ${e.message}")
                    }

                    override fun onInitSucceed() {
                        Log.d("SendBird", "Initialization succeeded")
                    }
                }
            }
        }, this)
    }

    private fun loginUser(email: String, password: String) {
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val user = auth.currentUser
                    val userId = user?.uid ?: ""
                    fetchUserData(userId)
                } else {
                    Log.w("Login", "signInWithEmail:failure", task.exception)
                    Toast.makeText(this, "Login failed: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                }
            }
    }

    private fun loginPsychologist(email: String, password: String) {
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val user = auth.currentUser
                    val userId = user?.uid ?: ""
                    fetchPsychologistData(userId)
                } else {
                    Log.w("Login", "signInWithEmail:failure", task.exception)
                    Toast.makeText(this, "Login failed: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                }
            }
    }

    private fun fetchUserData(userId: String) {
        database.child("users").child(userId).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    val userMap = snapshot.value as? Map<*, *>
                    val username = userMap?.get("username") as? String ?: ""

                    Log.d("Login", "Fetched username: $username")
                    initializeSendBird(userId, username)

                    val intent = Intent(this@LoginActivity, MainActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    startActivity(intent)
                    finish()
                } else {
                    Toast.makeText(this@LoginActivity, "This account is not a user account.", Toast.LENGTH_SHORT).show()
                    auth.signOut()
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.w("Login", "Failed to read user data", error.toException())
                Toast.makeText(this@LoginActivity, "Failed to read user data", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun fetchPsychologistData(userId: String) {
        database.child("psychologists").child(userId).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    val psychologistMap = snapshot.value as? Map<*, *>
                    val username = psychologistMap?.get("username") as? String ?: ""

                    Log.d("Login", "Fetched psychologist username: $username")
                    initializeSendBird(userId, username)

                    val intent = Intent(this@LoginActivity, PsychologistActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    startActivity(intent)
                    finish()
                } else {
                    Toast.makeText(this@LoginActivity, "This account is not a psychologist account.", Toast.LENGTH_SHORT).show()
                    auth.signOut()
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.w("Login", "Failed to read psychologist data", error.toException())
                Toast.makeText(this@LoginActivity, "Failed to read psychologist data", Toast.LENGTH_SHORT).show()
            }
        })
    }
}
