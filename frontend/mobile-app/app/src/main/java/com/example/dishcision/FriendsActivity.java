package com.example.dishcision;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
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
import com.example.dishcision.network.FeedPostsResponse;
import com.example.dishcision.network.FollowersListResponse;
import com.example.dishcision.network.SearchAccountsResponse;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class FriendsActivity extends AppCompatActivity {

    private static final int TAB_FOLLOWERS = 0;
    private static final int TAB_SUGGESTIONS = 1;
    private static final int TAB_FOLLOWING = 2;

    private ImageView btnBack;
    private EditText edtSearchFriends;
    private LinearLayout tabInvitations;
    private LinearLayout tabSuggestions;
    private LinearLayout tabFriends;
    private TextView tvTabInvitations;
    private TextView tvTabSuggestions;
    private TextView tvTabFriends;
    private TextView tvFriendsSectionTitle;
    private ProgressBar progressBarFriends;
    private LinearLayout containerFriendsList;
    private TextView tvFriendsEmpty;

    private int activeTab = TAB_FOLLOWERS;
    private int currentUserId = 0;
    private final List<FollowersListResponse.FollowItem> currentUsers = new ArrayList<>();
    private final Set<Integer> followingUserIds = new HashSet<>();
    private final Handler searchHandler = new Handler(Looper.getMainLooper());
    private Runnable searchRunnable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_friends);
        ApiClient.init(this);
        currentUserId = AuthHelper.getCurrentUserId(this);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(android.R.id.content), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        initViews();
        setupListeners();
        selectTab(TAB_FOLLOWERS);
    }

    private void initViews() {
        btnBack = findViewById(R.id.btnBack);
        edtSearchFriends = findViewById(R.id.edtSearchFriends);
        tabInvitations = findViewById(R.id.tabInvitations);
        tabSuggestions = findViewById(R.id.tabSuggestions);
        tabFriends = findViewById(R.id.tabFriends);
        tvTabInvitations = findViewById(R.id.tvTabInvitations);
        tvTabSuggestions = findViewById(R.id.tvTabSuggestions);
        tvTabFriends = findViewById(R.id.tvTabFriends);
        tvFriendsSectionTitle = findViewById(R.id.tvFriendsSectionTitle);
        progressBarFriends = findViewById(R.id.progressBarFriends);
        containerFriendsList = findViewById(R.id.containerFriendsList);
        tvFriendsEmpty = findViewById(R.id.tvFriendsEmpty);
    }

    private void setupListeners() {
        if (btnBack != null) {
            btnBack.setOnClickListener(v -> finish());
        }

        if (tabInvitations != null) {
            tabInvitations.setOnClickListener(v -> selectTab(TAB_FOLLOWERS));
        }
        if (tabSuggestions != null) {
            tabSuggestions.setOnClickListener(v -> selectTab(TAB_SUGGESTIONS));
        }
        if (tabFriends != null) {
            tabFriends.setOnClickListener(v -> selectTab(TAB_FOLLOWING));
        }

        if (edtSearchFriends != null) {
            edtSearchFriends.addTextChangedListener(new TextWatcher() {
                @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    if (activeTab == TAB_SUGGESTIONS) {
                        if (searchRunnable != null) searchHandler.removeCallbacks(searchRunnable);
                        searchRunnable = () -> loadSuggestions();
                        searchHandler.postDelayed(searchRunnable, 300);
                    } else {
                        renderFilteredList();
                    }
                }

                @Override public void afterTextChanged(Editable s) {}
            });
        }
    }

    private void selectTab(int tab) {
        activeTab = tab;
        styleTab(tabInvitations, tvTabInvitations, tab == TAB_FOLLOWERS);
        styleTab(tabSuggestions, tvTabSuggestions, tab == TAB_SUGGESTIONS);
        styleTab(tabFriends, tvTabFriends, tab == TAB_FOLLOWING);

        if (tvFriendsSectionTitle != null) {
            if (tab == TAB_FOLLOWERS) tvFriendsSectionTitle.setText("Người theo dõi bạn");
            else if (tab == TAB_SUGGESTIONS) tvFriendsSectionTitle.setText("Gợi ý tài khoản");
            else tvFriendsSectionTitle.setText("Bạn đang theo dõi");
        }

        loadActiveTabData();
    }

    private void styleTab(LinearLayout container, TextView label, boolean active) {
        if (container != null) {
            container.setBackgroundResource(active ? R.drawable.bg_tab_active_white : 0);
            container.setElevation(active ? getResources().getDisplayMetrics().density : 0f);
        }
        if (label != null) {
            label.setTextColor(Color.parseColor(active ? "#FF5A36" : "#505568"));
            label.setTypeface(null, active ? Typeface.BOLD : Typeface.NORMAL);
        }
    }

    private void loadActiveTabData() {
        String auth = ApiClient.getAuthHeader(this);
        if (auth.isEmpty()) {
            currentUsers.clear();
            renderFilteredList();
            return;
        }

        showLoading(true);
        if (activeTab == TAB_SUGGESTIONS) {
            ApiClient.getApiService().getFollowing(auth, "me").enqueue(new Callback<FollowersListResponse>() {
                @Override
                public void onResponse(@NonNull Call<FollowersListResponse> call, @NonNull Response<FollowersListResponse> response) {
                    followingUserIds.clear();
                    if (response.isSuccessful() && response.body() != null && response.body().data != null) {
                        for (FollowersListResponse.FollowItem f : response.body().data) {
                            followingUserIds.add(f.userId);
                        }
                    }
                    loadSuggestions();
                }

                @Override
                public void onFailure(@NonNull Call<FollowersListResponse> call, @NonNull Throwable t) {
                    loadSuggestions();
                }
            });
            return;
        }

        Call<FollowersListResponse> call = activeTab == TAB_FOLLOWERS
                ? ApiClient.getApiService().getFollowers(auth, "me")
                : ApiClient.getApiService().getFollowing(auth, "me");

        call.enqueue(new Callback<FollowersListResponse>() {
            @Override
            public void onResponse(@NonNull Call<FollowersListResponse> call, @NonNull Response<FollowersListResponse> response) {
                if (isFinishing() || isDestroyed()) return;
                showLoading(false);
                currentUsers.clear();
                if (response.isSuccessful() && response.body() != null && response.body().data != null) {
                    currentUsers.addAll(response.body().data);
                    if (activeTab == TAB_FOLLOWING) {
                        followingUserIds.clear();
                        for (FollowersListResponse.FollowItem f : currentUsers) {
                            followingUserIds.add(f.userId);
                        }
                    }
                }
                renderFilteredList();
            }

            @Override
            public void onFailure(@NonNull Call<FollowersListResponse> call, @NonNull Throwable t) {
                if (isFinishing() || isDestroyed()) return;
                showLoading(false);
                currentUsers.clear();
                renderFilteredList();
                Toast.makeText(FriendsActivity.this, "Không thể tải danh sách bạn bè", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadSuggestions() {
        String q = edtSearchFriends != null && edtSearchFriends.getText() != null
                ? edtSearchFriends.getText().toString().trim()
                : "";
        ApiClient.getApiService().searchAccounts(q.isEmpty() ? null : q, 30).enqueue(new Callback<SearchAccountsResponse>() {
            @Override
            public void onResponse(@NonNull Call<SearchAccountsResponse> call, @NonNull Response<SearchAccountsResponse> response) {
                if (isFinishing() || isDestroyed() || activeTab != TAB_SUGGESTIONS) return;
                showLoading(false);
                currentUsers.clear();
                if (response.isSuccessful() && response.body() != null && response.body().data != null) {
                    for (FeedPostsResponse.Author acc : response.body().data) {
                        if (acc == null || acc.userId <= 0 || acc.userId == currentUserId) continue;
                        FollowersListResponse.FollowItem item = new FollowersListResponse.FollowItem();
                        item.userId = acc.userId;
                        item.username = acc.username;
                        item.fullName = acc.fullName;
                        item.avatarUrl = acc.avatarUrl;
                        item.bio = acc.bio;
                        item.isFollowing = followingUserIds.contains(acc.userId);
                        currentUsers.add(item);
                    }
                }
                renderFilteredList();
            }

            @Override
            public void onFailure(@NonNull Call<SearchAccountsResponse> call, @NonNull Throwable t) {
                if (isFinishing() || isDestroyed() || activeTab != TAB_SUGGESTIONS) return;
                showLoading(false);
                currentUsers.clear();
                renderFilteredList();
            }
        });
    }

    private void showLoading(boolean loading) {
        if (progressBarFriends != null) progressBarFriends.setVisibility(loading ? View.VISIBLE : View.GONE);
        if (loading) {
            if (containerFriendsList != null) containerFriendsList.setVisibility(View.GONE);
            if (tvFriendsEmpty != null) tvFriendsEmpty.setVisibility(View.GONE);
        }
    }

    private void renderFilteredList() {
        if (containerFriendsList == null) return;
        String q = edtSearchFriends != null && edtSearchFriends.getText() != null
                ? edtSearchFriends.getText().toString().trim().toLowerCase(Locale.ROOT)
                : "";

        List<FollowersListResponse.FollowItem> filtered = new ArrayList<>();
        for (FollowersListResponse.FollowItem item : currentUsers) {
            if (q.isEmpty() || activeTab == TAB_SUGGESTIONS
                    || (item.fullName != null && item.fullName.toLowerCase(Locale.ROOT).contains(q))
                    || (item.username != null && item.username.toLowerCase(Locale.ROOT).contains(q))) {
                filtered.add(item);
            }
        }

        containerFriendsList.removeAllViews();
        if (filtered.isEmpty()) {
            containerFriendsList.setVisibility(View.GONE);
            if (tvFriendsEmpty != null) {
                if (!q.isEmpty()) {
                    tvFriendsEmpty.setText("Không tìm thấy tài khoản phù hợp với \"" + q + "\".");
                } else if (activeTab == TAB_FOLLOWERS) {
                    tvFriendsEmpty.setText("Chưa có ai theo dõi bạn.");
                } else if (activeTab == TAB_SUGGESTIONS) {
                    tvFriendsEmpty.setText("Chưa có gợi ý tài khoản mới.");
                } else {
                    tvFriendsEmpty.setText("Bạn chưa theo dõi tài khoản nào.");
                }
                tvFriendsEmpty.setVisibility(View.VISIBLE);
            }
            return;
        }

        if (tvFriendsEmpty != null) tvFriendsEmpty.setVisibility(View.GONE);
        containerFriendsList.setVisibility(View.VISIBLE);

        LayoutInflater inflater = LayoutInflater.from(this);
        for (int i = 0; i < filtered.size(); i++) {
            FollowersListResponse.FollowItem user = filtered.get(i);
            View row = inflater.inflate(R.layout.item_follower_user, containerFriendsList, false);

            TextView tvAvatar = row.findViewById(R.id.tvFollowerAvatar);
            TextView tvName = row.findViewById(R.id.tvFollowerName);
            TextView tvHandle = row.findViewById(R.id.tvFollowerHandle);
            TextView btnAction = row.findViewById(R.id.btnFollowAction);

            String displayName = (user.fullName != null && !user.fullName.trim().isEmpty()) ? user.fullName.trim() : user.username;
            if (tvName != null) tvName.setText(displayName != null ? displayName : "Thành viên");
            String handleText = user.username != null ? "@" + user.username.replace("@", "") : "";
            if (user.bio != null && !user.bio.trim().isEmpty()) {
                handleText = handleText + " · " + user.bio.trim();
            }
            if (tvHandle != null) tvHandle.setText(handleText);
            if (tvAvatar != null) tvAvatar.setText(HomeActivity.getInitials(displayName));

            if (btnAction != null) {
                if (user.userId <= 0 || user.userId == currentUserId) {
                    btnAction.setVisibility(View.GONE);
                } else {
                    btnAction.setVisibility(View.VISIBLE);
                    updateFollowButtonUi(btnAction, user.isFollowing);
                    btnAction.setOnClickListener(v -> toggleFollow(user, btnAction));
                }
            }

            row.setOnClickListener(v -> {
                if (user.userId <= 0) return;
                Intent intent = new Intent(FriendsActivity.this, UserProfileActivity.class);
                intent.putExtra("USER_ID", user.userId);
                startActivity(intent);
            });

            containerFriendsList.addView(row);
        }
    }

    private void updateFollowButtonUi(TextView btn, boolean isFollowing) {
        if (btn == null) return;
        if (isFollowing) {
            btn.setText("Đang theo dõi");
            btn.setTextColor(Color.parseColor("#141B2B"));
            btn.setBackgroundResource(R.drawable.bg_button_light_small);
        } else {
            btn.setText(activeTab == TAB_FOLLOWERS ? "Theo dõi lại" : "Theo dõi");
            btn.setTextColor(Color.WHITE);
            btn.setBackgroundResource(R.drawable.bg_button_orange_small);
        }
    }

    private void toggleFollow(FollowersListResponse.FollowItem user, TextView btnAction) {
        String auth = ApiClient.getAuthHeader(this);
        if (auth.isEmpty()) {
            Toast.makeText(this, "Vui lòng đăng nhập lại", Toast.LENGTH_SHORT).show();
            return;
        }
        btnAction.setEnabled(false);
        boolean target = !user.isFollowing;
        Call<AuthResponse> call = target
                ? ApiClient.getApiService().followUser(auth, String.valueOf(user.userId))
                : ApiClient.getApiService().unfollowUser(auth, String.valueOf(user.userId));

        call.enqueue(new Callback<AuthResponse>() {
            @Override
            public void onResponse(@NonNull Call<AuthResponse> call, @NonNull Response<AuthResponse> response) {
                btnAction.setEnabled(true);
                if (isFinishing() || isDestroyed()) return;
                if (response.isSuccessful() || (target && response.code() == 409)) {
                    user.isFollowing = target;
                    if (target) followingUserIds.add(user.userId);
                    else followingUserIds.remove(user.userId);
                    updateFollowButtonUi(btnAction, target);
                    if (!target && activeTab == TAB_FOLLOWING) {
                        currentUsers.remove(user);
                        renderFilteredList();
                    }
                    Toast.makeText(
                            FriendsActivity.this,
                            target ? "Đã theo dõi " + (user.fullName != null ? user.fullName : user.username)
                                    : "Đã bỏ theo dõi",
                            Toast.LENGTH_SHORT
                    ).show();
                } else {
                    Toast.makeText(FriendsActivity.this, "Không thể cập nhật theo dõi", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<AuthResponse> call, @NonNull Throwable t) {
                btnAction.setEnabled(true);
                if (!isFinishing() && !isDestroyed()) {
                    Toast.makeText(FriendsActivity.this, "Lỗi kết nối máy chủ", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}
