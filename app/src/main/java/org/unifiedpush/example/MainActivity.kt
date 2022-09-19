package org.unifiedpush.example

import android.Manifest
import android.content.Intent
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            registerForActivityResult(
                ActivityResultContracts.RequestPermission()
            ) {/*granted ->*/
            } .launch(
                Manifest.permission.POST_NOTIFICATIONS
            )
        }
    }

    fun goToCheckList(view: View){
        val intent = Intent(this,
            CheckActivity::class.java)
        startActivity(intent)
    }

}
