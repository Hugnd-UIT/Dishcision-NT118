package com.example.dishcision;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.dishcision.network.ApiClient;
import com.example.dishcision.network.AuthHelper;
import com.example.dishcision.network.FeedPostsResponse;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MyPostsActivity extends AppCompatActivity {

    public static final String EXTRA_POST_INDEX = "POST_INDEX";
    public static final String EXTRA_POST_ID = "POST_ID";
    public static final String EXTRA_USER_ID = "USER_ID";
    public static final String EXTRA_USER_NAME = "USER_NAME";

    private ImageView btnBack;
    private ImageView btnNotification;
    private TextView tvHeaderTitle;
    private ScrollView scrollFeed;
    private LinearLayout feedContent;
    private LinearLayout myPostsContainer;
    private TextView tvEmptyMyPosts;

    private final List<FeedPostsResponse.PostItem> loadedPosts = new ArrayList<>();
    private final Map<Integer, View> postCardViews = new HashMap<>();

    private int targetUserId = 0;
    private String targetUserName = "";
    private int targetPostId = 0;
    private int targetPostIndex = 1;
    private boolean hasScrolledToInitialTarget = false;

    private final ActivityResultLauncher<Intent> editPostLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == RESULT_OK) {
                    fetchPostsFromBackend();
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_my_posts);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main_my_posts), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        readIntentExtras();
        initViews();
        setupListeners();
        fetchPostsFromBackend();
    }

    private void readIntentExtras() {
        Intent intent = getIntent();
        targetUserId = intent.getIntExtra(EXTRA_USER_ID, 0);
        if (targetUserId <= 0) {
            String raw = intent.getStringExtra(EXTRA_USER_ID);
            if (raw != null && !raw.trim().isEmpty()) {
                try {
                    targetUserId = Integer.parseInt(raw.trim());
                } catch (Exception ignored) {}
            }
        }
        String nameExtra = intent.getStringExtra(EXTRA_USER_NAME);
        targetUserName = nameExtra != null ? nameExtra.trim() : "";
        targetPostId = intent.getIntExtra(EXTRA_POST_ID, 0);
        targetPostIndex = intent.getIntExtra(EXTRA_POST_INDEX, 1);
    }

    private void initViews() {
        btnBack = findViewById(R.id.btnBack);
        btnNotification = findViewById(R.id.btnNotification);
        tvHeaderTitle = findViewById(R.id.tvHeaderTitle);
        scrollFeed = findViewById(R.id.scrollFeed);
        feedContent = findViewById(R.id.feedContent);
        myPostsContainer = findViewById(R.id.myPostsContainer);
        tvEmptyMyPosts = findViewById(R.id.tvEmptyMyPosts);

        int selfId = AuthHelper.getCurrentUserId(this);
        boolean isOwnPosts = targetUserId <= 0 || (selfId > 0 && targetUserId == selfId);
        if (tvHeaderTitle != null) {
            if (isOwnPosts) {
                tvHeaderTitle.setText("Bài viết của tôi");
            } else {
                tvHeaderTitle.setText(!targetUserName.isEmpty() ? "Bài viết của " + targetUserName : "Bài viết");
            }
        }
    }

    private void setupListeners() {
        if (btnBack != null) {
            btnBack.setOnClickListener(v -> finish());
        }

        if (btnNotification != null) {
            btnNotification.setOnClickListener(v -> startActivity(new Intent(MyPostsActivity.this, NotificationsActivity.class)));
        }
    }

    private void fetchPostsFromBackend() {
        String authHeader = ApiClient.getAuthHeader(this);
        int selfId = AuthHelper.getCurrentUserId(this);
        boolean isOwnPosts = targetUserId <= 0 || (selfId > 0 && targetUserId == selfId);

        if (isOwnPosts && authHeader.isEmpty()) {
            showEmptyState("Vui lòng đăng nhập lại để xem bài viết.");
            return;
        }

        Call<FeedPostsResponse> call = isOwnPosts
                ? ApiClient.getApiService().getMyPosts(authHeader)
                : ApiClient.getApiService().getUserPosts(authHeader, String.valueOf(targetUserId));

        call.enqueue(new Callback<FeedPostsResponse>() {
            @Override
            public void onResponse(@NonNull Call<FeedPostsResponse> call, @NonNull Response<FeedPostsResponse> response) {
                if (isFinishing() || isDestroyed()) return;
                if (response.isSuccessful() && response.body() != null && response.body().getData() != null) {
                    loadedPosts.clear();
                    loadedPosts.addAll(response.body().getData());
                    renderPosts();
                } else {
                    showEmptyState("Chưa thể tải danh sách bài viết.");
                }
            }

            @Override
            public void onFailure(@NonNull Call<FeedPostsResponse> call, @NonNull Throwable t) {
                if (isFinishing() || isDestroyed()) return;
                if (loadedPosts.isEmpty()) {
                    showEmptyState("Lỗi kết nối máy chủ.");
                } else {
                    Toast.makeText(MyPostsActivity.this, "Không thể làm mới bài viết", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void renderPosts() {
        if (myPostsContainer == null) return;
        myPostsContainer.removeAllViews();
        postCardViews.clear();

        if (loadedPosts.isEmpty()) {
            showEmptyState("Chưa có bài viết nào");
            return;
        }

        if (tvEmptyMyPosts != null) tvEmptyMyPosts.setVisibility(View.GONE);
        myPostsContainer.setVisibility(View.VISIBLE);

        for (FeedPostsResponse.PostItem post : loadedPosts) {
            View card = getLayoutInflater().inflate(R.layout.item_feed_post, myPostsContainer, false);
            PostFeedHelper.bindPostCard(this, card, post, editPostLauncher, new PostFeedHelper.PostCardListener() {
                @Override
                public void onPostDeleted(int postId) {
                    removePostCard(postId);
                }

                @Override
                public void onPostArchived(int postId) {
                    removePostCard(postId);
                }
            });
            myPostsContainer.addView(card);
            postCardViews.put(post.postId, card);
        }

        if (!hasScrolledToInitialTarget) {
            hasScrolledToInitialTarget = true;
            scrollToRequestedPost();
        }
    }

    private void removePostCard(int postId) {
        View card = postCardViews.remove(postId);
        if (card != null && myPostsContainer != null) {
            myPostsContainer.removeView(card);
        }
        for (int i = loadedPosts.size() - 1; i >= 0; i--) {
            if (loadedPosts.get(i).postId == postId) {
                loadedPosts.remove(i);
            }
        }
        if (loadedPosts.isEmpty()) {
            showEmptyState("Chưa có bài viết nào");
        }
    }

    private void showEmptyState(String message) {
        if (myPostsContainer != null) myPostsContainer.setVisibility(View.GONE);
        if (tvEmptyMyPosts != null) {
            tvEmptyMyPosts.setText(message);
            tvEmptyMyPosts.setVisibility(View.VISIBLE);
        }
    }

    private void scrollToRequestedPost() {
        if (scrollFeed == null || feedContent == null || loadedPosts.isEmpty()) return;

        View target = null;
        if (targetPostId > 0) {
            target = postCardViews.get(targetPostId);
        }
        if (target == null && targetPostIndex > 1) {
            int idx = Math.min(targetPostIndex - 1, loadedPosts.size() - 1);
            target = postCardViews.get(loadedPosts.get(idx).postId);
        }
        if (target == null) return;

        final View scrollTarget = target;
        scrollFeed.post(() -> {
            int targetTop = scrollTarget.getTop() - feedContent.getPaddingTop();
            int spaceBelow = feedContent.getHeight() - targetTop;
            int missing = scrollFeed.getHeight() - spaceBelow;
            if (missing > 0) {
                feedContent.setPadding(feedContent.getPaddingLeft(), feedContent.getPaddingTop(),
                        feedContent.getPaddingRight(), feedContent.getPaddingBottom() + missing);
                scrollFeed.post(() -> scrollFeed.scrollTo(0, Math.max(0, targetTop)));
            } else {
                scrollFeed.scrollTo(0, Math.max(0, targetTop));
            }
        });
    }

    public static String formatTime(String isoDateString) {
        if (isoDateString == null || isoDateString.trim().isEmpty()) {
            return "Vừa xong";
        }
        try {
            SimpleDateFormat sdf;
            if (isoDateString.contains(".")) {
                sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US);
            } else {
                sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US);
            }
            sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
            Date postDate = sdf.parse(isoDateString);
            if (postDate == null) return "Vừa xong";

            long now = System.currentTimeMillis();
            long diff = now - postDate.getTime();
            if (diff < 0) diff = 0;

            long diffSeconds = diff / 1000;
            long diffMinutes = diffSeconds / 60;
            long diffHours = diffMinutes / 60;
            long diffDays = diffHours / 24;

            if (diffSeconds < 60) {
                return "Vừa xong";
            } else if (diffMinutes < 60) {
                return diffMinutes + " phút trước";
            } else if (diffHours < 24) {
                return diffHours + " giờ trước";
            } else if (diffDays < 7) {
                return diffDays + " ngày trước";
            } else {
                Calendar calNow = Calendar.getInstance();
                Calendar calPost = Calendar.getInstance();
                calPost.setTime(postDate);
                if (calNow.get(Calendar.YEAR) == calPost.get(Calendar.YEAR)) {
                    return new SimpleDateFormat("d 'tháng' M", new Locale("vi", "VN")).format(postDate);
                } else {
                    return new SimpleDateFormat("d 'tháng' M, yyyy", new Locale("vi", "VN")).format(postDate);
                }
            }
        } catch (Exception e) {
            return "Vừa xong";
        }
    }
}
