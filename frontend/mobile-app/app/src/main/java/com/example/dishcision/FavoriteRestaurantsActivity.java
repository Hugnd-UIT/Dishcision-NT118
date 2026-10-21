package com.example.dishcision;

import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.bottomsheet.BottomSheetDialog;

public class FavoriteRestaurantsActivity extends AppCompatActivity {

    private ImageView btnBack;
    private ImageView btnNotification;

    private LinearLayout navHome;
    private LinearLayout navDecide;
    private LinearLayout navCreate;
    private LinearLayout navMessages;
    private LinearLayout navProfile;

    private LinearLayout btnViewMenuPhoGiaTruyen;
    private LinearLayout btnViewMenuComTam;
    private LinearLayout btnViewMenuTocoToco;

    private LinearLayout storyBepLucNam;
    private LinearLayout storyPhoThin;
    private LinearLayout storyTraMuaThu;
    private LinearLayout storyPizzaLoCui;

    private TextView chipAll;
    private TextView chipOpen;
    private TextView chipNearMe;
    private TextView currentActiveChip;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_favorite_restaurants);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main_favorite_restaurants), (v, insets) -> {
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

        navHome = findViewById(R.id.navHome);
        navDecide = findViewById(R.id.navDecide);
        navCreate = findViewById(R.id.navCreate);
        navMessages = findViewById(R.id.navMessages);
        navProfile = findViewById(R.id.navProfile);

        btnViewMenuPhoGiaTruyen = findViewById(R.id.btnViewMenuPhoGiaTruyen);
        btnViewMenuComTam = findViewById(R.id.btnViewMenuComTam);
        btnViewMenuTocoToco = findViewById(R.id.btnViewMenuTocoToco);

        storyBepLucNam = findViewById(R.id.storyBepLucNam);
        storyPhoThin = findViewById(R.id.storyPhoThin);
        storyTraMuaThu = findViewById(R.id.storyTraMuaThu);
        storyPizzaLoCui = findViewById(R.id.storyPizzaLoCui);

        chipAll = findViewById(R.id.chipAll);
        chipOpen = findViewById(R.id.chipOpen);
        chipNearMe = findViewById(R.id.chipNearMe);
        currentActiveChip = chipAll;
    }

    private void setupListeners() {
        if (btnBack != null) {
            btnBack.setOnClickListener(v -> finish());
        }

        if (btnNotification != null) {
            btnNotification.setOnClickListener(v -> {
                Intent intent = new Intent(FavoriteRestaurantsActivity.this, NotificationsActivity.class);
                startActivity(intent);
            });
        }

        if (navHome != null) {
            navHome.setOnClickListener(v -> {
                Intent intent = new Intent(FavoriteRestaurantsActivity.this, HomeActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                startActivity(intent);
                finish();
            });
        }

        if (navDecide != null) {
            navDecide.setOnClickListener(v -> {
                Intent intent = new Intent(FavoriteRestaurantsActivity.this, DecideActivity.class);
                startActivity(intent);
            });
        }

        if (navCreate != null) {
            navCreate.setOnClickListener(v -> {
                Intent intent = new Intent(FavoriteRestaurantsActivity.this, CreatePostActivity.class);
                startActivity(intent);
            });
        }

        if (navMessages != null) {
            navMessages.setOnClickListener(v -> {
                Intent intent = new Intent(FavoriteRestaurantsActivity.this, MessagesInboxActivity.class);
                startActivity(intent);
            });
        }

        if (navProfile != null) {
            navProfile.setOnClickListener(v -> {
                Intent intent = new Intent(FavoriteRestaurantsActivity.this, ProfileActivity.class);
                startActivity(intent);
            });
        }

        if (btnViewMenuPhoGiaTruyen != null) {
            btnViewMenuPhoGiaTruyen.setOnClickListener(v -> openMenuBottomSheet("Menu Quán Phở Bò Gia Truyền 1986"));
        }
        if (btnViewMenuComTam != null) {
            btnViewMenuComTam.setOnClickListener(v -> openMenuBottomSheet("Menu Cơm Tấm Ba Nghiền"));
        }
        if (btnViewMenuTocoToco != null) {
            btnViewMenuTocoToco.setOnClickListener(v -> openMenuBottomSheet("Menu Trà Sữa TocoToco - Cầu Giấy"));
        }

        if (storyBepLucNam != null) storyBepLucNam.setOnClickListener(v -> openStoryViewer("Bếp Lục Nam"));
        if (storyPhoThin != null) storyPhoThin.setOnClickListener(v -> openStoryViewer("Phở Thìn"));
        if (storyTraMuaThu != null) storyTraMuaThu.setOnClickListener(v -> openStoryViewer("Trà Mùa Thu"));
        if (storyPizzaLoCui != null) storyPizzaLoCui.setOnClickListener(v -> openStoryViewer("Pizza Lò Củi"));

        if (chipAll != null) chipAll.setOnClickListener(v -> setActiveChip(chipAll));
        if (chipOpen != null) chipOpen.setOnClickListener(v -> setActiveChip(chipOpen));
        if (chipNearMe != null) chipNearMe.setOnClickListener(v -> setActiveChip(chipNearMe));
    }

    private void openMenuBottomSheet(String storeName) {
        BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(FavoriteRestaurantsActivity.this);
        View dialogView = getLayoutInflater().inflate(R.layout.layout_dialog_menu_bottomsheet, null);
        bottomSheetDialog.setContentView(dialogView);

        TextView tvMenuStoreName = dialogView.findViewById(R.id.tvMenuStoreName);
        RelativeLayout btnCloseMenu = dialogView.findViewById(R.id.btnCloseMenu);

        if (tvMenuStoreName != null && storeName != null) {
            tvMenuStoreName.setText(storeName);
        }

        if (btnCloseMenu != null) {
            btnCloseMenu.setOnClickListener(v -> bottomSheetDialog.dismiss());
        }

        bottomSheetDialog.show();
    }

    private void openStoryViewer(String storeName) {
        Intent intent = new Intent(FavoriteRestaurantsActivity.this, StoryActivity.class);
        intent.putExtra(StoryActivity.EXTRA_STORE_NAME, storeName);
        startActivity(intent);
    }

    private void setActiveChip(TextView selectedChip) {
        if (selectedChip == null || selectedChip == currentActiveChip) return;

        if (currentActiveChip != null) {
            currentActiveChip.setBackgroundResource(R.drawable.bg_chip_inactive);
            currentActiveChip.setTextColor(ContextCompat.getColor(this, R.color.text_dark));
            currentActiveChip.setTypeface(null, Typeface.NORMAL);
        }

        selectedChip.setBackgroundResource(R.drawable.bg_chip_active);
        selectedChip.setTextColor(ContextCompat.getColor(this, R.color.white));
        selectedChip.setTypeface(null, Typeface.BOLD);

        currentActiveChip = selectedChip;
    }
}
