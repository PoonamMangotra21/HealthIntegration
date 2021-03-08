package com.samsung.android.app.stepdiary.util

import java.text.SimpleDateFormat
import java.util.*

object CalendarHelper {

    fun getStartTime(): Long {
        val calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"))
        val now = Date()
        calendar.time = now
        calendar.add(Calendar.HOUR_OF_DAY, -1)
        val startTime = calendar.timeInMillis
        return startTime
    }

    fun getEndTime(): Long {
        val calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"))
        val now = Date()
        calendar.time = now
        val endTime = calendar.timeInMillis
        return endTime
    }

    fun formattedTime(time: Long): String {
        val dateFormat = SimpleDateFormat("yyyy/MM/dd (E)", Locale.US)
        dateFormat.timeZone = TimeZone.getTimeZone("UTC")
        return dateFormat.format(time)
    }

    fun getTodayStartUtcTime(): Long {
        val today = Calendar.getInstance(TimeZone.getTimeZone("UTC"))
        today[Calendar.HOUR_OF_DAY] = 0
        today[Calendar.MINUTE] = 0
        today[Calendar.SECOND] = 0
        today[Calendar.MILLISECOND] = 0
        return today.timeInMillis
    }

}