package com.julivalex.note.dialog;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.design.widget.TextInputLayout;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TimePicker;

import com.julivalex.note.R;
import com.julivalex.note.Utils;
import com.julivalex.note.model.ModelTask;
import com.julivalex.note.receiver.AlarmHelper;

import java.util.Calendar;

/**
 * Created by julivalex on 11/6/2017.
 */

public class EditClassDialogFragment extends DialogFragment {

    public static EditClassDialogFragment newImstance(ModelTask task) {
        EditClassDialogFragment editClassDialogFragment = new EditClassDialogFragment();

        Bundle args = new Bundle();
        args.putString("title", task.getTitle());
        args.putLong("date", task.getDate());
        args.putInt("priority", task.getPriority());
        args.putLong("time_stamp", task.getTimeStamp());

        editClassDialogFragment.setArguments(args);
        return editClassDialogFragment;
    }

    private EditingTaskListener editingTaskListener;

    public interface EditingTaskListener {
        void onTaskEdited(ModelTask updateTask);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            editingTaskListener = (EditingTaskListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString() + " must implement EditingTaskListener");
        }
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        Bundle args = getArguments();
        String title = args.getString("title");
        long date = args.getLong("date", 0);
        int priority = args.getInt("priority", 0);
        long timeStamp = args.getLong("time_stamp", 0);

        final ModelTask task = new ModelTask(title, date, priority, 0, timeStamp);

        final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(R.string.dialog_editing_title);

        LayoutInflater inflater = getActivity().getLayoutInflater();

        View container = inflater.inflate(R.layout.dialog_task, null);

        final TextInputLayout tilTitle = (TextInputLayout) container.findViewById(R.id.tilDialogTaskTitle);
        final EditText etTitle = tilTitle.getEditText();

        final TextInputLayout tilDate = (TextInputLayout) container.findViewById(R.id.tilDialogTaskDate);
        final EditText etDate = tilDate.getEditText();

        TextInputLayout tilTime = (TextInputLayout) container.findViewById(R.id.tilDialogTaskTime);
        final EditText etTime = tilTime.getEditText();

        //Инициализация spinner
        Spinner spPriority = (Spinner) container.findViewById(R.id.spDialogTaskPriority);

        if(etTitle != null && etDate != null && etTime != null) {

            etTitle.setText(task.getTitle());
            etTitle.setSelection(etTitle.length());
            if (task.getDate() != 0) {
                etDate.setText(Utils.getDate(task.getDate()));
                etTime.setText(Utils.getTime(task.getDate()));
            }

            tilTitle.setHint(getResources().getString(R.string.task_title));
            tilDate.setHint(getResources().getString(R.string.task_date));
            tilTime.setHint(getResources().getString(R.string.task_time));

            builder.setView(container);

            //Cоздание адаптера для спинера
            ArrayAdapter<String> adapter = new ArrayAdapter<String>(getActivity(),
                    android.R.layout.simple_spinner_dropdown_item, ModelTask.PRIORITY_LEVELS);

            spPriority.setAdapter(adapter);
            spPriority.setSelection(task.getPriority());
            spPriority.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> adapterView, View view, int position, long l) {
                    //Сохраняем в модель значение приоритета, выбраного из спиннера на диалоговом окне
                    task.setPriority(position);
                }

                @Override
                public void onNothingSelected(AdapterView<?> adapterView) {

                }
            });

            //Если указана только дата без времени, то добавляем час к текущему времени
            final Calendar calendar = Calendar.getInstance();
            calendar.set(Calendar.HOUR_OF_DAY, calendar.get(Calendar.HOUR_OF_DAY) + 1);
            if (etDate.length() != 0 || etTime.length() != 0) {
                calendar.setTimeInMillis(task.getDate());
            }

            etDate.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    if (etDate.length() == 0) {
                        etDate.setText(" ");
                    }

                    DialogFragment datePickerFragment = new DatePickerFragment() {
                        @Override
                        public void onDateSet(DatePicker datePicker, int year, int month, int day) {
                            calendar.set(Calendar.YEAR, year);
                            calendar.set(Calendar.MONTH, month);
                            calendar.set(Calendar.DAY_OF_MONTH, day);
                            etDate.setText(Utils.getDate(calendar.getTimeInMillis()));
                        }

                        @Override
                        public void onCancel(DialogInterface dialog) {
                            etDate.setText(null);
                        }
                    };
                    datePickerFragment.show(getFragmentManager(), "DatePickerFragment");
                }
            });

            etTime.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View view) {

                    if (etTime.length() == 0) {
                        etTime.setText(" ");
                    }

                    DialogFragment timePickerFragment = new TimePickerFragment() {
                        @Override
                        public void onTimeSet(TimePicker timePicker, int hourOfDay, int minute) {
                            calendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
                            calendar.set(Calendar.MINUTE, minute);
                            calendar.set(Calendar.SECOND, 0);
                            etTime.setText(Utils.getTime(calendar.getTimeInMillis()));
                        }

                        @Override
                        public void onCancel(DialogInterface dialog) {
                            etTime.setText(null);
                        }
                    };
                    timePickerFragment.show(getFragmentManager(), "TimePickerFragment");
                }
            });

            //Создание и нажатие кнопки ОК
            builder.setPositiveButton(R.string.dialog_ok, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    //Сохраняем в модель заголовок и дату(кладем в модель полную дату и время)
                    task.setTitle(etTitle.getText().toString());
                    task.setStatus(ModelTask.STATUS_CURRENT);
                    if (etDate.length() != 0 || etTime.length() != 0) {
                        task.setDate(calendar.getTimeInMillis());

                        AlarmHelper alarmHelper = AlarmHelper.getInstance();
                        alarmHelper.setAlarm(task);
                    }

                    task.setStatus(ModelTask.STATUS_CURRENT);
                    editingTaskListener.onTaskEdited(task);
                    dialogInterface.dismiss();
                }
            });

            //Создание и нажатие кнопки Cancel
            builder.setNegativeButton(R.string.dialog_cancel, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    dialogInterface.cancel();
                }
            });

            //Создание диалога и навешивание листнера при показе диалогового окна
            AlertDialog alertDialog = builder.create();

            //Если при первом показе диалога заголовка нет, то блокируем кнопку ОК
            //и вызываем сообщение об в поле заголовка.
            //Применили setOnShowListener, чтобы добраться к positiveButton
            alertDialog.setOnShowListener(new DialogInterface.OnShowListener() {
                @Override
                public void onShow(DialogInterface dialog) {
                    final Button positiveButton = ((AlertDialog) dialog).getButton(DialogInterface.BUTTON_POSITIVE);
                    if (etTitle.length() == 0) {
                        positiveButton.setEnabled(false);
                        tilTitle.setError(getResources().getString(R.string.dialog_error_empty_title));
                    }

                    etTitle.addTextChangedListener(new TextWatcher() {
                        @Override
                        public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                        }

                        @Override
                        public void onTextChanged(CharSequence s, int i, int i1, int i2) {
                            if (s.length() == 0) {
                                positiveButton.setEnabled(false);
                                tilTitle.setError(getResources().getString(R.string.dialog_error_empty_title));
                            } else {
                                positiveButton.setEnabled(true);
                                tilTitle.setErrorEnabled(false);
                            }
                        }

                        @Override
                        public void afterTextChanged(Editable editable) {

                        }
                    });
                }
            });

            return alertDialog;
        }
        return null;
    }
}