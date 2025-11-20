package com.example.healthtrack;

import static com.example.healthtrack.MainActivity.KEY_LOGGED_IN_USER;
import static com.example.healthtrack.MainActivity.SESSION_PREFS_NAME;

import android.app.AlertDialog;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.InputType;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.progressindicator.CircularProgressIndicator;
import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.helper.DateAsXAxisLabelFormatter;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public class HydrationActivity extends AppCompatActivity {

    private TextView dailyTotalTV, goalTV;
    private MaterialButton add250BTN, add500BTN, customBTN;
    private CircularProgressIndicator hydrationCircularProgress;
    private GraphView hydrationGraph;

    private SharedPreferences userPreferences;
    private int currentHydration = 0;
    private final int hydrationGoal = 2000;
    private final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
    private final String HYDRATION_ENTRIES_KEY_PREFIX = "hydration_entries_";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_hydration);

        SharedPreferences sessionManager = getSharedPreferences(SESSION_PREFS_NAME, MODE_PRIVATE);
        String loggedInUser = sessionManager.getString(KEY_LOGGED_IN_USER, null);

        if (loggedInUser == null) {
            finish();
            return;
        }

        userPreferences = getSharedPreferences("user_prefs_" + loggedInUser, MODE_PRIVATE);

        dailyTotalTV = findViewById(R.id.dailyTotalTV);
        goalTV = findViewById(R.id.goalTV);
        hydrationCircularProgress = findViewById(R.id.hydrationCircularProgress);
        hydrationGraph = findViewById(R.id.hydrationGraph);
        add250BTN = findViewById(R.id.add250BTN);
        add500BTN = findViewById(R.id.add500BTN);
        customBTN = findViewById(R.id.customBTN);

        setupGraph();
        loadHydrationData();

        add250BTN.setOnClickListener(v -> addWater(250));
        add500BTN.setOnClickListener(v -> addWater(500));
        customBTN.setOnClickListener(v -> showCustomAddDialog());
    }

    private void setupGraph() {
        hydrationGraph.getViewport().setXAxisBoundsManual(true);
        hydrationGraph.getGridLabelRenderer().setLabelFormatter(new DateAsXAxisLabelFormatter(this));
        hydrationGraph.getGridLabelRenderer().setNumHorizontalLabels(4);
    }

    private void loadHydrationData() {
        String today = sdf.format(Calendar.getInstance().getTime());
        Set<String> entries = userPreferences.getStringSet(HYDRATION_ENTRIES_KEY_PREFIX + today, new HashSet<>());
        List<DataPoint> dataPoints = new ArrayList<>();
        currentHydration = 0;

        List<String> sortedEntries = new ArrayList<>(entries);
        Collections.sort(sortedEntries, Comparator.comparingLong(entry -> Long.parseLong(entry.split(":")[0])));

        int cumulativeAmount = 0;
        for (String entry : sortedEntries) {
            try {
                long timestamp = Long.parseLong(entry.split(":")[0]);
                int amount = Integer.parseInt(entry.split(":")[1]);
                cumulativeAmount += amount;
                dataPoints.add(new DataPoint(new Date(timestamp), cumulativeAmount));
            } catch (Exception e) {
                // Ignore malformed entries
            }
        }
        currentHydration = cumulativeAmount;

        if (!dataPoints.isEmpty()) {
            LineGraphSeries<DataPoint> series = new LineGraphSeries<>(dataPoints.toArray(new DataPoint[0]));
            series.setDrawDataPoints(true);
            series.setDataPointsRadius(8);
            hydrationGraph.addSeries(series);

            hydrationGraph.getViewport().setMinX(dataPoints.get(0).getX());
            hydrationGraph.getViewport().setMaxX(dataPoints.get(dataPoints.size() - 1).getX());
        } else {
            hydrationGraph.removeAllSeries();
        }

        saveTotalHydration();
        updateUI();
    }

    private void saveHydrationEntry(int amount) {
        String today = sdf.format(Calendar.getInstance().getTime());
        String entry = System.currentTimeMillis() + ":" + amount;
        Set<String> entries = userPreferences.getStringSet(HYDRATION_ENTRIES_KEY_PREFIX + today, new HashSet<>());
        entries.add(entry);

        SharedPreferences.Editor editor = userPreferences.edit();
        editor.putStringSet(HYDRATION_ENTRIES_KEY_PREFIX + today, entries);
        editor.apply();
    }

    private void saveTotalHydration() {
        String today = sdf.format(Calendar.getInstance().getTime());
        SharedPreferences.Editor editor = userPreferences.edit();
        editor.putInt("hydration_" + today, currentHydration);
        editor.apply();
    }

    private void addWater(int amount) {
        currentHydration += amount;
        saveHydrationEntry(amount);
        loadHydrationData(); // Reload all data to update chart correctly
        Toast.makeText(this, amount + " ml added", Toast.LENGTH_SHORT).show();
    }

    private void updateUI() {
        dailyTotalTV.setText(String.format(Locale.getDefault(), "%d ml", currentHydration));
        goalTV.setText(String.format(Locale.getDefault(), "of %d ml", hydrationGoal));
        int progress = (int) ((currentHydration * 100.0f) / hydrationGoal);
        hydrationCircularProgress.setProgress(progress, true);
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
