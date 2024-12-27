package com.brainyclockuser.base

import android.app.ProgressDialog
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.SystemClock
import android.util.Log
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import com.brainyclockuser.BrainyClockUserApp
import com.brainyclockuser.R
import com.brainyclockuser.api.ApiManager
import com.brainyclockuser.base.model.CustomError
import com.brainyclockuser.utils.ApiConstants
import com.brainyclockuser.utils.AppConstant
import com.brainyclockuser.utils.NetworkUtils
import com.brainyclockuser.utils.PrefUtils
import com.brainyclockuser.utils.permission.PermissionUtil
import com.google.android.material.snackbar.BaseTransientBottomBar
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.ResponseBody
import org.json.JSONObject
import retrofit2.HttpException
import java.net.ConnectException
import java.net.SocketTimeoutException
import java.util.*

@AndroidEntryPoint
abstract class BaseActivity : AppCompatActivity() {
    protected var mInputMethodManager: InputMethodManager? = null
    private var mProgressDialog: ProgressDialog? = null
    private var mLastClickTime: Long = 0

    var apiManager: ApiManager = BrainyClockUserApp.getAppComponent().provideApiManager()
    var prefUtils: PrefUtils = BrainyClockUserApp.getAppComponent().providePrefUtil()
    var networkUtils: NetworkUtils = BrainyClockUserApp.getAppComponent().provideNetworkUtils()
    var permissionUtil: PermissionUtil =
        BrainyClockUserApp.getAppComponent().providePermissionUtil()

    open fun handleError(throwable: Throwable) {
        if (throwable is HttpException) {
            val httpException: HttpException = throwable as HttpException
            handleHttpError(httpException)
        } else if (throwable is SocketTimeoutException || throwable is ConnectException) {
            handleNetworkError()
        } else if (throwable is CustomError) {
            handleCustomError(throwable)
        } else {
            throwable.printStackTrace()
            Log.e("TAG>>>", "handleError: " + throwable.message)
//            showError(getString(R.string.api_failure))
        }
    }

    private fun handleNetworkError() {
        //ConnectException only occurs when
        //either internet was not connected before calling api
        //or internet was turned off in the middle of outgoing request
        showError(getString(R.string.internet_not_available))
    }

    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<out String>, grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == requestCode) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission granted, you can proceed with BLE advertising
                //startBleAdvertising()
            } else {
                // Permission denied, handle this situation
            }
        }
    }

    private fun handleHttpError(exception: HttpException) {
        Log.e(
            "---",
            "handleHttpError called code :" + exception.code()
                .toString() + " message : " + exception.message()
        )
        val responseBody: ResponseBody? = exception.response()?.errorBody()
        val body = responseBody?.string()
        Log.e("---", "call: $body")
        if (body != null) {
            val jb = JSONObject(body)
            try {
                showError(jb.getString("msg"))
                return
            } catch (_: Exception) {
            }
        }
        when (exception.code()) {
            ApiConstants.ResponseCode.NOT_FOUND -> showError(getString(R.string.resource_not_found))
            ApiConstants.ResponseCode.CONFLICT -> showError(getString(R.string.server_conflict))
            else -> showError(getString(R.string.unknown_error_occurred))
        }
    }

    private fun handleCustomError(exception: CustomError) {
        Log.d("---", "handleCustomError() called with: exception = [" + exception.message + "]")

        showError(exception.error)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mProgressDialog = ProgressDialog(this)
        mProgressDialog!!.setMessage(getString(R.string.loading))
        mProgressDialog!!.setCancelable(false)
        Log.e("base app", "onCreate: baseapp activity")
        mInputMethodManager = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager?
//        if (isTokenExpired()) {
//            refreshToken()
//        }
    }

    private fun isTokenExpired(): Boolean {
        return if (prefUtils.getStringData(
                this, AppConstant.SharedPreferences.ACCESS_TOKEN
            ) != null
        ) {
            val tokenExpiredOn: Long =
                prefUtils.getLongData(this, AppConstant.SharedPreferences.TOKEN_EXPIRE_ON)
            Calendar.getInstance().timeInMillis > tokenExpiredOn
        } else false
    }

    private fun refreshToken() {
        val requestBody: RequestBody =
            MultipartBody.Builder().setType(MultipartBody.FORM).addFormDataPart(
                    ApiConstants.params.EMAIL,
                    prefUtils.getStringData(this, AppConstant.SharedPreferences.EMAIL) ?: ""
                ).build()

        apiManager.refreshToken(
            prefUtils.getStringData(this, AppConstant.SharedPreferences.REFRESH_TOKEN) ?: "",
            requestBody
        ).subscribe({
            if (it.success) {
                prefUtils.saveData(
                    this, AppConstant.SharedPreferences.ACCESS_TOKEN, it.accessToken
                )
                val calendar = Calendar.getInstance()
                calendar.add(Calendar.SECOND, 3600)
                prefUtils.saveData(
                    this, AppConstant.SharedPreferences.TOKEN_EXPIRE_ON, calendar.timeInMillis
                )
            }
        }, {
            //handleError(it)
        })
    }

    fun showLoader() {
        if (!mProgressDialog!!.isShowing) {
            mProgressDialog!!.show()
        }
    }

    fun hideLoader() {
        if (mProgressDialog!!.isShowing) {
            mProgressDialog!!.dismiss()
        }
    }

    fun showAlert(msg: String?) {
        if (msg == null) return
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
    }

    fun showError(msg: String?) {
        if (msg == null) return
        Snackbar.make(findViewById(android.R.id.content), msg, BaseTransientBottomBar.LENGTH_SHORT)
            .show()
    }

    fun hideKeyBoard(view: View?) {
        if (view != null) {
            mInputMethodManager!!.hideSoftInputFromWindow(view.windowToken, 0)
        }
    }

    fun showKeyBoard(view: View?) {
        if (view != null) {
            mInputMethodManager!!.showSoftInput(view, 0)
        }
    }

    /**
     * it will return true if consecutive click occurs within [AppConstant.Delays.MIN_TIME_BETWEEN_CLICKS]
     *
     * @return true indicating do not allow any click, false otherwise
     */
    val isClickDisabled: Boolean
        get() = if (SystemClock.elapsedRealtime() - mLastClickTime < AppConstant.Delays.MIN_TIME_BETWEEN_CLICKS) true else {
            mLastClickTime = SystemClock.elapsedRealtime()
            false
        }

    /**
     * to add fragment in container
     * tag will be same as class name of fragment
     *
     * @param containerId    id of fragment container
     * @param addToBackStack should be added to backstack?
     */
    fun addFragment(fragment: Fragment, containerId: Int, addToBackStack: Boolean) {
        val fragmentManager: FragmentManager = getSupportFragmentManager()
        val fragmentTransaction = fragmentManager.beginTransaction()
        fragmentTransaction.add(containerId, fragment)
        if (addToBackStack) {
            fragmentTransaction.addToBackStack(fragment.javaClass.simpleName)
        }
        fragmentTransaction.commit()
    }

    /**
     * to replace fragment in container
     * tag will be same as class name of fragment
     *
     * @param containerId        id of fragment container
     * @param isAddedToBackStack should be added to backstack?
     */
    fun replaceFragment(fragment: Fragment, containerId: Int, isAddedToBackStack: Boolean) {
        val fragmentManager: FragmentManager = getSupportFragmentManager()
        val fragmentTransaction = fragmentManager.beginTransaction()
        fragmentTransaction.replace(containerId, fragment)
        if (isAddedToBackStack) {
            fragmentTransaction.addToBackStack(fragment.javaClass.simpleName)
        }
        fragmentTransaction.commit()
    }
}