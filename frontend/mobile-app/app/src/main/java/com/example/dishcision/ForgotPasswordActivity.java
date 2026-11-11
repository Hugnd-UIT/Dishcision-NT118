package com.example.dishcision;

import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.dishcision.network.ApiClient;
import com.example.dishcision.network.AuthHelper;
import com.example.dishcision.network.AuthResponse;
import com.example.dishcision.network.ForgotPasswordRequest;

import org.json.JSONObject;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

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

        ApiClient.init(this);
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
        if (email.isEmpty()) {
            Toast.makeText(this, "Vui lòng nhập email của bạn", Toast.LENGTH_SHORT).show();
            if (edtForgotEmail != null) edtForgotEmail.requestFocus();
            return;
        }
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            Toast.makeText(this, "Email không hợp lệ", Toast.LENGTH_SHORT).show();
            if (edtForgotEmail != null) edtForgotEmail.requestFocus();
            return;
        }

        if (btnSendCode != null) btnSendCode.setEnabled(false);
        ForgotPasswordRequest request = new ForgotPasswordRequest(email);
        ApiClient.getApiService().forgotPassword(request).enqueue(new Callback<AuthResponse>() {
            @Override
            public void onResponse(Call<AuthResponse> call, Response<AuthResponse> response) {
                if (isFinishing() || isDestroyed()) return;
                if (btnSendCode != null) btnSendCode.setEnabled(true);
                if (response.isSuccessful()) {
                    Toast.makeText(ForgotPasswordActivity.this, "Đã gửi mã OTP nếu email tồn tại trên hệ thống", Toast.LENGTH_SHORT).show();
                    int retrySec = (response.body() != null && response.body().getData() != null
                            && response.body().getData().retryAfterSeconds > 0)
                            ? response.body().getData().retryAfterSeconds
                            : AuthHelper.OTP_RESEND_COOLDOWN_SECONDS;
                    navigateToOtp(email, retrySec);
                } else {
                    String errMsg = parseErrorMessage(response, "Không thể gửi yêu cầu (mã " + response.code() + ")");
                    Toast.makeText(ForgotPasswordActivity.this, errMsg, Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<AuthResponse> call, Throwable t) {
                if (isFinishing() || isDestroyed()) return;
                if (btnSendCode != null) btnSendCode.setEnabled(true);
                Toast.makeText(ForgotPasswordActivity.this, "Lỗi kết nối máy chủ: " + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    private String parseErrorMessage(Response<?> response, String fallback) {
        try {
            if (response.errorBody() != null) {
                String raw = response.errorBody().string();
                String msg = new JSONObject(raw).optString("message", "").trim();
                if (!msg.isEmpty()) return msg;
            }
        } catch (Exception ignored) {}
        return fallback;
    }

    private void navigateToOtp(String email, int retryAfterSeconds) {
        Intent intent = new Intent(ForgotPasswordActivity.this, VerifyOtpActivity.class);
        intent.putExtra(VerifyOtpActivity.EXTRA_EMAIL, email);
        intent.putExtra(VerifyOtpActivity.EXTRA_RETRY_AFTER_SECONDS, retryAfterSeconds);
        startActivity(intent);
    }

    public String getEnteredForgotEmail() {
        return edtForgotEmail != null ? edtForgotEmail.getText().toString().trim() : "";
    }
}
