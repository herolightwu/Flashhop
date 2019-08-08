package com.flashhop.app.models;

import com.google.gson.annotations.SerializedName;

public class AlarmModel {
    @SerializedName("action")
    public String action;
    public String wId;
    public String uId;
    public String uName;
    public String uPhoto = "";
    public String sDesc = "";
    public String created_at;
    public int is_checked;
    public EventModel event;
    public UserModel user;
}
