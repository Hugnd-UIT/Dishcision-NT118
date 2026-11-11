package com.example.dishcision;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.View;
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
import com.example.dishcision.network.ChatMessagesResponse;
import com.example.dishcision.network.ImageLoader;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SharedMediaActivity extends AppCompatActivity {

    private static final int TAB_PHOTOS = 0;
    private static final int TAB_FILES = 1;
    private static final int TAB_LINKS = 2;
    private static final Pattern URL_PATTERN = Pattern.compile("https?://\\S+");

    private ImageView btnBack;
    private ImageView imgAvatar;
    private TextView tvContactName;
    private LinearLayout tabPhotos;
    private LinearLayout tabFiles;
    private LinearLayout tabLinks;
    private TextView tvTabPhotos;
    private TextView tvTabFiles;
    private TextView tvTabLinks;
    private TextView tvMediaCount;
    private ProgressBar progressSharedMedia;
    private TextView tvEmptySharedMedia;
    private LinearLayout layoutMediaContentContainer;

    private int targetUserId = 0;
    private int activeTab = TAB_PHOTOS;
    private final List<ChatMessagesResponse.MessageItem> allMessages = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_shared_media);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(android.R.id.content), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        targetUserId = getIntent().getIntExtra("USER_ID", 0);
        activeTab = getIntent().getIntExtra("INITIAL_TAB", TAB_PHOTOS);

        initViews();
        setupListeners();
        updateTabStyles();
        fetchSharedContent();
    }

    private void initViews() {
        btnBack = findViewById(R.id.btnBack);
        imgAvatar = findViewById(R.id.imgAvatar);
        tvContactName = findViewById(R.id.tvContactName);
        tabPhotos = findViewById(R.id.tabPhotos);
        tabFiles = findViewById(R.id.tabFiles);
        tabLinks = findViewById(R.id.tabLinks);
        tvTabPhotos = findViewById(R.id.tvTabPhotos);
        tvTabFiles = findViewById(R.id.tvTabFiles);
        tvTabLinks = findViewById(R.id.tvTabLinks);
        tvMediaCount = findViewById(R.id.tvMediaCount);
        progressSharedMedia = findViewById(R.id.progressSharedMedia);
        tvEmptySharedMedia = findViewById(R.id.tvEmptySharedMedia);
        layoutMediaContentContainer = findViewById(R.id.layoutMediaContentContainer);

        String chatName = getIntent().getStringExtra("CHAT_NAME");
        if (tvContactName != null) {
            tvContactName.setText(chatName != null && !chatName.trim().isEmpty() ? chatName.trim() : "Đoạn chat");
        }
        String avatarUrl = getIntent().getStringExtra("AVATAR_URL");
        if (imgAvatar != null && avatarUrl != null && !avatarUrl.trim().isEmpty()) {
            ImageLoader.loadAvatar(imgAvatar, avatarUrl);
        }
    }

    private void setupListeners() {
        if (btnBack != null) btnBack.setOnClickListener(v -> finish());
        if (tabPhotos != null) tabPhotos.setOnClickListener(v -> { activeTab = TAB_PHOTOS; updateTabStyles(); renderActiveTab(); });
        if (tabFiles != null) tabFiles.setOnClickListener(v -> { activeTab = TAB_FILES; updateTabStyles(); renderActiveTab(); });
        if (tabLinks != null) tabLinks.setOnClickListener(v -> { activeTab = TAB_LINKS; updateTabStyles(); renderActiveTab(); });
    }

    private void updateTabStyles() {
        styleTab(tabPhotos, tvTabPhotos, activeTab == TAB_PHOTOS);
        styleTab(tabFiles, tvTabFiles, activeTab == TAB_FILES);
        styleTab(tabLinks, tvTabLinks, activeTab == TAB_LINKS);
    }

    private void styleTab(LinearLayout tab, TextView label, boolean active) {
        if (tab != null) tab.setBackgroundResource(active ? R.drawable.bg_tab_active_white : 0);
        if (label != null) {
            label.setTextColor(active ? 0xFFFF5A36 : 0xFF505568);
            label.setTypeface(null, active ? Typeface.BOLD : Typeface.NORMAL);
        }
    }

    private void fetchSharedContent() {
        String auth = ApiClient.getAuthHeader(this);
        if (auth.isEmpty() || targetUserId <= 0) {
            renderActiveTab();
            return;
        }
        if (progressSharedMedia != null) progressSharedMedia.setVisibility(View.VISIBLE);

        ApiClient.getApiService().getChatMessages(auth, targetUserId, 100).enqueue(new Callback<ChatMessagesResponse>() {
            @Override
            public void onResponse(@NonNull Call<ChatMessagesResponse> call, @NonNull Response<ChatMessagesResponse> response) {
                if (isFinishing() || isDestroyed()) return;
                if (progressSharedMedia != null) progressSharedMedia.setVisibility(View.GONE);
                allMessages.clear();
                if (response.isSuccessful() && response.body() != null && response.body().data != null) {
                    allMessages.addAll(response.body().data);
                }
                renderActiveTab();
            }

            @Override
            public void onFailure(@NonNull Call<ChatMessagesResponse> call, @NonNull Throwable t) {
                if (isFinishing() || isDestroyed()) return;
                if (progressSharedMedia != null) progressSharedMedia.setVisibility(View.GONE);
                renderActiveTab();
            }
        });
    }

    private void renderActiveTab() {
        if (layoutMediaContentContainer == null) return;
        layoutMediaContentContainer.removeAllViews();

        if (activeTab == TAB_PHOTOS) {
            List<String> imageUrls = new ArrayList<>();
            for (ChatMessagesResponse.MessageItem m : allMessages) {
                if (m != null && m.mediaUrl != null && !m.mediaUrl.trim().isEmpty()) {
                    imageUrls.add(m.mediaUrl.trim());
                }
            }
            if (tvMediaCount != null) tvMediaCount.setText(imageUrls.size() + " ảnh đã chia sẻ");
            if (imageUrls.isEmpty()) {
                showEmpty("Chưa có ảnh nào được chia sẻ trong cuộc trò chuyện.");
                return;
            }
            if (tvEmptySharedMedia != null) tvEmptySharedMedia.setVisibility(View.GONE);
            renderPhotoGrid(imageUrls);
        } else if (activeTab == TAB_FILES) {
            if (tvMediaCount != null) tvMediaCount.setText("0 tệp đính kèm");
            showEmpty("Chưa có tệp tài liệu nào được chia sẻ.");
        } else {
            List<ChatMessagesResponse.MessageItem> linkMsgs = new ArrayList<>();
            for (ChatMessagesResponse.MessageItem m : allMessages) {
                if (m != null && m.content != null && URL_PATTERN.matcher(m.content).find()) {
                    linkMsgs.add(m);
                }
            }
            if (tvMediaCount != null) tvMediaCount.setText(linkMsgs.size() + " liên kết đã chia sẻ");
            if (linkMsgs.isEmpty()) {
                showEmpty("Chưa có liên kết nào được chia sẻ.");
                return;
            }
            if (tvEmptySharedMedia != null) tvEmptySharedMedia.setVisibility(View.GONE);
            renderLinksList(linkMsgs);
        }
    }

    private void showEmpty(String msg) {
        if (tvEmptySharedMedia != null) {
            tvEmptySharedMedia.setText(msg);
            tvEmptySharedMedia.setVisibility(View.VISIBLE);
        }
    }

    private void renderPhotoGrid(List<String> urls) {
        int colGap = dpToPx(6);
        int cellHeight = dpToPx(108);

        for (int i = 0; i < urls.size(); i += 3) {
            LinearLayout row = new LinearLayout(this);
            LinearLayout.LayoutParams rowLp = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT, cellHeight);
            rowLp.bottomMargin = colGap;
            row.setLayoutParams(rowLp);
            row.setOrientation(LinearLayout.HORIZONTAL);

            for (int c = 0; c < 3; c++) {
                int idx = i + c;
                LinearLayout.LayoutParams cellLp = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.MATCH_PARENT, 1f);
                if (c < 2) cellLp.setMarginEnd(colGap);

                if (idx >= urls.size()) {
                    View spacer = new View(this);
                    spacer.setVisibility(View.INVISIBLE);
                    spacer.setLayoutParams(cellLp);
                    row.addView(spacer);
                    continue;
                }

                String url = urls.get(idx);
                ImageView iv = new ImageView(this);
                iv.setLayoutParams(cellLp);
                iv.setScaleType(ImageView.ScaleType.CENTER_CROP);
                iv.setBackgroundResource(R.drawable.bg_image_thumbnail);
                ImageLoader.loadImage(iv, url);
                iv.setOnClickListener(v -> showFullScreenPhoto(url));
                row.addView(iv);
            }
            layoutMediaContentContainer.addView(row);
        }
    }

    private void renderLinksList(List<ChatMessagesResponse.MessageItem> linkMsgs) {
        for (ChatMessagesResponse.MessageItem m : linkMsgs) {
            Matcher matcher = URL_PATTERN.matcher(m.content);
            String firstUrl = matcher.find() ? matcher.group() : "";

            LinearLayout card = new LinearLayout(this);
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            lp.bottomMargin = dpToPx(10);
            card.setLayoutParams(lp);
            card.setBackgroundResource(R.drawable.bg_card_white);
            card.setOrientation(LinearLayout.VERTICAL);
            card.setPadding(dpToPx(14), dpToPx(14), dpToPx(14), dpToPx(14));
            card.setClickable(true);
            card.setFocusable(true);

            TextView tvUrl = new TextView(this);
            tvUrl.setText(firstUrl);
            tvUrl.setTextColor(0xFFFF5A36);
            tvUrl.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
            tvUrl.setTypeface(null, Typeface.BOLD);
            card.addView(tvUrl);

            TextView tvSnippet = new TextView(this);
            LinearLayout.LayoutParams snLp = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            snLp.topMargin = dpToPx(4);
            tvSnippet.setLayoutParams(snLp);
            tvSnippet.setText(m.content.trim());
            tvSnippet.setTextColor(0xFF505568);
            tvSnippet.setTextSize(TypedValue.COMPLEX_UNIT_SP, 13);
            card.addView(tvSnippet);

            String timeStr = ChatPrefsHelper.formatShortTime(m.createdAt);
            if (!timeStr.isEmpty()) {
                TextView tvTime = new TextView(this);
                LinearLayout.LayoutParams tLp = new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                tLp.topMargin = dpToPx(4);
                tvTime.setLayoutParams(tLp);
                tvTime.setText(timeStr);
                tvTime.setTextColor(0xFF8E7570);
                tvTime.setTextSize(TypedValue.COMPLEX_UNIT_SP, 11);
                card.addView(tvTime);
            }

            if (!firstUrl.isEmpty()) {
                final String targetUrl = firstUrl;
                card.setOnClickListener(v -> {
                    try {
                        startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(targetUrl)));
                    } catch (Exception e) {
                        Toast.makeText(this, "Không thể mở liên kết", Toast.LENGTH_SHORT).show();
                    }
                });
            }
            layoutMediaContentContainer.addView(card);
        }
    }

    private void showFullScreenPhoto(String url) {
        PostFeedHelper.openFullscreenImage(this, null, null, url);
    }

    private int dpToPx(int dp) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, getResources().getDisplayMetrics());
    }
}
