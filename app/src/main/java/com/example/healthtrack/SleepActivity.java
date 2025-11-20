package com.example.healthtrack;

import static com.example.healthtrack.MainActivity.KEY_LOGGED_IN_USER;
import static com.example.healthtrack.MainActivity.SESSION_PREFS_NAME;

import android.app.TimePickerDialog;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.chip.Chip;
import com.google.android.material.progressindicator.CircularProgressIndicator;
import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.helper.StaticLabelsFormatter;
import com.jjoe64.graphview.series.BarGraphSeries;
import com.jjoe64.graphview.series.DataPoint;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

public class SleepActivity extends AppCompatActivity {

    private Chip chipBedtime, chipWakeup;
    private AutoCompleteTextView spinnerSleepQuality;
    private Button btnSaveSleep;
    private GraphView sleepGraph;
    private TextView durationTV;
    private CircularProgressIndicator sleepCircularProgress;

    private SharedPreferences userPreferences;
    private final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
    private final SimpleDateFormat timeFormat = new SimpleDateFormat("hh:mm a", Locale.getDefault());
    private final String SLEEP_LOG_PREFIX = "sleep_log_";
    private Date bedtime, wakeupTime;
    private final float sleepGoal = 8.0f; // 8 hours goal

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

        chipBedtime = findViewById(R.id.chipBedtime);
        chipWakeup = findViewById(R.id.chipWakeup);
        spinnerSleepQuality = findViewById(R.id.spinnerSleepQuality);
        btnSaveSleep = findViewById(R.id.btnSaveSleep);
        sleepGraph = findViewById(R.id.sleepGraph);
        durationTV = findViewById(R.id.durationTV);
        sleepCircularProgress = findViewById(R.id.sleepCircularProgress);

        setupSpinners();
        setupTimePickers();
        btnSaveSleep.setOnClickListener(v -> saveSleepLog());

        loadWeeklySleepData();
    }

    private void setupTimePickers() {
        chipBedtime.setOnClickListener(v -> showTimePicker(chipBedtime, true));
        chipWakeup.setOnClickListener(v -> showTimePicker(chipWakeup, false));
    }

    private void showTimePicker(final Chip chip, final boolean isBedtime) {
        Calendar cal = Calendar.getInstance();
        int hour = cal.get(Calendar.HOUR_OF_DAY);
        int minute = cal.get(Calendar.MINUTE);

        TimePickerDialog timePickerDialog = new TimePickerDialog(this,
                (view, hourOfDay, minuteOfHour) -> {
                    cal.set(Calendar.HOUR_OF_DAY, hourOfDay);
                    cal.set(Calendar.MINUTE, minuteOfHour);
                    if (isBedtime) {
                        bedtime = cal.getTime();
                    } else {
                        wakeupTime = cal.getTime();
                    }
                    chip.setText(timeFormat.format(cal.getTime()));
                    calculateAndShowDuration();
                }, hour, minute, false);
        timePickerDialog.show();
    }

    private void calculateAndShowDuration() {
        if (bedtime != null && wakeupTime != null) {
            long durationMillis = wakeupTime.getTime() - bedtime.getTime();
            if (durationMillis < 0) {
                durationMillis += TimeUnit.DAYS.toMillis(1);
            }

            long hours = TimeUnit.MILLISECONDS.toHours(durationMillis);
            long minutes = TimeUnit.MILLISECONDS.toMinutes(durationMillis) % 60;
            durationTV.setText(String.format(Locale.getDefault(), "%dh %dm", hours, minutes));

            float durationHours = (float) durationMillis / (1000 * 60 * 60);
            int progress = (int) ((durationHours * 100.0f) / sleepGoal);
            sleepCircularProgress.setProgress(progress, true);
        }
    }

    private void setupSpinners() {
        String[] sleepQualityOptions = {"Excellent", "Good", "Fair", "Poor"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, sleepQualityOptions);
        spinnerSleepQuality.setAdapter(adapter);
    }

    private void saveSleepLog() {
        if (bedtime == null || wakeupTime == null || spinnerSleepQuality.getText().toString().isEmpty()) {
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        long durationMillis = wakeupTime.getTime() - bedtime.getTime();
        if (durationMillis < 0) {
            durationMillis += TimeUnit.DAYS.toMillis(1);
        }
        float hours = (float) durationMillis / (1000 * 60 * 60);
        String quality = spinnerSleepQuality.getText().toString();

        String today = sdf.format(Calendar.getInstance().getTime());
        String logEntry = hours + "," + quality;

        SharedPreferences.Editor editor = userPreferences.edit();
        editor.putString(SLEEP_LOG_PREFIX + today, logEntry);
        editor.putFloat("sleep_" + today, hours); // For dashboard compatibility
        editor.apply();

        Toast.makeText(this, "Sleep logged successfully!", Toast.LENGTH_SHORT).show();
        loadWeeklySleepData(); // Refresh graph
    }

    private void loadWeeklySleepData() {
        List<DataPoint> dataPoints = new ArrayList<>();
        List<String> labels = new ArrayList<>();
        Calendar cal = Calendar.getInstance();

        for (int i = 6; i >= 0; i--) {
            cal.setTime(new Date());
            cal.add(Calendar.DAY_OF_YEAR, -i);
            String dateKey = sdf.format(cal.getTime());
            String logEntry = userPreferences.getString(SLEEP_LOG_PREFIX + dateKey, "0,N/A");

            float hours = Float.parseFloat(logEntry.split(",")[0]);
            dataPoints.add(new DataPoint(6 - i, hours));

            SimpleDateFormat dayFormat = new SimpleDateFormat("EEE", Locale.getDefault());
            labels.add(dayFormat.format(cal.getTime()));
        }

        BarGraphSeries<DataPoint> series = new BarGraphSeries<>(dataPoints.toArray(new DataPoint[0]));
        series.setSpacing(50);

        sleepGraph.removeAllSeries();
        sleepGraph.addSeries(series);

        StaticLabelsFormatter staticLabelsFormatter = new StaticLabelsFormatter(sleepGraph);
        staticLabelsFormatter.setHorizontalLabels(labels.toArray(new String[0]));
        sleepGraph.getGridLabelRenderer().setLabelFormatter(staticLabelsFormatter);
        sleepGraph.getViewport().setYAxisBoundsManual(true);
        sleepGraph.getViewport().setMinY(0);
        sleepGraph.getViewport().setMaxY(12);
    }
}
