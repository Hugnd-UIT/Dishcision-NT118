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

public class ReportSuccessActivity extends AppCompatActivity {

    public static final String EXTRA_TARGET_TITLE = "extra_target_title";
    public static final String EXTRA_REASON_TEXT = "extra_reason_text";
    public static final String EXTRA_REPORT_DATE = "extra_report_date";

    private ImageView btnBack;
    private View btnViewInbox;
    private View btnManageInteractions;
    private TextView tvSuccessTarget, tvSuccessReason, tvSuccessDate;

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
        btnManageInteractions = findViewById(R.id.btnManageInteractions);
        tvSuccessTarget = findViewById(R.id.tvSuccessTarget);
        tvSuccessReason = findViewById(R.id.tvSuccessReason);
        tvSuccessDate = findViewById(R.id.tvSuccessDate);

        String targetTitle = getIntent().getStringExtra(EXTRA_TARGET_TITLE);
        String reasonText = getIntent().getStringExtra(EXTRA_REASON_TEXT);
        String reportDate = getIntent().getStringExtra(EXTRA_REPORT_DATE);

        if (tvSuccessTarget != null && targetTitle != null && !targetTitle.isEmpty()) {
            tvSuccessTarget.setText(targetTitle);
        }
        if (tvSuccessReason != null) {
            tvSuccessReason.setText(reasonText != null ? reasonText : "");
        }
        if (tvSuccessDate != null) {
            tvSuccessDate.setText(reportDate != null ? reportDate : "");
        }

        if (btnBack != null) {
            btnBack.setOnClickListener(v -> finish());
        }

        if (btnViewInbox != null) {
            btnViewInbox.setOnClickListener(v -> {
                startActivity(new Intent(this, HelpInboxActivity.class));
                finish();
            });
        }

        if (btnManageInteractions != null) {
            btnManageInteractions.setOnClickListener(v -> startActivity(new Intent(this, PrivacySettingsActivity.class)));
        }
    }
}
