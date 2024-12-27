package com.brainyclockuser.ui.auth.model

import com.brainyclockuser.base.model.BaseObjectModel
import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

class UserModel : BaseObjectModel<UserModel>() {
    @SerializedName("employee_id")
    @Expose
    var employeeId: Int? = null

    @SerializedName("name")
    @Expose
    var name: String? = null

    @SerializedName("email")
    @Expose
    var email: String? = null

    @SerializedName("company_id")
    @Expose
    var companyId: Int? = null

    @SerializedName("access_token")
    @Expose
    var accessToken: String? = null

    @SerializedName("refresh_token")
    @Expose
    var refreshToken: String? = null

    @SerializedName("department_name")
    @Expose
    var departmentName: String? = null

    @SerializedName("department_id")
    var departmentId: String? = null

    @SerializedName("type")
    var type: Int? = null

    @SerializedName("locations")
    var locationModel: List<LocationModel>? = null
}

class LocationModel: BaseObjectModel<LocationModel>() {
    @SerializedName("id")
    var Id: Int? = null

    @SerializedName("company_id")
    var CompanyId: Int? = null

    @SerializedName("location_name")
    var locationName: String? = null

    @SerializedName("address")
    var address: String? = null

    @SerializedName("pincode")
    var pincode: String? = null

    @SerializedName("latitude")
    var latitude: String? = null

    @SerializedName("longitude")
    var longitude: String? = null

    @SerializedName("state")
    var state: String? = null

    @SerializedName("city")
    var city: String? = null

    @SerializedName("country")
    var country: String? = null
    @SerializedName("geofence_radius")
    var geofence_radius: Int = 100
}