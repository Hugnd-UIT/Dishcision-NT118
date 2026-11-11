package com.example.dishcision;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
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
import com.example.dishcision.network.AuthHelper;
import com.example.dishcision.network.ImageLoader;
import com.example.dishcision.network.ImageOrientationHelper;
import com.example.dishcision.network.ProfileSyncHelper;
import com.example.dishcision.network.TokenManager;
import com.example.dishcision.network.UploadResponse;
import com.example.dishcision.network.UserProfileResponse;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashSet;
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

    private ProfileSyncHelper.ProfileState baselineState =
            new ProfileSyncHelper.ProfileState("", "", "", "", "", null);
    private final Set<String> workingPreferences = new LinkedHashSet<>();
    private String currentAvatarUrl = "";

    private boolean isPopulatingFields = false;
    private boolean isNameEdited = false;
    private boolean isUsernameEdited = false;
    private boolean isBioEdited = false;
    private boolean isCityEdited = false;
    private boolean isAvatarEdited = false;
    private boolean isPreferencesEdited = false;
    private boolean skipFirstResumeFetch = true;

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
        if (skipFirstResumeFetch) {
            skipFirstResumeFetch = false;
            return;
        }
        if (!isSaving && !isUploadingAvatar) {
            fetchExistingProfile();
        }
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
        SharedPreferences prefs = getSharedPreferences("DishcisionPrefs", MODE_PRIVATE);

        String fullName = intent.hasExtra("FULL_NAME")
                ? intent.getStringExtra("FULL_NAME")
                : prefs.getString("user_name", tokenManager.getUserName());
        String username = intent.hasExtra("USERNAME")
                ? intent.getStringExtra("USERNAME")
                : prefs.getString("user_username", "");
        String bio = intent.hasExtra("BIO")
                ? intent.getStringExtra("BIO")
                : prefs.getString("user_bio", "");
        String avatarUrl = intent.hasExtra("AVATAR_URL")
                ? intent.getStringExtra("AVATAR_URL")
                : prefs.getString("avatar_url", "");
        String city = intent.hasExtra("CITY")
                ? intent.getStringExtra("CITY")
                : prefs.getString("user_city", "");
        Set<String> savedPrefs = prefs.getStringSet("user_taste_preferences", null);

        baselineState = new ProfileSyncHelper.ProfileState(fullName, username, bio, city, avatarUrl, savedPrefs);
        currentAvatarUrl = baselineState.avatarUrl;
        workingPreferences.clear();
        workingPreferences.addAll(baselineState.preferences);

        applyStateToUi(baselineState);
        fetchExistingProfile();
    }

    private void applyStateToUi(ProfileSyncHelper.ProfileState state) {
        isPopulatingFields = true;
        try {
            if (!isNameEdited && edtDisplayName != null) {
                edtDisplayName.setText(state.fullName);
            }
            if (!isUsernameEdited && edtUsername != null) {
                edtUsername.setText(state.username);
                updateProfileUrlPreview(state.username);
            }
            if (!isBioEdited && edtBio != null) {
                edtBio.setText(state.bio);
                updateBioCount(state.bio.length());
            }
            if (!isCityEdited && edtCity != null) {
                edtCity.setText(state.city);
            }
            if (!isAvatarEdited) {
                currentAvatarUrl = state.avatarUrl;
                if (imgAvatar != null) {
                    ImageLoader.loadAvatar(imgAvatar, currentAvatarUrl);
                }
            }
            if (!isPreferencesEdited) {
                workingPreferences.clear();
                workingPreferences.addAll(state.preferences);
                renderPreferences();
            }
        } finally {
            isPopulatingFields = false;
        }
    }

    private void updateProfileUrlPreview(String rawUsername) {
        String clean = ProfileSyncHelper.normalizeUsername(rawUsername);
        if (tvProfileUrl != null) {
            tvProfileUrl.setText("dishcision.com/@" + (clean.isEmpty() ? "user" : clean));
        }
    }

    private void fetchExistingProfile() {
        String authHeader = ApiClient.getAuthHeader(this);
        if (authHeader.isEmpty()) return;
        ApiClient.getApiService().getMyProfile(authHeader).enqueue(new Callback<UserProfileResponse>() {
            @Override
            public void onResponse(Call<UserProfileResponse> call, Response<UserProfileResponse> response) {
                if (isFinishing() || isDestroyed()) return;
                if (response.isSuccessful() && response.body() != null && response.body().getData() != null) {
                    UserProfileResponse.UserData user = response.body().getData();
                    ProfileSyncHelper.ProfileState serverState = ProfileSyncHelper.fromUserData(user);
                    baselineState = serverState;
                    persistUserToPrefs(user, serverState);
                    applyStateToUi(serverState);
                } else if (response.code() == 401) {
                    AuthHelper.handleUnauthorized(EditProfileActivity.this, null);
                }
            }

            @Override
            public void onFailure(Call<UserProfileResponse> call, Throwable t) {}
        });
    }

    private void renderPreferences() {
        if (layoutPreferenceChipsContainer == null) return;
        layoutPreferenceChipsContainer.removeAllViews();

        List<String> list = new ArrayList<>(workingPreferences);

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
        if (workingPreferences.remove(tasteName)) {
            isPreferencesEdited = true;
            renderPreferences();
            Toast.makeText(this, "Đã xóa khẩu vị: " + tasteName + " (nhấn Lưu để áp dụng)", Toast.LENGTH_SHORT).show();
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

        if (edtDisplayName != null) {
            edtDisplayName.addTextChangedListener(new TextWatcher() {
                @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
                @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}
                @Override public void afterTextChanged(Editable s) {
                    if (!isPopulatingFields) isNameEdited = true;
                }
            });
        }

        if (edtUsername != null) {
            edtUsername.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    updateProfileUrlPreview(s != null ? s.toString() : "");
                }

                @Override
                public void afterTextChanged(Editable s) {
                    if (!isPopulatingFields) isUsernameEdited = true;
                }
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
                public void afterTextChanged(Editable s) {
                    if (!isPopulatingFields) isBioEdited = true;
                }
            });
        }

        if (edtCity != null) {
            edtCity.addTextChangedListener(new TextWatcher() {
                @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
                @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}
                @Override public void afterTextChanged(Editable s) {
                    if (!isPopulatingFields) isCityEdited = true;
                }
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
                        addCustomTastePreference(taste.startsWith("✨") ? taste : "✨ " + taste);
                    }
                })
                .setNegativeButton("Hủy", null)
                .show();
    }

    private void addCustomTastePreference(String tasteName) {
        if (workingPreferences.add(tasteName)) {
            isPreferencesEdited = true;
            renderPreferences();
            Toast.makeText(this, "Đã thêm khẩu vị: " + tasteName + " (nhấn Lưu để áp dụng)", Toast.LENGTH_SHORT).show();
        }
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

    private ProfileSyncHelper.ProfileState readCurrentStateFromForm() {
        String name = edtDisplayName != null ? edtDisplayName.getText().toString() : "";
        String username = edtUsername != null ? edtUsername.getText().toString() : "";
        String bio = edtBio != null ? edtBio.getText().toString() : "";
        String city = edtCity != null ? edtCity.getText().toString() : "";
        return new ProfileSyncHelper.ProfileState(name, username, bio, city, currentAvatarUrl, workingPreferences);
    }

    private boolean isFormModified() {
        return !ProfileSyncHelper.buildProfileDiffPayload(baselineState, readCurrentStateFromForm()).isEmpty();
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
        isUploadingAvatar = true;
        if (btnChangeAvatar != null) {
            btnChangeAvatar.setText("Đang tải ảnh...");
        }

        String authHeader = ApiClient.getAuthHeader(this);
        ImageOrientationHelper.prepareUploadAsync(
                this,
                imageUri,
                "avatar_",
                ImageOrientationHelper.DEFAULT_AVATAR_MAX_DIM,
                new ImageOrientationHelper.PrepareUploadCallback() {
                    @Override
                    public void onReady(ImageOrientationHelper.PreparedImage prepared, Bitmap previewBitmap) {
                        if (isFinishing() || isDestroyed()) {
                            ImageOrientationHelper.deleteQuietly(prepared.file);
                            return;
                        }
                        if (imgAvatar != null && previewBitmap != null) {
                            Bitmap circular = ImageLoader.getCircularBitmap(previewBitmap);
                            imgAvatar.setImageBitmap(circular != null ? circular : previewBitmap);
                        }

                        RequestBody requestFile = RequestBody.create(
                                MediaType.parse(prepared.mimeType), prepared.file
                        );
                        MultipartBody.Part filePart = MultipartBody.Part.createFormData(
                                "file", prepared.file.getName(), requestFile
                        );

                        ApiClient.getApiService().uploadSingleImage(authHeader, filePart).enqueue(new Callback<UploadResponse>() {
                            @Override
                            public void onResponse(Call<UploadResponse> call, Response<UploadResponse> response) {
                                ImageOrientationHelper.deleteQuietly(prepared.file);
                                isUploadingAvatar = false;
                                if (btnChangeAvatar != null) {
                                    btnChangeAvatar.setText("Đổi ảnh đại diện");
                                }

                                if (response.isSuccessful() && response.body() != null && response.body().data != null) {
                                    String rawUrl = (response.body().data.url != null && !response.body().data.url.isEmpty())
                                            ? response.body().data.url
                                            : response.body().data.fullUrl;
                                    currentAvatarUrl = rawUrl != null ? rawUrl.trim() : "";
                                    isAvatarEdited = !currentAvatarUrl.equals(baselineState.avatarUrl);
                                    Toast.makeText(EditProfileActivity.this, "Tải ảnh đại diện lên thành công!", Toast.LENGTH_SHORT).show();
                                } else if (response.code() == 401) {
                                    AuthHelper.handleUnauthorized(EditProfileActivity.this, null);
                                } else {
                                    Toast.makeText(EditProfileActivity.this, "Không thể tải ảnh: Mã phản hồi " + response.code(), Toast.LENGTH_SHORT).show();
                                }
                            }

                            @Override
                            public void onFailure(Call<UploadResponse> call, Throwable t) {
                                ImageOrientationHelper.deleteQuietly(prepared.file);
                                isUploadingAvatar = false;
                                if (btnChangeAvatar != null) {
                                    btnChangeAvatar.setText("Đổi ảnh đại diện");
                                }
                                Toast.makeText(EditProfileActivity.this, "Lỗi kết nối tải ảnh: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        });
                    }

                    @Override
                    public void onError(Exception error) {
                        isUploadingAvatar = false;
                        if (btnChangeAvatar != null) {
                            btnChangeAvatar.setText("Đổi ảnh đại diện");
                        }
                        Toast.makeText(EditProfileActivity.this, "Không thể xử lý ảnh: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                }
        );
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

        if (city.length() > 100) {
            Toast.makeText(this, "Thành phố tối đa 100 ký tự!", Toast.LENGTH_SHORT).show();
            if (edtCity != null) edtCity.requestFocus();
            return;
        }

        ProfileSyncHelper.ProfileState currentState = readCurrentStateFromForm();
        Map<String, Object> updatePayload = ProfileSyncHelper.buildProfileDiffPayload(baselineState, currentState);
        if (updatePayload.isEmpty()) {
            Toast.makeText(this, "Không có thay đổi mới cần lưu.", Toast.LENGTH_SHORT).show();
            setResult(RESULT_OK);
            finish();
            return;
        }

        isSaving = true;
        if (btnSave != null) {
            btnSave.setEnabled(false);
            btnSave.setText("Đang lưu...");
        }

        String authHeader = ApiClient.getAuthHeader(this);
        ApiClient.getApiService().updateMyProfile(authHeader, updatePayload).enqueue(new Callback<UserProfileResponse>() {
            @Override
            public void onResponse(Call<UserProfileResponse> call, Response<UserProfileResponse> response) {
                isSaving = false;
                if (btnSave != null) {
                    btnSave.setEnabled(true);
                    btnSave.setText("Lưu");
                }

                if (response.isSuccessful()) {
                    UserProfileResponse.UserData savedUser = response.body() != null ? response.body().getData() : null;
                    ProfileSyncHelper.ProfileState authoritativeState = savedUser != null
                            ? ProfileSyncHelper.fromUserData(savedUser)
                            : currentState;
                    baselineState = authoritativeState;
                    persistUserToPrefs(savedUser, authoritativeState);
                    Toast.makeText(EditProfileActivity.this, "Đã lưu thay đổi hồ sơ thành công!", Toast.LENGTH_SHORT).show();
                    setResult(RESULT_OK);
                    finish();
                } else if (response.code() == 401) {
                    AuthHelper.handleUnauthorized(EditProfileActivity.this, null);
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
            public void onFailure(Call<UserProfileResponse> call, Throwable t) {
                isSaving = false;
                if (btnSave != null) {
                    btnSave.setEnabled(true);
                    btnSave.setText("Lưu");
                }
                Toast.makeText(EditProfileActivity.this, "Lỗi kết nối máy chủ: " + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    private void persistUserToPrefs(UserProfileResponse.UserData savedUser, ProfileSyncHelper.ProfileState state) {
        SharedPreferences.Editor editor = getSharedPreferences("DishcisionPrefs", MODE_PRIVATE).edit();
        if (!state.fullName.isEmpty()) editor.putString("user_name", state.fullName);
        if (!state.username.isEmpty()) editor.putString("user_username", state.username);
        if (!state.bio.isEmpty()) editor.putString("user_bio", state.bio);
        else editor.remove("user_bio");
        if (!state.city.isEmpty()) editor.putString("user_city", state.city);
        else editor.remove("user_city");
        if (!state.avatarUrl.isEmpty()) editor.putString("avatar_url", state.avatarUrl);
        else editor.remove("avatar_url");
        editor.putStringSet("user_taste_preferences", new HashSet<>(state.preferences));

        int uid = savedUser != null ? savedUser.userId : AuthHelper.getCurrentUserId(this);
        if (uid > 0) {
            editor.putInt("user_id", uid);
            if (savedUser != null && savedUser.allergies != null) {
                editor.putStringSet("user_allergies_" + uid, new HashSet<>(savedUser.allergies));
            }
        }
        if (savedUser != null) {
            if (savedUser.email != null && !savedUser.email.isEmpty()) {
                editor.putString("user_email", savedUser.email);
            }
            editor.putBoolean("is_google_login", savedUser.isGoogleAccount);
        }
        editor.apply();

        String email = savedUser != null ? savedUser.email : null;
        tokenManager.saveAuthData(null, uid, email, state.fullName);
    }
}
