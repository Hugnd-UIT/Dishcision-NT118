package com.example.dishcision;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.dishcision.network.ApiClient;
import com.example.dishcision.network.CreatePostResponse;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/** Hiển thị đúng một bài viết (mở từ thông báo), không phải cả bảng tin. */
public class PostDetailActivity extends AppCompatActivity {

    public static final String EXTRA_POST_ID = "POST_ID";

    private LinearLayout container;
    private TextView tvEmpty;
    private int postId;

    private final ActivityResultLauncher<Intent> editPostLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == RESULT_OK) loadPost();
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_my_posts);
        ApiClient.init(this);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main_my_posts), (v, insets) -> {
            Insets bars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(bars.left, bars.top, bars.right, bars.bottom);
            return insets;
        });

        postId = getIntent().getIntExtra(EXTRA_POST_ID, 0);
        ((TextView) findViewById(R.id.tvHeaderTitle)).setText("Bài viết");
        ((ImageView) findViewById(R.id.btnBack)).setOnClickListener(v -> finish());
        ImageView bell = findViewById(R.id.btnNotification);
        if (bell != null) bell.setVisibility(View.GONE);
        container = findViewById(R.id.myPostsContainer);
        tvEmpty = findViewById(R.id.tvEmptyMyPosts);

        if (postId <= 0) {
            showMessage("Không tìm thấy bài viết.");
            return;
        }
        loadPost();
    }

    private void loadPost() {
        String auth = ApiClient.getAuthHeader(this);
        if (auth.isEmpty()) {
            showMessage("Vui lòng đăng nhập lại để xem bài viết.");
            return;
        }
        ApiClient.getApiService().getPostById(auth, postId).enqueue(new Callback<CreatePostResponse>() {
            @Override
            public void onResponse(@NonNull Call<CreatePostResponse> call, @NonNull Response<CreatePostResponse> response) {
                if (isFinishing() || isDestroyed()) return;
                if (response.isSuccessful() && response.body() != null && response.body().getData() != null) {
                    showPost(response.body());
                } else if (response.code() == 403) {
                    showMessage("Bài viết này đã được giới hạn quyền xem.");
                } else if (response.code() == 404) {
                    showMessage("Bài viết không còn tồn tại hoặc đã bị ẩn.");
                } else {
                    showMessage("Chưa thể tải bài viết (mã " + response.code() + ").");
                }
            }

            @Override
            public void onFailure(@NonNull Call<CreatePostResponse> call, @NonNull Throwable t) {
                if (isFinishing() || isDestroyed()) return;
                showMessage("Lỗi kết nối máy chủ.");
            }
        });
    }

    private void showPost(CreatePostResponse body) {
        tvEmpty.setVisibility(View.GONE);
        container.setVisibility(View.VISIBLE);
        container.removeAllViews();
        View card = getLayoutInflater().inflate(R.layout.item_feed_post, container, false);
        PostFeedHelper.bindPostCard(this, card, body.getData(), editPostLauncher, new PostFeedHelper.PostCardListener() {
            @Override
            public void onPostDeleted(int id) {
                finish();
            }

            @Override
            public void onPostArchived(int id) {
                finish();
            }
        });
        container.addView(card);
    }

    private void showMessage(String message) {
        container.setVisibility(View.GONE);
        tvEmpty.setText(message);
        tvEmpty.setVisibility(View.VISIBLE);
    }
}
