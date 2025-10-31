package com.example.healthtrack;

import static com.example.healthtrack.MainActivity.KEY_LOGGED_IN_USER;
import static com.example.healthtrack.MainActivity.SESSION_PREFS_NAME;

import android.app.AlertDialog;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.InputType;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class HydrationActivity extends AppCompatActivity {

    private TextView dailyTotalTV;
    private Button add250BTN, add500BTN, customBTN;
    private SharedPreferences userPreferences;
    private int currentHydration = 0;
    private final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());

    // Additional functionality from d_suma
    private SharedPreferences sharedPreferences;
    private int dailyTotal;
    private final int hydrationGoal = 2000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_hydration);

        // Session check
        SharedPreferences sessionManager = getSharedPreferences(SESSION_PREFS_NAME, MODE_PRIVATE);
        String loggedInUser = sessionManager.getString(KEY_LOGGED_IN_USER, null);

        if (loggedInUser == null) {
            finish();
            return;
        }

        userPreferences = getSharedPreferences("user_prefs_" + loggedInUser, MODE_PRIVATE);
        sharedPreferences = getSharedPreferences("HydrationPrefs", MODE_PRIVATE);

        // Window insets
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.hydration), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Views
        dailyTotalTV = findViewById(R.id.dailyTotalTV);
        add250BTN = findViewById(R.id.add250BTN);
        add500BTN = findViewById(R.id.add500BTN);
        customBTN = findViewById(R.id.customBTN);

        // Load existing hydration
        loadHydrationData();
        dailyTotal = sharedPreferences.getInt("dailyTotal", 0);
        updateUI();

        // Button listeners
        add250BTN.setOnClickListener(v -> addWater(250));
        add500BTN.setOnClickListener(v -> addWater(500));
        customBTN.setOnClickListener(v -> showCustomAddDialog());
    }

    private void loadHydrationData() {
        String today = sdf.format(Calendar.getInstance().getTime());
        currentHydration = userPreferences.getInt("hydration_" + today, 0);
    }

    private void saveHydrationData() {
        String today = sdf.format(Calendar.getInstance().getTime());
        SharedPreferences.Editor editor = userPreferences.edit();
        editor.putInt("hydration_" + today, currentHydration);
        editor.apply();
    }

    private void addWater(int amount) {
        // Update both original and d_suma tracking
        currentHydration += amount;
        saveHydrationData();

        dailyTotal += amount;
        sharedPreferences.edit().putInt("dailyTotal", dailyTotal).apply();

        updateUI();
        Toast.makeText(this, amount + " ml added", Toast.LENGTH_SHORT).show();
    }

    private void updateUI() {
        dailyTotalTV.setText("Total: " + dailyTotal + " ml / " + hydrationGoal + " ml");
    }

    private void showCustomAddDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Add Custom Amount");

        final EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_CLASS_NUMBER);
        builder.setView(input);

        builder.setPositiveButton("Add", (dialog, which) -> {
            String amountStr = input.getText().toString();
            if (!amountStr.isEmpty()) {
                int amount = Integer.parseInt(amountStr);
                addWater(amount);
            } else {
                Toast.makeText(this, "Please enter an amount", Toast.LENGTH_SHORT).show();
            }
        });
        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());

        builder.show();
    }
}
