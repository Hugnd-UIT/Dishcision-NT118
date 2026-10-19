package com.example.dishcision;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class NotificationSettingsActivity extends AppCompatActivity {

    private ImageView btnBack;
    private SwitchCompat switchPush;
    private SwitchCompat switchEmail;
    private TextView btnDeviceSettingsLink;
    private SwitchCompat switchInteractions;
    private SwitchCompat switchMessages;
    private SwitchCompat switchFollowing;
    private SwitchCompat switchPromotionalOffers;
    private LinearLayout btnSaveNotificationSettings;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_notification_settings);

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
        switchPush = findViewById(R.id.switchPush);
        switchEmail = findViewById(R.id.switchEmail);
        btnDeviceSettingsLink = findViewById(R.id.btnDeviceSettingsLink);
        switchInteractions = findViewById(R.id.switchInteractions);
        switchMessages = findViewById(R.id.switchMessages);
        switchFollowing = findViewById(R.id.switchFollowing);
        switchPromotionalOffers = findViewById(R.id.switchPromotionalOffers);
        btnSaveNotificationSettings = findViewById(R.id.btnSaveNotificationSettings);
    }

    private void setupListeners() {
        if (btnBack != null) {
            btnBack.setOnClickListener(v -> finish());
        }

        if (btnDeviceSettingsLink != null) {
            btnDeviceSettingsLink.setOnClickListener(v -> {
                Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                Uri uri = Uri.fromParts("package", getPackageName(), null);
                intent.setData(uri);
                try {
                    startActivity(intent);
                } catch (Exception e) {
                    Toast.makeText(this, "Mở Cài đặt hệ thống ứng dụng", Toast.LENGTH_SHORT).show();
                }
            });
        }

        setupSwitchListener(switchPush, "Thông báo đẩy");
        setupSwitchListener(switchEmail, "Email nhận tin khuyến mãi");
        setupSwitchListener(switchInteractions, "Tương tác trong ứng dụng");
        setupSwitchListener(switchMessages, "Tin nhắn trò chuyện");
        setupSwitchListener(switchFollowing, "Quán đang theo dõi");
        setupSwitchListener(switchPromotionalOffers, "Gợi ý ưu đãi");

        if (btnSaveNotificationSettings != null) {
            btnSaveNotificationSettings.setOnClickListener(v -> {
                Toast.makeText(this, "Đã lưu cài đặt thông báo thành công!", Toast.LENGTH_SHORT).show();
                finish();
            });
        }
    }

    private void setupSwitchListener(SwitchCompat switchCompat, String title) {
        if (switchCompat != null) {
            switchCompat.setOnCheckedChangeListener((buttonView, isChecked) -> {
                String status = isChecked ? "Bật" : "Tắt";
                Toast.makeText(this, title + ": " + status, Toast.LENGTH_SHORT).show();
            });
        }
    }
}
