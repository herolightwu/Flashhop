package com.flashhop.app.models;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.List;

public class EventModel {
    @SerializedName("id")
    public String id = "";

    @SerializedName("title")
    public String title;

    @SerializedName("date")
    public String date;

    @SerializedName("time")
    public String time;

    @SerializedName("address")
    public String address = "";

    @SerializedName("location")
    public String loc;

    @SerializedName("people")
    public String people;//range

    @SerializedName("age")
    public String age;//range

    @SerializedName("category")
    public String category;

    @SerializedName("photo")
    public String photo = "";

    @SerializedName("description")
    public String desc;

    @SerializedName("gender")
    public int gender;//0-both,1-boys,2-girls

    @SerializedName("followable")
    public int followable;//0,1

    @SerializedName("invitation")
    public int invitaion;//0,1

    @SerializedName("state")
    public int state = 0;//0-draft, 1-publish

    @SerializedName("is_pay_later")
    public int is_pay_later = 0;

    public String price = "0";
    public String currency = "CAD";

    public int db_id = -1;
    public String created_at;
    public String updated_at;
    public UserModel creator;
    public List<UserModel> members = new ArrayList<>();
    public int nLike = 0;
}
