package com.example.dishcision;

import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.dishcision.network.ApiClient;
import com.example.dishcision.network.FeedPostsResponse;
import com.example.dishcision.network.ImageLoader;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class FavoriteDishesActivity extends AppCompatActivity {

    private ImageView btnBack;
    private ImageView btnNotification;

    private LinearLayout savedPostsContainer;
    private TextView tvSavedCount;
    private TextView tvEmptySaved;
    private EditText edtSearch;
    private final List<FeedPostsResponse.PostItem> savedPosts = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_favorite_dishes);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main_favorite_dishes), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        initViews();
        setupListeners();
    }

    private void initViews() {
        btnBack = findViewById(R.id.btnBack);
        btnNotification = findViewById(R.id.btnNotification);

        savedPostsContainer = findViewById(R.id.savedPostsContainer);
        tvSavedCount = findViewById(R.id.tvSavedCount);
        tvEmptySaved = findViewById(R.id.tvEmptySaved);
        edtSearch = findViewById(R.id.edtSearch);
        if (edtSearch != null) {
            edtSearch.addTextChangedListener(new TextWatcher() {
                @Override public void beforeTextChanged(CharSequence s, int a, int b, int c) {}
                @Override public void onTextChanged(CharSequence s, int a, int b, int c) {}
                @Override public void afterTextChanged(Editable s) { renderSavedPosts(); }
            });
        }
    }

    private void setupListeners() {
        if (btnBack != null) {
            btnBack.setOnClickListener(v -> finish());
        }

        if (btnNotification != null) {
            btnNotification.setOnClickListener(v -> {
                Intent intent = new Intent(FavoriteDishesActivity.this, NotificationsActivity.class);
                startActivity(intent);
            });
        }

        BottomNavHelper.bind(this, BottomNavHelper.TAB_NONE);
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Refresh every time so posts saved/unsaved elsewhere show up
        fetchFavoriteDishesFromBackend();
    }

    private void fetchFavoriteDishesFromBackend() {
        String authHeader = ApiClient.getAuthHeader(this);
        ApiClient.getApiService().getSavedPosts(authHeader, 50).enqueue(new Callback<FeedPostsResponse>() {
            @Override
            public void onResponse(Call<FeedPostsResponse> call, Response<FeedPostsResponse> response) {
                if (isFinishing()) return;
                if (response.isSuccessful() && response.body() != null && response.body().getData() != null) {
                    savedPosts.clear();
                    savedPosts.addAll(response.body().getData());
                    renderSavedPosts();
                } else {
                    showMessage(response.code() == 401
                            ? "Phiên đăng nhập đã hết hạn. Vui lòng đăng xuất và đăng nhập lại để xem bài viết đã lưu."
                            : "Không tải được bài viết đã lưu (mã " + response.code() + ")");
                }
            }

            @Override
            public void onFailure(Call<FeedPostsResponse> call, Throwable t) {
                if (isFinishing()) return;
                showMessage("Không thể kết nối máy chủ. Vui lòng thử lại.");
            }
        });
    }

    private void renderSavedPosts() {
        if (savedPostsContainer == null) return;
        savedPostsContainer.removeAllViews();

        String query = edtSearch != null ? edtSearch.getText().toString().trim().toLowerCase(Locale.ROOT) : "";
        int shown = 0;
        for (FeedPostsResponse.PostItem post : savedPosts) {
            if (!query.isEmpty() && !matches(post, query)) continue;
            savedPostsContainer.addView(buildSavedPostView(post));
            shown++;
        }

        if (tvSavedCount != null) tvSavedCount.setText(savedPosts.size() + " bài đã lưu");
        if (tvEmptySaved != null) {
            if (savedPosts.isEmpty()) {
                tvEmptySaved.setText("Chưa có bài viết nào được lưu.\nBấm biểu tượng 🔖 trên một bài viết để lưu vào đây.");
                tvEmptySaved.setVisibility(View.VISIBLE);
            } else if (shown == 0) {
                tvEmptySaved.setText("Không có bài đã lưu nào khớp với \"" + query + "\".");
                tvEmptySaved.setVisibility(View.VISIBLE);
            } else {
                tvEmptySaved.setVisibility(View.GONE);
            }
        }
    }

    private void showMessage(String message) {
        if (tvSavedCount != null) tvSavedCount.setText(savedPosts.size() + " bài đã lưu");
        if (tvEmptySaved != null && savedPosts.isEmpty()) {
            tvEmptySaved.setText(message);
            tvEmptySaved.setVisibility(View.VISIBLE);
        } else {
            Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
        }
    }

    private static boolean matches(FeedPostsResponse.PostItem post, String query) {
        StringBuilder haystack = new StringBuilder();
        if (post.content != null) haystack.append(post.content).append(' ');
        if (post.locationUrl != null) haystack.append(post.locationUrl).append(' ');
        if (post.author != null) {
            if (post.author.fullName != null) haystack.append(post.author.fullName).append(' ');
            if (post.author.username != null) haystack.append(post.author.username).append(' ');
        }
        if (post.hashtags != null) for (String h : post.hashtags) haystack.append(h).append(' ');
        return haystack.toString().toLowerCase(Locale.ROOT).contains(query);
    }

    private View buildSavedPostView(FeedPostsResponse.PostItem post) {
        View item = LayoutInflater.from(this).inflate(R.layout.item_saved_post, savedPostsContainer, false);

        ImageView img = item.findViewById(R.id.imgSavedPost);
        if (post.images != null && !post.images.isEmpty() && post.images.get(0).imageUrl != null) {
            ImageLoader.loadImage(img, post.images.get(0).imageUrl);
        } else {
            img.setVisibility(View.GONE);
        }

        ImageView avatar = item.findViewById(R.id.imgSavedAuthor);
        TextView author = item.findViewById(R.id.tvSavedAuthor);
        String authorName = "Người dùng";
        if (post.author != null) {
            if (post.author.fullName != null && !post.author.fullName.isEmpty()) authorName = post.author.fullName;
            else if (post.author.username != null) authorName = post.author.username;
            if (post.author.avatarUrl != null && !post.author.avatarUrl.isEmpty()) {
                ImageLoader.loadAvatar(avatar, post.author.avatarUrl);
            }
        }
        author.setText(authorName);

        TextView meta = item.findViewById(R.id.tvSavedMeta);
        String time = MyPostsActivity.formatTime(post.createdAt);
        meta.setText(post.locationUrl != null && !post.locationUrl.isEmpty() ? post.locationUrl + " • " + time : time);

        TextView content = item.findViewById(R.id.tvSavedContent);
        content.setText(post.content != null ? post.content : "");
        // Tap the caption to expand / collapse it
        content.setOnClickListener(v -> content.setMaxLines(content.getMaxLines() == 3 ? Integer.MAX_VALUE : 3));

        TextView hashtags = item.findViewById(R.id.tvSavedHashtags);
        if (post.hashtags != null && !post.hashtags.isEmpty()) {
            StringBuilder sb = new StringBuilder();
            for (String h : post.hashtags) sb.append('#').append(h).append(' ');
            hashtags.setText(sb.toString().trim());
            hashtags.setVisibility(View.VISIBLE);
        }

        TextView stats = item.findViewById(R.id.tvSavedStats);
        stats.setText(post.likesCount + " thích • " + post.commentsCount + " bình luận");

        ImageView btnUnsave = item.findViewById(R.id.btnUnsave);
        btnUnsave.setOnClickListener(v -> confirmUnsave(post));
        return item;
    }

    private void confirmUnsave(FeedPostsResponse.PostItem post) {
        new AlertDialog.Builder(this)
                .setTitle("Bỏ lưu bài viết?")
                .setMessage("Bài viết sẽ bị xóa khỏi danh sách đã lưu của bạn.")
                .setNegativeButton("Hủy", null)
                .setPositiveButton("Bỏ lưu", (d, w) -> unsave(post))
                .show();
    }

    private void unsave(FeedPostsResponse.PostItem post) {
        int index = savedPosts.indexOf(post);
        savedPosts.remove(post);
        renderSavedPosts();

        ApiClient.getApiService().unsavePost(ApiClient.getAuthHeader(this), post.postId)
                .enqueue(new Callback<Object>() {
                    @Override
                    public void onResponse(Call<Object> call, Response<Object> response) {
                        if (isFinishing()) return;
                        if (response.isSuccessful()) {
                            Toast.makeText(FavoriteDishesActivity.this, "Đã bỏ lưu bài viết", Toast.LENGTH_SHORT).show();
                        } else {
                            restore(post, index, "Không thể bỏ lưu (mã " + response.code() + ")");
                        }
                    }

                    @Override
                    public void onFailure(Call<Object> call, Throwable t) {
                        if (isFinishing()) return;
                        restore(post, index, "Không thể kết nối máy chủ");
                    }
                });
    }

    private void restore(FeedPostsResponse.PostItem post, int index, String message) {
        savedPosts.add(Math.max(0, Math.min(index, savedPosts.size())), post);
        renderSavedPosts();
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }
}
