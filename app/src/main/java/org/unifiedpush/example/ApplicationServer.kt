package org.unifiedpush.example

import android.content.Context
import android.os.Build
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import android.util.Log
import android.widget.Toast
import androidx.annotation.RequiresApi
import com.android.volley.RequestQueue
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.google.crypto.tink.apps.webpush.WebPushHybridEncrypt
import com.google.crypto.tink.subtle.EllipticCurves
import org.json.JSONObject
import org.unifiedpush.example.utils.TAG
import java.net.URL
import java.security.KeyFactory
import java.security.KeyPair
import java.security.KeyPairGenerator
import java.security.KeyStore
import java.security.KeyStore.PrivateKeyEntry
import java.security.SecureRandom
import java.security.Signature
import java.security.interfaces.ECPublicKey
import java.security.spec.ECGenParameterSpec
import java.security.spec.ECPublicKeySpec

/**
 * This class emulates an application server
 */
class ApplicationServer(val context: Context) {
    private val store = Store(context)

    fun sendNotification(callback: (error: String?) -> Unit) {
        if (store.devMode && store.devCleartextTest) {
            sendPlainTextNotification(callback)
        } else if (store.devMode && store.devWrongKeysTest) {
            sendWebPushNotification(fakeKeys = true, callback)
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
        sendWebPushNotification(fakeKeys = false, callback)
    }

    private fun sendWebPushNotification(fakeKeys: Boolean, callback: (error: String?) -> Unit) {
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
                    val auth = if (fakeKeys) { genAuth() } else { store.b64authSecret?.b64decode() }
                    val hybridEncrypt =
                        WebPushHybridEncrypt.Builder()
                            .withAuthSecret(auth)
                            .withRecipientPublicKey(store.serializedPubKey?.decodePubKey() as ECPublicKey)
                            .build()
                    return hybridEncrypt.encrypt("WebPush test".toByteArray(), null)
                }

                override fun getHeaders(): Map<String, String> {
                    val params: MutableMap<String, String> = HashMap()
                    params["Content-Encoding"] = "aes128gcm"
                    params["TTL"] = "0"
                    params["Urgency"] = "high"
                    if (vapidImplementedForSdk() &&
                        ((store.devMode && store.devUseVapid) ||
                                        store.distributorRequiresVapid)) {
                        params["Authorization"] = getVapidHeader()
                    }
                    return params
                }
            }
        requestQueue.add(stringRequest)
    }

    private fun genAuth(): ByteArray {
        return ByteArray(16).apply {
            SecureRandom().nextBytes(this)
        }
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

    /**
     * Generate VAPID header for the endpoint, valid for 12h
     *
     * This is for the `Authorization` header.
     *
     * @return [String] "vapid t=$JWT,k=$PUBKEY"
     */
    @RequiresApi(Build.VERSION_CODES.M)
    fun getVapidHeader(): String {
        val header = JSONObject()
            .put("alg", "ES256")
            .put("typ", "JWT")
            .toString().toByteArray(Charsets.UTF_8)
            .b64encode()
        val endpoint = URL(store.endpoint)
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
        val signature = sign("$header.$body".toByteArray(Charsets.UTF_8))?.b64encode() ?: ""
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
            // This should be called. When we sign something, the key are already created.
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

    companion object {
        private const val KEYSTORE_PROVIDER = "AndroidKeyStore"
        private const val ALIAS = "ApplicationServer"
    }
}
