package com.example.dishcision;

import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
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

import com.example.dishcision.network.ApiClient;
import com.example.dishcision.network.CreatePostResponse;
import com.example.dishcision.network.CurrentUserBinder;
import com.example.dishcision.network.ImageLoader;
import com.example.dishcision.network.UpdatePostRequest;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class EditPostActivity extends AppCompatActivity {

    private ImageView btnBack;
    private ImageView imgAuthorAvatar;
    private TextView tvAuthorName;
    private TextView tvAuthorHandle;
    private EditText edtPostText;
    private View sectionAttachedImages;
    private TextView tvAttachedImagesLabel;
    private View containerEditImg1;
    private View containerEditImg2;
    private ImageView imgEditPreview1;
    private ImageView imgEditPreview2;
    private TextView btnStatusActive;
    private TextView btnStatusHidden;
    private EditText edtLocationUrl;
    private EditText edtVoucherCode;
    private EditText edtHashtags;
    private RelativeLayout btnSaveChange;
    private TextView tvBtnSaveText;
    private RelativeLayout btnCancelEdit;

    private int postId = 0;
    private String selectedStatus = "Active";
    private boolean isSaving = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_edit_post);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main_edit_post), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        postId = getIntent().getIntExtra("POST_ID", 0);
        if (postId <= 0) {
            Toast.makeText(this, "Không tìm thấy bài viết cần chỉnh sửa", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        initViews();
        bindInitialData();
        setupListeners();
    }

    private void initViews() {
        btnBack = findViewById(R.id.btnBack);
        imgAuthorAvatar = findViewById(R.id.imgAuthorAvatar);
        tvAuthorName = findViewById(R.id.tvAuthorName);
        tvAuthorHandle = findViewById(R.id.tvAuthorHandle);
        edtPostText = findViewById(R.id.edtPostText);
        sectionAttachedImages = findViewById(R.id.sectionAttachedImages);
        tvAttachedImagesLabel = findViewById(R.id.tvAttachedImagesLabel);
        containerEditImg1 = findViewById(R.id.containerEditImg1);
        containerEditImg2 = findViewById(R.id.containerEditImg2);
        imgEditPreview1 = findViewById(R.id.imgEditPreview1);
        imgEditPreview2 = findViewById(R.id.imgEditPreview2);
        btnStatusActive = findViewById(R.id.btnStatusActive);
        btnStatusHidden = findViewById(R.id.btnStatusHidden);
        edtLocationUrl = findViewById(R.id.edtLocationUrl);
        edtVoucherCode = findViewById(R.id.edtVoucherCode);
        edtHashtags = findViewById(R.id.edtHashtags);
        btnSaveChange = findViewById(R.id.btnSaveChange);
        tvBtnSaveText = findViewById(R.id.tvBtnSaveText);
        btnCancelEdit = findViewById(R.id.btnCancelEdit);
    }

    private void bindInitialData() {
        CurrentUserBinder.bind(this, imgAuthorAvatar, tvAuthorName, tvAuthorHandle);

        String content = getIntent().getStringExtra("POST_CONTENT");
        if (edtPostText != null && content != null) {
            edtPostText.setText(content);
        }

        String location = getIntent().getStringExtra("POST_LOCATION");
        if (edtLocationUrl != null && location != null && !location.trim().isEmpty()) {
            edtLocationUrl.setText(PostFeedHelper.decodeLocationDisplay(location));
        }

        String voucher = getIntent().getStringExtra("POST_VOUCHER");
        if (edtVoucherCode != null && voucher != null) {
            edtVoucherCode.setText(voucher);
        }

        ArrayList<String> hashtags = getIntent().getStringArrayListExtra("POST_HASHTAGS");
        if (edtHashtags != null && hashtags != null && !hashtags.isEmpty()) {
            edtHashtags.setText(PostFeedHelper.formatHashtags(hashtags));
        }

        String statusExtra = getIntent().getStringExtra("POST_STATUS");
        if ("Hidden".equalsIgnoreCase(statusExtra)) {
            selectedStatus = "Hidden";
        } else {
            selectedStatus = "Active";
        }
        updateStatusButtons();

        ArrayList<String> imageUrls = getIntent().getStringArrayListExtra("POST_IMAGES");
        if (imageUrls != null && !imageUrls.isEmpty() && sectionAttachedImages != null) {
            sectionAttachedImages.setVisibility(View.VISIBLE);
            if (tvAttachedImagesLabel != null) {
                tvAttachedImagesLabel.setText("Ảnh đính kèm • " + imageUrls.size());
            }
            if (containerEditImg1 != null && imgEditPreview1 != null && !imageUrls.get(0).trim().isEmpty()) {
                containerEditImg1.setVisibility(View.VISIBLE);
                ImageLoader.loadImage(imgEditPreview1, imageUrls.get(0));
            }
            if (imageUrls.size() > 1 && containerEditImg2 != null && imgEditPreview2 != null && !imageUrls.get(1).trim().isEmpty()) {
                containerEditImg2.setVisibility(View.VISIBLE);
                ImageLoader.loadImage(imgEditPreview2, imageUrls.get(1));
            }
        }
    }

    private void setupListeners() {
        if (btnBack != null) {
            btnBack.setOnClickListener(v -> finish());
        }

        if (btnCancelEdit != null) {
            btnCancelEdit.setOnClickListener(v -> finish());
        }

        if (btnStatusActive != null) {
            btnStatusActive.setOnClickListener(v -> {
                selectedStatus = "Active";
                updateStatusButtons();
            });
        }

        if (btnStatusHidden != null) {
            btnStatusHidden.setOnClickListener(v -> {
                selectedStatus = "Hidden";
                updateStatusButtons();
            });
        }

        if (btnSaveChange != null) {
            btnSaveChange.setOnClickListener(v -> performUpdatePost());
        }
    }

    private void updateStatusButtons() {
        boolean isActive = "Active".equals(selectedStatus);
        if (btnStatusActive != null) {
            btnStatusActive.setBackgroundResource(isActive ? R.drawable.bg_badge_expert : R.drawable.bg_chip_inactive);
            btnStatusActive.setTextColor(isActive ? Color.parseColor("#FF5A36") : Color.parseColor("#141B2B"));
            btnStatusActive.setTypeface(null, isActive ? Typeface.BOLD : Typeface.NORMAL);
        }
        if (btnStatusHidden != null) {
            btnStatusHidden.setBackgroundResource(!isActive ? R.drawable.bg_badge_expert : R.drawable.bg_chip_inactive);
            btnStatusHidden.setTextColor(!isActive ? Color.parseColor("#FF5A36") : Color.parseColor("#141B2B"));
            btnStatusHidden.setTypeface(null, !isActive ? Typeface.BOLD : Typeface.NORMAL);
        }
    }

    private void performUpdatePost() {
        if (isSaving) return;

        String authHeader = ApiClient.getAuthHeader(this);
        if (authHeader.isEmpty()) {
            Toast.makeText(this, "Vui lòng đăng nhập lại", Toast.LENGTH_SHORT).show();
            return;
        }

        String content = edtPostText != null ? edtPostText.getText().toString().trim() : "";
        if (content.isEmpty()) {
            Toast.makeText(this, "Nội dung bài viết không được để trống!", Toast.LENGTH_SHORT).show();
            if (edtPostText != null) edtPostText.requestFocus();
            return;
        }

        String rawLocation = edtLocationUrl != null ? edtLocationUrl.getText().toString().trim() : "";
        String normalizedLocation = PostFeedHelper.normalizeLocationUrl(rawLocation);

        String voucherInput = edtVoucherCode != null ? edtVoucherCode.getText().toString().trim() : "";
        String hashtagsInput = edtHashtags != null ? edtHashtags.getText().toString().trim() : "";
        List<String> hashtags = new ArrayList<>();
        if (!hashtagsInput.isEmpty()) {
            for (String part : hashtagsInput.split("[,\\s]+")) {
                String clean = part.replace("#", "").trim();
                if (!clean.isEmpty()) {
                    hashtags.add(clean);
                }
            }
        }

        UpdatePostRequest request = new UpdatePostRequest();
        request.content = content;
        request.locationUrl = normalizedLocation;
        request.voucherCode = voucherInput;
        request.hashtags = hashtags;
        request.status = selectedStatus;

        isSaving = true;
        if (btnSaveChange != null) btnSaveChange.setEnabled(false);
        if (tvBtnSaveText != null) tvBtnSaveText.setText("Đang lưu...");

        ApiClient.getApiService().updatePost(authHeader, postId, request).enqueue(new Callback<CreatePostResponse>() {
            @Override
            public void onResponse(@NonNull Call<CreatePostResponse> call, @NonNull Response<CreatePostResponse> response) {
                isSaving = false;
                if (btnSaveChange != null) btnSaveChange.setEnabled(true);
                if (tvBtnSaveText != null) tvBtnSaveText.setText("Lưu thay đổi");

                if (response.isSuccessful()) {
                    Toast.makeText(EditPostActivity.this, "Đã cập nhật bài viết!", Toast.LENGTH_SHORT).show();
                    setResult(RESULT_OK);
                    finish();
                } else {
                    Toast.makeText(EditPostActivity.this, "Cập nhật thất bại (mã " + response.code() + ")", Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<CreatePostResponse> call, @NonNull Throwable t) {
                isSaving = false;
                if (btnSaveChange != null) btnSaveChange.setEnabled(true);
                if (tvBtnSaveText != null) tvBtnSaveText.setText("Lưu thay đổi");
                Toast.makeText(EditPostActivity.this, "Lỗi kết nối máy chủ: " + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }
}
