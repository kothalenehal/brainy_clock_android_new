package com.brainyclockuser.ui.shifts

import com.brainyclockuser.base.model.BaseModel
import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

open class EmployeeHistoryModel<T> : BaseModel() {
    @SerializedName("data")
    @Expose
    var data: List<T>? = null
        private set

    fun setData(data: List<T>) {
        this.data = data
    }

    @SerializedName("totalWorkingHours")
    @Expose
    var totalWorkingHours: String? = null

}