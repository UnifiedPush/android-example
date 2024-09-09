package org.unifiedpush.example.utils

import android.content.Context
import org.unifiedpush.android.connector.UnifiedPush
import org.unifiedpush.android.connector.ui.SelectDistributorDialogsBuilder
import org.unifiedpush.android.connector.ui.UnifiedPushFunctions

class RegistrationDialogs(context: Context, override var mayUseCurrent: Boolean, override var mayUseDefault: Boolean): SelectDistributorDialogsBuilder(
    context,
    object : UnifiedPushFunctions {
        override fun tryUseDefaultDistributor(callback: (Boolean) -> Unit) { UnifiedPush.tryUseDefaultDistributor(context, callback) }

        override fun getAckDistributor(): String? { return UnifiedPush.getAckDistributor(context) }

        override fun getDistributors(): List<String> { return UnifiedPush.getDistributors(context) }

        override fun registerApp(instance: String) { UnifiedPush.registerApp(context, instance) }

        override fun saveDistributor(distributor: String) { UnifiedPush.saveDistributor(context, distributor) }
    },
)
/* {
    /**
     * This is an example to ignore noDistributorFound
     */
    override fun onNoDistributorFound() {
        // DO NOTHING
    }
} */