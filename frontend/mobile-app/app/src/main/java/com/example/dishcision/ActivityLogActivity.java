package com.example.dishcision;

import android.os.Bundle;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
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
            spinnerTime.setOnClickListener(v ->
                Toast.makeText(this, "Lọc theo thời gian: Tháng 10, 2026", Toast.LENGTH_SHORT).show()
            );
        }

        if (spinnerType != null) {
            spinnerType.setOnClickListener(v ->
                Toast.makeText(this, "Lọc theo loại hoạt động", Toast.LENGTH_SHORT).show()
            );
        }

        if (btnArchive != null) {
            btnArchive.setOnClickListener(v -> {
                android.content.Intent intent = new android.content.Intent(ActivityLogActivity.this, ArchiveActivity.class);
                startActivity(intent);
            });
        }
    }
}
