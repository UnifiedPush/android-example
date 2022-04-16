package org.unifiedpush.example

import android.content.Context
import org.unifiedpush.android.embedded_fcm_distributor.EmbeddedDistributorReceiver

class EmbeddedDistributor: EmbeddedDistributorReceiver() {

    //toDevFossFcm//override val googleProjectNumber = "64518491375"

    override fun getEndpoint(context: Context, fcmToken: String, instance: String): String {
        return "https://fcm.example.unifiedpush.org/FCM?v2&instance=$instance&token=$fcmToken"
    }
}
