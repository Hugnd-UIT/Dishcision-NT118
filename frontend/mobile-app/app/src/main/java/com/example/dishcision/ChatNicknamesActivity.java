package com.example.dishcision;

import android.os.Bundle;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class ChatNicknamesActivity extends AppCompatActivity {

    private ImageView btnBack;
    private TextView tvName1;
    private TextView tvNickname1;
    private TextView btnAddNickname1;
    private TextView tvName2;
    private TextView tvNickname2;
    private TextView btnAddNickname2;

    private TextView tvFormTitle;
    private EditText edtNicknameInput;
    private TextView tvFormSubtext;
    private LinearLayout btnConfirmNickname;
    private LinearLayout btnCancelNickname;

    private String targetUserForNickname = "Maya Lin";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_chat_nicknames);

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
        tvName1 = findViewById(R.id.tvName1);
        tvNickname1 = findViewById(R.id.tvNickname1);
        btnAddNickname1 = findViewById(R.id.btnAddNickname1);

        tvName2 = findViewById(R.id.tvName2);
        tvNickname2 = findViewById(R.id.tvNickname2);
        btnAddNickname2 = findViewById(R.id.btnAddNickname2);

        tvFormTitle = findViewById(R.id.tvFormTitle);
        edtNicknameInput = findViewById(R.id.edtNicknameInput);
        tvFormSubtext = findViewById(R.id.tvFormSubtext);
        btnConfirmNickname = findViewById(R.id.btnConfirmNickname);
        btnCancelNickname = findViewById(R.id.btnCancelNickname);

        String chatName = getIntent().getStringExtra("CHAT_NAME");
        if (chatName != null && !chatName.isEmpty() && tvName1 != null) {
            tvName1.setText(chatName);
            targetUserForNickname = chatName;
            if (tvFormTitle != null) {
                tvFormTitle.setText("Thêm biệt danh cho " + chatName);
            }
        }
    }

    private void setupListeners() {
        if (btnBack != null) {
            btnBack.setOnClickListener(v -> finish());
        }

        if (btnAddNickname1 != null) {
            btnAddNickname1.setOnClickListener(v -> {
                String name = tvName1 != null ? tvName1.getText().toString() : "Maya Lin";
                targetUserForNickname = name;
                if (tvFormTitle != null) {
                    tvFormTitle.setText("Thêm biệt danh cho " + name);
                }
                if (edtNicknameInput != null) {
                    edtNicknameInput.requestFocus();
                }
            });
        }

        if (btnAddNickname2 != null) {
            btnAddNickname2.setOnClickListener(v -> {
                targetUserForNickname = "Bạn";
                if (tvFormTitle != null) {
                    tvFormTitle.setText("Thêm biệt danh cho Bạn");
                }
                if (edtNicknameInput != null) {
                    edtNicknameInput.requestFocus();
                }
            });
        }

        if (btnCancelNickname != null) {
            btnCancelNickname.setOnClickListener(v -> {
                if (edtNicknameInput != null) {
                    edtNicknameInput.setText("");
                }
            });
        }

        if (btnConfirmNickname != null) {
            btnConfirmNickname.setOnClickListener(v -> {
                String input = edtNicknameInput != null ? edtNicknameInput.getText().toString().trim() : "";
                if (input.isEmpty()) {
                    Toast.makeText(this, "Vui lòng nhập biệt danh!", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (targetUserForNickname.equals("Bạn")) {
                    if (tvNickname2 != null) tvNickname2.setText("Biệt danh: " + input);
                } else {
                    if (tvNickname1 != null) tvNickname1.setText("Biệt danh: " + input);
                }

                if (tvFormSubtext != null) {
                    tvFormSubtext.setText("Đã áp dụng biệt danh: “" + input + "” cho " + targetUserForNickname);
                }

                Toast.makeText(this, "Đã đặt biệt danh: " + input, Toast.LENGTH_SHORT).show();
            });
        }
    }
}
