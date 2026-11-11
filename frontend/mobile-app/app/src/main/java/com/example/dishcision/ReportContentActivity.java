package com.example.dishcision;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.dishcision.network.ApiClient;
import com.example.dishcision.network.ReportContentRequest;
import com.example.dishcision.network.ReportContentResponse;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ReportContentActivity extends AppCompatActivity {

    private ImageView btnBack;
    private RadioGroup rgReasons;
    private EditText edtExtraDesc;
    private View btnSubmitReport;
    private TextView tvReportTargetTitle, tvReportTargetSubtitle, tvReportTargetExcerpt;
    private int postId = 0;
    private int reportedUserId = 0;
    private String targetTitleText = "";
    private boolean isSubmitting = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_report_content);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main_report_content), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        postId = getIntent().getIntExtra("POST_ID", 0);
        reportedUserId = getIntent().getIntExtra("REPORTED_USER_ID", 0);
        if (postId <= 0 && reportedUserId <= 0) {
            Toast.makeText(this, "Không xác định được đối tượng cần báo cáo", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        initViews();
        bindTargetContext();
        setupListeners();
    }

    private void initViews() {
        btnBack = findViewById(R.id.btnBack);
        rgReasons = findViewById(R.id.rgReasons);
        edtExtraDesc = findViewById(R.id.edtExtraDesc);
        btnSubmitReport = findViewById(R.id.btnSubmitReport);
        tvReportTargetTitle = findViewById(R.id.tvReportTargetTitle);
        tvReportTargetSubtitle = findViewById(R.id.tvReportTargetSubtitle);
        tvReportTargetExcerpt = findViewById(R.id.tvReportTargetExcerpt);
    }

    private void bindTargetContext() {
        String customTitle = getIntent().getStringExtra("TARGET_TITLE");
        String customSubtitle = getIntent().getStringExtra("TARGET_SUBTITLE");
        String customExcerpt = getIntent().getStringExtra("TARGET_EXCERPT");

        if (customTitle != null && !customTitle.trim().isEmpty()) {
            targetTitleText = customTitle.trim();
        } else {
            targetTitleText = postId > 0 ? "Bài viết trong bảng tin" : "Tài khoản người dùng";
        }

        if (tvReportTargetTitle != null) {
            tvReportTargetTitle.setText(targetTitleText);
        }

        String sub = (customSubtitle != null && !customSubtitle.trim().isEmpty())
                ? customSubtitle.trim()
                : (postId > 0 ? "Mã bài viết #" + postId : "Mã tài khoản #" + reportedUserId);
        if (tvReportTargetSubtitle != null) {
            tvReportTargetSubtitle.setText(sub);
            tvReportTargetSubtitle.setVisibility(View.VISIBLE);
        }

        if (tvReportTargetExcerpt != null) {
            if (customExcerpt != null && !customExcerpt.trim().isEmpty()) {
                tvReportTargetExcerpt.setText(customExcerpt.trim());
                tvReportTargetExcerpt.setVisibility(View.VISIBLE);
            } else {
                tvReportTargetExcerpt.setVisibility(View.GONE);
            }
        }
    }

    private void setupListeners() {
        if (btnBack != null) btnBack.setOnClickListener(v -> finish());

        if (btnSubmitReport != null) {
            btnSubmitReport.setOnClickListener(v -> submitReport());
        }
    }

    private void submitReport() {
        if (isSubmitting || rgReasons == null) return;

        String authHeader = ApiClient.getAuthHeader(this);
        if (authHeader.isEmpty()) {
            Toast.makeText(this, "Vui lòng đăng nhập lại để gửi báo cáo", Toast.LENGTH_SHORT).show();
            return;
        }

        int checkedId = rgReasons.getCheckedRadioButtonId();
        if (checkedId == -1) {
            Toast.makeText(this, "Vui lòng chọn lý do báo cáo", Toast.LENGTH_SHORT).show();
            return;
        }

        String reason = "Other";
        String reasonLabel = "Lý do khác";
        if (checkedId == R.id.rbSpam) {
            reason = "Spam";
            reasonLabel = "Nội dung rác hoặc quảng cáo lặp lại";
        } else if (checkedId == R.id.rbFake) {
            reason = "FakeNews";
            reasonLabel = "Thông tin gây hiểu nhầm";
        } else if (checkedId == R.id.rbHarass) {
            reason = "Harassment";
            reasonLabel = "Quấy rối hoặc ngôn từ thù ghét";
        } else if (checkedId == R.id.rbInappropriate) {
            reason = "Other";
            reasonLabel = "Nội dung không phù hợp";
        } else if (checkedId == R.id.rbOther) {
            reason = "Other";
            reasonLabel = "Lý do khác";
        }

        String desc = edtExtraDesc != null ? edtExtraDesc.getText().toString().trim() : "";
        final String finalReasonLabel = reasonLabel;

        isSubmitting = true;
        if (btnSubmitReport != null) btnSubmitReport.setEnabled(false);

        ReportContentRequest req = new ReportContentRequest(
                postId > 0 ? postId : null,
                reportedUserId > 0 ? reportedUserId : null,
                reason,
                desc
        );
        ApiClient.getApiService().createReport(authHeader, req).enqueue(new Callback<ReportContentResponse>() {
            @Override
            public void onResponse(@NonNull Call<ReportContentResponse> call, @NonNull Response<ReportContentResponse> response) {
                isSubmitting = false;
                if (btnSubmitReport != null) btnSubmitReport.setEnabled(true);
                if (response.isSuccessful()) {
                    String today = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(new Date());
                    Intent intent = new Intent(ReportContentActivity.this, ReportSuccessActivity.class);
                    intent.putExtra(ReportSuccessActivity.EXTRA_TARGET_TITLE, targetTitleText);
                    intent.putExtra(ReportSuccessActivity.EXTRA_REASON_TEXT, finalReasonLabel);
                    intent.putExtra(ReportSuccessActivity.EXTRA_REPORT_DATE, today);
                    startActivity(intent);
                    finish();
                } else {
                    Toast.makeText(ReportContentActivity.this, "Gửi báo cáo thất bại (mã " + response.code() + ")", Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<ReportContentResponse> call, @NonNull Throwable t) {
                isSubmitting = false;
                if (btnSubmitReport != null) btnSubmitReport.setEnabled(true);
                Toast.makeText(ReportContentActivity.this, "Lỗi kết nối máy chủ: " + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }
}
