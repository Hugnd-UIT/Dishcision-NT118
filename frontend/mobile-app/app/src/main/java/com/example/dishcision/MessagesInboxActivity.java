package com.example.dishcision;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.dishcision.network.ApiClient;
import com.example.dishcision.network.ChatsListResponse;
import com.example.dishcision.network.ImageLoader;
import com.example.dishcision.network.NotificationResponse;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MessagesInboxActivity extends AppCompatActivity {

    private static final int FILTER_ALL = 0;
    private static final int FILTER_UNREAD = 1;
    private static final int FILTER_RESTAURANTS = 2;
    private static final int FILTER_FRIENDS = 3;

    private View viewNotifDotInbox;
    private LinearLayout btnComposeChat;
    private EditText edtSearchInbox;
    private LinearLayout chipAll;
    private TextView tvChipAllLabel;
    private TextView tvAllUnreadBadge;
    private TextView chipUnread;
    private TextView chipRestaurants;
    private TextView chipFriends;
    private ProgressBar progressInbox;
    private TextView tvEmptyInbox;
    private LinearLayout layoutConversationsContainer;
    private LinearLayout layoutInboxFooter;

    private LinearLayout navHome;
    private LinearLayout navDecide;
    private View navAdd;
    private LinearLayout navProfile;

    private final List<ChatsListResponse.ConversationItem> allConversations = new ArrayList<>();
    private int activeFilter = FILTER_ALL;
    private boolean firstResume = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_messages_inbox);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(android.R.id.content), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        initViews();
        setupListeners();
        fetchChats();
        fetchUnreadNotifDot();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (firstResume) {
            firstResume = false;
            return;
        }
        fetchChats();
        fetchUnreadNotifDot();
    }

    private void initViews() {
        ImageView btnNotification = findViewById(R.id.btnNotification);
        if (btnNotification != null) {
            btnNotification.setOnClickListener(v ->
                    startActivity(new Intent(MessagesInboxActivity.this, NotificationsActivity.class)));
        }
        viewNotifDotInbox = findViewById(R.id.viewNotifDotInbox);
        btnComposeChat = findViewById(R.id.btnComposeChat);
        edtSearchInbox = findViewById(R.id.edtSearchInbox);
        chipAll = findViewById(R.id.chipAll);
        tvChipAllLabel = findViewById(R.id.tvChipAllLabel);
        tvAllUnreadBadge = findViewById(R.id.tvAllUnreadBadge);
        chipUnread = findViewById(R.id.chipUnread);
        chipRestaurants = findViewById(R.id.chipRestaurants);
        chipFriends = findViewById(R.id.chipFriends);
        progressInbox = findViewById(R.id.progressInbox);
        tvEmptyInbox = findViewById(R.id.tvEmptyInbox);
        layoutConversationsContainer = findViewById(R.id.layoutConversationsContainer);
        layoutInboxFooter = findViewById(R.id.layoutInboxFooter);

        navHome = findViewById(R.id.navHome);
        navDecide = findViewById(R.id.navDecide);
        navAdd = findViewById(R.id.navAdd);
        navProfile = findViewById(R.id.navProfile);
    }

    private void setupListeners() {
        if (btnComposeChat != null) {
            btnComposeChat.setOnClickListener(v ->
                    startActivity(new Intent(MessagesInboxActivity.this, NewMessageActivity.class)));
        }

        if (edtSearchInbox != null) {
            edtSearchInbox.addTextChangedListener(new TextWatcher() {
                @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
                @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                    renderConversations();
                }
                @Override public void afterTextChanged(Editable s) {}
            });
        }

        if (chipAll != null) chipAll.setOnClickListener(v -> selectFilter(FILTER_ALL));
        if (chipUnread != null) chipUnread.setOnClickListener(v -> selectFilter(FILTER_UNREAD));
        if (chipRestaurants != null) chipRestaurants.setOnClickListener(v -> selectFilter(FILTER_RESTAURANTS));
        if (chipFriends != null) chipFriends.setOnClickListener(v -> selectFilter(FILTER_FRIENDS));

        if (navHome != null) {
            navHome.setOnClickListener(v -> {
                Intent intent = new Intent(MessagesInboxActivity.this, HomeActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                startActivity(intent);
                finish();
            });
        }

        if (navDecide != null) {
            navDecide.setOnClickListener(v ->
                    startActivity(new Intent(MessagesInboxActivity.this, DecideActivity.class)));
        }

        if (navAdd != null) {
            navAdd.setOnClickListener(v ->
                    startActivity(new Intent(MessagesInboxActivity.this, CreatePostActivity.class)));
        }

        if (navProfile != null) {
            navProfile.setOnClickListener(v ->
                    startActivity(new Intent(MessagesInboxActivity.this, ProfileActivity.class)));
        }
    }

    private void selectFilter(int filter) {
        activeFilter = filter;
        if (chipAll != null && tvChipAllLabel != null) {
            boolean active = filter == FILTER_ALL;
            chipAll.setBackgroundResource(active ? R.drawable.bg_chip_dark_active : R.drawable.bg_chip_preference);
            tvChipAllLabel.setTextColor(active ? Color.WHITE : 0xFF141B2B);
            tvChipAllLabel.setTypeface(null, active ? Typeface.BOLD : Typeface.NORMAL);
        }
        styleFilterChip(chipUnread, filter == FILTER_UNREAD);
        styleFilterChip(chipRestaurants, filter == FILTER_RESTAURANTS);
        styleFilterChip(chipFriends, filter == FILTER_FRIENDS);
        renderConversations();
    }

    private void styleFilterChip(TextView chip, boolean active) {
        if (chip == null) return;
        chip.setBackgroundResource(active ? R.drawable.bg_chip_dark_active : R.drawable.bg_chip_preference);
        chip.setTextColor(active ? Color.WHITE : 0xFF141B2B);
        chip.setTypeface(null, active ? Typeface.BOLD : Typeface.NORMAL);
    }

    private void fetchUnreadNotifDot() {
        String auth = ApiClient.getAuthHeader(this);
        if (auth.isEmpty() || viewNotifDotInbox == null) return;
        ApiClient.getApiService().getNotifications(auth).enqueue(new Callback<NotificationResponse>() {
            @Override
            public void onResponse(@NonNull Call<NotificationResponse> call, @NonNull Response<NotificationResponse> response) {
                if (isFinishing() || isDestroyed() || viewNotifDotInbox == null) return;
                boolean hasUnread = false;
                if (response.isSuccessful() && response.body() != null && response.body().data != null) {
                    for (NotificationResponse.NotificationItem item : response.body().data) {
                        if (item != null && !item.isRead) {
                            hasUnread = true;
                            break;
                        }
                    }
                }
                viewNotifDotInbox.setVisibility(hasUnread ? View.VISIBLE : View.GONE);
            }

            @Override
            public void onFailure(@NonNull Call<NotificationResponse> call, @NonNull Throwable t) {}
        });
    }

    private void fetchChats() {
        String auth = ApiClient.getAuthHeader(this);
        if (auth.isEmpty()) {
            allConversations.clear();
            renderConversations();
            return;
        }
        if (progressInbox != null && allConversations.isEmpty()) {
            progressInbox.setVisibility(View.VISIBLE);
        }
        ApiClient.getApiService().getChats(auth).enqueue(new Callback<ChatsListResponse>() {
            @Override
            public void onResponse(@NonNull Call<ChatsListResponse> call, @NonNull Response<ChatsListResponse> response) {
                if (isFinishing() || isDestroyed()) return;
                if (progressInbox != null) progressInbox.setVisibility(View.GONE);
                if (response.isSuccessful() && response.body() != null && response.body().data != null) {
                    allConversations.clear();
                    allConversations.addAll(response.body().data);
                    renderConversations();
                } else {
                    Toast.makeText(MessagesInboxActivity.this, "Không thể tải danh sách tin nhắn", Toast.LENGTH_SHORT).show();
                    renderConversations();
                }
            }

            @Override
            public void onFailure(@NonNull Call<ChatsListResponse> call, @NonNull Throwable t) {
                if (isFinishing() || isDestroyed()) return;
                if (progressInbox != null) progressInbox.setVisibility(View.GONE);
                renderConversations();
                Toast.makeText(MessagesInboxActivity.this, "Lỗi kết nối máy chủ", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void renderConversations() {
        if (layoutConversationsContainer == null) return;
        layoutConversationsContainer.removeAllViews();

        int totalUnread = 0;
        for (ChatsListResponse.ConversationItem conv : allConversations) {
            if (conv != null && conv.unreadCount > 0) {
                totalUnread += conv.unreadCount;
            }
        }
        if (tvAllUnreadBadge != null) {
            if (totalUnread > 0) {
                tvAllUnreadBadge.setText(String.valueOf(totalUnread));
                tvAllUnreadBadge.setVisibility(View.VISIBLE);
            } else {
                tvAllUnreadBadge.setVisibility(View.GONE);
            }
        }

        String query = edtSearchInbox != null && edtSearchInbox.getText() != null
                ? edtSearchInbox.getText().toString().toLowerCase(Locale.ROOT).trim()
                : "";

        List<ChatsListResponse.ConversationItem> filtered = new ArrayList<>();
        for (ChatsListResponse.ConversationItem conv : allConversations) {
            if (conv == null || conv.recipient == null) continue;
            boolean isRestaurant = "RestaurantAccount".equals(conv.recipient.role);

            if (activeFilter == FILTER_UNREAD && conv.unreadCount <= 0) continue;
            if (activeFilter == FILTER_RESTAURANTS && !isRestaurant) continue;
            if (activeFilter == FILTER_FRIENDS && isRestaurant) continue;

            if (!query.isEmpty()) {
                String name = conv.recipient.fullName != null ? conv.recipient.fullName.toLowerCase(Locale.ROOT) : "";
                String username = conv.recipient.username != null ? conv.recipient.username.toLowerCase(Locale.ROOT) : "";
                String lastText = conv.lastMessage != null && conv.lastMessage.content != null
                        ? conv.lastMessage.content.toLowerCase(Locale.ROOT)
                        : "";
                if (!name.contains(query) && !username.contains(query) && !lastText.contains(query)) {
                    continue;
                }
            }
            filtered.add(conv);
        }

        if (filtered.isEmpty()) {
            if (tvEmptyInbox != null) {
                tvEmptyInbox.setText(query.isEmpty() && activeFilter == FILTER_ALL
                        ? "Chưa có cuộc trò chuyện nào.\nNhấn nút cây bút ở góc trên để nhắn tin cho bạn bè hoặc quán ăn."
                        : "Không có cuộc trò chuyện phù hợp.");
                tvEmptyInbox.setVisibility(View.VISIBLE);
            }
            if (layoutInboxFooter != null) layoutInboxFooter.setVisibility(View.GONE);
            return;
        }

        if (tvEmptyInbox != null) tvEmptyInbox.setVisibility(View.GONE);
        if (layoutInboxFooter != null) layoutInboxFooter.setVisibility(View.VISIBLE);

        for (int i = 0; i < filtered.size(); i++) {
            ChatsListResponse.ConversationItem item = filtered.get(i);
            layoutConversationsContainer.addView(buildConversationRow(item));
            if (i < filtered.size() - 1) {
                View divider = new View(this);
                divider.setLayoutParams(new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT, dpToPx(1)));
                divider.setBackgroundColor(0xFFE9EBF4);
                layoutConversationsContainer.addView(divider);
            }
        }
    }

    private View buildConversationRow(ChatsListResponse.ConversationItem item) {
        LinearLayout row = new LinearLayout(this);
        row.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(Gravity.CENTER_VERTICAL);
        row.setPadding(0, dpToPx(12), 0, dpToPx(12));
        row.setClickable(true);
        row.setFocusable(true);
        TypedValue outValue = new TypedValue();
        getTheme().resolveAttribute(android.R.attr.selectableItemBackground, outValue, true);
        row.setBackgroundResource(outValue.resourceId);

        ImageView avatar = new ImageView(this);
        avatar.setLayoutParams(new LinearLayout.LayoutParams(dpToPx(48), dpToPx(48)));
        avatar.setScaleType(ImageView.ScaleType.CENTER_CROP);
        avatar.setImageResource(R.drawable.bg_avatar_circle);
        if (item.recipient.avatarUrl != null && !item.recipient.avatarUrl.trim().isEmpty()) {
            ImageLoader.loadAvatar(avatar, item.recipient.avatarUrl);
        }
        row.addView(avatar);

        LinearLayout infoCol = new LinearLayout(this);
        LinearLayout.LayoutParams infoLp = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f);
        infoLp.setMarginStart(dpToPx(12));
        infoCol.setLayoutParams(infoLp);
        infoCol.setOrientation(LinearLayout.VERTICAL);

        LinearLayout topRow = new LinearLayout(this);
        topRow.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
        topRow.setGravity(Gravity.CENTER_VERTICAL);
        topRow.setOrientation(LinearLayout.HORIZONTAL);

        String displayName = (item.recipient.fullName != null && !item.recipient.fullName.trim().isEmpty())
                ? item.recipient.fullName.trim()
                : (item.recipient.username != null ? item.recipient.username : "Thành viên");

        String savedNick = ChatPrefsHelper.getPartnerNickname(this, item.recipient.userId);
        String shownName = !savedNick.isEmpty() ? savedNick : displayName;

        TextView tvName = new TextView(this);
        tvName.setText(shownName);
        tvName.setTextColor(0xFF141B2B);
        tvName.setTextSize(TypedValue.COMPLEX_UNIT_SP, 15);
        tvName.setTypeface(null, Typeface.BOLD);
        tvName.setMaxLines(1);
        tvName.setEllipsize(TextUtils.TruncateAt.END);
        topRow.addView(tvName);

        boolean isVerified = "RestaurantAccount".equals(item.recipient.role) || "Admin".equals(item.recipient.role);
        if (isVerified) {
            ImageView ivVerified = new ImageView(this);
            LinearLayout.LayoutParams verLp = new LinearLayout.LayoutParams(dpToPx(15), dpToPx(15));
            verLp.setMarginStart(dpToPx(4));
            ivVerified.setLayoutParams(verLp);
            ivVerified.setImageResource(R.drawable.ic_verified);
            topRow.addView(ivVerified);
        }

        View spacer = new View(this);
        spacer.setLayoutParams(new LinearLayout.LayoutParams(0, 1, 1f));
        topRow.addView(spacer);

        TextView tvTime = new TextView(this);
        String timeStr = item.lastMessage != null ? ChatPrefsHelper.formatShortTime(item.lastMessage.createdAt) : "";
        tvTime.setText(timeStr);
        tvTime.setTextColor(item.unreadCount > 0 ? 0xFFFF5A36 : 0xFF6B7280);
        tvTime.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12);
        if (item.unreadCount > 0) tvTime.setTypeface(null, Typeface.BOLD);
        topRow.addView(tvTime);

        infoCol.addView(topRow);

        LinearLayout bottomRow = new LinearLayout(this);
        LinearLayout.LayoutParams botLp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        botLp.topMargin = dpToPx(3);
        bottomRow.setLayoutParams(botLp);
        bottomRow.setGravity(Gravity.CENTER_VERTICAL);
        bottomRow.setOrientation(LinearLayout.HORIZONTAL);

        TextView tvPreview = new TextView(this);
        tvPreview.setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f));
        tvPreview.setMaxLines(1);
        tvPreview.setEllipsize(TextUtils.TruncateAt.END);
        tvPreview.setTextSize(TypedValue.COMPLEX_UNIT_SP, 13);
        tvPreview.setTextColor(item.unreadCount > 0 ? 0xFF141B2B : 0xFF6B7280);
        if (item.unreadCount > 0) tvPreview.setTypeface(null, Typeface.BOLD);

        String previewText = "";
        if (item.lastMessage != null) {
            String msgContent = item.lastMessage.content != null ? item.lastMessage.content.trim() : "";
            boolean hasMedia = item.lastMessage.mediaUrl != null && !item.lastMessage.mediaUrl.trim().isEmpty();
            if (!msgContent.isEmpty()) {
                previewText = msgContent;
            } else if (hasMedia) {
                previewText = "📷 Đã gửi một hình ảnh";
            }
        }
        tvPreview.setText(previewText);
        bottomRow.addView(tvPreview);

        if (item.unreadCount > 0) {
            TextView tvBadge = new TextView(this);
            LinearLayout.LayoutParams badgeLp = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            badgeLp.setMarginStart(dpToPx(8));
            tvBadge.setLayoutParams(badgeLp);
            tvBadge.setBackgroundResource(R.drawable.bg_unread_badge);
            tvBadge.setPadding(dpToPx(7), dpToPx(2), dpToPx(7), dpToPx(2));
            tvBadge.setText(String.valueOf(item.unreadCount));
            tvBadge.setTextColor(Color.WHITE);
            tvBadge.setTextSize(TypedValue.COMPLEX_UNIT_SP, 11);
            tvBadge.setTypeface(null, Typeface.BOLD);
            bottomRow.addView(tvBadge);
        }

        infoCol.addView(bottomRow);
        row.addView(infoCol);

        row.setOnClickListener(v -> {
            Intent intent = new Intent(MessagesInboxActivity.this, ChatActivity.class);
            intent.putExtra("USER_ID", item.recipient.userId);
            intent.putExtra("CHAT_NAME", displayName);
            if (item.recipient.avatarUrl != null) {
                intent.putExtra("AVATAR_URL", item.recipient.avatarUrl);
            }
            if (item.recipient.role != null) {
                intent.putExtra("USER_ROLE", item.recipient.role);
            }
            startActivity(intent);
        });

        return row;
    }

    private int dpToPx(int dp) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, getResources().getDisplayMetrics());
    }
}
