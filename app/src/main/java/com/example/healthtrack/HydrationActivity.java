package com.example.healthtrack;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class HydrationActivity extends AppCompatActivity {

    private TextView dailyTotalTV;
    private Button add250BTN, add500BTN, customBTN;
    private SharedPreferences sharedPreferences;
    private int dailyTotal;
    private final int hydrationGoal = 2000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_hydration);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.hydration), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        dailyTotalTV = findViewById(R.id.dailyTotalTV);
        add250BTN = findViewById(R.id.add250BTN);
        add500BTN = findViewById(R.id.add500BTN);
        customBTN = findViewById(R.id.customBTN);

        sharedPreferences = getSharedPreferences("HydrationPrefs", MODE_PRIVATE);
        dailyTotal = sharedPreferences.getInt("dailyTotal", 0);
        updateDailyTotal();

        add250BTN.setOnClickListener(v -> addWater(250));
        add500BTN.setOnClickListener(v -> addWater(500));
        customBTN.setOnClickListener(v -> {
            android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(this);
            final android.widget.EditText input = new android.widget.EditText(this);
            input.setInputType(android.text.InputType.TYPE_CLASS_NUMBER);
            builder.setTitle("Custom Water Intake (ml)")
                    .setView(input)
                    .setPositiveButton("Add", (dialog, which) -> {
                        String value = input.getText().toString();
                        if (!value.isEmpty()) addWater(Integer.parseInt(value));
                    })
                    .setNegativeButton("Cancel", (dialog, which) -> dialog.cancel())
                    .show();
        });
    }

    private void addWater(int amount) {
        dailyTotal += amount;
        sharedPreferences.edit().putInt("dailyTotal", dailyTotal).apply();
        updateDailyTotal();
        Toast.makeText(this, amount + " ml added", Toast.LENGTH_SHORT).show();
    }

    private void updateDailyTotal() {
        dailyTotalTV.setText("Total: " + dailyTotal + " ml / " + hydrationGoal + " ml");
    }
}
