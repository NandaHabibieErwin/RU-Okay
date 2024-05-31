    package com.blackjack.ru_okay

    import android.content.Intent
    import android.os.Bundle
    import android.util.Log
    import android.widget.Button
    import android.widget.EditText
    import androidx.activity.enableEdgeToEdge
    import androidx.appcompat.app.AppCompatActivity
    import androidx.core.view.ViewCompat
    import androidx.core.view.WindowInsetsCompat
    import com.google.firebase.auth.FirebaseAuth

    class RegisterActivity : AppCompatActivity() {
        val auth: FirebaseAuth = FirebaseAuth.getInstance()
        override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)
            if (auth.currentUser != null){
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

                val registerButton = findViewById<Button>(R.id.btnRegister)
                registerButton.setOnClickListener {
                    val emailEditText = findViewById<EditText>(R.id.registerEmail)  // Replace with your email EditText id
                    val passwordEditText = findViewById<EditText>(R.id.registerPassword)  // Replace with your password EditText id
                    val email = emailEditText.text.toString()
                    val password = passwordEditText.text.toString()

                    registerUser(email, password)
                }
            }

        private fun registerUser(email: String, password: String) {
            auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        // Registration successful
                        val user = auth.currentUser
                        val intent = Intent(this, MainActivity::class.java)
                        startActivity(intent)
                        finish()
                    } else {
                        // Registration failed
                        Log.w("Registration", "createUserWithEmail:failure", task.exception)
                    }
                }
        }
        }
