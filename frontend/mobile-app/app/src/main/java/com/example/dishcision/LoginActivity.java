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
import com.example.dishcision.network.AuthHelper;
import com.example.dishcision.network.AuthResponse;
import com.example.dishcision.network.GoogleAuthRequest;
import com.example.dishcision.network.LoginRequest;
import com.example.dishcision.network.TokenManager;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;

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
    private GoogleSignInClient mGoogleSignInClient;
    private ActivityResultLauncher<Intent> googleSignInLauncher;

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

        ApiClient.init(this);
        initViews();
        setupGoogleSignIn();
        setupListeners();
        setupTermsAndPrivacy();
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

                                if (btnGoogleLogin != null) btnGoogleLogin.setEnabled(false);
                                Toast.makeText(this, "Đang xác thực tài khoản Google...", Toast.LENGTH_SHORT).show();

                                GoogleAuthRequest googleReq = new GoogleAuthRequest(email, fullName, photoUrl);
                                ApiClient.getApiService().googleAuth(googleReq).enqueue(new Callback<AuthResponse>() {
                                    @Override
                                    public void onResponse(Call<AuthResponse> call, Response<AuthResponse> response) {
                                        if (btnGoogleLogin != null) btnGoogleLogin.setEnabled(true);
                                        if (response.isSuccessful() && response.body() != null
                                                && TokenManager.isValidJwtToken(response.body().getToken())) {
                                            AuthHelper.saveSessionFromAuth(LoginActivity.this, response.body(), email, fullName, true);
                                            Toast.makeText(LoginActivity.this, "Đăng nhập Google thành công: " + fullName, Toast.LENGTH_SHORT).show();
                                            Intent intent = new Intent(LoginActivity.this, HomeActivity.class);
                                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                            startActivity(intent);
                                            finish();
                                        } else {
                                            Toast.makeText(LoginActivity.this, "Xác thực Google thất bại (mã " + response.code() + ")", Toast.LENGTH_LONG).show();
                                        }
                                    }

                                    @Override
                                    public void onFailure(Call<AuthResponse> call, Throwable t) {
                                        if (btnGoogleLogin != null) btnGoogleLogin.setEnabled(true);
                                        Toast.makeText(LoginActivity.this, "Lỗi kết nối máy chủ: " + t.getMessage(), Toast.LENGTH_LONG).show();
                                    }
                                });
                            }
                        } catch (ApiException e) {
                            Toast.makeText(this, "Đăng nhập Google thất bại (Mã: " + e.getStatusCode() + ")", Toast.LENGTH_SHORT).show();
                        }
                    }
                }
        );
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

        View layoutLogo = findViewById(R.id.layoutLogo);
        if (layoutLogo != null) {
            layoutLogo.setOnClickListener(v -> showServerConfigDialog());
        }

        if (btnGoogleLogin != null) {
            btnGoogleLogin.setOnClickListener(v -> {
                if (mGoogleSignInClient != null && googleSignInLauncher != null) {
                    mGoogleSignInClient.signOut().addOnCompleteListener(task -> {
                        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
                        googleSignInLauncher.launch(signInIntent);
                    });
                }
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

        if (password.isEmpty()) {
            Toast.makeText(this, "Vui lòng nhập Mật khẩu!", Toast.LENGTH_SHORT).show();
            if (edtPassword != null) edtPassword.requestFocus();
            return;
        }

        if (btnLogin != null) btnLogin.setEnabled(false);
        LoginRequest request = new LoginRequest(email, password);

        ApiClient.getApiService().login(request).enqueue(new Callback<AuthResponse>() {
            @Override
            public void onResponse(Call<AuthResponse> call, Response<AuthResponse> response) {
                if (btnLogin != null) btnLogin.setEnabled(true);
                if (response.isSuccessful() && response.body() != null) {
                    AuthResponse.AuthData data = response.body().getData();
                    if (data != null && data.requiresTwoFactor
                            && data.tempToken != null && !data.tempToken.isEmpty()) {
                        String emailDisplay = (data.emailMasked != null && !data.emailMasked.isEmpty())
                                ? data.emailMasked
                                : (data.email != null && !data.email.isEmpty() ? data.email : email);
                        Toast.makeText(LoginActivity.this, "Đã gửi mã xác thực 2FA về email!", Toast.LENGTH_SHORT).show();
                        openTwoFactorVerification(data.tempToken, emailDisplay);
                        return;
                    }
                    if (TokenManager.isValidJwtToken(response.body().getToken())) {
                        AuthHelper.saveSessionFromAuth(LoginActivity.this, response.body(), email, "", false);

                        Toast.makeText(LoginActivity.this, "Đăng nhập thành công!", Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(LoginActivity.this, HomeActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);
                        finish();
                        return;
                    }
                }
                String errorMsg;
                if (response.code() == 401) {
                    errorMsg = "Mật khẩu hoặc Email/Tên đăng nhập không chính xác!";
                } else if (response.code() == 403) {
                    errorMsg = "Tài khoản chưa được kích hoạt hoặc đã bị khóa!";
                } else if (response.code() == 503) {
                    errorMsg = "Không thể gửi email xác thực lúc này. Vui lòng thử lại!";
                } else {
                    errorMsg = "Tài khoản hoặc mật khẩu không hợp lệ!";
                }
                Toast.makeText(LoginActivity.this, "Đăng nhập thất bại: " + errorMsg, Toast.LENGTH_LONG).show();
            }

            @Override
            public void onFailure(Call<AuthResponse> call, Throwable t) {
                if (btnLogin != null) btnLogin.setEnabled(true);
                Toast.makeText(LoginActivity.this, "Lỗi kết nối Server API: " + t.getMessage(), Toast.LENGTH_LONG).show();
                showServerConfigDialog();
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

    private void openTwoFactorVerification(String tempToken, String emailDisplay) {
        Intent intent = new Intent(LoginActivity.this, VerifyOtpActivity.class);
        intent.putExtra(VerifyOtpActivity.EXTRA_MODE, VerifyOtpActivity.MODE_2FA_LOGIN);
        intent.putExtra(VerifyOtpActivity.EXTRA_TEMP_TOKEN, tempToken);
        intent.putExtra(VerifyOtpActivity.EXTRA_EMAIL, emailDisplay);
        startActivity(intent);
    }

    private void showServerConfigDialog() {
        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(this);
        builder.setTitle("Cấu hình Server API Docker");

        final android.widget.EditText input = new android.widget.EditText(this);
        input.setHint("VD: https://dishcision.duckdns.org");
        input.setText(ApiClient.getServerRootUrl().replace("http://", "").replace("https://", ""));
        input.setPadding(40, 30, 40, 30);
        builder.setView(input);

        builder.setPositiveButton("Lưu & Thử lại", (dialog, which) -> {
            String server = input.getText().toString().trim();
            if (!server.isEmpty()) {
                ApiClient.setBaseUrl(this, server);
                Toast.makeText(this, "Đã cập nhật: " + ApiClient.getBaseUrl(), Toast.LENGTH_SHORT).show();
                performLogin();
            }
        });

        builder.setNegativeButton("Huỷ", (dialog, which) -> dialog.cancel());
        builder.setNeutralButton("Mặc định (dishcision.duckdns.org)", (dialog, which) -> {
            ApiClient.setBaseUrl(this, ApiClient.DEFAULT_BASE_URL);
            Toast.makeText(this, "Đã khôi phục máy chủ mặc định", Toast.LENGTH_SHORT).show();
            performLogin();
        });

        builder.show();
    }
}
