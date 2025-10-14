package com.example.healthtrack;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class DashboardActivity extends AppCompatActivity {

    private Button hydrationButton, sleepButton, stepsButton, goalsButton;
    private Button focusTimerButton, achievementsButton, reportsButton, notificationsButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_dashboard);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.dashboard), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Initialize buttons
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

    private void setupNavigationButtons() {
        // Navigate to Hydration screen
        hydrationButton.setOnClickListener(v -> {
            Intent intent = new Intent(DashboardActivity.this, HydrationActivity.class);
            startActivity(intent);
        });

        // Navigate to Sleep screen (to be added later)
        sleepButton.setOnClickListener(v -> {

        });

        // Navigate to Step Counter screen
        stepsButton.setOnClickListener(v -> {
            Intent intent = new Intent(DashboardActivity.this, StepCounterActivity.class);
            startActivity(intent);
        });

        // Navigate to Goals screen (to be added later)
        goalsButton.setOnClickListener(v -> {

        });

        // Navigate to Relaxation (Focus Timer) screen
        focusTimerButton.setOnClickListener(v -> {
            Intent intent = new Intent(DashboardActivity.this, RelaxationActivity.class);
            startActivity(intent);
        });

        // Achievements screen (to be added later)
        achievementsButton.setOnClickListener(v -> {

        });

        // Reports screen (to be added later)
        reportsButton.setOnClickListener(v -> {

        });

        // Notifications screen (to be added later)
        notificationsButton.setOnClickListener(v -> {

        });
    }
}
