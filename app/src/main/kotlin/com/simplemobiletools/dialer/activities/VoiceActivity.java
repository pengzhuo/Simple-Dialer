package com.simplemobiletools.dialer.activities;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;

import androidx.annotation.Nullable;

import com.simplemobiletools.commons.views.MyTextView;
import com.simplemobiletools.dialer.R;

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

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_call);

        Intent intent = getIntent();
        String phone_number = intent.getStringExtra("number");

        //修改头像
        caller_avatar = findViewById(R.id.caller_avatar);
        caller_avatar.setImageResource(R.drawable.ic_launcher_foreground);

        //设置手机号码
        caller_name = findViewById(R.id.caller_name_label);
        caller_name.setText(phone_number);
        //设置归属地
        caller_number = findViewById(R.id.caller_number);
        caller_number.setText("未知");
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

            }
        });
        //关闭拨号键盘按钮
        dialpad_close = findViewById(R.id.dialpad_close);
        dialpad_close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialpad_flag = false;
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
        //通话管理按钮
        call_manager = findViewById(R.id.call_manage);
        //挂断按钮
        call_end = findViewById(R.id.call_end);
        call_end.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });
    }
}
