package com.samsung.android.app.stepdiary.util

import java.sql.Timestamp
import java.util.*

object CalendarHelper {

    fun getStartTime(): Long {
        return Calendar.getInstance().timeInMillis
    }

    fun getEndTime(): Long {
        val timestamp = Timestamp(System.currentTimeMillis() + 60 * 60 * 1000)
        return timestamp.time
    }
}