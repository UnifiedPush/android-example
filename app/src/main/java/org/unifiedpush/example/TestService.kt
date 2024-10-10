package org.unifiedpush.example

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.annotation.RequiresApi
import org.unifiedpush.example.utils.updateRegistrationInfo
import kotlin.concurrent.Volatile

class TestService: Service() {
    override fun onBind(intent: Intent?): IBinder? {
        Log.d(TAG, "Bound")
        return null
    }

    override fun onCreate() {
        super.onCreate()
        Log.i(TAG, "Created")
        createNotificationChannel()
        val notification = createNotification()
        startForeground(NOTIFICATION_ID_FOREGROUND, notification)
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.i(TAG, "Destroyed")
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager =
                this.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            val channel = NotificationChannel(
                CHANNEL_ID,
                "TestService",
                NotificationManager.IMPORTANCE_HIGH
            ).let {
                it.description = "test"
                it
            }
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun createNotification(): Notification {
        val builder: Notification.Builder =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                Notification.Builder(
                    this,
                    CHANNEL_ID
                )
            } else {
                Notification.Builder(this)
            }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            builder.setForegroundServiceBehavior(Notification.FOREGROUND_SERVICE_IMMEDIATE)
        }

        return builder
            .setContentTitle("TestService")
            .setContentText("Run in foreground")
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setTicker("foo")
            .setOngoing(true)
            .build()
    }

    companion object {
        @RequiresApi(Build.VERSION_CODES.O)
        fun startForeground(context: Context) {
            synchronized(lock) {
                val intent = Intent(context, TestService::class.java)
                context.startForegroundService(intent)
                started = true
            }
            context.updateRegistrationInfo()
        }
        fun stop(context: Context) {
            synchronized(lock) {
                val intent = Intent(context, TestService::class.java)
                context.stopService(intent)
                started = false
            }
            context.updateRegistrationInfo()
        }

        fun isStarted(): Boolean = synchronized(lock) {
            started
        }

        @Volatile
        private var started = false
        private var lock = Object()
        private const val CHANNEL_ID = "TestService:ChannelId"
        private const val NOTIFICATION_ID_FOREGROUND = 0x1000
        private const val TAG = "TestService"
    }
}