package com.example.dishcision;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
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
import com.example.dishcision.network.FeedPostsResponse;
import com.example.dishcision.network.ImageLoader;
import com.example.dishcision.network.TokenManager;
import com.example.dishcision.network.UserProfileResponse;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ProfileActivity extends AppCompatActivity {

    private ImageView btnBack;
    private ImageView btnShare;
    private ImageView btnSettings;
    private LinearLayout btnEditProfile;
    private LinearLayout btnQrCode;

    private ImageView imgAvatar;
    private ImageView imgVerified;
    private TextView tvHandle;
    private TextView tvFullName;
    private TextView tvRoleBadge;
    private TextView tvLocation;
    private TextView tvBio;
    private TextView tvPostsCount;
    private TextView tvFollowersCount;
    private TextView tvFollowingCount;
    private LinearLayout layoutProfileTags;

    private LinearLayout colPosts;
    private LinearLayout colFollowers;
    private LinearLayout colFollowing;
    private View tabMyPosts;

    private LinearLayout gridPostsContainer;
    private TextView tvEmptyPosts;

    private LinearLayout navHome;
    private LinearLayout navDecide;
    private View navAdd;
    private LinearLayout navMessage;
    private LinearLayout navProfile;

    private TokenManager tokenManager;

    private String currentFullName = "";
    private String currentUsername = "";
    private String currentBio = "";
    private String currentAvatarUrl = "";
    private int currentUserId = 0;
    private boolean skipFirstResumeFetch = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_profile);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(android.R.id.content), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        tokenManager = new TokenManager(this);

        initViews();
        setupListeners();
        loadLocalProfileDetails();
        fetchProfileFromApi();
        fetchMyPostsFromBackend();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (skipFirstResumeFetch) {
            skipFirstResumeFetch = false;
            return;
        }
        loadLocalProfileDetails();
        fetchProfileFromApi();
        fetchMyPostsFromBackend();
    }

    private void initViews() {
        btnBack = findViewById(R.id.btnBack);
        btnShare = findViewById(R.id.btnShare);
        btnSettings = findViewById(R.id.btnSettings);
        btnEditProfile = findViewById(R.id.btnEditProfile);
        btnQrCode = findViewById(R.id.btnQrCode);

        imgAvatar = findViewById(R.id.imgAvatar);
        imgVerified = findViewById(R.id.imgVerified);
        tvHandle = findViewById(R.id.tvHandle);
        tvFullName = findViewById(R.id.tvFullName);
        tvRoleBadge = findViewById(R.id.tvRoleBadge);
        tvLocation = findViewById(R.id.tvLocation);
        tvBio = findViewById(R.id.tvBio);
        tvPostsCount = findViewById(R.id.tvPostsCount);
        tvFollowersCount = findViewById(R.id.tvFollowersCount);
        tvFollowingCount = findViewById(R.id.tvFollowingCount);
        layoutProfileTags = findViewById(R.id.layoutProfileTags);

        colPosts = findViewById(R.id.colPosts);
        colFollowers = findViewById(R.id.colFollowers);
        colFollowing = findViewById(R.id.colFollowing);
        tabMyPosts = findViewById(R.id.tabMyPosts);

        gridPostsContainer = findViewById(R.id.gridPostsContainer);
        tvEmptyPosts = findViewById(R.id.tvEmptyPosts);

        navHome = findViewById(R.id.navHome);
        navDecide = findViewById(R.id.navDecide);
        navAdd = findViewById(R.id.navAdd);
        navMessage = findViewById(R.id.navMessage);
        navProfile = findViewById(R.id.navProfile);
    }

    private void loadLocalProfileDetails() {
        SharedPreferences prefs = getSharedPreferences("DishcisionPrefs", MODE_PRIVATE);
        String savedName = prefs.getString("user_name", tokenManager.getUserName());
        String savedUsername = prefs.getString("user_username", "");
        String savedAvatar = prefs.getString("avatar_url", "");
        String savedCity = prefs.getString("user_city", "");

        if (currentFullName.isEmpty() && savedName != null && !savedName.trim().isEmpty()) {
            currentFullName = savedName.trim();
            if (tvFullName != null) tvFullName.setText(currentFullName);
        }
        if (currentUsername.isEmpty() && savedUsername != null && !savedUsername.trim().isEmpty()) {
            currentUsername = savedUsername.replace("@", "").trim();
            if (tvHandle != null) tvHandle.setText("@" + currentUsername);
        }
        if (currentAvatarUrl.isEmpty() && savedAvatar != null && !savedAvatar.trim().isEmpty()) {
            currentAvatarUrl = savedAvatar.trim();
            if (imgAvatar != null) ImageLoader.loadAvatar(imgAvatar, currentAvatarUrl);
        }
        if (tvLocation != null) {
            tvLocation.setText(savedCity != null && !savedCity.trim().isEmpty() ? savedCity : "Chưa cập nhật vị trí");
        }

        Set<String> savedTastes = prefs.getStringSet("user_taste_preferences", null);
        renderTasteTags(savedTastes);
    }

    private void renderTasteTags(Collection<String> tastes) {
        if (layoutProfileTags == null) return;
        layoutProfileTags.removeAllViews();
        if (tastes == null || tastes.isEmpty()) return;

        for (String t : tastes) {
            if (t == null || t.trim().isEmpty()) continue;
            String cleanTag = "#" + t.replaceAll("[^a-zA-Z0-9À-ỹ]", "");
            if ("#".equals(cleanTag)) cleanTag = "#KhauVi";
            TextView tagTv = new TextView(this);
            tagTv.setBackgroundResource(R.drawable.bg_tag_chip_pink);
            tagTv.setText(cleanTag);
            tagTv.setTextColor(Color.parseColor("#FF5A36"));
            tagTv.setTextSize(12);
            tagTv.setTypeface(tagTv.getTypeface(), Typeface.BOLD);
            int pxH = dpToPx(12);
            int pxV = dpToPx(6);
            tagTv.setPadding(pxH, pxV, pxH, pxV);

            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            lp.rightMargin = dpToPx(8);
            tagTv.setLayoutParams(lp);

            layoutProfileTags.addView(tagTv);
        }
    }

    private int dpToPx(int dp) {
        return (int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP, dp, getResources().getDisplayMetrics());
    }

    private void setupListeners() {
        if (btnBack != null) {
            btnBack.setOnClickListener(v -> finish());
        }

        if (btnShare != null) {
            btnShare.setOnClickListener(v -> {
                String handle = !currentUsername.isEmpty() ? "@" + currentUsername.replace("@", "") : "";
                String name = !currentFullName.isEmpty() ? currentFullName : (!handle.isEmpty() ? handle : "Thành viên Dishcision");
                String profileLink = currentUserId > 0 ? ApiClient.getServerRootUrl() + "/users/" + currentUserId : "";
                Intent shareIntent = new Intent(Intent.ACTION_SEND);
                shareIntent.setType("text/plain");
                shareIntent.putExtra(Intent.EXTRA_SUBJECT, "Hồ sơ Dishcision");
                String shareText = "Xem hồ sơ của " + name + (handle.isEmpty() ? "" : " (" + handle + ")") + " trên Dishcision";
                if (!profileLink.isEmpty()) shareText += "\n" + profileLink;
                shareIntent.putExtra(Intent.EXTRA_TEXT, shareText);
                startActivity(Intent.createChooser(shareIntent, "Chia sẻ hồ sơ qua"));
            });
        }

        if (btnSettings != null) {
            btnSettings.setOnClickListener(v -> startActivity(new Intent(ProfileActivity.this, SettingsActivity.class)));
        }

        if (btnEditProfile != null) {
            btnEditProfile.setOnClickListener(v -> {
                Intent intent = new Intent(ProfileActivity.this, EditProfileActivity.class);
                intent.putExtra("FULL_NAME", currentFullName);
                intent.putExtra("BIO", currentBio);
                intent.putExtra("AVATAR_URL", currentAvatarUrl);
                intent.putExtra("USERNAME", currentUsername);
                startActivity(intent);
            });
        }

        if (btnQrCode != null) {
            btnQrCode.setOnClickListener(v -> {
                String displayName = !currentFullName.isEmpty() ? currentFullName : "Thành viên Dishcision";
                String handleText = !currentUsername.isEmpty() ? "@" + currentUsername.replace("@", "") : "Chưa có username";
                new AlertDialog.Builder(this)
                        .setTitle("Mã hồ sơ cá nhân")
                        .setMessage("Tài khoản: " + displayName + "\nHandle: " + handleText + "\n\nChia sẻ handle này để bạn bè tìm và theo dõi bạn trên Dishcision.")
                        .setPositiveButton("Đóng", (dialog, which) -> dialog.dismiss())
                        .show();
            });
        }

        if (colPosts != null) {
            colPosts.setOnClickListener(v -> startActivity(new Intent(ProfileActivity.this, MyPostsActivity.class)));
        }

        if (tabMyPosts != null) {
            tabMyPosts.setOnClickListener(v -> startActivity(new Intent(ProfileActivity.this, MyPostsActivity.class)));
        }

        if (colFollowers != null) {
            colFollowers.setOnClickListener(v -> openFollowersScreen("followers"));
        }

        if (colFollowing != null) {
            colFollowing.setOnClickListener(v -> openFollowersScreen("following"));
        }

        if (navHome != null) {
            navHome.setOnClickListener(v -> {
                Intent intent = new Intent(ProfileActivity.this, HomeActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                startActivity(intent);
                finish();
            });
        }

        if (navDecide != null) {
            navDecide.setOnClickListener(v -> startActivity(new Intent(ProfileActivity.this, DecideActivity.class)));
        }

        if (navAdd != null) {
            navAdd.setOnClickListener(v -> startActivity(new Intent(ProfileActivity.this, CreatePostActivity.class)));
        }

        if (navMessage != null) {
            navMessage.setOnClickListener(v -> startActivity(new Intent(ProfileActivity.this, MessagesInboxActivity.class)));
        }

        if (navProfile != null) {
            navProfile.setOnClickListener(v -> {});
        }
    }

    private void openFollowersScreen(String activeTab) {
        Intent intent = new Intent(ProfileActivity.this, FollowersActivity.class);
        intent.putExtra("USER_ID", "me");
        if (tvFullName != null) intent.putExtra("DISPLAY_NAME", tvFullName.getText().toString());
        if (tvHandle != null) intent.putExtra("USERNAME", tvHandle.getText().toString());
        if (tvFollowersCount != null) intent.putExtra("FOLLOWERS_COUNT", tvFollowersCount.getText().toString());
        if (tvFollowingCount != null) intent.putExtra("FOLLOWING_COUNT", tvFollowingCount.getText().toString());
        intent.putExtra("ACTIVE_TAB", activeTab);
        startActivity(intent);
    }

    private void fetchProfileFromApi() {
        String authHeader = ApiClient.getAuthHeader(this);
        if (authHeader.isEmpty()) return;

        ApiClient.getApiService().getMyProfile(authHeader).enqueue(new Callback<UserProfileResponse>() {
            @Override
            public void onResponse(@NonNull Call<UserProfileResponse> call, @NonNull Response<UserProfileResponse> response) {
                if (isFinishing() || isDestroyed()) return;
                if (response.isSuccessful() && response.body() != null && response.body().getData() != null) {
                    UserProfileResponse.UserData user = response.body().getData();

                    currentUserId = user.userId;
                    currentFullName = user.fullName != null ? user.fullName.trim() : "";
                    currentUsername = user.username != null ? user.username.replace("@", "").trim() : "";
                    currentBio = user.bio != null ? user.bio.trim() : "";
                    currentAvatarUrl = user.avatarUrl != null ? user.avatarUrl.trim() : "";

                    SharedPreferences.Editor editor = getSharedPreferences("DishcisionPrefs", MODE_PRIVATE).edit();
                    if (currentUserId > 0) editor.putInt("user_id", currentUserId);
                    if (!currentFullName.isEmpty()) editor.putString("user_name", currentFullName);
                    if (!currentUsername.isEmpty()) editor.putString("user_username", currentUsername);
                    if (user.email != null && !user.email.isEmpty()) editor.putString("user_email", user.email);
                    if (!currentAvatarUrl.isEmpty()) editor.putString("avatar_url", currentAvatarUrl);
                    else editor.remove("avatar_url");
                    editor.putBoolean("is_google_login", user.isGoogleAccount);
                    if (user.preferences != null) {
                        editor.putStringSet("user_taste_preferences", new HashSet<>(user.preferences));
                    }
                    editor.apply();

                    if (currentUserId > 0) {
                        tokenManager.saveAuthData(null, currentUserId, user.email, currentFullName);
                    }

                    if (tvFullName != null) {
                        tvFullName.setText(!currentFullName.isEmpty() ? currentFullName : currentUsername);
                    }
                    if (tvHandle != null) {
                        tvHandle.setText(currentUsername.isEmpty() ? "" : "@" + currentUsername);
                    }
                    if (tvBio != null) {
                        tvBio.setText(currentBio.isEmpty() ? "Chưa có tiểu sử giới thiệu." : currentBio);
                    }
                    if (tvPostsCount != null) {
                        tvPostsCount.setText(PostFeedHelper.formatCount(user.postsCount));
                    }
                    if (tvFollowersCount != null) {
                        tvFollowersCount.setText(PostFeedHelper.formatCount(user.followersCount));
                    }
                    if (tvFollowingCount != null) {
                        tvFollowingCount.setText(PostFeedHelper.formatCount(user.followingCount));
                    }

                    boolean isRestaurant = "RestaurantAccount".equals(user.role);
                    boolean isAdmin = "Admin".equals(user.role);
                    if (imgVerified != null) {
                        imgVerified.setVisibility(isRestaurant || isAdmin ? View.VISIBLE : View.GONE);
                    }
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

                    if (imgAvatar != null) {
                        if (!currentAvatarUrl.isEmpty()) {
                            ImageLoader.loadAvatar(imgAvatar, currentAvatarUrl);
                        } else {
                            imgAvatar.setImageResource(R.drawable.bg_avatar_circle);
                        }
                    }
                    if (user.preferences != null) {
                        renderTasteTags(user.preferences);
                    }
                } else if (response.code() == 401) {
                    Toast.makeText(ProfileActivity.this, "Phiên đăng nhập hết hạn, vui lòng đăng nhập lại", Toast.LENGTH_LONG).show();
                    AuthHelper.clearLocalSession(ProfileActivity.this);
                    Intent intent = new Intent(ProfileActivity.this, LoginActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    finish();
                }
            }

            @Override
            public void onFailure(@NonNull Call<UserProfileResponse> call, @NonNull Throwable t) {}
        });
    }

    private void fetchMyPostsFromBackend() {
        String authHeader = ApiClient.getAuthHeader(this);
        if (authHeader.isEmpty()) return;

        ApiClient.getApiService().getMyPosts(authHeader).enqueue(new Callback<FeedPostsResponse>() {
            @Override
            public void onResponse(@NonNull Call<FeedPostsResponse> call, @NonNull Response<FeedPostsResponse> response) {
                if (isFinishing() || isDestroyed()) return;
                if (response.isSuccessful() && response.body() != null) {
                    List<FeedPostsResponse.PostItem> posts = response.body().getData();
                    PostFeedHelper.renderPostGrid(
                            ProfileActivity.this,
                            gridPostsContainer,
                            tvEmptyPosts,
                            posts,
                            "Chưa có bài viết nào",
                            (post, pos) -> {
                                Intent intent = new Intent(ProfileActivity.this, MyPostsActivity.class);
                                intent.putExtra(MyPostsActivity.EXTRA_POST_ID, post.postId);
                                intent.putExtra(MyPostsActivity.EXTRA_POST_INDEX, pos + 1);
                                startActivity(intent);
                            }
                    );
                } else if (gridPostsContainer != null && gridPostsContainer.getChildCount() == 0) {
                    PostFeedHelper.renderPostGrid(ProfileActivity.this, gridPostsContainer, tvEmptyPosts, null, "Chưa có bài viết nào", null);
                }
            }

            @Override
            public void onFailure(@NonNull Call<FeedPostsResponse> call, @NonNull Throwable t) {
                if (isFinishing() || isDestroyed()) return;
                if (gridPostsContainer != null && gridPostsContainer.getChildCount() == 0) {
                    PostFeedHelper.renderPostGrid(ProfileActivity.this, gridPostsContainer, tvEmptyPosts, null, "Chưa có bài viết nào", null);
                }
            }
        });
    }
}
