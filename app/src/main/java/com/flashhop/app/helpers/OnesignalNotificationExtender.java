package com.flashhop.app.helpers;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.os.Build;
import android.provider.ContactsContract;
import android.support.v4.app.NotificationCompat;

import com.flashhop.app.MyApp;
import com.flashhop.app.R;
import com.flashhop.app.utils.Const;
import com.onesignal.NotificationExtenderService;
import com.onesignal.OSNotificationDisplayedResult;
import com.onesignal.OSNotificationReceivedResult;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;


public class OnesignalNotificationExtender extends NotificationExtenderService {

    @Override
    protected boolean onNotificationProcessing(OSNotificationReceivedResult receivedResult) {

        final OSNotificationReceivedResult receiveData = receivedResult;
        // Read Properties from result
        OverrideSettings overrideSettings = new OverrideSettings();
        overrideSettings.extender = new NotificationCompat.Extender() {
            @Override
            public NotificationCompat.Builder extend(NotificationCompat.Builder builder) {
                // Sets the background notification color to Red on Android 5.0+ devices.
                try{
                    JSONObject oneData = receiveData.payload.additionalData;
                    if (!oneData.has("action")) {
                        return null;
                    }
                    String type = oneData.getString("action");
                    String body = receiveData.payload.body;//oneData.getString("content");
                    NotificationCompat.BigTextStyle bigText = new NotificationCompat.BigTextStyle();
                    bigText.bigText(body);
                    bigText.setBigContentTitle("Flashhop");
                    bigText.setSummaryText(type);

                    //mBuilder.setContentIntent(pendingIntent);
                    builder.setSmallIcon(R.mipmap.ic_launcher_round);
                    builder.setContentTitle("Flashhop");
                    builder.setContentText(body);
                    builder.setPriority(NotificationCompat.PRIORITY_DEFAULT);
                    builder.setStyle(bigText);
                    builder.setDefaults(Notification.DEFAULT_ALL);
                    builder.setAutoCancel(true);
                    builder.setVibrate(new long[] { 500, 500, 1000, 1000, 1000 });

                    builder.getNotification().flags |= Notification.FLAG_AUTO_CANCEL;

                    NotificationManager mNotificationManager =
                            (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        builder.setChannelId("notify_001");
                        NotificationChannel channel = new NotificationChannel("notify_001",
                                "Flashhop",
                                NotificationManager.IMPORTANCE_DEFAULT);
                        mNotificationManager.createNotificationChannel(channel);
                    }
                    if(MyApp.getApp() != null){
                        if((MyApp.home_type == Const.HOME_CHAT || MyApp.home_type == Const.HOME_MSG ) && type.equals("chat")){
                            return null;
                        }
                        if(MyApp.home_type == Const.HOME_ALARM && !type.equals("chat")){
                           return null;
                        }
                        return builder;
                    } else{
                        return builder;
                    }
                } catch (JSONException ex){
                    return null;
                }
            }
        };

        JSONObject oneSignalData = receivedResult.payload.additionalData;

        OSNotificationDisplayedResult displayedResult = displayNotification(overrideSettings);
        //displayNotification(receiveData.payload.body, type);
        return true;
    }

    private void displayNotification(String body, String type){
        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(this, "notify_001");

        NotificationCompat.BigTextStyle bigText = new NotificationCompat.BigTextStyle();
        bigText.bigText(body);
        bigText.setBigContentTitle("Flashhop");
        bigText.setSummaryText(type);

        //mBuilder.setContentIntent(pendingIntent);
        mBuilder.setSmallIcon(R.mipmap.ic_launcher_round);
        mBuilder.setContentTitle("Flashhop");
        mBuilder.setContentText(body);
        mBuilder.setPriority(NotificationCompat.PRIORITY_MAX);
        mBuilder.setStyle(bigText);
        mBuilder.setDefaults(Notification.DEFAULT_ALL);
        mBuilder.setAutoCancel(true);
        mBuilder.setVibrate(new long[] { 500, 500, 1000, 1000, 1000 });

        NotificationManager mNotificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel("notify_001",
                    "Channel human readable title",
                    NotificationManager.IMPORTANCE_DEFAULT);
            mNotificationManager.createNotificationChannel(channel);
        }

        mNotificationManager.notify(0, mBuilder.build());
    }

}
