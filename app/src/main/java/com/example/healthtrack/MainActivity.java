package com.example.healthtrack;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class MainActivity extends AppCompatActivity {

    private EditText usernameEditText, passwordEditText;
    private EditText ageEditText, weightEditText, heightEditText;
    private Button loginButton, registerButton;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        usernameEditText = findViewById(R.id.edtUsername);
        passwordEditText = findViewById(R.id.edtPassword);
        ageEditText = findViewById(R.id.edtAge);
        weightEditText = findViewById(R.id.edtWeight);
        heightEditText = findViewById(R.id.edtHeight);
        loginButton = findViewById(R.id.btnLogin);
        registerButton = findViewById(R.id.btnRegister);


    }

    public void handleLogin(View v) {
        String username = usernameEditText.getText().toString();
        String password = passwordEditText.getText().toString();

        if (username.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Please enter username and password", Toast.LENGTH_SHORT).show();
            return;
        }
        navigateToDashboard();
    }

    public void handleRegistration(View v) {
        String username = usernameEditText.getText().toString();
        String password = passwordEditText.getText().toString();
        String ageStr = ageEditText.getText().toString();
        String weightStr = weightEditText.getText().toString();
        String heightStr = heightEditText.getText().toString();

        Toast.makeText(this, "Registration successful! Welcome, " + username + "!", Toast.LENGTH_LONG).show();
        navigateToDashboard();

    }

    private void navigateToDashboard() {
        Intent intent = new Intent(MainActivity.this, DashboardActivity.class);
        startActivity(intent);
        finish();
    }
}