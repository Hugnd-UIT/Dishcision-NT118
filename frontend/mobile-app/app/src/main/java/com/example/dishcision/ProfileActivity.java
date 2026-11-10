package com.example.dishcision;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.dishcision.network.ApiClient;
import com.example.dishcision.network.FeedPostsResponse;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import java.util.List;
import com.example.dishcision.network.ImageLoader;
import com.example.dishcision.network.TokenManager;
import com.example.dishcision.network.UserProfileResponse;

import java.util.ArrayList;
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
    private TextView tvHandle;
    private TextView tvFullName;
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

    // 3-Column Grid of Posts (Window Tiles)
    private View cardGridPost1;
    private View cardGridPost2;
    private View cardGridPost3;
    private View cardGridPost4;
    private View cardGridPost5;
    private View cardGridPost6;

    private ImageView imgGridPost1;
    private ImageView imgGridPost2;
    private ImageView imgGridPost3;
    private ImageView imgGridPost4;
    private ImageView imgGridPost5;
    private ImageView imgGridPost6;

    private TextView tvGridRating1;
    private TextView tvGridRating2;
    private TextView tvGridRating3;
    private TextView tvGridRating4;
    private TextView tvGridRating5;
    private TextView tvGridRating6;
    private LinearLayout rowGridPosts1;
    private LinearLayout rowGridPosts2;
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
        fetchProfileFromApi();
        fetchMyPostsFromBackend();
    }

    @Override
    protected void onResume() {
        super.onResume();
        fetchProfileFromApi();
        loadLocalProfileDetails();
        fetchMyPostsFromBackend();
    }

    private void initViews() {
        btnBack = findViewById(R.id.btnBack);
        btnShare = findViewById(R.id.btnShare);
        btnSettings = findViewById(R.id.btnSettings);
        btnEditProfile = findViewById(R.id.btnEditProfile);
        btnQrCode = findViewById(R.id.btnQrCode);

        imgAvatar = findViewById(R.id.imgAvatar);
        tvHandle = findViewById(R.id.tvHandle);
        tvFullName = findViewById(R.id.tvFullName);
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

        cardGridPost1 = findViewById(R.id.cardGridPost1);
        cardGridPost2 = findViewById(R.id.cardGridPost2);
        cardGridPost3 = findViewById(R.id.cardGridPost3);
        cardGridPost4 = findViewById(R.id.cardGridPost4);
        cardGridPost5 = findViewById(R.id.cardGridPost5);
        cardGridPost6 = findViewById(R.id.cardGridPost6);

        imgGridPost1 = findViewById(R.id.imgGridPost1);
        imgGridPost2 = findViewById(R.id.imgGridPost2);
        imgGridPost3 = findViewById(R.id.imgGridPost3);
        imgGridPost4 = findViewById(R.id.imgGridPost4);
        imgGridPost5 = findViewById(R.id.imgGridPost5);
        imgGridPost6 = findViewById(R.id.imgGridPost6);

        tvGridRating1 = findViewById(R.id.tvGridRating1);
        tvGridRating2 = findViewById(R.id.tvGridRating2);
        tvGridRating3 = findViewById(R.id.tvGridRating3);
        tvGridRating4 = findViewById(R.id.tvGridRating4);
        tvGridRating5 = findViewById(R.id.tvGridRating5);
        tvGridRating6 = findViewById(R.id.tvGridRating6);
        rowGridPosts1 = findViewById(R.id.rowGridPosts1);
        rowGridPosts2 = findViewById(R.id.rowGridPosts2);
        tvEmptyPosts = findViewById(R.id.tvEmptyPosts);

        navHome = findViewById(R.id.navHome);
        navDecide = findViewById(R.id.navDecide);
        navAdd = findViewById(R.id.navAdd);
        navMessage = findViewById(R.id.navMessage);
        navProfile = findViewById(R.id.navProfile);
    }

    private void loadLocalProfileDetails() {
        SharedPreferences prefs = getSharedPreferences("DishcisionPrefs", MODE_PRIVATE);
        String savedCity = prefs.getString("user_city", "TP. Hồ Chí Minh");
        if (tvLocation != null) {
            tvLocation.setText(savedCity);
        }

        Set<String> savedTastes = prefs.getStringSet("user_taste_preferences", null);
        renderTasteTags(savedTastes);
    }

    private void renderTasteTags(java.util.Collection<String> tastes) {
        if (layoutProfileTags == null) return;
        layoutProfileTags.removeAllViews();
        if (tastes == null || tastes.isEmpty()) return;

        for (String t : tastes) {
            String cleanTag = "#" + t.replaceAll("[^a-zA-Z0-9À-ỹ]", "");
            if (cleanTag.equals("#")) cleanTag = "#KhauVi";
            TextView tagTv = new TextView(this);
            tagTv.setBackgroundResource(R.drawable.bg_tag_chip_pink);
            tagTv.setText(cleanTag);
            tagTv.setTextColor(Color.parseColor("#FF5A36"));
            tagTv.setTextSize(12);
            tagTv.setTypeface(tagTv.getTypeface(), android.graphics.Typeface.BOLD);
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
                String handle = tvHandle != null ? tvHandle.getText().toString() : "";
                Intent shareIntent = new Intent(Intent.ACTION_SEND);
                shareIntent.setType("text/plain");
                shareIntent.putExtra(Intent.EXTRA_SUBJECT, "Dishcision Profile");
                shareIntent.putExtra(Intent.EXTRA_TEXT, "Xem hồ sơ người dùng " + (currentFullName.isEmpty() ? handle : currentFullName) + " trên ứng dụng Dishcision: " + handle);
                startActivity(Intent.createChooser(shareIntent, "Chia sẻ hồ sơ qua"));
            });
        }

        if (btnSettings != null) {
            btnSettings.setOnClickListener(v -> {
                Intent intent = new Intent(ProfileActivity.this, SettingsActivity.class);
                startActivity(intent);
            });
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
                new AlertDialog.Builder(this)
                        .setTitle("Mã QR cá nhân")
                        .setMessage("Tài khoản: " + (currentFullName.isEmpty() ? "User" : currentFullName) + "\nHandle: " + (currentUsername.isEmpty() ? "@user" : "@" + currentUsername) + "\n\nTính năng quét mã QR kết bạn trực tiếp đang sẵn sàng kết nối.")
                        .setPositiveButton("Đóng", (dialog, which) -> dialog.dismiss())
                        .show();
            });
        }

        // Navigation to Followers & Friends & MyPosts
        if (colPosts != null) {
            colPosts.setOnClickListener(v -> {
                Intent intent = new Intent(ProfileActivity.this, MyPostsActivity.class);
                startActivity(intent);
            });
        }

        if (tabMyPosts != null) {
            tabMyPosts.setOnClickListener(v -> {
                Intent intent = new Intent(ProfileActivity.this, MyPostsActivity.class);
                startActivity(intent);
            });
        }

        if (colFollowers != null) {
            colFollowers.setOnClickListener(v -> {
                Intent intent = new Intent(ProfileActivity.this, FollowersActivity.class);
                if (tvFullName != null) intent.putExtra("DISPLAY_NAME", tvFullName.getText().toString());
                if (tvHandle != null) intent.putExtra("USERNAME", tvHandle.getText().toString());
                if (tvFollowersCount != null) intent.putExtra("FOLLOWERS_COUNT", tvFollowersCount.getText().toString());
                if (tvFollowingCount != null) intent.putExtra("FOLLOWING_COUNT", tvFollowingCount.getText().toString());
                intent.putExtra("ACTIVE_TAB", "followers");
                startActivity(intent);
            });
        }

        if (colFollowing != null) {
            colFollowing.setOnClickListener(v -> {
                Intent intent = new Intent(ProfileActivity.this, FollowersActivity.class);
                if (tvFullName != null) intent.putExtra("DISPLAY_NAME", tvFullName.getText().toString());
                if (tvHandle != null) intent.putExtra("USERNAME", tvHandle.getText().toString());
                if (tvFollowersCount != null) intent.putExtra("FOLLOWERS_COUNT", tvFollowersCount.getText().toString());
                if (tvFollowingCount != null) intent.putExtra("FOLLOWING_COUNT", tvFollowingCount.getText().toString());
                intent.putExtra("ACTIVE_TAB", "following");
                startActivity(intent);
            });
        }

        setupPostInteractions();

        // Bottom Navigation Bar
        if (navHome != null) {
            navHome.setOnClickListener(v -> {
                Intent intent = new Intent(ProfileActivity.this, HomeActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                startActivity(intent);
                finish();
            });
        }

        if (navDecide != null) {
            navDecide.setOnClickListener(v -> {
                Intent intent = new Intent(ProfileActivity.this, DecideActivity.class);
                startActivity(intent);
            });
        }

        if (navAdd != null) {
            navAdd.setOnClickListener(v -> {
                Intent intent = new Intent(ProfileActivity.this, CreatePostActivity.class);
                startActivity(intent);
            });
        }

        if (navMessage != null) {
            navMessage.setOnClickListener(v -> {
                Intent intent = new Intent(ProfileActivity.this, MessagesInboxActivity.class);
                startActivity(intent);
            });
        }

        if (navProfile != null) {
            navProfile.setOnClickListener(v -> {
                // Already on Profile
            });
        }
    }

    private void fetchProfileFromApi() {
        String authHeader = ApiClient.getAuthHeader(this);
        ApiClient.getApiService().getMyProfile(authHeader).enqueue(new Callback<UserProfileResponse>() {
            @Override
            public void onResponse(Call<UserProfileResponse> call, Response<UserProfileResponse> response) {
                if (response.isSuccessful() && response.body() != null && response.body().getData() != null) {
                    UserProfileResponse.UserData user = response.body().getData();

                    currentFullName = user.fullName != null ? user.fullName : "";
                    currentUsername = user.username != null ? user.username : "";
                    currentBio = user.bio != null ? user.bio : "";
                    currentAvatarUrl = user.avatarUrl != null ? user.avatarUrl : "";

                    if (tvFullName != null && !currentFullName.isEmpty()) {
                        tvFullName.setText(currentFullName);
                    }
                    if (tvHandle != null && !currentUsername.isEmpty()) {
                        tvHandle.setText(currentUsername.startsWith("@") ? currentUsername : "@" + currentUsername);
                    }
                    if (tvBio != null) {
                        tvBio.setText(currentBio.isEmpty() ? "Chưa có tiểu sử giới thiệu." : currentBio);
                    }
                    if (tvPostsCount != null) {
                        tvPostsCount.setText(String.valueOf(user.postsCount));
                    }
                    if (tvFollowersCount != null) {
                        tvFollowersCount.setText(String.valueOf(user.followersCount));
                    }
                    if (tvFollowingCount != null) {
                        tvFollowingCount.setText(String.valueOf(user.followingCount));
                    }
                    if (imgAvatar != null && !currentAvatarUrl.isEmpty()) {
                        ImageLoader.loadAvatar(imgAvatar, currentAvatarUrl);
                    }
                    if (user.preferences != null && !user.preferences.isEmpty()) {
                        getSharedPreferences("DishcisionPrefs", MODE_PRIVATE).edit()
                                .putStringSet("user_taste_preferences", new java.util.HashSet<>(user.preferences))
                                .apply();
                        renderTasteTags(user.preferences);
                    }
                }
            }

            @Override
            public void onFailure(Call<UserProfileResponse> call, Throwable t) {
                // Silently fallback
            }
        });
    }
    private void setupPostInteractions() {
        View[] gridCards = {cardGridPost1, cardGridPost2, cardGridPost3, cardGridPost4, cardGridPost5, cardGridPost6};
        for (int i = 0; i < gridCards.length; i++) {
            if (gridCards[i] == null) continue;
            final int postIndex = i + 1;
            gridCards[i].setOnClickListener(v -> {
                Intent intent = new Intent(ProfileActivity.this, MyPostsActivity.class);
                intent.putExtra(MyPostsActivity.EXTRA_POST_INDEX, postIndex);
                startActivity(intent);
            });
        }
    }

    private void fetchMyPostsFromBackend() {
        String authHeader = ApiClient.getAuthHeader(this);
        ApiClient.getApiService().getMyPosts(authHeader).enqueue(new Callback<FeedPostsResponse>() {
            @Override
            public void onResponse(Call<FeedPostsResponse> call, Response<FeedPostsResponse> response) {
                if (response.isSuccessful() && response.body() != null && response.body().getData() != null) {
                    List<FeedPostsResponse.PostItem> posts = response.body().getData();
                    bindProfilePosts(posts);
                }
            }

            @Override
            public void onFailure(Call<FeedPostsResponse> call, Throwable t) {}
        });
    }

    private void bindProfilePosts(List<FeedPostsResponse.PostItem> posts) {
        View[] gridCards = new View[]{cardGridPost1, cardGridPost2, cardGridPost3, cardGridPost4, cardGridPost5, cardGridPost6};
        ImageView[] gridImgs = new ImageView[]{imgGridPost1, imgGridPost2, imgGridPost3, imgGridPost4, imgGridPost5, imgGridPost6};
        TextView[] gridRatings = new TextView[]{tvGridRating1, tvGridRating2, tvGridRating3, tvGridRating4, tvGridRating5, tvGridRating6};

        int count = (posts != null) ? posts.size() : 0;

        if (tvPostsCount != null) {
            tvPostsCount.setText(String.valueOf(count));
        }

        if (count == 0) {
            if (rowGridPosts1 != null) rowGridPosts1.setVisibility(View.GONE);
            if (rowGridPosts2 != null) rowGridPosts2.setVisibility(View.GONE);
            if (tvEmptyPosts != null) tvEmptyPosts.setVisibility(View.VISIBLE);
            return;
        }

        if (tvEmptyPosts != null) tvEmptyPosts.setVisibility(View.GONE);
        if (rowGridPosts1 != null) rowGridPosts1.setVisibility(View.VISIBLE);
        if (rowGridPosts2 != null) rowGridPosts2.setVisibility(count > 3 ? View.VISIBLE : View.GONE);

        for (int i = 0; i < gridCards.length; i++) {
            if (gridCards[i] == null) continue;

            if (i < count) {
                gridCards[i].setVisibility(View.VISIBLE);
                FeedPostsResponse.PostItem post = posts.get(i);
                if (gridImgs[i] != null && post.images != null && !post.images.isEmpty()) {
                    ImageLoader.loadImage(gridImgs[i], post.images.get(0).imageUrl);
                }
                if (gridRatings[i] != null) {
                    if (post.likesCount > 0) {
                        gridRatings[i].setText("👍 " + post.likesCount);
                        gridRatings[i].setVisibility(View.VISIBLE);
                    } else {
                        gridRatings[i].setVisibility(View.GONE);
                    }
                }
            } else {
                if (i < 3) {
                    // Keep invisible so columns 1 & 2 maintain 1/3 width
                    gridCards[i].setVisibility(View.INVISIBLE);
                } else if (i >= 3 && count < 6) {
                    gridCards[i].setVisibility(View.INVISIBLE);
                } else {
                    gridCards[i].setVisibility(View.GONE);
                }
            }
        }
    }

}
