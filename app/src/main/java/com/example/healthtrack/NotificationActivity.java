package com.example.healthtrack;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.Toast;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

public class NotificationActivity extends AppCompatActivity {

    private Switch switchHydration, switchSleep, switchSteps, switchChallenges;
    private Button clearAllBTN;
    private SharedPreferences notifPrefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notification);

        notifPrefs = getSharedPreferences("notification_prefs", MODE_PRIVATE);

        switchHydration = findViewById(R.id.switchHydration);
        switchSleep = findViewById(R.id.switchSleep);
        switchSteps = findViewById(R.id.switchSteps);
        switchChallenges = findViewById(R.id.switchChallenges);
        clearAllBTN = findViewById(R.id.clearAllBTN);

        loadPreferences();

        CompoundButton.OnCheckedChangeListener listener = (button, isChecked) -> {
            String key = "";
            if (button == switchHydration) key = "hydration_enabled";
            else if (button == switchSleep) key = "sleep_enabled";
            else if (button == switchSteps) key = "steps_enabled";
            else if (button == switchChallenges) key = "challenges_enabled";

            notifPrefs.edit().putBoolean(key, isChecked).apply();
            String msg = (isChecked ? "Enabled " : "Disabled ") + key.replace("_enabled", "") + " notifications";
            Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
        };

        switchHydration.setOnCheckedChangeListener(listener);
        switchSleep.setOnCheckedChangeListener(listener);
        switchSteps.setOnCheckedChangeListener(listener);
        switchChallenges.setOnCheckedChangeListener(listener);

        clearAllBTN.setOnClickListener(v -> clearAll());
    }

    private void loadPreferences() {
        switchHydration.setChecked(notifPrefs.getBoolean("hydration_enabled", true));
        switchSleep.setChecked(notifPrefs.getBoolean("sleep_enabled", true));
        switchSteps.setChecked(notifPrefs.getBoolean("steps_enabled", false));
        switchChallenges.setChecked(notifPrefs.getBoolean("challenges_enabled", false));
    }

    private void clearAll() {
        notifPrefs.edit().clear().apply();
        loadPreferences();
        Toast.makeText(this, "All reminders cleared", Toast.LENGTH_SHORT).show();
    }
}
