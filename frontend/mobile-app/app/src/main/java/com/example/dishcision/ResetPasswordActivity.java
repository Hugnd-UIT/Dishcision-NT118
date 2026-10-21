package com.example.dishcision;

import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

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
    private String userEmail = "m•••@example.com";

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
            tvResetSubtitle.setText("Tạo mật khẩu mới cho tài khoản dùng email " + userEmail + ".");
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
        // TỰ ĐỘNG BỎ QUA KIỂM TRA ĐIỀU KIỆN ĐỂ TEST CHUYỂN TRANG
        navigateToSuccess();
    }

    private void navigateToSuccess() {
        Intent intent = new Intent(ResetPasswordActivity.this, ResetPasswordSuccessActivity.class);
        intent.putExtra(ResetPasswordSuccessActivity.EXTRA_EMAIL, userEmail);
        startActivity(intent);
        finish();
    }

    public String getEnteredNewPassword() {
        return edtNewPassword != null ? edtNewPassword.getText().toString() : "";
    }
}
