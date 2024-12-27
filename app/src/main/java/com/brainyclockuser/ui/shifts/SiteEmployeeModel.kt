package com.brainyclockuser.ui.shifts

import com.brainyclockuser.base.model.BaseArrayModel
import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit

class SiteEmployeeModel : BaseArrayModel<SiteEmployeeModel>() {

    companion object {
        const val STATUS_PRESENT = 0
        const val STATUS_LATE = 1
        const val STATUS_ABSENT = 2
        const val STATUS_CLOCKED_OUT = 3
        const val STATUS_INITIAL = 4
    }

    private val LATE_BUFFER_IN_MIN = 10

    private val ABSENT_BUFFER_IN_MIN = 30

    val formatter: SimpleDateFormat = SimpleDateFormat(
        "yyyy-MM-dd HH:mm:ss",
        Locale.getDefault()
    )

    val formatterShort: SimpleDateFormat = SimpleDateFormat(
        "yyyy-MM-dd HH:mm",
        Locale.getDefault()
    )

    @SerializedName("employeeId")
    @Expose
    var employeeId: Int? = null

    @SerializedName("employeeName")
    @Expose
    var employeeName: String? = null

    @SerializedName("timeIn")
    @Expose
    var timeIn: String? = null

    @SerializedName("timeOut")
    @Expose
    var timeOut: String? = null

    @SerializedName("lunchIn")
    @Expose
    var lunchIn: String? = null

    @SerializedName("lunchOut")
    @Expose
    var lunchOut: String? = null

    @SerializedName("createdDate")
    @Expose
    var createdDate: String? = null

    @SerializedName("shiftTimeIn")
    @Expose
    var shiftTimeIn: String? = null

    @SerializedName("shiftTimeOut")
    @Expose
    var shiftTimeOut: String? = null

    override fun toString(): String {
        return "$employeeId $employeeName $timeIn $timeOut $shiftTimeIn $shiftTimeOut $createdDate"
    }

    fun getStatus(): Int {
        if(timeIn != null) {
            return if (timeOut != null) STATUS_CLOCKED_OUT else STATUS_PRESENT
        }
        val dateValue = getDateValue(shiftTimeIn)
        val diffInMillis: Long = Date().time - dateValue!!.time
        val diffInMin = TimeUnit.MILLISECONDS.toMinutes(diffInMillis)
        if(diffInMin > 0 && diffInMin > ABSENT_BUFFER_IN_MIN)
            return STATUS_ABSENT
        if( diffInMin > 0 && diffInMin > LATE_BUFFER_IN_MIN)
            return STATUS_LATE
        return STATUS_INITIAL
    }

    private fun getDateValue(value: String?): Date? {
        if(value == null) {
            return null
        }
        val currentDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        return formatter.parse("$currentDate $value") ?: formatterShort.parse("$currentDate $value")
    }


}