package org.unifiedpush.example.activities

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import kotlinx.coroutines.Job
import org.unifiedpush.example.Store
import org.unifiedpush.example.activities.CheckActivity.Companion.goToCheckActivity
import org.unifiedpush.example.activities.ui.MainUi
import org.unifiedpush.example.activities.ui.theme.AppTheme
import org.unifiedpush.example.utils.RegistrationDialogs
import org.unifiedpush.example.utils.TAG

class MainActivity : ComponentActivity() {
    private lateinit var appBarViewModel: AppBarViewModel
    private var job : Job? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        appBarViewModel = AppBarViewModel(this)

        job = Events.registerForEvents { onEvent(it) }

        setContent {
            AppTheme {
                MainUi(appBarViewModel)
            }
        }
    }

    override fun onResume() {
        super.onResume()
        val store = Store(this)
        if (store.endpoint != null) {
            goToCheckActivity(this)
            finish()
        } else {
            // We reset the value
            store.distributorRequiresVapid = false
        }
    }

    override fun onDestroy() {
        job?.cancel()
        job = null
        super.onDestroy()
    }

    private fun onEvent(type: Events.Type) {
        when (type) {
            Events.Type.Register -> {
                runOnUiThread {
                    RegistrationDialogs(this, mayUseCurrent = true, mayUseDefault = true).run()
                }
            }
            Events.Type.UpdateUi -> {
                goToCheckActivity(this)
                finish()
            }
            else -> {}
        }
    }

    companion object {
        fun goToMainActivity(context: Context) {
            Log.d(TAG, "Go to MainActivity")
            val intent =
                Intent(
                    context,
                    MainActivity::class.java,
                )
            context.startActivity(intent)
        }
    }
}