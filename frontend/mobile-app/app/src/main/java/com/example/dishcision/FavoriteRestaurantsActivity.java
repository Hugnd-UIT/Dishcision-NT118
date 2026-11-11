package com.example.dishcision;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.View;
import android.widget.EditText;
import android.widget.HorizontalScrollView;
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

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class FavoriteRestaurantsActivity extends AppCompatActivity {

    private ImageView btnBack;
    private ImageView btnNotification;
    private EditText edtSearch;
    private TextView tvSummaryBadge;

    private TextView chipAll;
    private TextView chipOpen;
    private TextView chipNearMe;
    private TextView currentActiveChip;

    private TextView tvStoriesHeader;
    private HorizontalScrollView scrollStories;
    private LinearLayout layoutStoriesContainer;
    private LinearLayout layoutStoresContainer;
    private TextView tvEmptyStores;

    private final List<FollowingRestaurantsResponse.StoreItem> loadedStores = new ArrayList<>();
    private final List<FollowingRestaurantsResponse.StoryItem> loadedStories = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_favorite_restaurants);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main_favorite_restaurants), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        ApiClient.init(this);
        initViews();
        setupListeners();
    }

    @Override
    protected void onResume() {
        super.onResume();
        fetchRestaurantsFromBackend();
    }

    private void initViews() {
        btnBack = findViewById(R.id.btnBack);
        btnNotification = findViewById(R.id.btnNotification);
        edtSearch = findViewById(R.id.edtSearch);
        tvSummaryBadge = findViewById(R.id.tvSummaryBadge);

        chipAll = findViewById(R.id.chipAll);
        chipOpen = findViewById(R.id.chipOpen);
        chipNearMe = findViewById(R.id.chipNearMe);
        currentActiveChip = chipAll;

        tvStoriesHeader = findViewById(R.id.tvStoriesHeader);
        scrollStories = findViewById(R.id.scrollStories);
        layoutStoriesContainer = findViewById(R.id.layoutStoriesContainer);
        layoutStoresContainer = findViewById(R.id.layoutStoresContainer);
        tvEmptyStores = findViewById(R.id.tvEmptyStores);
    }

    private void setupListeners() {
        if (btnBack != null) btnBack.setOnClickListener(v -> finish());

        if (btnNotification != null) {
            btnNotification.setOnClickListener(v -> startActivity(new Intent(this, NotificationsActivity.class)));
        }

        if (edtSearch != null) {
            edtSearch.addTextChangedListener(new TextWatcher() {
                @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
                @Override public void onTextChanged(CharSequence s, int start, int before, int count) { renderStores(); }
                @Override public void afterTextChanged(Editable s) {}
            });
        }

        BottomNavHelper.bind(this, BottomNavHelper.TAB_NONE);

        if (chipAll != null) chipAll.setOnClickListener(v -> {
            if (currentActiveChip != chipAll) {
                setActiveChip(chipAll);
                fetchRestaurantsFromBackend();
            }
        });
        if (chipOpen != null) chipOpen.setOnClickListener(v -> {
            setActiveChip(chipOpen);
            renderStores();
        });
        if (chipNearMe != null) chipNearMe.setOnClickListener(v -> {
            if (currentActiveChip != chipNearMe) {
                setActiveChip(chipNearMe);
                fetchRestaurantsFromBackend();
            }
        });
    }

    private void setActiveChip(TextView selectedChip) {
        if (selectedChip == null || selectedChip == currentActiveChip) return;

        if (currentActiveChip != null) {
            currentActiveChip.setBackgroundResource(R.drawable.bg_chip_inactive);
            currentActiveChip.setTextColor(ContextCompat.getColor(this, R.color.text_dark));
            currentActiveChip.setTypeface(null, Typeface.NORMAL);
        }

        selectedChip.setBackgroundResource(R.drawable.bg_chip_active);
        selectedChip.setTextColor(ContextCompat.getColor(this, R.color.white));
        selectedChip.setTypeface(null, Typeface.BOLD);

        currentActiveChip = selectedChip;
    }

    private void fetchRestaurantsFromBackend() {
        String authHeader = ApiClient.getAuthHeader(this);
        boolean exploreMode = currentActiveChip == chipNearMe;
        Call<FollowingRestaurantsResponse> call = exploreMode
                ? ApiClient.getApiService().getStores(authHeader, null)
                : ApiClient.getApiService().getFollowingRestaurants(authHeader);

        call.enqueue(new Callback<FollowingRestaurantsResponse>() {
            @Override
            public void onResponse(@NonNull Call<FollowingRestaurantsResponse> call, @NonNull Response<FollowingRestaurantsResponse> response) {
                if (isFinishing()) return;
                if (response.isSuccessful() && response.body() != null && response.body().getData() != null) {
                    FollowingRestaurantsResponse.RestaurantData data = response.body().getData();
                    loadedStores.clear();
                    if (data.restaurants != null) loadedStores.addAll(data.restaurants);
                    loadedStories.clear();
                    if (data.stories != null) loadedStories.addAll(data.stories);

                    if (tvSummaryBadge != null) {
                        tvSummaryBadge.setText(data.newDishesSummary != null && !data.newDishesSummary.isEmpty()
                                ? data.newDishesSummary
                                : loadedStores.size() + " quán ăn");
                    }

                    int openCount = 0;
                    for (FollowingRestaurantsResponse.StoreItem item : loadedStores) {
                        if (item.isOpen) openCount++;
                    }
                    if (!exploreMode) {
                        if (chipAll != null) chipAll.setText("Đang theo dõi (" + loadedStores.size() + ")");
                        if (chipOpen != null) chipOpen.setText("Đang mở cửa (" + openCount + ")");
                    }

                    renderStories();
                    renderStores();
                } else {
                    if (tvSummaryBadge != null) tvSummaryBadge.setText("Không thể tải danh sách quán");
                    Toast.makeText(FavoriteRestaurantsActivity.this, "Không thể tải danh sách quán (mã " + response.code() + ")", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<FollowingRestaurantsResponse> call, @NonNull Throwable t) {
                if (isFinishing()) return;
                if (tvSummaryBadge != null) tvSummaryBadge.setText("Lỗi kết nối");
                Toast.makeText(FavoriteRestaurantsActivity.this, "Lỗi kết nối máy chủ", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void renderStories() {
        if (layoutStoriesContainer == null) return;
        layoutStoriesContainer.removeAllViews();

        Map<Integer, FollowingRestaurantsResponse.StoryItem> distinctByStore = new LinkedHashMap<>();
        for (FollowingRestaurantsResponse.StoryItem st : loadedStories) {
            if (st.userId > 0 && !distinctByStore.containsKey(st.userId)) {
                distinctByStore.put(st.userId, st);
            }
        }

        if (distinctByStore.isEmpty()) {
            if (tvStoriesHeader != null) tvStoriesHeader.setVisibility(View.GONE);
            if (scrollStories != null) scrollStories.setVisibility(View.GONE);
            return;
        }

        if (tvStoriesHeader != null) tvStoriesHeader.setVisibility(View.VISIBLE);
        if (scrollStories != null) scrollStories.setVisibility(View.VISIBLE);

        boolean first = true;
        for (FollowingRestaurantsResponse.StoryItem st : distinctByStore.values()) {
            LinearLayout col = new LinearLayout(this);
            LinearLayout.LayoutParams colParams = new LinearLayout.LayoutParams(dpToPx(72), LinearLayout.LayoutParams.WRAP_CONTENT);
            if (!first) colParams.setMarginStart(dpToPx(10));
            first = false;
            col.setLayoutParams(colParams);
            col.setOrientation(LinearLayout.VERTICAL);
            col.setGravity(Gravity.CENTER_HORIZONTAL);
            col.setClickable(true);
            col.setFocusable(true);

            RelativeLayout ring = new RelativeLayout(this);
            ring.setLayoutParams(new LinearLayout.LayoutParams(dpToPx(60), dpToPx(60)));
            ring.setBackgroundResource(R.drawable.bg_story_ring);
            int pad = dpToPx(3);
            ring.setPadding(pad, pad, pad, pad);

            ImageView avatar = new ImageView(this);
            avatar.setLayoutParams(new RelativeLayout.LayoutParams(
                    RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.MATCH_PARENT));
            avatar.setScaleType(ImageView.ScaleType.CENTER_CROP);
            avatar.setImageResource(R.drawable.logo);
            if (st.avatarUrl != null && !st.avatarUrl.isEmpty()) {
                ImageLoader.loadAvatar(avatar, st.avatarUrl);
            }
            ring.addView(avatar);

            TextView tvName = new TextView(this);
            LinearLayout.LayoutParams nameParams = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            nameParams.topMargin = dpToPx(6);
            tvName.setLayoutParams(nameParams);
            tvName.setMaxLines(1);
            tvName.setEllipsize(TextUtils.TruncateAt.END);
            tvName.setText(st.storeName != null ? st.storeName : "");
            tvName.setTextColor(ContextCompat.getColor(this, R.color.text_dark));
            tvName.setTextSize(12f);

            col.addView(ring);
            col.addView(tvName);
            col.setOnClickListener(v -> {
                Intent intent = new Intent(this, StoryActivity.class);
                intent.putExtra(StoryActivity.EXTRA_USER_ID, st.userId);
                intent.putExtra(StoryActivity.EXTRA_STORE_NAME, st.storeName);
                startActivity(intent);
            });
            layoutStoriesContainer.addView(col);
        }
    }

    private void renderStores() {
        if (layoutStoresContainer == null) return;
        layoutStoresContainer.removeAllViews();

        String query = edtSearch != null ? edtSearch.getText().toString().trim().toLowerCase(Locale.ROOT) : "";
        boolean onlyOpen = currentActiveChip == chipOpen;

        List<FollowingRestaurantsResponse.StoreItem> filtered = new ArrayList<>();
        for (FollowingRestaurantsResponse.StoreItem store : loadedStores) {
            if (onlyOpen && !store.isOpen) continue;
            if (!query.isEmpty()) {
                String haystack = ((store.name != null ? store.name : "") + " " +
                        (store.address != null ? store.address : "") + " " +
                        (store.highlightDish != null ? store.highlightDish : "")).toLowerCase(Locale.ROOT);
                if (!haystack.contains(query)) continue;
            }
            filtered.add(store);
        }

        if (filtered.isEmpty()) {
            if (tvEmptyStores != null) {
                if (loadedStores.isEmpty() && currentActiveChip != chipNearMe) {
                    tvEmptyStores.setText("Bạn chưa theo dõi quán ăn nào.\nChạm \"Khám phá quán\" ở trên để tìm quán mới!");
                } else {
                    tvEmptyStores.setText("Không tìm thấy quán ăn phù hợp.");
                }
                tvEmptyStores.setVisibility(View.VISIBLE);
            }
            return;
        }

        if (tvEmptyStores != null) tvEmptyStores.setVisibility(View.GONE);

        for (int i = 0; i < filtered.size(); i++) {
            layoutStoresContainer.addView(buildStoreCard(filtered.get(i), i > 0));
        }
    }

    private View buildStoreCard(FollowingRestaurantsResponse.StoreItem store, boolean topMargin) {
        LinearLayout card = new LinearLayout(this);
        LinearLayout.LayoutParams cardParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        if (topMargin) cardParams.topMargin = dpToPx(16);
        card.setLayoutParams(cardParams);
        card.setBackgroundResource(R.drawable.bg_card);
        card.setOrientation(LinearLayout.VERTICAL);

        RelativeLayout imageBox = new RelativeLayout(this);
        imageBox.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, dpToPx(200)));

        ImageView imgCover = new ImageView(this);
        imgCover.setLayoutParams(new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.MATCH_PARENT));
        imgCover.setScaleType(ImageView.ScaleType.CENTER_CROP);
        imgCover.setImageResource(R.drawable.logo);
        String coverSrc = (store.coverUrl != null && !store.coverUrl.isEmpty()) ? store.coverUrl : store.avatarUrl;
        if (coverSrc != null && !coverSrc.isEmpty()) {
            ImageLoader.loadImage(imgCover, coverSrc);
        }
        imageBox.addView(imgCover);

        LinearLayout badgesRow = new LinearLayout(this);
        RelativeLayout.LayoutParams badgesParams = new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
        badgesParams.addRule(RelativeLayout.ALIGN_PARENT_START);
        badgesParams.addRule(RelativeLayout.ALIGN_PARENT_TOP);
        int m12 = dpToPx(12);
        badgesParams.setMargins(m12, m12, m12, m12);
        badgesRow.setLayoutParams(badgesParams);
        badgesRow.setOrientation(LinearLayout.HORIZONTAL);

        TextView tvStatus = new TextView(this);
        tvStatus.setBackgroundResource(R.drawable.bg_badge_verified);
        tvStatus.setPadding(dpToPx(10), dpToPx(5), dpToPx(10), dpToPx(5));
        tvStatus.setText(store.isOpen ? "Đang mở cửa" : "Đã đóng cửa");
        tvStatus.setTextColor(Color.parseColor(store.isOpen ? "#0A8050" : "#6B7280"));
        tvStatus.setTextSize(12f);
        tvStatus.setTypeface(null, Typeface.BOLD);
        badgesRow.addView(tvStatus);

        if (store.openHours != null && !store.openHours.isEmpty()) {
            TextView tvHours = new TextView(this);
            LinearLayout.LayoutParams hrsParams = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            hrsParams.setMarginStart(dpToPx(8));
            tvHours.setLayoutParams(hrsParams);
            tvHours.setBackgroundResource(R.drawable.bg_badge_rating);
            tvHours.setPadding(dpToPx(10), dpToPx(5), dpToPx(10), dpToPx(5));
            tvHours.setText(store.openHours);
            tvHours.setTextColor(ContextCompat.getColor(this, R.color.text_dark));
            tvHours.setTextSize(12f);
            tvHours.setTypeface(null, Typeface.BOLD);
            badgesRow.addView(tvHours);
        }

        if (store.rating > 0) {
            LinearLayout ratingPill = new LinearLayout(this);
            LinearLayout.LayoutParams rpParams = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            rpParams.setMarginStart(dpToPx(8));
            ratingPill.setLayoutParams(rpParams);
            ratingPill.setBackgroundResource(R.drawable.bg_badge_rating);
            ratingPill.setGravity(Gravity.CENTER_VERTICAL);
            ratingPill.setPadding(dpToPx(8), dpToPx(5), dpToPx(8), dpToPx(5));

            ImageView star = new ImageView(this);
            star.setLayoutParams(new LinearLayout.LayoutParams(dpToPx(14), dpToPx(14)));
            star.setImageResource(R.drawable.ic_star);

            TextView tvRating = new TextView(this);
            LinearLayout.LayoutParams trParams = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            trParams.setMarginStart(dpToPx(4));
            tvRating.setLayoutParams(trParams);
            tvRating.setText(String.format(Locale.US, "%.1f (%d)", store.rating, store.reviewsCount));
            tvRating.setTextColor(ContextCompat.getColor(this, R.color.text_dark));
            tvRating.setTextSize(12f);
            tvRating.setTypeface(null, Typeface.BOLD);

            ratingPill.addView(star);
            ratingPill.addView(tvRating);
            badgesRow.addView(ratingPill);
        }

        imageBox.addView(badgesRow);
        card.addView(imageBox);

        LinearLayout infoBody = new LinearLayout(this);
        infoBody.setOrientation(LinearLayout.VERTICAL);
        int p16 = dpToPx(16);
        infoBody.setPadding(p16, p16, p16, p16);

        LinearLayout titleRow = new LinearLayout(this);
        titleRow.setOrientation(LinearLayout.HORIZONTAL);
        titleRow.setGravity(Gravity.CENTER_VERTICAL);

        TextView tvName = new TextView(this);
        tvName.setText(store.name != null ? store.name : "");
        tvName.setTextColor(ContextCompat.getColor(this, R.color.text_dark));
        tvName.setTextSize(18f);
        tvName.setTypeface(null, Typeface.BOLD);

        ImageView verified = new ImageView(this);
        LinearLayout.LayoutParams vParams = new LinearLayout.LayoutParams(dpToPx(18), dpToPx(18));
        vParams.setMarginStart(dpToPx(6));
        verified.setLayoutParams(vParams);
        verified.setImageResource(R.drawable.ic_verified);

        titleRow.addView(tvName);
        titleRow.addView(verified);
        infoBody.addView(titleRow);

        if (store.address != null && !store.address.isEmpty()) {
            TextView tvAddr = new TextView(this);
            LinearLayout.LayoutParams aParams = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            aParams.topMargin = dpToPx(4);
            tvAddr.setLayoutParams(aParams);
            tvAddr.setText("📍 " + store.address);
            tvAddr.setTextColor(ContextCompat.getColor(this, R.color.text_gray));
            tvAddr.setTextSize(13f);
            infoBody.addView(tvAddr);
        }

        if (store.highlightDish != null && !store.highlightDish.isEmpty()) {
            TextView tvHighlight = new TextView(this);
            LinearLayout.LayoutParams hParams = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            hParams.topMargin = dpToPx(4);
            tvHighlight.setLayoutParams(hParams);
            String priceSuffix = (store.dishPrice != null && !store.dishPrice.isEmpty()) ? " • " + store.dishPrice : "";
            tvHighlight.setText("Món nổi bật: " + store.highlightDish + priceSuffix);
            tvHighlight.setTextColor(ContextCompat.getColor(this, R.color.primary));
            tvHighlight.setTextSize(12f);
            infoBody.addView(tvHighlight);
        }

        LinearLayout actionsRow = new LinearLayout(this);
        LinearLayout.LayoutParams arParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, dpToPx(44));
        arParams.topMargin = dpToPx(16);
        actionsRow.setLayoutParams(arParams);
        actionsRow.setGravity(Gravity.CENTER_VERTICAL);
        actionsRow.setOrientation(LinearLayout.HORIZONTAL);

        TextView btnFollow = new TextView(this);
        LinearLayout.LayoutParams bfParams = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.MATCH_PARENT, 1f);
        bfParams.setMarginEnd(dpToPx(6));
        btnFollow.setLayoutParams(bfParams);
        btnFollow.setBackgroundResource(R.drawable.bg_chip_inactive);
        btnFollow.setClickable(true);
        btnFollow.setFocusable(true);
        btnFollow.setGravity(Gravity.CENTER);
        btnFollow.setText(store.isFollowing ? "Đang theo dõi" : "+ Theo dõi");
        btnFollow.setTextColor(ContextCompat.getColor(this, R.color.text_dark));
        btnFollow.setTextSize(13f);
        btnFollow.setTypeface(null, Typeface.BOLD);
        btnFollow.setOnClickListener(v -> toggleFollowStore(store, btnFollow));

        LinearLayout btnMenu = new LinearLayout(this);
        LinearLayout.LayoutParams bmParams = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.MATCH_PARENT, 1f);
        bmParams.setMarginStart(dpToPx(6));
        bmParams.setMarginEnd(dpToPx(6));
        btnMenu.setLayoutParams(bmParams);
        btnMenu.setBackgroundResource(R.drawable.bg_button_primary);
        btnMenu.setClickable(true);
        btnMenu.setFocusable(true);
        btnMenu.setGravity(Gravity.CENTER);

        TextView tvMenuLabel = new TextView(this);
        tvMenuLabel.setText("🍴 Xem menu");
        tvMenuLabel.setTextColor(ContextCompat.getColor(this, R.color.white));
        tvMenuLabel.setTextSize(14f);
        tvMenuLabel.setTypeface(null, Typeface.BOLD);
        btnMenu.addView(tvMenuLabel);
        btnMenu.setOnClickListener(v -> StoreMenuBottomSheetHelper.showByStoreId(this, store.id, store.name));

        RelativeLayout btnShare = new RelativeLayout(this);
        LinearLayout.LayoutParams bsParams = new LinearLayout.LayoutParams(dpToPx(44), LinearLayout.LayoutParams.MATCH_PARENT);
        bsParams.setMarginStart(dpToPx(6));
        btnShare.setLayoutParams(bsParams);
        btnShare.setBackgroundResource(R.drawable.bg_chip_inactive);
        btnShare.setClickable(true);
        btnShare.setFocusable(true);

        ImageView shareIcon = new ImageView(this);
        RelativeLayout.LayoutParams siParams = new RelativeLayout.LayoutParams(dpToPx(20), dpToPx(20));
        siParams.addRule(RelativeLayout.CENTER_IN_PARENT);
        shareIcon.setLayoutParams(siParams);
        shareIcon.setImageResource(R.drawable.ic_share);
        btnShare.addView(shareIcon);
        btnShare.setOnClickListener(v -> shareStore(store));

        actionsRow.addView(btnFollow);
        actionsRow.addView(btnMenu);
        actionsRow.addView(btnShare);
        infoBody.addView(actionsRow);

        card.addView(infoBody);

        View.OnClickListener openDetail = v -> {
            Intent intent = new Intent(this, StoreDetailActivity.class);
            intent.putExtra("STORE_ID", store.id);
            intent.putExtra("STORE_NAME", store.name);
            startActivity(intent);
        };
        imageBox.setOnClickListener(openDetail);
        titleRow.setOnClickListener(openDetail);

        return card;
    }

    private void toggleFollowStore(FollowingRestaurantsResponse.StoreItem store, TextView btnFollow) {
        String authHeader = ApiClient.getAuthHeader(this);
        if (authHeader.isEmpty()) {
            Toast.makeText(this, "Vui lòng đăng nhập để theo dõi quán ăn", Toast.LENGTH_SHORT).show();
            return;
        }

        btnFollow.setEnabled(false);
        Call<AuthResponse> call = store.isFollowing
                ? ApiClient.getApiService().unfollowUser(authHeader, String.valueOf(store.id))
                : ApiClient.getApiService().followUser(authHeader, String.valueOf(store.id));

        call.enqueue(new Callback<AuthResponse>() {
            @Override
            public void onResponse(@NonNull Call<AuthResponse> call, @NonNull Response<AuthResponse> response) {
                btnFollow.setEnabled(true);
                if (response.isSuccessful()) {
                    store.isFollowing = !store.isFollowing;
                    btnFollow.setText(store.isFollowing ? "Đang theo dõi" : "+ Theo dõi");
                    Toast.makeText(FavoriteRestaurantsActivity.this,
                            store.isFollowing ? "Đã theo dõi " + store.name : "Đã bỏ theo dõi " + store.name,
                            Toast.LENGTH_SHORT).show();
                    if (!store.isFollowing && currentActiveChip != chipNearMe) {
                        fetchRestaurantsFromBackend();
                    }
                } else {
                    Toast.makeText(FavoriteRestaurantsActivity.this, "Không thể cập nhật trạng thái theo dõi", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<AuthResponse> call, @NonNull Throwable t) {
                btnFollow.setEnabled(true);
                Toast.makeText(FavoriteRestaurantsActivity.this, "Lỗi kết nối máy chủ", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void shareStore(FollowingRestaurantsResponse.StoreItem store) {
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("text/plain");
        String body = (store.name != null ? store.name : "Quán ăn trên Dishcision")
                + (store.address != null && !store.address.isEmpty() ? " - " + store.address : "")
                + "\nhttps://dishcision.duckdns.org/stores/" + store.id;
        shareIntent.putExtra(Intent.EXTRA_TEXT, body);
        startActivity(Intent.createChooser(shareIntent, "Chia sẻ quán ăn"));
    }

    private int dpToPx(int dp) {
        return Math.round(dp * getResources().getDisplayMetrics().density);
    }
}
