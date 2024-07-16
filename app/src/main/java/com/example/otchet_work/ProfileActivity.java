package com.example.otchet_work;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.otchet_work.Models.User;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class ProfileActivity extends AppCompatActivity {

    private EditText firstNameEditText, lastNameEditText, passwordEditText;
    private Button updateProfileButton;
    private BottomNavigationView bottomNavigationView;
    private TextView logoutTextView;

    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        initializeFirebase();
        initializeUI();
        setupBottomNavigationView();
        setupListeners();
        loadUserData();
    }

    private void initializeFirebase() {
        mAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            mDatabase = FirebaseDatabase.getInstance().getReference("users").child(currentUser.getUid());
        } else {
            // Handle situation where currentUser is null (e.g., redirect to login screen)
            showToast("User not authenticated");
            startActivity(new Intent(this, LoginActivity.class));
            finish();
        }
    }

    private void initializeUI() {
        firstNameEditText = findViewById(R.id.profile_first_name);
        lastNameEditText = findViewById(R.id.profile_last_name);
        passwordEditText = findViewById(R.id.profile_password);
        updateProfileButton = findViewById(R.id.profile_update_btn);
        bottomNavigationView = findViewById(R.id.navigation_bar_profile);
        logoutTextView = findViewById(R.id.logout_linK);
    }

    private void setupBottomNavigationView() {
        bottomNavigationView.setOnNavigationItemSelectedListener(this::onNavigationItemSelected);
        bottomNavigationView.setSelectedItemId(R.id.navigation_profile);
    }

    private void setupListeners() {
        updateProfileButton.setOnClickListener(v -> updateProfile());
        logoutTextView.setOnClickListener(v -> logout());
    }

    private boolean onNavigationItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.navigation_home) {
            startActivity(new Intent(this, MainActivity.class));
            return true;
        } else if (item.getItemId() == R.id.navigation_profile) {
            return true;
        }
        return false;
    }

    private void loadUserData() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            mDatabase.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (snapshot.exists()) {
                        User user = snapshot.getValue(User.class);
                        if (user != null) {
                            firstNameEditText.setText(user.getFirstName());
                            lastNameEditText.setText(user.getLastName());
                            passwordEditText.setText(user.getPassword());
                        }
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    showToast("Failed to load user data");
                }
            });
        }
    }

    private void updateProfile() {
        String firstName = firstNameEditText.getText().toString().trim();
        String lastName = lastNameEditText.getText().toString().trim();
        String newPassword = passwordEditText.getText().toString().trim();

        if (validateInput(firstName, lastName, newPassword)) {

            FirebaseUser currentUser = mAuth.getCurrentUser();
            if (currentUser != null) {
                String email = currentUser.getEmail();
                User updatedUser = new User(email, newPassword, firstName, lastName);

                // Update user data in Realtime Database
                updateUserDataInDatabase(updatedUser);

                // Update password in Firebase Authentication
                currentUser.updatePassword(newPassword)
                        .addOnSuccessListener(aVoid -> showToast("Password updated successfully"))
                        .addOnFailureListener(e -> showToast("Failed to update password"));
            } else {
                showToast("User not authenticated");
                // Handle situation where currentUser is null (e.g., redirect to login screen)
                startActivity(new Intent(this, LoginActivity.class));
                finish();
            }
        }
    }

    private boolean validateInput(String firstName, String lastName, String password) {
        if (firstName.isEmpty()) {
            firstNameEditText.setError("First name is required");
            firstNameEditText.requestFocus();
            return false;
        }
        if (lastName.isEmpty()) {
            lastNameEditText.setError("Last name is required");
            lastNameEditText.requestFocus();
            return false;
        }
        if (password.isEmpty()) {
            passwordEditText.setError("Password is required");
            passwordEditText.requestFocus();
            return false;
        }
        return true;
    }

    private void updateUserDataInDatabase(User updatedUser) {
        mDatabase.setValue(updatedUser).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                showToast("Profile updated successfully");
            } else {
                showToast("Error updating profile");
            }
        });
    }

    private void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    private void logout() {
        SharedPreferences sharedPreferences = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.remove("userID");
        editor.apply();
        FirebaseAuth.getInstance().signOut();

        Intent intent = new Intent(ProfileActivity.this, LoginActivity.class);
        startActivity(intent);
        finish();
    }
}