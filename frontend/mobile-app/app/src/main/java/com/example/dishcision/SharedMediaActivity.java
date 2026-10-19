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
            tabPhotos.setOnClickListener(v ->
                Toast.makeText(this, "Xem danh sách ảnh đã chia sẻ", Toast.LENGTH_SHORT).show()
            );
        }

        if (tabFiles != null) {
            tabFiles.setOnClickListener(v ->
                Toast.makeText(this, "Xem danh sách tệp đính kèm", Toast.LENGTH_SHORT).show()
            );
        }

        if (tabLinks != null) {
            tabLinks.setOnClickListener(v ->
                Toast.makeText(this, "Xem danh sách liên kết web", Toast.LENGTH_SHORT).show()
            );
        }

        if (btnLoadMoreMedia != null) {
            btnLoadMoreMedia.setOnClickListener(v ->
                Toast.makeText(this, "Tải thêm phương tiện đã chia sẻ", Toast.LENGTH_SHORT).show()
            );
        }
    }
}
