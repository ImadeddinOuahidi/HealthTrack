package com.example.healthtrack;

import static com.example.healthtrack.MainActivity.KEY_LOGGED_IN_USER;
import static com.example.healthtrack.MainActivity.SESSION_PREFS_NAME;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.card.MaterialCardView;
import com.google.android.material.progressindicator.LinearProgressIndicator;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class DashboardActivity extends AppCompatActivity {

    private MaterialCardView hydrationButton, sleepButton, stepsButton, goalsButton;
    private MaterialCardView focusTimerButton, achievementsButton, reportsButton, notificationsButton;
    private TextView userNameTV, hydrationLabelTV, sleepLabelTV, stepsLabelTV;
    private LinearProgressIndicator hydrationPB, sleepPB, stepsPB;
    private ImageButton settingsIBTN;
    private SharedPreferences sessionManager, userPreferences, goalsPreferences;
    private final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

        sessionManager = getSharedPreferences(SESSION_PREFS_NAME, MODE_PRIVATE);
        String loggedInUser = sessionManager.getString(KEY_LOGGED_IN_USER, null);

        if (loggedInUser == null) {
            startActivity(new Intent(this, MainActivity.class));
            finish();
            return;
        }

        userPreferences = getSharedPreferences("user_prefs_" + loggedInUser, MODE_PRIVATE);
        goalsPreferences = getSharedPreferences("goals_prefs_" + loggedInUser, MODE_PRIVATE);

        userNameTV = findViewById(R.id.userNameTV);
        String username = userPreferences.getString("username", "User");
        userNameTV.setText(username);

        settingsIBTN = findViewById(R.id.settingsIBTN);
        settingsIBTN.setOnClickListener(v -> logout());

        hydrationLabelTV = findViewById(R.id.hydrationLabelTV);
        hydrationPB = findViewById(R.id.hydrationPB);

        sleepLabelTV = findViewById(R.id.sleepLabelTV);
        sleepPB = findViewById(R.id.sleepPB);

        stepsLabelTV = findViewById(R.id.stepsLabelTV);
        stepsPB = findViewById(R.id.stepsPB);

        updateUI();

        hydrationButton = findViewById(R.id.hydrationBTN);
        sleepButton = findViewById(R.id.sleepBTN);
        stepsButton = findViewById(R.id.stepsBTN);
        goalsButton = findViewById(R.id.goalsBTN);
        focusTimerButton = findViewById(R.id.focusTimerBTN);
        achievementsButton = findViewById(R.id.achievementsBTN);
        reportsButton = findViewById(R.id.reportsBTN);
        notificationsButton = findViewById(R.id.notificationsBTN);

        setupClickListeners();
    }

    private void setupClickListeners() {
        hydrationButton.setOnClickListener(v -> startActivity(new Intent(this, HydrationActivity.class)));
        sleepButton.setOnClickListener(v -> startActivity(new Intent(this, SleepActivity.class)));
        stepsButton.setOnClickListener(v -> startActivity(new Intent(this, StepCounterActivity.class)));
        goalsButton.setOnClickListener(v -> startActivity(new Intent(this, GoalsActivity.class)));
        focusTimerButton.setOnClickListener(v -> startActivity(new Intent(this, RelaxationActivity.class)));
        achievementsButton.setOnClickListener(v -> startActivity(new Intent(this, RewardsActivity.class)));
        reportsButton.setOnClickListener(v -> startActivity(new Intent(this, ReportsActivity.class)));
        notificationsButton.setOnClickListener(v -> startActivity(new Intent(this, NotificationActivity.class)));
    }

    private void updateUI() {
        updateHydrationUI();
        updateSleepUI();
        updateStepsUI();
    }

    private void updateHydrationUI() {
        String today = sdf.format(Calendar.getInstance().getTime());
        int currentHydration = userPreferences.getInt("hydration_" + today, 0);
        int dailyGoal = goalsPreferences.getInt("hydration_goal", 2000);
        hydrationLabelTV.setText(String.format(Locale.getDefault(), "Hydration: %d / %d ml", currentHydration, dailyGoal));
        hydrationPB.setMax(dailyGoal);
        hydrationPB.setProgress(currentHydration);
    }

    private void updateSleepUI() {
        String today = sdf.format(Calendar.getInstance().getTime());
        float currentSleep = userPreferences.getFloat("sleep_" + today, 0f);
        float dailyGoal = goalsPreferences.getFloat("sleep_goal", 8f);
        sleepLabelTV.setText(String.format(Locale.getDefault(), "Sleep: %.1f / %.1f hrs", currentSleep, dailyGoal));
        sleepPB.setMax((int) (dailyGoal * 10));
        sleepPB.setProgress((int) (currentSleep * 10));
    }

    private void updateStepsUI() {
        String today = sdf.format(Calendar.getInstance().getTime());
        int currentSteps = userPreferences.getInt("steps_" + today, 0);
        int dailyGoal = goalsPreferences.getInt("steps_goal", 10000);
        stepsLabelTV.setText(String.format(Locale.getDefault(), "Steps: %d / %d", currentSteps, dailyGoal));
        stepsPB.setMax(dailyGoal);
        stepsPB.setProgress(currentSteps);
    }

    private void logout() {
        sessionManager.edit().remove(KEY_LOGGED_IN_USER).apply();
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
        finish();
    }

    @Override
    protected void onResume() {
        super.onResume();
        String loggedInUser = sessionManager.getString(KEY_LOGGED_IN_USER, null);
        if (loggedInUser != null) {
            userPreferences = getSharedPreferences("user_prefs_" + loggedInUser, MODE_PRIVATE);
            goalsPreferences = getSharedPreferences("goals_prefs_" + loggedInUser, MODE_PRIVATE);
            updateUI();
        }
    }
}
