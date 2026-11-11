package com.example.dishcision;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.inputmethod.EditorInfo;
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
import com.example.dishcision.network.DishModel;
import com.example.dishcision.network.DishesResponse;
import com.example.dishcision.network.ImageLoader;
import com.example.dishcision.network.TrendingResponse;

import org.json.JSONArray;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SearchActivity extends AppCompatActivity {

    private static final String SEARCH_PREFS = "DishcisionSearchPrefs";
    private static final String KEY_HISTORY = "search_history";
    private static final int MAX_HISTORY = 8;

    private ImageView btnBack;
    private EditText edtSearchQuery;
    private ImageView btnClearSearch;
    private TextView btnDoSearch;

    private HorizontalScrollView scrollSuggestionChips;
    private LinearLayout layoutSuggestionContainer;

    private LinearLayout cardBestMatch;
    private ImageView imgBestMatchDish;
    private TextView tvBestMatchCategory;
    private TextView tvBestMatchName;
    private TextView tvBestMatchMeta;
    private TextView tvBestMatchPrice;

    private LinearLayout layoutHistorySection;
    private LinearLayout layoutHistoryContainer;
    private TextView btnClearHistory;

    private LinearLayout layoutTrendingContainer;
    private TextView tvEmptyTrending;
    private RelativeLayout btnSpinWheel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_search);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main_search), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        ApiClient.init(this);
        initViews();
        setupListeners();
        renderHistory();
        fetchTrendingAndSuggestions();
        fetchFeaturedDish();
    }

    @Override
    protected void onResume() {
        super.onResume();
        renderHistory();
    }

    private void initViews() {
        btnBack = findViewById(R.id.btnBack);
        edtSearchQuery = findViewById(R.id.edtSearchQuery);
        btnClearSearch = findViewById(R.id.btnClearSearch);
        btnDoSearch = findViewById(R.id.btnDoSearch);

        scrollSuggestionChips = findViewById(R.id.scrollSuggestionChips);
        layoutSuggestionContainer = findViewById(R.id.layoutSuggestionContainer);

        cardBestMatch = findViewById(R.id.cardBestMatch);
        imgBestMatchDish = findViewById(R.id.imgBestMatchDish);
        tvBestMatchCategory = findViewById(R.id.tvBestMatchCategory);
        tvBestMatchName = findViewById(R.id.tvBestMatchName);
        tvBestMatchMeta = findViewById(R.id.tvBestMatchMeta);
        tvBestMatchPrice = findViewById(R.id.tvBestMatchPrice);

        layoutHistorySection = findViewById(R.id.layoutHistorySection);
        layoutHistoryContainer = findViewById(R.id.layoutHistoryContainer);
        btnClearHistory = findViewById(R.id.btnClearHistory);

        layoutTrendingContainer = findViewById(R.id.layoutTrendingContainer);
        tvEmptyTrending = findViewById(R.id.tvEmptyTrending);
        btnSpinWheel = findViewById(R.id.btnSpinWheel);
    }

    private void setupListeners() {
        if (btnBack != null) {
            btnBack.setOnClickListener(v -> finish());
        }

        if (btnClearSearch != null && edtSearchQuery != null) {
            btnClearSearch.setOnClickListener(v -> edtSearchQuery.setText(""));
        }

        if (btnDoSearch != null) {
            btnDoSearch.setOnClickListener(v -> submitCurrentQuery());
        }

        if (edtSearchQuery != null) {
            edtSearchQuery.setOnEditorActionListener((v, actionId, event) -> {
                if (actionId == EditorInfo.IME_ACTION_SEARCH || actionId == EditorInfo.IME_ACTION_DONE) {
                    submitCurrentQuery();
                    return true;
                }
                return false;
            });
        }

        if (btnClearHistory != null) {
            btnClearHistory.setOnClickListener(v -> {
                saveHistoryList(new ArrayList<>());
                renderHistory();
            });
        }

        if (btnSpinWheel != null) {
            btnSpinWheel.setOnClickListener(v -> startActivity(new Intent(SearchActivity.this, DecideActivity.class)));
        }
    }

    private void submitCurrentQuery() {
        String query = edtSearchQuery != null && edtSearchQuery.getText() != null
                ? edtSearchQuery.getText().toString().trim()
                : "";
        if (query.isEmpty()) {
            Toast.makeText(this, "Vui lòng nhập từ khóa tìm kiếm", Toast.LENGTH_SHORT).show();
            return;
        }
        executeSearch(query);
    }

    private void executeSearch(String query) {
        if (query == null || query.trim().isEmpty()) return;
        String clean = query.trim();
        addToHistory(clean);
        Intent intent = new Intent(SearchActivity.this, SearchResultsActivity.class);
        intent.putExtra("SEARCH_QUERY", clean);
        startActivity(intent);
    }

    private void fetchFeaturedDish() {
        ApiClient.getApiService().getDishes(null, null, null, 1).enqueue(new Callback<DishesResponse>() {
            @Override
            public void onResponse(@NonNull Call<DishesResponse> call, @NonNull Response<DishesResponse> response) {
                if (isFinishing() || isDestroyed()) return;
                if (response.isSuccessful() && response.body() != null
                        && response.body().data != null && !response.body().data.isEmpty()) {
                    bindFeaturedDish(response.body().data.get(0));
                } else if (cardBestMatch != null) {
                    cardBestMatch.setVisibility(View.GONE);
                }
            }

            @Override
            public void onFailure(@NonNull Call<DishesResponse> call, @NonNull Throwable t) {
                if (cardBestMatch != null) cardBestMatch.setVisibility(View.GONE);
            }
        });
    }

    private void bindFeaturedDish(DishModel dish) {
        if (dish == null || cardBestMatch == null) return;
        cardBestMatch.setVisibility(View.VISIBLE);
        if (tvBestMatchName != null) tvBestMatchName.setText(dish.name != null ? dish.name : "");
        if (tvBestMatchCategory != null) {
            tvBestMatchCategory.setText(dish.category != null && !dish.category.isEmpty() ? dish.category : "");
        }
        if (tvBestMatchMeta != null) {
            String rating = String.format(Locale.US, "★ %.1f (%d)", dish.rating, Math.max(0, dish.reviewsCount));
            if (dish.storeName != null && !dish.storeName.trim().isEmpty()) {
                tvBestMatchMeta.setText(rating + " • " + dish.storeName.trim());
            } else {
                tvBestMatchMeta.setText(rating);
            }
        }
        if (tvBestMatchPrice != null) {
            String priceText = dish.formattedPrice != null && !dish.formattedPrice.isEmpty()
                    ? dish.formattedPrice
                    : formatPrice(dish.price);
            tvBestMatchPrice.setText(priceText);
        }
        if (imgBestMatchDish != null) {
            imgBestMatchDish.setImageResource(R.drawable.logo);
            if (dish.imageUrl != null && !dish.imageUrl.trim().isEmpty()) {
                ImageLoader.loadImage(imgBestMatchDish, dish.imageUrl);
            }
        }
        cardBestMatch.setOnClickListener(v -> {
            Intent intent = new Intent(SearchActivity.this, DishDetailActivity.class);
            intent.putExtra("DISH_ID", dish.id);
            startActivity(intent);
        });
    }

    private void fetchTrendingAndSuggestions() {
        ApiClient.getApiService().getTrending().enqueue(new Callback<TrendingResponse>() {
            @Override
            public void onResponse(@NonNull Call<TrendingResponse> call, @NonNull Response<TrendingResponse> response) {
                if (isFinishing() || isDestroyed()) return;
                if (response.isSuccessful() && response.body() != null && response.body().data != null) {
                    renderTrendingData(response.body().data);
                } else {
                    showEmptyTrending();
                }
            }

            @Override
            public void onFailure(@NonNull Call<TrendingResponse> call, @NonNull Throwable t) {
                if (!isFinishing() && !isDestroyed()) {
                    showEmptyTrending();
                }
            }
        });
    }

    private void renderTrendingData(TrendingResponse.TrendingData data) {
        List<String> suggestions = new ArrayList<>();
        if (data.trendingDishes != null) {
            for (String d : data.trendingDishes) {
                if (d != null && !d.trim().isEmpty() && !suggestions.contains(d.trim())) {
                    suggestions.add(d.trim());
                }
            }
        }

        if (scrollSuggestionChips != null && layoutSuggestionContainer != null) {
            layoutSuggestionContainer.removeAllViews();
            List<String> chipLabels = new ArrayList<>(suggestions);
            if (chipLabels.isEmpty() && data.trendingHashtags != null) {
                for (TrendingResponse.HashtagItem item : data.trendingHashtags) {
                    if (item != null && item.tag != null && !item.tag.trim().isEmpty()) {
                        chipLabels.add(item.tag.replace("#", "").trim());
                    }
                }
            }
            if (chipLabels.isEmpty()) {
                scrollSuggestionChips.setVisibility(View.GONE);
            } else {
                scrollSuggestionChips.setVisibility(View.VISIBLE);
                int limit = Math.min(6, chipLabels.size());
                for (int i = 0; i < limit; i++) {
                    String term = chipLabels.get(i);
                    TextView chip = new TextView(this);
                    chip.setText(term);
                    chip.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12);
                    chip.setTextColor(ContextCompat.getColor(this, R.color.text_dark));
                    chip.setBackgroundResource(R.drawable.bg_chip_inactive);
                    chip.setPadding(dp(14), dp(6), dp(14), dp(6));
                    chip.setClickable(true);
                    chip.setFocusable(true);
                    LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.WRAP_CONTENT,
                            LinearLayout.LayoutParams.WRAP_CONTENT
                    );
                    if (i > 0) lp.setMarginStart(dp(8));
                    chip.setLayoutParams(lp);
                    chip.setOnClickListener(v -> executeSearch(term));
                    layoutSuggestionContainer.addView(chip);
                }
            }
        }

        if (layoutTrendingContainer == null) return;
        layoutTrendingContainer.removeAllViews();

        List<String> trendingTerms = new ArrayList<>(suggestions);
        if (trendingTerms.isEmpty() && data.trendingHashtags != null) {
            for (TrendingResponse.HashtagItem item : data.trendingHashtags) {
                if (item != null && item.tag != null && !item.tag.trim().isEmpty()) {
                    trendingTerms.add(item.tag.replace("#", "").trim());
                }
            }
        }

        if (trendingTerms.isEmpty()) {
            showEmptyTrending();
            return;
        }
        if (tvEmptyTrending != null) tvEmptyTrending.setVisibility(View.GONE);

        int limit = Math.min(6, trendingTerms.size());
        for (int i = 0; i < limit; i++) {
            final int rank = i + 1;
            final String term = trendingTerms.get(i);

            RelativeLayout row = new RelativeLayout(this);
            row.setPadding(0, dp(10), 0, dp(10));
            row.setClickable(true);
            row.setFocusable(true);
            row.setOnClickListener(v -> executeSearch(term));

            TextView tvTerm = new TextView(this);
            tvTerm.setText(rank + "  " + term);
            tvTerm.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
            tvTerm.setTextColor(ContextCompat.getColor(this, R.color.text_dark));
            if (rank == 1) tvTerm.setTypeface(null, Typeface.BOLD);
            RelativeLayout.LayoutParams lpLeft = new RelativeLayout.LayoutParams(
                    RelativeLayout.LayoutParams.WRAP_CONTENT,
                    RelativeLayout.LayoutParams.WRAP_CONTENT
            );
            lpLeft.addRule(RelativeLayout.ALIGN_PARENT_START);
            lpLeft.addRule(RelativeLayout.CENTER_VERTICAL);
            row.addView(tvTerm, lpLeft);

            TextView tvArrow = new TextView(this);
            tvArrow.setText("↗");
            tvArrow.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
            tvArrow.setTextColor(ContextCompat.getColor(this, R.color.text_gray));
            RelativeLayout.LayoutParams lpRight = new RelativeLayout.LayoutParams(
                    RelativeLayout.LayoutParams.WRAP_CONTENT,
                    RelativeLayout.LayoutParams.WRAP_CONTENT
            );
            lpRight.addRule(RelativeLayout.ALIGN_PARENT_END);
            lpRight.addRule(RelativeLayout.CENTER_VERTICAL);
            row.addView(tvArrow, lpRight);

            layoutTrendingContainer.addView(row);
        }
    }

    private void showEmptyTrending() {
        if (layoutTrendingContainer != null) layoutTrendingContainer.removeAllViews();
        if (tvEmptyTrending != null) tvEmptyTrending.setVisibility(View.VISIBLE);
    }

    private void renderHistory() {
        List<String> history = loadHistoryList();
        if (layoutHistorySection == null || layoutHistoryContainer == null) return;
        layoutHistoryContainer.removeAllViews();

        if (history.isEmpty()) {
            layoutHistorySection.setVisibility(View.GONE);
            return;
        }
        layoutHistorySection.setVisibility(View.VISIBLE);

        LinearLayout currentRow = null;
        for (int i = 0; i < history.size(); i++) {
            if (i % 2 == 0) {
                currentRow = new LinearLayout(this);
                currentRow.setOrientation(LinearLayout.HORIZONTAL);
                LinearLayout.LayoutParams rowLp = new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                );
                if (i > 0) rowLp.topMargin = dp(8);
                currentRow.setLayoutParams(rowLp);
                layoutHistoryContainer.addView(currentRow);
            }

            final String term = history.get(i);
            LinearLayout chip = new LinearLayout(this);
            chip.setOrientation(LinearLayout.HORIZONTAL);
            chip.setGravity(Gravity.CENTER_VERTICAL);
            chip.setBackgroundResource(R.drawable.bg_chip_inactive);
            chip.setPadding(dp(12), dp(6), dp(10), dp(6));

            LinearLayout.LayoutParams chipLp = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            );
            if (i % 2 == 1) chipLp.setMarginStart(dp(8));
            chip.setLayoutParams(chipLp);

            TextView tvTerm = new TextView(this);
            tvTerm.setText(term);
            tvTerm.setTextColor(ContextCompat.getColor(this, R.color.text_dark));
            tvTerm.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12);
            tvTerm.setMaxWidth(dp(180));
            tvTerm.setSingleLine(true);
            tvTerm.setOnClickListener(v -> executeSearch(term));
            chip.addView(tvTerm);

            TextView tvRemove = new TextView(this);
            tvRemove.setText(" ✕");
            tvRemove.setTextColor(ContextCompat.getColor(this, R.color.text_gray));
            tvRemove.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12);
            tvRemove.setPadding(dp(6), 0, 0, 0);
            tvRemove.setOnClickListener(v -> {
                removeFromHistory(term);
                renderHistory();
            });
            chip.addView(tvRemove);

            currentRow.addView(chip);
        }
    }

    private List<String> loadHistoryList() {
        List<String> list = new ArrayList<>();
        SharedPreferences prefs = getSharedPreferences(SEARCH_PREFS, MODE_PRIVATE);
        String raw = prefs.getString(KEY_HISTORY, "");
        if (raw == null || raw.isEmpty()) return list;
        try {
            JSONArray arr = new JSONArray(raw);
            for (int i = 0; i < arr.length(); i++) {
                String item = arr.optString(i, "").trim();
                if (!item.isEmpty()) list.add(item);
            }
        } catch (Exception ignored) {}
        return list;
    }

    private void saveHistoryList(List<String> list) {
        JSONArray arr = new JSONArray();
        for (String item : list) {
            arr.put(item);
        }
        getSharedPreferences(SEARCH_PREFS, MODE_PRIVATE)
                .edit()
                .putString(KEY_HISTORY, arr.toString())
                .apply();
    }

    private void addToHistory(String term) {
        List<String> list = loadHistoryList();
        for (int i = list.size() - 1; i >= 0; i--) {
            if (list.get(i).equalsIgnoreCase(term)) {
                list.remove(i);
            }
        }
        list.add(0, term);
        while (list.size() > MAX_HISTORY) {
            list.remove(list.size() - 1);
        }
        saveHistoryList(list);
    }

    private void removeFromHistory(String term) {
        List<String> list = loadHistoryList();
        for (int i = list.size() - 1; i >= 0; i--) {
            if (list.get(i).equalsIgnoreCase(term)) {
                list.remove(i);
            }
        }
        saveHistoryList(list);
    }

    private String formatPrice(int price) {
        if (price <= 0) return "";
        return String.format(Locale.US, "%,dđ", price).replace(",", ".");
    }

    private int dp(int value) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, value, getResources().getDisplayMetrics());
    }
}
