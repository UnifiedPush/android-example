package org.unifiedpush.example.activities

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import org.unifiedpush.example.Store
import org.unifiedpush.example.activities.ui.AppBarUiState

class AppBarViewModel(
    uiState: AppBarUiState,
    context: Context? = null
): ViewModel() {

    var uiState by mutableStateOf(uiState)
        private set

    private var store: Store? = context?.let { Store(context) }

    constructor(context: Context) : this(
        uiState = AppBarUiState.from(context),
        context = context
    )

    fun toggleDevMode() {
        uiState = uiState.copy(devMode = !uiState.devMode)
        store?.devMode = uiState.devMode
        Events.emit(Events.Type.UpdateUi)
    }

    fun toggleErrorIfDecryptionFail() {
        uiState = uiState.copy(errorIfDecryptionFail = !uiState.errorIfDecryptionFail)
        store?.devForceEncrypted = uiState.errorIfDecryptionFail
    }

    fun toggleUseVapid() {
        uiState = uiState.copy(useVapid = !uiState.useVapid)
        store?.devUseVapid = uiState.useVapid
        Events.emit(Events.Type.UpdateUi)
    }

    fun toggleSendClearTextTests() {
        uiState = uiState.copy(sendClearTextTests = !uiState.sendClearTextTests)
        store?.devCleartextTest = uiState.sendClearTextTests
        Events.emit(Events.Type.UpdateUi)
    }

    fun toggleUseWrongVapidKeys() {
        uiState = uiState.copy(useWrongVapidKeys = !uiState.useWrongVapidKeys)
        store?.devWrongVapidKeysTest = uiState.useWrongVapidKeys
    }

    fun toggleUseWrongEncryptionKeys() {
        uiState = uiState.copy(useWrongEncryptionKeys = !uiState.useWrongEncryptionKeys)
        store?.devWrongKeysTest = uiState.useWrongEncryptionKeys
        Events.emit(Events.Type.UpdateUi)
    }

    fun toggleStartForegroundServiceOnMessage() {
        uiState = uiState.copy(startForegroundServiceOnMessage = !uiState.startForegroundServiceOnMessage)
        store?.devStartForeground = uiState.startForegroundServiceOnMessage
    }
}