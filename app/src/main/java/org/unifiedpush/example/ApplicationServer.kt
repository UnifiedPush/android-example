package org.unifiedpush.example

import android.content.Context
import android.util.Log
import android.widget.Toast
import com.android.volley.RequestQueue
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.google.crypto.tink.apps.webpush.WebPushHybridEncrypt
import org.unifiedpush.example.utils.TAG
import java.security.interfaces.ECPublicKey

/**
 * This class emulates an application server
 */
class ApplicationServer(val context: Context) {
    private val store = Store(context)

    fun sendNotification(callback: (error: String?) -> Unit) {
        if (store.webpush) {
            sendWebPushNotification(callback)
        } else {
            sendPlainTextNotification(callback)
        }
    }

    private fun sendPlainTextNotification(callback: (error: String?) -> Unit) {
        val requestQueue: RequestQueue = Volley.newRequestQueue(context)
        val url = Store(context).endpoint
        val stringRequest: StringRequest =
            object :
                StringRequest(
                    Method.POST,
                    url,
                    Response.Listener {
                        Toast.makeText(context, "Done", Toast.LENGTH_SHORT).show()
                        callback(null)
                    },
                    Response.ErrorListener { e ->
                        Toast.makeText(context, "An error occurred.", Toast.LENGTH_SHORT).show()
                        Log.e(TAG, "An error occurred while testing the endpoint:\n$e")
                        callback(e.toString())
                    },
                ) {
                override fun getParams(): MutableMap<String, String> {
                    val params = mutableMapOf<String, String>()
                    params["title"] = "Test"
                    params["message"] = "With UnifiedPush"
                    params["priority"] = "5"
                    return params
                }
            }
        requestQueue.add(stringRequest)
    }

    private fun sendWebPushNotification(callback: (error: String?) -> Unit) {
        val requestQueue: RequestQueue = Volley.newRequestQueue(context)
        val url = Store(context).endpoint
        val stringRequest: StringRequest =
            object :
                StringRequest(
                    Method.POST,
                    url,
                    Response.Listener {
                        Toast.makeText(context, "Done", Toast.LENGTH_SHORT).show()
                        callback(null)
                    },
                    Response.ErrorListener { e ->
                        Toast.makeText(context, "An error occurred.", Toast.LENGTH_SHORT).show()
                        Log.e(TAG, "An error occurred while testing the endpoint:\n$e")
                        callback(e.toString())
                    },
                ) {
                override fun getBody(): ByteArray {
                    val hybridEncrypt =
                        WebPushHybridEncrypt.Builder()
                            .withAuthSecret(store.authSecret)
                            .withRecipientPublicKey(store.keyPair.public as ECPublicKey)
                            .build()
                    return hybridEncrypt.encrypt("WebPush test".toByteArray(), null)
                }

                override fun getHeaders(): Map<String, String> {
                    val params: MutableMap<String, String> = HashMap()
                    params["Content-Encoding"] = "aes128gcm"
                    params["TTL"] = "0"
                    params["Urgency"] = "high"
                    return params
                }
            }
        requestQueue.add(stringRequest)
    }

    // The endpoint is "sent" to the application server
    fun storeEndpoint(endpoint: String?) {
        store.endpoint = endpoint
    }

    fun storeEndpoint(
        endpoint: String,
        _auth: String,
        _p256dh: String,
    ) {
        store.endpoint = endpoint
        // auth and p256dh are already store
        // if it was a real application server, they would have been registered by now
    }
}
