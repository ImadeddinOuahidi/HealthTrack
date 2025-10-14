package com.example.healthtrack; // change to your package name

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.widget.ProgressBar;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

public class StepCounterActivity extends AppCompatActivity implements SensorEventListener {

    private TextView txtSteps;
    private ProgressBar progressSteps;
    private SensorManager sensorManager;
    private Sensor stepSensor;

    private boolean isSensorPresent = false;
    private int stepCount = 0;
    private int dailyGoal = 10000; // Example daily goal

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_step_counter);

        txtSteps = findViewById(R.id.txtSteps);
        progressSteps = findViewById(R.id.progressSteps);

        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);

        if (sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER) != null) {
            stepSensor = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER);
            isSensorPresent = true;
        } else {
            txtSteps.setText("No Step Sensor Detected 😔");
            isSensorPresent = false;
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
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        // Not used
    }
}
