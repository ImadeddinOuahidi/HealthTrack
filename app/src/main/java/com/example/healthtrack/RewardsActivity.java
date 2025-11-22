package com.example.healthtrack;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RewardsActivity extends AppCompatActivity {

    private RecyclerView achievementsRV;
    private AchievementAdapter adapter;
    private List<Achievement> masterAchievementList;

    private DatabaseReference mDatabase;
    private FirebaseUser currentUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rewards);

        currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) {
            finish();
            return;
        }
        mDatabase = FirebaseDatabase.getInstance().getReference().child("users").child(currentUser.getUid());

        achievementsRV = findViewById(R.id.achievementsRV);
        achievementsRV.setLayoutManager(new LinearLayoutManager(this));

        initializeAchievements();

        adapter = new AchievementAdapter(this, masterAchievementList);
        achievementsRV.setAdapter(adapter);

        loadUserAchievements();
    }

    private void initializeAchievements() {
        masterAchievementList = new ArrayList<>();
        
        // ========== HYDRATION ACHIEVEMENTS ==========
        masterAchievementList.add(new Achievement("hydration_novice", "Hydration Novice", "Log your first 2,000 ml of water.", R.drawable.hydration));
        masterAchievementList.add(new Achievement("hydration_enthusiast", "Hydration Enthusiast", "Reach 3,000 ml in a single day.", R.drawable.hydration));
        masterAchievementList.add(new Achievement("hydration_master", "Hydration Master", "Reach 4,000 ml in a single day.", R.drawable.hydration));
        masterAchievementList.add(new Achievement("water_warrior", "Water Warrior", "Log 50,000 ml of water total.", R.drawable.hydration));
        masterAchievementList.add(new Achievement("aqua_legend", "Aqua Legend", "Log 100,000 ml of water total.", R.drawable.hydration));
        masterAchievementList.add(new Achievement("hydration_streak_7", "Week Hydrator", "Maintain hydration goal for 7 consecutive days.", R.drawable.hydration));
        masterAchievementList.add(new Achievement("hydration_streak_30", "Monthly Hydrator", "Maintain hydration goal for 30 consecutive days.", R.drawable.hydration));
        
        // ========== SLEEP ACHIEVEMENTS ==========
        masterAchievementList.add(new Achievement("sleep_initiate", "Sleep Initiate", "Log your first 8 hours of sleep.", R.drawable.sleep));
        masterAchievementList.add(new Achievement("sleep_warrior", "Sleep Warrior", "Log 9 hours of sleep in a single night.", R.drawable.sleep));
        masterAchievementList.add(new Achievement("sleep_master", "Sleep Master", "Log 10 hours of sleep in a single night.", R.drawable.sleep));
        masterAchievementList.add(new Achievement("sleep_consistency", "Consistent Sleeper", "Log sleep for 7 consecutive days.", R.drawable.sleep));
        masterAchievementList.add(new Achievement("sleep_excellence", "Sleep Excellence", "Log sleep for 30 consecutive days.", R.drawable.sleep));
        masterAchievementList.add(new Achievement("perfect_sleep_week", "Perfect Sleep Week", "Meet sleep goal for 7 consecutive days.", R.drawable.sleep));
        masterAchievementList.add(new Achievement("sleep_centurion", "Sleep Centurion", "Log 100 total hours of sleep.", R.drawable.sleep));
        
        // ========== STEPS ACHIEVEMENTS ==========
        masterAchievementList.add(new Achievement("first_steps", "First Steps", "Log your first 1,000 steps.", R.drawable.footstep));
        masterAchievementList.add(new Achievement("daily_walker", "Daily Walker", "Reach 5,000 steps in a day.", R.drawable.footstep));
        masterAchievementList.add(new Achievement("step_champion", "Step Champion", "Reach 10,000 steps in a day.", R.drawable.footstep));
        masterAchievementList.add(new Achievement("step_elite", "Step Elite", "Reach 15,000 steps in a day.", R.drawable.footstep));
        masterAchievementList.add(new Achievement("step_legend", "Step Legend", "Reach 20,000 steps in a day.", R.drawable.footstep));
        masterAchievementList.add(new Achievement("marathon_runner", "Marathon Runner", "Log a total of 42,195 steps.", R.drawable.footstep));
        masterAchievementList.add(new Achievement("step_milestone_100k", "100K Steps", "Log 100,000 total steps.", R.drawable.footstep));
        masterAchievementList.add(new Achievement("step_milestone_500k", "500K Steps", "Log 500,000 total steps.", R.drawable.footstep));
        masterAchievementList.add(new Achievement("step_streak_7", "Week Walker", "Reach daily step goal for 7 consecutive days.", R.drawable.footstep));
        masterAchievementList.add(new Achievement("step_streak_30", "Monthly Walker", "Reach daily step goal for 30 consecutive days.", R.drawable.footstep));
        
        // ========== GOAL ACHIEVEMENTS ==========
        masterAchievementList.add(new Achievement("goal_setter", "Goal Setter", "Set your first personal goal.", R.drawable.target));
        masterAchievementList.add(new Achievement("goal_achiever", "Goal Achiever", "Achieve all three daily goals in one day.", R.drawable.target));
        masterAchievementList.add(new Achievement("goal_master", "Goal Master", "Achieve all daily goals for 7 consecutive days.", R.drawable.target));
        masterAchievementList.add(new Achievement("goal_legend", "Goal Legend", "Achieve all daily goals for 30 consecutive days.", R.drawable.target));
        masterAchievementList.add(new Achievement("triple_threat", "Triple Threat", "Complete hydration, sleep, and steps goals in one day.", R.drawable.target));
        masterAchievementList.add(new Achievement("perfect_week", "Perfect Week", "Achieve all goals every day for a week.", R.drawable.target));
        
        // ========== RELAXATION/FOCUS TIMER ACHIEVEMENTS ==========
        masterAchievementList.add(new Achievement("focus_beginner", "Focus Beginner", "Complete your first 5-minute focus session.", R.drawable.timer));
        masterAchievementList.add(new Achievement("focus_enthusiast", "Focus Enthusiast", "Complete a 15-minute focus session.", R.drawable.timer));
        masterAchievementList.add(new Achievement("focus_master", "Focus Master", "Complete a 30-minute focus session.", R.drawable.timer));
        masterAchievementList.add(new Achievement("focus_zen", "Focus Zen", "Complete a 60-minute focus session.", R.drawable.timer));
        masterAchievementList.add(new Achievement("focus_streak_7", "Weekly Focus", "Complete focus sessions for 7 consecutive days.", R.drawable.timer));
        masterAchievementList.add(new Achievement("focus_hours_10", "10 Hours Focused", "Complete 10 total hours of focus time.", R.drawable.timer));
        masterAchievementList.add(new Achievement("focus_hours_50", "50 Hours Focused", "Complete 50 total hours of focus time.", R.drawable.timer));
        
        // ========== CONSISTENCY & STREAK ACHIEVEMENTS ==========
        masterAchievementList.add(new Achievement("daily_logger_7", "Week Logger", "Log activities for 7 consecutive days.", R.drawable.medal_));
        masterAchievementList.add(new Achievement("daily_logger_30", "Monthly Logger", "Log activities for 30 consecutive days.", R.drawable.medal_));
        masterAchievementList.add(new Achievement("daily_logger_100", "Centurion Logger", "Log activities for 100 consecutive days.", R.drawable.medal_));
        masterAchievementList.add(new Achievement("consistency_king", "Consistency King", "Maintain a 7-day streak across all activities.", R.drawable.medal_));
        masterAchievementList.add(new Achievement("consistency_legend", "Consistency Legend", "Maintain a 30-day streak across all activities.", R.drawable.medal_));
        
        // ========== MILESTONE ACHIEVEMENTS ==========
        masterAchievementList.add(new Achievement("first_week", "First Week", "Complete your first week using HealthTrack.", R.drawable.medal_));
        masterAchievementList.add(new Achievement("first_month", "First Month", "Complete your first month using HealthTrack.", R.drawable.medal_));
        masterAchievementList.add(new Achievement("dedicated_user", "Dedicated User", "Use HealthTrack for 100 days.", R.drawable.medal_));
        masterAchievementList.add(new Achievement("loyal_member", "Loyal Member", "Use HealthTrack for 365 days.", R.drawable.medal_));
        
        // ========== SPECIAL ACHIEVEMENTS ==========
        masterAchievementList.add(new Achievement("early_bird", "Early Bird", "Log your sleep before 10 PM.", R.drawable.sleep));
        masterAchievementList.add(new Achievement("night_owl", "Night Owl", "Log your sleep after midnight.", R.drawable.sleep));
        masterAchievementList.add(new Achievement("morning_person", "Morning Person", "Log activities before 8 AM.", R.drawable.footstep));
        masterAchievementList.add(new Achievement("overachiever", "Overachiever", "Exceed all three daily goals by 50%.", R.drawable.target));
        masterAchievementList.add(new Achievement("balanced_life", "Balanced Life", "Maintain balanced goals for 14 consecutive days.", R.drawable.medal_));
        masterAchievementList.add(new Achievement("wellness_warrior", "Wellness Warrior", "Complete 100 total activities across all categories.", R.drawable.medal_));
        masterAchievementList.add(new Achievement("health_champion", "Health Champion", "Complete 500 total activities across all categories.", R.drawable.medal_));
        masterAchievementList.add(new Achievement("report_analyzer", "Report Analyzer", "View your reports 10 times.", R.drawable.report));
        masterAchievementList.add(new Achievement("notification_setter", "Notification Setter", "Set up all notification reminders.", R.drawable.notification));
    }

    private void loadUserAchievements() {
        mDatabase.child("achievements").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    Map<String, Boolean> unlockedAchievements = new HashMap<>();
                    for (DataSnapshot achievementSnapshot : snapshot.getChildren()) {
                        unlockedAchievements.put(achievementSnapshot.getKey(), achievementSnapshot.getValue(Boolean.class));
                    }

                    for (Achievement achievement : masterAchievementList) {
                        if (unlockedAchievements.containsKey(achievement.getId()) && unlockedAchievements.get(achievement.getId())) {
                            achievement.setUnlocked(true);
                        }
                    }
                    adapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Handle error
            }
        });
    }
}
