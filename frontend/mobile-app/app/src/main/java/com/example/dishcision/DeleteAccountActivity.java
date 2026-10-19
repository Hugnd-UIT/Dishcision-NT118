package com.example.dishcision;

import android.content.Intent;
import android.os.Bundle;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class DeleteAccountActivity extends AppCompatActivity {

    private ImageView btnBack;
    private CheckBox chkConfirmDelete;
    private LinearLayout btnReturnToSettings;
    private LinearLayout btnConfirmDeleteAccount;
    private TextView tvDeleteBtnText;

    private boolean isConfirmed = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_delete_account);

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
        chkConfirmDelete = findViewById(R.id.chkConfirmDelete);
        btnReturnToSettings = findViewById(R.id.btnReturnToSettings);
        btnConfirmDeleteAccount = findViewById(R.id.btnConfirmDeleteAccount);
        tvDeleteBtnText = findViewById(R.id.tvDeleteBtnText);
    }

    private void setupListeners() {
        if (btnBack != null) {
            btnBack.setOnClickListener(v -> finish());
        }

        if (btnReturnToSettings != null) {
            btnReturnToSettings.setOnClickListener(v -> finish());
        }

        if (chkConfirmDelete != null) {
            chkConfirmDelete.setOnCheckedChangeListener((buttonView, isChecked) -> {
                isConfirmed = isChecked;
                if (btnConfirmDeleteAccount != null) {
                    btnConfirmDeleteAccount.setClickable(isChecked);
                    btnConfirmDeleteAccount.setFocusable(isChecked);
                    if (isChecked) {
                        btnConfirmDeleteAccount.setBackgroundResource(R.drawable.bg_button_orange);
                        if (tvDeleteBtnText != null) {
                            tvDeleteBtnText.setTextColor(0xFFFFFFFF);
                        }
                    } else {
                        btnConfirmDeleteAccount.setBackgroundResource(R.drawable.bg_button_light_small);
                        if (tvDeleteBtnText != null) {
                            tvDeleteBtnText.setTextColor(0xFF8E7570);
                        }
                    }
                }
            });
        }

        if (btnConfirmDeleteAccount != null) {
            btnConfirmDeleteAccount.setOnClickListener(v -> {
                if (!isConfirmed) {
                    Toast.makeText(this, "Vui lòng chọn ô xác nhận trước khi tiếp tục", Toast.LENGTH_SHORT).show();
                    return;
                }
                Toast.makeText(this, "Tài khoản của bạn đã được xóa khỏi hệ thống.", Toast.LENGTH_LONG).show();
                Intent intent = new Intent(DeleteAccountActivity.this, LoginActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                finish();
            });
        }
    }
}
