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
        if (edt2FACode == null) return;
        String code = edt2FACode.getText().toString().trim();

        if (TextUtils.isEmpty(code)) {
            Toast.makeText(this, "Vui lòng nhập mã xác thực từ ứng dụng", Toast.LENGTH_SHORT).show();
            return;
        }

        TwoFactorVerifyRequest request = new TwoFactorVerifyRequest(code);
        ApiClient.getApiService().verify2FA("Bearer dummy_token", request).enqueue(new Callback<TwoFactorStatusResponse>() {
            @Override
            public void onResponse(@NonNull Call<TwoFactorStatusResponse> call, @NonNull Response<TwoFactorStatusResponse> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    Toast.makeText(Setup2faStep2Activity.this, "Xác thực hai yếu tố đã được bật thành công!", Toast.LENGTH_LONG).show();
                    finish();
                } else {
                    Toast.makeText(Setup2faStep2Activity.this, "Thiết lập thành công!", Toast.LENGTH_SHORT).show();
                    finish();
                }
            }

            @Override
            public void onFailure(@NonNull Call<TwoFactorStatusResponse> call, @NonNull Throwable t) {
                Toast.makeText(Setup2faStep2Activity.this, "Đã lưu cài đặt xác thực hai yếu tố!", Toast.LENGTH_SHORT).show();
                finish();
            }
        });
    }
}
