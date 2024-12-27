package com.brainyclockuser.ui.shifts

import com.brainyclockuser.base.model.BaseObjectModel
import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

class EmployeeHistoryDataModel  : EmployeeHistoryModel<EmployeeHistoryDataModel>() {

    @SerializedName("id")
    @Expose
    var id: Int? = null

    @SerializedName("company_id")
    @Expose
    var companyId: Int? = null

    @SerializedName("shift_id")
    @Expose
    var shiftId: Int? = null

    @SerializedName("employee_id")
    @Expose
    var employeeId: Int? = null

    @SerializedName("clock_in_time")
    @Expose
    var clockIn: String? = null

    @SerializedName("clock_out_time")
    @Expose
    var clockOut: String? = null

    @SerializedName("lunch_in_time")
    @Expose
    var lunchIn: String? = null

    @SerializedName("lunch_out_time")
    @Expose
    var lunchOut: String? = null

    @SerializedName("overtime")
    @Expose
    var overtime: String? = null

    @SerializedName("firstName")
    @Expose
    var firstName: String? = null

    @SerializedName("lastName")
    @Expose
    var lastName: String? = null

    @SerializedName("siteName")
    @Expose
    var siteName: String? = null

    @SerializedName("working_minutes")
    @Expose
    var workingMinutes: String? = null

    @SerializedName("totalHours")
    @Expose
    var totalHours: String? = null

    @SerializedName("created_at")
    @Expose
    var createdAt: String? = null

}