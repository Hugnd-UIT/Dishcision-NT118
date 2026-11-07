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
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.dishcision.network.ApiClient;
import com.example.dishcision.network.AuthResponse;
import com.example.dishcision.network.RegisterRequest;
import com.example.dishcision.network.TokenManager;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;

import org.json.JSONObject;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class RegisterActivity extends AppCompatActivity {

    private EditText edtFullName;
    private EditText edtRegisterEmail;
    private EditText edtRegisterPassword;
    private EditText edtConfirmPassword;
    private ImageView btnToggleRegisterPassword;
    private RelativeLayout btnRegister;
    private RelativeLayout btnGoogleRegister;
    private TextView tvLoginLink;

    private boolean isRegisterPasswordVisible = false;
    private GoogleSignInClient mGoogleSignInClient;
    private ActivityResultLauncher<Intent> googleSignInLauncher;

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
        setupGoogleSignIn();
        setupListeners();
        setupRegisterTermsAndPrivacy();
    }

    private void setupGoogleSignIn() {
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .requestProfile()
                .build();

        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        googleSignInLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(result.getData());
                        try {
                            GoogleSignInAccount account = task.getResult(ApiException.class);
                            if (account != null) {
                                String email = account.getEmail();
                                String fullName = account.getDisplayName();
                                String photoUrl = account.getPhotoUrl() != null ? account.getPhotoUrl().toString() : "";

                                // Save locally
                                getSharedPreferences("DishcisionPrefs", MODE_PRIVATE).edit()
                                        .putString("user_email", email)
                                        .putString("user_name", fullName)
                                        .putString("avatar_url", photoUrl)
                                        .apply();

                                new TokenManager(RegisterActivity.this).saveAuthData("google_token_" + System.currentTimeMillis(), email, fullName);

                                Toast.makeText(this, "Đăng ký thành công với Google: " + fullName, Toast.LENGTH_SHORT).show();
                                Intent intent = new Intent(RegisterActivity.this, HomeActivity.class);
                                startActivity(intent);
                                finish();
                            }
                        } catch (ApiException e) {
                            Toast.makeText(this, "Đăng ký Google thất bại (Mã: " + e.getStatusCode() + ")", Toast.LENGTH_SHORT).show();
                        }
                    }
                }
        );
    }

    private void initViews() {
        edtFullName = findViewById(R.id.edtFullName);
        edtRegisterEmail = findViewById(R.id.edtRegisterEmail);
        edtRegisterPassword = findViewById(R.id.edtRegisterPassword);
        edtConfirmPassword = findViewById(R.id.edtConfirmPassword);
        btnToggleRegisterPassword = findViewById(R.id.btnToggleRegisterPassword);
        btnRegister = findViewById(R.id.btnRegister);
        btnGoogleRegister = findViewById(R.id.btnGoogleRegister);
        tvLoginLink = findViewById(R.id.tvLoginLink);
    }

    private void setupListeners() {
        if (btnRegister != null) {
            btnRegister.setOnClickListener(v -> performRegister());
        }

        if (btnGoogleRegister != null) {
            btnGoogleRegister.setOnClickListener(v -> {
                if (mGoogleSignInClient != null && googleSignInLauncher != null) {
                    Intent signInIntent = mGoogleSignInClient.getSignInIntent();
                    googleSignInLauncher.launch(signInIntent);
                }
            });
        }

        if (btnToggleRegisterPassword != null && edtRegisterPassword != null) {
            btnToggleRegisterPassword.setOnClickListener(v -> {
                if (isRegisterPasswordVisible) {
                    edtRegisterPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                    isRegisterPasswordVisible = false;
                } else {
                    edtRegisterPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
                    isRegisterPasswordVisible = true;
                }
                edtRegisterPassword.setSelection(edtRegisterPassword.getText().length());
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
        String fullName = edtFullName != null ? edtFullName.getText().toString().trim() : "";
        String email = edtRegisterEmail != null ? edtRegisterEmail.getText().toString().trim() : "";
        String password = edtRegisterPassword != null ? edtRegisterPassword.getText().toString().trim() : "";
        String confirmPassword = edtConfirmPassword != null ? edtConfirmPassword.getText().toString().trim() : "";

        if (fullName.isEmpty()) {
            Toast.makeText(this, "Vui lòng nhập Họ và tên!", Toast.LENGTH_SHORT).show();
            if (edtFullName != null) edtFullName.requestFocus();
            return;
        }

        if (email.isEmpty()) {
            Toast.makeText(this, "Vui lòng nhập Email!", Toast.LENGTH_SHORT).show();
            if (edtRegisterEmail != null) edtRegisterEmail.requestFocus();
            return;
        }

        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            Toast.makeText(this, "Định dạng Email không hợp lệ!", Toast.LENGTH_SHORT).show();
            if (edtRegisterEmail != null) edtRegisterEmail.requestFocus();
            return;
        }

        if (password.isEmpty() || password.length() < 6) {
            Toast.makeText(this, "Mật khẩu phải từ 6 ký tự trở lên!", Toast.LENGTH_SHORT).show();
            if (edtRegisterPassword != null) edtRegisterPassword.requestFocus();
            return;
        }

        if (!password.equals(confirmPassword)) {
            Toast.makeText(this, "Mật khẩu xác nhận không trùng khớp!", Toast.LENGTH_SHORT).show();
            if (edtConfirmPassword != null) edtConfirmPassword.requestFocus();
            return;
        }

        Toast.makeText(this, "⏳ Đang xử lý đăng ký tài khoản...", Toast.LENGTH_SHORT).show();
        if (btnRegister != null) btnRegister.setEnabled(false);

        TokenManager tokenManager = new TokenManager(this);
        RegisterRequest request = new RegisterRequest(fullName, email, password);

        ApiClient.getApiService().register(request).enqueue(new Callback<AuthResponse>() {
            @Override
            public void onResponse(Call<AuthResponse> call, Response<AuthResponse> response) {
                if (btnRegister != null) btnRegister.setEnabled(true);

                if (response.isSuccessful() && response.body() != null) {
                    AuthResponse authData = response.body();
                    String token = authData.getToken();
                    tokenManager.saveAuthData(token, email, fullName);

                    Toast.makeText(RegisterActivity.this, "🎉 Đăng ký tài khoản thành công!", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
                    startActivity(intent);
                    finish();
                } else {
                    String cleanError = "Email này đã tồn tại trên hệ thống!";
                    if (response.code() == 409) {
                        cleanError = "Email hoặc Tên đăng nhập này đã được sử dụng!";
                    } else if (response.errorBody() != null) {
                        try {
                            String rawJson = response.errorBody().string();
                            JSONObject jsonObject = new JSONObject(rawJson);
                            if (jsonObject.has("message")) {
                                cleanError = jsonObject.getString("message");
                            } else if (jsonObject.has("error")) {
                                cleanError = jsonObject.getString("error");
                            }
                        } catch (Exception ignored) {}
                    }
                    Toast.makeText(RegisterActivity.this, "Đăng ký thất bại: " + cleanError, Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<AuthResponse> call, Throwable t) {
                if (btnRegister != null) btnRegister.setEnabled(true);
                Toast.makeText(RegisterActivity.this, "Lỗi kết nối Server API: " + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    private void setupRegisterTermsAndPrivacy() {
        TextView tvRegisterTerms = findViewById(R.id.tvRegisterTerms);
        if (tvRegisterTerms == null) return;

        String fullText = "Bằng việc đăng ký, bạn đồng ý với Điều khoản dịch vụ & Chính sách bảo mật của Dishcision.";

        SpannableString spannableString = new SpannableString(fullText);

        int termsStart = fullText.indexOf("Điều khoản dịch vụ");
        int termsEnd = termsStart + "Điều khoản dịch vụ".length();

        int privacyStart = fullText.indexOf("Chính sách bảo mật");
        int privacyEnd = privacyStart + "Chính sách bảo mật".length();

        if (termsStart != -1) {
            spannableString.setSpan(new ClickableSpan() {
                @Override
                public void onClick(@NonNull View widget) {
                    Intent intent = new Intent(RegisterActivity.this, TermsOfServiceActivity.class);
                    startActivity(intent);
                }
            }, termsStart, termsEnd, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        }

        if (privacyStart != -1) {
            spannableString.setSpan(new ClickableSpan() {
                @Override
                public void onClick(@NonNull View widget) {
                    Intent intent = new Intent(RegisterActivity.this, PrivacyPolicyActivity.class);
                    startActivity(intent);
                }
            }, privacyStart, privacyEnd, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        }

        tvRegisterTerms.setText(spannableString);
        tvRegisterTerms.setMovementMethod(LinkMovementMethod.getInstance());
    }
}
