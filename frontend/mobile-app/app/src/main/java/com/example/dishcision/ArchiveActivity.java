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

public class ArchiveActivity extends AppCompatActivity {

    private ImageView btnBack;
    private LinearLayout btnViewPost1;
    private LinearLayout btnRestore1;
    private LinearLayout btnViewPost2;
    private LinearLayout btnRestore2;
    private LinearLayout btnViewPost3;
    private LinearLayout btnRestore3;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_archive);

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
        btnViewPost1 = findViewById(R.id.btnViewPost1);
        btnRestore1 = findViewById(R.id.btnRestore1);
        btnViewPost2 = findViewById(R.id.btnViewPost2);
        btnRestore2 = findViewById(R.id.btnRestore2);
        btnViewPost3 = findViewById(R.id.btnViewPost3);
        btnRestore3 = findViewById(R.id.btnRestore3);
    }

    private void setupListeners() {
        if (btnBack != null) {
            btnBack.setOnClickListener(v -> finish());
        }

        if (btnViewPost1 != null) {
            btnViewPost1.setOnClickListener(v -> {
                android.content.Intent intent = new android.content.Intent(ArchiveActivity.this, MyReviewsActivity.class);
                startActivity(intent);
            });
        }

        if (btnRestore1 != null) {
            btnRestore1.setOnClickListener(v ->
                Toast.makeText(this, "Đã khôi phục bài viết về hồ sơ", Toast.LENGTH_SHORT).show()
            );
        }

        if (btnViewPost2 != null) {
            btnViewPost2.setOnClickListener(v -> {
                android.content.Intent intent = new android.content.Intent(ArchiveActivity.this, MyReviewsActivity.class);
                startActivity(intent);
            });
        }

        if (btnRestore2 != null) {
            btnRestore2.setOnClickListener(v ->
                Toast.makeText(this, "Đã khôi phục bài viết về hồ sơ", Toast.LENGTH_SHORT).show()
            );
        }

        if (btnViewPost3 != null) {
            btnViewPost3.setOnClickListener(v -> {
                android.content.Intent intent = new android.content.Intent(ArchiveActivity.this, MyReviewsActivity.class);
                startActivity(intent);
            });
        }

        if (btnRestore3 != null) {
            btnRestore3.setOnClickListener(v ->
                Toast.makeText(this, "Đã khôi phục bài viết về hồ sơ", Toast.LENGTH_SHORT).show()
            );
        }
    }
}
