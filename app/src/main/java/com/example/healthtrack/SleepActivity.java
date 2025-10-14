package com.example.healthtrack;  // <-- replace with your actual package name

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import androidx.appcompat.app.AppCompatActivity;

public class SleepActivity extends AppCompatActivity {

    private EditText edtBedtime, edtWakeup;
    private Spinner spinnerSleepQuality;
    private Button btnSaveSleep;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sleep); // links to activity_sleep.xml

        // Initialize views
        edtBedtime = findViewById(R.id.edtBedtime);
        edtWakeup = findViewById(R.id.edtWakeup);
        spinnerSleepQuality = findViewById(R.id.spinnerSleepQuality);
        btnSaveSleep = findViewById(R.id.btnSaveSleep);

        // Example: simple click action
        btnSaveSleep.setOnClickListener(v -> {
            String bedtime = edtBedtime.getText().toString();
            String wakeup = edtWakeup.getText().toString();
            String quality = spinnerSleepQuality.getSelectedItem() != null
                    ? spinnerSleepQuality.getSelectedItem().toString()
                    : "Not selected";

            if (bedtime.isEmpty() || wakeup.isEmpty()) {
                btnSaveSleep.setText("Enter all fields!");
            } else {
                btnSaveSleep.setText("Saved!");
            }
        });
    }
}
