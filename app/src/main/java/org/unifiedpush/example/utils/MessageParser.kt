package org.unifiedpush.example.utils

import java.net.URLDecoder

/**
 * Decode message as a `application/x-www-form-urlencoded` message.
 *
 * @return a map of key => value.
 * "title=myTitle&message=myContent" will return a Map {"title"=>"myTitle, "message"=>"myContent"}
 */
internal fun decodeMessage(message: String): Map<String, String> {
    val params =
        try {
            val dict = message.split("&")
            dict.associate {
                try {
                    URLDecoder.decode(it.split("=")[0], "UTF-8") to
                            URLDecoder.decode(it.split("=")[1], "UTF-8")
                } catch (e: Exception) {
                    "" to ""
                }
            }
        } catch (e: Exception) {
            mapOf("message" to message)
        }
    if (params.keys.contains("message") && params.keys.contains("title")) {
        return params
    }
    return mapOf("message" to message)
}