import android.util.Log
import com.brainyclockuser.base.model.BaseArrayModel
import com.brainyclockuser.ui.clockin.ShiftsModels
import com.brainyclockuser.ui.shifts.SiteEmployeeModel
import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit

data class StaffShiftResponse(
    @SerializedName("message")
    @Expose
    val message: String? = null,

    @SerializedName("success")
    @Expose
    val success: Boolean? = null,

    @SerializedName("data")
    @Expose
    val data: List<AllStaffShift>? = null
)

class AllStaffShift : BaseArrayModel<AllStaffShift>() {
    companion object {
        const val STATUS_PRESENT = 0
        const val STATUS_LATE = 1
        const val STATUS_ABSENT = 2
        const val STATUS_LUNCH_IN = 3
        const val STATUS_LUNCH_OUT = 4
        const val STATUS_CLOCKED_OUT = 5
        const val STATUS_INITIAL = 6
        const val LATE_BUFFER_IN_MIN = 10
        const val ABSENT_BUFFER_IN_MIN = 60
    }

    val formatter: SimpleDateFormat = SimpleDateFormat(
        "yyyy-MM-dd HH:mm:ss",
        Locale.getDefault()
    )

    val formatterShort: SimpleDateFormat = SimpleDateFormat(
        "yyyy-MM-dd HH:mm",
        Locale.getDefault()
    )


    @SerializedName("id")
    @Expose
    val id: Int? = null

    @SerializedName("employeeId")
    @Expose
    val employeeId: Int? = null

    @SerializedName("personalId")
    @Expose
    val personalId: Int? = null

    @SerializedName("position")
    @Expose
    val position: String? = null

    @SerializedName("notes")
    @Expose
    val notes: String? = null

    @SerializedName("date")
    @Expose
    val date: String? = null

    @SerializedName("siteId")
    @Expose
    val siteId: Int? = null

    @SerializedName("siteType")
    @Expose
    val siteType: String? = null

    @SerializedName("endDate")
    @Expose
    val endDate: String? = null

    @SerializedName("startDate")
    @Expose
    val startDate: String? = null

    @SerializedName("eventId")
    @Expose
    val eventId: Int? = null

    @SerializedName("siteName")
    @Expose
    val siteName: String? = null

    @SerializedName("distNumber")
    @Expose
    val distNumber: String? = null

    @SerializedName("distName")
    @Expose
    val distName: String? = null

    // Weekly time-in and time-out details
    @SerializedName("mondayTimeIn")
    @Expose
    var mondayTimeIn: String? = null

    @SerializedName("mondayTimeOut")
    @Expose
    var mondayTimeOut: String? = null

    @SerializedName("tuesdayTimeIn")
    @Expose
    var tuesdayTimeIn: String? = null

    @SerializedName("tuesdayTimeOut")
    @Expose
    var tuesdayTimeOut: String? = null

    @SerializedName("wednesdayTimeIn")
    @Expose
    var wednesdayTimeIn: String? = null

    @SerializedName("wednesdayTimeOut")
    @Expose
    var wednesdayTimeOut: String? = null

    @SerializedName("thursdayTimeIn")
    @Expose
    var thursdayTimeIn: String? = null

    @SerializedName("thursdayTimeOut")
    @Expose
    var thursdayTimeOut: String? = null

    @SerializedName("fridayTimeIn")
    @Expose
    var fridayTimeIn: String? = null

    @SerializedName("fridayTimeOut")
    @Expose
    var fridayTimeOut: String? = null

    @SerializedName("firstName")
    @Expose
    val firstName: String? = null

    @SerializedName("lastName")
    @Expose
    val lastName: String? = null

    @SerializedName("clockIn")
    @Expose
    var clockIn: String? = null

    @SerializedName("clockOut")
    @Expose
    var clockOut: String? = null

    @SerializedName("lunchIn")
    @Expose
    var lunchIn: String? = null

    @SerializedName("lunchOut")
    @Expose
    var lunchOut: String? = null

    fun getCustomStatus(): Int {
        if(isAbsent()) {
            return  STATUS_ABSENT
        }
        if(isLate()) {
            return  STATUS_LATE
        }
        if(clockIn != null && clockOut == null) {
            return STATUS_PRESENT
        }
        if(clockIn != null && clockOut != null) {
            return STATUS_CLOCKED_OUT
        }
        return STATUS_INITIAL
    }

    fun getClockedStatus(): Int {
        if(lunchIn != null && lunchOut == null) {
            return STATUS_LUNCH_IN
        }
        if(lunchIn != null && lunchOut != null && clockOut == null) {
            return STATUS_LUNCH_OUT
        }
        if(clockIn != null && clockOut == null) {
            return STATUS_PRESENT
        }
        if(clockIn != null && clockOut != null) {
            return STATUS_CLOCKED_OUT
        }
        return STATUS_INITIAL
    }

    fun onlyClockedIn(): Boolean {
        return clockIn != null
    }

    private fun getClockInDetails(): Pair<String?, String?> {
        val currentDay = Calendar.getInstance().get(Calendar.DAY_OF_WEEK);
        if(currentDay == Calendar.MONDAY) {
            return Pair(mondayTimeIn, mondayTimeOut)
        }
        if(currentDay == Calendar.TUESDAY) {
            return Pair(tuesdayTimeIn, tuesdayTimeOut)
        }
        if(currentDay == Calendar.WEDNESDAY) {
            return Pair(wednesdayTimeIn, wednesdayTimeOut)
        }
        if(currentDay == Calendar.THURSDAY) {
            return Pair(thursdayTimeIn, thursdayTimeOut)
        }
        if(currentDay == Calendar.FRIDAY) {
            return Pair(fridayTimeIn, fridayTimeOut)
        }
        return Pair(null, null)
    }

    fun getClockInTimes(): Pair<Date?, Date?> {
        val pattern = "HH:mm:ss"
        val datePattern = "yyyy-MM-dd"
        val dateFormatter = SimpleDateFormat(datePattern, Locale.getDefault());
        val dateString = dateFormatter.format(Date());
        val timeFormatter = SimpleDateFormat("$datePattern $pattern", Locale.getDefault())
        val clockInDetails = getClockInDetails()
        return Pair(timeFormatter.parse("$dateString ${clockInDetails.first}"), timeFormatter.parse("$dateString ${clockInDetails.second}"))
    }

    fun getClockInOutTimes(): Pair<Date?, Date?> {
        val pattern = "HH:mm:ss"
        val datePattern = "yyyy-MM-dd"
        val dateFormatter = SimpleDateFormat(datePattern, Locale.getDefault());
        val dateString = dateFormatter.format(Date());
        val timeFormatter = SimpleDateFormat("$datePattern $pattern", Locale.getDefault())
        if(clockIn != null) {
            if(clockOut != null) {
                return Pair(timeFormatter.parse("$dateString $clockIn"), timeFormatter.parse("$dateString $clockOut"))
            }
            return Pair(timeFormatter.parse("$dateString $clockIn"), null)
        }
        return Pair(null, null)
    }

    fun isAbsent(): Boolean {
        val clockInDetails = getClockInTimes()
        val currentDate = clockInDetails.first
        val clockInOut = getClockInOutTimes();
        if(currentDate != null) {
            val clocked = clockInOut.first ?: Date()
            val differenceInMilliseconds = clocked.time - currentDate.time
            val differenceInMinutes = differenceInMilliseconds / (1000 * 60)
            return differenceInMinutes > ABSENT_BUFFER_IN_MIN
        } else {
            return false
        }
    }

    fun isLate(): Boolean {
//        val clockInDetails = getClockInTimes()
//        val currentDate = clockInDetails.first
//        val clockInOut = getClockInOutTimes();
//        if(currentDate != null && clockInOut.first != null && clockInOut.second == null) {
//            val clocked = clockInOut.first ?: Date()
//            val differenceInMilliseconds = clocked.time - currentDate.time
//            val differenceInMinutes = differenceInMilliseconds / (1000 * 60)
//            return differenceInMinutes in (LATE_BUFFER_IN_MIN + 1)..<ABSENT_BUFFER_IN_MIN
//        } else {
//            return false
//        }
        return this.lateTimes() != null
    }

    fun lateTimes(): Pair<Long, Long>? {
        val clockInDetails = getClockInTimes()
        val currentDate = clockInDetails.first
        val clockInOut = getClockInOutTimes();
        if(currentDate != null && clockInOut.first != null && clockInOut.second == null) {
            val clocked = clockInOut.first ?: Date()
            val differenceInMilliseconds = clocked.time - currentDate.time
            val differenceInMinutes = differenceInMilliseconds / (1000 * 60)
            val differenceInHours = differenceInMinutes / 60
            val status = differenceInMinutes in (LATE_BUFFER_IN_MIN + 1)..<ABSENT_BUFFER_IN_MIN
            return if(status) {
                Pair(differenceInHours, differenceInMinutes)
            } else {
                null
            }
        } else {
            return null
        }

    }

    fun diffInTimes(startDate: Date, endDate: Date): Pair<Long, Long>? {
        val differenceInMilliseconds = endDate.time - startDate.time
        val differenceInMinutes = differenceInMilliseconds / (1000 * 60)
        val differenceInHours = differenceInMinutes / 60
        val differencePendingMinutes = differenceInMinutes % 60
        return Pair(differenceInHours, differencePendingMinutes)
    }

}