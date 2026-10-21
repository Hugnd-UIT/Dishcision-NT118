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

public class PinnedMessagesActivity extends AppCompatActivity {

    private ImageView btnBack;
    private TextView tvContactName;
    private TextView btnViewInChat1;
    private TextView btnUnpin1;
    private TextView btnViewInChat2;
    private TextView btnUnpin2;
    private LinearLayout cardPinned1;
    private LinearLayout cardPinned2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_pinned_messages);

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
        btnViewInChat1 = findViewById(R.id.btnViewInChat1);
        btnUnpin1 = findViewById(R.id.btnUnpin1);
        btnViewInChat2 = findViewById(R.id.btnViewInChat2);
        btnUnpin2 = findViewById(R.id.btnUnpin2);
        cardPinned1 = findViewById(R.id.cardPinned1);
        cardPinned2 = findViewById(R.id.cardPinned2);

        String chatName = getIntent().getStringExtra("CHAT_NAME");
        if (chatName != null && !chatName.isEmpty() && tvContactName != null) {
            tvContactName.setText(chatName);
        }
    }

    private void setupListeners() {
        if (btnBack != null) {
            btnBack.setOnClickListener(v -> finish());
        }

        if (btnViewInChat1 != null) {
            btnViewInChat1.setOnClickListener(v -> finish());
        }

        if (btnUnpin1 != null) {
            btnUnpin1.setOnClickListener(v -> {
                if (cardPinned1 != null) {
                    cardPinned1.setVisibility(android.view.View.GONE);
                }
                Toast.makeText(this, "Đã bỏ ghim tin nhắn", Toast.LENGTH_SHORT).show();
            });
        }

        if (btnViewInChat2 != null) {
            btnViewInChat2.setOnClickListener(v -> finish());
        }

        if (btnUnpin2 != null) {
            btnUnpin2.setOnClickListener(v -> {
                if (cardPinned2 != null) {
                    cardPinned2.setVisibility(android.view.View.GONE);
                }
                Toast.makeText(this, "Đã bỏ ghim tin nhắn", Toast.LENGTH_SHORT).show();
            });
        }
    }
}
