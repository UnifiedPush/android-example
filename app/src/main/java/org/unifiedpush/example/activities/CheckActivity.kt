package org.unifiedpush.example.activities

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
import org.unifiedpush.example.TestService
import org.unifiedpush.example.activities.MainActivity.Companion.goToMainActivity
import org.unifiedpush.example.utils.RegistrationDialogs
import org.unifiedpush.example.utils.TAG
import org.unifiedpush.example.utils.registerOnRegistrationUpdate
import org.unifiedpush.example.utils.updateRegistrationInfo


class CheckActivity : WithOverlayActivity() {
    private var internalReceiver: BroadcastReceiver? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        setContentView(R.layout.activity_check)
        super.onCreate(savedInstanceState)

        findViewById<Button>(R.id.button_unregister).setOnClickListener { unregister() }
        findViewById<Button>(R.id.button_notify).setOnClickListener {
            ApplicationServer(this).sendNotification { error ->
                findViewById<TextView>(R.id.error_text).text = error ?: ""
            }
        }

        setDevButtons()
    }

    /**
     * Set buttons for developer mode.
     */
    private fun setDevButtons() {
        findViewById<Button>(R.id.button_reregister).setOnClickListener {
            reRegister()
        }
        findViewById<Button>(R.id.button_start_service).setOnClickListener {
            TestService.stop(this)
        }
        setDevButtonsVisibility()
    }

    /**
     * Set visibility of dev buttons.
     */
    private fun setDevButtonsVisibility() {
        val gone = !store.devMode
        val devButtons = listOf(
            R.id.button_reregister,
            R.id.button_start_service,
        )
        devButtons.forEach {
            findViewById<Button>(it).isGone = gone
        }
        findViewById<Button>(R.id.button_start_service).isEnabled = TestService.isStarted()
    }

    override fun onResume() {
        super.onResume()
        internalReceiver =
            registerOnRegistrationUpdate {
                setEndpointOrGoToMain()
                setDevButtonsVisibility()
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
        store.endpoint?.let {
            findViewById<TextView>(R.id.text_endpoint_value).apply {
                text = it.also {
                    Log.d(TAG, "endpoint $it")
                }
            }
            findViewById<TextView>(R.id.text_auth_value).apply {
                text = store.b64authSecret.also {
                    Log.d(TAG, "auth: $it")
                }
            }
            findViewById<TextView>(R.id.text_p256dh_value).apply {
                text = store.serializedPubKey.also {
                    Log.d(TAG, "p256dh: $it")
                }
            }
        } ?: run {
            goToMainActivity(this)
            finish()
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
        RegistrationDialogs(this, mayUseCurrent = true, mayUseDefault = true).run()
        Toast.makeText(applicationContext, "Registration sent.", Toast.LENGTH_SHORT).show()
    }

    companion object {
        fun goToCheckActivity(context: Context) {
            val intent =
                Intent(
                    context,
                    CheckActivity::class.java,
                )
            context.startActivity(intent)
        }
    }
}
