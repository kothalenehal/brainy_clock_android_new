package com.brainyclockuser.di.component

import android.app.Application
import com.brainyclockuser.api.ApiManager
import com.brainyclockuser.api.RetrofitInterceptor
import com.brainyclockuser.base.BaseActivity
import com.brainyclockuser.base.model.BaseModel
import com.brainyclockuser.di.module.NetworkModule
import com.brainyclockuser.di.module.TwitterModule
import com.brainyclockuser.utils.NetworkUtils
import com.brainyclockuser.utils.PrefUtils
import com.brainyclockuser.utils.permission.PermissionUtil
import com.sensifyawareapp.di.module.AppModule
import dagger.BindsInstance
import dagger.Component
import okhttp3.OkHttpClient
import javax.inject.Singleton

@Singleton
@Component(modules = [AppModule::class, NetworkModule::class, TwitterModule::class])
interface AppComponent {
    fun inject(activity: BaseActivity)

    fun inject(baseModel: BaseModel);
    fun inject(interceptor: RetrofitInterceptor)
    fun provideApiManager(): ApiManager
    fun providePrefUtil(): PrefUtils
    fun provideApplication(): Application

    //    fun provideFileUtils(): FileUtils?
    fun provideNetworkUtils(): NetworkUtils
    fun providePermissionUtil(): PermissionUtil
    fun getOkHttpClient(): OkHttpClient
}