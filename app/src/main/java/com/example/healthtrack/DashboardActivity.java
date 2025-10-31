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

public class DashboardActivity extends AppCompatActivity {

    private MaterialCardView hydrationButton, sleepButton, stepsButton, goalsButton;
    private MaterialCardView focusTimerButton, achievementsButton, reportsButton, notificationsButton;
    private TextView userNameTV, hydrationLabelTV;
    private LinearProgressIndicator hydrationPB;
    private ImageButton settingsIBTN;
    private SharedPreferences sessionManager, userPreferences;

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

        userNameTV = findViewById(R.id.userNameTV);
        String username = userPreferences.getString("username", "User");
        userNameTV.setText(username);

        settingsIBTN = findViewById(R.id.settingsIBTN);
        settingsIBTN.setOnClickListener(v -> logout());

        hydrationLabelTV = findViewById(R.id.hydrationLabelTV);
        hydrationPB = findViewById(R.id.hydrationPB);
        updateHydrationUI();

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

    private void updateHydrationUI() {
        int currentHydration = userPreferences.getInt("hydration", 0);
        int dailyGoal = 2000;
        hydrationLabelTV.setText("Hydration: " + currentHydration + " / " + dailyGoal + " ml");
        hydrationPB.setMax(dailyGoal);
        hydrationPB.setProgress(currentHydration);
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
            updateHydrationUI();
        }
    }
}