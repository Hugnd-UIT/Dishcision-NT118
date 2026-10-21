package com.example.dishcision;

import android.content.Intent;
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

public class ChatDetailActivity extends AppCompatActivity {

    private ImageView btnBack;
    private ImageView btnShare;
    private ImageView btnMoreOptions;
    private ImageView btnProfileUser;
    private TextView tvContactName;

    private LinearLayout btnViewProfile;
    private LinearLayout btnMuteNotifications;
    private LinearLayout btnSearchChat;

    private LinearLayout btnPinnedMessages;
    private LinearLayout btnChangeTheme;
    private LinearLayout btnChangeEmoji;
    private LinearLayout btnEditNickname;

    private LinearLayout btnSharedMedia;
    private LinearLayout btnSharedFiles;
    private LinearLayout btnSharedLinks;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_chat_detail);

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
        btnShare = findViewById(R.id.btnShare);
        btnMoreOptions = findViewById(R.id.btnMoreOptions);
        btnProfileUser = findViewById(R.id.btnProfileUser);
        tvContactName = findViewById(R.id.tvContactName);

        btnViewProfile = findViewById(R.id.btnViewProfile);
        btnMuteNotifications = findViewById(R.id.btnMuteNotifications);
        btnSearchChat = findViewById(R.id.btnSearchChat);

        btnPinnedMessages = findViewById(R.id.btnPinnedMessages);
        btnChangeTheme = findViewById(R.id.btnChangeTheme);
        btnChangeEmoji = findViewById(R.id.btnChangeEmoji);
        btnEditNickname = findViewById(R.id.btnEditNickname);

        btnSharedMedia = findViewById(R.id.btnSharedMedia);
        btnSharedFiles = findViewById(R.id.btnSharedFiles);
        btnSharedLinks = findViewById(R.id.btnSharedLinks);

        String chatName = getIntent().getStringExtra("CHAT_NAME");
        if (chatName != null && !chatName.isEmpty() && tvContactName != null) {
            tvContactName.setText(chatName);
        }
    }

    private void setupListeners() {
        if (btnBack != null) {
            btnBack.setOnClickListener(v -> finish());
        }

        if (btnShare != null) {
            btnShare.setOnClickListener(v ->
                Toast.makeText(this, "Chia sẻ liên hệ", Toast.LENGTH_SHORT).show()
            );
        }

        if (btnMoreOptions != null) {
            btnMoreOptions.setOnClickListener(v ->
                Toast.makeText(this, "Tùy chọn khác", Toast.LENGTH_SHORT).show()
            );
        }

        if (btnProfileUser != null || btnViewProfile != null) {
            android.view.View.OnClickListener openProfile = v -> {
                Intent intent = new Intent(ChatDetailActivity.this, UserProfileActivity.class);
                startActivity(intent);
            };
            if (btnProfileUser != null) btnProfileUser.setOnClickListener(openProfile);
            if (btnViewProfile != null) btnViewProfile.setOnClickListener(openProfile);
        }

        if (btnMuteNotifications != null) {
            btnMuteNotifications.setOnClickListener(v ->
                Toast.makeText(this, "Đã tắt thông báo cuộc trò chuyện", Toast.LENGTH_SHORT).show()
            );
        }

        if (btnSearchChat != null) {
            btnSearchChat.setOnClickListener(v -> {
                CharSequence contact = tvContactName != null ? tvContactName.getText() : "Maya Lin";
                Intent intent = new Intent(ChatDetailActivity.this, SearchInChatActivity.class);
                intent.putExtra("CHAT_NAME", contact.toString());
                startActivity(intent);
            });
        }

        if (btnPinnedMessages != null) {
            btnPinnedMessages.setOnClickListener(v -> {
                CharSequence contact = tvContactName != null ? tvContactName.getText() : "Maya Lin";
                Intent intent = new Intent(ChatDetailActivity.this, PinnedMessagesActivity.class);
                intent.putExtra("CHAT_NAME", contact.toString());
                startActivity(intent);
            });
        }

        if (btnChangeTheme != null || btnChangeEmoji != null) {
            android.view.View.OnClickListener openTheme = v -> {
                CharSequence contact = tvContactName != null ? tvContactName.getText() : "Maya Lin";
                Intent intent = new Intent(ChatDetailActivity.this, ChatThemeActivity.class);
                intent.putExtra("CHAT_NAME", contact.toString());
                startActivity(intent);
            };
            if (btnChangeTheme != null) btnChangeTheme.setOnClickListener(openTheme);
            if (btnChangeEmoji != null) btnChangeEmoji.setOnClickListener(openTheme);
        }

        if (btnEditNickname != null) {
            btnEditNickname.setOnClickListener(v -> {
                CharSequence contact = tvContactName != null ? tvContactName.getText() : "Maya Lin";
                Intent intent = new Intent(ChatDetailActivity.this, ChatNicknamesActivity.class);
                intent.putExtra("CHAT_NAME", contact.toString());
                startActivity(intent);
            });
        }

        if (btnSharedMedia != null || btnSharedFiles != null || btnSharedLinks != null) {
            android.view.View.OnClickListener openMedia = v -> {
                CharSequence contact = tvContactName != null ? tvContactName.getText() : "Maya Lin";
                Intent intent = new Intent(ChatDetailActivity.this, SharedMediaActivity.class);
                intent.putExtra("CHAT_NAME", contact.toString());
                startActivity(intent);
            };
            if (btnSharedMedia != null) btnSharedMedia.setOnClickListener(openMedia);
            if (btnSharedFiles != null) btnSharedFiles.setOnClickListener(openMedia);
            if (btnSharedLinks != null) btnSharedLinks.setOnClickListener(openMedia);
        }
    }
}
