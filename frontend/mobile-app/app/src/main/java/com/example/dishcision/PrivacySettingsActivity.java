package com.example.dishcision;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

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

public class PrivacySettingsActivity extends AppCompatActivity {

    private ImageView btnBack;
    private LinearLayout btnNewPosts, btnProfileInfo, btnFollowingList;
    private LinearLayout btnFindEmail, btnFriendRequests, btnMessages;
    private LinearLayout btnComments, btnBlocked, btnRestricted;

    private TextView tvNewPostsValue, tvProfileInfoValue, tvFollowingListValue;
    private TextView tvFindEmailValue, tvFriendRequestsValue, tvMessagesValue;
    private TextView tvCommentsValue, tvBlockedValue, tvRestrictedValue;
    private TextView btnLearnMorePrivacy;

    private PrivacySettingsModel currentPrivacy = new PrivacySettingsModel();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_privacy_settings);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main_privacy_settings), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        initViews();
        setupListeners();
    }

    @Override
    protected void onResume() {
        super.onResume();
        fetchPrivacySettingsFromBackend();
    }

    private void initViews() {
        btnBack = findViewById(R.id.btnBack);

        btnNewPosts = findViewById(R.id.btnNewPosts);
        btnProfileInfo = findViewById(R.id.btnProfileInfo);
        btnFollowingList = findViewById(R.id.btnFollowingList);

        btnFindEmail = findViewById(R.id.btnFindEmail);
        btnFriendRequests = findViewById(R.id.btnFriendRequests);
        btnMessages = findViewById(R.id.btnMessages);

        btnComments = findViewById(R.id.btnComments);
        btnBlocked = findViewById(R.id.btnBlocked);
        btnRestricted = findViewById(R.id.btnRestricted);

        tvNewPostsValue = findViewById(R.id.tvNewPostsValue);
        tvProfileInfoValue = findViewById(R.id.tvProfileInfoValue);
        tvFollowingListValue = findViewById(R.id.tvFollowingListValue);

        tvFindEmailValue = findViewById(R.id.tvFindEmailValue);
        tvFriendRequestsValue = findViewById(R.id.tvFriendRequestsValue);
        tvMessagesValue = findViewById(R.id.tvMessagesValue);

        tvCommentsValue = findViewById(R.id.tvCommentsValue);
        tvBlockedValue = findViewById(R.id.tvBlockedValue);
        tvRestrictedValue = findViewById(R.id.tvRestrictedValue);

        btnLearnMorePrivacy = findViewById(R.id.btnLearnMorePrivacy);
    }

    private void setupListeners() {
        if (btnBack != null) {
            btnBack.setOnClickListener(v -> finish());
        }

        if (btnNewPosts != null) {
            btnNewPosts.setOnClickListener(v -> openRadioChoice("new_posts", "Bài đăng mới", "Ai có thể xem nội dung của bạn", "Ai có thể xem bài đăng mới của bạn?", "Chọn đối tượng mặc định mỗi khi bạn chia sẻ bài đăng mới trên Dishcision.", "Chỉ áp dụng cho bài đăng mới", "Lựa chọn này không thay đổi các bài đã đăng. Bạn vẫn có thể chọn đối tượng riêng khi tạo từng bài đăng."));
        }

        if (btnProfileInfo != null) {
            btnProfileInfo.setOnClickListener(v -> openRadioChoice("profile_info", "Thông tin cá nhân", "Ai có thể xem nội dung của bạn", "Ai có thể xem thông tin cá nhân của bạn?", "Chọn đối tượng được xem thông tin cá nhân bạn chia sẻ trên hồ sơ.", "Thông tin của bạn, bạn quyết định", "Với \"Chỉ mình tôi\", thông tin cá nhân chỉ hiển thị với bạn. Lựa chọn này không thay đổi đối tượng xem bài đăng."));
        }

        if (btnFollowingList != null) {
            btnFollowingList.setOnClickListener(v -> openRadioChoice("following_list", "Danh sách đang theo dõi", "Ai có thể xem nội dung của bạn", "Ai có thể xem danh sách bạn đang theo dõi?", "Quyết định ai được xem những tài khoản bạn theo dõi trên Dishcision.", "Chỉ thay đổi quyền xem danh sách", "Lựa chọn này không thay đổi những người bạn đang theo dõi hoặc quyền xem bài đăng của bạn."));
        }

        if (btnFindEmail != null) {
            btnFindEmail.setOnClickListener(v -> openRadioChoice("find_email", "Tìm bạn bằng email", "Tìm thấy và liên hệ với bạn", "Ai có thể tìm thấy bạn bằng email?", "Chọn ai có thể dùng email để tìm thấy tài khoản của bạn trên Dishcision.", "Email vẫn được giữ riêng tư", "Cài đặt này chỉ kiểm soát việc tìm tài khoản bằng email, không hiển thị email của bạn cho người khác."));
        }

        if (btnFriendRequests != null) {
            btnFriendRequests.setOnClickListener(v -> openRadioChoice("friend_requests", "Gửi lời mời kết bạn", "Tìm thấy và liên hệ với bạn", "Ai có thể gửi lời mời kết bạn cho bạn?", "Chọn những người được gửi lời mời để kết nối với bạn trên Dishcision.", "Kết nối qua những người bạn chung", "Chỉ người có ít nhất một bạn chung mới có thể gửi lời mời. Danh sách bạn bè hiện tại của bạn không thay đổi."));
        }

        if (btnMessages != null) {
            btnMessages.setOnClickListener(v -> openRadioChoice("messages", "Gửi tin nhắn cho bạn", "Tìm thấy và liên hệ với bạn", "Ai có thể gửi tin nhắn cho bạn?", "Chọn đối tượng được phép liên hệ với bạn qua tin nhắn trên Dishcision.", "Trò chuyện trong phạm vi bạn chọn", "Hiện tại, chỉ bạn bè được gửi tin nhắn cho bạn. Cài đặt này không thay đổi quyền xem hồ sơ hoặc bài đăng."));
        }

        if (btnComments != null) {
            btnComments.setOnClickListener(v -> openRadioChoice("comments", "Bình luận bài đăng của bạn", "Quản lý tương tác", "Ai có thể bình luận trên bài đăng của bạn?", "Chọn những người được tham gia trò chuyện dưới các bài đăng của bạn.", "Quyền xem bài đăng vẫn được giữ nguyên", "Một người chỉ có thể bình luận khi họ cũng được xem bài đăng. Chọn \"Mọi người\" không làm bài đăng trở thành công khai."));
        }

        if (btnBlocked != null) {
            btnBlocked.setOnClickListener(v -> {
                Intent intent = new Intent(PrivacySettingsActivity.this, BlockedAccountsActivity.class);
                startActivity(intent);
            });
        }

        if (btnRestricted != null) {
            btnRestricted.setOnClickListener(v -> {
                Intent intent = new Intent(PrivacySettingsActivity.this, RestrictedAccountsActivity.class);
                startActivity(intent);
            });
        }

        if (btnLearnMorePrivacy != null) {
            btnLearnMorePrivacy.setOnClickListener(v -> {
                Intent intent = new Intent(PrivacySettingsActivity.this, PrivacyPolicyActivity.class);
                startActivity(intent);
            });
        }
    }

    private void openRadioChoice(String key, String title, String category, String qTitle, String qDesc, String infoTitle, String infoDesc) {
        Intent intent = new Intent(PrivacySettingsActivity.this, PrivacyRadioChoiceActivity.class);
        intent.putExtra("SETTING_KEY", key);
        intent.putExtra("TITLE", title);
        intent.putExtra("CATEGORY", category);
        intent.putExtra("QUESTION_TITLE", qTitle);
        intent.putExtra("QUESTION_DESC", qDesc);
        intent.putExtra("INFO_TITLE", infoTitle);
        intent.putExtra("INFO_DESC", infoDesc);
        startActivity(intent);
    }

    private void fetchPrivacySettingsFromBackend() {
        ApiClient.getApiService().getPrivacySettings("Bearer dummy_token").enqueue(new Callback<PrivacySettingsResponse>() {
            @Override
            public void onResponse(@NonNull Call<PrivacySettingsResponse> call, @NonNull Response<PrivacySettingsResponse> response) {
                if (response.isSuccessful() && response.body() != null && response.body().getData() != null) {
                    currentPrivacy = response.body().getData();
                    updateUIFromModel();
                }
            }

            @Override
            public void onFailure(@NonNull Call<PrivacySettingsResponse> call, @NonNull Throwable t) {
                updateUIFromModel();
            }
        });
    }

    private void updateUIFromModel() {
        if (tvNewPostsValue != null) tvNewPostsValue.setText(formatValue(currentPrivacy.getNewPosts()));
        if (tvProfileInfoValue != null) tvProfileInfoValue.setText(formatValue(currentPrivacy.getProfileInfo()));
        if (tvFollowingListValue != null) tvFollowingListValue.setText(formatValue(currentPrivacy.getFollowingList()));
        if (tvFindEmailValue != null) tvFindEmailValue.setText(formatValue(currentPrivacy.getFindEmail()));
        if (tvFriendRequestsValue != null) tvFriendRequestsValue.setText(formatValue(currentPrivacy.getFriendRequests()));
        if (tvMessagesValue != null) tvMessagesValue.setText(formatValue(currentPrivacy.getMessages()));
        if (tvCommentsValue != null) tvCommentsValue.setText(formatValue(currentPrivacy.getComments()));
    }

    private String formatValue(String val) {
        if (val == null) return "Bạn bè";
        switch (val) {
            case "public": return "Công khai";
            case "friends": return "Bạn bè";
            case "only_me": return "Chỉ mình tôi";
            case "friends_of_friends": return "Bạn của bạn bè";
            case "no_one": return "Không ai";
            default: return "Bạn bè";
        }
    }
}
