package com.flashhop.app.models;

public class MessageEvent {
    public String action;
    public String sender_name;
    public String sender_id;
    public String sender_avatar;
    public String alarm_id;
    public boolean bOpened;

    public MessageEvent(String act, boolean bOpened){ //, String cont, String s_id
        this.action = act;
        //this.content = cont;
        //this.sender_id = s_id;
        this.bOpened = bOpened;
    }

    public MessageEvent(String act, boolean bOpened, String name, String id, String avatar, String wid){ //, String cont, String s_id
        this.action = act;
        this.sender_id = id;
        this.sender_name = name;
        this.bOpened = bOpened;
        this.sender_avatar = avatar;
        this.alarm_id = wid;
    }
}
