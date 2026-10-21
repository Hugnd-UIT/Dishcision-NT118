package com.example.dishcision;

import android.content.Intent;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class ChatActivity extends AppCompatActivity {

    private ImageView btnBack;
    private TextView tvChatTitle;
    private ImageView btnInfo;
    private ImageView btnProfileUser;
    private TextView btnViewLocation;
    private ImageView btnCamera;
    private ImageView btnAttachImage;
    private ImageView btnMic;
    private EditText edtChatMessage;
    private FrameLayout btnSendMessage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_chat);

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
        tvChatTitle = findViewById(R.id.tvChatTitle);
        btnInfo = findViewById(R.id.btnInfo);
        btnProfileUser = findViewById(R.id.btnProfileUser);
        btnViewLocation = findViewById(R.id.btnViewLocation);
        btnCamera = findViewById(R.id.btnCamera);
        btnAttachImage = findViewById(R.id.btnAttachImage);
        btnMic = findViewById(R.id.btnMic);
        edtChatMessage = findViewById(R.id.edtChatMessage);
        btnSendMessage = findViewById(R.id.btnSendMessage);

        String chatName = getIntent().getStringExtra("CHAT_NAME");
        if (chatName != null && !chatName.isEmpty() && tvChatTitle != null) {
            tvChatTitle.setText(chatName);
        }
    }

    private void setupListeners() {
        if (btnBack != null) {
            btnBack.setOnClickListener(v -> finish());
        }

        if (btnInfo != null) {
            btnInfo.setOnClickListener(v -> {
                CharSequence title = tvChatTitle != null ? tvChatTitle.getText() : "Trò chuyện";
                Intent intent = new Intent(ChatActivity.this, ChatDetailActivity.class);
                intent.putExtra("CHAT_NAME", title.toString());
                startActivity(intent);
            });
        }

        if (btnProfileUser != null) {
            btnProfileUser.setOnClickListener(v -> {
                Intent intent = new Intent(ChatActivity.this, UserProfileActivity.class);
                startActivity(intent);
            });
        }

        if (btnViewLocation != null) {
            btnViewLocation.setOnClickListener(v ->
                Toast.makeText(this, "Mở bản đồ xem địa điểm Quận 1", Toast.LENGTH_SHORT).show()
            );
        }

        if (btnCamera != null) {
            btnCamera.setOnClickListener(v ->
                Toast.makeText(this, "Mở máy ảnh chụp ảnh món ăn", Toast.LENGTH_SHORT).show()
            );
        }

        if (btnAttachImage != null) {
            btnAttachImage.setOnClickListener(v ->
                Toast.makeText(this, "Chọn hình ảnh từ thư viện", Toast.LENGTH_SHORT).show()
            );
        }

        if (btnMic != null) {
            btnMic.setOnClickListener(v ->
                Toast.makeText(this, "Ghi âm tin nhắn thoại", Toast.LENGTH_SHORT).show()
            );
        }

        if (btnSendMessage != null) {
            btnSendMessage.setOnClickListener(v -> {
                String message = edtChatMessage != null ? edtChatMessage.getText().toString().trim() : "";
                if (message.isEmpty()) {
                    Toast.makeText(this, "Vui lòng nhập nội dung tin nhắn", Toast.LENGTH_SHORT).show();
                    return;
                }
                Toast.makeText(this, "Đã gửi: " + message, Toast.LENGTH_SHORT).show();
                if (edtChatMessage != null) {
                    edtChatMessage.setText("");
                }
            });
        }
    }
}
