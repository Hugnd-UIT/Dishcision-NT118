package com.example.dishcision;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.dishcision.network.ApiClient;
import com.example.dishcision.network.PrivacySettingsModel;
import com.example.dishcision.network.PrivacySettingsResponse;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class PrivacyRadioChoiceActivity extends AppCompatActivity {

    private ImageView btnBack;
    private TextView tvToolbarTitle, tvCategoryLabel, tvQuestionTitle, tvQuestionDesc, tvInfoTitle, tvInfoDesc;
    private RadioGroup radioGroup;
    private View layoutOption1, layoutOption2, layoutOption3, layoutOption4;
    private View divider1, divider2, divider3;
    private RadioButton rbOption1, rbOption2, rbOption3, rbOption4;
    private TextView tvOption1Desc, tvOption2Desc, tvOption3Desc, tvOption4Desc;

    private String settingKey = "new_posts";
    private PrivacySettingsModel currentPrivacy = new PrivacySettingsModel();
    private boolean isUserAction = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_privacy_radio_choice);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main_privacy_radio), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        initViews();
        readIntentExtras();
        setupListeners();
        fetchCurrentSettingFromBackend();
    }

    private void initViews() {
        btnBack = findViewById(R.id.btnBack);
        tvToolbarTitle = findViewById(R.id.tvToolbarTitle);
        tvCategoryLabel = findViewById(R.id.tvCategoryLabel);
        tvQuestionTitle = findViewById(R.id.tvQuestionTitle);
        tvQuestionDesc = findViewById(R.id.tvQuestionDesc);
        tvInfoTitle = findViewById(R.id.tvInfoTitle);
        tvInfoDesc = findViewById(R.id.tvInfoDesc);

        radioGroup = findViewById(R.id.radioGroup);

        layoutOption1 = findViewById(R.id.layoutOption1);
        layoutOption2 = findViewById(R.id.layoutOption2);
        layoutOption3 = findViewById(R.id.layoutOption3);
        layoutOption4 = findViewById(R.id.layoutOption4);

        divider1 = findViewById(R.id.divider1);
        divider2 = findViewById(R.id.divider2);
        divider3 = findViewById(R.id.divider3);

        rbOption1 = findViewById(R.id.rbOption1);
        rbOption2 = findViewById(R.id.rbOption2);
        rbOption3 = findViewById(R.id.rbOption3);
        rbOption4 = findViewById(R.id.rbOption4);

        tvOption1Desc = findViewById(R.id.tvOption1Desc);
        tvOption2Desc = findViewById(R.id.tvOption2Desc);
        tvOption3Desc = findViewById(R.id.tvOption3Desc);
        tvOption4Desc = findViewById(R.id.tvOption4Desc);
    }

    private void readIntentExtras() {
        Intent intent = getIntent();
        if (intent == null) return;

        if (intent.hasExtra("SETTING_KEY")) settingKey = intent.getStringExtra("SETTING_KEY");
        if (intent.hasExtra("TITLE") && tvToolbarTitle != null) tvToolbarTitle.setText(intent.getStringExtra("TITLE"));
        if (intent.hasExtra("CATEGORY") && tvCategoryLabel != null) tvCategoryLabel.setText(intent.getStringExtra("CATEGORY"));
        if (intent.hasExtra("QUESTION_TITLE") && tvQuestionTitle != null) tvQuestionTitle.setText(intent.getStringExtra("QUESTION_TITLE"));
        if (intent.hasExtra("QUESTION_DESC") && tvQuestionDesc != null) tvQuestionDesc.setText(intent.getStringExtra("QUESTION_DESC"));
        if (intent.hasExtra("INFO_TITLE") && tvInfoTitle != null) tvInfoTitle.setText(intent.getStringExtra("INFO_TITLE"));
        if (intent.hasExtra("INFO_DESC") && tvInfoDesc != null) tvInfoDesc.setText(intent.getStringExtra("INFO_DESC"));

        configureOptionsForKey(settingKey);
    }

    private void configureOptionsForKey(String key) {
        if ("find_email".equals(key)) {
            // 4 options: Mọi người, Bạn của bạn bè, Bạn bè, Không ai
            layoutOption1.setVisibility(View.VISIBLE);
            rbOption1.setText("Mọi người");
            tvOption1Desc.setText("Bất kỳ ai trên Dishcision.");

            layoutOption2.setVisibility(View.VISIBLE);
            rbOption2.setText("Bạn của bạn bè");
            tvOption2Desc.setText("Người có ít nhất một bạn chung.");

            layoutOption3.setVisibility(View.VISIBLE);
            rbOption3.setText("Bạn bè");
            tvOption3Desc.setText("Những người đã kết bạn với bạn.");

            divider3.setVisibility(View.VISIBLE);
            layoutOption4.setVisibility(View.VISIBLE);
            rbOption4.setText("Không ai");
            tvOption4Desc.setText("Không cho phép tìm bạn bằng email.");

        } else if ("friend_requests".equals(key)) {
            // 2 options: Mọi người, Bạn của bạn bè
            layoutOption1.setVisibility(View.VISIBLE);
            rbOption1.setText("Mọi người");
            tvOption1Desc.setText("Bất kỳ ai trên Dishcision.");

            layoutOption2.setVisibility(View.VISIBLE);
            rbOption2.setText("Bạn của bạn bè");
            tvOption2Desc.setText("Người có ít nhất một bạn chung.");

            divider2.setVisibility(View.GONE);
            layoutOption3.setVisibility(View.GONE);
            divider3.setVisibility(View.GONE);
            layoutOption4.setVisibility(View.GONE);

        } else if ("messages".equals(key) || "comments".equals(key)) {
            // 3 options: Mọi người, Bạn bè, Không ai
            layoutOption1.setVisibility(View.VISIBLE);
            rbOption1.setText("Mọi người");
            tvOption1Desc.setText("messages".equals(key) ? "Bất kỳ ai trên Dishcision." : "Bất kỳ ai có thể xem bài đăng.");

            layoutOption2.setVisibility(View.VISIBLE);
            rbOption2.setText("Bạn bè");
            tvOption2Desc.setText("messages".equals(key) ? "Những người đã kết bạn với bạn." : "Bạn bè có thể xem bài đăng.");

            layoutOption3.setVisibility(View.VISIBLE);
            rbOption3.setText("Không ai");
            tvOption3Desc.setText("messages".equals(key) ? "Không ai được gửi tin nhắn cho bạn." : "Không cho phép bình luận.");

            divider3.setVisibility(View.GONE);
            layoutOption4.setVisibility(View.GONE);

        } else {
            // Default 3 options: Công khai, Bạn bè, Chỉ mình tôi
            layoutOption1.setVisibility(View.VISIBLE);
            rbOption1.setText("Công khai");
            tvOption1Desc.setText("Bất kỳ ai trên Dishcision.");

            layoutOption2.setVisibility(View.VISIBLE);
            rbOption2.setText("Bạn bè");
            tvOption2Desc.setText("Những người đã kết bạn với bạn.");

            layoutOption3.setVisibility(View.VISIBLE);
            rbOption3.setText("Chỉ mình tôi");
            tvOption3Desc.setText("Chỉ bạn có thể xem.");

            divider3.setVisibility(View.GONE);
            layoutOption4.setVisibility(View.GONE);
        }
    }

    private void setupListeners() {
        if (btnBack != null) {
            btnBack.setOnClickListener(v -> finish());
        }

        View.OnClickListener clickListener = v -> {
            if (v == layoutOption1 || v == rbOption1) selectOption(1);
            else if (v == layoutOption2 || v == rbOption2) selectOption(2);
            else if (v == layoutOption3 || v == rbOption3) selectOption(3);
            else if (v == layoutOption4 || v == rbOption4) selectOption(4);
        };

        if (layoutOption1 != null) layoutOption1.setOnClickListener(clickListener);
        if (layoutOption2 != null) layoutOption2.setOnClickListener(clickListener);
        if (layoutOption3 != null) layoutOption3.setOnClickListener(clickListener);
        if (layoutOption4 != null) layoutOption4.setOnClickListener(clickListener);

        if (rbOption1 != null) rbOption1.setOnClickListener(clickListener);
        if (rbOption2 != null) rbOption2.setOnClickListener(clickListener);
        if (rbOption3 != null) rbOption3.setOnClickListener(clickListener);
        if (rbOption4 != null) rbOption4.setOnClickListener(clickListener);
    }

    private void selectOption(int optionIndex) {
        rbOption1.setChecked(optionIndex == 1);
        rbOption2.setChecked(optionIndex == 2);
        rbOption3.setChecked(optionIndex == 3);
        rbOption4.setChecked(optionIndex == 4);

        int checkedId = R.id.rbOption1;
        if (optionIndex == 2) checkedId = R.id.rbOption2;
        if (optionIndex == 3) checkedId = R.id.rbOption3;
        if (optionIndex == 4) checkedId = R.id.rbOption4;

        String selectedVal = getValueFromCheckedId(checkedId);
        updateModelValue(settingKey, selectedVal);
        savePrivacyToBackend();
    }

    private String getValueFromCheckedId(int checkedId) {
        if ("find_email".equals(settingKey)) {
            if (checkedId == R.id.rbOption1) return "public";
            if (checkedId == R.id.rbOption2) return "friends_of_friends";
            if (checkedId == R.id.rbOption3) return "friends";
            if (checkedId == R.id.rbOption4) return "no_one";
        } else if ("friend_requests".equals(settingKey)) {
            if (checkedId == R.id.rbOption1) return "public";
            if (checkedId == R.id.rbOption2) return "friends_of_friends";
        } else if ("messages".equals(settingKey) || "comments".equals(settingKey)) {
            if (checkedId == R.id.rbOption1) return "public";
            if (checkedId == R.id.rbOption2) return "friends";
            if (checkedId == R.id.rbOption3) return "no_one";
        } else {
            if (checkedId == R.id.rbOption1) return "public";
            if (checkedId == R.id.rbOption2) return "friends";
            if (checkedId == R.id.rbOption3) return "only_me";
        }
        return "friends";
    }

    private void setCheckedFromValue(String val) {
        isUserAction = false;
        if ("find_email".equals(settingKey)) {
            if ("public".equals(val)) rbOption1.setChecked(true);
            else if ("friends_of_friends".equals(val)) rbOption2.setChecked(true);
            else if ("friends".equals(val)) rbOption3.setChecked(true);
            else if ("no_one".equals(val)) rbOption4.setChecked(true);
        } else if ("friend_requests".equals(settingKey)) {
            if ("public".equals(val)) rbOption1.setChecked(true);
            else rbOption2.setChecked(true);
        } else if ("messages".equals(settingKey) || "comments".equals(settingKey)) {
            if ("public".equals(val)) rbOption1.setChecked(true);
            else if ("friends".equals(val)) rbOption2.setChecked(true);
            else if ("no_one".equals(val)) rbOption3.setChecked(true);
        } else {
            if ("public".equals(val)) rbOption1.setChecked(true);
            else if ("only_me".equals(val)) rbOption3.setChecked(true);
            else rbOption2.setChecked(true);
        }
        isUserAction = true;
    }

    private void fetchCurrentSettingFromBackend() {
        ApiClient.getApiService().getPrivacySettings("Bearer dummy_token").enqueue(new Callback<PrivacySettingsResponse>() {
            @Override
            public void onResponse(@NonNull Call<PrivacySettingsResponse> call, @NonNull Response<PrivacySettingsResponse> response) {
                if (response.isSuccessful() && response.body() != null && response.body().getData() != null) {
                    currentPrivacy = response.body().getData();
                }
                setCheckedFromValue(getCurrentValue(settingKey));
            }

            @Override
            public void onFailure(@NonNull Call<PrivacySettingsResponse> call, @NonNull Throwable t) {
                setCheckedFromValue(getCurrentValue(settingKey));
            }
        });
    }

    private String getCurrentValue(String key) {
        if ("new_posts".equals(key)) return currentPrivacy.getNewPosts();
        if ("profile_info".equals(key)) return currentPrivacy.getProfileInfo();
        if ("following_list".equals(key)) return currentPrivacy.getFollowingList();
        if ("find_email".equals(key)) return currentPrivacy.getFindEmail();
        if ("friend_requests".equals(key)) return currentPrivacy.getFriendRequests();
        if ("messages".equals(key)) return currentPrivacy.getMessages();
        if ("comments".equals(key)) return currentPrivacy.getComments();
        return "friends";
    }

    private void updateModelValue(String key, String val) {
        if ("new_posts".equals(key)) currentPrivacy.setNewPosts(val);
        else if ("profile_info".equals(key)) currentPrivacy.setProfileInfo(val);
        else if ("following_list".equals(key)) currentPrivacy.setFollowingList(val);
        else if ("find_email".equals(key)) currentPrivacy.setFindEmail(val);
        else if ("friend_requests".equals(key)) currentPrivacy.setFriendRequests(val);
        else if ("messages".equals(key)) currentPrivacy.setMessages(val);
        else if ("comments".equals(key)) currentPrivacy.setComments(val);
    }

    private void savePrivacyToBackend() {
        ApiClient.getApiService().updatePrivacySettings("Bearer dummy_token", currentPrivacy).enqueue(new Callback<PrivacySettingsResponse>() {
            @Override
            public void onResponse(@NonNull Call<PrivacySettingsResponse> call, @NonNull Response<PrivacySettingsResponse> response) {
                Toast.makeText(PrivacyRadioChoiceActivity.this, "Đã lưu thay đổi", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onFailure(@NonNull Call<PrivacySettingsResponse> call, @NonNull Throwable t) {
                Toast.makeText(PrivacyRadioChoiceActivity.this, "Đã lưu tùy chọn", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
