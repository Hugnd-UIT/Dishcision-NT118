package com.example.dishcision;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
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
import com.example.dishcision.network.HelpInboxResponse;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class HelpInboxActivity extends AppCompatActivity {

    private ImageView btnBack;
    private TextView tabAll, tabReport, tabRequest;
    private LinearLayout layoutListsContainer;
    private View btnBackToHelp;

    private List<HelpInboxResponse.HelpItem> allItems = new ArrayList<>();
    private String currentTab = "ALL";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_help_inbox);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main_help_inbox), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        initViews();
        setupListeners();
        fetchInboxFromBackend();
    }

    private void initViews() {
        btnBack = findViewById(R.id.btnBack);
        tabAll = findViewById(R.id.tabAll);
        tabReport = findViewById(R.id.tabReport);
        tabRequest = findViewById(R.id.tabRequest);
        layoutListsContainer = findViewById(R.id.layoutListsContainer);
        btnBackToHelp = findViewById(R.id.btnBackToHelp);
    }

    private void setupListeners() {
        if (btnBack != null) btnBack.setOnClickListener(v -> finish());
        
        if (btnBackToHelp != null) {
            btnBackToHelp.setOnClickListener(v -> {
                Intent intent = new Intent(this, HelpCenterActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
                finish();
            });
        }

        tabAll.setOnClickListener(v -> switchTab("ALL"));
        tabReport.setOnClickListener(v -> switchTab("REPORT"));
        tabRequest.setOnClickListener(v -> switchTab("SUPPORT"));
    }

    private void switchTab(String tab) {
        currentTab = tab;
        
        // Reset tabs style
        tabAll.setBackground(null);
        tabAll.setTextColor(Color.parseColor("#6B7280"));
        tabReport.setBackground(null);
        tabReport.setTextColor(Color.parseColor("#6B7280"));
        tabRequest.setBackground(null);
        tabRequest.setTextColor(Color.parseColor("#6B7280"));

        if ("ALL".equals(tab)) {
            tabAll.setBackgroundResource(R.drawable.bg_tab_active_white);
            tabAll.setTextColor(Color.parseColor("#C5221F"));
        } else if ("REPORT".equals(tab)) {
            tabReport.setBackgroundResource(R.drawable.bg_tab_active_white);
            tabReport.setTextColor(Color.parseColor("#C5221F"));
        } else if ("SUPPORT".equals(tab)) {
            tabRequest.setBackgroundResource(R.drawable.bg_tab_active_white);
            tabRequest.setTextColor(Color.parseColor("#C5221F"));
        }

        renderLists();
    }

    private void fetchInboxFromBackend() {
        ApiClient.getApiService().getHelpInbox("Bearer dummy_token").enqueue(new Callback<HelpInboxResponse>() {
            @Override
            public void onResponse(@NonNull Call<HelpInboxResponse> call, @NonNull Response<HelpInboxResponse> response) {
                if (response.isSuccessful() && response.body() != null && response.body().getData() != null) {
                    allItems = response.body().getData().getItems();
                    renderLists();
                }
            }

            @Override
            public void onFailure(@NonNull Call<HelpInboxResponse> call, @NonNull Throwable t) {
                // Ignore for now, layout has static mock content already
            }
        });
    }

    private void renderLists() {
        if (layoutListsContainer == null || allItems == null || allItems.isEmpty()) return;
        layoutListsContainer.removeAllViews();

        List<HelpInboxResponse.HelpItem> reports = new ArrayList<>();
        List<HelpInboxResponse.HelpItem> supports = new ArrayList<>();

        for (HelpInboxResponse.HelpItem item : allItems) {
            if ("Report".equals(item.getType())) reports.add(item);
            else supports.add(item);
        }

        if ("ALL".equals(currentTab) || "REPORT".equals(currentTab)) {
            if (!reports.isEmpty()) {
                addSectionHeader("Báo cáo · " + reports.size());
                LinearLayout card = createCardContainer();
                for (int i = 0; i < reports.size(); i++) {
                    card.addView(createItemView(reports.get(i), true));
                    if (i < reports.size() - 1) card.addView(createDivider());
                }
                layoutListsContainer.addView(card);
            }
        }

        if ("ALL".equals(currentTab) || "SUPPORT".equals(currentTab)) {
            if (!supports.isEmpty()) {
                addSectionHeader("Yêu cầu hỗ trợ · " + supports.size());
                LinearLayout card = createCardContainer();
                for (int i = 0; i < supports.size(); i++) {
                    card.addView(createItemView(supports.get(i), false));
                    if (i < supports.size() - 1) card.addView(createDivider());
                }
                layoutListsContainer.addView(card);
            }
        }
    }

    private void addSectionHeader(String title) {
        TextView header = new TextView(this);
        header.setText(title);
        header.setTextColor(Color.parseColor("#141B2B"));
        header.setTextSize(16f);
        header.setTypeface(null, Typeface.BOLD);
        
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        params.topMargin = dpToPx(24);
        header.setLayoutParams(params);
        
        layoutListsContainer.addView(header);
    }

    private LinearLayout createCardContainer() {
        LinearLayout card = new LinearLayout(this);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setBackgroundResource(R.drawable.bg_card);
        
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        params.topMargin = dpToPx(12);
        card.setLayoutParams(params);
        
        return card;
    }

    private View createItemView(HelpInboxResponse.HelpItem item, boolean isReport) {
        // Just inflate a simple layout for each item or create programmatically
        // For brevity in code generation, we'll navigate to detail on click
        
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(dpToPx(16), dpToPx(16), dpToPx(16), dpToPx(16));
        
        // Emulate the row layout programmatically to replace the mock
        LinearLayout row1 = new LinearLayout(this);
        row1.setOrientation(LinearLayout.HORIZONTAL);
        row1.setGravity(Gravity.CENTER_VERTICAL);
        
        ImageView icon = new ImageView(this);
        icon.setImageResource(isReport ? R.drawable.ic_book : R.drawable.ic_headset);
        icon.setColorFilter(Color.parseColor(isReport ? "#141B2B" : "#505568"));
        
        LinearLayout textContainer = new LinearLayout(this);
        textContainer.setOrientation(LinearLayout.VERTICAL);
        LinearLayout.LayoutParams textParams = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f);
        textParams.setMarginStart(dpToPx(12));
        textContainer.setLayoutParams(textParams);
        
        TextView title = new TextView(this);
        title.setText(item.getTitle());
        title.setTextColor(Color.parseColor("#141B2B"));
        title.setTextSize(14f);
        title.setTypeface(null, Typeface.BOLD);
        
        TextView subtitle = new TextView(this);
        subtitle.setText(item.getSubtitle());
        subtitle.setTextColor(Color.parseColor("#6B7280"));
        subtitle.setTextSize(13f);
        
        textContainer.addView(title);
        textContainer.addView(subtitle);
        
        ImageView arrow = new ImageView(this);
        arrow.setImageResource(R.drawable.ic_chevron_right);
        
        row1.addView(icon);
        row1.addView(textContainer);
        row1.addView(arrow);
        
        // Row 2
        RelativeLayout row2 = new RelativeLayout(this);
        LinearLayout.LayoutParams row2Params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        row2Params.topMargin = dpToPx(16);
        row2.setLayoutParams(row2Params);
        
        TextView date = new TextView(this);
        date.setText(item.getDate());
        date.setTextColor(Color.parseColor("#6B7280"));
        date.setTextSize(12f);
        
        RelativeLayout.LayoutParams dateParams = new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
        dateParams.addRule(RelativeLayout.CENTER_VERTICAL);
        date.setLayoutParams(dateParams);
        
        TextView status = new TextView(this);
        status.setText(item.getStatus());
        status.setBackgroundResource(R.drawable.bg_badge_verified_green);
        status.setTextColor(Color.parseColor("#059669"));
        status.setTextSize(12f);
        status.setTypeface(null, Typeface.BOLD);
        status.setPadding(dpToPx(10), dpToPx(4), dpToPx(10), dpToPx(4));
        
        RelativeLayout.LayoutParams statusParams = new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
        statusParams.addRule(RelativeLayout.ALIGN_PARENT_END);
        status.setLayoutParams(statusParams);
        
        row2.addView(date);
        row2.addView(status);
        
        root.addView(row1);
        root.addView(row2);
        
        // Add click listener
        root.setOnClickListener(v -> {
            if (!isReport) {
                Intent intent = new Intent(this, HelpRequestDetailActivity.class);
                intent.putExtra("TICKET_ID", item.getId());
                startActivity(intent);
            } else {
                Toast.makeText(this, "Tính năng xem chi tiết báo cáo chưa có thiết kế", Toast.LENGTH_SHORT).show();
            }
        });
        
        return root;
    }

    private View createDivider() {
        View divider = new View(this);
        divider.setBackgroundColor(Color.parseColor("#E9EBF4"));
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, dpToPx(1));
        params.setMarginStart(dpToPx(16));
        params.setMarginEnd(dpToPx(16));
        divider.setLayoutParams(params);
        return divider;
    }
    
    private int dpToPx(int dp) {
        float density = getResources().getDisplayMetrics().density;
        return Math.round((float) dp * density);
    }
}