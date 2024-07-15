package com.blackjack.ru_okay.agora
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.app.Notification
import android.app.NotificationManager
import androidx.core.app.NotificationCompat
import com.blackjack.ru_okay.R

class NotificationReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val consultId = intent.getStringExtra("consultId")

        val notification = NotificationCompat.Builder(context, "consult_reminder_channel")
            .setSmallIcon(R.drawable.icon)
            .setContentTitle("Consultation Reminder")
            .setContentText("You have a consultation scheduled in one hour.")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .build()

        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(consultId.hashCode(), notification)
    }
}
