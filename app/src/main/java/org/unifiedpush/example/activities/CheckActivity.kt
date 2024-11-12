package org.unifiedpush.example.activities

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.view.isGone
import org.unifiedpush.android.connector.LinkActivityHelper
import org.unifiedpush.android.connector.UnifiedPush
import org.unifiedpush.example.ApplicationServer
import org.unifiedpush.example.R
import org.unifiedpush.example.TestService
import org.unifiedpush.example.Tests
import org.unifiedpush.example.Urgency
import org.unifiedpush.example.activities.MainActivity.Companion.goToMainActivity
import org.unifiedpush.example.utils.RegistrationDialogs
import org.unifiedpush.example.utils.TAG
import org.unifiedpush.example.utils.registerOnRegistrationUpdate
import org.unifiedpush.example.utils.updateRegistrationInfo
import org.unifiedpush.example.utils.vapidImplementedForSdk

class CheckActivity : WithOverlayActivity() {
    private var internalReceiver: BroadcastReceiver? = null
    private val helper = LinkActivityHelper(this)

    override fun onCreate(savedInstanceState: Bundle?) {
        setContentView(R.layout.activity_check)
        super.onCreate(savedInstanceState)

        findViewById<Button>(R.id.button_unregister).setOnClickListener { unregister() }
        findViewById<Button>(R.id.button_notify).setOnClickListener {
            ApplicationServer(this).sendNotification { setError(it) }
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
        findViewById<Button>(R.id.button_test_deep_link).setOnClickListener {
            /**
             * We use the [LinkActivityHelper] with [onActivityResult], but we could
             * also use [UnifiedPush.tryUseDefaultDistributor] directly:
             *
             * ```
             * UnifiedPush.tryUseDefaultDistributor(this) { success ->
             *      Log.d(TAG, "Distributor found=$success")
             * }
             * ```
             */
            if (!helper.startLinkActivityForResult()) {
                Log.d(TAG, "No distributor found")
            }
        }
        findViewById<Button>(R.id.button_change_distrib).setOnClickListener {
            RegistrationDialogs(this, mayUseCurrent = false, mayUseDefault = false).run()
        }
        findViewById<Button>(R.id.button_set_urgency).setOnClickListener {
            chooseUrgencyDialog()
        }
        if (vapidImplementedForSdk()) {
            findViewById<Button>(R.id.button_update_vapid).setOnClickListener {
                ApplicationServer(this).updateVapidKey()
                refreshUiOrGoToMain()
            }
        }
        findViewById<Button>(R.id.button_test_ttl).setOnClickListener {
            Tests(this).testTTL { setError(it) }
        }
        findViewById<Button>(R.id.button_test_topic).setOnClickListener {
            Tests(this).testTopic { setError(it) }
        }
        findViewById<Button>(R.id.button_test_in_background).setOnClickListener {
            Tests(this).testMessageInBackgroundStart()
        }
        setDevButtonsVisibility()
    }

    private fun setError(error: String?) {
        Log.d(TAG, "Error: $error")
        findViewById<TextView>(R.id.error_text).apply {
            text = error?.let { "Error:\n$error" } ?: ""
            isGone = error == null
        }
    }

    /**
     * Set visibility of dev buttons.
     */
    private fun setDevButtonsVisibility() {
        val gone = !store.devMode
        val devButtons =
            listOf(
                R.id.button_reregister,
                R.id.button_start_service,
                R.id.button_test_deep_link,
                R.id.button_change_distrib,
                R.id.button_set_urgency,
                R.id.button_update_vapid,
                R.id.button_test_topic,
                R.id.button_test_ttl,
                R.id.button_test_in_background,
            )
        devButtons.forEach {
            findViewById<Button>(it).isGone = gone
        }
        findViewById<Button>(R.id.button_start_service).isEnabled = TestService.isStarted()
        findViewById<Button>(R.id.button_set_urgency).isEnabled = !store.devCleartextTest
        findViewById<Button>(R.id.button_test_topic).isEnabled = !store.devCleartextTest
        findViewById<Button>(R.id.button_test_ttl).isEnabled = !store.devCleartextTest
    }

    /**
     * Receive link activity result.
     *
     * If the link succeed, we register our app.
     */
    override fun onActivityResult(
        requestCode: Int,
        resultCode: Int,
        data: Intent?,
    ) {
        super.onActivityResult(requestCode, resultCode, data)
        val success = helper.onLinkActivityResult(requestCode, resultCode, data)
        Log.d(TAG, "Distributor found=$success")
        if (success) {
            UnifiedPush.registerApp(this)
        }
    }

    override fun onResume() {
        super.onResume()
        internalReceiver =
            registerOnRegistrationUpdate {
                refreshUiOrGoToMain()
            }
        refreshUiOrGoToMain()
    }

    override fun onPause() {
        super.onPause()
        internalReceiver?.let {
            unregisterReceiver(it)
        }
        internalReceiver = null
        Tests(this).testMessageInBackgroundRun { setError(it) }
    }

    private fun refreshUiOrGoToMain() {
        store.endpoint?.let {
            findViewById<TextView>(R.id.text_endpoint_value).apply {
                text =
                    it.also {
                        Log.d(TAG, "endpoint $it")
                    }
            }
            findViewById<TextView>(R.id.text_auth_value).apply {
                text =
                    store.b64authSecret.also {
                        Log.d(TAG, "auth: $it")
                    }
            }
            findViewById<TextView>(R.id.text_p256dh_value).apply {
                text =
                    store.serializedPubKey.also {
                        Log.d(TAG, "p256dh: $it")
                    }
            }
            setVapid()
            setDevButtons()
        } ?: run {
            goToMainActivity(this)
            finish()
        }
    }

    private fun setVapid() {
        if (!vapidImplementedForSdk()) {
            findViewById<TextView>(R.id.text_vapid_required_by_distrib).isGone = true
            findViewById<TextView>(R.id.text_vapid_value).isGone = true
            findViewById<Button>(R.id.button_update_vapid).isGone = true
        } else {
            val distUseVapid = store.distributorRequiresVapid
            val devUseVapid = store.devMode && store.devUseVapid
            findViewById<TextView>(R.id.text_vapid_required_by_distrib).apply {
                isGone = !distUseVapid && !devUseVapid
                text = if (devUseVapid) "VAPID:" else "VAPID, required by the distributor:"
            }
            findViewById<TextView>(R.id.text_vapid_value).apply {
                isGone = !distUseVapid && !devUseVapid
                text = ApplicationServer(context).getVapidHeader()
            }
        }
    }

    private fun chooseUrgencyDialog() {
        val builder: AlertDialog.Builder = AlertDialog.Builder(this)
        builder.setTitle("Choose urgency")

        val options = Urgency.entries.map { it.value }.toTypedArray()
        val checked = options.indexOf(store.urgency.value)

        builder.setSingleChoiceItems(options, checked) { _, which ->
            store.urgency = Urgency.fromValue(options[which])
        }

        val dialog: AlertDialog = builder.create()
        dialog.show()
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
