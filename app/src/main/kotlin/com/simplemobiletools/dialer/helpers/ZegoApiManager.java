package com.simplemobiletools.dialer.helpers;

import android.util.Log;

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

    public interface ZegoLoginCallBack {
        void onFailed();

        void onSuccess();
    }

    private ZegoApiManager() {

    }

    public static synchronized ZegoApiManager getInstance() {
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
        engine.logoutRoom(room_id);
    }

    public void fetchCustomAudioRenderPCMData(ByteBuffer data, int dataLength, ZegoAudioFrameParam param){
        engine.fetchCustomAudioRenderPCMData(data, dataLength, param);
    }

    public void sendCustomAudioCapturePCMData(ByteBuffer data, int dataLength, ZegoAudioFrameParam param){
        engine.sendCustomAudioCapturePCMData(data, dataLength, param);
    }

    public void enableCustomAudioIO() {
        ZegoCustomAudioConfig config = new ZegoCustomAudioConfig();
        config.sourceType = ZegoAudioSourceType.CUSTOM;
        engine.enableCustomAudioIO(true, config);
    }

    public void enableSpeaker(boolean flag) {
        engine.setAudioRouteToSpeaker(flag);
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
}
