package com.example.dishcision;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class SettingsActivity extends AppCompatActivity {

    private ImageView btnBack;
    private LinearLayout btnAccountManagement;
    private LinearLayout btnChangePassword;
    private LinearLayout btnPrivacy;

    private LinearLayout btnTastePreferences;
    private LinearLayout btnDeliveryAddresses;

    private LinearLayout btnNotificationSettings;
    private SwitchCompat switchPush;
    private SwitchCompat switchEmailPromos;

    private LinearLayout btnHelpCenter;
    private LinearLayout btnHelpInbox;
    private LinearLayout btnTerms;
    private LinearLayout btnAppRating;

    private LinearLayout btnLogout;
    private LinearLayout btnDeleteAccount;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_settings);

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
        btnAccountManagement = findViewById(R.id.btnAccountManagement);
        btnChangePassword = findViewById(R.id.btnChangePassword);
        btnPrivacy = findViewById(R.id.btnPrivacy);

        btnTastePreferences = findViewById(R.id.btnTastePreferences);
        btnDeliveryAddresses = findViewById(R.id.btnDeliveryAddresses);

        btnNotificationSettings = findViewById(R.id.btnNotificationSettings);
        switchPush = findViewById(R.id.switchPush);
        switchEmailPromos = findViewById(R.id.switchEmailPromos);

        btnHelpCenter = findViewById(R.id.btnHelpCenter);
        btnHelpInbox = findViewById(R.id.btnHelpInbox);
        btnTerms = findViewById(R.id.btnTerms);
        btnAppRating = findViewById(R.id.btnAppRating);

        btnLogout = findViewById(R.id.btnLogout);
        btnDeleteAccount = findViewById(R.id.btnDeleteAccount);
    }

    private void setupListeners() {
        if (btnBack != null) {
            btnBack.setOnClickListener(v -> finish());
        }

        if (btnAccountManagement != null) {
            btnAccountManagement.setOnClickListener(v -> {
                Intent intent = new Intent(SettingsActivity.this, EditProfileActivity.class);
                startActivity(intent);
            });
        }

        if (btnChangePassword != null) {
            btnChangePassword.setOnClickListener(v -> {
                Intent intent = new Intent(SettingsActivity.this, ChangePasswordActivity.class);
                startActivity(intent);
            });
        }

        if (btnPrivacy != null) {
            btnPrivacy.setOnClickListener(v -> {
                Intent intent = new Intent(SettingsActivity.this, PrivacyPolicyActivity.class);
                startActivity(intent);
            });
        }

        if (btnTastePreferences != null) {
            btnTastePreferences.setOnClickListener(v -> {
                Intent intent = new Intent(SettingsActivity.this, TastePreferencesActivity.class);
                startActivity(intent);
            });
        }

        if (btnDeliveryAddresses != null) {
            btnDeliveryAddresses.setOnClickListener(v -> {
                Intent intent = new Intent(SettingsActivity.this, DeliveryAddressesActivity.class);
                startActivity(intent);
            });
        }

        if (btnNotificationSettings != null) {
            btnNotificationSettings.setOnClickListener(v -> {
                Intent intent = new Intent(SettingsActivity.this, NotificationSettingsActivity.class);
                startActivity(intent);
            });
        }

        if (switchPush != null) {
            switchPush.setOnCheckedChangeListener((buttonView, isChecked) -> {
                String status = isChecked ? "Bật" : "Tắt";
                Toast.makeText(this, "Thông báo đẩy: " + status, Toast.LENGTH_SHORT).show();
            });
        }

        if (switchEmailPromos != null) {
            switchEmailPromos.setOnCheckedChangeListener((buttonView, isChecked) -> {
                String status = isChecked ? "Bật" : "Tắt";
                Toast.makeText(this, "Email nhận tin: " + status, Toast.LENGTH_SHORT).show();
            });
        }

        if (btnHelpCenter != null) {
            btnHelpCenter.setOnClickListener(v -> {
                Intent intent = new Intent(SettingsActivity.this, HelpCenterActivity.class);
                startActivity(intent);
            });
        }

        if (btnHelpInbox != null) {
            btnHelpInbox.setOnClickListener(v -> {
                Intent intent = new Intent(SettingsActivity.this, HelpInboxActivity.class);
                startActivity(intent);
            });
        }

        if (btnTerms != null) {
            btnTerms.setOnClickListener(v -> {
                Intent intent = new Intent(SettingsActivity.this, TermsOfServiceActivity.class);
                startActivity(intent);
            });
        }

        if (btnAppRating != null) {
            btnAppRating.setOnClickListener(v ->
                Toast.makeText(this, "Cảm ơn bạn đã đánh giá ứng dụng Dishcision!", Toast.LENGTH_SHORT).show()
            );
        }

        if (btnLogout != null) {
            btnLogout.setOnClickListener(v -> {
                Toast.makeText(this, "Đã đăng xuất tài khoản", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(SettingsActivity.this, LoginActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                finish();
            });
        }

        if (btnDeleteAccount != null) {
            btnDeleteAccount.setOnClickListener(v -> {
                Intent intent = new Intent(SettingsActivity.this, DeleteAccountActivity.class);
                startActivity(intent);
            });
        }
    }
}
