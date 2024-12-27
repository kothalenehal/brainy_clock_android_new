package com.brainyclockuser.di.module

import android.util.Log
import com.brainyclockuser.BrainyClockUserApp
import com.brainyclockuser.api.RetrofitInterceptor
import com.brainyclockuser.utils.ApiConstants
import com.brainyclockuser.utils.AppConstant
import com.google.gson.FieldNamingPolicy
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.Authenticator
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.Route
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.adapter.rxjava3.RxJava3CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Singleton
import com.brainyclockuser.utils.PrefUtils
import okhttp3.MultipartBody
import okhttp3.RequestBody
import org.json.JSONObject
import java.io.IOException
import java.util.concurrent.TimeUnit
import javax.inject.Named

/**
 *
 *
 */
@Module
@InstallIn(SingletonComponent::class)
class NetworkModule() {
    /*@Provides
    @Singleton
    Gson provideGson() {
        return new GsonBuilder().serializeNulls().registerTypeAdapterFactory(AutoValueGsonFactory.create()).create();
    }*/
    @Provides
    @Singleton
    fun provideOkHttpClient(@Named("TokenCallback") tokenCallback: ITokenAuthenticatorCallback): OkHttpClient {
//        return new OkHttpClient.Builder().addInterceptor(new RetrofitInterceptor()).build();
        val interceptor = HttpLoggingInterceptor()
        interceptor.setLevel(HttpLoggingInterceptor.Level.BODY)
        return OkHttpClient.Builder()
            .authenticator(TokenAuthenticator(tokenCallback))
            .addInterceptor(RetrofitInterceptor())
            .connectTimeout(100, TimeUnit.SECONDS)
            .writeTimeout(100, TimeUnit.SECONDS)
            .readTimeout(100, TimeUnit.SECONDS)
            .addInterceptor(interceptor)
            .build()
    }

    @Provides
    @Singleton
    fun provideGson(): Gson {
        val gsonBuilder = GsonBuilder()
        gsonBuilder.setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
        return gsonBuilder.create()
    }

    @Provides
    @Singleton
    fun provideGsonConverterFactory(gson: Gson): GsonConverterFactory {
        return GsonConverterFactory.create(gson)
    }

    @Provides
    @Singleton
    fun provideRetrofit(okHttpClient: OkHttpClient, factory: GsonConverterFactory): Retrofit {

        return Retrofit.Builder()
            .baseUrl(ApiConstants.BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(factory)
            .addCallAdapterFactory(RxJava3CallAdapterFactory.create())
            .build()
    }
}

class TokenAuthenticator(private val tokenCallback: ITokenAuthenticatorCallback): Authenticator {

    override fun authenticate(route: Route?, response: Response): Request? {
        var TAG = "refreshToken"
        var prefUtils = PrefUtils()
        var context = BrainyClockUserApp.getAppComponent().provideApplication()
        var refreshToken = prefUtils.getStringData(context, AppConstant.SharedPreferences.REFRESH_TOKEN) ?: ""

        Log.v(TAG, "Refresh Token Found $refreshToken")

        var client = OkHttpClient.Builder().build();

        val requestBody: RequestBody = MultipartBody.Builder()
            .setType(MultipartBody.FORM)
            .addFormDataPart(
                ApiConstants.params.EMAIL,
                prefUtils.getStringData(context, AppConstant.SharedPreferences.EMAIL) ?: ""
            )
            .build()

        val request = Request.Builder()
            .url("${ApiConstants.BASE_URL}${ApiConstants.EndPoints.REFRESH_TOKEN}")
            .addHeader("refresh-token", refreshToken)
            .post(requestBody)
            .build()
        var responseBody = ""
        client.newCall(request).execute().use { response ->
            if (response.isSuccessful) {
                responseBody = response.body!!.string()
                Log.v(TAG, "Token Response $responseBody")
            } else {
                Log.v(TAG, "Failed to execute Refresh Token")
                tokenCallback.onRefreshTokenExpired()
            }

        }

        var newAccessToken = ""
        if(responseBody.isNotEmpty()) {
            val jsonObj = JSONObject(responseBody)
            newAccessToken = jsonObj.getString("access_token")
        }
        Log.v(TAG, "New Access Token $newAccessToken")
        if(newAccessToken.isNotEmpty()) {
            prefUtils.saveData(
                context,
                AppConstant.SharedPreferences.ACCESS_TOKEN,
                newAccessToken
            )
        }

        return response.request
            .newBuilder()
            .header(ApiConstants.params.AUTHORIZATION, "Bearer $newAccessToken")
            .build()
    }

}

interface ITokenAuthenticatorCallback {

    fun onRefreshTokenExpired()

}