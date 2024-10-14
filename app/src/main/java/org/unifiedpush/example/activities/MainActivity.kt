package org.unifiedpush.example.activities

import android.Manifest
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.widget.Button
import androidx.activity.result.contract.ActivityResultContracts
import org.unifiedpush.example.R
import org.unifiedpush.example.activities.CheckActivity.Companion.goToCheckActivity
import org.unifiedpush.example.utils.RegistrationDialogs
import org.unifiedpush.example.utils.registerOnRegistrationUpdate

class MainActivity : WithOverlayActivity() {
    private var internalReceiver: BroadcastReceiver? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        setContentView(R.layout.activity_main)
        super.onCreate(savedInstanceState)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            registerForActivityResult(
                ActivityResultContracts.RequestPermission(),
            ) { // granted ->
            }.launch(
                Manifest.permission.POST_NOTIFICATIONS,
            )
        }

        findViewById<Button>(R.id.register_button).setOnClickListener {
            RegistrationDialogs(this, mayUseCurrent = true, mayUseDefault = true).run()
        }
    }

    override fun onResume() {
        super.onResume()
        if (store.endpoint != null) {
            goToCheckActivity(this)
            finish()
        } else {
            // We reset the value
            store.distributorRequiresVapid = false
            internalReceiver =
                registerOnRegistrationUpdate {
                    if (store.endpoint != null) {
                        goToCheckActivity(this)
                        finish()
                    }
                }
        }
    }

    override fun onPause() {
        super.onPause()
        internalReceiver?.let {
            unregisterReceiver(it)
        }
    }

    companion object {
        fun goToMainActivity(context: Context) {
            val intent =
                Intent(
                    context,
                    MainActivity::class.java,
                )
            context.startActivity(intent)
        }
    }
}
