package com.simplemobiletools.dialer.helpers;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Build;
import android.provider.Settings;
import android.telephony.TelephonyManager;

import androidx.annotation.RequiresApi;

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

    @RequiresApi(api = Build.VERSION_CODES.O)
    public static String getImei(Context context){
        @SuppressLint("ServiceCast") TelephonyManager telephonyManager = (TelephonyManager) context.getSystemService(Context.TELECOM_SERVICE);
        return telephonyManager.getImei();
    }
}
