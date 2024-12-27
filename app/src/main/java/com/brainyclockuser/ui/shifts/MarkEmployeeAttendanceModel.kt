package com.brainyclockuser.ui.shifts

import com.brainyclockuser.base.model.BaseObjectModel
import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import java.time.LocalTime
import java.time.format.DateTimeFormatter

/*data class MarkEmployeeAttendanceModel(

    @SerializedName("Site_id")
    @Expose
    var siteId: Int? = null,

    @SerializedName("employee_id")
    @Expose
    var employeeId: Int? = null,

    @SerializedName("time")
    @Expose
    var time: String? = null,

    @SerializedName("Type")
    @Expose
    var type: String? = null,

    @SerializedName("eventId")
    @Expose
    var eventId: Int? = null,

    @SerializedName("markAttendanceBy")
    @Expose
    var markAttendanceBy: String? = null,

    @SerializedName("company_id")
    @Expose
    var companyId: Int? = null
)*/

class MarkEmployeeAttendanceModel : BaseObjectModel<MarkEmployeeAttendanceModel>() {

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
    var markAttendanceBy: String? = null

    @SerializedName("deptNumber")
    @Expose
    var deptNumber: String? = null

    @SerializedName("company_id")
    @Expose
    var companyId: Int? = null

    @SerializedName("position")
    @Expose
    var position: String? = null

}