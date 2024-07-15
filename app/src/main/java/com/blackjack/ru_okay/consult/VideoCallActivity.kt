package com.blackjack.ru_okay.consult

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.widget.FrameLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import io.agora.agorauikit_android.AgoraConnectionData
import io.agora.agorauikit_android.AgoraSettings
import io.agora.agorauikit_android.AgoraVideoViewer
import io.agora.rtc2.Constants
import com.blackjack.ru_okay.R

class VideoCallActivity : AppCompatActivity() {

    private var agView: AgoraVideoViewer? = null

    // Fill the app ID of your project generated on Agora Console.
    private val appId: String = "6f7c47cff1da4228b5cc2877c076cfc8"

    // Fill the channel name.
    private val channelName = "Consult"

    // Fill the temp token generated on Agora Console.
    private val token = "007eJxTYHgS657Hk2z99FnBxGxf9bc6Ch0Xl2o+WKuSYSHPmHU49p4Cg1maebKJeXJammFKoomRkUWSaXKykYW5ebKBuVlyWrJFye3JaQ2BjAyWBz4yMEIhiM/O4JyfV1yaU8LAAACAxCC9"

    private val PERMISSION_REQ_ID = 22
    private val REQUESTED_PERMISSIONS = arrayOf(
        Manifest.permission.RECORD_AUDIO,
        Manifest.permission.CAMERA
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_call)

        if (checkSelfPermission(REQUESTED_PERMISSIONS[0], PERMISSION_REQ_ID) &&
            checkSelfPermission(REQUESTED_PERMISSIONS[1], PERMISSION_REQ_ID)) {
            initializeAndJoinChannel()
        }
    }

    private fun checkSelfPermission(permission: String, requestCode: Int): Boolean {
        if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, REQUESTED_PERMISSIONS, requestCode)
            return false
        }
        return true
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            PERMISSION_REQ_ID -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED &&
                    grantResults[1] == PackageManager.PERMISSION_GRANTED) {
                    initializeAndJoinChannel()
                } else {
                    Log.e("Permission", "Permissions not granted")
                }
            }
        }
    }

    private fun initializeAndJoinChannel() {
        runOnUiThread {
            try {
                Log.d("VideoCallActivity", "Attempting to create AgoraVideoViewer")
                agView = AgoraVideoViewer(this, AgoraConnectionData(appId, token), AgoraVideoViewer.Style.FLOATING, AgoraSettings())
                Log.d("VideoCallActivity", "AgoraVideoViewer created successfully")

                addContentView(agView, FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT))
                Log.d("VideoCallActivity", "AgoraVideoViewer added to layout. Joining channel")
                joinChannel()
            } catch (e: Exception) {
                Log.e("AgoraVideoViewer", "Could not initialize AgoraVideoViewer. Check that your app Id is valid.")
                Log.e("Exception", e.toString())
            }
        }
    }

    private fun joinChannel() {
        runOnUiThread {
            try {
                Log.d("VideoCallActivity", "Joining channel: $channelName")
                agView?.join(channelName, token, Constants.CLIENT_ROLE_BROADCASTER)
                Log.d("VideoCallActivity", "Successfully joined channel: $channelName")
            } catch (e: Exception) {
                Log.e("VideoCallActivity", "Error joining channel")
                Log.e("Exception", e.toString())
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        leaveChannelAndCleanup()
    }

    private fun leaveChannelAndCleanup() {
        runOnUiThread {
            try {
                agView?.leaveChannel()
                agView = null
                Log.d("VideoCallActivity", "Left channel and cleaned up resources")
            } catch (e: Exception) {
                Log.e("VideoCallActivity", "Error leaving channel or cleaning up resources")
                Log.e("Exception", e.toString())
            }
        }
    }
}
