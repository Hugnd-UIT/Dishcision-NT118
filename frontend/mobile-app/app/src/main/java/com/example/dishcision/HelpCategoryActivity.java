package com.example.dishcision;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class HelpCategoryActivity extends AppCompatActivity {

    private ImageView btnBack;
    private TextView tvCatTitle, tvCatDesc;
    private ImageView imgCatIcon;
    private View btnSubmitRequest;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_help_category);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main_help_cat), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        initViews();
        readIntentExtras();
        setupListeners();
    }

    private void initViews() {
        btnBack = findViewById(R.id.btnBack);
        tvCatTitle = findViewById(R.id.tvCatTitle);
        tvCatDesc = findViewById(R.id.tvCatDesc);
        imgCatIcon = findViewById(R.id.imgCatIcon);
        btnSubmitRequest = findViewById(R.id.btnSubmitRequest);
    }

    private void readIntentExtras() {
        Intent intent = getIntent();
        if (intent == null) return;
        int catId = intent.getIntExtra("CAT_ID", 1);
        
        switch (catId) {
            case 1:
                tvCatTitle.setText("Tài khoản & đăng nhập");
                tvCatDesc.setText("Truy cập lại tài khoản, đổi mật khẩu và giải quyết các vấn đề khi đăng nhập.");
                imgCatIcon.setImageResource(R.drawable.ic_person_outline);
                break;
            case 2:
                tvCatTitle.setText("Khám phá món & quán");
                tvCatDesc.setText("Tìm ý tưởng cho bữa ăn, khám phá quán và chọn điều phù hợp với sở thích của bạn.");
                imgCatIcon.setImageResource(R.drawable.ic_utensils);
                break;
            case 3:
                tvCatTitle.setText("Đã lưu & theo dõi");
                tvCatDesc.setText("Xem lại món bạn muốn thử và quản lý những quán, người dùng bạn đang theo dõi.");
                imgCatIcon.setImageResource(R.drawable.ic_bookmark);
                break;
            case 4:
                tvCatTitle.setText("Bài viết & story");
                tvCatDesc.setText("Chia sẻ trải nghiệm ẩm thực và chọn ai có thể xem nội dung của bạn.");
                imgCatIcon.setImageResource(R.drawable.ic_image);
                break;
            case 5:
                tvCatTitle.setText("Tin nhắn");
                tvCatDesc.setText("Bắt đầu trò chuyện, tìm lại tin nhắn và xử lý những vấn đề khi liên hệ với người khác.");
                imgCatIcon.setImageResource(R.drawable.ic_comment);
                break;
            case 6:
                tvCatTitle.setText("An toàn & riêng tư");
                tvCatDesc.setText("Bảo vệ tài khoản, chọn cách kết nối và quản lý những tương tác không mong muốn.");
                imgCatIcon.setImageResource(R.drawable.ic_shield_check);
                break;
        }
    }

    private void setupListeners() {
        if (btnBack != null) btnBack.setOnClickListener(v -> finish());
        
        if (btnSubmitRequest != null) {
            btnSubmitRequest.setOnClickListener(v -> {
                Intent intent = new Intent(this, SubmitHelpRequestActivity.class);
                startActivity(intent);
            });
        }
    }
}