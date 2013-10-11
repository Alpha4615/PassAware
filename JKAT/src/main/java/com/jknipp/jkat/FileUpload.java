package com.jknipp.jkat;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.util.Log;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.ByteArrayBody;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.InputStreamReader;


/**
 * Created by Jonathan on 7/28/13.
 */
public class FileUpload {
    private final String TAG = "FileUpload";

    protected String FileName;
    protected Context context;
    protected Bitmap bitmap;
    protected ScheduleItem sItem;
    protected DataHandler writer;
    protected SharedPreferences mPreferences;
    protected ATServ myService;
    protected static Integer APPVERSION = 0;
    protected static final Integer MAX_UPLOAD_ATTEMPTS = 3;

    public FileUpload(Context context, ScheduleItem item, DataHandler writer, SharedPreferences preferences) {
        sItem = item;
        try {
            this.APPVERSION = context.getPackageManager().getPackageInfo(context.getPackageName(), 0).versionCode;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        this.FileName = "/data/data/" + context.getString(R.string.package_name) + "/files/" + item.getFileName();
        this.writer = writer;
        decodeFile(FileName);

        mPreferences = preferences;
    }

    public void doUpload() {
        if (this.sItem.getUploadAttemptCount() < FileUpload.MAX_UPLOAD_ATTEMPTS && sItem.getInProcess() != 1) {

            sItem.setInProcess(1);
            writer.updateItem(sItem);

            new ImageUploadTask().execute();
        } else {
            Log.i(TAG, "Not uploading file, we failed to upload it too many times.");
        }
    }

    public void doUploadFromService(ATServ service) {
        this.myService = service;
        this.doUpload();
    }


    private void decrementUploadsQueuedForUpload() {
        this.myService.decrementUploadsQueuedForUpload();
    }

    class ImageUploadTask extends AsyncTask<Void, Void, String> {
        @Override
        protected String doInBackground(Void... unused) {
            try {

                HttpClient httpClient = new DefaultHttpClient();
                HttpContext localContext = new BasicHttpContext();
                HttpPost httpPost = new HttpPost(
                        /**
                         * @todo Make this stuff constants somewhere
                         */
                        //"http://universium.net/jkatWeb/"
                        // "https://secure3177.hostgator.com/~josh06/jkatWeb/"
                        //"http://thermalninja.com/jkatWeb"
                        "https://lightningcoders.com/jkatWeb"
                                + "/upload.php");

                MultipartEntity entity = new MultipartEntity(
                        HttpMultipartMode.BROWSER_COMPATIBLE);

                ByteArrayOutputStream bos = new ByteArrayOutputStream();
                /**
                 * @TODO Add a variable quality measure for premium users
                 */
                bitmap.compress(Bitmap.CompressFormat.JPEG,
                        30,
                        bos);
                byte[] data = bos.toByteArray();
                entity.addPart("returnformat", new StringBody("json"));
                entity.addPart("appVersion", new StringBody(Integer.toString(APPVERSION)));
                entity.addPart("uploaded", new ByteArrayBody(data,
                        FileName));
                entity.addPart("email", new StringBody(mPreferences.getString("notifications_email", "")));
                httpPost.setEntity(entity);
                HttpResponse response = httpClient.execute(httpPost,
                        localContext);
                BufferedReader reader = new BufferedReader(
                        new InputStreamReader(
                                response.getEntity().getContent(), "UTF-8"));

                String sResponse = reader.readLine();
                return sResponse;
            } catch (Exception e) {

                Log.e(TAG, "Exception " + e.getMessage());
                Log.e(e.getClass().getName(), e.getMessage(), e);
                return null;
            }

            // (null);
        }

        @Override
        protected void onProgressUpdate(Void... unsued) {

        }

        @Override
        protected void onPostExecute(String sResponse) {
            try {
                Log.d(TAG, "onPostExecute");

                sItem.setInProcess(0);

                if (myService != null) { // are we uploading via a call from service instance? then tell the service we're done with this file so the queue counter can be reduced
                    decrementUploadsQueuedForUpload();

                    if (myService.CURRENT_UPLOADS_BEING_PROCESSED == 0) { //we just finished this queue run, should we continue uploading later or kill the service now?
                        Log.i(TAG, "Upload pass complete, checking queue for more!");
                        myService.retryUploads();
                    }
                }
                if (sResponse != null) {
                    JSONObject JResponse = new JSONObject(sResponse);
                    String success = JResponse.getString("SUCCESS");
                    String message = JResponse.getString("MESSAGE");
                    String extra = JResponse.getString("EXTRA");

                    if (success.equals("0")) {
                        Log.e(TAG, "Server uploader returned a well-formed rejection response: " + message);
                        Log.e(TAG, extra);
                        Integer uploadCount = sItem.getUploadAttemptCount();
                        sItem.setReasonScheduled(ScheduleItem.REASON_UPLOADFAILED);
                        sItem.setUploadAttemptCount(uploadCount + 1);
                    } else if (success.equals("-1")) {
                        Log.e(TAG, "Unknown error occurred:" + message);
                    } else if (success.equals("1")) {
                        //update the file data entry to let the OS know it's been uploaded
                        Log.i(TAG, "Server accepted upload");
                        sItem.setTimeCompleted(DataHandler.getTime());
                        Log.d(TAG, JResponse.toString());

                        //we don't need to keep the file locally anymore
                        File deleteme = new File(FileName);
                        deleteme.delete();
                    } else { // the server returned neither a yes or no (server down, script no longer functioning, service closed etc)

                        Log.e(TAG, "Server returned an unknown response. Failure assumed.");
                        Log.d(TAG, sResponse);

                        int uploadCount = sItem.getUploadAttemptCount();
                        sItem.setReasonScheduled(ScheduleItem.REASON_UPLOADFAILED);
                        sItem.setUploadAttemptCount((uploadCount + 1));
                    }
                } else {
                    Log.e(TAG, "Server returned no response. Failure assumed. Killing service.");

                    int uploadCount = sItem.getUploadAttemptCount();
                    sItem.setReasonScheduled(ScheduleItem.REASON_UPLOADFAILED);
                    sItem.setUploadAttemptCount((uploadCount + 1));

                    if (myService != null) {
                        // stop the service to prevent a loop.
                        myService.stopService();
                    }

                    //  throw new Exception("Upload Server returned no response. Possible server-side crash.");

                }

                writer.updateItem(sItem);
            } catch (Exception e) {
                Log.e(TAG, "Exception Reached");
                Log.e(e.getClass().getName(), e.getMessage(), e);
            }
        }
    }

    public void decodeFile(String filePath) {
        // Decode image size
        BitmapFactory.Options o = new BitmapFactory.Options();
        o.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(filePath, o);

        // The new size we want to scale to
        final int REQUIRED_SIZE = 1024;

        // Find the correct scale value. It should be the power of 2.
        int width_tmp = o.outWidth, height_tmp = o.outHeight;
        int scale = 1;
        while (true) {
            if (width_tmp < REQUIRED_SIZE && height_tmp < REQUIRED_SIZE)
                break;
            width_tmp /= 2;
            height_tmp /= 2;
            scale *= 2;
        }

        // Decode with inSampleSize
        BitmapFactory.Options o2 = new BitmapFactory.Options();
        o2.inSampleSize = scale;
        bitmap = BitmapFactory.decodeFile(filePath, o2);
    }

}