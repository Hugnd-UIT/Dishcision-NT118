package com.example.dishcision;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.dishcision.network.AddressModel;
import com.example.dishcision.network.AddressResponse;
import com.example.dishcision.network.AddressesResponse;
import com.example.dishcision.network.ApiClient;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class DeliveryAddressesActivity extends AppCompatActivity {

    private ImageView btnBack;
    private LinearLayout layoutAddressesContainer;
    private TextView tvEmptyAddresses;
    private LinearLayout btnAddAddress;

    private final List<AddressModel> addressList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_delivery_addresses);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(android.R.id.content), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        ApiClient.init(this);
        initViews();
        setupListeners();
    }

    @Override
    protected void onResume() {
        super.onResume();
        fetchAddressesFromBackend();
    }

    private void initViews() {
        btnBack = findViewById(R.id.btnBack);
        layoutAddressesContainer = findViewById(R.id.layoutAddressesContainer);
        tvEmptyAddresses = findViewById(R.id.tvEmptyAddresses);
        btnAddAddress = findViewById(R.id.btnAddAddress);
    }

    private void setupListeners() {
        if (btnBack != null) {
            btnBack.setOnClickListener(v -> finish());
        }

        if (btnAddAddress != null) {
            btnAddAddress.setOnClickListener(v -> {
                Intent intent = new Intent(DeliveryAddressesActivity.this, AddAddressActivity.class);
                startActivity(intent);
            });
        }
    }

    private void fetchAddressesFromBackend() {
        String authHeader = ApiClient.getAuthHeader(this);
        if (authHeader.isEmpty()) {
            addressList.clear();
            renderAddresses();
            return;
        }

        ApiClient.getApiService().getAddresses(authHeader).enqueue(new Callback<AddressesResponse>() {
            @Override
            public void onResponse(@NonNull Call<AddressesResponse> call, @NonNull Response<AddressesResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    addressList.clear();
                    if (response.body().data != null) {
                        addressList.addAll(response.body().data);
                    }
                    renderAddresses();
                } else {
                    Toast.makeText(DeliveryAddressesActivity.this, "Không thể tải danh sách địa chỉ", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<AddressesResponse> call, @NonNull Throwable t) {
                Toast.makeText(DeliveryAddressesActivity.this, "Lỗi kết nối máy chủ", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void renderAddresses() {
        if (layoutAddressesContainer == null) return;
        layoutAddressesContainer.removeAllViews();

        if (addressList.isEmpty()) {
            if (tvEmptyAddresses != null) {
                tvEmptyAddresses.setVisibility(View.VISIBLE);
            }
            return;
        }

        if (tvEmptyAddresses != null) {
            tvEmptyAddresses.setVisibility(View.GONE);
        }

        for (int i = 0; i < addressList.size(); i++) {
            AddressModel item = addressList.get(i);
            layoutAddressesContainer.addView(createAddressCard(item, i > 0));
        }
    }

    private View createAddressCard(AddressModel item, boolean addTopMargin) {
        LinearLayout card = new LinearLayout(this);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setBackgroundResource(R.drawable.bg_card_white);
        int pad = dpToPx(16);
        card.setPadding(pad, pad, pad, pad);
        LinearLayout.LayoutParams cardParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        if (addTopMargin) {
            cardParams.topMargin = dpToPx(16);
        }
        card.setLayoutParams(cardParams);

        LinearLayout headerRow = new LinearLayout(this);
        headerRow.setOrientation(LinearLayout.HORIZONTAL);
        headerRow.setGravity(Gravity.CENTER_VERTICAL);

        ImageView icon = new ImageView(this);
        icon.setLayoutParams(new LinearLayout.LayoutParams(dpToPx(20), dpToPx(20)));
        String labelText = item.label != null && !item.label.isEmpty() ? item.label : "Nhà riêng";
        if ("Công ty".equalsIgnoreCase(labelText)) {
            icon.setImageResource(R.drawable.ic_store);
        } else if ("Khác".equalsIgnoreCase(labelText)) {
            icon.setImageResource(R.drawable.ic_location);
        } else {
            icon.setImageResource(R.drawable.ic_home);
        }
        icon.setColorFilter(Color.parseColor("#5B403A"));

        TextView tvLabel = new TextView(this);
        LinearLayout.LayoutParams labelParams = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f);
        labelParams.setMarginStart(dpToPx(8));
        tvLabel.setLayoutParams(labelParams);
        tvLabel.setText(labelText);
        tvLabel.setTextColor(Color.parseColor("#141B2B"));
        tvLabel.setTextSize(16f);
        tvLabel.setTypeface(null, Typeface.BOLD);

        headerRow.addView(icon);
        headerRow.addView(tvLabel);

        if (item.isDefault) {
            TextView badgeDefault = new TextView(this);
            badgeDefault.setBackgroundResource(R.drawable.bg_badge_expert);
            badgeDefault.setPadding(dpToPx(10), dpToPx(4), dpToPx(10), dpToPx(4));
            badgeDefault.setText("Mặc định");
            badgeDefault.setTextColor(Color.parseColor("#FF5A36"));
            badgeDefault.setTextSize(11f);
            badgeDefault.setTypeface(null, Typeface.BOLD);
            headerRow.addView(badgeDefault);
        }

        card.addView(headerRow);

        TextView tvDetail = new TextView(this);
        LinearLayout.LayoutParams detailParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        detailParams.topMargin = dpToPx(10);
        tvDetail.setLayoutParams(detailParams);
        tvDetail.setText(item.detailAddress != null ? item.detailAddress : "");
        tvDetail.setTextColor(Color.parseColor("#141B2B"));
        tvDetail.setTextSize(15f);
        tvDetail.setTypeface(null, Typeface.BOLD);
        card.addView(tvDetail);

        TextView tvRecipient = new TextView(this);
        LinearLayout.LayoutParams recParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        recParams.topMargin = dpToPx(4);
        tvRecipient.setLayoutParams(recParams);
        String name = item.recipientName != null ? item.recipientName : "";
        String phone = item.phoneNumber != null ? item.phoneNumber : "";
        tvRecipient.setText(name + (!name.isEmpty() && !phone.isEmpty() ? " · " : "") + phone);
        tvRecipient.setTextColor(Color.parseColor("#8E7570"));
        tvRecipient.setTextSize(13f);
        card.addView(tvRecipient);

        if (item.notes != null && !item.notes.trim().isEmpty()) {
            TextView tvNotes = new TextView(this);
            LinearLayout.LayoutParams noteParams = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            noteParams.topMargin = dpToPx(2);
            tvNotes.setLayoutParams(noteParams);
            tvNotes.setText("Ghi chú: " + item.notes.trim());
            tvNotes.setTextColor(Color.parseColor("#6B7280"));
            tvNotes.setTextSize(12f);
            card.addView(tvNotes);
        }

        View divider = new View(this);
        LinearLayout.LayoutParams divParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, dpToPx(1));
        divParams.topMargin = dpToPx(12);
        divParams.bottomMargin = dpToPx(12);
        divider.setLayoutParams(divParams);
        divider.setBackgroundColor(Color.parseColor("#E9EBF4"));
        card.addView(divider);

        LinearLayout actionsRow = new LinearLayout(this);
        actionsRow.setOrientation(LinearLayout.HORIZONTAL);

        LinearLayout btnEdit = createActionButton("Chỉnh sửa", R.drawable.ic_pencil);
        btnEdit.setOnClickListener(v -> {
            Intent intent = new Intent(DeliveryAddressesActivity.this, AddAddressActivity.class);
            intent.putExtra("ADDRESS_ID", item.id);
            intent.putExtra("ADDRESS_LABEL", item.label);
            intent.putExtra("ADDRESS_NAME", item.recipientName);
            intent.putExtra("ADDRESS_PHONE", item.phoneNumber);
            intent.putExtra("ADDRESS_DETAIL", item.detailAddress);
            intent.putExtra("ADDRESS_NOTES", item.notes);
            intent.putExtra("ADDRESS_IS_DEFAULT", item.isDefault);
            startActivity(intent);
        });
        actionsRow.addView(btnEdit);

        if (!item.isDefault) {
            LinearLayout btnSetDefault = createActionButton("Đặt mặc định", 0);
            LinearLayout.LayoutParams defParams = (LinearLayout.LayoutParams) btnSetDefault.getLayoutParams();
            defParams.setMarginStart(dpToPx(8));
            btnSetDefault.setLayoutParams(defParams);
            btnSetDefault.setOnClickListener(v -> setDefaultOnBackend(item.id));
            actionsRow.addView(btnSetDefault);
        }

        LinearLayout btnDelete = createActionButton("Xóa", 0);
        LinearLayout.LayoutParams delParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT, dpToPx(40));
        delParams.setMarginStart(dpToPx(8));
        btnDelete.setLayoutParams(delParams);
        btnDelete.setPadding(dpToPx(14), 0, dpToPx(14), 0);
        TextView tvDel = (TextView) btnDelete.getChildAt(0);
        tvDel.setTextColor(Color.parseColor("#C5221F"));
        btnDelete.setOnClickListener(v -> confirmDeleteAddress(item));
        actionsRow.addView(btnDelete);

        card.addView(actionsRow);
        return card;
    }

    private LinearLayout createActionButton(String text, int iconResId) {
        LinearLayout btn = new LinearLayout(this);
        btn.setLayoutParams(new LinearLayout.LayoutParams(0, dpToPx(40), 1f));
        btn.setBackgroundResource(R.drawable.bg_button_light_blue);
        btn.setClickable(true);
        btn.setFocusable(true);
        btn.setGravity(Gravity.CENTER);
        btn.setOrientation(LinearLayout.HORIZONTAL);

        if (iconResId != 0) {
            ImageView img = new ImageView(this);
            img.setLayoutParams(new LinearLayout.LayoutParams(dpToPx(16), dpToPx(16)));
            img.setImageResource(iconResId);
            img.setColorFilter(Color.parseColor("#5B403A"));
            btn.addView(img);
        }

        TextView tv = new TextView(this);
        LinearLayout.LayoutParams tvParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        if (iconResId != 0) {
            tvParams.setMarginStart(dpToPx(6));
        }
        tv.setLayoutParams(tvParams);
        tv.setText(text);
        tv.setTextColor(Color.parseColor("#5B403A"));
        tv.setTextSize(13f);
        tv.setTypeface(null, Typeface.BOLD);
        btn.addView(tv);
        return btn;
    }

    private void setDefaultOnBackend(int addressId) {
        String authHeader = ApiClient.getAuthHeader(this);
        if (authHeader.isEmpty()) return;

        ApiClient.getApiService().setDefaultAddress(authHeader, addressId).enqueue(new Callback<AddressResponse>() {
            @Override
            public void onResponse(@NonNull Call<AddressResponse> call, @NonNull Response<AddressResponse> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(DeliveryAddressesActivity.this, "Đã đặt làm địa chỉ mặc định", Toast.LENGTH_SHORT).show();
                    fetchAddressesFromBackend();
                } else {
                    Toast.makeText(DeliveryAddressesActivity.this, "Không thể cập nhật địa chỉ mặc định", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<AddressResponse> call, @NonNull Throwable t) {
                Toast.makeText(DeliveryAddressesActivity.this, "Lỗi kết nối máy chủ", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void confirmDeleteAddress(AddressModel item) {
        new AlertDialog.Builder(this)
                .setTitle("Xóa địa chỉ")
                .setMessage("Bạn có chắc chắn muốn xóa địa chỉ \"" + (item.label != null ? item.label : "") + "\" không?")
                .setNegativeButton("Hủy", null)
                .setPositiveButton("Xóa", (dialog, which) -> deleteOnBackend(item.id))
                .show();
    }

    private void deleteOnBackend(int addressId) {
        String authHeader = ApiClient.getAuthHeader(this);
        if (authHeader.isEmpty()) return;

        ApiClient.getApiService().deleteAddress(authHeader, addressId).enqueue(new Callback<Object>() {
            @Override
            public void onResponse(@NonNull Call<Object> call, @NonNull Response<Object> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(DeliveryAddressesActivity.this, "Đã xóa địa chỉ", Toast.LENGTH_SHORT).show();
                    fetchAddressesFromBackend();
                } else {
                    Toast.makeText(DeliveryAddressesActivity.this, "Không thể xóa địa chỉ", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<Object> call, @NonNull Throwable t) {
                Toast.makeText(DeliveryAddressesActivity.this, "Lỗi kết nối máy chủ", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private int dpToPx(int dp) {
        return Math.round(dp * getResources().getDisplayMetrics().density);
    }
}
