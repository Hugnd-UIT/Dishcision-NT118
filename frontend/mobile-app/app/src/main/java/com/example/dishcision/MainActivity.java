package com.example.dishcision;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import android.content.SharedPreferences;

import com.example.dishcision.network.TokenManager;

public class MainActivity extends AppCompatActivity {

    private static final int SPLASH_DELAY = 2000;

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

        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            boolean isLoggedIn = checkUserLoggedIn();

            if (isLoggedIn) {
                navigateToHome();
            } else {
                navigateToLogin();
            }
            finish();
        }, SPLASH_DELAY);
    }

    private boolean checkUserLoggedIn() {
        TokenManager tokenManager = new TokenManager(this);
        if (tokenManager.isLoggedIn()) return true;

        SharedPreferences prefs = getSharedPreferences("DishcisionPrefs", MODE_PRIVATE);
        String token = prefs.getString("access_token", null);
        String googleEmail = prefs.getString("user_email", null);
        return (token != null && !token.trim().isEmpty()) || (googleEmail != null && !googleEmail.trim().isEmpty());
    }

    private void navigateToHome() {
        Intent intent = new Intent(this, HomeActivity.class);
        startActivity(intent);
    }

    private void navigateToLogin() {
        Intent intent = new Intent(this, LoginActivity.class);
        startActivity(intent);
    }
}
