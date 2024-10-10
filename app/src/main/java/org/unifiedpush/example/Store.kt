package org.unifiedpush.example

import android.content.Context
import org.unifiedpush.android.connector.UnifiedPush

private const val PREF_MASTER = "org.unifiedpush.example::store"
private const val PREF_DEV_MODE = "org.unifiedpush.example::store::devMode"
private const val PREF_DEV_FOREGROUND_SERVICE = "org.unifiedpush.example::store::dev::foregroundService"
private const val PREF_DEV_CLEARTEXT_TEST = "org.unifiedpush.example::store::dev::cleartextTest"
private const val PREF_DEV_FORCE_ENCRYPTED = "org.unifiedpush.example::store::dev::forceEncrypted"
private const val PREF_ENDPOINT = "org.unifiedpush.example::store::endpoint"
private const val PREF_PUBKEY = "org.unifiedpush.example::store::pubkey"
private const val PREF_AUTHKEY = "org.unifiedpush.example::store::authkey"

class Store(val context: Context) {
    private val prefs = context.getSharedPreferences(PREF_MASTER, Context.MODE_PRIVATE)


    /**
     * Is Developer mode enabled.
     */
    var devMode: Boolean
        get() = prefs.getBoolean(PREF_DEV_MODE, false)
        set(value) = prefs.edit().putBoolean(PREF_DEV_MODE, value).apply()

    var devStartForeground: Boolean
        get() = prefs.getBoolean(PREF_DEV_FOREGROUND_SERVICE, false)
        set(value) = prefs.edit().putBoolean(PREF_DEV_FOREGROUND_SERVICE, value).apply()

    var devCleartextTest: Boolean
        get() = prefs.getBoolean(PREF_DEV_CLEARTEXT_TEST, false)
        set(value) = prefs.edit().putBoolean(PREF_DEV_CLEARTEXT_TEST, value).apply()

    var devForceEncrypted: Boolean
        get() = prefs.getBoolean(PREF_DEV_FORCE_ENCRYPTED, false)
        set(value) = prefs.edit().putBoolean(PREF_DEV_FORCE_ENCRYPTED, value).apply()


    var endpoint: String?
        get() = UnifiedPush.getAckDistributor(context)?.let { prefs.getString(PREF_ENDPOINT, null) }
        set(value) {
            if (value == null) {
                prefs.edit().remove(PREF_ENDPOINT).apply()
            } else {
                prefs.edit().putString(PREF_ENDPOINT, value).apply()
            }
        }

    /**
     * Store the pubkey for the registration on the "server"
     */
    var serializedPubKey: String?
        get() = prefs.getString(PREF_PUBKEY, null)
        set(value) = prefs.edit().putString(PREF_PUBKEY, value).apply()

    /**
     * Store the auth secret for the registration on the "server"
     */
    var b64authSecret: String?
        get() = prefs.getString(PREF_AUTHKEY, null)
        set(value) = prefs.edit().putString(PREF_AUTHKEY, value).apply()
}
