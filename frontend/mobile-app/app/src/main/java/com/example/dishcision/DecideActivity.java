package com.example.dishcision;

import android.content.Intent;
import android.os.Bundle;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class DecideActivity extends AppCompatActivity {

    private RelativeLayout btnSpinCenter;
    private LinearLayout navHome;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_decide);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main_decide), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        initViews();
        setupListeners();
    }

    private void initViews() {
        btnSpinCenter = findViewById(R.id.btnSpinCenter);
        navHome = findViewById(R.id.navHome);
    }

    private void setupListeners() {
        if (btnSpinCenter != null) {
            btnSpinCenter.setOnClickListener(v -> {
                Intent intent = new Intent(DecideActivity.this, TodayRestaurantResultActivity.class);
                intent.putExtra(TodayRestaurantResultActivity.EXTRA_DISH_NAME, "Pizza Ý");
                startActivity(intent);
            });
        }

        if (navHome != null) {
            navHome.setOnClickListener(v -> {
                Intent intent = new Intent(DecideActivity.this, HomeActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                startActivity(intent);
                finish();
            });
        }
    }
}
