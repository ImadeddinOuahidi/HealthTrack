package com.example.healthtrack;

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
        setContentView(R.layout.activity_sleep);

        edtBedtime = findViewById(R.id.edtBedtime);
        edtWakeup = findViewById(R.id.edtWakeup);
        spinnerSleepQuality = findViewById(R.id.spinnerSleepQuality);
        btnSaveSleep = findViewById(R.id.btnSaveSleep);


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
