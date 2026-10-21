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

public class ChatThemeActivity extends AppCompatActivity {

    private ImageView btnBack;
    private TextView tvContactName;
    private TextView tvPreviewThemeTitle;
    private TextView tvPreviewEmoji;
    private LinearLayout previewEmojiBubble;

    private LinearLayout cardThemeOrange;
    private LinearLayout cardThemeGreen;
    private LinearLayout cardThemePurple;

    private LinearLayout boxEmojiNoodle;
    private LinearLayout boxEmojiChili;
    private LinearLayout boxEmojiPizza;
    private LinearLayout boxEmojiHeart;
    private LinearLayout boxEmojiYum;

    private TextView tvSelectedEmojiNote;
    private LinearLayout btnSaveTheme;

    private String selectedTheme = "Cam Ớt Cay";
    private String selectedEmoji = "🍜";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_chat_theme);

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
        tvPreviewThemeTitle = findViewById(R.id.tvPreviewThemeTitle);
        tvPreviewEmoji = findViewById(R.id.tvPreviewEmoji);
        previewEmojiBubble = findViewById(R.id.previewEmojiBubble);

        cardThemeOrange = findViewById(R.id.cardThemeOrange);
        cardThemeGreen = findViewById(R.id.cardThemeGreen);
        cardThemePurple = findViewById(R.id.cardThemePurple);

        boxEmojiNoodle = findViewById(R.id.boxEmojiNoodle);
        boxEmojiChili = findViewById(R.id.boxEmojiChili);
        boxEmojiPizza = findViewById(R.id.boxEmojiPizza);
        boxEmojiHeart = findViewById(R.id.boxEmojiHeart);
        boxEmojiYum = findViewById(R.id.boxEmojiYum);

        tvSelectedEmojiNote = findViewById(R.id.tvSelectedEmojiNote);
        btnSaveTheme = findViewById(R.id.btnSaveTheme);

        String chatName = getIntent().getStringExtra("CHAT_NAME");
        if (chatName != null && !chatName.isEmpty() && tvContactName != null) {
            tvContactName.setText(chatName);
        }
    }

    private void setupListeners() {
        if (btnBack != null) {
            btnBack.setOnClickListener(v -> finish());
        }

        if (cardThemeOrange != null) {
            cardThemeOrange.setOnClickListener(v -> updateTheme("Cam Ớt Cay"));
        }

        if (cardThemeGreen != null) {
            cardThemeGreen.setOnClickListener(v -> updateTheme("Trà xanh"));
        }

        if (cardThemePurple != null) {
            cardThemePurple.setOnClickListener(v -> updateTheme("Khoai môn"));
        }

        if (boxEmojiNoodle != null) boxEmojiNoodle.setOnClickListener(v -> updateEmoji("🍜"));
        if (boxEmojiChili != null) boxEmojiChili.setOnClickListener(v -> updateEmoji("🌶️"));
        if (boxEmojiPizza != null) boxEmojiPizza.setOnClickListener(v -> updateEmoji("🍕"));
        if (boxEmojiHeart != null) boxEmojiHeart.setOnClickListener(v -> updateEmoji("🖤"));
        if (boxEmojiYum != null) boxEmojiYum.setOnClickListener(v -> updateEmoji("😋"));

        if (btnSaveTheme != null) {
            btnSaveTheme.setOnClickListener(v -> {
                Toast.makeText(this, "Đã lưu chủ đề: " + selectedTheme + " & biểu tượng " + selectedEmoji, Toast.LENGTH_SHORT).show();
                finish();
            });
        }
    }

    private void updateTheme(String themeName) {
        selectedTheme = themeName;
        if (tvPreviewThemeTitle != null) {
            tvPreviewThemeTitle.setText("XEM TRƯỚC · " + themeName.toUpperCase());
        }
        Toast.makeText(this, "Đã xem trước chủ đề: " + themeName, Toast.LENGTH_SHORT).show();
    }

    private void updateEmoji(String emoji) {
        selectedEmoji = emoji;
        if (tvPreviewEmoji != null) {
            tvPreviewEmoji.setText(emoji);
        }
        if (tvSelectedEmojiNote != null) {
            tvSelectedEmojiNote.setText("Đang chọn " + emoji + " · Các biểu tượng khác chưa áp dụng.");
        }
        Toast.makeText(this, "Đã chọn biểu tượng: " + emoji, Toast.LENGTH_SHORT).show();
    }
}
