package com.message.messengerapp.Notification

import android.annotation.TargetApi
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.ContextWrapper
import android.net.Uri
import android.os.Build

// Helper class for managing notifications on devices running Android Oreo (API level 26) and above
class OreoNotification(base: Context?) : ContextWrapper(base) {

    // NotificationManager instance for managing notifications
    private var notificationManager: NotificationManager? = null

    companion object {
        // Constants for the notification channel
        private const val CHANNEL_ID = "com.message.messengerapp"
        private const val CHANNEL_NAME = "Messenger App"
    }

    // Initialize the notification channel
    init {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            createChannel()
        }
    }

    // Create the notification channel
    @TargetApi(Build.VERSION_CODES.O)
    private fun createChannel() {
        // Create a new notification channel with the specified ID, name, and importance
        val channel = NotificationChannel(
            CHANNEL_ID,
            CHANNEL_NAME,
            NotificationManager.IMPORTANCE_DEFAULT
        )
        // Configure the channel settings
        channel.enableLights(false)
        channel.enableVibration(true)
        channel.lockscreenVisibility = Notification.VISIBILITY_PRIVATE
        // Register the channel with the system
        getManager!!.createNotificationChannel(channel)
    }

    // Get the NotificationManager instance
    val getManager: NotificationManager?
        get() {
            if (notificationManager == null) {
                // Initialize the NotificationManager if it's null
                notificationManager =
                    getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            }
            return notificationManager
        }

    // Build and return an Oreo notification
    @TargetApi(Build.VERSION_CODES.O)
    fun getOreoNotification(
        title: String?,
        body: String?,
        pendingIntent: PendingIntent?,
        soundUri: Uri?,
        icon: String?
    ): Notification.Builder {
        // Create and configure a notification builder with the specified channel ID
        return Notification.Builder(applicationContext, CHANNEL_ID)
            .setContentIntent(pendingIntent)
            .setContentTitle(title)
            .setContentText(body)
            .setSmallIcon(icon!!.toInt())
            .setSound(soundUri)
            .setAutoCancel(true)
    }
}
