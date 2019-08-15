package com.flashhop.app;



import android.content.Intent;
import android.graphics.Typeface;
import android.location.Location;
import android.support.multidex.MultiDexApplication;

import com.androidnetworking.AndroidNetworking;
import com.flashhop.app.helpers.SaveSharedPrefrence;
import com.flashhop.app.models.MessageEvent;
import com.flashhop.app.models.UserModel;
import com.flashhop.app.start.SplashActivity;
import com.flashhop.app.utils.Const;
import com.google.android.gms.maps.model.LatLng;
import com.onesignal.OSNotification;
import com.onesignal.OSNotificationOpenResult;
import com.onesignal.OneSignal;

import org.greenrobot.eventbus.EventBus;
import org.json.JSONException;
import org.json.JSONObject;


public class MyApp extends MultiDexApplication {

    public static MyApp myApp = null;
    public static int home_type = Const.HOME_HOME;
    public static UserModel curUser;
    public static Location curLoc;
    public static String myAddr="";
    public static String selAddr="";
    public static LatLng selLoc = null;
    public static Typeface myfont_normal, myfont_italic;
    public static int chatBadge, whatMeBadge, whatFriendBadge;

    public static MyApp getInstance(){
        if(myApp == null)
        {
            myApp = new MyApp();
            curUser = new UserModel();
            curLoc = new Location("");
            curLoc.setLatitude(0.0);
            curLoc.setLatitude(0.0);
        }
        return myApp;
    }

    public static MyApp getApp(){
        return myApp;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        AndroidNetworking.initialize(getApplicationContext());
        OneSignal.startInit(this)
                .autoPromptLocation(false) // default call promptLocation later
                .inFocusDisplaying(OneSignal.OSInFocusDisplayOption.Notification)
                .setNotificationReceivedHandler(new myNotificationReceivedHandler())
                .setNotificationOpenedHandler(new myNotificationOpenedHandler())
                .unsubscribeWhenNotificationsAreDisabled(true)
                .init();
    }

    private class myNotificationReceivedHandler implements OneSignal.NotificationReceivedHandler {
        @Override
        public void notificationReceived(OSNotification notification) {
            Intent intent;
            SaveSharedPrefrence sharedPreferences = new SaveSharedPrefrence();
            JSONObject oneSignalData = notification.payload.additionalData;
            try{
                if(oneSignalData.has("action")){
                    String type = oneSignalData.getString("action");
                    if(MyApp.getApp() == null){
                        //intent = new Intent(getApplicationContext(), SplashActivity.class);
                    } else if(type.equals("chat")){
                        if(MyApp.getInstance().home_type != Const.HOME_CHAT){
                            MyApp.getInstance().chatBadge = 1;
                            sharedPreferences.putString(getApplicationContext(), SaveSharedPrefrence.KEY_BADGE_CHAT, "1");
                        }
                    } else if(type.equals("requested_friend") || type.equals("accept_friend_request")||type.equals("non_friend_invite")||
                            type.equals("ping_2hours_event")||type.equals("ping_30mins_event")||type.equals("ping_less_member") ||
                            type.equals("tipped") || type.equals("throw_back") ){
                        if(MyApp.getInstance().home_type != Const.HOME_ALARM){
                            MyApp.getInstance().whatMeBadge = 1;
                        }
                    } else if(type.equals("liked") || type.equals("disliked") || type.equals("me_too")){
                        if(MyApp.getInstance().home_type != Const.HOME_ALARM){
                            MyApp.getInstance().whatMeBadge = 1;
                        }
                        String wid = oneSignalData.getString("whatsup_id");
                        String sName = oneSignalData.getString("sender_name");
                        String sid = oneSignalData.getString("sender_id");
                        String avatar = oneSignalData.getString("sender_avatar");
                        EventBus.getDefault().post(new MessageEvent(type, true, sName, sid, avatar, wid));
                        return;
                    } else if(type.equals("pinned") || type.equals("join_event") || type.equals("host_event")){
                        if(MyApp.getInstance().home_type != Const.HOME_ALARM){
                            MyApp.getInstance().whatFriendBadge = 1;
                            sharedPreferences.putString(getApplicationContext(), SaveSharedPrefrence.KEY_BADGE_FRIEND, "1");
                        }
                    } else if(type.equals("tagged")||type.equals("friend_invite")){
                        String sender_id = oneSignalData.getString("sender_id");
                        if(sender_id.equals(MyApp.curUser.uid)){
                            if(MyApp.getInstance().home_type != Const.HOME_ALARM){
                                MyApp.getInstance().whatMeBadge = 1;
                                sharedPreferences.putString(getApplicationContext(), SaveSharedPrefrence.KEY_BADGE_ME, "1");
                            }
                        } else{
                            if(MyApp.getInstance().home_type != Const.HOME_ALARM){
                                MyApp.getInstance().whatFriendBadge = 1;
                                sharedPreferences.putString(getApplicationContext(), SaveSharedPrefrence.KEY_BADGE_FRIEND, "1");
                            }
                        }
                    }

                    EventBus.getDefault().post(new MessageEvent(type, false));
                }
            } catch (JSONException ex){

            }
        }
    }

    private class myNotificationOpenedHandler implements OneSignal.NotificationOpenedHandler {
        // This fires when a notification is opened by tapping on it.
        @Override
        public void notificationOpened(OSNotificationOpenResult result) {
            Intent intent;
            JSONObject oneSignalData = result.notification.payload.additionalData;
            try{
                if(oneSignalData.has("action")){
                    String type = oneSignalData.getString("action");
                    if(MyApp.getApp() == null){
                        intent = new Intent(getApplicationContext(), SplashActivity.class);
                        getApplicationContext().startActivity(intent);
                        return;
                    } else if(type.equals("chat")){
                        if(MyApp.getInstance().home_type != Const.HOME_CHAT){
                            MyApp.getInstance().chatBadge = 1;
                        }
                    } else if(type.equals("requested_friend") || type.equals("accept_friend_request")||type.equals("non_friend_invite")||
                            type.equals("ping_2hours_event")||type.equals("ping_30mins_event")||type.equals("ping_less_member") ||
                            type.equals("tipped") || type.equals("throw_back")){
                        if(MyApp.getInstance().home_type != Const.HOME_ALARM){
                            MyApp.getInstance().whatMeBadge = 1;
                        }
                    } else if(type.equals("liked") || type.equals("disliked") || type.equals("me_too")){
                        if(MyApp.getInstance().home_type != Const.HOME_ALARM){
                            MyApp.getInstance().whatMeBadge = 1;
                        }
                        String wid = oneSignalData.getString("whatsup_id");
                        String sName = oneSignalData.getString("sender_name");
                        String sid = oneSignalData.getString("sender_id");
                        String avatar = oneSignalData.getString("sender_avatar");
                        //EventBus.getDefault().post(new MessageEvent(type, true, sName, sid, avatar, wid));
                        return;
                    } else if(type.equals("pinned") || type.equals("join_event") || type.equals("host_event")){
                        if(MyApp.getInstance().home_type != Const.HOME_ALARM){
                            MyApp.getInstance().whatFriendBadge = 1;
                        }
                    } else if(type.equals("tagged")||type.equals("friend_invite")){
                        String sender_id = oneSignalData.getString("sender_id");
                        if(sender_id.equals(MyApp.curUser.uid)){
                            if(MyApp.getInstance().home_type != Const.HOME_ALARM){
                                MyApp.getInstance().whatMeBadge = 1;
                            }
                        } else{
                            if(MyApp.getInstance().home_type != Const.HOME_ALARM){
                                MyApp.getInstance().whatFriendBadge = 1;
                            }
                        }
                    }

                    EventBus.getDefault().post(new MessageEvent(type, true));
                }
            } catch (JSONException ex){

            }
        }
    }
}
