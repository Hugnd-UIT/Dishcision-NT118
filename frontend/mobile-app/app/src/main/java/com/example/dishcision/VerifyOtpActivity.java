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

public class VerifyOtpActivity extends AppCompatActivity {

    public static final String EXTRA_EMAIL = "extra_email";

    private ImageView btnBack;
    private TextView tvTargetEmail;
    private EditText edtOtpCode;
    private RelativeLayout btnVerifyOtp;
    private TextView tvResendCode;
    private TextView tvChangeEmail;

    private String userEmail = "m•••@example.com";

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

        initViews();
        setupData();
        setupListeners();
    }

    private void initViews() {
        btnBack = findViewById(R.id.btnBack);
        tvTargetEmail = findViewById(R.id.tvTargetEmail);
        edtOtpCode = findViewById(R.id.edtOtpCode);
        btnVerifyOtp = findViewById(R.id.btnVerifyOtp);
        tvResendCode = findViewById(R.id.tvResendCode);
        tvChangeEmail = findViewById(R.id.tvChangeEmail);
    }

    private void setupData() {
        if (getIntent() != null && getIntent().hasExtra(EXTRA_EMAIL)) {
            String passedEmail = getIntent().getStringExtra(EXTRA_EMAIL);
            if (passedEmail != null && !passedEmail.isEmpty()) {
                userEmail = passedEmail;
            }
        }
        if (tvTargetEmail != null) {
            tvTargetEmail.setText(userEmail);
        }
    }

    private void setupListeners() {
        if (btnBack != null) {
            btnBack.setOnClickListener(v -> finish());
        }

        if (btnVerifyOtp != null) {
            btnVerifyOtp.setOnClickListener(v -> {
                String code = edtOtpCode != null ? edtOtpCode.getText().toString().trim() : "";
                if (code.isEmpty()) {
                    code = "123456";
                }
                Intent intent = new Intent(VerifyOtpActivity.this, ResetPasswordActivity.class);
                intent.putExtra(ResetPasswordActivity.EXTRA_TOKEN_HASH, code);
                intent.putExtra(ResetPasswordActivity.EXTRA_EMAIL, userEmail);
                startActivity(intent);
            });
        }

        if (tvResendCode != null) {
            tvResendCode.setOnClickListener(v -> Toast.makeText(VerifyOtpActivity.this, "Đã gửi lại mã khôi phục", Toast.LENGTH_SHORT).show());
        }

        if (tvChangeEmail != null) {
            tvChangeEmail.setOnClickListener(v -> finish());
        }
    }
}
