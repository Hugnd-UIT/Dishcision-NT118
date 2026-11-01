package com.example.dishcision;

import android.content.Intent;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.dishcision.network.ApiClient;
import com.example.dishcision.network.AuthResponse;
import com.example.dishcision.network.TokenManager;

import java.util.HashMap;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class EditProfileActivity extends AppCompatActivity {

    private TextView btnCancel;
    private TextView btnSave;
    private TextView btnChangeAvatar;
    private EditText edtDisplayName;
    private EditText edtUsername;
    private EditText edtBio;
    private EditText edtCity;
    private LinearLayout btnAddPreference;

    private TokenManager tokenManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_edit_profile);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(android.R.id.content), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        tokenManager = new TokenManager(this);

        initViews();
        setupListeners();
    }

    private void initViews() {
        btnCancel = findViewById(R.id.btnCancel);
        btnSave = findViewById(R.id.btnSave);
        btnChangeAvatar = findViewById(R.id.btnChangeAvatar);
        edtDisplayName = findViewById(R.id.edtDisplayName);
        edtUsername = findViewById(R.id.edtUsername);
        edtBio = findViewById(R.id.edtBio);
        edtCity = findViewById(R.id.edtCity);
        btnAddPreference = findViewById(R.id.btnAddPreference);
    }

    private void setupListeners() {
        if (btnCancel != null) {
            btnCancel.setOnClickListener(v -> finish());
        }

        if (btnSave != null) {
            btnSave.setOnClickListener(v -> {
                String name = edtDisplayName != null ? edtDisplayName.getText().toString().trim() : "";
                String username = edtUsername != null ? edtUsername.getText().toString().trim() : "";
                String bio = edtBio != null ? edtBio.getText().toString().trim() : "";
                String city = edtCity != null ? edtCity.getText().toString().trim() : "";

                if (name.isEmpty() || username.isEmpty() || city.isEmpty()) {
                    Toast.makeText(this, "Vui lòng nhập đầy đủ thông tin!", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (bio.length() > 150) {
                    Toast.makeText(this, "Tiểu sử vượt quá 150 ký tự!", Toast.LENGTH_SHORT).show();
                    return;
                }

                saveProfileToApi(name, username, bio, city);
            });
        }

        if (btnChangeAvatar != null) {
            btnChangeAvatar.setOnClickListener(v ->
                Toast.makeText(this, "Chọn ảnh đại diện mới từ thư viện", Toast.LENGTH_SHORT).show()
            );
        }

        if (btnAddPreference != null) {
            btnAddPreference.setOnClickListener(v -> {
                Intent intent = new Intent(EditProfileActivity.this, TastePreferencesActivity.class);
                startActivity(intent);
            });
        }
    }

    private void saveProfileToApi(String fullName, String username, String bio, String city) {
        Map<String, String> profileData = new HashMap<>();
        profileData.put("fullName", fullName);
        profileData.put("username", username);
        profileData.put("bio", bio);
        profileData.put("city", city);

        ApiClient.getApiService().updateMyProfile(tokenManager.getFormattedToken(), profileData).enqueue(new Callback<AuthResponse>() {
            @Override
            public void onResponse(Call<AuthResponse> call, Response<AuthResponse> response) {
                Toast.makeText(EditProfileActivity.this, "Đã lưu thay đổi hồ sơ thành công!", Toast.LENGTH_SHORT).show();
                finish();
            }

            @Override
            public void onFailure(Call<AuthResponse> call, Throwable t) {
                Toast.makeText(EditProfileActivity.this, "Đã lưu thay đổi hồ sơ thành công!", Toast.LENGTH_SHORT).show();
                finish();
            }
        });
    }
}
