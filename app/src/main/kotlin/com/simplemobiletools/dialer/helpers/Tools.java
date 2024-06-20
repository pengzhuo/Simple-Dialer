package com.simplemobiletools.dialer.helpers;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Build;
import android.provider.Settings;
import android.telephony.TelephonyManager;

import java.util.UUID;

public class Tools {
    private static final String TAG = "Tools";

    @SuppressLint("HardwareIds")
    public static String getAndroidId(Context context){
        String id = Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID);
        if (id == null){
            id = UUID.randomUUID().toString();
        }
        return id;
    }

    public static String getImei(Context context){
        TelephonyManager telephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
        if (telephonyManager != null){
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
                return telephonyManager.getImei();
            }else{
                return telephonyManager.getDeviceId();
            }
        }
        return getAndroidId(context);
    }
}
