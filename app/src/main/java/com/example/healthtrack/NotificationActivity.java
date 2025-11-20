package com.example.healthtrack;

import static com.example.healthtrack.MainActivity.KEY_LOGGED_IN_USER;
import static com.example.healthtrack.MainActivity.SESSION_PREFS_NAME;

import android.app.NotificationManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.switchmaterial.SwitchMaterial;

public class NotificationActivity extends AppCompatActivity {

    private SwitchMaterial switchHydration, switchSleep, switchSteps, switchChallenges;
    private Button clearAllBTN;
    private SharedPreferences notificationPrefs;

    private static final String KEY_HYDRATION_REMINDER = "hydration_reminder";
    private static final String KEY_SLEEP_REMINDER = "sleep_reminder";
    private static final String KEY_STEPS_REMINDER = "steps_reminder";
    private static final String KEY_CHALLENGES_REMINDER = "challenges_reminder";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notification);

        SharedPreferences sessionManager = getSharedPreferences(SESSION_PREFS_NAME, MODE_PRIVATE);
        String loggedInUser = sessionManager.getString(KEY_LOGGED_IN_USER, null);

        if (loggedInUser == null) {
            finish();
            return;
        }

        notificationPrefs = getSharedPreferences("notification_prefs_" + loggedInUser, MODE_PRIVATE);

        switchHydration = findViewById(R.id.switchHydration);
        switchSleep = findViewById(R.id.switchSleep);
        switchSteps = findViewById(R.id.switchSteps);
        switchChallenges = findViewById(R.id.switchChallenges);
        clearAllBTN = findViewById(R.id.clearAllBTN);

        loadSettings();
        setupListeners();
    }

    private void loadSettings() {
        switchHydration.setChecked(notificationPrefs.getBoolean(KEY_HYDRATION_REMINDER, true));
        switchSleep.setChecked(notificationPrefs.getBoolean(KEY_SLEEP_REMINDER, true));
        switchSteps.setChecked(notificationPrefs.getBoolean(KEY_STEPS_REMINDER, true));
        switchChallenges.setChecked(notificationPrefs.getBoolean(KEY_CHALLENGES_REMINDER, true));
    }

    private void setupListeners() {
        switchHydration.setOnCheckedChangeListener((buttonView, isChecked) -> {
            saveSetting(KEY_HYDRATION_REMINDER, isChecked);
        });

        switchSleep.setOnCheckedChangeListener((buttonView, isChecked) -> {
            saveSetting(KEY_SLEEP_REMINDER, isChecked);
        });

        switchSteps.setOnCheckedChangeListener((buttonView, isChecked) -> {
            saveSetting(KEY_STEPS_REMINDER, isChecked);
        });

        switchChallenges.setOnCheckedChangeListener((buttonView, isChecked) -> {
            saveSetting(KEY_CHALLENGES_REMINDER, isChecked);
        });

        clearAllBTN.setOnClickListener(v -> {
            NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            if (notificationManager != null) {
                notificationManager.cancelAll();
            }
            Toast.makeText(this, "All notifications cleared!", Toast.LENGTH_SHORT).show();
        });
    }

    private void saveSetting(String key, boolean value) {
        SharedPreferences.Editor editor = notificationPrefs.edit();
        editor.putBoolean(key, value);
        editor.apply();
    }
}
