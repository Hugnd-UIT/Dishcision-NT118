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

    private ImageView btnBack;
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

    public String getEnteredNewPassword() {
        return edtNewPassword != null ? edtNewPassword.getText().toString() : "";
    }
}
