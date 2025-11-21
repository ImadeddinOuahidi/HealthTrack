package com.example.healthtrack;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.slider.Slider;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Locale;

public class GoalsActivity extends AppCompatActivity {

    private Slider sliderHydrationGoal, sliderSleepGoal, sliderStepGoal;
    private TextInputEditText hydrationGoalValueET, sleepGoalValueET, stepsGoalValueET;
    private Button btnSaveGoals;

    private DatabaseReference mDatabase;
    private FirebaseUser currentUser;
    private boolean isUpdatingFromSlider = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_goals);

        currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) {
            finish();
            return;
        }
        mDatabase = FirebaseDatabase.getInstance().getReference().child("users").child(currentUser.getUid());

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
        mDatabase.child("goals").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    Integer hydrationGoal = snapshot.child("hydration_goal").getValue(Integer.class);
                    Float sleepGoal = snapshot.child("sleep_goal").getValue(Float.class);
                    Integer stepGoal = snapshot.child("steps_goal").getValue(Integer.class);

                    sliderHydrationGoal.setValue(hydrationGoal != null ? hydrationGoal : 2000);
                    sliderSleepGoal.setValue(sleepGoal != null ? sleepGoal : 8.0f);
                    sliderStepGoal.setValue(stepGoal != null ? stepGoal : 10000);

                    hydrationGoalValueET.setText(String.valueOf(hydrationGoal != null ? hydrationGoal : 2000));
                    sleepGoalValueET.setText(String.valueOf(sleepGoal != null ? sleepGoal : 8.0f));
                    stepsGoalValueET.setText(String.valueOf(stepGoal != null ? stepGoal : 10000));
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void saveGoals() {
        try {
            mDatabase.child("goals/hydration_goal").setValue(Integer.parseInt(hydrationGoalValueET.getText().toString()));
            mDatabase.child("goals/sleep_goal").setValue(Float.parseFloat(sleepGoalValueET.getText().toString()));
            mDatabase.child("goals/steps_goal").setValue(Integer.parseInt(stepsGoalValueET.getText().toString()));
            
            mDatabase.child("achievements/goal_setter").setValue(true);

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
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            if (!isUpdatingFromSlider) {
                try {
                    float value = Float.parseFloat(s.toString());
                    if (value >= slider.getValueFrom() && value <= slider.getValueTo()) {
                        slider.setValue(value);
                    }
                } catch (NumberFormatException e) {
                    // Ignore
                }
            }
        }

        @Override
        public void afterTextChanged(Editable s) {
        }
    }
}
