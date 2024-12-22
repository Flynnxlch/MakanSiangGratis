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
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class RegisterActivity extends AppCompatActivity {
    Button Register;
    EditText Nama, Email, Password;
    TextView Login;
    ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_register);

        // Initialize views
        progressBar = findViewById(R.id.prog_bar);
        progressBar.setVisibility(View.GONE);

        Register = findViewById(R.id.regis_btn);
        Nama = findViewById(R.id.sign_name);
        Email = findViewById(R.id.sign_email);
        Password = findViewById(R.id.sign_pass);
        Login = findViewById(R.id.sign_regis);

        // Navigate to LoginActivity
        Login.setOnClickListener(v -> startActivity(new Intent(RegisterActivity.this, LoginActivity.class)));

        // Handle registration
        Register.setOnClickListener(v -> {
            createUser();
            progressBar.setVisibility(View.VISIBLE);
        });
    }

    private void createUser() {
        String username = Nama.getText().toString().trim();
        String useremail = Email.getText().toString().trim();
        String userpassword = Password.getText().toString().trim();

        // Validate inputs
        if (TextUtils.isEmpty(username)) {
            Toast.makeText(this, "Nama Masih Kosong", Toast.LENGTH_SHORT).show();
            return;
        }
        if (TextUtils.isEmpty(useremail)) {
            Toast.makeText(this, "Email Masih Kosong", Toast.LENGTH_SHORT).show();
            return;
        }
        if (TextUtils.isEmpty(userpassword)) {
            Toast.makeText(this, "Password Nihil", Toast.LENGTH_SHORT).show();
            return;
        }
        if (userpassword.length() < 6) {
            Toast.makeText(this, "Password Harus lebih dari 6 kata", Toast.LENGTH_SHORT).show();
            return;
        }

        progressBar.setVisibility(View.VISIBLE);

        // Initialize FirebaseAuth
        FirebaseAuth auth = FirebaseAuth.getInstance();
        auth.createUserWithEmailAndPassword(useremail, userpassword)
                .addOnCompleteListener(task -> {
                    progressBar.setVisibility(View.GONE);
                    if (task.isSuccessful()) {
                        // Get user ID from FirebaseAuth
                        String userId = auth.getCurrentUser().getUid();
                        // Save user details to Firebase Realtime Database
                        saveUserToDatabase(userId, username, useremail);
                        Toast.makeText(RegisterActivity.this, "Registration Successful", Toast.LENGTH_SHORT).show();
                        startActivity(new Intent(RegisterActivity.this, MainActivity.class));
                        finish();
                    } else {

                        Toast.makeText(RegisterActivity.this, "Registration Failed: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                    }
                });

    }

    private void saveUserToDatabase(String userId, String username, String email) {
        // Get Firebase Realtime Database reference
        DatabaseReference database = FirebaseDatabase.getInstance().getReference("Users");

        // Create user object
        User user = new User(username, email);

        // Save user details under the user ID
        database.child(userId).setValue(user)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(this, "User saved in database", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(this, "Failed to save user: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                    }
                });
    }

    // User model class
    public static class User {
        public String username;
        public String email;

        // Default constructor for calls to DataSnapshot.getValue(User.class)
        public User() {}

        public User(String username, String email) {
            this.username = username;
            this.email = email;
        }
    }
}
