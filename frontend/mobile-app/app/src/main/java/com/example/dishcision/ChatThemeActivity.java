package com.example.dishcision;

import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.util.TypedValue;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.dishcision.network.ImageLoader;

public class ChatThemeActivity extends AppCompatActivity {

    private ImageView btnBack;
    private ImageView imgAvatar;
    private TextView tvContactName;
    private TextView tvPreviewThemeTitle;
    private TextView tvPreviewEmoji;
    private LinearLayout previewEmojiBubble;

    private LinearLayout cardThemeOrange;
    private LinearLayout cardThemeGreen;
    private LinearLayout cardThemePurple;
    private TextView tvStatusOrange;
    private TextView tvStatusGreen;
    private TextView tvStatusPurple;

    private LinearLayout boxEmojiNoodle;
    private LinearLayout boxEmojiChili;
    private LinearLayout boxEmojiPizza;
    private LinearLayout boxEmojiHeart;
    private LinearLayout boxEmojiYum;

    private TextView tvSelectedEmojiNote;
    private LinearLayout btnSaveTheme;

    private int targetUserId = 0;
    private String selectedTheme = "Cam Ớt Cay";
    private int selectedColor = 0xFFFF5A36;
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

        targetUserId = getIntent().getIntExtra("USER_ID", 0);
        selectedTheme = ChatPrefsHelper.getThemeName(this, targetUserId);
        selectedColor = ChatPrefsHelper.getThemeColor(this, targetUserId);
        selectedEmoji = ChatPrefsHelper.getQuickEmoji(this, targetUserId);

        initViews();
        updateThemePreview();
        updateEmojiPreview();
        setupListeners();
    }

    private void initViews() {
        btnBack = findViewById(R.id.btnBack);
        imgAvatar = findViewById(R.id.imgAvatar);
        tvContactName = findViewById(R.id.tvContactName);
        tvPreviewThemeTitle = findViewById(R.id.tvPreviewThemeTitle);
        tvPreviewEmoji = findViewById(R.id.tvPreviewEmoji);
        previewEmojiBubble = findViewById(R.id.previewEmojiBubble);

        cardThemeOrange = findViewById(R.id.cardThemeOrange);
        cardThemeGreen = findViewById(R.id.cardThemeGreen);
        cardThemePurple = findViewById(R.id.cardThemePurple);
        tvStatusOrange = findViewById(R.id.tvStatusOrange);
        tvStatusGreen = findViewById(R.id.tvStatusGreen);
        tvStatusPurple = findViewById(R.id.tvStatusPurple);

        boxEmojiNoodle = findViewById(R.id.boxEmojiNoodle);
        boxEmojiChili = findViewById(R.id.boxEmojiChili);
        boxEmojiPizza = findViewById(R.id.boxEmojiPizza);
        boxEmojiHeart = findViewById(R.id.boxEmojiHeart);
        boxEmojiYum = findViewById(R.id.boxEmojiYum);

        tvSelectedEmojiNote = findViewById(R.id.tvSelectedEmojiNote);
        btnSaveTheme = findViewById(R.id.btnSaveTheme);

        String chatName = getIntent().getStringExtra("CHAT_NAME");
        if (tvContactName != null) {
            tvContactName.setText(chatName != null && !chatName.trim().isEmpty() ? chatName.trim() : "Đoạn chat");
        }
        String avatarUrl = getIntent().getStringExtra("AVATAR_URL");
        if (imgAvatar != null && avatarUrl != null && !avatarUrl.trim().isEmpty()) {
            ImageLoader.loadAvatar(imgAvatar, avatarUrl);
        }
    }

    private void setupListeners() {
        if (btnBack != null) btnBack.setOnClickListener(v -> finish());

        if (cardThemeOrange != null) {
            cardThemeOrange.setOnClickListener(v -> {
                selectedTheme = "Cam Ớt Cay";
                selectedColor = 0xFFFF5A36;
                updateThemePreview();
            });
        }
        if (cardThemeGreen != null) {
            cardThemeGreen.setOnClickListener(v -> {
                selectedTheme = "Trà xanh";
                selectedColor = 0xFF2E9E6E;
                updateThemePreview();
            });
        }
        if (cardThemePurple != null) {
            cardThemePurple.setOnClickListener(v -> {
                selectedTheme = "Khoai môn";
                selectedColor = 0xFF7B5EA7;
                updateThemePreview();
            });
        }

        if (boxEmojiNoodle != null) boxEmojiNoodle.setOnClickListener(v -> setEmoji("🍜"));
        if (boxEmojiChili != null) boxEmojiChili.setOnClickListener(v -> setEmoji("🌶️"));
        if (boxEmojiPizza != null) boxEmojiPizza.setOnClickListener(v -> setEmoji("🍕"));
        if (boxEmojiHeart != null) boxEmojiHeart.setOnClickListener(v -> setEmoji("❤️"));
        if (boxEmojiYum != null) boxEmojiYum.setOnClickListener(v -> setEmoji("😋"));

        if (btnSaveTheme != null) {
            btnSaveTheme.setOnClickListener(v -> {
                ChatPrefsHelper.setTheme(this, targetUserId, selectedTheme, selectedColor);
                ChatPrefsHelper.setQuickEmoji(this, targetUserId, selectedEmoji);
                Toast.makeText(this, "Đã áp dụng chủ đề " + selectedTheme + " & biểu tượng " + selectedEmoji, Toast.LENGTH_SHORT).show();
                finish();
            });
        }
    }

    private void setEmoji(String emoji) {
        selectedEmoji = emoji;
        updateEmojiPreview();
    }

    private void updateThemePreview() {
        if (tvPreviewThemeTitle != null) {
            tvPreviewThemeTitle.setText("XEM TRƯỚC · " + selectedTheme.toUpperCase());
        }
        if (previewEmojiBubble != null) {
            GradientDrawable bg = new GradientDrawable();
            bg.setCornerRadius(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 16, getResources().getDisplayMetrics()));
            bg.setColor(selectedColor);
            previewEmojiBubble.setBackground(bg);
        }

        styleThemeCard(cardThemeOrange, tvStatusOrange, "Cam Ớt Cay".equals(selectedTheme));
        styleThemeCard(cardThemeGreen, tvStatusGreen, "Trà xanh".equals(selectedTheme));
        styleThemeCard(cardThemePurple, tvStatusPurple, "Khoai môn".equals(selectedTheme));
    }

    private void styleThemeCard(LinearLayout card, TextView statusTv, boolean active) {
        if (card != null) {
            card.setBackgroundResource(active ? R.drawable.bg_card_selected_peach : R.drawable.bg_card_white);
        }
        if (statusTv != null) {
            statusTv.setText(active ? "Đang chọn" : "Chưa áp dụng");
            statusTv.setTextColor(active ? 0xFFFF5A36 : 0xFF8E7570);
        }
    }

    private void updateEmojiPreview() {
        if (tvPreviewEmoji != null) tvPreviewEmoji.setText(selectedEmoji);
        if (tvSelectedEmojiNote != null) tvSelectedEmojiNote.setText("Đang chọn " + selectedEmoji);
        styleEmojiBox(boxEmojiNoodle, "🍜".equals(selectedEmoji));
        styleEmojiBox(boxEmojiChili, "🌶️".equals(selectedEmoji));
        styleEmojiBox(boxEmojiPizza, "🍕".equals(selectedEmoji));
        styleEmojiBox(boxEmojiHeart, "❤️".equals(selectedEmoji));
        styleEmojiBox(boxEmojiYum, "😋".equals(selectedEmoji));
    }

    private void styleEmojiBox(LinearLayout box, boolean active) {
        if (box != null) {
            box.setBackgroundResource(active ? R.drawable.bg_chip_selected_orange : R.drawable.bg_card_white);
        }
    }
}
