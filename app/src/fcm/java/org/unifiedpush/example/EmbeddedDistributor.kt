package org.unifiedpush.example

import org.unifiedpush.android.embedded_fcm_distributor.DefaultGateway
import org.unifiedpush.android.embedded_fcm_distributor.EmbeddedDistributorReceiver

class EmbeddedDistributor : EmbeddedDistributorReceiver() {
    override val gateway = DefaultGateway
}
