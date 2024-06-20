package com.simplemobiletools.dialer

import android.app.Application
import android.os.Build
import androidx.annotation.RequiresApi
import com.simplemobiletools.commons.extensions.checkUseEnglish
import com.simplemobiletools.dialer.helpers.WSClient
import com.simplemobiletools.dialer.helpers.ZegoApiManager
import kotlin.concurrent.thread

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
        application = this
        init()
    }

    fun init(){
        // 初始化zego sdk
        ZegoApiManager.getInstance().initEngine()
        // 初始化websocket
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            WSClient.getInstance().Connect()
        }
    }
}
