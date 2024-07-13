package com.blackjack.ru_okay.consult

import android.os.Bundle
import android.util.Log
import android.widget.FrameLayout
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.blackjack.ru_okay.R
import io.agora.agorauikit_android.*
import kotlinx.coroutines.*

class CallActivity : AppCompatActivity() {
    private var agView: AgoraVideoViewer? = null
    private val appId = "6f7c47cff1da4228b5cc2877c076cfc8"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_call)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val psychologistId = intent.getStringExtra("psychologistId") ?: "defaultChannel"
        val psychologistName = intent.getStringExtra("psychologistName")

        // Initialize AgoraVideoViewer here
        initializeAgoraViewer(psychologistId, psychologistName)
    }

    private fun initializeAgoraViewer(channelId: String, info: String?) {
        CoroutineScope(Dispatchers.Main).launch {
            try {
                agView = withContext(Dispatchers.Default) {
                    AgoraVideoViewer(
                        this@CallActivity,
                        AgoraConnectionData(appId),
                        AgoraVideoViewer.Style.FLOATING // You can change the style as per your requirement
                    ).apply {
                        // Enable the local video feed

                    }
                }

                agView?.let {
                    addContentView(
                        it,
                        FrameLayout.LayoutParams(
                            FrameLayout.LayoutParams.MATCH_PARENT,
                            FrameLayout.LayoutParams.MATCH_PARENT
                        )
                    )

                    // Join the channel
                    it.join(
                        channel = channelId,
                        role = io.agora.rtc2.Constants.CLIENT_ROLE_BROADCASTER
                    )

                    Log.d("CallActivity", "Successfully joined channel: $channelId")
                }
            } catch (e: Exception) {
                Log.e("CallActivity", "Error initializing AgoraVideoViewer: ${e.message}")
            }
        }
    }
}
