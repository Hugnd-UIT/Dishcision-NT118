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
import com.example.dishcision.network.FeedPostsResponse;
import com.example.dishcision.network.ImageLoader;
import com.example.dishcision.network.SearchAccountsResponse;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class BlockedAccountsActivity extends AppCompatActivity {

    private ImageView btnBack;
    private TextView btnAddBlocked;
    private EditText edtSearchBlocked;
    private TextView tvBlockedCount;
    private LinearLayout layoutBlockedContainer;
    private TextView tvNoBlockedAccounts;

    private final List<AccountModel> allBlocked = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_blocked_accounts);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main_blocked_accounts), (v, insets) -> {
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
        fetchBlockedAccountsFromBackend();
    }

    private void initViews() {
        btnBack = findViewById(R.id.btnBack);
        btnAddBlocked = findViewById(R.id.btnAddBlocked);
        edtSearchBlocked = findViewById(R.id.edtSearchBlocked);
        tvBlockedCount = findViewById(R.id.tvBlockedCount);
        layoutBlockedContainer = findViewById(R.id.layoutBlockedContainer);
        tvNoBlockedAccounts = findViewById(R.id.tvNoBlockedAccounts);
    }

    private void setupListeners() {
        if (btnBack != null) {
            btnBack.setOnClickListener(v -> finish());
        }

        if (btnAddBlocked != null) {
            btnAddBlocked.setOnClickListener(v -> showAddBlockedDialog());
        }

        if (edtSearchBlocked != null) {
            edtSearchBlocked.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    renderBlockedList(s.toString());
                }

                @Override
                public void afterTextChanged(Editable s) {}
            });
        }
    }

    private void fetchBlockedAccountsFromBackend() {
        String authHeader = ApiClient.getAuthHeader(this);
        if (authHeader.isEmpty()) {
            allBlocked.clear();
            renderBlockedList("");
            return;
        }

        ApiClient.getApiService().getBlockedAccounts(authHeader).enqueue(new Callback<AccountsListResponse>() {
            @Override
            public void onResponse(@NonNull Call<AccountsListResponse> call, @NonNull Response<AccountsListResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    allBlocked.clear();
                    if (response.body().getData() != null) {
                        allBlocked.addAll(response.body().getData());
                    }
                    String q = edtSearchBlocked != null ? edtSearchBlocked.getText().toString() : "";
                    renderBlockedList(q);
                } else {
                    Toast.makeText(BlockedAccountsActivity.this, "Không thể tải danh sách đã chặn", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<AccountsListResponse> call, @NonNull Throwable t) {
                Toast.makeText(BlockedAccountsActivity.this, "Lỗi kết nối máy chủ", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void unblockOnBackend(int id) {
        String authHeader = ApiClient.getAuthHeader(this);
        if (authHeader.isEmpty()) {
            Toast.makeText(this, "Vui lòng đăng nhập lại", Toast.LENGTH_SHORT).show();
            return;
        }

        AccountActionRequest req = new AccountActionRequest(id);
        ApiClient.getApiService().unblockAccount(authHeader, req).enqueue(new Callback<AccountsListResponse>() {
            @Override
            public void onResponse(@NonNull Call<AccountsListResponse> call, @NonNull Response<AccountsListResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    allBlocked.clear();
                    if (response.body().getData() != null) {
                        allBlocked.addAll(response.body().getData());
                    }
                    String q = edtSearchBlocked != null ? edtSearchBlocked.getText().toString() : "";
                    renderBlockedList(q);
                    Toast.makeText(BlockedAccountsActivity.this, "Đã bỏ chặn tài khoản", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(BlockedAccountsActivity.this, "Không thể bỏ chặn (mã " + response.code() + ")", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<AccountsListResponse> call, @NonNull Throwable t) {
                Toast.makeText(BlockedAccountsActivity.this, "Lỗi kết nối máy chủ", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void blockOnBackend(int targetId, AlertDialog dialog) {
        String authHeader = ApiClient.getAuthHeader(this);
        if (authHeader.isEmpty()) {
            Toast.makeText(this, "Vui lòng đăng nhập lại", Toast.LENGTH_SHORT).show();
            return;
        }

        AccountActionRequest req = new AccountActionRequest(targetId);
        ApiClient.getApiService().blockAccount(authHeader, req).enqueue(new Callback<AccountsListResponse>() {
            @Override
            public void onResponse(@NonNull Call<AccountsListResponse> call, @NonNull Response<AccountsListResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    allBlocked.clear();
                    if (response.body().getData() != null) {
                        allBlocked.addAll(response.body().getData());
                    }
                    String q = edtSearchBlocked != null ? edtSearchBlocked.getText().toString() : "";
                    renderBlockedList(q);
                    Toast.makeText(BlockedAccountsActivity.this, "Đã chặn tài khoản", Toast.LENGTH_SHORT).show();
                    if (dialog != null && dialog.isShowing()) {
                        dialog.dismiss();
                    }
                } else {
                    Toast.makeText(BlockedAccountsActivity.this, "Không thể chặn tài khoản (mã " + response.code() + ")", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<AccountsListResponse> call, @NonNull Throwable t) {
                Toast.makeText(BlockedAccountsActivity.this, "Lỗi kết nối máy chủ", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showAddBlockedDialog() {
        final int selfId = AuthHelper.getCurrentUserId(this);
        AccountPickerDialog.show(this,
                "Thêm tài khoản để chặn",
                "Người bị chặn không thể xem hồ sơ, bài viết hay bình luận của bạn.",
                "Chặn",
                "Không có tài khoản phù hợp",
                (keyword, page, done) -> ApiClient.getApiService()
                        .searchAccountsPage(keyword, AccountPickerDialog.PAGE_SIZE, page)
                        .enqueue(new Callback<SearchAccountsResponse>() {
                            @Override
                            public void onResponse(@NonNull Call<SearchAccountsResponse> call, @NonNull Response<SearchAccountsResponse> response) {
                                if (!response.isSuccessful() || response.body() == null || response.body().data == null) {
                                    done.done(AccountPickerDialog.Page.error());
                                    return;
                                }
                                List<AccountPickerDialog.Item> items = new ArrayList<>();
                                for (FeedPostsResponse.Author a : response.body().data) {
                                    if (a.userId <= 0 || a.userId == selfId || isAlreadyBlocked(a.userId)) continue;
                                    items.add(new AccountPickerDialog.Item(a.userId, a.username, a.fullName, a.avatarUrl));
                                }
                                done.done(new AccountPickerDialog.Page(items,
                                        response.body().data.size() >= AccountPickerDialog.PAGE_SIZE, false));
                            }

                            @Override
                            public void onFailure(@NonNull Call<SearchAccountsResponse> call, @NonNull Throwable t) {
                                done.done(AccountPickerDialog.Page.error());
                            }
                        }),
                (item, dialog) -> blockOnBackend(item.userId, dialog));
    }

    private boolean isAlreadyBlocked(int userId) {
        for (AccountModel item : allBlocked) {
            if (item.getId() == userId) return true;
        }
        return false;
    }

    private void renderBlockedList(String query) {
        if (tvBlockedCount != null) {
            tvBlockedCount.setText(allBlocked.size() + " tài khoản");
        }
        if (layoutBlockedContainer == null) return;
        layoutBlockedContainer.removeAllViews();

        String q = query != null ? query.trim().toLowerCase(Locale.ROOT) : "";
        List<AccountModel> filtered = new ArrayList<>();
        for (AccountModel item : allBlocked) {
            if (q.isEmpty()) {
                filtered.add(item);
            } else {
                String name = item.getName() != null ? item.getName().toLowerCase(Locale.ROOT) : "";
                String username = item.getUsername() != null ? item.getUsername().toLowerCase(Locale.ROOT) : "";
                if (name.contains(q) || username.contains(q)) {
                    filtered.add(item);
                }
            }
        }

        if (filtered.isEmpty()) {
            if (tvNoBlockedAccounts != null) {
                tvNoBlockedAccounts.setText(!q.isEmpty() && !allBlocked.isEmpty()
                        ? "Không tìm thấy tài khoản phù hợp"
                        : "Không có tài khoản nào bị chặn");
                tvNoBlockedAccounts.setVisibility(View.VISIBLE);
            }
            return;
        }

        if (tvNoBlockedAccounts != null) {
            tvNoBlockedAccounts.setVisibility(View.GONE);
        }

        for (int i = 0; i < filtered.size(); i++) {
            AccountModel item = filtered.get(i);
            layoutBlockedContainer.addView(createAccountRow(item));
            if (i < filtered.size() - 1) {
                layoutBlockedContainer.addView(createDivider());
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
        tvStatus.setText((item.getStatus() != null && !item.getStatus().isEmpty()) ? item.getStatus() : "Đã chặn");
        tvStatus.setTextColor(Color.parseColor("#6B7280"));
        tvStatus.setTextSize(12f);

        infoCol.addView(tvName);
        infoCol.addView(tvStatus);

        Button btnUnblock = new Button(this);
        btnUnblock.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT, dpToPx(36)));
        btnUnblock.setBackgroundResource(R.drawable.bg_card);
        btnUnblock.setPadding(dpToPx(14), 0, dpToPx(14), 0);
        btnUnblock.setText(R.string.btn_unblock);
        btnUnblock.setAllCaps(false);
        btnUnblock.setTextColor(Color.parseColor("#141B2B"));
        btnUnblock.setTextSize(13f);
        btnUnblock.setTypeface(null, Typeface.BOLD);
        btnUnblock.setOnClickListener(v -> unblockOnBackend(item.getId()));

        row.addView(infoCol);
        row.addView(btnUnblock);
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
