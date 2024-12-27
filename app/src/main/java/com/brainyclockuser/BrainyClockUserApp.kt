package com.brainyclockuser

import android.app.Application
import android.content.Intent
import androidx.multidex.MultiDex
import com.brainyclockuser.di.component.AppComponent
import com.brainyclockuser.di.component.DaggerAppComponent
import com.brainyclockuser.di.module.ITokenAuthenticatorCallback
import com.brainyclockuser.di.module.NetworkModule
import com.brainyclockuser.di.module.TwitterModule
import com.brainyclockuser.ui.auth.AuthActivity
import com.sensifyawareapp.di.module.AppModule
import dagger.hilt.android.HiltAndroidApp
import org.greenrobot.eventbus.EventBus

@HiltAndroidApp
class BrainyClockUserApp : Application(), ITokenAuthenticatorCallback {
    override fun onCreate() {
        super.onCreate()
        MultiDex.install(this)
        component = DaggerAppComponent.builder()
            .twitterModule(TwitterModule())
            .appModule(AppModule(this))
            .networkModule(NetworkModule())
            .build()
    }

    override fun onRefreshTokenExpired() {
        val okHttpClient = component.getOkHttpClient()
        okHttpClient.dispatcher.cancelAll()
        BrainyClockUserApp.getAppComponent().providePrefUtil().clearData(this@BrainyClockUserApp)
        val intent = Intent(this@BrainyClockUserApp, AuthActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
        }
        startActivity(intent)
    }

    companion object {
        private lateinit var component: AppComponent

        fun getAppComponent(): AppComponent {
            return component
        }

    }
}
