package org.unifiedpush.example.activities

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import org.unifiedpush.example.Store
import org.unifiedpush.example.Urgency
import org.unifiedpush.example.activities.ui.CheckUiState

class CheckViewModel(
    uiState: CheckUiState,
    context: Context? = null
) : ViewModel() {

    var uiState by mutableStateOf(uiState)
        private set

    private var store: Store? = context?.let { Store(context) }

    constructor(context: Context) : this(
        CheckUiState.from(context) ?: CheckUiState.error(),
        context
    )

    /**
     * @return `true` if the app is still connected to the distributor
     */
    fun refresh(context: Context): Boolean {
        return CheckUiState.from(context)?.let {
            uiState = it
        } != null
    }

    fun setError(error: String?) {
        uiState = uiState.copy(error = error)
    }

    fun setUrgency(urgency: Urgency) {
        uiState = uiState.copy(urgency = urgency)
        store?.urgency = urgency
    }
}
