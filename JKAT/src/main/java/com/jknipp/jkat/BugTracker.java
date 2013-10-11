package com.jknipp.jkat;

import android.content.Context;
import android.content.SharedPreferences;

import com.bugsense.trace.BugSenseHandler;

/**
 * Created by Jonathan on 9/7/13.
 */
public class BugTracker extends BugSenseHandler {
    private static String APIKEY = "d849ff98";
    private static Context mContext;

    public static void initialize(Context context) {
        mContext = context;
        BugSenseHandler.initAndStartSession(context, APIKEY);

        refreshExtraData();
    }

    public static void refreshExtraData() {
        SharedPreferences prefs = mContext.getSharedPreferences(
                Const.PREFERENCES_FILE, Context.MODE_PRIVATE);
        ;

        BugSenseHandler.setUserIdentifier(prefs.getString("notifications_email", "[ANON]"));
        BugSenseHandler.addCrashExtraData("app_Enabled", Boolean.toString(prefs.getBoolean("enabled", false)));
        BugSenseHandler.addCrashExtraData("admin_Enabled", Boolean.toString(prefs.getBoolean("adminEnabled", false)));
        BugSenseHandler.addCrashExtraData("threshold", prefs.getString("threshold", "__NOTSET__"));
        BugSenseHandler.addCrashExtraData("notifications_email", prefs.getString("notifications_email", "__NOTSET__"));

    }
}
