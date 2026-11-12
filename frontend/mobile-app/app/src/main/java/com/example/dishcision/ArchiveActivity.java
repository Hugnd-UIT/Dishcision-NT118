package com.example.dishcision;

import android.content.Intent;
import android.graphics.Color;
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
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.dishcision.network.ApiClient;
import com.example.dishcision.network.AuthHelper;
import com.example.dishcision.network.CreatePostResponse;
import com.example.dishcision.network.FeedPostsResponse;
import com.example.dishcision.network.ImageLoader;
import com.example.dishcision.network.StoriesResponse;
import com.example.dishcision.network.StoryModel;
import com.example.dishcision.network.UpdatePostRequest;
import com.example.dishcision.network.UserProfileResponse;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ArchiveActivity extends AppCompatActivity {

    private ImageView btnBack;
    private LinearLayout tabArchivedPosts;
    private LinearLayout tabArchivedStory;
    private TextView tvTabArchivedPosts;
    private TextView tvTabArchivedStory;
    private TextView tvArchiveSectionTitle;
    private ProgressBar progressBarArchive;
    private LinearLayout containerArchiveList;
    private TextView tvArchiveEmpty;

    private boolean showingStories = false;
    private int currentUserId = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_archive);
        ApiClient.init(this);
        currentUserId = AuthHelper.getCurrentUserId(this);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(android.R.id.content), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        initViews();
        setupListeners();
        selectTab(false);
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadActiveTab();
    }

    private void initViews() {
        btnBack = findViewById(R.id.btnBack);
        tabArchivedPosts = findViewById(R.id.tabArchivedPosts);
        tabArchivedStory = findViewById(R.id.tabArchivedStory);
        tvTabArchivedPosts = findViewById(R.id.tvTabArchivedPosts);
        tvTabArchivedStory = findViewById(R.id.tvTabArchivedStory);
        tvArchiveSectionTitle = findViewById(R.id.tvArchiveSectionTitle);
        progressBarArchive = findViewById(R.id.progressBarArchive);
        containerArchiveList = findViewById(R.id.containerArchiveList);
        tvArchiveEmpty = findViewById(R.id.tvArchiveEmpty);
    }

    private void setupListeners() {
        if (btnBack != null) {
            btnBack.setOnClickListener(v -> finish());
        }
        if (tabArchivedPosts != null) {
            tabArchivedPosts.setOnClickListener(v -> selectTab(false));
        }
        if (tabArchivedStory != null) {
            tabArchivedStory.setOnClickListener(v -> selectTab(true));
        }
    }

    private void selectTab(boolean stories) {
        showingStories = stories;
        float density = getResources().getDisplayMetrics().density;

        if (tabArchivedPosts != null) {
            tabArchivedPosts.setBackgroundResource(!stories ? R.drawable.bg_tab_active_white : 0);
            tabArchivedPosts.setElevation(!stories ? density : 0f);
        }
        if (tvTabArchivedPosts != null) {
            tvTabArchivedPosts.setTextColor(Color.parseColor(!stories ? "#FF5A36" : "#505568"));
            tvTabArchivedPosts.setTypeface(null, !stories ? Typeface.BOLD : Typeface.NORMAL);
        }

        if (tabArchivedStory != null) {
            tabArchivedStory.setBackgroundResource(stories ? R.drawable.bg_tab_active_white : 0);
            tabArchivedStory.setElevation(stories ? density : 0f);
        }
        if (tvTabArchivedStory != null) {
            tvTabArchivedStory.setTextColor(Color.parseColor(stories ? "#FF5A36" : "#505568"));
            tvTabArchivedStory.setTypeface(null, stories ? Typeface.BOLD : Typeface.NORMAL);
        }

        if (tvArchiveSectionTitle != null) {
            tvArchiveSectionTitle.setText(stories ? "Story của bạn" : "Bài viết đã lưu trữ");
        }
        loadActiveTab();
    }

    private void loadActiveTab() {
        if (showingStories) {
            loadArchivedStories();
        } else {
            loadArchivedPosts();
        }
    }

    private void showLoading(boolean loading) {
        if (progressBarArchive != null) progressBarArchive.setVisibility(loading ? View.VISIBLE : View.GONE);
        if (loading) {
            if (containerArchiveList != null) containerArchiveList.setVisibility(View.GONE);
            if (tvArchiveEmpty != null) tvArchiveEmpty.setVisibility(View.GONE);
        }
    }

    private void loadArchivedPosts() {
        String auth = ApiClient.getAuthHeader(this);
        if (auth.isEmpty()) {
            renderEmpty("Vui lòng đăng nhập để xem kho lưu trữ.");
            return;
        }
        showLoading(true);
        ApiClient.getApiService().getMyPostsByStatus(auth, "Hidden").enqueue(new Callback<FeedPostsResponse>() {
            @Override
            public void onResponse(@NonNull Call<FeedPostsResponse> call, @NonNull Response<FeedPostsResponse> response) {
                if (isFinishing() || isDestroyed() || showingStories) return;
                showLoading(false);
                if (response.isSuccessful() && response.body() != null && response.body().getData() != null) {
                    renderArchivedPosts(response.body().getData());
                } else {
                    renderEmpty("Không có bài viết nào trong kho lưu trữ.");
                }
            }

            @Override
            public void onFailure(@NonNull Call<FeedPostsResponse> call, @NonNull Throwable t) {
                if (isFinishing() || isDestroyed() || showingStories) return;
                showLoading(false);
                renderEmpty("Lỗi kết nối khi tải kho lưu trữ.");
            }
        });
    }

    private void loadArchivedStories() {
        String auth = ApiClient.getAuthHeader(this);
        if (currentUserId <= 0 && !auth.isEmpty()) {
            showLoading(true);
            ApiClient.getApiService().getMyProfile(auth).enqueue(new Callback<UserProfileResponse>() {
                @Override
                public void onResponse(@NonNull Call<UserProfileResponse> call, @NonNull Response<UserProfileResponse> response) {
                    if (isFinishing() || isDestroyed()) return;
                    if (response.isSuccessful() && response.body() != null && response.body().getData() != null) {
                        currentUserId = response.body().getData().userId;
                    }
                    fetchUserStories();
                }

                @Override
                public void onFailure(@NonNull Call<UserProfileResponse> call, @NonNull Throwable t) {
                    fetchUserStories();
                }
            });
            return;
        }
        fetchUserStories();
    }

    private void fetchUserStories() {
        if (currentUserId <= 0) {
            renderEmpty("Vui lòng đăng nhập để xem Story của bạn.");
            return;
        }
        showLoading(true);
        ApiClient.getApiService().getStories(currentUserId, null).enqueue(new Callback<StoriesResponse>() {
            @Override
            public void onResponse(@NonNull Call<StoriesResponse> call, @NonNull Response<StoriesResponse> response) {
                if (isFinishing() || isDestroyed() || !showingStories) return;
                showLoading(false);
                if (response.isSuccessful() && response.body() != null && response.body().data != null) {
                    List<StoryModel> mine = new ArrayList<>();
                    for (StoryModel s : response.body().data) {
                        if (s != null && s.userId == currentUserId) {
                            mine.add(s);
                        }
                    }
                    renderArchivedStories(mine);
                } else {
                    renderEmpty("Bạn chưa đăng Story nào.");
                }
            }

            @Override
            public void onFailure(@NonNull Call<StoriesResponse> call, @NonNull Throwable t) {
                if (isFinishing() || isDestroyed() || !showingStories) return;
                showLoading(false);
                renderEmpty("Lỗi kết nối khi tải Story.");
            }
        });
    }

    private void renderEmpty(String message) {
        if (containerArchiveList != null) {
            containerArchiveList.removeAllViews();
            containerArchiveList.setVisibility(View.GONE);
        }
        if (tvArchiveEmpty != null) {
            tvArchiveEmpty.setText(message);
            tvArchiveEmpty.setVisibility(View.VISIBLE);
        }
    }

    private void renderArchivedPosts(List<FeedPostsResponse.PostItem> posts) {
        if (containerArchiveList == null) return;
        containerArchiveList.removeAllViews();
        if (posts == null || posts.isEmpty()) {
            renderEmpty("Chưa có bài viết nào được lưu trữ.");
            return;
        }
        if (tvArchiveEmpty != null) tvArchiveEmpty.setVisibility(View.GONE);
        containerArchiveList.setVisibility(View.VISIBLE);

        for (FeedPostsResponse.PostItem post : posts) {
            LinearLayout card = createCardShell();

            LinearLayout topRow = new LinearLayout(this);
            topRow.setOrientation(LinearLayout.HORIZONTAL);
            topRow.setGravity(Gravity.CENTER_VERTICAL);

            ImageView thumb = new ImageView(this);
            thumb.setLayoutParams(new LinearLayout.LayoutParams(dp(72), dp(72)));
            thumb.setBackgroundResource(R.drawable.bg_image_thumbnail);
            thumb.setScaleType(ImageView.ScaleType.CENTER_CROP);
            if (post.images != null && !post.images.isEmpty() && post.images.get(0).imageUrl != null && !post.images.get(0).imageUrl.trim().isEmpty()) {
                ImageLoader.loadImage(thumb, post.images.get(0).imageUrl);
            } else {
                thumb.setImageResource(R.drawable.ic_image);
                int pad = dp(18);
                thumb.setPadding(pad, pad, pad, pad);
            }

            LinearLayout meta = new LinearLayout(this);
            LinearLayout.LayoutParams metaLp = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f);
            metaLp.setMarginStart(dp(12));
            meta.setLayoutParams(metaLp);
            meta.setOrientation(LinearLayout.VERTICAL);

            TextView tvTitle = new TextView(this);
            String title = post.content != null && !post.content.trim().isEmpty() ? post.content.trim() : "Bài viết không có nội dung chữ";
            tvTitle.setText(title);
            tvTitle.setMaxLines(2);
            tvTitle.setEllipsize(android.text.TextUtils.TruncateAt.END);
            tvTitle.setTextColor(Color.parseColor("#141B2B"));
            tvTitle.setTextSize(TypedValue.COMPLEX_UNIT_SP, 15);
            tvTitle.setTypeface(null, Typeface.BOLD);

            TextView tvDate = new TextView(this);
            LinearLayout.LayoutParams dateLp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            dateLp.topMargin = dp(4);
            tvDate.setLayoutParams(dateLp);
            String loc = PostFeedHelper.decodeLocationDisplay(post.locationUrl);
            String dateStr = MyPostsActivity.formatTime(post.createdAt);
            tvDate.setText(loc.isEmpty() ? "Đã lưu trữ · " + dateStr : "Đã lưu trữ · " + loc + " · " + dateStr);
            tvDate.setTextColor(Color.parseColor("#6B7280"));
            tvDate.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12);

            meta.addView(tvTitle);
            meta.addView(tvDate);
            topRow.addView(thumb);
            topRow.addView(meta);

            LinearLayout actionsRow = new LinearLayout(this);
            LinearLayout.LayoutParams actionsLp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            actionsLp.topMargin = dp(12);
            actionsRow.setLayoutParams(actionsLp);
            actionsRow.setOrientation(LinearLayout.HORIZONTAL);

            TextView btnView = createActionButton("Xem bài viết", false);
            LinearLayout.LayoutParams viewLp = new LinearLayout.LayoutParams(0, dp(38), 1f);
            btnView.setLayoutParams(viewLp);
            btnView.setOnClickListener(v -> showPostPreview(post));

            TextView btnRestore = createActionButton("Khôi phục về hồ sơ", true);
            LinearLayout.LayoutParams restoreLp = new LinearLayout.LayoutParams(0, dp(38), 1f);
            restoreLp.setMarginStart(dp(12));
            btnRestore.setLayoutParams(restoreLp);
            btnRestore.setOnClickListener(v -> restorePost(post, btnRestore));

            actionsRow.addView(btnView);
            actionsRow.addView(btnRestore);

            card.addView(topRow);
            card.addView(actionsRow);
            containerArchiveList.addView(card);
        }
    }

    private void renderArchivedStories(List<StoryModel> stories) {
        if (containerArchiveList == null) return;
        containerArchiveList.removeAllViews();
        if (stories == null || stories.isEmpty()) {
            renderEmpty("Bạn chưa có Story nào trong kho lưu trữ.");
            return;
        }
        if (tvArchiveEmpty != null) tvArchiveEmpty.setVisibility(View.GONE);
        containerArchiveList.setVisibility(View.VISIBLE);

        for (StoryModel story : stories) {
            LinearLayout card = createCardShell();

            LinearLayout topRow = new LinearLayout(this);
            topRow.setOrientation(LinearLayout.HORIZONTAL);
            topRow.setGravity(Gravity.CENTER_VERTICAL);

            ImageView thumb = new ImageView(this);
            thumb.setLayoutParams(new LinearLayout.LayoutParams(dp(72), dp(72)));
            thumb.setBackgroundResource(R.drawable.bg_image_thumbnail);
            thumb.setScaleType(ImageView.ScaleType.CENTER_CROP);
            if (story.mediaUrl != null && !story.mediaUrl.trim().isEmpty()) {
                ImageLoader.loadImage(thumb, story.mediaUrl);
            }

            LinearLayout meta = new LinearLayout(this);
            LinearLayout.LayoutParams metaLp = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f);
            metaLp.setMarginStart(dp(12));
            meta.setLayoutParams(metaLp);
            meta.setOrientation(LinearLayout.VERTICAL);

            TextView tvTitle = new TextView(this);
            tvTitle.setText(story.caption != null && !story.caption.trim().isEmpty() ? story.caption.trim() : "Story ẩm thực");
            tvTitle.setMaxLines(2);
            tvTitle.setEllipsize(android.text.TextUtils.TruncateAt.END);
            tvTitle.setTextColor(Color.parseColor("#141B2B"));
            tvTitle.setTextSize(TypedValue.COMPLEX_UNIT_SP, 15);
            tvTitle.setTypeface(null, Typeface.BOLD);

            TextView tvDate = new TextView(this);
            LinearLayout.LayoutParams dateLp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            dateLp.topMargin = dp(4);
            tvDate.setLayoutParams(dateLp);
            tvDate.setText("Story · " + MyPostsActivity.formatTime(story.createdAt));
            tvDate.setTextColor(Color.parseColor("#6B7280"));
            tvDate.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12);

            meta.addView(tvTitle);
            meta.addView(tvDate);
            topRow.addView(thumb);
            topRow.addView(meta);

            TextView btnOpen = createActionButton("Xem Story", false);
            LinearLayout.LayoutParams btnLp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, dp(38));
            btnLp.topMargin = dp(12);
            btnOpen.setLayoutParams(btnLp);
            btnOpen.setOnClickListener(v -> {
                Intent intent = new Intent(ArchiveActivity.this, StoryActivity.class);
                intent.putExtra(StoryActivity.EXTRA_STORY_ID, story.id);
                intent.putExtra(StoryActivity.EXTRA_USER_ID, story.userId);
                String storeOrAuthorName = story.userFullName != null && !story.userFullName.isEmpty() ? story.userFullName : story.username;
                intent.putExtra(StoryActivity.EXTRA_STORE_NAME, storeOrAuthorName);
                intent.putExtra("STORY_ID", story.id);
                intent.putExtra("USER_ID", story.userId);
                intent.putExtra("STORE_NAME", storeOrAuthorName);
                intent.putExtra("STORY_CAPTION", story.caption);
                intent.putExtra("MEDIA_URL", story.mediaUrl);
                intent.putExtra("AVATAR_URL", story.userAvatar);
                startActivity(intent);
            });

            card.addView(topRow);
            card.addView(btnOpen);
            containerArchiveList.addView(card);
        }
    }

    private void showPostPreview(FeedPostsResponse.PostItem post) {
        if (post.images != null && !post.images.isEmpty()) {
            ArrayList<String> urls = new ArrayList<>();
            for (FeedPostsResponse.PostImage img : post.images) {
                if (img != null && img.imageUrl != null && !img.imageUrl.trim().isEmpty()) {
                    urls.add(img.imageUrl.trim());
                }
            }
            if (!urls.isEmpty()) {
                String author = post.author != null && post.author.fullName != null ? post.author.fullName : "Bài viết của bạn";
                PostFeedHelper.openFullscreenImages(this, author, post.content, urls, 0);
                return;
            }
        }
        StringBuilder msg = new StringBuilder();
        if (post.content != null && !post.content.trim().isEmpty()) {
            msg.append(post.content.trim());
        }
        String loc = PostFeedHelper.decodeLocationDisplay(post.locationUrl);
        if (!loc.isEmpty()) {
            if (msg.length() > 0) msg.append("\n\n");
            msg.append("Địa điểm: ").append(loc);
        }
        if (post.voucherCode != null && !post.voucherCode.trim().isEmpty()) {
            if (msg.length() > 0) msg.append("\n");
            msg.append("Mã ưu đãi: ").append(post.voucherCode.trim());
        }
        String tags = PostFeedHelper.formatHashtags(post.hashtags);
        if (!tags.isEmpty()) {
            if (msg.length() > 0) msg.append("\n");
            msg.append(tags);
        }
        new AlertDialog.Builder(this)
                .setTitle("Bài viết đã lưu trữ")
                .setMessage(msg.length() > 0 ? msg.toString() : "Không có nội dung")
                .setPositiveButton("Đóng", null)
                .show();
    }

    private void restorePost(FeedPostsResponse.PostItem post, TextView btnRestore) {
        String auth = ApiClient.getAuthHeader(this);
        if (auth.isEmpty()) return;
        btnRestore.setEnabled(false);

        UpdatePostRequest req = new UpdatePostRequest();
        req.status = "Active";
        ApiClient.getApiService().updatePost(auth, post.postId, req).enqueue(new Callback<CreatePostResponse>() {
            @Override
            public void onResponse(@NonNull Call<CreatePostResponse> call, @NonNull Response<CreatePostResponse> response) {
                btnRestore.setEnabled(true);
                if (isFinishing() || isDestroyed()) return;
                if (response.isSuccessful()) {
                    Toast.makeText(ArchiveActivity.this, "Đã khôi phục bài viết về hồ sơ!", Toast.LENGTH_SHORT).show();
                    loadArchivedPosts();
                } else {
                    Toast.makeText(ArchiveActivity.this, "Không thể khôi phục (mã " + response.code() + ")", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<CreatePostResponse> call, @NonNull Throwable t) {
                btnRestore.setEnabled(true);
                if (!isFinishing() && !isDestroyed()) {
                    Toast.makeText(ArchiveActivity.this, "Lỗi kết nối máy chủ", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private LinearLayout createCardShell() {
        LinearLayout card = new LinearLayout(this);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        lp.bottomMargin = dp(12);
        card.setLayoutParams(lp);
        card.setBackgroundResource(R.drawable.bg_card_white);
        card.setOrientation(LinearLayout.VERTICAL);
        int pad = dp(16);
        card.setPadding(pad, pad, pad, pad);
        return card;
    }

    private TextView createActionButton(String text, boolean highlight) {
        TextView btn = new TextView(this);
        btn.setGravity(Gravity.CENTER);
        btn.setText(text);
        btn.setTextSize(TypedValue.COMPLEX_UNIT_SP, 13);
        btn.setTypeface(null, highlight ? Typeface.BOLD : Typeface.NORMAL);
        btn.setTextColor(Color.parseColor(highlight ? "#FFFFFF" : "#141B2B"));
        btn.setBackgroundResource(highlight ? R.drawable.bg_button_orange_small : R.drawable.bg_button_light_small);
        btn.setClickable(true);
        btn.setFocusable(true);
        return btn;
    }

    private int dp(int value) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, value, getResources().getDisplayMetrics());
    }
}
