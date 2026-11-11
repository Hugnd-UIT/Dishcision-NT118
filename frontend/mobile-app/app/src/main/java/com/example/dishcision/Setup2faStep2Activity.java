package com.example.dishcision;

import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.dishcision.network.ApiClient;
import com.example.dishcision.network.AuthHelper;
import com.example.dishcision.network.TwoFactorSetupResponse;
import com.example.dishcision.network.TwoFactorStatusResponse;
import com.example.dishcision.network.TwoFactorVerifyRequest;

import org.json.JSONObject;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class Setup2faStep2Activity extends AppCompatActivity {

    private ImageView btnBack;
    private TextView tvStep2Desc;
    private TextView tvResend2FACode;
    private EditText edt2FACode;
    private Button btnConfirm;
    private View btnBackGuide;
    private boolean isSubmitting = false;
    private boolean isSendingCode = false;
    private CountDownTimer resendTimer;
    private int remainingCooldownSeconds = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_setup_2fa_step2);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main_setup_2fa_step2), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        initViews();
        setupListeners();
        requestSetupEmailOtp(false);
    }

    @Override
    protected void onDestroy() {
        cancelResendTimer();
        super.onDestroy();
    }

    private void initViews() {
        btnBack = findViewById(R.id.btnBack);
        tvStep2Desc = findViewById(R.id.tvStep2Desc);
        tvResend2FACode = findViewById(R.id.tvResend2FACode);
        edt2FACode = findViewById(R.id.edt2FACode);
        btnConfirm = findViewById(R.id.btnConfirm);
        btnBackGuide = findViewById(R.id.btnBackGuide);

        com.example.dishcision.network.CurrentUserBinder.bind(this,
                findViewById(R.id.imgAvatar),
                findViewById(R.id.tvProfileName),
                findViewById(R.id.tvProfileHandle));
    }

    private void setupListeners() {
        if (btnBack != null) {
            btnBack.setOnClickListener(v -> finish());
        }

        if (btnBackGuide != null) {
            btnBackGuide.setOnClickListener(v -> finish());
        }

        if (tvResend2FACode != null) {
            tvResend2FACode.setOnClickListener(v -> {
                if (isSendingCode || remainingCooldownSeconds > 0) return;
                requestSetupEmailOtp(true);
            });
        }

        if (btnConfirm != null) {
            btnConfirm.setOnClickListener(v -> submit2FACodeToBackend());
        }
    }

    private void requestSetupEmailOtp(boolean isManualResend) {
        if (isSendingCode || (isManualResend && remainingCooldownSeconds > 0)) return;
        String authHeader = ApiClient.getAuthHeader(this);
        if (authHeader.isEmpty()) {
            Toast.makeText(this, "Vui lòng đăng nhập lại", Toast.LENGTH_SHORT).show();
            return;
        }

        setSendingResendState(isManualResend);
        if (tvStep2Desc != null && !isManualResend) {
            tvStep2Desc.setText("Đang gửi mã xác nhận gồm 6 chữ số tới email của bạn...");
        }

        ApiClient.getApiService().get2FASetup(authHeader).enqueue(new Callback<TwoFactorSetupResponse>() {
            @Override
            public void onResponse(@NonNull Call<TwoFactorSetupResponse> call, @NonNull Response<TwoFactorSetupResponse> response) {
                isSendingCode = false;
                if (isFinishing() || isDestroyed()) return;

                if (response.isSuccessful() && response.body() != null) {
                    TwoFactorSetupResponse.SetupData data = response.body().data;
                    String emailDisplay = "";
                    boolean alreadySent = false;
                    int retryAfterSeconds = AuthHelper.OTP_RESEND_COOLDOWN_SECONDS;
                    if (data != null) {
                        if (data.emailMasked != null && !data.emailMasked.isEmpty()) emailDisplay = data.emailMasked;
                        else if (data.email != null) emailDisplay = data.email;
                        alreadySent = data.alreadySent;
                        if (data.retryAfterSeconds > 0) {
                            retryAfterSeconds = data.retryAfterSeconds;
                        }
                    }
                    if (tvStep2Desc != null) {
                        tvStep2Desc.setText(emailDisplay.isEmpty()
                                ? "Chúng tôi đã gửi mã xác nhận gồm 6 chữ số tới email đăng ký của bạn."
                                : "Chúng tôi đã gửi mã xác nhận gồm 6 chữ số tới " + emailDisplay + ".");
                    }
                    if (isManualResend) {
                        String toastMsg = (alreadySent && response.body().message != null
                                && !response.body().message.trim().isEmpty())
                                ? response.body().message
                                : "Đã gửi lại mã xác thực tới email!";
                        Toast.makeText(Setup2faStep2Activity.this, toastMsg, Toast.LENGTH_SHORT).show();
                    }
                    startResendCountdown(retryAfterSeconds);
                } else {
                    String errMsg = parseErrorMessage(response, "Không thể gửi email xác thực");
                    if (tvStep2Desc != null) {
                        tvStep2Desc.setText("Chưa gửi được mã qua email. Bấm Gửi lại mã để thử lại.");
                    }
                    Toast.makeText(Setup2faStep2Activity.this, errMsg, Toast.LENGTH_LONG).show();
                    if (response.code() == 429) {
                        int waitSec = AuthHelper.extractCooldownSeconds(errMsg, AuthHelper.OTP_RESEND_COOLDOWN_SECONDS);
                        startResendCountdown(waitSec);
                    } else {
                        resetResendButton();
                    }
                }
            }

            @Override
            public void onFailure(@NonNull Call<TwoFactorSetupResponse> call, @NonNull Throwable t) {
                isSendingCode = false;
                if (isFinishing() || isDestroyed()) return;
                resetResendButton();
                if (tvStep2Desc != null) {
                    tvStep2Desc.setText("Lỗi kết nối máy chủ. Bấm Gửi lại mã để thử lại.");
                }
                Toast.makeText(Setup2faStep2Activity.this, "Không thể kết nối máy chủ", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void startResendCountdown(int seconds) {
        if (tvResend2FACode == null) return;
        cancelResendTimer();
        int targetSeconds = seconds > 0 ? seconds : AuthHelper.OTP_RESEND_COOLDOWN_SECONDS;
        remainingCooldownSeconds = targetSeconds;
        tvResend2FACode.setEnabled(false);
        tvResend2FACode.setClickable(false);
        tvResend2FACode.setTextColor(ContextCompat.getColor(this, R.color.text_gray));
        tvResend2FACode.setText("Gửi lại mã sau " + targetSeconds + "s");

        resendTimer = new CountDownTimer(targetSeconds * 1000L, 1000L) {
            @Override
            public void onTick(long millisUntilFinished) {
                if (isFinishing() || isDestroyed() || tvResend2FACode == null) return;
                int sec = Math.min(targetSeconds, Math.max(1, Math.round(millisUntilFinished / 1000.0f)));
                remainingCooldownSeconds = sec;
                tvResend2FACode.setText("Gửi lại mã sau " + sec + "s");
            }

            @Override
            public void onFinish() {
                resendTimer = null;
                remainingCooldownSeconds = 0;
                if (isFinishing() || isDestroyed()) return;
                if (!isSendingCode) {
                    resetResendButton();
                }
            }
        }.start();
    }

    private void setSendingResendState(boolean isManualResend) {
        isSendingCode = true;
        cancelResendTimer();
        remainingCooldownSeconds = 0;
        if (tvResend2FACode == null) return;
        tvResend2FACode.setEnabled(false);
        tvResend2FACode.setClickable(false);
        tvResend2FACode.setTextColor(ContextCompat.getColor(this, R.color.text_gray));
        tvResend2FACode.setText(isManualResend ? "Đang gửi lại mã..." : "Đang gửi mã...");
    }

    private void resetResendButton() {
        remainingCooldownSeconds = 0;
        if (tvResend2FACode == null) return;
        tvResend2FACode.setEnabled(true);
        tvResend2FACode.setClickable(true);
        tvResend2FACode.setTextColor(ContextCompat.getColor(this, R.color.primary));
        tvResend2FACode.setText("Gửi lại mã xác thực qua email");
    }

    private void cancelResendTimer() {
        if (resendTimer != null) {
            resendTimer.cancel();
            resendTimer = null;
        }
    }

    private void submit2FACodeToBackend() {
        if (edt2FACode == null || isSubmitting) return;
        String code = edt2FACode.getText().toString().trim();

        if (!code.matches("\\d{6}")) {
            Toast.makeText(this, "Vui lòng nhập đủ mã gồm 6 chữ số trong email", Toast.LENGTH_SHORT).show();
            edt2FACode.requestFocus();
            return;
        }

        String authHeader = ApiClient.getAuthHeader(this);
        if (authHeader.isEmpty()) {
            Toast.makeText(this, "Vui lòng đăng nhập lại", Toast.LENGTH_SHORT).show();
            return;
        }

        isSubmitting = true;
        if (btnConfirm != null) btnConfirm.setEnabled(false);

        TwoFactorVerifyRequest request = new TwoFactorVerifyRequest(code);
        ApiClient.getApiService().verify2FA(authHeader, request).enqueue(new Callback<TwoFactorStatusResponse>() {
            @Override
            public void onResponse(@NonNull Call<TwoFactorStatusResponse> call, @NonNull Response<TwoFactorStatusResponse> response) {
                isSubmitting = false;
                if (isFinishing() || isDestroyed()) return;
                if (btnConfirm != null) btnConfirm.setEnabled(true);
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    Toast.makeText(Setup2faStep2Activity.this, "Đã bật xác thực 2 yếu tố qua email!", Toast.LENGTH_LONG).show();
                    finish();
                } else {
                    String errMsg = parseErrorMessage(response, "Mã xác thực không chính xác hoặc đã hết hạn");
                    Toast.makeText(Setup2faStep2Activity.this, errMsg, Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<TwoFactorStatusResponse> call, @NonNull Throwable t) {
                isSubmitting = false;
                if (isFinishing() || isDestroyed()) return;
                if (btnConfirm != null) btnConfirm.setEnabled(true);
                Toast.makeText(Setup2faStep2Activity.this, "Lỗi kết nối máy chủ: " + t.getMessage(), Toast.LENGTH_LONG).show();
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
}
