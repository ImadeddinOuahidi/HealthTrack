package com.example.healthtrack;

import static com.example.healthtrack.MainActivity.KEY_LOGGED_IN_USER;
import static com.example.healthtrack.MainActivity.SESSION_PREFS_NAME;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class GoalsActivity extends AppCompatActivity {

    private ProgressBar progressHydrationGoal, progressSleepGoal, progressStepGoal, progressChallenge;
    private EditText edtHydrationGoal, edtSleepGoal, edtStepGoal, edtChallengeGoal;
    private Button btnSaveGoals;
    private SharedPreferences goalsPrefs;
    private SharedPreferences sessionPrefs;
    private String loggedInUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_goals);

        sessionPrefs = getSharedPreferences(SESSION_PREFS_NAME, MODE_PRIVATE);
        loggedInUser = sessionPrefs.getString(KEY_LOGGED_IN_USER, null);

        if (loggedInUser == null) {
            Toast.makeText(this, "No active user session found!", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Each user gets their own goal file
        goalsPrefs = getSharedPreferences("goals_prefs_" + loggedInUser, MODE_PRIVATE);

        progressHydrationGoal = findViewById(R.id.progressHydrationGoal);
        progressSleepGoal = findViewById(R.id.progressSleepGoal);
        progressStepGoal = findViewById(R.id.progressStepGoal);
        progressChallenge = findViewById(R.id.progressChallenge);

        edtHydrationGoal = findViewById(R.id.edtHydrationGoal);
        edtSleepGoal = findViewById(R.id.edtSleepGoal);
        edtStepGoal = findViewById(R.id.edtStepGoal);
        edtChallengeGoal = findViewById(R.id.edtChallengeGoal);
        btnSaveGoals = findViewById(R.id.btnSaveGoals);

        loadGoals();

        btnSaveGoals.setOnClickListener(v -> saveGoals());
    }

    private void saveGoals() {
        if (edtHydrationGoal.getText().toString().isEmpty() ||
                edtSleepGoal.getText().toString().isEmpty() ||
                edtStepGoal.getText().toString().isEmpty() ||
                edtChallengeGoal.getText().toString().isEmpty()) {
            Toast.makeText(this, "Please fill all goal fields", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            int hydration = Integer.parseInt(edtHydrationGoal.getText().toString());
            float sleep = Float.parseFloat(edtSleepGoal.getText().toString());
            int steps = Integer.parseInt(edtStepGoal.getText().toString());
            int challenge = Integer.parseInt(edtChallengeGoal.getText().toString());

            if (hydration <= 0 || sleep <= 0 || steps <= 0 || challenge <= 0) {
                Toast.makeText(this, "Values must be greater than zero", Toast.LENGTH_SHORT).show();
                return;
            }

            SharedPreferences.Editor editor = goalsPrefs.edit();
            editor.putInt("hydration_goal", hydration);
            editor.putFloat("sleep_goal", sleep);
            editor.putInt("steps_goal", steps);
            editor.putInt("challenge_goal", challenge);
            editor.apply();

            updateProgressBars(hydration, sleep, steps, challenge);

            Toast.makeText(this, "Goals saved successfully for " + loggedInUser, Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            Toast.makeText(this, "Error saving goals: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private void loadGoals() {
        int hydration = goalsPrefs.getInt("hydration_goal", 2000);
        float sleep = goalsPrefs.getFloat("sleep_goal", 8f);
        int steps = goalsPrefs.getInt("steps_goal", 10000);
        int challenge = goalsPrefs.getInt("challenge_goal", 7);

        edtHydrationGoal.setText(String.valueOf(hydration));
        edtSleepGoal.setText(String.valueOf(sleep));
        edtStepGoal.setText(String.valueOf(steps));
        edtChallengeGoal.setText(String.valueOf(challenge));

        updateProgressBars(hydration, sleep, steps, challenge);
    }

    private void updateProgressBars(int hydration, float sleep, int steps, int challenge) {
        progressHydrationGoal.setMax(Math.max(hydration, 1));
        progressHydrationGoal.setProgress(1200);

        progressSleepGoal.setMax((int) Math.max(sleep, 1));
        progressSleepGoal.setProgress(7);

        progressStepGoal.setMax(Math.max(steps, 1));
        progressStepGoal.setProgress(8500);

        progressChallenge.setMax(Math.max(challenge, 1));
        progressChallenge.setProgress(3);
    }
}
