package com.example.dishcision;

import android.content.Intent;
import android.os.Bundle;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class AccountMenuActivity extends AppCompatActivity {

    private LinearLayout menuForYou;
    private LinearLayout menuFavorites;
    private LinearLayout menuFollowing;
    private LinearLayout menuReviews;
    private LinearLayout menuLogout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_account_menu);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(android.R.id.content), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        initViews();
        setupListeners();
    }

    private void initViews() {
        menuForYou = findViewById(R.id.menuForYou);
        menuFavorites = findViewById(R.id.menuFavorites);
        menuFollowing = findViewById(R.id.menuFollowing);
        menuReviews = findViewById(R.id.menuReviews);
        menuLogout = findViewById(R.id.menuLogout);
    }

    private void setupListeners() {
        if (menuForYou != null) {
            menuForYou.setOnClickListener(v -> {
                Intent intent = new Intent(AccountMenuActivity.this, HomeActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                startActivity(intent);
                finish();
            });
        }

        if (menuFavorites != null) {
            menuFavorites.setOnClickListener(v -> {
                Intent intent = new Intent(AccountMenuActivity.this, FavoriteDishesActivity.class);
                startActivity(intent);
            });
        }

        if (menuFollowing != null) {
            menuFollowing.setOnClickListener(v -> {
                Intent intent = new Intent(AccountMenuActivity.this, FavoriteRestaurantsActivity.class);
                startActivity(intent);
            });
        }

        if (menuReviews != null) {
            menuReviews.setOnClickListener(v -> {
                Intent intent = new Intent(AccountMenuActivity.this, MyReviewsActivity.class);
                startActivity(intent);
            });
        }

        if (menuLogout != null) {
            menuLogout.setOnClickListener(v -> {
                Toast.makeText(this, "Đã đăng xuất tài khoản", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(AccountMenuActivity.this, LoginActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                finish();
            });
        }
    }
}
