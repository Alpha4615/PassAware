package com.jknipp.jkat;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class BootCompleted extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        Intent i = new Intent("com.jknipp.jkat.ATServ");
        i.setClass(context, ATServ.class);
        context.startService(i);
        Log.i("BOOT", "Starting service after boot");
    }
}
