package com.brainyclockuser.ui

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.view.Window
import com.brainyclockuser.R
import com.brainyclockuser.base.BaseActivity
import com.brainyclockuser.ui.auth.AuthActivity
import com.brainyclockuser.utils.AppConstant

class SplashActivity : BaseActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        val window: Window = window
        window.decorView.systemUiVisibility = (
                View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        or View.SYSTEM_UI_FLAG_FULLSCREEN)
        Handler(Looper.getMainLooper()).postDelayed({
            if (prefUtils.getStringData(
                    this,
                    AppConstant.SharedPreferences.EMAIL
                ) == null
            ) {
                startActivity(Intent(this@SplashActivity, AuthActivity::class.java))
            } else startActivity(Intent(this@SplashActivity, MainActivity::class.java))

            finish()
        }, 2000)
    }
}