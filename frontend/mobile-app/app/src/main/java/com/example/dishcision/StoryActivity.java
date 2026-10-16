package com.example.dishcision;

import android.os.Bundle;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class StoryActivity extends AppCompatActivity {

    public static final String EXTRA_STORE_NAME = "extra_store_name";

    private ImageView btnCloseStory;
    private TextView tvStoryStoreName;
    private TextView tvStoryTitle;
    private EditText edtReply;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_story);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main_story), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        initViews();
        setupData();
        setupListeners();
    }

    private void initViews() {
        btnCloseStory = findViewById(R.id.btnCloseStory);
        tvStoryStoreName = findViewById(R.id.tvStoryStoreName);
        tvStoryTitle = findViewById(R.id.tvStoryTitle);
        edtReply = findViewById(R.id.edtReply);
    }

    private void setupData() {
        String storeName = getIntent().getStringExtra(EXTRA_STORE_NAME);
        if (storeName != null && !storeName.isEmpty()) {
            if (tvStoryStoreName != null) tvStoryStoreName.setText(storeName);
            if (tvStoryTitle != null) tvStoryTitle.setText(storeName);
            if (edtReply != null) edtReply.setHint("Trả lời " + storeName + "...");
        }
    }

    private void setupListeners() {
        if (btnCloseStory != null) {
            btnCloseStory.setOnClickListener(v -> finish());
        }
    }
}
