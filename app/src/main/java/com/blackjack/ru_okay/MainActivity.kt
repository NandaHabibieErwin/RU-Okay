package com.blackjack.ru_okay

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.blackjack.ru_okay.databinding.ActivityMainBinding
import com.blackjack.ru_okay.fragments.CommunityFragment
import com.blackjack.ru_okay.fragments.ConsultFragment
import com.blackjack.ru_okay.fragments.HomeFragment
import com.blackjack.ru_okay.fragments.JournalFragment
import com.blackjack.ru_okay.fragments.SettingFragment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.sendbird.android.exception.SendbirdException
import com.sendbird.android.handler.InitResultHandler
import com.sendbird.uikit.SendbirdUIKit
import com.sendbird.uikit.adapter.SendbirdUIKitAdapter
import com.sendbird.uikit.interfaces.UserInfo

class MainActivity : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth
    private lateinit var binding: ActivityMainBinding
    private lateinit var database: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance("https://ru-okaydemo-default-rtdb.asia-southeast1.firebasedatabase.app/").reference
        val currentUser = auth.currentUser
        val userID = currentUser?.uid ?: "null"

        // Initialize ViewBinding
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Retrieve username and initialize Sendbird
        retrieveUsernameAndInitializeSendbird(userID)

        // Load initial fragment
        loadFragment(HomeFragment.newInstance(userID))

        // Set up bottom navigation
        binding.bottomNav.setOnItemSelectedListener {
            when (it.itemId) {
                R.id.home -> {
                    loadFragment(HomeFragment.newInstance(userID))
                    true
                }
                R.id.consult -> {
                    loadFragment(ConsultFragment())
                    true
                }
                R.id.community -> {
                    loadFragment(CommunityFragment())
                    true
                }
                R.id.journal -> {
                    loadFragment(JournalFragment())
                    true
                }
                R.id.profile -> {
                    loadFragment(SettingFragment())
                    true
                }
                else -> false
            }
        }
    }

    private fun retrieveUsernameAndInitializeSendbird(userID: String) {
        database.child("users").child(userID).child("profile").child("username").addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val username = snapshot.value as? String ?: "default_username"
                initSendbird(userID, username)
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("MainActivity", "Failed to retrieve username: ${error.message}")
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
}
