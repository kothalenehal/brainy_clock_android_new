package com.brainyclockuser.ui.clockin

import android.util.Log
import androidx.lifecycle.MutableLiveData
import com.brainyclockuser.BrainyClockUserApp
import com.brainyclockuser.base.BaseViewModel
import com.brainyclockuser.base.model.BaseModel
import com.brainyclockuser.ui.shifts.MarkEmployeeAttendanceModel
import com.brainyclockuser.ui.shifts.SiteEmployeeAttendanceModel
import com.brainyclockuser.utils.AppConstant
import com.google.gson.Gson
import dagger.hilt.android.lifecycle.HiltViewModel
import okhttp3.MultipartBody
import okhttp3.RequestBody
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import javax.inject.Inject

@HiltViewModel
class ShiftViewModel @Inject() constructor() :
    BaseViewModel() {
    val shifts: MutableLiveData<List<ShiftsModels>> = MutableLiveData()
    val attendance: MutableLiveData<BaseModel> = MutableLiveData()
    val requestOvertime: MutableLiveData<BaseModel> = MutableLiveData()
    val responseVerifyQRCode: MutableLiveData<BaseModel> = MutableLiveData()
    val errorMsg: MutableLiveData<String> = MutableLiveData()

    val postAttendanceModel: MutableLiveData<MarkEmployeeAttendanceModel> = MutableLiveData()

    fun callShiftApi() {
        loading.value = true
        apiManager.getShift(
            prefUtils.getIntData(
                BrainyClockUserApp.getAppComponent().provideApplication(),
                AppConstant.SharedPreferences.EMPLOYEE_ID
            ).toString()
        ).subscribe({
            loading.value = false
            Log.e("callShiftApi", it.message.toString())
            if (it.success)
                filterShift(it)
            else errorMsg.value = it.message

        }, {
            loading.value = false
            throwable.value = it
        })
    }

    private fun filterShift(shiftsModel: ShiftsModels) {
        val todayList = ArrayList<ShiftsModels>()
        val dayFormatter = SimpleDateFormat("EEE", Locale.getDefault())
        val dayName = dayFormatter.format(Date()).lowercase()
        if (shiftsModel.data != null) {
            val allShifts = shiftsModel.data!!
            for (shift in allShifts) {
                if(shift.isValid() && shift.isToday(allShifts)) {
                    for (day in shift.days!!) {
                        if (day.day!! == dayName) {
                            if (day.timeIn != null && day.timeOut != null) {
                                todayList.add(shift)
                            }
                        }
                    }
                }
            }
        }
        shifts.value = todayList
    }

    fun markAttendance(siteId: Int, time: String,type:String, eventId: Int, position: String? = null, distNumber: String? = null) {
        var attendanceModel = MarkEmployeeAttendanceModel()
        attendanceModel.siteId = siteId
        attendanceModel.employeeId = prefUtils.getIntData(
            BrainyClockUserApp.getAppComponent().provideApplication(),
            AppConstant.SharedPreferences.EMPLOYEE_ID
        )
        attendanceModel.time = time
        attendanceModel.type = type
        attendanceModel.eventId = eventId
        attendanceModel.position = position
        attendanceModel.markAttendanceBy = "" + attendanceModel.employeeId
        attendanceModel.deptNumber = distNumber
        attendanceModel.companyId = prefUtils.getIntData(
            BrainyClockUserApp.getAppComponent().provideApplication(),
            AppConstant.SharedPreferences.COMPANY_ID
        )
        Log.e("TAG>>>", "API Request Payload: " + Gson().toJson(attendanceModel))

        loading.value = true
        apiManager.markEmployeeAttendance(
            attendanceModel
        ).subscribe({
            loading.value = false
            print(it)
            Log.e("TAG>>>>", "markAttendance: " + it)
            if (it.success) {
                postAttendanceModel.value = it.data
            } else {
                errorMsg.value = it.message
            }
        }, {
            loading.value = false
            throwable.value = it
        })
    }

    fun markAttendance(
        id: Int,
        time: String,
        type: Int,
        email: String
    ) {
        Log.e("Type ", type.toString())
        val requestBody: RequestBody = MultipartBody.Builder()
            .setType(MultipartBody.FORM)
            .addFormDataPart("shiftId", id.toString())
            .addFormDataPart("time", time)
            .addFormDataPart(
                "type", when (type) {
                    AppConstant.CLOCK_IN -> "clockIn"
                    AppConstant.CLOCK_OUT -> "clockOut"
                    AppConstant.LUNCH_IN -> "lunchIn"
                    else -> "lunchOut"
                }
            )
//            .addFormDataPart("email", email)
            .build()

        loading.value = true
        apiManager.markAttendance(
            requestBody
        ).subscribe({
            loading.value = false
            attendance.value = it

        }, {
            loading.value = false
            throwable.value = it
        })
    }

    fun sendOvertimeRequest(id: Int) {
        val requestBody: RequestBody = MultipartBody.Builder()
            .setType(MultipartBody.FORM)
            .addFormDataPart("shiftId", id.toString())
            .addFormDataPart("currentShiftId", id.toString())
            .addFormDataPart("hours", "1")
            .build()

        loading.value = true
        apiManager.requestOvertime(
            requestBody
        ).subscribe({
            loading.value = false
            requestOvertime.value = it

        }, {
            loading.value = false
            throwable.value = it
        })
    }

    fun verifyQRCode(data: String) {
        apiManager.verifyQRCode(
            prefUtils.getIntData(
                BrainyClockUserApp.getAppComponent().provideApplication(),
                AppConstant.SharedPreferences.EMPLOYEE_ID
            ).toString(),
            data
        ).subscribe({
            loading.value = false
            responseVerifyQRCode.value = it

        }, {
            loading.value = false
            throwable.value = it
        })
    }

}