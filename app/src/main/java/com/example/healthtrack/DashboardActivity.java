package com.example.healthtrack;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.card.MaterialCardView;
import com.google.android.material.progressindicator.CircularProgressIndicator;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class DashboardActivity extends AppCompatActivity {

    private MaterialCardView hydrationButton, sleepButton, stepsButton, goalsButton;
    private MaterialCardView focusTimerButton, achievementsButton, reportsButton, notificationsButton;
    private TextView userNameTV, hydrationLabelTV, sleepLabelTV, stepsLabelTV, profileInitialTV;
    private TextView hydrationPercentTV, hydrationValueTV, sleepPercentTV, sleepValueTV, stepsPercentTV, stepsValueTV;
    private com.google.android.material.progressindicator.CircularProgressIndicator hydrationPB, sleepPB, stepsPB;
    private ImageButton logoutIBTN;

    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;
    private FirebaseUser currentUser;

    private final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            startActivity(new Intent(this, MainActivity.class));
            finish();
            return;
        }
        mDatabase = FirebaseDatabase.getInstance().getReference().child("users").child(currentUser.getUid());

        // Views
        userNameTV = findViewById(R.id.userNameTV);
        profileInitialTV = findViewById(R.id.profileInitialTV);
        hydrationLabelTV = findViewById(R.id.hydrationLabelTV);
        hydrationPB = findViewById(R.id.hydrationPB);
        hydrationPercentTV = findViewById(R.id.hydrationPercentTV);
        hydrationValueTV = findViewById(R.id.hydrationValueTV);
        sleepLabelTV = findViewById(R.id.sleepLabelTV);
        sleepPB = findViewById(R.id.sleepPB);
        sleepPercentTV = findViewById(R.id.sleepPercentTV);
        sleepValueTV = findViewById(R.id.sleepValueTV);
        stepsLabelTV = findViewById(R.id.stepsLabelTV);
        stepsPB = findViewById(R.id.stepsPB);
        stepsPercentTV = findViewById(R.id.stepsPercentTV);
        stepsValueTV = findViewById(R.id.stepsValueTV);
        logoutIBTN = findViewById(R.id.logoutIBTN);
        hydrationButton = findViewById(R.id.hydrationBTN);
        sleepButton = findViewById(R.id.sleepBTN);
        stepsButton = findViewById(R.id.stepsBTN);
        goalsButton = findViewById(R.id.goalsBTN);
        focusTimerButton = findViewById(R.id.focusTimerBTN);
        achievementsButton = findViewById(R.id.achievementsBTN);
        reportsButton = findViewById(R.id.reportsBTN);
        notificationsButton = findViewById(R.id.notificationsBTN);

        setupClickListeners();
        loadData();
    }

    private void setupClickListeners() {
        logoutIBTN.setOnClickListener(v -> {
            mAuth.signOut();
            startActivity(new Intent(this, MainActivity.class));
            finish();
        });
        hydrationButton.setOnClickListener(v -> startActivity(new Intent(this, HydrationActivity.class)));
        sleepButton.setOnClickListener(v -> startActivity(new Intent(this, SleepActivity.class)));
        stepsButton.setOnClickListener(v -> startActivity(new Intent(this, StepCounterActivity.class)));
        goalsButton.setOnClickListener(v -> startActivity(new Intent(this, GoalsActivity.class)));
        focusTimerButton.setOnClickListener(v -> startActivity(new Intent(this, RelaxationActivity.class)));
        achievementsButton.setOnClickListener(v -> startActivity(new Intent(this, RewardsActivity.class)));
        reportsButton.setOnClickListener(v -> startActivity(new Intent(this, ReportsActivity.class)));
        notificationsButton.setOnClickListener(v -> startActivity(new Intent(this, NotificationActivity.class)));
    }

    private void loadData() {
        // Load profile
        mDatabase.child("profile").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    String username = snapshot.child("username").getValue(String.class);
                    userNameTV.setText(username);
                    if (username != null && !username.isEmpty()) {
                        profileInitialTV.setText(String.valueOf(username.charAt(0)).toUpperCase());
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) { }
        });

        // Load goals and daily logs
        mDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    String today = sdf.format(Calendar.getInstance().getTime());
                    // Goals
                    Integer hydrationGoal = snapshot.child("goals/hydration_goal").getValue(Integer.class);
                    Float sleepGoal = snapshot.child("goals/sleep_goal").getValue(Float.class);
                    Integer stepGoal = snapshot.child("goals/steps_goal").getValue(Integer.class);

                    // Daily logs
                    Integer currentHydration = snapshot.child("daily_logs/" + today + "/hydration").getValue(Integer.class);
                    Float currentSleep = snapshot.child("daily_logs/" + today + "/sleep").getValue(Float.class);
                    Integer currentSteps = snapshot.child("daily_logs/" + today + "/steps").getValue(Integer.class);

                    updateHydrationUI(currentHydration != null ? currentHydration : 0, hydrationGoal != null ? hydrationGoal : 2000);
                    updateSleepUI(currentSleep != null ? currentSleep : 0f, sleepGoal != null ? sleepGoal : 8f);
                    updateStepsUI(currentSteps != null ? currentSteps : 0, stepGoal != null ? stepGoal : 10000);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) { }
        });
    }

    private void updateHydrationUI(int current, int goal) {
        hydrationLabelTV.setText("Hydration");
        hydrationValueTV.setText(String.format(Locale.getDefault(), "%d / %d ml", current, goal));
        
        int percentage = goal > 0 ? (int) ((current * 100.0f) / goal) : 0;
        percentage = Math.min(percentage, 100); // Cap at 100%
        
        hydrationPercentTV.setText(String.format(Locale.getDefault(), "%d%%", percentage));
        hydrationPB.setMax(100);
        hydrationPB.setProgress(percentage, true);
    }

    private void updateSleepUI(float current, float goal) {
        sleepLabelTV.setText("Sleep");
        sleepValueTV.setText(String.format(Locale.getDefault(), "%.1f / %.1f hrs", current, goal));
        
        int percentage = goal > 0 ? (int) ((current * 100.0f) / goal) : 0;
        percentage = Math.min(percentage, 100); // Cap at 100%
        
        sleepPercentTV.setText(String.format(Locale.getDefault(), "%d%%", percentage));
        sleepPB.setMax(100);
        sleepPB.setProgress(percentage, true);
    }

    private void updateStepsUI(int current, int goal) {
        stepsLabelTV.setText("Steps");
        stepsValueTV.setText(String.format(Locale.getDefault(), "%d / %d", current, goal));
        
        int percentage = goal > 0 ? (int) ((current * 100.0f) / goal) : 0;
        percentage = Math.min(percentage, 100); // Cap at 100%
        
        stepsPercentTV.setText(String.format(Locale.getDefault(), "%d%%", percentage));
        stepsPB.setMax(100);
        stepsPB.setProgress(percentage, true);
    }
}
