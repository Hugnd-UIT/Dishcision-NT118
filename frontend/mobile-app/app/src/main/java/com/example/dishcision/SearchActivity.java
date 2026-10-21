package com.example.dishcision;

import android.content.Intent;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class SearchActivity extends AppCompatActivity {

    private ImageView btnBack;
    private EditText edtSearchQuery;
    private ImageView btnClearSearch;
    private TextView btnDoSearch;
    private LinearLayout cardBestMatch;
    private RelativeLayout btnSpinWheel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_search);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main_search), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        initViews();
        setupListeners();
    }

    private void initViews() {
        btnBack = findViewById(R.id.btnBack);
        edtSearchQuery = findViewById(R.id.edtSearchQuery);
        btnClearSearch = findViewById(R.id.btnClearSearch);
        btnDoSearch = findViewById(R.id.btnDoSearch);
        cardBestMatch = findViewById(R.id.cardBestMatch);
        btnSpinWheel = findViewById(R.id.btnSpinWheel);
    }

    private void setupListeners() {
        if (btnBack != null) {
            btnBack.setOnClickListener(v -> finish());
        }

        if (btnClearSearch != null && edtSearchQuery != null) {
            btnClearSearch.setOnClickListener(v -> edtSearchQuery.setText(""));
        }

        if (btnDoSearch != null) {
            btnDoSearch.setOnClickListener(v -> {
                Intent intent = new Intent(SearchActivity.this, SearchResultsActivity.class);
                startActivity(intent);
            });
        }

        if (btnSpinWheel != null) {
            btnSpinWheel.setOnClickListener(v -> {
                Intent intent = new Intent(SearchActivity.this, DecideActivity.class);
                startActivity(intent);
            });
        }
    }
}
