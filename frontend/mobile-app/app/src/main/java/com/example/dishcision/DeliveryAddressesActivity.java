package com.example.dishcision;

import android.os.Bundle;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class DeliveryAddressesActivity extends AppCompatActivity {

    private ImageView btnBack;
    private TextView badgeDefault1;
    private TextView badgeDefault2;
    private LinearLayout btnEditAddress1;
    private LinearLayout btnEditAddress2;
    private LinearLayout btnSetDefaultAddress2;
    private LinearLayout btnAddAddress;

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

        initViews();
        setupListeners();
    }

    private void initViews() {
        btnBack = findViewById(R.id.btnBack);
        badgeDefault1 = findViewById(R.id.badgeDefault1);
        badgeDefault2 = findViewById(R.id.badgeDefault2);
        btnEditAddress1 = findViewById(R.id.btnEditAddress1);
        btnEditAddress2 = findViewById(R.id.btnEditAddress2);
        btnSetDefaultAddress2 = findViewById(R.id.btnSetDefaultAddress2);
        btnAddAddress = findViewById(R.id.btnAddAddress);
    }

    private void setupListeners() {
        if (btnBack != null) {
            btnBack.setOnClickListener(v -> finish());
        }

        if (btnSetDefaultAddress2 != null) {
            btnSetDefaultAddress2.setOnClickListener(v -> {
                if (badgeDefault1 != null) badgeDefault1.setVisibility(android.view.View.GONE);
                if (badgeDefault2 != null) badgeDefault2.setVisibility(android.view.View.VISIBLE);
                Toast.makeText(this, "Đã đặt Công ty làm địa chỉ mặc định", Toast.LENGTH_SHORT).show();
            });
        }

        if (btnEditAddress1 != null || btnEditAddress2 != null || btnAddAddress != null) {
            android.view.View.OnClickListener openAddAddress = v -> {
                android.content.Intent intent = new android.content.Intent(DeliveryAddressesActivity.this, AddAddressActivity.class);
                startActivity(intent);
            };
            if (btnEditAddress1 != null) btnEditAddress1.setOnClickListener(openAddAddress);
            if (btnEditAddress2 != null) btnEditAddress2.setOnClickListener(openAddAddress);
            if (btnAddAddress != null) btnAddAddress.setOnClickListener(openAddAddress);
        }
    }
}
