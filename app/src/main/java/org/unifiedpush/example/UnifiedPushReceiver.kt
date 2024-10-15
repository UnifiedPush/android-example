package org.unifiedpush.example

import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import android.widget.Toast
import org.unifiedpush.android.connector.FailedReason
import org.unifiedpush.android.connector.MessagingReceiver
import org.unifiedpush.android.connector.UnifiedPush
import org.unifiedpush.android.connector.data.PushEndpoint
import org.unifiedpush.android.connector.data.PushMessage
import org.unifiedpush.example.utils.Notifier
import org.unifiedpush.example.utils.TAG
import org.unifiedpush.example.utils.decodeMessage
import org.unifiedpush.example.utils.updateRegistrationInfo

class UnifiedPushReceiver : MessagingReceiver() {
    override fun onMessage(
        context: Context,
        message: PushMessage,
        instance: String,
    ) {
        val store = Store(context)
        if (!store.devMode){
            val params = decodeMessage(message.content.toString(Charsets.UTF_8))
            notify(context, params)
        } else {
            // For developer mode only
            val params = if (store.devForceEncrypted && !message.decrypted) {
                mapOf(
                    "title" to "Error",
                    "message" to "Couldn't decrypt message.",
                    "priority" to "8",
                )
            } else {
                decodeMessage(message.content.toString(Charsets.UTF_8))
            }
            notify(context, params)
            if (store.devStartForeground && Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                TestService.startForeground(context)
                context.updateRegistrationInfo()
            }
        }
    }

    private fun notify(context: Context, params: Map<String, String>) {
        val text = params["message"] ?: "Internal error"
        val priority = params["priority"]?.toInt() ?: 8
        val title = params["title"] ?: context.getString(R.string.app_name)
        Notifier(context).showNotification(title, text, priority)
    }

    override fun onNewEndpoint(
        context: Context,
        endpoint: PushEndpoint,
        instance: String,
    ) {
        Log.d(TAG, "New Endpoint: $endpoint")
        ApplicationServer(context).storeEndpoint(endpoint.url)
        endpoint.pubKeySet?.let {
            ApplicationServer(context).storeWebPushKeys(
                it.auth,
                it.pubKey
            )
        }
        context.updateRegistrationInfo()
    }

    override fun onRegistrationFailed(context: Context, reason: FailedReason, instance: String) {
        Toast.makeText(context, "Registration Failed: $reason", Toast.LENGTH_SHORT).show()
        if (reason == FailedReason.VAPID_REQUIRED) {
            if (vapidImplementedForSdk()) {
                val store = Store(context)
                store.distributorRequiresVapid = true
                ApplicationServer(context).genVapidKey()
                UnifiedPush.registerApp(context, instance, vapid = store.vapidPubKey)
            } else {
                Toast.makeText(context, "Distributor requires VAPID but it isn't implemented for old Android versions.", Toast.LENGTH_SHORT).show()
                UnifiedPush.forceRemoveDistributor(context)
            }
        } else {
            UnifiedPush.forceRemoveDistributor(context)
        }
    }

    override fun onUnregistered(
        context: Context,
        instance: String,
    ) {
        // Remove the endpoint on the application server
        ApplicationServer(context).storeEndpoint(null)
        context.updateRegistrationInfo()
        val appName = context.getString(R.string.app_name)
        Toast.makeText(context, "$appName is unregistered", Toast.LENGTH_SHORT).show()
    }


    /**
     * Log when an event is received, even if it may be ignored, due to unknown token.
     *
     * You don't need to do this.
     */
    override fun onReceive(
        context: Context,
        intent: Intent,
    ) {
        Log.d(TAG, "event received")
        super.onReceive(context, intent)
    }
}
