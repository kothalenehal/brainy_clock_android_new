package com.brainyclockuser.di.module

import android.content.Context
import android.view.inputmethod.InputMethodManager
import com.brainyclockuser.api.ApiInterface
import com.sensifyawareapp.di.scopes.ViewScope
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import javax.inject.Singleton

/**
 *
 *
 */
@Module
@InstallIn(SingletonComponent::class)
class TwitterModule {
    @Provides
    @Singleton
    fun provideRetrofit(retrofit: Retrofit): ApiInterface {
        return retrofit.create(ApiInterface::class.java)
    }

    @ViewScope
    @Provides
    fun provideInputMethodManager(context: Context): InputMethodManager {
        return context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
    }
}