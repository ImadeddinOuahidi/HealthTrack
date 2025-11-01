package com.example.healthtrack;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class RewardsActivity extends AppCompatActivity {

    private SharedPreferences userPreferences;
    private String loggedInUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_rewards);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.rewards), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        loggedInUser = getSharedPreferences(MainActivity.SESSION_PREFS_NAME, MODE_PRIVATE)
                .getString(MainActivity.KEY_LOGGED_IN_USER, null);

        if (loggedInUser == null) {
            finish();
            return;
        }

        userPreferences = getSharedPreferences("user_prefs_" + loggedInUser, MODE_PRIVATE);

        setupBadgeClickListeners();
    }

    private void setupBadgeClickListeners() {
        int[] badgeIds = {
                R.id.badge1, R.id.badge2, R.id.badge3, R.id.badge4, R.id.badge5
        };
        String[] badgeKeys = {
                "badge_hydration", "badge_sleep", "badge_steps", "badge_focus", "badge_goal"
        };
        String[] badgeNames = {
                "Hydration Streak", "Sleep Champ", "10k Steps", "Focus Master", "Goal Setter"
        };

        for (int i = 0; i < badgeIds.length; i++) {
            LinearLayout badge = findViewById(badgeIds[i]);
            String key = badgeKeys[i];
            String name = badgeNames[i];
            badge.setOnClickListener(v -> {
                boolean unlocked = userPreferences.getBoolean(key, false);
                if (unlocked) {
                    Toast.makeText(this, name + " unlocked!", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(this, name + " is locked. Keep progressing!", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }
}
