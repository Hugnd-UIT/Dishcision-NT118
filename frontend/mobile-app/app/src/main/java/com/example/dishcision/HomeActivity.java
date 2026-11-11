package com.example.dishcision;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.PopupMenu;
import androidx.core.graphics.Insets;
import androidx.core.view.GravityCompat;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.example.dishcision.network.ApiClient;
import com.example.dishcision.network.AuthHelper;
import com.example.dishcision.network.CommentsListResponse;
import com.example.dishcision.network.CreateCommentRequest;
import com.example.dishcision.network.CreateCommentResponse;
import com.example.dishcision.network.SavePostButton;
import com.example.dishcision.network.FeedPostsResponse;
import com.example.dishcision.network.ImageLoader;
import com.example.dishcision.network.TokenManager;
import com.example.dishcision.network.UserProfileResponse;
import com.google.android.material.bottomsheet.BottomSheetDialog;

import java.util.List;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class HomeActivity extends AppCompatActivity {

    private DrawerLayout drawerLayout;
    private LinearLayout btnOpenSidebar;
    private ImageView btnNotification;
    private EditText edtSearch;
    private ImageView imgSearchIcon;
    private RelativeLayout btnFilter;

    // Bottom Navigation Bar Items
    private LinearLayout navHome;
    private LinearLayout navDecide;
    private LinearLayout navCreate;
    private LinearLayout navMessages;
    private LinearLayout navProfile;

    // Sidebar Items
    private LinearLayout menuForYou;
    private LinearLayout menuFavorites;
    private LinearLayout menuFollowing;
    private LinearLayout menuReviews;
    private LinearLayout menuLogout;
    private TextView tvSidebarUserName;
    private TextView tvSidebarUserHandle;

    // Feed Post Interactive Elements
    private RelativeLayout containerAvatar1;
    private RelativeLayout containerAvatar2;
    private RelativeLayout containerAvatar3;

    private RelativeLayout containerPostImage1;
    private RelativeLayout containerPostImage2;
    private RelativeLayout containerPostImage3;

    private TextView tvAuthorName1, tvUsername1, tvLocation1;
    private TextView tvAuthorName2, tvLocation2;
    private TextView tvAuthorName3, tvUsername3, tvLocation3;
    private TextView tvPostText1, tvPostText2, tvPostText3;

    private TextView tvHashtags1;
    private TextView tvHashtags2;
    private TextView tvHashtags3;

    private LinearLayout btnLike1;
    private LinearLayout btnLike2;
    private LinearLayout btnLike3;

    private LinearLayout btnComment1;
    private LinearLayout btnComment2;
    private LinearLayout btnComment3;

    private ImageView btnShare1;
    private ImageView btnShare2;
    private ImageView btnShare3;

    private ImageView btnBookmark1;
    private ImageView btnBookmark2;
    private ImageView btnBookmark3;

    private boolean isLiked1 = false;
    private boolean isLiked2 = false;
    private boolean isLiked3 = false;

    private FeedPostsResponse.PostItem currentPost1;
    private FeedPostsResponse.PostItem currentPost2;
    private FeedPostsResponse.PostItem currentPost3;

    private final SavePostButton[] saveButtons = new SavePostButton[3];

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_home);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main_home), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        initViews();
        setupListeners();
        fetchFeedFromBackend();
    }

    @Override
    protected void onResume() {
        super.onResume();
        fetchFeedFromBackend();
    }

    private void initViews() {
        drawerLayout = findViewById(R.id.drawerLayout);
        btnOpenSidebar = findViewById(R.id.btnOpenSidebar);
        btnNotification = findViewById(R.id.btnNotification);
        edtSearch = findViewById(R.id.edtSearch);
        imgSearchIcon = findViewById(R.id.imgSearchIcon);
        btnFilter = findViewById(R.id.btnFilter);

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
        tvSidebarUserName = findViewById(R.id.tvSidebarUserName);
        tvSidebarUserHandle = findViewById(R.id.tvSidebarUserHandle);

        containerAvatar1 = findViewById(R.id.containerAvatar1);
        containerAvatar2 = findViewById(R.id.containerAvatar2);
        containerAvatar3 = findViewById(R.id.containerAvatar3);

        containerPostImage1 = findViewById(R.id.containerPostImage1);
        containerPostImage2 = findViewById(R.id.containerPostImage2);
        containerPostImage3 = findViewById(R.id.containerPostImage3);

        tvAuthorName1 = findViewById(R.id.tvAuthorName1);
        tvUsername1 = findViewById(R.id.tvUsername1);
        tvLocation1 = findViewById(R.id.tvLocation1);

        tvAuthorName2 = findViewById(R.id.tvAuthorName2);
        tvLocation2 = findViewById(R.id.tvLocation2);

        tvAuthorName3 = findViewById(R.id.tvAuthorName3);
        tvUsername3 = findViewById(R.id.tvUsername3);
        tvLocation3 = findViewById(R.id.tvLocation3);

        tvPostText1 = findViewById(R.id.tvPostText1);
        tvPostText2 = findViewById(R.id.tvPostText2);
        tvPostText3 = findViewById(R.id.tvPostText3);

        tvHashtags1 = findViewById(R.id.tvHashtags1);
        tvHashtags2 = findViewById(R.id.tvHashtags2);
        tvHashtags3 = findViewById(R.id.tvHashtags3);

        btnLike1 = findViewById(R.id.btnLike1);
        btnLike2 = findViewById(R.id.btnLike2);
        btnLike3 = findViewById(R.id.btnLike3);

        btnComment1 = findViewById(R.id.btnComment1);
        btnComment2 = findViewById(R.id.btnComment2);
        btnComment3 = findViewById(R.id.btnComment3);

        btnShare1 = findViewById(R.id.btnShare1);
        btnShare2 = findViewById(R.id.btnShare2);
        btnShare3 = findViewById(R.id.btnShare3);

        btnBookmark1 = findViewById(R.id.btnBookmark1);
        btnBookmark2 = findViewById(R.id.btnBookmark2);
        btnBookmark3 = findViewById(R.id.btnBookmark3);

        View card1 = containerAvatar1 != null && containerAvatar1.getParent() != null && containerAvatar1.getParent().getParent() instanceof View ? (View) containerAvatar1.getParent().getParent() : null;
        View card2 = containerAvatar2 != null && containerAvatar2.getParent() != null && containerAvatar2.getParent().getParent() instanceof View ? (View) containerAvatar2.getParent().getParent() : null;
        View card3 = containerAvatar3 != null && containerAvatar3.getParent() != null && containerAvatar3.getParent().getParent() instanceof View ? (View) containerAvatar3.getParent().getParent() : null;
        if (card1 != null) card1.setVisibility(View.GONE);
        if (card2 != null) card2.setVisibility(View.GONE);
        if (card3 != null) card3.setVisibility(View.GONE);
    }

    private void setupListeners() {
        // Top Header
        if (btnOpenSidebar != null && drawerLayout != null) {
            btnOpenSidebar.setOnClickListener(v -> drawerLayout.openDrawer(GravityCompat.START));
        }

        if (btnNotification != null) {
            btnNotification.setOnClickListener(v -> {
                Intent intent = new Intent(HomeActivity.this, NotificationsActivity.class);
                startActivity(intent);
            });
        }

        // Realtime Feed Search
        if (edtSearch != null) {
            edtSearch.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    filterFeed(s.toString().toLowerCase().trim());
                }

                @Override
                public void afterTextChanged(Editable s) {}
            });
        }

        if (imgSearchIcon != null) {
            imgSearchIcon.setOnClickListener(v -> {
                Intent intent = new Intent(HomeActivity.this, SearchActivity.class);
                startActivity(intent);
            });
        }

        if (btnFilter != null) {
            btnFilter.setOnClickListener(v -> {
                Intent intent = new Intent(HomeActivity.this, SearchActivity.class);
                startActivity(intent);
            });
        }

        // Bottom Navigation Bar
        if (navHome != null) {
            navHome.setOnClickListener(v -> {
                ScrollView scrollFeed = findViewById(R.id.scrollFeed);
                if (scrollFeed != null) scrollFeed.smoothScrollTo(0, 0);
            });
        }

        if (navDecide != null) {
            navDecide.setOnClickListener(v -> {
                Intent intent = new Intent(HomeActivity.this, DecideActivity.class);
                startActivity(intent);
            });
        }

        if (navCreate != null) {
            navCreate.setOnClickListener(v -> {
                Intent intent = new Intent(HomeActivity.this, CreatePostActivity.class);
                startActivity(intent);
            });
        }

        if (navMessages != null) {
            navMessages.setOnClickListener(v -> {
                Intent intent = new Intent(HomeActivity.this, MessagesInboxActivity.class);
                startActivity(intent);
            });
        }

        if (navProfile != null) {
            navProfile.setOnClickListener(v -> {
                Intent intent = new Intent(HomeActivity.this, ProfileActivity.class);
                startActivity(intent);
            });
        }

        // Sidebar Menu
        if (menuForYou != null) {
            menuForYou.setOnClickListener(v -> {
                if (drawerLayout != null) drawerLayout.closeDrawer(GravityCompat.START);
                Intent intent = new Intent(HomeActivity.this, DecideActivity.class);
                startActivity(intent);
            });
        }

        if (menuFavorites != null) {
            menuFavorites.setOnClickListener(v -> {
                if (drawerLayout != null) drawerLayout.closeDrawer(GravityCompat.START);
                Intent intent = new Intent(HomeActivity.this, FavoriteDishesActivity.class);
                startActivity(intent);
            });
        }

        if (menuFollowing != null) {
            menuFollowing.setOnClickListener(v -> {
                if (drawerLayout != null) drawerLayout.closeDrawer(GravityCompat.START);
                Intent intent = new Intent(HomeActivity.this, FavoriteRestaurantsActivity.class);
                startActivity(intent);
            });
        }

        if (menuReviews != null) {
            menuReviews.setOnClickListener(v -> {
                if (drawerLayout != null) drawerLayout.closeDrawer(GravityCompat.START);
                Intent intent = new Intent(HomeActivity.this, MyReviewsActivity.class);
                startActivity(intent);
            });
        }

        LinearLayout menuMyPosts = findViewById(R.id.menuMyPosts);
        if (menuMyPosts != null) {
            menuMyPosts.setOnClickListener(v -> {
                if (drawerLayout != null) drawerLayout.closeDrawer(GravityCompat.START);
                Intent intent = new Intent(HomeActivity.this, MyPostsActivity.class);
                startActivity(intent);
            });
        }

        if (menuLogout != null) {
            menuLogout.setOnClickListener(v -> AuthHelper.logout(HomeActivity.this));
        }

        // User Avatars in Feed
        View.OnClickListener openUserProfile = v -> {
            Intent intent = new Intent(HomeActivity.this, UserProfileActivity.class);
            startActivity(intent);
        };
        if (containerAvatar1 != null) containerAvatar1.setOnClickListener(openUserProfile);
        if (containerAvatar2 != null) containerAvatar2.setOnClickListener(openUserProfile);
        if (containerAvatar3 != null) containerAvatar3.setOnClickListener(openUserProfile);

        // Fullscreen Facebook-style Photo Viewer on Post Image Click
        if (containerPostImage1 != null) {
            containerPostImage1.setOnClickListener(v -> {
                String author = (tvAuthorName1 != null ? tvAuthorName1.getText() : "Maya Lin") + " • " + (tvUsername1 != null ? tvUsername1.getText() : "@mayafoodie");
                String caption = tvPostText1 != null ? tvPostText1.getText().toString() : "Tìm thấy tô salad ngũ sắc tươi ngon nhất phố! 🥗✨ #MonChay";
                openFullscreenImage(author, caption);
            });
        }
        if (containerPostImage2 != null) {
            containerPostImage2.setOnClickListener(v -> {
                String author = tvAuthorName2 != null ? tvAuthorName2.getText().toString() : "Julian Cole";
                String caption = tvPostText2 != null ? tvPostText2.getText().toString() : "Món pasta thủ công tươi mới vừa ra lò chiều nay! 🍝🇮🇹 #ItalianFood";
                openFullscreenImage(author, caption);
            });
        }
        if (containerPostImage3 != null) {
            containerPostImage3.setOnClickListener(v -> {
                String author = (tvAuthorName3 != null ? tvAuthorName3.getText() : "An Nhiên") + " • " + (tvUsername3 != null ? tvUsername3.getText() : "@annhien_food");
                String caption = tvPostText3 != null ? tvPostText3.getText().toString() : "Bánh mousse dâu tây mọng nước ngọt thanh nhẹ dịu... 🍰🍓 #BanhNgot";
                openFullscreenImage(author, caption);
            });
        }

        // Hashtags Clickable
        View.OnClickListener hashtagListener = v -> {
            Intent intent = new Intent(HomeActivity.this, SearchResultsActivity.class);
            startActivity(intent);
        };
        if (tvHashtags1 != null) tvHashtags1.setOnClickListener(hashtagListener);
        if (tvHashtags2 != null) tvHashtags2.setOnClickListener(hashtagListener);
        if (tvHashtags3 != null) tvHashtags3.setOnClickListener(hashtagListener);

        // Heart / Like Buttons
        if (btnLike1 != null) {
            btnLike1.setOnClickListener(v -> {
                if (currentPost1 == null) return;
                isLiked1 = !isLiked1;
                ImageView img = (ImageView) btnLike1.getChildAt(0);
                TextView txt = (TextView) btnLike1.getChildAt(1);
                String authHeader = ApiClient.getAuthHeader(this);
                if (isLiked1) {
                    currentPost1.likesCount++;
                    img.setImageResource(R.drawable.ic_heart_red);
                    txt.setTextColor(Color.parseColor("#E53935"));
                    ApiClient.getApiService().likePost(authHeader, currentPost1.postId).enqueue(new Callback<Object>() {
                        @Override public void onResponse(Call<Object> c, Response<Object> r) {}
                        @Override public void onFailure(Call<Object> c, Throwable t) {}
                    });
                } else {
                    if (currentPost1.likesCount > 0) currentPost1.likesCount--;
                    img.setImageResource(R.drawable.ic_heart);
                    txt.setTextColor(Color.parseColor("#1E232C"));
                    ApiClient.getApiService().unlikePost(authHeader, currentPost1.postId).enqueue(new Callback<Object>() {
                        @Override public void onResponse(Call<Object> c, Response<Object> r) {}
                        @Override public void onFailure(Call<Object> c, Throwable t) {}
                    });
                }
                txt.setText(currentPost1.likesCount + " Thích");
            });
        }

        if (btnLike2 != null) {
            btnLike2.setOnClickListener(v -> {
                if (currentPost2 == null) return;
                isLiked2 = !isLiked2;
                ImageView img = (ImageView) btnLike2.getChildAt(0);
                TextView txt = (TextView) btnLike2.getChildAt(1);
                String authHeader = ApiClient.getAuthHeader(this);
                if (isLiked2) {
                    currentPost2.likesCount++;
                    img.setImageResource(R.drawable.ic_heart_red);
                    txt.setTextColor(Color.parseColor("#E53935"));
                    ApiClient.getApiService().likePost(authHeader, currentPost2.postId).enqueue(new Callback<Object>() {
                        @Override public void onResponse(Call<Object> c, Response<Object> r) {}
                        @Override public void onFailure(Call<Object> c, Throwable t) {}
                    });
                } else {
                    if (currentPost2.likesCount > 0) currentPost2.likesCount--;
                    img.setImageResource(R.drawable.ic_heart);
                    txt.setTextColor(Color.parseColor("#1E232C"));
                    ApiClient.getApiService().unlikePost(authHeader, currentPost2.postId).enqueue(new Callback<Object>() {
                        @Override public void onResponse(Call<Object> c, Response<Object> r) {}
                        @Override public void onFailure(Call<Object> c, Throwable t) {}
                    });
                }
                txt.setText(currentPost2.likesCount + " Thích");
            });
        }

        if (btnLike3 != null) {
            btnLike3.setOnClickListener(v -> {
                if (currentPost3 == null) return;
                isLiked3 = !isLiked3;
                ImageView img = (ImageView) btnLike3.getChildAt(0);
                TextView txt = (TextView) btnLike3.getChildAt(1);
                String authHeader = ApiClient.getAuthHeader(this);
                if (isLiked3) {
                    currentPost3.likesCount++;
                    img.setImageResource(R.drawable.ic_heart_red);
                    txt.setTextColor(Color.parseColor("#E53935"));
                    ApiClient.getApiService().likePost(authHeader, currentPost3.postId).enqueue(new Callback<Object>() {
                        @Override public void onResponse(Call<Object> c, Response<Object> r) {}
                        @Override public void onFailure(Call<Object> c, Throwable t) {}
                    });
                } else {
                    if (currentPost3.likesCount > 0) currentPost3.likesCount--;
                    img.setImageResource(R.drawable.ic_heart);
                    txt.setTextColor(Color.parseColor("#1E232C"));
                    ApiClient.getApiService().unlikePost(authHeader, currentPost3.postId).enqueue(new Callback<Object>() {
                        @Override public void onResponse(Call<Object> c, Response<Object> r) {}
                        @Override public void onFailure(Call<Object> c, Throwable t) {}
                    });
                }
                txt.setText(currentPost3.likesCount + " Thích");
            });
        }

        // Comments
        if (btnComment1 != null) {
            btnComment1.setOnClickListener(v -> {
                if (currentPost1 != null) {
                    TextView txtComment = (TextView) btnComment1.getChildAt(1);
                    openCommentsBottomSheet(currentPost1, txtComment);
                }
            });
        }
        if (btnComment2 != null) {
            btnComment2.setOnClickListener(v -> {
                if (currentPost2 != null) {
                    TextView txtComment = (TextView) btnComment2.getChildAt(1);
                    openCommentsBottomSheet(currentPost2, txtComment);
                }
            });
        }
        if (btnComment3 != null) {
            btnComment3.setOnClickListener(v -> {
                if (currentPost3 != null) {
                    TextView txtComment = (TextView) btnComment3.getChildAt(1);
                    openCommentsBottomSheet(currentPost3, txtComment);
                }
            });
        }

        // Share
        View.OnClickListener openShare = v -> openShareBottomSheet();
        if (btnShare1 != null) btnShare1.setOnClickListener(openShare);
        if (btnShare2 != null) btnShare2.setOnClickListener(openShare);
        if (btnShare3 != null) btnShare3.setOnClickListener(openShare);

        // Bookmarks: save the post to "Bài viết đã lưu"
        saveButtons[0] = new SavePostButton(this, btnBookmark1);
        saveButtons[1] = new SavePostButton(this, btnBookmark2);
        saveButtons[2] = new SavePostButton(this, btnBookmark3);

        // Options (3 dots)
        ImageView btnOptions1 = findViewById(R.id.btnOptions1);
        if (btnOptions1 != null) {
            btnOptions1.setOnClickListener(v -> openPostOptionsBottomSheet());
        }
        ImageView btnOptions2 = findViewById(R.id.btnOptions2);
        if (btnOptions2 != null) {
            btnOptions2.setOnClickListener(v -> openPostOptionsBottomSheet());
        }
        ImageView btnOptions3 = findViewById(R.id.btnOptions3);
        if (btnOptions3 != null) {
            btnOptions3.setOnClickListener(v -> openPostOptionsBottomSheet());
        }
    }

    private void filterFeed(String query) {
        View card1 = (View) containerAvatar1.getParent().getParent();
        View card2 = (View) containerAvatar2.getParent().getParent();
        View card3 = (View) containerAvatar3.getParent().getParent();

        if (query.isEmpty()) {
            if (card1 != null) card1.setVisibility(View.VISIBLE);
            if (card2 != null) card2.setVisibility(View.VISIBLE);
            if (card3 != null) card3.setVisibility(View.VISIBLE);
            return;
        }

        String text1 = ((tvAuthorName1 != null ? tvAuthorName1.getText() : "") + " "
                + (tvPostText1 != null ? tvPostText1.getText() : "") + " "
                + (tvHashtags1 != null ? tvHashtags1.getText() : "")).toLowerCase();
        String text2 = ((tvAuthorName2 != null ? tvAuthorName2.getText() : "") + " "
                + (tvPostText2 != null ? tvPostText2.getText() : "") + " "
                + (tvHashtags2 != null ? tvHashtags2.getText() : "")).toLowerCase();
        String text3 = ((tvAuthorName3 != null ? tvAuthorName3.getText() : "") + " "
                + (tvPostText3 != null ? tvPostText3.getText() : "") + " "
                + (tvHashtags3 != null ? tvHashtags3.getText() : "")).toLowerCase();

        if (card1 != null) {
            card1.setVisibility(text1.contains(query) ? View.VISIBLE : View.GONE);
        }
        if (card2 != null) {
            card2.setVisibility(text2.contains(query) ? View.VISIBLE : View.GONE);
        }
        if (card3 != null) {
            card3.setVisibility(text3.contains(query) ? View.VISIBLE : View.GONE);
        }
    }

    private void openFullscreenImage(String author, String caption) {
        Dialog dialog = new Dialog(this, android.R.style.Theme_Black_NoTitleBar_Fullscreen);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.layout_dialog_full_image);
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.parseColor("#EE000000")));
            dialog.getWindow().setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.MATCH_PARENT);
            dialog.getWindow().setFlags(
                    WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
                    WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
            );
        }

        TextView tvFullAuthor = dialog.findViewById(R.id.tvFullAuthor);
        TextView tvFullCaption = dialog.findViewById(R.id.tvFullCaption);
        ImageView imgFullPhoto = dialog.findViewById(R.id.imgFullPhoto);
        View btnClose = dialog.findViewById(R.id.btnCloseFullImage);

        if (imgFullPhoto != null) {
            imgFullPhoto.setScaleType(ImageView.ScaleType.FIT_CENTER);
        }

        if (tvFullAuthor != null) tvFullAuthor.setText(author);
        if (tvFullCaption != null) tvFullCaption.setText(caption);
        if (btnClose != null) btnClose.setOnClickListener(v -> dialog.dismiss());

        dialog.show();
    }

    private void loadPostComments(FeedPostsResponse.PostItem post, LinearLayout container, TextView tvTitle, TextView txtCommentCount) {
        if (container == null || post == null) return;
        int postId = post.postId;
        container.removeAllViews();

        TextView tvLoading = new TextView(this);
        tvLoading.setText("Đang tải bình luận...");
        tvLoading.setTextColor(Color.parseColor("#9E8A83"));
        tvLoading.setPadding(0, 20, 0, 20);
        container.addView(tvLoading);

        ApiClient.getApiService().getPostComments(postId).enqueue(new Callback<CommentsListResponse>() {
            @Override
            public void onResponse(Call<CommentsListResponse> call, Response<CommentsListResponse> response) {
                container.removeAllViews();
                if (response.isSuccessful() && response.body() != null && response.body().data != null) {
                    List<CommentsListResponse.CommentItem> comments = response.body().data;
                    post.commentsCount = comments.size();
                    if (tvTitle != null) {
                        tvTitle.setText("Bình luận (" + comments.size() + ")");
                    }
                    if (txtCommentCount != null) {
                        txtCommentCount.setText(comments.size() + " Bình luận");
                    }
                    if (comments.isEmpty()) {
                        TextView tvEmpty = new TextView(HomeActivity.this);
                        tvEmpty.setText("Chưa có bình luận nào. Hãy là người đầu tiên bình luận!");
                        tvEmpty.setTextColor(Color.parseColor("#9E8A83"));
                        tvEmpty.setPadding(0, 30, 0, 30);
                        container.addView(tvEmpty);
                        return;
                    }

                    for (CommentsListResponse.CommentItem comment : comments) {
                        LinearLayout row = new LinearLayout(HomeActivity.this);
                        row.setOrientation(LinearLayout.VERTICAL);
                        row.setPadding(0, 12, 0, 12);

                        RelativeLayout authorRow = new RelativeLayout(HomeActivity.this);

                        TextView tvAuthor = new TextView(HomeActivity.this);
                        String name = (comment.author != null && comment.author.fullName != null && !comment.author.fullName.isEmpty())
                                ? comment.author.fullName : "Người dùng";
                        tvAuthor.setText(name);
                        tvAuthor.setTextSize(14);
                        tvAuthor.setTypeface(null, Typeface.BOLD);
                        tvAuthor.setTextColor(Color.parseColor("#1E232C"));

                        RelativeLayout.LayoutParams lpAuthor = new RelativeLayout.LayoutParams(
                                RelativeLayout.LayoutParams.WRAP_CONTENT,
                                RelativeLayout.LayoutParams.WRAP_CONTENT
                        );
                        lpAuthor.addRule(RelativeLayout.ALIGN_PARENT_START);
                        authorRow.addView(tvAuthor, lpAuthor);

                        TextView tvDelete = new TextView(HomeActivity.this);
                        tvDelete.setText("Xóa");
                        tvDelete.setTextSize(12);
                        tvDelete.setTextColor(Color.parseColor("#E53935"));
                        tvDelete.setPadding(16, 4, 16, 4);
                        tvDelete.setClickable(true);
                        tvDelete.setFocusable(true);

                        RelativeLayout.LayoutParams lpDelete = new RelativeLayout.LayoutParams(
                                RelativeLayout.LayoutParams.WRAP_CONTENT,
                                RelativeLayout.LayoutParams.WRAP_CONTENT
                        );
                        lpDelete.addRule(RelativeLayout.ALIGN_PARENT_END);
                        authorRow.addView(tvDelete, lpDelete);

                        tvDelete.setOnClickListener(v -> {
                            new AlertDialog.Builder(HomeActivity.this)
                                    .setTitle("Xóa bình luận")
                                    .setMessage("Bạn có chắc chắn muốn xóa bình luận này?")
                                    .setPositiveButton("Xóa", (dialog, which) -> {
                                        String authHeader = ApiClient.getAuthHeader(HomeActivity.this);
                                        ApiClient.getApiService().deleteComment(authHeader, comment.commentId)
                                                .enqueue(new Callback<Object>() {
                                                    @Override
                                                    public void onResponse(Call<Object> c, Response<Object> r) {
                                                        if (r.isSuccessful()) {
                                                            Toast.makeText(HomeActivity.this, "Đã xóa bình luận!", Toast.LENGTH_SHORT).show();
                                                            loadPostComments(post, container, tvTitle, txtCommentCount);
                                                        } else {
                                                            Toast.makeText(HomeActivity.this, "Không thể xóa (mã " + r.code() + ")", Toast.LENGTH_SHORT).show();
                                                        }
                                                    }

                                                    @Override
                                                    public void onFailure(Call<Object> c, Throwable t) {
                                                        Toast.makeText(HomeActivity.this, "Lỗi kết nối khi xóa: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                                                    }
                                                });
                                    })
                                    .setNegativeButton("Hủy", null)
                                    .show();
                        });

                        TextView tvContent = new TextView(HomeActivity.this);
                        tvContent.setText(comment.content != null ? comment.content : "");
                        tvContent.setTextSize(13);
                        tvContent.setTextColor(Color.parseColor("#3E2723"));
                        tvContent.setPadding(0, 4, 0, 0);

                        TextView tvTime = new TextView(HomeActivity.this);
                        tvTime.setText(MyPostsActivity.formatTime(comment.createdAt));
                        tvTime.setTextSize(11);
                        tvTime.setTextColor(Color.parseColor("#9E8A83"));
                        tvTime.setPadding(0, 2, 0, 0);

                        row.addView(authorRow);
                        row.addView(tvContent);
                        row.addView(tvTime);

                        container.addView(row);
                    }
                } else {
                    TextView tvErr = new TextView(HomeActivity.this);
                    tvErr.setText("Không thể tải bình luận.");
                    tvErr.setTextColor(Color.parseColor("#E53935"));
                    container.addView(tvErr);
                }
            }

            @Override
            public void onFailure(Call<CommentsListResponse> call, Throwable t) {
                container.removeAllViews();
                TextView tvErr = new TextView(HomeActivity.this);
                tvErr.setText("Lỗi kết nối khi tải bình luận.");
                tvErr.setTextColor(Color.parseColor("#E53935"));
                container.addView(tvErr);
            }
        });
    }

    public static String getInitials(String name) {
        if (name == null || name.trim().isEmpty()) return "U";
        String[] parts = name.trim().split("\\s+");
        if (parts.length == 1) {
            return parts[0].substring(0, Math.min(2, parts[0].length())).toUpperCase();
        } else {
            return (parts[0].substring(0, 1) + parts[parts.length - 1].substring(0, 1)).toUpperCase();
        }
    }

    private void openCommentsBottomSheet(FeedPostsResponse.PostItem post, TextView txtCommentCount) {
        if (post == null) return;

        BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(HomeActivity.this);
        View dialogView = getLayoutInflater().inflate(R.layout.layout_dialog_comments, null);
        bottomSheetDialog.setContentView(dialogView);

        TextView tvCommentsTitle = dialogView.findViewById(R.id.tvCommentsTitle);
        if (tvCommentsTitle != null) {
            tvCommentsTitle.setText("Bình luận (" + post.commentsCount + ")");
        }

        TextView tvCommentInputHeader = dialogView.findViewById(R.id.tvCommentInputHeader);
        if (tvCommentInputHeader != null) {
            TokenManager tokenManager = new TokenManager(HomeActivity.this);
            String currentUserName = tokenManager.getUserName();
            if (currentUserName == null || currentUserName.trim().isEmpty()) {
                SharedPreferences prefs = getSharedPreferences("DishcisionPrefs", MODE_PRIVATE);
                currentUserName = prefs.getString("user_name", "Người dùng");
            }
            if (currentUserName == null || currentUserName.trim().isEmpty()) {
                currentUserName = "Người dùng";
            }
            String initials = getInitials(currentUserName);
            tvCommentInputHeader.setText(initials + "  Bình luận với tên " + currentUserName);

            String authHeader = ApiClient.getAuthHeader(this);
            ApiClient.getApiService().getMyProfile(authHeader).enqueue(new Callback<UserProfileResponse>() {
                @Override
                public void onResponse(Call<UserProfileResponse> call, Response<UserProfileResponse> response) {
                    if (response.isSuccessful() && response.body() != null && response.body().getData() != null) {
                        UserProfileResponse.UserData u = response.body().getData();
                        if (u.fullName != null && !u.fullName.trim().isEmpty()) {
                            String name = u.fullName;
                            String init = getInitials(name);
                            tvCommentInputHeader.setText(init + "  Bình luận với tên " + name);
                        }
                    }
                }

                @Override
                public void onFailure(Call<UserProfileResponse> call, Throwable t) {}
            });
        }

        View btnCloseComments = dialogView.findViewById(R.id.btnCloseComments);
        TextView btnSortComments = dialogView.findViewById(R.id.btnSortComments);
        LinearLayout containerCommentsList = dialogView.findViewById(R.id.containerCommentsList);
        EditText edtCommentInput = dialogView.findViewById(R.id.edtCommentInput);
        View btnSendComment = dialogView.findViewById(R.id.btnSendComment);

        View btnEmojiPicker = dialogView.findViewById(R.id.btnEmojiPicker);
        View btnAttachFileComment = dialogView.findViewById(R.id.btnAttachFileComment);
        LinearLayout layoutEmojiBar = dialogView.findViewById(R.id.layoutEmojiBar);

        TextView emojiHeart = dialogView.findViewById(R.id.emojiHeart);
        TextView emojiLaugh = dialogView.findViewById(R.id.emojiLaugh);
        TextView emojiLove = dialogView.findViewById(R.id.emojiLove);
        TextView emojiFire = dialogView.findViewById(R.id.emojiFire);
        TextView emojiClap = dialogView.findViewById(R.id.emojiClap);

        if (btnCloseComments != null) {
            btnCloseComments.setOnClickListener(v -> bottomSheetDialog.dismiss());
        }

        if (btnSortComments != null) {
            btnSortComments.setOnClickListener(v -> {
                PopupMenu popupMenu = new PopupMenu(HomeActivity.this, btnSortComments);
                popupMenu.getMenu().add("Phù hợp nhất");
                popupMenu.getMenu().add("Mới nhất");
                popupMenu.getMenu().add("Tất cả bình luận");
                popupMenu.setOnMenuItemClickListener(item -> {
                    btnSortComments.setText(item.getTitle() + " ▼");
                    Toast.makeText(HomeActivity.this, "Sắp xếp: " + item.getTitle(), Toast.LENGTH_SHORT).show();
                    return true;
                });
                popupMenu.show();
            });
        }

        if (btnEmojiPicker != null && layoutEmojiBar != null) {
            btnEmojiPicker.setOnClickListener(v -> {
                int vis = layoutEmojiBar.getVisibility();
                layoutEmojiBar.setVisibility(vis == View.VISIBLE ? View.GONE : View.VISIBLE);
            });
        }

        View.OnClickListener appendEmoji = v -> {
            if (v instanceof TextView && edtCommentInput != null) {
                String emoji = ((TextView) v).getText().toString();
                edtCommentInput.append(emoji);
                edtCommentInput.requestFocus();
            }
        };

        if (emojiHeart != null) emojiHeart.setOnClickListener(appendEmoji);
        if (emojiLaugh != null) emojiLaugh.setOnClickListener(appendEmoji);
        if (emojiLove != null) emojiLove.setOnClickListener(appendEmoji);
        if (emojiFire != null) emojiFire.setOnClickListener(appendEmoji);
        if (emojiClap != null) emojiClap.setOnClickListener(appendEmoji);

        if (btnAttachFileComment != null) {
            btnAttachFileComment.setOnClickListener(v ->
                Toast.makeText(HomeActivity.this, "Chọn ảnh/tệp đính kèm bình luận", Toast.LENGTH_SHORT).show()
            );
        }

        if (containerCommentsList != null) {
            loadPostComments(post, containerCommentsList, tvCommentsTitle, txtCommentCount);
        }

        if (btnSendComment != null) {
            btnSendComment.setOnClickListener(v -> {
                String text = edtCommentInput != null ? edtCommentInput.getText().toString().trim() : "";
                if (text.isEmpty()) {
                    Toast.makeText(HomeActivity.this, "Vui lòng nhập nội dung bình luận!", Toast.LENGTH_SHORT).show();
                    return;
                }
                String authHeader = ApiClient.getAuthHeader(this);
                ApiClient.getApiService().createPostComment(authHeader, post.postId, new CreateCommentRequest(text))
                        .enqueue(new Callback<CreateCommentResponse>() {
                            @Override
                            public void onResponse(Call<CreateCommentResponse> call, Response<CreateCommentResponse> response) {
                                if (response.isSuccessful() && response.body() != null && response.body().success) {
                                    if (edtCommentInput != null) edtCommentInput.setText("");
                                    if (layoutEmojiBar != null) layoutEmojiBar.setVisibility(View.GONE);
                                    post.commentsCount++;
                                    if (tvCommentsTitle != null) {
                                        tvCommentsTitle.setText("Bình luận (" + post.commentsCount + ")");
                                    }
                                    if (txtCommentCount != null) {
                                        txtCommentCount.setText(post.commentsCount + " Bình luận");
                                    }
                                    if (containerCommentsList != null) {
                                        loadPostComments(post, containerCommentsList, tvCommentsTitle, txtCommentCount);
                                    }
                                    Toast.makeText(HomeActivity.this, "Đã gửi bình luận!", Toast.LENGTH_SHORT).show();
                                } else {
                                    Toast.makeText(HomeActivity.this, "Gửi bình luận thất bại (mã " + response.code() + ")", Toast.LENGTH_SHORT).show();
                                }
                            }

                            @Override
                            public void onFailure(Call<CreateCommentResponse> call, Throwable t) {
                                Toast.makeText(HomeActivity.this, "Lỗi kết nối khi gửi bình luận: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        });
            });
        }

        bottomSheetDialog.show();
    }

    private void openShareBottomSheet() {
        BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(HomeActivity.this);
        View dialogView = getLayoutInflater().inflate(R.layout.layout_dialog_share_post, null);
        bottomSheetDialog.setContentView(dialogView);

        View btnCloseShare = dialogView.findViewById(R.id.btnCloseShare);
        View btnSendDishcision = dialogView.findViewById(R.id.btnSendDishcision);
        View btnCopyLink = dialogView.findViewById(R.id.btnCopyLink);

        View contactMaya = dialogView.findViewById(R.id.contactMaya);
        View contactFriends = dialogView.findViewById(R.id.contactFriends);
        View contactGroup = dialogView.findViewById(R.id.contactGroup);

        View tickMaya = dialogView.findViewById(R.id.tickMaya);
        View tickFriends = dialogView.findViewById(R.id.tickFriends);
        View tickGroup = dialogView.findViewById(R.id.tickGroup);

        EditText edtSearchShare = dialogView.findViewById(R.id.edtSearchShare);

        if (btnCloseShare != null) {
            btnCloseShare.setOnClickListener(v -> bottomSheetDialog.dismiss());
        }

        if (contactMaya != null && tickMaya != null) {
            contactMaya.setOnClickListener(v -> {
                int currentVis = tickMaya.getVisibility();
                if (currentVis == View.VISIBLE) {
                    tickMaya.setVisibility(View.GONE);
                } else {
                    tickMaya.setVisibility(View.VISIBLE);
                    Toast.makeText(HomeActivity.this, "Đã chọn: Maya Lin", Toast.LENGTH_SHORT).show();
                }
            });
        }

        if (contactFriends != null && tickFriends != null) {
            contactFriends.setOnClickListener(v -> {
                int currentVis = tickFriends.getVisibility();
                if (currentVis == View.VISIBLE) {
                    tickFriends.setVisibility(View.GONE);
                } else {
                    tickFriends.setVisibility(View.VISIBLE);
                    Toast.makeText(HomeActivity.this, "Đã chọn: Linh Đan", Toast.LENGTH_SHORT).show();
                }
            });
        }

        if (contactGroup != null && tickGroup != null) {
            contactGroup.setOnClickListener(v -> {
                int currentVis = tickGroup.getVisibility();
                if (currentVis == View.VISIBLE) {
                    tickGroup.setVisibility(View.GONE);
                } else {
                    tickGroup.setVisibility(View.VISIBLE);
                    Toast.makeText(HomeActivity.this, "Đã chọn: Quang Huy", Toast.LENGTH_SHORT).show();
                }
            });
        }

        if (edtSearchShare != null) {
            edtSearchShare.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    String query = s.toString().toLowerCase().trim();
                    if (contactMaya != null) {
                        contactMaya.setVisibility("maya lin".contains(query) || query.isEmpty() ? View.VISIBLE : View.GONE);
                    }
                    if (contactFriends != null) {
                        contactFriends.setVisibility("linh đan".contains(query) || query.isEmpty() ? View.VISIBLE : View.GONE);
                    }
                    if (contactGroup != null) {
                        contactGroup.setVisibility("quang huy".contains(query) || query.isEmpty() ? View.VISIBLE : View.GONE);
                    }
                }

                @Override
                public void afterTextChanged(Editable s) {}
            });
        }

        if (btnSendDishcision != null) {
            btnSendDishcision.setOnClickListener(v -> {
                Toast.makeText(HomeActivity.this, "Đã chia sẻ bài viết!", Toast.LENGTH_SHORT).show();
                bottomSheetDialog.dismiss();
            });
        }

        if (btnCopyLink != null) {
            btnCopyLink.setOnClickListener(v -> {
                Toast.makeText(HomeActivity.this, "Đã sao chép liên kết bài viết!", Toast.LENGTH_SHORT).show();
                bottomSheetDialog.dismiss();
            });
        }

        bottomSheetDialog.show();
    }

    private void openPostOptionsBottomSheet() {
        View dialogView = getLayoutInflater().inflate(R.layout.layout_dialog_post_options, null);
        BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(HomeActivity.this);
        bottomSheetDialog.setContentView(dialogView);

        View btnClose = dialogView.findViewById(R.id.btnCloseOptions);
        View layoutCopyLink = dialogView.findViewById(R.id.layoutCopyLink);
        View layoutSharePost = dialogView.findViewById(R.id.layoutSharePost);
        View btnReportPost = dialogView.findViewById(R.id.btnReportPost);
        View layoutEditPost = dialogView.findViewById(R.id.layoutEditPost);
        View layoutArchivePost = dialogView.findViewById(R.id.layoutArchivePost);
        View layoutDeletePost = dialogView.findViewById(R.id.layoutDeletePost);
        View dividerDelete = dialogView.findViewById(R.id.dividerDelete);

        if (layoutEditPost != null) layoutEditPost.setVisibility(View.GONE);
        if (layoutArchivePost != null) layoutArchivePost.setVisibility(View.GONE);
        if (layoutDeletePost != null) layoutDeletePost.setVisibility(View.GONE);
        if (dividerDelete != null) dividerDelete.setVisibility(View.GONE);
        if (btnReportPost != null) btnReportPost.setVisibility(View.VISIBLE);

        if (btnClose != null) {
            btnClose.setOnClickListener(v -> bottomSheetDialog.dismiss());
        }

        if (layoutCopyLink != null) {
            layoutCopyLink.setOnClickListener(v -> {
                bottomSheetDialog.dismiss();
                ClipboardManager clipboard = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
                ClipData clip = ClipData.newPlainText("Post Link", "https://dishcision.app/posts/feed");
                if (clipboard != null) clipboard.setPrimaryClip(clip);
                Toast.makeText(this, "Đã sao chép liên kết bài viết", Toast.LENGTH_SHORT).show();
            });
        }

        if (layoutSharePost != null) {
            layoutSharePost.setOnClickListener(v -> {
                bottomSheetDialog.dismiss();
                openShareBottomSheet();
            });
        }

        if (btnReportPost != null) {
            btnReportPost.setOnClickListener(v -> {
                bottomSheetDialog.dismiss();
                Intent intent = new Intent(HomeActivity.this, ReportContentActivity.class);
                startActivity(intent);
            });
        }

        bottomSheetDialog.show();
    }

    private void fetchFeedFromBackend() {
        String authHeader = ApiClient.getAuthHeader(this);
        ApiClient.getApiService().getFeedPosts(authHeader).enqueue(new Callback<FeedPostsResponse>() {
            @Override
            public void onResponse(Call<FeedPostsResponse> call, Response<FeedPostsResponse> response) {
                if (response.isSuccessful() && response.body() != null && response.body().getData() != null) {
                    List<FeedPostsResponse.PostItem> posts = response.body().getData();
                    bindFeedData(posts);
                }
            }

            @Override
            public void onFailure(Call<FeedPostsResponse> call, Throwable t) {
                // Fallback gracefully to default pre-rendered mock content
            }
        });

        // Check if signed in via Google
        SharedPreferences prefs = getSharedPreferences("DishcisionPrefs", MODE_PRIVATE);
        String googleName = prefs.getString("user_name", null);
        String googleEmail = prefs.getString("user_email", null);
        if (googleName != null && tvSidebarUserName != null) {
            tvSidebarUserName.setText(googleName);
        }
        if (googleEmail != null && tvSidebarUserHandle != null) {
            tvSidebarUserHandle.setText(googleEmail);
        }

        // Fetch User Profile for Sidebar
        ApiClient.getApiService().getMyProfile(authHeader).enqueue(new Callback<UserProfileResponse>() {
            @Override
            public void onResponse(Call<UserProfileResponse> call, Response<UserProfileResponse> response) {
                if (googleName != null) return; // Keep Google user name if logged in with Google
                if (response.isSuccessful() && response.body() != null && response.body().getData() != null) {
                    UserProfileResponse.UserData user = response.body().getData();
                    if (tvSidebarUserName != null && user.fullName != null) {
                        tvSidebarUserName.setText(user.fullName);
                    }
                    if (tvSidebarUserHandle != null && user.username != null) {
                        tvSidebarUserHandle.setText(user.username.startsWith("@") ? user.username : "@" + user.username);
                    }
                }
            }

            @Override
            public void onFailure(Call<UserProfileResponse> call, Throwable t) {}
        });
    }

    private void bindFeedData(List<FeedPostsResponse.PostItem> posts) {
        View card1 = containerAvatar1 != null && containerAvatar1.getParent() != null && containerAvatar1.getParent().getParent() instanceof View ? (View) containerAvatar1.getParent().getParent() : null;
        View card2 = containerAvatar2 != null && containerAvatar2.getParent() != null && containerAvatar2.getParent().getParent() instanceof View ? (View) containerAvatar2.getParent().getParent() : null;
        View card3 = containerAvatar3 != null && containerAvatar3.getParent() != null && containerAvatar3.getParent().getParent() instanceof View ? (View) containerAvatar3.getParent().getParent() : null;

        if (posts == null || posts.isEmpty()) {
            currentPost1 = null;
            currentPost2 = null;
            currentPost3 = null;
            if (card1 != null) card1.setVisibility(View.GONE);
            if (card2 != null) card2.setVisibility(View.GONE);
            if (card3 != null) card3.setVisibility(View.GONE);
            return;
        }

        currentPost1 = posts.size() > 0 ? posts.get(0) : null;
        currentPost2 = posts.size() > 1 ? posts.get(1) : null;
        currentPost3 = posts.size() > 2 ? posts.get(2) : null;

        for (int i = 0; i < saveButtons.length && i < posts.size(); i++) {
            FeedPostsResponse.PostItem p = posts.get(i);
            if (saveButtons[i] != null) {
                saveButtons[i].bind(p.postId, p.isSaved, p.author != null ? p.author.fullName : null);
            }
        }

        // Bind Card 1
        if (posts.size() > 0) {
            if (card1 != null) card1.setVisibility(View.VISIBLE);
            FeedPostsResponse.PostItem p1 = posts.get(0);
            if (p1.author != null && tvAuthorName1 != null && p1.author.fullName != null) {
                tvAuthorName1.setText(p1.author.fullName);
            }
            if (p1.author != null && tvUsername1 != null && p1.author.username != null) {
                tvUsername1.setText(p1.author.username.startsWith("@") ? p1.author.username : "@" + p1.author.username);
            }
            if (p1.author != null && p1.author.avatarUrl != null && !p1.author.avatarUrl.isEmpty() && containerAvatar1 != null && containerAvatar1.getChildCount() > 0) {
                ImageLoader.loadAvatar((ImageView) containerAvatar1.getChildAt(0), p1.author.avatarUrl);
            }
            if (tvLocation1 != null) {
                String loc = (p1.locationUrl != null && !p1.locationUrl.isEmpty()) ? p1.locationUrl : "Dishcision";
                String timeStr = MyPostsActivity.formatTime(p1.createdAt);
                tvLocation1.setText(loc + " • " + timeStr);
            }
            if (tvPostText1 != null && p1.content != null) {
                tvPostText1.setText(p1.content);
            }
            if (tvHashtags1 != null) {
                if (p1.hashtags != null && !p1.hashtags.isEmpty()) {
                    StringBuilder sb = new StringBuilder();
                    for (String tag : p1.hashtags) {
                        sb.append("#").append(tag).append(" ");
                    }
                    tvHashtags1.setText(sb.toString().trim());
                    tvHashtags1.setVisibility(View.VISIBLE);
                } else {
                    tvHashtags1.setVisibility(View.GONE);
                }
            }
            if (containerPostImage1 != null && containerPostImage1.getChildCount() > 0) {
                if (p1.images != null && !p1.images.isEmpty() && p1.images.get(0).imageUrl != null) {
                    ImageLoader.loadImage((ImageView) containerPostImage1.getChildAt(0), p1.images.get(0).imageUrl);
                    containerPostImage1.setVisibility(View.VISIBLE);
                } else {
                    containerPostImage1.setVisibility(View.GONE);
                }
            }
            if (btnLike1 != null) {
                TextView txtLike = (TextView) btnLike1.getChildAt(1);
                if (txtLike != null) txtLike.setText(p1.likesCount + " Thích");
            }
            if (btnComment1 != null) {
                TextView txtComment = (TextView) btnComment1.getChildAt(1);
                if (txtComment != null) txtComment.setText(p1.commentsCount + " Bình luận");
            }
        } else if (card1 != null) {
            card1.setVisibility(View.GONE);
        }

        // Bind Card 2
        if (posts.size() > 1) {
            if (card2 != null) card2.setVisibility(View.VISIBLE);
            FeedPostsResponse.PostItem p2 = posts.get(1);
            if (p2.author != null && tvAuthorName2 != null && p2.author.fullName != null) {
                tvAuthorName2.setText(p2.author.fullName);
            }
            if (p2.author != null && p2.author.avatarUrl != null && !p2.author.avatarUrl.isEmpty() && containerAvatar2 != null && containerAvatar2.getChildCount() > 0) {
                ImageLoader.loadAvatar((ImageView) containerAvatar2.getChildAt(0), p2.author.avatarUrl);
            }
            if (tvLocation2 != null) {
                String loc = (p2.locationUrl != null && !p2.locationUrl.isEmpty()) ? p2.locationUrl : "Dishcision";
                String timeStr = MyPostsActivity.formatTime(p2.createdAt);
                tvLocation2.setText(loc + " • " + timeStr);
            }
            if (tvPostText2 != null && p2.content != null) {
                tvPostText2.setText(p2.content);
            }
            if (tvHashtags2 != null) {
                if (p2.hashtags != null && !p2.hashtags.isEmpty()) {
                    StringBuilder sb = new StringBuilder();
                    for (String tag : p2.hashtags) {
                        sb.append("#").append(tag).append(" ");
                    }
                    tvHashtags2.setText(sb.toString().trim());
                    tvHashtags2.setVisibility(View.VISIBLE);
                } else {
                    tvHashtags2.setVisibility(View.GONE);
                }
            }
            if (containerPostImage2 != null && containerPostImage2.getChildCount() > 0) {
                if (p2.images != null && !p2.images.isEmpty() && p2.images.get(0).imageUrl != null) {
                    ImageLoader.loadImage((ImageView) containerPostImage2.getChildAt(0), p2.images.get(0).imageUrl);
                    containerPostImage2.setVisibility(View.VISIBLE);
                } else {
                    containerPostImage2.setVisibility(View.GONE);
                }
            }
            if (btnLike2 != null) {
                TextView txtLike = (TextView) btnLike2.getChildAt(1);
                if (txtLike != null) txtLike.setText(p2.likesCount + " Thích");
            }
            if (btnComment2 != null) {
                TextView txtComment = (TextView) btnComment2.getChildAt(1);
                if (txtComment != null) txtComment.setText(p2.commentsCount + " Bình luận");
            }
        } else if (card2 != null) {
            card2.setVisibility(View.GONE);
        }

        // Bind Card 3
        if (posts.size() > 2) {
            if (card3 != null) card3.setVisibility(View.VISIBLE);
            FeedPostsResponse.PostItem p3 = posts.get(2);
            if (p3.author != null && tvAuthorName3 != null && p3.author.fullName != null) {
                tvAuthorName3.setText(p3.author.fullName);
            }
            if (p3.author != null && tvUsername3 != null && p3.author.username != null) {
                tvUsername3.setText(p3.author.username.startsWith("@") ? p3.author.username : "@" + p3.author.username);
            }
            if (p3.author != null && p3.author.avatarUrl != null && !p3.author.avatarUrl.isEmpty() && containerAvatar3 != null && containerAvatar3.getChildCount() > 0) {
                ImageLoader.loadAvatar((ImageView) containerAvatar3.getChildAt(0), p3.author.avatarUrl);
            }
            if (tvLocation3 != null) {
                String loc = (p3.locationUrl != null && !p3.locationUrl.isEmpty()) ? p3.locationUrl : "Dishcision";
                String timeStr = MyPostsActivity.formatTime(p3.createdAt);
                tvLocation3.setText(loc + " • " + timeStr);
            }
            if (tvPostText3 != null && p3.content != null) {
                tvPostText3.setText(p3.content);
            }
            if (tvHashtags3 != null) {
                if (p3.hashtags != null && !p3.hashtags.isEmpty()) {
                    StringBuilder sb = new StringBuilder();
                    for (String tag : p3.hashtags) {
                        sb.append("#").append(tag).append(" ");
                    }
                    tvHashtags3.setText(sb.toString().trim());
                    tvHashtags3.setVisibility(View.VISIBLE);
                } else {
                    tvHashtags3.setVisibility(View.GONE);
                }
            }
            if (containerPostImage3 != null && containerPostImage3.getChildCount() > 0) {
                if (p3.images != null && !p3.images.isEmpty() && p3.images.get(0).imageUrl != null) {
                    ImageLoader.loadImage((ImageView) containerPostImage3.getChildAt(0), p3.images.get(0).imageUrl);
                    containerPostImage3.setVisibility(View.VISIBLE);
                } else {
                    containerPostImage3.setVisibility(View.GONE);
                }
            }
            if (btnLike3 != null) {
                TextView txtLike = (TextView) btnLike3.getChildAt(1);
                if (txtLike != null) txtLike.setText(p3.likesCount + " Thích");
            }
            if (btnComment3 != null) {
                TextView txtComment = (TextView) btnComment3.getChildAt(1);
                if (txtComment != null) txtComment.setText(p3.commentsCount + " Bình luận");
            }
        } else if (card3 != null) {
            card3.setVisibility(View.GONE);
        }
    }
}
