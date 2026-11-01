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

public class ReportSuccessActivity extends AppCompatActivity {

    private ImageView btnBack;
    private View btnViewInbox;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_report_success);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main_report_success), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        btnBack = findViewById(R.id.btnBack);
        btnViewInbox = findViewById(R.id.btnViewInbox);

        if (btnBack != null) btnBack.setOnClickListener(v -> finish());
        
        if (btnViewInbox != null) {
            btnViewInbox.setOnClickListener(v -> {
                Intent intent = new Intent(this, HelpInboxActivity.class);
                startActivity(intent);
                finish();
            });
        }
    }
}