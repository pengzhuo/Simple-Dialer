package com.simplemobiletools.dialer.helpers;

import static im.zego.zegoexpress.constants.ZegoAudioSampleRate.ZEGO_AUDIO_SAMPLE_RATE_44K;

import android.annotation.SuppressLint;
import android.content.Context;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.AudioTrack;
import android.media.MediaRecorder;
import android.os.Build;

import androidx.annotation.RequiresApi;

import com.simplemobiletools.dialer.App;

import java.nio.ByteBuffer;

import im.zego.zegoexpress.entity.ZegoAudioFrameParam;

public class AudioManager {
    private static final String TAG = "AudioManager";
    private static AudioManager instance;
    public static boolean isEnd = false;
    public Integer recordBufferSize;
    private final int captureSampleRate = 44100;
    private final int captureChannel = AudioFormat.CHANNEL_IN_MONO;
    ByteBuffer pcmBuffer;
    private AudioRecord audioRecord;
    private android.media.AudioManager audioManager;
    private final ZegoAudioFrameParam audioFrameParam = new ZegoAudioFrameParam();
    public int mRenderBufferSize;
    private AudioTrack mAudioTrack;
    private final int RENDER_SAMPLE_RATE = 44100;
    private final int CHANNEL = AudioFormat.CHANNEL_OUT_MONO;
    private ByteBuffer renderBuffer;
    private final int audioBufferCacheSize = 17640;
    private ZegoApiManager zegoApiManager;

    private AudioManager(){
        zegoApiManager = ZegoApiManager.getInstance();
        audioManager = (android.media.AudioManager) App.Companion.getApp().getSystemService(Context.AUDIO_SERVICE);
    }

    public static synchronized AudioManager getInstance(){
        if (instance == null){
            instance = new AudioManager();
        }
        return instance;
    }

    public void destroy(){

    }

    public void setMicrophoneMute(boolean mute){
        audioManager.setMicrophoneMute(mute);
    }

    public void setSpeakerphoneOn(boolean on){
        audioManager.setSpeakerphoneOn(on);
    }

    @SuppressLint("MissingPermission")
    public void initAudioRecord() {
        recordBufferSize = AudioRecord.getMinBufferSize(captureSampleRate
            , captureChannel
            , AudioFormat.ENCODING_PCM_16BIT);
        pcmBuffer = ByteBuffer.allocateDirect(recordBufferSize);

        audioRecord = new AudioRecord(
            MediaRecorder.AudioSource.VOICE_COMMUNICATION
            , captureSampleRate
            , captureChannel
            , AudioFormat.ENCODING_PCM_16BIT
            , recordBufferSize);
    }

    public void startRecord(){
        new Thread(new Runnable() {
            @RequiresApi(api = Build.VERSION_CODES.O)
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
                    zegoApiManager.sendCustomAudioCapturePCMData(pcmBuffer, recordBufferSize, audioFrameParam);
                }
            }
        }).start();
    }

    public void releaseRecord(){
        if (audioRecord != null){
            audioRecord.stop();
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
                        zegoApiManager.fetchCustomAudioRenderPCMData(renderBuffer, len, audioFrameParam);
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

    public void releaseAudioTrack(){
        if (mAudioTrack != null){
            mAudioTrack.stop();
            mAudioTrack.release();
            mAudioTrack = null;
        }
    }
}
