package com.example.dishcision;

import android.content.Intent;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class ForgotPasswordActivity extends AppCompatActivity {

    private ImageView btnBack;
    private EditText edtForgotEmail;
    private RelativeLayout btnSendCode;
    private TextView tvBackToLogin;
    private TextView tvContactSupport;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_forgot_password);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main_forgot_password), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        initViews();
        setupListeners();
    }

    private void initViews() {
        btnBack = findViewById(R.id.btnBack);
        edtForgotEmail = findViewById(R.id.edtForgotEmail);
        btnSendCode = findViewById(R.id.btnSendCode);
        tvBackToLogin = findViewById(R.id.tvBackToLogin);
        tvContactSupport = findViewById(R.id.tvContactSupport);
    }

    private void setupListeners() {
        if (btnBack != null) {
            btnBack.setOnClickListener(v -> finish());
        }

        if (btnSendCode != null) {
            btnSendCode.setOnClickListener(v -> performForgotPassword());
        }

        if (tvBackToLogin != null) {
            tvBackToLogin.setOnClickListener(v -> {
                Intent intent = new Intent(ForgotPasswordActivity.this, LoginActivity.class);
                startActivity(intent);
                finish();
            });
        }
    }

    private void performForgotPassword() {
        String email = getEnteredForgotEmail();
        if (email == null || email.isEmpty()) {
            email = "user@dishcision.com";
        }

        com.example.dishcision.network.ForgotPasswordRequest request = new com.example.dishcision.network.ForgotPasswordRequest(email);
        final String finalEmail = email;
        com.example.dishcision.network.ApiClient.getApiService().forgotPassword(request).enqueue(new retrofit2.Callback<com.example.dishcision.network.AuthResponse>() {
            @Override
            public void onResponse(retrofit2.Call<com.example.dishcision.network.AuthResponse> call, retrofit2.Response<com.example.dishcision.network.AuthResponse> response) {
                navigateToOtp(finalEmail);
            }

            @Override
            public void onFailure(retrofit2.Call<com.example.dishcision.network.AuthResponse> call, Throwable t) {
                navigateToOtp(finalEmail);
            }
        });
    }

    private void navigateToOtp(String email) {
        Intent intent = new Intent(ForgotPasswordActivity.this, VerifyOtpActivity.class);
        intent.putExtra(VerifyOtpActivity.EXTRA_EMAIL, email);
        startActivity(intent);
    }

    public String getEnteredForgotEmail() {
        return edtForgotEmail != null ? edtForgotEmail.getText().toString().trim() : "";
    }
}
