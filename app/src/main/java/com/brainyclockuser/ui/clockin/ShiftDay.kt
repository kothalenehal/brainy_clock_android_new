package com.brainyclockuser.ui.clockin

import AllStaffShift
import android.util.Log
import com.brainyclockuser.base.model.BaseArrayModel
import com.brainyclockuser.ui.shifts.checkForAdditionalForRegularShift
import com.brainyclockuser.ui.shifts.checkForAdditionalForRegularShiftDay
import com.brainyclockuser.ui.shifts.checkForValidAdditionalShift
import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Calendar
import java.util.Date
import java.util.Locale

class ShiftsModels : BaseArrayModel<ShiftsModels>() {
    @SerializedName("shift_id")
    @Expose
    var shiftId: Int? = null

    @SerializedName("name")
    @Expose
    var shiftName: String? = null

    // A list of ShiftDay objects to represent the days array
    @SerializedName("days")
    @Expose
    var days: List<ShiftDay>? = null  // List of ShiftDay objects

    @SerializedName("Date")
    @Expose
    var date: String? = null  // Nullable Date (could be null)

    @SerializedName("updatedDate")
    @Expose
    var updatedDate: String? = null

    @SerializedName("eventType")
    @Expose
    var eventType: String? = null

    @SerializedName("StartDate")
    @Expose
    var startDate: String? = null  // Start date (nullable)

    @SerializedName("EndDate")
    @Expose
    var endDate: String? = null  // End date (nullable)

    @SerializedName("location_id")
    @Expose
    var locationId: String? = null  // Location ID (string)

    @SerializedName("location_name")
    @Expose
    var locationName: String? = null  // Location Name

    @SerializedName("EventId")
    @Expose
    var eventId: Int? = null  // Event ID

    @SerializedName("clockIn")
    @Expose
    var clockIn: String? = null  // Nullable Clock In time

    @SerializedName("clockOut")
    @Expose
    var clockOut: String? = null  // Nullable Clock Out time

    @SerializedName("lunchIn")
    @Expose
    var lunchIn: String? = null  // Nullable Clock In time

    @SerializedName("lunchOut")
    @Expose
    var lunchOut: String? = null

    var customStatus: Int = 0

    @SerializedName("position")
    @Expose
    var position: String? = null  // Nullable Clock Out time

    override fun toString(): String {
        return "$shiftName $days $date $startDate $endDate $locationId $locationName $clockIn $clockOut"
    }

    fun hasSiteEmployeePermission() : Boolean {
        return clockIn != null && position == "Site Director"
    }

    fun getDayShift(): AllStaffShift {
        var allDayShift = AllStaffShift()
        allDayShift.clockIn = clockIn
        allDayShift.clockOut = clockOut
        allDayShift.lunchIn = lunchIn
        allDayShift.lunchOut = lunchOut

        val currentDay = Calendar.getInstance().get(Calendar.DAY_OF_WEEK);
//        val dayFormatter = SimpleDateFormat("EEE", Locale.getDefault())
//        val dayName = dayFormatter.format(Date()).lowercase()
        val days = this.days ?: ArrayList<ShiftDay>()
        if(days.isEmpty()) {
            return allDayShift
        }
        if(currentDay == Calendar.MONDAY) {
            val currentDay = days.find { it.day == "mon" }
            if(currentDay?.timeIn != null && currentDay.timeOut != null) {
                allDayShift.mondayTimeIn = currentDay?.timeIn
                allDayShift.mondayTimeOut = currentDay.timeOut
            }
        }
        if(currentDay == Calendar.TUESDAY) {
            val currentDay = days.find { it.day == "tue" }
            if(currentDay?.timeIn != null && currentDay.timeOut != null) {
                allDayShift.tuesdayTimeIn = currentDay?.timeIn
                allDayShift.tuesdayTimeOut = currentDay.timeOut
            }
        }
        if(currentDay == Calendar.WEDNESDAY) {
            val currentDay = days.find { it.day == "wed" }
            if(currentDay?.timeIn != null && currentDay.timeOut != null) {
                allDayShift.wednesdayTimeIn = currentDay?.timeIn
                allDayShift.wednesdayTimeOut = currentDay.timeOut
            }
        }
        if(currentDay == Calendar.THURSDAY) {
            val currentDay = days.find { it.day == "thu" }
            if(currentDay?.timeIn != null && currentDay.timeOut != null) {
                allDayShift.thursdayTimeIn = currentDay?.timeIn
                allDayShift.thursdayTimeOut = currentDay.timeOut
            }
        }
        if(currentDay == Calendar.FRIDAY) {
            val currentDay = days.find { it.day == "fri" }
            if(currentDay?.timeIn != null && currentDay.timeOut != null) {
                allDayShift.fridayTimeIn = currentDay?.timeIn
                allDayShift.fridayTimeOut = currentDay.timeOut
            }
        }
        return allDayShift
    }

    fun getClockedStatus(): Int {
        val dayShift = getDayShift()
        return dayShift.getClockedStatus()
    }

    fun isValid(): Boolean {
        return (this.startDate != null && this.endDate != null) || (this.date != null)
    }

    fun isToday(shifts: List<ShiftsModels>): Boolean {

        val dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")

        val date = this.date?.let {
            try {
                LocalDateTime.parse(it, dateTimeFormatter).toLocalDate()
            } catch (e: Exception) {
                Log.e("TAG>>>", "Error parsing date: $it", e)
                null
            }
        }
        val startDate = this.startDate?.let {
            try {
                LocalDateTime.parse(it, dateTimeFormatter).toLocalDate()
            } catch (e: Exception) {
                Log.e("TAG>>>", "Error parsing startDate: $it", e)
                null
            }
        }
        val endDate = this.endDate?.let {
            try {
                LocalDateTime.parse(it, dateTimeFormatter).toLocalDate()
            } catch (e: Exception) {
                Log.e("TAG>>>", "Error parsing endDate: $it", e)
                null
            }
        }

        // updatedDate
        val updatedDate = this.updatedDate?.let {
            try {
                LocalDateTime.parse(it, dateTimeFormatter).toLocalDate()
            } catch (e: Exception) {
                Log.e("TAG>>>", "Error parsing date: $it", e)
                null
            }
        }

        val today = LocalDate.now()

        val todayStart = today.atStartOfDay().toLocalDate()
        val todayEnd = today.atTime(23, 59, 59, 999_999_999).toLocalDate();

        // Additional Shift Check when shift is regular
        val hasAdditionalShift = checkForAdditionalForRegularShiftDay(shifts, this, todayStart, todayEnd, startDate, endDate, updatedDate)
//
//        val isValidAdditionalShift = checkForValidAdditionalShift(shifts, this, todayStart, todayEnd, startDate, endDate, updatedDate)

        if(updatedDate != null) {
            val updatedDateFormatted = LocalDateTime.now().toLocalDate().atStartOfDay().format(dateTimeFormatter)
            if(this.updatedDate == updatedDateFormatted) {
                return true;
            } else {
                return false
            }
        }

        return when {
//            hasAdditionalShift -> false
//
//            isValidAdditionalShift -> true

            startDate != null && endDate != null && !hasAdditionalShift -> today in startDate..endDate

            date != null -> today.equals(date)

            else -> false
        }

    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true // Check if it's the same reference
        if (other !is ShiftsModels) return false // Check if 'other' is a Person object

        // Compare properties
        return this.shiftId == other.shiftId
    }

    // Override hashCode to ensure consistent behavior with equals
    override fun hashCode(): Int {
        var result = 31 * shiftId.hashCode()
        return result
    }

}

// ShiftDay data class to represent individual shift details for each day
data class ShiftDay(
    @SerializedName("day")
    @Expose
    var day: String? = null,  // Day of the week (e.g., "mon", "tue")

    @SerializedName("timeIn")
    @Expose
    var timeIn: String? = null,  // Time in (nullable as per your example)

    @SerializedName("timeOut")
    @Expose
    var timeOut: String? = null  // Time out (nullable as per your example)
)