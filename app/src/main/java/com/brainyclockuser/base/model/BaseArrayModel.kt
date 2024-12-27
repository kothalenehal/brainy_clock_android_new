package com.brainyclockuser.base.model

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

open class BaseArrayModel<T> : BaseModel() {
    @SerializedName("data")
    @Expose
    var data: List<T>? = null
        private set

    fun setData(data: List<T>) {
        this.data = data
    }
}