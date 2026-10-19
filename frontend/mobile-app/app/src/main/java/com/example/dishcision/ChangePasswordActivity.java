package com.example.dishcision;

import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class ChangePasswordActivity extends AppCompatActivity {

    private ImageView btnBack;
    private TextView btnForgotPassword;
    private EditText edtCurrentPassword;
    private ImageView btnToggleCurrentPassword;
    private EditText edtNewPassword;
    private ImageView btnToggleNewPassword;
    private EditText edtConfirmNewPassword;
    private ImageView btnToggleConfirmPassword;
    private LinearLayout btnUpdatePassword;

    private boolean isCurrentVisible = false;
    private boolean isNewVisible = false;
    private boolean isConfirmVisible = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_change_password);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(android.R.id.content), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        initViews();
        setupListeners();
    }

    private void initViews() {
        btnBack = findViewById(R.id.btnBack);
        btnForgotPassword = findViewById(R.id.btnForgotPassword);
        edtCurrentPassword = findViewById(R.id.edtCurrentPassword);
        btnToggleCurrentPassword = findViewById(R.id.btnToggleCurrentPassword);
        edtNewPassword = findViewById(R.id.edtNewPassword);
        btnToggleNewPassword = findViewById(R.id.btnToggleNewPassword);
        edtConfirmNewPassword = findViewById(R.id.edtConfirmNewPassword);
        btnToggleConfirmPassword = findViewById(R.id.btnToggleConfirmPassword);
        btnUpdatePassword = findViewById(R.id.btnUpdatePassword);
    }

    private void setupListeners() {
        if (btnBack != null) {
            btnBack.setOnClickListener(v -> finish());
        }

        if (btnForgotPassword != null) {
            btnForgotPassword.setOnClickListener(v -> {
                Intent intent = new Intent(ChangePasswordActivity.this, ForgotPasswordActivity.class);
                startActivity(intent);
            });
        }

        if (btnToggleCurrentPassword != null) {
            btnToggleCurrentPassword.setOnClickListener(v -> {
                isCurrentVisible = !isCurrentVisible;
                togglePasswordVisibility(edtCurrentPassword, isCurrentVisible);
            });
        }

        if (btnToggleNewPassword != null) {
            btnToggleNewPassword.setOnClickListener(v -> {
                isNewVisible = !isNewVisible;
                togglePasswordVisibility(edtNewPassword, isNewVisible);
            });
        }

        if (btnToggleConfirmPassword != null) {
            btnToggleConfirmPassword.setOnClickListener(v -> {
                isConfirmVisible = !isConfirmVisible;
                togglePasswordVisibility(edtConfirmNewPassword, isConfirmVisible);
            });
        }

        if (btnUpdatePassword != null) {
            btnUpdatePassword.setOnClickListener(v -> {
                String currentPass = edtCurrentPassword != null ? edtCurrentPassword.getText().toString().trim() : "";
                String newPass = edtNewPassword != null ? edtNewPassword.getText().toString().trim() : "";
                String confirmPass = edtConfirmNewPassword != null ? edtConfirmNewPassword.getText().toString().trim() : "";

                if (currentPass.isEmpty()) {
                    Toast.makeText(this, "Vui lòng nhập mật khẩu hiện tại!", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (newPass.isEmpty()) {
                    Toast.makeText(this, "Vui lòng nhập mật khẩu mới!", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (!newPass.equals(confirmPass)) {
                    Toast.makeText(this, "Mật khẩu xác nhận không trùng khớp!", Toast.LENGTH_SHORT).show();
                    return;
                }

                Toast.makeText(this, "Đã cập nhật mật khẩu thành công!", Toast.LENGTH_SHORT).show();
                finish();
            });
        }
    }

    private void togglePasswordVisibility(EditText editText, boolean visible) {
        if (editText == null) return;
        if (visible) {
            editText.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
        } else {
            editText.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        }
        editText.setSelection(editText.getText().length());
    }
}
