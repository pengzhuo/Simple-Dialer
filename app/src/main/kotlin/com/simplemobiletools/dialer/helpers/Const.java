package com.simplemobiletools.dialer.helpers;

public class Const {
    public static final String API_URL = "aHR0cDovLzguMTQ4LjIxLjE4ODo5MDAw";
    public static final String WS_URL = "d3M6Ly84LjE0OC4yMS4xODg6OTAwMQ==";
    public static final int HEART_TIME = 1000;
    // 类型 0 拨号端 1 声音端
    public static int ROLE = 0;
    public static final int EVENT_MSG = 1;
    public static final int EVENT_ERROR = 2;
    public static long zegoAppId = 0;
    public static String zegoAppSign = null;
    public static String voice = null;

    public static final String VOICE_LOGIN = "voice_login"; //登录
    public static final String VOICE_DIAL = "voice_dial"; //拨号
    public static final String VOICE_ANSWER = "voice_answer"; //接听
    public static final String VOICE_HANGUP = "voice_hangup"; //挂断
    public static final String VOICE_CHOICE  = "voice_choice"; //选择变声(男 or 女)
    public static final String VOICE_LOGIN_ROOM  = "voice_login_room"; //登录语音房
    public static final String VOICE_FINISH = "voice_finish"; //通话结束
    public static final String VOICE_HEART = "voice_heart"; //心跳
    public static final String VOICE_CHANGE = "voice_change"; //变声
    public static final String VOICE_DEL_DIAL = "voice_del_dial"; //删除通话记录
    public static final String[] PHONE_AREA = new String[]{
        "https://cx.shouji.360.cn/phonearea.php?number=",
        "https://www.sogou.com/websearch/phoneAddress.jsp?phoneNumber="
    };

    public static int[] BG_MUSIC = new int[]{
//            R.raw.calling, R.raw.bg_1, R.raw.calling, R.raw.calling, R.raw.bg_2, R.raw.calling, R.raw.bg_10, R.raw.calling, R.raw.calling, R.raw.calling, R.raw.bg_3, R.raw.calling, R.raw.calling, R.raw.bg_4, R.raw.calling, R.raw.bg_5, R.raw.calling, R.raw.calling, R.raw.bg_6,R.raw.calling, R.raw.bg_7, R.raw.calling, R.raw.calling,R.raw.calling, R.raw.bg_8
    };

    public static final int DIAL_WFJT = 102;
    public static final int DIAL_THZ = 103;
}

