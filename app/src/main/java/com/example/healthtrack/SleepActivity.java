package com.example.healthtrack;

import android.app.TimePickerDialog;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.google.android.material.chip.Chip;
import com.google.android.material.progressindicator.CircularProgressIndicator;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

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
    private BarChart sleepGraph;
    private TextView durationTV, noDataTV;
    private CircularProgressIndicator sleepCircularProgress;

    private DatabaseReference mDatabase;
    private FirebaseUser currentUser;

    private final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
    private final SimpleDateFormat timeFormat = new SimpleDateFormat("hh:mm a", Locale.getDefault());
    private Date bedtime, wakeupTime;
    private float sleepGoal = 8.0f;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sleep);

        currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) {
            finish();
            return;
        }
        mDatabase = FirebaseDatabase.getInstance().getReference().child("users").child(currentUser.getUid());

        chipBedtime = findViewById(R.id.chipBedtime);
        chipWakeup = findViewById(R.id.chipWakeup);
        spinnerSleepQuality = findViewById(R.id.spinnerSleepQuality);
        btnSaveSleep = findViewById(R.id.btnSaveSleep);
        sleepGraph = findViewById(R.id.sleepGraph);
        durationTV = findViewById(R.id.durationTV);
        noDataTV = findViewById(R.id.noDataTV);
        sleepCircularProgress = findViewById(R.id.sleepCircularProgress);

        setupSpinners();
        setupTimePickers();
        btnSaveSleep.setOnClickListener(v -> saveSleepLog());

        loadGoalAndThenData();
    }

    private void setupTimePickers() {
        chipBedtime.setOnClickListener(v -> showTimePicker(chipBedtime, true));
        chipWakeup.setOnClickListener(v -> showTimePicker(chipWakeup, false));
    }

    private void showTimePicker(final Chip chip, final boolean isBedtime) {
        Calendar cal = Calendar.getInstance();
        int hour = cal.get(Calendar.HOUR_OF_DAY);
        int minute = cal.get(Calendar.MINUTE);

        new TimePickerDialog(this,
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
                }, hour, minute, false).show();
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

        DatabaseReference todayRef = mDatabase.child("daily_logs").child(today);
        todayRef.child("sleep").setValue(hours);
        todayRef.child("sleep_log").setValue(logEntry);

        Toast.makeText(this, "Sleep logged successfully!", Toast.LENGTH_SHORT).show();
        
        checkAchievements(hours);
        loadGoalAndThenData();
    }

    private void checkAchievements(float hours) {
        if (hours >= 8) {
            mDatabase.child("achievements/sleep_initiate").setValue(true);
        }
    }

    private void loadGoalAndThenData() {
        mDatabase.child("goals/sleep_goal").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    Float goal = snapshot.getValue(Float.class);
                    if (goal != null) {
                        sleepGoal = goal;
                    }
                }
                loadWeeklySleepData();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                loadWeeklySleepData();
            }
        });
    }

    private void loadWeeklySleepData() {
        mDatabase.child("daily_logs").limitToLast(7).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                final List<BarEntry> entries = new ArrayList<>();
                final List<String> labels = new ArrayList<>();
                if (snapshot.exists()) {
                    int i = 0;
                    for (DataSnapshot daySnapshot : snapshot.getChildren()) {
                        Float hours = daySnapshot.child("sleep").getValue(Float.class);
                        if (hours != null) {
                            entries.add(new BarEntry(i, hours));
                            labels.add(daySnapshot.getKey().substring(5)); // Format to MM-dd
                            i++;
                        }
                    }
                }

                if (entries.isEmpty()) {
                    sleepGraph.setVisibility(View.GONE);
                    noDataTV.setVisibility(View.VISIBLE);
                } else {
                    sleepGraph.setVisibility(View.VISIBLE);
                    noDataTV.setVisibility(View.GONE);

                    BarDataSet dataSet = new BarDataSet(entries, "Sleep Hours");
                    dataSet.setColor(getResources().getColor(R.color.purple_500));

                    BarData barData = new BarData(dataSet);
                    sleepGraph.setData(barData);

                    XAxis xAxis = sleepGraph.getXAxis();
                    xAxis.setValueFormatter(new ValueFormatter() {
                        @Override
                        public String getFormattedValue(float value) {
                            int index = (int) value;
                            if(index >= 0 && index < labels.size()){
                                return labels.get(index);
                            }
                            return "";
                        }
                    });
                    xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
                    xAxis.setGranularity(1f);
                    xAxis.setGranularityEnabled(true);

                    sleepGraph.getAxisRight().setEnabled(false);
                    sleepGraph.getDescription().setEnabled(false);
                    sleepGraph.invalidate();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                sleepGraph.setVisibility(View.GONE);
                noDataTV.setVisibility(View.VISIBLE);
                Toast.makeText(SleepActivity.this, "Failed to load sleep data.", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
