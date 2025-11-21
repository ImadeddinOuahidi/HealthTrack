package com.example.healthtrack;

import android.os.Bundle;
import android.os.CountDownTimer;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.chip.ChipGroup;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.progressindicator.CircularProgressIndicator;

import java.util.Locale;

public class RelaxationActivity extends AppCompatActivity {

    private long initialTimeInMillis = 25 * 60 * 1000;

    private TextView txtTimer;
    private FloatingActionButton fabPlayPause;
    private Button btnReset;
    private CircularProgressIndicator timerProgress;
    private ChipGroup durationChips;

    private CountDownTimer countDownTimer;
    private boolean isTimerRunning;
    private long timeLeftInMillis;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_relaxation);

        txtTimer = findViewById(R.id.txtTimer);
        fabPlayPause = findViewById(R.id.fabPlayPause);
        btnReset = findViewById(R.id.btnReset);
        timerProgress = findViewById(R.id.timerProgress);
        durationChips = findViewById(R.id.durationChips);

        timeLeftInMillis = initialTimeInMillis;

        setupListeners();
        updateTimerText();
    }

    private void setupListeners() {
        fabPlayPause.setOnClickListener(v -> {
            if (isTimerRunning) {
                pauseTimer();
            } else {
                startTimer();
            }
        });

        btnReset.setOnClickListener(v -> resetTimer());

        durationChips.setOnCheckedChangeListener((group, checkedId) -> {
            if (isTimerRunning) {
                // Prevent changing duration while timer is running, but still doesnt work need to change it
                return;
            }
            handleDurationChange(checkedId);
        });
    }

    private void handleDurationChange(int checkedId) {
        if (checkedId == R.id.chip15min) {
            initialTimeInMillis = 15 * 60 * 1000;
        } else if (checkedId == R.id.chip25min) {
            initialTimeInMillis = 25 * 60 * 1000;
        } else if (checkedId == R.id.chip45min) {
            initialTimeInMillis = 45 * 60 * 1000;
        }
        resetTimer();
    }

    private void startTimer() {
        countDownTimer = new CountDownTimer(timeLeftInMillis, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                timeLeftInMillis = millisUntilFinished;
                updateTimerText();
                updateProgressBar();
            }

            @Override
            public void onFinish() {
                isTimerRunning = false;
                fabPlayPause.setImageResource(R.drawable.ic_play);
            }
        }.start();

        isTimerRunning = true;
        fabPlayPause.setImageResource(R.drawable.ic_pause);
    }

    private void pauseTimer() {
        countDownTimer.cancel();
        isTimerRunning = false;
        fabPlayPause.setImageResource(R.drawable.ic_play);
    }

    private void resetTimer() {
        if (isTimerRunning) {
            pauseTimer();
        }
        timeLeftInMillis = initialTimeInMillis;
        updateTimerText();
        updateProgressBar();
    }

    private void updateTimerText() {
        int minutes = (int) (timeLeftInMillis / 1000) / 60;
        int seconds = (int) (timeLeftInMillis / 1000) % 60;
        String timeFormatted = String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds);
        txtTimer.setText(timeFormatted);
    }

    private void updateProgressBar() {
        int progress = (int) (100 * timeLeftInMillis / initialTimeInMillis);
        timerProgress.setProgress(progress, true);
    }
}
