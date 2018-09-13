package com.julivalex.note.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import com.julivalex.note.fragment.TaskFragment;
import com.julivalex.note.model.Item;
import com.julivalex.note.model.ModelSeparator;
import com.julivalex.note.model.ModelTask;

import java.util.ArrayList;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * Created by julivalex on 10.09.17.
 */

public abstract class TaskAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    List<Item> items;
    TaskFragment taskFragment;

    public boolean containsSeparatorOverdue;
    public boolean containsSeparatorToday;
    public boolean containsSeparatorTomorrow;
    public boolean containsSeparatorFuture;


    public TaskAdapter(TaskFragment taskFragment) {
        this.taskFragment = taskFragment;
        items = new ArrayList<>();
    }

    //Взять задачу из списка по позиции
    public Item getItem(int position) {
        return items.get(position);
    }

    //Добавление задачи в список
    public void addItem(Item item) {
        items.add(item);
        //Есть анимация при добавлении
        notifyItemInserted(getItemCount() - 1);
    }

    //Добавление задачи в список задач по позиции
    public void addItem(int position, Item item) {
        items.add(position, item);
        //Есть анимация при добавлении
        notifyItemInserted(position);
    }

    /**
     * Обновление задачи
     * @param newTask - задача
     */
    public void updateTask(ModelTask newTask) {
        for(int i = 0; i < getItemCount(); i++) {
            if(getItem(i).isTask()) {
                ModelTask task = (ModelTask) getItem(i);
                if(newTask.getTimeStamp() == task.getTimeStamp()) {
                    removeItem(i);
                    getTaskFragment().addTask(newTask, false);
                }
            }
        }
    }

    //Удаление задачи из списка
    public void removeItem(int position) {
        if(position >= 0 && position <= getItemCount() - 1) {
            items.remove(position);
            notifyItemRemoved(position);

            //удаление разделителя без задач
            if(position - 1 >= 0 && position <= getItemCount() - 1) {
                if(!getItem(position).isTask() && !getItem(position - 1).isTask()) {
                    ModelSeparator separator = (ModelSeparator) getItem(position - 1);
                    checkSeparators(separator.getType());

                    items.remove(position - 1);
                    notifyItemRemoved(position - 1);
                }
            } else if(getItemCount() - 1 >= 0 && !getItem(getItemCount() - 1).isTask()) {
                ModelSeparator separator = (ModelSeparator) getItem(getItemCount() - 1);
                checkSeparators(separator.getType());

                int positionTemp = getItemCount() - 1;
                items.remove(positionTemp);
                notifyItemRemoved(positionTemp);
            }
        }
    }

    /**
     * Определить тип сепаратора и установить в него false
     * @param type - тип сепаратора
     */
    public void checkSeparators(int type) {
        switch (type) {
            case ModelSeparator.TYPE_OVERDUE:
                containsSeparatorOverdue = false;
                break;
            case ModelSeparator.TYPE_TODAY:
                containsSeparatorToday = false;
                break;
            case ModelSeparator.TYPE_TOMORROW:
                containsSeparatorTomorrow = false;
                break;
            case ModelSeparator.TYPE_FUTURE:
                containsSeparatorFuture = false;
                break;
        }
    }

    //Удаление всех записей в списке
    public void removeAllItems() {
        if(getItemCount() != 0) {
            items = new ArrayList<>();
            notifyDataSetChanged();
            containsSeparatorOverdue = false;
            containsSeparatorToday = false;
            containsSeparatorTomorrow = false;
            containsSeparatorFuture = false;
        }
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    protected class TaskViewHolder extends RecyclerView.ViewHolder {
        protected TextView title;
        protected TextView date;
        protected CircleImageView priority;

        public TaskViewHolder(View itemView, TextView title, TextView date, CircleImageView priority) {
            super(itemView);
            this.title = title;
            this.date = date;
            this.priority = priority;
        }
    }

    protected class SeparatorViewHolder extends RecyclerView.ViewHolder {

        protected TextView type;

        public SeparatorViewHolder(View itemView, TextView type) {
            super(itemView);
            this.type = type;
        }
    }

    public TaskFragment getTaskFragment() {
        return taskFragment;
    }
}
