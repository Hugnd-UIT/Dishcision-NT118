package com.example.dishcision;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.text.util.Linkify;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.ScrollView;
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

import com.example.dishcision.network.ApiClient;
import com.example.dishcision.network.AuthHelper;
import com.example.dishcision.network.ChatMessagesResponse;
import com.example.dishcision.network.ImageLoader;
import com.example.dishcision.network.SendChatMessageRequest;
import com.example.dishcision.network.SendChatMessageResponse;
import com.example.dishcision.network.UploadResponse;
import com.example.dishcision.network.UserProfileResponse;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ChatActivity extends AppCompatActivity {

    public static final String EXTRA_SCROLL_TO_MSG_ID = "SCROLL_TO_MSG_ID";

    private ImageView btnBack;
    private ImageView imgChatAvatar;
    private TextView tvChatTitle;
    private TextView tvChatSubtitle;
    private ImageView btnInfo;
    private ImageView btnProfileUser;
    private ScrollView scrollChatHistory;
    private ProgressBar progressChat;
    private TextView tvEmptyChat;
    private LinearLayout layoutMessagesContainer;

    private ImageView btnCamera;
    private EditText edtChatMessage;
    private ImageView btnAttachImage;
    private TextView btnQuickEmoji;
    private FrameLayout btnSendMessage;

    private int targetUserId = 0;
    private int myUserId = 0;
    private String partnerName = "";
    private String partnerAvatarUrl = "";
    private String partnerRole = "";
    private final List<ChatMessagesResponse.MessageItem> currentMessages = new ArrayList<>();
    private final Map<Integer, View> messageViewMap = new HashMap<>();
    private int pendingScrollToMessageId = 0;
    private boolean isUploadingImage = false;
    private boolean isSending = false;

    private ActivityResultLauncher<String> imagePickerLauncher;
    private ActivityResultLauncher<Intent> detailLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_chat);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(android.R.id.content), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        myUserId = AuthHelper.getCurrentUserId(this);
        targetUserId = getIntent().getIntExtra("USER_ID", 0);
        partnerName = safeTrim(getIntent().getStringExtra("CHAT_NAME"));
        partnerAvatarUrl = safeTrim(getIntent().getStringExtra("AVATAR_URL"));
        partnerRole = safeTrim(getIntent().getStringExtra("USER_ROLE"));

        if (targetUserId <= 0) {
            Toast.makeText(this, "Không tìm thấy người nhận cuộc trò chuyện", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        imagePickerLauncher = registerForActivityResult(
                new ActivityResultContracts.GetContent(),
                uri -> {
                    if (uri != null) {
                        uploadAndSendImage(uri);
                    }
                }
        );

        detailLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        pendingScrollToMessageId = result.getData().getIntExtra(EXTRA_SCROLL_TO_MSG_ID, 0);
                    }
                }
        );

        initViews();
        setupListeners();
        applyPersonalizations();
        loadPartnerProfileIfNeeded();
    }

    @Override
    protected void onResume() {
        super.onResume();
        applyPersonalizations();
        fetchMessages();
    }

    private void initViews() {
        btnBack = findViewById(R.id.btnBack);
        imgChatAvatar = findViewById(R.id.imgChatAvatar);
        tvChatTitle = findViewById(R.id.tvChatTitle);
        tvChatSubtitle = findViewById(R.id.tvChatSubtitle);
        btnInfo = findViewById(R.id.btnInfo);
        btnProfileUser = findViewById(R.id.btnProfileUser);
        scrollChatHistory = findViewById(R.id.scrollChatHistory);
        progressChat = findViewById(R.id.progressChat);
        tvEmptyChat = findViewById(R.id.tvEmptyChat);
        layoutMessagesContainer = findViewById(R.id.layoutMessagesContainer);

        btnCamera = findViewById(R.id.btnCamera);
        edtChatMessage = findViewById(R.id.edtChatMessage);
        btnAttachImage = findViewById(R.id.btnAttachImage);
        btnQuickEmoji = findViewById(R.id.btnQuickEmoji);
        btnSendMessage = findViewById(R.id.btnSendMessage);
    }

    private void applyPersonalizations() {
        String nick = ChatPrefsHelper.getPartnerNickname(this, targetUserId);
        String displayedTitle = !nick.isEmpty() ? nick : (!partnerName.isEmpty() ? partnerName : "Thành viên");
        if (tvChatTitle != null) tvChatTitle.setText(displayedTitle);

        if (tvChatSubtitle != null) {
            if (!nick.isEmpty() && !partnerName.isEmpty()) {
                tvChatSubtitle.setText(partnerName);
                tvChatSubtitle.setTextColor(0xFF6B7280);
            } else if ("RestaurantAccount".equals(partnerRole)) {
                tvChatSubtitle.setText("Đối tác quán ăn Dishcision");
                tvChatSubtitle.setTextColor(0xFF00A86B);
            } else {
                tvChatSubtitle.setText("Trò chuyện trực tiếp");
                tvChatSubtitle.setTextColor(0xFF00A86B);
            }
        }

        if (imgChatAvatar != null && !partnerAvatarUrl.isEmpty()) {
            ImageLoader.loadAvatar(imgChatAvatar, partnerAvatarUrl);
        }

        int themeColor = ChatPrefsHelper.getThemeColor(this, targetUserId);
        if (btnSendMessage != null) {
            btnSendMessage.setBackgroundTintList(ColorStateList.valueOf(themeColor));
        }

        if (btnQuickEmoji != null) {
            btnQuickEmoji.setText(ChatPrefsHelper.getQuickEmoji(this, targetUserId));
        }

        renderMessages();
    }

    private void loadPartnerProfileIfNeeded() {
        String auth = ApiClient.getAuthHeader(this);
        if (auth.isEmpty()) return;
        ApiClient.getApiService().getUserProfile(auth, String.valueOf(targetUserId)).enqueue(new Callback<UserProfileResponse>() {
            @Override
            public void onResponse(@NonNull Call<UserProfileResponse> call, @NonNull Response<UserProfileResponse> response) {
                if (isFinishing() || isDestroyed() || !response.isSuccessful()
                        || response.body() == null || response.body().getData() == null) {
                    return;
                }
                UserProfileResponse.UserData user = response.body().getData();
                if (user.fullName != null && !user.fullName.trim().isEmpty()) {
                    partnerName = user.fullName.trim();
                } else if (user.username != null && partnerName.isEmpty()) {
                    partnerName = user.username;
                }
                if (user.avatarUrl != null && !user.avatarUrl.trim().isEmpty()) {
                    partnerAvatarUrl = user.avatarUrl.trim();
                }
                if (user.role != null) {
                    partnerRole = user.role;
                }
                applyPersonalizations();
            }

            @Override
            public void onFailure(@NonNull Call<UserProfileResponse> call, @NonNull Throwable t) {}
        });
    }

    private void setupListeners() {
        if (btnBack != null) {
            btnBack.setOnClickListener(v -> finish());
        }

        if (btnInfo != null) {
            btnInfo.setOnClickListener(v -> {
                Intent intent = new Intent(ChatActivity.this, ChatDetailActivity.class);
                intent.putExtra("USER_ID", targetUserId);
                intent.putExtra("CHAT_NAME", partnerName);
                intent.putExtra("AVATAR_URL", partnerAvatarUrl);
                intent.putExtra("USER_ROLE", partnerRole);
                detailLauncher.launch(intent);
            });
        }

        if (btnProfileUser != null) {
            btnProfileUser.setOnClickListener(v -> {
                Intent intent = new Intent(ChatActivity.this, UserProfileActivity.class);
                intent.putExtra("USER_ID", targetUserId);
                startActivity(intent);
            });
        }

        View.OnClickListener pickImageClick = v -> {
            if (isUploadingImage) {
                Toast.makeText(this, "Đang tải ảnh lên, vui lòng đợi...", Toast.LENGTH_SHORT).show();
                return;
            }
            imagePickerLauncher.launch("image/*");
        };
        if (btnCamera != null) btnCamera.setOnClickListener(pickImageClick);
        if (btnAttachImage != null) btnAttachImage.setOnClickListener(pickImageClick);

        if (btnQuickEmoji != null) {
            btnQuickEmoji.setOnClickListener(v -> {
                String emoji = ChatPrefsHelper.getQuickEmoji(this, targetUserId);
                sendMessageToBackend(emoji, null);
            });
        }

        if (btnSendMessage != null) {
            btnSendMessage.setOnClickListener(v -> {
                String message = edtChatMessage != null ? edtChatMessage.getText().toString().trim() : "";
                if (message.isEmpty()) {
                    Toast.makeText(this, "Vui lòng nhập nội dung tin nhắn", Toast.LENGTH_SHORT).show();
                    return;
                }
                sendMessageToBackend(message, null);
            });
        }
    }

    private void fetchMessages() {
        String auth = ApiClient.getAuthHeader(this);
        if (auth.isEmpty()) return;
        if (progressChat != null && currentMessages.isEmpty()) {
            progressChat.setVisibility(View.VISIBLE);
        }

        ApiClient.getApiService().getChatMessages(auth, targetUserId, 100).enqueue(new Callback<ChatMessagesResponse>() {
            @Override
            public void onResponse(@NonNull Call<ChatMessagesResponse> call, @NonNull Response<ChatMessagesResponse> response) {
                if (isFinishing() || isDestroyed()) return;
                if (progressChat != null) progressChat.setVisibility(View.GONE);
                if (response.isSuccessful() && response.body() != null && response.body().data != null) {
                    currentMessages.clear();
                    List<ChatMessagesResponse.MessageItem> list = new ArrayList<>(response.body().data);
                    Collections.reverse(list);
                    currentMessages.addAll(list);
                    renderMessages();
                    if (pendingScrollToMessageId > 0) {
                        scrollToMessage(pendingScrollToMessageId);
                        pendingScrollToMessageId = 0;
                    } else {
                        scrollToBottom();
                    }
                }
            }

            @Override
            public void onFailure(@NonNull Call<ChatMessagesResponse> call, @NonNull Throwable t) {
                if (isFinishing() || isDestroyed()) return;
                if (progressChat != null) progressChat.setVisibility(View.GONE);
                if (currentMessages.isEmpty() && tvEmptyChat != null) {
                    tvEmptyChat.setText("Không thể tải tin nhắn lúc này.");
                    tvEmptyChat.setVisibility(View.VISIBLE);
                }
            }
        });
    }

    private void renderMessages() {
        if (layoutMessagesContainer == null) return;
        layoutMessagesContainer.removeAllViews();
        messageViewMap.clear();

        if (currentMessages.isEmpty()) {
            if (tvEmptyChat != null) {
                tvEmptyChat.setText("Chưa có tin nhắn nào.\nHãy gửi lời chào để bắt đầu cuộc trò chuyện!");
                tvEmptyChat.setVisibility(View.VISIBLE);
            }
            return;
        }
        if (tvEmptyChat != null) tvEmptyChat.setVisibility(View.GONE);

        int themeColor = ChatPrefsHelper.getThemeColor(this, targetUserId);
        String selfNick = ChatPrefsHelper.getSelfNickname(this, targetUserId);
        String partnerNick = ChatPrefsHelper.getPartnerNickname(this, targetUserId);

        for (ChatMessagesResponse.MessageItem msg : currentMessages) {
            if (msg == null) continue;
            boolean isMine = myUserId > 0 && msg.senderId == myUserId;

            LinearLayout wrapper = new LinearLayout(this);
            LinearLayout.LayoutParams wrapperLp = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            wrapperLp.gravity = isMine ? Gravity.END : Gravity.START;
            wrapperLp.bottomMargin = dpToPx(8);
            wrapper.setLayoutParams(wrapperLp);
            wrapper.setOrientation(LinearLayout.VERTICAL);

            boolean isPinned = ChatPrefsHelper.isMessagePinned(this, targetUserId, msg.messageId);
            if (isPinned || (isMine && !selfNick.isEmpty()) || (!isMine && !partnerNick.isEmpty())) {
                TextView tvMetaLabel = new TextView(this);
                tvMetaLabel.setTextColor(0xFF8E7570);
                tvMetaLabel.setTextSize(TypedValue.COMPLEX_UNIT_SP, 10);
                String label = isMine ? selfNick : partnerNick;
                if (isPinned) {
                    label = (label.isEmpty() ? "" : label + " · ") + "📌 Đã ghim";
                }
                tvMetaLabel.setText(label);
                LinearLayout.LayoutParams metaLp = new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                metaLp.gravity = isMine ? Gravity.END : Gravity.START;
                metaLp.bottomMargin = dpToPx(2);
                tvMetaLabel.setLayoutParams(metaLp);
                wrapper.addView(tvMetaLabel);
            }

            LinearLayout bubble = new LinearLayout(this);
            bubble.setOrientation(LinearLayout.VERTICAL);
            GradientDrawable bg = new GradientDrawable();
            bg.setCornerRadius(dpToPx(16));
            bg.setColor(isMine ? themeColor : 0xFFF1F3F9);
            bubble.setBackground(bg);
            bubble.setPadding(dpToPx(14), dpToPx(10), dpToPx(14), dpToPx(10));

            if (msg.mediaUrl != null && !msg.mediaUrl.trim().isEmpty()) {
                ImageView ivMedia = new ImageView(this);
                LinearLayout.LayoutParams imgLp = new LinearLayout.LayoutParams(dpToPx(210), dpToPx(160));
                if (msg.content != null && !msg.content.trim().isEmpty()) {
                    imgLp.bottomMargin = dpToPx(8);
                }
                ivMedia.setLayoutParams(imgLp);
                ivMedia.setScaleType(ImageView.ScaleType.CENTER_CROP);
                ivMedia.setBackgroundResource(R.drawable.bg_image_thumbnail);
                ImageLoader.loadImage(ivMedia, msg.mediaUrl);
                ivMedia.setOnClickListener(v -> showFullScreenImage(msg.mediaUrl));
                bubble.addView(ivMedia);
            }

            if (msg.content != null && !msg.content.trim().isEmpty()) {
                TextView tvBody = new TextView(this);
                tvBody.setMaxWidth(dpToPx(255));
                tvBody.setLineSpacing(dpToPx(2), 1.0f);
                tvBody.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
                tvBody.setTextColor(isMine ? Color.WHITE : 0xFF141B2B);
                tvBody.setLinkTextColor(isMine ? 0xFFFFF4CC : 0xFFFF5A36);
                tvBody.setAutoLinkMask(Linkify.WEB_URLS);
                tvBody.setText(msg.content);
                bubble.addView(tvBody);
            }

            String timeStr = ChatPrefsHelper.formatShortTime(msg.createdAt);
            if (!timeStr.isEmpty()) {
                TextView tvTime = new TextView(this);
                LinearLayout.LayoutParams tLp = new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                tLp.gravity = Gravity.END;
                tLp.topMargin = dpToPx(4);
                tvTime.setLayoutParams(tLp);
                tvTime.setText(timeStr);
                tvTime.setTextSize(TypedValue.COMPLEX_UNIT_SP, 10);
                tvTime.setTextColor(isMine ? 0xDDFFFFFF : 0xFF8E7570);
                bubble.addView(tvTime);
            }

            bubble.setOnLongClickListener(v -> {
                showMessageActionsDialog(msg);
                return true;
            });

            wrapper.addView(bubble);
            messageViewMap.put(msg.messageId, wrapper);
            layoutMessagesContainer.addView(wrapper);
        }
    }

    private void showMessageActionsDialog(ChatMessagesResponse.MessageItem msg) {
        boolean isPinned = ChatPrefsHelper.isMessagePinned(this, targetUserId, msg.messageId);
        List<String> options = new ArrayList<>();
        options.add(isPinned ? "Bỏ ghim tin nhắn" : "Ghim tin nhắn");
        if (msg.content != null && !msg.content.trim().isEmpty()) {
            options.add("Sao chép nội dung");
        }

        new AlertDialog.Builder(this)
                .setItems(options.toArray(new String[0]), (dialog, which) -> {
                    String choice = options.get(which);
                    if ("Ghim tin nhắn".equals(choice) || "Bỏ ghim tin nhắn".equals(choice)) {
                        ChatPrefsHelper.togglePinMessage(ChatActivity.this, targetUserId, msg.messageId);
                        Toast.makeText(ChatActivity.this,
                                isPinned ? "Đã bỏ ghim tin nhắn" : "Đã ghim tin nhắn",
                                Toast.LENGTH_SHORT).show();
                        renderMessages();
                    } else if ("Sao chép nội dung".equals(choice)) {
                        ClipboardManager cm = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                        if (cm != null && msg.content != null) {
                            cm.setPrimaryClip(ClipData.newPlainText("Message", msg.content));
                            Toast.makeText(ChatActivity.this, "Đã sao chép", Toast.LENGTH_SHORT).show();
                        }
                    }
                })
                .show();
    }

    private void showFullScreenImage(String imageUrl) {
        PostFeedHelper.openFullscreenImage(this, null, null, imageUrl);
    }

    private void uploadAndSendImage(Uri imageUri) {
        String auth = ApiClient.getAuthHeader(this);
        if (auth.isEmpty()) return;

        try {
            isUploadingImage = true;
            Toast.makeText(this, "Đang tải ảnh lên...", Toast.LENGTH_SHORT).show();

            File tempFile = new File(getCacheDir(), "chat_img_" + System.currentTimeMillis() + ".jpg");
            InputStream in = getContentResolver().openInputStream(imageUri);
            if (in == null) {
                isUploadingImage = false;
                return;
            }
            FileOutputStream out = new FileOutputStream(tempFile);
            byte[] buf = new byte[4096];
            int read;
            while ((read = in.read(buf)) != -1) {
                out.write(buf, 0, read);
            }
            out.flush();
            out.close();
            in.close();

            RequestBody reqBody = RequestBody.create(MediaType.parse("image/*"), tempFile);
            MultipartBody.Part part = MultipartBody.Part.createFormData("file", tempFile.getName(), reqBody);

            ApiClient.getApiService().uploadSingleImage(auth, part).enqueue(new Callback<UploadResponse>() {
                @Override
                public void onResponse(@NonNull Call<UploadResponse> call, @NonNull Response<UploadResponse> response) {
                    isUploadingImage = false;
                    if (isFinishing() || isDestroyed()) return;
                    if (response.isSuccessful() && response.body() != null && response.body().data != null) {
                        String url = response.body().data.url;
                        if (url == null || url.trim().isEmpty()) url = response.body().data.fullUrl;
                        String caption = edtChatMessage != null ? edtChatMessage.getText().toString().trim() : "";
                        sendMessageToBackend(caption, url);
                    } else {
                        Toast.makeText(ChatActivity.this, "Không thể tải ảnh lên", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onFailure(@NonNull Call<UploadResponse> call, @NonNull Throwable t) {
                    isUploadingImage = false;
                    if (!isFinishing()) {
                        Toast.makeText(ChatActivity.this, "Lỗi kết nối khi tải ảnh", Toast.LENGTH_SHORT).show();
                    }
                }
            });
        } catch (Exception e) {
            isUploadingImage = false;
            Toast.makeText(this, "Không thể đọc tệp ảnh", Toast.LENGTH_SHORT).show();
        }
    }

    private void sendMessageToBackend(String content, String mediaUrl) {
        if (isSending) return;
        String auth = ApiClient.getAuthHeader(this);
        if (auth.isEmpty()) {
            Toast.makeText(this, "Vui lòng đăng nhập lại", Toast.LENGTH_SHORT).show();
            return;
        }
        isSending = true;
        if (btnSendMessage != null) btnSendMessage.setEnabled(false);

        SendChatMessageRequest req = new SendChatMessageRequest(content != null ? content : "", mediaUrl);
        ApiClient.getApiService().sendChatMessage(auth, targetUserId, req).enqueue(new Callback<SendChatMessageResponse>() {
            @Override
            public void onResponse(@NonNull Call<SendChatMessageResponse> call, @NonNull Response<SendChatMessageResponse> response) {
                isSending = false;
                if (btnSendMessage != null) btnSendMessage.setEnabled(true);
                if (isFinishing() || isDestroyed()) return;

                if (response.isSuccessful() && response.body() != null && response.body().data != null) {
                    if (edtChatMessage != null) edtChatMessage.setText("");
                    currentMessages.add(response.body().data);
                    renderMessages();
                    scrollToBottom();
                } else {
                    Toast.makeText(ChatActivity.this, "Không thể gửi tin nhắn (mã " + response.code() + ")", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<SendChatMessageResponse> call, @NonNull Throwable t) {
                isSending = false;
                if (btnSendMessage != null) btnSendMessage.setEnabled(true);
                if (!isFinishing()) {
                    Toast.makeText(ChatActivity.this, "Lỗi kết nối máy chủ", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void scrollToBottom() {
        if (scrollChatHistory != null) {
            scrollChatHistory.post(() -> scrollChatHistory.fullScroll(View.FOCUS_DOWN));
        }
    }

    private void scrollToMessage(int msgId) {
        if (scrollChatHistory == null) return;
        View target = messageViewMap.get(msgId);
        if (target == null) {
            scrollToBottom();
            return;
        }
        scrollChatHistory.post(() -> {
            scrollChatHistory.smoothScrollTo(0, Math.max(0, target.getTop() - dpToPx(24)));
            target.setAlpha(0.55f);
            target.animate().alpha(1f).setDuration(450).start();
        });
    }

    private String safeTrim(String s) {
        return s != null ? s.trim() : "";
    }

    private int dpToPx(int dp) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, getResources().getDisplayMetrics());
    }
}
