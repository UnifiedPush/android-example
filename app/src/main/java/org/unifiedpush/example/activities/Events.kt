package org.unifiedpush.example.activities

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

object Events {
    fun registerForEvents(handler: (Type) -> Unit): Job {
        return CoroutineScope(Dispatchers.IO).launch {
            events.collectLatest { event ->
                handler(event)
            }
        }
    }

    suspend inline fun emitAsync(type: Type) {
        mutEvents.emit(type)
    }

    fun emit(type: Type) {
        CoroutineScope(Dispatchers.IO).launch {
            emitAsync(type)
        }
    }

    enum class Type {
        Register,
        UpdateUi,
        Unregister,
        SendNotification,
        DeepLink,
        Reregister,
        StopForegroundService,

        // SetUrgency,
        ChangeDistributor,
        TestTopic,
        UpdateVapidKey,
        TestInBackground,
        TestTTL
    }

    val mutEvents: MutableSharedFlow<Type> = MutableSharedFlow()
    private val events = mutEvents.asSharedFlow()
}
