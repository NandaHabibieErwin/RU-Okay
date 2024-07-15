package com.blackjack.ru_okay

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.blackjack.ru_okay.consult.CallRequest
import com.blackjack.ru_okay.consult.VideoCallActivity
import com.blackjack.ru_okay.databinding.ActivityPsychologistBinding
import com.blackjack.ru_okay.fragments.PsychologistConsultFragment
import com.blackjack.ru_okay.fragments.PsychologistHomeFragment
import com.blackjack.ru_okay.fragments.PsychologistSettingFragment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.sendbird.android.exception.SendbirdException
import com.sendbird.android.handler.InitResultHandler
import com.sendbird.uikit.SendbirdUIKit
import com.sendbird.uikit.adapter.SendbirdUIKitAdapter
import com.sendbird.uikit.interfaces.UserInfo

class PsychologistActivity : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth
    private lateinit var binding: ActivityPsychologistBinding
    private lateinit var database: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance("https://ru-okaydemo-default-rtdb.asia-southeast1.firebasedatabase.app/").reference
        val currentUser = auth.currentUser
        val userID = currentUser?.uid ?: "null"

        // Initialize ViewBinding
        binding = ActivityPsychologistBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Retrieve username and initialize Sendbird
        retrieveUsernameAndInitializeSendbird(userID)
        listenForIncomingCalls(userID)
        // Load initial fragment
        loadFragment(PsychologistHomeFragment.newInstance(userID))

        // Set up bottom navigation
        binding.bottomNav.setOnItemSelectedListener {
            when (it.itemId) {
                R.id.home -> {
                    loadFragment(PsychologistHomeFragment.newInstance(userID))
                    true
                }
                R.id.consult -> {
                    loadFragment(PsychologistConsultFragment())
                    true
                }
                R.id.profile -> {
                    loadFragment(PsychologistSettingFragment())
                    true
                }
                else -> false
            }
        }
    }

    private fun retrieveUsernameAndInitializeSendbird(userID: String) {
        database.child("psychologists").child(userID).child("profile").child("username").addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val username = snapshot.value as? String ?: "default_username"
                initSendbird(userID, username)
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("PsychologistActivity", "Failed to retrieve username: ${error.message}")
                initSendbird(userID, "default_username")
            }
        })
    }

    private fun initSendbird(userID: String, username: String) {
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
                        return userID
                    }

                    override fun getNickname(): String {
                        return username
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
                        Log.e("Sendbird", "Initialization failed: ${e.message}")
                    }

                    override fun onInitSucceed() {
                        Log.d("Sendbird", "Initialization succeeded")
                        SendbirdUIKit.connect { user, e ->
                            if (e != null) {
                                if (user != null) {
                                    Log.w("", "The user is offline but you can access user information stored in the local cache.")
                                } else {
                                    Log.w("", "The user is offline and you can't access any user information stored in the local cache.")
                                }
                            } else {
                                Log.w("", "The user is online and connected to the server.")
                            }
                        }
                    }
                }
            }
        }, this)
    }

    private fun loadFragment(fragment: Fragment) {
        val transaction = supportFragmentManager.beginTransaction()
        transaction.replace(R.id.container, fragment)
        transaction.commit()
    }

    private fun listenForIncomingCalls(userId: String) {
        val callsRef = FirebaseDatabase.getInstance().reference.child("calls")
        callsRef.orderByChild("calleeId").equalTo(userId).addChildEventListener(object : ChildEventListener {
            override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                val callRequest = snapshot.getValue(CallRequest::class.java)
                if (callRequest?.status == "ringing") {
                    showIncomingCallDialog(snapshot.key!!, callRequest.callerId, callRequest.channelName)
                }
            }

            override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {}
            override fun onChildRemoved(snapshot: DataSnapshot) {}
            override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {}
            override fun onCancelled(error: DatabaseError) {}
        })
    }

    private fun showIncomingCallDialog(callId: String, callerId: String, channelName: String) {
        val dialog = AlertDialog.Builder(this)
            .setTitle("Incoming Call")
            .setMessage("You have an incoming call from $callerId")
            .setPositiveButton("Answer") { _, _ ->
                answerCall(callId, channelName)
            }
            .setNegativeButton("Reject") { _, _ ->
                rejectCall(callId)
            }
            .create()
        dialog.show()
    }

    private fun answerCall(callId: String, channelName: String) {
        FirebaseDatabase.getInstance().reference.child("calls").child(callId).child("status").setValue("accepted")
            .addOnCompleteListener {
                if (it.isSuccessful) {
                    val intent = Intent(this, VideoCallActivity::class.java).apply {
                        putExtra("channelName", channelName)
                    }
                    startActivity(intent)
                }
            }
    }

    private fun rejectCall(callId: String) {
        FirebaseDatabase.getInstance().reference.child("calls").child(callId).child("status").setValue("rejected")
    }
}
