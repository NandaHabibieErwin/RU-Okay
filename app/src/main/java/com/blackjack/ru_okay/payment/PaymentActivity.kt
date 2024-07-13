package com.blackjack.ru_okay.payment

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.blackjack.ru_okay.databinding.ActivityPaymentBinding
import com.blackjack.ru_okay.consult.ChatActivity

class PaymentActivity : AppCompatActivity() {
    private lateinit var binding: ActivityPaymentBinding
    private lateinit var psychologistId: String
    private lateinit var action: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPaymentBinding.inflate(layoutInflater)
        setContentView(binding.root)

        psychologistId = intent.getStringExtra("psychologist_id") ?: ""
        action = intent.getStringExtra("action") ?: ""

        binding.payButton.setOnClickListener {
            // Simulate payment success
            val success = true
            if (success) {
                val intent = when (action) {
                    "chat" -> Intent(this, ChatActivity::class.java)
                    "call" -> Intent(this, ChatActivity::class.java)
                    else -> return@setOnClickListener
                }
                intent.putExtra("psychologist_id", psychologistId)
                startActivity(intent)
                finish()
            } else {
                // Handle payment failure
            }
        }
    }
}