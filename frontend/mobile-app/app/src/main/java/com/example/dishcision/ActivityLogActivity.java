package com.example.dishcision;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.PopupMenu;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class ActivityLogActivity extends AppCompatActivity {

    private ImageView btnBack;
    private LinearLayout spinnerTime;
    private LinearLayout spinnerType;
    private LinearLayout btnArchive;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_log);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(android.R.id.content), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        initViews();
        com.example.dishcision.network.CurrentUserBinder.bind(this, findViewById(R.id.imgAvatar), findViewById(R.id.tvProfileName), findViewById(R.id.tvProfileHandle));
        setupListeners();
    }

    private void initViews() {
        btnBack = findViewById(R.id.btnBack);
        spinnerTime = findViewById(R.id.spinnerTime);
        spinnerType = findViewById(R.id.spinnerType);
        btnArchive = findViewById(R.id.btnArchive);
    }

    private void setupListeners() {
        if (btnBack != null) {
            btnBack.setOnClickListener(v -> finish());
        }

        if (spinnerTime != null) {
            spinnerTime.setOnClickListener(v -> {
                PopupMenu popup = new PopupMenu(this, spinnerTime);
                popup.getMenu().add("Tất cả thời gian");
                popup.getMenu().add("Hôm nay");
                popup.getMenu().add("7 ngày qua");
                popup.getMenu().add("Tháng này");
                popup.setOnMenuItemClickListener((MenuItem item) -> {
                    Toast.makeText(this, "Lọc thời gian: " + item.getTitle(), Toast.LENGTH_SHORT).show();
                    return true;
                });
                popup.show();
            });
        }

        if (spinnerType != null) {
            spinnerType.setOnClickListener(v -> {
                PopupMenu popup = new PopupMenu(this, spinnerType);
                popup.getMenu().add("Tất cả hoạt động");
                popup.getMenu().add("Bài đánh giá");
                popup.getMenu().add("Lượt thích món ăn");
                popup.getMenu().add("Bình luận");
                popup.setOnMenuItemClickListener((MenuItem item) -> {
                    Toast.makeText(this, "Lọc loại hoạt động: " + item.getTitle(), Toast.LENGTH_SHORT).show();
                    return true;
                });
                popup.show();
            });
        }

        if (btnArchive != null) {
            btnArchive.setOnClickListener(v -> {
                Intent intent = new Intent(ActivityLogActivity.this, ArchiveActivity.class);
                startActivity(intent);
            });
        }
    }
}
