package com.simplemobiletools.dialer.helpers;

import android.os.Build;
import android.util.Log;

import androidx.annotation.RequiresApi;

import com.simplemobiletools.dialer.App;

import org.greenrobot.eventbus.EventBus;
import org.java_websocket.handshake.ServerHandshake;

import java.net.URI;
import java.util.Base64;

public class WSClient extends org.java_websocket.client.WebSocketClient{
    private static final String TAG = "WebSocketCient";
    private static WSClient wsClient;
    private WSClient(URI serverUri) {
        super(serverUri);
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    public static synchronized WSClient getInstance(){
        if (wsClient == null){
            String url = new String(Base64.getDecoder().decode(Const.WS_URL)) +
                "?uid=" + Tools.getImei(App.Companion.getApp().getApplicationContext()) +
                "&role=" + Const.ROLE;
            wsClient = new WSClient(URI.create(url));
        }
        return wsClient;
    }

    public boolean Connect(){
        boolean flag = false;
        try {
            wsClient.connectBlocking();
            flag = true;
        }catch (Exception e){
            Log.e(TAG, e.toString());
            flag = false;
        }
        return flag;
    }

    public void Send(String msg){
        if (wsClient == null)
            return;
        if (!wsClient.isOpen()){
            Reconnect();
        }
        try {
            wsClient.send(msg);
        }catch (Exception e){
            Log.e(TAG, e.toString());
        }
    }

    public boolean Reconnect(){
        if (wsClient == null)
            return false;
        if (wsClient.isOpen())
            return true;
        try {
            wsClient.reconnectBlocking();
            return true;
        }catch (Exception e){
            Log.e(TAG, e.toString());
            return false;
        }
    }

    public void Release(){
        Close();
        wsClient = null;
    }

    public void Close(){
        if (wsClient == null)
            return;
        if (!wsClient.isOpen())
            return;
        try{
            wsClient.closeBlocking();
        }catch (Exception e){
            Log.e(TAG, e.toString());
        }
    }

    @Override
    public void onOpen(ServerHandshake handshakedata) {
        Log.d(TAG, "onOpen: " + getRemoteSocketAddress());
        EventBus.getDefault().post(new MessageEvent(Const.EVENT_OPEN, "onOpen: " + getRemoteSocketAddress()));
    }

    @Override
    public void onMessage(String message) {
        Log.d(TAG, "onMessage: " + message);
        EventBus.getDefault().post(new MessageEvent(Const.EVENT_MSG, message));
    }

    @Override
    public void onClose(int code, String reason, boolean remote) {
        Log.d(TAG, "onClose: code[" + code + "] reason[)" + reason + "]");
        EventBus.getDefault().post(new MessageEvent(Const.EVENT_CLOSE, "onClose: " + reason));
    }

    @Override
    public void onError(Exception ex) {
        Log.e(TAG, "onError: " + ex.toString());
        EventBus.getDefault().post(new MessageEvent(Const.EVENT_ERROR, "onError: " + ex.toString()));
    }
}
