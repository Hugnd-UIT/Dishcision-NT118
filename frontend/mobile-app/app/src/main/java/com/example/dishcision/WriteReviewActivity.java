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

public class WriteReviewActivity extends AppCompatActivity {

    private ImageView btnBack;
    private TextView btnChangeRestaurant;
    private TextView tvRatingText;
    private LinearLayout chipDineIn;
    private LinearLayout chipTakeaway;
    private EditText edtDishesTried;
    private EditText edtReviewContent;
    private LinearLayout btnUploadPhoto;
    private LinearLayout btnSubmitReview;

    private ImageView star1;
    private ImageView star2;
    private ImageView star3;
    private ImageView star4;
    private ImageView star5;

    private int currentRating = 4;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_write_review);

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
        btnChangeRestaurant = findViewById(R.id.btnChangeRestaurant);
        tvRatingText = findViewById(R.id.tvRatingText);
        chipDineIn = findViewById(R.id.chipDineIn);
        chipTakeaway = findViewById(R.id.chipTakeaway);
        edtDishesTried = findViewById(R.id.edtDishesTried);
        edtReviewContent = findViewById(R.id.edtReviewContent);
        btnUploadPhoto = findViewById(R.id.btnUploadPhoto);
        btnSubmitReview = findViewById(R.id.btnSubmitReview);

        star1 = findViewById(R.id.star1);
        star2 = findViewById(R.id.star2);
        star3 = findViewById(R.id.star3);
        star4 = findViewById(R.id.star4);
        star5 = findViewById(R.id.star5);
    }

    private void setupListeners() {
        if (btnBack != null) {
            btnBack.setOnClickListener(v -> finish());
        }

        if (btnChangeRestaurant != null) {
            btnChangeRestaurant.setOnClickListener(v -> {
                android.content.Intent intent = new android.content.Intent(WriteReviewActivity.this, FavoriteRestaurantsActivity.class);
                startActivity(intent);
            });
        }

        setupStarListeners();

        if (chipDineIn != null) {
            chipDineIn.setOnClickListener(v ->
                Toast.makeText(this, "Trải nghiệm: Ăn tại quán", Toast.LENGTH_SHORT).show()
            );
        }

        if (chipTakeaway != null) {
            chipTakeaway.setOnClickListener(v ->
                Toast.makeText(this, "Trải nghiệm: Mang về", Toast.LENGTH_SHORT).show()
            );
        }

        if (btnUploadPhoto != null) {
            btnUploadPhoto.setOnClickListener(v ->
                Toast.makeText(this, "Chọn ảnh từ thư viện", Toast.LENGTH_SHORT).show()
            );
        }

        if (btnSubmitReview != null) {
            btnSubmitReview.setOnClickListener(v -> {
                String dishes = edtDishesTried != null ? edtDishesTried.getText().toString().trim() : "";
                String reviewText = edtReviewContent != null ? edtReviewContent.getText().toString().trim() : "";
                if (reviewText.isEmpty()) {
                    Toast.makeText(this, "Vui lòng nhập cảm nhận của bạn!", Toast.LENGTH_SHORT).show();
                    return;
                }
                Toast.makeText(this, "Đã gửi đánh giá (" + currentRating + " sao) cho món " + dishes + "!", Toast.LENGTH_SHORT).show();
                finish();
            });
        }
    }

    private void setupStarListeners() {
        if (star1 != null) star1.setOnClickListener(v -> updateStarRating(1));
        if (star2 != null) star2.setOnClickListener(v -> updateStarRating(2));
        if (star3 != null) star3.setOnClickListener(v -> updateStarRating(3));
        if (star4 != null) star4.setOnClickListener(v -> updateStarRating(4));
        if (star5 != null) star5.setOnClickListener(v -> updateStarRating(5));
    }

    private void updateStarRating(int rating) {
        currentRating = rating;
        if (tvRatingText != null) {
            String label;
            switch (rating) {
                case 1: label = "1 / 5 - Tệ"; break;
                case 2: label = "2 / 5 - Tạm được"; break;
                case 3: label = "3 / 5 - Bình thường"; break;
                case 4: label = "4 / 5 - Rất hài lòng"; break;
                case 5: label = "5 / 5 - Tuyệt vời!"; break;
                default: label = rating + " / 5"; break;
            }
            tvRatingText.setText(label);
        }
        Toast.makeText(this, "Đã chọn " + rating + " sao", Toast.LENGTH_SHORT).show();
    }
}
