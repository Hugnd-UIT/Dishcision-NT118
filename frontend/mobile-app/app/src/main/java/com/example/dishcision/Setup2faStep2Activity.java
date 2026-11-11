package com.example.dishcision;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.dishcision.network.ApiClient;
import com.example.dishcision.network.TwoFactorStatusResponse;
import com.example.dishcision.network.TwoFactorVerifyRequest;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class Setup2faStep2Activity extends AppCompatActivity {

    private ImageView btnBack;
    private EditText edt2FACode;
    private Button btnConfirm;
    private View btnBackGuide;
    private boolean isSubmitting = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_setup_2fa_step2);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main_setup_2fa_step2), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        initViews();
        setupListeners();
    }

    private void initViews() {
        btnBack = findViewById(R.id.btnBack);
        edt2FACode = findViewById(R.id.edt2FACode);
        btnConfirm = findViewById(R.id.btnConfirm);
        btnBackGuide = findViewById(R.id.btnBackGuide);

        com.example.dishcision.network.CurrentUserBinder.bind(this,
                findViewById(R.id.imgAvatar),
                findViewById(R.id.tvProfileName),
                findViewById(R.id.tvProfileHandle));
    }

    private void setupListeners() {
        if (btnBack != null) {
            btnBack.setOnClickListener(v -> finish());
        }

        if (btnBackGuide != null) {
            btnBackGuide.setOnClickListener(v -> finish());
        }

        if (btnConfirm != null) {
            btnConfirm.setOnClickListener(v -> submit2FACodeToBackend());
        }
    }

    private void submit2FACodeToBackend() {
        if (edt2FACode == null || isSubmitting) return;
        String code = edt2FACode.getText().toString().trim();

        if (TextUtils.isEmpty(code)) {
            Toast.makeText(this, "Vui lòng nhập mã xác thực từ ứng dụng", Toast.LENGTH_SHORT).show();
            return;
        }

        String authHeader = ApiClient.getAuthHeader(this);
        if (authHeader.isEmpty()) {
            Toast.makeText(this, "Vui lòng đăng nhập lại", Toast.LENGTH_SHORT).show();
            return;
        }

        isSubmitting = true;
        if (btnConfirm != null) btnConfirm.setEnabled(false);

        TwoFactorVerifyRequest request = new TwoFactorVerifyRequest(code);
        ApiClient.getApiService().verify2FA(authHeader, request).enqueue(new Callback<TwoFactorStatusResponse>() {
            @Override
            public void onResponse(@NonNull Call<TwoFactorStatusResponse> call, @NonNull Response<TwoFactorStatusResponse> response) {
                isSubmitting = false;
                if (btnConfirm != null) btnConfirm.setEnabled(true);
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    Toast.makeText(Setup2faStep2Activity.this, "Xác thực hai yếu tố đã được bật thành công!", Toast.LENGTH_LONG).show();
                    finish();
                } else {
                    Toast.makeText(Setup2faStep2Activity.this, "Mã xác thực không hợp lệ (mã " + response.code() + ")", Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<TwoFactorStatusResponse> call, @NonNull Throwable t) {
                isSubmitting = false;
                if (btnConfirm != null) btnConfirm.setEnabled(true);
                Toast.makeText(Setup2faStep2Activity.this, "Lỗi kết nối máy chủ: " + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }
}
