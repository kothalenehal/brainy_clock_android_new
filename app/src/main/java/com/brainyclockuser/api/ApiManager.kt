package com.brainyclockuser.api

import AllStaffShift
import android.util.Log
import com.brainyclockuser.BrainyClockUserApp
import com.brainyclockuser.base.model.BaseModel
import com.brainyclockuser.base.model.CustomError
import com.brainyclockuser.ui.auth.model.RefreshAccessToken
import com.brainyclockuser.ui.auth.model.UserModel
import com.brainyclockuser.ui.clockin.ShiftsModel
import com.brainyclockuser.ui.clockin.ShiftsModels
import com.brainyclockuser.ui.shifts.EmployeeHistoryDataModel
import com.brainyclockuser.ui.shifts.EmployeeHistoryModel
import com.brainyclockuser.ui.shifts.MarkEmployeeAttendanceModel
import com.brainyclockuser.ui.shifts.SiteEmployeeAttendanceModel
import com.brainyclockuser.ui.shifts.SiteEmployeeModel
import com.brainyclockuser.utils.AppConstant
import com.brainyclockuser.utils.NetworkUtils
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.annotations.NonNull
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.core.ObservableTransformer
import io.reactivex.rxjava3.functions.Function
import io.reactivex.rxjava3.functions.Supplier
import io.reactivex.rxjava3.schedulers.Schedulers
import okhttp3.RequestBody
import java.net.ConnectException
import javax.inject.Inject
import javax.inject.Singleton


@Singleton
class ApiManager @Inject constructor(
    private val apiInterface: ApiInterface,
) {
    private val mNetworkUtils: NetworkUtils =
        BrainyClockUserApp.getAppComponent().provideNetworkUtils()

    private fun <T : Any> call(modelObservable: Observable<T>): @NonNull Observable<T> {
        return modelObservable
            .startWith(Observable.defer(Supplier defer@{
                //before calling each api, network connection is checked.
                if (!mNetworkUtils.isNetworkConnected()) {
                    //if network is not available, it will return error observable with ConnectException.
                    return@defer Observable.error(ConnectException("Device is not connected to network"))
                } else {
                    //if it is available, it will return empty observable. Empty observable just emits onCompleted() immediately
                    return@defer Observable.empty()
                }
            }))
            .flatMap<T>(Function flatMap@{ response: T ->
                if (response is BaseModel) {
                    val baseResponse: BaseModel = response as BaseModel
                    if (!baseResponse.success) {
                        val customApiError =
                            baseResponse.message?.let { CustomError(it) }
                        return@flatMap Observable.error(customApiError!!)
                    }
                    return@flatMap Observable.just(response)
                }
                Observable.just(response)
            })
            .doOnNext { response: T ->
                //logging response on success
                //you can change to to something else
                //for example, if all your apis returns error codes in success, then you can throw custom exception here
                if (AppConstant.IS_DEBUGGABLE) {
                    Log.e("---", "Response :\n$response")
                }
            }
            .doOnError { throwable: Throwable ->
                //printing stack trace on error
                if (AppConstant.IS_DEBUGGABLE) {
                    throwable.printStackTrace()
                    Log.e("---", "throwable :\n${throwable.message}")
                }
            }
    }

    private fun <T : Any> applySchedulers(): ObservableTransformer<T, T> {
        return ObservableTransformer<T, T> { observable: Observable<T> ->
            observable.subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
        }
    }

    fun signUp(
        requestBody: RequestBody,
    ): Observable<UserModel> {
        return call(apiInterface.signup(requestBody)).compose(
            applySchedulers()
        )
    }

    fun login(
        requestBody: RequestBody,
    ): Observable<UserModel> {
        return call(apiInterface.login(requestBody)).compose(
            applySchedulers()
        )
    }

    fun logout(
    ): Observable<BaseModel> {
        return call(apiInterface.logout()).compose(
            applySchedulers()
        )
    }

    fun changePassword(
        requestBody: RequestBody,
    ): Observable<BaseModel> {
        return call(apiInterface.changePassword(requestBody)).compose(
            applySchedulers()
        )
    }

    fun forgotPassword(
        requestBody: RequestBody,
    ): Observable<BaseModel> {
        return call(apiInterface.forgotPassword(requestBody)).compose(
            applySchedulers()
        )
    }

    fun resetPassword(
        requestBody: RequestBody,
    ): Observable<BaseModel> {
        return call(apiInterface.resetPassword(requestBody)).compose(
            applySchedulers()
        )
    }

    fun refreshToken(
        refreshToken: String,
        requestBody: RequestBody,
    ): Observable<RefreshAccessToken> {
        return call(apiInterface.refreshToken(refreshToken, requestBody)).compose(
            applySchedulers()
        )
    }

    fun getShift(
        empId: String
    ): Observable<ShiftsModels> {
        return call(apiInterface.getShift(empId)).compose(
            applySchedulers()
        )
    }

    fun markEmployeeAttendance(
        attendanceModel: MarkEmployeeAttendanceModel
    ): Observable<MarkEmployeeAttendanceModel> {
        return call(apiInterface.markEmployeeAttendance(attendanceModel)).compose(
            applySchedulers()
        )
    }

    fun markAttendance(
        requestBody: RequestBody,
        ): Observable<BaseModel> {
        return call(apiInterface.markAttendance(requestBody)).compose(
            applySchedulers()
        )
    }

    fun requestOvertime(
        requestBody: RequestBody,
        ): Observable<BaseModel> {
        return call(apiInterface.requestOvertime(requestBody)).compose(
            applySchedulers()
        )
    }

    fun verifyQRCode(empId : String, data:String):Observable<BaseModel>{
        return call(apiInterface.getVerifiedQR(empId, data)).compose(applySchedulers())
    }

    fun getSiteEmployees(
        empId: String
    ): Observable<AllStaffShift> {
        return call(apiInterface.getSiteEmployees(empId)).compose(
            applySchedulers()
        )
    }

    fun markSiteEmployeeAttendance(
        attendanceModel: SiteEmployeeAttendanceModel
    ): Observable<SiteEmployeeAttendanceModel> {
        return call(apiInterface.markSiteEmployeeAttendance(attendanceModel)).compose(
            applySchedulers()
        )
    }

    fun getEmployeeHistory(
        empId: String
    ): Observable<EmployeeHistoryModel<EmployeeHistoryDataModel>> {
        return call(apiInterface.getEmployeeHistory(empId)).compose(
            applySchedulers()
        )
    }

}