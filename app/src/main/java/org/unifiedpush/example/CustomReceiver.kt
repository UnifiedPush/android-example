package org.unifiedpush.example

import android.content.Context
import android.content.Intent
import android.util.Log
import android.widget.Toast
import org.unifiedpush.android.connector.LOG_TAG
import org.unifiedpush.android.connector.MessagingReceiver
import org.unifiedpush.android.connector.UnifiedPush
import org.unifiedpush.example.Utils.updateRegistrationInfo
import java.net.URLDecoder

class CustomReceiver : MessagingReceiver() {
    override fun onMessage(context: Context?, message: ByteArray, instance: String) {
        val dict = URLDecoder.decode(String(message),"UTF-8").split("&")
        val params= dict.associate { try{it.split("=")[0] to it.split("=")[1]}catch (e: Exception){"" to ""} }
        val text = params["message"]?: "New notification"
        val priority = params["priority"]?.toInt()?: 8
        val title = params["title"]?: context!!.getString(R.string.app_name)
        Notifier(context!!).sendNotification(title,text,priority)
    }

    override fun onNewEndpoint(context: Context?, endpoint: String, instance: String) {
        Log.d(LOG_TAG, "New Endpoint: $endpoint")
        updateRegistrationInfo(context!!, endpoint, true)
    }

    override fun onRegistrationFailed(context: Context?, instance: String) {
        Toast.makeText(context, "Registration Failed", Toast.LENGTH_SHORT).show()
        UnifiedPush.forceRemoveDistributor(context!!)
    }

    override fun onUnregistered(context: Context?, instance: String){
        updateRegistrationInfo(context!!, "", false)
        val appName = context.getString(R.string.app_name)
        Toast.makeText(context, "$appName is unregistered", Toast.LENGTH_SHORT).show()
    }

    override fun onReceive(context: Context?, intent: Intent?) {
        Log.d(LOG_TAG, "event received")
        super.onReceive(context, intent)
    }
}
