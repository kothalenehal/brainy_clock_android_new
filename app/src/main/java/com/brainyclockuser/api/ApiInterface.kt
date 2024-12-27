package com.brainyclockuser.api

import AllStaffShift
import com.brainyclockuser.base.model.BaseModel
import com.brainyclockuser.ui.auth.model.RefreshAccessToken
import com.brainyclockuser.ui.auth.model.UserModel
import com.brainyclockuser.ui.clockin.ShiftsModel
import com.brainyclockuser.ui.clockin.ShiftsModels
import com.brainyclockuser.ui.shifts.EmployeeHistoryDataModel
import com.brainyclockuser.ui.shifts.EmployeeHistoryModel
import com.brainyclockuser.ui.shifts.MarkEmployeeAttendanceModel
import com.brainyclockuser.ui.shifts.SiteEmployeeAttendanceModel
import com.brainyclockuser.ui.shifts.SiteEmployeeModel
import com.brainyclockuser.utils.ApiConstants
import io.reactivex.rxjava3.core.Observable
import okhttp3.RequestBody
import retrofit2.http.*
import javax.inject.Singleton

interface ApiInterface {

    @POST(ApiConstants.EndPoints.SIGNUP)
    fun signup(
        @Body requestBody: RequestBody
    ): Observable<UserModel>

    @POST(ApiConstants.EndPoints.LOGIN)
    fun login(@Body requestBody: RequestBody): Observable<UserModel>

    @GET(ApiConstants.EndPoints.LOGOUT)
    fun logout(): Observable<BaseModel>

    @POST(ApiConstants.EndPoints.CHANGE_PASSWORD)
    fun changePassword(@Body requestBody: RequestBody): Observable<BaseModel>

    @POST(ApiConstants.EndPoints.FORGOT_PASSWORD)
    fun forgotPassword(@Body requestBody: RequestBody): Observable<BaseModel>

    @POST(ApiConstants.EndPoints.VERIFY_FORGOT_PASSWORD)
    fun resetPassword(@Body requestBody: RequestBody): Observable<BaseModel>

    @POST(ApiConstants.EndPoints.REFRESH_TOKEN)
    fun refreshToken(
        @Header("refresh-token") refreshToken: String,
        @Body requestBody: RequestBody
    ): Observable<RefreshAccessToken>

    @GET("${ApiConstants.EndPoints.GET_EMPLOYEE_SHIFTS}/{empId}")
    fun getShifts(@Path("empId") empId: String): Observable<ShiftsModel>

    @GET("${ApiConstants.EndPoints.GET_EMPLOYEE_SHIFTS}/{empId}")
    fun getShift(@Path("empId") empId: String): Observable<ShiftsModels>

    @POST(ApiConstants.EndPoints.ATTENDANCE_MARK)
    fun markAttendance(@Body requestBody: RequestBody): Observable<BaseModel>

    @POST(ApiConstants.EndPoints.REQUEST_OVERTIME)
    fun requestOvertime(@Body requestBody: RequestBody): Observable<BaseModel>

    @GET("${ApiConstants.EndPoints.VERIFYQR_CODE}")
    fun getVerifiedQR(@Query("employeeId") empId: String,  @Query("location") data: String,): Observable<BaseModel>

    @GET("${ApiConstants.EndPoints.GET_SITE_EMPLOYEES}/{empId}")
    fun getSiteEmployees(@Path("empId") empId: String): Observable<AllStaffShift>

    @POST("${ApiConstants.EndPoints.MARK_SITE_EMPLOYEE_ATTENDANCE}")
    fun markSiteEmployeeAttendance(@Body attendanceModel: SiteEmployeeAttendanceModel): Observable<SiteEmployeeAttendanceModel>

    @POST("${ApiConstants.EndPoints.MARK_SITE_EMPLOYEE_ATTENDANCE}")
    fun markEmployeeAttendance(@Body attendanceModel: MarkEmployeeAttendanceModel): Observable<MarkEmployeeAttendanceModel>

    @GET("${ApiConstants.EndPoints.GET_EMPLOYEE_HISTORY}/{empId}")
    fun getEmployeeHistory(@Path("empId") empId: String): Observable<EmployeeHistoryModel<EmployeeHistoryDataModel>>

}
