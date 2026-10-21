package com.example.dishcision;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.bottomsheet.BottomSheetDialog;

public class DishDetailActivity extends AppCompatActivity {

    private ImageView btnBack;
    private RelativeLayout btnViewStoreMenu;
    private TextView btnGoToStoreDetail;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_dish_detail);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main_dish_detail), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        initViews();
        setupListeners();
    }

    private void initViews() {
        btnBack = findViewById(R.id.btnBack);
        btnViewStoreMenu = findViewById(R.id.btnViewStoreMenu);
        btnGoToStoreDetail = findViewById(R.id.btnGoToStoreDetail);
    }

    private void setupListeners() {
        if (btnBack != null) {
            btnBack.setOnClickListener(v -> finish());
        }

        if (btnViewStoreMenu != null) {
            btnViewStoreMenu.setOnClickListener(v -> openMenuBottomSheet());
        }

        if (btnGoToStoreDetail != null) {
            btnGoToStoreDetail.setOnClickListener(v -> {
                Intent intent = new Intent(DishDetailActivity.this, StoreDetailActivity.class);
                startActivity(intent);
            });
        }
    }

    private void openMenuBottomSheet() {
        BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(DishDetailActivity.this);
        View dialogView = getLayoutInflater().inflate(R.layout.layout_dialog_menu_bottomsheet, null);
        bottomSheetDialog.setContentView(dialogView);

        View btnCloseMenu = dialogView.findViewById(R.id.btnCloseMenu);
        if (btnCloseMenu != null) {
            btnCloseMenu.setOnClickListener(v -> bottomSheetDialog.dismiss());
        }

        bottomSheetDialog.show();
    }
}
