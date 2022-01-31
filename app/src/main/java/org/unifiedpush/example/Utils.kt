package org.unifiedpush.example

import android.content.Context
import android.content.Intent

object Utils {
    fun updateRegistrationInfo(context: Context,
                               endpoint: String,
                               registered: Boolean) {
        val broadcastIntent = Intent()
        broadcastIntent.`package` = context.packageName
        broadcastIntent.action = UPDATE
        broadcastIntent.putExtra("endpoint", endpoint)
        broadcastIntent.putExtra("registered", registered)
        context.sendBroadcast(broadcastIntent)
    }
}