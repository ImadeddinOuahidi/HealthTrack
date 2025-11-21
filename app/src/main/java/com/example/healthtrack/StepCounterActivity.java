package com.example.healthtrack;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.view.View;
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
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class StepCounterActivity extends AppCompatActivity implements SensorEventListener {

    private TextView txtSteps, distanceTV, caloriesTV, goalTV, noDataTV;
    private CircularProgressIndicator stepsCircularProgress;
    private BarChart stepsGraph;

    private SensorManager sensorManager;
    private Sensor stepSensor;
    private boolean isSensorPresent;
    private int stepCount = 0;
    private int stepGoal = 10000; // Default step goal

    private DatabaseReference mDatabase;
    private FirebaseUser currentUser;
    private final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_step_counter);

        currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) {
            finish();
            return;
        }
        mDatabase = FirebaseDatabase.getInstance().getReference().child("users").child(currentUser.getUid());

        txtSteps = findViewById(R.id.txtSteps);
        distanceTV = findViewById(R.id.distanceTV);
        caloriesTV = findViewById(R.id.caloriesTV);
        goalTV = findViewById(R.id.goalTV);
        stepsCircularProgress = findViewById(R.id.stepsCircularProgress);
        stepsGraph = findViewById(R.id.stepsGraph);
        noDataTV = findViewById(R.id.noDataTV);

        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        if (sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER) != null) {
            stepSensor = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER);
            isSensorPresent = true;
        } else {
            isSensorPresent = false;
        }

        loadGoalAndThenData();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (isSensorPresent) {
            sensorManager.registerListener(this, stepSensor, SensorManager.SENSOR_DELAY_NORMAL);
        }
        loadTodayStepCount();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (isSensorPresent) {
            sensorManager.unregisterListener(this);
            saveTodayStepCount();
        }
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_STEP_COUNTER) {
            stepCount = (int) event.values[0];
            updateUI();
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
    }

    private void updateUI() {
        txtSteps.setText(String.format(Locale.getDefault(), "%,d", stepCount));
        goalTV.setText(String.format(Locale.getDefault(), "of %,d steps", stepGoal));
        int progress = (int) ((stepCount * 100.0f) / stepGoal);
        stepsCircularProgress.setProgress(progress, true);

        double distanceKm = stepCount * 0.000762;
        double caloriesBurned = stepCount * 0.04;
        distanceTV.setText(String.format(Locale.getDefault(), "%.2f km", distanceKm));
        caloriesTV.setText(String.format(Locale.getDefault(), "%.0f kcal", caloriesBurned));

        checkAchievements(stepCount);
    }

    private void saveTodayStepCount() {
        String today = sdf.format(new Date());
        mDatabase.child("daily_logs").child(today).child("steps").setValue(stepCount);
    }

    private void loadTodayStepCount() {
        String today = sdf.format(new Date());
        mDatabase.child("daily_logs").child(today).child("steps").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    Integer steps = snapshot.getValue(Integer.class);
                    if (steps != null) {
                        stepCount = steps;
                    }
                }
                updateUI();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });
    }

    private void loadGoalAndThenData() {
        mDatabase.child("goals/steps_goal").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    Integer goal = snapshot.getValue(Integer.class);
                    if (goal != null) {
                        stepGoal = goal;
                    }
                }
                loadWeeklyStepData();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                loadWeeklyStepData();
            }
        });
    }

    private void loadWeeklyStepData() {
        mDatabase.child("daily_logs").limitToLast(7).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                final List<BarEntry> entries = new ArrayList<>();
                final List<String> labels = new ArrayList<>();
                if (snapshot.exists()) {
                    int i = 0;
                    for (DataSnapshot daySnapshot : snapshot.getChildren()) {
                        Integer steps = daySnapshot.child("steps").getValue(Integer.class);
                        if (steps != null) {
                            entries.add(new BarEntry(i, steps));
                            labels.add(daySnapshot.getKey().substring(5));
                            i++;
                        }
                    }
                }

                if (entries.isEmpty()) {
                    stepsGraph.setVisibility(View.GONE);
                    noDataTV.setVisibility(View.VISIBLE);
                } else {
                    stepsGraph.setVisibility(View.VISIBLE);
                    noDataTV.setVisibility(View.GONE);

                    BarDataSet dataSet = new BarDataSet(entries, "Daily Steps");
                    dataSet.setColor(getResources().getColor(R.color.purple_500));

                    BarData barData = new BarData(dataSet);
                    stepsGraph.setData(barData);

                    XAxis xAxis = stepsGraph.getXAxis();
                    xAxis.setValueFormatter(new ValueFormatter() {
                        @Override
                        public String getFormattedValue(float value) {
                            int index = (int) value;
                            if(index >=0 && index < labels.size()){
                                return labels.get(index);
                            }
                           return "";
                        }
                    });
                    xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
                    xAxis.setGranularity(1f);
                    xAxis.setGranularityEnabled(true);

                    stepsGraph.getAxisRight().setEnabled(false);
                    stepsGraph.getDescription().setEnabled(false);
                    stepsGraph.invalidate();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                stepsGraph.setVisibility(View.GONE);
                noDataTV.setVisibility(View.VISIBLE);
                Toast.makeText(StepCounterActivity.this, "Failed to load step data.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void checkAchievements(int currentSteps) {
        if (currentSteps >= 1000) {
            mDatabase.child("achievements/first_steps").setValue(true);
        }
        if (currentSteps >= 42195) {
            mDatabase.child("achievements/marathon_runner").setValue(true);
        }
    }
}
