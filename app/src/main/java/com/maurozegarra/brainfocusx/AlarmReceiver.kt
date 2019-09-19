package com.maurozegarra.brainfocusx

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class AlarmReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context?, intent: Intent?) {
        val notificationHelper = NotificationHelper()
        notificationHelper.createNotification(context)
    }

}