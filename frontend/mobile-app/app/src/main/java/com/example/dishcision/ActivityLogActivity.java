package com.example.dishcision;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.PopupMenu;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.dishcision.network.ActivityLogResponse;
import com.example.dishcision.network.ApiClient;
import com.example.dishcision.network.CurrentUserBinder;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ActivityLogActivity extends AppCompatActivity {

    private ImageView btnBack;
    private LinearLayout spinnerTime;
    private LinearLayout spinnerType;
    private TextView tvFilterTime;
    private TextView tvFilterType;
    private LinearLayout btnArchive;
    private ProgressBar progressBarActivityLog;
    private LinearLayout containerActivityLog;
    private TextView tvActivityLogEmpty;

    private Integer selectedDays = null;
    private String selectedType = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_log);
        ApiClient.init(this);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(android.R.id.content), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        initViews();
        CurrentUserBinder.bind(this, findViewById(R.id.imgAvatar), findViewById(R.id.tvProfileName), findViewById(R.id.tvProfileHandle));
        setupListeners();
        loadActivityLog();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadActivityLog();
    }

    private void initViews() {
        btnBack = findViewById(R.id.btnBack);
        spinnerTime = findViewById(R.id.spinnerTime);
        spinnerType = findViewById(R.id.spinnerType);
        tvFilterTime = findViewById(R.id.tvFilterTime);
        tvFilterType = findViewById(R.id.tvFilterType);
        btnArchive = findViewById(R.id.btnArchive);
        progressBarActivityLog = findViewById(R.id.progressBarActivityLog);
        containerActivityLog = findViewById(R.id.containerActivityLog);
        tvActivityLogEmpty = findViewById(R.id.tvActivityLogEmpty);
    }

    private void setupListeners() {
        if (btnBack != null) {
            btnBack.setOnClickListener(v -> finish());
        }

        if (spinnerTime != null) {
            spinnerTime.setOnClickListener(v -> {
                PopupMenu popup = new PopupMenu(this, spinnerTime);
                popup.getMenu().add(0, 0, 0, "Tất cả thời gian");
                popup.getMenu().add(0, 1, 1, "Hôm nay");
                popup.getMenu().add(0, 7, 2, "7 ngày qua");
                popup.getMenu().add(0, 30, 3, "30 ngày qua");
                popup.setOnMenuItemClickListener((MenuItem item) -> {
                    selectedDays = item.getItemId() > 0 ? item.getItemId() : null;
                    if (tvFilterTime != null) tvFilterTime.setText(item.getTitle());
                    loadActivityLog();
                    return true;
                });
                popup.show();
            });
        }

        if (spinnerType != null) {
            spinnerType.setOnClickListener(v -> {
                PopupMenu popup = new PopupMenu(this, spinnerType);
                popup.getMenu().add(0, 0, 0, "Tất cả hoạt động");
                popup.getMenu().add(0, 1, 1, "Bài viết");
                popup.getMenu().add(0, 2, 2, "Bình luận");
                popup.getMenu().add(0, 3, 3, "Lượt thích");
                popup.getMenu().add(0, 4, 4, "Bài đánh giá");
                popup.getMenu().add(0, 5, 5, "Bài đã lưu");
                popup.setOnMenuItemClickListener((MenuItem item) -> {
                    switch (item.getItemId()) {
                        case 1: selectedType = "post"; break;
                        case 2: selectedType = "comment"; break;
                        case 3: selectedType = "like"; break;
                        case 4: selectedType = "review"; break;
                        case 5: selectedType = "save"; break;
                        default: selectedType = null; break;
                    }
                    if (tvFilterType != null) tvFilterType.setText(item.getTitle());
                    loadActivityLog();
                    return true;
                });
                popup.show();
            });
        }

        if (btnArchive != null) {
            btnArchive.setOnClickListener(v -> startActivity(new Intent(ActivityLogActivity.this, ArchiveActivity.class)));
        }
    }

    private void loadActivityLog() {
        String auth = ApiClient.getAuthHeader(this);
        if (auth.isEmpty()) {
            renderItems(null, "Vui lòng đăng nhập để xem nhật ký hoạt động.");
            return;
        }
        if (progressBarActivityLog != null) progressBarActivityLog.setVisibility(View.VISIBLE);
        if (tvActivityLogEmpty != null) tvActivityLogEmpty.setVisibility(View.GONE);

        ApiClient.getApiService().getMyActivity(auth, selectedType, selectedDays).enqueue(new Callback<ActivityLogResponse>() {
            @Override
            public void onResponse(@NonNull Call<ActivityLogResponse> call, @NonNull Response<ActivityLogResponse> response) {
                if (isFinishing() || isDestroyed()) return;
                if (progressBarActivityLog != null) progressBarActivityLog.setVisibility(View.GONE);
                if (response.isSuccessful() && response.body() != null && response.body().data != null) {
                    renderItems(response.body().data, "Chưa có hoạt động nào phù hợp với bộ lọc.");
                } else {
                    renderItems(null, "Không thể tải nhật ký hoạt động.");
                }
            }

            @Override
            public void onFailure(@NonNull Call<ActivityLogResponse> call, @NonNull Throwable t) {
                if (isFinishing() || isDestroyed()) return;
                if (progressBarActivityLog != null) progressBarActivityLog.setVisibility(View.GONE);
                renderItems(null, "Lỗi kết nối máy chủ.");
                Toast.makeText(ActivityLogActivity.this, "Lỗi kết nối máy chủ", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void renderItems(List<ActivityLogResponse.ActivityItem> items, String emptyMsg) {
        if (containerActivityLog == null) return;
        containerActivityLog.removeAllViews();

        if (items == null || items.isEmpty()) {
            if (tvActivityLogEmpty != null) {
                tvActivityLogEmpty.setText(emptyMsg);
                tvActivityLogEmpty.setVisibility(View.VISIBLE);
            }
            return;
        }
        if (tvActivityLogEmpty != null) tvActivityLogEmpty.setVisibility(View.GONE);

        for (ActivityLogResponse.ActivityItem item : items) {
            LinearLayout card = new LinearLayout(this);
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            lp.bottomMargin = dp(10);
            card.setLayoutParams(lp);
            card.setBackgroundResource(R.drawable.bg_card_white);
            card.setGravity(Gravity.CENTER_VERTICAL);
            card.setOrientation(LinearLayout.HORIZONTAL);
            int pad = dp(14);
            card.setPadding(pad, pad, pad, pad);

            ImageView icon = new ImageView(this);
            icon.setLayoutParams(new LinearLayout.LayoutParams(dp(42), dp(42)));
            icon.setBackgroundResource(R.drawable.bg_icon_circle_light);
            int iconPad = dp(10);
            icon.setPadding(iconPad, iconPad, iconPad, iconPad);
            icon.setColorFilter(Color.parseColor("#505568"));
            if ("comment".equals(item.type)) {
                icon.setImageResource(R.drawable.ic_comment);
            } else if ("like".equals(item.type)) {
                icon.setImageResource(R.drawable.ic_heart_outline);
            } else if ("review".equals(item.type)) {
                icon.setImageResource(R.drawable.ic_thumb_up);
            } else if ("save".equals(item.type)) {
                icon.setImageResource(R.drawable.ic_bookmark);
            } else {
                icon.setImageResource(R.drawable.ic_image);
            }

            LinearLayout body = new LinearLayout(this);
            LinearLayout.LayoutParams bodyLp = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f);
            bodyLp.setMarginStart(dp(12));
            body.setLayoutParams(bodyLp);
            body.setOrientation(LinearLayout.VERTICAL);

            TextView tvTitle = new TextView(this);
            tvTitle.setText(item.title != null ? item.title : "");
            tvTitle.setTextColor(Color.parseColor("#141B2B"));
            tvTitle.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
            tvTitle.setTypeface(null, Typeface.BOLD);

            body.addView(tvTitle);

            if (item.snippet != null && !item.snippet.trim().isEmpty()) {
                TextView tvSnippet = new TextView(this);
                LinearLayout.LayoutParams sLp = new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                sLp.topMargin = dp(2);
                tvSnippet.setLayoutParams(sLp);
                tvSnippet.setText(item.snippet.trim());
                tvSnippet.setMaxLines(2);
                tvSnippet.setEllipsize(android.text.TextUtils.TruncateAt.END);
                tvSnippet.setTextColor(Color.parseColor("#505568"));
                tvSnippet.setTextSize(TypedValue.COMPLEX_UNIT_SP, 13);
                body.addView(tvSnippet);
            }

            TextView tvTime = new TextView(this);
            LinearLayout.LayoutParams tLp = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            tLp.topMargin = dp(4);
            tvTime.setLayoutParams(tLp);
            tvTime.setText(MyPostsActivity.formatTime(item.createdAt));
            tvTime.setTextColor(Color.parseColor("#8E7570"));
            tvTime.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12);
            body.addView(tvTime);

            card.addView(icon);
            card.addView(body);

            card.setOnClickListener(v -> {
                if ("review".equals(item.type)) {
                    startActivity(new Intent(ActivityLogActivity.this, MyReviewsActivity.class));
                } else if ("save".equals(item.type)) {
                    startActivity(new Intent(ActivityLogActivity.this, FavoriteDishesActivity.class));
                } else if ("post".equals(item.type)) {
                    startActivity(new Intent(ActivityLogActivity.this, MyPostsActivity.class));
                }
            });

            containerActivityLog.addView(card);
        }
    }

    private int dp(int value) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, value, getResources().getDisplayMetrics());
    }
}
