package org.unifiedpush.example.activities

import android.app.Activity
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.core.view.isGone
import org.unifiedpush.android.connector.UnifiedPush
import org.unifiedpush.example.ApplicationServer
import org.unifiedpush.example.R
import org.unifiedpush.example.Store
import org.unifiedpush.example.activities.MainActivity.Companion.goToMainActivity
import org.unifiedpush.example.utils.TAG
import org.unifiedpush.example.utils.WebPush
import org.unifiedpush.example.utils.registerOnRegistrationUpdate
import org.unifiedpush.example.utils.updateRegistrationInfo
import java.security.interfaces.ECPublicKey

class CheckActivity : Activity() {

    private var internalReceiver: BroadcastReceiver? = null
    private lateinit var store: Store

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_check)

        store = Store(this)

        findViewById<Button>(R.id.button_unregister).setOnClickListener { unregister() }
        findViewById<Button>(R.id.button_notify).setOnClickListener {
            ApplicationServer(this).sendNotification { error ->
                findViewById<TextView>(R.id.error_text).text = error ?: ""
            }
        }
        findViewById<Button>(R.id.button_reregister).setOnClickListener { reRegister() }

        (!store.webpush).let { webpush ->
            findViewById<TextView>(R.id.text_auth).isGone = webpush
            findViewById<TextView>(R.id.text_p256dh).isGone = webpush
            findViewById<TextView>(R.id.text_auth_value).apply {
                isGone = webpush
                text = WebPush.b64encode(store.authSecret)
            }
            findViewById<TextView>(R.id.text_p256dh_value).apply {
                isGone = webpush
                text = WebPush.serializePublicKey(store.keyPair.public as ECPublicKey)
            }
        }
        Log.d(TAG, "endpoint ${store.endpoint}")
        Log.d(TAG, "auth: ${WebPush.b64encode(store.authSecret)}")
        Log.d(TAG, "p256dh: ${WebPush.serializePublicKey(store.keyPair.public as ECPublicKey)}")
    }

    override fun onResume() {
        super.onResume()
        internalReceiver = registerOnRegistrationUpdate {
            setEndpointOrGoToMain()
        }
        setEndpointOrGoToMain()
    }

    override fun onPause() {
        super.onPause()
        internalReceiver?.let {
            unregisterReceiver(it)
        }
    }

    private fun setEndpointOrGoToMain() {
        if (store.endpoint == null) {
            goToMainActivity(this)
            finish()
        } else {
            findViewById<TextView>(R.id.text_endpoint_value).apply {
                text = store.endpoint ?: ""
            }
        }
    }

    private fun unregister() {
        Toast.makeText(this, "Unregistering", Toast.LENGTH_SHORT).show()
        ApplicationServer(this).storeEndpoint(null)
        UnifiedPush.unregisterApp(this)
        UnifiedPush.forceRemoveDistributor(this)
        updateRegistrationInfo()
    }

    private fun reRegister() {
        if (store.featureByteMessage) {
            UnifiedPush.registerAppWithDialog(
                this,
                features = arrayListOf(UnifiedPush.FEATURE_BYTES_MESSAGE)
            )
        } else {
            UnifiedPush.registerAppWithDialog(this)
        }
        Toast.makeText(applicationContext, "Registration sent.", Toast.LENGTH_SHORT).show()
    }

    companion object {
        fun goToCheckActivity(context: Context) {
            val intent = Intent(
                context,
                CheckActivity::class.java
            )
            context.startActivity(intent)
        }
    }
}
