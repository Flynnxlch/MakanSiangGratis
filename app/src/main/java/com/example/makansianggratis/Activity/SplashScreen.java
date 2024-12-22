package com.example.makansianggratis.Activity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

import androidx.appcompat.app.AppCompatActivity;

import com.example.makansianggratis.R;

public class SplashScreen extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen);

        boolean isLoggedIn = getSharedPreferences("user_data", MODE_PRIVATE).getBoolean("isLoggedIn", false);

        // Delay for 10 seconds with splash screen effect
        new Handler().postDelayed(() -> {
            if (isLoggedIn) {
                Intent intent = new Intent(SplashScreen.this, MainActivity.class);
                startActivity(intent);
            } else {
                Intent intent = new Intent(SplashScreen.this, LoginActivity.class);
                startActivity(intent);
            }
            finish();
        }, 10000); // Delay in milliseconds (10 seconds in this case)
    }
}