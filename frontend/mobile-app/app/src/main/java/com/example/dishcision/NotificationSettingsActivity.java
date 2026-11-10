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

import com.example.dishcision.network.NotificationPrefs;

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

        loadSwitch(switchPush, NotificationPrefs.PUSH);
        loadSwitch(switchEmail, NotificationPrefs.EMAIL_PROMOS);
        loadSwitch(switchInteractions, NotificationPrefs.INTERACTIONS);
        loadSwitch(switchMessages, NotificationPrefs.MESSAGES);
        loadSwitch(switchFollowing, NotificationPrefs.FOLLOWING);
        loadSwitch(switchPromotionalOffers, NotificationPrefs.OFFERS);

        // Category switches only matter while push notifications are on
        if (switchPush != null) {
            updateCategoryEnabled(switchPush.isChecked());
            switchPush.setOnCheckedChangeListener((b, isChecked) -> updateCategoryEnabled(isChecked));
        }

        if (btnSaveNotificationSettings != null) {
            btnSaveNotificationSettings.setOnClickListener(v -> {
                saveSwitch(switchPush, NotificationPrefs.PUSH);
                saveSwitch(switchEmail, NotificationPrefs.EMAIL_PROMOS);
                saveSwitch(switchInteractions, NotificationPrefs.INTERACTIONS);
                saveSwitch(switchMessages, NotificationPrefs.MESSAGES);
                saveSwitch(switchFollowing, NotificationPrefs.FOLLOWING);
                saveSwitch(switchPromotionalOffers, NotificationPrefs.OFFERS);
                Toast.makeText(this, "Đã lưu cài đặt thông báo", Toast.LENGTH_SHORT).show();
                finish();
            });
        }
    }

    private void loadSwitch(SwitchCompat sw, String key) {
        if (sw != null) sw.setChecked(NotificationPrefs.get(this, key));
    }

    private void saveSwitch(SwitchCompat sw, String key) {
        if (sw != null) NotificationPrefs.set(this, key, sw.isChecked());
    }

    private void updateCategoryEnabled(boolean pushOn) {
        SwitchCompat[] categories = {switchInteractions, switchMessages, switchFollowing, switchPromotionalOffers};
        for (SwitchCompat sw : categories) {
            if (sw != null) {
                sw.setEnabled(pushOn);
                sw.setAlpha(pushOn ? 1f : 0.4f);
            }
        }
    }
}
