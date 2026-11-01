package com.example.dishcision;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.dishcision.network.ApiClient;
import com.example.dishcision.network.TokenManager;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class NotificationsActivity extends AppCompatActivity {

    private ImageView btnBack;
    private ImageView btnHeaderBell;
    private TextView btnMarkAllRead;
    private TextView chipAll;
    private LinearLayout chipUnread;

    private LinearLayout itemNotify1;
    private LinearLayout itemNotify2;
    private LinearLayout itemNotify3;
    private View dotUnread1;

    private TokenManager tokenManager;
    private boolean isUnread1 = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_notifications);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(android.R.id.content), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        tokenManager = new TokenManager(this);

        initViews();
        setupListeners();
        fetchNotificationsFromApi();
    }

    private void initViews() {
        btnBack = findViewById(R.id.btnBack);
        btnHeaderBell = findViewById(R.id.btnHeaderBell);
        btnMarkAllRead = findViewById(R.id.btnMarkAllRead);
        chipAll = findViewById(R.id.chipAll);
        chipUnread = findViewById(R.id.chipUnread);

        itemNotify1 = findViewById(R.id.itemNotify1);
        itemNotify2 = findViewById(R.id.itemNotify2);
        itemNotify3 = findViewById(R.id.itemNotify3);
        dotUnread1 = findViewById(R.id.dotUnread1);
    }

    private void setupListeners() {
        if (btnBack != null) {
            btnBack.setOnClickListener(v -> finish());
        }

        if (btnHeaderBell != null) {
            btnHeaderBell.setOnClickListener(v ->
                Toast.makeText(this, "Thông báo ứng dụng Dishcision", Toast.LENGTH_SHORT).show()
            );
        }

        if (btnMarkAllRead != null) {
            btnMarkAllRead.setOnClickListener(v -> {
                isUnread1 = false;
                if (dotUnread1 != null) {
                    dotUnread1.setVisibility(View.GONE);
                }
                if (itemNotify1 != null) {
                    itemNotify1.setBackgroundColor(0xFFFFFFFF);
                }
                markNotificationReadApi("notify_1");
                Toast.makeText(this, "Đã đánh dấu tất cả là đã đọc", Toast.LENGTH_SHORT).show();
            });
        }

        if (chipAll != null) {
            chipAll.setOnClickListener(v -> {
                chipAll.setBackgroundResource(R.drawable.bg_button_light_blue);
                chipUnread.setBackgroundResource(0);
                if (itemNotify1 != null) itemNotify1.setVisibility(View.VISIBLE);
                if (itemNotify2 != null) itemNotify2.setVisibility(View.VISIBLE);
                if (itemNotify3 != null) itemNotify3.setVisibility(View.VISIBLE);
                Toast.makeText(this, "Hiển thị tất cả thông báo", Toast.LENGTH_SHORT).show();
            });
        }

        if (chipUnread != null) {
            chipUnread.setOnClickListener(v -> {
                chipUnread.setBackgroundResource(R.drawable.bg_button_light_blue);
                chipAll.setBackgroundResource(0);
                if (itemNotify1 != null) itemNotify1.setVisibility(isUnread1 ? View.VISIBLE : View.GONE);
                if (itemNotify2 != null) itemNotify2.setVisibility(View.GONE);
                if (itemNotify3 != null) itemNotify3.setVisibility(View.GONE);
                Toast.makeText(this, "Lọc thông báo chưa đọc", Toast.LENGTH_SHORT).show();
            });
        }

        if (itemNotify1 != null) {
            itemNotify1.setOnClickListener(v -> {
                isUnread1 = false;
                if (dotUnread1 != null) {
                    dotUnread1.setVisibility(View.GONE);
                }
                if (itemNotify1 != null) {
                    itemNotify1.setBackgroundColor(0xFFFFFFFF);
                }
                markNotificationReadApi("notify_1");
                Intent intent = new Intent(NotificationsActivity.this, MyReviewsActivity.class);
                startActivity(intent);
            });
        }

        if (itemNotify2 != null) {
            itemNotify2.setOnClickListener(v -> {
                Intent intent = new Intent(NotificationsActivity.this, MyReviewsActivity.class);
                startActivity(intent);
            });
        }

        if (itemNotify3 != null) {
            itemNotify3.setOnClickListener(v -> {
                Intent intent = new Intent(NotificationsActivity.this, ChatActivity.class);
                intent.putExtra("CHAT_NAME", "Bếp Nhà Lục Nam");
                startActivity(intent);
            });
        }
    }

    private void fetchNotificationsFromApi() {
        ApiClient.getApiService().getNotifications(tokenManager.getFormattedToken()).enqueue(new Callback<Object>() {
            @Override
            public void onResponse(Call<Object> call, Response<Object> response) {}

            @Override
            public void onFailure(Call<Object> call, Throwable t) {}
        });
    }

    private void markNotificationReadApi(String notificationId) {
        ApiClient.getApiService().markNotificationRead(tokenManager.getFormattedToken(), notificationId).enqueue(new Callback<Object>() {
            @Override
            public void onResponse(Call<Object> call, Response<Object> response) {}

            @Override
            public void onFailure(Call<Object> call, Throwable t) {}
        });
    }
}
