package com.example.dishcision;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.InputType;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextPaint;
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

public class RegisterActivity extends AppCompatActivity {

    private EditText edtFullName;
    private EditText edtRegisterEmail;
    private EditText edtRegisterPassword;
    private EditText edtConfirmPassword;
    private ImageView btnToggleRegisterPassword;
    private RelativeLayout btnRegister;
    private TextView tvLoginLink;

    private boolean isRegisterPasswordVisible = false;

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
        setupRegisterTermsAndPrivacy();
    }

    private void initViews() {
        edtFullName = findViewById(R.id.edtFullName);
        edtRegisterEmail = findViewById(R.id.edtRegisterEmail);
        edtRegisterPassword = findViewById(R.id.edtRegisterPassword);
        edtConfirmPassword = findViewById(R.id.edtConfirmPassword);
        btnToggleRegisterPassword = findViewById(R.id.btnToggleRegisterPassword);
        btnRegister = findViewById(R.id.btnRegister);
        tvLoginLink = findViewById(R.id.tvLoginLink);
    }

    private void setupListeners() {
        if (btnRegister != null) {
            btnRegister.setOnClickListener(v -> performRegister());
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
        // TỰ ĐỘNG BỎ QUA MỌI KIỂM TRA ĐIỀU KIỆN ĐỂ TEST CHUYỂN TRANG NGAY LẬP TỨC
        Toast.makeText(RegisterActivity.this, "Đăng ký thành công!", Toast.LENGTH_SHORT).show();
        Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
        startActivity(intent);
        finish();
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

                @Override
                public void updateDrawState(@NonNull TextPaint ds) {
                    super.updateDrawState(ds);
                    ds.setColor(Color.parseColor("#FF5722"));
                    ds.setUnderlineText(true);
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

                @Override
                public void updateDrawState(@NonNull TextPaint ds) {
                    super.updateDrawState(ds);
                    ds.setColor(Color.parseColor("#FF5722"));
                    ds.setUnderlineText(true);
                }
            }, privacyStart, privacyEnd, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        }

        tvRegisterTerms.setText(spannableString);
        tvRegisterTerms.setMovementMethod(LinkMovementMethod.getInstance());
    }

    public String getEnteredFullName() {
        return edtFullName != null ? edtFullName.getText().toString().trim() : "";
    }

    public String getEnteredRegisterEmail() {
        return edtRegisterEmail != null ? edtRegisterEmail.getText().toString().trim() : "";
    }

    public String getEnteredRegisterPassword() {
        return edtRegisterPassword != null ? edtRegisterPassword.getText().toString() : "";
    }
}
