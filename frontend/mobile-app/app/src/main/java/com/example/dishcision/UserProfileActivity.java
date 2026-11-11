package com.example.dishcision;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.PopupMenu;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.dishcision.network.ApiClient;
import com.example.dishcision.network.AuthHelper;
import com.example.dishcision.network.AuthResponse;
import com.example.dishcision.network.CurrentUserBinder;
import com.example.dishcision.network.FeedPostsResponse;
import com.example.dishcision.network.ImageLoader;
import com.example.dishcision.network.TokenManager;
import com.example.dishcision.network.UserProfileResponse;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class UserProfileActivity extends AppCompatActivity {

    private ImageView btnBack;
    private ImageView btnShare;
    private ImageView btnMore;
    private ImageView imgHeaderSelfAvatar;
    private ImageView imgAvatar;
    private ImageView imgVerifiedBadge;

    private LinearLayout colPosts;
    private LinearLayout colFollowers;
    private LinearLayout colFollowing;
    private TextView tvPostsCount;
    private TextView tvFollowersCount;
    private TextView tvFollowingCount;

    private TextView tvFullName;
    private TextView tvHandle;
    private LinearLayout layoutBadgesRow;
    private TextView tvRoleBadge;
    private TextView tvLocationBadge;
    private TextView tvBio;

    private LinearLayout btnFollow;
    private ImageView imgFollowIcon;
    private TextView tvFollowText;
    private LinearLayout btnMessage;

    private LinearLayout gridUserPostsContainer;
    private TextView tvEmptyUserPosts;

    private LinearLayout navHome;
    private LinearLayout navDecide;
    private View navAdd;
    private LinearLayout navMessage;
    private LinearLayout navProfile;

    private TokenManager tokenManager;
    private int targetUserId = 0;
    private String displayName = "";
    private String handleText = "";
    private int followersCount = 0;
    private int followingCount = 0;
    private boolean isFollowing = false;
    private boolean isFollowInFlight = false;
    private boolean skipFirstResumeFetch = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        targetUserId = getIntent().getIntExtra("USER_ID", 0);
        if (targetUserId <= 0) {
            String raw = getIntent().getStringExtra("USER_ID");
            if (raw != null && !raw.trim().isEmpty()) {
                try {
                    targetUserId = Integer.parseInt(raw.trim());
                } catch (Exception ignored) {}
            }
        }

        if (targetUserId <= 0) {
            Toast.makeText(this, "Không tìm thấy thông tin người dùng", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        int selfId = AuthHelper.getCurrentUserId(this);
        if (selfId > 0 && targetUserId == selfId) {
            startActivity(new Intent(this, ProfileActivity.class));
            finish();
            return;
        }

        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_user_profile);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(android.R.id.content), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        tokenManager = new TokenManager(this);

        initViews();
        setupListeners();
        CurrentUserBinder.bind(this, imgHeaderSelfAvatar, null, null);
        fetchUserProfileApi();
        fetchUserPostsApi();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (skipFirstResumeFetch) {
            skipFirstResumeFetch = false;
            return;
        }
        fetchUserProfileApi();
        fetchUserPostsApi();
    }

    private void initViews() {
        btnBack = findViewById(R.id.btnBack);
        btnShare = findViewById(R.id.btnShare);
        btnMore = findViewById(R.id.btnMore);
        imgHeaderSelfAvatar = findViewById(R.id.imgHeaderSelfAvatar);
        imgAvatar = findViewById(R.id.imgAvatar);
        imgVerifiedBadge = findViewById(R.id.imgVerifiedBadge);

        colPosts = findViewById(R.id.colPosts);
        colFollowers = findViewById(R.id.colFollowers);
        colFollowing = findViewById(R.id.colFollowing);
        tvPostsCount = findViewById(R.id.tvPostsCount);
        tvFollowersCount = findViewById(R.id.tvFollowersCount);
        tvFollowingCount = findViewById(R.id.tvFollowingCount);

        tvFullName = findViewById(R.id.tvFullName);
        tvHandle = findViewById(R.id.tvHandle);
        layoutBadgesRow = findViewById(R.id.layoutBadgesRow);
        tvRoleBadge = findViewById(R.id.tvRoleBadge);
        tvLocationBadge = findViewById(R.id.tvLocationBadge);
        tvBio = findViewById(R.id.tvBio);

        btnFollow = findViewById(R.id.btnFollow);
        imgFollowIcon = findViewById(R.id.imgFollowIcon);
        tvFollowText = findViewById(R.id.tvFollowText);
        btnMessage = findViewById(R.id.btnMessage);

        gridUserPostsContainer = findViewById(R.id.gridUserPostsContainer);
        tvEmptyUserPosts = findViewById(R.id.tvEmptyUserPosts);

        navHome = findViewById(R.id.navHome);
        navDecide = findViewById(R.id.navDecide);
        navAdd = findViewById(R.id.navAdd);
        navMessage = findViewById(R.id.navMessage);
        navProfile = findViewById(R.id.navProfile);
    }

    private void setupListeners() {
        if (btnBack != null) {
            btnBack.setOnClickListener(v -> finish());
        }

        if (imgHeaderSelfAvatar != null) {
            imgHeaderSelfAvatar.setOnClickListener(v -> startActivity(new Intent(UserProfileActivity.this, ProfileActivity.class)));
        }

        if (btnShare != null) {
            btnShare.setOnClickListener(v -> {
                String name = !displayName.isEmpty() ? displayName : (!handleText.isEmpty() ? handleText : "Thành viên Dishcision");
                String profileUrl = ApiClient.getServerRootUrl() + "/users/" + targetUserId;
                Intent shareIntent = new Intent(Intent.ACTION_SEND);
                shareIntent.setType("text/plain");
                shareIntent.putExtra(Intent.EXTRA_SUBJECT, "Hồ sơ Dishcision");
                shareIntent.putExtra(Intent.EXTRA_TEXT, "Xem trang cá nhân của " + name + " trên Dishcision:\n" + profileUrl);
                startActivity(Intent.createChooser(shareIntent, "Chia sẻ trang cá nhân"));
            });
        }

        if (btnMore != null) {
            btnMore.setOnClickListener(v -> {
                PopupMenu popupMenu = new PopupMenu(UserProfileActivity.this, btnMore);
                popupMenu.getMenu().add(0, 1, 0, "Sao chép liên kết hồ sơ");
                popupMenu.getMenu().add(0, 2, 1, "Báo cáo người dùng");
                popupMenu.setOnMenuItemClickListener(item -> {
                    if (item.getItemId() == 1) {
                        ClipboardManager clipboard = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
                        if (clipboard != null) {
                            clipboard.setPrimaryClip(ClipData.newPlainText("Profile Link", ApiClient.getServerRootUrl() + "/users/" + targetUserId));
                        }
                        Toast.makeText(UserProfileActivity.this, "Đã sao chép liên kết hồ sơ", Toast.LENGTH_SHORT).show();
                        return true;
                    }
                    if (item.getItemId() == 2) {
                        Intent intent = new Intent(UserProfileActivity.this, ReportContentActivity.class);
                        intent.putExtra("REPORTED_USER_ID", targetUserId);
                        if (tvFullName != null && tvFullName.getText() != null) {
                            intent.putExtra("TARGET_TITLE", tvFullName.getText().toString());
                        }
                        if (tvHandle != null && tvHandle.getText() != null) {
                            intent.putExtra("TARGET_SUBTITLE", tvHandle.getText().toString());
                        }
                        startActivity(intent);
                        return true;
                    }
                    return false;
                });
                popupMenu.show();
            });
        }

        if (colPosts != null) {
            colPosts.setOnClickListener(v -> openUserPosts(0, 1));
        }

        if (colFollowers != null) {
            colFollowers.setOnClickListener(v -> openFollowersScreen("followers"));
        }

        if (colFollowing != null) {
            colFollowing.setOnClickListener(v -> openFollowersScreen("following"));
        }

        if (btnFollow != null) {
            btnFollow.setOnClickListener(v -> toggleFollow());
        }

        if (btnMessage != null) {
            btnMessage.setOnClickListener(v -> {
                Intent intent = new Intent(UserProfileActivity.this, ChatActivity.class);
                intent.putExtra("USER_ID", targetUserId);
                intent.putExtra("CHAT_NAME", !displayName.isEmpty() ? displayName : handleText);
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
            navDecide.setOnClickListener(v -> startActivity(new Intent(UserProfileActivity.this, DecideActivity.class)));
        }

        if (navAdd != null) {
            navAdd.setOnClickListener(v -> startActivity(new Intent(UserProfileActivity.this, CreatePostActivity.class)));
        }

        if (navMessage != null) {
            navMessage.setOnClickListener(v -> startActivity(new Intent(UserProfileActivity.this, MessagesInboxActivity.class)));
        }

        if (navProfile != null) {
            navProfile.setOnClickListener(v -> startActivity(new Intent(UserProfileActivity.this, ProfileActivity.class)));
        }
    }

    private void openFollowersScreen(String activeTab) {
        Intent intent = new Intent(UserProfileActivity.this, FollowersActivity.class);
        intent.putExtra("USER_ID", String.valueOf(targetUserId));
        intent.putExtra("DISPLAY_NAME", displayName);
        intent.putExtra("USERNAME", handleText);
        intent.putExtra("FOLLOWERS_COUNT", PostFeedHelper.formatCount(followersCount));
        intent.putExtra("FOLLOWING_COUNT", PostFeedHelper.formatCount(followingCount));
        intent.putExtra("ACTIVE_TAB", activeTab);
        startActivity(intent);
    }

    private void openUserPosts(int postId, int position) {
        Intent intent = new Intent(UserProfileActivity.this, MyPostsActivity.class);
        intent.putExtra(MyPostsActivity.EXTRA_USER_ID, targetUserId);
        intent.putExtra(MyPostsActivity.EXTRA_USER_NAME, !displayName.isEmpty() ? displayName : handleText);
        if (postId > 0) intent.putExtra(MyPostsActivity.EXTRA_POST_ID, postId);
        intent.putExtra(MyPostsActivity.EXTRA_POST_INDEX, position);
        startActivity(intent);
    }

    private void fetchUserProfileApi() {
        String authHeader = ApiClient.getAuthHeader(this);
        ApiClient.getApiService().getUserProfile(authHeader, String.valueOf(targetUserId))
                .enqueue(new Callback<UserProfileResponse>() {
                    @Override
                    public void onResponse(@NonNull Call<UserProfileResponse> call, @NonNull Response<UserProfileResponse> response) {
                        if (isFinishing() || isDestroyed()) return;
                        if (response.isSuccessful() && response.body() != null && response.body().getData() != null) {
                            bindUserData(response.body().getData());
                        } else if (response.code() == 404) {
                            Toast.makeText(UserProfileActivity.this, "Người dùng không tồn tại", Toast.LENGTH_SHORT).show();
                            finish();
                        }
                    }

                    @Override
                    public void onFailure(@NonNull Call<UserProfileResponse> call, @NonNull Throwable t) {
                        if (!isFinishing() && !isDestroyed() && displayName.isEmpty()) {
                            Toast.makeText(UserProfileActivity.this, "Không thể tải thông tin người dùng", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    private void bindUserData(UserProfileResponse.UserData user) {
        String rawUsername = user.username != null ? user.username.replace("@", "").trim() : "";
        displayName = user.fullName != null && !user.fullName.trim().isEmpty() ? user.fullName.trim() : rawUsername;
        handleText = rawUsername.isEmpty() ? "" : "@" + rawUsername;

        if (tvFullName != null) tvFullName.setText(displayName);
        if (tvHandle != null) tvHandle.setText(handleText);

        followersCount = Math.max(0, user.followersCount);
        followingCount = Math.max(0, user.followingCount);
        if (tvPostsCount != null) tvPostsCount.setText(PostFeedHelper.formatCount(user.postsCount));
        if (tvFollowersCount != null) tvFollowersCount.setText(PostFeedHelper.formatCount(followersCount));
        if (tvFollowingCount != null) tvFollowingCount.setText(PostFeedHelper.formatCount(followingCount));

        if (tvBio != null) {
            if (user.profileRestricted) {
                tvBio.setText("Thông tin cá nhân đang ở chế độ riêng tư.");
            } else {
                String bio = user.bio != null ? user.bio.trim() : "";
                tvBio.setText(bio.isEmpty() ? "Chưa có tiểu sử giới thiệu." : bio);
            }
        }

        if (imgAvatar != null) {
            ImageLoader.loadAvatar(imgAvatar, user.avatarUrl);
        }

        boolean isRestaurant = "RestaurantAccount".equals(user.role);
        boolean isAdmin = "Admin".equals(user.role);
        if (imgVerifiedBadge != null) {
            imgVerifiedBadge.setVisibility(isRestaurant || isAdmin ? View.VISIBLE : View.GONE);
        }

        boolean showRole = isRestaurant || isAdmin;
        if (tvRoleBadge != null) {
            if (isRestaurant) {
                tvRoleBadge.setText("🏪 Đối tác Nhà hàng");
                tvRoleBadge.setVisibility(View.VISIBLE);
            } else if (isAdmin) {
                tvRoleBadge.setText("🛡️ Quản trị viên");
                tvRoleBadge.setVisibility(View.VISIBLE);
            } else {
                tvRoleBadge.setVisibility(View.GONE);
            }
        }

        boolean showCity = !user.profileRestricted && user.city != null && !user.city.trim().isEmpty();
        boolean showPref = !user.profileRestricted && user.preferences != null && !user.preferences.isEmpty();
        if (tvLocationBadge != null) {
            if (showCity) {
                tvLocationBadge.setText("📍 " + user.city.trim());
                tvLocationBadge.setVisibility(View.VISIBLE);
            } else if (showPref) {
                tvLocationBadge.setText("#" + user.preferences.get(0).replace("#", "").trim());
                tvLocationBadge.setVisibility(View.VISIBLE);
            } else {
                tvLocationBadge.setVisibility(View.GONE);
            }
        }

        if (layoutBadgesRow != null) {
            layoutBadgesRow.setVisibility(showRole || showCity || showPref ? View.VISIBLE : View.GONE);
        }

        isFollowing = user.isFollowing;
        updateFollowButtonUi();
    }

    private void updateFollowButtonUi() {
        if (btnFollow == null) return;
        if (isFollowing) {
            btnFollow.setBackgroundResource(R.drawable.bg_button_light_blue);
            if (imgFollowIcon != null) imgFollowIcon.setVisibility(View.GONE);
            if (tvFollowText != null) {
                tvFollowText.setText("✓ Đang theo dõi");
                tvFollowText.setTextColor(Color.parseColor("#141B2B"));
            }
        } else {
            btnFollow.setBackgroundResource(R.drawable.bg_button_orange);
            if (imgFollowIcon != null) imgFollowIcon.setVisibility(View.VISIBLE);
            if (tvFollowText != null) {
                tvFollowText.setText("+ Theo dõi");
                tvFollowText.setTextColor(Color.WHITE);
            }
        }
    }

    private void toggleFollow() {
        if (isFollowInFlight) return;
        if (!tokenManager.isLoggedIn()) {
            Toast.makeText(this, "Vui lòng đăng nhập lại để theo dõi", Toast.LENGTH_SHORT).show();
            return;
        }
        String auth = ApiClient.getAuthHeader(this);
        if (auth.isEmpty()) return;

        boolean targetState = !isFollowing;
        isFollowing = targetState;
        followersCount = Math.max(0, followersCount + (targetState ? 1 : -1));
        updateFollowButtonUi();
        if (tvFollowersCount != null) tvFollowersCount.setText(PostFeedHelper.formatCount(followersCount));
        isFollowInFlight = true;

        Call<AuthResponse> call = targetState
                ? ApiClient.getApiService().followUser(auth, String.valueOf(targetUserId))
                : ApiClient.getApiService().unfollowUser(auth, String.valueOf(targetUserId));

        call.enqueue(new Callback<AuthResponse>() {
            @Override
            public void onResponse(@NonNull Call<AuthResponse> call, @NonNull Response<AuthResponse> response) {
                isFollowInFlight = false;
                if (isFinishing() || isDestroyed()) return;
                if (response.isSuccessful()) {
                    Toast.makeText(UserProfileActivity.this, targetState ? "Đã theo dõi" : "Đã hủy theo dõi", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (targetState && response.code() == 409) {
                    isFollowing = true;
                    updateFollowButtonUi();
                    return;
                }
                isFollowing = !targetState;
                followersCount = Math.max(0, followersCount + (targetState ? -1 : 1));
                updateFollowButtonUi();
                if (tvFollowersCount != null) tvFollowersCount.setText(PostFeedHelper.formatCount(followersCount));
                Toast.makeText(UserProfileActivity.this, "Không thể cập nhật trạng thái theo dõi", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onFailure(@NonNull Call<AuthResponse> call, @NonNull Throwable t) {
                isFollowInFlight = false;
                if (isFinishing() || isDestroyed()) return;
                isFollowing = !targetState;
                followersCount = Math.max(0, followersCount + (targetState ? -1 : 1));
                updateFollowButtonUi();
                if (tvFollowersCount != null) tvFollowersCount.setText(PostFeedHelper.formatCount(followersCount));
                Toast.makeText(UserProfileActivity.this, "Lỗi kết nối máy chủ", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void fetchUserPostsApi() {
        String authHeader = ApiClient.getAuthHeader(this);
        ApiClient.getApiService().getUserPosts(authHeader, String.valueOf(targetUserId))
                .enqueue(new Callback<FeedPostsResponse>() {
                    @Override
                    public void onResponse(@NonNull Call<FeedPostsResponse> call, @NonNull Response<FeedPostsResponse> response) {
                        if (isFinishing() || isDestroyed()) return;
                        if (response.isSuccessful() && response.body() != null) {
                            List<FeedPostsResponse.PostItem> posts = response.body().getData();
                            PostFeedHelper.renderPostGrid(
                                    UserProfileActivity.this,
                                    gridUserPostsContainer,
                                    tvEmptyUserPosts,
                                    posts,
                                    "Người dùng chưa có bài viết nào",
                                    (post, pos) -> openUserPosts(post.postId, pos + 1)
                            );
                        } else if (response.code() == 403) {
                            PostFeedHelper.renderPostGrid(
                                    UserProfileActivity.this,
                                    gridUserPostsContainer,
                                    tvEmptyUserPosts,
                                    null,
                                    "Bài viết đang ở chế độ riêng tư",
                                    null
                            );
                        } else if (gridUserPostsContainer != null && gridUserPostsContainer.getChildCount() == 0) {
                            PostFeedHelper.renderPostGrid(UserProfileActivity.this, gridUserPostsContainer, tvEmptyUserPosts, null, "Người dùng chưa có bài viết nào", null);
                        }
                    }

                    @Override
                    public void onFailure(@NonNull Call<FeedPostsResponse> call, @NonNull Throwable t) {
                        if (isFinishing() || isDestroyed()) return;
                        if (gridUserPostsContainer != null && gridUserPostsContainer.getChildCount() == 0) {
                            PostFeedHelper.renderPostGrid(UserProfileActivity.this, gridUserPostsContainer, tvEmptyUserPosts, null, "Người dùng chưa có bài viết nào", null);
                        }
                    }
                });
    }
}
