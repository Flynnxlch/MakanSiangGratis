package com.example.makansianggratis.Activity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.example.makansianggratis.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class LoginActivity extends AppCompatActivity {
    Button Login, Test;
    EditText Input, Password;
    TextView Register;
    ProgressBar progressBar;

    private FirebaseAuth auth;
    private DatabaseReference database;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_login);

        // Initialize FirebaseAuth and DatabaseReference
        auth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance().getReference("Users");

        // Initialize views
        progressBar = findViewById(R.id.prog_bar);
        progressBar.setVisibility(View.GONE);

        Login = findViewById(R.id.login_btn);
        Input = findViewById(R.id.login_input); // Accepts email or username
        Password = findViewById(R.id.log_pass);
        Register = findViewById(R.id.log_daftar);

        // Navigate to RegisterActivity
        Register.setOnClickListener(v -> startActivity(new Intent(LoginActivity.this, RegisterActivity.class)));

        // Login button click handler
        Login.setOnClickListener(v -> {
            progressBar.setVisibility(View.VISIBLE);
            logUser();
        });
    }

    private void logUser() {
        String userInput = Input.getText().toString().trim(); // Can be email or username
        String mahaPass = Password.getText().toString().trim();

        // Validate input fields
        if (TextUtils.isEmpty(userInput)) {
            Toast.makeText(this, "Masukkan Email atau Username", Toast.LENGTH_SHORT).show();
            progressBar.setVisibility(View.GONE);
            return;
        }
        if (TextUtils.isEmpty(mahaPass)) {
            Toast.makeText(this, "Password Kosong Nih", Toast.LENGTH_SHORT).show();
            progressBar.setVisibility(View.GONE);
            return;
        }
        if (mahaPass.length() < 6) {
            Toast.makeText(this, "Password Harus lebih dari 6 kata", Toast.LENGTH_SHORT).show();
            progressBar.setVisibility(View.GONE);
            return;
        }

        // Determine if the input is an email or username
        if (userInput.contains("@")) {
            // Input is an email
            authenticateUser(userInput, mahaPass);
        } else {
            // Input is a username; search for the corresponding email in the database
            findEmailFromUsername(userInput, mahaPass);
        }
    }

    private void authenticateUser(String email, String password) {
        // Authenticate using FirebaseAuth
        auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    progressBar.setVisibility(View.GONE);
                    if (task.isSuccessful()) {
                        FirebaseUser firebaseUser = auth.getCurrentUser();
                        if (firebaseUser != null) {
                            String userId = firebaseUser.getUid();
                            fetchUserDetails(userId);
                        }
                    } else {
                        Toast.makeText(LoginActivity.this, "Login Gagal: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                    }
                });
    }

    private void findEmailFromUsername(String username, String password) {
        // Query the database to find the email corresponding to the username
        database.orderByChild("username").equalTo(username)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if (dataSnapshot.exists()) {
                            // Retrieve the email for the username
                            for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                                String email = snapshot.child("email").getValue(String.class);
                                if (email != null) {
                                    // Authenticate using the retrieved email
                                    authenticateUser(email, password);
                                    return;
                                }
                            }
                        } else {
                            progressBar.setVisibility(View.GONE);
                            Toast.makeText(LoginActivity.this, "Username tidak ditemukan", Toast.LENGTH_LONG).show();
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        progressBar.setVisibility(View.GONE);
                        Toast.makeText(LoginActivity.this, "Error: " + databaseError.getMessage(), Toast.LENGTH_LONG).show();
                    }
                });
    }

    private void fetchUserDetails(String userId) {
        // Fetch user details from the Firebase Realtime Database
        database.child(userId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    String username = dataSnapshot.child("username").getValue(String.class);
                    String email = dataSnapshot.child("email").getValue(String.class);

                    Toast.makeText(LoginActivity.this, "Welcome, " + username + "!", Toast.LENGTH_SHORT).show();

                    // Navigate to the main activity
                    Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                    intent.putExtra("username", username);
                    intent.putExtra("email", email);
                    startActivity(intent);
                    finish();
                } else {
                    Toast.makeText(LoginActivity.this, "Data pengguna tidak ditemukan di database", Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(LoginActivity.this, "Failed to fetch user details: " + databaseError.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }
}
