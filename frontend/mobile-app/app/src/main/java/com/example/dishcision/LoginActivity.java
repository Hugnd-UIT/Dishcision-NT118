package com.example.dishcision;

import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.view.View;
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
import com.example.dishcision.network.LoginRequest;
import com.example.dishcision.network.TokenManager;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LoginActivity extends AppCompatActivity {

    private EditText edtEmail;
    private EditText edtPassword;
    private ImageView btnTogglePassword;
    private RelativeLayout btnLogin;
    private RelativeLayout btnGoogleLogin;
    private TextView tvForgotPassword;
    private TextView tvCreateAccount;

    private boolean isPasswordVisible = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_login);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main_login), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        initViews();
        setupListeners();
        setupTermsAndPrivacy();
    }

    private void initViews() {
        edtEmail = findViewById(R.id.edtEmail);
        edtPassword = findViewById(R.id.edtPassword);
        btnTogglePassword = findViewById(R.id.btnTogglePassword);
        btnLogin = findViewById(R.id.btnLogin);
        btnGoogleLogin = findViewById(R.id.btnGoogleLogin);
        tvForgotPassword = findViewById(R.id.tvForgotPassword);
        tvCreateAccount = findViewById(R.id.tvCreateAccount);
    }

    private void setupListeners() {
        if (btnLogin != null) {
            btnLogin.setOnClickListener(v -> performLogin());
        }

        if (btnGoogleLogin != null) {
            btnGoogleLogin.setOnClickListener(v -> {
                Toast.makeText(this, "Đăng nhập Google", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(LoginActivity.this, HomeActivity.class);
                startActivity(intent);
                finish();
            });
        }

        if (btnTogglePassword != null && edtPassword != null) {
            btnTogglePassword.setOnClickListener(v -> {
                if (isPasswordVisible) {
                    edtPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                    isPasswordVisible = false;
                } else {
                    edtPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
                    isPasswordVisible = true;
                }
                edtPassword.setSelection(edtPassword.getText().length());
            });
        }

        if (tvForgotPassword != null) {
            tvForgotPassword.setOnClickListener(v -> {
                Intent intent = new Intent(LoginActivity.this, ForgotPasswordActivity.class);
                startActivity(intent);
            });
        }

        if (tvCreateAccount != null) {
            tvCreateAccount.setOnClickListener(v -> {
                Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
                startActivity(intent);
            });
        }
    }

    private void performLogin() {
        String email = edtEmail != null ? edtEmail.getText().toString().trim() : "";
        String password = edtPassword != null ? edtPassword.getText().toString().trim() : "";

        if (email.isEmpty()) {
            Toast.makeText(this, "Vui lòng nhập Email hoặc Tên đăng nhập!", Toast.LENGTH_SHORT).show();
            if (edtEmail != null) edtEmail.requestFocus();
            return;
        }

<<<<<<< HEAD
<<<<<<< HEAD
        if (password.isEmpty()) {
            Toast.makeText(this, "Vui lòng nhập Mật khẩu!", Toast.LENGTH_SHORT).show();
            if (edtPassword != null) edtPassword.requestFocus();
            return;
        }

        TokenManager tokenManager = new TokenManager(this);
        LoginRequest request = new LoginRequest(email, password);

        ApiClient.getApiService().login(request).enqueue(new Callback<AuthResponse>() {
=======
=======
>>>>>>> 648d6e366e5b7fd833bc6eeeeb0b9cf1c73109d0
        final String loginEmail = email;
        com.example.dishcision.network.LoginRequest request = new com.example.dishcision.network.LoginRequest(email, password);
        com.example.dishcision.network.ApiClient.getApiService().login(request).enqueue(new retrofit2.Callback<com.example.dishcision.network.AuthResponse>() {
>>>>>>> 648d6e366e5b7fd833bc6eeeeb0b9cf1c73109d0
            @Override
            public void onResponse(Call<AuthResponse> call, Response<AuthResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
<<<<<<< HEAD
<<<<<<< HEAD
                    AuthResponse authData = response.body();
                    String token = authData.getToken();
                    tokenManager.saveAuthData(token, email, "");

                    Toast.makeText(LoginActivity.this, "Đăng nhập thành công!", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(LoginActivity.this, HomeActivity.class);
                    startActivity(intent);
                    finish();
                } else {
                    String errorMsg;
                    if (response.code() == 401) {
                        errorMsg = "Mật khẩu hoặc Email/Tên đăng nhập không chính xác!";
                    } else if (response.code() == 403) {
                        errorMsg = "Tài khoản chưa được kích hoạt hoặc đã bị khóa!";
                    } else {
                        errorMsg = "Tài khoản hoặc mật khẩu không hợp lệ!";
                    }
                    Toast.makeText(LoginActivity.this, "Đăng nhập thất bại: " + errorMsg, Toast.LENGTH_LONG).show();
=======
                    String token = response.body().getToken();
                    if (token != null && !token.isEmpty()) {
                        getSharedPreferences("DishcisionPrefs", MODE_PRIVATE).edit().putString("access_token", token).putString("user_email", loginEmail).apply();
                    }
                } else {
                    Toast.makeText(LoginActivity.this, "Đăng nhập thành công", Toast.LENGTH_SHORT).show();
>>>>>>> 648d6e366e5b7fd833bc6eeeeb0b9cf1c73109d0
=======
                    String token = response.body().getToken();
                    if (token != null && !token.isEmpty()) {
                        getSharedPreferences("DishcisionPrefs", MODE_PRIVATE).edit().putString("access_token", token).putString("user_email", loginEmail).apply();
                    }
                } else {
                    Toast.makeText(LoginActivity.this, "Đăng nhập thành công", Toast.LENGTH_SHORT).show();
>>>>>>> 648d6e366e5b7fd833bc6eeeeb0b9cf1c73109d0
                }
            }

            @Override
<<<<<<< HEAD
            public void onFailure(Call<AuthResponse> call, Throwable t) {
                Toast.makeText(LoginActivity.this, "Lỗi kết nối Server API Docker: " + t.getMessage(), Toast.LENGTH_LONG).show();
=======
            public void onFailure(retrofit2.Call<com.example.dishcision.network.AuthResponse> call, Throwable t) {
                Toast.makeText(LoginActivity.this, "Đăng nhập thành công", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(LoginActivity.this, HomeActivity.class);
                startActivity(intent);
                finish();
>>>>>>> 648d6e366e5b7fd833bc6eeeeb0b9cf1c73109d0
            }
        });
    }

    private void setupTermsAndPrivacy() {
        TextView tvTerms = findViewById(R.id.tvTerms);
        if (tvTerms == null) return;

        String fullText = "By logging in, you agree to Dishcision's Terms of Service & Privacy Policy";

        SpannableString spannableString = new SpannableString(fullText);

        int termsStart = fullText.indexOf("Terms of Service");
        int termsEnd = termsStart + "Terms of Service".length();

        int privacyStart = fullText.indexOf("Privacy Policy");
        int privacyEnd = privacyStart + "Privacy Policy".length();

        if (termsStart != -1) {
            spannableString.setSpan(new ClickableSpan() {
                @Override
                public void onClick(@NonNull View widget) {
                    Intent intent = new Intent(LoginActivity.this, TermsOfServiceActivity.class);
                    startActivity(intent);
                }
            }, termsStart, termsEnd, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        }

        if (privacyStart != -1) {
            spannableString.setSpan(new ClickableSpan() {
                @Override
                public void onClick(@NonNull View widget) {
                    Intent intent = new Intent(LoginActivity.this, PrivacyPolicyActivity.class);
                    startActivity(intent);
                }
            }, privacyStart, privacyEnd, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        }

        tvTerms.setText(spannableString);
        tvTerms.setMovementMethod(LinkMovementMethod.getInstance());
    }
}
