package com.brainyclockuser.ui.shifts

import AllStaffShift
import android.util.Log
import androidx.lifecycle.MutableLiveData
import com.brainyclockuser.BrainyClockUserApp
import com.brainyclockuser.base.BaseViewModel
import com.brainyclockuser.base.model.BaseModel
import com.brainyclockuser.ui.auth.model.UserModel
import com.brainyclockuser.ui.clockin.ShiftsModel
import com.brainyclockuser.utils.ApiConstants
import com.brainyclockuser.utils.AppConstant
import com.google.gson.Gson
import dagger.hilt.android.lifecycle.HiltViewModel
import okhttp3.MultipartBody
import okhttp3.RequestBody
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject
import kotlin.collections.ArrayList
import kotlin.collections.List


@HiltViewModel
class SiteEmployeeViewModel @Inject() constructor() :
    BaseViewModel() {
    val allEmployees: MutableLiveData<List<AllStaffShift>> = MutableLiveData()
    val postAttendanceModel: MutableLiveData<SiteEmployeeAttendanceModel> = MutableLiveData()
    val errorMsg: MutableLiveData<String> = MutableLiveData()

    fun callSiteEmployeeApi() {
        loading.value = true
        val shiftID = prefUtils.getIntData(
            BrainyClockUserApp.getAppComponent().provideApplication(),
            AppConstant.SharedPreferences.SHIFT_ID
        ).toString()
        apiManager.getSiteEmployees(
            shiftID
        ).subscribe({
            loading.value = false
            Log.e("TAG>>>", "callSiteEmployeeApi: " + Gson().toJson(it))

            print(it)
            if (it.success) {
                setEmployeeData(it)
                if(it.data!!.isEmpty()) {
                    errorMsg.value = "No employees found"
                }
            } else {
                errorMsg.value = it.message
            }

        }, {
            loading.value = false
            throwable.value = it
        })
    }

    private fun setEmployeeData(shiftsModel: AllStaffShift) {
        allEmployees.value = shiftsModel.data
    }

    fun markAttendance(attendanceModel: SiteEmployeeAttendanceModel) {
        attendanceModel.companyId = prefUtils.getIntData(
            BrainyClockUserApp.getAppComponent().provideApplication(),
            AppConstant.SharedPreferences.COMPANY_ID
        )
        attendanceModel.markAttendanceBy = prefUtils.getIntData(
            BrainyClockUserApp.getAppComponent().provideApplication(),
            AppConstant.SharedPreferences.EMPLOYEE_ID
        )
        println(attendanceModel)
        loading.value = true
        apiManager.markSiteEmployeeAttendance(
            attendanceModel
        ).subscribe({
            loading.value = false
            print(it)
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

}