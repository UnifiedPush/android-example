package org.unifiedpush.example

import android.app.Activity
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import java.util.Timer
import kotlin.concurrent.schedule

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
        builder.setMessage("To check the TTL, you must first disconnect your distributor.\n" +
                "You can reconnect it after 10 seconds.\n" +
                "A notification will be sent, it should not be displayed by the application.\n\n" +
                "Press OK once the distributor is disconnected.")
        builder.setPositiveButton(android.R.string.ok) { _, _ ->
            onSuccess()
        }
        builder.setNegativeButton(android.R.string.cancel) { _, _ ->
            Toast.makeText(activity, "Aborting", Toast.LENGTH_SHORT).show()
        }
        builder.create().show()
    }
}