package com.example.dishcision;

import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.OpenableColumns;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.dishcision.network.ApiClient;
import com.example.dishcision.network.CreateStoryRequest;
import com.example.dishcision.network.CreateStoryResponse;
import com.example.dishcision.network.ImageOrientationHelper;
import com.example.dishcision.network.UploadResponse;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CreateStoryActivity extends AppCompatActivity {

    private ImageView btnBack;
    private TextView btnPickPhoto;
    private View layoutPreviewCanvas;
    private ImageView imgStoryPreview;
    private View layoutEmptyPreview;
    private EditText edtStoryCaption;
    private RelativeLayout btnShareStory;
    private TextView tvShareStoryLabel;

    private ActivityResultLauncher<String> imagePickerLauncher;
    private Uri selectedImageUri;
    private boolean isSubmitting = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_create_story);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main_create_story), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        ApiClient.init(this);
        initViews();
        setupImagePicker();
        setupListeners();
    }

    private void initViews() {
        btnBack = findViewById(R.id.btnBack);
        btnPickPhoto = findViewById(R.id.btnPickPhoto);
        layoutPreviewCanvas = findViewById(R.id.layoutPreviewCanvas);
        imgStoryPreview = findViewById(R.id.imgStoryPreview);
        layoutEmptyPreview = findViewById(R.id.layoutEmptyPreview);
        edtStoryCaption = findViewById(R.id.edtStoryCaption);
        btnShareStory = findViewById(R.id.btnShareStory);
        tvShareStoryLabel = findViewById(R.id.tvShareStoryLabel);
    }

    private void setupImagePicker() {
        imagePickerLauncher = registerForActivityResult(
                new ActivityResultContracts.GetContent(),
                uri -> {
                    if (uri != null) {
                        selectedImageUri = uri;
                        if (imgStoryPreview != null) {
                            ImageOrientationHelper.loadLocalUri(this, imgStoryPreview, uri, 1200);
                        }
                        if (layoutEmptyPreview != null) {
                            layoutEmptyPreview.setVisibility(View.GONE);
                        }
                        if (btnPickPhoto != null) {
                            btnPickPhoto.setText("Đổi ảnh");
                        }
                    }
                }
        );
    }

    private void setupListeners() {
        if (btnBack != null) {
            btnBack.setOnClickListener(v -> finish());
        }

        View.OnClickListener pickPhotoClick = v -> {
            if (!isSubmitting) {
                imagePickerLauncher.launch("image/*");
            }
        };
        if (btnPickPhoto != null) btnPickPhoto.setOnClickListener(pickPhotoClick);
        if (layoutPreviewCanvas != null) layoutPreviewCanvas.setOnClickListener(pickPhotoClick);

        if (btnShareStory != null) {
            btnShareStory.setOnClickListener(v -> publishStory());
        }
    }

    private void publishStory() {
        if (isSubmitting) return;

        String authHeader = ApiClient.getAuthHeader(this);
        if (authHeader.isEmpty()) {
            Toast.makeText(this, "Vui lòng đăng nhập để đăng story", Toast.LENGTH_SHORT).show();
            return;
        }

        if (selectedImageUri == null) {
            Toast.makeText(this, "Vui lòng chọn ảnh cho story", Toast.LENGTH_SHORT).show();
            return;
        }

        String caption = edtStoryCaption != null ? edtStoryCaption.getText().toString().trim() : "";

        setSubmitting(true, "Đang tải ảnh lên...");
        final Uri targetUri = selectedImageUri;
        final String displayName = resolveFileName(targetUri);

        ImageOrientationHelper.prepareUploadAsync(
                this,
                targetUri,
                "story_upload_",
                0,
                new ImageOrientationHelper.PrepareUploadCallback() {
                    @Override
                    public void onReady(ImageOrientationHelper.PreparedImage prepared, Bitmap previewBitmap) {
                        if (isFinishing() || isDestroyed()) {
                            ImageOrientationHelper.deleteQuietly(prepared.file);
                            return;
                        }

                        String fileName = ImageOrientationHelper.ensureExtensionMatchesMime(displayName, prepared.mimeType);
                        RequestBody requestFile = RequestBody.create(MediaType.parse(prepared.mimeType), prepared.file);
                        MultipartBody.Part filePart = MultipartBody.Part.createFormData("file", fileName, requestFile);

                        ApiClient.getApiService().uploadSingleImage(authHeader, filePart).enqueue(new Callback<UploadResponse>() {
                            @Override
                            public void onResponse(@NonNull Call<UploadResponse> call, @NonNull Response<UploadResponse> response) {
                                ImageOrientationHelper.deleteQuietly(prepared.file);
                                if (response.isSuccessful() && response.body() != null && response.body().data != null) {
                                    String uploadedUrl = !android.text.TextUtils.isEmpty(response.body().data.url)
                                            ? response.body().data.url
                                            : response.body().data.fullUrl;
                                    if (!android.text.TextUtils.isEmpty(uploadedUrl)) {
                                        createStoryRecord(authHeader, uploadedUrl, caption);
                                        return;
                                    }
                                }
                                setSubmitting(false, "Đăng Story");
                                Toast.makeText(CreateStoryActivity.this, "Tải ảnh thất bại (mã " + response.code() + ")", Toast.LENGTH_SHORT).show();
                            }

                            @Override
                            public void onFailure(@NonNull Call<UploadResponse> call, @NonNull Throwable t) {
                                ImageOrientationHelper.deleteQuietly(prepared.file);
                                setSubmitting(false, "Đăng Story");
                                Toast.makeText(CreateStoryActivity.this, "Lỗi tải ảnh: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        });
                    }

                    @Override
                    public void onError(Exception error) {
                        setSubmitting(false, "Đăng Story");
                        Toast.makeText(CreateStoryActivity.this, "Không thể xử lý ảnh đã chọn", Toast.LENGTH_SHORT).show();
                    }
                }
        );
    }

    private void createStoryRecord(String authHeader, String mediaUrl, String caption) {
        setSubmitting(true, "Đang đăng story...");
        ApiClient.getApiService().createStory(authHeader, new CreateStoryRequest(mediaUrl, caption))
                .enqueue(new Callback<CreateStoryResponse>() {
                    @Override
                    public void onResponse(@NonNull Call<CreateStoryResponse> call, @NonNull Response<CreateStoryResponse> response) {
                        setSubmitting(false, "Đăng Story");
                        if (response.isSuccessful() && response.body() != null) {
                            Toast.makeText(CreateStoryActivity.this, "Đăng story thành công", Toast.LENGTH_SHORT).show();
                            setResult(RESULT_OK);
                            finish();
                        } else {
                            Toast.makeText(CreateStoryActivity.this, "Đăng story thất bại (mã " + response.code() + ")", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(@NonNull Call<CreateStoryResponse> call, @NonNull Throwable t) {
                        setSubmitting(false, "Đăng Story");
                        Toast.makeText(CreateStoryActivity.this, "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void setSubmitting(boolean submitting, String label) {
        isSubmitting = submitting;
        if (btnShareStory != null) btnShareStory.setEnabled(!submitting);
        if (tvShareStoryLabel != null) tvShareStoryLabel.setText(label);
    }

    private String resolveFileName(Uri uri) {
        String result = null;
        try (Cursor cursor = getContentResolver().query(uri, null, null, null, null)) {
            if (cursor != null && cursor.moveToFirst()) {
                int idx = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
                if (idx >= 0) result = cursor.getString(idx);
            }
        } catch (Exception ignored) {}
        return (result != null && !result.trim().isEmpty()) ? result.trim() : "story.jpg";
    }
}
