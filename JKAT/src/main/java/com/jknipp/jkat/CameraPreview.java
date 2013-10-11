package com.jknipp.jkat;

import android.content.Context;
import android.content.SharedPreferences;
import android.hardware.Camera;
import android.os.Handler;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;

/**
 * A basic Camera preview class
 */
public class CameraPreview extends SurfaceView implements SurfaceHolder.Callback {
    private final String TAG = "CameraPreview";

    private SurfaceHolder mHolder;
    private Camera mCamera;
    private Camera.PictureCallback mJpegCallback;
    private SharedPreferences mPreferences;
    private Context mContext;

    public CameraPreview(Context context, Camera camera, Camera.PictureCallback jpegCallback) {
        super(context);
        mCamera = camera;
        mJpegCallback = jpegCallback;
        mContext = context;
        // Install a SurfaceHolder.Callback so we get notified when the
        // underlying surface is created and destroyed.
        mHolder = getHolder();
        mHolder.addCallback(this);
        // deprecated setting, but required on Android versions prior to 3.0
        mHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
    }

    public void surfaceCreated(SurfaceHolder holder) {
        // The Surface has been created, now tell the camera where to draw the preview.
        try {
            mCamera.setPreviewDisplay(holder);
            mCamera.startPreview();
        } catch (IOException e) {
            Log.d(TAG, "Error setting camera preview: " + e.getMessage());
        }
    }

    public void surfaceDestroyed(SurfaceHolder holder) {
        // empty. Take care of releasing the Camera preview in your activity.
    }

    public void surfaceChanged(SurfaceHolder holder, int format, int w, int h) {
        // If your preview can change or rotate, take care of those events here.
        // Make sure to stop the preview before resizing or reformatting it.
        Log.d(TAG, "SurfaceChanged called");
        if (mHolder.getSurface() == null) {
            // preview surface does not exist
            return;
        }

        // stop preview before making changes
        try {
            mCamera.stopPreview();
        } catch (Exception e) {
            // ignore: tried to stop a non-existent preview
        }

        // set preview size and make any resize, rotate or
        // reformatting changes here

        // start preview with new settings
        try {
            mCamera.setPreviewDisplay(mHolder);
            mCamera.startPreview();
            mPreferences = mContext.getSharedPreferences(Const.PREFERENCES_FILE, Context.MODE_PRIVATE);

            int delay = Integer.parseInt(mPreferences.getString("delay", "0"));

            final Handler handler = new Handler();
            Timer t = new Timer();
            Log.d(TAG, "Sleeping for " + delay);
            t.schedule(new TimerTask() {
                public void run() {
                    handler.post(new Runnable() {
                        public void run() {
                            Log.d(TAG, "dreaming!");
                            cameraInterfaceActivity.muteShutter();
                            mCamera.takePicture(null, null, mJpegCallback);
                            cameraInterfaceActivity.unmuteShutter();
                        }
                    });
                }
            }, delay);
            Log.d(TAG, "woke up! :)");
        } catch (Exception e) {
            e.printStackTrace();
            Log.d(TAG, "Error starting camera preview: " + e.getMessage());
        }
    }
}