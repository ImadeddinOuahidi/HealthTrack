package com.example.healthtrack;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class MainActivity extends AppCompatActivity {

    private EditText usernameEditText, passwordEditText;
    private EditText usernameRegEditText, passwordRegEditText, ageEditText, weightEditText, heightEditText;
    private Button loginButton, registerButton;
    private LinearLayout loginForm, registerForm;
    private SharedPreferences sessionManager;

    public static final String SESSION_PREFS_NAME = "session_prefs";
    public static final String KEY_LOGGED_IN_USER = "logged_in_user";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        sessionManager = getSharedPreferences(SESSION_PREFS_NAME, MODE_PRIVATE);

        // if already logged in
        if (sessionManager.contains(KEY_LOGGED_IN_USER)) {
            navigateToDashboard();
            return;
        }

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        usernameEditText = findViewById(R.id.edtUsername);
        passwordEditText = findViewById(R.id.edtPassword);
        usernameRegEditText = findViewById(R.id.edtUsernameReg);
        passwordRegEditText = findViewById(R.id.edtPasswordReg);
        ageEditText = findViewById(R.id.edtAge);
        weightEditText = findViewById(R.id.edtWeight);
        heightEditText = findViewById(R.id.edtHeight);
        loginButton = findViewById(R.id.btnLogin);
        registerButton = findViewById(R.id.btnRegister);
        loginForm = findViewById(R.id.login_form);
        registerForm = findViewById(R.id.register_form);
    }

    public void showLogin(View view) {
        loginForm.setVisibility(View.VISIBLE);
        registerForm.setVisibility(View.GONE);
    }

    public void showRegister(View view) {
        loginForm.setVisibility(View.GONE);
        registerForm.setVisibility(View.VISIBLE);
    }

    public void handleLogin(View v) {
        String username = usernameEditText.getText().toString().trim();
        String password = passwordEditText.getText().toString();

        if (username.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Please enter username and password", Toast.LENGTH_SHORT).show();
            return;
        }

        if (password.length() < 6) {
            Toast.makeText(this, "Password must be at least 6 characters long", Toast.LENGTH_SHORT).show();
            return;
        }

        // Stubbed validation (simple, local check)
        SharedPreferences userPrefs = getSharedPreferences("user_prefs_" + username, MODE_PRIVATE);
        String registeredPassword = userPrefs.getString("password", null);

        if (registeredPassword != null && registeredPassword.equals(password)) {
            sessionManager.edit().putString(KEY_LOGGED_IN_USER, username).apply();
            navigateToDashboard();
        } else {
            Toast.makeText(this, "Invalid username or password", Toast.LENGTH_SHORT).show();
        }
    }

    public void handleRegistration(View v) {
        String username = usernameRegEditText.getText().toString().trim();
        String password = passwordRegEditText.getText().toString();
        String ageStr = ageEditText.getText().toString();
        String weightStr = weightEditText.getText().toString();
        String heightStr = heightEditText.getText().toString();

        // validation
        if (username.isEmpty() || password.isEmpty() || ageStr.isEmpty() ||
                weightStr.isEmpty() || heightStr.isEmpty()) {
            Toast.makeText(this, "Please fill all the fields", Toast.LENGTH_SHORT).show();
            return;
        }

        if (password.length() < 6) {
            Toast.makeText(this, "Password must be at least 6 characters long", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            int age = Integer.parseInt(ageStr);
            if (age <= 0) {
                Toast.makeText(this, "Please enter a valid positive age", Toast.LENGTH_SHORT).show();
                return;
            }
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Age must be a number", Toast.LENGTH_SHORT).show();
            return;
        }

        // check if user exists
        SharedPreferences userCheck = getSharedPreferences("user_prefs_" + username, MODE_PRIVATE);
        if (userCheck.contains("username")) {
            Toast.makeText(this, "Username already exists", Toast.LENGTH_SHORT).show();
            return;
        }

        // save new user data (stub)
        SharedPreferences newUserPrefs = getSharedPreferences("user_prefs_" + username, MODE_PRIVATE);
        SharedPreferences.Editor editor = newUserPrefs.edit();
        editor.putString("username", username);
        editor.putString("password", password);
        editor.putString("age", ageStr);
        editor.putString("weight", weightStr);
        editor.putString("height", heightStr);
        editor.apply();

        sessionManager.edit().putString(KEY_LOGGED_IN_USER, username).apply();

        Toast.makeText(this, "Registration successful! Welcome, " + username + "!", Toast.LENGTH_LONG).show();
        navigateToDashboard();
    }

    private void navigateToDashboard() {
        Intent intent = new Intent(MainActivity.this, DashboardActivity.class);
        startActivity(intent);
        finish();
    }
}
