package com.example.healthtrack;

import static com.example.healthtrack.MainActivity.KEY_LOGGED_IN_USER;
import static com.example.healthtrack.MainActivity.SESSION_PREFS_NAME;

import android.app.TimePickerDialog;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.TextInputEditText;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

public class SleepActivity extends AppCompatActivity {

    private TextInputEditText edtBedtime, edtWakeup;
    private AutoCompleteTextView spinnerSleepQuality;
    private Button btnSaveSleep;

    private SharedPreferences userPreferences;
    private final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
    private final SimpleDateFormat timeFormat = new SimpleDateFormat("hh:mm a", Locale.getDefault());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sleep);

        SharedPreferences sessionManager = getSharedPreferences(SESSION_PREFS_NAME, MODE_PRIVATE);
        String loggedInUser = sessionManager.getString(KEY_LOGGED_IN_USER, null);

        if (loggedInUser == null) {
            finish();
            return;
        }

        userPreferences = getSharedPreferences("user_prefs_" + loggedInUser, MODE_PRIVATE);

        edtBedtime = findViewById(R.id.edtBedtime);
        edtWakeup = findViewById(R.id.edtWakeup);
        spinnerSleepQuality = findViewById(R.id.spinnerSleepQuality);
        btnSaveSleep = findViewById(R.id.btnSaveSleep);

        setupSpinners();

        setupTimePickers();

        btnSaveSleep.setOnClickListener(v -> saveSleepLog());
    }

    private void setupTimePickers() {
        edtBedtime.setOnClickListener(v -> showTimePicker(edtBedtime));
        edtWakeup.setOnClickListener(v -> showTimePicker(edtWakeup));
    }

    private void showTimePicker(final TextInputEditText editText) {
        Calendar cal = Calendar.getInstance();
        int hour = cal.get(Calendar.HOUR_OF_DAY);
        int minute = cal.get(Calendar.MINUTE);

        TimePickerDialog timePickerDialog = new TimePickerDialog(this,
                (view, hourOfDay, minuteOfHour) -> {
                    cal.set(Calendar.HOUR_OF_DAY, hourOfDay);
                    cal.set(Calendar.MINUTE, minuteOfHour);
                    editText.setText(timeFormat.format(cal.getTime()));
                }, hour, minute, false);
        timePickerDialog.show();
    }

    private void setupSpinners() {
        String[] sleepQualityOptions = {"Excellent", "Good", "Fair", "Poor"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, sleepQualityOptions);
        spinnerSleepQuality.setAdapter(adapter);
    }

    private void saveSleepLog() {
        String bedtimeStr = edtBedtime.getText().toString();
        String wakeupStr = edtWakeup.getText().toString();
        String quality = spinnerSleepQuality.getText().toString();

        if (bedtimeStr.isEmpty() || wakeupStr.isEmpty() || quality.isEmpty()) {
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            long bedtimeMillis = timeFormat.parse(bedtimeStr).getTime();
            long wakeupMillis = timeFormat.parse(wakeupStr).getTime();

            long durationMillis = wakeupMillis - bedtimeMillis;
            if (durationMillis < 0) {
                durationMillis += TimeUnit.DAYS.toMillis(1);
            }

            float hours = (float) durationMillis / TimeUnit.HOURS.toMillis(1);

            String today = sdf.format(Calendar.getInstance().getTime());
            SharedPreferences.Editor editor = userPreferences.edit();
            editor.putFloat("sleep_" + today, hours);
            editor.apply();

            Toast.makeText(this, "Sleep logged successfully!", Toast.LENGTH_SHORT).show();

        } catch (Exception e) {
            Toast.makeText(this, "Invalid time format", Toast.LENGTH_SHORT).show();
        }
    }


}
