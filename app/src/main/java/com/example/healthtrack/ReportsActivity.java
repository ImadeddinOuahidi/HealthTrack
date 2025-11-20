package com.example.healthtrack;

import static com.example.healthtrack.MainActivity.KEY_LOGGED_IN_USER;
import static com.example.healthtrack.MainActivity.SESSION_PREFS_NAME;

import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.GridLabelRenderer;
import com.jjoe64.graphview.helper.DateAsXAxisLabelFormatter;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ReportsActivity extends AppCompatActivity {

    private SharedPreferences userPreferences;
    private final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reports);

        SharedPreferences sessionManager = getSharedPreferences(SESSION_PREFS_NAME, MODE_PRIVATE);
        String loggedInUser = sessionManager.getString(KEY_LOGGED_IN_USER, null);

        if (loggedInUser == null) {
            finish();
            return;
        }

        userPreferences = getSharedPreferences("user_prefs_" + loggedInUser, MODE_PRIVATE);

        loadHydrationReport();
        loadSleepReport();
        loadStepsReport();
    }

    private void loadHydrationReport() {
        GraphView graph = findViewById(R.id.hydrationReportGraph);
        TextView averageTV = findViewById(R.id.hydrationAverageTV);
        List<DataPoint> dataPoints = new ArrayList<>();
        Calendar cal = Calendar.getInstance();
        float total = 0;
        int count = 0;

        for (int i = 0; i < 30; i++) {
            cal.setTime(new Date());
            cal.add(Calendar.DAY_OF_YEAR, -i);
            String dateKey = sdf.format(cal.getTime());
            int hydration = userPreferences.getInt("hydration_" + dateKey, 0);
            if (hydration > 0) {
                dataPoints.add(new DataPoint(cal.getTime(), hydration));
                total += hydration;
                count++;
            }
        }

        if (count > 0) {
            averageTV.setText(String.format(Locale.getDefault(), "Average: %.0f ml/day", total / count));
        }

        setupGraph(graph, dataPoints, "Hydration");
    }

    private void loadSleepReport() {
        GraphView graph = findViewById(R.id.sleepReportGraph);
        TextView averageTV = findViewById(R.id.sleepAverageTV);
        List<DataPoint> dataPoints = new ArrayList<>();
        Calendar cal = Calendar.getInstance();
        float total = 0;
        int count = 0;

        for (int i = 0; i < 30; i++) {
            cal.setTime(new Date());
            cal.add(Calendar.DAY_OF_YEAR, -i);
            String dateKey = sdf.format(cal.getTime());
            float sleep = userPreferences.getFloat("sleep_" + dateKey, 0f);
            if (sleep > 0) {
                dataPoints.add(new DataPoint(cal.getTime(), sleep));
                total += sleep;
                count++;
            }
        }

        if (count > 0) {
            averageTV.setText(String.format(Locale.getDefault(), "Average: %.1f hrs/night", total / count));
        }

        setupGraph(graph, dataPoints, "Sleep");
    }

    private void loadStepsReport() {
        GraphView graph = findViewById(R.id.stepsReportGraph);
        TextView averageTV = findViewById(R.id.stepsAverageTV);
        List<DataPoint> dataPoints = new ArrayList<>();
        Calendar cal = Calendar.getInstance();
        float total = 0;
        int count = 0;

        for (int i = 0; i < 30; i++) {
            cal.setTime(new Date());
            cal.add(Calendar.DAY_OF_YEAR, -i);
            String dateKey = sdf.format(cal.getTime());
            int steps = userPreferences.getInt("steps_" + dateKey, 0);
            if (steps > 0) {
                dataPoints.add(new DataPoint(cal.getTime(), steps));
                total += steps;
                count++;
            }
        }

        if (count > 0) {
            averageTV.setText(String.format(Locale.getDefault(), "Average: %.0f steps/day", total / count));
        }

        setupGraph(graph, dataPoints, "Steps");
    }

    private void setupGraph(GraphView graph, List<DataPoint> dataPoints, String title) {
        if (dataPoints.isEmpty()) {
            graph.setVisibility(GraphView.GONE);
            return;
        }

        Collections.sort(dataPoints, (dp1, dp2) -> Double.compare(dp1.getX(), dp2.getX()));

        LineGraphSeries<DataPoint> series = new LineGraphSeries<>(dataPoints.toArray(new DataPoint[0]));
        series.setColor(Color.parseColor("#2196F3"));
        series.setDrawDataPoints(true);
        series.setDataPointsRadius(6);
        series.setThickness(4);

        graph.removeAllSeries();
        graph.addSeries(series);

        // Formatting
        graph.getGridLabelRenderer().setLabelFormatter(new DateAsXAxisLabelFormatter(this, new SimpleDateFormat("MMM dd", Locale.getDefault())));
        graph.getGridLabelRenderer().setGridStyle(GridLabelRenderer.GridStyle.NONE);
        graph.getViewport().setXAxisBoundsManual(true);
        graph.getViewport().setMinX(dataPoints.get(0).getX());
        graph.getViewport().setMaxX(dataPoints.get(dataPoints.size() - 1).getX());
        graph.getGridLabelRenderer().setHumanRounding(false);
    }
}
