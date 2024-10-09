package org.unifiedpush.example.activities

import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import org.unifiedpush.example.R
import org.unifiedpush.example.Store

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
        menu?.findItem(R.id.action_dev_mode)?.apply {
            isChecked = store.devMode
            setOnMenuItemClickListener {
                val newState = !store.devMode
                store.devMode = newState
                it.isChecked = newState
                false
            }
        }
        return super.onCreateOptionsMenu(menu)
    }
}