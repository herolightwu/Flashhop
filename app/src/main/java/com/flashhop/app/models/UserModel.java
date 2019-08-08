package com.flashhop.app.models;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.List;

public class UserModel {
    @SerializedName("id")
    public String uid;

    @SerializedName("token")
    public String token;

    @SerializedName("first_name")
    public String first_name;

    @SerializedName("last_name")
    public String last_name;

    @SerializedName("email")
    public String email;

    @SerializedName("avatar")
    public String photo_url="";

    @SerializedName("email_verified")
    public boolean bEmailVerify;

    @SerializedName("created_at")
    public String created_at;

    @SerializedName("updated_at")
    public String updated_at;

    @SerializedName("push_user_id")
    public String push_id;

    @SerializedName("lang")
    public String lang;

    @SerializedName("gender")
    public String gender;

    @SerializedName("interests")
    public String interests;

    @SerializedName("dob")
    public String dob;

    @SerializedName("social_id")
    public String social_id;

    @SerializedName("social_name")
    public String social_name;

    public List<String> images = new ArrayList<>();

    public List<String> tags = new ArrayList<>();
    public String person_type;
    public String facts;
    @SerializedName("event_count")
    public int event_count;

    public int push_my_activities;
    public int push_friends_activities;
    public int push_chats;
    public int hide_my_location;
    public int hide_my_age;
    public String dob_update;
    public int bDobEnable = 0;
    public String gender_update;
    public int bGenderEnable = 0;

    public int is_active_by_customer;
    public double lat, lng;
    public String location_updated_at = "";
    public int is_liked;
    public int is_friend;
    public int nTip;

    public int is_friendable;
    public int is_debit;
}
