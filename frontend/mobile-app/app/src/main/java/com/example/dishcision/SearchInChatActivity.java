package com.example.dishcision;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
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

public class SearchInChatActivity extends AppCompatActivity {

    private ImageView btnBack;
    private TextView tvContactName;
    private EditText edtSearchQuery;
    private ImageView btnClearSearch;
    private TextView tvMatchHeader;
    private LinearLayout cardResult1;
    private LinearLayout cardResult2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_search_in_chat);

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
        edtSearchQuery = findViewById(R.id.edtSearchQuery);
        btnClearSearch = findViewById(R.id.btnClearSearch);
        tvMatchHeader = findViewById(R.id.tvMatchHeader);
        cardResult1 = findViewById(R.id.cardResult1);
        cardResult2 = findViewById(R.id.cardResult2);

        String chatName = getIntent().getStringExtra("CHAT_NAME");
        if (chatName != null && !chatName.isEmpty() && tvContactName != null) {
            tvContactName.setText(chatName);
        }
    }

    private void setupListeners() {
        if (btnBack != null) {
            btnBack.setOnClickListener(v -> finish());
        }

        if (btnClearSearch != null) {
            btnClearSearch.setOnClickListener(v -> {
                if (edtSearchQuery != null) {
                    edtSearchQuery.setText("");
                }
            });
        }

        if (edtSearchQuery != null) {
            edtSearchQuery.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    String query = s.toString().trim();
                    if (query.isEmpty()) {
                        if (tvMatchHeader != null) tvMatchHeader.setText("Nhập từ khóa để tìm trong đoạn chat");
                        if (cardResult1 != null) cardResult1.setVisibility(View.GONE);
                        if (cardResult2 != null) cardResult2.setVisibility(View.GONE);
                    } else {
                        if (tvMatchHeader != null) tvMatchHeader.setText("Tìm thấy kết quả cho “" + query + "”");
                        if (cardResult1 != null) cardResult1.setVisibility(View.VISIBLE);
                        if (cardResult2 != null) cardResult2.setVisibility(View.VISIBLE);
                    }
                }

                @Override
                public void afterTextChanged(Editable s) {}
            });
        }

        if (cardResult1 != null) {
            cardResult1.setOnClickListener(v -> {
                Toast.makeText(this, "Chuyển tới vị trí tin nhắn", Toast.LENGTH_SHORT).show();
                finish();
            });
        }

        if (cardResult2 != null) {
            cardResult2.setOnClickListener(v -> {
                Toast.makeText(this, "Chuyển tới vị trí tin nhắn", Toast.LENGTH_SHORT).show();
                finish();
            });
        }
    }
}
