package com.example.dishcision;

import android.content.SharedPreferences;
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

import com.example.dishcision.network.ImageLoader;

public class ChatNicknamesActivity extends AppCompatActivity {

    private ImageView btnBack;
    private ImageView imgPartnerAvatar;
    private ImageView imgSelfAvatar;
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

    private int targetUserId = 0;
    private String partnerName = "";
    private String selfName = "Bạn";
    private boolean editingPartner = true;

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

        targetUserId = getIntent().getIntExtra("USER_ID", 0);
        String extraName = getIntent().getStringExtra("CHAT_NAME");
        partnerName = extraName != null && !extraName.trim().isEmpty() ? extraName.trim() : "Đối phương";

        SharedPreferences prefs = getSharedPreferences("DishcisionPrefs", MODE_PRIVATE);
        String savedSelfName = prefs.getString("user_name", "");
        if (savedSelfName != null && !savedSelfName.trim().isEmpty()) {
            selfName = savedSelfName.trim();
        }

        initViews();
        bindData();
        setupListeners();
    }

    private void initViews() {
        btnBack = findViewById(R.id.btnBack);
        imgPartnerAvatar = findViewById(R.id.imgPartnerAvatar);
        imgSelfAvatar = findViewById(R.id.imgSelfAvatar);
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

        String avatarUrl = getIntent().getStringExtra("AVATAR_URL");
        if (imgPartnerAvatar != null && avatarUrl != null && !avatarUrl.trim().isEmpty()) {
            ImageLoader.loadAvatar(imgPartnerAvatar, avatarUrl);
        }
        String selfAvatarUrl = getSharedPreferences("DishcisionPrefs", MODE_PRIVATE).getString("avatar_url", "");
        if (imgSelfAvatar != null && selfAvatarUrl != null && !selfAvatarUrl.trim().isEmpty()) {
            ImageLoader.loadAvatar(imgSelfAvatar, selfAvatarUrl);
        }
    }

    private void bindData() {
        if (tvName1 != null) tvName1.setText(partnerName);
        if (tvName2 != null) tvName2.setText(selfName);

        String partnerNick = ChatPrefsHelper.getPartnerNickname(this, targetUserId);
        String selfNick = ChatPrefsHelper.getSelfNickname(this, targetUserId);

        if (tvNickname1 != null) {
            tvNickname1.setText(partnerNick.isEmpty() ? "Chưa đặt biệt danh" : "Biệt danh: " + partnerNick);
        }
        if (btnAddNickname1 != null) {
            btnAddNickname1.setText(partnerNick.isEmpty() ? "Thêm" : "Chỉnh sửa");
        }
        if (tvNickname2 != null) {
            tvNickname2.setText(selfNick.isEmpty() ? "Chưa đặt biệt danh" : "Biệt danh: " + selfNick);
        }
        if (btnAddNickname2 != null) {
            btnAddNickname2.setText(selfNick.isEmpty() ? "Thêm" : "Chỉnh sửa");
        }

        selectEditingTarget(editingPartner);
    }

    private void selectEditingTarget(boolean forPartner) {
        editingPartner = forPartner;
        String targetLabel = forPartner ? partnerName : selfName;
        String currentVal = forPartner
                ? ChatPrefsHelper.getPartnerNickname(this, targetUserId)
                : ChatPrefsHelper.getSelfNickname(this, targetUserId);

        if (tvFormTitle != null) {
            tvFormTitle.setText("Đặt biệt danh cho " + targetLabel);
        }
        if (edtNicknameInput != null) {
            edtNicknameInput.setText(currentVal);
        }
        if (tvFormSubtext != null) {
            tvFormSubtext.setText(currentVal.isEmpty()
                    ? "Chưa có biệt danh cho " + targetLabel + "."
                    : "Biệt danh hiện tại: “" + currentVal + "”");
        }
    }

    private void setupListeners() {
        if (btnBack != null) btnBack.setOnClickListener(v -> finish());

        if (btnAddNickname1 != null) {
            btnAddNickname1.setOnClickListener(v -> {
                selectEditingTarget(true);
                if (edtNicknameInput != null) edtNicknameInput.requestFocus();
            });
        }

        if (btnAddNickname2 != null) {
            btnAddNickname2.setOnClickListener(v -> {
                selectEditingTarget(false);
                if (edtNicknameInput != null) edtNicknameInput.requestFocus();
            });
        }

        if (btnCancelNickname != null) {
            btnCancelNickname.setOnClickListener(v -> {
                if (editingPartner) {
                    ChatPrefsHelper.setPartnerNickname(this, targetUserId, "");
                } else {
                    ChatPrefsHelper.setSelfNickname(this, targetUserId, "");
                }
                bindData();
                Toast.makeText(this, "Đã xóa biệt danh", Toast.LENGTH_SHORT).show();
            });
        }

        if (btnConfirmNickname != null) {
            btnConfirmNickname.setOnClickListener(v -> {
                String input = edtNicknameInput != null ? edtNicknameInput.getText().toString().trim() : "";
                if (editingPartner) {
                    ChatPrefsHelper.setPartnerNickname(this, targetUserId, input);
                } else {
                    ChatPrefsHelper.setSelfNickname(this, targetUserId, input);
                }
                bindData();
                Toast.makeText(this,
                        input.isEmpty() ? "Đã xóa biệt danh" : "Đã đặt biệt danh: " + input,
                        Toast.LENGTH_SHORT).show();
            });
        }
    }
}
