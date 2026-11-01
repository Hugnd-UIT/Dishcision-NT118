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

public class FriendsActivity extends AppCompatActivity {

    private ImageView btnBack;
    private EditText edtSearchFriends;
    private LinearLayout tabInvitations;
    private LinearLayout tabSuggestions;
    private LinearLayout tabFriends;

    private LinearLayout btnAccept1;
    private LinearLayout btnDelete1;
    private LinearLayout btnAccept2;
    private LinearLayout btnDelete2;
    private LinearLayout btnAddFriend1;
    private LinearLayout btnHide1;
    private LinearLayout btnAddFriend2;
    private LinearLayout btnHide2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_friends);

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
        edtSearchFriends = findViewById(R.id.edtSearchFriends);
        tabInvitations = findViewById(R.id.tabInvitations);
        tabSuggestions = findViewById(R.id.tabSuggestions);
        tabFriends = findViewById(R.id.tabFriends);

        btnAccept1 = findViewById(R.id.btnAccept1);
        btnDelete1 = findViewById(R.id.btnDelete1);
        btnAccept2 = findViewById(R.id.btnAccept2);
        btnDelete2 = findViewById(R.id.btnDelete2);
        btnAddFriend1 = findViewById(R.id.btnAddFriend1);
        btnHide1 = findViewById(R.id.btnHide1);
        btnAddFriend2 = findViewById(R.id.btnAddFriend2);
        btnHide2 = findViewById(R.id.btnHide2);
    }

    private void setupListeners() {
        if (btnBack != null) {
            btnBack.setOnClickListener(v -> finish());
        }

        if (tabInvitations != null) {
            tabInvitations.setOnClickListener(v -> selectTab(tabInvitations, tabSuggestions, tabFriends));
        }

        if (tabSuggestions != null) {
            tabSuggestions.setOnClickListener(v -> selectTab(tabSuggestions, tabInvitations, tabFriends));
        }

        if (tabFriends != null) {
            tabFriends.setOnClickListener(v -> selectTab(tabFriends, tabInvitations, tabSuggestions));
        }

        if (btnAccept1 != null) {
            btnAccept1.setOnClickListener(v -> {
                btnAccept1.setBackgroundResource(R.drawable.bg_button_light_small);
                Toast.makeText(this, "Đã chấp nhận lời mời của Thanh Vy!", Toast.LENGTH_SHORT).show();
            });
        }

        if (btnDelete1 != null) {
            btnDelete1.setOnClickListener(v -> {
                if (btnDelete1.getParent() instanceof LinearLayout) {
                    ((LinearLayout) btnDelete1.getParent()).setVisibility(android.view.View.GONE);
                }
                Toast.makeText(this, "Đã xóa lời mời của Thanh Vy", Toast.LENGTH_SHORT).show();
            });
        }

        if (btnAccept2 != null) {
            btnAccept2.setOnClickListener(v -> {
                btnAccept2.setBackgroundResource(R.drawable.bg_button_light_small);
                Toast.makeText(this, "Đã chấp nhận lời mời của Hoàng Nam!", Toast.LENGTH_SHORT).show();
            });
        }

        if (btnDelete2 != null) {
            btnDelete2.setOnClickListener(v -> {
                if (btnDelete2.getParent() instanceof LinearLayout) {
                    ((LinearLayout) btnDelete2.getParent()).setVisibility(android.view.View.GONE);
                }
                Toast.makeText(this, "Đã xóa lời mời của Hoàng Nam", Toast.LENGTH_SHORT).show();
            });
        }

        if (btnAddFriend1 != null) {
            btnAddFriend1.setOnClickListener(v -> {
                btnAddFriend1.setBackgroundResource(R.drawable.bg_button_light_small);
                Toast.makeText(this, "Đã gửi lời mời kết bạn tới Ngọc Hân!", Toast.LENGTH_SHORT).show();
            });
        }

        if (btnHide1 != null) {
            btnHide1.setOnClickListener(v ->
                Toast.makeText(this, "Đã ẩn gợi ý Ngọc Hân", Toast.LENGTH_SHORT).show()
            );
        }

        if (btnAddFriend2 != null) {
            btnAddFriend2.setOnClickListener(v -> {
                btnAddFriend2.setBackgroundResource(R.drawable.bg_button_light_small);
                Toast.makeText(this, "Đã gửi lời mời kết bạn tới Quang Huy!", Toast.LENGTH_SHORT).show();
            });
        }

        if (btnHide2 != null) {
            btnHide2.setOnClickListener(v ->
                Toast.makeText(this, "Đã ẩn gợi ý Quang Huy", Toast.LENGTH_SHORT).show()
            );
        }
    }

    private void selectTab(LinearLayout activeTab, LinearLayout inactive1, LinearLayout inactive2) {
        if (activeTab != null) activeTab.setBackgroundResource(R.drawable.bg_tab_active_white);
        if (inactive1 != null) inactive1.setBackgroundResource(0);
        if (inactive2 != null) inactive2.setBackgroundResource(0);
    }
}
