package com.example.dishcision;

import android.app.AlertDialog;
import android.content.SharedPreferences;
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
    private static final int MAX_REVIEW_PHOTOS = 5;

    private static class PhotoItem {
        Bitmap preview;
        String url;
    }

    private final List<PhotoItem> photos = new ArrayList<>();
    private final java.util.ArrayDeque<Uri> photoQueue = new java.util.ArrayDeque<>();
    private LinearLayout layoutReviewPhotos;
    private View scrollReviewPhotos;
    private boolean isUploadingPhoto = false;
    private boolean isSubmitting = false;
    private boolean submitted = false;
    private static final String DRAFT_PREFS = "review_draft";

    private ActivityResultLauncher<String> imagePickerLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_write_review);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(android.R.id.content), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, Math.max(systemBars.bottom, insets.getInsets(WindowInsetsCompat.Type.ime()).bottom));
            return insets;
        });

        ApiClient.init(this);
        initViews();
        prefillIntentData();
        restoreDraft();
        setupImagePicker();
        updateStarRating(currentRating);
        updateExperienceChips();
        setupListeners();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (!submitted) saveDraft();
    }

    // Bản nháp chỉ lưu trên máy (SharedPreferences), xóa khi đăng thành công.
    private void saveDraft() {
        String dishes = edtDishesTried != null ? edtDishesTried.getText().toString() : "";
        String content = edtReviewContent != null ? edtReviewContent.getText().toString() : "";
        SharedPreferences.Editor e = getSharedPreferences(DRAFT_PREFS, MODE_PRIVATE).edit();
        if (dishes.trim().isEmpty() && content.trim().isEmpty() && selectedRestaurantName.isEmpty()) {
            e.clear();
        } else {
            e.putString("name", selectedRestaurantName)
                    .putString("location", selectedRestaurantLocation)
                    .putInt("rating", currentRating)
                    .putBoolean("dineIn", isDineIn)
                    .putString("dishes", dishes)
                    .putString("content", content);
        }
        e.apply();
    }

    private void restoreDraft() {
        SharedPreferences sp = getSharedPreferences(DRAFT_PREFS, MODE_PRIVATE);
        String savedName = sp.getString("name", "");
        // Mở từ trang một quán cụ thể thì chỉ dùng lại bản nháp của đúng quán đó
        if (!selectedRestaurantName.isEmpty() && !selectedRestaurantName.equals(savedName)) return;
        selectedRestaurantName = savedName;
        selectedRestaurantLocation = sp.getString("location", "");
        currentRating = sp.getInt("rating", currentRating);
        isDineIn = sp.getBoolean("dineIn", true);
        if (edtDishesTried != null) edtDishesTried.setText(sp.getString("dishes", ""));
        if (edtReviewContent != null) edtReviewContent.setText(sp.getString("content", ""));
        updateRestaurantDisplay();
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
        layoutReviewPhotos = findViewById(R.id.layoutReviewPhotos);
        scrollReviewPhotos = findViewById(R.id.scrollReviewPhotos);
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
                new ActivityResultContracts.GetMultipleContents(),
                uris -> {
                    if (uris == null || uris.isEmpty()) return;
                    int room = MAX_REVIEW_PHOTOS - photos.size() - photoQueue.size();
                    if (uris.size() > room) {
                        Toast.makeText(this, "Tối đa " + MAX_REVIEW_PHOTOS + " ảnh cho mỗi đánh giá", Toast.LENGTH_SHORT).show();
                    }
                    for (Uri u : uris) {
                        if (room-- <= 0) break;
                        photoQueue.add(u);
                    }
                    if (!isUploadingPhoto) uploadNextPhoto();
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
                if (isSubmitting) return;
                if (photos.size() + photoQueue.size() >= MAX_REVIEW_PHOTOS) {
                    Toast.makeText(this, "Tối đa " + MAX_REVIEW_PHOTOS + " ảnh cho mỗi đánh giá", Toast.LENGTH_SHORT).show();
                    return;
                }
                imagePickerLauncher.launch("image/*");
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

                if (isFinishing() || isDestroyed()) return;
                View content = getLayoutInflater().inflate(R.layout.dialog_pick_restaurant, null);
                LinearLayout list = content.findViewById(R.id.listRestaurants);
                final AlertDialog dialog = new AlertDialog.Builder(WriteReviewActivity.this).setView(content).create();
                content.findViewById(R.id.btnRestaurantCancel).setOnClickListener(v -> dialog.dismiss());

                for (FollowingRestaurantsResponse.StoreItem store : stores) {
                    list.addView(restaurantRow(store.name, store.address, false, () -> {
                        selectedRestaurantName = store.name != null ? store.name : "";
                        selectedRestaurantLocation = store.address != null ? store.address : "";
                        updateRestaurantDisplay();
                        dialog.dismiss();
                    }));
                }
                list.addView(restaurantRow("Nhập tên quán khác...", null, true, () -> {
                    dialog.dismiss();
                    promptCustomRestaurantName();
                }));

                showCardDialog(dialog);
            }

            @Override
            public void onFailure(@NonNull Call<FollowingRestaurantsResponse> call, @NonNull Throwable t) {
                promptCustomRestaurantName();
            }
        });
    }

    private void promptCustomRestaurantName() {
        View content = getLayoutInflater().inflate(R.layout.dialog_restaurant_info, null);
        EditText edtName = content.findViewById(R.id.edtRestaurantName);
        EditText edtAddress = content.findViewById(R.id.edtRestaurantAddress);
        if (!selectedRestaurantName.isEmpty()) edtName.setText(selectedRestaurantName);
        if (!selectedRestaurantLocation.isEmpty()) edtAddress.setText(selectedRestaurantLocation);

        final AlertDialog dialog = new AlertDialog.Builder(this).setView(content).create();
        content.findViewById(R.id.btnRestaurantInfoCancel).setOnClickListener(v -> dialog.dismiss());
        // Tên rỗng thì báo lỗi và giữ hộp thoại mở
        content.findViewById(R.id.btnRestaurantInfoConfirm).setOnClickListener(v -> {
            String name = edtName.getText().toString().trim();
            if (name.isEmpty()) {
                edtName.setError("Vui lòng nhập tên quán");
                return;
            }
            selectedRestaurantName = name;
            selectedRestaurantLocation = edtAddress.getText().toString().trim();
            updateRestaurantDisplay();
            dialog.dismiss();
        });

        showCardDialog(dialog);
    }

    private void showCardDialog(AlertDialog dialog) {
        dialog.getWindow().setBackgroundDrawable(new android.graphics.drawable.ColorDrawable(Color.TRANSPARENT));
        dialog.getWindow().setSoftInputMode(android.view.WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
        dialog.show();
        dialog.getWindow().setLayout(
                (int) (getResources().getDisplayMetrics().widthPixels * 0.9f),
                android.view.ViewGroup.LayoutParams.WRAP_CONTENT);
    }

    private View restaurantRow(String name, String address, boolean accent, Runnable onClick) {
        float d = getResources().getDisplayMetrics().density;
        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.VERTICAL);
        row.setPadding(0, Math.round(12 * d), 0, Math.round(12 * d));
        row.setClickable(true);
        row.setOnClickListener(v -> onClick.run());

        TextView tvName = new TextView(this);
        tvName.setText(name != null ? name : "");
        tvName.setTextSize(15f);
        tvName.setTypeface(null, android.graphics.Typeface.BOLD);
        tvName.setTextColor(Color.parseColor(accent ? "#FF5722" : "#141B2B"));
        row.addView(tvName);

        if (!TextUtils.isEmpty(address)) {
            TextView tvAddr = new TextView(this);
            tvAddr.setText(address);
            tvAddr.setTextSize(12f);
            tvAddr.setTextColor(Color.parseColor("#8E7570"));
            row.addView(tvAddr);
        }
        return row;
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

    private void updatePhotoStatus() {
        if (tvUploadPhotoStatus == null) return;
        if (isUploadingPhoto) {
            tvUploadPhotoStatus.setText("Đang tải ảnh lên...");
        } else if (photos.isEmpty()) {
            tvUploadPhotoStatus.setText("Thêm ảnh trải nghiệm của bạn (tối đa " + MAX_REVIEW_PHOTOS + ")");
        } else {
            tvUploadPhotoStatus.setText("Thêm ảnh (" + photos.size() + "/" + MAX_REVIEW_PHOTOS + ")");
        }
    }

    private void renderPhotos() {
        if (layoutReviewPhotos == null) return;
        layoutReviewPhotos.removeAllViews();
        if (scrollReviewPhotos != null) scrollReviewPhotos.setVisibility(photos.isEmpty() ? View.GONE : View.VISIBLE);
        float d = getResources().getDisplayMetrics().density;
        for (PhotoItem p : new ArrayList<>(photos)) {
            android.widget.FrameLayout cell = new android.widget.FrameLayout(this);
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams((int) (76 * d), (int) (76 * d));
            lp.setMarginEnd((int) (8 * d));
            cell.setLayoutParams(lp);

            ImageView iv = new ImageView(this);
            iv.setLayoutParams(new android.widget.FrameLayout.LayoutParams(
                    android.view.ViewGroup.LayoutParams.MATCH_PARENT, android.view.ViewGroup.LayoutParams.MATCH_PARENT));
            iv.setScaleType(ImageView.ScaleType.CENTER_CROP);
            iv.setBackgroundColor(0xFFE8ECF4);
            if (p.preview != null) iv.setImageBitmap(p.preview);
            iv.setAlpha(p.url == null ? 0.5f : 1f);
            cell.addView(iv);

            if (p.url != null) {
                TextView x = new TextView(this);
                x.setText("✕");
                x.setTextColor(Color.WHITE);
                x.setTextSize(11);
                x.setGravity(android.view.Gravity.CENTER);
                x.setBackgroundColor(0x99000000);
                x.setLayoutParams(new android.widget.FrameLayout.LayoutParams(
                        (int) (22 * d), (int) (22 * d), android.view.Gravity.TOP | android.view.Gravity.END));
                x.setOnClickListener(v -> {
                    photos.remove(p);
                    renderPhotos();
                    updatePhotoStatus();
                });
                cell.addView(x);
            }
            layoutReviewPhotos.addView(cell);
        }
    }

    private void uploadNextPhoto() {
        final Uri uri = photoQueue.poll();
        if (uri == null) {
            isUploadingPhoto = false;
            updatePhotoStatus();
            return;
        }

        String authHeader = ApiClient.getAuthHeader(this);
        if (authHeader.isEmpty()) {
            photoQueue.clear();
            isUploadingPhoto = false;
            updatePhotoStatus();
            Toast.makeText(this, "Vui lòng đăng nhập lại", Toast.LENGTH_SHORT).show();
            return;
        }

        isUploadingPhoto = true;
        updatePhotoStatus();
        final PhotoItem item = new PhotoItem();
        photos.add(item);
        renderPhotos();
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
                        item.preview = previewBitmap;
                        renderPhotos();

                        String fileName = ImageOrientationHelper.ensureExtensionMatchesMime(displayName, prepared.mimeType);
                        RequestBody requestFile = RequestBody.create(MediaType.parse(prepared.mimeType), prepared.file);
                        MultipartBody.Part filePart = MultipartBody.Part.createFormData("file", fileName, requestFile);

                        ApiClient.getApiService().uploadSingleImage(authHeader, filePart).enqueue(new Callback<UploadResponse>() {
                            @Override
                            public void onResponse(@NonNull Call<UploadResponse> call, @NonNull Response<UploadResponse> response) {
                                ImageOrientationHelper.deleteQuietly(prepared.file);
                                if (response.isSuccessful() && response.body() != null && response.body().data != null) {
                                    String rawUrl = !TextUtils.isEmpty(response.body().data.url)
                                            ? response.body().data.url
                                            : response.body().data.fullUrl;
                                    if (!TextUtils.isEmpty(rawUrl)) {
                                        item.url = rawUrl;
                                        renderPhotos();
                                        uploadNextPhoto();
                                        return;
                                    }
                                }
                                failPhoto(item, "Tải ảnh thất bại");
                            }

                            @Override
                            public void onFailure(@NonNull Call<UploadResponse> call, @NonNull Throwable t) {
                                ImageOrientationHelper.deleteQuietly(prepared.file);
                                failPhoto(item, "Lỗi tải ảnh: " + t.getMessage());
                            }
                        });
                    }

                    @Override
                    public void onError(Exception error) {
                        failPhoto(item, "Không thể đọc ảnh đã chọn");
                    }
                }
        );
    }

    private void failPhoto(PhotoItem item, String message) {
        photos.remove(item);
        renderPhotos();
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
        uploadNextPhoto();
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

        List<Map<String, Object>> imageList = new ArrayList<>();
        for (PhotoItem p : photos) {
            if (TextUtils.isEmpty(p.url)) continue;
            Map<String, Object> img = new HashMap<>();
            img.put("imageUrl", p.url);
            img.put("displayOrder", imageList.size() + 1);
            imageList.add(img);
        }
        if (!imageList.isEmpty()) reviewData.put("images", imageList);

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
                    submitted = true;
                    getSharedPreferences(DRAFT_PREFS, MODE_PRIVATE).edit().clear().apply();
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
