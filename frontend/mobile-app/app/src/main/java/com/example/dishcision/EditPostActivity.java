package com.example.dishcision;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class EditPostActivity extends AppCompatActivity {

    private ImageView btnBack;
    private RelativeLayout btnSaveChange;
    private RelativeLayout btnCancelEdit;
    private TextView btnChangeLocation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_edit_post);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main_edit_post), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        initViews();
        setupListeners();
    }

    private void initViews() {
        btnBack = findViewById(R.id.btnBack);
        btnSaveChange = findViewById(R.id.btnSaveChange);
        btnCancelEdit = findViewById(R.id.btnCancelEdit);
        btnChangeLocation = findViewById(R.id.btnChangeLocation);
    }

    private void setupListeners() {
        if (btnBack != null) {
            btnBack.setOnClickListener(v -> finish());
        }

        if (btnSaveChange != null) {
            btnSaveChange.setOnClickListener(v -> finish());
        }

        if (btnCancelEdit != null) {
            btnCancelEdit.setOnClickListener(v -> finish());
        }

        if (btnChangeLocation != null) {
            btnChangeLocation.setOnClickListener(v -> {
                Intent intent = new Intent(EditPostActivity.this, AddLocationActivity.class);
                startActivity(intent);
            });
        }
    }
}
