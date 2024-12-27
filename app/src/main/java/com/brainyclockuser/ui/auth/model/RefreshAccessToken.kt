package com.brainyclockuser.ui.auth.model

import com.brainyclockuser.base.model.BaseModel
import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

class RefreshAccessToken: BaseModel() {
    @SerializedName("access_token")
    @Expose
    var accessToken: String? = null
}