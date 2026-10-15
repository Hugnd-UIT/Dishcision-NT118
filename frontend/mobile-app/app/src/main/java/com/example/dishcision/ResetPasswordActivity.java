package com.example.dishcision;

import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.text.TextUtils;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.dishcision.network.ApiClient;
import com.example.dishcision.network.AuthResponse;
import com.example.dishcision.network.ResetPasswordRequest;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ResetPasswordActivity extends AppCompatActivity {

    private ImageView btnBack;
    private EditText edtTokenHash;
    private EditText edtNewPassword;
    private EditText edtConfirmNewPassword;
    private ImageView btnToggleNewPassword;
    private ImageView btnToggleConfirmNewPassword;
    private RelativeLayout btnResetPassword;
    private TextView tvBackToLogin;

    private boolean isNewPasswordVisible = false;
    private boolean isConfirmNewPasswordVisible = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_reset_password);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main_reset_password), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        initViews();
        setupListeners();
    }

    private void initViews() {
        btnBack = findViewById(R.id.btnBack);
        edtTokenHash = findViewById(R.id.edtTokenHash);
        edtNewPassword = findViewById(R.id.edtNewPassword);
        edtConfirmNewPassword = findViewById(R.id.edtConfirmNewPassword);
        btnToggleNewPassword = findViewById(R.id.btnToggleNewPassword);
        btnToggleConfirmNewPassword = findViewById(R.id.btnToggleConfirmNewPassword);
        btnResetPassword = findViewById(R.id.btnResetPassword);
        tvBackToLogin = findViewById(R.id.tvBackToLogin);
    }

    private void setupListeners() {
        if (btnBack != null) {
            btnBack.setOnClickListener(v -> finish());
        }

        if (btnResetPassword != null) {
            btnResetPassword.setOnClickListener(v -> performResetPassword());
        }

        if (btnToggleNewPassword != null && edtNewPassword != null) {
            btnToggleNewPassword.setOnClickListener(v -> {
                if (isNewPasswordVisible) {
                    edtNewPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                    isNewPasswordVisible = false;
                } else {
                    edtNewPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
                    isNewPasswordVisible = true;
                }
                edtNewPassword.setSelection(edtNewPassword.getText().length());
            });
        }

        if (btnToggleConfirmNewPassword != null && edtConfirmNewPassword != null) {
            btnToggleConfirmNewPassword.setOnClickListener(v -> {
                if (isConfirmNewPasswordVisible) {
                    edtConfirmNewPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                    isConfirmNewPasswordVisible = false;
                } else {
                    edtConfirmNewPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
                    isConfirmNewPasswordVisible = true;
                }
                edtConfirmNewPassword.setSelection(edtConfirmNewPassword.getText().length());
            });
        }

        if (tvBackToLogin != null) {
            tvBackToLogin.setOnClickListener(v -> {
                Intent intent = new Intent(ResetPasswordActivity.this, LoginActivity.class);
                startActivity(intent);
                finish();
            });
        }
    }

    private void performResetPassword() {
        String tokenHash = edtTokenHash != null ? edtTokenHash.getText().toString().trim() : "";
        String newPassword = edtNewPassword != null ? edtNewPassword.getText().toString() : "";
        String confirmNewPassword = edtConfirmNewPassword != null ? edtConfirmNewPassword.getText().toString() : "";

        if (TextUtils.isEmpty(tokenHash)) {
            edtTokenHash.setError("Vui lòng nhập mã xác thực (Token)");
            edtTokenHash.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(newPassword)) {
            edtNewPassword.setError("Vui lòng nhập mật khẩu mới");
            edtNewPassword.requestFocus();
            return;
        }

        if (newPassword.length() < 8) {
            edtNewPassword.setError("Mật khẩu mới phải từ 8 ký tự trở lên");
            edtNewPassword.requestFocus();
            return;
        }

        if (!newPassword.equals(confirmNewPassword)) {
            edtConfirmNewPassword.setError("Mật khẩu xác nhận không khớp");
            edtConfirmNewPassword.requestFocus();
            return;
        }

        try {
            ResetPasswordRequest request = new ResetPasswordRequest(tokenHash, newPassword);
            ApiClient.getApiService().resetPassword(request).enqueue(new Callback<AuthResponse>() {
                @Override
                public void onResponse(@NonNull Call<AuthResponse> call, @NonNull Response<AuthResponse> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        Toast.makeText(ResetPasswordActivity.this, "Đặt lại mật khẩu thành công!", Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(ResetPasswordActivity.this, LoginActivity.class);
                        startActivity(intent);
                        finish();
                    } else {
                        String msg = response.body() != null && response.body().getMessage() != null
                                ? response.body().getMessage()
                                : "Mã lỗi server: " + response.code();
                        Toast.makeText(ResetPasswordActivity.this, "Đặt lại mật khẩu thất bại: " + msg, Toast.LENGTH_LONG).show();
                    }
                }

                @Override
                public void onFailure(@NonNull Call<AuthResponse> call, @NonNull Throwable t) {
                    Toast.makeText(ResetPasswordActivity.this, "Lỗi kết nối server: " + t.getMessage(), Toast.LENGTH_LONG).show();
                }
            });
        } catch (Exception e) {
            Toast.makeText(this, "Lỗi cấu hình URL API: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    public String getEnteredNewPassword() {
        return edtNewPassword != null ? edtNewPassword.getText().toString() : "";
    }
}
