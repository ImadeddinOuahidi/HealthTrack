package com.example.healthtrack;

import static com.example.healthtrack.MainActivity.KEY_LOGGED_IN_USER;
import static com.example.healthtrack.MainActivity.SESSION_PREFS_NAME;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class DashboardActivity extends AppCompatActivity {

    private Button hydrationButton, sleepButton, stepsButton, goalsButton;
    private Button focusTimerButton, achievementsButton, reportsButton, notificationsButton;
    private TextView userNameTV, hydrationLabelTV;
    private ProgressBar hydrationPB;
    private ImageButton settingsIBTN;
    private SharedPreferences sessionManager, userPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_dashboard);

        sessionManager = getSharedPreferences(SESSION_PREFS_NAME, MODE_PRIVATE);
        String loggedInUser = sessionManager.getString(KEY_LOGGED_IN_USER, null);

        // check if any user is logged in
        if (loggedInUser == null) {
            startActivity(new Intent(this, MainActivity.class));
            finish();
            return;
        }

        userPreferences = getSharedPreferences("user_prefs_" + loggedInUser, MODE_PRIVATE);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.dashboard), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

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

        setupNavigationButtons();
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

    private void setupNavigationButtons() {
        hydrationButton.setOnClickListener(v -> {
            Intent intent = new Intent(DashboardActivity.this, HydrationActivity.class);
            startActivity(intent);
        });

        sleepButton.setOnClickListener(v -> {
            Intent intent = new Intent(DashboardActivity.this, SleepActivity.class);
            startActivity(intent);
        });

        stepsButton.setOnClickListener(v -> {
            Intent intent = new Intent(DashboardActivity.this, StepCounterActivity.class);
            startActivity(intent);
        });

        goalsButton.setOnClickListener(v -> {
            Intent intent = new Intent(DashboardActivity.this, GoalsActivity.class);
            startActivity(intent);
        });

        focusTimerButton.setOnClickListener(v -> {
            Intent intent = new Intent(DashboardActivity.this, RelaxationActivity.class);
            startActivity(intent);
        });

        achievementsButton.setOnClickListener(v -> {
            Intent intent = new Intent(DashboardActivity.this, RewardsActivity.class);
            startActivity(intent);
        });

        reportsButton.setOnClickListener(v -> {
            Intent intent = new Intent(DashboardActivity.this, ReportsActivity.class);
            startActivity(intent);
        });

        notificationsButton.setOnClickListener(v -> {
            Intent intent = new Intent(DashboardActivity.this, NotificationActivity.class);
            startActivity(intent);
        });
    }
}
