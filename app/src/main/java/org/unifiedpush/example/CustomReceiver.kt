package org.unifiedpush.example

import android.content.Context
import org.unifiedpush.connector.MessagingReceiver
import org.unifiedpush.connector.MessagingReceiverHandler
import java.lang.Exception
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

    override fun onNewEndpoint(context: Context?, endpoint: String) {}
    override fun onUnregistered(context: Context?){}
    override fun onUnregisteredAck(context: Context?){}
}

class CustomReceiver: MessagingReceiver(handler)