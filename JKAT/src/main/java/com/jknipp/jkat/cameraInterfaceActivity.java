package com.jknipp.jkat;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.hardware.Camera;
import android.media.AudioManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.widget.FrameLayout;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;


/**
 * Created by Jonathan on 7/23/13.
 */
public class cameraInterfaceActivity extends Activity {
    public Camera mCamera;
    private CameraPreview mPreview;
    private static Activity thisActivity;
    private int mWhichCamera;
    protected boolean pictureTaken = false;
    private final String TAG = "cameraInterfaceActivity";
    private final static String static_TAG = "cameraInterfaceActivity";

    private Camera.PictureCallback mJpegCallback = new Camera.PictureCallback() {
        public void onPictureTaken(byte[] data, Camera camera) {
            String FILENAME = "";
            Boolean FileWritten = false;
            Boolean uploadNow = true;
            SharedPreferences preferences = thisActivity.getSharedPreferences(Const.PREFERENCES_FILE, Context.MODE_PRIVATE);

            try {

                FILENAME = String.format("PassAware-%d.jpg", System.currentTimeMillis());

                FileOutputStream fos = openFileOutput(FILENAME, Context.MODE_PRIVATE);
                fos.write(data);
                fos.close();
                FileWritten = true;

            } catch (FileNotFoundException e) {
                e.printStackTrace();
                Log.e(TAG, "Cannot write JPEG:" + e.getMessage());
            } catch (IOException e) {
                e.printStackTrace();
                Log.e(TAG, "Cannot write JPEG, IOException:" + e.getMessage());
            }

            if (FileWritten) {
                ScheduleItem newItem = new ScheduleItem();
                if (!isNetworkConnectionAvailable()) { // queue this for later!
                    uploadNow = false;
                    newItem.setReasonScheduled(ScheduleItem.REASON_NONETWORK);
                } else if (!isValidEmail(preferences.getString("notifications_email", ""))) {
                    uploadNow = false;
                    newItem.setReasonScheduled(ScheduleItem.REASON_INVALIDEMAIL);

                } else {
                    uploadNow = true;
                    newItem.setReasonScheduled(ScheduleItem.REASON_REGULARQUEUE);
                }
                newItem.setTimeScheduled(DataHandler.getTime())
                        .setFileName(FILENAME);

                DataHandler writer = new DataHandler(thisActivity);
                writer.addSchedule(newItem);

                if (uploadNow == true) {
                    Log.d(TAG, "Starting upload");
                    FileUpload uploader = new FileUpload(thisActivity, newItem, writer, preferences);
                    uploader.doUpload();
                    Log.d(TAG, "Upload call finished (it's async)");
                } else {
                    if (!isServiceRunning()) {
                        Log.i("ServiceStarter", "Starting Service from cameraInterfaceActivity");
                        startService(new Intent(thisActivity, ATServ.class));
                    }
                }

                pictureTaken = true;
            }

            //cameraInterfaceActivity.suicide();
            finish();
        }
    };

    private boolean isServiceRunning() {
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (ATServ.class.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }

    public static void suicide() {
        Log.d(static_TAG, "Self-Aborting because I'm finished.");
        thisActivity.finish();
    }

    public void onDestroy() {
        super.onDestroy();
        this.releaseCamera();
    }

    public static void muteShutter() {
        AudioManager mgr = (AudioManager) thisActivity.getSystemService(Context.AUDIO_SERVICE);
        mgr.setStreamMute(AudioManager.STREAM_SYSTEM, true);
    }

    public static void unmuteShutter() {
        final Handler handler = new Handler();
        Timer t = new Timer();
        t.schedule(new TimerTask() {
            public void run() {
                handler.post(new Runnable() {
                    public void run() {
                        AudioManager mgr = (AudioManager) thisActivity.getSystemService(Context.AUDIO_SERVICE);
                        mgr.setStreamMute(AudioManager.STREAM_SYSTEM, false);
                    }
                });
            }
        }, 1000);
    }

    public boolean isNetworkConnectionAvailable() {
        ConnectivityManager cm = (ConnectivityManager) this.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo info = cm.getActiveNetworkInfo();
        if (info == null) return false;
        NetworkInfo.State network = info.getState();
        return (network == NetworkInfo.State.CONNECTED || network == NetworkInfo.State.CONNECTING);
    }


    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        BugTracker.initialize(this);

        thisActivity = this;
        setContentView(R.layout.activity_camerainterfaceactivity);

        // Some phones have 2 cameras some have 1. Pick the front camera, which is always the second camera if there's 2.
        mWhichCamera = (Camera.getNumberOfCameras() == 2 ? 1 : 0);

        // Create an instance of Camera
        mCamera = getCameraInstance(mWhichCamera, mCamera);

        if (mCamera != null) {
            // Create our Preview view and set it as the content of our activity.
            mPreview = new CameraPreview(this, mCamera, mJpegCallback);

            FrameLayout preview = (FrameLayout) findViewById(R.id.camera_preview);
            preview.addView(mPreview);
        } else {
            finish(); // camera isn't available, nothing we can do about that.
        }
    }

    /**
     * A safe way to get an instance of the Camera object.
     */
    public static Camera getCameraInstance(int cameraChoice, Camera currentCamera) {
        Log.i("JKAT", "getCameraInstance called");
        if (currentCamera == null) {
            Camera c = null;
            try {
                c = Camera.open(cameraChoice); // attempt to get a Camera instance
                //  c.setDisplayOrientation(180);
                if (c == null)
                    Log.i(static_TAG, "Returning new camera instance");
            } catch (Exception e) {
                // Camera is not available (in use or does not exist)
                Log.e(static_TAG, "Cannot find camera: " + e.getMessage());

            }
            return c; // returns null if camera is unavailable
        } else {
            Log.i(static_TAG, "Returning found camera instance");
            return currentCamera;
        }
    }

    public void releaseCamera() {
        if (mCamera != null) {
            mCamera.release();        // release the camera for other applications
            mCamera = null;
            Log.i(TAG, "Released camera");

        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        // Log.d("JKAT", "Trying to release camera");
        // releaseCamera();              // release the camera immediately on pause event
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (pictureTaken) {
            Log.d(TAG, "Picture has already been taken");
            finish();
        }
        mCamera = this.getCameraInstance(mWhichCamera, mCamera);
    }

    public final static boolean isValidEmail(CharSequence target) {
        if (target == null) {
            return false;
        } else {
            return android.util.Patterns.EMAIL_ADDRESS.matcher(target).matches();
        }
    }

}