package com.example.dishcision;

import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.dishcision.network.ApiClient;
import com.example.dishcision.network.AuthHelper;
import com.example.dishcision.network.AuthResponse;
import com.example.dishcision.network.FollowersListResponse;
import com.example.dishcision.network.TokenManager;
import com.example.dishcision.network.UserProfileResponse;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class FollowersActivity extends AppCompatActivity {

    private ImageView btnBack;
    private TextView tvFollowersHeaderName;
    private TextView tvFollowersHeaderHandle;
    private TextView tvFollowersCountVal;
    private TextView tvFollowingCountVal;

    private LinearLayout tabFollowers;
    private LinearLayout tabFollowing;
    private TextView tvTabFollowers;
    private TextView tvTabFollowing;

    private EditText edtSearchFollowers;
    private ProgressBar progressBarFollowers;
    private LinearLayout layoutUsersList;
    private LinearLayout layoutEmptyState;
    private ImageView ivEmptyIcon;
    private TextView tvEmptyTitle;
    private TextView tvEmptySubtitle;

    private String targetUserId = "me";
    private boolean isTabFollowers = true;
    private int followersCount = 0;
    private int followingCount = 0;
    private final List<FollowersListResponse.FollowItem> currentUsers = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_followers);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(android.R.id.content), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        String extraUserId = getIntent().getStringExtra("USER_ID");
        if (extraUserId == null || extraUserId.trim().isEmpty()) {
            int intId = getIntent().getIntExtra("USER_ID", 0);
            if (intId > 0) extraUserId = String.valueOf(intId);
        }
        if (extraUserId != null && !extraUserId.trim().isEmpty()) {
            targetUserId = extraUserId.trim();
        }

        initViews();
        loadHeaderData();
        setupTabs();
        setupSearch();

        if (btnBack != null) {
            btnBack.setOnClickListener(v -> finish());
        }

        String activeTab = getIntent().getStringExtra("ACTIVE_TAB");
        if ("following".equalsIgnoreCase(activeTab)) {
            switchToTab(false);
        } else {
            switchToTab(true);
        }
    }

    private void initViews() {
        btnBack = findViewById(R.id.btnBack);
        tvFollowersHeaderName = findViewById(R.id.tvFollowersHeaderName);
        tvFollowersHeaderHandle = findViewById(R.id.tvFollowersHeaderHandle);
        tvFollowersCountVal = findViewById(R.id.tvFollowersCountVal);
        tvFollowingCountVal = findViewById(R.id.tvFollowingCountVal);

        tabFollowers = findViewById(R.id.tabFollowers);
        tabFollowing = findViewById(R.id.tabFollowing);
        tvTabFollowers = findViewById(R.id.tvTabFollowers);
        tvTabFollowing = findViewById(R.id.tvTabFollowing);

        edtSearchFollowers = findViewById(R.id.edtSearchFollowers);
        progressBarFollowers = findViewById(R.id.progressBarFollowers);
        layoutUsersList = findViewById(R.id.layoutUsersList);
        layoutEmptyState = findViewById(R.id.layoutEmptyState);
        ivEmptyIcon = findViewById(R.id.ivEmptyIcon);
        tvEmptyTitle = findViewById(R.id.tvEmptyTitle);
        tvEmptySubtitle = findViewById(R.id.tvEmptySubtitle);
    }

    private void loadHeaderData() {
        Intent intent = getIntent();
        String nameExtra = intent.getStringExtra("DISPLAY_NAME");
        String usernameExtra = intent.getStringExtra("USERNAME");
        String followersExtra = intent.getStringExtra("FOLLOWERS_COUNT");
        String followingExtra = intent.getStringExtra("FOLLOWING_COUNT");

        if (nameExtra != null && !nameExtra.isEmpty() && tvFollowersHeaderName != null) {
            tvFollowersHeaderName.setText(nameExtra);
        }
        if (usernameExtra != null && !usernameExtra.isEmpty() && tvFollowersHeaderHandle != null) {
            tvFollowersHeaderHandle.setText(usernameExtra.startsWith("@") ? usernameExtra : "@" + usernameExtra);
        }
        if (followersExtra != null && tvFollowersCountVal != null) {
            try {
                followersCount = Integer.parseInt(followersExtra.replaceAll("[^0-9]", ""));
            } catch (Exception ignored) {}
            tvFollowersCountVal.setText(followersExtra);
        }
        if (followingExtra != null && tvFollowingCountVal != null) {
            try {
                followingCount = Integer.parseInt(followingExtra.replaceAll("[^0-9]", ""));
            } catch (Exception ignored) {}
            tvFollowingCountVal.setText(followingExtra);
        }

        String auth = ApiClient.getAuthHeader(this);
        if (auth.isEmpty()) return;

        Call<UserProfileResponse> profileCall = "me".equals(targetUserId)
                ? ApiClient.getApiService().getMyProfile(auth)
                : ApiClient.getApiService().getUserProfile(auth, targetUserId);

        profileCall.enqueue(new Callback<UserProfileResponse>() {
            @Override
            public void onResponse(@NonNull Call<UserProfileResponse> call, @NonNull Response<UserProfileResponse> response) {
                if (isFinishing() || isDestroyed()) return;
                if (response.isSuccessful() && response.body() != null && response.body().getData() != null) {
                    UserProfileResponse.UserData user = response.body().getData();
                    if (tvFollowersHeaderName != null) {
                        tvFollowersHeaderName.setText(user.fullName != null && !user.fullName.trim().isEmpty() ? user.fullName : user.username);
                    }
                    if (tvFollowersHeaderHandle != null && user.username != null && !user.username.isEmpty()) {
                        tvFollowersHeaderHandle.setText("@" + user.username.replace("@", ""));
                    }
                    followersCount = user.followersCount;
                    followingCount = user.followingCount;
                    if (tvFollowersCountVal != null) {
                        tvFollowersCountVal.setText(PostFeedHelper.formatCount(followersCount));
                    }
                    if (tvFollowingCountVal != null) {
                        tvFollowingCountVal.setText(PostFeedHelper.formatCount(followingCount));
                    }
                }
            }

            @Override
            public void onFailure(@NonNull Call<UserProfileResponse> call, @NonNull Throwable t) {}
        });
    }

    private void setupTabs() {
        if (tabFollowers != null) {
            tabFollowers.setOnClickListener(v -> switchToTab(true));
        }
        if (tabFollowing != null) {
            tabFollowing.setOnClickListener(v -> switchToTab(false));
        }
    }

    private void switchToTab(boolean followers) {
        this.isTabFollowers = followers;
        float density = getResources().getDisplayMetrics().density;

        if (isTabFollowers) {
            if (tabFollowers != null) {
                tabFollowers.setBackgroundResource(R.drawable.bg_tab_active_white);
                tabFollowers.setElevation(1f * density);
            }
            if (tvTabFollowers != null) {
                tvTabFollowers.setTextColor(0xFFFF5A36);
                tvTabFollowers.setTypeface(null, Typeface.BOLD);
            }
            if (tabFollowing != null) {
                tabFollowing.setBackground(null);
                tabFollowing.setElevation(0f);
            }
            if (tvTabFollowing != null) {
                tvTabFollowing.setTextColor(0xFF505568);
                tvTabFollowing.setTypeface(null, Typeface.NORMAL);
            }
            if (edtSearchFollowers != null) {
                edtSearchFollowers.setHint("Tìm trong người theo dõi");
            }
        } else {
            if (tabFollowing != null) {
                tabFollowing.setBackgroundResource(R.drawable.bg_tab_active_white);
                tabFollowing.setElevation(1f * density);
            }
            if (tvTabFollowing != null) {
                tvTabFollowing.setTextColor(0xFFFF5A36);
                tvTabFollowing.setTypeface(null, Typeface.BOLD);
            }
            if (tabFollowers != null) {
                tabFollowers.setBackground(null);
                tabFollowers.setElevation(0f);
            }
            if (tvTabFollowers != null) {
                tvTabFollowers.setTextColor(0xFF505568);
                tvTabFollowers.setTypeface(null, Typeface.NORMAL);
            }
            if (edtSearchFollowers != null) {
                edtSearchFollowers.setHint("Tìm trong người đang theo dõi");
            }
        }

        loadTabUsers();
    }

    private void loadTabUsers() {
        if (progressBarFollowers != null) progressBarFollowers.setVisibility(View.VISIBLE);
        if (layoutEmptyState != null) layoutEmptyState.setVisibility(View.GONE);
        if (layoutUsersList != null) layoutUsersList.removeAllViews();

        String auth = ApiClient.getAuthHeader(this);
        if (auth.isEmpty()) {
            if (progressBarFollowers != null) progressBarFollowers.setVisibility(View.GONE);
            currentUsers.clear();
            applyFilterAndRender();
            return;
        }

        Call<FollowersListResponse> call = isTabFollowers
                ? ApiClient.getApiService().getFollowers(auth, targetUserId)
                : ApiClient.getApiService().getFollowing(auth, targetUserId);

        call.enqueue(new Callback<FollowersListResponse>() {
            @Override
            public void onResponse(@NonNull Call<FollowersListResponse> call, @NonNull Response<FollowersListResponse> response) {
                if (isFinishing() || isDestroyed()) return;
                if (progressBarFollowers != null) progressBarFollowers.setVisibility(View.GONE);
                currentUsers.clear();
                if (response.isSuccessful() && response.body() != null && response.body().data != null) {
                    currentUsers.addAll(response.body().data);
                    if (isTabFollowers) {
                        followersCount = currentUsers.size();
                        if (tvFollowersCountVal != null) tvFollowersCountVal.setText(PostFeedHelper.formatCount(followersCount));
                    } else {
                        followingCount = currentUsers.size();
                        if (tvFollowingCountVal != null) tvFollowingCountVal.setText(PostFeedHelper.formatCount(followingCount));
                    }
                }
                applyFilterAndRender();
            }

            @Override
            public void onFailure(@NonNull Call<FollowersListResponse> call, @NonNull Throwable t) {
                if (isFinishing() || isDestroyed()) return;
                if (progressBarFollowers != null) progressBarFollowers.setVisibility(View.GONE);
                currentUsers.clear();
                applyFilterAndRender();
            }
        });
    }

    private void setupSearch() {
        if (edtSearchFollowers != null) {
            edtSearchFollowers.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    applyFilterAndRender();
                }

                @Override
                public void afterTextChanged(Editable s) {}
            });
        }
    }

    private void applyFilterAndRender() {
        String query = "";
        if (edtSearchFollowers != null && edtSearchFollowers.getText() != null) {
            query = edtSearchFollowers.getText().toString().trim().toLowerCase(Locale.ROOT);
        }

        List<FollowersListResponse.FollowItem> filtered = new ArrayList<>();
        for (FollowersListResponse.FollowItem user : currentUsers) {
            if (query.isEmpty()
                    || (user.fullName != null && user.fullName.toLowerCase(Locale.ROOT).contains(query))
                    || (user.username != null && user.username.toLowerCase(Locale.ROOT).contains(query))) {
                filtered.add(user);
            }
        }

        if (layoutUsersList == null) return;
        layoutUsersList.removeAllViews();

        if (filtered.isEmpty()) {
            if (layoutEmptyState != null) {
                layoutEmptyState.setVisibility(View.VISIBLE);
                if (!query.isEmpty()) {
                    if (ivEmptyIcon != null) ivEmptyIcon.setImageResource(R.drawable.ic_search);
                    if (tvEmptyTitle != null) tvEmptyTitle.setText("Không tìm thấy kết quả");
                    if (tvEmptySubtitle != null) tvEmptySubtitle.setText("Thử tìm kiếm với từ khóa khác");
                } else if (isTabFollowers) {
                    if (ivEmptyIcon != null) ivEmptyIcon.setImageResource(R.drawable.ic_person_outline);
                    if (tvEmptyTitle != null) tvEmptyTitle.setText("Chưa có người theo dõi nào");
                    if (tvEmptySubtitle != null) tvEmptySubtitle.setText("Danh sách người theo dõi hiện đang trống.");
                } else {
                    if (ivEmptyIcon != null) ivEmptyIcon.setImageResource(R.drawable.ic_group_outline);
                    if (tvEmptyTitle != null) tvEmptyTitle.setText("Chưa theo dõi ai");
                    if (tvEmptySubtitle != null) tvEmptySubtitle.setText("Khám phá các reviewer và quán ngon để theo dõi!");
                }
            }
            return;
        }

        if (layoutEmptyState != null) layoutEmptyState.setVisibility(View.GONE);

        int selfId = AuthHelper.getCurrentUserId(this);
        LayoutInflater inflater = LayoutInflater.from(this);
        for (FollowersListResponse.FollowItem user : filtered) {
            View itemView = inflater.inflate(R.layout.item_follower_user, layoutUsersList, false);

            TextView tvAvatar = itemView.findViewById(R.id.tvFollowerAvatar);
            TextView tvName = itemView.findViewById(R.id.tvFollowerName);
            TextView tvHandle = itemView.findViewById(R.id.tvFollowerHandle);
            TextView btnAction = itemView.findViewById(R.id.btnFollowAction);

            String displayName = (user.fullName != null && !user.fullName.trim().isEmpty()) ? user.fullName : user.username;
            if (tvName != null) tvName.setText(displayName != null ? displayName : "Thành viên");
            if (tvHandle != null) tvHandle.setText("@" + (user.username != null ? user.username.replace("@", "") : ""));

            if (tvAvatar != null) {
                tvAvatar.setText(getInitials(displayName));
            }

            boolean isSelf = selfId > 0 && user.userId == selfId;
            if (btnAction != null) {
                if (isSelf) {
                    btnAction.setVisibility(View.GONE);
                } else {
                    btnAction.setVisibility(View.VISIBLE);
                    updateButtonUi(btnAction, user.isFollowing);
                    btnAction.setOnClickListener(v -> toggleFollow(user, btnAction));
                }
            }

            itemView.setOnClickListener(v -> {
                if (user.userId <= 0) return;
                if (selfId > 0 && user.userId == selfId) {
                    startActivity(new Intent(FollowersActivity.this, ProfileActivity.class));
                } else {
                    Intent intent = new Intent(FollowersActivity.this, UserProfileActivity.class);
                    intent.putExtra("USER_ID", user.userId);
                    startActivity(intent);
                }
            });

            layoutUsersList.addView(itemView);
        }
    }

    private void updateButtonUi(TextView btnAction, boolean isFollowing) {
        if (btnAction == null) return;
        if (isFollowing) {
            btnAction.setText("Đang theo dõi");
            btnAction.setTextColor(0xFF141B2B);
            btnAction.setBackgroundResource(R.drawable.bg_button_light_small);
        } else {
            boolean viewingOwnFollowers = "me".equals(targetUserId) && isTabFollowers;
            btnAction.setText(viewingOwnFollowers ? "Theo dõi lại" : "Theo dõi");
            btnAction.setTextColor(0xFFFF5A36);
            btnAction.setBackgroundResource(R.drawable.bg_button_light_small);
        }
    }

    private void toggleFollow(FollowersListResponse.FollowItem user, TextView btnAction) {
        TokenManager tm = new TokenManager(this);
        if (!tm.isLoggedIn()) return;
        String auth = ApiClient.getAuthHeader(this);
        if (auth.isEmpty()) return;

        btnAction.setEnabled(false);
        boolean targetFollowing = !user.isFollowing;
        Call<AuthResponse> call = targetFollowing
                ? ApiClient.getApiService().followUser(auth, String.valueOf(user.userId))
                : ApiClient.getApiService().unfollowUser(auth, String.valueOf(user.userId));

        call.enqueue(new Callback<AuthResponse>() {
            @Override
            public void onResponse(@NonNull Call<AuthResponse> call, @NonNull Response<AuthResponse> response) {
                btnAction.setEnabled(true);
                if (isFinishing() || isDestroyed()) return;
                if (response.isSuccessful() || (targetFollowing && response.code() == 409)) {
                    boolean changed = user.isFollowing != targetFollowing;
                    user.isFollowing = targetFollowing;
                    updateButtonUi(btnAction, user.isFollowing);
                    if ("me".equals(targetUserId) && changed) {
                        followingCount = Math.max(0, followingCount + (targetFollowing ? 1 : -1));
                        if (tvFollowingCountVal != null) {
                            tvFollowingCountVal.setText(PostFeedHelper.formatCount(followingCount));
                        }
                    }
                    Toast.makeText(
                            FollowersActivity.this,
                            targetFollowing ? "Đã theo dõi " + user.username : "Đã hủy theo dõi " + user.username,
                            Toast.LENGTH_SHORT
                    ).show();
                } else {
                    Toast.makeText(FollowersActivity.this, "Không thể cập nhật theo dõi", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<AuthResponse> call, @NonNull Throwable t) {
                btnAction.setEnabled(true);
                if (!isFinishing() && !isDestroyed()) {
                    Toast.makeText(FollowersActivity.this, "Lỗi kết nối", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private String getInitials(String name) {
        if (name == null || name.trim().isEmpty()) return "U";
        String[] parts = name.trim().split("\\s+");
        if (parts.length >= 2) {
            return ("" + parts[0].charAt(0) + parts[parts.length - 1].charAt(0)).toUpperCase(Locale.ROOT);
        }
        return name.substring(0, Math.min(2, name.length())).toUpperCase(Locale.ROOT);
    }
}
