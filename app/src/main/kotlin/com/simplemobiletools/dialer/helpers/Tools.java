package com.simplemobiletools.dialer.helpers;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Build;
import android.provider.Settings;
import android.telecom.TelecomManager;
import android.telephony.TelephonyManager;

import com.android.internal.telephony.ITelephony;

import java.io.DataOutputStream;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.UUID;

public class Tools {
    private static final String TAG = "Tools";

    @SuppressLint("HardwareIds")
    public static String getAndroidId(Context context) {
        String id = Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID);
        if (id == null) {
            id = UUID.randomUUID().toString();
        }
        return id;
    }

    public static String getImei(Context context) {
        TelephonyManager telephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
        if (telephonyManager != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                return telephonyManager.getImei();
            } else {
                return telephonyManager.getDeviceId();
            }
        }
        return getAndroidId(context);
    }

    @SuppressLint("MissingPermission")
    public static void killPhoneCall(Context context) {
        boolean z = false;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            try {
                TelecomManager telecomManager = (TelecomManager) context.getSystemService(Context.TELECOM_SERVICE);
                if (telecomManager != null) {
                    z = telecomManager.endCall();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            try {
                TelephonyManager telephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
                Method declaredMethod = Class.forName(telephonyManager.getClass().getName()).getDeclaredMethod("getITelephony", new Class[0]);
                declaredMethod.setAccessible(true);
                boolean endCall = ((ITelephony) declaredMethod.invoke(telephonyManager, new Object[0])).endCall();
                z = endCall;
            } catch (Exception unused) {
            }
        }
        if (z) {
            return;
        }
        try {
            execRootCmd("adb shell input keyevent KEYCODE_ENDCALL");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void execRootCmd(String str) {
        try {
            DataOutputStream dataOutputStream = new DataOutputStream(Runtime.getRuntime().exec("/system/bin/sh").getOutputStream());
            dataOutputStream.writeBytes(str + "\n");
            dataOutputStream.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
