package com.example.healthtrack;

import static com.example.healthtrack.MainActivity.KEY_LOGGED_IN_USER;
import static com.example.healthtrack.MainActivity.SESSION_PREFS_NAME;

import android.content.SharedPreferences;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.progressindicator.CircularProgressIndicator;
import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.helper.StaticLabelsFormatter;
import com.jjoe64.graphview.series.BarGraphSeries;
import com.jjoe64.graphview.series.DataPoint;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class StepCounterActivity extends AppCompatActivity implements SensorEventListener {

    private TextView txtSteps, distanceTV, caloriesTV, goalTV;
    private CircularProgressIndicator stepsCircularProgress;
    private GraphView stepsGraph;

    private SensorManager sensorManager;
    private Sensor stepSensor;
    private boolean isSensorPresent;
    private int stepCount = 0;
    private int stepGoal = 10000; // Default step goal

    private SharedPreferences userPreferences;
    private SharedPreferences goalsPreferences;
    private final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
    private final String STEPS_LOG_PREFIX = "steps_log_";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_step_counter);

        SharedPreferences sessionManager = getSharedPreferences(SESSION_PREFS_NAME, MODE_PRIVATE);
        String loggedInUser = sessionManager.getString(KEY_LOGGED_IN_USER, null);

        if (loggedInUser == null) {
            finish();
            return;
        }

        userPreferences = getSharedPreferences("user_prefs_" + loggedInUser, MODE_PRIVATE);
        goalsPreferences = getSharedPreferences("goals_prefs_" + loggedInUser, MODE_PRIVATE);
        stepGoal = goalsPreferences.getInt("steps_goal", 10000);

        txtSteps = findViewById(R.id.txtSteps);
        distanceTV = findViewById(R.id.distanceTV);
        caloriesTV = findViewById(R.id.caloriesTV);
        goalTV = findViewById(R.id.goalTV);
        stepsCircularProgress = findViewById(R.id.stepsCircularProgress);
        stepsGraph = findViewById(R.id.stepsGraph);

        goalTV.setText(String.format(Locale.getDefault(), "of %,d steps", stepGoal));

        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        if (sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER) != null) {
            stepSensor = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER);
            isSensorPresent = true;
        } else {
            isSensorPresent = false;
        }

        loadWeeklyStepData();
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
        // Not needed for this implementation
    }

    private void updateUI() {
        txtSteps.setText(String.format(Locale.getDefault(), "%,d", stepCount));
        int progress = (int) ((stepCount * 100.0f) / stepGoal);
        stepsCircularProgress.setProgress(progress, true);

        // Approximate calculations
        double distanceKm = stepCount * 0.000762; // Avg stride length
        double caloriesBurned = stepCount * 0.04;   // Avg calories per step
        distanceTV.setText(String.format(Locale.getDefault(), "%.2f km", distanceKm));
        caloriesTV.setText(String.format(Locale.getDefault(), "%.0f kcal", caloriesBurned));
    }

    private void saveTodayStepCount() {
        String today = sdf.format(new Date());
        SharedPreferences.Editor editor = userPreferences.edit();
        editor.putInt(STEPS_LOG_PREFIX + today, stepCount);
        editor.putInt("steps_" + today, stepCount); // For dashboard compatibility
        editor.apply();
    }

    private void loadTodayStepCount() {
        String today = sdf.format(new Date());
        stepCount = userPreferences.getInt(STEPS_LOG_PREFIX + today, 0);
        updateUI();
    }

    private void loadWeeklyStepData() {
        List<DataPoint> dataPoints = new ArrayList<>();
        List<String> labels = new ArrayList<>();
        Calendar cal = Calendar.getInstance();

        for (int i = 6; i >= 0; i--) {
            cal.setTime(new Date());
            cal.add(Calendar.DAY_OF_YEAR, -i);
            String dateKey = sdf.format(cal.getTime());
            int steps = userPreferences.getInt(STEPS_LOG_PREFIX + dateKey, 0);
            dataPoints.add(new DataPoint(6 - i, steps));

            SimpleDateFormat dayFormat = new SimpleDateFormat("EEE", Locale.getDefault());
            labels.add(dayFormat.format(cal.getTime()));
        }

        BarGraphSeries<DataPoint> series = new BarGraphSeries<>(dataPoints.toArray(new DataPoint[0]));
        series.setSpacing(50);

        stepsGraph.removeAllSeries();
        stepsGraph.addSeries(series);

        StaticLabelsFormatter staticLabelsFormatter = new StaticLabelsFormatter(stepsGraph);
        staticLabelsFormatter.setHorizontalLabels(labels.toArray(new String[0]));
        stepsGraph.getGridLabelRenderer().setLabelFormatter(staticLabelsFormatter);
        stepsGraph.getViewport().setYAxisBoundsManual(true);
        stepsGraph.getViewport().setMinY(0);
        stepsGraph.getViewport().setMaxY(stepGoal * 1.2); // 20% buffer
    }
}
