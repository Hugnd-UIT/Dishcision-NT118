package com.example.dishcision;

import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.dishcision.network.ApiClient;
import com.example.dishcision.network.FollowingRestaurantsResponse;
import com.google.android.material.bottomsheet.BottomSheetDialog;

import java.util.List;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class FavoriteRestaurantsActivity extends AppCompatActivity {

    private ImageView btnBack;
    private ImageView btnNotification;
    private EditText edtSearch;

    private LinearLayout navHome;
    private LinearLayout navDecide;
    private LinearLayout navCreate;
    private LinearLayout navMessages;
    private LinearLayout navProfile;

    private View cardStore1;
    private View cardStore2;
    private View cardStore3;

    private TextView tvStoreName1;
    private TextView tvStoreName2;
    private TextView tvStoreName3;

    private TextView btnFollowStore1;
    private TextView btnFollowStore2;
    private TextView btnFollowStore3;

    private View btnShareStore1;
    private View btnShareStore2;
    private View btnShareStore3;

    private LinearLayout btnViewMenuPhoGiaTruyen;
    private LinearLayout btnViewMenuComTam;
    private LinearLayout btnViewMenuTocoToco;

    private LinearLayout storyBepLucNam;
    private LinearLayout storyPhoThin;
    private LinearLayout storyTraMuaThu;
    private LinearLayout storyPizzaLoCui;

    private TextView chipAll;
    private TextView chipOpen;
    private TextView chipNearMe;
    private TextView currentActiveChip;

    private boolean isFollow1 = true;
    private boolean isFollow2 = true;
    private boolean isFollow3 = true;

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

        initViews();
        setupListeners();
        fetchFollowingRestaurantsFromBackend();
    }

    private void initViews() {
        btnBack = findViewById(R.id.btnBack);
        btnNotification = findViewById(R.id.btnNotification);
        edtSearch = findViewById(R.id.edtSearch);

        navHome = findViewById(R.id.navHome);
        navDecide = findViewById(R.id.navDecide);
        navCreate = findViewById(R.id.navCreate);
        navMessages = findViewById(R.id.navMessages);
        navProfile = findViewById(R.id.navProfile);

        cardStore1 = findViewById(R.id.cardStore1);
        cardStore2 = findViewById(R.id.cardStore2);
        cardStore3 = findViewById(R.id.cardStore3);

        tvStoreName1 = findViewById(R.id.tvStoreName1);
        tvStoreName2 = findViewById(R.id.tvStoreName2);
        tvStoreName3 = findViewById(R.id.tvStoreName3);

        btnFollowStore1 = findViewById(R.id.btnFollowStore1);
        btnFollowStore2 = findViewById(R.id.btnFollowStore2);
        btnFollowStore3 = findViewById(R.id.btnFollowStore3);

        btnShareStore1 = findViewById(R.id.btnShareStore1);
        btnShareStore2 = findViewById(R.id.btnShareStore2);
        btnShareStore3 = findViewById(R.id.btnShareStore3);

        btnViewMenuPhoGiaTruyen = findViewById(R.id.btnViewMenuPhoGiaTruyen);
        btnViewMenuComTam = findViewById(R.id.btnViewMenuComTam);
        btnViewMenuTocoToco = findViewById(R.id.btnViewMenuTocoToco);

        storyBepLucNam = findViewById(R.id.storyBepLucNam);
        storyPhoThin = findViewById(R.id.storyPhoThin);
        storyTraMuaThu = findViewById(R.id.storyTraMuaThu);
        storyPizzaLoCui = findViewById(R.id.storyPizzaLoCui);

        chipAll = findViewById(R.id.chipAll);
        chipOpen = findViewById(R.id.chipOpen);
        chipNearMe = findViewById(R.id.chipNearMe);
        currentActiveChip = chipAll;
    }

    private void setupListeners() {
        if (btnBack != null) {
            btnBack.setOnClickListener(v -> finish());
        }

        if (btnNotification != null) {
            btnNotification.setOnClickListener(v -> {
                Intent intent = new Intent(FavoriteRestaurantsActivity.this, NotificationsActivity.class);
                startActivity(intent);
            });
        }

        // Realtime search bar filter
        if (edtSearch != null) {
            edtSearch.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    filterStores(s.toString().toLowerCase().trim());
                }

                @Override
                public void afterTextChanged(Editable s) {}
            });
        }

        if (navHome != null) {
            navHome.setOnClickListener(v -> {
                Intent intent = new Intent(FavoriteRestaurantsActivity.this, HomeActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                startActivity(intent);
                finish();
            });
        }

        if (navDecide != null) {
            navDecide.setOnClickListener(v -> {
                Intent intent = new Intent(FavoriteRestaurantsActivity.this, DecideActivity.class);
                startActivity(intent);
            });
        }

        if (navCreate != null) {
            navCreate.setOnClickListener(v -> {
                Intent intent = new Intent(FavoriteRestaurantsActivity.this, CreatePostActivity.class);
                startActivity(intent);
            });
        }

        if (navMessages != null) {
            navMessages.setOnClickListener(v -> {
                Intent intent = new Intent(FavoriteRestaurantsActivity.this, MessagesInboxActivity.class);
                startActivity(intent);
            });
        }

        if (navProfile != null) {
            navProfile.setOnClickListener(v -> {
                Intent intent = new Intent(FavoriteRestaurantsActivity.this, ProfileActivity.class);
                startActivity(intent);
            });
        }

        // Follow / Unfollow buttons
        if (btnFollowStore1 != null) {
            btnFollowStore1.setOnClickListener(v -> {
                isFollow1 = !isFollow1;
                btnFollowStore1.setText(isFollow1 ? "Đang theo dõi" : "Theo dõi");
                Toast.makeText(this, isFollow1 ? "Đã theo dõi Phở Bò Gia Truyền" : "Đã bỏ theo dõi Phở Bò Gia Truyền", Toast.LENGTH_SHORT).show();
            });
        }

        if (btnFollowStore2 != null) {
            btnFollowStore2.setOnClickListener(v -> {
                isFollow2 = !isFollow2;
                btnFollowStore2.setText(isFollow2 ? "Đang theo dõi" : "Theo dõi");
                Toast.makeText(this, isFollow2 ? "Đã theo dõi Cơm Tấm Ba Nghiền" : "Đã bỏ theo dõi Cơm Tấm Ba Nghiền", Toast.LENGTH_SHORT).show();
            });
        }

        if (btnFollowStore3 != null) {
            btnFollowStore3.setOnClickListener(v -> {
                isFollow3 = !isFollow3;
                btnFollowStore3.setText(isFollow3 ? "Đang theo dõi" : "Theo dõi");
                Toast.makeText(this, isFollow3 ? "Đã theo dõi TocoToco" : "Đã bỏ theo dõi TocoToco", Toast.LENGTH_SHORT).show();
            });
        }

        // Share buttons
        View.OnClickListener shareListener = v -> openShareBottomSheet();
        if (btnShareStore1 != null) btnShareStore1.setOnClickListener(shareListener);
        if (btnShareStore2 != null) btnShareStore2.setOnClickListener(shareListener);
        if (btnShareStore3 != null) btnShareStore3.setOnClickListener(shareListener);

        if (btnViewMenuPhoGiaTruyen != null) {
            btnViewMenuPhoGiaTruyen.setOnClickListener(v -> openMenuBottomSheet("Menu Quán Phở Bò Gia Truyền 1986"));
        }
        if (btnViewMenuComTam != null) {
            btnViewMenuComTam.setOnClickListener(v -> openMenuBottomSheet("Menu Cơm Tấm Ba Nghiền"));
        }
        if (btnViewMenuTocoToco != null) {
            btnViewMenuTocoToco.setOnClickListener(v -> openMenuBottomSheet("Menu Trà Sữa TocoToco - Cầu Giấy"));
        }

        if (storyBepLucNam != null) storyBepLucNam.setOnClickListener(v -> openStoryViewer("Bếp Lục Nam"));
        if (storyPhoThin != null) storyPhoThin.setOnClickListener(v -> openStoryViewer("Phở Thìn"));
        if (storyTraMuaThu != null) storyTraMuaThu.setOnClickListener(v -> openStoryViewer("Trà Mùa Thu"));
        if (storyPizzaLoCui != null) storyPizzaLoCui.setOnClickListener(v -> openStoryViewer("Pizza Lò Củi"));

        if (chipAll != null) chipAll.setOnClickListener(v -> setActiveChip(chipAll));
        if (chipOpen != null) chipOpen.setOnClickListener(v -> setActiveChip(chipOpen));
        if (chipNearMe != null) chipNearMe.setOnClickListener(v -> setActiveChip(chipNearMe));
    }

    private void filterStores(String query) {
        if (query.isEmpty()) {
            if (cardStore1 != null) cardStore1.setVisibility(View.VISIBLE);
            if (cardStore2 != null) cardStore2.setVisibility(View.VISIBLE);
            if (cardStore3 != null) cardStore3.setVisibility(View.VISIBLE);
            return;
        }

        if (cardStore1 != null) {
            cardStore1.setVisibility("quán phở bò gia truyền 1986 phở bò hà nội".contains(query) ? View.VISIBLE : View.GONE);
        }
        if (cardStore2 != null) {
            cardStore2.setVisibility("cơm tấm ba nghiền hcm tphcm".contains(query) ? View.VISIBLE : View.GONE);
        }
        if (cardStore3 != null) {
            cardStore3.setVisibility("trà sữa tocotoco cầu giấy hà nội".contains(query) ? View.VISIBLE : View.GONE);
        }
    }

    private void openMenuBottomSheet(String storeName) {
        BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(FavoriteRestaurantsActivity.this);
        View dialogView = getLayoutInflater().inflate(R.layout.layout_dialog_menu_bottomsheet, null);
        bottomSheetDialog.setContentView(dialogView);

        TextView tvMenuStoreName = dialogView.findViewById(R.id.tvMenuStoreName);
        RelativeLayout btnCloseMenu = dialogView.findViewById(R.id.btnCloseMenu);

        if (tvMenuStoreName != null && storeName != null) {
            tvMenuStoreName.setText(storeName);
        }

        if (btnCloseMenu != null) {
            btnCloseMenu.setOnClickListener(v -> bottomSheetDialog.dismiss());
        }

        bottomSheetDialog.show();
    }

    private void openShareBottomSheet() {
        BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(FavoriteRestaurantsActivity.this);
        View dialogView = getLayoutInflater().inflate(R.layout.layout_dialog_share_post, null);
        bottomSheetDialog.setContentView(dialogView);

        View btnCloseShare = dialogView.findViewById(R.id.btnCloseShare);
        View btnSendDishcision = dialogView.findViewById(R.id.btnSendDishcision);
        View btnCopyLink = dialogView.findViewById(R.id.btnCopyLink);

        if (btnCloseShare != null) {
            btnCloseShare.setOnClickListener(v -> bottomSheetDialog.dismiss());
        }

        if (btnSendDishcision != null) {
            btnSendDishcision.setOnClickListener(v -> {
                Toast.makeText(FavoriteRestaurantsActivity.this, "Đã chia sẻ quán ăn!", Toast.LENGTH_SHORT).show();
                bottomSheetDialog.dismiss();
            });
        }

        if (btnCopyLink != null) {
            btnCopyLink.setOnClickListener(v -> {
                Toast.makeText(FavoriteRestaurantsActivity.this, "Đã sao chép liên kết quán ăn!", Toast.LENGTH_SHORT).show();
                bottomSheetDialog.dismiss();
            });
        }

        bottomSheetDialog.show();
    }

    private void openStoryViewer(String storeName) {
        Intent intent = new Intent(FavoriteRestaurantsActivity.this, StoryActivity.class);
        intent.putExtra(StoryActivity.EXTRA_STORE_NAME, storeName);
        startActivity(intent);
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

    private void fetchFollowingRestaurantsFromBackend() {
        String authHeader = ApiClient.getAuthHeader(this);
        ApiClient.getApiService().getFollowingRestaurants(authHeader).enqueue(new Callback<FollowingRestaurantsResponse>() {
            @Override
            public void onResponse(Call<FollowingRestaurantsResponse> call, Response<FollowingRestaurantsResponse> response) {
                if (response.isSuccessful() && response.body() != null && response.body().getData() != null) {
                    FollowingRestaurantsResponse.RestaurantData data = response.body().getData();
                    bindRestaurantsData(data);
                }
            }

            @Override
            public void onFailure(Call<FollowingRestaurantsResponse> call, Throwable t) {}
        });
    }

    private void bindRestaurantsData(FollowingRestaurantsResponse.RestaurantData data) {
        if (data == null || data.restaurants == null) return;
        List<FollowingRestaurantsResponse.StoreItem> stores = data.restaurants;
        if (stores.size() > 0) {
            FollowingRestaurantsResponse.StoreItem s1 = stores.get(0);
            if (tvStoreName1 != null && s1.name != null) tvStoreName1.setText(s1.name);
            if (btnFollowStore1 != null) {
                isFollow1 = s1.isFollowing;
                btnFollowStore1.setText(isFollow1 ? "Đang theo dõi" : "Theo dõi");
            }
        }
        if (stores.size() > 1) {
            FollowingRestaurantsResponse.StoreItem s2 = stores.get(1);
            if (tvStoreName2 != null && s2.name != null) tvStoreName2.setText(s2.name);
            if (btnFollowStore2 != null) {
                isFollow2 = s2.isFollowing;
                btnFollowStore2.setText(isFollow2 ? "Đang theo dõi" : "Theo dõi");
            }
        }
        if (stores.size() > 2) {
            FollowingRestaurantsResponse.StoreItem s3 = stores.get(2);
            if (tvStoreName3 != null && s3.name != null) tvStoreName3.setText(s3.name);
            if (btnFollowStore3 != null) {
                isFollow3 = s3.isFollowing;
                btnFollowStore3.setText(isFollow3 ? "Đang theo dõi" : "Theo dõi");
            }
        }
    }
}
