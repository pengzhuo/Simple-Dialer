package com.simplemobiletools.dialer.helpers;

import static im.zego.zegoexpress.constants.ZegoAudioSampleRate.ZEGO_AUDIO_SAMPLE_RATE_44K;

import android.app.Application;
import android.content.Context;
import android.content.pm.PackageManager;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioRecord;
import android.media.AudioTrack;
import android.media.MediaRecorder;
import android.media.audiofx.AcousticEchoCanceler;
import android.util.Log;

import androidx.core.app.ActivityCompat;

import com.simplemobiletools.dialer.App;

import org.json.JSONObject;

import java.nio.ByteBuffer;

import im.zego.zegoexpress.ZegoExpressEngine;
import im.zego.zegoexpress.callback.IZegoApiCalledEventHandler;
import im.zego.zegoexpress.callback.IZegoRoomLoginCallback;
import im.zego.zegoexpress.constants.ZegoAudioSourceType;
import im.zego.zegoexpress.constants.ZegoScenario;
import im.zego.zegoexpress.entity.ZegoAudioFrameParam;
import im.zego.zegoexpress.entity.ZegoCustomAudioConfig;
import im.zego.zegoexpress.entity.ZegoEngineProfile;
import im.zego.zegoexpress.entity.ZegoRoomConfig;
import im.zego.zegoexpress.entity.ZegoUser;

public class ZegoApiManager {
    private static final String TAG = "Zego";
    private static ZegoExpressEngine engine;
    private static ZegoApiManager instance;
    public static boolean isEnd = false;
    public Integer recordBufferSize;
    private final int captureSampleRate = 44100;
    private final int captureChannel = AudioFormat.CHANNEL_IN_MONO;
    ByteBuffer pcmBuffer;
    private AudioRecord audioRecord;
    private AudioManager audioManager;
    private final ZegoAudioFrameParam audioFrameParam = new ZegoAudioFrameParam();
    public int mRenderBufferSize;
    private AudioTrack mAudioTrack;
    private final int RENDER_SAMPLE_RATE = 44100;
    private final int CHANNEL = AudioFormat.CHANNEL_OUT_MONO;
    private ByteBuffer renderBuffer;
    private final int audioBufferCacheSize = 17640;

    public interface ZegoLoginCallBack {
        void onFailed();

        void onSuccess();
    }

    private ZegoApiManager() {

    }

    public static ZegoApiManager getInstance() {
        if (instance == null) {
            instance = new ZegoApiManager();
        }
        return instance;
    }

    public void initEngine() {
        ZegoEngineProfile zegoEngineProfile = new ZegoEngineProfile();
        zegoEngineProfile.appID = Const.zegoAppId;
        zegoEngineProfile.appSign = Const.zegoAppSign;
        zegoEngineProfile.scenario = ZegoScenario.STANDARD_VOICE_CALL;
        zegoEngineProfile.application = App.Companion.getApp();

        engine = ZegoExpressEngine.createEngine(zegoEngineProfile, null);

        ZegoExpressEngine.setApiCalledCallback(new IZegoApiCalledEventHandler() {
            @Override
            public void onApiCalledResult(int errorCode, String funcName, String info) {
                super.onApiCalledResult(errorCode, funcName, info);
                Log.d(TAG, "onApiCalledResult errorCode[" + errorCode + "] funcName[" + funcName + "] info[" + info + "]");
            }
        });
    }

    public void loginRoom(String room_id, String uid, ZegoLoginCallBack zegoLoginCallBack) {
        isEnd = false;
        ZegoUser zegoUser = new ZegoUser(uid);
        ZegoRoomConfig zegoRoomConfig = new ZegoRoomConfig();
        zegoRoomConfig.isUserStatusNotify = true;
        engine.loginRoom(room_id, zegoUser, zegoRoomConfig, new IZegoRoomLoginCallback() {
            @Override
            public void onRoomLoginResult(int errorCode, JSONObject extendedData) {
                Log.d(TAG, "onRoomLoginResult errorCode[" + errorCode + "] extendedData[" + extendedData.toString() + "]");
                if (errorCode == 0) {
                    zegoLoginCallBack.onSuccess();
                } else {
                    zegoLoginCallBack.onFailed();
                }
            }
        });
    }

    public void logoutRoom(String room_id) {
        isEnd = true;
        engine.logoutRoom(room_id);
    }

    public void startPublish(String stream_id) {
        engine.startPublishingStream(stream_id);
    }

    public void startPlay(String stream_id) {
        engine.startPlayingStream(stream_id);
    }

    public void stopPublish() {
        engine.stopPublishingStream();
    }

    public void stopPlay(String stream_id) {
        engine.stopPlayingStream(stream_id);
    }

    public void destroyEngine() {
        ZegoExpressEngine.destroyEngine(null);
    }

    public void enableCustomAudioIO() {
        ZegoCustomAudioConfig config = new ZegoCustomAudioConfig();
        config.sourceType = ZegoAudioSourceType.CUSTOM;
        engine.enableCustomAudioIO(true, config);
    }

    public void enableSpeaker(boolean flag) {
        engine.setAudioRouteToSpeaker(flag);
    }

    public void setAudioMode(boolean enableAec){
        audioManager = (AudioManager) App.Companion.getApp().getSystemService(Context.AUDIO_SERVICE);
        audioManager.setMode(enableAec ? AudioManager.MODE_IN_COMMUNICATION : AudioManager.MODE_NORMAL);
        audioManager.setSpeakerphoneOn(false);
    }

    public void setAEC(){
        AcousticEchoCanceler acousticEchoCanceler = AcousticEchoCanceler.create(audioRecord.getAudioSessionId());
        if (acousticEchoCanceler != null && acousticEchoCanceler.getEnabled()){
            acousticEchoCanceler.setEnabled(false);
        }
    }

    public void initAudioRecord() {
        recordBufferSize = AudioRecord.getMinBufferSize(captureSampleRate
                , captureChannel
                , AudioFormat.ENCODING_PCM_16BIT);
        pcmBuffer = ByteBuffer.allocateDirect(recordBufferSize);
        if (ActivityCompat.checkSelfPermission(App.Companion.getApp(), android.Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        if (Const.ROLE == 1){
            setAudioMode(true);
        }
        audioRecord = new AudioRecord(
                MediaRecorder.AudioSource.VOICE_COMMUNICATION
                , captureSampleRate
                , captureChannel
                , AudioFormat.ENCODING_PCM_16BIT
                , recordBufferSize);
    }

    public void startRecord(){
        new Thread(new Runnable() {
            @Override
            public void run() {
                if (audioRecord == null){
                    throw new IllegalStateException("AudioRecord is not init");
                }
                audioRecord.startRecording();
                while (!isEnd){
                    byte[] bytes = new byte[recordBufferSize];
                    audioRecord.read(bytes, 0, bytes.length);
                    pcmBuffer.clear();
                    pcmBuffer.put(bytes, 0, recordBufferSize);
                    audioFrameParam.sampleRate = ZEGO_AUDIO_SAMPLE_RATE_44K;
                    pcmBuffer.flip();
                    engine.sendCustomAudioCapturePCMData(pcmBuffer, recordBufferSize, audioFrameParam);
                }
            }
        }).start();
    }

    public void stopRecord(){
        if (audioRecord != null){
            audioRecord.stop();
        }
    }

    public void releaseRecord(){
        if (audioRecord != null){
            audioRecord.release();
            audioRecord = null;
        }
    }

    public void initAudioTrack(){
        mRenderBufferSize = AudioTrack.getMinBufferSize(RENDER_SAMPLE_RATE, CHANNEL, AudioFormat.ENCODING_PCM_16BIT);
        renderBuffer = ByteBuffer.allocateDirect(mRenderBufferSize);
        if (mRenderBufferSize <= 0) {
            throw new IllegalStateException("AudioTrack is not available " + mRenderBufferSize);
        }
        mAudioTrack = new AudioTrack.Builder()
                .setAudioFormat(new AudioFormat.Builder()
                        .setSampleRate(RENDER_SAMPLE_RATE)
                        .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                        .setChannelMask(CHANNEL)
                        .build())
                .setBufferSizeInBytes(mRenderBufferSize)
                .setTransferMode(AudioTrack.MODE_STREAM)
                .build();
    }

    public void startAudioTrack(){
        new Thread() {
            public void run() {
                try {
                    if (mAudioTrack == null) {
                        return;
                    }
                    mAudioTrack.play();
                    int len = mRenderBufferSize;
                    byte[] bytes = new byte[len];
                    while (!isEnd) {
                        renderBuffer.clear();//清除buffer
                        //采集
                        audioFrameParam.sampleRate = ZEGO_AUDIO_SAMPLE_RATE_44K;
                        engine.fetchCustomAudioRenderPCMData(renderBuffer, len, audioFrameParam);
                        if (renderBuffer != null) {
                            renderBuffer.get(bytes);
                        }
                        mAudioTrack.write(bytes, 0, bytes.length);
                    }
                }
                catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }.start();
    }

    public void stopAudioTrack(){
        if (mAudioTrack != null){
            mAudioTrack.stop();
        }
    }

    public void releaseAudioTrack(){
        if (mAudioTrack != null){
            mAudioTrack.release();
            mAudioTrack = null;
        }
    }
}
