package com.example.healthtrack;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.widget.Button;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

/**
 * RelaxationActivity handles focus and break timer cycles.
 * Tracks hydration breaks and displays motivational tips.
 *
 * Author: Suma Bandaru
 * Date: November 2025
 */
public class RelaxationActivity extends AppCompatActivity {

    private TextView txtTimer, tvTip;
    private Button btnStart, btnStop, btnReset;
    private CountDownTimer countDownTimer;
    private boolean isRunning = false;

    private long timeLeftInMillis = 25 * 60 * 1000;

    private final long focusDuration = 25 * 60 * 1000;
    private final long breakDuration = 5 * 60 * 1000;

    private boolean isFocus = true;
    private SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_relaxation);

        txtTimer = findViewById(R.id.txtTimer);
        tvTip = findViewById(R.id.tvTip);
        btnStart = findViewById(R.id.btnStart);
        btnStop = findViewById(R.id.btnStop);
        btnReset = findViewById(R.id.btnReset);

        sharedPreferences = getSharedPreferences("HydrationPrefs", MODE_PRIVATE);

        updateTimerText();
        tvTip.setText("Tip: Take deep breaths during breaks.");

        btnStart.setOnClickListener(v -> startTimer());
        btnStop.setOnClickListener(v -> stopTimer());
        btnReset.setOnClickListener(v -> resetTimer());
    }

    private void startTimer() {
        if (!isRunning) {
            countDownTimer = new CountDownTimer(timeLeftInMillis, 1000) {
                @Override
                public void onTick(long millisUntilFinished) {
                    timeLeftInMillis = millisUntilFinished;
                    updateTimerText();
                }

                @Override
                public void onFinish() {
                    if (isFocus) {
                        isFocus = false;
                        timeLeftInMillis = breakDuration;
                        tvTip.setText("Take a water break!");
                        int hydrationBreak = sharedPreferences.getInt("hydrationBreak", 0);
                        hydrationBreak++;
                        sharedPreferences.edit().putInt("hydrationBreak", hydrationBreak).apply();
                        startTimer();
                    } else {
                        isFocus = true;
                        timeLeftInMillis = focusDuration;
                        tvTip.setText("Focus session started.");
                        startTimer();
                    }
                }
            }.start();
            isRunning = true;
        }
    }

    private void stopTimer() {
        if (countDownTimer != null) countDownTimer.cancel();
        isRunning = false;
    }

    private void resetTimer() {
        stopTimer();
        timeLeftInMillis = focusDuration;
        isFocus = true;
        updateTimerText();
        tvTip.setText("Tip: Take deep breaths during breaks.");
    }

    private void updateTimerText() {
        int minutes = (int) (timeLeftInMillis / 1000) / 60;
        int seconds = (int) (timeLeftInMillis / 1000) % 60;
        txtTimer.setText(String.format("%02d:%02d", minutes, seconds));
    }
}
