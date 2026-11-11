package com.example.dishcision;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
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
import com.example.dishcision.network.ImageLoader;
import com.example.dishcision.network.SearchStoresResponse;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class TodayRestaurantResultActivity extends AppCompatActivity {

    public static final String EXTRA_DISH_NAME = "extra_dish_name";

    private ImageView btnBack;
    private ImageView btnSearchIcon;
    private TextView tvSelectedDishName;
    private EditText edtFilterStores;
    private TextView chipFilterAll;
    private TextView chipFilterRating;
    private TextView chipFilterOpen;
    private TextView tvResultStatus;
    private ProgressBar pbStoreLoading;
    private TextView tvEmptyStores;
    private LinearLayout layoutStoresContainer;

    private String selectedDishName = "";
    private String filterMode = "all";
    private final List<SearchStoresResponse.StoreSearchItem> loadedStores = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_today_restaurant_result);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main_today_restaurant_result), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        ApiClient.init(this);
        initViews();
        setupData();
        setupListeners();
        updateChipStyles();
        fetchMatchingStores();
    }

    private void initViews() {
        btnBack = findViewById(R.id.btnBack);
        btnSearchIcon = findViewById(R.id.btnSearchIcon);
        tvSelectedDishName = findViewById(R.id.tvSelectedDishName);
        edtFilterStores = findViewById(R.id.edtFilterStores);
        chipFilterAll = findViewById(R.id.chipFilterAll);
        chipFilterRating = findViewById(R.id.chipFilterRating);
        chipFilterOpen = findViewById(R.id.chipFilterOpen);
        tvResultStatus = findViewById(R.id.tvResultStatus);
        pbStoreLoading = findViewById(R.id.pbStoreLoading);
        tvEmptyStores = findViewById(R.id.tvEmptyStores);
        layoutStoresContainer = findViewById(R.id.layoutStoresContainer);
    }

    private void setupData() {
        if (getIntent() != null && getIntent().hasExtra(EXTRA_DISH_NAME)) {
            String dishName = getIntent().getStringExtra(EXTRA_DISH_NAME);
            if (dishName != null && !dishName.trim().isEmpty()) {
                selectedDishName = dishName.trim();
            }
        }
        if (tvSelectedDishName != null) {
            tvSelectedDishName.setText(!selectedDishName.isEmpty() ? selectedDishName : "Gợi ý hôm nay");
        }
    }

    private void setupListeners() {
        if (btnBack != null) btnBack.setOnClickListener(v -> finish());

        if (btnSearchIcon != null) {
            btnSearchIcon.setOnClickListener(v -> {
                Intent intent = new Intent(TodayRestaurantResultActivity.this, SearchResultsActivity.class);
                if (!selectedDishName.isEmpty()) intent.putExtra("SEARCH_QUERY", selectedDishName);
                startActivity(intent);
            });
        }

        if (chipFilterAll != null) chipFilterAll.setOnClickListener(v -> switchMode("all"));
        if (chipFilterRating != null) chipFilterRating.setOnClickListener(v -> switchMode("rating"));
        if (chipFilterOpen != null) chipFilterOpen.setOnClickListener(v -> switchMode("open"));

        if (edtFilterStores != null) {
            edtFilterStores.addTextChangedListener(new TextWatcher() {
                @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
                @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                    renderStores();
                }
                @Override public void afterTextChanged(Editable s) {}
            });
        }
    }

    private void switchMode(String mode) {
        filterMode = mode;
        updateChipStyles();
        renderStores();
    }

    private void updateChipStyles() {
        applyChip(chipFilterAll, "all".equals(filterMode));
        applyChip(chipFilterRating, "rating".equals(filterMode));
        applyChip(chipFilterOpen, "open".equals(filterMode));
    }

    private void applyChip(TextView chip, boolean active) {
        if (chip == null) return;
        if (active) {
            chip.setBackgroundResource(R.drawable.bg_chip_active);
            chip.setTextColor(ContextCompat.getColor(this, R.color.white));
            chip.setTypeface(null, Typeface.BOLD);
        } else {
            chip.setBackgroundResource(R.drawable.bg_chip_inactive);
            chip.setTextColor(ContextCompat.getColor(this, R.color.text_dark));
            chip.setTypeface(null, Typeface.NORMAL);
        }
    }

    private void fetchMatchingStores() {
        if (pbStoreLoading != null) pbStoreLoading.setVisibility(View.VISIBLE);
        if (tvEmptyStores != null) tvEmptyStores.setVisibility(View.GONE);
        if (tvResultStatus != null) tvResultStatus.setVisibility(View.GONE);

        String keyword = selectedDishName.isEmpty() ? null : selectedDishName;
        ApiClient.getApiService().searchStores(keyword, 20).enqueue(new Callback<SearchStoresResponse>() {
            @Override
            public void onResponse(@NonNull Call<SearchStoresResponse> call, @NonNull Response<SearchStoresResponse> response) {
                if (isFinishing() || isDestroyed()) return;
                loadedStores.clear();
                if (response.isSuccessful() && response.body() != null && response.body().data != null && !response.body().data.isEmpty()) {
                    if (pbStoreLoading != null) pbStoreLoading.setVisibility(View.GONE);
                    loadedStores.addAll(response.body().data);
                    if (tvResultStatus != null) {
                        tvResultStatus.setText("Tìm thấy " + loadedStores.size() + " quán phục vụ món phù hợp.");
                        tvResultStatus.setVisibility(View.VISIBLE);
                    }
                    renderStores();
                } else if (keyword != null) {
                    fetchAllStoresFallback();
                } else {
                    if (pbStoreLoading != null) pbStoreLoading.setVisibility(View.GONE);
                    renderStores();
                }
            }

            @Override
            public void onFailure(@NonNull Call<SearchStoresResponse> call, @NonNull Throwable t) {
                if (isFinishing() || isDestroyed()) return;
                if (pbStoreLoading != null) pbStoreLoading.setVisibility(View.GONE);
                showEmpty("Không thể tải danh sách quán ăn.");
            }
        });
    }

    private void fetchAllStoresFallback() {
        ApiClient.getApiService().searchStores(null, 20).enqueue(new Callback<SearchStoresResponse>() {
            @Override
            public void onResponse(@NonNull Call<SearchStoresResponse> call, @NonNull Response<SearchStoresResponse> response) {
                if (isFinishing() || isDestroyed()) return;
                if (pbStoreLoading != null) pbStoreLoading.setVisibility(View.GONE);
                loadedStores.clear();
                if (response.isSuccessful() && response.body() != null && response.body().data != null && !response.body().data.isEmpty()) {
                    loadedStores.addAll(response.body().data);
                    if (tvResultStatus != null) {
                        tvResultStatus.setText("Chưa có quán khớp chính xác \"" + selectedDishName + "\", gợi ý các quán đang có trên hệ thống:");
                        tvResultStatus.setVisibility(View.VISIBLE);
                    }
                }
                renderStores();
            }

            @Override
            public void onFailure(@NonNull Call<SearchStoresResponse> call, @NonNull Throwable t) {
                if (isFinishing() || isDestroyed()) return;
                if (pbStoreLoading != null) pbStoreLoading.setVisibility(View.GONE);
                showEmpty("Không thể tải danh sách quán ăn.");
            }
        });
    }

    private void renderStores() {
        String q = edtFilterStores != null && edtFilterStores.getText() != null
                ? edtFilterStores.getText().toString().trim().toLowerCase(Locale.ROOT)
                : "";

        List<SearchStoresResponse.StoreSearchItem> filtered = new ArrayList<>();
        for (SearchStoresResponse.StoreSearchItem store : loadedStores) {
            if (store == null) continue;
            if ("open".equals(filterMode) && !store.isOpen) continue;
            if (!q.isEmpty()) {
                String name = store.name != null ? store.name.toLowerCase(Locale.ROOT) : "";
                String addr = store.address != null ? store.address.toLowerCase(Locale.ROOT) : "";
                if (!name.contains(q) && !addr.contains(q)) continue;
            }
            filtered.add(store);
        }

        if ("rating".equals(filterMode)) {
            Collections.sort(filtered, (a, b) -> Double.compare(b.rating, a.rating));
        }

        if (layoutStoresContainer == null) return;
        layoutStoresContainer.removeAllViews();

        if (filtered.isEmpty()) {
            showEmpty("Chưa có quán ăn phù hợp.");
            return;
        }
        if (tvEmptyStores != null) tvEmptyStores.setVisibility(View.GONE);

        for (int i = 0; i < filtered.size(); i++) {
            layoutStoresContainer.addView(buildStoreCard(filtered.get(i), i > 0));
        }
    }

    private View buildStoreCard(SearchStoresResponse.StoreSearchItem store, boolean addTopMargin) {
        LinearLayout card = new LinearLayout(this);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setBackgroundResource(R.drawable.bg_card);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        if (addTopMargin) lp.topMargin = dp(16);
        card.setLayoutParams(lp);

        RelativeLayout mediaBox = new RelativeLayout(this);
        mediaBox.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, dp(190)));
        mediaBox.setBackgroundColor(Color.parseColor("#E8ECF4"));

        ImageView cover = new ImageView(this);
        cover.setLayoutParams(new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.MATCH_PARENT));
        cover.setScaleType(ImageView.ScaleType.CENTER_CROP);
        cover.setImageResource(R.drawable.logo);
        if (store.avatarUrl != null && !store.avatarUrl.trim().isEmpty()) {
            ImageLoader.loadImage(cover, store.avatarUrl);
        }
        mediaBox.addView(cover);

        TextView statusBadge = new TextView(this);
        statusBadge.setText(store.isOpen ? "🟢 Đang mở cửa" : "Đã đóng cửa");
        statusBadge.setTextColor(store.isOpen ? Color.parseColor("#0A8050") : Color.parseColor("#6B7280"));
        statusBadge.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12);
        statusBadge.setTypeface(null, Typeface.BOLD);
        statusBadge.setBackgroundResource(R.drawable.bg_badge_verified);
        statusBadge.setPadding(dp(10), dp(5), dp(10), dp(5));
        RelativeLayout.LayoutParams statusLp = new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
        statusLp.addRule(RelativeLayout.ALIGN_PARENT_START);
        statusLp.addRule(RelativeLayout.ALIGN_PARENT_TOP);
        statusLp.setMargins(dp(12), dp(12), dp(12), dp(12));
        mediaBox.addView(statusBadge, statusLp);

        LinearLayout bottomBadges = new LinearLayout(this);
        bottomBadges.setOrientation(LinearLayout.HORIZONTAL);
        RelativeLayout.LayoutParams bbLp = new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
        bbLp.addRule(RelativeLayout.ALIGN_PARENT_START);
        bbLp.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
        bbLp.setMargins(dp(12), dp(12), dp(12), dp(12));

        TextView tvRating = new TextView(this);
        tvRating.setText(String.format(Locale.US, "★ %.1f", store.rating));
        tvRating.setTextColor(ContextCompat.getColor(this, R.color.text_dark));
        tvRating.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12);
        tvRating.setTypeface(null, Typeface.BOLD);
        tvRating.setBackgroundResource(R.drawable.bg_badge_rating);
        tvRating.setPadding(dp(10), dp(5), dp(10), dp(5));
        bottomBadges.addView(tvRating);

        if (store.highlightDish != null && !store.highlightDish.trim().isEmpty()) {
            TextView tvHighlight = new TextView(this);
            String label = store.highlightDish.trim();
            if (store.dishPrice != null && !store.dishPrice.trim().isEmpty()) {
                label += " • " + store.dishPrice.trim();
            }
            tvHighlight.setText(label);
            tvHighlight.setTextColor(ContextCompat.getColor(this, R.color.text_dark));
            tvHighlight.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12);
            tvHighlight.setTypeface(null, Typeface.BOLD);
            tvHighlight.setBackgroundResource(R.drawable.bg_badge_rating);
            tvHighlight.setPadding(dp(10), dp(5), dp(10), dp(5));
            LinearLayout.LayoutParams hLp = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            hLp.setMarginStart(dp(8));
            tvHighlight.setLayoutParams(hLp);
            bottomBadges.addView(tvHighlight);
        }
        mediaBox.addView(bottomBadges, bbLp);
        card.addView(mediaBox);

        LinearLayout body = new LinearLayout(this);
        body.setOrientation(LinearLayout.VERTICAL);
        body.setPadding(dp(16), dp(16), dp(16), dp(16));

        TextView tvStoreName = new TextView(this);
        tvStoreName.setText(store.name != null ? store.name : "");
        tvStoreName.setTextColor(ContextCompat.getColor(this, R.color.text_dark));
        tvStoreName.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18);
        tvStoreName.setTypeface(null, Typeface.BOLD);
        body.addView(tvStoreName);

        StringBuilder metaLine = new StringBuilder();
        if (store.address != null && !store.address.trim().isEmpty()) {
            metaLine.append("📍 ").append(store.address.trim());
        }
        if (store.openHours != null && !store.openHours.trim().isEmpty()) {
            if (metaLine.length() > 0) metaLine.append(" • ");
            metaLine.append("⏱ ").append(store.openHours.trim());
        }
        if (metaLine.length() > 0) {
            TextView tvMeta = new TextView(this);
            tvMeta.setText(metaLine.toString());
            tvMeta.setTextColor(ContextCompat.getColor(this, R.color.text_gray));
            tvMeta.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12);
            LinearLayout.LayoutParams mLp = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            mLp.topMargin = dp(4);
            tvMeta.setLayoutParams(mLp);
            body.addView(tvMeta);
        }

        LinearLayout actions = new LinearLayout(this);
        actions.setOrientation(LinearLayout.HORIZONTAL);
        LinearLayout.LayoutParams aLp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, dp(44));
        aLp.topMargin = dp(16);
        actions.setLayoutParams(aLp);

        TextView btnDirections = new TextView(this);
        btnDirections.setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.MATCH_PARENT, 1f));
        ((LinearLayout.LayoutParams) btnDirections.getLayoutParams()).setMarginEnd(dp(6));
        btnDirections.setBackgroundResource(R.drawable.bg_chip_inactive);
        btnDirections.setGravity(Gravity.CENTER);
        btnDirections.setText("Chỉ đường");
        btnDirections.setTextColor(ContextCompat.getColor(this, R.color.text_dark));
        btnDirections.setTextSize(TypedValue.COMPLEX_UNIT_SP, 13);
        btnDirections.setTypeface(null, Typeface.BOLD);
        btnDirections.setClickable(true);
        btnDirections.setFocusable(true);
        btnDirections.setOnClickListener(v -> openMapsForStore(store));
        actions.addView(btnDirections);

        TextView btnDetail = new TextView(this);
        btnDetail.setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.MATCH_PARENT, 1f));
        ((LinearLayout.LayoutParams) btnDetail.getLayoutParams()).setMarginStart(dp(6));
        btnDetail.setBackgroundResource(R.drawable.bg_favorite_search);
        btnDetail.setGravity(Gravity.CENTER);
        btnDetail.setText("Thông tin chi tiết");
        btnDetail.setTextColor(ContextCompat.getColor(this, R.color.primary));
        btnDetail.setTextSize(TypedValue.COMPLEX_UNIT_SP, 13);
        btnDetail.setTypeface(null, Typeface.BOLD);
        btnDetail.setClickable(true);
        btnDetail.setFocusable(true);
        btnDetail.setOnClickListener(v -> openStoreDetail(store.id));
        actions.addView(btnDetail);

        body.addView(actions);
        card.addView(body);
        card.setOnClickListener(v -> openStoreDetail(store.id));
        return card;
    }

    private void openMapsForStore(SearchStoresResponse.StoreSearchItem store) {
        String query = store.address != null && !store.address.trim().isEmpty()
                ? store.name + ", " + store.address.trim()
                : store.name;
        if (query == null || query.trim().isEmpty()) {
            Toast.makeText(this, "Chưa có địa chỉ của quán", Toast.LENGTH_SHORT).show();
            return;
        }
        try {
            Uri uri = Uri.parse("geo:0,0?q=" + Uri.encode(query.trim()));
            Intent mapIntent = new Intent(Intent.ACTION_VIEW, uri);
            startActivity(mapIntent);
        } catch (Exception e) {
            Toast.makeText(this, "Không thể mở ứng dụng bản đồ", Toast.LENGTH_SHORT).show();
        }
    }

    private void openStoreDetail(int storeId) {
        Intent intent = new Intent(TodayRestaurantResultActivity.this, StoreDetailActivity.class);
        intent.putExtra("STORE_ID", storeId);
        startActivity(intent);
    }

    private void showEmpty(String msg) {
        if (layoutStoresContainer != null) layoutStoresContainer.removeAllViews();
        if (tvEmptyStores != null) {
            tvEmptyStores.setText(msg);
            tvEmptyStores.setVisibility(View.VISIBLE);
        }
    }

    private int dp(int value) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, value, getResources().getDisplayMetrics());
    }
}
