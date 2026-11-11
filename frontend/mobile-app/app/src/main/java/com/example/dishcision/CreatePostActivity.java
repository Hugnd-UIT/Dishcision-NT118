package com.example.dishcision;

import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.dishcision.network.ApiClient;
import com.example.dishcision.network.CreatePostRequest;
import com.example.dishcision.network.CreatePostResponse;
import com.example.dishcision.network.ImageLoader;
import com.example.dishcision.network.ImageOrientationHelper;
import com.example.dishcision.network.TokenManager;
import com.example.dishcision.network.UploadResponse;
import com.example.dishcision.network.UserProfileResponse;

import java.util.ArrayList;
import java.util.List;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CreatePostActivity extends AppCompatActivity {

    private TextView btnCancel;
    private View btnPublish;
    private TextView tvBtnPublishText;
    private EditText edtPostContent;

    private TextView tvAuthorName;
    private TextView tvAuthorHandle;
    private ImageView imgAuthorAvatar;

    private TextView tvImageCount;
    private View btnAddImage;
    private View containerImg1;
    private View containerImg2;
    private ImageView imgPreview1;
    private ImageView imgPreview2;
    private ImageView btnRemoveImg1;
    private ImageView btnRemoveImg2;

    private EditText edtLocationUrl;
    private EditText edtVoucherCode;
    private EditText edtHashtags;

    private final Uri[] slotUris = new Uri[2];
    private final Bitmap[] slotPreviews = new Bitmap[2];
    private final String[] slotUrls = new String[2];
    private final boolean[] slotUploading = new boolean[2];
    private final long[] slotTokens = new long[2];
    private long nextUploadToken = 1L;
    private ActivityResultLauncher<String> imagePickerLauncher;
    private boolean isPublishing = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_create_post);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main_create_post), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        initViews();
        setupImagePicker();
        setupListeners();
        loadUserProfile();
    }

    private void initViews() {
        btnCancel = findViewById(R.id.btnCancel);
        btnPublish = findViewById(R.id.btnPublish);
        tvBtnPublishText = findViewById(R.id.tvBtnPublishText);
        edtPostContent = findViewById(R.id.edtPostContent);

        tvAuthorName = findViewById(R.id.tvAuthorName);
        tvAuthorHandle = findViewById(R.id.tvAuthorHandle);
        imgAuthorAvatar = findViewById(R.id.imgAuthorAvatar);

        tvImageCount = findViewById(R.id.tvImageCount);
        btnAddImage = findViewById(R.id.btnAddImage);
        containerImg1 = findViewById(R.id.containerImg1);
        containerImg2 = findViewById(R.id.containerImg2);
        imgPreview1 = findViewById(R.id.imgPreview1);
        imgPreview2 = findViewById(R.id.imgPreview2);
        btnRemoveImg1 = findViewById(R.id.btnRemoveImg1);
        btnRemoveImg2 = findViewById(R.id.btnRemoveImg2);

        edtLocationUrl = findViewById(R.id.edtLocationUrl);
        edtVoucherCode = findViewById(R.id.edtVoucherCode);
        edtHashtags = findViewById(R.id.edtHashtags);
    }

    private void setupImagePicker() {
        imagePickerLauncher = registerForActivityResult(
                new ActivityResultContracts.GetContent(),
                uri -> {
                    if (uri != null) {
                        int slot = findFirstEmptySlot();
                        if (slot == -1) {
                            Toast.makeText(this, "Tối đa 2 hình ảnh cho mỗi bài viết!", Toast.LENGTH_SHORT).show();
                            return;
                        }
                        uploadImageUri(uri, slot);
                    }
                }
        );
    }

    private int findFirstEmptySlot() {
        for (int i = 0; i < 2; i++) {
            if (slotUris[i] == null && slotUrls[i] == null) {
                return i;
            }
        }
        return -1;
    }

    private int getActiveSlotCount() {
        int count = 0;
        for (int i = 0; i < 2; i++) {
            if (slotUris[i] != null || slotUrls[i] != null) {
                count++;
            }
        }
        return count;
    }

    private void removeSlot(int index) {
        if (index == 0) {
            slotUris[0] = slotUris[1];
            slotPreviews[0] = slotPreviews[1];
            slotUrls[0] = slotUrls[1];
            slotUploading[0] = slotUploading[1];
            slotTokens[0] = slotTokens[1];
            slotUris[1] = null;
            slotPreviews[1] = null;
            slotUrls[1] = null;
            slotUploading[1] = false;
            slotTokens[1] = 0L;
        } else if (index == 1) {
            slotUris[1] = null;
            slotPreviews[1] = null;
            slotUrls[1] = null;
            slotUploading[1] = false;
            slotTokens[1] = 0L;
        }
        updateImagePreviewUI();
    }

    private void setupListeners() {
        if (btnCancel != null) {
            btnCancel.setOnClickListener(v -> finish());
        }

        if (btnPublish != null) {
            btnPublish.setOnClickListener(v -> performPublishPost());
        }

        if (btnAddImage != null) {
            btnAddImage.setOnClickListener(v -> {
                if (getActiveSlotCount() >= 2) {
                    Toast.makeText(this, "Tối đa 2 hình ảnh cho mỗi bài viết!", Toast.LENGTH_SHORT).show();
                    return;
                }
                imagePickerLauncher.launch("image/*");
            });
        }

        if (btnRemoveImg1 != null) {
            btnRemoveImg1.setOnClickListener(v -> removeSlot(0));
        }

        if (btnRemoveImg2 != null) {
            btnRemoveImg2.setOnClickListener(v -> removeSlot(1));
        }
    }

    private void loadUserProfile() {
        TokenManager tokenManager = new TokenManager(this);
        if (tvAuthorName != null && !tokenManager.getUserName().isEmpty()) {
            tvAuthorName.setText(tokenManager.getUserName());
        }
        if (tvAuthorHandle != null && !tokenManager.getUserEmail().isEmpty()) {
            tvAuthorHandle.setText(tokenManager.getUserEmail());
        }

        String authHeader = ApiClient.getAuthHeader(this);
        if (authHeader.isEmpty()) return;

        ApiClient.getApiService().getMyProfile(authHeader).enqueue(new Callback<UserProfileResponse>() {
            @Override
            public void onResponse(Call<UserProfileResponse> call, Response<UserProfileResponse> response) {
                if (response.isSuccessful() && response.body() != null && response.body().getData() != null) {
                    UserProfileResponse.UserData user = response.body().getData();
                    if (tvAuthorName != null && user.fullName != null && !user.fullName.isEmpty()) {
                        tvAuthorName.setText(user.fullName);
                    }
                    if (tvAuthorHandle != null && user.username != null && !user.username.isEmpty()) {
                        tvAuthorHandle.setText(user.username.startsWith("@") ? user.username : "@" + user.username);
                    }
                    if (imgAuthorAvatar != null && user.avatarUrl != null && !user.avatarUrl.isEmpty()) {
                        ImageLoader.loadAvatar(imgAuthorAvatar, user.avatarUrl);
                    }
                }
            }

            @Override
            public void onFailure(Call<UserProfileResponse> call, Throwable t) {}
        });
    }

    private int findSlotByToken(long token) {
        if (token == 0L) return -1;
        for (int i = 0; i < 2; i++) {
            if (slotTokens[i] == token) {
                return i;
            }
        }
        return -1;
    }

    private void uploadImageUri(Uri uri, int slot) {
        String authHeader = ApiClient.getAuthHeader(this);
        if (authHeader.isEmpty()) {
            Toast.makeText(this, "Vui lòng đăng nhập lại trước khi tải ảnh!", Toast.LENGTH_SHORT).show();
            return;
        }

        final long uploadToken = nextUploadToken++;
        slotUris[slot] = uri;
        slotPreviews[slot] = null;
        slotUrls[slot] = null;
        slotUploading[slot] = true;
        slotTokens[slot] = uploadToken;
        updateImagePreviewUI();
        Toast.makeText(this, "Đang tải ảnh lên...", Toast.LENGTH_SHORT).show();

        ImageOrientationHelper.prepareUploadAsync(
                this,
                uri,
                "upload_post_",
                800,
                new ImageOrientationHelper.PrepareUploadCallback() {
                    @Override
                    public void onReady(ImageOrientationHelper.PreparedImage prepared, Bitmap previewBitmap) {
                        int targetSlot = findSlotByToken(uploadToken);
                        if (targetSlot == -1 || isFinishing() || isDestroyed()) {
                            ImageOrientationHelper.deleteQuietly(prepared.file);
                            return;
                        }

                        if (previewBitmap != null) {
                            slotPreviews[targetSlot] = previewBitmap;
                            updateImagePreviewUI();
                        }

                        RequestBody requestFile = RequestBody.create(
                                MediaType.parse(prepared.mimeType), prepared.file
                        );
                        MultipartBody.Part filePart = MultipartBody.Part.createFormData(
                                "file", prepared.file.getName(), requestFile
                        );

                        ApiClient.getApiService().uploadMedia(authHeader, filePart).enqueue(new Callback<UploadResponse>() {
                            @Override
                            public void onResponse(Call<UploadResponse> call, Response<UploadResponse> response) {
                                ImageOrientationHelper.deleteQuietly(prepared.file);
                                int activeSlot = findSlotByToken(uploadToken);
                                if (activeSlot == -1) return;

                                slotUploading[activeSlot] = false;
                                if (response.isSuccessful() && response.body() != null && response.body().data != null) {
                                    String rawUrl = (response.body().data.url != null && !response.body().data.url.isEmpty())
                                            ? response.body().data.url
                                            : response.body().data.fullUrl;
                                    String storedUrl = rawUrl != null ? rawUrl.trim() : "";
                                    if (!storedUrl.isEmpty()) {
                                        slotUrls[activeSlot] = storedUrl;
                                        updateImagePreviewUI();
                                        Toast.makeText(CreatePostActivity.this, "Tải ảnh lên thành công!", Toast.LENGTH_SHORT).show();
                                        return;
                                    }
                                }
                                slotUris[activeSlot] = null;
                                slotPreviews[activeSlot] = null;
                                slotUrls[activeSlot] = null;
                                slotTokens[activeSlot] = 0L;
                                updateImagePreviewUI();
                                Toast.makeText(CreatePostActivity.this, "Tải ảnh lên thất bại!", Toast.LENGTH_SHORT).show();
                            }

                            @Override
                            public void onFailure(Call<UploadResponse> call, Throwable t) {
                                ImageOrientationHelper.deleteQuietly(prepared.file);
                                int activeSlot = findSlotByToken(uploadToken);
                                if (activeSlot != -1) {
                                    slotUploading[activeSlot] = false;
                                    slotUris[activeSlot] = null;
                                    slotPreviews[activeSlot] = null;
                                    slotUrls[activeSlot] = null;
                                    slotTokens[activeSlot] = 0L;
                                    updateImagePreviewUI();
                                }
                                Toast.makeText(CreatePostActivity.this, "Lỗi kết nối khi tải ảnh: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        });
                    }

                    @Override
                    public void onError(Exception error) {
                        int activeSlot = findSlotByToken(uploadToken);
                        if (activeSlot != -1) {
                            slotUploading[activeSlot] = false;
                            slotUris[activeSlot] = null;
                            slotPreviews[activeSlot] = null;
                            slotUrls[activeSlot] = null;
                            slotTokens[activeSlot] = 0L;
                            updateImagePreviewUI();
                        }
                        Toast.makeText(CreatePostActivity.this, "Không thể đọc hình ảnh: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                }
        );
    }

    private void updateImagePreviewUI() {
        int count = getActiveSlotCount();
        if (tvImageCount != null) {
            tvImageCount.setText("Hình ảnh (" + count + "/2)");
        }

        if (slotPreviews[0] != null) {
            if (containerImg1 != null) containerImg1.setVisibility(View.VISIBLE);
            if (imgPreview1 != null) imgPreview1.setImageBitmap(slotPreviews[0]);
        } else if (slotUrls[0] != null) {
            if (containerImg1 != null) containerImg1.setVisibility(View.VISIBLE);
            if (imgPreview1 != null) ImageLoader.loadImage(imgPreview1, slotUrls[0]);
        } else if (slotUris[0] != null) {
            if (containerImg1 != null) containerImg1.setVisibility(View.VISIBLE);
            if (imgPreview1 != null) imgPreview1.setImageDrawable(null);
        } else {
            if (containerImg1 != null) containerImg1.setVisibility(View.GONE);
        }

        if (slotPreviews[1] != null) {
            if (containerImg2 != null) containerImg2.setVisibility(View.VISIBLE);
            if (imgPreview2 != null) imgPreview2.setImageBitmap(slotPreviews[1]);
        } else if (slotUrls[1] != null) {
            if (containerImg2 != null) containerImg2.setVisibility(View.VISIBLE);
            if (imgPreview2 != null) ImageLoader.loadImage(imgPreview2, slotUrls[1]);
        } else if (slotUris[1] != null) {
            if (containerImg2 != null) containerImg2.setVisibility(View.VISIBLE);
            if (imgPreview2 != null) imgPreview2.setImageDrawable(null);
        } else {
            if (containerImg2 != null) containerImg2.setVisibility(View.GONE);
        }

        if (btnAddImage != null) {
            btnAddImage.setVisibility(count >= 2 ? View.GONE : View.VISIBLE);
        }
    }

    private void performPublishPost() {
        if (isPublishing) return;

        String authHeader = ApiClient.getAuthHeader(this);
        if (authHeader.isEmpty()) {
            Toast.makeText(this, "Vui lòng đăng nhập lại để đăng bài viết!", Toast.LENGTH_SHORT).show();
            return;
        }

        if (slotUploading[0] || slotUploading[1]) {
            Toast.makeText(this, "Ảnh đang tải lên, vui lòng đợi xong rồi hãy đăng!", Toast.LENGTH_SHORT).show();
            return;
        }

        String content = edtPostContent != null ? edtPostContent.getText().toString().trim() : "";
        if (content.isEmpty()) {
            Toast.makeText(this, "Vui lòng nhập nội dung bài viết!", Toast.LENGTH_SHORT).show();
            if (edtPostContent != null) edtPostContent.requestFocus();
            return;
        }

        String locationInput = edtLocationUrl != null ? edtLocationUrl.getText().toString().trim() : "";
        String locationUrl = PostFeedHelper.normalizeLocationUrl(locationInput);
        if (locationUrl != null && locationUrl.isEmpty()) {
            locationUrl = null;
        }

        String voucherInput = edtVoucherCode != null ? edtVoucherCode.getText().toString().trim() : "";
        String voucherCode = !voucherInput.isEmpty() ? voucherInput : null;

        String hashtagsInput = edtHashtags != null ? edtHashtags.getText().toString().trim() : "";
        List<String> hashtags = null;
        if (!hashtagsInput.isEmpty()) {
            hashtags = new ArrayList<>();
            String[] parts = hashtagsInput.split("[,\\s]+");
            for (String p : parts) {
                String clean = p.replace("#", "").trim();
                if (!clean.isEmpty()) {
                    hashtags.add(clean);
                }
            }
            if (hashtags.isEmpty()) hashtags = null;
        }

        List<CreatePostRequest.PostImageRequest> imageRequests = new ArrayList<>();
        int displayOrder = 0;
        for (int i = 0; i < 2; i++) {
            if (slotUrls[i] != null && !slotUrls[i].isEmpty()) {
                imageRequests.add(new CreatePostRequest.PostImageRequest(slotUrls[i], displayOrder++));
            }
        }
        if (imageRequests.isEmpty()) {
            imageRequests = null;
        }

        CreatePostRequest request = new CreatePostRequest(content, locationUrl, voucherCode, hashtags, imageRequests);

        isPublishing = true;
        if (btnPublish != null) btnPublish.setEnabled(false);
        if (tvBtnPublishText != null) tvBtnPublishText.setText("Đang đăng...");

        ApiClient.getApiService().createPost(authHeader, request).enqueue(new Callback<CreatePostResponse>() {
            @Override
            public void onResponse(Call<CreatePostResponse> call, Response<CreatePostResponse> response) {
                isPublishing = false;
                if (btnPublish != null) btnPublish.setEnabled(true);
                if (tvBtnPublishText != null) tvBtnPublishText.setText("Đăng");

                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    Toast.makeText(CreatePostActivity.this, "Đăng bài viết thành công!", Toast.LENGTH_SHORT).show();
                    setResult(RESULT_OK);
                    finish();
                } else {
                    String error = "Lỗi khi tạo bài viết (mã " + response.code() + ")";
                    Toast.makeText(CreatePostActivity.this, error, Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<CreatePostResponse> call, Throwable t) {
                isPublishing = false;
                if (btnPublish != null) btnPublish.setEnabled(true);
                if (tvBtnPublishText != null) tvBtnPublishText.setText("Đăng");
                Toast.makeText(CreatePostActivity.this, "Lỗi kết nối khi đăng bài: " + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }
}
