package com.example.dishcision;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.view.Gravity;
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
import com.example.dishcision.network.AuthResponse;
import com.example.dishcision.network.FollowingRestaurantsResponse;
import com.example.dishcision.network.ImageLoader;
import com.example.dishcision.network.ReviewsResponse;
import com.example.dishcision.network.StoreDetailResponse;

import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class StoreDetailActivity extends AppCompatActivity {

    private ImageView btnBack;
    private ImageView imgStoreCover;
    private TextView tvStoreHandle;
    private TextView tvStoreName;
    private TextView tvStoreRating;
    private TextView tvStoreAddress;
    private TextView tvStoreHours;
    private TextView btnFollowStore;
    private RelativeLayout btnViewMenu;
    private TextView btnDirections;
    private TextView btnShareStore;
    private TextView tvStoreBio;
    private View layoutHighlightDish;
    private TextView tvHighlightDishName;
    private TextView tvHighlightDishDesc;
    private TextView tvHighlightDishPrice;
    private LinearLayout layoutReviewsContainer;
    private TextView tvEmptyReviews;
    private TextView btnViewAllReviews;
    private RelativeLayout btnWriteReview;

    private int storeId = 0;
    private String initialStoreName = "";
    private StoreDetailResponse.StoreDetailData currentStore;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_store_detail);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main_store_detail), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        ApiClient.init(this);
        storeId = getIntent().getIntExtra("STORE_ID", getIntent().getIntExtra("store_id", 0));
        initialStoreName = getIntent().getStringExtra("STORE_NAME");

        initViews();
        setupListeners();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (storeId > 0) {
            fetchStoreDetail(storeId);
        } else if (initialStoreName != null && !initialStoreName.trim().isEmpty()) {
            resolveStoreByName(initialStoreName.trim());
        } else {
            Toast.makeText(this, "Không tìm thấy thông tin quán ăn", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private void initViews() {
        btnBack = findViewById(R.id.btnBack);
        imgStoreCover = findViewById(R.id.imgStoreCover);
        tvStoreHandle = findViewById(R.id.tvStoreHandle);
        tvStoreName = findViewById(R.id.tvStoreName);
        tvStoreRating = findViewById(R.id.tvStoreRating);
        tvStoreAddress = findViewById(R.id.tvStoreAddress);
        tvStoreHours = findViewById(R.id.tvStoreHours);
        btnFollowStore = findViewById(R.id.btnFollowStore);
        btnViewMenu = findViewById(R.id.btnViewMenu);
        btnDirections = findViewById(R.id.btnDirections);
        btnShareStore = findViewById(R.id.btnShareStore);
        tvStoreBio = findViewById(R.id.tvStoreBio);
        layoutHighlightDish = findViewById(R.id.layoutHighlightDish);
        tvHighlightDishName = findViewById(R.id.tvHighlightDishName);
        tvHighlightDishDesc = findViewById(R.id.tvHighlightDishDesc);
        tvHighlightDishPrice = findViewById(R.id.tvHighlightDishPrice);
        layoutReviewsContainer = findViewById(R.id.layoutReviewsContainer);
        tvEmptyReviews = findViewById(R.id.tvEmptyReviews);
        btnViewAllReviews = findViewById(R.id.btnViewAllReviews);
        btnWriteReview = findViewById(R.id.btnWriteReview);
    }

    private void setupListeners() {
        if (btnBack != null) btnBack.setOnClickListener(v -> finish());

        if (btnViewMenu != null) {
            btnViewMenu.setOnClickListener(v -> {
                if (currentStore != null) {
                    StoreMenuBottomSheetHelper.showWithData(this, currentStore);
                } else if (storeId > 0) {
                    StoreMenuBottomSheetHelper.showByStoreId(this, storeId, initialStoreName);
                }
            });
        }

        if (btnFollowStore != null) {
            btnFollowStore.setOnClickListener(v -> toggleFollow());
        }

        if (btnDirections != null) {
            btnDirections.setOnClickListener(v -> openDirections());
        }

        if (btnShareStore != null) {
            btnShareStore.setOnClickListener(v -> shareStore());
        }

        if (btnViewAllReviews != null) {
            btnViewAllReviews.setOnClickListener(v -> {
                Intent intent = new Intent(this, MyReviewsActivity.class);
                if (currentStore != null && currentStore.name != null) {
                    intent.putExtra("RESTAURANT_NAME", currentStore.name);
                }
                startActivity(intent);
            });
        }

        if (btnWriteReview != null) {
            btnWriteReview.setOnClickListener(v -> {
                Intent intent = new Intent(StoreDetailActivity.this, WriteReviewActivity.class);
                if (currentStore != null) {
                    intent.putExtra("RESTAURANT_NAME", currentStore.name);
                    intent.putExtra("RESTAURANT_LOCATION", currentStore.address);
                }
                startActivity(intent);
            });
        }
    }

    private void resolveStoreByName(String name) {
        String authHeader = ApiClient.getAuthHeader(this);
        ApiClient.getApiService().getStores(authHeader, name).enqueue(new Callback<FollowingRestaurantsResponse>() {
            @Override
            public void onResponse(@NonNull Call<FollowingRestaurantsResponse> call, @NonNull Response<FollowingRestaurantsResponse> response) {
                if (isFinishing()) return;
                if (response.isSuccessful() && response.body() != null && response.body().getData() != null
                        && response.body().getData().restaurants != null && !response.body().getData().restaurants.isEmpty()) {
                    storeId = response.body().getData().restaurants.get(0).id;
                    fetchStoreDetail(storeId);
                } else {
                    Toast.makeText(StoreDetailActivity.this, "Không tìm thấy thông tin quán ăn", Toast.LENGTH_SHORT).show();
                    finish();
                }
            }

            @Override
            public void onFailure(@NonNull Call<FollowingRestaurantsResponse> call, @NonNull Throwable t) {
                if (isFinishing()) return;
                Toast.makeText(StoreDetailActivity.this, "Lỗi kết nối máy chủ", Toast.LENGTH_SHORT).show();
                finish();
            }
        });
    }

    private void fetchStoreDetail(int id) {
        String authHeader = ApiClient.getAuthHeader(this);
        ApiClient.getApiService().getStoreDetail(authHeader, id).enqueue(new Callback<StoreDetailResponse>() {
            @Override
            public void onResponse(@NonNull Call<StoreDetailResponse> call, @NonNull Response<StoreDetailResponse> response) {
                if (isFinishing()) return;
                if (response.isSuccessful() && response.body() != null && response.body().data != null) {
                    currentStore = response.body().data;
                    bindStoreData(currentStore);
                } else {
                    Toast.makeText(StoreDetailActivity.this, "Không thể tải thông tin quán (mã " + response.code() + ")", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<StoreDetailResponse> call, @NonNull Throwable t) {
                if (isFinishing()) return;
                Toast.makeText(StoreDetailActivity.this, "Lỗi kết nối máy chủ", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void bindStoreData(StoreDetailResponse.StoreDetailData store) {
        String cover = (store.coverUrl != null && !store.coverUrl.isEmpty()) ? store.coverUrl : store.avatarUrl;
        if (imgStoreCover != null && cover != null && !cover.isEmpty()) {
            ImageLoader.loadImage(imgStoreCover, cover);
        }

        if (tvStoreHandle != null) {
            String handleText = (store.username != null && !store.username.isEmpty())
                    ? "@" + store.username
                    : "QUÁN ĂN TRÊN DISHCISION";
            tvStoreHandle.setText(handleText + " • " + store.followersCount + " người theo dõi");
        }
        if (tvStoreName != null) tvStoreName.setText(store.name != null ? store.name : "");

        if (tvStoreRating != null) {
            if (store.rating > 0) {
                tvStoreRating.setText(String.format(Locale.US, "★ %.1f • %d đánh giá", store.rating, store.reviewsCount));
            } else {
                tvStoreRating.setText(store.reviewsCount + " đánh giá");
            }
        }

        if (tvStoreAddress != null) {
            if (store.address != null && !store.address.isEmpty()) {
                tvStoreAddress.setText("" + store.address);
                tvStoreAddress.setVisibility(View.VISIBLE);
            } else {
                tvStoreAddress.setVisibility(View.GONE);
            }
        }

        if (tvStoreHours != null) {
            String status = store.isOpen ? "Đang mở cửa" : "Đã đóng cửa";
            String hours = (store.openHours != null && !store.openHours.isEmpty()) ? " " + store.openHours : "";
            tvStoreHours.setText("⏱ " + status + hours);
        }

        if (btnFollowStore != null) {
            btnFollowStore.setText(store.isFollowing ? "Đang theo dõi" : "+ Theo dõi");
        }

        if (tvStoreBio != null) {
            tvStoreBio.setText((store.bio != null && !store.bio.trim().isEmpty())
                    ? store.bio.trim()
                    : "Khám phá thực đơn và đánh giá từ cộng đồng Dishcision.");
        }

        if (layoutHighlightDish != null) {
            if (store.highlightDish != null && !store.highlightDish.trim().isEmpty()) {
                layoutHighlightDish.setVisibility(View.VISIBLE);
                if (tvHighlightDishName != null) tvHighlightDishName.setText("♨ " + store.highlightDish.trim());
                if (tvHighlightDishDesc != null) tvHighlightDishDesc.setText(store.dishDescription != null ? store.dishDescription : "");
                if (tvHighlightDishPrice != null) tvHighlightDishPrice.setText(store.dishPrice != null ? store.dishPrice : "");

                int highlightDishId = 0;
                if (store.menu != null) {
                    for (StoreDetailResponse.MenuDishItem d : store.menu) {
                        if (d.name != null && d.name.equalsIgnoreCase(store.highlightDish.trim())) {
                            highlightDishId = d.id;
                            break;
                        }
                    }
                }
                if (highlightDishId > 0) {
                    int targetDishId = highlightDishId;
                    layoutHighlightDish.setOnClickListener(v -> {
                        Intent intent = new Intent(this, DishDetailActivity.class);
                        intent.putExtra("DISH_ID", targetDishId);
                        startActivity(intent);
                    });
                }
            } else {
                layoutHighlightDish.setVisibility(View.GONE);
            }
        }

        renderReviews(store.reviews);
    }

    private void renderReviews(List<ReviewsResponse.ReviewItem> reviews) {
        if (layoutReviewsContainer == null) return;
        layoutReviewsContainer.removeAllViews();

        if (reviews == null || reviews.isEmpty()) {
            if (tvEmptyReviews != null) tvEmptyReviews.setVisibility(View.VISIBLE);
            if (btnViewAllReviews != null) btnViewAllReviews.setVisibility(View.GONE);
            return;
        }

        if (tvEmptyReviews != null) tvEmptyReviews.setVisibility(View.GONE);
        if (btnViewAllReviews != null) btnViewAllReviews.setVisibility(View.VISIBLE);

        int limit = Math.min(3, reviews.size());
        for (int i = 0; i < limit; i++) {
            layoutReviewsContainer.addView(buildReviewCard(reviews.get(i)));
        }
    }

    private View buildReviewCard(ReviewsResponse.ReviewItem rev) {
        return ReviewCardHelper.build(this, rev);
    }

    private void toggleFollow() {
        if (currentStore == null) return;
        String authHeader = ApiClient.getAuthHeader(this);
        if (authHeader.isEmpty()) {
            Toast.makeText(this, "Vui lòng đăng nhập để theo dõi quán", Toast.LENGTH_SHORT).show();
            return;
        }

        btnFollowStore.setEnabled(false);
        Call<AuthResponse> call = currentStore.isFollowing
                ? ApiClient.getApiService().unfollowUser(authHeader, String.valueOf(currentStore.id))
                : ApiClient.getApiService().followUser(authHeader, String.valueOf(currentStore.id));

        call.enqueue(new Callback<AuthResponse>() {
            @Override
            public void onResponse(@NonNull Call<AuthResponse> call, @NonNull Response<AuthResponse> response) {
                btnFollowStore.setEnabled(true);
                if (response.isSuccessful() && currentStore != null) {
                    currentStore.isFollowing = !currentStore.isFollowing;
                    currentStore.followersCount = Math.max(0, currentStore.followersCount + (currentStore.isFollowing ? 1 : -1));
                    bindStoreData(currentStore);
                } else {
                    Toast.makeText(StoreDetailActivity.this, "Không thể cập nhật trạng thái theo dõi", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<AuthResponse> call, @NonNull Throwable t) {
                btnFollowStore.setEnabled(true);
                Toast.makeText(StoreDetailActivity.this, "Lỗi kết nối máy chủ", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void openDirections() {
        if (currentStore == null || currentStore.address == null || currentStore.address.trim().isEmpty()) {
            Toast.makeText(this, "Quán chưa có địa chỉ để chỉ đường", Toast.LENGTH_SHORT).show();
            return;
        }
        String query = (currentStore.name != null ? currentStore.name + ", " : "") + currentStore.address;
        Intent mapIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("geo:0,0?q=" + Uri.encode(query)));
        try {
            startActivity(mapIntent);
        } catch (Exception e) {
            Toast.makeText(this, "Không tìm thấy ứng dụng bản đồ", Toast.LENGTH_SHORT).show();
        }
    }

    private void shareStore() {
        if (currentStore == null) return;
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("text/plain");
        String text = currentStore.name
                + (currentStore.address != null && !currentStore.address.isEmpty() ? " - " + currentStore.address : "")
                + "\nhttps://dishcision.duckdns.org/stores/" + currentStore.id;
        shareIntent.putExtra(Intent.EXTRA_TEXT, text);
        startActivity(Intent.createChooser(shareIntent, "Chia sẻ quán ăn"));
    }

    private int dpToPx(int dp) {
        return Math.round(dp * getResources().getDisplayMetrics().density);
    }
}
