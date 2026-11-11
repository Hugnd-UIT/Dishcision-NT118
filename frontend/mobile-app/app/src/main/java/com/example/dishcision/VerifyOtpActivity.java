package com.example.dishcision;

import android.content.Intent;
import android.os.Bundle;
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
import com.example.dishcision.network.AuthResponse;
import com.example.dishcision.network.ForgotPasswordRequest;
import com.example.dishcision.network.VerifyOtpRequest;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class VerifyOtpActivity extends AppCompatActivity {

    public static final String EXTRA_EMAIL = "extra_email";

    private ImageView btnBack;
    private android.view.View layoutTargetEmailBox;
    private TextView tvTargetEmail;
    private EditText edtOtpCode;
    private RelativeLayout btnVerifyOtp;
    private TextView tvResendCode;
    private TextView tvChangeEmail;

    private String userEmail = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_verify_otp);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main_verify_otp), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        ApiClient.init(this);
        initViews();
        setupData();
        setupListeners();
    }

    private void initViews() {
        btnBack = findViewById(R.id.btnBack);
        layoutTargetEmailBox = findViewById(R.id.layoutTargetEmailBox);
        tvTargetEmail = findViewById(R.id.tvTargetEmail);
        edtOtpCode = findViewById(R.id.edtOtpCode);
        btnVerifyOtp = findViewById(R.id.btnVerifyOtp);
        tvResendCode = findViewById(R.id.tvResendCode);
        tvChangeEmail = findViewById(R.id.tvChangeEmail);
    }

    private void setupData() {
        if (getIntent() != null && getIntent().hasExtra(EXTRA_EMAIL)) {
            String passedEmail = getIntent().getStringExtra(EXTRA_EMAIL);
            if (passedEmail != null && !passedEmail.trim().isEmpty()) {
                userEmail = passedEmail.trim();
            }
        }
        if (userEmail.isEmpty()) {
            if (layoutTargetEmailBox != null) {
                layoutTargetEmailBox.setVisibility(android.view.View.GONE);
            }
        } else {
            if (layoutTargetEmailBox != null) {
                layoutTargetEmailBox.setVisibility(android.view.View.VISIBLE);
            }
            if (tvTargetEmail != null) {
                tvTargetEmail.setText(userEmail);
            }
        }
    }

    private void setupListeners() {
        if (btnBack != null) {
            btnBack.setOnClickListener(v -> finish());
        }

        if (btnVerifyOtp != null) {
            btnVerifyOtp.setOnClickListener(v -> {
                if (userEmail.isEmpty()) {
                    Toast.makeText(this, "Thiếu thông tin email khôi phục", Toast.LENGTH_SHORT).show();
                    return;
                }
                String code = edtOtpCode != null ? edtOtpCode.getText().toString().trim() : "";
                if (!code.matches("\\d{6}")) {
                    Toast.makeText(this, "Vui lòng nhập mã OTP gồm 6 chữ số", Toast.LENGTH_SHORT).show();
                    if (edtOtpCode != null) edtOtpCode.requestFocus();
                    return;
                }

                btnVerifyOtp.setEnabled(false);
                VerifyOtpRequest request = new VerifyOtpRequest(userEmail, code);
                ApiClient.getApiService().verifyOtp(request).enqueue(new Callback<AuthResponse>() {
                    @Override
                    public void onResponse(Call<AuthResponse> call, Response<AuthResponse> response) {
                        btnVerifyOtp.setEnabled(true);
                        if (response.isSuccessful() && response.body() != null
                                && response.body().getData() != null
                                && response.body().getData().tokenHash != null
                                && !response.body().getData().tokenHash.isEmpty()) {
                            Toast.makeText(VerifyOtpActivity.this, "Xác thực mã OTP thành công!", Toast.LENGTH_SHORT).show();
                            navigateToResetPassword(response.body().getData().tokenHash);
                        } else {
                            Toast.makeText(VerifyOtpActivity.this, "Mã OTP không chính xác hoặc đã hết hạn", Toast.LENGTH_LONG).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<AuthResponse> call, Throwable t) {
                        btnVerifyOtp.setEnabled(true);
                        Toast.makeText(VerifyOtpActivity.this, "Lỗi kết nối máy chủ: " + t.getMessage(), Toast.LENGTH_LONG).show();
                    }
                });
            });
        }

        if (tvResendCode != null) {
            tvResendCode.setOnClickListener(v -> {
                if (userEmail.isEmpty()) {
                    Toast.makeText(this, "Thiếu thông tin email khôi phục", Toast.LENGTH_SHORT).show();
                    return;
                }
                ForgotPasswordRequest req = new ForgotPasswordRequest(userEmail);
                ApiClient.getApiService().forgotPassword(req).enqueue(new Callback<AuthResponse>() {
                    @Override
                    public void onResponse(Call<AuthResponse> call, Response<AuthResponse> response) {
                        if (response.isSuccessful()) {
                            Toast.makeText(VerifyOtpActivity.this, "Đã gửi lại mã OTP tới email!", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(VerifyOtpActivity.this, "Không thể gửi lại mã (mã " + response.code() + ")", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<AuthResponse> call, Throwable t) {
                        Toast.makeText(VerifyOtpActivity.this, "Lỗi kết nối máy chủ", Toast.LENGTH_SHORT).show();
                    }
                });
            });
        }

        if (tvChangeEmail != null) {
            tvChangeEmail.setOnClickListener(v -> finish());
        }
    }

    private void navigateToResetPassword(String tokenHash) {
        Intent intent = new Intent(VerifyOtpActivity.this, ResetPasswordActivity.class);
        intent.putExtra(ResetPasswordActivity.EXTRA_TOKEN_HASH, tokenHash);
        intent.putExtra(ResetPasswordActivity.EXTRA_EMAIL, userEmail);
        startActivity(intent);
    }
}
