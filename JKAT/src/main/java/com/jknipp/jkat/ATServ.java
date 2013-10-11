package com.jknipp.jkat;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

import java.util.List;

/**
 * Created by Jonathan on 7/28/13.
 */
public class ATServ extends Service {
    private final String TAG = " ATServ";
    private int MAX_SIMULTANEOUS_UPLOADS = 2;
    protected int CURRENT_UPLOADS_BEING_PROCESSED = 0;

    /**
     * Class for clients to access.  Because we know this service always
     * runs in the same process as its clients, we don't need to deal with
     * IPC.
     */
    public class LocalBinder extends Binder {
        ATServ getService() {
            return ATServ.this;
        }
    }

    @Override
    public void onCreate() {
        BugTracker.initialize(this);

        //this.stopSelf();
        retryUploads(); // let's check if there are any uploads that need to be retried, ok?
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.i("LocalService", "Received start id " + startId + ": " + intent);
        // We want this service to continue running until it is explicitly
        // stopped, so return sticky.
        return START_STICKY;
    }

    public int incrementUploadsQueuedForUpload() {
        this.CURRENT_UPLOADS_BEING_PROCESSED = CURRENT_UPLOADS_BEING_PROCESSED + 1;

        return this.CURRENT_UPLOADS_BEING_PROCESSED;
    }

    public int decrementUploadsQueuedForUpload() {
        this.CURRENT_UPLOADS_BEING_PROCESSED = CURRENT_UPLOADS_BEING_PROCESSED - 1;

        return this.CURRENT_UPLOADS_BEING_PROCESSED;

    }

    public boolean isNetworkConnectionAvailable() {
        ConnectivityManager cm = (ConnectivityManager) this.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo info = cm.getActiveNetworkInfo();
        if (info == null) return false;
        NetworkInfo.State network = info.getState();
        return (network == NetworkInfo.State.CONNECTED || network == NetworkInfo.State.CONNECTING);
    }

    public void stopService() {
        Log.i(TAG, "Service being killed externally");
        this.stopSelf();
    }

    public void retryUploads() {
        if (!isNetworkConnectionAvailable()) {
            Log.e(TAG, "Stopping service, no network connection!");
            this.stopSelf();
            return;
        } // no sense trying to upload if there's no connection, ya know?

        DataHandler handler = new DataHandler(this);
        List<ScheduleItem> toUpload = handler.getPendingItems();
        SharedPreferences preferences = this.getSharedPreferences(Const.PREFERENCES_FILE, Context.MODE_PRIVATE);


        if (toUpload.size() == 0) {
            // No uploads waiting, so we can probably abort this service for now.
            Log.i(TAG, "Service terminating because upload queue was empty");
            this.stopSelf();
            return;
        }
        for (int i = 0; i < toUpload.size(); i++) {
            ScheduleItem individualItem = toUpload.get(i);
            if (this.CURRENT_UPLOADS_BEING_PROCESSED < this.MAX_SIMULTANEOUS_UPLOADS) { // only do X uploads at a time, others will have to wait until next poll

                if (individualItem.getUploadAttemptCount() < FileUpload.MAX_UPLOAD_ATTEMPTS) {
                    FileUpload reUploader = new FileUpload(this, individualItem, handler, preferences);
                    Log.i(TAG, "Trying to re-upload " + individualItem.getFileName());

                    this.incrementUploadsQueuedForUpload();
                    reUploader.doUploadFromService(this); // so we can update CURRENT_UPLOADS_BEING_PROCESSED
                }
            } else { //we're not going to be processing any more for upload as we reached the max, so we might as well stop iterating through this list and save precious CPU cycles
                Log.i(TAG, "Maxed out with upload queue. Left: " + (toUpload.size() - i));
                i = toUpload.size();
            }
        }
    }

    @Override
    public void onDestroy() {
        Log.i(TAG, "Terminated service (Final)");
        super.onDestroy();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    // This is the object that receives interactions from clients.  See
    // RemoteService for a more complete example.
    private final IBinder mBinder = new LocalBinder();

}