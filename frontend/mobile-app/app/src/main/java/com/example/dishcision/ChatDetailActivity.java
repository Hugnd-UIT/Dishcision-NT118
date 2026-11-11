package com.example.dishcision;

import android.content.Intent;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.dishcision.network.AccountActionRequest;
import com.example.dishcision.network.AccountsListResponse;
import com.example.dishcision.network.ApiClient;
import com.example.dishcision.network.ChatMessagesResponse;
import com.example.dishcision.network.ImageLoader;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ChatDetailActivity extends AppCompatActivity {

    private ImageView btnBack;
    private ImageView btnShare;
    private ImageView btnMoreOptions;
    private ImageView btnProfileUser;
    private ImageView imgDetailAvatar;
    private TextView tvContactName;
    private TextView tvContactSubtitle;

    private LinearLayout btnViewProfile;
    private LinearLayout btnMuteNotifications;
    private TextView tvMuteLabel;
    private LinearLayout btnSearchChat;

    private LinearLayout btnPinnedMessages;
    private TextView tvPinnedCountDetail;
    private LinearLayout btnChangeTheme;
    private TextView tvThemeSummary;
    private View viewThemeSwatch;
    private LinearLayout btnChangeEmoji;
    private TextView tvEmojiSummary;
    private LinearLayout btnEditNickname;

    private LinearLayout btnSharedMedia;
    private TextView tvSharedMediaCount;
    private LinearLayout btnSharedFiles;
    private LinearLayout btnSharedLinks;
    private TextView tvSharedLinksCount;

    private int targetUserId = 0;
    private String contactName = "";
    private String avatarUrl = "";
    private String userRole = "";
    private ActivityResultLauncher<Intent> subScreenLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_chat_detail);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(android.R.id.content), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        targetUserId = getIntent().getIntExtra("USER_ID", 0);
        contactName = safeTrim(getIntent().getStringExtra("CHAT_NAME"));
        avatarUrl = safeTrim(getIntent().getStringExtra("AVATAR_URL"));
        userRole = safeTrim(getIntent().getStringExtra("USER_ROLE"));

        subScreenLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null
                            && result.getData().hasExtra(ChatActivity.EXTRA_SCROLL_TO_MSG_ID)) {
                        setResult(RESULT_OK, result.getData());
                        finish();
                    }
                }
        );

        initViews();
        setupListeners();
    }

    @Override
    protected void onResume() {
        super.onResume();
        refreshSummaryUi();
        fetchSharedCounts();
    }

    private void initViews() {
        btnBack = findViewById(R.id.btnBack);
        btnShare = findViewById(R.id.btnShare);
        btnMoreOptions = findViewById(R.id.btnMoreOptions);
        btnProfileUser = findViewById(R.id.btnProfileUser);
        imgDetailAvatar = findViewById(R.id.imgDetailAvatar);
        tvContactName = findViewById(R.id.tvContactName);
        tvContactSubtitle = findViewById(R.id.tvContactSubtitle);

        btnViewProfile = findViewById(R.id.btnViewProfile);
        btnMuteNotifications = findViewById(R.id.btnMuteNotifications);
        tvMuteLabel = findViewById(R.id.tvMuteLabel);
        btnSearchChat = findViewById(R.id.btnSearchChat);

        btnPinnedMessages = findViewById(R.id.btnPinnedMessages);
        tvPinnedCountDetail = findViewById(R.id.tvPinnedCountDetail);
        btnChangeTheme = findViewById(R.id.btnChangeTheme);
        tvThemeSummary = findViewById(R.id.tvThemeSummary);
        viewThemeSwatch = findViewById(R.id.viewThemeSwatch);
        btnChangeEmoji = findViewById(R.id.btnChangeEmoji);
        tvEmojiSummary = findViewById(R.id.tvEmojiSummary);
        btnEditNickname = findViewById(R.id.btnEditNickname);

        btnSharedMedia = findViewById(R.id.btnSharedMedia);
        tvSharedMediaCount = findViewById(R.id.tvSharedMediaCount);
        btnSharedFiles = findViewById(R.id.btnSharedFiles);
        btnSharedLinks = findViewById(R.id.btnSharedLinks);
        tvSharedLinksCount = findViewById(R.id.tvSharedLinksCount);

        if (imgDetailAvatar != null && !avatarUrl.isEmpty()) {
            ImageLoader.loadAvatar(imgDetailAvatar, avatarUrl);
        }
    }

    private void refreshSummaryUi() {
        String nick = ChatPrefsHelper.getPartnerNickname(this, targetUserId);
        String displayed = !nick.isEmpty() ? nick : (!contactName.isEmpty() ? contactName : "Thành viên");
        if (tvContactName != null) tvContactName.setText(displayed);

        if (tvContactSubtitle != null) {
            if (!nick.isEmpty() && !contactName.isEmpty()) {
                tvContactSubtitle.setText("Tên tài khoản: " + contactName);
            } else if ("RestaurantAccount".equals(userRole)) {
                tvContactSubtitle.setText("Đối tác quán ăn trên Dishcision");
            } else {
                tvContactSubtitle.setText("Cuộc trò chuyện trực tiếp trên Dishcision");
            }
        }

        boolean muted = ChatPrefsHelper.isMuted(this, targetUserId);
        if (tvMuteLabel != null) {
            tvMuteLabel.setText(muted ? "Bật thông báo" : "Tắt thông báo");
        }

        int pinCount = ChatPrefsHelper.getPinnedMessageIds(this, targetUserId).size();
        if (tvPinnedCountDetail != null) {
            tvPinnedCountDetail.setText(pinCount + "  ›");
        }

        if (tvThemeSummary != null) {
            tvThemeSummary.setText(ChatPrefsHelper.getThemeName(this, targetUserId));
        }
        if (viewThemeSwatch != null) {
            GradientDrawable swatch = new GradientDrawable();
            swatch.setShape(GradientDrawable.OVAL);
            swatch.setColor(ChatPrefsHelper.getThemeColor(this, targetUserId));
            viewThemeSwatch.setBackground(swatch);
        }
        if (tvEmojiSummary != null) {
            tvEmojiSummary.setText(ChatPrefsHelper.getQuickEmoji(this, targetUserId));
        }
    }

    private void fetchSharedCounts() {
        String auth = ApiClient.getAuthHeader(this);
        if (auth.isEmpty() || targetUserId <= 0) return;

        ApiClient.getApiService().getChatMessages(auth, targetUserId, 100).enqueue(new Callback<ChatMessagesResponse>() {
            @Override
            public void onResponse(@NonNull Call<ChatMessagesResponse> call, @NonNull Response<ChatMessagesResponse> response) {
                if (isFinishing() || isDestroyed()) return;
                if (response.isSuccessful() && response.body() != null && response.body().data != null) {
                    int mediaCount = 0;
                    int linkCount = 0;
                    List<ChatMessagesResponse.MessageItem> items = response.body().data;
                    for (ChatMessagesResponse.MessageItem m : items) {
                        if (m == null) continue;
                        if (m.mediaUrl != null && !m.mediaUrl.trim().isEmpty()) mediaCount++;
                        if (m.content != null && (m.content.contains("http://") || m.content.contains("https://"))) linkCount++;
                    }
                    if (tvSharedMediaCount != null) tvSharedMediaCount.setText(mediaCount + "  ›");
                    if (tvSharedLinksCount != null) tvSharedLinksCount.setText(linkCount + "  ›");
                }
            }

            @Override
            public void onFailure(@NonNull Call<ChatMessagesResponse> call, @NonNull Throwable t) {}
        });
    }

    private void setupListeners() {
        if (btnBack != null) btnBack.setOnClickListener(v -> finish());

        if (btnShare != null) {
            btnShare.setOnClickListener(v -> {
                String name = !contactName.isEmpty() ? contactName : "Thành viên Dishcision";
                String link = targetUserId > 0 ? "\n" + ApiClient.getServerRootUrl() + "/users/" + targetUserId : "";
                Intent share = new Intent(Intent.ACTION_SEND);
                share.setType("text/plain");
                share.putExtra(Intent.EXTRA_TEXT, "Kết nối với " + name + " trên Dishcision" + link);
                startActivity(Intent.createChooser(share, "Chia sẻ liên hệ"));
            });
        }

        if (btnMoreOptions != null) {
            btnMoreOptions.setOnClickListener(this::showMorePopup);
        }

        View.OnClickListener openProfile = v -> openUserProfile();
        if (btnProfileUser != null) btnProfileUser.setOnClickListener(openProfile);
        if (btnViewProfile != null) btnViewProfile.setOnClickListener(openProfile);

        if (btnMuteNotifications != null) {
            btnMuteNotifications.setOnClickListener(v -> {
                boolean next = !ChatPrefsHelper.isMuted(this, targetUserId);
                ChatPrefsHelper.setMuted(this, targetUserId, next);
                refreshSummaryUi();
                Toast.makeText(this, next ? "Đã tắt thông báo cuộc trò chuyện" : "Đã bật thông báo cuộc trò chuyện", Toast.LENGTH_SHORT).show();
            });
        }

        if (btnSearchChat != null) {
            btnSearchChat.setOnClickListener(v -> launchSubScreen(SearchInChatActivity.class, -1));
        }
        if (btnPinnedMessages != null) {
            btnPinnedMessages.setOnClickListener(v -> launchSubScreen(PinnedMessagesActivity.class, -1));
        }
        View.OnClickListener openTheme = v -> launchSubScreen(ChatThemeActivity.class, -1);
        if (btnChangeTheme != null) btnChangeTheme.setOnClickListener(openTheme);
        if (btnChangeEmoji != null) btnChangeEmoji.setOnClickListener(openTheme);

        if (btnEditNickname != null) {
            btnEditNickname.setOnClickListener(v -> launchSubScreen(ChatNicknamesActivity.class, -1));
        }

        if (btnSharedMedia != null) {
            btnSharedMedia.setOnClickListener(v -> launchSubScreen(SharedMediaActivity.class, 0));
        }
        if (btnSharedFiles != null) {
            btnSharedFiles.setOnClickListener(v -> launchSubScreen(SharedMediaActivity.class, 1));
        }
        if (btnSharedLinks != null) {
            btnSharedLinks.setOnClickListener(v -> launchSubScreen(SharedMediaActivity.class, 2));
        }
    }

    private void showMorePopup(View anchor) {
        PopupMenu popup = new PopupMenu(this, anchor);
        popup.getMenu().add(0, 1, 0, "Xem trang cá nhân");
        popup.getMenu().add(0, 2, 1, "Báo cáo tài khoản");
        popup.getMenu().add(0, 3, 2, "Chặn người dùng");
        popup.setOnMenuItemClickListener(item -> {
            int id = item.getItemId();
            if (id == 1) {
                openUserProfile();
            } else if (id == 2 && targetUserId > 0) {
                Intent report = new Intent(this, ReportContentActivity.class);
                report.putExtra("REPORTED_USER_ID", targetUserId);
                if (!contactName.isEmpty()) report.putExtra("TARGET_TITLE", contactName);
                startActivity(report);
            } else if (id == 3 && targetUserId > 0) {
                confirmBlockUser();
            }
            return true;
        });
        popup.show();
    }

    private void confirmBlockUser() {
        String name = !contactName.isEmpty() ? contactName : "người dùng này";
        new AlertDialog.Builder(this)
                .setTitle("Chặn " + name + "?")
                .setMessage("Sau khi chặn, người này sẽ không thể nhắn tin hoặc tương tác với bạn nữa.")
                .setPositiveButton("Chặn", (d, w) -> {
                    String auth = ApiClient.getAuthHeader(this);
                    if (auth.isEmpty()) return;
                    ApiClient.getApiService().blockAccount(auth, new AccountActionRequest(targetUserId))
                            .enqueue(new Callback<AccountsListResponse>() {
                                @Override
                                public void onResponse(@NonNull Call<AccountsListResponse> call, @NonNull Response<AccountsListResponse> response) {
                                    if (response.isSuccessful()) {
                                        Toast.makeText(ChatDetailActivity.this, "Đã chặn " + name, Toast.LENGTH_SHORT).show();
                                        finish();
                                    } else {
                                        Toast.makeText(ChatDetailActivity.this, "Không thể chặn lúc này", Toast.LENGTH_SHORT).show();
                                    }
                                }

                                @Override
                                public void onFailure(@NonNull Call<AccountsListResponse> call, @NonNull Throwable t) {
                                    Toast.makeText(ChatDetailActivity.this, "Lỗi kết nối máy chủ", Toast.LENGTH_SHORT).show();
                                }
                            });
                })
                .setNegativeButton("Hủy", null)
                .show();
    }

    private void openUserProfile() {
        if (targetUserId <= 0) {
            Toast.makeText(this, "Chưa có ID người dùng cho liên hệ này", Toast.LENGTH_SHORT).show();
            return;
        }
        Intent intent = new Intent(this, UserProfileActivity.class);
        intent.putExtra("USER_ID", targetUserId);
        startActivity(intent);
    }

    private void launchSubScreen(Class<?> cls, int initialMediaTab) {
        Intent intent = new Intent(this, cls);
        intent.putExtra("USER_ID", targetUserId);
        intent.putExtra("CHAT_NAME", contactName);
        intent.putExtra("AVATAR_URL", avatarUrl);
        if (initialMediaTab >= 0) {
            intent.putExtra("INITIAL_TAB", initialMediaTab);
        }
        subScreenLauncher.launch(intent);
    }

    private String safeTrim(String s) {
        return s != null ? s.trim() : "";
    }
}
