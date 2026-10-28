package com.example.dishcision;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class HelpCenterActivity extends AppCompatActivity {

    private ImageView btnBack;
    private View cardCatAccount, cardCatDiscover, cardCatSaved, cardCatPost, cardCatMessage, cardCatSafety;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_help_center);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.btnBack), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(0, systemBars.top, 0, 0);
            return insets;
        });

        initViews();
        setupListeners();
    }

    private void initViews() {
        btnBack = findViewById(R.id.btnBack);
        cardCatAccount = findViewById(R.id.cardCatAccount);
        cardCatDiscover = findViewById(R.id.cardCatDiscover);
        cardCatSaved = findViewById(R.id.cardCatSaved);
        cardCatPost = findViewById(R.id.cardCatPost);
        cardCatMessage = findViewById(R.id.cardCatMessage);
        cardCatSafety = findViewById(R.id.cardCatSafety);
    }

    private void setupListeners() {
        if (btnBack != null) btnBack.setOnClickListener(v -> finish());

        if (cardCatAccount != null) cardCatAccount.setOnClickListener(v -> openCategory(1));
        if (cardCatDiscover != null) cardCatDiscover.setOnClickListener(v -> openCategory(2));
        if (cardCatSaved != null) cardCatSaved.setOnClickListener(v -> openCategory(3));
        if (cardCatPost != null) cardCatPost.setOnClickListener(v -> openCategory(4));
        if (cardCatMessage != null) cardCatMessage.setOnClickListener(v -> openCategory(5));
        if (cardCatSafety != null) cardCatSafety.setOnClickListener(v -> openCategory(6));
    }

    private void openCategory(int catId) {
        Intent intent = new Intent(this, HelpCategoryActivity.class);
        intent.putExtra("CAT_ID", catId);
        startActivity(intent);
    }
}
