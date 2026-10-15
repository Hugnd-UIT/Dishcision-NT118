package com.example.dishcision;

import android.content.Intent;
import android.os.Bundle;
import android.text.Html;
import android.text.InputType;
import android.text.TextUtils;
import android.widget.EditText;
import android.widget.ImageView;
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
import com.example.dishcision.network.AuthResponse;
import com.example.dishcision.network.RegisterRequest;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class RegisterActivity extends AppCompatActivity {

    private EditText edtFullName;
    private EditText edtRegisterEmail;
    private EditText edtRegisterPassword;
    private EditText edtConfirmPassword;
    private ImageView btnToggleRegisterPassword;
    private ImageView btnToggleConfirmPassword;
    private RelativeLayout btnRegister;
    private TextView tvLoginLink;

    private boolean isPasswordVisible = false;
    private boolean isConfirmPasswordVisible = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_register);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main_register), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        initViews();
        setupListeners();
        setupTermsText();
    }

    private void initViews() {
        edtFullName = findViewById(R.id.edtFullName);
        edtRegisterEmail = findViewById(R.id.edtRegisterEmail);
        edtRegisterPassword = findViewById(R.id.edtRegisterPassword);
        edtConfirmPassword = findViewById(R.id.edtConfirmPassword);
        btnToggleRegisterPassword = findViewById(R.id.btnToggleRegisterPassword);
        btnToggleConfirmPassword = findViewById(R.id.btnToggleConfirmPassword);
        btnRegister = findViewById(R.id.btnRegister);
        tvLoginLink = findViewById(R.id.tvLoginLink);
    }

    private void setupListeners() {
        if (btnRegister != null) {
            btnRegister.setOnClickListener(v -> performRegister());
        }

        if (btnToggleRegisterPassword != null && edtRegisterPassword != null) {
            btnToggleRegisterPassword.setOnClickListener(v -> {
                if (isPasswordVisible) {
                    edtRegisterPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                    isPasswordVisible = false;
                } else {
                    edtRegisterPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
                    isPasswordVisible = true;
                }
                edtRegisterPassword.setSelection(edtRegisterPassword.getText().length());
            });
        }

        if (btnToggleConfirmPassword != null && edtConfirmPassword != null) {
            btnToggleConfirmPassword.setOnClickListener(v -> {
                if (isConfirmPasswordVisible) {
                    edtConfirmPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                    isConfirmPasswordVisible = false;
                } else {
                    edtConfirmPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
                    isConfirmPasswordVisible = true;
                }
                edtConfirmPassword.setSelection(edtConfirmPassword.getText().length());
            });
        }

        if (tvLoginLink != null) {
            tvLoginLink.setOnClickListener(v -> {
                Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
                startActivity(intent);
                finish();
            });
        }
    }

    private void performRegister() {
        String fullName = getEnteredFullName();
        String email = getEnteredEmail();
        String password = edtRegisterPassword != null ? edtRegisterPassword.getText().toString() : "";
        String confirmPassword = edtConfirmPassword != null ? edtConfirmPassword.getText().toString() : "";

        if (TextUtils.isEmpty(fullName)) {
            edtFullName.setError("Vui lòng nhập họ và tên");
            edtFullName.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(email)) {
            edtRegisterEmail.setError("Vui lòng nhập địa chỉ email");
            edtRegisterEmail.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(password)) {
            edtRegisterPassword.setError("Vui lòng nhập mật khẩu");
            edtRegisterPassword.requestFocus();
            return;
        }

        if (password.length() < 8) {
            edtRegisterPassword.setError("Mật khẩu phải từ 8 ký tự trở lên");
            edtRegisterPassword.requestFocus();
            return;
        }

        if (!password.equals(confirmPassword)) {
            edtConfirmPassword.setError("Mật khẩu xác nhận không khớp");
            edtConfirmPassword.requestFocus();
            return;
        }

        RegisterRequest request = new RegisterRequest(fullName, email, password);
        ApiClient.getApiService().register(request).enqueue(new Callback<AuthResponse>() {
            @Override
            public void onResponse(@NonNull Call<AuthResponse> call, @NonNull Response<AuthResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Toast.makeText(RegisterActivity.this, "Đăng ký thành công!", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
                    startActivity(intent);
                    finish();
                } else {
                    String msg = response.body() != null && response.body().getMessage() != null
                            ? response.body().getMessage()
                            : "Mã lỗi server: " + response.code();
                    Toast.makeText(RegisterActivity.this, "Đăng ký thất bại: " + msg, Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<AuthResponse> call, @NonNull Throwable t) {
                Toast.makeText(RegisterActivity.this, "Lỗi kết nối server: " + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    private void setupTermsText() {
        TextView tvRegisterTerms = findViewById(R.id.tvRegisterTerms);
        if (tvRegisterTerms != null) {
            String htmlText = "Bằng việc đăng ký, bạn đồng ý với <font color='#FF5722'><b>Điều khoản dịch vụ</b></font> &amp; <font color='#FF5722'><b>Chính sách bảo mật</b></font> của Dishcision.";
            tvRegisterTerms.setText(Html.fromHtml(htmlText, Html.FROM_HTML_MODE_LEGACY));
            tvRegisterTerms.setOnClickListener(v -> {
                Intent intent = new Intent(RegisterActivity.this, TermsOfServiceActivity.class);
                startActivity(intent);
            });
        }
    }

    public String getEnteredFullName() {
        return edtFullName != null ? edtFullName.getText().toString().trim() : "";
    }

    public String getEnteredEmail() {
        return edtRegisterEmail != null ? edtRegisterEmail.getText().toString().trim() : "";
    }
}
