package com.example.dishcision;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
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

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class HelpRequestDetailActivity extends AppCompatActivity {

    private ImageView btnBack;
    private View btnBackToInbox;
    private TextView tvStatusBadge, tvTicketTitle, tvTicketSubtitle, tvSubmittedContent, tvSubmissionMethod, tvContactEmail;
    private TextView tvAttachmentsHeader;
    private LinearLayout layoutAttachmentsContainer, layoutTimelineContainer;

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
        if (ticketId == null || ticketId.trim().isEmpty()) {
            Toast.makeText(this, "Không tìm thấy mã yêu cầu", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
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
        tvAttachmentsHeader = findViewById(R.id.tvAttachmentsHeader);
        layoutAttachmentsContainer = findViewById(R.id.layoutAttachmentsContainer);
        layoutTimelineContainer = findViewById(R.id.layoutTimelineContainer);
    }

    private void setupListeners() {
        if (btnBack != null) btnBack.setOnClickListener(v -> finish());
        if (btnBackToInbox != null) btnBackToInbox.setOnClickListener(v -> finish());
    }

    private void fetchTicketDetail(String ticketId) {
        String authHeader = ApiClient.getAuthHeader(this);
        if (authHeader.isEmpty()) {
            Toast.makeText(this, "Vui lòng đăng nhập lại", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        ApiClient.getApiService().getTicketDetail(authHeader, ticketId).enqueue(new Callback<HelpDetailResponse>() {
            @Override
            public void onResponse(@NonNull Call<HelpDetailResponse> call, @NonNull Response<HelpDetailResponse> response) {
                if (response.isSuccessful() && response.body() != null && response.body().getData() != null) {
                    updateUI(response.body().getData());
                } else {
                    Toast.makeText(HelpRequestDetailActivity.this, "Không thể tải chi tiết yêu cầu", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<HelpDetailResponse> call, @NonNull Throwable t) {
                Toast.makeText(HelpRequestDetailActivity.this, "Lỗi kết nối máy chủ", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateUI(HelpDetailResponse.HelpDetailData data) {
        if (tvStatusBadge != null) tvStatusBadge.setText(data.getStatus() != null ? data.getStatus() : "");
        if (tvTicketTitle != null) tvTicketTitle.setText(data.getTitle() != null ? data.getTitle() : "");
        if (tvTicketSubtitle != null) {
            String cat = data.getCategory() != null ? data.getCategory() : "";
            String dt = data.getDate() != null ? data.getDate() : "";
            tvTicketSubtitle.setText(!cat.isEmpty() && !dt.isEmpty() ? cat + " · " + dt : cat + dt);
        }
        if (tvSubmittedContent != null) tvSubmittedContent.setText(data.getSubmittedContent() != null ? data.getSubmittedContent() : "");
        if (tvSubmissionMethod != null) tvSubmissionMethod.setText(data.getSubmissionMethod() != null ? data.getSubmissionMethod() : "");
        if (tvContactEmail != null) {
            String email = data.getContactEmail();
            tvContactEmail.setText((email != null && !email.isEmpty())
                    ? "Email liên hệ: " + email
                    : "Email liên hệ: Chưa cập nhật");
        }

        renderAttachments(data.getAttachments());
        renderTimeline(data.getTimeline());
    }

    private void renderAttachments(List<HelpDetailResponse.Attachment> attachments) {
        if (layoutAttachmentsContainer == null) return;
        layoutAttachmentsContainer.removeAllViews();

        if (attachments == null || attachments.isEmpty()) {
            if (tvAttachmentsHeader != null) tvAttachmentsHeader.setVisibility(View.GONE);
            layoutAttachmentsContainer.setVisibility(View.GONE);
            return;
        }

        if (tvAttachmentsHeader != null) tvAttachmentsHeader.setVisibility(View.VISIBLE);
        layoutAttachmentsContainer.setVisibility(View.VISIBLE);

        for (int i = 0; i < attachments.size(); i++) {
            HelpDetailResponse.Attachment att = attachments.get(i);
            LinearLayout row = new LinearLayout(this);
            row.setOrientation(LinearLayout.HORIZONTAL);
            row.setGravity(Gravity.CENTER_VERTICAL);
            int pad = dpToPx(16);
            row.setPadding(pad, pad, pad, pad);

            RelativeLayout iconBox = new RelativeLayout(this);
            iconBox.setLayoutParams(new LinearLayout.LayoutParams(dpToPx(40), dpToPx(40)));
            iconBox.setBackgroundResource(R.drawable.bg_recommend_card);
            ImageView img = new ImageView(this);
            RelativeLayout.LayoutParams imgParams = new RelativeLayout.LayoutParams(dpToPx(20), dpToPx(20));
            imgParams.addRule(RelativeLayout.CENTER_IN_PARENT);
            img.setLayoutParams(imgParams);
            img.setImageResource(R.drawable.ic_image);
            img.setColorFilter(Color.parseColor("#141B2B"));
            iconBox.addView(img);

            LinearLayout infoCol = new LinearLayout(this);
            infoCol.setOrientation(LinearLayout.VERTICAL);
            LinearLayout.LayoutParams infoParams = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f);
            infoParams.setMarginStart(dpToPx(12));
            infoCol.setLayoutParams(infoParams);

            TextView tvName = new TextView(this);
            tvName.setText(att.getName() != null && !att.getName().isEmpty() ? att.getName() : "Tệp đính kèm");
            tvName.setTextColor(Color.parseColor("#141B2B"));
            tvName.setTextSize(14f);
            tvName.setTypeface(null, Typeface.BOLD);

            TextView tvSub = new TextView(this);
            tvSub.setText(att.getSubtitle() != null && !att.getSubtitle().isEmpty() ? att.getSubtitle() : "Tệp đã gửi");
            tvSub.setTextColor(Color.parseColor("#6B7280"));
            tvSub.setTextSize(12f);

            infoCol.addView(tvName);
            infoCol.addView(tvSub);

            ImageView arrow = new ImageView(this);
            arrow.setLayoutParams(new LinearLayout.LayoutParams(dpToPx(16), dpToPx(16)));
            arrow.setImageResource(R.drawable.ic_chevron_right);
            arrow.setColorFilter(Color.parseColor("#141B2B"));

            row.addView(iconBox);
            row.addView(infoCol);
            row.addView(arrow);

            row.setOnClickListener(v -> {
                String url = ApiClient.normalizeMediaUrl(att.getUrl());
                if (url.isEmpty()) {
                    Toast.makeText(this, "Không có đường dẫn tệp", Toast.LENGTH_SHORT).show();
                    return;
                }
                try {
                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(url)));
                } catch (Exception e) {
                    Toast.makeText(this, "Không thể mở tệp đính kèm", Toast.LENGTH_SHORT).show();
                }
            });

            layoutAttachmentsContainer.addView(row);
            if (i < attachments.size() - 1) {
                View divider = new View(this);
                divider.setBackgroundColor(Color.parseColor("#E9EBF4"));
                LinearLayout.LayoutParams dp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, dpToPx(1));
                dp.setMarginStart(dpToPx(16));
                dp.setMarginEnd(dpToPx(16));
                divider.setLayoutParams(dp);
                layoutAttachmentsContainer.addView(divider);
            }
        }
    }

    private void renderTimeline(List<HelpDetailResponse.TimelineItem> timeline) {
        if (layoutTimelineContainer == null) return;
        layoutTimelineContainer.removeAllViews();
        if (timeline == null || timeline.isEmpty()) return;

        for (int i = 0; i < timeline.size(); i++) {
            HelpDetailResponse.TimelineItem step = timeline.get(i);
            boolean isLast = (i == timeline.size() - 1);

            LinearLayout row = new LinearLayout(this);
            row.setOrientation(LinearLayout.HORIZONTAL);
            if (i > 0) {
                LinearLayout.LayoutParams rowParams = new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                rowParams.topMargin = dpToPx(4);
                row.setLayoutParams(rowParams);
            }

            LinearLayout leftIndicator = new LinearLayout(this);
            leftIndicator.setOrientation(LinearLayout.VERTICAL);
            leftIndicator.setGravity(Gravity.CENTER_HORIZONTAL);

            RelativeLayout circle = new RelativeLayout(this);
            circle.setLayoutParams(new LinearLayout.LayoutParams(dpToPx(28), dpToPx(28)));
            circle.setBackgroundResource(step.isDone() ? R.drawable.bg_badge_verified_green : R.drawable.bg_icon_circle_light);

            ImageView icon = new ImageView(this);
            RelativeLayout.LayoutParams iconParams = new RelativeLayout.LayoutParams(dpToPx(16), dpToPx(16));
            iconParams.addRule(RelativeLayout.CENTER_IN_PARENT);
            icon.setLayoutParams(iconParams);
            icon.setImageResource(step.isDone() ? R.drawable.ic_check_small : R.drawable.ic_book);
            icon.setColorFilter(Color.parseColor(step.isDone() ? "#059669" : "#5A4744"));
            circle.addView(icon);
            leftIndicator.addView(circle);

            if (!isLast) {
                View line = new View(this);
                line.setLayoutParams(new LinearLayout.LayoutParams(dpToPx(2), dpToPx(40)));
                line.setBackgroundColor(Color.parseColor("#D1FAE5"));
                leftIndicator.addView(line);
            }

            LinearLayout contentCol = new LinearLayout(this);
            contentCol.setOrientation(LinearLayout.VERTICAL);
            LinearLayout.LayoutParams contentParams = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f);
            contentParams.setMarginStart(dpToPx(12));
            contentCol.setLayoutParams(contentParams);

            TextView tvStep = new TextView(this);
            tvStep.setText(step.getStep() != null ? step.getStep() : "");
            tvStep.setTextColor(Color.parseColor("#141B2B"));
            tvStep.setTextSize(14f);
            tvStep.setTypeface(null, Typeface.BOLD);

            String dateStr = step.getDate() != null ? step.getDate() : "";
            String descStr = step.getDesc() != null ? step.getDesc() : "";
            String fullDesc = (!dateStr.isEmpty() && !descStr.isEmpty()) ? dateStr + " · " + descStr : dateStr + descStr;

            TextView tvDesc = new TextView(this);
            tvDesc.setText(fullDesc);
            tvDesc.setTextColor(Color.parseColor("#6B7280"));
            tvDesc.setTextSize(13f);
            LinearLayout.LayoutParams descParams = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            descParams.topMargin = dpToPx(4);
            tvDesc.setLayoutParams(descParams);

            contentCol.addView(tvStep);
            contentCol.addView(tvDesc);

            row.addView(leftIndicator);
            row.addView(contentCol);
            layoutTimelineContainer.addView(row);
        }
    }

    private int dpToPx(int dp) {
        float density = getResources().getDisplayMetrics().density;
        return Math.round((float) dp * density);
    }
}
