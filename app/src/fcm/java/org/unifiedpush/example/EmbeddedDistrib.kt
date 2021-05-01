package org.unifiedpush.example

import android.content.Context
import org.unifiedpush.android.embedded_fcm_distributor.EmbeddedDistributorReceiver
import org.unifiedpush.android.embedded_fcm_distributor.GetEndpointHandler

val handlerFCM = object: GetEndpointHandler {
    override fun getEndpoint(context: Context?, token: String, instance: String): String {
        return "https://fcm.example.unifiedpush.org/FCM?instance=$instance&token=$token"
    }
}

class EmbeddedDistrib: EmbeddedDistributorReceiver(handlerFCM) {}
