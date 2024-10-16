package org.unifiedpush.example

import android.content.Context
import org.unifiedpush.android.connector.UnifiedPush

private const val PREF_MASTER = "org.unifiedpush.example::store"
private const val PREF_DEV_MODE = "org.unifiedpush.example::store::devMode"
private const val PREF_DEV_FOREGROUND_SERVICE = "org.unifiedpush.example::store::dev::foregroundService"
private const val PREF_DEV_CLEARTEXT_TEST = "org.unifiedpush.example::store::dev::cleartextTest"
private const val PREF_DEV_WRONG_KEYS_TEST = "org.unifiedpush.example::store::dev::wrongKeysTest"
private const val PREF_DEV_WRONG_VAPID_KEYS = "org.unifiedpush.example::store::dev::wrongVapidKeysTest"
private const val PREF_DEV_FORCE_ENCRYPTED = "org.unifiedpush.example::store::dev::forceEncrypted"
private const val PREF_DEV_USE_VAPID = "org.unifiedpush.example::store::dev::useVapid"
private const val PREF_ENDPOINT = "org.unifiedpush.example::store::endpoint"
private const val PREF_URGENCY = "org.unifiedpush.example::store::urgency"
private const val PREF_PUBKEY = "org.unifiedpush.example::store::pubkey"
private const val PREF_AUTHKEY = "org.unifiedpush.example::store::authkey"
private const val PREF_DISTRIB_REQ_VAPID = "org.unifiedpush.example::store::distribRequiresVapid"
private const val PREF_VAPID_PUBKEY = "org.unifiedpush.example::store::vapidPubKey"

/**
 * Class containing stored parameters and values.
 */
class Store(val context: Context) {
    private val prefs = context.getSharedPreferences(PREF_MASTER, Context.MODE_PRIVATE)


    /** Is Developer mode enabled. */
    var devMode: Boolean
        get() = prefs.getBoolean(PREF_DEV_MODE, false)
        set(value) = prefs.edit().putBoolean(PREF_DEV_MODE, value).apply()

    /** For developer mode: Start foreground service when a push message is received. */
    var devStartForeground: Boolean
        get() = prefs.getBoolean(PREF_DEV_FOREGROUND_SERVICE, false)
        set(value) = prefs.edit().putBoolean(PREF_DEV_FOREGROUND_SERVICE, value).apply()

    /** For developer mode: Send unencrypted push messages. */
    var devCleartextTest: Boolean
        get() = prefs.getBoolean(PREF_DEV_CLEARTEXT_TEST, false)
        set(value) = prefs.edit().putBoolean(PREF_DEV_CLEARTEXT_TEST, value).apply()

    /** For developer mode: Use wrong encryption key to send push messages. */
    var devWrongKeysTest: Boolean
        get() = prefs.getBoolean(PREF_DEV_WRONG_KEYS_TEST, false)
        set(value) = prefs.edit().putBoolean(PREF_DEV_WRONG_KEYS_TEST, value).apply()

    /** For developer mode: Use wrong VAPID key with push messages.*/
    var devWrongVapidKeysTest: Boolean
        get() = prefs.getBoolean(PREF_DEV_WRONG_VAPID_KEYS, false)
        set(value) = prefs.edit().putBoolean(PREF_DEV_WRONG_VAPID_KEYS, value).apply()

    /**
     * For developer mode: Show an error notification when receiving an unencrypted push message.
     *
     * When [PushMessage.decrypted][org.unifiedpush.android.connector.data.PushMessage.decrypted]
     * is false.
     */
    var devForceEncrypted: Boolean
        get() = prefs.getBoolean(PREF_DEV_FORCE_ENCRYPTED, false)
        set(value) = prefs.edit().putBoolean(PREF_DEV_FORCE_ENCRYPTED, value).apply()

    /** For developer mode: Use VAPID even if the distributor doesn't require it. */
    var devUseVapid: Boolean
        get() = prefs.getBoolean(PREF_DEV_USE_VAPID, false)
        set(value) = prefs.edit().putBoolean(PREF_DEV_USE_VAPID, value).apply()

    /** Push endpoint. Should be saved on application server. */
    var endpoint: String?
        get() = UnifiedPush.getAckDistributor(context)?.let { prefs.getString(PREF_ENDPOINT, null) }
        set(value) {
            if (value == null) {
                prefs.edit().remove(PREF_ENDPOINT).apply()
            } else {
                prefs.edit().putString(PREF_ENDPOINT, value).apply()
            }
        }

    /** For developer mode: Urgency for push messages. Should be chosen by the application server.  */
    var urgency: Urgency
        get() = Urgency.fromValue(prefs.getString(PREF_URGENCY, null))
        set(value) = prefs.edit().putString(PREF_URGENCY, value.value).apply()

    /** WebPush public key. Should be saved on application server. */
    var serializedPubKey: String?
        get() = prefs.getString(PREF_PUBKEY, null)
        set(value) = prefs.edit().putString(PREF_PUBKEY, value).apply()

    /** Does the distributor requires VAPID ? It should always be used if the application server supports it. */
    var distributorRequiresVapid: Boolean
        get() = prefs.getBoolean(PREF_DISTRIB_REQ_VAPID, false)
        set(value) = prefs.edit().putBoolean(PREF_DISTRIB_REQ_VAPID, value).apply()

    /** VAPID public key. Should be saved on application server. */
    var vapidPubKey: String?
        get() = prefs.getString(PREF_VAPID_PUBKEY, null)
        set(value) = prefs.edit().putString(PREF_VAPID_PUBKEY, value).apply()

    /** WebPush auth secret. Should be saved on application server. */
    var b64authSecret: String?
        get() = prefs.getString(PREF_AUTHKEY, null)
        set(value) = prefs.edit().putString(PREF_AUTHKEY, value).apply()
}
