package com.bish.tasklist

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

object Utils {
    fun String.capitalize(): String {
        return replaceFirstChar {
            if (it.isLowerCase()) it.titlecase(
                Locale.getDefault()
            ) else it.toString()
        }
    }

    fun Long.taskCreatedDate(): String {
        val sdf = SimpleDateFormat("dd-MMM-yyyy\nHH:mm:ss", Locale.getDefault())
        sdf.timeZone = TimeZone.getDefault()
        return sdf.format(Date(this))
    }
}