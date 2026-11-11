package com.example.dishcision;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.widget.ImageViewCompat;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.dishcision.network.ApiClient;
import com.example.dishcision.network.AuthHelper;
import com.example.dishcision.network.FeedPostsResponse;
import com.example.dishcision.network.ImageLoader;
import com.example.dishcision.network.TokenManager;
import com.example.dishcision.network.UserProfileResponse;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;

import java.util.Collection;
import java.util.EnumMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
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

    private static final int TAB_MY_POSTS = 0;
    private static final int TAB_SAVED_POSTS = 1;
    private static final int TAB_LIKED_POSTS = 2;

    private LinearLayout colPosts;
    private LinearLayout colFollowers;
    private LinearLayout colFollowing;
    private View tabMyPosts;
    private View tabSavedPosts;
    private View tabLikedPosts;
    private ImageView ivTabMyPosts;
    private ImageView ivTabSavedPosts;
    private ImageView ivTabLikedPosts;
    private TextView tvTabMyPosts;
    private TextView tvTabSavedPosts;
    private TextView tvTabLikedPosts;
    private View indicatorMyPosts;
    private View indicatorSavedPosts;
    private View indicatorLikedPosts;
    private TextView tvMyPostsBadge;

    private SwipeRefreshLayout swipeRefreshProfile;
    private ScrollView scrollProfile;
    private LinearLayout gridPostsContainer;
    private TextView tvEmptyPosts;

    private TokenManager tokenManager;

    private String currentFullName = "";
    private String currentUsername = "";
    private String currentBio = "";
    private String currentAvatarUrl = "";
    private String currentCity = "";
    private int currentUserId = 0;
    private boolean skipFirstResumeFetch = true;
    private int activePostTab = TAB_MY_POSTS;
    private int postFetchSeq = 0;
    private int refreshPendingCalls = 0;

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
        updateTabUi(activePostTab);
        loadLocalProfileDetails();
        refreshProfileAndPosts(false);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (skipFirstResumeFetch) {
            skipFirstResumeFetch = false;
            return;
        }
        loadLocalProfileDetails();
        refreshProfileAndPosts(false);
    }

    @Override
    protected void onStop() {
        super.onStop();
        refreshPendingCalls = 0;
        if (swipeRefreshProfile != null) {
            swipeRefreshProfile.setRefreshing(false);
        }
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
        tabSavedPosts = findViewById(R.id.tabSavedPosts);
        tabLikedPosts = findViewById(R.id.tabLikedPosts);
        ivTabMyPosts = findViewById(R.id.ivTabMyPosts);
        ivTabSavedPosts = findViewById(R.id.ivTabSavedPosts);
        ivTabLikedPosts = findViewById(R.id.ivTabLikedPosts);
        tvTabMyPosts = findViewById(R.id.tvTabMyPosts);
        tvTabSavedPosts = findViewById(R.id.tvTabSavedPosts);
        tvTabLikedPosts = findViewById(R.id.tvTabLikedPosts);
        indicatorMyPosts = findViewById(R.id.indicatorMyPosts);
        indicatorSavedPosts = findViewById(R.id.indicatorSavedPosts);
        indicatorLikedPosts = findViewById(R.id.indicatorLikedPosts);
        tvMyPostsBadge = findViewById(R.id.tvMyPostsBadge);

        swipeRefreshProfile = findViewById(R.id.swipeRefreshProfile);
        scrollProfile = findViewById(R.id.scrollProfile);
        gridPostsContainer = findViewById(R.id.gridPostsContainer);
        tvEmptyPosts = findViewById(R.id.tvEmptyPosts);
    }

    private void loadLocalProfileDetails() {
        SharedPreferences prefs = getSharedPreferences("DishcisionPrefs", MODE_PRIVATE);
        String savedName = prefs.getString("user_name", tokenManager.getUserName());
        String savedUsername = prefs.getString("user_username", "");
        String savedAvatar = prefs.getString("avatar_url", "");
        String savedBio = prefs.getString("user_bio", "");
        String savedCity = prefs.getString("user_city", "");
        currentBio = savedBio != null ? savedBio.trim() : "";
        currentCity = savedCity != null ? savedCity.trim() : "";

        if (savedName != null && !savedName.trim().isEmpty()) {
            currentFullName = savedName.trim();
            if (tvFullName != null) tvFullName.setText(currentFullName);
        }
        if (savedUsername != null && !savedUsername.trim().isEmpty()) {
            currentUsername = savedUsername.replace("@", "").trim();
            if (tvHandle != null) tvHandle.setText("@" + currentUsername);
        }
        if (savedAvatar != null && !savedAvatar.trim().isEmpty()) {
            currentAvatarUrl = savedAvatar.trim();
        }
        if (imgAvatar != null) {
            ImageLoader.loadAvatar(imgAvatar, currentAvatarUrl);
        }
        if (tvLocation != null) {
            tvLocation.setText(!currentCity.isEmpty() ? currentCity : "Chưa cập nhật vị trí");
        }
        if (tvBio != null) {
            tvBio.setText(currentBio.isEmpty() ? "Chưa có tiểu sử giới thiệu." : currentBio);
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
                intent.putExtra("CITY", currentCity);
                startActivity(intent);
            });
        }

        if (btnQrCode != null) {
            btnQrCode.setOnClickListener(v -> showProfileQrDialog());
        }

        if (colPosts != null) {
            colPosts.setOnClickListener(v -> openMyPostsForMode(MyPostsActivity.MODE_MY_POSTS, 0, 1));
        }

        if (tabMyPosts != null) {
            tabMyPosts.setOnClickListener(v -> selectPostTab(TAB_MY_POSTS));
        }

        if (tabSavedPosts != null) {
            tabSavedPosts.setOnClickListener(v -> selectPostTab(TAB_SAVED_POSTS));
        }

        if (tabLikedPosts != null) {
            tabLikedPosts.setOnClickListener(v -> selectPostTab(TAB_LIKED_POSTS));
        }

        if (tvMyPostsBadge != null) {
            tvMyPostsBadge.setOnClickListener(v -> openMyPostsForActiveTab(0, 1));
        }

        if (colFollowers != null) {
            colFollowers.setOnClickListener(v -> openFollowersScreen("followers"));
        }

        if (colFollowing != null) {
            colFollowing.setOnClickListener(v -> openFollowersScreen("following"));
        }

        if (swipeRefreshProfile != null) {
            swipeRefreshProfile.setColorSchemeColors(0xFFFF5A36, 0xFF141B2B);
            swipeRefreshProfile.setOnChildScrollUpCallback((parent, child) -> scrollProfile != null && scrollProfile.canScrollVertically(-1));
            swipeRefreshProfile.setOnRefreshListener(() -> refreshProfileAndPosts(true));
        }

        BottomNavHelper.bind(this, BottomNavHelper.TAB_PROFILE);
    }

    private void refreshProfileAndPosts(boolean showSpinner) {
        if (showSpinner && swipeRefreshProfile != null) {
            swipeRefreshProfile.setRefreshing(true);
            refreshPendingCalls = 2;
        } else {
            refreshPendingCalls = 0;
        }
        fetchProfileFromApi();
        fetchActiveTabPosts();
    }

    private void completeRefreshCall() {
        if (refreshPendingCalls > 0) {
            refreshPendingCalls--;
        }
        if (refreshPendingCalls == 0 && swipeRefreshProfile != null) {
            swipeRefreshProfile.setRefreshing(false);
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
        if (authHeader.isEmpty()) {
            completeRefreshCall();
            return;
        }

        ApiClient.getApiService().getMyProfile(authHeader).enqueue(new Callback<UserProfileResponse>() {
            @Override
            public void onResponse(@NonNull Call<UserProfileResponse> call, @NonNull Response<UserProfileResponse> response) {
                completeRefreshCall();
                if (isFinishing() || isDestroyed()) return;
                if (response.isSuccessful() && response.body() != null && response.body().getData() != null) {
                    UserProfileResponse.UserData user = response.body().getData();

                    currentUserId = user.userId;
                    currentFullName = user.fullName != null ? user.fullName.trim() : "";
                    currentUsername = user.username != null ? user.username.replace("@", "").trim() : "";
                    currentBio = user.bio != null ? user.bio.trim() : "";
                    currentAvatarUrl = user.avatarUrl != null ? user.avatarUrl.trim() : "";
                    currentCity = user.city != null ? user.city.trim() : "";

                    SharedPreferences.Editor editor = getSharedPreferences("DishcisionPrefs", MODE_PRIVATE).edit();
                    if (currentUserId > 0) editor.putInt("user_id", currentUserId);
                    if (!currentFullName.isEmpty()) editor.putString("user_name", currentFullName);
                    if (!currentUsername.isEmpty()) editor.putString("user_username", currentUsername);
                    if (user.email != null && !user.email.isEmpty()) editor.putString("user_email", user.email);
                    if (!currentAvatarUrl.isEmpty()) editor.putString("avatar_url", currentAvatarUrl);
                    else editor.remove("avatar_url");
                    if (!currentBio.isEmpty()) editor.putString("user_bio", currentBio);
                    else editor.remove("user_bio");
                    if (!currentCity.isEmpty()) editor.putString("user_city", currentCity);
                    else editor.remove("user_city");
                    editor.putBoolean("is_google_login", user.isGoogleAccount);
                    if (user.preferences != null) {
                        editor.putStringSet("user_taste_preferences", new HashSet<>(user.preferences));
                    }
                    if (currentUserId > 0 && user.allergies != null) {
                        editor.putStringSet("user_allergies_" + currentUserId, new HashSet<>(user.allergies));
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
                    if (tvLocation != null) {
                        tvLocation.setText(!currentCity.isEmpty() ? currentCity : "Chưa cập nhật vị trí");
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
                        ImageLoader.loadAvatar(imgAvatar, currentAvatarUrl);
                    }
                    if (user.preferences != null) {
                        renderTasteTags(user.preferences);
                    }
                } else if (response.code() == 401) {
                    AuthHelper.handleUnauthorized(ProfileActivity.this, null);
                }
            }

            @Override
            public void onFailure(@NonNull Call<UserProfileResponse> call, @NonNull Throwable t) {
                completeRefreshCall();
            }
        });
    }

    private void selectPostTab(int tab) {
        if (activePostTab == tab && gridPostsContainer != null && gridPostsContainer.getChildCount() > 0) {
            return;
        }
        activePostTab = tab;
        updateTabUi(tab);
        if (gridPostsContainer != null) gridPostsContainer.removeAllViews();
        if (tvEmptyPosts != null) tvEmptyPosts.setVisibility(View.GONE);
        fetchActiveTabPosts();
    }

    private void updateTabUi(int tab) {
        applySingleTabStyle(ivTabMyPosts, tvTabMyPosts, indicatorMyPosts, tab == TAB_MY_POSTS);
        applySingleTabStyle(ivTabSavedPosts, tvTabSavedPosts, indicatorSavedPosts, tab == TAB_SAVED_POSTS);
        applySingleTabStyle(ivTabLikedPosts, tvTabLikedPosts, indicatorLikedPosts, tab == TAB_LIKED_POSTS);
    }

    private void applySingleTabStyle(ImageView icon, TextView label, View indicator, boolean active) {
        if (icon != null) {
            ImageViewCompat.setImageTintList(icon, ColorStateList.valueOf(Color.parseColor(active ? "#FF5A36" : "#8E93A6")));
        }
        if (label != null) {
            label.setTextColor(Color.parseColor(active ? "#141B2B" : "#6B7280"));
            label.setTypeface(null, active ? Typeface.BOLD : Typeface.NORMAL);
        }
        if (indicator != null) {
            indicator.setBackgroundColor(Color.parseColor(active ? "#FF5A36" : "#E9EBF4"));
            LinearLayout.LayoutParams lp = (LinearLayout.LayoutParams) indicator.getLayoutParams();
            if (lp != null) {
                lp.height = dpToPx(active ? 2 : 1);
                lp.topMargin = active ? 0 : dpToPx(1);
                indicator.setLayoutParams(lp);
            }
        }
    }

    private String getActiveTabMode() {
        if (activePostTab == TAB_SAVED_POSTS) return MyPostsActivity.MODE_SAVED;
        if (activePostTab == TAB_LIKED_POSTS) return MyPostsActivity.MODE_LIKED;
        return MyPostsActivity.MODE_MY_POSTS;
    }

    private String getEmptyMessageForTab(int tab) {
        if (tab == TAB_SAVED_POSTS) return "Chưa có bài viết đã lưu nào";
        if (tab == TAB_LIKED_POSTS) return "Chưa có bài viết yêu thích nào";
        return "Chưa có bài viết nào";
    }

    private void openMyPostsForActiveTab(int postId, int position) {
        openMyPostsForMode(getActiveTabMode(), postId, position);
    }

    private void openMyPostsForMode(String mode, int postId, int position) {
        Intent intent = new Intent(ProfileActivity.this, MyPostsActivity.class);
        intent.putExtra(MyPostsActivity.EXTRA_MODE, mode);
        if (postId > 0) intent.putExtra(MyPostsActivity.EXTRA_POST_ID, postId);
        if (position > 0) intent.putExtra(MyPostsActivity.EXTRA_POST_INDEX, position);
        startActivity(intent);
    }

    private void fetchActiveTabPosts() {
        String authHeader = ApiClient.getAuthHeader(this);
        if (authHeader.isEmpty()) {
            completeRefreshCall();
            return;
        }

        final int seq = ++postFetchSeq;
        final int targetTab = activePostTab;
        final String emptyMessage = getEmptyMessageForTab(targetTab);
        final Call<FeedPostsResponse> call;
        if (targetTab == TAB_SAVED_POSTS) {
            call = ApiClient.getApiService().getSavedPosts(authHeader, 60);
        } else if (targetTab == TAB_LIKED_POSTS) {
            call = ApiClient.getApiService().getLikedPosts(authHeader, 60);
        } else {
            call = ApiClient.getApiService().getMyPosts(authHeader);
        }

        call.enqueue(new Callback<FeedPostsResponse>() {
            @Override
            public void onResponse(@NonNull Call<FeedPostsResponse> call, @NonNull Response<FeedPostsResponse> response) {
                completeRefreshCall();
                if (isFinishing() || isDestroyed() || seq != postFetchSeq) return;
                if (response.isSuccessful() && response.body() != null) {
                    List<FeedPostsResponse.PostItem> posts = response.body().getData();
                    PostFeedHelper.renderPostGrid(
                            ProfileActivity.this,
                            gridPostsContainer,
                            tvEmptyPosts,
                            posts,
                            emptyMessage,
                            (post, pos) -> openMyPostsForMode(
                                    targetTab == TAB_SAVED_POSTS
                                            ? MyPostsActivity.MODE_SAVED
                                            : (targetTab == TAB_LIKED_POSTS ? MyPostsActivity.MODE_LIKED : MyPostsActivity.MODE_MY_POSTS),
                                    post.postId,
                                    pos + 1
                            )
                    );
                } else {
                    PostFeedHelper.renderPostGrid(ProfileActivity.this, gridPostsContainer, tvEmptyPosts, null, emptyMessage, null);
                }
            }

            @Override
            public void onFailure(@NonNull Call<FeedPostsResponse> call, @NonNull Throwable t) {
                completeRefreshCall();
                if (isFinishing() || isDestroyed() || seq != postFetchSeq) return;
                if (gridPostsContainer != null && gridPostsContainer.getChildCount() == 0) {
                    PostFeedHelper.renderPostGrid(ProfileActivity.this, gridPostsContainer, tvEmptyPosts, null, emptyMessage, null);
                }
            }
        });
    }

    private void showProfileQrDialog() {
        String displayName = !currentFullName.isEmpty() ? currentFullName : "Thành viên Dishcision";
        String handleText = !currentUsername.isEmpty() ? "@" + currentUsername.replace("@", "") : "Chưa có username";
        int resolvedId = currentUserId > 0 ? currentUserId : AuthHelper.getCurrentUserId(this);
        final String qrContent = resolvedId > 0
                ? ApiClient.getServerRootUrl() + "/users/" + resolvedId
                : (!currentUsername.isEmpty() ? "dishcision://user/" + currentUsername.replace("@", "") : displayName);

        Bitmap qrBitmap = generateQrBitmap(qrContent, dpToPx(220));
        if (qrBitmap == null) {
            new AlertDialog.Builder(this)
                    .setTitle("Mã hồ sơ cá nhân")
                    .setMessage("Tài khoản: " + displayName + "\nHandle: " + handleText)
                    .setPositiveButton("Đóng", (d, w) -> d.dismiss())
                    .show();
            return;
        }

        LinearLayout container = new LinearLayout(this);
        container.setOrientation(LinearLayout.VERTICAL);
        container.setGravity(Gravity.CENTER_HORIZONTAL);
        int pad = dpToPx(20);
        container.setPadding(pad, dpToPx(16), pad, dpToPx(8));

        ImageView ivQr = new ImageView(this);
        int sizePx = dpToPx(220);
        LinearLayout.LayoutParams qrLp = new LinearLayout.LayoutParams(sizePx, sizePx);
        ivQr.setLayoutParams(qrLp);
        ivQr.setImageBitmap(qrBitmap);
        ivQr.setContentDescription("Mã QR hồ sơ cá nhân");
        container.addView(ivQr);

        TextView tvInfo = new TextView(this);
        LinearLayout.LayoutParams infoLp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        infoLp.topMargin = dpToPx(14);
        tvInfo.setLayoutParams(infoLp);
        tvInfo.setGravity(Gravity.CENTER);
        tvInfo.setText(displayName + "\n" + handleText);
        tvInfo.setTextColor(Color.parseColor("#141B2B"));
        tvInfo.setTextSize(TypedValue.COMPLEX_UNIT_SP, 15);
        tvInfo.setTypeface(null, Typeface.BOLD);
        container.addView(tvInfo);

        TextView tvHint = new TextView(this);
        LinearLayout.LayoutParams hintLp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        hintLp.topMargin = dpToPx(6);
        tvHint.setLayoutParams(hintLp);
        tvHint.setGravity(Gravity.CENTER);
        tvHint.setText("Quét mã QR hoặc chia sẻ liên kết để bạn bè xem trang cá nhân của bạn trên Dishcision.");
        tvHint.setTextColor(Color.parseColor("#6B7280"));
        tvHint.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12);
        container.addView(tvHint);

        new AlertDialog.Builder(this)
                .setTitle("Mã hồ sơ cá nhân")
                .setView(container)
                .setNeutralButton("Sao chép liên kết", (dialog, which) -> {
                    ClipboardManager clipboard = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
                    if (clipboard != null) {
                        clipboard.setPrimaryClip(ClipData.newPlainText("Profile Link", qrContent));
                    }
                    Toast.makeText(this, "Đã sao chép liên kết hồ sơ", Toast.LENGTH_SHORT).show();
                })
                .setPositiveButton("Đóng", (dialog, which) -> dialog.dismiss())
                .show();
    }

    private Bitmap generateQrBitmap(String content, int sizePx) {
        if (content == null || content.trim().isEmpty() || sizePx <= 0) return null;
        try {
            Map<EncodeHintType, Object> hints = new EnumMap<>(EncodeHintType.class);
            hints.put(EncodeHintType.CHARACTER_SET, "UTF-8");
            hints.put(EncodeHintType.MARGIN, 1);
            BitMatrix matrix = new QRCodeWriter().encode(content, BarcodeFormat.QR_CODE, sizePx, sizePx, hints);
            int width = matrix.getWidth();
            int height = matrix.getHeight();
            int[] pixels = new int[width * height];
            int fg = Color.parseColor("#141B2B");
            int bg = Color.WHITE;
            for (int y = 0; y < height; y++) {
                int offset = y * width;
                for (int x = 0; x < width; x++) {
                    pixels[offset + x] = matrix.get(x, y) ? fg : bg;
                }
            }
            Bitmap bmp = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
            bmp.setPixels(pixels, 0, width, 0, 0, width, height);
            return bmp;
        } catch (WriterException | RuntimeException e) {
            return null;
        }
    }
}
