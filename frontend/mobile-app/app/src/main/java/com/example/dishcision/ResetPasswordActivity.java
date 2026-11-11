package com.example.dishcision;

import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
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
import com.example.dishcision.network.ResetPasswordRequest;

import org.json.JSONObject;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ResetPasswordActivity extends AppCompatActivity {

    public static final String EXTRA_TOKEN_HASH = "extra_token_hash";
    public static final String EXTRA_EMAIL = "extra_email";

    private ImageView btnBack;
    private TextView tvResetSubtitle;
    private EditText edtTokenHash;
    private EditText edtNewPassword;
    private EditText edtConfirmNewPassword;
    private ImageView btnToggleNewPassword;
    private ImageView btnToggleConfirmNewPassword;
    private RelativeLayout btnResetPassword;

    private boolean isNewPasswordVisible = false;
    private boolean isConfirmNewPasswordVisible = false;
    private String userEmail = "";

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

        ApiClient.init(this);
        initViews();
        setupData();
        setupListeners();
    }

    private void initViews() {
        btnBack = findViewById(R.id.btnBack);
        tvResetSubtitle = findViewById(R.id.tvResetSubtitle);
        edtTokenHash = findViewById(R.id.edtTokenHash);
        edtNewPassword = findViewById(R.id.edtNewPassword);
        edtConfirmNewPassword = findViewById(R.id.edtConfirmNewPassword);
        btnToggleNewPassword = findViewById(R.id.btnToggleNewPassword);
        btnToggleConfirmNewPassword = findViewById(R.id.btnToggleConfirmNewPassword);
        btnResetPassword = findViewById(R.id.btnResetPassword);
    }

    private void setupData() {
        if (getIntent() != null) {
            if (getIntent().hasExtra(EXTRA_TOKEN_HASH)) {
                String token = getIntent().getStringExtra(EXTRA_TOKEN_HASH);
                if (token != null && edtTokenHash != null) {
                    edtTokenHash.setText(token);
                }
            }
            if (getIntent().hasExtra(EXTRA_EMAIL)) {
                String passedEmail = getIntent().getStringExtra(EXTRA_EMAIL);
                if (passedEmail != null && !passedEmail.isEmpty()) {
                    userEmail = passedEmail;
                }
            }
        }
        if (tvResetSubtitle != null) {
            if (!userEmail.isEmpty()) {
                tvResetSubtitle.setText("Tạo mật khẩu mới cho tài khoản dùng email " + userEmail + ".");
            } else {
                tvResetSubtitle.setText("Tạo mật khẩu mới cho tài khoản của bạn.");
            }
        }
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
    }

    private void performResetPassword() {
        String token = edtTokenHash != null ? edtTokenHash.getText().toString().trim() : "";
        String newPassword = getEnteredNewPassword();
        String confirmPassword = edtConfirmNewPassword != null ? edtConfirmNewPassword.getText().toString() : "";

        if (token.isEmpty()) {
            Toast.makeText(this, "Phiên đặt lại mật khẩu không hợp lệ", Toast.LENGTH_SHORT).show();
            return;
        }
        String pwError = AuthHelper.validateNewPassword(newPassword);
        if (pwError != null) {
            Toast.makeText(this, pwError, Toast.LENGTH_LONG).show();
            if (edtNewPassword != null) edtNewPassword.requestFocus();
            return;
        }
        if (!newPassword.equals(confirmPassword)) {
            Toast.makeText(this, "Mật khẩu xác nhận không trùng khớp", Toast.LENGTH_SHORT).show();
            if (edtConfirmNewPassword != null) edtConfirmNewPassword.requestFocus();
            return;
        }

        if (btnResetPassword != null) btnResetPassword.setEnabled(false);
        ResetPasswordRequest request = new ResetPasswordRequest(token, newPassword);
        ApiClient.getApiService().resetPassword(request).enqueue(new Callback<AuthResponse>() {
            @Override
            public void onResponse(Call<AuthResponse> call, Response<AuthResponse> response) {
                if (btnResetPassword != null) btnResetPassword.setEnabled(true);
                if (response.isSuccessful()) {
                    Toast.makeText(ResetPasswordActivity.this, "Đặt lại mật khẩu thành công!", Toast.LENGTH_SHORT).show();
                    navigateToSuccess();
                } else {
                    String errMsg = "Không thể đặt lại mật khẩu hoặc liên kết đã hết hạn";
                    if (response.errorBody() != null) {
                        try {
                            JSONObject json = new JSONObject(response.errorBody().string());
                            if (json.has("message") && !json.optString("message", "").isEmpty()) {
                                errMsg = json.getString("message");
                            }
                        } catch (Exception ignored) {}
                    }
                    Toast.makeText(ResetPasswordActivity.this, errMsg, Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<AuthResponse> call, Throwable t) {
                if (btnResetPassword != null) btnResetPassword.setEnabled(true);
                Toast.makeText(ResetPasswordActivity.this, "Lỗi kết nối máy chủ: " + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    private void navigateToSuccess() {
        Intent intent = new Intent(ResetPasswordActivity.this, ResetPasswordSuccessActivity.class);
        if (userEmail != null && !userEmail.isEmpty()) {
            intent.putExtra(ResetPasswordSuccessActivity.EXTRA_EMAIL, userEmail);
        }
        startActivity(intent);
        finish();
    }

    public String getEnteredNewPassword() {
        return edtNewPassword != null ? edtNewPassword.getText().toString() : "";
    }
}
