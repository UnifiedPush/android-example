package org.unifiedpush.example.activities.ui

import android.content.Context
import android.util.Log
import org.unifiedpush.example.ApplicationServer
import org.unifiedpush.example.Store
import org.unifiedpush.example.TestService
import org.unifiedpush.example.Urgency
import org.unifiedpush.example.utils.TAG
import org.unifiedpush.example.utils.genTestPageUrl
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
    val testPageUrl: String,
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
                testPageUrl = "Error",
                urgency = Urgency.NORMAL
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
            val p256dh = store.serializedPubKey ?: "Error"
            val auth = store.b64authSecret ?: "Error"
            val showVapid = vapidImplementedForSdk() && store.devMode && store.devUseVapid
            val vapidHeader = if (vapidImplementedForSdk()) {
                ApplicationServer(context).getVapidHeader(fakeKeys = (store.devMode && store.devWrongVapidKeysTest))
            } else {
                "Error"
            }
            val testPageUrl = genTestPageUrl(
                endpoint,
                p256dh,
                auth,
                vapidHeader,
                showVapid
            )
            Log.d(TAG, "testPageUrl: $testPageUrl")
            return CheckUiState(
                devMode = store.devMode,
                hasForegroundService = TestService.isStarted(),
                sendCleartext = store.devCleartextTest,
                endpoint = endpoint,
                auth = auth,
                p256dh = p256dh,
                showVapid = showVapid,
                vapid = vapidHeader,
                testPageUrl = testPageUrl,
                urgency = store.urgency
            )
        }
    }
}
