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
        // Add all your preset achievements here
        masterAchievementList.add(new Achievement("first_steps", "First Steps", "Log your first 1,000 steps.", R.drawable.footstep));
        masterAchievementList.add(new Achievement("hydration_novice", "Hydration Novice", "Log your first 2,000 ml of water.", R.drawable.hydration));
        masterAchievementList.add(new Achievement("sleep_initiate", "Sleep Initiate", "Log your first 8 hours of sleep.", R.drawable.sleep));
        masterAchievementList.add(new Achievement("goal_setter", "Goal Setter", "Set your first personal goal.", R.drawable.target));
        masterAchievementList.add(new Achievement("marathon_runner", "Marathon Runner", "Log a total of 42,195 steps.", R.drawable.footstep));
        masterAchievementList.add(new Achievement("water_warrior", "Water Warrior", "Log 50,000 ml of water.", R.drawable.hydration));
        // Add more achievements as needed
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
