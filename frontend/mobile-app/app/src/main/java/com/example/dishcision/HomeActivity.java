package com.example.dishcision;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.GravityCompat;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.google.android.material.bottomsheet.BottomSheetDialog;

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
    private LinearLayout menuFavorites;
    private LinearLayout menuFollowing;
    private LinearLayout menuReviews;
    private LinearLayout menuLogout;

    // Feed Post Interactive Elements
    private RelativeLayout containerAvatar1;
    private RelativeLayout containerAvatar2;
    private RelativeLayout containerAvatar3;

    private LinearLayout btnComment1;
    private LinearLayout btnComment2;
    private LinearLayout btnComment3;

    private ImageView btnShare1;
    private ImageView btnShare2;
    private ImageView btnShare3;

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

        menuFavorites = findViewById(R.id.menuFavorites);
        menuFollowing = findViewById(R.id.menuFollowing);
        menuReviews = findViewById(R.id.menuReviews);
        menuLogout = findViewById(R.id.menuLogout);

        containerAvatar1 = findViewById(R.id.containerAvatar1);
        containerAvatar2 = findViewById(R.id.containerAvatar2);
        containerAvatar3 = findViewById(R.id.containerAvatar3);

        btnComment1 = findViewById(R.id.btnComment1);
        btnComment2 = findViewById(R.id.btnComment2);
        btnComment3 = findViewById(R.id.btnComment3);

        btnShare1 = findViewById(R.id.btnShare1);
        btnShare2 = findViewById(R.id.btnShare2);
        btnShare3 = findViewById(R.id.btnShare3);
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

        // Search Bar
        View.OnClickListener openSearchListener = v -> {
            Intent intent = new Intent(HomeActivity.this, SearchActivity.class);
            startActivity(intent);
        };
        if (edtSearch != null) edtSearch.setOnClickListener(openSearchListener);
        if (imgSearchIcon != null) imgSearchIcon.setOnClickListener(openSearchListener);
        if (btnFilter != null) btnFilter.setOnClickListener(openSearchListener);

        // Bottom Navigation Bar
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

        if (menuLogout != null) {
            menuLogout.setOnClickListener(v -> {
                Intent intent = new Intent(HomeActivity.this, LoginActivity.class);
                startActivity(intent);
                finish();
            });
        }

        // User Avatars in Feed
        View.OnClickListener openUserProfile = v -> {
            Intent intent = new Intent(HomeActivity.this, UserProfileActivity.class);
            startActivity(intent);
        };
        if (containerAvatar1 != null) containerAvatar1.setOnClickListener(openUserProfile);
        if (containerAvatar2 != null) containerAvatar2.setOnClickListener(openUserProfile);
        if (containerAvatar3 != null) containerAvatar3.setOnClickListener(openUserProfile);

        // Comments
        View.OnClickListener openComments = v -> openCommentsBottomSheet();
        if (btnComment1 != null) btnComment1.setOnClickListener(openComments);
        if (btnComment2 != null) btnComment2.setOnClickListener(openComments);
        if (btnComment3 != null) btnComment3.setOnClickListener(openComments);

        // Share
        View.OnClickListener openShare = v -> openShareBottomSheet();
        if (btnShare1 != null) btnShare1.setOnClickListener(openShare);
        if (btnShare2 != null) btnShare2.setOnClickListener(openShare);
        if (btnShare3 != null) btnShare3.setOnClickListener(openShare);
    }

    private void openCommentsBottomSheet() {
        BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(HomeActivity.this);
        View dialogView = getLayoutInflater().inflate(R.layout.layout_dialog_comments, null);
        bottomSheetDialog.setContentView(dialogView);

        View btnCloseComments = dialogView.findViewById(R.id.btnCloseComments);
        if (btnCloseComments != null) {
            btnCloseComments.setOnClickListener(v -> bottomSheetDialog.dismiss());
        }

        bottomSheetDialog.show();
    }

    private void openShareBottomSheet() {
        BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(HomeActivity.this);
        View dialogView = getLayoutInflater().inflate(R.layout.layout_dialog_share_post, null);
        bottomSheetDialog.setContentView(dialogView);

        View btnCloseShare = dialogView.findViewById(R.id.btnCloseShare);
        if (btnCloseShare != null) {
            btnCloseShare.setOnClickListener(v -> bottomSheetDialog.dismiss());
        }

        bottomSheetDialog.show();
    }
}
