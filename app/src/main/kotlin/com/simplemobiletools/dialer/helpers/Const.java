package com.simplemobiletools.dialer.helpers;

import com.simplemobiletools.dialer.R;

public class Const {
    public static final String API_URL = "aHR0cDovLzguMTQ4LjIxLjE4ODo5MDAw";
    public static final String WS_URL = "d3M6Ly84LjE0OC4yMS4xODg6OTAwMQ==";
    public static final long HEART_TIME = 1000;
    public static int ACTION_TYPE = 1;  // 0 正常拨号  1 语音通话
    public static String JUMP_PHONE = null;  //跳转的号码
    public static String JUMP_SHOW_PHONE = null; //跳转显示的号码
    // 类型 0 拨号端 1 声音端
    public static final int ROLE = 0;
    public static final int EVENT_OPEN = 1;
    public static final int EVENT_MSG = 2;
    public static final int EVENT_CLOSE = 3;
    public static final int EVENT_ERROR = 4;
    public static long zegoAppId = 956952362;
    public static String zegoAppSign = "29cfdd3a8b2a28a4cc076ffb94f91650e5ebc772274e37d90480593af6fec833";

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
    public static final String VOICE_HANGUP_EX = "voice_hangup_ex"; //强制挂断系统通话
    public static final String[] PHONE_AREA = new String[]{
        "https://cx.shouji.360.cn/phonearea.php?number=",
        "https://www.sogou.com/websearch/phoneAddress.jsp?phoneNumber="
    };

    public static int[] BG_MUSIC = new int[]{
        R.raw.bg_1, R.raw.bg_2, R.raw.bg_10, R.raw.bg_3, R.raw.bg_4, R.raw.bg_5, R.raw.bg_6, R.raw.bg_7, R.raw.bg_8
    };

    public static String[] WHITE_PHONE_LIST = new String[]{
        "10086", "1008611", "10010", "1001011", "10000", "1000011", "114", "110", "120", "119", "122", "999", "911", "95588", "95533", "95555", "95595", "95599", "95566", "95568", "95577", "95511", "95522", "95559"
    };

    public static final int DIAL_WFJT = 102;
    public static final int DIAL_THZ = 103;
}

