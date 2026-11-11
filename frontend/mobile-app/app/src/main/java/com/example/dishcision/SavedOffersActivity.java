package com.example.dishcision;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.dishcision.network.ApiClient;
import com.example.dishcision.network.CurrentUserBinder;
import com.example.dishcision.network.FeedPostsResponse;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SavedOffersActivity extends AppCompatActivity {

    private ImageView btnBack;
    private TextView tvOffersCountBadge;
    private ProgressBar progressBarOffers;
    private LinearLayout containerSavedOffers;
    private TextView tvSavedOffersEmpty;
    private LinearLayout containerCommunityOffers;
    private TextView tvCommunityOffersEmpty;

    private final List<FeedPostsResponse.PostItem> savedOffers = new ArrayList<>();
    private final List<FeedPostsResponse.PostItem> communityOffers = new ArrayList<>();
    private final Set<Integer> savedPostIds = new HashSet<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_saved_offers);
        ApiClient.init(this);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main_saved_offers), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        initViews();
        CurrentUserBinder.bindInline(this, findViewById(R.id.tvProfileNameHandle));
        setupListeners();
        loadAllOffers();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadAllOffers();
    }

    private void initViews() {
        btnBack = findViewById(R.id.btnBack);
        tvOffersCountBadge = findViewById(R.id.tvOffersCountBadge);
        progressBarOffers = findViewById(R.id.progressBarOffers);
        containerSavedOffers = findViewById(R.id.containerSavedOffers);
        tvSavedOffersEmpty = findViewById(R.id.tvSavedOffersEmpty);
        containerCommunityOffers = findViewById(R.id.containerCommunityOffers);
        tvCommunityOffersEmpty = findViewById(R.id.tvCommunityOffersEmpty);
    }

    private void setupListeners() {
        if (btnBack != null) {
            btnBack.setOnClickListener(v -> finish());
        }
    }

    private void loadAllOffers() {
        String auth = ApiClient.getAuthHeader(this);
        if (progressBarOffers != null) progressBarOffers.setVisibility(View.VISIBLE);

        if (auth.isEmpty()) {
            savedOffers.clear();
            savedPostIds.clear();
            renderSavedOffers();
            loadCommunityOffers(auth);
            return;
        }

        ApiClient.getApiService().getSavedPosts(auth, 50).enqueue(new Callback<FeedPostsResponse>() {
            @Override
            public void onResponse(@NonNull Call<FeedPostsResponse> call, @NonNull Response<FeedPostsResponse> response) {
                if (isFinishing() || isDestroyed()) return;
                savedOffers.clear();
                savedPostIds.clear();
                if (response.isSuccessful() && response.body() != null && response.body().getData() != null) {
                    for (FeedPostsResponse.PostItem p : response.body().getData()) {
                        if (p == null) continue;
                        savedPostIds.add(p.postId);
                        if (p.voucherCode != null && !p.voucherCode.trim().isEmpty()) {
                            p.isSaved = true;
                            savedOffers.add(p);
                        }
                    }
                }
                renderSavedOffers();
                loadCommunityOffers(auth);
            }

            @Override
            public void onFailure(@NonNull Call<FeedPostsResponse> call, @NonNull Throwable t) {
                if (isFinishing() || isDestroyed()) return;
                savedOffers.clear();
                renderSavedOffers();
                loadCommunityOffers(auth);
            }
        });
    }

    private void loadCommunityOffers(String auth) {
        ApiClient.getApiService().getFeedPostsFiltered(auth.isEmpty() ? null : auth, true, "latest", 30)
                .enqueue(new Callback<FeedPostsResponse>() {
                    @Override
                    public void onResponse(@NonNull Call<FeedPostsResponse> call, @NonNull Response<FeedPostsResponse> response) {
                        if (isFinishing() || isDestroyed()) return;
                        if (progressBarOffers != null) progressBarOffers.setVisibility(View.GONE);
                        communityOffers.clear();
                        if (response.isSuccessful() && response.body() != null && response.body().getData() != null) {
                            for (FeedPostsResponse.PostItem p : response.body().getData()) {
                                if (p != null && p.voucherCode != null && !p.voucherCode.trim().isEmpty()) {
                                    p.isSaved = p.isSaved || savedPostIds.contains(p.postId);
                                    communityOffers.add(p);
                                }
                            }
                        }
                        renderCommunityOffers();
                    }

                    @Override
                    public void onFailure(@NonNull Call<FeedPostsResponse> call, @NonNull Throwable t) {
                        if (isFinishing() || isDestroyed()) return;
                        if (progressBarOffers != null) progressBarOffers.setVisibility(View.GONE);
                        communityOffers.clear();
                        renderCommunityOffers();
                    }
                });
    }

    private void renderSavedOffers() {
        if (tvOffersCountBadge != null) {
            tvOffersCountBadge.setText(savedOffers.size() + " ưu đãi");
        }
        if (containerSavedOffers == null) return;
        containerSavedOffers.removeAllViews();

        if (savedOffers.isEmpty()) {
            if (tvSavedOffersEmpty != null) tvSavedOffersEmpty.setVisibility(View.VISIBLE);
            return;
        }
        if (tvSavedOffersEmpty != null) tvSavedOffersEmpty.setVisibility(View.GONE);
        for (FeedPostsResponse.PostItem post : savedOffers) {
            containerSavedOffers.addView(buildOfferCard(post));
        }
    }

    private void renderCommunityOffers() {
        if (containerCommunityOffers == null) return;
        containerCommunityOffers.removeAllViews();

        if (communityOffers.isEmpty()) {
            if (tvCommunityOffersEmpty != null) tvCommunityOffersEmpty.setVisibility(View.VISIBLE);
            return;
        }
        if (tvCommunityOffersEmpty != null) tvCommunityOffersEmpty.setVisibility(View.GONE);
        for (FeedPostsResponse.PostItem post : communityOffers) {
            containerCommunityOffers.addView(buildOfferCard(post));
        }
    }

    private View buildOfferCard(FeedPostsResponse.PostItem post) {
        LinearLayout card = new LinearLayout(this);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        lp.topMargin = dp(12);
        card.setLayoutParams(lp);
        card.setBackgroundResource(R.drawable.bg_card);
        card.setOrientation(LinearLayout.VERTICAL);
        int p = dp(16);
        card.setPadding(p, p, p, p);

        String tagText = PostFeedHelper.formatHashtags(post.hashtags);
        if (tagText.isEmpty()) {
            tagText = PostFeedHelper.decodeLocationDisplay(post.locationUrl);
        }
        if (tagText.isEmpty()) {
            tagText = "Ưu đãi thành viên";
        }
        TextView tvCategory = new TextView(this);
        tvCategory.setText(tagText);
        tvCategory.setTextColor(Color.parseColor("#6B7280"));
        tvCategory.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12);

        RelativeLayout titleRow = new RelativeLayout(this);
        LinearLayout.LayoutParams titleLp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        titleLp.topMargin = dp(4);
        titleRow.setLayoutParams(titleLp);

        String voucherCode = post.voucherCode != null ? post.voucherCode.trim() : "";
        TextView tvVoucherTitle = new TextView(this);
        tvVoucherTitle.setText("Mã: " + voucherCode);
        tvVoucherTitle.setTextColor(Color.parseColor("#FF5A36"));
        tvVoucherTitle.setTextSize(TypedValue.COMPLEX_UNIT_SP, 17);
        tvVoucherTitle.setTypeface(null, Typeface.BOLD);
        RelativeLayout.LayoutParams tvTitleLp = new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
        tvTitleLp.addRule(RelativeLayout.ALIGN_PARENT_START);
        tvTitleLp.addRule(RelativeLayout.CENTER_VERTICAL);
        titleRow.addView(tvVoucherTitle, tvTitleLp);

        ImageView btnSaveToggle = new ImageView(this);
        btnSaveToggle.setImageResource(post.isSaved ? R.drawable.ic_bookmark_filled : R.drawable.ic_bookmark_outline);
        btnSaveToggle.setColorFilter(Color.parseColor(post.isSaved ? "#FF5A36" : "#6B7280"));
        RelativeLayout.LayoutParams iconLp = new RelativeLayout.LayoutParams(dp(24), dp(24));
        iconLp.addRule(RelativeLayout.ALIGN_PARENT_END);
        iconLp.addRule(RelativeLayout.CENTER_VERTICAL);
        titleRow.addView(btnSaveToggle, iconLp);
        btnSaveToggle.setOnClickListener(v -> toggleSaveOffer(post));

        String authorName = post.author != null && post.author.fullName != null && !post.author.fullName.trim().isEmpty()
                ? post.author.fullName.trim()
                : (post.author != null && post.author.username != null ? "@" + post.author.username : "Đối tác Dishcision");
        TextView tvAuthor = new TextView(this);
        LinearLayout.LayoutParams authorLp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        authorLp.topMargin = dp(2);
        tvAuthor.setLayoutParams(authorLp);
        tvAuthor.setText(authorName);
        tvAuthor.setTextColor(Color.parseColor("#141B2B"));
        tvAuthor.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
        tvAuthor.setTypeface(null, Typeface.BOLD);

        TextView tvSnippet = new TextView(this);
        LinearLayout.LayoutParams snippetLp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        snippetLp.topMargin = dp(4);
        tvSnippet.setLayoutParams(snippetLp);
        tvSnippet.setText(post.content != null ? post.content.trim() : "");
        tvSnippet.setMaxLines(2);
        tvSnippet.setEllipsize(android.text.TextUtils.TruncateAt.END);
        tvSnippet.setTextColor(Color.parseColor("#505568"));
        tvSnippet.setTextSize(TypedValue.COMPLEX_UNIT_SP, 13);

        RelativeLayout bottomRow = new RelativeLayout(this);
        LinearLayout.LayoutParams bottomLp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        bottomLp.topMargin = dp(12);
        bottomRow.setLayoutParams(bottomLp);

        TextView btnCopy = new TextView(this);
        btnCopy.setText("Sao chép mã");
        btnCopy.setTextColor(Color.parseColor("#141B2B"));
        btnCopy.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12);
        btnCopy.setTypeface(null, Typeface.BOLD);
        btnCopy.setBackgroundResource(R.drawable.bg_chip_inactive);
        btnCopy.setPadding(dp(12), dp(6), dp(12), dp(6));
        btnCopy.setOnClickListener(v -> copyCode(voucherCode));
        RelativeLayout.LayoutParams copyLp = new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
        copyLp.addRule(RelativeLayout.ALIGN_PARENT_START);
        copyLp.addRule(RelativeLayout.CENTER_VERTICAL);
        bottomRow.addView(btnCopy, copyLp);

        TextView btnOpenAuthor = new TextView(this);
        btnOpenAuthor.setText(post.author != null && "RestaurantAccount".equals(post.author.role) ? "Xem quán ➔" : "Xem tác giả ➔");
        btnOpenAuthor.setTextColor(Color.parseColor("#FF5A36"));
        btnOpenAuthor.setTextSize(TypedValue.COMPLEX_UNIT_SP, 13);
        btnOpenAuthor.setTypeface(null, Typeface.BOLD);
        btnOpenAuthor.setOnClickListener(v -> openPostAuthor(post));
        RelativeLayout.LayoutParams openLp = new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
        openLp.addRule(RelativeLayout.ALIGN_PARENT_END);
        openLp.addRule(RelativeLayout.CENTER_VERTICAL);
        bottomRow.addView(btnOpenAuthor, openLp);

        card.addView(tvCategory);
        card.addView(titleRow);
        card.addView(tvAuthor);
        card.addView(tvSnippet);
        card.addView(bottomRow);
        return card;
    }

    private void copyCode(String code) {
        if (code.isEmpty()) return;
        ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
        if (clipboard != null) {
            clipboard.setPrimaryClip(ClipData.newPlainText("Voucher Code", code));
        }
        Toast.makeText(this, "Đã sao chép mã ưu đãi: " + code, Toast.LENGTH_SHORT).show();
    }

    private void openPostAuthor(FeedPostsResponse.PostItem post) {
        if (post.author == null || post.author.userId <= 0) return;
        if ("RestaurantAccount".equals(post.author.role)) {
            Intent intent = new Intent(this, StoreDetailActivity.class);
            intent.putExtra("STORE_ID", post.author.userId);
            startActivity(intent);
        } else {
            Intent intent = new Intent(this, UserProfileActivity.class);
            intent.putExtra("USER_ID", post.author.userId);
            startActivity(intent);
        }
    }

    private void toggleSaveOffer(FeedPostsResponse.PostItem post) {
        String auth = ApiClient.getAuthHeader(this);
        if (auth.isEmpty()) {
            Toast.makeText(this, "Vui lòng đăng nhập để lưu ưu đãi", Toast.LENGTH_SHORT).show();
            return;
        }
        boolean target = !post.isSaved;
        Call<Object> call = target
                ? ApiClient.getApiService().savePost(auth, post.postId)
                : ApiClient.getApiService().unsavePost(auth, post.postId);

        call.enqueue(new Callback<Object>() {
            @Override
            public void onResponse(@NonNull Call<Object> call, @NonNull Response<Object> response) {
                if (isFinishing() || isDestroyed()) return;
                if (response.isSuccessful()) {
                    Toast.makeText(SavedOffersActivity.this, target ? "Đã lưu ưu đãi!" : "Đã bỏ lưu ưu đãi", Toast.LENGTH_SHORT).show();
                    loadAllOffers();
                } else {
                    Toast.makeText(SavedOffersActivity.this, "Không thể cập nhật trạng thái lưu", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<Object> call, @NonNull Throwable t) {
                if (!isFinishing() && !isDestroyed()) {
                    Toast.makeText(SavedOffersActivity.this, "Lỗi kết nối máy chủ", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private int dp(int value) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, value, getResources().getDisplayMetrics());
    }
}
