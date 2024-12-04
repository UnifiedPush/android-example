package org.unifiedpush.example.activities

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import kotlinx.coroutines.Job
import org.unifiedpush.android.connector.LinkActivityHelper
import org.unifiedpush.android.connector.UnifiedPush
import org.unifiedpush.example.ApplicationServer
import org.unifiedpush.example.TestService
import org.unifiedpush.example.Tests
import org.unifiedpush.example.activities.ui.CheckUi
import org.unifiedpush.example.activities.ui.theme.AppTheme
import org.unifiedpush.example.utils.RegistrationDialogs
import org.unifiedpush.example.utils.TAG
import org.unifiedpush.example.utils.vapidImplementedForSdk

class CheckActivity : ComponentActivity() {
    private lateinit var appBarViewModel: AppBarViewModel
    private lateinit var checkViewModel: CheckViewModel
    private val helper = LinkActivityHelper(this)
    private var job: Job? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        appBarViewModel = AppBarViewModel(this)
        checkViewModel = CheckViewModel(this)

        job = Events.registerForEvents { onEvent(it) }

        setContent {
            AppTheme {
                CheckUi(appBarViewModel, checkViewModel)
            }
        }
    }

    override fun onResume() {
        super.onResume()
        updateUi()
    }

    override fun onDestroy() {
        job?.cancel()
        job = null
        super.onDestroy()
    }

    override fun onPause() {
        super.onPause()
        Tests(this).testMessageInBackgroundRun()
    }

    /**
     * Receive link activity result.
     *
     * If the link succeed, we register our app.
     */
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        val success = helper.onLinkActivityResult(requestCode, resultCode, data)
        Log.d(TAG, "Distributor found=$success")
        if (success) {
            UnifiedPush.register(this)
        }
    }

    private fun onEvent(type: Events.Type) {
        runOnUiThread {
            when (type) {
                Events.Type.UpdateUi -> updateUi()
                Events.Type.Unregister -> unregister()
                Events.Type.SendNotification -> sendNotification()
                Events.Type.DeepLink -> deepLink()
                Events.Type.Reregister -> reRegister()
                Events.Type.StopForegroundService -> stopForegroundService()
                Events.Type.ChangeDistributor -> changeDistributor()
                Events.Type.TestTopic -> testTopic()
                Events.Type.UpdateVapidKey -> updateVapidKey()
                Events.Type.TestInBackground -> testInBackground()
                Events.Type.TestTTL -> testTTL()
                else -> {}
            }
        }
    }

    private fun updateUi() {
        if (!checkViewModel.refresh(this)) {
            MainActivity.goToMainActivity(this)
            finish()
        }
    }

    private fun unregister() {
        Toast.makeText(this, "Unregistering", Toast.LENGTH_SHORT).show()
        ApplicationServer(this).storeEndpoint(null)
        UnifiedPush.unregister(this)
        UnifiedPush.removeDistributor(this)
        updateUi()
    }

    private fun sendNotification() {
        ApplicationServer(this).sendNotification { checkViewModel.setError(it) }
    }

    private fun deepLink() {
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

    private fun reRegister() {
        RegistrationDialogs(this, mayUseCurrent = true, mayUseDefault = true).run()
        Toast.makeText(applicationContext, "Registration sent.", Toast.LENGTH_SHORT).show()
    }

    private fun stopForegroundService() {
        TestService.stop(this)
        updateUi()
    }

    private fun changeDistributor() {
        RegistrationDialogs(this, mayUseCurrent = false, mayUseDefault = false).run()
    }

    private fun testTopic() {
        Tests(this).testTopic { checkViewModel.setError(it) }
    }

    private fun updateVapidKey() {
        if (vapidImplementedForSdk()) {
            ApplicationServer(this).updateVapidKey()
            updateUi()
        }
    }

    private fun testInBackground() {
        Tests(this).testMessageInBackgroundStart()
    }

    private fun testTTL() {
        Tests(this).testTTL { checkViewModel.setError(it) }
    }

    companion object {
        fun goToCheckActivity(context: Context) {
            Log.d(TAG, "Go to CheckActivity")
            val intent =
                Intent(
                    context,
                    CheckActivity::class.java
                )
            context.startActivity(intent)
        }
    }
}
