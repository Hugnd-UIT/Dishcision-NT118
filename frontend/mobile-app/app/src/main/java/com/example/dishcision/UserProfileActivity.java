package com.example.dishcision;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class UserProfileActivity extends AppCompatActivity {

    private ImageView btnBack;
    private ImageView btnShare;
    private ImageView btnMore;
    private LinearLayout btnFollow;
    private LinearLayout btnMessage;

    private LinearLayout navHome;
    private LinearLayout navDecide;
    private LinearLayout navCreate;
    private LinearLayout navMessage;
    private LinearLayout navProfile;

    private boolean isFollowing = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_user_profile);

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
        btnMore = findViewById(R.id.btnMore);
        btnFollow = findViewById(R.id.btnFollow);
        btnMessage = findViewById(R.id.btnMessage);

        navHome = findViewById(R.id.navHome);
        navDecide = findViewById(R.id.navDecide);
        navCreate = findViewById(R.id.navCreate);
        navMessage = findViewById(R.id.navMessage);
        navProfile = findViewById(R.id.navProfile);
    }

    private void setupListeners() {
        if (btnBack != null) {
            btnBack.setOnClickListener(v -> finish());
        }

        if (btnShare != null) {
            btnShare.setOnClickListener(v ->
                Toast.makeText(this, "Chia sẻ trang cá nhân Linh Đan", Toast.LENGTH_SHORT).show()
            );
        }

        if (btnMore != null) {
            btnMore.setOnClickListener(v ->
                Toast.makeText(this, "Tùy chọn khác", Toast.LENGTH_SHORT).show()
            );
        }

        if (btnFollow != null) {
            btnFollow.setOnClickListener(v -> {
                isFollowing = !isFollowing;
                if (isFollowing) {
                    Toast.makeText(this, "Đã theo dõi Linh Đan", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(this, "Đã hủy theo dõi", Toast.LENGTH_SHORT).show();
                }
            });
        }

        if (btnMessage != null) {
            btnMessage.setOnClickListener(v -> {
                Intent intent = new Intent(UserProfileActivity.this, ChatActivity.class);
                intent.putExtra("CHAT_NAME", "Linh Đan (Đam Mê Ăn)");
                startActivity(intent);
            });
        }

        if (navHome != null) {
            navHome.setOnClickListener(v -> {
                Intent intent = new Intent(UserProfileActivity.this, HomeActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                startActivity(intent);
                finish();
            });
        }

        if (navDecide != null) {
            navDecide.setOnClickListener(v -> {
                Intent intent = new Intent(UserProfileActivity.this, DecideActivity.class);
                startActivity(intent);
            });
        }

        if (navCreate != null) {
            navCreate.setOnClickListener(v -> {
                Intent intent = new Intent(UserProfileActivity.this, CreatePostActivity.class);
                startActivity(intent);
            });
        }

        if (navMessage != null) {
            navMessage.setOnClickListener(v -> {
                Intent intent = new Intent(UserProfileActivity.this, MessagesInboxActivity.class);
                startActivity(intent);
            });
        }

        if (navProfile != null) {
            navProfile.setOnClickListener(v -> {
                Intent intent = new Intent(UserProfileActivity.this, ProfileActivity.class);
                startActivity(intent);
            });
        }
    }
}
