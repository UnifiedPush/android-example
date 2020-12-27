package org.unifiedpush.example

import android.app.Activity
import android.app.AlertDialog
import android.content.BroadcastReceiver
import android.content.Context
import android.content.IntentFilter
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import com.android.volley.RequestQueue
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import org.unifiedpush.connector.*


class CheckActivity : Activity() {

    private var endpoint = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_check)

        val btn: Button = findViewById<View>(R.id.button_notify) as Button
        btn.isEnabled = false


        val intentFilter = IntentFilter().apply {
            addAction(NEW_ENDPOINT)
            addAction(UNREGISTERED)
        }
        registerReceiver(checkReceiver, intentFilter)
        chooseDistributor(this)
    }

    override fun onDestroy() {
        unregisterReceiver(checkReceiver)
        super.onDestroy()
    }

    private val checkReceiver: BroadcastReceiver = object: MessagingReceiver(object : MessagingReceiverHandler {
        override fun onNewEndpoint(context: Context?, endpoint: String) {
            this@CheckActivity.endpoint = endpoint
            findViewById<TextView>(R.id.text_result_register).apply {
                text = "true"
            }
            findViewById<TextView>(R.id.text_gateway_value).apply {
                text = endpoint
            }
            val btn: Button = findViewById<View>(R.id.button_notify) as Button
            btn.isEnabled = true
        }

        override fun onUnregistered(context: Context?) {
            findViewById<TextView>(R.id.text_result_register).apply {
                text = "false"
            }
            findViewById<TextView>(R.id.text_gateway_value).apply {
                text = ""
            }
            val btn: Button = findViewById<View>(R.id.button_notify) as Button
            btn.isEnabled = false
        }

        override fun onUnregisteredAck(context: Context?) {
            onUnregistered(context)
        }

        override fun onMessage(context: Context?, message: String) {}
    }){}

    fun unregister(view: View) {
        Toast.makeText(this, "Unregistering", Toast.LENGTH_SHORT).show()
        unregisterApp(this)
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

    private fun chooseDistributor(context: Context){
        val builder: AlertDialog.Builder = AlertDialog.Builder(context)
        builder.setTitle("Choose a distributor") // add a list

        val distributors = getDistributors(context).toTypedArray()
        builder.setItems(distributors) { _, which ->
            val distributor = distributors[which]
            saveDistributor(this, distributor)
            Log.d("CheckActivity","distributor: $distributor")
            registerApp(this)
        } // create and show the alert dialog

        val dialog: AlertDialog = builder.create()
        dialog.show()
    }
}