package com.brainyclockuser.base.model

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

open class BaseModel {
    @SerializedName("success")
    @Expose
    var success = false

    @SerializedName("msg", alternate = ["message"])
    @Expose
    var message: String? = null
}