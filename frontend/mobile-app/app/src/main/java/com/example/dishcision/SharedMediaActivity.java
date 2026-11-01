package com.example.dishcision;

import android.os.Bundle;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class SharedMediaActivity extends AppCompatActivity {

    private ImageView btnBack;
    private TextView tvContactName;
    private LinearLayout tabPhotos;
    private LinearLayout tabFiles;
    private LinearLayout tabLinks;
    private LinearLayout btnLoadMoreMedia;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_shared_media);

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
        tvContactName = findViewById(R.id.tvContactName);
        tabPhotos = findViewById(R.id.tabPhotos);
        tabFiles = findViewById(R.id.tabFiles);
        tabLinks = findViewById(R.id.tabLinks);
        btnLoadMoreMedia = findViewById(R.id.btnLoadMoreMedia);

        String chatName = getIntent().getStringExtra("CHAT_NAME");
        if (chatName != null && !chatName.isEmpty() && tvContactName != null) {
            tvContactName.setText(chatName);
        }
    }

    private void setupListeners() {
        if (btnBack != null) {
            btnBack.setOnClickListener(v -> finish());
        }

        if (tabPhotos != null) {
            tabPhotos.setOnClickListener(v -> {
                selectTab(tabPhotos, tabFiles, tabLinks);
                Toast.makeText(this, "Đang hiển thị Ảnh đã chia sẻ (24)", Toast.LENGTH_SHORT).show();
            });
        }

        if (tabFiles != null) {
            tabFiles.setOnClickListener(v -> {
                selectTab(tabFiles, tabPhotos, tabLinks);
                Toast.makeText(this, "Đang hiển thị Tệp đính kèm (5)", Toast.LENGTH_SHORT).show();
            });
        }

        if (tabLinks != null) {
            tabLinks.setOnClickListener(v -> {
                selectTab(tabLinks, tabPhotos, tabFiles);
                Toast.makeText(this, "Đang hiển thị Liên kết web (12)", Toast.LENGTH_SHORT).show();
            });
        }

        if (btnLoadMoreMedia != null) {
            btnLoadMoreMedia.setOnClickListener(v ->
                Toast.makeText(this, "Đã tải thêm tất cả phương tiện!", Toast.LENGTH_SHORT).show()
            );
        }
    }

    private void selectTab(LinearLayout activeTab, LinearLayout inactiveTab1, LinearLayout inactiveTab2) {
        if (activeTab != null) activeTab.setBackgroundResource(R.drawable.bg_button_light_blue);
        if (inactiveTab1 != null) inactiveTab1.setBackgroundResource(0);
        if (inactiveTab2 != null) inactiveTab2.setBackgroundResource(0);
    }
}
