package com.brainyclockuser.ui.clockin

import com.brainyclockuser.base.model.BaseArrayModel
import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

class ShiftsModel : BaseArrayModel<ShiftsModel>() {

    @SerializedName("shift_id")
    @Expose
    var shiftId: Int? = null

    @SerializedName("name")
    @Expose
    var shiftName: String? = null

    @SerializedName("days")
    @Expose
    var days: String? = null

    @SerializedName("current_shift_id")
    @Expose
    var currentShiftId: Int? = null

    @SerializedName("clock_in_time")
    @Expose
        var shiftClockInTime: String? = null

    @SerializedName("clock_out_time")
    @Expose
    var shiftClockOutTime: String? = null

    @SerializedName("emp_clock_in")
    @Expose
    var empClockIn: String? = null

    @SerializedName("emp_clock_out")
    @Expose
    var empClockOut: String? = null

    @SerializedName("emp_lunch_in_time")
    @Expose
    var empLunchIn: String? = null

    @SerializedName("emp_lunch_out_time")
    @Expose
    var empLunchOut: String? = null

    @SerializedName("overtime")
    @Expose
    var overtime: String? = null

    @SerializedName("overtime_request_pending")
    @Expose
    var overtimeRequestPending: String? = null

    @SerializedName("status")
    @Expose
    var status: String? = null

    var customStatus: Int = 0

    override fun toString(): String {
        return "$shiftName $days $status $customStatus $empClockIn $empClockOut $shiftClockInTime $shiftClockOutTime"
    }

}