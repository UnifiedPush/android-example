package org.unifiedpush.example.activities

import android.app.Activity
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import org.unifiedpush.android.connector.UnifiedPush
import org.unifiedpush.example.R
import org.unifiedpush.example.Requests.sendNotification
import org.unifiedpush.example.Store
import org.unifiedpush.example.activities.MainActivity.Companion.goToMainActivity
import org.unifiedpush.example.utils.registerOnRegistrationUpdate
import org.unifiedpush.example.utils.updateRegistrationInfo

class CheckActivity : Activity() {

    private var internalReceiver: BroadcastReceiver? = null
    private lateinit var store: Store

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_check)

        store = Store(this)

        internalReceiver = registerOnRegistrationUpdate {
            setEndpointOrGoToMain()
        }
        setEndpointOrGoToMain()
        findViewById<Button>(R.id.button_unregister).setOnClickListener { unregister() }
        findViewById<Button>(R.id.button_notify).setOnClickListener {
            sendNotification(this) { error ->
                findViewById<TextView>(R.id.error_text).text = error ?: ""
            }
        }
        findViewById<Button>(R.id.button_reregister).setOnClickListener { reRegister() }
    }

    override fun onDestroy() {
        internalReceiver?.let {
            unregisterReceiver(it)
        }
        super.onDestroy()
    }

    private fun setEndpointOrGoToMain() {
        if (store.endpoint == null) {
            internalReceiver?.let {
                unregisterReceiver(it)
            }
            goToMainActivity(this)
        } else {
            findViewById<TextView>(R.id.text_gateway_value).apply {
                text = store.endpoint ?: ""
            }
        }
    }

    private fun unregister() {
        Toast.makeText(this, "Unregistering", Toast.LENGTH_SHORT).show()
        store.endpoint = null
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
