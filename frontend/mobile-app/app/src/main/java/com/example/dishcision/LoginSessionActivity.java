package com.example.dishcision;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.dishcision.network.ApiClient;
import com.example.dishcision.network.RevokeSessionRequest;
import com.example.dishcision.network.SessionModel;
import com.example.dishcision.network.SessionsResponse;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LoginSessionActivity extends AppCompatActivity {

    private ImageView btnBack;
    private View cardSession1;
    private View cardSession2;
    private View dividerSession;
    private TextView btnRevokeSession1;
    private TextView btnRevokeSession2;
    private TextView tvNoOtherSessions;
    private LinearLayout btnManageMultiple;
    private TextView btnUnrecognizedResolution;

    private boolean isSession1Active = true;
    private boolean isSession2Active = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_login_session);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main_login_session), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        initViews();
        setupListeners();
        fetchSessionsFromBackend();
    }

    private void initViews() {
        btnBack = findViewById(R.id.btnBack);
        cardSession1 = findViewById(R.id.cardSession1);
        cardSession2 = findViewById(R.id.cardSession2);
        dividerSession = findViewById(R.id.dividerSession);
        btnRevokeSession1 = findViewById(R.id.btnRevokeSession1);
        btnRevokeSession2 = findViewById(R.id.btnRevokeSession2);
        tvNoOtherSessions = findViewById(R.id.tvNoOtherSessions);
        btnManageMultiple = findViewById(R.id.btnManageMultiple);
        btnUnrecognizedResolution = findViewById(R.id.btnUnrecognizedResolution);
    }

    private void setupListeners() {
        if (btnBack != null) {
            btnBack.setOnClickListener(v -> finish());
        }

        if (btnRevokeSession1 != null) {
            btnRevokeSession1.setOnClickListener(v -> revokeSessionOnBackend("session-2", 1));
        }

        if (btnRevokeSession2 != null) {
            btnRevokeSession2.setOnClickListener(v -> revokeSessionOnBackend("session-3", 2));
        }

        if (btnManageMultiple != null) {
            btnManageMultiple.setOnClickListener(v -> revokeAllOtherSessionsOnBackend());
        }

        if (btnUnrecognizedResolution != null) {
            btnUnrecognizedResolution.setOnClickListener(v -> showUnrecognizedResolutionDialog());
        }
    }

    private void fetchSessionsFromBackend() {
        ApiClient.getApiService().getSessions("Bearer dummy_token").enqueue(new Callback<SessionsResponse>() {
            @Override
            public void onResponse(@NonNull Call<SessionsResponse> call, @NonNull Response<SessionsResponse> response) {
                if (response.isSuccessful() && response.body() != null && response.body().getData() != null) {
                    List<SessionModel> sessions = response.body().getData();
                    boolean hasSession2 = false;
                    boolean hasSession3 = false;
                    for (SessionModel s : sessions) {
                        if ("session-2".equals(s.getId())) hasSession2 = true;
                        if ("session-3".equals(s.getId())) hasSession3 = true;
                    }
                    isSession1Active = hasSession2;
                    isSession2Active = hasSession3;
                }
                updateSessionsUI();
            }

            @Override
            public void onFailure(@NonNull Call<SessionsResponse> call, @NonNull Throwable t) {
                updateSessionsUI();
            }
        });
    }

    private void revokeSessionOnBackend(String sessionId, int sessionIndex) {
        RevokeSessionRequest request = new RevokeSessionRequest(sessionId);
        ApiClient.getApiService().revokeSession("Bearer dummy_token", request).enqueue(new Callback<SessionsResponse>() {
            @Override
            public void onResponse(@NonNull Call<SessionsResponse> call, @NonNull Response<SessionsResponse> response) {
                if (sessionIndex == 1) {
                    isSession1Active = false;
                } else if (sessionIndex == 2) {
                    isSession2Active = false;
                }
                updateSessionsUI();
                Toast.makeText(LoginSessionActivity.this, "Đã đăng xuất phiên này thành công", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onFailure(@NonNull Call<SessionsResponse> call, @NonNull Throwable t) {
                if (sessionIndex == 1) isSession1Active = false;
                if (sessionIndex == 2) isSession2Active = false;
                updateSessionsUI();
                Toast.makeText(LoginSessionActivity.this, "Đã đăng xuất phiên này", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void revokeAllOtherSessionsOnBackend() {
        List<String> toRevoke = new ArrayList<>();
        if (isSession1Active) toRevoke.add("session-2");
        if (isSession2Active) toRevoke.add("session-3");

        RevokeSessionRequest request = new RevokeSessionRequest(toRevoke);
        ApiClient.getApiService().revokeSession("Bearer dummy_token", request).enqueue(new Callback<SessionsResponse>() {
            @Override
            public void onResponse(@NonNull Call<SessionsResponse> call, @NonNull Response<SessionsResponse> response) {
                isSession1Active = false;
                isSession2Active = false;
                updateSessionsUI();
                Toast.makeText(LoginSessionActivity.this, "Đã đăng xuất tất cả các phiên khác", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onFailure(@NonNull Call<SessionsResponse> call, @NonNull Throwable t) {
                isSession1Active = false;
                isSession2Active = false;
                updateSessionsUI();
                Toast.makeText(LoginSessionActivity.this, "Đã đăng xuất tất cả các phiên khác", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateSessionsUI() {
        if (cardSession1 != null) {
            cardSession1.setVisibility(isSession1Active ? View.VISIBLE : View.GONE);
        }
        if (cardSession2 != null) {
            cardSession2.setVisibility(isSession2Active ? View.VISIBLE : View.GONE);
        }
        if (dividerSession != null) {
            dividerSession.setVisibility((isSession1Active && isSession2Active) ? View.VISIBLE : View.GONE);
        }
        if (tvNoOtherSessions != null) {
            tvNoOtherSessions.setVisibility((!isSession1Active && !isSession2Active) ? View.VISIBLE : View.GONE);
        }
    }

    private void showUnrecognizedResolutionDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Hướng dẫn xử lý thiết bị lạ")
                .setMessage("1. Chọn 'Đăng xuất phiên này' đối với thiết bị không nhận ra.\n" +
                        "2. Đổi mật khẩu tài khoản ngay lập tức để bảo vệ tài khoản.\n" +
                        "3. Bật Xác thực hai yếu tố (2FA) để tăng cường bảo mật.")
                .setPositiveButton("Đổi mật khẩu", (dialog, which) -> {
                    Intent intent = new Intent(LoginSessionActivity.this, ChangePasswordActivity.class);
                    startActivity(intent);
                })
                .setNegativeButton("Đóng", null)
                .show();
    }
}
