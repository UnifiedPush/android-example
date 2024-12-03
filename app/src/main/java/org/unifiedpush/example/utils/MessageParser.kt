package org.unifiedpush.example.utils

import java.net.URLDecoder

private const val COULD_NOT_DECRYPT = "Could not decrypt content."

/**
 * Decode message as a `application/x-www-form-urlencoded` message.
 *
 * If the message doesn't contain "title=" and "message=", the full body is considered as the message.
 * If the message contains unknown characters, the message is [COULD_NOT_DECRYPT].
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
            notDecodedMap(message)
        }
    if (params.keys.contains("message") && params.keys.contains("title")) {
        return params
    }
    return notDecodedMap(message)
}

private fun notDecodedMap(message: String): Map<String, String> {
    return if (message.all { it.isDefined() && !it.isISOControl() }) {
        mapOf(
            "message" to message
        )
    } else {
        mapOf(
            "message" to COULD_NOT_DECRYPT
        )
    }
}
