package com.example.dishcision;

import android.os.Bundle;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.PopupMenu;
import androidx.appcompat.widget.SwitchCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.dishcision.network.AddressRequest;
import com.example.dishcision.network.AddressResponse;
import com.example.dishcision.network.ApiClient;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AddAddressActivity extends AppCompatActivity {

    private ImageView btnBack;
    private TextView tvHeaderTitle;
    private EditText edtRecipientName;
    private EditText edtRecipientPhone;
    private LinearLayout spinnerCity;
    private TextView tvSelectedCity;
    private LinearLayout spinnerDistrict;
    private TextView tvSelectedDistrict;
    private LinearLayout spinnerWard;
    private TextView tvSelectedWard;
    private EditText edtDetailAddress;
    private EditText edtNotes;

    private TextView tagHome;
    private TextView tagCompany;
    private TextView tagOther;
    private SwitchCompat switchSetDefault;
    private LinearLayout btnSaveAddress;

    private String selectedTag = "Nhà riêng";
    private int editingAddressId = 0;
    private String selectedCity = "";
    private String selectedDistrict = "";
    private String selectedWard = "";
    private boolean isSubmitting = false;
    private int placeholderColor = 0xFF8E7570;

    // shortcut: danh sách mẫu cố định, thay bằng API địa giới hành chính khi có.
    private static final String[] CITIES = {"Hà Nội", "TP. Hồ Chí Minh", "Đà Nẵng"};

    private static String[] districtsOf(String city) {
        switch (city) {
            case "Hà Nội": return new String[]{"Ba Đình", "Cầu Giấy", "Đống Đa"};
            case "TP. Hồ Chí Minh": return new String[]{"Quận 1", "Thủ Đức"};
            case "Đà Nẵng": return new String[]{"Hải Châu", "Sơn Trà"};
            default: return new String[0];
        }
    }

    private static String[] wardsOf(String district) {
        switch (district) {
            case "Ba Đình": return new String[]{"Phường Điện Biên", "Phường Phúc Xá"};
            case "Cầu Giấy": return new String[]{"Phường Dịch Vọng", "Phường Nghĩa Đô"};
            case "Đống Đa": return new String[]{"Phường Cát Linh", "Phường Láng Hạ"};
            case "Quận 1": return new String[]{"Phường Bến Nghé", "Phường Bến Thành"};
            case "Thủ Đức": return new String[]{"Phường Thảo Điền", "Phường An Phú"};
            case "Hải Châu": return new String[]{"Phường Hải Châu I", "Phường Thạch Thang"};
            case "Sơn Trà": return new String[]{"Phường An Hải Bắc", "Phường Mân Thái"};
            default: return new String[0];
        }
    }

    private void resetPlaceholder(TextView tv, String hint) {
        if (tv == null) return;
        tv.setText(hint);
        tv.setTextColor(placeholderColor);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_add_address);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(android.R.id.content), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, Math.max(systemBars.bottom, insets.getInsets(WindowInsetsCompat.Type.ime()).bottom));
            return insets;
        });

        ApiClient.init(this);
        initViews();
        setupListeners();
        checkForPrefill();
    }

    private void initViews() {
        btnBack = findViewById(R.id.btnBack);
        tvHeaderTitle = findViewById(R.id.tvHeaderTitle);
        edtRecipientName = findViewById(R.id.edtRecipientName);
        edtRecipientPhone = findViewById(R.id.edtRecipientPhone);
        spinnerCity = findViewById(R.id.spinnerCity);
        tvSelectedCity = findViewById(R.id.tvSelectedCity);
        if (tvSelectedCity != null) placeholderColor = tvSelectedCity.getCurrentTextColor();
        spinnerDistrict = findViewById(R.id.spinnerDistrict);
        tvSelectedDistrict = findViewById(R.id.tvSelectedDistrict);
        spinnerWard = findViewById(R.id.spinnerWard);
        tvSelectedWard = findViewById(R.id.tvSelectedWard);
        edtDetailAddress = findViewById(R.id.edtDetailAddress);
        edtNotes = findViewById(R.id.edtNotes);

        tagHome = findViewById(R.id.tagHome);
        tagCompany = findViewById(R.id.tagCompany);
        tagOther = findViewById(R.id.tagOther);
        switchSetDefault = findViewById(R.id.switchSetDefault);
        btnSaveAddress = findViewById(R.id.btnSaveAddress);
    }

    private void setupListeners() {
        if (btnBack != null) {
            btnBack.setOnClickListener(v -> finish());
        }

        if (spinnerCity != null) {
            spinnerCity.setOnClickListener(v -> {
                PopupMenu popup = new PopupMenu(this, spinnerCity);
                for (String c : CITIES) popup.getMenu().add(c);
                popup.setOnMenuItemClickListener((MenuItem item) -> {
                    String city = item.getTitle().toString();
                    if (!city.equals(selectedCity)) {
                        selectedDistrict = "";
                        selectedWard = "";
                        resetPlaceholder(tvSelectedDistrict, "Chọn quận, huyện");
                        resetPlaceholder(tvSelectedWard, "Chọn phường, xã");
                    }
                    selectedCity = city;
                    if (tvSelectedCity != null) {
                        tvSelectedCity.setText(selectedCity);
                        tvSelectedCity.setTextColor(0xFF141B2B);
                    }
                    return true;
                });
                popup.show();
            });
        }

        if (spinnerDistrict != null) {
            spinnerDistrict.setOnClickListener(v -> {
                if (selectedCity.isEmpty()) {
                    Toast.makeText(this, "Vui lòng chọn Tỉnh / Thành phố trước", Toast.LENGTH_SHORT).show();
                    return;
                }
                PopupMenu popup = new PopupMenu(this, spinnerDistrict);
                for (String d : districtsOf(selectedCity)) popup.getMenu().add(d);
                popup.setOnMenuItemClickListener((MenuItem item) -> {
                    String district = item.getTitle().toString();
                    if (!district.equals(selectedDistrict)) {
                        selectedWard = "";
                        resetPlaceholder(tvSelectedWard, "Chọn phường, xã");
                    }
                    selectedDistrict = district;
                    if (tvSelectedDistrict != null) {
                        tvSelectedDistrict.setText(selectedDistrict);
                        tvSelectedDistrict.setTextColor(0xFF141B2B);
                    }
                    return true;
                });
                popup.show();
            });
        }

        if (spinnerWard != null) {
            spinnerWard.setOnClickListener(v -> {
                if (selectedDistrict.isEmpty()) {
                    Toast.makeText(this, "Vui lòng chọn Quận / Huyện trước", Toast.LENGTH_SHORT).show();
                    return;
                }
                PopupMenu popup = new PopupMenu(this, spinnerWard);
                for (String w : wardsOf(selectedDistrict)) popup.getMenu().add(w);
                popup.setOnMenuItemClickListener((MenuItem item) -> {
                    selectedWard = item.getTitle().toString();
                    if (tvSelectedWard != null) {
                        tvSelectedWard.setText(selectedWard);
                        tvSelectedWard.setTextColor(0xFF141B2B);
                    }
                    return true;
                });
                popup.show();
            });
        }

        if (tagHome != null) {
            tagHome.setOnClickListener(v -> selectTag("Nhà riêng"));
        }

        if (tagCompany != null) {
            tagCompany.setOnClickListener(v -> selectTag("Công ty"));
        }

        if (tagOther != null) {
            tagOther.setOnClickListener(v -> selectTag("Khác"));
        }

        if (btnSaveAddress != null) {
            btnSaveAddress.setOnClickListener(v -> saveAddressToBackend());
        }
    }

    private void saveAddressToBackend() {
        if (isSubmitting) return;

        String name = edtRecipientName != null ? edtRecipientName.getText().toString().trim() : "";
        String phone = edtRecipientPhone != null ? edtRecipientPhone.getText().toString().trim() : "";
        String detail = edtDetailAddress != null ? edtDetailAddress.getText().toString().trim() : "";
        String notes = edtNotes != null ? edtNotes.getText().toString().trim() : "";
        boolean isDefault = switchSetDefault != null && switchSetDefault.isChecked();

        if (name.isEmpty() || phone.isEmpty() || detail.isEmpty()) {
            Toast.makeText(this, "Vui lòng điền đầy đủ các thông tin bắt buộc (*)", Toast.LENGTH_SHORT).show();
            return;
        }
        // Khi sửa địa chỉ cũ, tỉnh/quận/phường nằm sẵn trong phần chi tiết nên không bắt chọn lại.
        if (editingAddressId == 0 && (selectedCity.isEmpty() || selectedDistrict.isEmpty() || selectedWard.isEmpty())) {
            Toast.makeText(this, "Vui lòng chọn Tỉnh/Thành phố, Quận/Huyện và Phường/Xã", Toast.LENGTH_SHORT).show();
            return;
        }
        if (!phone.replaceAll("[\\s.-]", "").matches("^(0|\\+84)\\d{9}$")) {
            Toast.makeText(this, "Số điện thoại không hợp lệ", Toast.LENGTH_SHORT).show();
            return;
        }

        StringBuilder fullDetail = new StringBuilder(detail);
        if (!selectedWard.isEmpty() && !detail.contains(selectedWard)) {
            fullDetail.append(", ").append(selectedWard);
        }
        if (!selectedDistrict.isEmpty() && !detail.contains(selectedDistrict)) {
            fullDetail.append(", ").append(selectedDistrict);
        }
        if (!selectedCity.isEmpty() && !detail.contains(selectedCity)) {
            fullDetail.append(", ").append(selectedCity);
        }

        String authHeader = ApiClient.getAuthHeader(this);
        if (authHeader.isEmpty()) {
            Toast.makeText(this, "Vui lòng đăng nhập lại", Toast.LENGTH_SHORT).show();
            return;
        }

        isSubmitting = true;
        btnSaveAddress.setEnabled(false);

        AddressRequest req = new AddressRequest(
                selectedTag,
                name,
                phone,
                fullDetail.toString(),
                notes.isEmpty() ? null : notes,
                isDefault
        );

        Call<AddressResponse> call = editingAddressId > 0
                ? ApiClient.getApiService().updateAddress(authHeader, editingAddressId, req)
                : ApiClient.getApiService().addAddress(authHeader, req);

        call.enqueue(new Callback<AddressResponse>() {
            @Override
            public void onResponse(@NonNull Call<AddressResponse> call, @NonNull Response<AddressResponse> response) {
                isSubmitting = false;
                btnSaveAddress.setEnabled(true);
                if (response.isSuccessful()) {
                    Toast.makeText(AddAddressActivity.this,
                            editingAddressId > 0 ? "Đã cập nhật địa chỉ" : "Đã lưu địa chỉ " + selectedTag + " thành công!",
                            Toast.LENGTH_SHORT).show();
                    finish();
                } else {
                    Toast.makeText(AddAddressActivity.this, "Không thể lưu địa chỉ (mã " + response.code() + ")", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<AddressResponse> call, @NonNull Throwable t) {
                isSubmitting = false;
                btnSaveAddress.setEnabled(true);
                Toast.makeText(AddAddressActivity.this, "Lỗi kết nối máy chủ", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void selectTag(String tag) {
        selectedTag = tag;
        updateTagStyle(tagHome, "Nhà riêng".equals(tag), "Nhà riêng");
        updateTagStyle(tagCompany, "Công ty".equals(tag), "Công ty");
        updateTagStyle(tagOther, "Khác".equals(tag), "Khác");
    }

    private void updateTagStyle(TextView tagView, boolean isSelected, String label) {
        if (tagView == null) return;
        if (isSelected) {
            tagView.setBackgroundResource(R.drawable.bg_chip_selected_orange);
            tagView.setTextColor(0xFFFF5A36);
            tagView.setText("✔ " + label);
        } else {
            tagView.setBackgroundResource(R.drawable.bg_chip_preference);
            tagView.setTextColor(0xFF141B2B);
            tagView.setText(label);
        }
    }

    private void checkForPrefill() {
        editingAddressId = getIntent().getIntExtra("ADDRESS_ID", 0);
        if (editingAddressId > 0 && tvHeaderTitle != null) {
            tvHeaderTitle.setText("Chỉnh sửa địa chỉ");
        }

        String label = getIntent().getStringExtra("ADDRESS_LABEL");
        if (label != null && !label.isEmpty()) {
            selectTag(label);
        }

        String name = getIntent().getStringExtra("ADDRESS_NAME");
        if (name != null && edtRecipientName != null) {
            edtRecipientName.setText(name);
        }

        String phone = getIntent().getStringExtra("ADDRESS_PHONE");
        if (phone != null && edtRecipientPhone != null) {
            edtRecipientPhone.setText(phone);
        }

        String detail = getIntent().getStringExtra("ADDRESS_DETAIL");
        if (detail != null && edtDetailAddress != null) {
            edtDetailAddress.setText(detail);
        }

        String notes = getIntent().getStringExtra("ADDRESS_NOTES");
        if (notes != null && edtNotes != null) {
            edtNotes.setText(notes);
        }

        if (getIntent().hasExtra("ADDRESS_IS_DEFAULT") && switchSetDefault != null) {
            switchSetDefault.setChecked(getIntent().getBooleanExtra("ADDRESS_IS_DEFAULT", false));
        }
    }
}
