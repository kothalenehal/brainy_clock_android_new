package com.brainyclockuser.ui.settings

import androidx.lifecycle.MutableLiveData
import com.brainyclockuser.base.BaseViewModel
import com.brainyclockuser.base.model.BaseModel
import dagger.hilt.android.lifecycle.HiltViewModel
import okhttp3.MultipartBody
import okhttp3.RequestBody
import java.util.regex.Matcher
import java.util.regex.Pattern
import javax.inject.Inject


@HiltViewModel
class SettingsViewModel @Inject() constructor() :
    BaseViewModel() {
    val logout: MutableLiveData<BaseModel> = MutableLiveData()
    val changePassword: MutableLiveData<BaseModel> = MutableLiveData()
    val updateNotification: MutableLiveData<BaseModel> = MutableLiveData()


    fun callLogoutApi() {
        loading.value = true
        apiManager.logout().subscribe({
            loading.value = false
            logout.value = it

        }, {
            loading.value = false
            throwable.value = it
        })
    }

    fun callChangePasswordApi(oldPasswrod: String, newPassword: String) {

        val requestBody: RequestBody = MultipartBody.Builder()
            .setType(MultipartBody.FORM)
            .addFormDataPart("currentPassword", oldPasswrod)
            .addFormDataPart("newPassword", newPassword)
            .build()

        loading.value = true
        apiManager.changePassword(requestBody).subscribe({
            loading.value = false
            changePassword.value = it

        }, {
            loading.value = false
            throwable.value = it
        })
    }
}