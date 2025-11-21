package com.example.healthtrack;

import android.app.AlertDialog;
import android.os.Bundle;
import android.text.InputType;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.progressindicator.CircularProgressIndicator;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class HydrationActivity extends AppCompatActivity {

    private TextView dailyTotalTV, goalTV, noDataTV;
    private MaterialButton add250BTN, add500BTN, customBTN;
    private CircularProgressIndicator hydrationCircularProgress;
    private LineChart hydrationGraph;

    private DatabaseReference mDatabase;
    private FirebaseUser currentUser;

    private int currentHydration = 0;
    private int hydrationGoal = 2000; // Default goal
    private final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
    private final SimpleDateFormat timeSdf = new SimpleDateFormat("HH:mm", Locale.getDefault());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_hydration);

        currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) {
            finish();
            return;
        }
        mDatabase = FirebaseDatabase.getInstance().getReference().child("users").child(currentUser.getUid());

        dailyTotalTV = findViewById(R.id.dailyTotalTV);
        goalTV = findViewById(R.id.goalTV);
        hydrationCircularProgress = findViewById(R.id.hydrationCircularProgress);
        hydrationGraph = findViewById(R.id.hydrationGraph);
        noDataTV = findViewById(R.id.noDataTV);
        add250BTN = findViewById(R.id.add250BTN);
        add500BTN = findViewById(R.id.add500BTN);
        customBTN = findViewById(R.id.customBTN);

        loadGoalAndThenData();

        add250BTN.setOnClickListener(v -> addWater(250));
        add500BTN.setOnClickListener(v -> addWater(500));
        customBTN.setOnClickListener(v -> showCustomAddDialog());
    }

    private void loadGoalAndThenData() {
        mDatabase.child("goals/hydration_goal").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    Integer goal = snapshot.getValue(Integer.class);
                    if (goal != null) {
                        hydrationGoal = goal;
                    }
                }
                loadHydrationData();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                loadHydrationData();
            }
        });
    }

    private void loadHydrationData() {
        String today = sdf.format(Calendar.getInstance().getTime());
        mDatabase.child("daily_logs").child(today).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                final List<Entry> entries = new ArrayList<>();
                currentHydration = 0;

                if (snapshot.exists()) {
                    Integer totalHydration = snapshot.child("hydration").getValue(Integer.class);
                    currentHydration = totalHydration != null ? totalHydration : 0;

                    List<String> timeEntries = new ArrayList<>();
                    for (DataSnapshot entrySnapshot : snapshot.child("hydration_entries").getChildren()) {
                        timeEntries.add(entrySnapshot.getValue(String.class));
                    }
                    Collections.sort(timeEntries, Comparator.comparingLong(entry -> {
                        try {
                            return Long.parseLong(entry.split(":")[0]);
                        } catch (Exception e) {
                            return 0L;
                        }
                    }));

                    int cumulativeAmount = 0;
                    for (String entry : timeEntries) {
                        try {
                            long timestamp = Long.parseLong(entry.split(":")[0]);
                            int amount = Integer.parseInt(entry.split(":")[1]);
                            cumulativeAmount += amount;
                            entries.add(new Entry(timestamp, cumulativeAmount));
                        } catch (Exception e) {
                            // Ignore malformed entries
                        }
                    }
                }

                updateGraph(entries);
                updateUI();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                updateGraph(new ArrayList<>());
            }
        });
    }

    private void addWater(int amount) {
        String today = sdf.format(Calendar.getInstance().getTime());
        DatabaseReference todayRef = mDatabase.child("daily_logs").child(today);

        todayRef.child("hydration").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                int newTotal = (snapshot.exists() && snapshot.getValue(Integer.class) != null ? snapshot.getValue(Integer.class) : 0) + amount;
                todayRef.child("hydration").setValue(newTotal);

                String entry = System.currentTimeMillis() + ":" + amount;
                todayRef.child("hydration_entries").push().setValue(entry);

                Toast.makeText(HydrationActivity.this, amount + " ml added", Toast.LENGTH_SHORT).show();

                checkAchievements(newTotal);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) { }
        });
    }

    private void checkAchievements(int newTotal) {
        if (newTotal >= 2000) {
            mDatabase.child("achievements").child("hydration_novice").setValue(true);
        }
    }


    private void updateUI() {
        dailyTotalTV.setText(String.format(Locale.getDefault(), "%d ml", currentHydration));
        goalTV.setText(String.format(Locale.getDefault(), "of %d ml", hydrationGoal));
        if (hydrationGoal > 0) {
            int progress = (int) ((currentHydration * 100.0f) / hydrationGoal);
            hydrationCircularProgress.setProgress(progress, true);
        }
    }
    private void updateGraph(List<Entry> entries) {
        if (entries.isEmpty()) {
            hydrationGraph.setVisibility(View.GONE);
            noDataTV.setVisibility(View.VISIBLE);
        } else {
            hydrationGraph.setVisibility(View.VISIBLE);
            noDataTV.setVisibility(View.GONE);

            LineDataSet dataSet = new LineDataSet(entries, "Hydration");
            dataSet.setColor(getResources().getColor(R.color.purple_500));
            dataSet.setCircleColor(getResources().getColor(R.color.purple_500));
            dataSet.setCircleRadius(4f);
            dataSet.setDrawValues(false);

            LineData lineData = new LineData(dataSet);
            hydrationGraph.setData(lineData);

            XAxis xAxis = hydrationGraph.getXAxis();
            xAxis.setValueFormatter(new ValueFormatter() {
                @Override
                public String getFormattedValue(float value) {
                    return timeSdf.format(new Date((long) value));
                }
            });
            xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
            xAxis.setGranularity(1f);

            hydrationGraph.getAxisRight().setEnabled(false);
            hydrationGraph.getDescription().setEnabled(false);
            hydrationGraph.invalidate();
        }
    }


    private void showCustomAddDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Add Custom Amount");

        final EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_CLASS_NUMBER);
        input.setHint("Enter amount in ml");
        builder.setView(input);

        builder.setPositiveButton("Add", (dialog, which) -> {
            String amountStr = input.getText().toString();
            if (!amountStr.isEmpty()) {
                try {
                    int amount = Integer.parseInt(amountStr);
                    if (amount > 0) {
                        addWater(amount);
                    } else {
                        Toast.makeText(this, "Please enter a positive amount", Toast.LENGTH_SHORT).show();
                    }
                } catch (NumberFormatException e) {
                    Toast.makeText(this, "Please enter a valid number", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(this, "Please enter an amount", Toast.LENGTH_SHORT).show();
            }
        });
        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());

        builder.show();
    }
}
