package org.unifiedpush.example

import android.content.Context
import android.content.Intent
import android.util.Log
import android.widget.Toast
import org.unifiedpush.android.connector.EXTRA_BYTES_MESSAGE
import org.unifiedpush.android.connector.MessagingReceiver
import org.unifiedpush.android.connector.UnifiedPush
import org.unifiedpush.example.utils.Notifier
import org.unifiedpush.example.utils.TAG
import org.unifiedpush.example.utils.updateRegistrationInfo
import java.net.URLDecoder

class UnifiedPushReceiver : MessagingReceiver() {

    private lateinit var store: Store
    override fun onMessage(context: Context, message: ByteArray, instance: String) {
        val dict = URLDecoder.decode(String(message), "UTF-8").split("&")
        val params = dict.associate {
            try {
                it.split("=")[0] to it.split("=")[1]
            } catch (e: Exception) {
                "" to ""
            }
        }
        val text = params["message"] ?: "Could not find message content"
        val priority = params["priority"]?.toInt() ?: 8
        val title = params["title"] ?: context.getString(R.string.app_name)
        Notifier(context).showNotification(title, text, priority)
    }

    override fun onNewEndpoint(context: Context, endpoint: String, instance: String) {
        Log.d(TAG, "New Endpoint: $endpoint")
        store.endpoint = endpoint
        context.updateRegistrationInfo()
    }

    override fun onRegistrationFailed(context: Context, instance: String) {
        Toast.makeText(context, "Registration Failed", Toast.LENGTH_SHORT).show()
        UnifiedPush.forceRemoveDistributor(context)
    }

    override fun onUnregistered(context: Context, instance: String) {
        store.endpoint = null
        context.updateRegistrationInfo()
        val appName = context.getString(R.string.app_name)
        Toast.makeText(context, "$appName is unregistered", Toast.LENGTH_SHORT).show()
    }

    override fun onReceive(context: Context, intent: Intent) {
        Log.d(TAG, "event received")
        store = Store(context)
        if (store.featureByteMessage) {
            Log.d(
                TAG,
                "Bytes: " + intent.getByteArrayExtra(EXTRA_BYTES_MESSAGE)?.joinToString("") { byte ->
                    "%02x".format(byte)
                }
            )
        }
        super.onReceive(context, intent)
    }
}
