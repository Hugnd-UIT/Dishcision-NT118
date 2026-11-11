package com.example.dishcision;

import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.OpenableColumns;
import android.text.TextUtils;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
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
import com.example.dishcision.network.HelpCreateRequest;
import com.example.dishcision.network.HelpCreateResponse;
import com.example.dishcision.network.ImageOrientationHelper;
import com.example.dishcision.network.UploadResponse;

import java.util.ArrayList;
import java.util.List;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SubmitHelpRequestActivity extends AppCompatActivity {

    private ImageView btnBack;
    private Spinner spinnerSubject;
    private EditText edtTitle;
    private EditText edtDesc;
    private EditText edtEmail;
    private View btnAttach;
    private TextView tvAttachLabel;
    private View btnSubmit;

    private ActivityResultLauncher<String> imagePickerLauncher;
    private final List<HelpCreateRequest.AttachmentInput> uploadedAttachments = new ArrayList<>();
    private boolean isUploadingAttachment = false;
    private boolean isSubmitting = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_submit_help_request);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main_submit_help), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        initViews();
        setupSpinner();
        setupImagePicker();
        setupListeners();
    }

    private void initViews() {
        btnBack = findViewById(R.id.btnBack);
        spinnerSubject = findViewById(R.id.spinnerSubject);
        edtTitle = findViewById(R.id.edtTitle);
        edtDesc = findViewById(R.id.edtDesc);
        edtEmail = findViewById(R.id.edtEmail);
        btnAttach = findViewById(R.id.btnAttach);
        tvAttachLabel = findViewById(R.id.tvAttachLabel);
        btnSubmit = findViewById(R.id.btnSubmit);

        String savedEmail = getSharedPreferences("DishcisionPrefs", MODE_PRIVATE).getString("user_email", "");
        if (edtEmail != null && TextUtils.isEmpty(edtEmail.getText()) && !TextUtils.isEmpty(savedEmail)) {
            edtEmail.setText(savedEmail);
        }
    }

    private void setupSpinner() {
        if (spinnerSubject == null) return;
        String[] subjects = {"Chọn chủ đề", "Lỗi ứng dụng", "Đóng góp ý kiến", "Báo cáo người dùng", "Vấn đề tài khoản", "Khác"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, subjects);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerSubject.setAdapter(adapter);
    }

    private void setupImagePicker() {
        imagePickerLauncher = registerForActivityResult(
                new ActivityResultContracts.GetContent(),
                uri -> {
                    if (uri != null) {
                        uploadAttachment(uri);
                    }
                }
        );
    }

    private void setupListeners() {
        if (btnBack != null) btnBack.setOnClickListener(v -> finish());

        if (btnAttach != null) {
            btnAttach.setOnClickListener(v -> {
                if (isUploadingAttachment) return;
                imagePickerLauncher.launch("image/*");
            });
        }

        if (btnSubmit != null) {
            btnSubmit.setOnClickListener(v -> submitRequest());
        }
    }

    private void uploadAttachment(Uri uri) {
        String authHeader = ApiClient.getAuthHeader(this);
        if (authHeader.isEmpty()) {
            Toast.makeText(this, "Vui lòng đăng nhập lại trước khi tải ảnh", Toast.LENGTH_SHORT).show();
            return;
        }

        String displayName = resolveFileName(uri);
        isUploadingAttachment = true;
        if (tvAttachLabel != null) tvAttachLabel.setText("Đang tải ảnh lên...");

        ImageOrientationHelper.prepareUploadAsync(
                this,
                uri,
                "help_attach_",
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
                                isUploadingAttachment = false;
                                if (response.isSuccessful() && response.body() != null && response.body().data != null) {
                                    String rawUrl = (response.body().data.url != null && !response.body().data.url.isEmpty())
                                            ? response.body().data.url
                                            : response.body().data.fullUrl;
                                    if (rawUrl != null && !rawUrl.isEmpty()) {
                                        uploadedAttachments.clear();
                                        uploadedAttachments.add(new HelpCreateRequest.AttachmentInput(fileName, "Ảnh chụp màn hình", rawUrl));
                                        if (tvAttachLabel != null) tvAttachLabel.setText(fileName + " (chạm để đổi)");
                                        Toast.makeText(SubmitHelpRequestActivity.this, "Đính kèm ảnh thành công", Toast.LENGTH_SHORT).show();
                                        return;
                                    }
                                }
                                if (tvAttachLabel != null) tvAttachLabel.setText(R.string.submit_help_attachment);
                                Toast.makeText(SubmitHelpRequestActivity.this, "Tải ảnh đính kèm thất bại", Toast.LENGTH_SHORT).show();
                            }

                            @Override
                            public void onFailure(@NonNull Call<UploadResponse> call, @NonNull Throwable t) {
                                ImageOrientationHelper.deleteQuietly(prepared.file);
                                isUploadingAttachment = false;
                                if (tvAttachLabel != null) tvAttachLabel.setText(R.string.submit_help_attachment);
                                Toast.makeText(SubmitHelpRequestActivity.this, "Lỗi tải ảnh đính kèm", Toast.LENGTH_SHORT).show();
                            }
                        });
                    }

                    @Override
                    public void onError(Exception error) {
                        isUploadingAttachment = false;
                        if (tvAttachLabel != null) tvAttachLabel.setText(R.string.submit_help_attachment);
                        Toast.makeText(SubmitHelpRequestActivity.this, "Không thể đọc tệp đã chọn", Toast.LENGTH_SHORT).show();
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
        return (result != null && !result.trim().isEmpty()) ? result.trim() : "screenshot.jpg";
    }

    private void submitRequest() {
        if (isSubmitting) return;
        if (isUploadingAttachment) {
            Toast.makeText(this, "Ảnh đính kèm đang tải lên, vui lòng đợi", Toast.LENGTH_SHORT).show();
            return;
        }

        String subject = spinnerSubject != null ? spinnerSubject.getSelectedItem().toString() : "";
        String title = edtTitle != null ? edtTitle.getText().toString().trim() : "";
        String desc = edtDesc != null ? edtDesc.getText().toString().trim() : "";
        String email = edtEmail != null ? edtEmail.getText().toString().trim() : "";

        if (subject.equals("Chọn chủ đề") || TextUtils.isEmpty(title) || TextUtils.isEmpty(desc) || TextUtils.isEmpty(email)) {
            Toast.makeText(this, "Vui lòng điền các trường bắt buộc", Toast.LENGTH_SHORT).show();
            return;
        }

        String authHeader = ApiClient.getAuthHeader(this);
        if (authHeader.isEmpty()) {
            Toast.makeText(this, "Vui lòng đăng nhập lại", Toast.LENGTH_SHORT).show();
            return;
        }

        isSubmitting = true;
        if (btnSubmit != null) btnSubmit.setEnabled(false);

        HelpCreateRequest req = new HelpCreateRequest(
                subject,
                title,
                desc,
                email,
                uploadedAttachments.isEmpty() ? null : uploadedAttachments
        );
        ApiClient.getApiService().createTicket(authHeader, req).enqueue(new Callback<HelpCreateResponse>() {
            @Override
            public void onResponse(@NonNull Call<HelpCreateResponse> call, @NonNull Response<HelpCreateResponse> response) {
                isSubmitting = false;
                if (btnSubmit != null) btnSubmit.setEnabled(true);
                if (response.isSuccessful() && response.body() != null) {
                    Toast.makeText(SubmitHelpRequestActivity.this, "Đã gửi yêu cầu hỗ trợ", Toast.LENGTH_SHORT).show();
                    finish();
                } else {
                    Toast.makeText(SubmitHelpRequestActivity.this, "Gửi yêu cầu thất bại (mã " + response.code() + ")", Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<HelpCreateResponse> call, @NonNull Throwable t) {
                isSubmitting = false;
                if (btnSubmit != null) btnSubmit.setEnabled(true);
                Toast.makeText(SubmitHelpRequestActivity.this, "Lỗi kết nối máy chủ: " + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }
}
