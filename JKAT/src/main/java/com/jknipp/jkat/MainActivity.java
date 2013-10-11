package com.jknipp.jkat;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.admin.DevicePolicyManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ToggleButton;


public class MainActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        BugTracker.initialize(this);

        setContentView(R.layout.activity_main);

        if (!isServiceRunning()) {
            Log.i("ServiceStarter", "Starting Service from MainActivity");
            startService(new Intent(this, ATServ.class));
        }

    }

    private boolean isServiceRunning() {
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (ATServ.class.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        Intent i = new Intent();
        switch (item.getItemId()) {
            case R.id.action_settings:
                i.setClassName(Const.PACKAGE_NAME, Const.PACKAGE_NAME + ".SettingsActivity");
                this.startActivity(i);
                return true;
            case R.id.action_debug:
                i.setClassName(Const.PACKAGE_NAME, Const.PACKAGE_NAME + ".debug");
                this.startActivity(i);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public void EnableDeviceAdminBtn(View view) {
        Intent i = new Intent();
        i.setClassName("com.android.settings", "com.android.settings.DeviceAdminSettings");
        this.startActivity(i);

    }

    public void lockDevice(View view) {
        DevicePolicyManager deviceManager = (DevicePolicyManager) getSystemService(Context.DEVICE_POLICY_SERVICE);
        deviceManager.lockNow();
    }

    public void onResume() {
        super.onResume();
        SharedPreferences prefs = this.getSharedPreferences(
                Const.PREFERENCES_FILE, Context.MODE_PRIVATE);

        boolean currentAdminStatus = prefs.getBoolean("adminEnabled", false);

        ToggleButton enableAdminButton = (ToggleButton) findViewById(R.id.adminToggle);
        enableAdminButton.setChecked(currentAdminStatus);
    }
}
