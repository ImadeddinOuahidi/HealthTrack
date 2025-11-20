package com.example.healthtrack;

import static com.example.healthtrack.MainActivity.KEY_LOGGED_IN_USER;
import static com.example.healthtrack.MainActivity.SESSION_PREFS_NAME;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.slider.Slider;
import com.google.android.material.textfield.TextInputEditText;

import java.util.Locale;

public class GoalsActivity extends AppCompatActivity {

    private Slider sliderHydrationGoal, sliderSleepGoal, sliderStepGoal;
    private TextInputEditText hydrationGoalValueET, sleepGoalValueET, stepsGoalValueET;
    private Button btnSaveGoals;

    private SharedPreferences goalsPreferences;
    private boolean isUpdatingFromSlider = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_goals);

        SharedPreferences sessionManager = getSharedPreferences(SESSION_PREFS_NAME, MODE_PRIVATE);
        String loggedInUser = sessionManager.getString(KEY_LOGGED_IN_USER, null);

        if (loggedInUser == null) {
            finish();
            return;
        }

        goalsPreferences = getSharedPreferences("goals_prefs_" + loggedInUser, MODE_PRIVATE);

        // Sliders
        sliderHydrationGoal = findViewById(R.id.sliderHydrationGoal);
        sliderSleepGoal = findViewById(R.id.sliderSleepGoal);
        sliderStepGoal = findViewById(R.id.sliderStepGoal);

        // EditTexts for slider values
        hydrationGoalValueET = findViewById(R.id.hydrationGoalValueET);
        sleepGoalValueET = findViewById(R.id.sleepGoalValueET);
        stepsGoalValueET = findViewById(R.id.stepsGoalValueET);

        btnSaveGoals = findViewById(R.id.btnSaveGoals);

        loadGoals();
        setupListeners();
    }

    private void setupListeners() {
        // Hydration Listeners
        sliderHydrationGoal.addOnChangeListener((slider, value, fromUser) -> {
            if (fromUser) {
                isUpdatingFromSlider = true;
                hydrationGoalValueET.setText(String.format(Locale.getDefault(), "%.0f", value));
                isUpdatingFromSlider = false;
            }
        });
        hydrationGoalValueET.addTextChangedListener(new GoalTextWatcher(sliderHydrationGoal, hydrationGoalValueET));

        // Sleep Listeners
        sliderSleepGoal.addOnChangeListener((slider, value, fromUser) -> {
            if (fromUser) {
                isUpdatingFromSlider = true;
                sleepGoalValueET.setText(String.format(Locale.getDefault(), "%.1f", value));
                isUpdatingFromSlider = false;
            }
        });
        sleepGoalValueET.addTextChangedListener(new GoalTextWatcher(sliderSleepGoal, sleepGoalValueET));

        // Steps Listeners
        sliderStepGoal.addOnChangeListener((slider, value, fromUser) -> {
            if (fromUser) {
                isUpdatingFromSlider = true;
                stepsGoalValueET.setText(String.format(Locale.getDefault(), "%.0f", value));
                isUpdatingFromSlider = false;
            }
        });
        stepsGoalValueET.addTextChangedListener(new GoalTextWatcher(sliderStepGoal, stepsGoalValueET));

        btnSaveGoals.setOnClickListener(v -> saveGoals());
    }

    private void loadGoals() {
        int hydrationGoal = goalsPreferences.getInt("hydration_goal", 2000);
        float sleepGoal = goalsPreferences.getFloat("sleep_goal", 8.0f);
        int stepGoal = goalsPreferences.getInt("steps_goal", 10000);

        sliderHydrationGoal.setValue(hydrationGoal);
        sliderSleepGoal.setValue(sleepGoal);
        sliderStepGoal.setValue(stepGoal);

        hydrationGoalValueET.setText(String.valueOf(hydrationGoal));
        sleepGoalValueET.setText(String.valueOf(sleepGoal));
        stepsGoalValueET.setText(String.valueOf(stepGoal));
    }

    private void saveGoals() {
        try {
            SharedPreferences.Editor editor = goalsPreferences.edit();
            editor.putInt("hydration_goal", Integer.parseInt(hydrationGoalValueET.getText().toString()));
            editor.putFloat("sleep_goal", Float.parseFloat(sleepGoalValueET.getText().toString()));
            editor.putInt("steps_goal", Integer.parseInt(stepsGoalValueET.getText().toString()));
            editor.apply();
            Toast.makeText(this, "Goals saved successfully!", Toast.LENGTH_SHORT).show();
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Please enter valid numbers for all goals.", Toast.LENGTH_SHORT).show();
        }
    }

    private class GoalTextWatcher implements TextWatcher {
        private final Slider slider;
        private final TextInputEditText editText;

        GoalTextWatcher(Slider slider, TextInputEditText editText) {
            this.slider = slider;
            this.editText = editText;
        }

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) { }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            if (!isUpdatingFromSlider) {
                try {
                    float value = Float.parseFloat(s.toString());
                    if (value >= slider.getValueFrom() && value <= slider.getValueTo()) {
                        slider.setValue(value);
                    }
                } catch (NumberFormatException e) {
                    // Ignore, let the user continue typing
                }
            }
        }

        @Override
        public void afterTextChanged(Editable s) { }
    }
}
