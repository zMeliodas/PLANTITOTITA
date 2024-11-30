package com.meliodas.plantitotita.fragments;

import android.app.AlertDialog;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import androidx.appcompat.widget.SwitchCompat;
import androidx.fragment.app.Fragment;
import com.bumptech.glide.Glide;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.imageview.ShapeableImageView;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.meliodas.plantitotita.R;
import com.meliodas.plantitotita.mainmodule.DatabaseManager;
import com.meliodas.plantitotita.mainmodule.Reminder;

import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

public class ReminderFragment extends Fragment {
    private DatabaseManager databaseManager;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the fragment layout
        return inflater.inflate(R.layout.fragment_reminder, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Initialize views
        CalendarView calendarView = view.findViewById(R.id.calendarView);
        FloatingActionButton addReminderFab = view.findViewById(R.id.addReminderFab);
        LinearLayout plantGalleryLayout = view.findViewById(R.id.plantGalleryLayout);
        databaseManager = new DatabaseManager();

        updateReminders();
    }

    private void showAddReminderAlertDialog(Calendar selectedDate, Reminder reminder) {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());

        View alertDialogView = LayoutInflater.from(requireContext()).inflate(R.layout.custom_alert_dialog_reminder_pop_upp, null);
        builder.setView(alertDialogView);

        // Initialize alert dialog views
        SwitchCompat repeatSwitch = alertDialogView.findViewById(R.id.switchCompat);
        AutoCompleteTextView taskInput = alertDialogView.findViewById(R.id.autoCompleteTxtView);
        TextInputLayout otherTaskInput = alertDialogView.findViewById(R.id.otherTaskInput);
        AutoCompleteTextView plantInput = alertDialogView.findViewById(R.id.autoCompleteTxtView1);
        TimePicker timePicker = alertDialogView.findViewById(R.id.timePicker);

        TextView dateTextView = alertDialogView.findViewById(R.id.dateTextView);
        TextView btnOkay = alertDialogView.findViewById(R.id.btnOkay);
        TextView btnCancel = alertDialogView.findViewById(R.id.btnCancel);
        TextView btnRepeatDays = alertDialogView.findViewById(R.id.btnRepeatDays);
        TextView addReminderTitle = alertDialogView.findViewById(R.id.addReminderTextView);

        final String[] image = new String[1];

        // Set current date
        String currentDate = new SimpleDateFormat("EEE • dd/MM/yy", Locale.getDefault()).format(selectedDate.getTime());
        dateTextView.setText(currentDate);

        // Set up task dropdown
        String[] taskOptions = {"Watering", "Fertilizing", "Pruning", "Other"};
        ArrayAdapter<String> taskAdapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_dropdown_item_1line, taskOptions);
        taskInput.setAdapter(taskAdapter);


        Map<String, String> plantMap = new HashMap<>();
        // Fetch identified plants and set up plant dropdown
        databaseManager.getPlantIdentifications(FirebaseAuth.getInstance().getUid(), data -> {
            List<Map<String, String>> plantNames = data.stream()
                    .map(plant -> {
                        plantMap.put("name", plant.get("name").toString());
                        plantMap.put("image", plant.get("image").toString());
                        image[0] = plantMap.getOrDefault("image", "");
                        return plantMap;
                    })
                    .collect(Collectors.toList());
            ArrayAdapter<String> plantAdapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_dropdown_item_1line,
                    plantNames.stream().map(plant -> plant.get("name")).collect(Collectors.toList()));
            plantInput.setAdapter(plantAdapter);
        });



        // Handle task selection
        taskInput.setOnItemClickListener((parent, view1, position, id) -> {
            if ("Other".equals(taskOptions[position])) {
                otherTaskInput.setVisibility(View.VISIBLE);
            } else {
                otherTaskInput.setVisibility(View.GONE);
            }
        });

        // Create and show the alert dialog
        AlertDialog alertDialog = builder.create();

        if (alertDialog.getWindow() != null) {
            alertDialog.getWindow().setBackgroundDrawable(new ColorDrawable(0));
        }

        Reminder updatedReminder = new Reminder();
        if (reminder != null) {
            addReminderTitle.setText("Edit Reminder");
            plantInput.setText(reminder.getPlant());
            taskInput.setText(reminder.getTask());
            repeatSwitch.setChecked(reminder.isRepeat());

            updatedReminder.setTitle(reminder.getTitle());
            updatedReminder.setId(reminder.getId());
            updatedReminder.setTask(reminder.getTask());
            updatedReminder.setPlant(reminder.getPlant());
            updatedReminder.setImage(reminder.getImage());
            updatedReminder.setTimeInMillis(reminder.getTimeInMillis());
            updatedReminder.setRepeat(reminder.isRepeat());
            updatedReminder.setRepeatDays(reminder.getRepeatDays());
        }
        // Cancel button
        btnCancel.setOnClickListener(v -> alertDialog.dismiss());

        // Okay button - save reminder
        btnOkay.setOnClickListener(v -> {
            // Validate inputs
            if (taskInput.getText().toString().isEmpty() || plantInput.getText().toString().isEmpty()) {
                Toast.makeText(requireContext(), "Please fill all required fields", Toast.LENGTH_SHORT).show();
                return;
            }

            String task = taskInput.getText().toString().equals("Other") ?
                    otherTaskInput.getEditText().getText().toString() :
                    taskInput.getText().toString();
            String plant = plantInput.getText().toString();

            int hour = timePicker.getHour();
            int minute = timePicker.getMinute();

            Calendar reminderTime = (Calendar) selectedDate.clone();
            reminderTime.set(Calendar.HOUR_OF_DAY, hour);
            reminderTime.set(Calendar.MINUTE, minute);

            // Generate title
            String title = plant + " - " + task;

            updatedReminder.setTitle(title);
            updatedReminder.setTask(task);
            updatedReminder.setTimeInMillis(reminderTime.getTimeInMillis());
            updatedReminder.setPlant(plant);
            updatedReminder.setRepeat(repeatSwitch.isChecked());
            updatedReminder.setImage(image[0]);

            databaseManager.saveReminder(updatedReminder, this.requireContext(), () -> {
                updateReminders();
                alertDialog.dismiss();
            });
        });

        // Repeat days button - show custom alert dialog
        btnRepeatDays.setOnClickListener(v -> showRepeatDaysDialog(selectedDate, updatedReminder, btnRepeatDays));

        setRepeatDaysText(selectedDate, btnRepeatDays, updatedReminder);

        btnRepeatDays.setVisibility(updatedReminder.isRepeat() ? View.VISIBLE : View.GONE);
        repeatSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            btnRepeatDays.setVisibility(isChecked ? View.VISIBLE : View.GONE);
            showRepeatDaysDialog(selectedDate, updatedReminder, btnRepeatDays);
            setRepeatDaysText(selectedDate, btnRepeatDays, updatedReminder);
        });

        alertDialog.show();
    }

    private void updateReminders() {
        CalendarView calendarView = requireView().findViewById(R.id.calendarView);
        LinearLayout plantGalleryLayout = requireView().findViewById(R.id.plantGalleryLayout);
        FloatingActionButton addReminderFab = requireView().findViewById(R.id.addReminderFab);

        Calendar selectedDate = Calendar.getInstance();
        selectedDate.setTimeInMillis(calendarView.getDate());

        databaseManager.getRemindersForDate(FirebaseAuth.getInstance().getUid(), selectedDate.getTimeInMillis(), data -> {
            plantGalleryLayout.removeAllViews();
            for (Map<String, Object> reminder : data) {
                View reminderView = reminderView(reminder);
                plantGalleryLayout.addView(reminderView);
            }
        });

        calendarView.setOnDateChangeListener((view, year, month, dayOfMonth) -> {
            selectedDate.set(year, month, dayOfMonth);
            databaseManager.getRemindersForDate(FirebaseAuth.getInstance().getUid(), selectedDate.getTimeInMillis(), data -> {
                plantGalleryLayout.removeAllViews();
                for (Map<String, Object> reminder : data) {
                    View reminderView = reminderView(reminder);
                    plantGalleryLayout.addView(reminderView);
                }
            });
        });

        // Show add reminder dialog with the selected date
        addReminderFab.setOnClickListener(v -> showAddReminderAlertDialog(selectedDate, null));
    }

    private void setRepeatDaysText(Calendar selectedDate, TextView btnRepeatDays, Reminder reminder) {
        StringBuilder repeatDaysString = new StringBuilder();
        for (String day : reminder.getRepeatDays()) {
            if (reminder.getRepeatDays().isEmpty()) {
                repeatDaysString.append(new SimpleDateFormat("dd/MM/yy", Locale.getDefault()).format(selectedDate.getTime()));
                break;
            }

            if (reminder.getRepeatDays().size() == 7) {
                repeatDaysString.append("Everyday");
                break;
            }

            if (reminder.getRepeatDays().size() == 1) {
                repeatDaysString.append(day).append("s");
                break;
            }

            if (reminder.getRepeatDays().size() == 2 && reminder.getRepeatDays().contains("Saturday") && reminder.getRepeatDays().contains("Sunday")) {
                repeatDaysString.append("Weekends");
                break;
            }

            if (reminder.getRepeatDays().size() == 5 && !reminder.getRepeatDays().contains("Saturday") && !reminder.getRepeatDays().contains("Sunday")) {
                repeatDaysString.append("Weekdays");
                break;
            }

            repeatDaysString.append(shortHandDay(day)).append(", ");
        }

        btnRepeatDays.setText(repeatDaysString + " ➤");
    }

    private void showRepeatDaysDialog(Calendar selectedDate, Reminder reminder, TextView btnRepeatDays) {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());

        View repeatDaysView = LayoutInflater.from(requireContext()).inflate(R.layout.custom_alert_dialog_days, null);
        builder.setView(repeatDaysView);

        // Initialize repeat days dialog views
        CheckBox mondayCheckBox = repeatDaysView.findViewById(R.id.linearLayoutMonday).findViewById(R.id.checkboxMonday);
        CheckBox tuesdayCheckBox = repeatDaysView.findViewById(R.id.linearLayoutTuesday).findViewById(R.id.checkboxTuesday);
        CheckBox wednesdayCheckBox = repeatDaysView.findViewById(R.id.linearLayoutWednesday).findViewById(R.id.checkboxWednesday);
        CheckBox thursdayCheckBox = repeatDaysView.findViewById(R.id.linearLayoutThursday).findViewById(R.id.checkboxThursday);
        CheckBox fridayCheckBox = repeatDaysView.findViewById(R.id.linearLayoutFriday).findViewById(R.id.checkboxFriday);
        CheckBox saturdayCheckBox = repeatDaysView.findViewById(R.id.linearLayoutSaturday).findViewById(R.id.checkboxSaturday);
        CheckBox sundayCheckBox = repeatDaysView.findViewById(R.id.linearLayoutSunday).findViewById(R.id.checkboxSunday);
        TextView btnOkay = repeatDaysView.findViewById(R.id.btnOkay);
        TextView btnCancel = repeatDaysView.findViewById(R.id.btnCancel);

        // Set default repeat day to the current day of the selected date
        int dayOfWeek = selectedDate.get(Calendar.DAY_OF_WEEK);
        switch (dayOfWeek) {
            case Calendar.MONDAY:
                mondayCheckBox.setChecked(true);
                break;
            case Calendar.TUESDAY:
                tuesdayCheckBox.setChecked(true);
                break;
            case Calendar.WEDNESDAY:
                wednesdayCheckBox.setChecked(true);
                break;
            case Calendar.THURSDAY:
                thursdayCheckBox.setChecked(true);
                break;
            case Calendar.FRIDAY:
                fridayCheckBox.setChecked(true);
                break;
            case Calendar.SATURDAY:
                saturdayCheckBox.setChecked(true);
                break;
            case Calendar.SUNDAY:
                sundayCheckBox.setChecked(true);
                break;
        }

        reminder.getRepeatDays().forEach(day -> {
            switch (day) {
                case "Monday":
                    mondayCheckBox.setChecked(true);
                    break;
                case "Tuesday":
                    tuesdayCheckBox.setChecked(true);
                    break;
                case "Wednesday":
                    wednesdayCheckBox.setChecked(true);
                    break;
                case "Thursday":
                    thursdayCheckBox.setChecked(true);
                    break;
                case "Friday":
                    fridayCheckBox.setChecked(true);
                    break;
                case "Saturday":
                    saturdayCheckBox.setChecked(true);
                    break;
                case "Sunday":
                    sundayCheckBox.setChecked(true);
                    break;
            }
        });

        // Create and show the repeat days dialog
        AlertDialog repeatDaysDialog = builder.create();

        repeatDaysDialog.getWindow().setBackgroundDrawable(new ColorDrawable(0));

        // Cancel button
        btnCancel.setOnClickListener(v -> repeatDaysDialog.dismiss());

        // Okay button - save repeat days
        btnOkay.setOnClickListener(v -> {
            // Get selected repeat days
            List<String> repeatDays = new ArrayList<>();
            if (mondayCheckBox.isChecked()) repeatDays.add("Monday");
            if (tuesdayCheckBox.isChecked()) repeatDays.add("Tuesday");
            if (wednesdayCheckBox.isChecked()) repeatDays.add("Wednesday");
            if (thursdayCheckBox.isChecked()) repeatDays.add("Thursday");
            if (fridayCheckBox.isChecked()) repeatDays.add("Friday");
            if (saturdayCheckBox.isChecked()) repeatDays.add("Saturday");
            if (sundayCheckBox.isChecked()) repeatDays.add("Sunday");

            // Save repeat days
            reminder.setRepeatDays(repeatDays);

            setRepeatDaysText(selectedDate, btnRepeatDays, reminder);

            repeatDaysDialog.dismiss();
        });

        repeatDaysDialog.show();
    }

    public View reminderView(Map<String, Object> reminder) {
        Reminder parsedReminder = new Reminder(
                reminder.get("id").toString(),
                reminder.get("title").toString(),
                reminder.get("task").toString(),
                reminder.get("plant").toString(),
                reminder.get("image").toString(),
                (Long) reminder.get("timeInMillis"),
                (Boolean) reminder.get("repeat"),
                (List<String>) reminder.get("repeatDays")
        );

        View reminderView = LayoutInflater.from(requireContext()).inflate(R.layout.custom_container_reminder_gallery, null);

        ShapeableImageView plantImageView = reminderView.findViewById(R.id.reminderImage);
        // Set plant image
        Glide.with(requireContext())
                .load(parsedReminder.getImage())
                .centerCrop()
                .into(plantImageView);

        TextView titleTextView = reminderView.findViewById(R.id.plantIDName);
        TextView taskTextView = reminderView.findViewById(R.id.task);
        TextView timeTextView = reminderView.findViewById(R.id.time);

        Button editButton = reminderView.findViewById(R.id.reminderBtnEdit);
        Button deleteButton = reminderView.findViewById(R.id.reminderBtnDelete);

        titleTextView.setText(parsedReminder.getPlant());
        taskTextView.setText(parsedReminder.getTask());

        List<String> repeatDays = parsedReminder.getRepeatDays();

        StringBuilder repeatDaysString = new StringBuilder();
        for (String day : repeatDays) {
            if (repeatDays.size() == 0) {
                repeatDaysString.append(new SimpleDateFormat("dd/MM/yy", Locale.getDefault()).format(parsedReminder.getTimeInMillis()));
                break;
            }

            if (repeatDays.size() == 7) {
                repeatDaysString.append("Everyday");
                break;
            }

            if (repeatDays.size() == 1) {
                repeatDaysString.append(day).append("s");
                break;
            }

            if (repeatDays.size() == 2 && repeatDays.contains("Saturday") && repeatDays.contains("Sunday")) {
                repeatDaysString.append("Weekends");
                break;
            }

            if (repeatDays.size() == 5 && !repeatDays.contains("Saturday") && !repeatDays.contains("Sunday")) {
                repeatDaysString.append("Weekdays");
                break;
            }

            repeatDaysString.append(shortHandDay(day)).append(", ");
        }

        if (repeatDays.isEmpty()) {
            repeatDaysString.append(new SimpleDateFormat("dd/MM/yy", Locale.getDefault()).format(parsedReminder.getTimeInMillis()));
        }

        repeatDaysString.append(" at ").append(new SimpleDateFormat("hh:mm a", Locale.getDefault()).format(parsedReminder.getTimeInMillis()));

        timeTextView.setText(repeatDaysString.toString());

        editButton.setOnClickListener(v -> {
            // Show add reminder dialog with the selected reminder
            Calendar selectedDate = Calendar.getInstance();
            selectedDate.setTimeInMillis(parsedReminder.getTimeInMillis());
            showAddReminderAlertDialog(selectedDate, parsedReminder);
        });

        deleteButton.setOnClickListener(v -> {
            showDialog("Are you sure you want to delete this reminder?", () -> {
                databaseManager.deleteReminder(parsedReminder.getId(), this::updateReminders);
            });
        });

        return reminderView;
    }

    private String shortHandDay(String day) {
        return day.substring(0, 3);
    }

    private void showDialog(String message, Runnable positiveAction) {
        View view = LayoutInflater.from(requireContext()).inflate(R.layout.custom_alert_dialog_log_out, null);

        androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(requireContext());
        builder.setView(view);

        final androidx.appcompat.app.AlertDialog alertDialog = builder.create();

        TextView dialogMessage = view.findViewById(R.id.dialogMessage);
        Button continueButton = view.findViewById(R.id.dialogContinueButton);
        Button continueButton1 = view.findViewById(R.id.dialogContinueButton1);

        // Set the custom message
        dialogMessage.setText(message);

        // Set the positive button action
        continueButton.setText("Yes");
        continueButton.setOnClickListener(view1 -> {
            positiveAction.run();
            alertDialog.dismiss();
        });

        // Set the negative button action
        continueButton1.setText("No");
        continueButton1.setOnClickListener(view1 -> {
            alertDialog.dismiss();
        });

        if (alertDialog.getWindow() != null) {
            alertDialog.getWindow().setBackgroundDrawable(new ColorDrawable(0));
        }

        alertDialog.show();
    }
}