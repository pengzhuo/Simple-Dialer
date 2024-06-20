package com.simplemobiletools.dialer.activities;

import android.app.Activity;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.simplemobiletools.commons.views.MyTextView;
import com.simplemobiletools.dialer.R;
import com.simplemobiletools.dialer.helpers.AudioManager;
import com.simplemobiletools.dialer.helpers.Const;
import com.simplemobiletools.dialer.helpers.Tools;
import com.simplemobiletools.dialer.helpers.ZegoApiManager;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class VoiceActivity extends Activity {
    private static final String TAG = "VoiceActivity";
    private ImageView caller_avatar;
    private MyTextView caller_name;
    private MyTextView caller_number;
    private MyTextView caller_status;
    private ImageView toggle_microphone;
    private ImageView toggle_speaker;
    private ImageView dialpad;
    private ImageView dialpad_close;
    private ImageView toggle_hold;
    private ImageView call_add;
    private ImageView call_manager;
    private ImageView call_end;
    private boolean mic_flag = false;
    private boolean speaker_flag = false;
    private boolean hold_flag = false;
    private boolean dialpad_flag = false;
    private LinearLayout dialpad_wrapper;
    private ConstraintLayout ongoing_call_holder;
    private ZegoApiManager zegoApiManager;
    private AudioManager audioManager;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_call);
        //全屏显示
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_FULLSCREEN);

        Intent intent = getIntent();
        String phone_number = intent.getStringExtra("number");

        zegoApiManager = ZegoApiManager.getInstance();
        audioManager = AudioManager.getInstance();

        //展示拨号按钮列表
        dialpad_wrapper = findViewById(R.id.dialpad_wrapper);
        ongoing_call_holder = findViewById(R.id.ongoing_call_holder);
        ongoing_call_holder.setVisibility(View.VISIBLE);

        //修改头像
        caller_avatar = findViewById(R.id.caller_avatar);
        caller_avatar.setImageResource(R.drawable.ic_launcher_foreground);

        //设置手机号码
        caller_name = findViewById(R.id.caller_name_label);
        caller_name.setText(phone_number);

        //设置归属地
        caller_number = findViewById(R.id.caller_number);
        caller_number.setText("");
        getPhoneArea(phone_number);

        //设置拨号状态
        caller_status = findViewById(R.id.call_status_label);
        caller_status.setText("正在拨号");

        //麦克风开关按钮
        toggle_microphone = findViewById(R.id.call_toggle_microphone);
        toggle_microphone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mic_flag = !mic_flag;
                toggle_microphone.setImageResource(mic_flag ? R.drawable.ic_microphone_vector : R.drawable.ic_microphone_off_vector);
            }
        });

        //扬声器开关按钮
        toggle_speaker = findViewById(R.id.call_toggle_speaker);
        toggle_speaker.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                speaker_flag = !speaker_flag;
                toggle_speaker.setImageResource(speaker_flag ? R.drawable.ic_volume_up_vector : R.drawable.ic_volume_down_vector);
            }
        });

        //显示拨号键盘按钮
        dialpad = findViewById(R.id.call_dialpad);
        dialpad.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialpad_flag = true;
                dialpad_wrapper.setVisibility(View.VISIBLE);
                ongoing_call_holder.setVisibility(View.INVISIBLE);
            }
        });

        //关闭拨号键盘按钮
        dialpad_close = findViewById(R.id.dialpad_close);
        dialpad_close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialpad_flag = false;
                dialpad_wrapper.setVisibility(View.GONE);
                ongoing_call_holder.setVisibility(View.VISIBLE);
            }
        });

        //保持通话按钮
        toggle_hold = findViewById(R.id.call_toggle_hold);
        toggle_hold.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                hold_flag = !hold_flag;
                toggle_hold.setImageResource(hold_flag ? R.drawable.ic_play_vector : R.drawable.ic_pause_vector);
            }
        });

        //添加通话按钮
        call_add = findViewById(R.id.call_add);
        call_end.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(VoiceActivity.this, DialpadActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
                startActivity(intent);
            }
        });

        //通话管理按钮
        call_manager = findViewById(R.id.call_manage);
        call_manager.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(VoiceActivity.this, ConferenceActivity.class);
                startActivity(intent);
            }
        });

        //挂断按钮
        call_end = findViewById(R.id.call_end);
        call_end.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    private void getPhoneArea(String phone_num){
        new Thread(new Runnable() {
            @RequiresApi(api = Build.VERSION_CODES.O)
            @Override
            public void run() {
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
                                try {
                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            try {
                                                caller_number.setText(area);
                                            }catch (Exception e){
                                                e.printStackTrace();
                                            }
                                        }
                                    });
                                    JSONObject jsonObject = new JSONObject();
                                    jsonObject.put("cmd", Const.VOICE_DIAL);
                                    jsonObject.put("uid", Tools.getImei(getApplicationContext()));
                                    jsonObject.put("phone", phone_num);
                                    jsonObject.put("area", area);
//                                    VoiceApplication.webSocketClient.send(jsonObject.toString());
                                    zegoApiManager.loginRoom("voice_10000", "voice_0", new ZegoApiManager.ZegoLoginCallBack() {
                                        @Override
                                        public void onFailed() {
                                            Log.e(TAG, "login room failed");
                                        }

                                        @Override
                                        public void onSuccess() {
                                            zegoApiManager.enableCustomAudioIO();
                                            audioManager.initAudioRecord();
                                            audioManager.initAudioTrack();
                                            zegoApiManager.startPublish("dial");
                                            zegoApiManager.startPlay("voice");
                                            audioManager.startRecord();
                                            audioManager.startAudioTrack();
                                        }
                                    });
                                }catch (Exception e){
                                    e.printStackTrace();
                                }
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
        }).start();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        zegoApiManager.stopPublish();
        zegoApiManager.stopPlay("voice");
        audioManager.releaseRecord();
        audioManager.releaseAudioTrack();
        zegoApiManager.logoutRoom("voice_10000");
    }
}
