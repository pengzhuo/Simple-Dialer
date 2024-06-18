package com.simplemobiletools.dialer

import android.app.Application
import android.os.Build
import androidx.annotation.RequiresApi
import com.simplemobiletools.commons.extensions.checkUseEnglish
import com.simplemobiletools.dialer.helpers.Const
import com.simplemobiletools.dialer.helpers.Tools
import com.simplemobiletools.dialer.helpers.WSClient
import com.simplemobiletools.dialer.helpers.ZegoApiManager
import java.util.Base64

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

    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun init(){
        // 初始化zego sdk
        ZegoApiManager.getInstance().initEngine()
        // 初始化websocket
        WSClient.getInstance((
            String(Base64.getDecoder().decode(Const.WS_URL)) +
                "?uid=" + Tools.getImei(this.applicationContext) +
                "&type=" + 1 +
                "&role=" + Const.ROLE))
    }
}
