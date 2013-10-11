package com.jknipp.jkat;

import android.util.Log;

/**
 * Created by Jonathan on 7/28/13.
 */
public class ScheduleItem {

    protected int _id;
    protected int ID;
    protected String fileName;
    protected long timeScheduled;
    protected int reasonScheduled;
    protected long timeCompleted;
    protected int uploadAttemptCount;
    protected int inProcess;

    public static final int REASON_NONETWORK = 1;
    public static final int REASON_USERPREFERENCES = 2;
    public static final int REASON_REGULARQUEUE = 4;
    public static final int REASON_UPLOADFAILED = 8;
    public static final int REASON_INVALIDEMAIL = 16;

    public ScheduleItem() {

    }

    public ScheduleItem(int id, String fileName, long timeScheduled, int reasonScheduled, long timeCompleted, int uploadAttemptCount, int inProcess) {
        this._id = id;
        this.ID = id;
        this.fileName = fileName;
        this.timeScheduled = timeScheduled;
        this.reasonScheduled = reasonScheduled;
        this.timeCompleted = timeCompleted;
        this.uploadAttemptCount = uploadAttemptCount;
        this.inProcess = inProcess;
    }

    public int getID() {
        return this._id;
    }

    public int getReasonScheduled() {
        return this.reasonScheduled;
    }

    public String getFileName() {
        return this.fileName;
    }

    public long getTimeScheduled() {
        return this.timeScheduled;
    }

    public long getTimeCompleted() {
        return this.timeCompleted;
    }

    public int getUploadAttemptCount() {
        return this.uploadAttemptCount;
    }

    public int getInProcess() {
        return this.inProcess;
    }

    public ScheduleItem setID(int id) {
        this._id = id;
        this.ID = id;
        return this;
    }

    public ScheduleItem setReasonScheduled(int reasonScheduled) {
        this.reasonScheduled = reasonScheduled;
        return this;
    }

    public ScheduleItem setFileName(String fileName) {
        this.fileName = fileName;
        return this;
    }

    public ScheduleItem setTimeScheduled(long timeScheduled) {
        this.timeScheduled = timeScheduled;
        return this;
    }

    public ScheduleItem setTimeCompleted(long timeCompleted) {
        this.timeCompleted = timeCompleted;
        return this;
    }

    public ScheduleItem setUploadAttemptCount(int uploadAttemptCount) {
        this.uploadAttemptCount = uploadAttemptCount;
        return this;
    }

    public ScheduleItem setInProcess(int inProcess) {
        Log.d("Ugh", "For "+this._id+ "Old inProcess "+this.inProcess+" New "+inProcess);
        this.inProcess = (inProcess == 1 ? 1 : 0);
        return this;
    }


}
