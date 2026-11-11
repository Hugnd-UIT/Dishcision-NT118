package com.example.dishcision;

import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.View;
import android.widget.FrameLayout;
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
import com.example.dishcision.network.NotificationResponse;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class NotificationsActivity extends AppCompatActivity {

    private ImageView btnBack;
    private ImageView btnHeaderBell;
    private TextView btnMarkAllRead;
    private TextView chipAll;
    private LinearLayout chipUnread;
    private TextView tvChipUnreadLabel;
    private TextView tvUnreadCount;
    private LinearLayout layoutNotificationsContainer;
    private TextView tvEmptyNotifications;

    private final List<NotificationResponse.NotificationItem> notifications = new ArrayList<>();
    private boolean showOnlyUnread = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_notifications);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main_notifications), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        ApiClient.init(this);
        initViews();
        setupListeners();
    }

    @Override
    protected void onResume() {
        super.onResume();
        fetchNotificationsFromApi();
    }

    private void initViews() {
        btnBack = findViewById(R.id.btnBack);
        btnHeaderBell = findViewById(R.id.btnHeaderBell);
        btnMarkAllRead = findViewById(R.id.btnMarkAllRead);
        chipAll = findViewById(R.id.chipAll);
        chipUnread = findViewById(R.id.chipUnread);
        tvChipUnreadLabel = findViewById(R.id.tvChipUnreadLabel);
        tvUnreadCount = findViewById(R.id.tvUnreadCount);
        layoutNotificationsContainer = findViewById(R.id.layoutNotificationsContainer);
        tvEmptyNotifications = findViewById(R.id.tvEmptyNotifications);
    }

    private void setupListeners() {
        if (btnBack != null) btnBack.setOnClickListener(v -> finish());

        if (btnHeaderBell != null) {
            btnHeaderBell.setOnClickListener(v -> fetchNotificationsFromApi());
        }

        if (btnMarkAllRead != null) {
            btnMarkAllRead.setOnClickListener(v -> markAllAsRead());
        }

        if (chipAll != null) {
            chipAll.setOnClickListener(v -> {
                showOnlyUnread = false;
                updateFilterChips();
                renderNotifications();
            });
        }

        if (chipUnread != null) {
            chipUnread.setOnClickListener(v -> {
                showOnlyUnread = true;
                updateFilterChips();
                renderNotifications();
            });
        }
    }

    private void updateFilterChips() {
        if (chipAll != null) {
            chipAll.setBackgroundResource(!showOnlyUnread ? R.drawable.bg_button_light_blue : 0);
            chipAll.setTypeface(null, !showOnlyUnread ? Typeface.BOLD : Typeface.NORMAL);
        }
        if (chipUnread != null) {
            chipUnread.setBackgroundResource(showOnlyUnread ? R.drawable.bg_button_light_blue : 0);
        }
        if (tvChipUnreadLabel != null) {
            tvChipUnreadLabel.setTypeface(null, showOnlyUnread ? Typeface.BOLD : Typeface.NORMAL);
        }
    }

    private void fetchNotificationsFromApi() {
        String authHeader = ApiClient.getAuthHeader(this);
        if (authHeader.isEmpty()) {
            notifications.clear();
            updateUnreadBadge();
            renderNotifications();
            return;
        }

        ApiClient.getApiService().getNotifications(authHeader).enqueue(new Callback<NotificationResponse>() {
            @Override
            public void onResponse(@NonNull Call<NotificationResponse> call, @NonNull Response<NotificationResponse> response) {
                notifications.clear();
                if (response.isSuccessful() && response.body() != null && response.body().data != null) {
                    notifications.addAll(response.body().data);
                }
                updateUnreadBadge();
                renderNotifications();
            }

            @Override
            public void onFailure(@NonNull Call<NotificationResponse> call, @NonNull Throwable t) {
                Toast.makeText(NotificationsActivity.this, "Không thể tải thông báo: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateUnreadBadge() {
        int unread = 0;
        for (NotificationResponse.NotificationItem item : notifications) {
            if (!item.isRead) unread++;
        }
        if (tvUnreadCount != null) {
            if (unread > 0) {
                tvUnreadCount.setVisibility(View.VISIBLE);
                tvUnreadCount.setText(String.valueOf(unread));
            } else {
                tvUnreadCount.setVisibility(View.GONE);
            }
        }
    }

    private void renderNotifications() {
        if (layoutNotificationsContainer == null) return;
        layoutNotificationsContainer.removeAllViews();

        List<NotificationResponse.NotificationItem> filtered = new ArrayList<>();
        for (NotificationResponse.NotificationItem item : notifications) {
            if (showOnlyUnread && item.isRead) continue;
            filtered.add(item);
        }

        if (filtered.isEmpty()) {
            if (tvEmptyNotifications != null) {
                tvEmptyNotifications.setText(showOnlyUnread ? "Không có thông báo chưa đọc" : "Chưa có thông báo nào");
                tvEmptyNotifications.setVisibility(View.VISIBLE);
            }
            return;
        }
        if (tvEmptyNotifications != null) tvEmptyNotifications.setVisibility(View.GONE);

        for (int i = 0; i < filtered.size(); i++) {
            NotificationResponse.NotificationItem item = filtered.get(i);
            layoutNotificationsContainer.addView(buildNotificationRow(item));
            if (i < filtered.size() - 1) {
                View divider = new View(this);
                divider.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, dpToPx(1)));
                divider.setBackgroundColor(Color.parseColor("#E9EBF4"));
                layoutNotificationsContainer.addView(divider);
            }
        }
    }

    private View buildNotificationRow(NotificationResponse.NotificationItem item) {
        LinearLayout row = new LinearLayout(this);
        row.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
        row.setBackgroundColor(item.isRead ? Color.WHITE : Color.parseColor("#FFF6F2"));
        row.setClickable(true);
        row.setFocusable(true);
        row.setGravity(Gravity.CENTER_VERTICAL);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setPadding(dpToPx(16), dpToPx(14), dpToPx(16), dpToPx(14));

        FrameLayout avatarFrame = new FrameLayout(this);
        avatarFrame.setLayoutParams(new LinearLayout.LayoutParams(dpToPx(46), dpToPx(46)));

        ImageView imgCircle = new ImageView(this);
        imgCircle.setLayoutParams(new FrameLayout.LayoutParams(dpToPx(46), dpToPx(46)));
        imgCircle.setBackgroundResource(R.drawable.bg_avatar_circle);
        imgCircle.setPadding(dpToPx(12), dpToPx(12), dpToPx(12), dpToPx(12));
        imgCircle.setImageResource(resolveTypeIcon(item.type));
        imgCircle.setColorFilter(Color.parseColor("#FF5A36"));
        avatarFrame.addView(imgCircle);

        row.addView(avatarFrame);

        LinearLayout textCol = new LinearLayout(this);
        LinearLayout.LayoutParams colParams = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f);
        colParams.setMarginStart(dpToPx(12));
        textCol.setLayoutParams(colParams);
        textCol.setOrientation(LinearLayout.VERTICAL);

        TextView tvMessage = new TextView(this);
        tvMessage.setText(item.message != null ? item.message : "");
        tvMessage.setTextColor(Color.parseColor("#141B2B"));
        tvMessage.setTextSize(14f);
        tvMessage.setLineSpacing(0f, 1.15f);
        if (!item.isRead) tvMessage.setTypeface(null, Typeface.BOLD);
        textCol.addView(tvMessage);

        String dateStr = formatDate(item.createdAt);
        if (!dateStr.isEmpty()) {
            TextView tvTime = new TextView(this);
            LinearLayout.LayoutParams timeParams = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            timeParams.topMargin = dpToPx(4);
            tvTime.setLayoutParams(timeParams);
            tvTime.setText(dateStr);
            tvTime.setTextColor(Color.parseColor("#8E7570"));
            tvTime.setTextSize(12f);
            textCol.addView(tvTime);
        }

        row.addView(textCol);

        if (!item.isRead) {
            View dot = new View(this);
            LinearLayout.LayoutParams dotParams = new LinearLayout.LayoutParams(dpToPx(10), dpToPx(10));
            dotParams.setMarginStart(dpToPx(8));
            dot.setLayoutParams(dotParams);
            dot.setBackgroundResource(R.drawable.bg_unread_badge);
            row.addView(dot);
        }

        row.setOnClickListener(v -> handleNotificationClick(item));
        return row;
    }

    private int resolveTypeIcon(String type) {
        if (type == null) return R.drawable.ic_notification;
        switch (type) {
            case "Like":
                return R.drawable.ic_heart_outline;
            case "Comment":
                return R.drawable.ic_comment;
            case "Follow":
                return R.drawable.ic_user;
            default:
                return R.drawable.ic_notification;
        }
    }

    private void handleNotificationClick(NotificationResponse.NotificationItem item) {
        if (!item.isRead) {
            item.isRead = true;
            updateUnreadBadge();
            renderNotifications();
            markNotificationReadApi(String.valueOf(item.notificationId));
        }

        if ("Follow".equals(item.type)) {
            startActivity(new Intent(this, FriendsActivity.class));
        } else if (item.relatedPostId != null && item.relatedPostId > 0) {
            Intent intent = new Intent(this, HomeActivity.class);
            intent.putExtra("POST_ID", item.relatedPostId);
            startActivity(intent);
        } else if (!TextUtils.isEmpty(item.message)) {
            new AlertDialog.Builder(this)
                    .setTitle("Thông báo")
                    .setMessage(item.message)
                    .setPositiveButton("Đóng", null)
                    .show();
        }
    }

    private void markAllAsRead() {
        String authHeader = ApiClient.getAuthHeader(this);
        if (authHeader.isEmpty()) return;

        int markedCount = 0;
        for (NotificationResponse.NotificationItem item : notifications) {
            if (!item.isRead) {
                item.isRead = true;
                markedCount++;
                markNotificationReadApi(String.valueOf(item.notificationId));
            }
        }
        updateUnreadBadge();
        renderNotifications();
        if (markedCount > 0) {
            Toast.makeText(this, "Đã đánh dấu tất cả là đã đọc", Toast.LENGTH_SHORT).show();
        }
    }

    private void markNotificationReadApi(String notificationId) {
        String authHeader = ApiClient.getAuthHeader(this);
        if (authHeader.isEmpty()) return;
        ApiClient.getApiService().markNotificationRead(authHeader, notificationId).enqueue(new Callback<Object>() {
            @Override
            public void onResponse(@NonNull Call<Object> call, @NonNull Response<Object> response) {}

            @Override
            public void onFailure(@NonNull Call<Object> call, @NonNull Throwable t) {}
        });
    }

    private String formatDate(String iso) {
        if (TextUtils.isEmpty(iso)) return "";
        if (iso.length() >= 16) return iso.substring(0, 10) + " " + iso.substring(11, 16);
        if (iso.length() >= 10) return iso.substring(0, 10);
        return iso;
    }

    private int dpToPx(int dp) {
        return Math.round(dp * getResources().getDisplayMetrics().density);
    }
}
