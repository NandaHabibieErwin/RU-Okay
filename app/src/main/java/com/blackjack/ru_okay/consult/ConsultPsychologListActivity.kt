package com.blackjack.ru_okay.consult

import android.Manifest
import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.blackjack.ru_okay.R
import com.blackjack.ru_okay.databinding.ActivityConsultPsychologListBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class ConsultPsychologListActivity : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth
    private lateinit var binding: ActivityConsultPsychologListBinding
    private lateinit var adapter: PsychologistAdapter
    private val psychologists = mutableListOf<Psychologist>()

    companion object {
        private const val REQUEST_NOTIFICATION_PERMISSION = 1
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityConsultPsychologListBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Create notification channel
        createNotificationChannel()

        auth = FirebaseAuth.getInstance()

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        adapter = PsychologistAdapter(psychologists) { psychologist ->
            val intent = Intent(this, VideoCallActivity::class.java).apply {
            }
            startActivity(intent)
            showNotification(psychologist)
        }
        binding.recyclerViewPsychologists.layoutManager = LinearLayoutManager(this)
        binding.recyclerViewPsychologists.adapter = adapter

        checkUserAndLoadData()
    }

    private fun checkUserAndLoadData() {
        val currentUser = auth.currentUser
        if (currentUser == null) {
            // Redirect to login activity if user is not authenticated
            Log.w("ConsultPsychologListActivity", "Unauthenticated")
        } else {
            loadPsychologists()
        }
    }

    private fun loadPsychologists() {
        val databaseReference = FirebaseDatabase.getInstance().reference.child("psychologists")
        databaseReference.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                psychologists.clear()
                for (dataSnapshot in snapshot.children) {
                    val id = dataSnapshot.key ?: ""
                    val profile = dataSnapshot.child("profile")
                    val name = profile.child("username").getValue(String::class.java) ?: ""
                    val specialty = "Specialty info not available" // Add actual field if available
                    val imageUrl = "Image URL not available" // Add actual field if available
                    val rating = 0.0f // Add actual field if available
                    val psychologist = Psychologist(id, name, specialty, imageUrl, rating)
                    psychologists.add(psychologist)
                }
                adapter.notifyDataSetChanged()
            }

            override fun onCancelled(error: DatabaseError) {
                // Handle possible errors.
                Log.e("ConsultPsychologListActivity", "Failed to load psychologists", error.toException())
            }
        })
    }

    private fun showNotification(psychologist: Psychologist) {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
            != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                this, arrayOf(Manifest.permission.POST_NOTIFICATIONS),
                REQUEST_NOTIFICATION_PERMISSION
            )
        } else {
            displayNotification(psychologist)
            notifyPsychologist(psychologist)
        }
    }

    @SuppressLint("MissingPermission")
    private fun displayNotification(psychologist: Psychologist) {
        val intent = Intent(this, VideoCallActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent: PendingIntent = PendingIntent.getActivity(
            this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE
        )

        val builder = NotificationCompat.Builder(this, "consult_channel_id")
            .setSmallIcon(R.drawable.icon) // Replace with your own icon
            .setContentTitle("Consultation Started")
            .setContentText("You have joined a consultation with ${psychologist.name}.")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)

        with(NotificationManagerCompat.from(this)) {
            // notificationId is a unique int for each notification that you must define
            notify(1, builder.build())
        }
    }

    private fun notifyPsychologist(psychologist: Psychologist) {
        val notificationData = mapOf(
            "userJoined" to true,
            "message" to "A user has joined your consultation."
        )

        FirebaseDatabase.getInstance().reference.child("psychologists")
            .child(psychologist.id).child("notifications")
            .setValue(notificationData)
            .addOnSuccessListener {
                Log.d("ConsultPsychologListActivity", "Psychologist notified successfully")
            }
            .addOnFailureListener { e ->
                Log.e("ConsultPsychologListActivity", "Failed to notify psychologist", e)
            }
    }



    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "Consultation Channel"
            val descriptionText = "Channel for consultation notifications"
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel("consult_channel_id", name, importance).apply {
                description = descriptionText
            }
            val notificationManager: NotificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_NOTIFICATION_PERMISSION) {
            if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                // Permission granted, display the notification
                // You might want to keep track of the psychologist for which the notification was to be shown
                // and call displayNotification(psychologist) here if needed.
            } else {
                Toast.makeText(this, "Notification permission denied", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
