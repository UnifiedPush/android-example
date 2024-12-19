package org.unifiedpush.example

import android.app.Activity
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import java.util.Timer
import kotlin.concurrent.schedule
import org.unifiedpush.example.utils.DelayedRequestWorker

class Tests(private val activity: Activity) {
    fun testTTL(callback: (error: String?) -> Unit) {
        testTTLIntro {
            ApplicationServer(activity).sendTestTTLNotification { e ->
                Toast.makeText(activity, "Notification sent.", Toast.LENGTH_SHORT).show()
                e?. run {
                    Toast.makeText(activity, "TTL not (fully) supported.", Toast.LENGTH_SHORT).show()
                } ?: run {
                    Thread.dumpStack()
                    Timer().schedule(10_000L) {
                        activity.runOnUiThread {
                            Toast.makeText(
                                activity,
                                "You can reconnect your distributor.",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                }
                callback(e)
            }
        }
    }

    private fun testTTLIntro(onSuccess: () -> Unit) {
        val builder: AlertDialog.Builder = AlertDialog.Builder(activity)
        builder.setTitle("Testing TTL")
        builder.setMessage(
            "To check the TTL, you must first disconnect your distributor.\n" +
                "You can reconnect it after 10 seconds.\n" +
                "A notification will be sent, it should not be displayed by the application.\n\n" +
                "Press OK once the distributor is disconnected."
        )
        builder.setPositiveButton(android.R.string.ok) { _, _ ->
            onSuccess()
        }
        builder.setNegativeButton(android.R.string.cancel) { _, _ ->
            Toast.makeText(activity, "Aborting", Toast.LENGTH_SHORT).show()
        }
        builder.create().show()
    }

    fun testTopic(callback: (error: String?) -> Unit) {
        testTopicIntro {
            ApplicationServer(activity).sendTestTopicNotifications { e ->
                Toast.makeText(activity, "Notifications sent.", Toast.LENGTH_SHORT).show()
                e ?: run {
                    Thread.dumpStack()
                    Timer().schedule(10_000L) {
                        activity.runOnUiThread {
                            Toast.makeText(
                                activity,
                                "You can reconnect your distributor.",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                }
                callback(e)
            }
        }
    }

    private fun testTopicIntro(onSuccess: () -> Unit) {
        val builder: AlertDialog.Builder = AlertDialog.Builder(activity)
        builder.setTitle("Testing Topic")
        builder.setMessage(
            "To check topics, you must first disconnect your distributor.\n" +
                "You can reconnect it after 10 seconds.\n" +
                "2 notifications will be sent, only the 2nd one should be displayed by the application.\n\n" +
                "If you see 2 notifications, your distributor doesn't support notification update.\n\n" +
                "Press OK once the distributor is disconnected."
        )
        builder.setPositiveButton(android.R.string.ok) { _, _ ->
            onSuccess()
        }
        builder.setNegativeButton(android.R.string.cancel) { _, _ ->
            Toast.makeText(activity, "Aborting", Toast.LENGTH_SHORT).show()
        }
        builder.create().show()
    }

    /**
     * Display introduction for test sending a notification while the app is in background.
     *
     * Calls [testMessageInBackgroundIntro] and set [runBackgroundCheck] to `true`.
     *
     * It requires the user to put the application in background and [testMessageInBackgroundRun]
     * to be called in [Activity.onPause].
     */
    fun testMessageInBackgroundStart() {
        testMessageInBackgroundIntro { runBackgroundCheck = true }
    }

    private fun testMessageInBackgroundIntro(onSuccess: () -> Unit) {
        val builder: AlertDialog.Builder = AlertDialog.Builder(activity)
        builder.setTitle("Testing notifications in background")
        builder.setMessage(
            "To check, you need to put this application in the background.\n" +
                "A notification will be sent after 7 seconds.\n\n" +
                "It's also possible to put the application in the background and send unencrypted " +
                "POST message to the endpoint via a terminal to test foreground services.\n\n" +
                "Press OK to continue."
        )
        builder.setPositiveButton(android.R.string.ok) { _, _ ->
            Toast.makeText(activity, "Notification will be sent in the background", Toast.LENGTH_SHORT).show()
            onSuccess()
        }
        builder.setNegativeButton(android.R.string.cancel) { _, _ ->
            Toast.makeText(activity, "Aborting", Toast.LENGTH_SHORT).show()
        }
        builder.create().show()
    }

    /**
     * Send a notification after 5 seconds.
     *
     * Should be called in [Activity.onPause]
     */
    fun testMessageInBackgroundRun() {
        if (runBackgroundCheck) {
            runBackgroundCheck = false
            DelayedRequestWorker.enqueue(activity, 7_000L)
        }
    }

    private companion object {
        var runBackgroundCheck = false
    }
}
