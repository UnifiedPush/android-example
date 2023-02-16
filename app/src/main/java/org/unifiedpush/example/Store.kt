package org.unifiedpush.example

import android.content.Context
import org.unifiedpush.example.utils.SerializedKeyPair
import org.unifiedpush.example.utils.WebPush
import java.security.KeyPair

private const val PREF_MASTER = "org.unifiedpush.example::store"
private const val PREF_ENDPOINT = "org.unifiedpush.example::store::endpoint"
private const val PREF_FEATURE_BYTE_MESSAGE = "org.unifiedpush.example::store::feature_byte_message"
private const val PREF_WEBPUSH = "org.unifiedpush.example::store::webpush"
private const val PREF_PUBKEY = "org.unifiedpush.example::store::pubkey"
private const val PREF_PRIVKEY = "org.unifiedpush.example::store::privkey"
private const val PREF_AUTHKEY = "org.unifiedpush.example::store::authkey"

class Store(context: Context) {

    private val prefs = context.getSharedPreferences(PREF_MASTER, Context.MODE_PRIVATE)

    var endpoint: String?
        get() = prefs.getString(PREF_ENDPOINT, null)
        set(value) {
            if (value == null) {
                prefs.edit().remove(PREF_ENDPOINT).apply()
            } else {
                prefs.edit().putString(PREF_ENDPOINT, value).apply()
            }
        }

    var featureByteMessage: Boolean
        get() = prefs.getBoolean(PREF_FEATURE_BYTE_MESSAGE, false)
        set(value) = prefs.edit().putBoolean(PREF_FEATURE_BYTE_MESSAGE, value).apply()

    var webpush: Boolean
        get() = prefs.getBoolean(PREF_WEBPUSH, false)
        set(value) = prefs.edit().putBoolean(PREF_WEBPUSH, value).apply()

    var keyPair: KeyPair
        get() {
            val publicKey = prefs.getString(PREF_PUBKEY, null)
            val privateKey = prefs.getString(PREF_PRIVKEY, null)
            return if (publicKey.isNullOrBlank() || privateKey.isNullOrBlank()) {
                val kp = WebPush.generateKeyPair()
                keyPair = kp
                kp
            } else {
                WebPush.decodeKeyPair(
                    SerializedKeyPair(
                        privateKey,
                        publicKey
                    )
                )
            }
        }
        set(value) {
            WebPush.encodeKeyPair(value).let {
                prefs.edit().putString(PREF_PUBKEY, it.public).apply()
                prefs.edit().putString(PREF_PRIVKEY, it.private).apply()
            }
        }

    val serializedPubKey: String?
        get() = prefs.getString(PREF_PUBKEY, null)?.let { WebPush.serializePublicKey(WebPush.decodePubKey(it)) }

    var authSecret: ByteArray
        get() = prefs.getString(PREF_AUTHKEY, null)?.let {
            WebPush.b64decode(it)
        }
            ?: WebPush.generateAuthSecret().apply {
                authSecret = this
            }
        set(value) = prefs.edit().putString(PREF_AUTHKEY, WebPush.b64encode(value)).apply()

    val b64authSecret: String?
        get() = prefs.getString(PREF_AUTHKEY, null)
}
