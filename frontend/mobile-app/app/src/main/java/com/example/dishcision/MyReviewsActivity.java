package com.example.dishcision;

import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.PopupMenu;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.dishcision.network.ApiClient;
import com.example.dishcision.network.CurrentUserBinder;
import com.example.dishcision.network.ImageLoader;
import com.example.dishcision.network.ReviewsResponse;
import com.example.dishcision.network.TokenManager;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MyReviewsActivity extends AppCompatActivity {

    private ImageView btnBack;
    private LinearLayout navHome;
    private LinearLayout btnSort;
    private LinearLayout btnWriteNewReview;
    private TextView tvSortSelected;
    private TextView tvHeaderTitle;
    private TextView tvSectionTitle;
    private TextView tvModeBadge;
    private TextView tvProfileName;
    private TextView tvUserSubtitle;
    private TextView tvAvgRating;
    private TextView tvStarsSummary;
    private TextView tvTotalReviewsCount;
    private TextView tvPhotoReviewsCount;
    private TextView tvFiveStarCount;

    private TextView chipAll;
    private TextView chipWithPhotos;
    private TextView chipFiveStar;
    private TextView chipFourStar;
    private TextView chipThreeStar;
    private TextView chipOneStar;
    private TextView currentActiveChip;

    private LinearLayout layoutReviewsContainer;
    private TextView tvEmptyReviews;

    private final List<ReviewsResponse.ReviewItem> loadedReviews = new ArrayList<>();
    private String restaurantNameFilter = null;
    private String sortMode = "newest";
    private int currentUserId = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_my_reviews);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main_my_reviews), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        ApiClient.init(this);
        currentUserId = new TokenManager(this).getUserId();
        restaurantNameFilter = getIntent().getStringExtra("RESTAURANT_NAME");

        initViews();
        bindHeaderMode();
        setupListeners();
    }

    @Override
    protected void onResume() {
        super.onResume();
        fetchReviewsFromBackend();
    }

    private void initViews() {
        btnBack = findViewById(R.id.btnBack);
        ImageView btnNotification = findViewById(R.id.btnNotification);
        if (btnNotification != null) {
            btnNotification.setOnClickListener(v -> startActivity(new Intent(this, NotificationsActivity.class)));
        }
        navHome = findViewById(R.id.navHome);
        btnSort = findViewById(R.id.btnSort);
        btnWriteNewReview = findViewById(R.id.btnWriteNewReview);
        tvSortSelected = findViewById(R.id.tvSortSelected);
        tvHeaderTitle = findViewById(R.id.tvHeaderTitle);
        tvSectionTitle = findViewById(R.id.tvSectionTitle);
        tvModeBadge = findViewById(R.id.tvModeBadge);
        tvProfileName = findViewById(R.id.tvProfileName);
        tvUserSubtitle = findViewById(R.id.tvUserSubtitle);
        tvAvgRating = findViewById(R.id.tvAvgRating);
        tvStarsSummary = findViewById(R.id.tvStarsSummary);
        tvTotalReviewsCount = findViewById(R.id.tvTotalReviewsCount);
        tvPhotoReviewsCount = findViewById(R.id.tvPhotoReviewsCount);
        tvFiveStarCount = findViewById(R.id.tvFiveStarCount);

        chipAll = findViewById(R.id.chipAll);
        chipWithPhotos = findViewById(R.id.chipWithPhotos);
        chipFiveStar = findViewById(R.id.chipFiveStar);
        chipFourStar = findViewById(R.id.chipFourStar);
        chipThreeStar = findViewById(R.id.chipThreeStar);
        chipOneStar = findViewById(R.id.chipOneStar);
        currentActiveChip = chipAll;

        layoutReviewsContainer = findViewById(R.id.layoutReviewsContainer);
        tvEmptyReviews = findViewById(R.id.tvEmptyReviews);
    }

    private void bindHeaderMode() {
        if (!TextUtils.isEmpty(restaurantNameFilter)) {
            if (tvHeaderTitle != null) tvHeaderTitle.setText("Đánh giá quán ăn");
            if (tvSectionTitle != null) tvSectionTitle.setText(restaurantNameFilter);
            if (tvModeBadge != null) tvModeBadge.setText("Cộng đồng");
            if (tvProfileName != null) tvProfileName.setText(restaurantNameFilter);
            if (tvUserSubtitle != null) tvUserSubtitle.setText("Đánh giá từ thực khách Dishcision");
        } else {
            CurrentUserBinder.bind(this, findViewById(R.id.imgAvatar), tvProfileName, tvUserSubtitle);
        }
    }

    private void setupListeners() {
        if (btnBack != null) btnBack.setOnClickListener(v -> finish());

        if (navHome != null) {
            navHome.setOnClickListener(v -> {
                Intent intent = new Intent(this, HomeActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                startActivity(intent);
                finish();
            });
        }

        LinearLayout navDecide = findViewById(R.id.navDecide);
        if (navDecide != null) navDecide.setOnClickListener(v -> startActivity(new Intent(this, DecideActivity.class)));

        View navAdd = findViewById(R.id.navAdd);
        if (navAdd != null) navAdd.setOnClickListener(v -> openWriteReview());

        LinearLayout navMessage = findViewById(R.id.navMessage);
        if (navMessage != null) navMessage.setOnClickListener(v -> startActivity(new Intent(this, MessagesInboxActivity.class)));

        LinearLayout navProfile = findViewById(R.id.navProfile);
        if (navProfile != null) navProfile.setOnClickListener(v -> startActivity(new Intent(this, ProfileActivity.class)));

        if (btnSort != null) btnSort.setOnClickListener(v -> showSortDropdownMenu());
        if (btnWriteNewReview != null) btnWriteNewReview.setOnClickListener(v -> openWriteReview());

        if (chipAll != null) chipAll.setOnClickListener(v -> setActiveChip(chipAll));
        if (chipWithPhotos != null) chipWithPhotos.setOnClickListener(v -> setActiveChip(chipWithPhotos));
        if (chipFiveStar != null) chipFiveStar.setOnClickListener(v -> setActiveChip(chipFiveStar));
        if (chipFourStar != null) chipFourStar.setOnClickListener(v -> setActiveChip(chipFourStar));
        if (chipThreeStar != null) chipThreeStar.setOnClickListener(v -> setActiveChip(chipThreeStar));
        if (chipOneStar != null) chipOneStar.setOnClickListener(v -> setActiveChip(chipOneStar));
    }

    private void openWriteReview() {
        Intent intent = new Intent(this, WriteReviewActivity.class);
        if (!TextUtils.isEmpty(restaurantNameFilter)) {
            intent.putExtra("RESTAURANT_NAME", restaurantNameFilter);
        }
        startActivity(intent);
    }

    private void showSortDropdownMenu() {
        if (btnSort == null) return;
        PopupMenu popupMenu = new PopupMenu(this, btnSort);
        popupMenu.getMenu().add("Mới nhất");
        popupMenu.getMenu().add("Cũ nhất");
        popupMenu.getMenu().add("Đánh giá cao nhất");
        popupMenu.getMenu().add("Đánh giá thấp nhất");

        popupMenu.setOnMenuItemClickListener((MenuItem item) -> {
            String title = String.valueOf(item.getTitle());
            if ("Cũ nhất".equals(title)) sortMode = "oldest";
            else if ("Đánh giá cao nhất".equals(title)) sortMode = "highest";
            else if ("Đánh giá thấp nhất".equals(title)) sortMode = "lowest";
            else sortMode = "newest";

            if (tvSortSelected != null) tvSortSelected.setText(title + " ▾");
            renderReviews();
            return true;
        });
        popupMenu.show();
    }

    private void setActiveChip(TextView selectedChip) {
        if (selectedChip == null) return;
        if (currentActiveChip != null) {
            currentActiveChip.setBackgroundResource(R.drawable.bg_chip_inactive);
            currentActiveChip.setTextColor(ContextCompat.getColor(this, R.color.text_dark));
            currentActiveChip.setTypeface(null, Typeface.NORMAL);
        }
        selectedChip.setBackgroundResource(R.drawable.bg_chip_active);
        selectedChip.setTextColor(ContextCompat.getColor(this, R.color.white));
        selectedChip.setTypeface(null, Typeface.BOLD);
        currentActiveChip = selectedChip;
        renderReviews();
    }

    private void fetchReviewsFromBackend() {
        String authorIdParam = TextUtils.isEmpty(restaurantNameFilter) && currentUserId > 0
                ? String.valueOf(currentUserId)
                : null;
        String storeParam = !TextUtils.isEmpty(restaurantNameFilter) ? restaurantNameFilter : null;

        ApiClient.getApiService().getReviews(storeParam, null, authorIdParam)
                .enqueue(new Callback<ReviewsResponse>() {
                    @Override
                    public void onResponse(@NonNull Call<ReviewsResponse> call, @NonNull Response<ReviewsResponse> response) {
                        loadedReviews.clear();
                        if (response.isSuccessful() && response.body() != null && response.body().getData() != null) {
                            loadedReviews.addAll(response.body().getData());
                        }
                        updateSummaryStats();
                        renderReviews();
                    }

                    @Override
                    public void onFailure(@NonNull Call<ReviewsResponse> call, @NonNull Throwable t) {
                        loadedReviews.clear();
                        updateSummaryStats();
                        renderReviews();
                        Toast.makeText(MyReviewsActivity.this, "Không thể tải đánh giá: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void updateSummaryStats() {
        int total = loadedReviews.size();
        int withPhotos = 0;
        int fiveStars = 0;
        int fourStars = 0;
        int threeStars = 0;
        int lowStars = 0;
        double sumRating = 0;

        for (ReviewsResponse.ReviewItem item : loadedReviews) {
            sumRating += item.rating;
            if (item.images != null && !item.images.isEmpty()) withPhotos++;
            if (item.rating >= 5) fiveStars++;
            else if (item.rating == 4) fourStars++;
            else if (item.rating == 3) threeStars++;
            else lowStars++;
        }

        double avg = total > 0 ? (sumRating / total) : 0.0;
        if (tvAvgRating != null) tvAvgRating.setText(String.format(Locale.US, "%.1f", avg));
        if (tvStarsSummary != null) tvStarsSummary.setText(buildStarsString((int) Math.round(avg)));
        if (tvTotalReviewsCount != null) tvTotalReviewsCount.setText(String.valueOf(total));
        if (tvPhotoReviewsCount != null) tvPhotoReviewsCount.setText(String.valueOf(withPhotos));
        if (tvFiveStarCount != null) tvFiveStarCount.setText(String.valueOf(fiveStars));

        if (chipAll != null) chipAll.setText("Tất cả (" + total + ")");
        if (chipWithPhotos != null) chipWithPhotos.setText("Có ảnh (" + withPhotos + ")");
        if (chipFiveStar != null) chipFiveStar.setText("5 sao (" + fiveStars + ")");
        if (chipFourStar != null) chipFourStar.setText("4 sao (" + fourStars + ")");
        if (chipThreeStar != null) chipThreeStar.setText("3 sao (" + threeStars + ")");
        if (chipOneStar != null) chipOneStar.setText("1–2 sao (" + lowStars + ")");
    }

    private String buildStarsString(int filledCount) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 5; i++) {
            sb.append(i < filledCount ? "★" : "☆");
        }
        return sb.toString();
    }

    private void renderReviews() {
        if (layoutReviewsContainer == null) return;
        layoutReviewsContainer.removeAllViews();

        List<ReviewsResponse.ReviewItem> filtered = new ArrayList<>();
        for (ReviewsResponse.ReviewItem item : loadedReviews) {
            if (currentActiveChip == chipWithPhotos && (item.images == null || item.images.isEmpty())) continue;
            if (currentActiveChip == chipFiveStar && item.rating != 5) continue;
            if (currentActiveChip == chipFourStar && item.rating != 4) continue;
            if (currentActiveChip == chipThreeStar && item.rating != 3) continue;
            if (currentActiveChip == chipOneStar && item.rating > 2) continue;
            filtered.add(item);
        }

        if ("oldest".equals(sortMode)) {
            Collections.sort(filtered, (a, b) -> safeStr(a.createdAt).compareTo(safeStr(b.createdAt)));
        } else if ("highest".equals(sortMode)) {
            Collections.sort(filtered, (a, b) -> Integer.compare(b.rating, a.rating));
        } else if ("lowest".equals(sortMode)) {
            Collections.sort(filtered, (a, b) -> Integer.compare(a.rating, b.rating));
        } else {
            Collections.sort(filtered, (a, b) -> safeStr(b.createdAt).compareTo(safeStr(a.createdAt)));
        }

        if (filtered.isEmpty()) {
            if (tvEmptyReviews != null) tvEmptyReviews.setVisibility(View.VISIBLE);
            return;
        }
        if (tvEmptyReviews != null) tvEmptyReviews.setVisibility(View.GONE);

        for (ReviewsResponse.ReviewItem item : filtered) {
            layoutReviewsContainer.addView(buildReviewCard(item));
        }
    }

    private View buildReviewCard(ReviewsResponse.ReviewItem item) {
        LinearLayout card = new LinearLayout(this);
        LinearLayout.LayoutParams cardParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        cardParams.bottomMargin = dpToPx(12);
        card.setLayoutParams(cardParams);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setBackgroundResource(R.drawable.bg_card_white);
        card.setPadding(dpToPx(16), dpToPx(16), dpToPx(16), dpToPx(16));

        LinearLayout header = new LinearLayout(this);
        header.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
        header.setGravity(Gravity.CENTER_VERTICAL);
        header.setOrientation(LinearLayout.HORIZONTAL);

        ImageView icon = new ImageView(this);
        LinearLayout.LayoutParams iconParams = new LinearLayout.LayoutParams(dpToPx(42), dpToPx(42));
        icon.setLayoutParams(iconParams);
        icon.setBackgroundResource(R.drawable.bg_tag_chip_pink);
        int pad = dpToPx(9);
        icon.setPadding(pad, pad, pad, pad);
        icon.setImageResource(R.drawable.ic_utensils);
        icon.setColorFilter(Color.parseColor("#5B403A"));
        header.addView(icon);

        LinearLayout titleCol = new LinearLayout(this);
        LinearLayout.LayoutParams titleColParams = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f);
        titleColParams.setMarginStart(dpToPx(12));
        titleCol.setLayoutParams(titleColParams);
        titleCol.setOrientation(LinearLayout.VERTICAL);

        TextView tvStore = new TextView(this);
        tvStore.setText(!TextUtils.isEmpty(item.restaurantName) ? item.restaurantName : "Quán ăn");
        tvStore.setTextColor(Color.parseColor("#141B2B"));
        tvStore.setTextSize(15f);
        tvStore.setTypeface(null, Typeface.BOLD);
        titleCol.addView(tvStore);

        String meta = "";
        if (item.author != null && !TextUtils.isEmpty(item.author.fullName)) {
            meta = item.author.fullName;
        }
        String formattedDate = formatDate(item.createdAt);
        if (!formattedDate.isEmpty()) {
            meta = meta.isEmpty() ? formattedDate : meta + " • " + formattedDate;
        }
        if (!meta.isEmpty()) {
            TextView tvMeta = new TextView(this);
            tvMeta.setText(meta);
            tvMeta.setTextColor(Color.parseColor("#6B7280"));
            tvMeta.setTextSize(12f);
            titleCol.addView(tvMeta);
        }
        header.addView(titleCol);

        TextView tvScore = new TextView(this);
        tvScore.setText("★ " + item.rating + ".0");
        tvScore.setBackgroundResource(R.drawable.bg_rating_score_badge);
        tvScore.setPadding(dpToPx(10), dpToPx(4), dpToPx(10), dpToPx(4));
        tvScore.setTextColor(Color.parseColor("#FF8A00"));
        tvScore.setTextSize(13f);
        tvScore.setTypeface(null, Typeface.BOLD);
        header.addView(tvScore);

        if (item.author != null && currentUserId > 0 && item.author.userId == currentUserId) {
            ImageView btnDelete = new ImageView(this);
            LinearLayout.LayoutParams delParams = new LinearLayout.LayoutParams(dpToPx(28), dpToPx(28));
            delParams.setMarginStart(dpToPx(8));
            btnDelete.setLayoutParams(delParams);
            btnDelete.setPadding(dpToPx(4), dpToPx(4), dpToPx(4), dpToPx(4));
            btnDelete.setImageResource(R.drawable.ic_trash);
            btnDelete.setColorFilter(Color.parseColor("#8E7570"));
            btnDelete.setClickable(true);
            btnDelete.setFocusable(true);
            btnDelete.setOnClickListener(v -> confirmDeleteReview(item));
            header.addView(btnDelete);
        }

        card.addView(header);

        if (item.tags != null && !item.tags.isEmpty()) {
            LinearLayout tagsRow = new LinearLayout(this);
            LinearLayout.LayoutParams tagsParams = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            tagsParams.topMargin = dpToPx(10);
            tagsRow.setLayoutParams(tagsParams);
            tagsRow.setOrientation(LinearLayout.HORIZONTAL);

            int maxTags = Math.min(item.tags.size(), 3);
            for (int i = 0; i < maxTags; i++) {
                TextView tagView = new TextView(this);
                LinearLayout.LayoutParams tagLp = new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                if (i > 0) tagLp.setMarginStart(dpToPx(6));
                tagView.setLayoutParams(tagLp);
                tagView.setBackgroundResource(R.drawable.bg_chip_preference);
                tagView.setPadding(dpToPx(10), dpToPx(5), dpToPx(10), dpToPx(5));
                tagView.setText(item.tags.get(i));
                tagView.setTextColor(Color.parseColor("#141B2B"));
                tagView.setTextSize(12f);
                tagsRow.addView(tagView);
            }
            card.addView(tagsRow);
        }

        if (item.images != null && !item.images.isEmpty() && !TextUtils.isEmpty(item.images.get(0).imageUrl)) {
            ImageView imgPhoto = new ImageView(this);
            LinearLayout.LayoutParams imgParams = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT, dpToPx(180));
            imgParams.topMargin = dpToPx(10);
            imgPhoto.setLayoutParams(imgParams);
            imgPhoto.setScaleType(ImageView.ScaleType.CENTER_CROP);
            imgPhoto.setBackgroundResource(R.drawable.bg_image_thumbnail);
            ImageLoader.loadImage(imgPhoto, item.images.get(0).imageUrl);
            card.addView(imgPhoto);
        }

        if (!TextUtils.isEmpty(item.content)) {
            TextView tvBody = new TextView(this);
            LinearLayout.LayoutParams bodyParams = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            bodyParams.topMargin = dpToPx(10);
            tvBody.setLayoutParams(bodyParams);
            tvBody.setLineSpacing(0f, 1.2f);
            tvBody.setText(item.content);
            tvBody.setTextColor(Color.parseColor("#505568"));
            tvBody.setTextSize(14f);
            card.addView(tvBody);
        }

        if (!TextUtils.isEmpty(item.restaurantName)) {
            tvStore.setOnClickListener(v -> {
                Intent intent = new Intent(this, StoreDetailActivity.class);
                intent.putExtra("STORE_NAME", item.restaurantName);
                startActivity(intent);
            });
        }

        return card;
    }

    private void confirmDeleteReview(ReviewsResponse.ReviewItem item) {
        new AlertDialog.Builder(this)
                .setTitle("Xóa đánh giá")
                .setMessage("Bạn có chắc muốn xóa đánh giá tại " + safeStr(item.restaurantName) + "?")
                .setNegativeButton("Hủy", null)
                .setPositiveButton("Xóa", (d, w) -> deleteReview(item.reviewId))
                .show();
    }

    private void deleteReview(int reviewId) {
        String authHeader = ApiClient.getAuthHeader(this);
        if (authHeader.isEmpty()) {
            Toast.makeText(this, "Vui lòng đăng nhập lại", Toast.LENGTH_SHORT).show();
            return;
        }
        ApiClient.getApiService().deleteReview(authHeader, reviewId).enqueue(new Callback<Object>() {
            @Override
            public void onResponse(@NonNull Call<Object> call, @NonNull Response<Object> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(MyReviewsActivity.this, "Đã xóa đánh giá", Toast.LENGTH_SHORT).show();
                    fetchReviewsFromBackend();
                } else {
                    Toast.makeText(MyReviewsActivity.this, "Không thể xóa đánh giá (mã " + response.code() + ")", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<Object> call, @NonNull Throwable t) {
                Toast.makeText(MyReviewsActivity.this, "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private String formatDate(String iso) {
        if (TextUtils.isEmpty(iso)) return "";
        if (iso.length() >= 10) return iso.substring(0, 10);
        return iso;
    }

    private String safeStr(String s) {
        return s != null ? s : "";
    }

    private int dpToPx(int dp) {
        return Math.round(dp * getResources().getDisplayMetrics().density);
    }
}
