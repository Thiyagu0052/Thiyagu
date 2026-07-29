package com.example.util

import android.graphics.BitmapFactory
import android.util.Base64

object ImageUtils {

    fun parseImageUris(rawString: String?): List<String> {
        if (rawString.isNullOrBlank()) return emptyList()

        val tokens = when {
            rawString.contains("|") -> rawString.split("|")
            rawString.contains("\n") -> rawString.split("\n")
            else -> rawString.split(",")
        }

        val result = mutableListOf<String>()
        var i = 0
        while (i < tokens.size) {
            val token = tokens[i].trim()
            if (token.isEmpty()) {
                i++
                continue
            }

            if (token.startsWith("data:image/") && !token.contains(";base64,") && i + 1 < tokens.size) {
                val reconstructed = "$token,${tokens[i + 1].trim()}"
                result.add(reconstructed)
                i += 2
            } else {
                result.add(token)
                i++
            }
        }
        return result
    }

    fun joinImageUris(uris: List<String>): String {
        return uris.map { it.trim() }.filter { it.isNotEmpty() }.joinToString("|")
    }

    fun getImageModel(uriStr: String): Any {
        if (uriStr.startsWith("data:image/")) {
            return try {
                val base64Data = uriStr.substringAfter("base64,")
                val bytes = Base64.decode(base64Data, Base64.DEFAULT)
                BitmapFactory.decodeByteArray(bytes, 0, bytes.size) ?: uriStr
            } catch (e: Exception) {
                uriStr
            }
        }
        return uriStr
    }
}
