package org.unifiedpush.example.activities

import android.os.Build
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import org.unifiedpush.example.R
import org.unifiedpush.example.Store
import org.unifiedpush.example.utils.updateRegistrationInfo

open class WithOverlayActivity: AppCompatActivity()  {
    lateinit var store: Store

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        store = Store(this)
        setSupportActionBar(findViewById(R.id.toolbar))
        findViewById<androidx.appcompat.widget.Toolbar>(R.id.toolbar)
    }

    // Keep the overlay menu open after an item is selected
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        item.setShowAsAction(MenuItem.SHOW_AS_ACTION_COLLAPSE_ACTION_VIEW)
        item.actionView = View(this)
        item.setOnActionExpandListener(
            object : MenuItem.OnActionExpandListener {
                override fun onMenuItemActionExpand(item: MenuItem): Boolean {
                    return false
                }

                override fun onMenuItemActionCollapse(item: MenuItem): Boolean {
                    return false
                }
            },
        )
        return false
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.overlay_main, menu)
        menu?.apply {
            findItem(R.id.action_dev_mode)?.setOnMenuItemClickListener {
                store.devMode = !store.devMode
                setMenuItemVisibility(menu)
                this@WithOverlayActivity.updateRegistrationInfo()
                false
            }
            findItem(R.id.action_force_encrypted)?.setOnMenuItemClickListener {
                store.devForceEncrypted = !store.devForceEncrypted
                setMenuItemVisibility(menu)
                false
            }
            findItem(R.id.action_use_vapid)?.setOnMenuItemClickListener {
                store.devUseVapid = !store.devUseVapid
                setMenuItemVisibility(menu)
                this@WithOverlayActivity.updateRegistrationInfo()
                false
            }
            findItem(R.id.action_cleartext_test)?.setOnMenuItemClickListener {
                store.devCleartextTest = !store.devCleartextTest
                setMenuItemVisibility(menu)
                false
            }
            findItem(R.id.action_wrongkeys_test)?.setOnMenuItemClickListener {
                store.devWrongKeysTest = !store.devWrongKeysTest
                setMenuItemVisibility(menu)
                false
            }
            findItem(R.id.action_wrong_vapid)?.setOnMenuItemClickListener {
                store.devWrongVapidKeysTest = !store.devWrongVapidKeysTest
                setMenuItemVisibility(menu)
                false
            }
            findItem(R.id.action_start_foreground_on_message)?.setOnMenuItemClickListener {
                store.devStartForeground = !store.devStartForeground
                setMenuItemVisibility(menu)
                false
            }
            setMenuItemVisibility(this)
        }
        return super.onCreateOptionsMenu(menu)
    }

    private fun setMenuItemVisibility(menu: Menu) {
        val devMode = store.devMode
        menu.findItem(R.id.action_dev_mode)?.isChecked = devMode
        menu.findItem(R.id.action_force_encrypted)?.apply {
            isVisible = devMode
            isChecked = store.devForceEncrypted
        }
        menu.findItem(R.id.action_use_vapid)?.apply {
            isVisible = devMode
            isChecked = store.devUseVapid
        }
        menu.findItem(R.id.action_cleartext_test)?.apply {
            isVisible = devMode
            isChecked = store.devCleartextTest
        }
        menu.findItem(R.id.action_wrongkeys_test)?.apply {
            val clearText = store.devCleartextTest
            isVisible = devMode
            isEnabled = !clearText
            isChecked = !clearText && store.devWrongKeysTest
        }
        menu.findItem(R.id.action_wrong_vapid)?.apply {
            val useVapid = store.distributorRequiresVapid || store.devUseVapid
            isVisible = devMode
            isEnabled = useVapid
            isChecked = useVapid && store.devWrongVapidKeysTest
        }
        menu.findItem(R.id.action_start_foreground_on_message)?.apply {
            isVisible = devMode && Build.VERSION.SDK_INT >= Build.VERSION_CODES.O
            isChecked = store.devStartForeground
        }
    }
}