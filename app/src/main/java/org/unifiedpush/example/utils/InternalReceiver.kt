package org.unifiedpush.example.utils

import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build

private const val UPDATE = "org.unifiedpush.example.android.action.UPDATE"

fun Context.updateRegistrationInfo() {
    val broadcastIntent = Intent()
    broadcastIntent.`package` = this.packageName
    broadcastIntent.action = UPDATE
    this.sendBroadcast(broadcastIntent)
}

@SuppressLint("UnspecifiedRegisterReceiverFlag")
fun Context.registerOnRegistrationUpdate(onUpdate: () -> Unit): BroadcastReceiver {
    val intentFilter =
        IntentFilter().apply {
            addAction(UPDATE)
        }
    val checkReceiver: BroadcastReceiver =
        object : BroadcastReceiver() {
            override fun onReceive(
                context: Context?,
                intent: Intent?,
            ) {
                when (intent!!.action) {
                    UPDATE -> {
                        onUpdate()
                    }
                }
            }
        }
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        this.registerReceiver(checkReceiver, intentFilter, Context.RECEIVER_NOT_EXPORTED)
    } else {
        this.registerReceiver(checkReceiver, intentFilter)
    }
    return checkReceiver
}
