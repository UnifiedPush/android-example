package org.unifiedpush.example

import android.content.Context
import android.util.Log
import android.widget.Toast
import com.android.volley.RequestQueue
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import org.unifiedpush.example.utils.TAG

object Requests {
    fun sendNotification(context: Context, callback: (error: String?) -> Unit) {
        val requestQueue: RequestQueue = Volley.newRequestQueue(context)
        val url = Store(context).endpoint
        val stringRequest: StringRequest =
            object :
                StringRequest(
                    Method.POST, url,
                    Response.Listener {
                        Toast.makeText(context, "Done", Toast.LENGTH_SHORT).show()
                        callback(null)
                    },
                    Response.ErrorListener { e ->
                        Toast.makeText(context, "An error occurred.", Toast.LENGTH_SHORT).show()
                        Log.e(TAG, "An error occurred while testing the endpoint:\n$e")
                        callback(e.toString())
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
