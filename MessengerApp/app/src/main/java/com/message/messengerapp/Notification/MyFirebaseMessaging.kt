package com.message.messengerapp.Notification

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.media.RingtoneManager
import android.os.Build
import android.os.Bundle
import androidx.core.app.NotificationCompat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.message.messengerapp.MessageChatActivity

// Service class for handling Firebase Cloud Messaging (FCM) messages
class MyFirebaseMessaging : FirebaseMessagingService() {

    // Override method called when a new FCM message is received
    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)

        // Extract data from the message
        val sented = remoteMessage.data["sented"]
        val user = remoteMessage.data["user"]

        // Get the ID of the current user from SharedPreferences
        val sharedPref = getSharedPreferences("PREFS", Context.MODE_PRIVATE)
        val currentOnlineUser = sharedPref.getString("currentUser", "none")

        // Get the current Firebase user
        val firebaseUser = FirebaseAuth.getInstance().currentUser

        // Check if the message is intended for the current user
        if (firebaseUser != null && sented == firebaseUser.uid) {
            // Check if the current user is not the sender of the message
            if (currentOnlineUser != user) {
                // Decide whether to show a notification based on Android version
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    sendOreoNotification(remoteMessage)
                } else {
                    sendNotification(remoteMessage)
                }
            }
        }
    }

    // Function to send a notification for devices running Android versions lower than Oreo
    private fun sendNotification(remoteMessage: RemoteMessage) {
        // Extract data from the message
        val user = remoteMessage.data["user"]
        val icon = remoteMessage.data["icon"]
        val title = remoteMessage.data["title"]
        val body = remoteMessage.data["body"]

        // Extract user ID from the data
        val j = user?.replace("[\\D]".toRegex(), "")?.toInt() ?: 0

        // Create an intent to open the MessageChatActivity
        val intent = Intent(this, MessageChatActivity::class.java)
        val bundle = Bundle()
        bundle.putString("userid", user)
        intent.putExtras(bundle)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)

        // Create a pending intent for the notification
        val pendingIntent = PendingIntent.getActivity(
            this,
            j,
            intent,
            PendingIntent.FLAG_ONE_SHOT or PendingIntent.FLAG_IMMUTABLE
        )

        // Set the default notification sound
        val defaultSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)

        // Build the notification
        val builder: NotificationCompat.Builder = NotificationCompat.Builder(this)
            .setSmallIcon(icon?.toInt() ?: 0)
            .setContentTitle(title)
            .setContentText(body)
            .setAutoCancel(true)
            .setSound(defaultSound)
            .setContentIntent(pendingIntent)

        // Display the notification
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(j, builder.build())
    }

    // Function to send a notification for devices running Android Oreo and above
    private fun sendOreoNotification(remoteMessage: RemoteMessage) {
        // Extract data from the message
        val user = remoteMessage.data["user"]
        val icon = remoteMessage.data["icon"]
        val title = remoteMessage.data["title"]
        val body = remoteMessage.data["body"]

        // Extract user ID from the data
        val j = user?.replace("[\\D]".toRegex(), "")?.toInt() ?: 0

        // Create an intent to open the MessageChatActivity
        val intent = Intent(this, MessageChatActivity::class.java)
        val bundle = Bundle()
        bundle.putString("userid", user)
        intent.putExtras(bundle)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)

        // Create a pending intent for the notification
        val pendingIntent = PendingIntent.getActivity(
            this,
            j,
            intent,
            PendingIntent.FLAG_ONE_SHOT or PendingIntent.FLAG_IMMUTABLE
        )

        // Set the default notification sound
        val defaultSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)

        // Create an instance of OreoNotification class
        val oreoNotification = OreoNotification(this)

        // Build the Oreo notification
        val builder = oreoNotification.getOreoNotification(title, body, pendingIntent, defaultSound, icon)

        // Display the notification
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(j, builder.build())
    }
}
