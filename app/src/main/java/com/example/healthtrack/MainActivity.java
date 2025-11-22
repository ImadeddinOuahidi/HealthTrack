package com.example.healthtrack;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButtonToggleGroup;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseAuthWeakPasswordException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    private TextInputEditText usernameEditText, passwordEditText;
    private TextInputEditText usernameRegEditText, passwordRegEditText, ageEditText, weightEditText, heightEditText;
    private Button btnLogin, btnRegister;
    private LinearLayout loginForm, registerForm;
    private MaterialButtonToggleGroup toggleButtons;

    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference();

        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            navigateToDashboard();
            return;
        }

        usernameEditText = findViewById(R.id.edtUsername);
        passwordEditText = findViewById(R.id.edtPassword);
        usernameRegEditText = findViewById(R.id.edtUsernameReg);
        passwordRegEditText = findViewById(R.id.edtPasswordReg);
        ageEditText = findViewById(R.id.edtAge);
        weightEditText = findViewById(R.id.edtWeight);
        heightEditText = findViewById(R.id.edtHeight);
        btnLogin = findViewById(R.id.btnLogin);
        btnRegister = findViewById(R.id.btnRegister);
        loginForm = findViewById(R.id.login_form);
        registerForm = findViewById(R.id.register_form);
        toggleButtons = findViewById(R.id.toggle_buttons);

        setupToggleListener();

        btnLogin.setOnClickListener(this::handleLogin);
        btnRegister.setOnClickListener(this::handleRegistration);
    }

    private void setupToggleListener() {
        toggleButtons.addOnButtonCheckedListener((group, checkedId, isChecked) -> {
            if (isChecked) {
                if (checkedId == R.id.show_login_button) {
                    showLogin(null);
                } else if (checkedId == R.id.show_register_button) {
                    showRegister(null);
                }
            }
        });
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
        String email = usernameEditText.getText().toString().trim();
        String password = passwordEditText.getText().toString();

        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Please enter email and password", Toast.LENGTH_SHORT).show();
            return;
        }

        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        navigateToDashboard();
                    } else {
                        String errorMessage;
                        try {
                            throw task.getException();
                        } catch (FirebaseAuthInvalidCredentialsException e) {
                            errorMessage = "Incorrect password or email. Please try again.";
                        } catch (Exception e) {
                            errorMessage = "Login failed. Please try again later.";
                        }
                        Toast.makeText(MainActivity.this, errorMessage, Toast.LENGTH_LONG).show();
                    }
                });
    }

    public void handleRegistration(View v) {
        String email = usernameRegEditText.getText().toString().trim();
        String password = passwordRegEditText.getText().toString();
        final String username = email.split("@")[0];
        final String age = ageEditText.getText().toString();
        final String weight = weightEditText.getText().toString();
        final String height = heightEditText.getText().toString();

        if (email.isEmpty() || password.isEmpty() || age.isEmpty() || weight.isEmpty() || height.isEmpty()) {
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = mAuth.getCurrentUser();
                        if (user != null) {
                            writeNewUser(user.getUid(), username, email, age, weight, height);
                        }
                        navigateToDashboard();
                    } else {
                        String errorMessage;
                        try {
                            throw task.getException();
                        } catch (FirebaseAuthWeakPasswordException e) {
                            errorMessage = "Password must be at least 6 characters long.";
                        } catch (FirebaseAuthInvalidCredentialsException e) {
                            errorMessage = "Please enter a valid email address.";
                        } catch (FirebaseAuthUserCollisionException e) {
                            errorMessage = "This email address is already in use.";
                        } catch (Exception e) {
                            errorMessage = "Registration failed. Please try again later.";
                        }
                        Toast.makeText(MainActivity.this, errorMessage, Toast.LENGTH_LONG).show();
                    }
                });
    }

    private void writeNewUser(String userId, String username, String email, String age, String weight, String height) {
        Map<String, Object> profile = new HashMap<>();
        profile.put("username", username);
        profile.put("email", email);
        profile.put("age", age);
        profile.put("weight", weight);
        profile.put("height", height);

        mDatabase.child("users").child(userId).child("profile").setValue(profile);
    }

    private void navigateToDashboard() {
        Intent intent = new Intent(MainActivity.this, DashboardActivity.class);
        startActivity(intent);
        finish();
    }
}
