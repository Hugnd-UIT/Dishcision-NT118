package com.example.dishcision;

import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class FavoriteDishesActivity extends AppCompatActivity {

    private ImageView btnBack;
    private ImageView btnNotification;

    private LinearLayout navHome;
    private LinearLayout navDecide;
    private LinearLayout navCreate;
    private LinearLayout navMessages;
    private LinearLayout navProfile;

    private TextView chipAll;
    private TextView chipNoodle;
    private TextView chipRice;
    private TextView chipSnack;
    private TextView chipDessert;

    private TextView currentActiveChip;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_favorite_dishes);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main_favorite_dishes), (v, insets) -> {
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

        chipAll = findViewById(R.id.chipAll);
        chipNoodle = findViewById(R.id.chipNoodle);
        chipRice = findViewById(R.id.chipRice);
        chipSnack = findViewById(R.id.chipSnack);
        chipDessert = findViewById(R.id.chipDessert);

        currentActiveChip = chipAll;
    }

    private void setupListeners() {
        if (btnBack != null) {
            btnBack.setOnClickListener(v -> finish());
        }

        if (btnNotification != null) {
            btnNotification.setOnClickListener(v -> {
                Intent intent = new Intent(FavoriteDishesActivity.this, NotificationsActivity.class);
                startActivity(intent);
            });
        }

        if (navHome != null) {
            navHome.setOnClickListener(v -> {
                Intent intent = new Intent(FavoriteDishesActivity.this, HomeActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                startActivity(intent);
                finish();
            });
        }

        if (navDecide != null) {
            navDecide.setOnClickListener(v -> {
                Intent intent = new Intent(FavoriteDishesActivity.this, DecideActivity.class);
                startActivity(intent);
            });
        }

        if (navCreate != null) {
            navCreate.setOnClickListener(v -> {
                Intent intent = new Intent(FavoriteDishesActivity.this, CreatePostActivity.class);
                startActivity(intent);
            });
        }

        if (navMessages != null) {
            navMessages.setOnClickListener(v -> {
                Intent intent = new Intent(FavoriteDishesActivity.this, MessagesInboxActivity.class);
                startActivity(intent);
            });
        }

        if (navProfile != null) {
            navProfile.setOnClickListener(v -> {
                Intent intent = new Intent(FavoriteDishesActivity.this, ProfileActivity.class);
                startActivity(intent);
            });
        }

        if (chipAll != null) chipAll.setOnClickListener(v -> setActiveChip(chipAll));
        if (chipNoodle != null) chipNoodle.setOnClickListener(v -> setActiveChip(chipNoodle));
        if (chipRice != null) chipRice.setOnClickListener(v -> setActiveChip(chipRice));
        if (chipSnack != null) chipSnack.setOnClickListener(v -> setActiveChip(chipSnack));
        if (chipDessert != null) chipDessert.setOnClickListener(v -> setActiveChip(chipDessert));
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
