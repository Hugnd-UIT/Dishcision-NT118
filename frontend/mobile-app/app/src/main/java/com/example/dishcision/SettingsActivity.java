package com.example.dishcision;

import android.Manifest;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.dishcision.network.AuthHelper;
import com.example.dishcision.network.CurrentUserBinder;
import com.example.dishcision.network.ImageLoader;
import com.example.dishcision.network.NotificationPrefs;

public class SettingsActivity extends AppCompatActivity {

    private static final int REQ_POST_NOTIFICATIONS = 4101;

    private ImageView imgAvatar;
    private TextView tvUserName;
    private TextView tvUserHandle;
    private SwitchCompat switchPush;
    private SwitchCompat switchEmailPromos;

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

        imgAvatar = findViewById(R.id.imgAvatar);
        tvUserName = findViewById(R.id.tvUserName);
        tvUserHandle = findViewById(R.id.tvUserHandle);
        switchPush = findViewById(R.id.switchPush);
        switchEmailPromos = findViewById(R.id.switchEmailPromos);

        boolean isGoogleLogin = getSharedPreferences("DishcisionPrefs", MODE_PRIVATE).getBoolean("is_google_login", false);
        if (isGoogleLogin) {
            // Google accounts have no Dishcision password to change
            setVisible(R.id.btnChangePassword, false);
        }

        setupNavigationRows();
        setupNotificationSwitches();
        setupActionRows();
        showAppVersion();
    }

    @Override
    protected void onResume() {
        super.onResume();
        CurrentUserBinder.bind(this, imgAvatar, tvUserName, tvUserHandle);
        // Values may have been changed on the Notification settings screen
        bindSwitchSilently(switchPush, NotificationPrefs.get(this, NotificationPrefs.PUSH) && systemAllowsNotifications());
        bindSwitchSilently(switchEmailPromos, NotificationPrefs.get(this, NotificationPrefs.EMAIL_PROMOS));
    }

    // ── Rows that open another screen ─────────────

    private void setupNavigationRows() {
        View back = findViewById(R.id.btnBack);
        if (back != null) back.setOnClickListener(v -> finish());

        // Account
        openOnClick(R.id.cardUserProfile, EditProfileActivity.class);
        openOnClick(R.id.btnAccountManagement, EditProfileActivity.class);
        openOnClick(R.id.btnSecurityLogin, SecurityLoginActivity.class);
        openOnClick(R.id.btnChangePassword, ChangePasswordActivity.class);
        openOnClick(R.id.btnPrivacy, PrivacySettingsActivity.class);
        openOnClick(R.id.btnBlockedAccounts, BlockedAccountsActivity.class);
        openOnClick(R.id.btnRestrictedAccounts, RestrictedAccountsActivity.class);

        // Your activity
        openOnClick(R.id.btnSavedPosts, FavoriteDishesActivity.class);
        openOnClick(R.id.btnFavoriteRestaurants, FavoriteRestaurantsActivity.class);
        openOnClick(R.id.btnMyReviews, MyReviewsActivity.class);
        openOnClick(R.id.btnSavedOffers, SavedOffersActivity.class);
        openOnClick(R.id.btnArchive, ArchiveActivity.class);
        openOnClick(R.id.btnActivityLog, ActivityLogActivity.class);

        // Food preferences
        openOnClick(R.id.btnTastePreferences, TastePreferencesActivity.class);
        openOnClick(R.id.btnDeliveryAddresses, DeliveryAddressesActivity.class);

        // Notifications
        openOnClick(R.id.btnNotificationSettings, NotificationSettingsActivity.class);

        // Help & info
        openOnClick(R.id.btnHelpCenter, HelpCenterActivity.class);
        openOnClick(R.id.btnHelpInbox, HelpInboxActivity.class);
        openOnClick(R.id.btnTerms, TermsOfServiceActivity.class);

        // System
        openOnClick(R.id.btnDeleteAccount, DeleteAccountActivity.class);
    }

    private void openOnClick(int viewId, Class<?> target) {
        View view = findViewById(viewId);
        if (view != null) {
            view.setOnClickListener(v -> startActivity(new Intent(this, target)));
        }
    }

    // ── Notification switches ─────────────────────

    private void setupNotificationSwitches() {
        if (switchPush != null) {
            switchPush.setOnCheckedChangeListener((b, isChecked) -> onPushToggled(isChecked));
        }
        if (switchEmailPromos != null) {
            switchEmailPromos.setOnCheckedChangeListener((b, isChecked) -> {
                NotificationPrefs.set(this, NotificationPrefs.EMAIL_PROMOS, isChecked);
                Toast.makeText(this, isChecked ? "Đã bật email nhận tin & khuyến mãi" : "Đã tắt email nhận tin & khuyến mãi",
                        Toast.LENGTH_SHORT).show();
            });
        }
        // Tapping anywhere on the row flips its switch
        View rowPush = findViewById(R.id.rowPush);
        if (rowPush != null && switchPush != null) rowPush.setOnClickListener(v -> switchPush.toggle());
        View rowEmail = findViewById(R.id.rowEmailPromos);
        if (rowEmail != null && switchEmailPromos != null) rowEmail.setOnClickListener(v -> switchEmailPromos.toggle());
    }

    private void onPushToggled(boolean enable) {
        if (!enable) {
            NotificationPrefs.set(this, NotificationPrefs.PUSH, false);
            Toast.makeText(this, "Đã tắt thông báo đẩy", Toast.LENGTH_SHORT).show();
            return;
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU
                && ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.POST_NOTIFICATIONS}, REQ_POST_NOTIFICATIONS);
            return;
        }
        if (!NotificationManagerCompat.from(this).areNotificationsEnabled()) {
            bindSwitchSilently(switchPush, false);
            showOpenSystemNotificationSettings();
            return;
        }
        NotificationPrefs.set(this, NotificationPrefs.PUSH, true);
        Toast.makeText(this, "Đã bật thông báo đẩy", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode != REQ_POST_NOTIFICATIONS) return;
        boolean granted = grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED;
        if (granted) {
            NotificationPrefs.set(this, NotificationPrefs.PUSH, true);
            Toast.makeText(this, "Đã bật thông báo đẩy", Toast.LENGTH_SHORT).show();
        } else {
            NotificationPrefs.set(this, NotificationPrefs.PUSH, false);
            bindSwitchSilently(switchPush, false);
            showOpenSystemNotificationSettings();
        }
    }

    private boolean systemAllowsNotifications() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU
                && ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                != PackageManager.PERMISSION_GRANTED) {
            return false;
        }
        return NotificationManagerCompat.from(this).areNotificationsEnabled();
    }

    private void showOpenSystemNotificationSettings() {
        new AlertDialog.Builder(this)
                .setTitle("Thông báo đang bị tắt")
                .setMessage("Dishcision chưa được phép gửi thông báo. Mở cài đặt hệ thống để bật?")
                .setNegativeButton("Để sau", null)
                .setPositiveButton("Mở cài đặt", (d, w) -> {
                    Intent intent;
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        intent = new Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS)
                                .putExtra(Settings.EXTRA_APP_PACKAGE, getPackageName());
                    } else {
                        intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                                Uri.fromParts("package", getPackageName(), null));
                    }
                    startActivity(intent);
                })
                .show();
    }

    /** Sets a switch without firing its listener. */
    private void bindSwitchSilently(SwitchCompat sw, boolean checked) {
        if (sw == null) return;
        sw.setOnCheckedChangeListener(null);
        sw.setChecked(checked);
        setupNotificationSwitches();
    }

    // ── Rows that perform an action ───────────────

    private void setupActionRows() {
        View rate = findViewById(R.id.btnAppRating);
        if (rate != null) rate.setOnClickListener(v -> openStoreListing());

        View share = findViewById(R.id.btnShareApp);
        if (share != null) share.setOnClickListener(v -> shareApp());

        View clearCache = findViewById(R.id.btnClearCache);
        if (clearCache != null) clearCache.setOnClickListener(v -> confirmClearCache());

        View logout = findViewById(R.id.btnLogout);
        if (logout != null) logout.setOnClickListener(v -> confirmLogout());
    }

    private void openStoreListing() {
        String pkg = getPackageName();
        try {
            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + pkg)));
        } catch (ActivityNotFoundException e) {
            startActivity(new Intent(Intent.ACTION_VIEW,
                    Uri.parse("https://play.google.com/store/apps/details?id=" + pkg)));
        }
    }

    private void shareApp() {
        Intent send = new Intent(Intent.ACTION_SEND);
        send.setType("text/plain");
        send.putExtra(Intent.EXTRA_SUBJECT, "Dishcision");
        send.putExtra(Intent.EXTRA_TEXT,
                "Hôm nay ăn gì? Thử Dishcision để chọn món và khám phá quán ngon: "
                        + "https://play.google.com/store/apps/details?id=" + getPackageName());
        startActivity(Intent.createChooser(send, "Chia sẻ Dishcision"));
    }

    private void confirmClearCache() {
        new AlertDialog.Builder(this)
                .setTitle("Xóa bộ nhớ đệm?")
                .setMessage("Ảnh đã tải sẽ được tải lại khi bạn xem. Tài khoản và cài đặt của bạn không bị ảnh hưởng.")
                .setNegativeButton("Hủy", null)
                .setPositiveButton("Xóa", (d, w) -> {
                    int freedKb = ImageLoader.clearCache();
                    deleteDirContents(getCacheDir());
                    String size = freedKb >= 1024 ? (freedKb / 1024) + " MB" : freedKb + " KB";
                    Toast.makeText(this, "Đã xóa bộ nhớ đệm (" + size + " ảnh)", Toast.LENGTH_SHORT).show();
                })
                .show();
    }

    private static void deleteDirContents(java.io.File dir) {
        if (dir == null) return;
        java.io.File[] children = dir.listFiles();
        if (children == null) return;
        for (java.io.File child : children) {
            if (child.isDirectory()) deleteDirContents(child);
            //noinspection ResultOfMethodCallIgnored
            child.delete();
        }
    }

    private void confirmLogout() {
        String name = tvUserName != null ? tvUserName.getText().toString() : "";
        new AlertDialog.Builder(this)
                .setTitle("Đăng xuất?")
                .setMessage(name.isEmpty() ? "Bạn có chắc muốn đăng xuất khỏi Dishcision?"
                        : "Bạn có chắc muốn đăng xuất tài khoản " + name + "?")
                .setNegativeButton("Hủy", null)
                .setPositiveButton("Đăng xuất", (d, w) -> AuthHelper.logout(this))
                .show();
    }

    private void showAppVersion() {
        TextView tvVersion = findViewById(R.id.tvAppVersion);
        if (tvVersion == null) return;
        try {
            PackageInfo info = getPackageManager().getPackageInfo(getPackageName(), 0);
            long code = Build.VERSION.SDK_INT >= Build.VERSION_CODES.P ? info.getLongVersionCode() : info.versionCode;
            tvVersion.setText("Phiên bản " + info.versionName + " (Build " + code + ")");
        } catch (PackageManager.NameNotFoundException e) {
            tvVersion.setText("Phiên bản —");
        }
    }

    private void setVisible(int viewId, boolean visible) {
        View view = findViewById(viewId);
        if (view != null) view.setVisibility(visible ? View.VISIBLE : View.GONE);
    }
}
