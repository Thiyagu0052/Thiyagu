package com.example.util

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object DateUtils {
    /**
     * Returns the current time in ISO 8601 format.
     * Example: 2026-07-31T08:56:40.630+05:30
     */
    fun getCurrentIsoTimestamp(): String {
        return try {
            val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX", Locale.getDefault())
            sdf.format(Date())
        } catch (e: Exception) {
            // Fallback for older APIs if XXX is not supported (though API 24+ should support it)
            val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZZZZZ", Locale.getDefault())
            sdf.format(Date())
        }
    }

    /**
     * Converts HH:mm (24h) to hh:mm a (AM/PM).
     */
    fun formatToAmPm(time24h: String): String {
        return try {
            val sdf24 = SimpleDateFormat("HH:mm", Locale.getDefault())
            val sdfAmPm = SimpleDateFormat("hh:mm a", Locale.getDefault())
            val date = sdf24.parse(time24h)
            if (date != null) sdfAmPm.format(date) else time24h
        } catch (e: Exception) {
            time24h
        }
    }
}
