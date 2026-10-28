package com.example.dishcision;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.dishcision.network.ApiClient;
import com.example.dishcision.network.HelpDetailResponse;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class HelpRequestDetailActivity extends AppCompatActivity {

    private ImageView btnBack;
    private View btnBackToInbox;
    private TextView tvStatusBadge, tvTicketTitle, tvTicketSubtitle, tvSubmittedContent, tvSubmissionMethod, tvContactEmail;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_help_request_detail);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main_help_detail), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        initViews();
        setupListeners();

        String ticketId = getIntent().getStringExtra("TICKET_ID");
        if (ticketId == null) ticketId = "3";
        fetchTicketDetail(ticketId);
    }

    private void initViews() {
        btnBack = findViewById(R.id.btnBack);
        btnBackToInbox = findViewById(R.id.btnBackToInbox);
        tvStatusBadge = findViewById(R.id.tvStatusBadge);
        tvTicketTitle = findViewById(R.id.tvTicketTitle);
        tvTicketSubtitle = findViewById(R.id.tvTicketSubtitle);
        tvSubmittedContent = findViewById(R.id.tvSubmittedContent);
        tvSubmissionMethod = findViewById(R.id.tvSubmissionMethod);
        tvContactEmail = findViewById(R.id.tvContactEmail);
    }

    private void setupListeners() {
        if (btnBack != null) btnBack.setOnClickListener(v -> finish());
        if (btnBackToInbox != null) btnBackToInbox.setOnClickListener(v -> finish());
    }

    private void fetchTicketDetail(String ticketId) {
        ApiClient.getApiService().getTicketDetail("Bearer dummy", ticketId).enqueue(new Callback<HelpDetailResponse>() {
            @Override
            public void onResponse(@NonNull Call<HelpDetailResponse> call, @NonNull Response<HelpDetailResponse> response) {
                if (response.isSuccessful() && response.body() != null && response.body().getData() != null) {
                    HelpDetailResponse.HelpDetailData data = response.body().getData();
                    updateUI(data);
                }
            }

            @Override
            public void onFailure(@NonNull Call<HelpDetailResponse> call, @NonNull Throwable t) {
                // Layout is already hardcoded to match the design for demo
            }
        });
    }

    private void updateUI(HelpDetailResponse.HelpDetailData data) {
        if (tvStatusBadge != null && data.getStatus() != null) tvStatusBadge.setText(data.getStatus());
        if (tvTicketTitle != null && data.getTitle() != null) tvTicketTitle.setText(data.getTitle());
        if (tvTicketSubtitle != null && data.getCategory() != null && data.getDate() != null) {
            tvTicketSubtitle.setText(data.getCategory() + " · " + data.getDate());
        }
        if (tvSubmittedContent != null && data.getSubmittedContent() != null) tvSubmittedContent.setText(data.getSubmittedContent());
        if (tvSubmissionMethod != null && data.getSubmissionMethod() != null) tvSubmissionMethod.setText(data.getSubmissionMethod());
        if (tvContactEmail != null && data.getContactEmail() != null) tvContactEmail.setText(data.getContactEmail());
    }
}