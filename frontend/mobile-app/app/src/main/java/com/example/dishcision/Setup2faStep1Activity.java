package com.example.dishcision;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.dishcision.network.ApiClient;
import com.example.dishcision.network.TwoFactorSetupResponse;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class Setup2faStep1Activity extends AppCompatActivity {

    private ImageView btnBack;
    private View btnContinue;
    private TextView tvSetupKey;
    private String rawSecret = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_setup_2fa_step1);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main_setup_2fa_step1), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        initViews();
        setupListeners();
        loadSetupKey();
    }

    private void initViews() {
        btnBack = findViewById(R.id.btnBack);
        btnContinue = findViewById(R.id.btnContinue);
        tvSetupKey = findViewById(R.id.tvSetupKey);
    }

    private void setupListeners() {
        if (btnBack != null) {
            btnBack.setOnClickListener(v -> finish());
        }

        if (btnContinue != null) {
            btnContinue.setOnClickListener(v -> {
                Intent intent = new Intent(Setup2faStep1Activity.this, Setup2faStep2Activity.class);
                startActivity(intent);
            });
        }

        if (tvSetupKey != null) {
            tvSetupKey.setOnClickListener(v -> {
                if (rawSecret == null || rawSecret.trim().isEmpty()) {
                    return;
                }
                ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                if (clipboard != null) {
                    clipboard.setPrimaryClip(ClipData.newPlainText("2FA Setup Key", rawSecret));
                    Toast.makeText(this, "Đã sao chép khóa thiết lập", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    private void loadSetupKey() {
        String authHeader = ApiClient.getAuthHeader(this);
        if (authHeader.isEmpty()) {
            if (tvSetupKey != null) tvSetupKey.setText("Vui lòng đăng nhập lại");
            return;
        }

        ApiClient.getApiService().get2FASetup(authHeader).enqueue(new Callback<TwoFactorSetupResponse>() {
            @Override
            public void onResponse(@NonNull Call<TwoFactorSetupResponse> call, @NonNull Response<TwoFactorSetupResponse> response) {
                if (response.isSuccessful() && response.body() != null && response.body().data != null) {
                    TwoFactorSetupResponse.SetupData data = response.body().data;
                    rawSecret = data.secret != null ? data.secret : "";
                    String displayKey = (data.formattedKey != null && !data.formattedKey.isEmpty()) ? data.formattedKey : rawSecret;
                    if (tvSetupKey != null) {
                        tvSetupKey.setText(displayKey.isEmpty() ? "Không có khóa" : displayKey);
                    }
                } else {
                    if (tvSetupKey != null) tvSetupKey.setText("Không thể tải khóa thiết lập");
                    Toast.makeText(Setup2faStep1Activity.this, "Không thể lấy khóa 2FA (mã " + response.code() + ")", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<TwoFactorSetupResponse> call, @NonNull Throwable t) {
                if (tvSetupKey != null) tvSetupKey.setText("Lỗi kết nối máy chủ");
                Toast.makeText(Setup2faStep1Activity.this, "Không thể kết nối máy chủ", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
