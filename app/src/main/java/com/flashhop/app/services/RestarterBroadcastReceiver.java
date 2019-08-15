package com.flashhop.app.services;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class RestarterBroadcastReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        Log.i(RestarterBroadcastReceiver.class.getSimpleName(), "Service Stops!");
        context.startService(new Intent(context, GPSService.class));
    }
}
