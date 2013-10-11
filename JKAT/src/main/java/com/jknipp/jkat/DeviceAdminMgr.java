package com.jknipp.jkat;


import android.app.admin.DeviceAdminReceiver;
import android.app.admin.DevicePolicyManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;


/**
 * Created by Jonathan on 7/23/13.
 */
public class DeviceAdminMgr extends DeviceAdminReceiver {
    public final String PreferenceKey = "com.jknipp.jkat_preferences";
    private final String TAG = "DeviceAdminMgr";

    @Override
    public void onPasswordFailed(Context context, Intent intent) {
        SharedPreferences preferences = context.getSharedPreferences(PreferenceKey, Context.MODE_PRIVATE);

        if (!preferences.getBoolean("enabled", true)) return;

        DevicePolicyManager policyManager = (DevicePolicyManager) context.getSystemService(Context.DEVICE_POLICY_SERVICE);
        Log.d(TAG, "Bad Password observed. This is " + policyManager.getCurrentFailedPasswordAttempts() + "/" + (Integer.parseInt(preferences.getString("threshold", "2")) + 1));

        if (policyManager.getCurrentFailedPasswordAttempts() > Integer.parseInt(preferences.getString("threshold", "2"))) {
            //     Intent i = new Intent(context, cameraInterfaceActivity.class);
            Intent i = new Intent();
            i.setClassName("com.jknipp.jkat", "com.jknipp.jkat.cameraInterfaceActivity");

            i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(i);
        }
    }

    public void onPasswordSucceeded(Context context, Intent intent) {
        Log.d(TAG, "Correct password observed. Falling back until next locktime.");
    }

    public void onDisabled(Context context, Intent intent) {
        SharedPreferences prefs = context.getSharedPreferences(
                PreferenceKey, Context.MODE_PRIVATE);

        prefs.edit().putBoolean("adminEnabled", false).commit();
    }

    public void onEnabled(Context context, Intent intent) {
        SharedPreferences prefs = context.getSharedPreferences(
                PreferenceKey, Context.MODE_PRIVATE);
        prefs.edit().putBoolean("adminEnabled", true).commit();
    }
}
