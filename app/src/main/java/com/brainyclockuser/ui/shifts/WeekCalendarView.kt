package com.brainyclockuser.ui.shifts

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import com.brainyclockuser.R

class WeekCalendarView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr) {

    private val weekTitleTextView: TextView
    private val dateRangeTextView: TextView
    open val backArrowButton: ImageButton
    open val forwardArrowButton: ImageButton
    open val calendarHelper = WeekCalendarHelper()
    open var isShowingCurrentWeek = true

    init {
        // Inflate the layout
        LayoutInflater.from(context).inflate(R.layout.view_week_calendar, this, true)

        // Initialize views
        weekTitleTextView = findViewById(R.id.weekTitle)
        dateRangeTextView = findViewById(R.id.dateRange)
        backArrowButton = findViewById(R.id.backArrow)
        forwardArrowButton = findViewById(R.id.forwardArrow)

        // Set up initial display
        updateDisplay()

        // Set up click listeners
        /*backArrowButton.setOnClickListener {
            if (!isShowingCurrentWeek) {
                isShowingCurrentWeek = true
                calendarHelper.moveToCurrentWeek()
                updateDisplay()
            }
        }

        forwardArrowButton.setOnClickListener {
            if (isShowingCurrentWeek) {
                isShowingCurrentWeek = false
                calendarHelper.moveToNextWeek()
                updateDisplay()
            }
        }*/
    }

    open fun updateDisplay() {
        // Update arrow visibility
        backArrowButton.alpha = if (isShowingCurrentWeek) 0.3f else 1.0f
        forwardArrowButton.alpha = if (isShowingCurrentWeek) 1.0f else 0.3f

        // Update title and date range
        weekTitleTextView.text = if (isShowingCurrentWeek) "This Week" else "Next Week"
        dateRangeTextView.text = calendarHelper.getCurrentWeekRange()
    }
}