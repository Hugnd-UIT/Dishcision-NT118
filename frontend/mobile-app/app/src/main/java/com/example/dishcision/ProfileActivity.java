package com.example.dishcision;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.dishcision.network.ApiClient;
import com.example.dishcision.network.UserProfileResponse;
import com.example.dishcision.network.TokenManager;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ProfileActivity extends AppCompatActivity {

    private ImageView btnBack;
    private ImageView btnShare;
    private ImageView btnSettings;
    private LinearLayout btnEditProfile;
    private LinearLayout btnQrCode;

    private TextView tvHandle;
    private TextView tvFullName;
    private TextView tvBio;
    private TextView tvPostsCount;
    private TextView tvFollowersCount;
    private TextView tvFollowingCount;

    private LinearLayout navHome;
    private LinearLayout navDecide;
    private LinearLayout navCreate;
    private LinearLayout navMessage;

    private TokenManager tokenManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_profile);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(android.R.id.content), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        tokenManager = new TokenManager(this);

        initViews();
        setupListeners();
        fetchProfileFromApi();
    }

    private void initViews() {
        btnBack = findViewById(R.id.btnBack);
        btnShare = findViewById(R.id.btnShare);
        btnSettings = findViewById(R.id.btnSettings);
        btnEditProfile = findViewById(R.id.btnEditProfile);
        btnQrCode = findViewById(R.id.btnQrCode);

        tvHandle = findViewById(R.id.tvHandle);
        tvFullName = findViewById(R.id.tvFullName);
        tvBio = findViewById(R.id.tvBio);
        tvPostsCount = findViewById(R.id.tvPostsCount);
        tvFollowersCount = findViewById(R.id.tvFollowersCount);
        tvFollowingCount = findViewById(R.id.tvFollowingCount);

        navHome = findViewById(R.id.navHome);
        navDecide = findViewById(R.id.navDecide);
        navCreate = findViewById(R.id.navCreate);
        navMessage = findViewById(R.id.navMessage);
    }

    private void setupListeners() {
        if (btnBack != null) {
            btnBack.setOnClickListener(v -> finish());
        }

        if (btnShare != null) {
            btnShare.setOnClickListener(v ->
                Toast.makeText(this, "Chia sẻ hồ sơ cá nhân", Toast.LENGTH_SHORT).show()
            );
        }

        if (btnSettings != null) {
            btnSettings.setOnClickListener(v -> {
                Intent intent = new Intent(ProfileActivity.this, SettingsActivity.class);
                startActivity(intent);
            });
        }

        if (btnEditProfile != null) {
            btnEditProfile.setOnClickListener(v -> {
                Intent intent = new Intent(ProfileActivity.this, EditProfileActivity.class);
                startActivity(intent);
            });
        }

        if (btnQrCode != null) {
            btnQrCode.setOnClickListener(v ->
                Toast.makeText(this, "Mã QR cá nhân", Toast.LENGTH_SHORT).show()
            );
        }

        if (navHome != null) {
            navHome.setOnClickListener(v -> {
                Intent intent = new Intent(ProfileActivity.this, HomeActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                startActivity(intent);
                finish();
            });
        }

        if (navDecide != null) {
            navDecide.setOnClickListener(v -> {
                Intent intent = new Intent(ProfileActivity.this, DecideActivity.class);
                startActivity(intent);
            });
        }

        if (navCreate != null) {
            navCreate.setOnClickListener(v -> {
                Intent intent = new Intent(ProfileActivity.this, CreatePostActivity.class);
                startActivity(intent);
            });
        }

        if (navMessage != null) {
            navMessage.setOnClickListener(v -> {
                Intent intent = new Intent(ProfileActivity.this, MessagesInboxActivity.class);
                startActivity(intent);
            });
        }
    }

    private void fetchProfileFromApi() {
        String authHeader = ApiClient.getAuthHeader(this);
        ApiClient.getApiService().getMyProfile(authHeader).enqueue(new Callback<UserProfileResponse>() {
            @Override
            public void onResponse(Call<UserProfileResponse> call, Response<UserProfileResponse> response) {
                if (response.isSuccessful() && response.body() != null && response.body().getData() != null) {
                    UserProfileResponse.UserData user = response.body().getData();
                    if (tvFullName != null && user.fullName != null) {
                        tvFullName.setText(user.fullName);
                    }
                    if (tvHandle != null && user.username != null) {
                        tvHandle.setText(user.username.startsWith("@") ? user.username : "@" + user.username);
                    }
                    if (tvBio != null && user.bio != null) {
                        tvBio.setText(user.bio);
                    }
                    if (tvPostsCount != null) {
                        tvPostsCount.setText(String.valueOf(user.postsCount));
                    }
                    if (tvFollowersCount != null) {
                        tvFollowersCount.setText(String.valueOf(user.followersCount));
                    }
                    if (tvFollowingCount != null) {
                        tvFollowingCount.setText(String.valueOf(user.followingCount));
                    }
                }
            }

            @Override
            public void onFailure(Call<UserProfileResponse> call, Throwable t) {}
        });
    }
}
