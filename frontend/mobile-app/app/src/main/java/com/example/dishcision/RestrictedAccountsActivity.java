package com.example.dishcision;

import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.dishcision.network.AccountActionRequest;
import com.example.dishcision.network.AccountModel;
import com.example.dishcision.network.AccountsListResponse;
import com.example.dishcision.network.ApiClient;
import com.example.dishcision.network.AuthHelper;
import com.example.dishcision.network.FollowersListResponse;
import com.example.dishcision.network.ImageLoader;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class RestrictedAccountsActivity extends AppCompatActivity {

    private ImageView btnBack;
    private TextView btnAddRestricted;
    private TextView tvRestrictedCount;
    private LinearLayout layoutRestrictedContainer;
    private TextView tvNoRestrictedAccounts;

    private final List<AccountModel> allRestricted = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_restricted_accounts);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main_restricted_accounts), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        initViews();
        setupListeners();
    }

    @Override
    protected void onResume() {
        super.onResume();
        fetchRestrictedAccountsFromBackend();
    }

    private void initViews() {
        btnBack = findViewById(R.id.btnBack);
        btnAddRestricted = findViewById(R.id.btnAddRestricted);
        tvRestrictedCount = findViewById(R.id.tvRestrictedCount);
        layoutRestrictedContainer = findViewById(R.id.layoutRestrictedContainer);
        tvNoRestrictedAccounts = findViewById(R.id.tvNoRestrictedAccounts);
    }

    private void setupListeners() {
        if (btnBack != null) {
            btnBack.setOnClickListener(v -> finish());
        }

        if (btnAddRestricted != null) {
            btnAddRestricted.setOnClickListener(v -> showAddRestrictedDialog());
        }
    }

    private void fetchRestrictedAccountsFromBackend() {
        String authHeader = ApiClient.getAuthHeader(this);
        if (authHeader.isEmpty()) {
            allRestricted.clear();
            renderRestrictedList();
            return;
        }

        ApiClient.getApiService().getRestrictedAccounts(authHeader).enqueue(new Callback<AccountsListResponse>() {
            @Override
            public void onResponse(@NonNull Call<AccountsListResponse> call, @NonNull Response<AccountsListResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    allRestricted.clear();
                    if (response.body().getData() != null) {
                        allRestricted.addAll(response.body().getData());
                    }
                    renderRestrictedList();
                } else {
                    Toast.makeText(RestrictedAccountsActivity.this, "Không thể tải danh sách hạn chế", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<AccountsListResponse> call, @NonNull Throwable t) {
                Toast.makeText(RestrictedAccountsActivity.this, "Lỗi kết nối máy chủ", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void unrestrictOnBackend(int id) {
        String authHeader = ApiClient.getAuthHeader(this);
        if (authHeader.isEmpty()) {
            Toast.makeText(this, "Vui lòng đăng nhập lại", Toast.LENGTH_SHORT).show();
            return;
        }

        AccountActionRequest req = new AccountActionRequest(id);
        ApiClient.getApiService().unrestrictAccount(authHeader, req).enqueue(new Callback<AccountsListResponse>() {
            @Override
            public void onResponse(@NonNull Call<AccountsListResponse> call, @NonNull Response<AccountsListResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    allRestricted.clear();
                    if (response.body().getData() != null) {
                        allRestricted.addAll(response.body().getData());
                    }
                    renderRestrictedList();
                    Toast.makeText(RestrictedAccountsActivity.this, "Đã bỏ hạn chế tài khoản", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(RestrictedAccountsActivity.this, "Không thể bỏ hạn chế (mã " + response.code() + ")", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<AccountsListResponse> call, @NonNull Throwable t) {
                Toast.makeText(RestrictedAccountsActivity.this, "Lỗi kết nối máy chủ", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void restrictOnBackend(int targetId, AlertDialog dialog) {
        String authHeader = ApiClient.getAuthHeader(this);
        if (authHeader.isEmpty()) {
            Toast.makeText(this, "Vui lòng đăng nhập lại", Toast.LENGTH_SHORT).show();
            return;
        }

        AccountActionRequest req = new AccountActionRequest(targetId);
        ApiClient.getApiService().restrictAccount(authHeader, req).enqueue(new Callback<AccountsListResponse>() {
            @Override
            public void onResponse(@NonNull Call<AccountsListResponse> call, @NonNull Response<AccountsListResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    allRestricted.clear();
                    if (response.body().getData() != null) {
                        allRestricted.addAll(response.body().getData());
                    }
                    renderRestrictedList();
                    Toast.makeText(RestrictedAccountsActivity.this, "Đã hạn chế tài khoản", Toast.LENGTH_SHORT).show();
                    if (dialog != null && dialog.isShowing()) {
                        dialog.dismiss();
                    }
                } else {
                    Toast.makeText(RestrictedAccountsActivity.this, "Không thể hạn chế tài khoản (mã " + response.code() + ")", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<AccountsListResponse> call, @NonNull Throwable t) {
                Toast.makeText(RestrictedAccountsActivity.this, "Lỗi kết nối máy chủ", Toast.LENGTH_SHORT).show();
            }
        });
    }

    // "Bạn bè" = những người mình đang theo dõi (cùng định nghĩa với quyền riêng tư "Bạn bè" của app)
    private List<FollowersListResponse.FollowItem> friendsCache;

    private AccountPickerDialog.Page friendsPage(String keyword, int page) {
        String kw = keyword.toLowerCase(Locale.ROOT);
        int selfId = AuthHelper.getCurrentUserId(this);
        List<AccountPickerDialog.Item> all = new ArrayList<>();
        for (FollowersListResponse.FollowItem f : friendsCache) {
            if (f.userId <= 0 || f.userId == selfId || isAlreadyRestricted(f.userId)) continue;
            String name = (f.fullName != null ? f.fullName : "") + " " + (f.username != null ? f.username : "");
            if (!kw.isEmpty() && !name.toLowerCase(Locale.ROOT).contains(kw.replace("@", ""))) continue;
            all.add(new AccountPickerDialog.Item(f.userId, f.username, f.fullName, f.avatarUrl));
        }
        int from = Math.min((page - 1) * AccountPickerDialog.PAGE_SIZE, all.size());
        int to = Math.min(from + AccountPickerDialog.PAGE_SIZE, all.size());
        return new AccountPickerDialog.Page(new ArrayList<>(all.subList(from, to)), to < all.size(), false);
    }

    private void showAddRestrictedDialog() {
        AccountPickerDialog.show(this,
                "Thêm tài khoản hạn chế",
                "Chỉ có thể hạn chế những người bạn đã kết bạn.",
                "Hạn chế",
                "Không có người bạn nào phù hợp",
                (keyword, page, done) -> {
                    if (friendsCache != null) {
                        done.done(friendsPage(keyword, page));
                        return;
                    }
                    ApiClient.getApiService().getFollowing(ApiClient.getAuthHeader(this), "me")
                            .enqueue(new Callback<FollowersListResponse>() {
                                @Override
                                public void onResponse(@NonNull Call<FollowersListResponse> call, @NonNull Response<FollowersListResponse> response) {
                                    if (!response.isSuccessful() || response.body() == null || response.body().data == null) {
                                        done.done(AccountPickerDialog.Page.error());
                                        return;
                                    }
                                    friendsCache = response.body().data;
                                    done.done(friendsPage(keyword, page));
                                }

                                @Override
                                public void onFailure(@NonNull Call<FollowersListResponse> call, @NonNull Throwable t) {
                                    done.done(AccountPickerDialog.Page.error());
                                }
                            });
                },
                (item, dialog) -> restrictOnBackend(item.userId, dialog));
    }

    private boolean isAlreadyRestricted(int userId) {
        for (AccountModel item : allRestricted) {
            if (item.getId() == userId) return true;
        }
        return false;
    }

    private void renderRestrictedList() {
        if (tvRestrictedCount != null) {
            tvRestrictedCount.setText(allRestricted.size() + " tài khoản");
        }
        if (layoutRestrictedContainer == null) return;
        layoutRestrictedContainer.removeAllViews();

        if (allRestricted.isEmpty()) {
            if (tvNoRestrictedAccounts != null) tvNoRestrictedAccounts.setVisibility(View.VISIBLE);
            return;
        }
        if (tvNoRestrictedAccounts != null) tvNoRestrictedAccounts.setVisibility(View.GONE);

        for (int i = 0; i < allRestricted.size(); i++) {
            layoutRestrictedContainer.addView(createAccountRow(allRestricted.get(i)));
            if (i < allRestricted.size() - 1) {
                layoutRestrictedContainer.addView(createDivider());
            }
        }
    }

    private View createAccountRow(AccountModel item) {
        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(Gravity.CENTER_VERTICAL);
        int pad = dpToPx(16);
        row.setPadding(pad, pad, pad, pad);

        if (item.getAvatarUrl() != null && !item.getAvatarUrl().trim().isEmpty()) {
            ImageView img = new ImageView(this);
            img.setLayoutParams(new LinearLayout.LayoutParams(dpToPx(40), dpToPx(40)));
            img.setScaleType(ImageView.ScaleType.CENTER_CROP);
            img.setBackgroundResource(R.drawable.bg_avatar_circle);
            ImageLoader.loadAvatar(img, item.getAvatarUrl());
            row.addView(img);
        } else {
            TextView tvInitials = new TextView(this);
            tvInitials.setLayoutParams(new LinearLayout.LayoutParams(dpToPx(40), dpToPx(40)));
            tvInitials.setBackgroundResource(R.drawable.bg_avatar_initials);
            tvInitials.setGravity(Gravity.CENTER);
            tvInitials.setText(resolveInitials(item));
            tvInitials.setTextColor(Color.parseColor("#141B2B"));
            tvInitials.setTextSize(14f);
            tvInitials.setTypeface(null, Typeface.BOLD);
            row.addView(tvInitials);
        }

        LinearLayout infoCol = new LinearLayout(this);
        infoCol.setOrientation(LinearLayout.VERTICAL);
        LinearLayout.LayoutParams infoParams = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f);
        infoParams.setMarginStart(dpToPx(12));
        infoCol.setLayoutParams(infoParams);

        TextView tvName = new TextView(this);
        String displayName = (item.getName() != null && !item.getName().isEmpty())
                ? item.getName()
                : (item.getUsername() != null ? "@" + item.getUsername() : "Người dùng #" + item.getId());
        tvName.setText(displayName);
        tvName.setTextColor(Color.parseColor("#141B2B"));
        tvName.setTextSize(15f);
        tvName.setTypeface(null, Typeface.BOLD);

        TextView tvStatus = new TextView(this);
        tvStatus.setText((item.getStatus() != null && !item.getStatus().isEmpty()) ? item.getStatus() : "Đã hạn chế");
        tvStatus.setTextColor(Color.parseColor("#6B7280"));
        tvStatus.setTextSize(12f);

        infoCol.addView(tvName);
        infoCol.addView(tvStatus);

        Button btnUnrestrict = new Button(this);
        btnUnrestrict.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT, dpToPx(36)));
        btnUnrestrict.setBackgroundResource(R.drawable.bg_card);
        btnUnrestrict.setPadding(dpToPx(14), 0, dpToPx(14), 0);
        btnUnrestrict.setText("Bỏ hạn chế");
        btnUnrestrict.setAllCaps(false);
        btnUnrestrict.setTextColor(Color.parseColor("#141B2B"));
        btnUnrestrict.setTextSize(13f);
        btnUnrestrict.setTypeface(null, Typeface.BOLD);
        btnUnrestrict.setOnClickListener(v -> unrestrictOnBackend(item.getId()));

        row.addView(infoCol);
        row.addView(btnUnrestrict);
        return row;
    }

    private String resolveInitials(AccountModel item) {
        if (item.getInitials() != null && !item.getInitials().trim().isEmpty()) {
            return item.getInitials().trim();
        }
        String src = item.getName() != null && !item.getName().trim().isEmpty() ? item.getName().trim() : item.getUsername();
        if (src == null || src.isEmpty()) return "?";
        String[] parts = src.split("\\s+");
        if (parts.length >= 2) {
            return ("" + parts[0].charAt(0) + parts[parts.length - 1].charAt(0)).toUpperCase(Locale.ROOT);
        }
        return src.substring(0, Math.min(2, src.length())).toUpperCase(Locale.ROOT);
    }

    private View createDivider() {
        View divider = new View(this);
        divider.setBackgroundColor(Color.parseColor("#E9EBF4"));
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, dpToPx(1));
        params.setMarginStart(dpToPx(16));
        params.setMarginEnd(dpToPx(16));
        divider.setLayoutParams(params);
        return divider;
    }

    private int dpToPx(int dp) {
        float density = getResources().getDisplayMetrics().density;
        return Math.round((float) dp * density);
    }
}
