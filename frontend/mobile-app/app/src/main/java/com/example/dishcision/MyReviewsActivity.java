package com.example.dishcision;

import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.PopupMenu;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class MyReviewsActivity extends AppCompatActivity {

    private ImageView btnBack;
    private LinearLayout navHome;
    private LinearLayout btnSort;
    private TextView tvSortSelected;

    private TextView chipAll;
    private TextView chipFiveStar;
    private TextView chipFourStar;
    private TextView chipThreeStar;
    private TextView chipOneStar;

    private TextView currentActiveChip;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_my_reviews);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main_my_reviews), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        initViews();
        setupListeners();
    }

    private void initViews() {
        btnBack = findViewById(R.id.btnBack);
        navHome = findViewById(R.id.navHome);
        btnSort = findViewById(R.id.btnSort);
        tvSortSelected = findViewById(R.id.tvSortSelected);

        chipAll = findViewById(R.id.chipAll);
        chipFiveStar = findViewById(R.id.chipFiveStar);
        chipFourStar = findViewById(R.id.chipFourStar);
        chipThreeStar = findViewById(R.id.chipThreeStar);
        chipOneStar = findViewById(R.id.chipOneStar);

        currentActiveChip = chipAll;
    }

    private void setupListeners() {
        if (btnBack != null) {
            btnBack.setOnClickListener(v -> finish());
        }

        if (navHome != null) {
            navHome.setOnClickListener(v -> {
                Intent intent = new Intent(MyReviewsActivity.this, HomeActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                startActivity(intent);
                finish();
            });
        }

        if (btnSort != null) {
            btnSort.setOnClickListener(v -> showSortDropdownMenu());
        }

        if (chipAll != null) chipAll.setOnClickListener(v -> setActiveChip(chipAll));
        if (chipFiveStar != null) chipFiveStar.setOnClickListener(v -> setActiveChip(chipFiveStar));
        if (chipFourStar != null) chipFourStar.setOnClickListener(v -> setActiveChip(chipFourStar));
        if (chipThreeStar != null) chipThreeStar.setOnClickListener(v -> setActiveChip(chipThreeStar));
        if (chipOneStar != null) chipOneStar.setOnClickListener(v -> setActiveChip(chipOneStar));
    }

    private void showSortDropdownMenu() {
        if (btnSort == null) return;

        PopupMenu popupMenu = new PopupMenu(MyReviewsActivity.this, btnSort);
        popupMenu.getMenu().add("Mới nhất");
        popupMenu.getMenu().add("Cũ nhất");
        popupMenu.getMenu().add("Đánh giá cao nhất");
        popupMenu.getMenu().add("Đánh giá thấp nhất");

        popupMenu.setOnMenuItemClickListener((MenuItem item) -> {
            if (tvSortSelected != null) {
                tvSortSelected.setText(item.getTitle());
            }
            return true;
        });

        popupMenu.show();
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
