package com.julivalex.note.database;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.julivalex.note.model.ModelTask;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by julivalex on 16.09.17.
 */

public class DBQueryManager {

    private SQLiteDatabase db;

    DBQueryManager(SQLiteDatabase db) {
        this.db = db;
    }

    /**
     * CRUD (READ - query)
     * Метод для чтения одной записи в БД, которую выбираем по timeStamp
     *
     * @param timeStamp - уникальный ключ для записей в БД
     * @return - задача
     */
    public ModelTask getTask(long timeStamp) {
        ModelTask modelTask = null;

        Cursor c = db.query(DBHelper.TASKS_TABLE, null, DBHelper.SELECTION_TIME_STAMP,
                new String[]{Long.toString(timeStamp)}, null, null, null);

        if (c.moveToFirst()) {
            String title = c.getString(c.getColumnIndex(DBHelper.TASK_TITLE_COLUMN));
            long date = c.getLong(c.getColumnIndex(DBHelper.TASK_DATE_COLUMN));
            int priority = c.getInt(c.getColumnIndex(DBHelper.TASK_PRIORITY_COLUMN));
            int status = c.getInt(c.getColumnIndex(DBHelper.TASK_STATUS_COLUMN));

            modelTask = new ModelTask(title, date, priority, status, timeStamp);

        }
        c.close();

        return modelTask;
    }

    /**
     * CRUD (READ - query)
     * Метод для чтения всех данных из БД, вызывается после создания activity один раз
     * Для каждого фрагмента свой
     *
     * @param selection     - условие выборки
     * @param selectionArgs - параметры для условия выборки
     * @param orderBy       - сортировка (по умолчанию по возрастанию)
     * @return - список всех задач из БД
     */
    public List<ModelTask> getTasks(String selection, String[] selectionArgs, String orderBy) {

        List<ModelTask> tasks = new ArrayList<>();
        Cursor c = db.query(DBHelper.TASKS_TABLE, null, selection, selectionArgs, null, null, orderBy);

        //Ставим позицию курсора на первую строку выборки
        //если в выборке нет строк, вернется false
        if (c.moveToFirst()) {

            do {
                //Получаем значения по номерам столбцов
                String title = c.getString(c.getColumnIndex(DBHelper.TASK_TITLE_COLUMN));
                long date = c.getLong(c.getColumnIndex(DBHelper.TASK_DATE_COLUMN));
                int priority = c.getInt(c.getColumnIndex(DBHelper.TASK_PRIORITY_COLUMN));
                int status = c.getInt(c.getColumnIndex(DBHelper.TASK_STATUS_COLUMN));
                long timeStamp = c.getLong(c.getColumnIndex(DBHelper.TASK_TIME_STAMP_COLUMN));

                ModelTask modelTask = new ModelTask(title, date, priority, status, timeStamp);
                tasks.add(modelTask);
                //Переход на следующую строку или выходим из цикла
            } while (c.moveToNext());
        }
        c.close();

        return tasks;
    }
}
