package com.example.dishcision;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.GravityCompat;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.example.dishcision.network.ApiClient;
import com.example.dishcision.network.AuthHelper;
import com.example.dishcision.network.CurrentUserBinder;
import com.example.dishcision.network.FeedPostsResponse;
import com.example.dishcision.network.NotificationResponse;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class HomeActivity extends AppCompatActivity {

    private DrawerLayout drawerLayout;
    private LinearLayout btnOpenSidebar;
    private ImageView btnNotification;
    private View viewNotifDot;
    private EditText edtSearch;
    private ImageView imgSearchIcon;
    private RelativeLayout btnFilter;
    private LinearLayout feedContainer;
    private TextView tvEmptyFeed;

    private LinearLayout navHome;
    private LinearLayout navDecide;
    private LinearLayout navCreate;
    private LinearLayout navMessages;
    private LinearLayout navProfile;

    private LinearLayout menuForYou;
    private LinearLayout menuFavorites;
    private LinearLayout menuFollowing;
    private LinearLayout menuReviews;
    private LinearLayout menuLogout;
    private ImageView imgSidebarAvatar;
    private TextView tvSidebarUserName;
    private TextView tvSidebarUserHandle;

    private final List<FeedPostsResponse.PostItem> allPosts = new ArrayList<>();
    private ActivityResultLauncher<Intent> postFormLauncher;
    private boolean skipFirstResumeFetch = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_home);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main_home), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars() | WindowInsetsCompat.Type.displayCutout());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        postFormLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK) {
                        fetchFeedFromBackend();
                    }
                }
        );

        initViews();
        setupListeners();
        CurrentUserBinder.bind(this, imgSidebarAvatar, tvSidebarUserName, tvSidebarUserHandle);
        fetchFeedFromBackend();
        fetchUnreadNotificationDot();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (skipFirstResumeFetch) {
            skipFirstResumeFetch = false;
            return;
        }
        CurrentUserBinder.bind(this, imgSidebarAvatar, tvSidebarUserName, tvSidebarUserHandle);
        fetchFeedFromBackend();
        fetchUnreadNotificationDot();
    }

    private void initViews() {
        drawerLayout = findViewById(R.id.drawerLayout);
        btnOpenSidebar = findViewById(R.id.btnOpenSidebar);
        btnNotification = findViewById(R.id.btnNotification);
        viewNotifDot = findViewById(R.id.viewNotifDot);
        edtSearch = findViewById(R.id.edtSearch);
        imgSearchIcon = findViewById(R.id.imgSearchIcon);
        btnFilter = findViewById(R.id.btnFilter);
        feedContainer = findViewById(R.id.feedContainer);
        tvEmptyFeed = findViewById(R.id.tvEmptyFeed);

        navHome = findViewById(R.id.navHome);
        navDecide = findViewById(R.id.navDecide);
        navCreate = findViewById(R.id.navCreate);
        navMessages = findViewById(R.id.navMessages);
        navProfile = findViewById(R.id.navProfile);

        menuForYou = findViewById(R.id.menuForYou);
        menuFavorites = findViewById(R.id.menuFavorites);
        menuFollowing = findViewById(R.id.menuFollowing);
        menuReviews = findViewById(R.id.menuReviews);
        menuLogout = findViewById(R.id.menuLogout);
        imgSidebarAvatar = findViewById(R.id.imgSidebarAvatar);
        tvSidebarUserName = findViewById(R.id.tvSidebarUserName);
        tvSidebarUserHandle = findViewById(R.id.tvSidebarUserHandle);
    }

    private void setupListeners() {
        if (btnOpenSidebar != null && drawerLayout != null) {
            btnOpenSidebar.setOnClickListener(v -> drawerLayout.openDrawer(GravityCompat.START));
        }

        if (btnNotification != null) {
            btnNotification.setOnClickListener(v -> startActivity(new Intent(HomeActivity.this, NotificationsActivity.class)));
        }

        if (edtSearch != null) {
            edtSearch.addTextChangedListener(new TextWatcher() {
                @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    filterAndRenderFeed();
                }

                @Override public void afterTextChanged(Editable s) {}
            });
        }

        if (imgSearchIcon != null) {
            imgSearchIcon.setOnClickListener(v -> startActivity(new Intent(HomeActivity.this, SearchActivity.class)));
        }

        if (btnFilter != null) {
            btnFilter.setOnClickListener(v -> startActivity(new Intent(HomeActivity.this, SearchActivity.class)));
        }

        if (navHome != null) {
            navHome.setOnClickListener(v -> {
                ScrollView scrollFeed = findViewById(R.id.scrollFeed);
                if (scrollFeed != null) scrollFeed.smoothScrollTo(0, 0);
            });
        }

        if (navDecide != null) {
            navDecide.setOnClickListener(v -> startActivity(new Intent(HomeActivity.this, DecideActivity.class)));
        }

        if (navCreate != null) {
            navCreate.setOnClickListener(v -> postFormLauncher.launch(new Intent(HomeActivity.this, CreatePostActivity.class)));
        }

        if (navMessages != null) {
            navMessages.setOnClickListener(v -> startActivity(new Intent(HomeActivity.this, MessagesInboxActivity.class)));
        }

        if (navProfile != null) {
            navProfile.setOnClickListener(v -> startActivity(new Intent(HomeActivity.this, ProfileActivity.class)));
        }

        if (menuForYou != null) {
            menuForYou.setOnClickListener(v -> {
                if (drawerLayout != null) drawerLayout.closeDrawer(GravityCompat.START);
                startActivity(new Intent(HomeActivity.this, DecideActivity.class));
            });
        }

        if (menuFavorites != null) {
            menuFavorites.setOnClickListener(v -> {
                if (drawerLayout != null) drawerLayout.closeDrawer(GravityCompat.START);
                startActivity(new Intent(HomeActivity.this, FavoriteDishesActivity.class));
            });
        }

        if (menuFollowing != null) {
            menuFollowing.setOnClickListener(v -> {
                if (drawerLayout != null) drawerLayout.closeDrawer(GravityCompat.START);
                startActivity(new Intent(HomeActivity.this, FavoriteRestaurantsActivity.class));
            });
        }

        if (menuReviews != null) {
            menuReviews.setOnClickListener(v -> {
                if (drawerLayout != null) drawerLayout.closeDrawer(GravityCompat.START);
                startActivity(new Intent(HomeActivity.this, MyReviewsActivity.class));
            });
        }

        LinearLayout menuMyPosts = findViewById(R.id.menuMyPosts);
        if (menuMyPosts != null) {
            menuMyPosts.setOnClickListener(v -> {
                if (drawerLayout != null) drawerLayout.closeDrawer(GravityCompat.START);
                startActivity(new Intent(HomeActivity.this, MyPostsActivity.class));
            });
        }

        if (menuLogout != null) {
            menuLogout.setOnClickListener(v -> AuthHelper.logout(HomeActivity.this));
        }
    }

    private void fetchUnreadNotificationDot() {
        String authHeader = ApiClient.getAuthHeader(this);
        if (authHeader.isEmpty() || viewNotifDot == null) {
            if (viewNotifDot != null) viewNotifDot.setVisibility(View.GONE);
            return;
        }
        ApiClient.getApiService().getNotifications(authHeader).enqueue(new Callback<NotificationResponse>() {
            @Override
            public void onResponse(@NonNull Call<NotificationResponse> call, @NonNull Response<NotificationResponse> response) {
                if (isFinishing() || isDestroyed() || viewNotifDot == null) return;
                boolean hasUnread = false;
                if (response.isSuccessful() && response.body() != null && response.body().data != null) {
                    for (NotificationResponse.NotificationItem item : response.body().data) {
                        if (item != null && !item.isRead) {
                            hasUnread = true;
                            break;
                        }
                    }
                }
                viewNotifDot.setVisibility(hasUnread ? View.VISIBLE : View.GONE);
            }

            @Override
            public void onFailure(@NonNull Call<NotificationResponse> call, @NonNull Throwable t) {}
        });
    }

    private void fetchFeedFromBackend() {
        String authHeader = ApiClient.getAuthHeader(this);
        ApiClient.getApiService().getFeedPosts(authHeader).enqueue(new Callback<FeedPostsResponse>() {
            @Override
            public void onResponse(@NonNull Call<FeedPostsResponse> call, @NonNull Response<FeedPostsResponse> response) {
                if (isFinishing()) return;
                if (response.isSuccessful() && response.body() != null && response.body().getData() != null) {
                    allPosts.clear();
                    allPosts.addAll(response.body().getData());
                    filterAndRenderFeed();
                } else if (allPosts.isEmpty()) {
                    showEmptyState("Chưa thể tải bảng tin lúc này.");
                }
            }

            @Override
            public void onFailure(@NonNull Call<FeedPostsResponse> call, @NonNull Throwable t) {
                if (isFinishing()) return;
                if (allPosts.isEmpty()) {
                    showEmptyState("Không thể kết nối máy chủ.");
                } else {
                    Toast.makeText(HomeActivity.this, "Không thể làm mới bảng tin", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void filterAndRenderFeed() {
        if (feedContainer == null) return;
        String query = edtSearch != null && edtSearch.getText() != null
                ? edtSearch.getText().toString().toLowerCase(Locale.ROOT).trim()
                : "";

        List<FeedPostsResponse.PostItem> matched = new ArrayList<>();
        for (FeedPostsResponse.PostItem post : allPosts) {
            if (post == null) continue;
            if (query.isEmpty() || matchesQuery(post, query)) {
                matched.add(post);
            }
        }

        feedContainer.removeAllViews();
        if (matched.isEmpty()) {
            showEmptyState(query.isEmpty() ? "Chưa có bài viết nào." : "Không tìm thấy bài viết phù hợp.");
            return;
        }
        if (tvEmptyFeed != null) tvEmptyFeed.setVisibility(View.GONE);

        LayoutInflater inflater = LayoutInflater.from(this);
        PostFeedHelper.PostCardListener cardListener = new PostFeedHelper.PostCardListener() {
            @Override
            public void onPostDeleted(int postId) {
                removePostById(postId);
            }

            @Override
            public void onPostArchived(int postId) {
                removePostById(postId);
            }
        };

        for (FeedPostsResponse.PostItem post : matched) {
            View cardView = inflater.inflate(R.layout.item_feed_post, feedContainer, false);
            PostFeedHelper.bindPostCard(this, cardView, post, postFormLauncher, cardListener);
            feedContainer.addView(cardView);
        }
    }

    private void removePostById(int postId) {
        for (int i = allPosts.size() - 1; i >= 0; i--) {
            if (allPosts.get(i).postId == postId) {
                allPosts.remove(i);
            }
        }
        filterAndRenderFeed();
    }

    private boolean matchesQuery(FeedPostsResponse.PostItem post, String query) {
        if (post.content != null && post.content.toLowerCase(Locale.ROOT).contains(query)) return true;
        if (post.voucherCode != null && post.voucherCode.toLowerCase(Locale.ROOT).contains(query)) return true;
        String loc = PostFeedHelper.decodeLocationDisplay(post.locationUrl).toLowerCase(Locale.ROOT);
        if (loc.contains(query)) return true;
        if (post.author != null) {
            if (post.author.fullName != null && post.author.fullName.toLowerCase(Locale.ROOT).contains(query)) return true;
            if (post.author.username != null && post.author.username.toLowerCase(Locale.ROOT).contains(query)) return true;
        }
        String tags = PostFeedHelper.formatHashtags(post.hashtags).toLowerCase(Locale.ROOT);
        return tags.contains(query);
    }

    private void showEmptyState(String msg) {
        if (feedContainer != null) feedContainer.removeAllViews();
        if (tvEmptyFeed != null) {
            tvEmptyFeed.setText(msg);
            tvEmptyFeed.setVisibility(View.VISIBLE);
        }
    }

    public static String getInitials(String name) {
        if (name == null || name.trim().isEmpty()) return "U";
        String[] parts = name.trim().split("\\s+");
        if (parts.length == 1) {
            return parts[0].substring(0, Math.min(2, parts[0].length())).toUpperCase();
        }
        return (parts[0].substring(0, 1) + parts[parts.length - 1].substring(0, 1)).toUpperCase();
    }
}
