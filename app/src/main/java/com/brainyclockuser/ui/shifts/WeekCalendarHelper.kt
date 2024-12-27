package com.brainyclockuser.ui.shifts

import java.text.SimpleDateFormat
import java.util.*

class WeekCalendarHelper {
    private val calendar: Calendar = Calendar.getInstance()
    private val dateFormatter = SimpleDateFormat("MMM d", Locale.getDefault())

    init {
        // Initialize to start of current week
        moveToCurrentWeek()
    }

    fun moveToCurrentWeek() {
        calendar.timeInMillis = System.currentTimeMillis()
        moveToStartOfWeek()
    }

    private fun moveToStartOfWeek() {
        // If we're not already at the start of the week, move to it
        while (calendar.get(Calendar.DAY_OF_WEEK) != Calendar.MONDAY) {
            calendar.add(Calendar.DAY_OF_YEAR, -1)
        }
    }

    fun getCurrentWeekRange(): String {
        // Get start date
        val startDate = dateFormatter.format(calendar.time)

        // Move to end of week
        calendar.add(Calendar.DAY_OF_YEAR, 6)
        val endDate = dateFormatter.format(calendar.time)

        // Move back to start of week
        calendar.add(Calendar.DAY_OF_YEAR, -6)

        return "$startDate - $endDate"
    }

    fun moveToNextWeek() {
        calendar.add(Calendar.WEEK_OF_YEAR, 1)
        moveToStartOfWeek()
    }
}