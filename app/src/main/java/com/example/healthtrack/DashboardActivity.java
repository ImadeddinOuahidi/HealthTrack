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
    private ProgressBar hydrationPB;
    private TextView hydrationLabelTV;
    private SharedPreferences sharedPreferences;
    private final int hydrationGoal = 2000;

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

        hydrationPB = findViewById(R.id.hydrationPB);
        hydrationLabelTV = findViewById(R.id.hydrationLabelTV);

        sharedPreferences = getSharedPreferences("HydrationPrefs", MODE_PRIVATE);

        hydrationButton.setOnClickListener(v -> startActivity(new Intent(DashboardActivity.this, HydrationActivity.class)));
        focusTimerButton.setOnClickListener(v -> startActivity(new Intent(DashboardActivity.this, RelaxationActivity.class)));

        sleepButton.setOnClickListener(v -> {});
        stepsButton.setOnClickListener(v -> {});
        goalsButton.setOnClickListener(v -> startActivity(new Intent(DashboardActivity.this, GoalsActivity.class)));
        achievementsButton.setOnClickListener(v -> {});
        reportsButton.setOnClickListener(v -> {});
        notificationsButton.setOnClickListener(v -> {});

        updateHydrationProgress();
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateHydrationProgress();
    }

    private void updateHydrationProgress() {
        int dailyTotal = sharedPreferences.getInt("dailyTotal", 0) + sharedPreferences.getInt("hydrationBreak", 0) * 250;
        int progress = Math.min(100, (dailyTotal * 100) / hydrationGoal);
        hydrationPB.setProgress(progress);
        hydrationLabelTV.setText("Hydration: " + dailyTotal + " / " + hydrationGoal + " ml");
    }
}
