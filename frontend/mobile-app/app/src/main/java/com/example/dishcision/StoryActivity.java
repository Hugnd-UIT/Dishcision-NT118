package com.example.dishcision;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.dishcision.network.ApiClient;
import com.example.dishcision.network.ImageLoader;
import com.example.dishcision.network.SendChatMessageRequest;
import com.example.dishcision.network.SendChatMessageResponse;
import com.example.dishcision.network.StoriesResponse;
import com.example.dishcision.network.StoryModel;
import com.example.dishcision.network.TokenManager;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class StoryActivity extends AppCompatActivity {

    public static final String EXTRA_USER_ID = "extra_user_id";
    public static final String EXTRA_STORE_NAME = "extra_store_name";
    public static final String EXTRA_STORY_ID = "extra_story_id";

    private ImageView btnCloseStory;
    private ImageView imgStoryPhoto;
    private ImageView imgStoryAvatar;
    private LinearLayout layoutStoryAuthorInfo;
    private View containerStoryAvatar;
    private TextView tvStoryStoreName;
    private TextView tvStoryMeta;
    private TextView tvStoryTitle;
    private TextView tvStorySubtitle;
    private View layoutReplyInput;
    private EditText edtReply;
    private ImageView btnSendReply;
    private ImageView btnStoryShare;
    private View btnPrevStory;
    private View btnNextStory;
    private LinearLayout progressSegments;

    private final List<StoryModel> stories = new ArrayList<>();
    private final List<View> segmentViews = new ArrayList<>();
    private int currentStoryIndex = 0;
    private int filterUserId = -1;
    private String filterStoreName = null;
    private int targetStoryId = -1;
    private int currentLoggedInUserId = -1;
    private boolean isSendingReply = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_story);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main_story), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        ApiClient.init(this);
        TokenManager tm = new TokenManager(this);
        currentLoggedInUserId = tm.getUserId();

        filterUserId = getIntent().getIntExtra(EXTRA_USER_ID, -1);
        if (filterUserId <= 0 && getIntent().hasExtra("USER_ID")) {
            filterUserId = getIntent().getIntExtra("USER_ID", -1);
        }
        filterStoreName = getIntent().getStringExtra(EXTRA_STORE_NAME);
        if (TextUtils.isEmpty(filterStoreName) && getIntent().hasExtra("STORE_NAME")) {
            filterStoreName = getIntent().getStringExtra("STORE_NAME");
        }
        targetStoryId = getIntent().getIntExtra(EXTRA_STORY_ID, -1);
        if (targetStoryId <= 0 && getIntent().hasExtra("STORY_ID")) {
            targetStoryId = getIntent().getIntExtra("STORY_ID", -1);
        }

        initViews();
        setupListeners();
        loadStories();
    }

    private void initViews() {
        btnCloseStory = findViewById(R.id.btnCloseStory);
        imgStoryPhoto = findViewById(R.id.imgStoryPhoto);
        imgStoryAvatar = findViewById(R.id.imgStoryAvatar);
        containerStoryAvatar = findViewById(R.id.containerStoryAvatar);
        layoutStoryAuthorInfo = findViewById(R.id.layoutStoryAuthorInfo);
        tvStoryStoreName = findViewById(R.id.tvStoryStoreName);
        tvStoryMeta = findViewById(R.id.tvStoryMeta);
        tvStoryTitle = findViewById(R.id.tvStoryTitle);
        tvStorySubtitle = findViewById(R.id.tvStorySubtitle);
        layoutReplyInput = findViewById(R.id.layoutReplyInput);
        edtReply = findViewById(R.id.edtReply);
        btnSendReply = findViewById(R.id.btnSendReply);
        btnStoryShare = findViewById(R.id.btnStoryShare);
        btnPrevStory = findViewById(R.id.btnPrevStory);
        btnNextStory = findViewById(R.id.btnNextStory);
        progressSegments = findViewById(R.id.progressSegments);
    }

    private void loadStories() {
        Integer userIdParam = filterUserId > 0 ? filterUserId : null;
        String storeNameParam = !TextUtils.isEmpty(filterStoreName) ? filterStoreName.trim() : null;

        ApiClient.getApiService().getStories(userIdParam, storeNameParam).enqueue(new Callback<StoriesResponse>() {
            @Override
            public void onResponse(@NonNull Call<StoriesResponse> call, @NonNull Response<StoriesResponse> response) {
                if (isFinishing() || isDestroyed()) return;

                if (!response.isSuccessful()) {
                    Toast.makeText(StoryActivity.this, "Không thể tải story (mã " + response.code() + ")", Toast.LENGTH_SHORT).show();
                    finish();
                    return;
                }

                if (response.body() == null || response.body().data == null || response.body().data.isEmpty()) {
                    Toast.makeText(StoryActivity.this, "Chưa có story nào để hiển thị", Toast.LENGTH_SHORT).show();
                    finish();
                    return;
                }

                stories.clear();
                stories.addAll(response.body().data);
                currentStoryIndex = 0;
                if (targetStoryId > 0) {
                    for (int i = 0; i < stories.size(); i++) {
                        if (stories.get(i).id == targetStoryId) {
                            currentStoryIndex = i;
                            break;
                        }
                    }
                }

                buildProgressSegments();
                updateStoryView();
            }

            @Override
            public void onFailure(@NonNull Call<StoriesResponse> call, @NonNull Throwable t) {
                if (isFinishing() || isDestroyed()) return;
                Toast.makeText(StoryActivity.this, "Không thể tải story: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                finish();
            }
        });
    }

    private void buildProgressSegments() {
        if (progressSegments == null) return;
        progressSegments.removeAllViews();
        segmentViews.clear();

        int count = stories.size();
        int marginPx = (int) (4 * getResources().getDisplayMetrics().density);
        for (int i = 0; i < count; i++) {
            View seg = new View(this);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.MATCH_PARENT, 1f);
            if (i > 0) params.setMarginStart(marginPx);
            seg.setLayoutParams(params);
            seg.setBackgroundColor(0x66FFFFFF);
            progressSegments.addView(seg);
            segmentViews.add(seg);
        }
    }

    private void updateStoryView() {
        if (stories.isEmpty()) return;
        if (currentStoryIndex < 0) currentStoryIndex = 0;
        if (currentStoryIndex >= stories.size()) currentStoryIndex = stories.size() - 1;

        StoryModel story = stories.get(currentStoryIndex);
        String displayName = !TextUtils.isEmpty(story.userFullName)
                ? story.userFullName
                : (!TextUtils.isEmpty(story.username) ? story.username : "Dishcision");

        if (tvStoryStoreName != null) tvStoryStoreName.setText(displayName);
        if (tvStoryTitle != null) tvStoryTitle.setText(displayName);
        if (tvStoryMeta != null) {
            tvStoryMeta.setText(!TextUtils.isEmpty(story.username) ? "@" + story.username : "");
        }
        if (tvStorySubtitle != null) {
            tvStorySubtitle.setText(story.caption != null ? story.caption : "");
        }

        if (imgStoryPhoto != null) {
            imgStoryPhoto.setImageDrawable(null);
            if (!TextUtils.isEmpty(story.mediaUrl)) {
                ImageLoader.loadImage(imgStoryPhoto, story.mediaUrl);
            }
        }

        if (imgStoryAvatar != null) {
            imgStoryAvatar.setImageResource(R.drawable.ic_profile);
            if (!TextUtils.isEmpty(story.userAvatar)) {
                ImageLoader.loadAvatar(imgStoryAvatar, story.userAvatar);
            }
        }

        boolean isOwnStory = currentLoggedInUserId > 0 && story.userId == currentLoggedInUserId;
        if (layoutReplyInput != null) {
            layoutReplyInput.setVisibility(isOwnStory ? View.GONE : View.VISIBLE);
        }
        if (btnSendReply != null) {
            btnSendReply.setVisibility(isOwnStory ? View.GONE : View.VISIBLE);
        }
        if (edtReply != null && !isOwnStory) {
            edtReply.setHint("Nhắn tin cho " + displayName + "...");
        }

        for (int i = 0; i < segmentViews.size(); i++) {
            segmentViews.get(i).setBackgroundColor(currentStoryIndex >= i ? 0xFFFFFFFF : 0x66FFFFFF);
        }
    }

    private void setupListeners() {
        if (btnCloseStory != null) {
            btnCloseStory.setOnClickListener(v -> finish());
        }

        View.OnClickListener openAuthorProfile = v -> {
            if (stories.isEmpty()) return;
            StoryModel story = stories.get(currentStoryIndex);
            if (story.userId > 0) {
                Intent intent = new Intent(this, UserProfileActivity.class);
                intent.putExtra("USER_ID", String.valueOf(story.userId));
                intent.putExtra("USER_NAME", story.userFullName);
                startActivity(intent);
            }
        };
        if (containerStoryAvatar != null) containerStoryAvatar.setOnClickListener(openAuthorProfile);
        if (layoutStoryAuthorInfo != null) layoutStoryAuthorInfo.setOnClickListener(openAuthorProfile);

        if (btnPrevStory != null) {
            btnPrevStory.setOnClickListener(v -> {
                if (stories.isEmpty()) return;
                if (currentStoryIndex > 0) {
                    currentStoryIndex--;
                    updateStoryView();
                } else {
                    Toast.makeText(this, "Đã ở story đầu tiên", Toast.LENGTH_SHORT).show();
                }
            });
        }

        if (btnNextStory != null) {
            btnNextStory.setOnClickListener(v -> {
                if (stories.isEmpty()) {
                    finish();
                    return;
                }
                if (currentStoryIndex < stories.size() - 1) {
                    currentStoryIndex++;
                    updateStoryView();
                } else {
                    finish();
                }
            });
        }

        if (btnStoryShare != null) {
            btnStoryShare.setOnClickListener(v -> shareCurrentStory());
        }

        if (btnSendReply != null) {
            btnSendReply.setOnClickListener(v -> sendStoryReply());
        }

        if (edtReply != null) {
            edtReply.setOnEditorActionListener((v, actionId, event) -> {
                if (actionId == EditorInfo.IME_ACTION_SEND || actionId == EditorInfo.IME_ACTION_DONE) {
                    sendStoryReply();
                    return true;
                }
                return false;
            });
        }
    }

    private void sendStoryReply() {
        if (stories.isEmpty() || edtReply == null || isSendingReply) return;
        String replyText = edtReply.getText().toString().trim();
        if (replyText.isEmpty()) return;

        String authHeader = ApiClient.getAuthHeader(this);
        if (authHeader.isEmpty()) {
            Toast.makeText(this, "Vui lòng đăng nhập để nhắn tin phản hồi", Toast.LENGTH_SHORT).show();
            return;
        }

        StoryModel story = stories.get(currentStoryIndex);
        if (story.userId <= 0 || (currentLoggedInUserId > 0 && story.userId == currentLoggedInUserId)) {
            return;
        }

        isSendingReply = true;
        String payloadText = !TextUtils.isEmpty(story.caption)
                ? "Phản hồi story (\"" + story.caption + "\"): " + replyText
                : "Phản hồi story: " + replyText;

        ApiClient.getApiService().sendChatMessage(authHeader, story.userId, new SendChatMessageRequest(payloadText, null))
                .enqueue(new Callback<SendChatMessageResponse>() {
                    @Override
                    public void onResponse(@NonNull Call<SendChatMessageResponse> call, @NonNull Response<SendChatMessageResponse> response) {
                        isSendingReply = false;
                        if (response.isSuccessful()) {
                            edtReply.setText("");
                            Toast.makeText(StoryActivity.this, "Đã gửi phản hồi qua tin nhắn", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(StoryActivity.this, "Không thể gửi phản hồi (mã " + response.code() + ")", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(@NonNull Call<SendChatMessageResponse> call, @NonNull Throwable t) {
                        isSendingReply = false;
                        Toast.makeText(StoryActivity.this, "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void shareCurrentStory() {
        if (stories.isEmpty()) return;
        StoryModel story = stories.get(currentStoryIndex);
        String authorName = !TextUtils.isEmpty(story.userFullName) ? story.userFullName : story.username;
        StringBuilder shareText = new StringBuilder();
        shareText.append("Xem story của ").append(authorName).append(" trên Dishcision");
        if (!TextUtils.isEmpty(story.caption)) {
            shareText.append(": ").append(story.caption);
        }
        String mediaLink = ApiClient.normalizeMediaUrl(story.mediaUrl);
        if (!TextUtils.isEmpty(mediaLink)) {
            shareText.append("\n").append(mediaLink);
        }

        Intent sendIntent = new Intent(Intent.ACTION_SEND);
        sendIntent.setType("text/plain");
        sendIntent.putExtra(Intent.EXTRA_TEXT, shareText.toString());
        startActivity(Intent.createChooser(sendIntent, "Chia sẻ story"));
    }
}
