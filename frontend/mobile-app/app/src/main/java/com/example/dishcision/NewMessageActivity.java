package com.example.dishcision;

import android.content.Intent;
import android.os.Bundle;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class NewMessageActivity extends AppCompatActivity {

    private TextView btnCancel;
    private LinearLayout chipRecipientMaya;
    private LinearLayout itemMayaLin;
    private LinearLayout itemLucNam;
    private LinearLayout itemJulian;
    private LinearLayout itemPhoBo;
    private LinearLayout itemTiemTra;
    private LinearLayout btnStartChat;

    private String selectedContactName = "Maya Lin";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_new_message);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(android.R.id.content), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        initViews();
        setupListeners();
    }

    private void initViews() {
        btnCancel = findViewById(R.id.btnCancel);
        chipRecipientMaya = findViewById(R.id.chipRecipientMaya);
        itemMayaLin = findViewById(R.id.itemMayaLin);
        itemLucNam = findViewById(R.id.itemLucNam);
        itemJulian = findViewById(R.id.itemJulian);
        itemPhoBo = findViewById(R.id.itemPhoBo);
        itemTiemTra = findViewById(R.id.itemTiemTra);
        btnStartChat = findViewById(R.id.btnStartChat);
    }

    private void setupListeners() {
        if (btnCancel != null) {
            btnCancel.setOnClickListener(v -> finish());
        }

        if (chipRecipientMaya != null) {
            chipRecipientMaya.setOnClickListener(v -> {
                selectedContactName = "";
                Toast.makeText(this, "Đã bỏ chọn Maya Lin", Toast.LENGTH_SHORT).show();
            });
        }

        setupContactItem(itemMayaLin, "Maya Lin");
        setupContactItem(itemLucNam, "Bếp Nhà Lục Nam");
        setupContactItem(itemJulian, "Julian Cole");
        setupContactItem(itemPhoBo, "Phở Bò Gia Truyền 1986");
        setupContactItem(itemTiemTra, "Tiệm Trà & Bánh Mùa Thu");

        if (btnStartChat != null) {
            btnStartChat.setOnClickListener(v -> {
                if (selectedContactName == null || selectedContactName.isEmpty()) {
                    Toast.makeText(this, "Vui lòng chọn ít nhất 1 người hoặc quán ăn để bắt đầu trò chuyện", Toast.LENGTH_SHORT).show();
                    return;
                }
                Intent intent = new Intent(NewMessageActivity.this, ChatDetailActivity.class);
                intent.putExtra("CHAT_NAME", selectedContactName);
                startActivity(intent);
                finish();
            });
        }
    }

    private void setupContactItem(LinearLayout item, String name) {
        if (item != null) {
            item.setOnClickListener(v -> {
                selectedContactName = name;
                Intent intent = new Intent(NewMessageActivity.this, ChatDetailActivity.class);
                intent.putExtra("CHAT_NAME", name);
                startActivity(intent);
                finish();
            });
        }
    }
}
