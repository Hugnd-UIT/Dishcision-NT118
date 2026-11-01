package com.example.dishcision;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioGroup;
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

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ReportContentActivity extends AppCompatActivity {

    private ImageView btnBack;
    private RadioGroup rgReasons;
    private EditText edtExtraDesc;
    private View btnSubmitReport;

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

        initViews();
        setupListeners();
    }

    private void initViews() {
        btnBack = findViewById(R.id.btnBack);
        rgReasons = findViewById(R.id.rgReasons);
        edtExtraDesc = findViewById(R.id.edtExtraDesc);
        btnSubmitReport = findViewById(R.id.btnSubmitReport);
    }

    private void setupListeners() {
        if (btnBack != null) btnBack.setOnClickListener(v -> finish());
        
        if (btnSubmitReport != null) {
            btnSubmitReport.setOnClickListener(v -> submitReport());
        }
    }

    private void submitReport() {
        if (rgReasons == null) return;
        int checkedId = rgReasons.getCheckedRadioButtonId();
        if (checkedId == -1) {
            Toast.makeText(this, "Vui lòng chọn lý do báo cáo", Toast.LENGTH_SHORT).show();
            return;
        }

        String reason = "Other";
        if (checkedId == R.id.rbSpam) reason = "Spam";
        else if (checkedId == R.id.rbFake) reason = "FakeNews";
        else if (checkedId == R.id.rbHarass) reason = "Harassment";
        else if (checkedId == R.id.rbInappropriate) reason = "Other";
        
        String desc = edtExtraDesc != null ? edtExtraDesc.getText().toString().trim() : "";

        // Assuming post 1 is being reported
        ReportContentRequest req = new ReportContentRequest(1, null, reason, desc);
        ApiClient.getApiService().createReport("Bearer dummy_token", req).enqueue(new Callback<ReportContentResponse>() {
            @Override
            public void onResponse(@NonNull Call<ReportContentResponse> call, @NonNull Response<ReportContentResponse> response) {
                Intent intent = new Intent(ReportContentActivity.this, ReportSuccessActivity.class);
                startActivity(intent);
                finish();
            }

            @Override
            public void onFailure(@NonNull Call<ReportContentResponse> call, @NonNull Throwable t) {
                Intent intent = new Intent(ReportContentActivity.this, ReportSuccessActivity.class);
                startActivity(intent);
                finish();
            }
        });
    }
}