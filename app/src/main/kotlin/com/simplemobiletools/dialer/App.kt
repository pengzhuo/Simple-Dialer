package com.simplemobiletools.dialer

import android.app.Application
import com.simplemobiletools.commons.extensions.checkUseEnglish
import com.simplemobiletools.dialer.helpers.ZegoApiManager

class App : Application() {

    companion object{
        private lateinit var application: Application
        fun getApp(): Application{
            return application
        }
    }

    override fun onCreate() {
        super.onCreate()
        checkUseEnglish()
        App.application = this
        ZegoApiManager.getInstance().initEngine()
    }
}
