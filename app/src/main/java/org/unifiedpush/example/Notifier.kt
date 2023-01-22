package org.unifiedpush.example

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import java.util.concurrent.ThreadLocalRandom


class Notifier(var context: Context){
    /** For showing and hiding our notification.  */
    private var gNM: NotificationManager? = null
    private var channelId = "UP-example-ID"

    init {
        channelId = context.packageName
        createNotificationChannel()
        gNM = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    }

    fun sendNotification(title: String,text: String, priority: Int){
        val notificationBuilder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            Notification.Builder(context, channelId)
        } else {
            Notification.Builder(context)
        }

        val notification =
            notificationBuilder.setSmallIcon(R.drawable.ic_launcher_notification) // the status icon
                .setTicker(text) // the status text
                .setWhen(System.currentTimeMillis()) // the time stamp
                .setContentTitle(title) // the label of the entry
                .setContentText(text) // the contents of the entry
                .setPriority(priority)
                .build()

        val notificationId = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            ThreadLocalRandom.current().nextInt()
        } else {
            13737
        }
        gNM!!.notify(notificationId, notification)
    }

    private fun createNotificationChannel() {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && channelId.isNotEmpty()) {
            val name = context.packageName
            val descriptionText = "UP - example"
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(channelId, name, importance).apply {
                description = descriptionText
            }
            // Register the channel with the system
            val notificationManager: NotificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }
}