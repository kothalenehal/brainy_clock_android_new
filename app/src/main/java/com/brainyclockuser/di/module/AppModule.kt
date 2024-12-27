package com.sensifyawareapp.di.module

import android.app.Application
import com.brainyclockuser.BrainyClockUserApp
import com.brainyclockuser.api.ApiInterface
import com.brainyclockuser.api.ApiManager
import com.brainyclockuser.base.BaseActivity
import com.brainyclockuser.di.module.ITokenAuthenticatorCallback
import com.brainyclockuser.utils.NetworkUtils
import com.brainyclockuser.utils.PrefUtils
import com.brainyclockuser.utils.permission.PermissionUtil
import dagger.Module
import dagger.Provides
import dagger.hilt.migration.DisableInstallInCheck
import javax.inject.Named
import javax.inject.Singleton

/**
 *
 *
 */
@Module
@DisableInstallInCheck
class AppModule(application: BrainyClockUserApp) {
    private val mApplication: BrainyClockUserApp = application

    @Provides
    @Singleton
    fun providesApplication(): Application {
        return mApplication
    }

    // Dagger will only look for methods annotated with @Provides
    @Provides
    @Singleton
    fun  // Application reference must come from AppModule.class
            providesSharedPreferences(): PrefUtils {
        return PrefUtils()
    }

    @Provides
    @Singleton
    fun provideBaseAppActivity(baseActivity: BaseActivity): BaseActivity {
        return baseActivity
    }

    @Provides
    @Singleton
    fun provideApiManager(apiInterface: ApiInterface): ApiManager {
        return ApiManager(apiInterface)
    }

    /*@Provides
    @Singleton
    fun provideFileUtils(): FileUtils {
        return FileUtils()
    }*/

    @Provides
    @Singleton
    fun provideNetworkUtils(): NetworkUtils {
        return NetworkUtils(mApplication.applicationContext)
    }

    @Provides
    @Singleton
    fun providePermissionUtil(): PermissionUtil {
        return PermissionUtil()
    }

    @Provides
    @Named("TokenCallback")
    fun provideBaseUrl(): ITokenAuthenticatorCallback {
        return mApplication
    }

}