package com.brainyclockuser.api

import com.brainyclockuser.BrainyClockUserApp
import com.brainyclockuser.utils.ApiConstants
import com.brainyclockuser.utils.AppConstant
import com.brainyclockuser.utils.PrefUtils
import okhttp3.Interceptor
import okhttp3.Request
import okhttp3.Response
import java.io.IOException

/**
 * Interceptor for Retrofit to add auth key to header
 */
class RetrofitInterceptor : Interceptor {
    @Throws(IOException::class)
    override fun intercept(chain: Interceptor.Chain): Response {
        var request: Request = chain.request()
        val token = PrefUtils().getStringData(
            BrainyClockUserApp.getAppComponent().provideApplication(),
            AppConstant.SharedPreferences.ACCESS_TOKEN
        )
        request = request.newBuilder()
            .addHeader(ApiConstants.params.AUTHORIZATION, "Bearer $token")
            .addHeader("Connection", "keep-alive")
            .addHeader("Accept", "application/json")
            .addHeader("Accept-Encoding", "gzip, deflate, br")
            .addHeader("Accept", "*/*")
            .build()
        return chain.proceed(request)
    }
}