package com.jknipp.jkat;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Jonathan on 7/28/13.
 */
public class DataHandler extends SQLiteOpenHelper {
    private final String TAG = "DataHandler";

    // All Static variables
    // Database Version
    private static final int DATABASE_VERSION = 3;

    // Database Name
    private static final String DATABASE_NAME = "theftMitigation";

    // Contacts table name
    private static final String TABLE_SCHEDULE = "schedule";

    // Contacts Table Columns names
    private static final String KEY_ID = "ID";
    private static final String KEY_FILENAME = "filename";
    private static final String KEY_IN_PROCESS = "in_process";
    private static final String KEY_TIME_SCHEDULED = "time_scheduled";
    private static final String KEY_TIME_COMPLETED = "time_completed";
    private static final String KEY_REASON_SCHEDULED = "reason_scheduled";
    private static final String KEY_UPLOAD_ATTEMPT_COUNT = "upload_attempt_count";


    public DataHandler(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    // Creating Tables
    @Override
    public void onCreate(SQLiteDatabase db) {
        String CREATE_SCHEDULE_TABLE = "CREATE TABLE " + TABLE_SCHEDULE + "("
                + KEY_ID + " INTEGER PRIMARY KEY," + KEY_FILENAME + " TEXT,"
                + KEY_TIME_SCHEDULED + " INTEGER," + KEY_TIME_COMPLETED + " INTEGER,"
                + KEY_UPLOAD_ATTEMPT_COUNT + " INTEGER," + KEY_REASON_SCHEDULED + " INTEGER," +
                KEY_IN_PROCESS + " INTEGER" +
                ")";

        db.execSQL(CREATE_SCHEDULE_TABLE);
    }

    // Upgrading database
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
    Log.d(TAG, "The oldVersion is "+oldVersion+" new version is "+newVersion);
        if (oldVersion < 2) {
            String ADD_UPLOAD_ATTEMPT_COUNT = "ALTER TABLE " + TABLE_SCHEDULE + " ADD COLUMN " + KEY_UPLOAD_ATTEMPT_COUNT + " INTEGER";
            db.execSQL(ADD_UPLOAD_ATTEMPT_COUNT);
        }
        if (oldVersion < 3) {
            String ADD_IN_PROCESS = "ALTER TABLE " + TABLE_SCHEDULE + " ADD COLUMN " + KEY_IN_PROCESS + " INTEGER";
            db.execSQL(ADD_IN_PROCESS);
        }

    }

    public void addSchedule(ScheduleItem item) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(KEY_FILENAME, item.fileName);
        values.put(KEY_REASON_SCHEDULED, item.reasonScheduled);
        values.put(KEY_TIME_SCHEDULED, item.timeScheduled);
        values.put(KEY_TIME_COMPLETED, item.timeCompleted);
        values.put(KEY_UPLOAD_ATTEMPT_COUNT, item.uploadAttemptCount);
        values.put(KEY_IN_PROCESS, item.inProcess);

        // Inserting Row
        long newID = db.insert(TABLE_SCHEDULE, null, values);
        item.setID((int) newID);
        db.close(); // Closing database connection
    }

    public ScheduleItem getItem(int id) {
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.query(TABLE_SCHEDULE, new String[]{KEY_ID,
                KEY_FILENAME, KEY_TIME_SCHEDULED, KEY_REASON_SCHEDULED, KEY_TIME_COMPLETED, KEY_UPLOAD_ATTEMPT_COUNT, KEY_IN_PROCESS}, KEY_ID + "=?",
                new String[]{String.valueOf(id)}, null, null, null, null);
        if (cursor != null)
            cursor.moveToFirst();

        ScheduleItem item = new ScheduleItem(Integer.parseInt(cursor.getString(0)), // ID
                cursor.getString(1),  //File name
                Long.parseLong(cursor.getString(2)), // time scheduled
                Integer.parseInt(cursor.getString(3)), // reason scheduled
                Long.parseLong(cursor.getString(4)), // time completed
                Integer.parseInt(cursor.getString(5)), // upload attempt count
                Integer.parseInt(cursor.getString(6)) // in process
                );

        // return item
        return item;
    }

    public List<ScheduleItem> getAllItems() {
        List<ScheduleItem> scheduleItemList = new ArrayList<ScheduleItem>();
        // Select All Query
      //  String selectQuery = "SELECT  * FROM " + TABLE_SCHEDULE;



        SQLiteDatabase db = this.getWritableDatabase();
    //    Cursor cursor = db.rawQuery(selectQuery, null);

      Cursor cursor =  db.query(TABLE_SCHEDULE, new String[]{KEY_ID,
                KEY_FILENAME, KEY_TIME_SCHEDULED, KEY_REASON_SCHEDULED, KEY_TIME_COMPLETED, KEY_UPLOAD_ATTEMPT_COUNT, KEY_IN_PROCESS}, null,
                null, null, null, null, null);

        // looping through all rows and adding to list
        if (cursor.moveToFirst()) {
            do {
                ScheduleItem contact = new ScheduleItem();
                contact.setID(Integer.parseInt(cursor.getString(0))); /// ID
                contact.setFileName(cursor.getString(1)); //file name
                contact.setTimeScheduled(Long.parseLong(cursor.getString(2))); // time scheduled
                contact.setReasonScheduled(Integer.parseInt(cursor.getString(3))); // reasons scheduled
                contact.setTimeCompleted(Long.parseLong(cursor.getString(4))); // time completed
                contact.setUploadAttemptCount(Integer.parseInt(cursor.getString(5)));
                contact.setInProcess(Integer.parseInt(cursor.getString(6)));


                // Adding contact to list
                scheduleItemList.add(contact);
            } while (cursor.moveToNext());
        }

        return scheduleItemList;
    }

    public List<ScheduleItem> getPendingItems() {
        List<ScheduleItem> scheduleItemList = new ArrayList<ScheduleItem>();
        // Select All Query
        String selectQuery = "SELECT  " +
                KEY_ID + "," +
                KEY_FILENAME + "," +
                KEY_TIME_SCHEDULED + "," +
                KEY_TIME_COMPLETED + "," +
                KEY_REASON_SCHEDULED + "," +
                KEY_UPLOAD_ATTEMPT_COUNT + "," +
                KEY_IN_PROCESS +
                " FROM " + TABLE_SCHEDULE + " WHERE " + KEY_TIME_COMPLETED + " = 0 "
                   + " AND " + KEY_UPLOAD_ATTEMPT_COUNT + " < " + FileUpload.MAX_UPLOAD_ATTEMPTS
                   + " AND " + KEY_IN_PROCESS + " != 1";

        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        // looping through all rows and adding to list
        if (cursor.moveToFirst()) {
            do {
                ScheduleItem contact = new ScheduleItem();
                contact.setID(Integer.parseInt(cursor.getString(0)));
                contact.setFileName(cursor.getString(1));
                contact.setTimeScheduled(Long.parseLong(cursor.getString(2)));
                contact.setTimeCompleted(Long.parseLong(cursor.getString(3)));
                contact.setReasonScheduled(Integer.parseInt(cursor.getString(4)));
                contact.setUploadAttemptCount((Integer.parseInt(cursor.getString(5))));
                contact.setInProcess((Integer.parseInt(cursor.getString(6))));

                // Adding contact to list
                scheduleItemList.add(contact);
            } while (cursor.moveToNext());
        }

        return scheduleItemList;
    }

    public int getItemsCount() {
        String countQuery = "SELECT  * FROM " + TABLE_SCHEDULE;
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(countQuery, null);
        cursor.close();

        // return count
        return cursor.getCount();
    }

    public int updateItem(ScheduleItem item) {
        SQLiteDatabase db = this.getWritableDatabase();
        String WHERE = KEY_ID + "=?";
        String[] whereArgs = new String[]{String.valueOf(item.getID())};

        ContentValues values = new ContentValues();

        values.put(KEY_FILENAME, item.getFileName());
        values.put(KEY_REASON_SCHEDULED, item.getReasonScheduled());
        values.put(KEY_TIME_SCHEDULED, item.getTimeScheduled());
        values.put(KEY_TIME_COMPLETED, item.getTimeCompleted());
        values.put(KEY_UPLOAD_ATTEMPT_COUNT, item.getUploadAttemptCount());
        values.put(KEY_IN_PROCESS, item.getInProcess());

        Log.i(TAG, "New inProcess! "+ item.getInProcess());
        int result = db.update(TABLE_SCHEDULE, values, WHERE, whereArgs);

        db.close();

        return result;
    }

    public void deleteItem(ScheduleItem item) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_SCHEDULE, KEY_ID + " = ?",
                new String[]{String.valueOf(item.getID())});
        db.close();
    }

    public static long getTime() {
        return System.currentTimeMillis() / 1000L;
    }
}