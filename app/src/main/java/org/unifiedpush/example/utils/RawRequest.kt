package org.unifiedpush.example.utils

import androidx.annotation.GuardedBy
import com.android.volley.NetworkResponse
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.HttpHeaderParser

/**
 * Creates a new request with the given method.
 *
 * @param method the request method to use
 * @param url URL to fetch the string at
 * @param mListener Listener to receive the NetworkResponse response
 * @param errorListener Error listener, or null to ignore errors
 */
open class RawRequest(
    method: Int,
    url: String?,
    @field:GuardedBy("mLock") private var mListener: Response.Listener<NetworkResponse>?,
    errorListener: Response.ErrorListener?
) :
    Request<NetworkResponse>(method, url, errorListener) {
    /** Lock to guard mListener as it is cleared on cancel() and read on delivery.  */
    private val mLock = Any()

    /**
     * Creates a new GET request.
     *
     * @param url URL to fetch the string at
     * @param listener Listener to receive the NetworkResponse response
     * @param errorListener Error listener, or null to ignore errors
     */
    constructor(
        url: String?,
        listener: Response.Listener<NetworkResponse>?,
        errorListener: Response.ErrorListener?
    ) : this(
        Method.GET,
        url,
        listener,
        errorListener
    )

    override fun cancel() {
        super.cancel()
        synchronized(mLock) {
            mListener = null
        }
    }

    override fun deliverResponse(response: NetworkResponse) {
        var listener: Response.Listener<NetworkResponse>?
        synchronized(mLock) {
            listener = mListener
        }
        if (listener != null) {
            listener!!.onResponse(response)
        }
    }

    override fun parseNetworkResponse(response: NetworkResponse): Response<NetworkResponse> {
        return Response.success(response, HttpHeaderParser.parseCacheHeaders(response))
    }
}
