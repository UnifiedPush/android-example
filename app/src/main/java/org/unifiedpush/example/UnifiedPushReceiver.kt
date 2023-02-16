package org.unifiedpush.example

import android.content.Context
import android.content.Intent
import android.util.Log
import android.widget.Toast
import com.google.crypto.tink.apps.webpush.WebPushHybridDecrypt
import org.unifiedpush.android.connector.MessagingReceiver
import org.unifiedpush.android.connector.UnifiedPush
import org.unifiedpush.example.utils.Notifier
import org.unifiedpush.example.utils.TAG
import org.unifiedpush.example.utils.updateRegistrationInfo
import java.net.URLDecoder
import java.security.interfaces.ECPrivateKey
import java.security.interfaces.ECPublicKey

class UnifiedPushReceiver : MessagingReceiver() {

    override fun onMessage(context: Context, message: ByteArray, instance: String) {
        val store = Store(context)

        if (store.featureByteMessage) {
            Log.d(
                TAG,
                "Bytes: " + message.joinToString("") { byte ->
                    "%02x".format(byte)
                }
            )
        }

        val params = if (store.webpush) {
            try {
                val keyPair = store.keyPair
                val hybridDecrypt = WebPushHybridDecrypt.Builder()
                    .withAuthSecret(store.authSecret)
                    .withRecipientPublicKey(keyPair.public as ECPublicKey)
                    .withRecipientPrivateKey(keyPair.private as ECPrivateKey)
                    .build()
                decodeMessage(
                    context,
                    hybridDecrypt.decrypt(message, null).toString(Charsets.UTF_8)
                )
            } catch (_: Exception) {
                mapOf("message" to "Could not decrypt webpush message")
            }
        } else {
            decodeMessage(context, message.toString(Charsets.UTF_8))
        }
        val text = params["message"] ?: "Internal error"
        val priority = params["priority"]?.toInt() ?: 8
        val title = params["title"] ?: context.getString(R.string.app_name)
        Notifier(context).showNotification(title, text, priority)
    }

    override fun onNewEndpoint(context: Context, endpoint: String, instance: String) {
        Log.d(TAG, "New Endpoint: $endpoint")
        val store = Store(context)
        // Send the endpoint to the application server
        if (store.webpush) {
            val auth = store.b64authSecret
            val p256dh = store.serializedPubKey
            if (auth != null && p256dh != null) {
                ApplicationServer(context).storeEndpoint(endpoint, auth, p256dh)
            }
        } else {
            ApplicationServer(context).storeEndpoint(endpoint)
        }
        context.updateRegistrationInfo()
    }

    override fun onRegistrationFailed(context: Context, instance: String) {
        Toast.makeText(context, "Registration Failed", Toast.LENGTH_SHORT).show()
        UnifiedPush.forceRemoveDistributor(context)
    }

    override fun onUnregistered(context: Context, instance: String) {
        // Remove the endpoint on the application server
        ApplicationServer(context).storeEndpoint(null)
        context.updateRegistrationInfo()
        val appName = context.getString(R.string.app_name)
        Toast.makeText(context, "$appName is unregistered", Toast.LENGTH_SHORT).show()
    }

    override fun onReceive(context: Context, intent: Intent) {
        Log.d(TAG, "event received")
        super.onReceive(context, intent)
    }

    private fun decodeMessage(context: Context, message: String): Map<String, String> {
        val params = try {
            val dict = URLDecoder.decode(message, "UTF-8").split("&")
            dict.associate {
                try {
                    it.split("=")[0] to it.split("=")[1]
                } catch (e: Exception) {
                    "" to ""
                }
            }
        } catch (e: Exception) {
            mapOf("message" to message)
        }
        if (params.keys.contains("message") && params.keys.contains("title")) {
            return params
        }
        return mapOf("message" to message)
    }
}
