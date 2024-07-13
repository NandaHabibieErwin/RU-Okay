package com.blackjack.ru_okay.auth

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import android.content.Intent
import android.os.Handler
import android.os.Looper
import com.blackjack.ru_okay.R

class SplashActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splashscreen)

        Handler(Looper.getMainLooper()).postDelayed({
            val intent = Intent(this, LandingActivity::class.java)
            startActivity(intent)
            finish()
        }, 3000)
    }
}