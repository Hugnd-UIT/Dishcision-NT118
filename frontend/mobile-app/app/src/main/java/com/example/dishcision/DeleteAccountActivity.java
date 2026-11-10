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
                new androidx.appcompat.app.AlertDialog.Builder(this)
                        .setTitle("Xóa vĩnh viễn tài khoản?")
                        .setMessage("Hồ sơ, bài viết và dữ liệu của bạn sẽ bị xóa. Hành động này không thể hoàn tác.")
                        .setNegativeButton("Hủy", null)
                        .setPositiveButton("Xóa tài khoản", (d, w) -> deleteAccountOnBackend())
                        .show();
            });
        }
    }

    private void deleteAccountOnBackend() {
        if (btnConfirmDeleteAccount != null) btnConfirmDeleteAccount.setEnabled(false);
        if (tvDeleteBtnText != null) tvDeleteBtnText.setText("Đang xóa...");

        com.example.dishcision.network.ApiClient.getApiService()
                .deleteMyAccount(com.example.dishcision.network.ApiClient.getAuthHeader(this))
                .enqueue(new retrofit2.Callback<Object>() {
                    @Override
                    public void onResponse(retrofit2.Call<Object> call, retrofit2.Response<Object> response) {
                        if (response.isSuccessful()) {
                            Toast.makeText(DeleteAccountActivity.this, "Tài khoản của bạn đã được xóa khỏi hệ thống.", Toast.LENGTH_LONG).show();
                            com.example.dishcision.network.AuthHelper.clearLocalSession(DeleteAccountActivity.this);
                            Intent intent = new Intent(DeleteAccountActivity.this, LoginActivity.class);
                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            startActivity(intent);
                            finish();
                        } else {
                            onDeleteFailed("Không thể xóa tài khoản (mã " + response.code() + "). Vui lòng thử lại.");
                        }
                    }

                    @Override
                    public void onFailure(retrofit2.Call<Object> call, Throwable t) {
                        onDeleteFailed("Không thể kết nối máy chủ. Vui lòng thử lại.");
                    }
                });
    }

    private void onDeleteFailed(String message) {
        if (btnConfirmDeleteAccount != null) btnConfirmDeleteAccount.setEnabled(true);
        if (tvDeleteBtnText != null) tvDeleteBtnText.setText("Xóa tài khoản");
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
    }
}
