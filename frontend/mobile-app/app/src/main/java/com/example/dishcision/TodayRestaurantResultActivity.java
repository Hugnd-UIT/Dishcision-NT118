package com.example.dishcision;

import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class TodayRestaurantResultActivity extends AppCompatActivity {

    public static final String EXTRA_DISH_NAME = "extra_dish_name";

    private ImageView btnBack;
    private TextView tvSelectedDishName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_today_restaurant_result);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main_today_restaurant_result), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        initViews();
        setupData();
        setupListeners();
    }

    private void initViews() {
        btnBack = findViewById(R.id.btnBack);
        tvSelectedDishName = findViewById(R.id.tvSelectedDishName);
    }

    private void setupData() {
        if (getIntent() != null && getIntent().hasExtra(EXTRA_DISH_NAME)) {
            String dishName = getIntent().getStringExtra(EXTRA_DISH_NAME);
            if (dishName != null && !dishName.isEmpty() && tvSelectedDishName != null) {
                tvSelectedDishName.setText(dishName);
            }
        }
    }

    private void setupListeners() {
        if (btnBack != null) {
            btnBack.setOnClickListener(v -> finish());
        }
    }
}
