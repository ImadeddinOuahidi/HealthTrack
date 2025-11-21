package com.example.healthtrack;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.google.android.material.switchmaterial.SwitchMaterial;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Calendar;
import java.util.Locale;

public class NotificationActivity extends AppCompatActivity {

    private SwitchMaterial switchHydration, switchSleep, switchSteps;
    private TextInputEditText etHydrationInterval;
    private TextInputLayout tilHydrationInterval;
    private TextView tvSleepTime, tvStepsTime;
    private Button btnSaveReminders;
    private DatabaseReference mDatabase;

    private static final int HYDRATION_NOTIFICATION_ID = 1;
    private static final int SLEEP_NOTIFICATION_ID = 2;
    private static final int STEPS_NOTIFICATION_ID = 3;

    private final ActivityResultLauncher<String> requestPermissionLauncher = registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
        if (!isGranted) {
            Toast.makeText(this, "Notification permission is required for reminders.", Toast.LENGTH_LONG).show();
        }
    });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notification);

        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) {
            finish();
            return;
        }
        mDatabase = FirebaseDatabase.getInstance().getReference().child("users").child(currentUser.getUid()).child("settings");

        switchHydration = findViewById(R.id.switchHydration);
        switchSleep = findViewById(R.id.switchSleep);
        switchSteps = findViewById(R.id.switchSteps);
        etHydrationInterval = findViewById(R.id.etHydrationInterval);
        tilHydrationInterval = findViewById(R.id.tilHydrationInterval);
        tvSleepTime = findViewById(R.id.tvSleepTime);
        tvStepsTime = findViewById(R.id.tvStepsTime);
        btnSaveReminders = findViewById(R.id.btnSaveReminders);

        askForNotificationPermission();
        loadSettings();
        setupListeners();
    }

    private void askForNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                requestPermissionLauncher.launch(android.Manifest.permission.POST_NOTIFICATIONS);
            }
        }
    }

    private void loadSettings() {
        mDatabase.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    boolean isHydrationEnabled = snapshot.child("hydration_reminder_enabled").getValue(Boolean.class) == Boolean.TRUE;
                    switchHydration.setChecked(isHydrationEnabled);
                    etHydrationInterval.setText(snapshot.child("hydration_interval_minutes").getValue(Integer.class) != null ? String.valueOf(snapshot.child("hydration_interval_minutes").getValue(Integer.class)) : "60");
                    updateControlState(tilHydrationInterval, isHydrationEnabled);

                    boolean isSleepEnabled = snapshot.child("sleep_reminder_enabled").getValue(Boolean.class) == Boolean.TRUE;
                    switchSleep.setChecked(isSleepEnabled);
                    tvSleepTime.setText(snapshot.child("sleep_reminder_time").getValue(String.class) != null ? snapshot.child("sleep_reminder_time").getValue(String.class) : "Select Time");
                    updateControlState(tvSleepTime, isSleepEnabled);

                    boolean isStepsEnabled = snapshot.child("steps_reminder_enabled").getValue(Boolean.class) == Boolean.TRUE;
                    switchSteps.setChecked(isStepsEnabled);
                    tvStepsTime.setText(snapshot.child("steps_reminder_time").getValue(String.class) != null ? snapshot.child("steps_reminder_time").getValue(String.class) : "Select Time");
                    updateControlState(tvStepsTime, isStepsEnabled);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) { }
        });
    }

    private void setupListeners() {
        switchHydration.setOnCheckedChangeListener((buttonView, isChecked) -> updateControlState(tilHydrationInterval, isChecked));
        switchSleep.setOnCheckedChangeListener((buttonView, isChecked) -> updateControlState(tvSleepTime, isChecked));
        switchSteps.setOnCheckedChangeListener((buttonView, isChecked) -> updateControlState(tvStepsTime, isChecked));

        tvSleepTime.setOnClickListener(v -> {
            if(v.isEnabled()) showTimePicker(tvSleepTime);
        });
        tvStepsTime.setOnClickListener(v -> {
            if(v.isEnabled()) showTimePicker(tvStepsTime);
        });
        btnSaveReminders.setOnClickListener(v -> saveAndScheduleReminders());
    }

    private void updateControlState(View view, boolean isEnabled) {
        view.setEnabled(isEnabled);
        view.setAlpha(isEnabled ? 1.0f : 0.5f);
    }

    private void showTimePicker(TextView textView) {
        Calendar c = Calendar.getInstance();
        int hour = c.get(Calendar.HOUR_OF_DAY);
        int minute = c.get(Calendar.MINUTE);

        new TimePickerDialog(this, (view, hourOfDay, minuteOfHour) -> {
            String time = String.format(Locale.getDefault(), "%02d:%02d", hourOfDay, minuteOfHour);
            textView.setText(time);
        }, hour, minute, true).show();
    }

    private void saveAndScheduleReminders() {
        // Hydration
        boolean hydrationEnabled = switchHydration.isChecked();
        mDatabase.child("hydration_reminder_enabled").setValue(hydrationEnabled);
        if (hydrationEnabled) {
            String intervalStr = etHydrationInterval.getText().toString();
            if (!TextUtils.isEmpty(intervalStr)) {
                int interval = Integer.parseInt(intervalStr);
                mDatabase.child("hydration_interval_minutes").setValue(interval);
                scheduleReminder(HYDRATION_NOTIFICATION_ID, "Time for water!", "Stay hydrated to meet your goal.", interval, true);
            } else {
                Toast.makeText(this, "Please enter a hydration interval.", Toast.LENGTH_SHORT).show();
                return; 
            }
        } else {
            cancelReminder(HYDRATION_NOTIFICATION_ID);
        }

        // Sleep
        boolean sleepEnabled = switchSleep.isChecked();
        mDatabase.child("sleep_reminder_enabled").setValue(sleepEnabled);
        if (sleepEnabled) {
            String time = tvSleepTime.getText().toString();
            if (!time.equals("Select Time")) {
                mDatabase.child("sleep_reminder_time").setValue(time);
                String[] timeParts = time.split(":");
                scheduleReminder(SLEEP_NOTIFICATION_ID, "Bedtime approaching!", "Time to wind down for a good night's sleep.", Integer.parseInt(timeParts[0]), Integer.parseInt(timeParts[1]));
            } else {
                Toast.makeText(this, "Please select a sleep reminder time.", Toast.LENGTH_SHORT).show();
                return; 
            }
        } else {
            cancelReminder(SLEEP_NOTIFICATION_ID);
        }

        // Steps
        boolean stepsEnabled = switchSteps.isChecked();
        mDatabase.child("steps_reminder_enabled").setValue(stepsEnabled);
        if (stepsEnabled) {
            String time = tvStepsTime.getText().toString();
            if (!time.equals("Select Time")) {
                mDatabase.child("steps_reminder_time").setValue(time);
                String[] timeParts = time.split(":");
                scheduleReminder(STEPS_NOTIFICATION_ID, "Daily Steps Reminder", "Don\'t forget to reach your step goal today!", Integer.parseInt(timeParts[0]), Integer.parseInt(timeParts[1]));
            } else {
                Toast.makeText(this, "Please select a steps reminder time.", Toast.LENGTH_SHORT).show();
                return; 
            }
        } else {
            cancelReminder(STEPS_NOTIFICATION_ID);
        }

        Toast.makeText(this, "Reminders saved!", Toast.LENGTH_SHORT).show();
    }

    private void scheduleReminder(int notificationId, String title, String message, int hour, int minute) {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, hour);
        calendar.set(Calendar.MINUTE, minute);
        calendar.set(Calendar.SECOND, 0);

        if (calendar.before(Calendar.getInstance())) {
            calendar.add(Calendar.DAY_OF_YEAR, 1);
        }

        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(this, AlarmReceiver.class);
        intent.putExtra("title", title);
        intent.putExtra("message", message);
        intent.putExtra("notification_id", notificationId);

        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, notificationId, intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
        if (alarmManager != null) {
            alarmManager.setInexactRepeating(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), AlarmManager.INTERVAL_DAY, pendingIntent);
        }
    }

    private void scheduleReminder(int notificationId, String title, String message, int intervalMinutes, boolean isRepeating) {
        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(this, AlarmReceiver.class);
        intent.putExtra("title", title);
        intent.putExtra("message", message);
        intent.putExtra("notification_id", notificationId);

        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, notificationId, intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
        if (alarmManager != null) {
            long triggerAtMillis = System.currentTimeMillis() + (long) intervalMinutes * 60 * 1000;
            alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, triggerAtMillis, (long) intervalMinutes * 60 * 1000, pendingIntent);
        }
    }

    private void cancelReminder(int notificationId) {
        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(this, AlarmReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, notificationId, intent, PendingIntent.FLAG_NO_CREATE | PendingIntent.FLAG_IMMUTABLE);

        if (alarmManager != null && pendingIntent != null) {
            alarmManager.cancel(pendingIntent);
        }
    }
}
