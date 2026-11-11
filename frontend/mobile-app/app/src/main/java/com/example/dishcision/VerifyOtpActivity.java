package com.example.dishcision;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.dishcision.network.ApiClient;
import com.example.dishcision.network.AuthHelper;
import com.example.dishcision.network.AuthResponse;
import com.example.dishcision.network.ForgotPasswordRequest;
import com.example.dishcision.network.ResendLogin2FARequest;
import com.example.dishcision.network.TokenManager;
import com.example.dishcision.network.VerifyLogin2FARequest;
import com.example.dishcision.network.VerifyOtpRequest;

import org.json.JSONObject;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class VerifyOtpActivity extends AppCompatActivity {

    public static final String EXTRA_EMAIL = "extra_email";
    public static final String EXTRA_MODE = "extra_mode";
    public static final String EXTRA_TEMP_TOKEN = "extra_temp_token";
    public static final String EXTRA_IS_GOOGLE_LOGIN = "extra_is_google_login";
    public static final String MODE_FORGOT_PASSWORD = "forgot_password";
    public static final String MODE_2FA_LOGIN = "2fa_login";

    private ImageView btnBack;
    private TextView tvHeaderTitle;
    private View layoutStepProgress;
    private TextView tvOtpSublabel;
    private TextView tvOtpTitle;
    private TextView tvOtpSubtitle;
    private TextView tvOtpFieldLabel;
    private TextView tvVerifyOtpBtnText;
    private TextView tvOtpNotice;
    private android.view.View layoutTargetEmailBox;
    private TextView tvTargetEmail;
    private EditText edtOtpCode;
    private RelativeLayout btnVerifyOtp;
    private TextView tvResendCode;
    private TextView tvChangeEmail;

    private String userEmail = "";
    private String mode = MODE_FORGOT_PASSWORD;
    private String tempToken = "";
    private boolean isGoogleLogin = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_verify_otp);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main_verify_otp), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        ApiClient.init(this);
        initViews();
        setupData();
        setupListeners();
    }

    private void initViews() {
        btnBack = findViewById(R.id.btnBack);
        tvHeaderTitle = findViewById(R.id.tvHeaderTitle);
        layoutStepProgress = findViewById(R.id.layoutStepProgress);
        tvOtpSublabel = findViewById(R.id.tvOtpSublabel);
        tvOtpTitle = findViewById(R.id.tvOtpTitle);
        tvOtpSubtitle = findViewById(R.id.tvOtpSubtitle);
        tvOtpFieldLabel = findViewById(R.id.tvOtpFieldLabel);
        tvVerifyOtpBtnText = findViewById(R.id.tvVerifyOtpBtnText);
        tvOtpNotice = findViewById(R.id.tvOtpNotice);
        layoutTargetEmailBox = findViewById(R.id.layoutTargetEmailBox);
        tvTargetEmail = findViewById(R.id.tvTargetEmail);
        edtOtpCode = findViewById(R.id.edtOtpCode);
        btnVerifyOtp = findViewById(R.id.btnVerifyOtp);
        tvResendCode = findViewById(R.id.tvResendCode);
        tvChangeEmail = findViewById(R.id.tvChangeEmail);
    }

    private void setupData() {
        Intent intent = getIntent();
        if (intent != null) {
            if (intent.hasExtra(EXTRA_EMAIL)) {
                String passedEmail = intent.getStringExtra(EXTRA_EMAIL);
                if (passedEmail != null && !passedEmail.trim().isEmpty()) {
                    userEmail = passedEmail.trim();
                }
            }
            String passedMode = intent.getStringExtra(EXTRA_MODE);
            if (MODE_2FA_LOGIN.equals(passedMode)) {
                mode = MODE_2FA_LOGIN;
            }
            String passedToken = intent.getStringExtra(EXTRA_TEMP_TOKEN);
            if (passedToken != null) {
                tempToken = passedToken.trim();
            }
            isGoogleLogin = intent.getBooleanExtra(EXTRA_IS_GOOGLE_LOGIN, false);
        }

        if (MODE_2FA_LOGIN.equals(mode)) {
            if (tvHeaderTitle != null) tvHeaderTitle.setText("Xác thực hai yếu tố");
            if (layoutStepProgress != null) layoutStepProgress.setVisibility(View.GONE);
            if (tvOtpSublabel != null) tvOtpSublabel.setText("XÁC THỰC ĐĂNG NHẬP 2FA");
            if (tvOtpTitle != null) tvOtpTitle.setText("Nhập mã từ email");
            if (tvOtpSubtitle != null) tvOtpSubtitle.setText("Chúng tôi đã gửi mã xác thực gồm 6 chữ số đến email đăng ký của bạn.");
            if (tvOtpFieldLabel != null) tvOtpFieldLabel.setText("Mã xác thực 6 chữ số");
            if (edtOtpCode != null) edtOtpCode.setHint("Nhập mã 6 chữ số trong email");
            if (tvVerifyOtpBtnText != null) tvVerifyOtpBtnText.setText("➔ Xác nhận đăng nhập");
            if (tvChangeEmail != null) tvChangeEmail.setText("Quay lại đăng nhập");
            if (tvOtpNotice != null) tvOtpNotice.setText("ℹ️ Không chia sẻ mã xác thực đăng nhập với người khác.");
        }

        if (userEmail.isEmpty()) {
            if (layoutTargetEmailBox != null) {
                layoutTargetEmailBox.setVisibility(android.view.View.GONE);
            }
        } else {
            if (layoutTargetEmailBox != null) {
                layoutTargetEmailBox.setVisibility(android.view.View.VISIBLE);
            }
            if (tvTargetEmail != null) {
                tvTargetEmail.setText(userEmail);
            }
        }
    }

    private void setupListeners() {
        if (btnBack != null) {
            btnBack.setOnClickListener(v -> finish());
        }

        if (btnVerifyOtp != null) {
            btnVerifyOtp.setOnClickListener(v -> {
                String code = edtOtpCode != null ? edtOtpCode.getText().toString().trim() : "";
                if (!code.matches("\\d{6}")) {
                    Toast.makeText(this, "Vui lòng nhập mã OTP gồm 6 chữ số", Toast.LENGTH_SHORT).show();
                    if (edtOtpCode != null) edtOtpCode.requestFocus();
                    return;
                }

                if (MODE_2FA_LOGIN.equals(mode)) {
                    submit2FALoginOtp(code);
                    return;
                }

                if (userEmail.isEmpty()) {
                    Toast.makeText(this, "Thiếu thông tin email khôi phục", Toast.LENGTH_SHORT).show();
                    return;
                }

                btnVerifyOtp.setEnabled(false);
                VerifyOtpRequest request = new VerifyOtpRequest(userEmail, code);
                ApiClient.getApiService().verifyOtp(request).enqueue(new Callback<AuthResponse>() {
                    @Override
                    public void onResponse(Call<AuthResponse> call, Response<AuthResponse> response) {
                        btnVerifyOtp.setEnabled(true);
                        if (response.isSuccessful() && response.body() != null
                                && response.body().getData() != null
                                && response.body().getData().tokenHash != null
                                && !response.body().getData().tokenHash.isEmpty()) {
                            Toast.makeText(VerifyOtpActivity.this, "Xác thực mã OTP thành công!", Toast.LENGTH_SHORT).show();
                            navigateToResetPassword(response.body().getData().tokenHash);
                        } else {
                            Toast.makeText(VerifyOtpActivity.this, "Mã OTP không chính xác hoặc đã hết hạn", Toast.LENGTH_LONG).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<AuthResponse> call, Throwable t) {
                        btnVerifyOtp.setEnabled(true);
                        Toast.makeText(VerifyOtpActivity.this, "Lỗi kết nối máy chủ: " + t.getMessage(), Toast.LENGTH_LONG).show();
                    }
                });
            });
        }

        if (tvResendCode != null) {
            tvResendCode.setOnClickListener(v -> {
                if (MODE_2FA_LOGIN.equals(mode)) {
                    resend2FALoginOtp();
                    return;
                }
                if (userEmail.isEmpty()) {
                    Toast.makeText(this, "Thiếu thông tin email khôi phục", Toast.LENGTH_SHORT).show();
                    return;
                }
                ForgotPasswordRequest req = new ForgotPasswordRequest(userEmail);
                ApiClient.getApiService().forgotPassword(req).enqueue(new Callback<AuthResponse>() {
                    @Override
                    public void onResponse(Call<AuthResponse> call, Response<AuthResponse> response) {
                        if (response.isSuccessful()) {
                            Toast.makeText(VerifyOtpActivity.this, "Đã gửi lại mã OTP tới email!", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(VerifyOtpActivity.this, "Không thể gửi lại mã (mã " + response.code() + ")", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<AuthResponse> call, Throwable t) {
                        Toast.makeText(VerifyOtpActivity.this, "Lỗi kết nối máy chủ", Toast.LENGTH_SHORT).show();
                    }
                });
            });
        }

        if (tvChangeEmail != null) {
            tvChangeEmail.setOnClickListener(v -> finish());
        }
    }

    private void submit2FALoginOtp(String code) {
        if (tempToken.isEmpty()) {
            Toast.makeText(this, "Phiên xác thực đã hết hạn. Vui lòng đăng nhập lại.", Toast.LENGTH_LONG).show();
            finish();
            return;
        }
        btnVerifyOtp.setEnabled(false);
        ApiClient.getApiService().verifyLogin2FA(new VerifyLogin2FARequest(tempToken, code)).enqueue(new Callback<AuthResponse>() {
            @Override
            public void onResponse(Call<AuthResponse> call, Response<AuthResponse> response) {
                btnVerifyOtp.setEnabled(true);
                if (isFinishing() || isDestroyed()) return;

                if (response.isSuccessful() && response.body() != null
                        && TokenManager.isValidJwtToken(response.body().getToken())) {
                    AuthHelper.saveSessionFromAuth(VerifyOtpActivity.this, response.body(), userEmail, "", isGoogleLogin);
                    Toast.makeText(VerifyOtpActivity.this, "Đăng nhập thành công!", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(VerifyOtpActivity.this, HomeActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    finish();
                } else {
                    String err = parseErrorMessage(response, "Mã xác thực không chính xác hoặc đã hết hạn");
                    Toast.makeText(VerifyOtpActivity.this, err, Toast.LENGTH_LONG).show();
                    if (response.code() == 429) {
                        finish();
                    }
                }
            }

            @Override
            public void onFailure(Call<AuthResponse> call, Throwable t) {
                btnVerifyOtp.setEnabled(true);
                if (isFinishing() || isDestroyed()) return;
                Toast.makeText(VerifyOtpActivity.this, "Lỗi kết nối máy chủ: " + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    private void resend2FALoginOtp() {
        if (tempToken.isEmpty()) {
            Toast.makeText(this, "Phiên xác thực đã hết hạn. Vui lòng đăng nhập lại.", Toast.LENGTH_LONG).show();
            finish();
            return;
        }
        tvResendCode.setEnabled(false);
        ApiClient.getApiService().resendLogin2FA(new ResendLogin2FARequest(tempToken)).enqueue(new Callback<AuthResponse>() {
            @Override
            public void onResponse(Call<AuthResponse> call, Response<AuthResponse> response) {
                if (isFinishing() || isDestroyed()) return;
                tvResendCode.setEnabled(true);
                if (response.isSuccessful()) {
                    Toast.makeText(VerifyOtpActivity.this, "Đã gửi lại mã xác thực tới email!", Toast.LENGTH_SHORT).show();
                } else {
                    String err = parseErrorMessage(response, "Không thể gửi lại mã (mã " + response.code() + ")");
                    Toast.makeText(VerifyOtpActivity.this, err, Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<AuthResponse> call, Throwable t) {
                if (isFinishing() || isDestroyed()) return;
                tvResendCode.setEnabled(true);
                Toast.makeText(VerifyOtpActivity.this, "Lỗi kết nối máy chủ", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private String parseErrorMessage(Response<?> response, String fallback) {
        try {
            if (response.errorBody() != null) {
                String raw = response.errorBody().string();
                String msg = new JSONObject(raw).optString("message", "").trim();
                if (!msg.isEmpty()) return msg;
            }
        } catch (Exception ignored) {}
        return fallback;
    }

    private void navigateToResetPassword(String tokenHash) {
        Intent intent = new Intent(VerifyOtpActivity.this, ResetPasswordActivity.class);
        intent.putExtra(ResetPasswordActivity.EXTRA_TOKEN_HASH, tokenHash);
        intent.putExtra(ResetPasswordActivity.EXTRA_EMAIL, userEmail);
        startActivity(intent);
    }
}
