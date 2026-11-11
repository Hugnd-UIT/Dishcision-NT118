package com.example.dishcision;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.dishcision.network.ApiClient;
import com.example.dishcision.network.AuthHelper;
import com.example.dishcision.network.DishModel;
import com.example.dishcision.network.FeedPostsResponse;
import com.example.dishcision.network.ImageLoader;
import com.example.dishcision.network.SearchAccountsResponse;
import com.example.dishcision.network.SearchDishesResponse;
import com.example.dishcision.network.SearchStoresResponse;
import com.google.android.material.bottomsheet.BottomSheetDialog;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SearchResultsActivity extends AppCompatActivity {

    private enum TabType {
        DISHES, STORES, USERS, POSTS
    }

    private ImageView btnBack;
    private ImageView imgSearchIcon;
    private EditText edtSearchQuery;
    private ImageView btnClearSearch;
    private LinearLayout btnOpenFilter;
    private TextView tvFilterLabel;
    private TextView tvResultHeader;
    private ProgressBar pbSearchLoading;
    private TextView tvEmptyResults;
    private LinearLayout layoutResultsContainer;

    private TextView tabDishes;
    private TextView tabRestaurants;
    private TextView tabUsers;
    private TextView tabPosts;

    private TabType currentTab = TabType.DISHES;
    private String sortMode = "default";
    private int priceBand = 0;
    private double minRating = 0.0;

    private final List<DishModel> loadedDishes = new ArrayList<>();
    private final List<SearchStoresResponse.StoreSearchItem> loadedStores = new ArrayList<>();
    private final List<FeedPostsResponse.Author> loadedUsers = new ArrayList<>();
    private final List<FeedPostsResponse.PostItem> loadedPosts = new ArrayList<>();

    private final Handler searchHandler = new Handler(Looper.getMainLooper());
    private Runnable pendingSearchRunnable;
    private boolean suppressTextWatcher = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_search_results);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main_search_results), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        ApiClient.init(this);
        initViews();
        setupListeners();
        updateTabStyles();
        updateFilterIndicator();
        performSearch();
    }

    private void initViews() {
        btnBack = findViewById(R.id.btnBack);
        imgSearchIcon = findViewById(R.id.imgSearchIcon);
        edtSearchQuery = findViewById(R.id.edtSearchQuery);
        btnClearSearch = findViewById(R.id.btnClearSearch);
        btnOpenFilter = findViewById(R.id.btnOpenFilter);
        tvFilterLabel = findViewById(R.id.tvFilterLabel);
        tvResultHeader = findViewById(R.id.tvResultHeader);
        pbSearchLoading = findViewById(R.id.pbSearchLoading);
        tvEmptyResults = findViewById(R.id.tvEmptyResults);
        layoutResultsContainer = findViewById(R.id.layoutResultsContainer);

        tabDishes = findViewById(R.id.tabDishes);
        tabRestaurants = findViewById(R.id.tabRestaurants);
        tabUsers = findViewById(R.id.tabUsers);
        tabPosts = findViewById(R.id.tabPosts);

        String query = getIntent().getStringExtra("SEARCH_QUERY");
        if (edtSearchQuery != null && query != null && !query.trim().isEmpty()) {
            suppressTextWatcher = true;
            edtSearchQuery.setText(query.trim());
            edtSearchQuery.setSelection(edtSearchQuery.getText().length());
            suppressTextWatcher = false;
        }
    }

    private void setupListeners() {
        if (btnBack != null) btnBack.setOnClickListener(v -> finish());

        if (imgSearchIcon != null) imgSearchIcon.setOnClickListener(v -> performSearch());

        if (btnClearSearch != null && edtSearchQuery != null) {
            btnClearSearch.setOnClickListener(v -> edtSearchQuery.setText(""));
        }

        if (edtSearchQuery != null) {
            edtSearchQuery.setOnEditorActionListener((v, actionId, event) -> {
                if (actionId == EditorInfo.IME_ACTION_SEARCH || actionId == EditorInfo.IME_ACTION_DONE) {
                    if (pendingSearchRunnable != null) searchHandler.removeCallbacks(pendingSearchRunnable);
                    performSearch();
                    return true;
                }
                return false;
            });

            edtSearchQuery.addTextChangedListener(new TextWatcher() {
                @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    if (suppressTextWatcher) return;
                    if (pendingSearchRunnable != null) searchHandler.removeCallbacks(pendingSearchRunnable);
                    pendingSearchRunnable = () -> performSearch();
                    searchHandler.postDelayed(pendingSearchRunnable, 350);
                }

                @Override public void afterTextChanged(Editable s) {}
            });
        }

        if (tabDishes != null) tabDishes.setOnClickListener(v -> switchTab(TabType.DISHES));
        if (tabRestaurants != null) tabRestaurants.setOnClickListener(v -> switchTab(TabType.STORES));
        if (tabUsers != null) tabUsers.setOnClickListener(v -> switchTab(TabType.USERS));
        if (tabPosts != null) tabPosts.setOnClickListener(v -> switchTab(TabType.POSTS));

        if (btnOpenFilter != null) {
            btnOpenFilter.setOnClickListener(v -> openFilterBottomSheet());
        }
    }

    private void switchTab(TabType tab) {
        if (currentTab == tab) return;
        currentTab = tab;
        updateTabStyles();
        if (btnOpenFilter != null) {
            btnOpenFilter.setVisibility(tab == TabType.DISHES || tab == TabType.STORES ? View.VISIBLE : View.GONE);
        }
        performSearch();
    }

    private void updateTabStyles() {
        applyTabStyle(tabDishes, currentTab == TabType.DISHES);
        applyTabStyle(tabRestaurants, currentTab == TabType.STORES);
        applyTabStyle(tabUsers, currentTab == TabType.USERS);
        applyTabStyle(tabPosts, currentTab == TabType.POSTS);
    }

    private void applyTabStyle(TextView tab, boolean active) {
        if (tab == null) return;
        if (active) {
            tab.setBackgroundResource(R.drawable.bg_chip_inactive);
            tab.setTextColor(ContextCompat.getColor(this, R.color.primary));
            tab.setTypeface(null, Typeface.BOLD);
        } else {
            tab.setBackgroundResource(0);
            tab.setTextColor(ContextCompat.getColor(this, R.color.text_gray));
            tab.setTypeface(null, Typeface.NORMAL);
        }
    }

    private String getCurrentQuery() {
        if (edtSearchQuery == null || edtSearchQuery.getText() == null) return "";
        return edtSearchQuery.getText().toString().trim();
    }

    private void updateHeaderLabel(int count) {
        if (tvResultHeader == null) return;
        String q = getCurrentQuery();
        String prefix;
        switch (currentTab) {
            case STORES:
                prefix = q.isEmpty() ? "Quán ăn" : "Quán ăn cho \"" + q + "\"";
                break;
            case USERS:
                prefix = q.isEmpty() ? "Người dùng" : "Người dùng cho \"" + q + "\"";
                break;
            case POSTS:
                prefix = q.isEmpty() ? "Bài viết" : "Bài viết cho \"" + q + "\"";
                break;
            case DISHES:
            default:
                prefix = q.isEmpty() ? "Món ăn" : "Món ăn cho \"" + q + "\"";
                break;
        }
        tvResultHeader.setText(prefix + " (" + count + ")");
    }

    private void performSearch() {
        String q = getCurrentQuery();
        String queryParam = q.isEmpty() ? null : q;

        if (pbSearchLoading != null) pbSearchLoading.setVisibility(View.VISIBLE);
        if (tvEmptyResults != null) tvEmptyResults.setVisibility(View.GONE);
        if (layoutResultsContainer != null) layoutResultsContainer.removeAllViews();

        switch (currentTab) {
            case STORES:
                ApiClient.getApiService().searchStores(queryParam, 40).enqueue(new Callback<SearchStoresResponse>() {
                    @Override
                    public void onResponse(@NonNull Call<SearchStoresResponse> call, @NonNull Response<SearchStoresResponse> response) {
                        if (isFinishing() || isDestroyed()) return;
                        if (pbSearchLoading != null) pbSearchLoading.setVisibility(View.GONE);
                        loadedStores.clear();
                        if (response.isSuccessful() && response.body() != null && response.body().data != null) {
                            loadedStores.addAll(response.body().data);
                        }
                        renderStores();
                    }

                    @Override
                    public void onFailure(@NonNull Call<SearchStoresResponse> call, @NonNull Throwable t) {
                        if (isFinishing() || isDestroyed()) return;
                        if (pbSearchLoading != null) pbSearchLoading.setVisibility(View.GONE);
                        showEmpty("Không thể tải kết quả tìm kiếm.");
                    }
                });
                break;

            case USERS:
                ApiClient.getApiService().searchAccounts(queryParam, 40).enqueue(new Callback<SearchAccountsResponse>() {
                    @Override
                    public void onResponse(@NonNull Call<SearchAccountsResponse> call, @NonNull Response<SearchAccountsResponse> response) {
                        if (isFinishing() || isDestroyed()) return;
                        if (pbSearchLoading != null) pbSearchLoading.setVisibility(View.GONE);
                        loadedUsers.clear();
                        if (response.isSuccessful() && response.body() != null && response.body().data != null) {
                            loadedUsers.addAll(response.body().data);
                        }
                        renderUsers();
                    }

                    @Override
                    public void onFailure(@NonNull Call<SearchAccountsResponse> call, @NonNull Throwable t) {
                        if (isFinishing() || isDestroyed()) return;
                        if (pbSearchLoading != null) pbSearchLoading.setVisibility(View.GONE);
                        showEmpty("Không thể tải danh sách người dùng.");
                    }
                });
                break;

            case POSTS:
                ApiClient.getApiService().searchFood(queryParam, null, 30).enqueue(new Callback<FeedPostsResponse>() {
                    @Override
                    public void onResponse(@NonNull Call<FeedPostsResponse> call, @NonNull Response<FeedPostsResponse> response) {
                        if (isFinishing() || isDestroyed()) return;
                        if (pbSearchLoading != null) pbSearchLoading.setVisibility(View.GONE);
                        loadedPosts.clear();
                        if (response.isSuccessful() && response.body() != null && response.body().getData() != null) {
                            loadedPosts.addAll(response.body().getData());
                        }
                        renderPosts();
                    }

                    @Override
                    public void onFailure(@NonNull Call<FeedPostsResponse> call, @NonNull Throwable t) {
                        if (isFinishing() || isDestroyed()) return;
                        if (pbSearchLoading != null) pbSearchLoading.setVisibility(View.GONE);
                        showEmpty("Không thể tải bài viết.");
                    }
                });
                break;

            case DISHES:
            default:
                ApiClient.getApiService().searchDishes(queryParam, 50).enqueue(new Callback<SearchDishesResponse>() {
                    @Override
                    public void onResponse(@NonNull Call<SearchDishesResponse> call, @NonNull Response<SearchDishesResponse> response) {
                        if (isFinishing() || isDestroyed()) return;
                        if (pbSearchLoading != null) pbSearchLoading.setVisibility(View.GONE);
                        loadedDishes.clear();
                        if (response.isSuccessful() && response.body() != null && response.body().data != null) {
                            loadedDishes.addAll(response.body().data);
                        }
                        renderDishes();
                    }

                    @Override
                    public void onFailure(@NonNull Call<SearchDishesResponse> call, @NonNull Throwable t) {
                        if (isFinishing() || isDestroyed()) return;
                        if (pbSearchLoading != null) pbSearchLoading.setVisibility(View.GONE);
                        showEmpty("Không thể tải danh sách món ăn.");
                    }
                });
                break;
        }
    }

    private void renderDishes() {
        List<DishModel> filtered = new ArrayList<>();
        for (DishModel dish : loadedDishes) {
            if (dish == null) continue;
            if (minRating > 0 && dish.rating < minRating) continue;
            if (priceBand == 1 && dish.price >= 50000) continue;
            if (priceBand == 2 && (dish.price < 50000 || dish.price > 100000)) continue;
            if (priceBand == 3 && dish.price <= 100000) continue;
            filtered.add(dish);
        }

        if ("rating".equals(sortMode)) {
            Collections.sort(filtered, (a, b) -> Double.compare(b.rating, a.rating));
        } else if ("price_asc".equals(sortMode)) {
            Collections.sort(filtered, (a, b) -> Integer.compare(a.price, b.price));
        }

        updateHeaderLabel(filtered.size());
        if (layoutResultsContainer == null) return;
        layoutResultsContainer.removeAllViews();

        if (filtered.isEmpty()) {
            showEmpty("Không tìm thấy món ăn phù hợp.");
            return;
        }
        if (tvEmptyResults != null) tvEmptyResults.setVisibility(View.GONE);

        for (int i = 0; i < filtered.size(); i++) {
            DishModel dish = filtered.get(i);
            View card = i == 0 ? buildFeaturedDishCard(dish) : buildCompactDishCard(dish);
            layoutResultsContainer.addView(card);
        }
    }

    private View buildFeaturedDishCard(DishModel dish) {
        LinearLayout card = new LinearLayout(this);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setBackgroundResource(R.drawable.bg_card);
        card.setClickable(true);
        card.setFocusable(true);
        card.setOnClickListener(v -> openDishDetail(dish.id));

        RelativeLayout imgBox = new RelativeLayout(this);
        imgBox.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, dp(190)));
        imgBox.setBackgroundColor(Color.parseColor("#E8ECF4"));

        ImageView img = new ImageView(this);
        img.setLayoutParams(new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.MATCH_PARENT));
        img.setScaleType(ImageView.ScaleType.CENTER_CROP);
        img.setImageResource(R.drawable.logo);
        if (dish.imageUrl != null && !dish.imageUrl.trim().isEmpty()) {
            ImageLoader.loadImage(img, dish.imageUrl);
        }
        imgBox.addView(img);

        TextView badge = new TextView(this);
        badge.setText("Khớp nhất");
        badge.setTextColor(ContextCompat.getColor(this, R.color.primary));
        badge.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12);
        badge.setTypeface(null, Typeface.BOLD);
        badge.setBackgroundResource(R.drawable.bg_badge_expert);
        badge.setPadding(dp(10), dp(5), dp(10), dp(5));
        RelativeLayout.LayoutParams badgeLp = new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
        badgeLp.addRule(RelativeLayout.ALIGN_PARENT_START);
        badgeLp.addRule(RelativeLayout.ALIGN_PARENT_TOP);
        badgeLp.setMargins(dp(12), dp(12), dp(12), dp(12));
        imgBox.addView(badge, badgeLp);

        card.addView(imgBox);

        LinearLayout body = new LinearLayout(this);
        body.setOrientation(LinearLayout.VERTICAL);
        body.setPadding(dp(14), dp(14), dp(14), dp(14));

        TextView tvName = new TextView(this);
        tvName.setText(dish.name != null ? dish.name : "");
        tvName.setTextColor(ContextCompat.getColor(this, R.color.text_dark));
        tvName.setTextSize(TypedValue.COMPLEX_UNIT_SP, 17);
        tvName.setTypeface(null, Typeface.BOLD);
        body.addView(tvName);

        TextView tvMeta = new TextView(this);
        String meta = String.format(Locale.US, "★ %.1f (%d)", dish.rating, Math.max(0, dish.reviewsCount));
        if (dish.storeName != null && !dish.storeName.trim().isEmpty()) {
            meta += " • " + dish.storeName.trim();
        }
        tvMeta.setText(meta);
        tvMeta.setTextColor(ContextCompat.getColor(this, R.color.text_gray));
        tvMeta.setTextSize(TypedValue.COMPLEX_UNIT_SP, 13);
        LinearLayout.LayoutParams metaLp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        metaLp.topMargin = dp(4);
        tvMeta.setLayoutParams(metaLp);
        body.addView(tvMeta);

        RelativeLayout bottom = new RelativeLayout(this);
        LinearLayout.LayoutParams bottomLp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        bottomLp.topMargin = dp(10);
        bottom.setLayoutParams(bottomLp);

        TextView tvPrice = new TextView(this);
        tvPrice.setText(dish.formattedPrice != null && !dish.formattedPrice.isEmpty() ? dish.formattedPrice : formatPrice(dish.price));
        tvPrice.setTextColor(ContextCompat.getColor(this, R.color.primary));
        tvPrice.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
        tvPrice.setTypeface(null, Typeface.BOLD);
        RelativeLayout.LayoutParams priceLp = new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
        priceLp.addRule(RelativeLayout.ALIGN_PARENT_START);
        priceLp.addRule(RelativeLayout.CENTER_VERTICAL);
        bottom.addView(tvPrice, priceLp);

        TextView tvCta = new TextView(this);
        tvCta.setText("Xem món ➔");
        tvCta.setTextColor(ContextCompat.getColor(this, R.color.primary));
        tvCta.setTextSize(TypedValue.COMPLEX_UNIT_SP, 13);
        tvCta.setTypeface(null, Typeface.BOLD);
        RelativeLayout.LayoutParams ctaLp = new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
        ctaLp.addRule(RelativeLayout.ALIGN_PARENT_END);
        ctaLp.addRule(RelativeLayout.CENTER_VERTICAL);
        bottom.addView(tvCta, ctaLp);

        body.addView(bottom);
        card.addView(body);
        return card;
    }

    private View buildCompactDishCard(DishModel dish) {
        LinearLayout card = new LinearLayout(this);
        card.setOrientation(LinearLayout.HORIZONTAL);
        card.setBackgroundResource(R.drawable.bg_card);
        card.setPadding(dp(12), dp(12), dp(12), dp(12));
        LinearLayout.LayoutParams cardLp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        cardLp.topMargin = dp(12);
        card.setLayoutParams(cardLp);
        card.setClickable(true);
        card.setFocusable(true);
        card.setOnClickListener(v -> openDishDetail(dish.id));

        ImageView thumb = new ImageView(this);
        thumb.setLayoutParams(new LinearLayout.LayoutParams(dp(80), dp(80)));
        thumb.setBackgroundColor(Color.parseColor("#E8ECF4"));
        thumb.setScaleType(ImageView.ScaleType.CENTER_CROP);
        thumb.setImageResource(R.drawable.logo);
        if (dish.imageUrl != null && !dish.imageUrl.trim().isEmpty()) {
            ImageLoader.loadImage(thumb, dish.imageUrl);
        }
        card.addView(thumb);

        LinearLayout info = new LinearLayout(this);
        info.setOrientation(LinearLayout.VERTICAL);
        LinearLayout.LayoutParams infoLp = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f);
        infoLp.setMarginStart(dp(12));
        info.setLayoutParams(infoLp);

        TextView tvName = new TextView(this);
        tvName.setText(dish.name != null ? dish.name : "");
        tvName.setTextColor(ContextCompat.getColor(this, R.color.text_dark));
        tvName.setTextSize(TypedValue.COMPLEX_UNIT_SP, 15);
        tvName.setTypeface(null, Typeface.BOLD);
        info.addView(tvName);

        String subtitle = dish.storeName != null && !dish.storeName.isEmpty() ? dish.storeName : "";
        if (dish.description != null && !dish.description.trim().isEmpty()) {
            subtitle = subtitle.isEmpty() ? dish.description.trim() : subtitle + " • " + dish.description.trim();
        }
        TextView tvSub = new TextView(this);
        tvSub.setText(subtitle);
        tvSub.setMaxLines(2);
        tvSub.setTextColor(ContextCompat.getColor(this, R.color.text_gray));
        tvSub.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12);
        LinearLayout.LayoutParams subLp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        subLp.topMargin = dp(2);
        tvSub.setLayoutParams(subLp);
        info.addView(tvSub);

        RelativeLayout footer = new RelativeLayout(this);
        LinearLayout.LayoutParams fLp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        fLp.topMargin = dp(6);
        footer.setLayoutParams(fLp);

        TextView tvPrice = new TextView(this);
        String priceStr = dish.formattedPrice != null && !dish.formattedPrice.isEmpty() ? dish.formattedPrice : formatPrice(dish.price);
        tvPrice.setText(priceStr + String.format(Locale.US, "  •  ★ %.1f", dish.rating));
        tvPrice.setTextColor(ContextCompat.getColor(this, R.color.text_dark));
        tvPrice.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12);
        tvPrice.setTypeface(null, Typeface.BOLD);
        footer.addView(tvPrice);

        TextView tvMore = new TextView(this);
        tvMore.setText("Xem món ➔");
        tvMore.setTextColor(ContextCompat.getColor(this, R.color.primary));
        tvMore.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12);
        tvMore.setTypeface(null, Typeface.BOLD);
        RelativeLayout.LayoutParams moreLp = new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
        moreLp.addRule(RelativeLayout.ALIGN_PARENT_END);
        footer.addView(tvMore, moreLp);

        info.addView(footer);
        card.addView(info);
        return card;
    }

    private void renderStores() {
        List<SearchStoresResponse.StoreSearchItem> filtered = new ArrayList<>();
        for (SearchStoresResponse.StoreSearchItem s : loadedStores) {
            if (s == null) continue;
            if (minRating > 0 && s.rating < minRating) continue;
            filtered.add(s);
        }

        if ("rating".equals(sortMode)) {
            Collections.sort(filtered, (a, b) -> Double.compare(b.rating, a.rating));
        }

        updateHeaderLabel(filtered.size());
        if (layoutResultsContainer == null) return;
        layoutResultsContainer.removeAllViews();

        if (filtered.isEmpty()) {
            showEmpty("Không tìm thấy quán ăn phù hợp.");
            return;
        }
        if (tvEmptyResults != null) tvEmptyResults.setVisibility(View.GONE);

        for (int i = 0; i < filtered.size(); i++) {
            SearchStoresResponse.StoreSearchItem store = filtered.get(i);
            LinearLayout card = new LinearLayout(this);
            card.setOrientation(LinearLayout.HORIZONTAL);
            card.setBackgroundResource(R.drawable.bg_card);
            card.setPadding(dp(12), dp(12), dp(12), dp(12));
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            if (i > 0) lp.topMargin = dp(12);
            card.setLayoutParams(lp);
            card.setClickable(true);
            card.setFocusable(true);
            card.setOnClickListener(v -> openStoreDetail(store.id));

            ImageView avatar = new ImageView(this);
            avatar.setLayoutParams(new LinearLayout.LayoutParams(dp(64), dp(64)));
            avatar.setBackgroundColor(Color.parseColor("#E8ECF4"));
            avatar.setScaleType(ImageView.ScaleType.CENTER_CROP);
            avatar.setImageResource(R.drawable.logo);
            if (store.avatarUrl != null && !store.avatarUrl.trim().isEmpty()) {
                ImageLoader.loadImage(avatar, store.avatarUrl);
            }
            card.addView(avatar);

            LinearLayout info = new LinearLayout(this);
            info.setOrientation(LinearLayout.VERTICAL);
            LinearLayout.LayoutParams infoLp = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f);
            infoLp.setMarginStart(dp(12));
            info.setLayoutParams(infoLp);

            TextView tvName = new TextView(this);
            tvName.setText(store.name != null ? store.name : "");
            tvName.setTextColor(ContextCompat.getColor(this, R.color.text_dark));
            tvName.setTextSize(TypedValue.COMPLEX_UNIT_SP, 15);
            tvName.setTypeface(null, Typeface.BOLD);
            info.addView(tvName);

            if (store.address != null && !store.address.trim().isEmpty()) {
                TextView tvAddr = new TextView(this);
                tvAddr.setText("📍 " + store.address.trim());
                tvAddr.setTextColor(ContextCompat.getColor(this, R.color.text_gray));
                tvAddr.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12);
                LinearLayout.LayoutParams aLp = new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                aLp.topMargin = dp(2);
                tvAddr.setLayoutParams(aLp);
                info.addView(tvAddr);
            }

            StringBuilder meta = new StringBuilder();
            meta.append(String.format(Locale.US, "★ %.1f", store.rating));
            meta.append(store.isOpen ? " • Đang mở cửa" : " • Đã đóng cửa");
            if (store.highlightDish != null && !store.highlightDish.isEmpty()) {
                meta.append(" • ").append(store.highlightDish);
            }
            TextView tvMeta = new TextView(this);
            tvMeta.setText(meta.toString());
            tvMeta.setTextColor(store.isOpen ? Color.parseColor("#0A8050") : ContextCompat.getColor(this, R.color.text_gray));
            tvMeta.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12);
            LinearLayout.LayoutParams mLp = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            mLp.topMargin = dp(4);
            tvMeta.setLayoutParams(mLp);
            info.addView(tvMeta);

            card.addView(info);
            layoutResultsContainer.addView(card);
        }
    }

    private void renderUsers() {
        updateHeaderLabel(loadedUsers.size());
        if (layoutResultsContainer == null) return;
        layoutResultsContainer.removeAllViews();

        if (loadedUsers.isEmpty()) {
            showEmpty("Không tìm thấy người dùng nào.");
            return;
        }
        if (tvEmptyResults != null) tvEmptyResults.setVisibility(View.GONE);

        int selfId = AuthHelper.getCurrentUserId(this);
        for (int i = 0; i < loadedUsers.size(); i++) {
            FeedPostsResponse.Author user = loadedUsers.get(i);
            if (user == null) continue;

            LinearLayout row = new LinearLayout(this);
            row.setOrientation(LinearLayout.HORIZONTAL);
            row.setGravity(Gravity.CENTER_VERTICAL);
            row.setBackgroundResource(R.drawable.bg_card);
            row.setPadding(dp(12), dp(12), dp(12), dp(12));
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            if (i > 0) lp.topMargin = dp(10);
            row.setLayoutParams(lp);
            row.setClickable(true);
            row.setFocusable(true);
            row.setOnClickListener(v -> {
                if (selfId > 0 && user.userId == selfId) {
                    startActivity(new Intent(SearchResultsActivity.this, ProfileActivity.class));
                } else {
                    Intent intent = new Intent(SearchResultsActivity.this, UserProfileActivity.class);
                    intent.putExtra("USER_ID", user.userId);
                    startActivity(intent);
                }
            });

            ImageView avatar = new ImageView(this);
            avatar.setLayoutParams(new LinearLayout.LayoutParams(dp(46), dp(46)));
            avatar.setImageResource(R.drawable.logo);
            if (user.avatarUrl != null && !user.avatarUrl.trim().isEmpty()) {
                ImageLoader.loadAvatar(avatar, user.avatarUrl);
            }
            row.addView(avatar);

            LinearLayout info = new LinearLayout(this);
            info.setOrientation(LinearLayout.VERTICAL);
            LinearLayout.LayoutParams infoLp = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f);
            infoLp.setMarginStart(dp(12));
            info.setLayoutParams(infoLp);

            String displayName = user.fullName != null && !user.fullName.trim().isEmpty() ? user.fullName.trim() : user.username;
            TextView tvName = new TextView(this);
            tvName.setText(displayName != null ? displayName : "Thành viên");
            tvName.setTextColor(ContextCompat.getColor(this, R.color.text_dark));
            tvName.setTextSize(TypedValue.COMPLEX_UNIT_SP, 15);
            tvName.setTypeface(null, Typeface.BOLD);
            info.addView(tvName);

            String handle = user.username != null && !user.username.isEmpty()
                    ? "@" + user.username.replace("@", "")
                    : "";
            if (!handle.isEmpty()) {
                TextView tvHandle = new TextView(this);
                tvHandle.setText(handle + (user.bio != null && !user.bio.trim().isEmpty() ? " • " + user.bio.trim() : ""));
                tvHandle.setMaxLines(1);
                tvHandle.setEllipsize(android.text.TextUtils.TruncateAt.END);
                tvHandle.setTextColor(ContextCompat.getColor(this, R.color.text_gray));
                tvHandle.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12);
                info.addView(tvHandle);
            }

            row.addView(info);
            layoutResultsContainer.addView(row);
        }
    }

    private void renderPosts() {
        updateHeaderLabel(loadedPosts.size());
        if (layoutResultsContainer == null) return;
        layoutResultsContainer.removeAllViews();

        if (loadedPosts.isEmpty()) {
            showEmpty("Không tìm thấy bài viết nào.");
            return;
        }
        if (tvEmptyResults != null) tvEmptyResults.setVisibility(View.GONE);

        PostFeedHelper.PostCardListener cardListener = new PostFeedHelper.PostCardListener() {
            @Override
            public void onPostDeleted(int postId) {
                for (int i = loadedPosts.size() - 1; i >= 0; i--) {
                    if (loadedPosts.get(i).postId == postId) loadedPosts.remove(i);
                }
                renderPosts();
            }

            @Override
            public void onPostArchived(int postId) {
                onPostDeleted(postId);
            }
        };

        for (FeedPostsResponse.PostItem post : loadedPosts) {
            View cardView = getLayoutInflater().inflate(R.layout.item_feed_post, layoutResultsContainer, false);
            PostFeedHelper.bindPostCard(this, cardView, post, null, cardListener);
            layoutResultsContainer.addView(cardView);
        }
    }

    private void showEmpty(String msg) {
        updateHeaderLabel(0);
        if (layoutResultsContainer != null) layoutResultsContainer.removeAllViews();
        if (tvEmptyResults != null) {
            tvEmptyResults.setText(msg);
            tvEmptyResults.setVisibility(View.VISIBLE);
        }
    }

    private void openDishDetail(int dishId) {
        Intent intent = new Intent(SearchResultsActivity.this, DishDetailActivity.class);
        intent.putExtra("DISH_ID", dishId);
        startActivity(intent);
    }

    private void openStoreDetail(int storeId) {
        Intent intent = new Intent(SearchResultsActivity.this, StoreDetailActivity.class);
        intent.putExtra("STORE_ID", storeId);
        startActivity(intent);
    }

    private void openFilterBottomSheet() {
        BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(SearchResultsActivity.this);
        View dialogView = getLayoutInflater().inflate(R.layout.layout_dialog_filter, null);
        bottomSheetDialog.setContentView(dialogView);

        View btnCloseFilter = dialogView.findViewById(R.id.btnCloseFilter);
        TextView btnResetFilter = dialogView.findViewById(R.id.btnResetFilter);
        View btnApplyFilter = dialogView.findViewById(R.id.btnApplyFilter);

        TextView chipSortDefault = dialogView.findViewById(R.id.chipSortDefault);
        TextView chipSortRating = dialogView.findViewById(R.id.chipSortRating);
        TextView chipSortPriceAsc = dialogView.findViewById(R.id.chipSortPriceAsc);

        TextView chipPriceAll = dialogView.findViewById(R.id.chipPriceAll);
        TextView chipPriceUnder50 = dialogView.findViewById(R.id.chipPriceUnder50);
        TextView chipPrice50To100 = dialogView.findViewById(R.id.chipPrice50To100);
        TextView chipPriceOver100 = dialogView.findViewById(R.id.chipPriceOver100);

        TextView chipRatingAll = dialogView.findViewById(R.id.chipRatingAll);
        TextView chipRating40 = dialogView.findViewById(R.id.chipRating40);
        TextView chipRating45 = dialogView.findViewById(R.id.chipRating45);

        final String[] tempSort = {sortMode};
        final int[] tempPrice = {priceBand};
        final double[] tempRating = {minRating};

        Runnable refreshDialogChips = () -> {
            styleFilterChip(chipSortDefault, "default".equals(tempSort[0]), true);
            styleFilterChip(chipSortRating, "rating".equals(tempSort[0]), true);
            styleFilterChip(chipSortPriceAsc, "price_asc".equals(tempSort[0]), true);

            styleFilterChip(chipPriceAll, tempPrice[0] == 0, false);
            styleFilterChip(chipPriceUnder50, tempPrice[0] == 1, false);
            styleFilterChip(chipPrice50To100, tempPrice[0] == 2, false);
            styleFilterChip(chipPriceOver100, tempPrice[0] == 3, false);

            styleFilterChip(chipRatingAll, tempRating[0] == 0.0, false);
            styleFilterChip(chipRating40, Math.abs(tempRating[0] - 4.0) < 0.01, false);
            styleFilterChip(chipRating45, Math.abs(tempRating[0] - 4.5) < 0.01, false);
        };
        refreshDialogChips.run();

        if (chipSortDefault != null) chipSortDefault.setOnClickListener(v -> { tempSort[0] = "default"; refreshDialogChips.run(); });
        if (chipSortRating != null) chipSortRating.setOnClickListener(v -> { tempSort[0] = "rating"; refreshDialogChips.run(); });
        if (chipSortPriceAsc != null) chipSortPriceAsc.setOnClickListener(v -> { tempSort[0] = "price_asc"; refreshDialogChips.run(); });

        if (chipPriceAll != null) chipPriceAll.setOnClickListener(v -> { tempPrice[0] = 0; refreshDialogChips.run(); });
        if (chipPriceUnder50 != null) chipPriceUnder50.setOnClickListener(v -> { tempPrice[0] = 1; refreshDialogChips.run(); });
        if (chipPrice50To100 != null) chipPrice50To100.setOnClickListener(v -> { tempPrice[0] = 2; refreshDialogChips.run(); });
        if (chipPriceOver100 != null) chipPriceOver100.setOnClickListener(v -> { tempPrice[0] = 3; refreshDialogChips.run(); });

        if (chipRatingAll != null) chipRatingAll.setOnClickListener(v -> { tempRating[0] = 0.0; refreshDialogChips.run(); });
        if (chipRating40 != null) chipRating40.setOnClickListener(v -> { tempRating[0] = 4.0; refreshDialogChips.run(); });
        if (chipRating45 != null) chipRating45.setOnClickListener(v -> { tempRating[0] = 4.5; refreshDialogChips.run(); });

        if (btnResetFilter != null) {
            btnResetFilter.setOnClickListener(v -> {
                tempSort[0] = "default";
                tempPrice[0] = 0;
                tempRating[0] = 0.0;
                refreshDialogChips.run();
            });
        }

        if (btnCloseFilter != null) btnCloseFilter.setOnClickListener(v -> bottomSheetDialog.dismiss());

        if (btnApplyFilter != null) {
            btnApplyFilter.setOnClickListener(v -> {
                sortMode = tempSort[0];
                priceBand = tempPrice[0];
                minRating = tempRating[0];
                updateFilterIndicator();
                bottomSheetDialog.dismiss();
                if (currentTab == TabType.DISHES) renderDishes();
                else if (currentTab == TabType.STORES) renderStores();
            });
        }

        bottomSheetDialog.show();
    }

    private void styleFilterChip(TextView chip, boolean active, boolean primaryFilled) {
        if (chip == null) return;
        if (active) {
            chip.setBackgroundResource(primaryFilled ? R.drawable.bg_chip_active : R.drawable.bg_badge_expert);
            chip.setTextColor(ContextCompat.getColor(this, primaryFilled ? R.color.white : R.color.primary));
            chip.setTypeface(null, Typeface.BOLD);
        } else {
            chip.setBackgroundResource(R.drawable.bg_chip_inactive);
            chip.setTextColor(ContextCompat.getColor(this, R.color.text_dark));
            chip.setTypeface(null, Typeface.NORMAL);
        }
    }

    private void updateFilterIndicator() {
        if (tvFilterLabel == null) return;
        boolean hasFilter = !"default".equals(sortMode) || priceBand != 0 || minRating > 0;
        tvFilterLabel.setText(hasFilter ? "Lọc •" : "Lọc");
        tvFilterLabel.setTextColor(ContextCompat.getColor(this, hasFilter ? R.color.primary : R.color.text_dark));
    }

    private String formatPrice(int price) {
        if (price <= 0) return "";
        return String.format(Locale.US, "%,dđ", price).replace(",", ".");
    }

    private int dp(int value) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, value, getResources().getDisplayMetrics());
    }
}
