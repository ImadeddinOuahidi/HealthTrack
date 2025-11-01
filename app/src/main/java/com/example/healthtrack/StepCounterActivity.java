package com.example.healthtrack;

import android.content.Context;
import android.content.SharedPreferences;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.widget.ProgressBar;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import java.time.LocalDate;

public class StepCounterActivity extends AppCompatActivity implements SensorEventListener {

    private TextView txtSteps;
    private ProgressBar progressSteps;
    private SensorManager sensorManager;
    private Sensor stepSensor;
    private boolean isSensorPresent = false;
    private int stepCount = 0;
    private int dailyGoal = 10000;

    private SharedPreferences sharedPreferences;
    private static final String PREF_NAME = "StepCounterPrefs";
    private static final String KEY_STEP_COUNT = "stepCount";
    private static final String KEY_DATE = "lastSavedDate";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_step_counter);

        txtSteps = findViewById(R.id.txtSteps);
        progressSteps = findViewById(R.id.progressSteps);

        sharedPreferences = getSharedPreferences(PREF_NAME, MODE_PRIVATE);
        String today = LocalDate.now().toString();
        String lastSavedDate = sharedPreferences.getString(KEY_DATE, "");

        if (!today.equals(lastSavedDate)) {
            stepCount = 0;
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putString(KEY_DATE, today);
            editor.putInt(KEY_STEP_COUNT, 0);
            editor.apply();
        } else {
            stepCount = sharedPreferences.getInt(KEY_STEP_COUNT, 0);
        }

        txtSteps.setText(stepCount + " Steps");
        progressSteps.setProgress(Math.min((stepCount * 100) / dailyGoal, 100));

        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        if (sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER) != null) {
            stepSensor = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER);
            isSensorPresent = true;
        } else {
            txtSteps.setText("No Step Sensor Detected 😔");
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (isSensorPresent) {
            sensorManager.registerListener(this, stepSensor, SensorManager.SENSOR_DELAY_UI);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (isSensorPresent) {
            sensorManager.unregisterListener(this);
        }
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_STEP_COUNTER) {
            stepCount = (int) event.values[0];
            txtSteps.setText(stepCount + " Steps");

            int progress = Math.min((stepCount * 100) / dailyGoal, 100);
            progressSteps.setProgress(progress);

            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putInt(KEY_STEP_COUNT, stepCount);
            editor.apply();
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        // Not used
    }
}

