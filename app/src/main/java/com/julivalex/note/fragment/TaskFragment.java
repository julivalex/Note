package com.julivalex.note.fragment;

import android.app.AlertDialog;
import android.app.DialogFragment;
import android.app.Fragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.julivalex.note.MainActivity;
import com.julivalex.note.R;
import com.julivalex.note.adapter.TaskAdapter;
import com.julivalex.note.dialog.EditClassDialogFragment;
import com.julivalex.note.model.Item;
import com.julivalex.note.model.ModelTask;
import com.julivalex.note.receiver.AlarmHelper;

/**
 * Created by julivalex on 10.09.17.
 */

public abstract class TaskFragment extends Fragment {

    protected RecyclerView recyclerView;
    protected RecyclerView.LayoutManager layoutManager;
    protected TaskAdapter adapter;

    public MainActivity activity;

    public AlarmHelper alarmHelper;

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        if(getActivity() != null) {
            activity = (MainActivity) getActivity();
        }

        alarmHelper = AlarmHelper.getInstance();

        //Чтение данных из БД
        addTaskFromDB();
    }

    /**
     * Добавление элементов в список
     *
     * @param newTask - задача
     * @param saveToDB - параметр сохранения задачи в БД
     */
    public abstract void addTask(ModelTask newTask, boolean saveToDB);

    public void updateTask(ModelTask task) {
        adapter.updateTask(task);
    }

    /**
     * Удаление элемента по его позиции
     * @param position - позиция удаляемого элемента
     */
    public void removeTaskDialog(final int position) {
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(getActivity());
        dialogBuilder.setMessage(R.string.dialog_removing_message);

        Item item = adapter.getItem(position);

        if(item.isTask()) {

            ModelTask removingTask = (ModelTask) item;

            final long timeStamp = removingTask.getTimeStamp();
            final boolean[] isRemoved = { false };

            dialogBuilder.setPositiveButton(R.string.dialog_ok, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int witch) {

                    adapter.removeItem(position);
                    isRemoved[0] = true;

                    Snackbar snackbar = Snackbar.make(getActivity().findViewById(R.id.coordinator),
                            R.string.removed, Snackbar.LENGTH_LONG);

                    snackbar.setAction(R.string.dialog_cancel, new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            addTask(activity.dbHelper.query().getTask(timeStamp), false);
                            isRemoved[0] = false;
                        }
                    });
                    snackbar.getView().addOnAttachStateChangeListener(new View.OnAttachStateChangeListener() {

                        //Появляется на экране
                        @Override
                        public void onViewAttachedToWindow(View view) {

                        }

                        //Исчезает с экрана
                        @Override
                        public void onViewDetachedFromWindow(View view) {
                            if(isRemoved[0]) {
                                alarmHelper.removeAlarm(timeStamp);
                                activity.dbHelper.removeTask(timeStamp);
                            }
                        }
                    });

                    snackbar.show();

                    dialog.dismiss();
                }
            });

            dialogBuilder.setNegativeButton(R.string.dialog_cancel, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.cancel();
                }
            });
        }

        dialogBuilder.show();
    }

    public void showTaskEditDialog(ModelTask task) {
        DialogFragment editingTaskDialog = EditClassDialogFragment.newImstance(task);
        editingTaskDialog.show(getActivity().getFragmentManager(), "EditTaskDialogFragment");
    }

    /**
     * Поиск задач по заголовку
     * @param title - заголовок
     */
    public abstract void findTasks(String title);

    /**
     * Чтение данных их БД, выполняется при создании activity
     * Для CurrentTaskFragment и DoneTaskFragment вызывается свой addTaskFromDB
     */
    public abstract void addTaskFromDB();

    /**
     * Добавление task из CurrentTaskFragment в DoneTaskFragment и наоборот
     * @param task - задача
     */
    public abstract void moveTask(ModelTask task);

    public abstract void checkAdapter();
}
