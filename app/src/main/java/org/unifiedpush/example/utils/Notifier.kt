package org.unifiedpush.example.utils

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.annotation.RequiresApi
import org.unifiedpush.example.R
import java.util.concurrent.ThreadLocalRandom

class Notifier(var context: Context) {
    private val channelId = context.packageName
    private val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    init {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            createNotificationChannel()
        }
    }

    fun showNotification(
        title: String,
        text: String,
        priority: Int,
    ) {
        val notificationBuilder =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
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

        val notificationId =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                ThreadLocalRandom.current().nextInt()
            } else {
                13737
            }
        nm.notify(notificationId, notification)
    }

    @RequiresApi(26)
    private fun createNotificationChannel() {
        val name = context.packageName
        val descriptionText = "Test notifications"
        val importance = NotificationManager.IMPORTANCE_DEFAULT
        val channel =
            NotificationChannel(channelId, name, importance).apply {
                description = descriptionText
            }
        nm.createNotificationChannel(channel)
    }
}
