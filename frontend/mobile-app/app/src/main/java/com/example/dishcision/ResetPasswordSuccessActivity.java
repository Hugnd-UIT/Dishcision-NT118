package com.example.dishcision;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class ResetPasswordSuccessActivity extends AppCompatActivity {

    public static final String EXTRA_EMAIL = "extra_email";

    private ImageView btnBack;
    private TextView tvAccountSummary;
    private RelativeLayout btnBackToLogin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_reset_password_success);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main_reset_password_success), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        initViews();
        setupData();
        setupListeners();
    }

    private void initViews() {
        btnBack = findViewById(R.id.btnBack);
        tvAccountSummary = findViewById(R.id.tvAccountSummary);
        btnBackToLogin = findViewById(R.id.btnBackToLogin);
    }

    private void setupData() {
        if (getIntent() != null && getIntent().hasExtra(EXTRA_EMAIL)) {
            String passedEmail = getIntent().getStringExtra(EXTRA_EMAIL);
            if (passedEmail != null && !passedEmail.isEmpty() && tvAccountSummary != null) {
                tvAccountSummary.setText("ℹ️ Tài khoản khôi phục: " + passedEmail);
            }
        }
    }

    private void setupListeners() {
        if (btnBack != null) {
            btnBack.setOnClickListener(v -> navigateToLogin());
        }

        if (btnBackToLogin != null) {
            btnBackToLogin.setOnClickListener(v -> navigateToLogin());
        }
    }

    private void navigateToLogin() {
        Intent intent = new Intent(ResetPasswordSuccessActivity.this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }
}
