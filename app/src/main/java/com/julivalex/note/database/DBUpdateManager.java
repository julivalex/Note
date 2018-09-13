package com.julivalex.note.database;

import android.content.ContentValues;
import android.database.sqlite.SQLiteDatabase;

import com.julivalex.note.model.ModelTask;

/**
 * Created by julivalex on 16.09.17.
 */

public class DBUpdateManager {

    private SQLiteDatabase db;

    //CRUD (U - update)
    DBUpdateManager(SQLiteDatabase db) {
        this.db = db;
    }

    //Обновить название по значения timeStamp
    public void updateTitle(long timeStamp, String title) {
        update(DBHelper.TASK_TITLE_COLUMN, timeStamp, title);
    }

    //Обновить дату по значению timeStamp
    public void updateDate(long timeStamp, long date) {
        update(DBHelper.TASK_DATE_COLUMN, timeStamp, date);
    }

    //Обновить приоритет по значению timeStamp
    public void updatePriority(long timeStamp, int priority) {
        update(DBHelper.TASK_PRIORITY_COLUMN, timeStamp, priority);
    }

    //Обновить статус по значению timeStamp
    public void updateStatus(long timeStamp, int status) {
        update(DBHelper.TASK_STATUS_COLUMN, timeStamp, status);
    }

    //Обновить весь task (строки)
    public void updateTask(ModelTask task) {
        updateTitle(task.getTimeStamp(), task.getTitle());
        updateDate(task.getTimeStamp(), task.getDate());
        updatePriority(task.getTimeStamp(), task.getPriority());
        updateStatus(task.getTimeStamp(), task.getStatus());
    }

    //Обновляем данные для одной строки value(значение столбца) по key (значение timeStamp)
    private void update(String column, long key, String value) {
        ContentValues cv = new ContentValues();
        cv.put(column, value);
        db.update(DBHelper.TASKS_TABLE, cv, DBHelper.TASK_TIME_STAMP_COLUMN + " = " + key, null);
    }

    //Обновляем данные для одной строки value(значение столбца) по key (значение timeStamp)
    private void update(String column, long key, long value) {
        ContentValues cv = new ContentValues();
        cv.put(column, value);
        db.update(DBHelper.TASKS_TABLE, cv, DBHelper.TASK_TIME_STAMP_COLUMN + " = " + key, null);
    }

}
