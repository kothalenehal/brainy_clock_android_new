package com.brainyclockuser.base.model

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

open class BaseObjectModel<T> : BaseModel() {
    @SerializedName("data")
    @Expose
    var data: T? = null
        private set

    fun setData(data: T) {
        this.data = data
    }
}