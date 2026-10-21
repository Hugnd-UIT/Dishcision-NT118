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

public class FollowersActivity extends AppCompatActivity {

    private ImageView btnBack;
    private TextView btnStatus1;
    private TextView btnStatus2;
    private TextView btnStatus3;
    private TextView btnStatus4;
    private TextView btnStatus5;
    private TextView btnStatus6;
    private LinearLayout btnLoadMore;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_followers);

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
        btnStatus1 = findViewById(R.id.btnStatus1);
        btnStatus2 = findViewById(R.id.btnStatus2);
        btnStatus3 = findViewById(R.id.btnStatus3);
        btnStatus4 = findViewById(R.id.btnStatus4);
        btnStatus5 = findViewById(R.id.btnStatus5);
        btnStatus6 = findViewById(R.id.btnStatus6);
        btnLoadMore = findViewById(R.id.btnLoadMore);
    }

    private void setupListeners() {
        if (btnBack != null) {
            btnBack.setOnClickListener(v -> finish());
        }

        setupToggleFollow(btnStatus1, "Thanh Vy");
        setupToggleFollow(btnStatus2, "Quang Huy");
        setupToggleFollow(btnStatus3, "Ngọc Hân");
        setupToggleFollow(btnStatus4, "Hoàng Nam");
        setupToggleFollow(btnStatus5, "Mai Anh");
        setupToggleFollow(btnStatus6, "Gia Bảo");

        if (btnLoadMore != null) {
            btnLoadMore.setOnClickListener(v ->
                Toast.makeText(this, "Tải thêm danh sách người theo dõi...", Toast.LENGTH_SHORT).show()
            );
        }
    }

    private void setupToggleFollow(TextView btn, String name) {
        if (btn != null) {
            btn.setOnClickListener(v -> {
                CharSequence text = btn.getText();
                if ("Đang theo dõi".contentEquals(text)) {
                    btn.setText("Theo dõi lại");
                    Toast.makeText(this, "Đã hủy theo dõi " + name, Toast.LENGTH_SHORT).show();
                } else {
                    btn.setText("Đang theo dõi");
                    Toast.makeText(this, "Đã theo dõi " + name, Toast.LENGTH_SHORT).show();
                }
            });
        }
    }
}
