package org.unifiedpush.example

import android.content.Context
import android.content.Intent
import android.widget.Toast
import org.unifiedpush.android.connector.MessagingReceiver
import org.unifiedpush.android.connector.MessagingReceiverHandler
import java.net.URLDecoder

val handler = object: MessagingReceiverHandler{
    override fun onMessage(context: Context?, message: String) {
        val dict = URLDecoder.decode(message,"UTF-8").split("&")
        val params= dict.associate { try{it.split("=")[0] to it.split("=")[1]}catch (e: Exception){"" to ""} }
        val text = params["message"]?: "New notification"
        val priority = params["priority"]?.toInt()?: 8
        val title = params["title"]?: "UP - Example"
        Notifier(context!!).sendNotification(title,text,priority)
    }

    override fun onNewEndpoint(context: Context?, endpoint: String) {
        val broadcastIntent = Intent()
        broadcastIntent.`package` = context!!.packageName
        broadcastIntent.action = UPDATE
        broadcastIntent.putExtra("endpoint", endpoint)
        broadcastIntent.putExtra("registered", "true")
        context.sendBroadcast(broadcastIntent)
    }

    override fun onRegistrationFailed(context: Context?) {
        Toast.makeText(context, "Registration Failed", Toast.LENGTH_SHORT).show()
    }

    override fun onRegistrationRefused(context: Context?) {
        Toast.makeText(context, "Registration is Refused", Toast.LENGTH_SHORT).show()
    }

    override fun onUnregistered(context: Context?){
        val broadcastIntent = Intent()
        broadcastIntent.`package` = context!!.packageName
        broadcastIntent.action = UPDATE
        broadcastIntent.putExtra("endpoint", "")
        broadcastIntent.putExtra("registered", "false")
        context.sendBroadcast(broadcastIntent)
    }
}

class CustomReceiver: MessagingReceiver(handler)
