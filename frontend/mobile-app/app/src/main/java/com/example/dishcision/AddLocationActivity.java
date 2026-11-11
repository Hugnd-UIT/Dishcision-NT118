package com.example.dishcision;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.dishcision.network.ApiClient;
import com.example.dishcision.network.FollowingRestaurantsResponse;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AddLocationActivity extends AppCompatActivity {

    private ImageView btnBack;
    private EditText edtLocationQuery;
    private ImageView btnClearSearch;
    private LinearLayout layoutSelectedSection;
    private TextView tvSelectedLocationName;
    private TextView tvSelectedLocationAddress;
    private LinearLayout layoutLocationsContainer;
    private TextView tvEmptyLocations;
    private RelativeLayout btnConfirmLocation;
    private RelativeLayout btnNoLocation;

    private final List<FollowingRestaurantsResponse.StoreItem> stores = new ArrayList<>();
    private String selectedName = "";
    private String selectedAddress = "";
    private String latestQuery = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_add_location);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main_add_location), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        ApiClient.init(this);
        initViews();
        setupListeners();
        searchLocations("");
    }

    private void initViews() {
        btnBack = findViewById(R.id.btnBack);
        edtLocationQuery = findViewById(R.id.edtLocationQuery);
        btnClearSearch = findViewById(R.id.btnClearSearch);
        layoutSelectedSection = findViewById(R.id.layoutSelectedSection);
        tvSelectedLocationName = findViewById(R.id.tvSelectedLocationName);
        tvSelectedLocationAddress = findViewById(R.id.tvSelectedLocationAddress);
        layoutLocationsContainer = findViewById(R.id.layoutLocationsContainer);
        tvEmptyLocations = findViewById(R.id.tvEmptyLocations);
        btnConfirmLocation = findViewById(R.id.btnConfirmLocation);
        btnNoLocation = findViewById(R.id.btnNoLocation);
    }

    private void setupListeners() {
        if (btnBack != null) btnBack.setOnClickListener(v -> finish());

        if (btnClearSearch != null) {
            btnClearSearch.setOnClickListener(v -> {
                if (edtLocationQuery != null) edtLocationQuery.setText("");
            });
        }

        if (edtLocationQuery != null) {
            edtLocationQuery.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {}

                @Override
                public void afterTextChanged(Editable s) {
                    searchLocations(s != null ? s.toString().trim() : "");
                }
            });
        }

        if (btnConfirmLocation != null) {
            btnConfirmLocation.setOnClickListener(v -> {
                String query = edtLocationQuery != null ? edtLocationQuery.getText().toString().trim() : "";
                if (selectedName.isEmpty() && !query.isEmpty()) {
                    selectedName = query;
                }
                if (selectedName.isEmpty()) {
                    Toast.makeText(this, "Vui lòng chọn hoặc nhập tên địa điểm", Toast.LENGTH_SHORT).show();
                    return;
                }
                String url = "https://maps.google.com/?q=" + Uri.encode(selectedAddress.isEmpty() ? selectedName : (selectedName + " " + selectedAddress));
                Intent result = new Intent();
                result.putExtra("LOCATION_NAME", selectedName);
                result.putExtra("LOCATION_ADDRESS", selectedAddress);
                result.putExtra("LOCATION_URL", url);
                setResult(RESULT_OK, result);
                finish();
            });
        }

        if (btnNoLocation != null) {
            btnNoLocation.setOnClickListener(v -> {
                Intent result = new Intent();
                result.putExtra("LOCATION_NAME", "");
                result.putExtra("LOCATION_ADDRESS", "");
                result.putExtra("LOCATION_URL", "");
                setResult(RESULT_OK, result);
                finish();
            });
        }
    }

    private void searchLocations(String query) {
        latestQuery = query;
        String authHeader = ApiClient.getAuthHeader(this);
        String queryParam = query.isEmpty() ? null : query;

        ApiClient.getApiService().getStores(authHeader, queryParam).enqueue(new Callback<FollowingRestaurantsResponse>() {
            @Override
            public void onResponse(@NonNull Call<FollowingRestaurantsResponse> call, @NonNull Response<FollowingRestaurantsResponse> response) {
                if (!query.equals(latestQuery)) return;
                stores.clear();
                if (response.isSuccessful() && response.body() != null && response.body().getData() != null
                        && response.body().getData().restaurants != null) {
                    stores.addAll(response.body().getData().restaurants);
                }
                renderLocationResults();
            }

            @Override
            public void onFailure(@NonNull Call<FollowingRestaurantsResponse> call, @NonNull Throwable t) {
                if (!query.equals(latestQuery)) return;
                stores.clear();
                renderLocationResults();
            }
        });
    }

    private void renderLocationResults() {
        if (layoutLocationsContainer == null) return;
        layoutLocationsContainer.removeAllViews();

        List<String[]> rows = new ArrayList<>();
        for (FollowingRestaurantsResponse.StoreItem st : stores) {
            rows.add(new String[]{
                    st.name != null ? st.name : "",
                    !TextUtils.isEmpty(st.address) ? st.address : "Quán ăn trên Dishcision"
            });
        }

        if (!latestQuery.isEmpty()) {
            boolean exactMatch = false;
            for (FollowingRestaurantsResponse.StoreItem st : stores) {
                if (st.name != null && st.name.equalsIgnoreCase(latestQuery)) {
                    exactMatch = true;
                    break;
                }
            }
            if (!exactMatch) {
                rows.add(new String[]{latestQuery, "Sử dụng tên địa điểm đã nhập"});
            }
        }

        if (rows.isEmpty()) {
            layoutLocationsContainer.setVisibility(View.GONE);
            if (tvEmptyLocations != null) tvEmptyLocations.setVisibility(View.VISIBLE);
            return;
        }

        layoutLocationsContainer.setVisibility(View.VISIBLE);
        if (tvEmptyLocations != null) tvEmptyLocations.setVisibility(View.GONE);

        for (int i = 0; i < rows.size(); i++) {
            String name = rows.get(i)[0];
            String address = rows.get(i)[1];
            layoutLocationsContainer.addView(buildLocationRow(name, address));

            if (i < rows.size() - 1) {
                View divider = new View(this);
                divider.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, dpToPx(1)));
                divider.setBackgroundColor(ContextCompat.getColor(this, R.color.border_gray));
                layoutLocationsContainer.addView(divider);
            }
        }
    }

    private View buildLocationRow(String name, String address) {
        LinearLayout row = new LinearLayout(this);
        row.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
        row.setClickable(true);
        row.setFocusable(true);
        row.setGravity(Gravity.CENTER_VERTICAL);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setPadding(0, dpToPx(8), 0, dpToPx(8));

        RelativeLayout iconWrap = new RelativeLayout(this);
        iconWrap.setLayoutParams(new LinearLayout.LayoutParams(dpToPx(32), dpToPx(32)));
        iconWrap.setBackgroundResource(R.drawable.bg_logo_circle);

        ImageView pin = new ImageView(this);
        RelativeLayout.LayoutParams pinParams = new RelativeLayout.LayoutParams(dpToPx(16), dpToPx(16));
        pinParams.addRule(RelativeLayout.CENTER_IN_PARENT);
        pin.setLayoutParams(pinParams);
        pin.setImageResource(R.drawable.ic_location);
        iconWrap.addView(pin);
        row.addView(iconWrap);

        LinearLayout textCol = new LinearLayout(this);
        LinearLayout.LayoutParams colParams = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f);
        colParams.setMarginStart(dpToPx(10));
        textCol.setLayoutParams(colParams);
        textCol.setOrientation(LinearLayout.VERTICAL);

        TextView tvName = new TextView(this);
        tvName.setText(name);
        tvName.setTextColor(ContextCompat.getColor(this, R.color.text_dark));
        tvName.setTextSize(14f);
        tvName.setTypeface(null, Typeface.BOLD);
        textCol.addView(tvName);

        TextView tvAddress = new TextView(this);
        tvAddress.setText(address);
        tvAddress.setTextColor(ContextCompat.getColor(this, R.color.text_gray));
        tvAddress.setTextSize(12f);
        textCol.addView(tvAddress);
        row.addView(textCol);

        boolean isSelected = name.equals(selectedName);
        TextView tvCheck = new TextView(this);
        tvCheck.setText(isSelected ? "✓" : "➔");
        tvCheck.setTextColor(isSelected ? ContextCompat.getColor(this, R.color.primary) : Color.parseColor("#888888"));
        tvCheck.setTextSize(isSelected ? 16f : 14f);
        if (isSelected) tvCheck.setTypeface(null, Typeface.BOLD);
        row.addView(tvCheck);

        row.setOnClickListener(v -> {
            selectedName = name;
            selectedAddress = "Sử dụng tên địa điểm đã nhập".equals(address) ? "" : address;
            if (layoutSelectedSection != null) layoutSelectedSection.setVisibility(View.VISIBLE);
            if (tvSelectedLocationName != null) tvSelectedLocationName.setText(selectedName);
            if (tvSelectedLocationAddress != null) {
                tvSelectedLocationAddress.setText(!selectedAddress.isEmpty() ? selectedAddress : "Địa điểm đính kèm bài viết");
            }
            renderLocationResults();
        });

        return row;
    }

    private int dpToPx(int dp) {
        return Math.round(dp * getResources().getDisplayMetrics().density);
    }
}
