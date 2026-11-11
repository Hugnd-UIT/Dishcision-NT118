package com.example.dishcision;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.dishcision.network.ApiClient;
import com.example.dishcision.network.DishDetailResponse;
import com.example.dishcision.network.ImageLoader;

import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class DishDetailActivity extends AppCompatActivity {

    private ImageView btnBack;
    private ImageView imgDishPhoto;
    private TextView tvDishCategory;
    private TextView tvDishName;
    private TextView tvDishPrice;
    private TextView tvDishDesc;
    private TextView btnSaveDish;
    private TextView tvServeStoreName;
    private TextView tvServeStoreAddress;
    private TextView tvServeStoreRating;
    private RelativeLayout btnViewStoreMenu;
    private TextView btnGoToStoreDetail;
    private LinearLayout layoutDishReviewsContainer;
    private TextView tvEmptyDishReviews;

    private int dishId = 0;
    private DishDetailResponse.DishDetailData currentDish;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_dish_detail);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main_dish_detail), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        ApiClient.init(this);
        dishId = getIntent().getIntExtra("DISH_ID", getIntent().getIntExtra("dish_id", 0));

        initViews();
        setupListeners();

        if (dishId > 0) {
            fetchDishDetail(dishId);
        } else {
            Toast.makeText(this, "Không tìm thấy thông tin món ăn", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private void initViews() {
        btnBack = findViewById(R.id.btnBack);
        imgDishPhoto = findViewById(R.id.imgDishPhoto);
        tvDishCategory = findViewById(R.id.tvDishCategory);
        tvDishName = findViewById(R.id.tvDishName);
        tvDishPrice = findViewById(R.id.tvDishPrice);
        tvDishDesc = findViewById(R.id.tvDishDesc);
        btnSaveDish = findViewById(R.id.btnSaveDish);
        tvServeStoreName = findViewById(R.id.tvServeStoreName);
        tvServeStoreAddress = findViewById(R.id.tvServeStoreAddress);
        tvServeStoreRating = findViewById(R.id.tvServeStoreRating);
        btnViewStoreMenu = findViewById(R.id.btnViewStoreMenu);
        btnGoToStoreDetail = findViewById(R.id.btnGoToStoreDetail);
        layoutDishReviewsContainer = findViewById(R.id.layoutDishReviewsContainer);
        tvEmptyDishReviews = findViewById(R.id.tvEmptyDishReviews);
    }

    private void setupListeners() {
        if (btnBack != null) btnBack.setOnClickListener(v -> finish());

        if (btnViewStoreMenu != null) {
            btnViewStoreMenu.setOnClickListener(v -> {
                if (currentDish != null && currentDish.storeId > 0) {
                    StoreMenuBottomSheetHelper.showByStoreId(this, currentDish.storeId, currentDish.storeName);
                }
            });
        }

        if (btnGoToStoreDetail != null) {
            btnGoToStoreDetail.setOnClickListener(v -> {
                if (currentDish != null && currentDish.storeId > 0) {
                    Intent intent = new Intent(DishDetailActivity.this, StoreDetailActivity.class);
                    intent.putExtra("STORE_ID", currentDish.storeId);
                    intent.putExtra("STORE_NAME", currentDish.storeName);
                    startActivity(intent);
                }
            });
        }

        if (btnSaveDish != null) {
            btnSaveDish.setOnClickListener(v -> {
                if (currentDish != null && currentDish.name != null && !currentDish.name.isEmpty()) {
                    Intent intent = new Intent(this, SearchResultsActivity.class);
                    intent.putExtra("SEARCH_QUERY", currentDish.name);
                    startActivity(intent);
                }
            });
        }
    }

    private void fetchDishDetail(int id) {
        ApiClient.getApiService().getDishDetail(id).enqueue(new Callback<DishDetailResponse>() {
            @Override
            public void onResponse(@NonNull Call<DishDetailResponse> call, @NonNull Response<DishDetailResponse> response) {
                if (isFinishing()) return;
                if (response.isSuccessful() && response.body() != null && response.body().data != null) {
                    currentDish = response.body().data;
                    bindDish(currentDish);
                } else {
                    Toast.makeText(DishDetailActivity.this, "Không thể tải chi tiết món (mã " + response.code() + ")", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<DishDetailResponse> call, @NonNull Throwable t) {
                if (isFinishing()) return;
                Toast.makeText(DishDetailActivity.this, "Lỗi kết nối máy chủ", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void bindDish(DishDetailResponse.DishDetailData dish) {
        if (imgDishPhoto != null && dish.imageUrl != null && !dish.imageUrl.isEmpty()) {
            ImageLoader.loadImage(imgDishPhoto, dish.imageUrl);
        }
        if (tvDishCategory != null) {
            tvDishCategory.setText(dish.category != null && !dish.category.isEmpty()
                    ? dish.category.toUpperCase(Locale.ROOT)
                    : "MÓN ĐẶC TRƯNG");
        }
        if (tvDishName != null) tvDishName.setText(dish.name != null ? dish.name : "");
        if (tvDishPrice != null) tvDishPrice.setText(dish.formattedPrice != null ? dish.formattedPrice : "");
        if (tvDishDesc != null) tvDishDesc.setText(dish.description != null ? dish.description : "");

        if (tvServeStoreName != null) tvServeStoreName.setText(dish.storeName != null ? dish.storeName : "");
        if (tvServeStoreAddress != null) {
            tvServeStoreAddress.setText(dish.storeAddress != null && !dish.storeAddress.isEmpty()
                    ? "📍 " + dish.storeAddress : "");
        }
        if (tvServeStoreRating != null) {
            tvServeStoreRating.setText(String.format(Locale.US, "★ %.1f điểm quán • %d đánh giá",
                    dish.storeRating, dish.storeReviewsCount));
        }

        if (layoutDishReviewsContainer == null) return;
        layoutDishReviewsContainer.removeAllViews();

        if (dish.reviews == null || dish.reviews.isEmpty()) {
            if (tvEmptyDishReviews != null) tvEmptyDishReviews.setVisibility(View.VISIBLE);
            return;
        }
        if (tvEmptyDishReviews != null) tvEmptyDishReviews.setVisibility(View.GONE);

        float density = getResources().getDisplayMetrics().density;
        for (DishDetailResponse.DishReviewItem rev : dish.reviews) {
            TextView tv = new TextView(this);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            params.topMargin = Math.round(8 * density);
            tv.setLayoutParams(params);
            tv.setBackgroundResource(R.drawable.bg_info_box);
            int pad = Math.round(12 * density);
            tv.setPadding(pad, pad, pad, pad);
            String author = (rev.authorName != null && !rev.authorName.isEmpty()) ? rev.authorName : "Thực khách";
            tv.setText(author + " (" + rev.rating + "★): " + (rev.content != null ? rev.content : ""));
            tv.setTextColor(ContextCompat.getColor(this, R.color.text_dark));
            tv.setTextSize(13f);
            layoutDishReviewsContainer.addView(tv);
        }
    }
}
