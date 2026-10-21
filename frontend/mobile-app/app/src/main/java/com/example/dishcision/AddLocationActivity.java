package com.example.dishcision;

import android.os.Bundle;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class AddLocationActivity extends AppCompatActivity {

    private ImageView btnBack;
    private RelativeLayout btnConfirmLocation;
    private RelativeLayout btnNoLocation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_add_location);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main_add_location), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        initViews();
        setupListeners();
    }

    private void initViews() {
        btnBack = findViewById(R.id.btnBack);
        btnConfirmLocation = findViewById(R.id.btnConfirmLocation);
        btnNoLocation = findViewById(R.id.btnNoLocation);
    }

    private void setupListeners() {
        if (btnBack != null) {
            btnBack.setOnClickListener(v -> finish());
        }

        if (btnConfirmLocation != null) {
            btnConfirmLocation.setOnClickListener(v -> finish());
        }

        if (btnNoLocation != null) {
            btnNoLocation.setOnClickListener(v -> finish());
        }
    }
}
