package com.example.dishcision;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.dishcision.network.ApiClient;
import com.example.dishcision.network.HelpCreateRequest;
import com.example.dishcision.network.HelpCreateResponse;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SubmitHelpRequestActivity extends AppCompatActivity {

    private ImageView btnBack;
    private Spinner spinnerSubject;
    private EditText edtTitle;
    private EditText edtDesc;
    private EditText edtEmail;
    private View btnAttach;
    private View btnSubmit;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_submit_help_request);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main_submit_help), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        initViews();
        setupSpinner();
        setupListeners();
    }

    private void initViews() {
        btnBack = findViewById(R.id.btnBack);
        spinnerSubject = findViewById(R.id.spinnerSubject);
        edtTitle = findViewById(R.id.edtTitle);
        edtDesc = findViewById(R.id.edtDesc);
        edtEmail = findViewById(R.id.edtEmail);
        btnAttach = findViewById(R.id.btnAttach);
        btnSubmit = findViewById(R.id.btnSubmit);
    }

    private void setupSpinner() {
        if (spinnerSubject == null) return;
        String[] subjects = {"Chọn chủ đề", "Lỗi ứng dụng", "Đóng góp ý kiến", "Báo cáo người dùng", "Vấn đề tài khoản", "Khác"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, subjects);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerSubject.setAdapter(adapter);
    }

    private void setupListeners() {
        if (btnBack != null) btnBack.setOnClickListener(v -> finish());

        if (btnAttach != null) {
            btnAttach.setOnClickListener(v -> Toast.makeText(this, "Chọn file đính kèm", Toast.LENGTH_SHORT).show());
        }

        if (btnSubmit != null) {
            btnSubmit.setOnClickListener(v -> submitRequest());
        }
    }

    private void submitRequest() {
        String subject = spinnerSubject != null ? spinnerSubject.getSelectedItem().toString() : "";
        String title = edtTitle != null ? edtTitle.getText().toString().trim() : "";
        String desc = edtDesc != null ? edtDesc.getText().toString().trim() : "";
        String email = edtEmail != null ? edtEmail.getText().toString().trim() : "";

        if (subject.equals("Chọn chủ đề") || TextUtils.isEmpty(title) || TextUtils.isEmpty(desc) || TextUtils.isEmpty(email)) {
            Toast.makeText(this, "Vui lòng điền các trường bắt buộc", Toast.LENGTH_SHORT).show();
            return;
        }

        HelpCreateRequest req = new HelpCreateRequest(subject, title, desc, email);
        ApiClient.getApiService().createTicket("Bearer dummy_token", req).enqueue(new Callback<HelpCreateResponse>() {
            @Override
            public void onResponse(@NonNull Call<HelpCreateResponse> call, @NonNull Response<HelpCreateResponse> response) {
                Toast.makeText(SubmitHelpRequestActivity.this, "Đã gửi yêu cầu hỗ trợ", Toast.LENGTH_SHORT).show();
                finish();
            }

            @Override
            public void onFailure(@NonNull Call<HelpCreateResponse> call, @NonNull Throwable t) {
                Toast.makeText(SubmitHelpRequestActivity.this, "Đã gửi yêu cầu hỗ trợ", Toast.LENGTH_SHORT).show();
                finish();
            }
        });
    }
}