package com.simplemobiletools.dialer.helpers;

import android.annotation.SuppressLint;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.provider.CallLog;
import android.provider.Settings;
import android.telecom.TelecomManager;
import android.telephony.TelephonyManager;
import android.util.Log;

import androidx.annotation.RequiresApi;

import com.android.internal.telephony.ITelephony;

import org.greenrobot.eventbus.EventBus;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.UUID;

public class Tools {
    private static final String TAG = "Tools";

    public static void setBlackList(Context context, String blackList){
        SharedPreferences sp = context.getSharedPreferences("voice_config", Context.MODE_PRIVATE);
        String oldBlackList = getBlackList(context);
        if (oldBlackList.isEmpty()) {
            oldBlackList = blackList;
        }else{
            oldBlackList = oldBlackList + "," + blackList;
        }
        SharedPreferences.Editor editor = sp.edit();
        editor.putString("blackList", oldBlackList);
        editor.commit();
    }

    public static String getBlackList(Context context){
        SharedPreferences sp = context.getSharedPreferences("voice_config", Context.MODE_PRIVATE);
        return sp.getString("blackList", "");
    }

    public static boolean isInBlackList(Context context, String phoneNum){
        boolean flag = true;
        String blackList = getBlackList(context);
        if (blackList.isEmpty()){
            flag = false;
        }else{
            String[] blackListArr = blackList.split(",");
            for (String num : blackListArr){
                if (phoneNum.contains(num)){
                    flag = true;
                    break;
                }else{
                    flag = false;
                }
            }
        }
        return flag;
    }

    public static void setUid(Context context, String uid){
        SharedPreferences sp = context.getSharedPreferences("voice_config", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        editor.putString("uid", uid);
        editor.commit();
    }

    public static String getUid(Context context){
        SharedPreferences sp = context.getSharedPreferences("voice_config", Context.MODE_PRIVATE);
        return sp.getString("uid", "");
    }

    @SuppressLint("HardwareIds")
    public static String getAndroidId(Context context) {
        String id = null;
        try {
            id = Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID);
        }catch (Exception e){
            e.printStackTrace();
            id = getUid(context);
            if (id == null || id.isEmpty()){
                id = UUID.randomUUID().toString();
                setUid(context, id);
            }
        }
        return id;
    }

    public static String getImei(Context context) {
        String imei = null;
        try {
            TelephonyManager telephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
            if (telephonyManager != null) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    imei = telephonyManager.getImei();
                } else {
                    imei = telephonyManager.getDeviceId();
                }
            }
            return imei;
        }catch (Exception e){
            e.printStackTrace();
            return getAndroidId(context);
        }
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

    public static void insertCallLogEntry(Context context, String number, long callDate, long duration, int callType, String area) {
        ContentResolver contentResolver = context.getContentResolver();
        // 创建一个新的ContentValues对象
        ContentValues values = new ContentValues();
        values.put(CallLog.Calls.NUMBER, number);           // 插入电话号码
        values.put(CallLog.Calls.DATE, callDate);           // 插入通话时间
        values.put(CallLog.Calls.DURATION, duration);       // 插入通话时长
        values.put(CallLog.Calls.TYPE, callType);           // 插入通话类型
        values.put(CallLog.Calls.GEOCODED_LOCATION, area);  // 插入归属地

        // 插入通话记录
        contentResolver.insert(CallLog.Calls.CONTENT_URI, values);
    }

    public static void checkPhoneStatus(String phone_num){
        new Thread(new Runnable() {
            @RequiresApi(api = Build.VERSION_CODES.O)
            @Override
            public void run() {
                HttpURLConnection connection = null;
                try {
                    String boundary = "--------" + UUID.randomUUID().toString();
                    String boundary2 = "--" + boundary;
                    URL url = new URL(new String(Base64.getDecoder().decode(Const.CHECK_PHONE_URL)));
                    connection = (HttpURLConnection) url.openConnection();
                    connection.setRequestMethod("POST");
                    connection.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + boundary);
                    connection.setDoOutput(true);
                    OutputStream outputStream = connection.getOutputStream();

                    StringBuilder sb = new StringBuilder();
                    sb.append(boundary2).append("\r\n");
                    sb.append("Content-Disposition: form-data; name=\"appId\"").append("\r\n");
                    sb.append("\r\n");
                    sb.append(new String(Base64.getDecoder().decode(Const.CHECK_PHONE_APPID))).append("\r\n");
                    sb.append(boundary2).append("\r\n");
                    sb.append("Content-Disposition: form-data; name=\"appKey\"").append("\r\n");
                    sb.append("\r\n");
                    sb.append(new String(Base64.getDecoder().decode(Const.CHECK_PHONE_APPKEY))).append("\r\n");
                    sb.append(boundary2).append("\r\n");
                    sb.append("Content-Disposition: form-data; name=\"mobiles\"").append("\r\n");
                    sb.append("\r\n");
                    sb.append(phone_num).append("\r\n");
                    sb.append(boundary2).append("--").append("\r\n");
                    outputStream.write(sb.toString().getBytes(StandardCharsets.UTF_8));
                    outputStream.flush();
                    outputStream.close();

                    int code = connection.getResponseCode();
                    if (code == HttpURLConnection.HTTP_OK){
                        BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                        String line;
                        StringBuilder response = new StringBuilder();
                        while ((line = reader.readLine()) != null){
                            response.append(line);
                        }
                        reader.close();
                        String responseData = response.toString();
                        Log.e(TAG, "checkPhoneStatus: [" + responseData + "]");
                        JSONObject jsonObj = new JSONObject(responseData);
                        if (jsonObj.getInt("code") == 200){
                            JSONObject obj = jsonObj.getJSONObject("data");
                            int status = obj.getInt("status");
                            String area = obj.getString("area");
                            String carrier = obj.getString("carrier");
                            area = area + Const.YYS[Integer.parseInt(carrier) - 1];
                            JSONObject resultObj = new JSONObject();
                            resultObj.put("area" , area);
                            resultObj.put("status", status);
                            EventBus.getDefault().post(new MessageEvent(Const.EVENT_NORMAL, resultObj.toString()));
                        }
                    }
                }catch (Exception e){
                    e.printStackTrace();
                }finally {
                    if (connection != null){
                        connection.disconnect();
                    }
                }
            }
        }).start();
    }

    private void getPhoneArea(String phone_num){
        HttpURLConnection connection = null;
        try {
            URL url = new URL(Const.PHONE_AREA[0] + phone_num);
            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            int code = connection.getResponseCode();
            if (code == HttpURLConnection.HTTP_OK){
                BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                String line;
                StringBuilder response = new StringBuilder();
                while ((line = reader.readLine()) != null){
                    response.append(line);
                }
                reader.close();
                String responseData = response.toString();
                Log.e(TAG, " phone_area: " + responseData);
                JSONObject jsonObj = new JSONObject(responseData);
                if (jsonObj.getInt("code") == 0){
                    JSONObject obj = jsonObj.getJSONObject("data");
                    if (!phone_num.isEmpty()){
                        String area = obj.getString("province") + obj.getString("city");
                    }
                }
            }
        }catch (Exception e){
            e.printStackTrace();
        }finally {
            if (connection != null){
                connection.disconnect();
            }
        }
    }
}
