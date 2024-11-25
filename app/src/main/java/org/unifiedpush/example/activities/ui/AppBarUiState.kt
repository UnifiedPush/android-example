package org.unifiedpush.example.activities.ui

import android.content.Context
import org.unifiedpush.example.Store

data class AppBarUiState(
    val devMode: Boolean,
    val errorIfDecryptionFail: Boolean,
    val useVapid: Boolean,
    val sendClearTextTests: Boolean,
    val useWrongVapidKeys: Boolean,
    val useWrongEncryptionKeys: Boolean,
    val startForegroundServiceOnMessage: Boolean
) {
   companion object {
       fun from(context: Context): AppBarUiState {
           val store = Store(context)
           return AppBarUiState(
               devMode = store.devMode,
               errorIfDecryptionFail = store.devForceEncrypted,
               useVapid = store.devUseVapid,
               sendClearTextTests = store.devCleartextTest,
               useWrongVapidKeys = store.devWrongVapidKeysTest,
               useWrongEncryptionKeys = store.devWrongKeysTest,
               startForegroundServiceOnMessage = store.devStartForeground
           )
       }
   }
}