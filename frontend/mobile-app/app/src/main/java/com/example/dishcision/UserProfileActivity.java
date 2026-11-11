package com.example.dishcision;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.dishcision.network.ApiClient;
import com.example.dishcision.network.AuthResponse;
import com.example.dishcision.network.ImageLoader;
import com.example.dishcision.network.TokenManager;
import com.example.dishcision.network.UserProfileResponse;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class UserProfileActivity extends AppCompatActivity {

    /** Intent extra: numeric id of the user whose profile to show (String or int). */
    public static final String EXTRA_USER_ID = "USER_ID";

    private ImageView btnBack;
    private ImageView btnShare;
    private ImageView btnMore;
    private LinearLayout btnFollow;
    private LinearLayout btnMessage;

    private LinearLayout navHome;
    private LinearLayout navDecide;
    private LinearLayout navCreate;
    private LinearLayout navMessage;
    private LinearLayout navProfile;

    private ImageView imgAvatar;
    private TextView tvFullName, tvHandle, tvBio;
    private TextView tvPostsCount, tvFollowersCount, tvFollowingCount, tvFollowLabel;

    private TokenManager tokenManager;
    private String targetUserId;
    private String displayName = "";
    private int followersCount = 0;
    private boolean isFollowing = false;
    private boolean followRequestInFlight = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_user_profile);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(android.R.id.content), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        tokenManager = new TokenManager(this);

        Object rawId = getIntent().getExtras() != null ? getIntent().getExtras().get(EXTRA_USER_ID) : null;
        targetUserId = rawId != null ? String.valueOf(rawId).trim() : null;
        if (targetUserId == null || !targetUserId.matches("\\d+")) {
            Toast.makeText(this, "Không xác định được người dùng", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        initViews();
        clearPlaceholders();
        setupListeners();
        fetchUserProfileApi();
    }

    private void initViews() {
        btnBack = findViewById(R.id.btnBack);
        btnShare = findViewById(R.id.btnShare);
        btnMore = findViewById(R.id.btnMore);
        btnFollow = findViewById(R.id.btnFollow);
        btnMessage = findViewById(R.id.btnMessage);

        imgAvatar = findViewById(R.id.imgAvatar);
        tvFullName = findViewById(R.id.tvFullName);
        tvHandle = findViewById(R.id.tvHandle);
        tvBio = findViewById(R.id.tvBio);
        tvPostsCount = findViewById(R.id.tvPostsCount);
        tvFollowersCount = findViewById(R.id.tvFollowersCount);
        tvFollowingCount = findViewById(R.id.tvFollowingCount);
        tvFollowLabel = findViewById(R.id.tvFollowLabel);

        navHome = findViewById(R.id.navHome);
        navDecide = findViewById(R.id.navDecide);
        navCreate = findViewById(R.id.navAdd);
        navMessage = findViewById(R.id.navMessage);
        navProfile = findViewById(R.id.navProfile);
    }

    private void setupListeners() {
        if (btnBack != null) {
            btnBack.setOnClickListener(v -> finish());
        }

        if (btnShare != null) {
            btnShare.setOnClickListener(v ->
                Toast.makeText(this, "Chia sẻ trang cá nhân " + displayName, Toast.LENGTH_SHORT).show()
            );
        }

        if (btnMore != null) {
            btnMore.setOnClickListener(v ->
                Toast.makeText(this, "Tùy chọn khác", Toast.LENGTH_SHORT).show()
            );
        }

        if (btnFollow != null) {
            btnFollow.setOnClickListener(v -> toggleFollow());
        }

        if (btnMessage != null) {
            btnMessage.setOnClickListener(v -> {
                Intent intent = new Intent(UserProfileActivity.this, ChatActivity.class);
                intent.putExtra("CHAT_NAME", displayName);
                startActivity(intent);
            });
        }

        if (navHome != null) {
            navHome.setOnClickListener(v -> {
                Intent intent = new Intent(UserProfileActivity.this, HomeActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                startActivity(intent);
                finish();
            });
        }

        if (navDecide != null) {
            navDecide.setOnClickListener(v -> {
                Intent intent = new Intent(UserProfileActivity.this, DecideActivity.class);
                startActivity(intent);
            });
        }

        if (navCreate != null) {
            navCreate.setOnClickListener(v -> {
                Intent intent = new Intent(UserProfileActivity.this, CreatePostActivity.class);
                startActivity(intent);
            });
        }

        if (navMessage != null) {
            navMessage.setOnClickListener(v -> {
                Intent intent = new Intent(UserProfileActivity.this, MessagesInboxActivity.class);
                startActivity(intent);
            });
        }

        if (navProfile != null) {
            navProfile.setOnClickListener(v -> {
                Intent intent = new Intent(UserProfileActivity.this, ProfileActivity.class);
                startActivity(intent);
            });
        }
    }

    /** Wipe the hard-coded mock values from the layout until real data arrives. */
    private void clearPlaceholders() {
        setText(tvFullName, "");
        setText(tvHandle, "");
        setText(tvBio, "");
        setText(tvPostsCount, "0");
        setText(tvFollowersCount, "0");
        setText(tvFollowingCount, "0");
        renderFollowButton();
    }

    private void fetchUserProfileApi() {
        ApiClient.getApiService().getUserProfile(targetUserId).enqueue(new Callback<UserProfileResponse>() {
            @Override
            public void onResponse(Call<UserProfileResponse> call, Response<UserProfileResponse> response) {
                if (isFinishing() || isDestroyed()) return;
                if (!response.isSuccessful() || response.body() == null || response.body().getData() == null) {
                    Toast.makeText(UserProfileActivity.this,
                            response.code() == 404 ? "Người dùng không tồn tại" : "Không tải được trang cá nhân",
                            Toast.LENGTH_SHORT).show();
                    return;
                }
                bindUser(response.body().getData());
            }

            @Override
            public void onFailure(Call<UserProfileResponse> call, Throwable t) {
                if (isFinishing() || isDestroyed()) return;
                Toast.makeText(UserProfileActivity.this, "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void bindUser(UserProfileResponse.UserData user) {
        String fullName = user.fullName != null && !user.fullName.trim().isEmpty() ? user.fullName : user.username;
        displayName = fullName != null ? fullName : "";
        setText(tvFullName, displayName);
        setText(tvHandle, user.username != null ? "@" + user.username : "");
        setText(tvBio, user.bio != null ? user.bio : "");
        setText(tvPostsCount, String.valueOf(user.postsCount));
        setText(tvFollowingCount, String.valueOf(user.followingCount));
        followersCount = user.followersCount;
        setText(tvFollowersCount, String.valueOf(followersCount));
        if (imgAvatar != null && user.avatarUrl != null && !user.avatarUrl.trim().isEmpty()) {
            ImageLoader.loadAvatar(imgAvatar, user.avatarUrl);
        }
        isFollowing = user.isFollowing;
        renderFollowButton();
    }

    private void toggleFollow() {
        if (followRequestInFlight) return;
        if (!tokenManager.isLoggedIn()) {
            Toast.makeText(this, "Vui lòng đăng nhập để theo dõi", Toast.LENGTH_SHORT).show();
            return;
        }
        followRequestInFlight = true;
        final boolean wantFollow = !isFollowing;
        String auth = tokenManager.getFormattedToken();
        Call<AuthResponse> request = wantFollow
                ? ApiClient.getApiService().followUser(auth, targetUserId)
                : ApiClient.getApiService().unfollowUser(auth, targetUserId);

        request.enqueue(new Callback<AuthResponse>() {
            @Override
            public void onResponse(Call<AuthResponse> call, Response<AuthResponse> response) {
                followRequestInFlight = false;
                if (isFinishing() || isDestroyed()) return;
                // 409 on follow = already following: the server state is "following" either way.
                boolean ok = response.isSuccessful() || (wantFollow && response.code() == 409);
                if (!ok) {
                    Toast.makeText(UserProfileActivity.this,
                            response.code() == 401 ? "Phiên đăng nhập hết hạn, vui lòng đăng nhập lại" : "Không thể cập nhật theo dõi",
                            Toast.LENGTH_SHORT).show();
                    return;
                }
                if (response.isSuccessful()) {
                    followersCount = Math.max(0, followersCount + (wantFollow ? 1 : -1));
                    setText(tvFollowersCount, String.valueOf(followersCount));
                }
                isFollowing = wantFollow;
                renderFollowButton();
                Toast.makeText(UserProfileActivity.this,
                        wantFollow ? "Đã theo dõi " + displayName : "Đã hủy theo dõi",
                        Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onFailure(Call<AuthResponse> call, Throwable t) {
                followRequestInFlight = false;
                if (isFinishing() || isDestroyed()) return;
                Toast.makeText(UserProfileActivity.this, "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void renderFollowButton() {
        setText(tvFollowLabel, isFollowing ? "Đang theo dõi" : "+ Theo dõi");
    }

    private static void setText(TextView view, String text) {
        if (view != null) view.setText(text);
    }
}
