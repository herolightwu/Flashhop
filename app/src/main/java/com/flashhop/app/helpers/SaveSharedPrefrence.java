package com.flashhop.app.helpers;

import android.content.Context;
import android.content.SharedPreferences;

public class SaveSharedPrefrence {
    SharedPreferences sharedPreferences;

    public static final String PREFS_NAME = "Flashhop";
    public static final String KEY_USER_ID = "key_userid";
    public static final String KEY_USER_TOKEN = "key_token";
    public static final String KEY_LOGIN = "key_login";
    public static final String KEY_INSTA_TOKEN = "insta_token";
    public static final String KEY_FACE_TOKEN = "face_token";
    public static final String KEY_NOTIFICATION = "key_noti";
    public static final String KEY_LOCATION = "key_loc";
    public static final String KEY_GUIDE = "key_guide";
    public static final String KEY_CARDINFO = "key_cardinfo";
    public static final String KEY_SERVICE = "key_service_location";
    public static final String KEY_BADGE_CHAT  = "key_badge_chat";
    public static final String KEY_BADGE_ME  = "key_badge_me";
    public static final String KEY_BADGE_FRIEND  = "key_badge_friend";


    public String getString(Context context, String key) {
        sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        String status = sharedPreferences.getString(key, null);

        return status;
    }

    public void putString(Context context, String key, String value)
    {
        sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(key, value);

        editor.commit();
    }

    public void clear(Context context)
    {
        sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.clear();
        editor.apply();
    }


    public void saveKeyLogin(Context context, boolean bStatus ){
        sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean(KEY_LOGIN, bStatus);

        editor.commit();
    }

    public boolean getKeyLogin(Context context){
        sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        boolean status = sharedPreferences.getBoolean(KEY_LOGIN, false);

        return status;
    }

    public void saveKeyUserID(Context context, String userid){
        sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(KEY_USER_ID, userid);

        editor.commit();
    }

    public String getKeyUserID(Context context){
        sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        String status = sharedPreferences.getString(KEY_USER_ID, "");

        return status;
    }

    public void DeletePrefrence(Context context) {

        sharedPreferences = context.getSharedPreferences(PREFS_NAME,
                Context.MODE_PRIVATE);

        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.clear();
        editor.commit();

    }

}
