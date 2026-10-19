package com.example.dishcision;

import android.content.Intent;
import android.os.Bundle;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class MessagesInboxActivity extends AppCompatActivity {

    private LinearLayout btnComposeChat;
    private LinearLayout chatItem1;
    private LinearLayout chatItem2;
    private LinearLayout chatItem3;
    private LinearLayout chatItem4;
    private LinearLayout chatItem5;
    private LinearLayout chatItem6;

    private LinearLayout navHome;
    private LinearLayout navDecide;
    private LinearLayout navProfile;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_messages_inbox);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(android.R.id.content), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        initViews();
        setupListeners();
    }

    private void initViews() {
        android.widget.ImageView btnNotification = findViewById(R.id.btnNotification);
        if (btnNotification != null) {
            btnNotification.setOnClickListener(v -> {
                Intent intent = new Intent(MessagesInboxActivity.this, NotificationsActivity.class);
                startActivity(intent);
            });
        }
        btnComposeChat = findViewById(R.id.btnComposeChat);
        chatItem1 = findViewById(R.id.chatItem1);
        chatItem2 = findViewById(R.id.chatItem2);
        chatItem3 = findViewById(R.id.chatItem3);
        chatItem4 = findViewById(R.id.chatItem4);
        chatItem5 = findViewById(R.id.chatItem5);
        chatItem6 = findViewById(R.id.chatItem6);

        navHome = findViewById(R.id.navHome);
        navDecide = findViewById(R.id.navDecide);
        navProfile = findViewById(R.id.navProfile);
    }

    private void setupListeners() {
        if (btnComposeChat != null) {
            btnComposeChat.setOnClickListener(v -> {
                Intent intent = new Intent(MessagesInboxActivity.this, NewMessageActivity.class);
                startActivity(intent);
            });
        }

        setupChatItemClick(chatItem1, "Bếp Nhà Lục Nam");
        setupChatItemClick(chatItem2, "Maya Lin");
        setupChatItemClick(chatItem3, "Phở Bò Gia Truyền 1986");
        setupChatItemClick(chatItem4, "Julian Cole");
        setupChatItemClick(chatItem5, "Tiệm Trà & Bánh Mùa Thu");
        setupChatItemClick(chatItem6, "Hoàng Yến (Foodie Sài Gòn)");

        if (navHome != null) {
            navHome.setOnClickListener(v -> {
                Intent intent = new Intent(MessagesInboxActivity.this, HomeActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                startActivity(intent);
                finish();
            });
        }

        if (navDecide != null) {
            navDecide.setOnClickListener(v ->
                Toast.makeText(this, "Tính năng Quyết định", Toast.LENGTH_SHORT).show()
            );
        }

        if (navProfile != null) {
            navProfile.setOnClickListener(v -> {
                Intent intent = new Intent(MessagesInboxActivity.this, ProfileActivity.class);
                startActivity(intent);
            });
        }
    }

    private void setupChatItemClick(LinearLayout chatItem, String name) {
        if (chatItem != null) {
            chatItem.setOnClickListener(v -> {
                Intent intent = new Intent(MessagesInboxActivity.this, ChatActivity.class);
                intent.putExtra("CHAT_NAME", name);
                startActivity(intent);
            });
        }
    }
}
