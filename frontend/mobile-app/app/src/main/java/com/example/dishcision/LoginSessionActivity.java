package com.example.dishcision;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
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
import com.example.dishcision.network.AuthHelper;
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
    private TextView tvCurrentDeviceName;
    private TextView tvCurrentSessionStatus;
    private TextView tvCurrentSessionBadge;
    private TextView btnManageCurrentSession;
    private LinearLayout layoutOtherSessionsList;
    private TextView tvNoOtherSessions;
    private LinearLayout btnManageMultiple;
    private TextView btnUnrecognizedResolution;

    private SessionModel currentSession;
    private final List<SessionModel> otherSessions = new ArrayList<>();

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
    }

    @Override
    protected void onResume() {
        super.onResume();
        fetchSessionsFromBackend();
    }

    private void initViews() {
        btnBack = findViewById(R.id.btnBack);
        tvCurrentDeviceName = findViewById(R.id.tvCurrentDeviceName);
        tvCurrentSessionStatus = findViewById(R.id.tvCurrentSessionStatus);
        tvCurrentSessionBadge = findViewById(R.id.tvCurrentSessionBadge);
        btnManageCurrentSession = findViewById(R.id.btnManageCurrentSession);
        layoutOtherSessionsList = findViewById(R.id.layoutOtherSessionsList);
        tvNoOtherSessions = findViewById(R.id.tvNoOtherSessions);
        btnManageMultiple = findViewById(R.id.btnManageMultiple);
        btnUnrecognizedResolution = findViewById(R.id.btnUnrecognizedResolution);
    }

    private void setupListeners() {
        if (btnBack != null) {
            btnBack.setOnClickListener(v -> finish());
        }

        if (btnManageCurrentSession != null) {
            btnManageCurrentSession.setOnClickListener(v -> new AlertDialog.Builder(this)
                    .setTitle("Phiên đăng nhập hiện tại")
                    .setMessage("Bạn có muốn đăng xuất khỏi thiết bị này không?")
                    .setNegativeButton("Hủy", null)
                    .setPositiveButton("Đăng xuất", (dialog, which) -> AuthHelper.logout(LoginSessionActivity.this))
                    .show());
        }

        if (btnManageMultiple != null) {
            btnManageMultiple.setOnClickListener(v -> revokeAllOtherSessionsOnBackend());
        }

        if (btnUnrecognizedResolution != null) {
            btnUnrecognizedResolution.setOnClickListener(v -> showUnrecognizedResolutionDialog());
        }
    }

    private void fetchSessionsFromBackend() {
        String authHeader = ApiClient.getAuthHeader(this);
        if (authHeader.isEmpty()) {
            applySessionList(new ArrayList<>());
            return;
        }

        ApiClient.getApiService().getSessions(authHeader).enqueue(new Callback<SessionsResponse>() {
            @Override
            public void onResponse(@NonNull Call<SessionsResponse> call, @NonNull Response<SessionsResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    applySessionList(response.body().getData());
                } else {
                    Toast.makeText(LoginSessionActivity.this, "Không thể tải danh sách phiên đăng nhập", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<SessionsResponse> call, @NonNull Throwable t) {
                Toast.makeText(LoginSessionActivity.this, "Lỗi kết nối máy chủ", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void applySessionList(List<SessionModel> sessions) {
        currentSession = null;
        otherSessions.clear();
        if (sessions != null) {
            for (SessionModel s : sessions) {
                if (s.isCurrent()) {
                    if (currentSession == null) currentSession = s;
                } else {
                    otherSessions.add(s);
                }
            }
        }
        updateSessionsUI();
    }

    private void revokeSessionOnBackend(String sessionId) {
        String authHeader = ApiClient.getAuthHeader(this);
        if (authHeader.isEmpty()) {
            Toast.makeText(this, "Vui lòng đăng nhập lại", Toast.LENGTH_SHORT).show();
            return;
        }

        RevokeSessionRequest request = new RevokeSessionRequest(sessionId);
        ApiClient.getApiService().revokeSession(authHeader, request).enqueue(new Callback<SessionsResponse>() {
            @Override
            public void onResponse(@NonNull Call<SessionsResponse> call, @NonNull Response<SessionsResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    applySessionList(response.body().getData());
                    Toast.makeText(LoginSessionActivity.this, "Đã đăng xuất phiên này thành công", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(LoginSessionActivity.this, "Không thể thu hồi phiên (mã " + response.code() + ")", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<SessionsResponse> call, @NonNull Throwable t) {
                Toast.makeText(LoginSessionActivity.this, "Lỗi kết nối máy chủ", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void revokeAllOtherSessionsOnBackend() {
        if (otherSessions.isEmpty()) {
            Toast.makeText(this, "Không có phiên đăng nhập nào khác", Toast.LENGTH_SHORT).show();
            return;
        }
        String authHeader = ApiClient.getAuthHeader(this);
        if (authHeader.isEmpty()) {
            Toast.makeText(this, "Vui lòng đăng nhập lại", Toast.LENGTH_SHORT).show();
            return;
        }

        List<String> toRevoke = new ArrayList<>();
        for (SessionModel s : otherSessions) {
            if (s.getId() != null) toRevoke.add(s.getId());
        }

        RevokeSessionRequest request = new RevokeSessionRequest(toRevoke);
        ApiClient.getApiService().revokeSession(authHeader, request).enqueue(new Callback<SessionsResponse>() {
            @Override
            public void onResponse(@NonNull Call<SessionsResponse> call, @NonNull Response<SessionsResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    applySessionList(response.body().getData());
                    Toast.makeText(LoginSessionActivity.this, "Đã đăng xuất tất cả các phiên khác", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(LoginSessionActivity.this, "Không thể đăng xuất các phiên khác", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<SessionsResponse> call, @NonNull Throwable t) {
                Toast.makeText(LoginSessionActivity.this, "Lỗi kết nối máy chủ", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateSessionsUI() {
        if (currentSession != null) {
            if (tvCurrentDeviceName != null && currentSession.getDeviceName() != null) {
                tvCurrentDeviceName.setText(currentSession.getDeviceName());
            }
            if (tvCurrentSessionStatus != null) {
                String statusLine = currentSession.getSubtitle() != null ? currentSession.getSubtitle() : "";
                if (currentSession.getLocation() != null && !currentSession.getLocation().isEmpty()
                        && !statusLine.contains(currentSession.getLocation())) {
                    statusLine = statusLine.isEmpty() ? currentSession.getLocation() : statusLine + " · " + currentSession.getLocation();
                }
                tvCurrentSessionStatus.setText(statusLine.isEmpty() ? getString(R.string.session_current_status) : statusLine);
            }
            if (tvCurrentSessionBadge != null) {
                String badge = currentSession.getStatusText();
                tvCurrentSessionBadge.setText((badge != null && !badge.isEmpty()) ? badge : getString(R.string.session_this_device));
            }
        }

        if (layoutOtherSessionsList == null) return;
        layoutOtherSessionsList.removeAllViews();

        if (otherSessions.isEmpty()) {
            if (tvNoOtherSessions != null) tvNoOtherSessions.setVisibility(View.VISIBLE);
            if (btnManageMultiple != null) btnManageMultiple.setVisibility(View.GONE);
            return;
        }

        if (tvNoOtherSessions != null) tvNoOtherSessions.setVisibility(View.GONE);
        if (btnManageMultiple != null) btnManageMultiple.setVisibility(View.VISIBLE);

        for (int i = 0; i < otherSessions.size(); i++) {
            layoutOtherSessionsList.addView(createSessionRow(otherSessions.get(i)));
            if (i < otherSessions.size() - 1) {
                layoutOtherSessionsList.addView(createDivider());
            }
        }
    }

    private View createSessionRow(SessionModel session) {
        LinearLayout card = new LinearLayout(this);
        card.setOrientation(LinearLayout.VERTICAL);
        int pad = dpToPx(16);
        card.setPadding(pad, pad, pad, pad);

        LinearLayout topRow = new LinearLayout(this);
        topRow.setOrientation(LinearLayout.HORIZONTAL);
        topRow.setGravity(Gravity.CENTER_VERTICAL);

        RelativeLayout iconCircle = new RelativeLayout(this);
        iconCircle.setLayoutParams(new LinearLayout.LayoutParams(dpToPx(40), dpToPx(40)));
        iconCircle.setBackgroundResource(R.drawable.bg_icon_circle_light);

        ImageView icon = new ImageView(this);
        RelativeLayout.LayoutParams iconParams = new RelativeLayout.LayoutParams(dpToPx(22), dpToPx(22));
        iconParams.addRule(RelativeLayout.CENTER_IN_PARENT);
        icon.setLayoutParams(iconParams);
        icon.setImageResource(R.drawable.ic_devices);
        icon.setColorFilter(Color.parseColor("#141B2B"));
        iconCircle.addView(icon);

        LinearLayout infoCol = new LinearLayout(this);
        infoCol.setOrientation(LinearLayout.VERTICAL);
        LinearLayout.LayoutParams infoParams = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f);
        infoParams.setMarginStart(dpToPx(12));
        infoCol.setLayoutParams(infoParams);

        TextView tvDevice = new TextView(this);
        tvDevice.setText(session.getDeviceName() != null ? session.getDeviceName() : "Thiết bị khác");
        tvDevice.setTextColor(Color.parseColor("#141B2B"));
        tvDevice.setTextSize(15f);
        tvDevice.setTypeface(null, Typeface.BOLD);

        String sub = session.getSubtitle() != null ? session.getSubtitle() : "";
        if (session.getLocation() != null && !session.getLocation().isEmpty() && !sub.contains(session.getLocation())) {
            sub = sub.isEmpty() ? session.getLocation() : sub + " · " + session.getLocation();
        }
        TextView tvSub = new TextView(this);
        tvSub.setText(sub);
        tvSub.setTextColor(Color.parseColor("#6B7280"));
        tvSub.setTextSize(12f);

        infoCol.addView(tvDevice);
        infoCol.addView(tvSub);

        topRow.addView(iconCircle);
        topRow.addView(infoCol);

        TextView btnRevoke = new TextView(this);
        LinearLayout.LayoutParams revokeParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        revokeParams.topMargin = dpToPx(12);
        btnRevoke.setLayoutParams(revokeParams);
        btnRevoke.setText(R.string.session_logout_action);
        btnRevoke.setTextColor(Color.parseColor("#C5221F"));
        btnRevoke.setTextSize(14f);
        btnRevoke.setTypeface(null, Typeface.BOLD);
        btnRevoke.setClickable(true);
        btnRevoke.setFocusable(true);
        btnRevoke.setOnClickListener(v -> revokeSessionOnBackend(session.getId()));

        card.addView(topRow);
        card.addView(btnRevoke);
        return card;
    }

    private View createDivider() {
        View divider = new View(this);
        divider.setBackgroundColor(Color.parseColor("#E9EBF4"));
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, dpToPx(1));
        params.setMarginStart(dpToPx(16));
        params.setMarginEnd(dpToPx(16));
        divider.setLayoutParams(params);
        return divider;
    }

    private int dpToPx(int dp) {
        float density = getResources().getDisplayMetrics().density;
        return Math.round((float) dp * density);
    }

    private void showUnrecognizedResolutionDialog() {
        boolean isGoogleLogin = getSharedPreferences("DishcisionPrefs", MODE_PRIVATE).getBoolean("is_google_login", false);
        AlertDialog.Builder builder = new AlertDialog.Builder(this)
                .setTitle("Hướng dẫn xử lý thiết bị lạ")
                .setMessage(isGoogleLogin
                        ? "1. Chọn 'Đăng xuất phiên này' đối với thiết bị không nhận ra.\n2. Kiểm tra bảo mật tài khoản Google và bật Xác thực hai yếu tố (2FA)."
                        : "1. Chọn 'Đăng xuất phiên này' đối với thiết bị không nhận ra.\n2. Đổi mật khẩu tài khoản ngay lập tức để bảo vệ tài khoản.\n3. Bật Xác thực hai yếu tố (2FA) để tăng cường bảo mật.")
                .setNegativeButton("Đóng", null);

        if (!isGoogleLogin) {
            builder.setPositiveButton("Đổi mật khẩu", (dialog, which) -> {
                Intent intent = new Intent(LoginSessionActivity.this, ChangePasswordActivity.class);
                startActivity(intent);
            });
        }
        builder.show();
    }
}
