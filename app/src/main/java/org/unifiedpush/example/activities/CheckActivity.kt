package org.unifiedpush.example.activities

import android.app.Activity
import android.content.BroadcastReceiver
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
import org.unifiedpush.android.connector.UnifiedPush
import org.unifiedpush.example.R
import org.unifiedpush.example.registerOnRegistrationUpdate
import org.unifiedpush.example.updateRegistrationInfo
import org.unifiedpush.example.utils.Notifier

class CheckActivity : Activity() {

    companion object {
        private var endpoint = ""
        const val featureByteMessage = false
        private val TAG = CheckActivity::class.java.simpleName
    }

    private var checkReceiver: BroadcastReceiver? = null
    private var notifier: Notifier? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_check)

        val btn: Button = findViewById<View>(R.id.button_notify) as Button
        btn.isEnabled = false

        checkReceiver = registerOnRegistrationUpdate { registered, endpoint ->
            onRegistrationUpdate(registered, endpoint)
        }

        if (featureByteMessage) {
            UnifiedPush.registerAppWithDialog(
                this,
                features = arrayListOf(UnifiedPush.FEATURE_BYTES_MESSAGE)
            )
        } else {
            UnifiedPush.registerAppWithDialog(this)
        }
    }

    override fun onDestroy() {
        UnifiedPush.unregisterApp(this)
        checkReceiver?.let {
            unregisterReceiver(it)
        }
        super.onDestroy()
    }

    private fun onRegistrationUpdate(registered: Boolean, new_endpoint: String?) {
        endpoint = new_endpoint ?: ""
        findViewById<TextView>(R.id.text_result_register).apply {
            text = registered.toString()
        }
        findViewById<TextView>(R.id.text_gateway_value).apply {
            text = endpoint ?: ""
        }
        val btn: Button = findViewById<View>(R.id.button_notify) as Button
        btn.isEnabled = registered
    }

    fun unregister(view: View) {
        Toast.makeText(this, "Unregistering", Toast.LENGTH_SHORT).show()
        UnifiedPush.unregisterApp(this)
        UnifiedPush.forceRemoveDistributor(this)
        updateRegistrationInfo(false, "")
    }

    fun reRegister(view: View) {
        if (featureByteMessage) {
            UnifiedPush.registerAppWithDialog(
                this,
                features = arrayListOf(UnifiedPush.FEATURE_BYTES_MESSAGE)
            )
        } else {
            UnifiedPush.registerAppWithDialog(this)
        }
    }

    fun sendNotification(view: View) {
        val requestQueue: RequestQueue = Volley.newRequestQueue(this)
        val url = endpoint
        val stringRequest: StringRequest =
            object :
                StringRequest(
                    Method.POST, url,
                    Response.Listener {
                        Toast.makeText(applicationContext, "Done", Toast.LENGTH_SHORT).show()
                        findViewById<TextView>(R.id.error_text).text = ""
                    },
                    Response.ErrorListener { e ->
                        Toast.makeText(applicationContext, "An error occurred.", Toast.LENGTH_SHORT).show()
                        Log.e(TAG, "An error occurred while testing the endpoint:\n$e")
                        findViewById<TextView>(R.id.error_text).text = "Error:\n$e"
                    }
                ) {
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
