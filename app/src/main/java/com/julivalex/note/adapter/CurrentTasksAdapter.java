package com.julivalex.note.adapter;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.res.Resources;
import android.icu.util.Calendar;
import android.os.Handler;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.julivalex.note.R;
import com.julivalex.note.Utils;
import com.julivalex.note.fragment.CurrentTaskFragment;
import com.julivalex.note.model.Item;
import com.julivalex.note.model.ModelSeparator;
import com.julivalex.note.model.ModelTask;

import de.hdodenhof.circleimageview.CircleImageView;


/**
 * Created by julivalex on 09.09.17.
 */

public class CurrentTasksAdapter extends TaskAdapter {

    private static final int TYPE_TASK = 0;
    private static final int TYPE_SEPARATOR = 1;

    public CurrentTasksAdapter(CurrentTaskFragment taskFragment) {
        super(taskFragment);
    }

    @Override
    public int getItemViewType(int position) {
        //Выбираем тип задачи или разделителя
        if(getItem(position).isTask()) {
            return TYPE_TASK;
        } else {
            return TYPE_SEPARATOR;
        }
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        switch (viewType) {
            //Если тип - задача, по инфлейтим соответствующий layout
            case TYPE_TASK:
                View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.model_task, parent, false);
                TextView title = (TextView) v.findViewById(R.id.tvTaskTitle);
                TextView date = (TextView) v.findViewById(R.id.tvTaskDate);
                CircleImageView priority = (CircleImageView) v.findViewById(R.id.cvTaskPriority);

                return new TaskViewHolder(v, title, date, priority);

            case TYPE_SEPARATOR:
                View separator = LayoutInflater.from(parent.getContext()).inflate(R.layout.model_separator,
                        parent, false);
                TextView type = (TextView) separator.findViewById(R.id.tvSeparatorName);
                return new SeparatorViewHolder(separator, type);

            default:
                return null;
        }

    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, final int position) {
        final Item item = items.get(position);

        final Resources resources = holder.itemView.getResources();

        if(item.isTask()) {
            holder.itemView.setEnabled(true);
            final ModelTask task = (ModelTask) item;
            final TaskViewHolder taskViewHolder = (TaskViewHolder) holder;

            //Устанавливаем цвет фона и элемент видимым, так как он имеет тип task
            final View itemView = taskViewHolder.itemView;

            itemView.setVisibility(View.VISIBLE);
            taskViewHolder.priority.setEnabled(true);

            if(task.getDate() != 0 && task.getDate() < java.util.Calendar.getInstance().getTimeInMillis()) {
                itemView.setBackgroundColor(resources.getColor(R.color.gray_200));
            } else {
                itemView.setBackgroundColor(resources.getColor(R.color.gray_50));
            }

            //По длительному нажатию удаляем элемент
            //Диалог вызывается с задержкой для показа анимации перед его вызовом
            itemView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View view) {
                    Handler handler = new Handler();
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            //Для удаления берем позицию из taskViewHolder
                            getTaskFragment().removeTaskDialog(taskViewHolder.getLayoutPosition());
                        }
                    }, 1000);

                    return true;
                }
            });

            //Достаем заголовок и дату из модели и кладем во viewHolder
            //Данные отобразятся в recyclerView
            taskViewHolder.title.setText(task.getTitle());
            taskViewHolder.title.setTextColor(resources.getColor(R.color.primary_text_default_material_light));

            //Кладем полную дату (дата + время)
            if(task.getDate() != 0) {
                taskViewHolder.date.setText(Utils.getFullDate(task.getDate()));
            } else {
                taskViewHolder.date.setText(null);
            }
            taskViewHolder.date.setTextColor(resources.getColor(R.color.secondary_text_default_material_light));

            //Устанавливаем цвет для отображения CircleImageView
            //в зависимости от приоритета (из модели)
            //Устанавливаем сплошную иконку для невыполненой задачи
            taskViewHolder.priority.setColorFilter(resources.getColor(task.getPriorityColor()));
            taskViewHolder.priority.setImageResource(R.drawable.ic_circle_white_48dp);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    getTaskFragment().showTaskEditDialog(task);
                }
            });

            taskViewHolder.priority.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    taskViewHolder.priority.setEnabled(false);
                    task.setStatus(ModelTask.STATUS_DONE);

                    //Обновление статуса в БД
                    getTaskFragment().activity.dbHelper.update().updateStatus(task.getTimeStamp(), task.getStatus());

                    //Установка блеклого цвета при изменении статуса
                    taskViewHolder.title.setTextColor(resources.getColor(R.color.primary_text_disabled_material_light));
                    taskViewHolder.date.setTextColor(resources.getColor(R.color.secondary_text_disabled_material_light));
                    taskViewHolder.priority.setColorFilter(resources.getColor(task.getPriorityColor()));


                    ObjectAnimator flipIn = ObjectAnimator.ofFloat(taskViewHolder.priority, "rotationY", -180f, 0f);

                    flipIn.addListener(new Animator.AnimatorListener() {
                        @Override
                        public void onAnimationStart(Animator animator) {

                        }

                        @Override
                        public void onAnimationEnd(Animator animator) {
                            //Меняем иконку у проритета на выполненную по завершении анимации
                            if(task.getStatus() == ModelTask.STATUS_DONE) {

                                taskViewHolder.priority.setImageResource(R.drawable.ic_check_circle_white_48dp);

                                ObjectAnimator translationX = ObjectAnimator.
                                        ofFloat(itemView, "translationX", 0f, itemView.getWidth());

                                ObjectAnimator translationXBack = ObjectAnimator
                                        .ofFloat(itemView, "translationX", itemView.getWidth(), 0f);

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
        } else {
            ModelSeparator separator = (ModelSeparator) item;
            SeparatorViewHolder separatorViewHolder = (SeparatorViewHolder) holder;
            separatorViewHolder.type.setText(resources.getString(separator.getType()));
        }
    }
}
