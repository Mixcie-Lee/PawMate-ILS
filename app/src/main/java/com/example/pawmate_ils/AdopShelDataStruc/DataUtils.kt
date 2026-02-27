package com.example.pawmate_ils.AdopShelDataStruc
import java.text.SimpleDateFormat
import java.util.*

object DateUtils {
    fun formatToPST(createdAt: Long): String {
        val date = Date(createdAt)
        // Matches your reference image: "04 MAR AT 01:54"
        val sdf = SimpleDateFormat("dd MMM 'AT' HH:mm", Locale.ENGLISH)

        // Force Manila Time (GMT+8) regardless of device settings
        sdf.timeZone = TimeZone.getTimeZone("Asia/Manila")

        return sdf.format(date).uppercase()
    }
}