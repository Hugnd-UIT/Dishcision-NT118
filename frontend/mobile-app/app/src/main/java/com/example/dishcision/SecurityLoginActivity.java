package com.example.dishcision;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.dishcision.network.ApiClient;
import com.example.dishcision.network.TwoFactorStatusResponse;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SecurityLoginActivity extends AppCompatActivity {

    private ImageView btnBack;
    private ImageView img2FAIcon;
    private TextView tv2FAStatusTag;
    private TextView tv2FADesc;
    private Button btnSetup2FA;
    private LinearLayout btnChangePassword;
    private LinearLayout btnActiveSessions;

    private boolean is2FAEnabled = false;
    private boolean isGoogleLogin = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_security_login);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main_security_login), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        initViews();
        setupListeners();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (!isGoogleLogin) {
            fetch2FAStatusFromBackend();
        }
    }

    private void initViews() {
        btnBack = findViewById(R.id.btnBack);
        img2FAIcon = findViewById(R.id.img2FAIcon);
        tv2FAStatusTag = findViewById(R.id.tv2FAStatusTag);
        tv2FADesc = findViewById(R.id.tv2FADesc);
        btnSetup2FA = findViewById(R.id.btnSetup2FA);
        btnChangePassword = findViewById(R.id.btnChangePassword);
        isGoogleLogin = getSharedPreferences("DishcisionPrefs", MODE_PRIVATE).getBoolean("is_google_login", false);
        if (isGoogleLogin) {
            if (btnChangePassword != null) btnChangePassword.setVisibility(View.GONE);
            update2FAUIState();
        }
        btnActiveSessions = findViewById(R.id.btnActiveSessions);

        com.example.dishcision.network.CurrentUserBinder.bind(this,
                findViewById(R.id.imgAvatar),
                findViewById(R.id.tvProfileName),
                findViewById(R.id.tvProfileHandle));
    }

    private void setupListeners() {
        if (btnBack != null) {
            btnBack.setOnClickListener(v -> finish());
        }

        if (btnSetup2FA != null) {
            btnSetup2FA.setOnClickListener(v -> {
                if (is2FAEnabled) {
                    disable2FAOnBackend();
                } else {
                    Intent intent = new Intent(SecurityLoginActivity.this, Setup2faStep2Activity.class);
                    startActivity(intent);
                }
            });
        }

        if (btnChangePassword != null) {
            btnChangePassword.setOnClickListener(v -> {
                Intent intent = new Intent(SecurityLoginActivity.this, ChangePasswordActivity.class);
                startActivity(intent);
            });
        }

        if (btnActiveSessions != null) {
            btnActiveSessions.setOnClickListener(v -> {
                Intent intent = new Intent(SecurityLoginActivity.this, LoginSessionActivity.class);
                startActivity(intent);
            });
        }
    }

    private void fetch2FAStatusFromBackend() {
        ApiClient.getApiService().get2FAStatus(ApiClient.getAuthHeader(this)).enqueue(new Callback<TwoFactorStatusResponse>() {
            @Override
            public void onResponse(@NonNull Call<TwoFactorStatusResponse> call, @NonNull Response<TwoFactorStatusResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    is2FAEnabled = response.body().isTwoFactorEnabled();
                } else {
                    is2FAEnabled = false;
                }
                update2FAUIState();
            }

            @Override
            public void onFailure(@NonNull Call<TwoFactorStatusResponse> call, @NonNull Throwable t) {
                update2FAUIState();
            }
        });
    }

    private void disable2FAOnBackend() {
        ApiClient.getApiService().disable2FA(ApiClient.getAuthHeader(this)).enqueue(new Callback<TwoFactorStatusResponse>() {
            @Override
            public void onResponse(@NonNull Call<TwoFactorStatusResponse> call, @NonNull Response<TwoFactorStatusResponse> response) {
                is2FAEnabled = false;
                update2FAUIState();
                Toast.makeText(SecurityLoginActivity.this, "Đã tắt xác thực hai yếu tố", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onFailure(@NonNull Call<TwoFactorStatusResponse> call, @NonNull Throwable t) {
                Toast.makeText(SecurityLoginActivity.this, "Không thể kết nối máy chủ", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void update2FAUIState() {
        if (tv2FAStatusTag == null || btnSetup2FA == null) return;

        if (isGoogleLogin) {
            tv2FAStatusTag.setText("Bảo vệ qua Google");
            tv2FAStatusTag.setTextColor(Color.parseColor("#00A86B"));
            if (img2FAIcon != null) {
                img2FAIcon.setImageResource(R.drawable.ic_shield_check);
                img2FAIcon.setColorFilter(Color.parseColor("#00A86B"));
            }
            if (tv2FADesc != null) {
                tv2FADesc.setText("Tài khoản này đăng nhập bằng Google nên không cần bật xác thực 2FA qua email.");
            }
            btnSetup2FA.setVisibility(View.GONE);
            return;
        }

        btnSetup2FA.setVisibility(View.VISIBLE);
        if (tv2FADesc != null) {
            tv2FADesc.setText(R.string.security_2fa_desc);
        }

        if (is2FAEnabled) {
            tv2FAStatusTag.setText("Đã bật");
            tv2FAStatusTag.setTextColor(Color.parseColor("#00A86B"));
            if (img2FAIcon != null) {
                img2FAIcon.setImageResource(R.drawable.ic_shield_check);
                img2FAIcon.setColorFilter(Color.parseColor("#00A86B"));
            }
            btnSetup2FA.setText("Tắt xác thực hai yếu tố");
        } else {
            tv2FAStatusTag.setText(R.string.security_not_setup);
            tv2FAStatusTag.setTextColor(Color.parseColor("#D9381E"));
            if (img2FAIcon != null) {
                img2FAIcon.setImageResource(R.drawable.ic_shield);
                img2FAIcon.setColorFilter(Color.parseColor("#D9381E"));
            }
            btnSetup2FA.setText(R.string.security_2fa_button);
        }
    }
}
