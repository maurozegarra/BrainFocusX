package com.maurozegarra.brainfocusx

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat

class NotificationHelper {
    private val notificationId: Int = 1
    lateinit var context: Context

    fun createNotification(context: Context?) {
// Create an explicit intent for an Activity in your app
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }

        val pendingIntent: PendingIntent =
            PendingIntent.getActivity(context, 0, intent, 0)

        // 3/4.- Create a notification
        val notificationBuilder = NotificationCompat.Builder(context!!, CHANNEL_1_ID)
            .setSmallIcon(R.drawable.ic_reminder)
            .setContentTitle("Title")
            .setContentText("Message")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            // Set the intent that will fire when the user taps the notification
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)

        // 4/4.- Show the notification
        with(NotificationManagerCompat.from(context)) {
            // notificationId is a unique int for each notification that you must define
            notify(notificationId, notificationBuilder.build())
        }
    }
}