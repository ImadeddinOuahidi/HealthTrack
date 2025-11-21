package com.example.healthtrack;

import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
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
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.lang.ref.WeakReference;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

public class ReportsActivity extends AppCompatActivity {

    private DatabaseReference mDatabase;
    private FirebaseUser currentUser;

    private LineChart hydrationChart, sleepChart, stepsChart;
    private TextView hydrationAverageTV, sleepAverageTV, stepsAverageTV;
    private TextView noHydrationDataTV, noSleepDataTV, noStepsDataTV;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reports);

        currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) {
            finish();
            return;
        }

        hydrationChart = findViewById(R.id.hydrationReportGraph);
        sleepChart = findViewById(R.id.sleepReportGraph);
        stepsChart = findViewById(R.id.stepsReportGraph);

        hydrationAverageTV = findViewById(R.id.hydrationAverageTV);
        sleepAverageTV = findViewById(R.id.sleepAverageTV);
        stepsAverageTV = findViewById(R.id.stepsAverageTV);

        noHydrationDataTV = findViewById(R.id.noHydrationDataTV);
        noSleepDataTV = findViewById(R.id.noSleepDataTV);
        noStepsDataTV = findViewById(R.id.noStepsDataTV);

        mDatabase = FirebaseDatabase.getInstance().getReference().child("users").child(currentUser.getUid()).child("daily_logs");
        loadReports();
    }

    private void loadReports() {
        mDatabase.limitToLast(30).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                new LoadReportsTask(ReportsActivity.this).execute(snapshot);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(ReportsActivity.this, "Failed to load reports.", Toast.LENGTH_SHORT).show();
                handleNoDataForAllGraphs(true);
            }
        });
    }

    private static class LoadReportsTask extends AsyncTask<DataSnapshot, Void, ReportData> {
        private final WeakReference<ReportsActivity> activityReference;

        LoadReportsTask(ReportsActivity context) {
            activityReference = new WeakReference<>(context);
        }

        @Override
        protected ReportData doInBackground(DataSnapshot... snapshots) {
            DataSnapshot snapshot = snapshots[0];
            ReportData reportData = new ReportData();
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            long referenceTimestamp = 0;

            if (snapshot.exists()) {
                List<Entry> hydrationEntries = new ArrayList<>();
                List<Entry> sleepEntries = new ArrayList<>();
                List<Entry> stepsEntries = new ArrayList<>();
                final List<String> xLabels = new ArrayList<>();
                int i = 0;

                for (DataSnapshot daySnapshot : snapshot.getChildren()) {
                    try {
                        Date date = sdf.parse(daySnapshot.getKey());
                        if (i == 0) {
                            referenceTimestamp = date.getTime();
                        }
                        long daysBetween = TimeUnit.MILLISECONDS.toDays(date.getTime() - referenceTimestamp);

                        Integer hydration = daySnapshot.child("hydration").getValue(Integer.class);
                        if (hydration != null) {
                            hydrationEntries.add(new Entry(daysBetween, hydration));
                            reportData.totalHydration += hydration;
                            reportData.hydrationCount++;
                        }
                        Float sleep = daySnapshot.child("sleep").getValue(Float.class);
                        if (sleep != null) {
                            sleepEntries.add(new Entry(daysBetween, sleep));
                            reportData.totalSleep += sleep;
                            reportData.sleepCount++;
                        }
                        Integer steps = daySnapshot.child("steps").getValue(Integer.class);
                        if (steps != null) {
                            stepsEntries.add(new Entry(daysBetween, steps));
                            reportData.totalSteps += steps;
                            reportData.stepsCount++;
                        }
                        xLabels.add(daySnapshot.getKey().substring(5));
                        i++;
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                }

                reportData.hydrationDataSet = new LineDataSet(hydrationEntries, "Hydration");
                reportData.sleepDataSet = new LineDataSet(sleepEntries, "Sleep");
                reportData.stepsDataSet = new LineDataSet(stepsEntries, "Steps");
                reportData.xLabels = xLabels;
            }
            return reportData;
        }

        @Override
        protected void onPostExecute(ReportData reportData) {
            ReportsActivity activity = activityReference.get();
            if (activity == null || activity.isFinishing()) return;

            activity.updateGraph(activity.hydrationChart, activity.noHydrationDataTV, reportData.hydrationDataSet, reportData.xLabels);
            activity.updateGraph(activity.sleepChart, activity.noSleepDataTV, reportData.sleepDataSet, reportData.xLabels);
            activity.updateGraph(activity.stepsChart, activity.noStepsDataTV, reportData.stepsDataSet, reportData.xLabels);

            if (reportData.hydrationCount > 0) activity.hydrationAverageTV.setText(String.format(Locale.getDefault(), "Average: %.0f ml/day", reportData.totalHydration / reportData.hydrationCount));
            if (reportData.sleepCount > 0) activity.sleepAverageTV.setText(String.format(Locale.getDefault(), "Average: %.1f hrs/night", reportData.totalSleep / reportData.sleepCount));
            if (reportData.stepsCount > 0) activity.stepsAverageTV.setText(String.format(Locale.getDefault(), "Average: %.0f steps/day", reportData.totalSteps / reportData.stepsCount));

            activity.handleNoDataForAllGraphs(reportData.hydrationDataSet.getEntryCount() == 0 && reportData.sleepDataSet.getEntryCount() == 0 && reportData.stepsDataSet.getEntryCount() == 0);
        }
    }

    private void updateGraph(LineChart chart, TextView noDataTV, LineDataSet dataSet, final List<String> xLabels) {
        if (dataSet == null || dataSet.getEntryCount() == 0) {
            chart.setVisibility(View.GONE);
            noDataTV.setVisibility(View.VISIBLE);
            return;
        }
        chart.setVisibility(View.VISIBLE);
        noDataTV.setVisibility(View.GONE);

        dataSet.setColor(Color.parseColor("#2196F3"));
        dataSet.setCircleColor(Color.parseColor("#2196F3"));
        dataSet.setCircleRadius(4f);
        dataSet.setDrawValues(false);

        LineData lineData = new LineData(dataSet);
        chart.setData(lineData);

        XAxis xAxis = chart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setGranularity(1f);
        xAxis.setValueFormatter(new ValueFormatter() {
            @Override
            public String getFormattedValue(float value) {
                int index = (int) value;
                if (index >= 0 && index < xLabels.size()) {
                    return xLabels.get(index);
                }
                return "";
            }
        });

        chart.getAxisRight().setEnabled(false);
        chart.getDescription().setEnabled(false);
        chart.invalidate();
    }

    private void handleNoDataForAllGraphs(boolean noData) {
        if (noData) {
            hydrationChart.setVisibility(View.GONE);
            noHydrationDataTV.setVisibility(View.VISIBLE);
            sleepChart.setVisibility(View.GONE);
            noSleepDataTV.setVisibility(View.VISIBLE);
            stepsChart.setVisibility(View.GONE);
            noStepsDataTV.setVisibility(View.VISIBLE);
        }
    }

    private static class ReportData {
        LineDataSet hydrationDataSet, sleepDataSet, stepsDataSet;
        List<String> xLabels;
        float totalHydration = 0, totalSleep = 0, totalSteps = 0;
        int hydrationCount = 0, sleepCount = 0, stepsCount = 0;
    }
}
