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

    private static final int MAX_STORY_IMAGES = 5;
    private ActivityResultLauncher<String> imagePickerLauncher;
    private final java.util.List<Uri> selectedUris = new java.util.ArrayList<>();
    private final java.util.List<String> uploadedUrls = new java.util.ArrayList<>();
    private boolean isSubmitting = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_create_story);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main_create_story), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, Math.max(systemBars.bottom, insets.getInsets(WindowInsetsCompat.Type.ime()).bottom));
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
                new ActivityResultContracts.GetMultipleContents(),
                uris -> {
                    if (uris == null || uris.isEmpty()) return;
                    selectedUris.clear();
                    for (Uri u : uris) {
                        if (selectedUris.size() >= MAX_STORY_IMAGES) break;
                        selectedUris.add(u);
                    }
                    if (uris.size() > MAX_STORY_IMAGES) {
                        Toast.makeText(this, "Mỗi story tối đa " + MAX_STORY_IMAGES + " ảnh, đã lấy " + MAX_STORY_IMAGES + " ảnh đầu", Toast.LENGTH_SHORT).show();
                    }
                    if (imgStoryPreview != null) {
                        ImageOrientationHelper.loadLocalUri(this, imgStoryPreview, selectedUris.get(0), 1200);
                    }
                    if (layoutEmptyPreview != null) {
                        layoutEmptyPreview.setVisibility(View.GONE);
                    }
                    if (btnPickPhoto != null) {
                        btnPickPhoto.setText(selectedUris.size() > 1 ? "Đổi ảnh (" + selectedUris.size() + ")" : "Đổi ảnh");
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

        if (selectedUris.isEmpty()) {
            Toast.makeText(this, "Vui lòng chọn ảnh cho story", Toast.LENGTH_SHORT).show();
            return;
        }

        String caption = edtStoryCaption != null ? edtStoryCaption.getText().toString().trim() : "";
        uploadedUrls.clear();
        uploadAt(0, authHeader, caption);
    }

    private void uploadAt(int index, String authHeader, String caption) {
        final int total = selectedUris.size();
        setSubmitting(true, "Đang tải ảnh " + (index + 1) + "/" + total + "...");
        final Uri targetUri = selectedUris.get(index);
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
                                        uploadedUrls.add(uploadedUrl);
                                        if (index + 1 < total) {
                                            uploadAt(index + 1, authHeader, caption);
                                        } else {
                                            // Đăng từ ảnh cuối về ảnh đầu để ảnh đầu hiện đầu tiên trong trình xem story.
                                            createStoryRecord(authHeader, total - 1, caption, 0);
                                        }
                                        return;
                                    }
                                }
                                setSubmitting(false, "Đăng Story");
                                Toast.makeText(CreateStoryActivity.this, "Tải ảnh " + (index + 1) + " thất bại (mã " + response.code() + ")", Toast.LENGTH_SHORT).show();
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

    private void createStoryRecord(String authHeader, int urlIndex, String caption, int postedCount) {
        final int total = uploadedUrls.size();
        setSubmitting(true, "Đang đăng story " + (postedCount + 1) + "/" + total + "...");
        ApiClient.getApiService().createStory(authHeader, new CreateStoryRequest(uploadedUrls.get(urlIndex), caption))
                .enqueue(new Callback<CreateStoryResponse>() {
                    @Override
                    public void onResponse(@NonNull Call<CreateStoryResponse> call, @NonNull Response<CreateStoryResponse> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            if (urlIndex > 0) {
                                createStoryRecord(authHeader, urlIndex - 1, caption, postedCount + 1);
                                return;
                            }
                            setSubmitting(false, "Đăng Story");
                            Toast.makeText(CreateStoryActivity.this, "Đăng story thành công", Toast.LENGTH_SHORT).show();
                            setResult(RESULT_OK);
                            finish();
                        } else {
                            finishWithPartial(postedCount, total, "mã " + response.code());
                        }
                    }

                    @Override
                    public void onFailure(@NonNull Call<CreateStoryResponse> call, @NonNull Throwable t) {
                        finishWithPartial(postedCount, total, t.getMessage());
                    }
                });
    }

    private void finishWithPartial(int posted, int total, String reason) {
        setSubmitting(false, "Đăng Story");
        if (posted > 0) {
            Toast.makeText(this, "Mới đăng được " + posted + "/" + total + " ảnh (" + reason + ")", Toast.LENGTH_LONG).show();
            setResult(RESULT_OK);
            finish();
        } else {
            Toast.makeText(this, "Đăng story thất bại (" + reason + ")", Toast.LENGTH_SHORT).show();
        }
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
