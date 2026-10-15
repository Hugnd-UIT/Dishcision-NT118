package com.example.dishcision;

import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
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
    private ImageView btnToggleConfirmPassword;
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
        tvLoginLink = findViewById(R.id.tvLoginLink);
    }

    private void setupListeners() {
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

    private void setupTermsText() {
        TextView tvRegisterTerms = findViewById(R.id.tvRegisterTerms);
        if (tvRegisterTerms != null) {
            String termsText = getString(R.string.register_terms_prefix) +
                    getString(R.string.terms_of_service_vn) + " & " +
                    getString(R.string.privacy_policy_vn) +
                    getString(R.string.register_terms_suffix);
            tvRegisterTerms.setText(termsText);
        }
    }
}
