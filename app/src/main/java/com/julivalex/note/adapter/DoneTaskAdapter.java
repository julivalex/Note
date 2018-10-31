package com.julivalex.note.adapter;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.res.Resources;
import android.os.Handler;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.julivalex.note.R;
import com.julivalex.note.Utils;
import com.julivalex.note.fragment.DoneTaskFragment;
import com.julivalex.note.model.Item;
import com.julivalex.note.model.ModelTask;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * Created by julivalex on 11.09.17.
 */

public class DoneTaskAdapter extends TaskAdapter {

    public DoneTaskAdapter(DoneTaskFragment taskFragment) {
        super(taskFragment);
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.model_task, parent, false);
        TextView title = (TextView) v.findViewById(R.id.tvTaskTitle);
        TextView date = (TextView) v.findViewById(R.id.tvTaskDate);
        CircleImageView priority = (CircleImageView) v.findViewById(R.id.cvTaskPriority);

        return new TaskViewHolder(v, title, date, priority);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, final int position) {
        final Item item = items.get(position);

        if (item.isTask()) {
            holder.itemView.setEnabled(true);
            final ModelTask task = (ModelTask) item;
            final TaskViewHolder taskViewHolder = (TaskViewHolder) holder;

            //Устанавливаем цвет фона и элемент видимым, так как он имеет тип task
            final View itemView = taskViewHolder.itemView;
            final Resources resources = itemView.getResources();
            itemView.setVisibility(View.VISIBLE);
            taskViewHolder.priority.setEnabled(true);


            itemView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View view) {

                    Handler handler = new Handler();
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            getTaskFragment().removeTaskDialog(taskViewHolder.getLayoutPosition());
                        }
                    }, 1000);

                    return true;
                }
            });

            //Достаем заголовок и дату из модели и кладем во viewHolder
            //Данные отобразятся в recyclerView
            taskViewHolder.title.setText(task.getTitle());
            taskViewHolder.title.setTextColor(resources.getColor(R.color.primary_text_disabled_material_light));

            //Кладем полную дату (дата + время)
            if (task.getDate() != 0) {
                taskViewHolder.date.setText(Utils.getFullDate(task.getDate()));
            } else {
                taskViewHolder.date.setText(null);
            }
            taskViewHolder.date.setTextColor(resources.getColor(R.color.secondary_text_disabled_material_light));

            //Устанавливаем цвет для отображения CircleImageView
            //в зависимости от приоритета (из модели)
            //Устанавливаем сплошную иконку для невыполненой задачи
            taskViewHolder.priority.setColorFilter(resources.getColor(task.getPriorityColor()));
            taskViewHolder.priority.setImageResource(R.drawable.ic_check_circle_white_48dp);


            taskViewHolder.priority.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    taskViewHolder.priority.setEnabled(false);
                    task.setStatus(ModelTask.STATUS_CURRENT);
                    //Обновление статуса в БД
                    getTaskFragment().activity.dbHelper.update().updateStatus(task.getTimeStamp(), task.getStatus());

                    //Установка яркого цвета при изменении статуса
                    taskViewHolder.title.setTextColor(resources.getColor(R.color.primary_text_default_material_light));
                    taskViewHolder.date.setTextColor(resources.getColor(R.color.secondary_text_default_material_light));
                    taskViewHolder.priority.setColorFilter(resources.getColor(task.getPriorityColor()));


                    ObjectAnimator flipIn = ObjectAnimator.ofFloat(taskViewHolder.priority, "rotationY", 180f, 0f);
                    taskViewHolder.priority.setImageResource(R.drawable.ic_circle_white_48dp);

                    flipIn.addListener(new Animator.AnimatorListener() {
                        @Override
                        public void onAnimationStart(Animator animator) {

                        }

                        @Override
                        public void onAnimationEnd(Animator animator) {
                            //Меняем иконку у проритета на выполненную по завершении анимации
                            if (task.getStatus() != ModelTask.STATUS_DONE) {

                                ObjectAnimator translationX = ObjectAnimator.
                                        ofFloat(itemView.getWidth(), "translationX", 0f, -itemView.getWidth());

                                ObjectAnimator translationXBack = ObjectAnimator
                                        .ofFloat(itemView.getWidth(), "translationX", -itemView.getWidth(), 0f);

                                translationX.addListener(new Animator.AnimatorListener() {
                                    @Override
                                    public void onAnimationStart(Animator animator) {
                                    }

                                    @Override
                                    public void onAnimationEnd(Animator animator) {
                                        itemView.setVisibility(View.GONE);
                                        getTaskFragment().moveTask(task);
                                        removeItem(taskViewHolder.getLayoutPosition());
                                    }

                                    @Override
                                    public void onAnimationCancel(Animator animator) {

                                    }

                                    @Override
                                    public void onAnimationRepeat(Animator animator) {

                                    }
                                });

                                AnimatorSet translationSet = new AnimatorSet();
                                translationSet.play(translationX).before(translationXBack);
                                translationSet.start();
                            }
                        }

                        @Override
                        public void onAnimationCancel(Animator animator) {

                        }

                        @Override
                        public void onAnimationRepeat(Animator animator) {

                        }
                    });

                    flipIn.start();
                }
            });
        }
    }
}
