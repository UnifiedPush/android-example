package org.unifiedpush.example

import android.content.Context

private const val PREF_MASTER = "org.unifiedpush.example::store"
private const val PREF_ENDPOINT = "org.unifiedpush.example::store::endpoint"
private const val PREF_FEATURE_BYTE_MESSAGE = "org.unifiedpush.example::store::feature_byte_message"

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
}
