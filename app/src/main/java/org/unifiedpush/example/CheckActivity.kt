package org.unifiedpush.example

import android.app.Activity
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import com.android.volley.RequestQueue
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import org.unifiedpush.android.connector.*

const val UPDATE = "org.unifiedpush.example.android.action.UPDATE"

class CheckActivity : Activity() {

    private var endpoint = ""
    private val up = Registration()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_check)

        val btn: Button = findViewById<View>(R.id.button_notify) as Button
        btn.isEnabled = false


        val intentFilter = IntentFilter().apply {
            addAction(UPDATE)
        }
        registerReceiver(checkReceiver, intentFilter)
        up.registerAppWithDialog(this)
    }

    override fun onDestroy() {
        up.unregisterApp(this)
        unregisterReceiver(checkReceiver)
        super.onDestroy()
    }

    private val checkReceiver: BroadcastReceiver = object: BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            when(intent!!.action){
                UPDATE -> {
                    endpoint = intent.getStringExtra("endpoint")?: ""
                    val registered = intent.getStringExtra("registered")?: "false"
                    findViewById<TextView>(R.id.text_result_register).apply {
                        text = registered
                    }
                    findViewById<TextView>(R.id.text_gateway_value).apply {
                        text = endpoint
                    }
                    val btn: Button = findViewById<View>(R.id.button_notify) as Button
                    btn.isEnabled = (registered == "true")
                }
            }
        }
    }

    fun unregister(view: View) {
        Toast.makeText(this, "Unregistering", Toast.LENGTH_SHORT).show()
        up.unregisterApp(this)
    }

    fun sendNotification(view: View) {
        val requestQueue: RequestQueue = Volley.newRequestQueue(this)
        val url = endpoint
        val stringRequest: StringRequest =
            object :
                StringRequest(Method.POST, url,
                    Response.Listener<String> { Toast.makeText(applicationContext, "Done", Toast.LENGTH_SHORT).show() },
                    Response.ErrorListener { Toast.makeText(applicationContext, "An error occurred", Toast.LENGTH_SHORT).show() }) {
                override fun getParams(): MutableMap<String, String> {
                    val params = mutableMapOf<String, String>()
                    params["title"] = "Test"
                    params["message"] = "With UnifiedPush"
                    params["priority"] = "5"
                    return params
                }
            }
        requestQueue.add(stringRequest)
    }
}