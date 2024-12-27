package com.brainyclockuser.ui.auth

import android.text.TextUtils
import android.util.Patterns
import androidx.lifecycle.MutableLiveData
import com.brainyclockuser.base.BaseViewModel
import com.brainyclockuser.base.model.BaseModel
import com.brainyclockuser.ui.auth.model.UserModel
import com.brainyclockuser.utils.ApiConstants
import dagger.hilt.android.lifecycle.HiltViewModel
import okhttp3.MultipartBody
import okhttp3.RequestBody
import java.util.regex.Matcher
import java.util.regex.Pattern
import javax.inject.Inject


@HiltViewModel
class AuthenticationViewModel @Inject() constructor() :
    BaseViewModel() {
    val validateLivedata: MutableLiveData<Any> = MutableLiveData()
    val forgotPassword: MutableLiveData<BaseModel> = MutableLiveData()
    val resetPassword: MutableLiveData<BaseModel> = MutableLiveData()
    val userModelLivedata: MutableLiveData<UserModel> = MutableLiveData()

    var strEmail: String = ""
    var strPassword: String = ""
    var strFirstName: String = ""
    var strLastName: String = ""
    var strReEnterPassword: String = ""
    var strCode: String = ""
    var strCompanyId: String = ""
    var strEmpId: String = ""

    fun onSignIn() {
        if (isValid()) {
            validateLivedata.value = Constant.IS_VALID
            validateLivedata.value = ""
            callLoginApi()
        }
    }

    fun onSignUp() {
        if (signUpValidation()) {
            validateLivedata.value = Constant.IS_VALID
            validateLivedata.value = ""
            callSignupApi()
        }
    }


    private fun isValid(): Boolean {
        if (TextUtils.isEmpty(strEmail.trim())) {
            validateLivedata.value = Constant.IS_EMAIL_EMPTY
            return false
        } else if (!Patterns.EMAIL_ADDRESS.matcher(strEmail.trim()).matches()) {
            validateLivedata.value = Constant.IS_EMAIL_INVALID
            return false
        } else if (TextUtils.isEmpty(strPassword.trim())) {
            validateLivedata.value = Constant.IS_PASSWORD_EMPTY
            return false
        } else if (strPassword.trim().length < 8) {
            validateLivedata.value = Constant.IS_PASSWORD_INVALID
            return false
        }
        return true
    }

    private fun signUpValidation(): Boolean {
        if (TextUtils.isEmpty(strFirstName.trim())) {
            validateLivedata.value = Constant.IS_FIRST_NAME_EMPTY
            return false
        } else if (TextUtils.isEmpty(strLastName.trim())) {
            validateLivedata.value = Constant.IS_LAST_NAME_EMPTY
            return false
        } else if (TextUtils.isEmpty(strEmail.trim())) {
            validateLivedata.value = Constant.IS_EMAIL_EMPTY
            return false
        } else if (!Patterns.EMAIL_ADDRESS.matcher(strEmail.trim()).matches()) {
            validateLivedata.value = Constant.IS_EMAIL_INVALID
            return false
        } else if (TextUtils.isEmpty(strCompanyId.trim())) {
            validateLivedata.value = Constant.IS_COMPANY_ID_EMPTY
            return false
        } else if (TextUtils.isEmpty(strEmpId.trim())) {
            validateLivedata.value = Constant.IS_EMP_ID_EMPTY
            return false
        } else if (TextUtils.isEmpty(strPassword.trim())) {
            validateLivedata.value = Constant.IS_PASSWORD_EMPTY
            return false
        } else if (strPassword.trim().length < 8) {
            validateLivedata.value = Constant.IS_PASSWORD_INVALID
            return false
        } else if (!isValidPassword(strPassword)) {
            validateLivedata.value = Constant.IS_PASSWORD_INVALID
            return false
        } else if (TextUtils.isEmpty(strReEnterPassword.trim())) {
            validateLivedata.value = Constant.IS_RE_PASS_EMPTY
            return false
        } else if (strReEnterPassword != strPassword) {
            validateLivedata.value = Constant.IS_PASS_NOT_MATCH
            return false
        }
        return true
    }


    private fun callSignupApi() {

        val requestBody: RequestBody = MultipartBody.Builder()
            .setType(MultipartBody.FORM)
            .addFormDataPart(ApiConstants.params.EMAIL, strEmail)
            .addFormDataPart(ApiConstants.params.PASSWORD, strPassword)
            .addFormDataPart(ApiConstants.params.FIRSTNAME, strFirstName)
            .addFormDataPart(ApiConstants.params.LASTNAME, strLastName)
            .addFormDataPart(ApiConstants.params.EMPLOYEE_ID, strEmpId)
            .addFormDataPart(ApiConstants.params.COMPANY_ID, strCompanyId)
            .build()

        loading.value = true
        apiManager.signUp(requestBody).subscribe({
            loading.value = false
            userModelLivedata.value = it

        }, {
            loading.value = false
            throwable.value = it
        })
    }

    private fun callLoginApi() {
        val requestBody: RequestBody = MultipartBody.Builder()
            .setType(MultipartBody.FORM)
            .addFormDataPart(ApiConstants.params.EMAIL, strEmail)
            .addFormDataPart(ApiConstants.params.PASSWORD, strPassword)
            .build()

        loading.value = true
        apiManager.login(requestBody).subscribe({
            loading.value = false
            userModelLivedata.value = it

        }, {
            loading.value = false
            throwable.value = it
        })
    }

    private fun isValidPassword(password: String): Boolean {
        val pattern: Pattern
        val passwordPattern = "^(?=.*[0-9])(?=.*[A-Z])(?=.*[@#$%^&+=!])(?=\\S+$).{4,}$"
        pattern = Pattern.compile(passwordPattern)
        val matcher: Matcher = pattern.matcher(password)
        return matcher.matches()
    }

    fun forgotPassword() {
        if (isValidEmail()) {
            validateLivedata.value = Constant.IS_VALID
            validateLivedata.value = ""
            callForgotPasswordApi()
        }
    }

    private fun callForgotPasswordApi() {
        val requestBody: RequestBody = MultipartBody.Builder()
            .setType(MultipartBody.FORM)
            .addFormDataPart(ApiConstants.params.EMAIL, strEmail)
            .build()

        loading.value = true
        apiManager.forgotPassword(requestBody).subscribe({
            loading.value = false
            forgotPassword.value = it

        }, {
            loading.value = false
            throwable.value = it
        })
    }

    fun resetPassword() {
        if (isValidCodeAndPwd()) {
            validateLivedata.value = ""
            callResetPasswordApi()
        }
    }

    private fun callResetPasswordApi() {

        val requestBody: RequestBody = MultipartBody.Builder()
            .setType(MultipartBody.FORM)
            .addFormDataPart(ApiConstants.params.EMAIL, strEmail)
            .addFormDataPart(ApiConstants.params.PASSWORD, strPassword)
            .addFormDataPart("confirmationCode", strCode)
            .build()

        loading.value = true
        apiManager.resetPassword(requestBody).subscribe({
            loading.value = false
            resetPassword.value = it

        }, {
            loading.value = false
            throwable.value = it
        })
    }

    private fun isValidEmail(): Boolean {
        if (TextUtils.isEmpty(strEmail.trim())) {
            validateLivedata.value = Constant.IS_EMAIL_EMPTY
            return false
        } else if (!Patterns.EMAIL_ADDRESS.matcher(strEmail.trim()).matches()) {
            validateLivedata.value = Constant.IS_EMAIL_INVALID
            return false
        }
        return true
    }

    private fun isValidCodeAndPwd(): Boolean {
        if (TextUtils.isEmpty(strCode.trim())) {
            validateLivedata.value = Constant.IS_CODE_EMPTY
            return false
        } else if (TextUtils.isEmpty(strPassword.trim())) {
            validateLivedata.value = Constant.IS_PASSWORD_EMPTY
            return false
        } else if (strPassword.trim().length < 8) {
            validateLivedata.value = Constant.IS_PASSWORD_INVALID
            return false
        } else if (!isValidPassword(strPassword)) {
            validateLivedata.value = Constant.IS_PASSWORD_INVALID
            return false
        }
        return true
    }
}