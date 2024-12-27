package com.brainyclockuser.ui.shifts

import com.brainyclockuser.base.model.BaseArrayModel
import com.brainyclockuser.base.model.BaseObjectModel
import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

class SiteEmployeeAttendanceModel  : BaseObjectModel<SiteEmployeeAttendanceModel>() {

    @SerializedName("site_id")
    @Expose
    var siteId: Int? = null

    @SerializedName("employee_id")
    @Expose
    var employeeId: Int? = null

    @SerializedName("time")
    @Expose
    var time: String? = null

    @SerializedName("type")
    @Expose
    var type: String? = null

    @SerializedName("eventId")
    @Expose
    var eventId: Int? = null

    @SerializedName("markAttendanceBy")
    @Expose
    var markAttendanceBy: Int? = null

    @SerializedName("company_id")
    @Expose
    var companyId: Int? = null

    @SerializedName("deptNumber")
    @Expose
    var deptNumber: String? = null

    @SerializedName("position")
    @Expose
    var position: String? = null

    var arrayIndex: Int = -1;

    @SerializedName("clock_in_time")
    @Expose
    var postClockInTime: String? = null

    @SerializedName("clock_out_time")
    @Expose
    var postClockOutTime: String? = null

}