package com.julivalex.note.fragment;


import android.app.Activity;
import android.os.Bundle;
import android.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.julivalex.note.R;
import com.julivalex.note.adapter.DoneTaskAdapter;
import com.julivalex.note.database.DBHelper;
import com.julivalex.note.model.ModelTask;

import java.util.ArrayList;
import java.util.List;


/**
 * A simple {@link Fragment} subclass.
 */
public class DoneTaskFragment extends TaskFragment {

    public DoneTaskFragment() {
    }

    OnTaskRestoreListener onTaskRestoreListener;

    public interface OnTaskRestoreListener {
        void onTaskRestore(ModelTask task);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            onTaskRestoreListener = (OnTaskRestoreListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString() + " must implement OnTaskRestoreListener");
        }
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_done_task, container, false);
        recyclerView = (RecyclerView) rootView.findViewById(R.id.rvDoneTasks);
        layoutManager = new LinearLayoutManager(getActivity());
        recyclerView.setLayoutManager(layoutManager);
        adapter = new DoneTaskAdapter(this);
        recyclerView.setAdapter(adapter);

        return rootView;
    }

    /**
     * Поиск в БД как по заголовку, так и по буквосочетаниям,
     * которые задаются в строке поиска  ("%" + title + "%")
     * @param title - заголовок
     */
    @Override
    public void findTasks(String title) {
        adapter.removeAllItems();
        List<ModelTask> tasks = new ArrayList<>();
        tasks.addAll(activity.dbHelper.query().getTasks(DBHelper.SELECTION_LIKE_TITLE + " AND " + DBHelper.SELECTION_STATUS,
                new String[] { "%" + title + "%", Integer.toString(ModelTask.STATUS_DONE) },
                DBHelper.TASK_DATE_COLUMN));

        for (int i = 0; i < tasks.size(); i++) {
            addTask(tasks.get(i), false);
        }
    }

    /**
     * Метод вызывается один раз при создании активити.
     * В DoneTaskFragment добавляются из базы только выполненные задачи,
     * отсортированные по дате (сортировка по умолчанию идет по возрастанию)
     */
    @Override
    public void addTaskFromDB() {
        adapter.removeAllItems();
        List<ModelTask> tasks = new ArrayList<>();
        tasks.addAll(activity.dbHelper.query().getTasks(DBHelper.SELECTION_STATUS,
                new String[] { Integer.toString(ModelTask.STATUS_DONE) }, DBHelper.TASK_DATE_COLUMN));

        for (int i = 0; i < tasks.size(); i++) {
            addTask(tasks.get(i), false);
        }
    }

    /**
     * Добавление задачи в CurrentTaskFragment
     * @param task - задача
     */
    @Override
    public void moveTask(ModelTask task) {
        if(task.getDate() != 0) {
            alarmHelper.setAlarm(task);
        }
        onTaskRestoreListener.onTaskRestore(task);
    }

    @Override
    public void checkAdapter() {
        if(adapter == null) {
            adapter = new DoneTaskAdapter(this);
            addTaskFromDB();
        }
    }

    /**
     * Метод, который добавляет только задачи без разделителей
     * @param newTask - задача
     * @param saveToDB - параметр сохранения задачи в БД
     */
    @Override
    public void addTask(ModelTask newTask, boolean saveToDB) {
        int position = -1;

        //Определение позиции, в которую вставлять новую задачу (старые задачи по дате вверху)
        for (int i = 0; i < adapter.getItemCount(); i++) {
            if (adapter.getItem(i).isTask()) {
                ModelTask task = (ModelTask) adapter.getItem(i);
                if (newTask.getDate() < task.getDate()) {
                    position = i;
                    break;
                }
            }
        }

        //Если в списке нет ни одного элемента, то просто добавляем элемент
        //Если в списке один и более элементов, то сравниваем даты и добавляем элемент
        if (position != -1) {
            adapter.addItem(position, newTask);
        } else {
            adapter.addItem(newTask);
        }

        //Запись в БД при первом добавлении
        if (saveToDB) {
            activity.dbHelper.saveTask(newTask);
        }
    }
}
