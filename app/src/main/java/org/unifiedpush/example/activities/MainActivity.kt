package org.unifiedpush.example.activities

import android.Manifest
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
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
        setSupportActionBar(findViewById(R.id.toolbar))
        findViewById<androidx.appcompat.widget.Toolbar>(R.id.toolbar)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            registerForActivityResult(
                ActivityResultContracts.RequestPermission()
            ) { /*granted ->*/
            }.launch(
                Manifest.permission.POST_NOTIFICATIONS
            )
        }

        store = Store(this)
        findViewById<Button>(R.id.register_button).setOnClickListener {
            if (store.featureByteMessage) {
                registerAppWithDialog(this, FEATURE_BYTES_MESSAGE)
            } else {
                registerAppWithDialog(this)
            }
        }
    }

    override fun onResume() {
        super.onResume()
        if (store.endpoint != null) {
            goToCheckActivity(this)
            finish()
        } else {
            internalReceiver = registerOnRegistrationUpdate {
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

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Keep the overlay menu open after an item is selected
        when (item.itemId) {
            R.id.action_feature_byte_message,
            R.id.action_webpush -> {
                item.setShowAsAction(MenuItem.SHOW_AS_ACTION_COLLAPSE_ACTION_VIEW)
                item.actionView = View(this)
                item.setOnActionExpandListener(object : MenuItem.OnActionExpandListener {
                    override fun onMenuItemActionExpand(item: MenuItem): Boolean {
                        return false
                    }

                    override fun onMenuItemActionCollapse(item: MenuItem): Boolean {
                        return false
                    }
                })
                return false
            }
            else -> {
                return true
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.overlay_main, menu)
        menu?.findItem(R.id.action_feature_byte_message)?.apply {
            isChecked = store.featureByteMessage
            isEnabled = !store.webpush
            setOnMenuItemClickListener {
                val check = !it.isChecked
                it.isChecked = check
                store.featureByteMessage = check
                false
            }
        }
        menu?.findItem(R.id.action_webpush)?.apply {
            isChecked = store.webpush
            setOnMenuItemClickListener {
                val check = !it.isChecked
                it.isChecked = check
                menu.findItem(R.id.action_feature_byte_message)?.apply {
                    isChecked = check
                    isEnabled = !check
                }
                store.webpush = check
                store.featureByteMessage = check
                // gen authSecret and keyPair
                store.authSecret
                store.keyPair
                false
            }
        }

        return super.onCreateOptionsMenu(menu)
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
