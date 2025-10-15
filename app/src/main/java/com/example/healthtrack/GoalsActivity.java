package com.example.healthtrack;

import android.os.Bundle;
import android.widget.ProgressBar;
import androidx.appcompat.app.AppCompatActivity;

public class GoalsActivity extends AppCompatActivity {

    private ProgressBar progressHydrationGoal, progressSleepGoal, progressStepGoal, progressChallenge;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_goals);

        // Initialize ProgressBars
        progressHydrationGoal = findViewById(R.id.progressHydrationGoal);
        progressSleepGoal = findViewById(R.id.progressSleepGoal);
        progressStepGoal = findViewById(R.id.progressStepGoal);
        progressChallenge = findViewById(R.id.progressChallenge);

        // Example: set progress values (demo)
        progressHydrationGoal.setProgress(60);
        progressSleepGoal.setProgress(80);
        progressStepGoal.setProgress(85);
        progressChallenge.setProgress(40);
    }
}
