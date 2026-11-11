package com.example.dishcision;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.OnBackPressedCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.dishcision.network.ApiClient;
import com.example.dishcision.network.AuthResponse;
import com.example.dishcision.network.ImageLoader;
import com.example.dishcision.network.TokenManager;
import com.example.dishcision.network.UploadResponse;
import com.example.dishcision.network.UserProfileResponse;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class EditProfileActivity extends AppCompatActivity {

    private TextView btnCancel;
    private TextView btnSave;
    private ImageView imgAvatar;
    private TextView btnChangeAvatar;
    private EditText edtDisplayName;
    private EditText edtUsername;
    private TextView tvProfileUrl;
    private TextView tvBioCount;
    private EditText edtBio;
    private EditText edtCity;
    private LinearLayout btnAddPreference;
    private LinearLayout layoutPreferenceChipsContainer;

    private TokenManager tokenManager;

    private String initialFullName = "";
    private String initialUsername = "";
    private String initialBio = "";
    private String initialAvatarUrl = "";
    private String currentAvatarUrl = "";
    private String initialCity = "";

    private ActivityResultLauncher<String> imagePickerLauncher;
    private boolean isUploadingAvatar = false;
    private boolean isSaving = false;

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
        setupImagePicker();
        setupDataFromIntentOrApi();
        setupListeners();
        setupBackPressHandler();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadAndRenderPreferences();
    }

    private void initViews() {
        btnCancel = findViewById(R.id.btnCancel);
        btnSave = findViewById(R.id.btnSave);
        imgAvatar = findViewById(R.id.imgAvatar);
        btnChangeAvatar = findViewById(R.id.btnChangeAvatar);
        edtDisplayName = findViewById(R.id.edtDisplayName);
        edtUsername = findViewById(R.id.edtUsername);
        tvProfileUrl = findViewById(R.id.tvProfileUrl);
        tvBioCount = findViewById(R.id.tvBioCount);
        edtBio = findViewById(R.id.edtBio);
        edtCity = findViewById(R.id.edtCity);
        btnAddPreference = findViewById(R.id.btnAddPreference);
        layoutPreferenceChipsContainer = findViewById(R.id.layoutPreferenceChipsContainer);

        // edtUsername is editable
    }

    private void setupImagePicker() {
        imagePickerLauncher = registerForActivityResult(
                new ActivityResultContracts.GetContent(),
                uri -> {
                    if (uri != null) {
                        uploadAvatarImage(uri);
                    }
                }
        );
    }

    private void setupDataFromIntentOrApi() {
        Intent intent = getIntent();
        String extraFullName = intent.getStringExtra("FULL_NAME");
        String extraUsername = intent.getStringExtra("USERNAME");
        String extraBio = intent.getStringExtra("BIO");
        String extraAvatar = intent.getStringExtra("AVATAR_URL");

        SharedPreferences prefs = getSharedPreferences("DishcisionPrefs", MODE_PRIVATE);
        initialCity = prefs.getString("user_city", "");
        if (edtCity != null) {
            edtCity.setText(initialCity);
        }

        if (extraFullName != null && !extraFullName.isEmpty()) {
            initialFullName = extraFullName;
            initialBio = extraBio != null ? extraBio : "";
            initialAvatarUrl = extraAvatar != null ? extraAvatar : "";
            currentAvatarUrl = initialAvatarUrl;

            populateFields(extraFullName, extraUsername != null ? extraUsername : "", initialBio, initialAvatarUrl);
        } else {
            fetchExistingProfile();
        }
    }

    private void populateFields(String fullName, String username, String bio, String avatarUrl) {
        if (edtDisplayName != null) {
            edtDisplayName.setText(fullName);
        }
        initialUsername = username != null ? username.replace("@", "").trim() : "";
        if (edtUsername != null) {
            edtUsername.setText(initialUsername);
            if (tvProfileUrl != null) {
                tvProfileUrl.setText("dishcision.com/@" + (initialUsername.isEmpty() ? "user" : initialUsername));
            }
        }
        if (edtBio != null) {
            edtBio.setText(bio);
        }
        updateBioCount(bio != null ? bio.length() : 0);

        if (imgAvatar != null && avatarUrl != null && !avatarUrl.isEmpty()) {
            ImageLoader.loadAvatar(imgAvatar, avatarUrl);
        }
    }

    private void fetchExistingProfile() {
        String authHeader = ApiClient.getAuthHeader(this);
        ApiClient.getApiService().getMyProfile(authHeader).enqueue(new Callback<UserProfileResponse>() {
            @Override
            public void onResponse(Call<UserProfileResponse> call, Response<UserProfileResponse> response) {
                if (response.isSuccessful() && response.body() != null && response.body().getData() != null) {
                    UserProfileResponse.UserData user = response.body().getData();
                    initialFullName = user.fullName != null ? user.fullName : "";
                    initialBio = user.bio != null ? user.bio : "";
                    initialAvatarUrl = user.avatarUrl != null ? user.avatarUrl : "";
                    currentAvatarUrl = initialAvatarUrl;

                    populateFields(initialFullName, user.username, initialBio, initialAvatarUrl);

                    if (user.preferences != null && !user.preferences.isEmpty()) {
                        getSharedPreferences("DishcisionPrefs", MODE_PRIVATE).edit()
                                .putStringSet("user_taste_preferences", new HashSet<>(user.preferences))
                                .apply();
                        loadAndRenderPreferences();
                    }
                }
            }

            @Override
            public void onFailure(Call<UserProfileResponse> call, Throwable t) {}
        });
    }

    private void loadAndRenderPreferences() {
        if (layoutPreferenceChipsContainer == null) return;
        layoutPreferenceChipsContainer.removeAllViews();

        SharedPreferences prefs = getSharedPreferences("DishcisionPrefs", MODE_PRIVATE);
        Set<String> savedSet = prefs.getStringSet("user_taste_preferences", null);

        List<String> list = (savedSet != null) ? new ArrayList<>(savedSet) : new ArrayList<>();

        if (list.isEmpty()) {
            TextView emptyTv = new TextView(this);
            emptyTv.setText("Chưa có khẩu vị nào được chọn. Nhấn '+ Thêm khẩu vị' bên dưới.");
            emptyTv.setTextColor(Color.parseColor("#8E7570"));
            emptyTv.setTextSize(13);
            emptyTv.setPadding(0, dpToPx(4), 0, dpToPx(8));
            layoutPreferenceChipsContainer.addView(emptyTv);
            return;
        }

        LinearLayout currentRow = null;
        for (int i = 0; i < list.size(); i++) {
            if (i % 2 == 0) {
                currentRow = new LinearLayout(this);
                currentRow.setOrientation(LinearLayout.HORIZONTAL);
                LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                lp.bottomMargin = dpToPx(8);
                currentRow.setLayoutParams(lp);
                layoutPreferenceChipsContainer.addView(currentRow);
            }

            final String tasteName = list.get(i);
            View chipView = getLayoutInflater().inflate(R.layout.item_preference_chip, currentRow, false);
            TextView tvName = chipView.findViewById(R.id.tvChipName);
            ImageView btnRemove = chipView.findViewById(R.id.btnRemoveChip);

            tvName.setText(tasteName);
            btnRemove.setOnClickListener(v -> removeTastePreference(tasteName));

            if (currentRow != null) {
                currentRow.addView(chipView);
            }
        }
    }

    private void removeTastePreference(String tasteName) {
        SharedPreferences prefs = getSharedPreferences("DishcisionPrefs", MODE_PRIVATE);
        Set<String> savedSet = prefs.getStringSet("user_taste_preferences", null);
        if (savedSet != null) {
            Set<String> newSet = new HashSet<>(savedSet);
            newSet.remove(tasteName);
            prefs.edit().putStringSet("user_taste_preferences", newSet).apply();
            loadAndRenderPreferences();
            Toast.makeText(this, "Đã xóa khẩu vị: " + tasteName, Toast.LENGTH_SHORT).show();
        }
    }

    private int dpToPx(int dp) {
        return (int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP, dp, getResources().getDisplayMetrics());
    }

    private void setupListeners() {
        if (btnCancel != null) {
            btnCancel.setOnClickListener(v -> handleCancelAttempt());
        }

        if (btnChangeAvatar != null) {
            btnChangeAvatar.setOnClickListener(v -> {
                if (isUploadingAvatar) {
                    Toast.makeText(this, "Đang tải ảnh lên, vui lòng đợi...", Toast.LENGTH_SHORT).show();
                    return;
                }
                imagePickerLauncher.launch("image/*");
            });
        }

        if (imgAvatar != null) {
            imgAvatar.setOnClickListener(v -> {
                if (!isUploadingAvatar) {
                    imagePickerLauncher.launch("image/*");
                }
            });
        }

        if (edtUsername != null) {
            edtUsername.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    String clean = s != null ? s.toString().trim().replace("@", "") : "";
                    if (tvProfileUrl != null) {
                        tvProfileUrl.setText("dishcision.com/@" + (clean.isEmpty() ? "user" : clean));
                    }
                }

                @Override
                public void afterTextChanged(Editable s) {}
            });
        }

        if (edtBio != null) {
            edtBio.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    updateBioCount(s != null ? s.length() : 0);
                }

                @Override
                public void afterTextChanged(Editable s) {}
            });
        }

        if (btnSave != null) {
            btnSave.setOnClickListener(v -> validateAndSave());
        }

        if (btnAddPreference != null) {
            btnAddPreference.setOnClickListener(v -> showAddPreferenceOptions());
        }
        View tvBtnAdd = findViewById(R.id.tvBtnAddPreference);
        if (tvBtnAdd != null) {
            tvBtnAdd.setOnClickListener(v -> showAddPreferenceOptions());
        }
    }

    private void showAddPreferenceOptions() {
        final EditText input = new EditText(this);
        input.setHint("Nhập khẩu vị mới (VD: Thuần chay, Ít ngọt...)");
        int padding = dpToPx(16);
        input.setPadding(padding, padding, padding, padding);

        new AlertDialog.Builder(this)
                .setTitle("Thêm khẩu vị món ăn")
                .setView(input)
                .setPositiveButton("Thêm ngay", (dialog, which) -> {
                    String taste = input.getText().toString().trim();
                    if (!taste.isEmpty()) {
                        addCustomTastePreference("✨ " + taste);
                    }
                })
                .setNegativeButton("Hủy", null)
                .show();
    }

    private void addCustomTastePreference(String tasteName) {
        SharedPreferences prefs = getSharedPreferences("DishcisionPrefs", MODE_PRIVATE);
        Set<String> savedSet = prefs.getStringSet("user_taste_preferences", null);
        Set<String> newSet = savedSet != null ? new HashSet<>(savedSet) : new HashSet<>();
        newSet.add(tasteName);
        prefs.edit().putStringSet("user_taste_preferences", newSet).apply();
        loadAndRenderPreferences();
        Toast.makeText(this, "Đã thêm khẩu vị: " + tasteName, Toast.LENGTH_SHORT).show();
    }

    private void updateBioCount(int length) {
        if (tvBioCount != null) {
            tvBioCount.setText(length + "/255");
            if (length > 255) {
                tvBioCount.setTextColor(Color.parseColor("#FF5A36"));
            } else {
                tvBioCount.setTextColor(Color.parseColor("#8E7570"));
            }
        }
    }

    private void setupBackPressHandler() {
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                handleCancelAttempt();
            }
        });
    }

    private boolean isFormModified() {
        String currentName = edtDisplayName != null ? edtDisplayName.getText().toString().trim() : "";
        String currentUsername = edtUsername != null ? edtUsername.getText().toString().trim().replace("@", "") : "";
        String currentBioText = edtBio != null ? edtBio.getText().toString().trim() : "";
        String currentCity = edtCity != null ? edtCity.getText().toString().trim() : "";
        boolean avatarModified = !currentAvatarUrl.equals(initialAvatarUrl);

        return !currentName.equals(initialFullName)
                || !currentUsername.equals(initialUsername)
                || !currentBioText.equals(initialBio)
                || !currentCity.equals(initialCity)
                || avatarModified;
    }

    private void handleCancelAttempt() {
        if (isFormModified()) {
            new AlertDialog.Builder(this)
                    .setTitle("Hủy thay đổi?")
                    .setMessage("Bạn có các thông tin đã chỉnh sửa nhưng chưa lưu. Bạn có chắc muốn hủy bỏ?")
                    .setPositiveButton("Hủy bỏ", (dialog, which) -> finish())
                    .setNegativeButton("Tiếp tục sửa", (dialog, which) -> dialog.dismiss())
                    .show();
        } else {
            finish();
        }
    }

    private void uploadAvatarImage(Uri imageUri) {
        try {
            isUploadingAvatar = true;
            if (btnChangeAvatar != null) {
                btnChangeAvatar.setText("Đang tải ảnh...");
            }
            if (imgAvatar != null) {
                imgAvatar.setImageURI(imageUri);
            }

            File tempFile = new File(getCacheDir(), "avatar_" + System.currentTimeMillis() + ".jpg");
            InputStream inputStream = getContentResolver().openInputStream(imageUri);
            if (inputStream == null) {
                isUploadingAvatar = false;
                if (btnChangeAvatar != null) btnChangeAvatar.setText("Đổi ảnh đại diện");
                return;
            }

            FileOutputStream outputStream = new FileOutputStream(tempFile);
            byte[] buffer = new byte[4096];
            int read;
            while ((read = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, read);
            }
            outputStream.flush();
            outputStream.close();
            inputStream.close();

            RequestBody requestFile = RequestBody.create(MediaType.parse("image/*"), tempFile);
            MultipartBody.Part filePart = MultipartBody.Part.createFormData("file", tempFile.getName(), requestFile);

            String authHeader = ApiClient.getAuthHeader(this);
            ApiClient.getApiService().uploadSingleImage(authHeader, filePart).enqueue(new Callback<UploadResponse>() {
                @Override
                public void onResponse(Call<UploadResponse> call, Response<UploadResponse> response) {
                    isUploadingAvatar = false;
                    if (btnChangeAvatar != null) {
                        btnChangeAvatar.setText("Đổi ảnh đại diện");
                    }

                    if (response.isSuccessful() && response.body() != null && response.body().data != null) {
                        String rawUrl = (response.body().data.url != null && !response.body().data.url.isEmpty())
                                ? response.body().data.url
                                : response.body().data.fullUrl;
                        currentAvatarUrl = ApiClient.normalizeMediaUrl(rawUrl);
                        Toast.makeText(EditProfileActivity.this, "Tải ảnh đại diện lên thành công!", Toast.LENGTH_SHORT).show();
                    } else if (response.code() == 401) {
                        Toast.makeText(EditProfileActivity.this, "Phiên đăng nhập hết hạn, vui lòng đăng nhập lại!", Toast.LENGTH_LONG).show();
                    } else {
                        Toast.makeText(EditProfileActivity.this, "Không thể tải ảnh: Mã phản hồi " + response.code(), Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onFailure(Call<UploadResponse> call, Throwable t) {
                    isUploadingAvatar = false;
                    if (btnChangeAvatar != null) {
                        btnChangeAvatar.setText("Đổi ảnh đại diện");
                    }
                    Toast.makeText(EditProfileActivity.this, "Lỗi kết nối tải ảnh: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });

        } catch (Exception e) {
            isUploadingAvatar = false;
            if (btnChangeAvatar != null) {
                btnChangeAvatar.setText("Đổi ảnh đại diện");
            }
            Toast.makeText(this, "Không thể xử lý ảnh: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void validateAndSave() {
        if (isSaving) return;

        if (isUploadingAvatar) {
            Toast.makeText(this, "Ảnh đại diện đang tải lên, vui lòng đợi...", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!tokenManager.isLoggedIn()) {
            new AlertDialog.Builder(this)
                    .setTitle("Cần đăng nhập")
                    .setMessage("Bạn chưa có phiên đăng nhập hợp lệ hoặc phiên đã hết hạn. Vui lòng đăng nhập tài khoản để lưu thông tin.")
                    .setPositiveButton("Đăng nhập", (dialog, which) -> {
                        tokenManager.clearAuth();
                        Intent intent = new Intent(EditProfileActivity.this, LoginActivity.class);
                        startActivity(intent);
                    })
                    .setNegativeButton("Hủy", null)
                    .show();
            return;
        }

        String name = edtDisplayName != null ? edtDisplayName.getText().toString().trim() : "";
        String username = edtUsername != null ? edtUsername.getText().toString().trim().replace("@", "") : "";
        String bio = edtBio != null ? edtBio.getText().toString().trim() : "";
        String city = edtCity != null ? edtCity.getText().toString().trim() : "";

        if (name.isEmpty()) {
            Toast.makeText(this, "Họ tên không được để trống!", Toast.LENGTH_SHORT).show();
            if (edtDisplayName != null) edtDisplayName.requestFocus();
            return;
        }

        if (username.isEmpty()) {
            Toast.makeText(this, "Tên người dùng (@username) không được để trống!", Toast.LENGTH_SHORT).show();
            if (edtUsername != null) edtUsername.requestFocus();
            return;
        }

        if (username.length() < 3 || username.length() > 50) {
            Toast.makeText(this, "Tên người dùng phải từ 3 đến 50 ký tự!", Toast.LENGTH_SHORT).show();
            if (edtUsername != null) edtUsername.requestFocus();
            return;
        }

        if (!username.matches("^[a-zA-Z0-9_.]+$")) {
            Toast.makeText(this, "Tên người dùng chỉ được chứa chữ cái, số, dấu gạch dưới và dấu chấm!", Toast.LENGTH_SHORT).show();
            if (edtUsername != null) edtUsername.requestFocus();
            return;
        }

        if (bio.length() > 255) {
            Toast.makeText(this, "Tiểu sử vượt quá 255 ký tự (" + bio.length() + "/255)!", Toast.LENGTH_SHORT).show();
            if (edtBio != null) edtBio.requestFocus();
            return;
        }

        SharedPreferences.Editor localPrefEditor = getSharedPreferences("DishcisionPrefs", MODE_PRIVATE).edit();
        if (!city.isEmpty()) {
            localPrefEditor.putString("user_city", city);
        } else {
            localPrefEditor.remove("user_city");
        }
        localPrefEditor.apply();

        isSaving = true;
        if (btnSave != null) {
            btnSave.setEnabled(false);
            btnSave.setText("Đang lưu...");
        }

        Map<String, Object> updatePayload = new HashMap<>();
        updatePayload.put("fullName", name);
        updatePayload.put("username", username);
        updatePayload.put("bio", bio);
        if (!currentAvatarUrl.isEmpty()) {
            updatePayload.put("avatarUrl", currentAvatarUrl);
        }
        Set<String> currentPrefs = getSharedPreferences("DishcisionPrefs", MODE_PRIVATE).getStringSet("user_taste_preferences", null);
        if (currentPrefs != null) {
            updatePayload.put("preferences", new ArrayList<>(currentPrefs));
        }

        String authHeader = ApiClient.getAuthHeader(this);
        ApiClient.getApiService().updateMyProfile(authHeader, updatePayload).enqueue(new Callback<AuthResponse>() {
            @Override
            public void onResponse(Call<AuthResponse> call, Response<AuthResponse> response) {
                isSaving = false;
                if (btnSave != null) {
                    btnSave.setEnabled(true);
                    btnSave.setText("Lưu");
                }

                if (response.isSuccessful()) {
                    SharedPreferences.Editor editor = getSharedPreferences("DishcisionPrefs", MODE_PRIVATE).edit();
                    editor.putString("user_name", name);
                    editor.putString("user_username", username);
                    if (!currentAvatarUrl.isEmpty()) {
                        editor.putString("avatar_url", currentAvatarUrl);
                    }
                    editor.apply();
                    tokenManager.saveAuthData(null, 0, null, name);
                    Toast.makeText(EditProfileActivity.this, "Đã lưu thay đổi hồ sơ thành công!", Toast.LENGTH_SHORT).show();
                    setResult(RESULT_OK);
                    finish();
                } else if (response.code() == 401) {
                    new AlertDialog.Builder(EditProfileActivity.this)
                            .setTitle("Phiên đăng nhập hết hạn")
                            .setMessage("Phiên đăng nhập của bạn đã hết hạn (401: Token expired). Vui lòng đăng nhập lại tài khoản để lưu thay đổi.")
                            .setPositiveButton("Đăng nhập lại", (dialog, which) -> {
                                tokenManager.clearAuth();
                                Intent intent = new Intent(EditProfileActivity.this, LoginActivity.class);
                                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                startActivity(intent);
                                finish();
                            })
                            .setNegativeButton("Đóng", (dialog, which) -> dialog.dismiss())
                            .show();
                } else {
                    String errorMsg = "Mã lỗi: " + response.code();
                    try {
                        if (response.errorBody() != null) {
                            String raw = response.errorBody().string();
                            com.google.gson.JsonObject obj = com.google.gson.JsonParser.parseString(raw).getAsJsonObject();
                            if (obj.has("message")) {
                                errorMsg = obj.get("message").getAsString();
                            } else {
                                errorMsg = raw;
                            }
                        }
                    } catch (Exception ignored) {}
                    Toast.makeText(EditProfileActivity.this, "Lưu thất bại: " + errorMsg, Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<AuthResponse> call, Throwable t) {
                isSaving = false;
                if (btnSave != null) {
                    btnSave.setEnabled(true);
                    btnSave.setText("Lưu");
                }
                Toast.makeText(EditProfileActivity.this, "Lỗi kết nối máy chủ: " + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }
}
