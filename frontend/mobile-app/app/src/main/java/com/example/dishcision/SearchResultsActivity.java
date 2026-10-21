package com.example.dishcision;

import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.bottomsheet.BottomSheetDialog;

public class SearchResultsActivity extends AppCompatActivity {

    private ImageView btnBack;
    private EditText edtSearchQuery;
    private ImageView btnClearSearch;
    private LinearLayout btnOpenFilter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_search_results);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main_search_results), (v, insets) -> {
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
        btnOpenFilter = findViewById(R.id.btnOpenFilter);
    }

    private void setupListeners() {
        if (btnBack != null) {
            btnBack.setOnClickListener(v -> finish());
        }

        if (btnClearSearch != null && edtSearchQuery != null) {
            btnClearSearch.setOnClickListener(v -> edtSearchQuery.setText(""));
        }

        if (btnOpenFilter != null) {
            btnOpenFilter.setOnClickListener(v -> openFilterBottomSheet());
        }
    }

    private void openFilterBottomSheet() {
        BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(SearchResultsActivity.this);
        View dialogView = getLayoutInflater().inflate(R.layout.layout_dialog_filter, null);
        bottomSheetDialog.setContentView(dialogView);

        View btnCloseFilter = dialogView.findViewById(R.id.btnCloseFilter);
        View btnApplyFilter = dialogView.findViewById(R.id.btnApplyFilter);

        if (btnCloseFilter != null) {
            btnCloseFilter.setOnClickListener(v -> bottomSheetDialog.dismiss());
        }

        if (btnApplyFilter != null) {
            btnApplyFilter.setOnClickListener(v -> bottomSheetDialog.dismiss());
        }

        bottomSheetDialog.show();
    }
}
