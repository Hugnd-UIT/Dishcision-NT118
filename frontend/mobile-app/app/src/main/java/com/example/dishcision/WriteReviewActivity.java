package com.example.dishcision;

import android.app.AlertDialog;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.provider.OpenableColumns;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
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
import com.example.dishcision.network.FollowingRestaurantsResponse;
import com.example.dishcision.network.ImageOrientationHelper;
import com.example.dishcision.network.UploadResponse;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class WriteReviewActivity extends AppCompatActivity {

    private ImageView btnBack;
    private View layoutRestaurantPicker;
    private TextView tvSelectedRestaurant;
    private TextView tvSelectedLocation;
    private TextView btnChangeRestaurant;
    private TextView tvRatingText;
    private LinearLayout chipDineIn;
    private LinearLayout chipTakeaway;
    private TextView tvChipDineIn;
    private TextView tvChipTakeaway;
    private EditText edtDishesTried;
    private EditText edtReviewContent;
    private LinearLayout btnUploadPhoto;
    private ImageView imgUploadedPreview;
    private TextView tvUploadPhotoStatus;
    private LinearLayout btnSubmitReview;
    private TextView tvSubmitReviewLabel;

    private ImageView star1;
    private ImageView star2;
    private ImageView star3;
    private ImageView star4;
    private ImageView star5;

    private int currentRating = 4;
    private boolean isDineIn = true;
    private String selectedRestaurantName = "";
    private String selectedRestaurantLocation = "";
    private String uploadedPhotoUrl = null;
    private boolean isUploadingPhoto = false;
    private boolean isSubmitting = false;

    private ActivityResultLauncher<String> imagePickerLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_write_review);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(android.R.id.content), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        ApiClient.init(this);
        initViews();
        prefillIntentData();
        setupImagePicker();
        updateStarRating(currentRating);
        updateExperienceChips();
        setupListeners();
    }

    private void initViews() {
        btnBack = findViewById(R.id.btnBack);
        layoutRestaurantPicker = findViewById(R.id.layoutRestaurantPicker);
        tvSelectedRestaurant = findViewById(R.id.tvSelectedRestaurant);
        tvSelectedLocation = findViewById(R.id.tvSelectedLocation);
        btnChangeRestaurant = findViewById(R.id.btnChangeRestaurant);
        tvRatingText = findViewById(R.id.tvRatingText);
        chipDineIn = findViewById(R.id.chipDineIn);
        chipTakeaway = findViewById(R.id.chipTakeaway);
        tvChipDineIn = findViewById(R.id.tvChipDineIn);
        tvChipTakeaway = findViewById(R.id.tvChipTakeaway);
        edtDishesTried = findViewById(R.id.edtDishesTried);
        edtReviewContent = findViewById(R.id.edtReviewContent);
        btnUploadPhoto = findViewById(R.id.btnUploadPhoto);
        imgUploadedPreview = findViewById(R.id.imgUploadedPreview);
        tvUploadPhotoStatus = findViewById(R.id.tvUploadPhotoStatus);
        btnSubmitReview = findViewById(R.id.btnSubmitReview);
        tvSubmitReviewLabel = findViewById(R.id.tvSubmitReviewLabel);

        star1 = findViewById(R.id.star1);
        star2 = findViewById(R.id.star2);
        star3 = findViewById(R.id.star3);
        star4 = findViewById(R.id.star4);
        star5 = findViewById(R.id.star5);
    }

    private void prefillIntentData() {
        String name = getIntent().getStringExtra("RESTAURANT_NAME");
        String location = getIntent().getStringExtra("RESTAURANT_LOCATION");
        if (!TextUtils.isEmpty(name)) {
            selectedRestaurantName = name.trim();
            selectedRestaurantLocation = location != null ? location.trim() : "";
            updateRestaurantDisplay();
        }
    }

    private void updateRestaurantDisplay() {
        if (tvSelectedRestaurant != null) {
            tvSelectedRestaurant.setText(!selectedRestaurantName.isEmpty() ? selectedRestaurantName : "Chọn quán ăn");
        }
        if (tvSelectedLocation != null) {
            if (!selectedRestaurantLocation.isEmpty()) {
                tvSelectedLocation.setText(selectedRestaurantLocation);
            } else if (!selectedRestaurantName.isEmpty()) {
                tvSelectedLocation.setText("Đã chọn quán ăn");
            } else {
                tvSelectedLocation.setText("Chạm để chọn hoặc nhập tên quán");
            }
        }
    }

    private void setupImagePicker() {
        imagePickerLauncher = registerForActivityResult(
                new ActivityResultContracts.GetContent(),
                uri -> {
                    if (uri != null) {
                        uploadReviewPhoto(uri);
                    }
                }
        );
    }

    private void setupListeners() {
        if (btnBack != null) btnBack.setOnClickListener(v -> finish());

        View.OnClickListener openPicker = v -> openRestaurantPicker();
        if (layoutRestaurantPicker != null) layoutRestaurantPicker.setOnClickListener(openPicker);
        if (btnChangeRestaurant != null) btnChangeRestaurant.setOnClickListener(openPicker);

        setupStarListeners();

        if (chipDineIn != null) {
            chipDineIn.setOnClickListener(v -> {
                isDineIn = true;
                updateExperienceChips();
            });
        }

        if (chipTakeaway != null) {
            chipTakeaway.setOnClickListener(v -> {
                isDineIn = false;
                updateExperienceChips();
            });
        }

        if (btnUploadPhoto != null) {
            btnUploadPhoto.setOnClickListener(v -> {
                if (!isUploadingPhoto && !isSubmitting) {
                    imagePickerLauncher.launch("image/*");
                }
            });
        }

        if (btnSubmitReview != null) {
            btnSubmitReview.setOnClickListener(v -> submitReview());
        }
    }

    private void openRestaurantPicker() {
        String authHeader = ApiClient.getAuthHeader(this);
        ApiClient.getApiService().getStores(authHeader, null).enqueue(new Callback<FollowingRestaurantsResponse>() {
            @Override
            public void onResponse(@NonNull Call<FollowingRestaurantsResponse> call, @NonNull Response<FollowingRestaurantsResponse> response) {
                List<FollowingRestaurantsResponse.StoreItem> stores =
                        (response.isSuccessful() && response.body() != null && response.body().getData() != null
                                && response.body().getData().restaurants != null)
                                ? response.body().getData().restaurants
                                : new ArrayList<>();

                List<String> labels = new ArrayList<>();
                for (FollowingRestaurantsResponse.StoreItem store : stores) {
                    labels.add(!TextUtils.isEmpty(store.address) ? store.name + " • " + store.address : store.name);
                }
                labels.add("Nhập tên quán khác...");

                new AlertDialog.Builder(WriteReviewActivity.this)
                        .setTitle("Chọn quán ăn")
                        .setItems(labels.toArray(new String[0]), (dialog, which) -> {
                            if (which < stores.size()) {
                                FollowingRestaurantsResponse.StoreItem picked = stores.get(which);
                                selectedRestaurantName = picked.name != null ? picked.name : "";
                                selectedRestaurantLocation = picked.address != null ? picked.address : "";
                                updateRestaurantDisplay();
                            } else {
                                promptCustomRestaurantName();
                            }
                        })
                        .setNegativeButton("Hủy", null)
                        .show();
            }

            @Override
            public void onFailure(@NonNull Call<FollowingRestaurantsResponse> call, @NonNull Throwable t) {
                promptCustomRestaurantName();
            }
        });
    }

    private void promptCustomRestaurantName() {
        LinearLayout box = new LinearLayout(this);
        box.setOrientation(LinearLayout.VERTICAL);
        int pad = Math.round(16 * getResources().getDisplayMetrics().density);
        box.setPadding(pad, pad, pad, 0);

        EditText edtName = new EditText(this);
        edtName.setHint("Tên quán ăn *");
        if (!selectedRestaurantName.isEmpty()) edtName.setText(selectedRestaurantName);
        box.addView(edtName);

        EditText edtAddress = new EditText(this);
        edtAddress.setHint("Địa chỉ (tùy chọn)");
        if (!selectedRestaurantLocation.isEmpty()) edtAddress.setText(selectedRestaurantLocation);
        box.addView(edtAddress);

        new AlertDialog.Builder(this)
                .setTitle("Nhập thông tin quán")
                .setView(box)
                .setNegativeButton("Hủy", null)
                .setPositiveButton("Xác nhận", (d, w) -> {
                    String name = edtName.getText().toString().trim();
                    if (name.isEmpty()) {
                        Toast.makeText(this, "Vui lòng nhập tên quán", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    selectedRestaurantName = name;
                    selectedRestaurantLocation = edtAddress.getText().toString().trim();
                    updateRestaurantDisplay();
                })
                .show();
    }

    private void setupStarListeners() {
        if (star1 != null) star1.setOnClickListener(v -> updateStarRating(1));
        if (star2 != null) star2.setOnClickListener(v -> updateStarRating(2));
        if (star3 != null) star3.setOnClickListener(v -> updateStarRating(3));
        if (star4 != null) star4.setOnClickListener(v -> updateStarRating(4));
        if (star5 != null) star5.setOnClickListener(v -> updateStarRating(5));
    }

    private void updateStarRating(int rating) {
        currentRating = rating;
        ImageView[] stars = {star1, star2, star3, star4, star5};
        int activeColor = Color.parseColor("#FF5A36");
        int inactiveColor = Color.parseColor("#D9DCE5");
        for (int i = 0; i < stars.length; i++) {
            if (stars[i] == null) continue;
            boolean filled = i < rating;
            stars[i].setImageResource(filled ? R.drawable.ic_star : R.drawable.ic_star_outline);
            stars[i].setColorFilter(filled ? activeColor : inactiveColor);
        }

        if (tvRatingText != null) {
            String label;
            switch (rating) {
                case 1: label = "1 / 5 - Tệ"; break;
                case 2: label = "2 / 5 - Tạm được"; break;
                case 3: label = "3 / 5 - Bình thường"; break;
                case 4: label = "4 / 5 - Rất hài lòng"; break;
                case 5: label = "5 / 5 - Tuyệt vời!"; break;
                default: label = rating + " / 5"; break;
            }
            tvRatingText.setText(label);
        }
    }

    private void updateExperienceChips() {
        if (chipDineIn != null && tvChipDineIn != null) {
            chipDineIn.setBackgroundResource(isDineIn ? R.drawable.bg_chip_selected_orange : R.drawable.bg_chip_preference);
            tvChipDineIn.setText(isDineIn ? "✔ Ăn tại quán" : "Ăn tại quán");
            tvChipDineIn.setTextColor(Color.parseColor(isDineIn ? "#FF5A36" : "#505568"));
            tvChipDineIn.setTypeface(null, isDineIn ? Typeface.BOLD : Typeface.NORMAL);
        }
        if (chipTakeaway != null && tvChipTakeaway != null) {
            chipTakeaway.setBackgroundResource(!isDineIn ? R.drawable.bg_chip_selected_orange : R.drawable.bg_chip_preference);
            tvChipTakeaway.setText(!isDineIn ? "✔ Mang về" : "Mang về");
            tvChipTakeaway.setTextColor(Color.parseColor(!isDineIn ? "#FF5A36" : "#505568"));
            tvChipTakeaway.setTypeface(null, !isDineIn ? Typeface.BOLD : Typeface.NORMAL);
        }
    }

    private void uploadReviewPhoto(Uri uri) {
        String authHeader = ApiClient.getAuthHeader(this);
        if (authHeader.isEmpty()) {
            Toast.makeText(this, "Vui lòng đăng nhập lại", Toast.LENGTH_SHORT).show();
            return;
        }

        isUploadingPhoto = true;
        if (tvUploadPhotoStatus != null) tvUploadPhotoStatus.setText("Đang tải ảnh lên...");
        final String displayName = resolveFileName(uri);

        ImageOrientationHelper.prepareUploadAsync(
                this,
                uri,
                "review_photo_",
                800,
                new ImageOrientationHelper.PrepareUploadCallback() {
                    @Override
                    public void onReady(ImageOrientationHelper.PreparedImage prepared, Bitmap previewBitmap) {
                        if (isFinishing() || isDestroyed()) {
                            ImageOrientationHelper.deleteQuietly(prepared.file);
                            return;
                        }
                        if (imgUploadedPreview != null && previewBitmap != null) {
                            imgUploadedPreview.setImageBitmap(previewBitmap);
                        }

                        String fileName = ImageOrientationHelper.ensureExtensionMatchesMime(displayName, prepared.mimeType);
                        RequestBody requestFile = RequestBody.create(MediaType.parse(prepared.mimeType), prepared.file);
                        MultipartBody.Part filePart = MultipartBody.Part.createFormData("file", fileName, requestFile);

                        ApiClient.getApiService().uploadSingleImage(authHeader, filePart).enqueue(new Callback<UploadResponse>() {
                            @Override
                            public void onResponse(@NonNull Call<UploadResponse> call, @NonNull Response<UploadResponse> response) {
                                ImageOrientationHelper.deleteQuietly(prepared.file);
                                isUploadingPhoto = false;
                                if (response.isSuccessful() && response.body() != null && response.body().data != null) {
                                    String rawUrl = !TextUtils.isEmpty(response.body().data.url)
                                            ? response.body().data.url
                                            : response.body().data.fullUrl;
                                    if (!TextUtils.isEmpty(rawUrl)) {
                                        uploadedPhotoUrl = rawUrl;
                                        if (tvUploadPhotoStatus != null) tvUploadPhotoStatus.setText(fileName + " (chạm để đổi)");
                                        Toast.makeText(WriteReviewActivity.this, "Tải ảnh thành công", Toast.LENGTH_SHORT).show();
                                        return;
                                    }
                                }
                                uploadedPhotoUrl = null;
                                if (tvUploadPhotoStatus != null) tvUploadPhotoStatus.setText("Thêm ảnh trải nghiệm của bạn");
                                if (imgUploadedPreview != null) imgUploadedPreview.setImageResource(R.drawable.ic_image);
                                Toast.makeText(WriteReviewActivity.this, "Tải ảnh thất bại", Toast.LENGTH_SHORT).show();
                            }

                            @Override
                            public void onFailure(@NonNull Call<UploadResponse> call, @NonNull Throwable t) {
                                ImageOrientationHelper.deleteQuietly(prepared.file);
                                isUploadingPhoto = false;
                                uploadedPhotoUrl = null;
                                if (tvUploadPhotoStatus != null) tvUploadPhotoStatus.setText("Thêm ảnh trải nghiệm của bạn");
                                if (imgUploadedPreview != null) imgUploadedPreview.setImageResource(R.drawable.ic_image);
                                Toast.makeText(WriteReviewActivity.this, "Lỗi tải ảnh: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        });
                    }

                    @Override
                    public void onError(Exception error) {
                        isUploadingPhoto = false;
                        if (tvUploadPhotoStatus != null) tvUploadPhotoStatus.setText("Thêm ảnh trải nghiệm của bạn");
                        Toast.makeText(WriteReviewActivity.this, "Không thể đọc ảnh đã chọn", Toast.LENGTH_SHORT).show();
                    }
                }
        );
    }

    private String resolveFileName(Uri uri) {
        String result = null;
        try (Cursor cursor = getContentResolver().query(uri, null, null, null, null)) {
            if (cursor != null && cursor.moveToFirst()) {
                int idx = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
                if (idx >= 0) result = cursor.getString(idx);
            }
        } catch (Exception ignored) {}
        return (result != null && !result.trim().isEmpty()) ? result.trim() : "review.jpg";
    }

    private void submitReview() {
        if (isSubmitting) return;
        if (isUploadingPhoto) {
            Toast.makeText(this, "Ảnh đang tải lên, vui lòng đợi", Toast.LENGTH_SHORT).show();
            return;
        }

        String authHeader = ApiClient.getAuthHeader(this);
        if (authHeader.isEmpty()) {
            Toast.makeText(this, "Vui lòng đăng nhập để đăng đánh giá", Toast.LENGTH_SHORT).show();
            return;
        }

        if (selectedRestaurantName.isEmpty()) {
            Toast.makeText(this, "Vui lòng chọn hoặc nhập tên quán ăn", Toast.LENGTH_SHORT).show();
            openRestaurantPicker();
            return;
        }

        String dishes = edtDishesTried != null ? edtDishesTried.getText().toString().trim() : "";
        String reviewText = edtReviewContent != null ? edtReviewContent.getText().toString().trim() : "";
        if (reviewText.isEmpty()) {
            Toast.makeText(this, "Vui lòng nhập cảm nhận của bạn!", Toast.LENGTH_SHORT).show();
            return;
        }

        List<String> tags = new ArrayList<>();
        tags.add(isDineIn ? "Ăn tại quán" : "Mang về");
        if (!dishes.isEmpty()) {
            String[] split = dishes.split(",");
            for (String s : split) {
                String t = s.trim();
                if (!t.isEmpty()) tags.add(t);
            }
        }

        Map<String, Object> reviewData = new HashMap<>();
        reviewData.put("restaurantName", selectedRestaurantName);
        if (!selectedRestaurantLocation.isEmpty()) {
            if (selectedRestaurantLocation.startsWith("http://") || selectedRestaurantLocation.startsWith("https://")) {
                reviewData.put("locationUrl", selectedRestaurantLocation);
            } else {
                reviewData.put("locationUrl", "https://maps.google.com/?q=" + Uri.encode(selectedRestaurantName + " " + selectedRestaurantLocation));
            }
        }
        reviewData.put("rating", currentRating);
        reviewData.put("content", reviewText);
        reviewData.put("tags", tags);

        if (!TextUtils.isEmpty(uploadedPhotoUrl)) {
            Map<String, Object> img = new HashMap<>();
            img.put("imageUrl", uploadedPhotoUrl);
            img.put("displayOrder", 1);
            reviewData.put("images", Collections.singletonList(img));
        }

        isSubmitting = true;
        if (btnSubmitReview != null) btnSubmitReview.setEnabled(false);
        if (tvSubmitReviewLabel != null) tvSubmitReviewLabel.setText("Đang đăng...");

        ApiClient.getApiService().createReview(authHeader, reviewData).enqueue(new Callback<Object>() {
            @Override
            public void onResponse(@NonNull Call<Object> call, @NonNull Response<Object> response) {
                isSubmitting = false;
                if (btnSubmitReview != null) btnSubmitReview.setEnabled(true);
                if (tvSubmitReviewLabel != null) tvSubmitReviewLabel.setText("Đăng đánh giá");

                if (response.isSuccessful()) {
                    Toast.makeText(WriteReviewActivity.this, "Đã gửi đánh giá thành công!", Toast.LENGTH_SHORT).show();
                    setResult(RESULT_OK);
                    finish();
                } else {
                    Toast.makeText(WriteReviewActivity.this, "Đăng đánh giá thất bại (mã " + response.code() + ")", Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<Object> call, @NonNull Throwable t) {
                isSubmitting = false;
                if (btnSubmitReview != null) btnSubmitReview.setEnabled(true);
                if (tvSubmitReviewLabel != null) tvSubmitReviewLabel.setText("Đăng đánh giá");
                Toast.makeText(WriteReviewActivity.this, "Lỗi kết nối máy chủ: " + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }
}
