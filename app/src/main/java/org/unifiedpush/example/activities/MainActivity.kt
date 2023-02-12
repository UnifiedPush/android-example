package org.unifiedpush.example.activities

import android.Manifest
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.widget.Button
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import org.unifiedpush.android.connector.UnifiedPush.FEATURE_BYTES_MESSAGE
import org.unifiedpush.android.connector.UnifiedPush.registerAppWithDialog
import org.unifiedpush.example.R
import org.unifiedpush.example.Store
import org.unifiedpush.example.activities.CheckActivity.Companion.goToCheckActivity
import org.unifiedpush.example.utils.registerOnRegistrationUpdate

class MainActivity : AppCompatActivity() {

    private lateinit var store: Store
    private var internalReceiver: BroadcastReceiver? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_main)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            registerForActivityResult(
                ActivityResultContracts.RequestPermission()
            ) { /*granted ->*/
            }.launch(
                Manifest.permission.POST_NOTIFICATIONS
            )
        }

        store = Store(this)
        if (store.endpoint != null) {
            goToCheckActivity(this)
        } else {
            internalReceiver = registerOnRegistrationUpdate {
                if (store.endpoint != null) {
                    internalReceiver?.let {
                        unregisterReceiver(it)
                    }
                    goToCheckActivity(this)
                }
            }
            findViewById<Button>(R.id.register_button).setOnClickListener {
                if (store.featureByteMessage) {
                    registerAppWithDialog(this, FEATURE_BYTES_MESSAGE)
                } else {
                    registerAppWithDialog(this)
                }
            }
        }
    }

    override fun onDestroy() {
        internalReceiver?.let {
            unregisterReceiver(it)
        }
        super.onDestroy()
    }

    companion object {
        fun goToMainActivity(context: Context) {
            val intent = Intent(
                context,
                MainActivity::class.java
            )
            context.startActivity(intent)
        }
    }
}
