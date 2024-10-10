package org.unifiedpush.example

import android.content.Context
import android.util.Base64
import android.util.Log
import android.widget.Toast
import com.android.volley.RequestQueue
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.google.crypto.tink.apps.webpush.WebPushHybridEncrypt
import com.google.crypto.tink.subtle.EllipticCurves
import org.unifiedpush.example.utils.TAG
import java.security.KeyFactory
import java.security.interfaces.ECPublicKey
import java.security.spec.ECPublicKeySpec

/**
 * This class emulates an application server
 */
class ApplicationServer(val context: Context) {
    private val store = Store(context)

    fun sendNotification(callback: (error: String?) -> Unit) {
        if (store.devMode && store.devCleartextTest) {
            sendPlainTextNotification(callback)
        } else {
            sendWebPushNotification(callback)
        }
    }

    /**
     * @hide
     * Send plain text notifications.
     *
     * Will be used in dev mode.
     */
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
                    params["message"] = "Send in cleartext."
                    params["priority"] = "5"
                    return params
                }
            }
        requestQueue.add(stringRequest)
    }

    /**
     * Send a notification encrypted with RFC8291
     */
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
                            .withAuthSecret(store.b64authSecret?.b64decode())
                            .withRecipientPublicKey(store.serializedPubKey?.decodePubKey() as ECPublicKey)
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

    fun storeWebPushKeys(
        auth: String,
        p256dh: String,
    ) {
        store.b64authSecret = auth
        store.serializedPubKey = p256dh
    }
}

fun String.decodePubKey(): ECPublicKey {
    val point = EllipticCurves.pointDecode(
        EllipticCurves.CurveType.NIST_P256,
        EllipticCurves.PointFormatType.UNCOMPRESSED, this.b64decode())
    val spec = EllipticCurves.getCurveSpec(EllipticCurves.CurveType.NIST_P256)
    return KeyFactory.getInstance("EC").generatePublic(ECPublicKeySpec(point, spec)) as ECPublicKey
}

fun String.b64decode(): ByteArray {
    return Base64.decode(
        this,
        Base64.URL_SAFE or Base64.NO_WRAP or Base64.NO_PADDING,
    )
}