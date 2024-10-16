package org.unifiedpush.example

import android.content.Context
import android.os.Build
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Log
import android.widget.Toast
import androidx.annotation.RequiresApi
import com.android.volley.NetworkResponse
import com.android.volley.RequestQueue
import com.android.volley.Response
import com.android.volley.VolleyError
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.google.crypto.tink.apps.webpush.WebPushHybridEncrypt
import com.google.crypto.tink.subtle.EllipticCurves
import org.json.JSONObject
import org.unifiedpush.example.utils.RawRequest
import org.unifiedpush.example.utils.TAG
import org.unifiedpush.example.utils.b64decode
import org.unifiedpush.example.utils.b64encode
import org.unifiedpush.example.utils.decodePubKey
import org.unifiedpush.example.utils.encode
import org.unifiedpush.example.utils.vapidImplementedForSdk
import java.net.URL
import java.security.KeyPair
import java.security.KeyPairGenerator
import java.security.KeyStore
import java.security.KeyStore.PrivateKeyEntry
import java.security.SecureRandom
import java.security.Signature
import java.security.interfaces.ECPublicKey
import java.security.spec.ECGenParameterSpec

/**
 * This class emulates an application server
 */
class ApplicationServer(private val context: Context) {
    private val store = Store(context)

    /**
     * Emulate notification sent from the application server to the push service.
     *
     * If the request fail, [callback] runs with the error message.
     */
    fun sendNotification(callback: (error: String?) -> Unit) {
        if (store.devMode && store.devCleartextTest) {
            sendPlainTextNotification { e ->
                callbackWithToasts(e, callback)
            }
        } else if (store.devMode && store.devWrongKeysTest) {
            sendWebPushNotification(content = "This is impossible to decrypt", fakeKeys = true) { _, e ->
                callbackWithToasts(e, callback)
            }
        } else {
            sendWebPushNotification(content = "WebPush test", fakeKeys = false) { _, e ->
                callbackWithToasts(e, callback)
            }
        }
    }

    private fun callbackWithToasts(e: VolleyError?, callback: (error: String?) -> Unit) {
        e?.let {
            Toast.makeText(context, "An error occurred.", Toast.LENGTH_SHORT).show()
            callback(it.toString())
        } ?: run {
            Toast.makeText(context, "Done", Toast.LENGTH_SHORT).show()
            callback(null)
        }
    }

    /**
     * Emulate notification sent from the application server to the push service with a TTL.
     *
     * If the request fail, or the response doesn't contain TTL=5 [callback] runs with the error message.
     */
    fun sendTestTTLNotification(callback: (error: String?) -> Unit) {
        sendWebPushNotification(content = "This must be deleted before being delivered.", fakeKeys = false) { r, e ->
            e?.let { return@sendWebPushNotification callback(e.toString()) }
            r?.let { rep ->
                var ttl: String?
                if (rep.headers?.keys?.contains("TTL") != true) {
                    return@sendWebPushNotification callback("The response doesn't contain TTL header.")
                } else if (rep.headers?.get("TTL").also { ttl = it } != "5") {
                    return@sendWebPushNotification callback("The response doesn't support TTL for 5 seconds (TTL=$ttl).")
                } else {
                    return@sendWebPushNotification callback(null)
                }
            }
        }
    }

    /**
     * Emulate 2 notifications sent from the application server to the push service with the same topic.
     *
     * If the distributor is not connected, the first push message should be override.
     *
     * If the request fail [callback] runs with the error message.
     */
    fun sendTestTopicNotifications(callback: (error: String?) -> Unit) {
        sendWebPushNotification(content = "1st notification, it must be replaced before being delivered.", fakeKeys = false, topic = "test", ttl = 60) { _, e1 ->
            e1?.let { return@sendWebPushNotification callbackWithToasts(e1, callback) }
                ?: run {
                    sendWebPushNotification(
                        content = "2nd notification, it must have replaced the previous one.",
                        fakeKeys = false,
                        topic = "test",
                        ttl = 60
                    ) { _, e2 ->
                        callbackWithToasts(e2, callback)
                    }
                }
        }
    }

    /**
     * @hide
     * Send plain text notifications.
     *
     * Will be used in dev mode.
     */
    private fun sendPlainTextNotification(callback: (error: VolleyError?) -> Unit) {
        val requestQueue: RequestQueue = Volley.newRequestQueue(context)
        val url = Store(context).endpoint
        val stringRequest: StringRequest =
            object :
                StringRequest(
                    Method.POST,
                    url,
                    Response.Listener {
                        callback(null)
                    },
                    Response.ErrorListener(callback),
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
    private fun sendWebPushNotification(content: String, fakeKeys: Boolean, topic: String? = null, ttl: Int = 5, callback: (response: NetworkResponse?, error: VolleyError?) -> Unit) {
        val requestQueue: RequestQueue = Volley.newRequestQueue(context)
        val url = Store(context).endpoint
        val request = object :
            RawRequest(
                Method.POST,
                url,
                Response.Listener { r ->
                    callback(r, null)
                },
                Response.ErrorListener { e ->
                    callback(null, e)
                },
            ) {
            override fun getBody(): ByteArray {
                val auth = if (fakeKeys) { genAuth() } else { store.b64authSecret?.b64decode() }
                val hybridEncrypt =
                    WebPushHybridEncrypt.Builder()
                        .withAuthSecret(auth)
                        .withRecipientPublicKey(store.serializedPubKey?.decodePubKey() as ECPublicKey)
                        .build()
                return hybridEncrypt.encrypt(content.toByteArray(), null)
            }

            override fun getHeaders(): Map<String, String> {
                val params: MutableMap<String, String> = HashMap()
                params["Content-Encoding"] = "aes128gcm"
                params["TTL"] = "$ttl"
                params["Urgency"] = store.urgency.value
                topic?.let {
                    params["Topic"] = it
                }
                if (vapidImplementedForSdk() &&
                    ((store.devMode && store.devUseVapid) ||
                            store.distributorRequiresVapid)) {
                    params["Authorization"] = getVapidHeader(fakeKeys = (store.devMode && store.devWrongVapidKeysTest))
                }
                return params
            }
        }
        requestQueue.add(request)
    }

    private fun genAuth(): ByteArray {
        return ByteArray(16).apply {
            SecureRandom().nextBytes(this)
        }
    }

    /**
     * Emulate saving the endpoint on the application server.
     */
    fun storeEndpoint(endpoint: String?) {
        store.endpoint = endpoint
    }

    /**
     * Emulate saving the web push public keys on the application server.
     */
    fun storeWebPushKeys(
        auth: String,
        p256dh: String,
    ) {
        store.b64authSecret = auth
        store.serializedPubKey = p256dh
    }

    /**
     * Generate VAPID header for the endpoint, valid for 12h
     *
     * This is for the `Authorization` header.
     *
     * @return [String] "vapid t=$JWT,k=$PUBKEY"
     */
    @RequiresApi(Build.VERSION_CODES.M)
    fun getVapidHeader(fakeKeys: Boolean = false): String {
        val endpointStr = store.endpoint ?: return ""
        val header = JSONObject()
            .put("alg", "ES256")
            .put("typ", "JWT")
            .toString().toByteArray(Charsets.UTF_8)
            .b64encode()
        val endpoint = URL(endpointStr)
        val time12h = ((System.currentTimeMillis() / 1000) + 43200).toString() // +12h

        /**
         * [org.json.JSONStringer#string] Doesn't follow RFC, '/' = 0x2F doesn't have to be escaped
         */
        val body = JSONObject()
            .put("aud", "${endpoint.protocol}://${endpoint.authority}")
            .put("exp", time12h)
            .toString()
            .replace("\\/", "/")
            .toByteArray(Charsets.UTF_8)
            .b64encode()
        val toSign = "$header.$body".toByteArray(Charsets.UTF_8)
        val signature = (if (fakeKeys) signWithTempKey(toSign)
            else sign(toSign))?.b64encode() ?: ""
        val jwt = "$header.$body.$signature"
        return "vapid t=$jwt,k=${store.vapidPubKey}"
    }

    /**
     * Generate a new KeyPair for VAPID on the fake server side
     */
    @RequiresApi(Build.VERSION_CODES.M)
    fun genVapidKey(): KeyPair {
        Log.d(TAG, "Generating a new KP.")
        val generator =
            KeyPairGenerator.getInstance(KeyProperties.KEY_ALGORITHM_EC, KEYSTORE_PROVIDER)
        generator.initialize(
            KeyGenParameterSpec.Builder(ALIAS, KeyProperties.PURPOSE_SIGN)
                .setAlgorithmParameterSpec(ECGenParameterSpec("secp256r1"))
                .setDigests(KeyProperties.DIGEST_SHA256)
                .setUserAuthenticationRequired(false)
                .build()
        )
        return generator.generateKeyPair().also {
            val pubkey = (it.public as ECPublicKey).encode()
            Log.d(TAG, "Pubkey: $pubkey")
            store.vapidPubKey = pubkey
        }
    }

    /**
     * Sign [data] using the generated VAPID key pair
     */
    @RequiresApi(Build.VERSION_CODES.M)
    private fun sign(data: ByteArray): ByteArray? {
        val ks = KeyStore.getInstance(KEYSTORE_PROVIDER).apply {
            load(null)
        }
        if (!ks.containsAlias(ALIAS) || !ks.entryInstanceOf(ALIAS, PrivateKeyEntry::class.java)) {
            // This should never be called. When we sign something, the key are already created.
            genVapidKey()
        }/* else {
            ks.deleteEntry(ALIAS)
            genKeyPair()
        }*/
        val entry: KeyStore.Entry = ks.getEntry(ALIAS, null)
        if (entry !is PrivateKeyEntry) {
            Log.w(TAG, "Not an instance of a PrivateKeyEntry")
            return null
        }
        return Signature.getInstance("SHA256withECDSA").run {
            initSign(entry.privateKey)
            update(data)
            sign()
        }
    }

    private fun signWithTempKey(data: ByteArray): ByteArray? {
        val keyPair: KeyPair =
            EllipticCurves.generateKeyPair(EllipticCurves.CurveType.NIST_P256)
        return Signature.getInstance("SHA256withECDSA").run {
            initSign(keyPair.private)
            update(data)
            sign()
        }
    }

    private companion object {
        private const val KEYSTORE_PROVIDER = "AndroidKeyStore"
        private const val ALIAS = "ApplicationServer"
    }
}
