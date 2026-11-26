package com.example.dishcision;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

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

    private void setupFaqFilter() {
        final android.view.View list = findViewById(R.id.faqList);
        final String[] topics = {"all", "account", "security", "post"};
        final TextView[] chips = {
                findViewById(R.id.chipFaqAll), findViewById(R.id.chipFaqAccount),
                findViewById(R.id.chipFaqSecurity), findViewById(R.id.chipFaqPost)};
        for (int i = 0; i < chips.length; i++) {
            final String topic = topics[i];
            chips[i].setOnClickListener(v -> {
                for (int k = 0; k < chips.length; k++) {
                    boolean on = chips[k] == v;
                    chips[k].setBackgroundResource(on ? R.drawable.bg_recommend_card : R.drawable.bg_chip_preference);
                    chips[k].setTextColor(on ? 0xFFC5221F : 0xFF505568);
                }
                android.view.ViewGroup group = (android.view.ViewGroup) list;
                for (int c = 0; c < group.getChildCount(); c++) {
                    android.view.View card = group.getChildAt(c);
                    Object tag = card.getTag();
                    boolean show = "all".equals(topic)
                            || (tag != null && java.util.Arrays.asList(tag.toString().split(",")).contains(topic));
                    card.setVisibility(show ? android.view.View.VISIBLE : android.view.View.GONE);
                }
            });
        }
    }

    private void setupListeners() {
        setupFaqFilter();
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
