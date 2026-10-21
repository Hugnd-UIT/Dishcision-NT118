package com.example.dishcision;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.animation.DecelerateInterpolator;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class DecideActivity extends AppCompatActivity {

    private RelativeLayout btnSpinCenter;
    private View containerWheel;
    private ImageView btnNotification;

    private LinearLayout navHome;
    private LinearLayout navCreate;
    private LinearLayout navMessages;
    private LinearLayout navProfile;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_decide);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main_decide), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        initViews();
        setupListeners();
    }

    private void initViews() {
        btnSpinCenter = findViewById(R.id.btnSpinCenter);
        containerWheel = findViewById(R.id.containerWheel);
        btnNotification = findViewById(R.id.btnNotification);

        navHome = findViewById(R.id.navHome);
        navCreate = findViewById(R.id.navCreate);
        navMessages = findViewById(R.id.navMessages);
        navProfile = findViewById(R.id.navProfile);
    }

    private void setupListeners() {
        if (btnSpinCenter != null) {
            btnSpinCenter.setOnClickListener(v -> {
                btnSpinCenter.setEnabled(false);
                if (containerWheel != null) {
                    float currentRotation = containerWheel.getRotation();
                    containerWheel.animate()
                            .rotation(currentRotation + 1440f)
                            .setDuration(2200)
                            .setInterpolator(new DecelerateInterpolator())
                            .withEndAction(() -> {
                                btnSpinCenter.setEnabled(true);
                                Intent intent = new Intent(DecideActivity.this, TodayRestaurantResultActivity.class);
                                intent.putExtra(TodayRestaurantResultActivity.EXTRA_DISH_NAME, "Pizza Ý");
                                startActivity(intent);
                            })
                            .start();
                } else {
                    btnSpinCenter.setEnabled(true);
                    Intent intent = new Intent(DecideActivity.this, TodayRestaurantResultActivity.class);
                    intent.putExtra(TodayRestaurantResultActivity.EXTRA_DISH_NAME, "Pizza Ý");
                    startActivity(intent);
                }
            });
        }

        if (btnNotification != null) {
            btnNotification.setOnClickListener(v -> {
                Intent intent = new Intent(DecideActivity.this, NotificationsActivity.class);
                startActivity(intent);
            });
        }

        if (navHome != null) {
            navHome.setOnClickListener(v -> {
                Intent intent = new Intent(DecideActivity.this, HomeActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                startActivity(intent);
                finish();
            });
        }

        if (navCreate != null) {
            navCreate.setOnClickListener(v -> {
                Intent intent = new Intent(DecideActivity.this, CreatePostActivity.class);
                startActivity(intent);
            });
        }

        if (navMessages != null) {
            navMessages.setOnClickListener(v -> {
                Intent intent = new Intent(DecideActivity.this, MessagesInboxActivity.class);
                startActivity(intent);
            });
        }

        if (navProfile != null) {
            navProfile.setOnClickListener(v -> {
                Intent intent = new Intent(DecideActivity.this, ProfileActivity.class);
                startActivity(intent);
            });
        }
    }
}
