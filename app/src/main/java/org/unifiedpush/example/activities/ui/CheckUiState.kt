package org.unifiedpush.example.activities.ui

import android.content.Context
import org.unifiedpush.example.Store
import org.unifiedpush.example.TestService
import org.unifiedpush.example.Urgency
import org.unifiedpush.example.utils.vapidImplementedForSdk

data class CheckUiState(
    val error: String? = null,
    val devMode: Boolean = false,
    val hasForegroundService: Boolean = false,
    val sendCleartext: Boolean = false,
    val endpoint: String,
    val auth: String,
    val p256dh: String,
    val showVapid: Boolean,
    val vapid: String,
    val urgency: Urgency
) {
    companion object {
        fun error(): CheckUiState {
            return CheckUiState(
                null,
                false,
                endpoint = "Error",
                auth = "Error",
                p256dh = "Error",
                showVapid = false,
                vapid = "Error",
                urgency = Urgency.NORMAL,
            )
        }

        /**
         * @return `null` if the application doesn't have any endpoint
         */
        fun from(context: Context): CheckUiState? {
            val store = Store(context)

            /**
             * Auth secret, p256dh and VAPID should never be used when null,
             * we set a dummy value.
             */
            val endpoint = store.endpoint ?: return null
                return CheckUiState(
                devMode = store.devMode,
                hasForegroundService = TestService.isStarted(),
                sendCleartext = store.devCleartextTest,
                endpoint = endpoint,
                auth = store.b64authSecret ?: "Error",
                p256dh = store.serializedPubKey ?: "Error",
                showVapid = vapidImplementedForSdk() && store.devMode && store.devUseVapid,
                vapid = store.vapidPubKey ?: "Error",
                urgency = store.urgency
            )
        }
    }
}