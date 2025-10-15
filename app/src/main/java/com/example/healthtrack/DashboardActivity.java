package com.example.healthtrack;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
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
        hydrationButton.setOnClickListener(v -> {
            Intent intent = new Intent(DashboardActivity.this, HydrationActivity.class);
            startActivity(intent);
        });

        sleepButton.setOnClickListener(v -> {

        });

        stepsButton.setOnClickListener(v -> {

        });

        goalsButton.setOnClickListener(v -> {

        });

        focusTimerButton.setOnClickListener(v -> {

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