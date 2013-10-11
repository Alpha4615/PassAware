package com.jknipp.jkat;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import java.io.File;
import java.util.List;

public class debug extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        BugTracker.initialize(this);
        setContentView(R.layout.activity_debug);
        // Show the Up button in the action bar.
        setupActionBar();

        getFiles(this);


        final Button button = (Button) findViewById(R.id.forceClose);
        button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                fakeError(v);
            }
        });

    }

    public void fakeError(View view) {
        int i = 35 / 0;
    }

    public void getPendingFiles(View view) {
        DataHandler handler = new DataHandler(this);
        List<ScheduleItem> pending = handler.getPendingItems();

        for (int i = 0; i < pending.size(); i++) {
            ScheduleItem workWith = pending.get(i);
            Log.i("PendingCounter", workWith.getFileName());
        }
    }

    /* public void forceClose(view View) {
        int zerp = 5/0;
    }*/

    /**
     * Set up the {@link android.app.ActionBar}, if the API is available.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    private void setupActionBar() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            getActionBar().setDisplayHomeAsUpEnabled(true);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.debug, menu);
        return true;
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                // This ID represents the Home or Up button. In the case of this
                // activity, the Up button is shown. Use NavUtils to allow users
                // to navigate up one level in the application structure. For
                // more details, see the Navigation pattern on Android Design:
                //
                // http://developer.android.com/design/patterns/navigation.html#up-vs-back
                //
                NavUtils.navigateUpFromSameTask(this);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private static void getFiles(Context context) {
        String path = "/data/data/com.jknipp.jkat/files";
        int counter = 0;
        Log.d("Files", "Path: " + path);
        try {
            File f = new File(path);
            File file[] = f.listFiles();
            Log.d("Files", "Size: " + file.length);
            for (int i = 0; i < file.length; i++) {
                Log.d("Files", "FileName:" + file[i].getName());
                counter++;
            }
            Toast toast = Toast.makeText(context, "There are " + counter + " files!", Toast.LENGTH_LONG);
            toast.show();
        } catch (NullPointerException e) {
            Toast toast = Toast.makeText(context, "No files!", Toast.LENGTH_LONG);
            toast.show();
        }
    }
}
