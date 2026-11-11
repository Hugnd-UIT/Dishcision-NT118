package com.example.dishcision;

import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.Gravity;
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
import com.example.dishcision.network.AuthHelper;
import com.example.dishcision.network.ChatMessagesResponse;
import com.example.dishcision.network.ImageLoader;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class PinnedMessagesActivity extends AppCompatActivity {

    private ImageView btnBack;
    private ImageView imgAvatar;
    private TextView tvContactName;
    private ProgressBar progressPinned;
    private TextView tvEmptyPinned;
    private LinearLayout layoutPinnedContainer;

    private int targetUserId = 0;
    private int myUserId = 0;
    private String partnerName = "";
    private final List<ChatMessagesResponse.MessageItem> allMessages = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_pinned_messages);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(android.R.id.content), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        myUserId = AuthHelper.getCurrentUserId(this);
        targetUserId = getIntent().getIntExtra("USER_ID", 0);
        String extraName = getIntent().getStringExtra("CHAT_NAME");
        partnerName = extraName != null && !extraName.trim().isEmpty() ? extraName.trim() : "Đối phương";

        initViews();
        setupListeners();
        fetchAndRenderPinned();
    }

    private void initViews() {
        btnBack = findViewById(R.id.btnBack);
        imgAvatar = findViewById(R.id.imgAvatar);
        tvContactName = findViewById(R.id.tvContactName);
        progressPinned = findViewById(R.id.progressPinned);
        tvEmptyPinned = findViewById(R.id.tvEmptyPinned);
        layoutPinnedContainer = findViewById(R.id.layoutPinnedContainer);

        if (tvContactName != null) tvContactName.setText(partnerName);
        String avatarUrl = getIntent().getStringExtra("AVATAR_URL");
        if (imgAvatar != null && avatarUrl != null && !avatarUrl.trim().isEmpty()) {
            ImageLoader.loadAvatar(imgAvatar, avatarUrl);
        }
    }

    private void setupListeners() {
        if (btnBack != null) btnBack.setOnClickListener(v -> finish());
    }

    private void fetchAndRenderPinned() {
        Set<String> pinnedIds = ChatPrefsHelper.getPinnedMessageIds(this, targetUserId);
        if (pinnedIds.isEmpty()) {
            renderPinnedMessages();
            return;
        }
        String auth = ApiClient.getAuthHeader(this);
        if (auth.isEmpty() || targetUserId <= 0) {
            renderPinnedMessages();
            return;
        }
        if (progressPinned != null) progressPinned.setVisibility(View.VISIBLE);
        ApiClient.getApiService().getChatMessages(auth, targetUserId, 100).enqueue(new Callback<ChatMessagesResponse>() {
            @Override
            public void onResponse(@NonNull Call<ChatMessagesResponse> call, @NonNull Response<ChatMessagesResponse> response) {
                if (isFinishing() || isDestroyed()) return;
                if (progressPinned != null) progressPinned.setVisibility(View.GONE);
                allMessages.clear();
                if (response.isSuccessful() && response.body() != null && response.body().data != null) {
                    allMessages.addAll(response.body().data);
                }
                renderPinnedMessages();
            }

            @Override
            public void onFailure(@NonNull Call<ChatMessagesResponse> call, @NonNull Throwable t) {
                if (isFinishing() || isDestroyed()) return;
                if (progressPinned != null) progressPinned.setVisibility(View.GONE);
                renderPinnedMessages();
            }
        });
    }

    private void renderPinnedMessages() {
        if (layoutPinnedContainer == null) return;
        layoutPinnedContainer.removeAllViews();

        Set<String> pinnedIds = ChatPrefsHelper.getPinnedMessageIds(this, targetUserId);
        List<ChatMessagesResponse.MessageItem> pinnedList = new ArrayList<>();
        for (ChatMessagesResponse.MessageItem msg : allMessages) {
            if (msg != null && pinnedIds.contains(String.valueOf(msg.messageId))) {
                pinnedList.add(msg);
            }
        }

        if (pinnedList.isEmpty()) {
            if (tvEmptyPinned != null) tvEmptyPinned.setVisibility(View.VISIBLE);
            return;
        }
        if (tvEmptyPinned != null) tvEmptyPinned.setVisibility(View.GONE);

        for (ChatMessagesResponse.MessageItem msg : pinnedList) {
            layoutPinnedContainer.addView(buildPinnedCard(msg));
        }
    }

    private View buildPinnedCard(ChatMessagesResponse.MessageItem msg) {
        LinearLayout card = new LinearLayout(this);
        LinearLayout.LayoutParams cardLp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        cardLp.topMargin = dpToPx(12);
        card.setLayoutParams(cardLp);
        card.setBackgroundResource(R.drawable.bg_card_white);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setPadding(dpToPx(16), dpToPx(16), dpToPx(16), dpToPx(16));

        boolean isMine = myUserId > 0 && msg.senderId == myUserId;
        String senderLabel = isMine ? "Bạn" : partnerName;
        String timeStr = ChatPrefsHelper.formatShortTime(msg.createdAt);

        TextView tvSender = new TextView(this);
        tvSender.setText(senderLabel + (timeStr.isEmpty() ? "" : " · " + timeStr));
        tvSender.setTextColor(0xFF141B2B);
        tvSender.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
        tvSender.setTypeface(null, Typeface.BOLD);
        card.addView(tvSender);

        LinearLayout contentBox = new LinearLayout(this);
        LinearLayout.LayoutParams boxLp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        boxLp.topMargin = dpToPx(10);
        contentBox.setLayoutParams(boxLp);
        contentBox.setBackgroundResource(R.drawable.bg_input_field_light);
        contentBox.setPadding(dpToPx(12), dpToPx(12), dpToPx(12), dpToPx(12));

        TextView tvContent = new TextView(this);
        String body = msg.content != null && !msg.content.trim().isEmpty()
                ? msg.content.trim()
                : "📷 Hình ảnh đã chia sẻ";
        tvContent.setText(body);
        tvContent.setTextColor(0xFF141B2B);
        tvContent.setTextSize(TypedValue.COMPLEX_UNIT_SP, 13);
        contentBox.addView(tvContent);
        card.addView(contentBox);

        LinearLayout actionsRow = new LinearLayout(this);
        LinearLayout.LayoutParams actLp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        actLp.topMargin = dpToPx(12);
        actionsRow.setLayoutParams(actLp);
        actionsRow.setGravity(Gravity.CENTER_VERTICAL);
        actionsRow.setOrientation(LinearLayout.HORIZONTAL);

        TextView btnJump = new TextView(this);
        btnJump.setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f));
        btnJump.setText("Xem trong đoạn chat");
        btnJump.setTextColor(0xFFFF5A36);
        btnJump.setTextSize(TypedValue.COMPLEX_UNIT_SP, 13);
        btnJump.setTypeface(null, Typeface.BOLD);
        btnJump.setPadding(0, dpToPx(4), 0, dpToPx(4));
        btnJump.setOnClickListener(v -> {
            Intent res = new Intent();
            res.putExtra(ChatActivity.EXTRA_SCROLL_TO_MSG_ID, msg.messageId);
            setResult(RESULT_OK, res);
            finish();
        });
        actionsRow.addView(btnJump);

        TextView btnUnpin = new TextView(this);
        btnUnpin.setText("Bỏ ghim");
        btnUnpin.setTextColor(0xFF8E7570);
        btnUnpin.setTextSize(TypedValue.COMPLEX_UNIT_SP, 13);
        btnUnpin.setPadding(dpToPx(8), dpToPx(4), 0, dpToPx(4));
        btnUnpin.setOnClickListener(v -> {
            ChatPrefsHelper.removePinnedMessage(this, targetUserId, msg.messageId);
            Toast.makeText(this, "Đã bỏ ghim tin nhắn", Toast.LENGTH_SHORT).show();
            renderPinnedMessages();
        });
        actionsRow.addView(btnUnpin);

        card.addView(actionsRow);
        return card;
    }

    private int dpToPx(int dp) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, getResources().getDisplayMetrics());
    }
}
