package com.example.dishcision;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
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
import com.example.dishcision.network.FeedPostsResponse;
import com.example.dishcision.network.FollowersListResponse;
import com.example.dishcision.network.ImageLoader;
import com.example.dishcision.network.SearchAccountsResponse;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class NewMessageActivity extends AppCompatActivity {

    private static final int FILTER_ALL = 0;
    private static final int FILTER_FRIENDS = 1;
    private static final int FILTER_RESTAURANTS = 2;

    private TextView btnCancel;
    private LinearLayout layoutSelectedRecipientRow;
    private LinearLayout chipSelectedRecipient;
    private TextView tvSelectedRecipientName;
    private EditText edtSearchContact;
    private TextView tvSelectedCount;
    private TextView chipAll;
    private TextView chipFriends;
    private TextView chipRestaurants;
    private ProgressBar progressContacts;
    private TextView tvEmptyContacts;
    private LinearLayout layoutContactsContainer;
    private LinearLayout btnStartChat;

    private final Map<Integer, FeedPostsResponse.Author> contactsById = new LinkedHashMap<>();
    private final List<FeedPostsResponse.Author> remoteResults = new ArrayList<>();
    private FeedPostsResponse.Author selectedContact = null;
    private int activeFilter = FILTER_ALL;
    private int currentUserId = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_new_message);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(android.R.id.content), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        currentUserId = AuthHelper.getCurrentUserId(this);
        initViews();
        setupListeners();
        loadInitialContacts();
    }

    private void initViews() {
        btnCancel = findViewById(R.id.btnCancel);
        layoutSelectedRecipientRow = findViewById(R.id.layoutSelectedRecipientRow);
        chipSelectedRecipient = findViewById(R.id.chipSelectedRecipient);
        tvSelectedRecipientName = findViewById(R.id.tvSelectedRecipientName);
        edtSearchContact = findViewById(R.id.edtSearchContact);
        tvSelectedCount = findViewById(R.id.tvSelectedCount);
        chipAll = findViewById(R.id.chipAll);
        chipFriends = findViewById(R.id.chipFriends);
        chipRestaurants = findViewById(R.id.chipRestaurants);
        progressContacts = findViewById(R.id.progressContacts);
        tvEmptyContacts = findViewById(R.id.tvEmptyContacts);
        layoutContactsContainer = findViewById(R.id.layoutContactsContainer);
        btnStartChat = findViewById(R.id.btnStartChat);
    }

    private void setupListeners() {
        if (btnCancel != null) {
            btnCancel.setOnClickListener(v -> finish());
        }

        if (chipSelectedRecipient != null) {
            chipSelectedRecipient.setOnClickListener(v -> {
                selectedContact = null;
                updateSelectionUi();
                renderContacts();
            });
        }

        if (chipAll != null) chipAll.setOnClickListener(v -> selectFilter(FILTER_ALL));
        if (chipFriends != null) chipFriends.setOnClickListener(v -> selectFilter(FILTER_FRIENDS));
        if (chipRestaurants != null) chipRestaurants.setOnClickListener(v -> selectFilter(FILTER_RESTAURANTS));

        if (edtSearchContact != null) {
            edtSearchContact.addTextChangedListener(new TextWatcher() {
                @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    String q = s.toString().trim();
                    if (q.isEmpty()) {
                        remoteResults.clear();
                        renderContacts();
                    } else {
                        searchRemoteAccounts(q);
                    }
                }

                @Override public void afterTextChanged(Editable s) {}
            });
        }

        if (btnStartChat != null) {
            btnStartChat.setOnClickListener(v -> {
                if (selectedContact == null || selectedContact.userId <= 0) {
                    Toast.makeText(this, "Vui lòng chọn một người hoặc quán ăn để bắt đầu trò chuyện", Toast.LENGTH_SHORT).show();
                    return;
                }
                openChatWithContact(selectedContact);
            });
        }
    }

    private void selectFilter(int filter) {
        activeFilter = filter;
        styleChip(chipAll, filter == FILTER_ALL);
        styleChip(chipFriends, filter == FILTER_FRIENDS);
        styleChip(chipRestaurants, filter == FILTER_RESTAURANTS);
        renderContacts();
    }

    private void styleChip(TextView chip, boolean active) {
        if (chip == null) return;
        chip.setBackgroundResource(active ? R.drawable.bg_chip_dark_active : R.drawable.bg_chip_preference);
        chip.setTextColor(active ? Color.WHITE : 0xFF141B2B);
        chip.setTypeface(null, active ? Typeface.BOLD : Typeface.NORMAL);
    }

    private void loadInitialContacts() {
        if (progressContacts != null) progressContacts.setVisibility(View.VISIBLE);
        String auth = ApiClient.getAuthHeader(this);
        if (!auth.isEmpty()) {
            ApiClient.getApiService().getFollowing(auth, "me").enqueue(new Callback<FollowersListResponse>() {
                @Override
                public void onResponse(@NonNull Call<FollowersListResponse> call, @NonNull Response<FollowersListResponse> response) {
                    if (isFinishing() || isDestroyed()) return;
                    if (response.isSuccessful() && response.body() != null && response.body().data != null) {
                        for (FollowersListResponse.FollowItem item : response.body().data) {
                            if (item != null && item.userId > 0 && item.userId != currentUserId && !contactsById.containsKey(item.userId)) {
                                FeedPostsResponse.Author a = new FeedPostsResponse.Author();
                                a.userId = item.userId;
                                a.username = item.username;
                                a.fullName = item.fullName;
                                a.avatarUrl = item.avatarUrl;
                                a.bio = item.bio;
                                contactsById.put(a.userId, a);
                            }
                        }
                        renderContacts();
                    }
                }

                @Override
                public void onFailure(@NonNull Call<FollowersListResponse> call, @NonNull Throwable t) {}
            });
        }

        ApiClient.getApiService().searchAccounts(null, 30).enqueue(new Callback<SearchAccountsResponse>() {
            @Override
            public void onResponse(@NonNull Call<SearchAccountsResponse> call, @NonNull Response<SearchAccountsResponse> response) {
                if (isFinishing() || isDestroyed()) return;
                if (progressContacts != null) progressContacts.setVisibility(View.GONE);
                if (response.isSuccessful() && response.body() != null && response.body().data != null) {
                    for (FeedPostsResponse.Author a : response.body().data) {
                        if (a != null && a.userId > 0 && a.userId != currentUserId && !"Admin".equals(a.role)) {
                            contactsById.put(a.userId, a);
                        }
                    }
                }
                renderContacts();
            }

            @Override
            public void onFailure(@NonNull Call<SearchAccountsResponse> call, @NonNull Throwable t) {
                if (isFinishing() || isDestroyed()) return;
                if (progressContacts != null) progressContacts.setVisibility(View.GONE);
                renderContacts();
            }
        });
    }

    private void searchRemoteAccounts(String query) {
        ApiClient.getApiService().searchAccounts(query, 25).enqueue(new Callback<SearchAccountsResponse>() {
            @Override
            public void onResponse(@NonNull Call<SearchAccountsResponse> call, @NonNull Response<SearchAccountsResponse> response) {
                if (isFinishing() || isDestroyed()) return;
                String currentQuery = edtSearchContact != null && edtSearchContact.getText() != null
                        ? edtSearchContact.getText().toString().trim()
                        : "";
                if (!currentQuery.equals(query)) return;

                remoteResults.clear();
                if (response.isSuccessful() && response.body() != null && response.body().data != null) {
                    for (FeedPostsResponse.Author a : response.body().data) {
                        if (a != null && a.userId > 0 && a.userId != currentUserId && !"Admin".equals(a.role)) {
                            remoteResults.add(a);
                            contactsById.put(a.userId, a);
                        }
                    }
                }
                renderContacts();
            }

            @Override
            public void onFailure(@NonNull Call<SearchAccountsResponse> call, @NonNull Throwable t) {
                renderContacts();
            }
        });
    }

    private void updateSelectionUi() {
        if (selectedContact != null) {
            String displayName = getDisplayName(selectedContact);
            if (layoutSelectedRecipientRow != null) layoutSelectedRecipientRow.setVisibility(View.VISIBLE);
            if (tvSelectedRecipientName != null) tvSelectedRecipientName.setText(displayName);
            if (tvSelectedCount != null) tvSelectedCount.setText("Đã chọn 1");
        } else {
            if (layoutSelectedRecipientRow != null) layoutSelectedRecipientRow.setVisibility(View.GONE);
            if (tvSelectedCount != null) tvSelectedCount.setText("Chưa chọn");
        }
    }

    private void renderContacts() {
        if (layoutContactsContainer == null) return;
        layoutContactsContainer.removeAllViews();

        String query = edtSearchContact != null && edtSearchContact.getText() != null
                ? edtSearchContact.getText().toString().toLowerCase(Locale.ROOT).trim()
                : "";

        List<FeedPostsResponse.Author> sourceList = query.isEmpty()
                ? new ArrayList<>(contactsById.values())
                : new ArrayList<>(remoteResults);

        if (!query.isEmpty() && sourceList.isEmpty()) {
            for (FeedPostsResponse.Author a : contactsById.values()) {
                String n = a.fullName != null ? a.fullName.toLowerCase(Locale.ROOT) : "";
                String u = a.username != null ? a.username.toLowerCase(Locale.ROOT) : "";
                if (n.contains(query) || u.contains(query)) {
                    sourceList.add(a);
                }
            }
        }

        List<FeedPostsResponse.Author> filtered = new ArrayList<>();
        for (FeedPostsResponse.Author a : sourceList) {
            if (a == null) continue;
            boolean isRestaurant = "RestaurantAccount".equals(a.role);
            if (activeFilter == FILTER_FRIENDS && isRestaurant) continue;
            if (activeFilter == FILTER_RESTAURANTS && !isRestaurant) continue;
            filtered.add(a);
        }

        if (filtered.isEmpty()) {
            if (tvEmptyContacts != null) tvEmptyContacts.setVisibility(View.VISIBLE);
            return;
        }
        if (tvEmptyContacts != null) tvEmptyContacts.setVisibility(View.GONE);

        for (FeedPostsResponse.Author contact : filtered) {
            layoutContactsContainer.addView(buildContactRow(contact));
        }
    }

    private View buildContactRow(FeedPostsResponse.Author contact) {
        boolean isSelected = selectedContact != null && selectedContact.userId == contact.userId;

        LinearLayout row = new LinearLayout(this);
        LinearLayout.LayoutParams rowLp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        rowLp.bottomMargin = dpToPx(6);
        row.setLayoutParams(rowLp);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(Gravity.CENTER_VERTICAL);
        row.setPadding(dpToPx(12), dpToPx(12), dpToPx(12), dpToPx(12));
        row.setClickable(true);
        row.setFocusable(true);
        if (isSelected) {
            row.setBackgroundResource(R.drawable.bg_card_selected_peach);
        } else {
            TypedValue outVal = new TypedValue();
            getTheme().resolveAttribute(android.R.attr.selectableItemBackground, outVal, true);
            row.setBackgroundResource(outVal.resourceId);
        }

        ImageView avatar = new ImageView(this);
        avatar.setLayoutParams(new LinearLayout.LayoutParams(dpToPx(48), dpToPx(48)));
        avatar.setScaleType(ImageView.ScaleType.CENTER_CROP);
        avatar.setImageResource(R.drawable.bg_avatar_circle);
        if (contact.avatarUrl != null && !contact.avatarUrl.trim().isEmpty()) {
            ImageLoader.loadAvatar(avatar, contact.avatarUrl);
        }
        row.addView(avatar);

        LinearLayout col = new LinearLayout(this);
        LinearLayout.LayoutParams colLp = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f);
        colLp.setMarginStart(dpToPx(12));
        col.setLayoutParams(colLp);
        col.setOrientation(LinearLayout.VERTICAL);

        TextView tvName = new TextView(this);
        tvName.setText(getDisplayName(contact));
        tvName.setTextColor(0xFF141B2B);
        tvName.setTextSize(TypedValue.COMPLEX_UNIT_SP, 15);
        tvName.setTypeface(null, Typeface.BOLD);
        col.addView(tvName);

        TextView tvSub = new TextView(this);
        boolean isRestaurant = "RestaurantAccount".equals(contact.role);
        String handle = contact.username != null && !contact.username.isEmpty() ? "@" + contact.username.replace("@", "") : "";
        String roleLabel = isRestaurant ? "Quán ăn" : "Thành viên";
        tvSub.setText(handle.isEmpty() ? roleLabel : roleLabel + " · " + handle);
        tvSub.setTextColor(0xFF6B7280);
        tvSub.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12);
        LinearLayout.LayoutParams subLp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        subLp.topMargin = dpToPx(2);
        tvSub.setLayoutParams(subLp);
        col.addView(tvSub);

        row.addView(col);

        ImageView ivCheck = new ImageView(this);
        ivCheck.setLayoutParams(new LinearLayout.LayoutParams(dpToPx(24), dpToPx(24)));
        ivCheck.setImageResource(isSelected ? R.drawable.ic_check_circle_filled : R.drawable.ic_check_circle_outline);
        row.addView(ivCheck);

        row.setOnClickListener(v -> {
            if (isSelected) {
                openChatWithContact(contact);
            } else {
                selectedContact = contact;
                updateSelectionUi();
                renderContacts();
            }
        });

        return row;
    }

    private String getDisplayName(FeedPostsResponse.Author contact) {
        if (contact.fullName != null && !contact.fullName.trim().isEmpty()) {
            return contact.fullName.trim();
        }
        return contact.username != null ? contact.username : "Thành viên";
    }

    private void openChatWithContact(FeedPostsResponse.Author contact) {
        Intent intent = new Intent(NewMessageActivity.this, ChatActivity.class);
        intent.putExtra("USER_ID", contact.userId);
        intent.putExtra("CHAT_NAME", getDisplayName(contact));
        if (contact.avatarUrl != null) intent.putExtra("AVATAR_URL", contact.avatarUrl);
        if (contact.role != null) intent.putExtra("USER_ROLE", contact.role);
        startActivity(intent);
        finish();
    }

    private int dpToPx(int dp) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, getResources().getDisplayMetrics());
    }
}
