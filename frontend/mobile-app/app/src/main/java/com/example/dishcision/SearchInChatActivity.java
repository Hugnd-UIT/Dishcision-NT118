package com.example.dishcision;

import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.TypedValue;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

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
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SearchInChatActivity extends AppCompatActivity {

    private ImageView btnBack;
    private ImageView imgAvatar;
    private TextView tvContactName;
    private EditText edtSearchQuery;
    private ImageView btnClearSearch;
    private TextView tvMatchHeader;
    private ProgressBar progressSearchChat;
    private TextView tvEmptySearchResults;
    private LinearLayout layoutSearchResultsContainer;

    private int targetUserId = 0;
    private int myUserId = 0;
    private String partnerName = "";
    private final List<ChatMessagesResponse.MessageItem> allMessages = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_search_in_chat);

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
        fetchMessages();
    }

    private void initViews() {
        btnBack = findViewById(R.id.btnBack);
        imgAvatar = findViewById(R.id.imgAvatar);
        tvContactName = findViewById(R.id.tvContactName);
        edtSearchQuery = findViewById(R.id.edtSearchQuery);
        btnClearSearch = findViewById(R.id.btnClearSearch);
        tvMatchHeader = findViewById(R.id.tvMatchHeader);
        progressSearchChat = findViewById(R.id.progressSearchChat);
        tvEmptySearchResults = findViewById(R.id.tvEmptySearchResults);
        layoutSearchResultsContainer = findViewById(R.id.layoutSearchResultsContainer);

        if (tvContactName != null) tvContactName.setText(partnerName);
        String avatarUrl = getIntent().getStringExtra("AVATAR_URL");
        if (imgAvatar != null && avatarUrl != null && !avatarUrl.trim().isEmpty()) {
            ImageLoader.loadAvatar(imgAvatar, avatarUrl);
        }
    }

    private void setupListeners() {
        if (btnBack != null) btnBack.setOnClickListener(v -> finish());

        if (btnClearSearch != null) {
            btnClearSearch.setOnClickListener(v -> {
                if (edtSearchQuery != null) edtSearchQuery.setText("");
            });
        }

        if (edtSearchQuery != null) {
            edtSearchQuery.addTextChangedListener(new TextWatcher() {
                @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
                @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                    filterAndRender();
                }
                @Override public void afterTextChanged(Editable s) {}
            });
        }
    }

    private void fetchMessages() {
        String auth = ApiClient.getAuthHeader(this);
        if (auth.isEmpty() || targetUserId <= 0) return;
        if (progressSearchChat != null) progressSearchChat.setVisibility(View.VISIBLE);

        ApiClient.getApiService().getChatMessages(auth, targetUserId, 100).enqueue(new Callback<ChatMessagesResponse>() {
            @Override
            public void onResponse(@NonNull Call<ChatMessagesResponse> call, @NonNull Response<ChatMessagesResponse> response) {
                if (isFinishing() || isDestroyed()) return;
                if (progressSearchChat != null) progressSearchChat.setVisibility(View.GONE);
                allMessages.clear();
                if (response.isSuccessful() && response.body() != null && response.body().data != null) {
                    allMessages.addAll(response.body().data);
                }
                filterAndRender();
            }

            @Override
            public void onFailure(@NonNull Call<ChatMessagesResponse> call, @NonNull Throwable t) {
                if (isFinishing() || isDestroyed()) return;
                if (progressSearchChat != null) progressSearchChat.setVisibility(View.GONE);
                filterAndRender();
            }
        });
    }

    private void filterAndRender() {
        if (layoutSearchResultsContainer == null) return;
        layoutSearchResultsContainer.removeAllViews();

        String query = edtSearchQuery != null && edtSearchQuery.getText() != null
                ? edtSearchQuery.getText().toString().trim()
                : "";

        if (query.isEmpty()) {
            if (tvMatchHeader != null) tvMatchHeader.setText("Nhập từ khóa để tìm trong đoạn chat");
            if (tvEmptySearchResults != null) tvEmptySearchResults.setVisibility(View.GONE);
            return;
        }

        String lower = query.toLowerCase(Locale.ROOT);
        List<ChatMessagesResponse.MessageItem> matches = new ArrayList<>();
        for (ChatMessagesResponse.MessageItem m : allMessages) {
            if (m != null && m.content != null && m.content.toLowerCase(Locale.ROOT).contains(lower)) {
                matches.add(m);
            }
        }

        if (tvMatchHeader != null) {
            tvMatchHeader.setText("Tìm thấy " + matches.size() + " tin nhắn khớp với “" + query + "”");
        }
        if (matches.isEmpty()) {
            if (tvEmptySearchResults != null) tvEmptySearchResults.setVisibility(View.VISIBLE);
            return;
        }
        if (tvEmptySearchResults != null) tvEmptySearchResults.setVisibility(View.GONE);

        for (ChatMessagesResponse.MessageItem m : matches) {
            layoutSearchResultsContainer.addView(buildResultCard(m));
        }
    }

    private View buildResultCard(ChatMessagesResponse.MessageItem msg) {
        LinearLayout card = new LinearLayout(this);
        LinearLayout.LayoutParams cardLp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        cardLp.topMargin = dpToPx(12);
        card.setLayoutParams(cardLp);
        card.setBackgroundResource(R.drawable.bg_card_white);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setPadding(dpToPx(16), dpToPx(16), dpToPx(16), dpToPx(16));
        card.setClickable(true);
        card.setFocusable(true);

        boolean isMine = myUserId > 0 && msg.senderId == myUserId;
        String senderLabel = isMine ? "Bạn" : partnerName;
        String timeStr = ChatPrefsHelper.formatShortTime(msg.createdAt);

        LinearLayout header = new LinearLayout(this);
        header.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
        header.setOrientation(LinearLayout.HORIZONTAL);

        TextView tvSender = new TextView(this);
        tvSender.setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f));
        tvSender.setText(senderLabel);
        tvSender.setTextColor(0xFF141B2B);
        tvSender.setTextSize(TypedValue.COMPLEX_UNIT_SP, 15);
        tvSender.setTypeface(null, Typeface.BOLD);
        header.addView(tvSender);

        TextView tvTime = new TextView(this);
        tvTime.setText(timeStr);
        tvTime.setTextColor(0xFF6B7280);
        tvTime.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12);
        header.addView(tvTime);
        card.addView(header);

        TextView tvBody = new TextView(this);
        LinearLayout.LayoutParams bodyLp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        bodyLp.topMargin = dpToPx(8);
        tvBody.setLayoutParams(bodyLp);
        tvBody.setText(msg.content);
        tvBody.setTextColor(0xFF141B2B);
        tvBody.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
        card.addView(tvBody);

        View divider = new View(this);
        LinearLayout.LayoutParams divLp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, dpToPx(1));
        divLp.topMargin = dpToPx(12);
        divider.setLayoutParams(divLp);
        divider.setBackgroundColor(0xFFE9EBF4);
        card.addView(divider);

        TextView tvJump = new TextView(this);
        LinearLayout.LayoutParams jumpLp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        jumpLp.topMargin = dpToPx(10);
        tvJump.setLayoutParams(jumpLp);
        tvJump.setText("Xem trong đoạn chat →");
        tvJump.setTextColor(0xFFFF5A36);
        tvJump.setTextSize(TypedValue.COMPLEX_UNIT_SP, 13);
        tvJump.setTypeface(null, Typeface.BOLD);
        card.addView(tvJump);

        card.setOnClickListener(v -> {
            Intent res = new Intent();
            res.putExtra(ChatActivity.EXTRA_SCROLL_TO_MSG_ID, msg.messageId);
            setResult(RESULT_OK, res);
            finish();
        });

        return card;
    }

    private int dpToPx(int dp) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, getResources().getDisplayMetrics());
    }
}
