package org.unifiedpush.example

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter

private const val UPDATE = "org.unifiedpush.example.android.action.UPDATE"

fun Context.updateRegistrationInfo(registered: Boolean, endpoint: String?) {
    val broadcastIntent = Intent()
    broadcastIntent.`package` = this.packageName
    broadcastIntent.action = UPDATE
    broadcastIntent.putExtra("registered", registered)
    broadcastIntent.putExtra("endpoint", endpoint)
    this.sendBroadcast(broadcastIntent)
}

fun Context.registerOnRegistrationUpdate(
    onUpdate: (registered: Boolean, endpoint: String?) -> Unit
): BroadcastReceiver {
    val intentFilter = IntentFilter().apply {
        addAction(UPDATE)
    }
    val checkReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            when (intent!!.action) {
                UPDATE -> {
                    val registered = intent.getBooleanExtra("registered", false)
                    val endpoint = intent.getStringExtra("endpoint")
                    onUpdate(registered, endpoint)
                }
            }
        }
    }
    this.registerReceiver(checkReceiver, intentFilter)
    return checkReceiver
}
