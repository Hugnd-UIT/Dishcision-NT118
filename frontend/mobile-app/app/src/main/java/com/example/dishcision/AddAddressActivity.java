package com.example.dishcision;

import android.os.Bundle;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class AddAddressActivity extends AppCompatActivity {

    private ImageView btnBack;
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
    private LinearLayout btnSaveAddress;

    private String selectedTag = "Nhà riêng";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_add_address);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(android.R.id.content), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        initViews();
        setupListeners();
    }

    private void initViews() {
        btnBack = findViewById(R.id.btnBack);
        edtRecipientName = findViewById(R.id.edtRecipientName);
        edtRecipientPhone = findViewById(R.id.edtRecipientPhone);
        spinnerCity = findViewById(R.id.spinnerCity);
        tvSelectedCity = findViewById(R.id.tvSelectedCity);
        spinnerDistrict = findViewById(R.id.spinnerDistrict);
        tvSelectedDistrict = findViewById(R.id.tvSelectedDistrict);
        spinnerWard = findViewById(R.id.spinnerWard);
        tvSelectedWard = findViewById(R.id.tvSelectedWard);
        edtDetailAddress = findViewById(R.id.edtDetailAddress);
        edtNotes = findViewById(R.id.edtNotes);

        tagHome = findViewById(R.id.tagHome);
        tagCompany = findViewById(R.id.tagCompany);
        tagOther = findViewById(R.id.tagOther);
        btnSaveAddress = findViewById(R.id.btnSaveAddress);
    }

    private void setupListeners() {
        if (btnBack != null) {
            btnBack.setOnClickListener(v -> finish());
        }

        if (spinnerCity != null) {
            spinnerCity.setOnClickListener(v -> {
                if (tvSelectedCity != null) tvSelectedCity.setText("Hà Nội");
                Toast.makeText(this, "Đã chọn: Hà Nội", Toast.LENGTH_SHORT).show();
            });
        }

        if (spinnerDistrict != null) {
            spinnerDistrict.setOnClickListener(v -> {
                if (tvSelectedDistrict != null) tvSelectedDistrict.setText("Ba Đình");
                Toast.makeText(this, "Đã chọn: Ba Đình", Toast.LENGTH_SHORT).show();
            });
        }

        if (spinnerWard != null) {
            spinnerWard.setOnClickListener(v -> {
                if (tvSelectedWard != null) tvSelectedWard.setText("Phường Điện Biên");
                Toast.makeText(this, "Đã chọn: Phường Điện Biên", Toast.LENGTH_SHORT).show();
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
            btnSaveAddress.setOnClickListener(v -> {
                String name = edtRecipientName != null ? edtRecipientName.getText().toString().trim() : "";
                String phone = edtRecipientPhone != null ? edtRecipientPhone.getText().toString().trim() : "";
                String detail = edtDetailAddress != null ? edtDetailAddress.getText().toString().trim() : "";

                if (name.isEmpty() || phone.isEmpty() || detail.isEmpty()) {
                    Toast.makeText(this, "Vui lòng điền đầy đủ các thông tin bắt buộc (*)", Toast.LENGTH_SHORT).show();
                    return;
                }

                Toast.makeText(this, "Đã lưu địa chỉ " + selectedTag + " thành công!", Toast.LENGTH_SHORT).show();
                finish();
            });
        }
    }

    private void selectTag(String tag) {
        selectedTag = tag;
        Toast.makeText(this, "Nhãn địa chỉ: " + tag, Toast.LENGTH_SHORT).show();
    }
}
