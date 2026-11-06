package com.example.dishcision;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.bottomsheet.BottomSheetDialog;

public class StoryActivity extends AppCompatActivity {

    public static final String EXTRA_STORE_NAME = "extra_store_name";

    private ImageView btnCloseStory;
    private TextView tvStoryStoreName;
    private TextView tvStoryTitle;
    private EditText edtReply;
    private ImageView btnStoryLike;
    private ImageView btnStoryShare;

    private View btnPrevStory;
    private View btnNextStory;

    private View segment1;
    private View segment2;
    private View segment3;

    private boolean isStoryLiked = false;
    private int currentStoryIndex = 0;
    private String storeName = "Phở Thìn";

    private final String[][] STORY_DATA = {
            {"Một bát phở nóng, một chút bình yên.", "Nước dùng ninh xương đậm đà 24h."},
            {"Món mới tuần này: Phở Bò Tái Lăn Trứng Chèn", "Thơm nức mùi tỏi phi giòn tan."},
            {"Ưu đãi độc quyền giảm 20% hôm nay", "Nhập mã DISHCISION20 khi đến quán."}
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_story);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main_story), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        initViews();
        setupData();
        setupListeners();
    }

    private void initViews() {
        btnCloseStory = findViewById(R.id.btnCloseStory);
        tvStoryStoreName = findViewById(R.id.tvStoryStoreName);
        tvStoryTitle = findViewById(R.id.tvStoryTitle);
        edtReply = findViewById(R.id.edtReply);

        btnStoryLike = findViewById(R.id.btnStoryLike);
        btnStoryShare = findViewById(R.id.btnStoryShare);

        btnPrevStory = findViewById(R.id.btnPrevStory);
        btnNextStory = findViewById(R.id.btnNextStory);

        segment1 = findViewById(R.id.segment1);
        segment2 = findViewById(R.id.segment2);
        segment3 = findViewById(R.id.segment3);
    }

    private void setupData() {
        String name = getIntent().getStringExtra(EXTRA_STORE_NAME);
        if (name != null && !name.isEmpty()) {
            storeName = name;
        }
        if (tvStoryStoreName != null) tvStoryStoreName.setText(storeName);
        if (tvStoryTitle != null) tvStoryTitle.setText(storeName);
        if (edtReply != null) edtReply.setHint("Trả lời " + storeName + "...");

        updateStoryView();
    }

    private void updateStoryView() {
        if (currentStoryIndex < 0) currentStoryIndex = 0;
        if (currentStoryIndex >= STORY_DATA.length) currentStoryIndex = STORY_DATA.length - 1;

        TextView tvSubtitle = findViewById(R.id.tvStorySubtitle);
        if (tvSubtitle != null) {
            tvSubtitle.setText(STORY_DATA[currentStoryIndex][0]);
        }

        // Update progress bar segment highlights
        if (segment1 != null) segment1.setBackgroundColor(currentStoryIndex >= 0 ? 0xFFFFFFFF : 0x66FFFFFF);
        if (segment2 != null) segment2.setBackgroundColor(currentStoryIndex >= 1 ? 0xFFFFFFFF : 0x66FFFFFF);
        if (segment3 != null) segment3.setBackgroundColor(currentStoryIndex >= 2 ? 0xFFFFFFFF : 0x66FFFFFF);
    }

    private void setupListeners() {
        if (btnCloseStory != null) {
            btnCloseStory.setOnClickListener(v -> finish());
        }

        // Left tap zone -> Prev story
        if (btnPrevStory != null) {
            btnPrevStory.setOnClickListener(v -> {
                if (currentStoryIndex > 0) {
                    currentStoryIndex--;
                    updateStoryView();
                } else {
                    Toast.makeText(this, "Đã ở Story đầu tiên", Toast.LENGTH_SHORT).show();
                }
            });
        }

        // Right tap zone -> Next story
        if (btnNextStory != null) {
            btnNextStory.setOnClickListener(v -> {
                if (currentStoryIndex < STORY_DATA.length - 1) {
                    currentStoryIndex++;
                    updateStoryView();
                } else {
                    finish(); // Finish story viewing when reaching end
                }
            });
        }

        // Heart like button
        if (btnStoryLike != null) {
            btnStoryLike.setOnClickListener(v -> {
                isStoryLiked = !isStoryLiked;
                if (isStoryLiked) {
                    btnStoryLike.setImageResource(R.drawable.ic_heart_filled_red);
                    Toast.makeText(this, "Đã thích Story của " + storeName, Toast.LENGTH_SHORT).show();
                } else {
                    btnStoryLike.setImageResource(R.drawable.ic_heart);
                }
            });
        }

        // Share button
        if (btnStoryShare != null) {
            btnStoryShare.setOnClickListener(v -> openShareBottomSheet());
        }

        // Reply comment field
        if (edtReply != null) {
            edtReply.setOnEditorActionListener((v, actionId, event) -> {
                if (actionId == EditorInfo.IME_ACTION_SEND || actionId == EditorInfo.IME_ACTION_DONE) {
                    String replyText = edtReply.getText().toString().trim();
                    if (!replyText.isEmpty()) {
                        Toast.makeText(this, "Đã gửi phản hồi tới " + storeName + ": " + replyText, Toast.LENGTH_SHORT).show();
                        edtReply.setText("");
                    }
                    return true;
                }
                return false;
            });
        }
    }

    private void openShareBottomSheet() {
        BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(this);
        View dialogView = getLayoutInflater().inflate(R.layout.layout_dialog_share_post, null);
        bottomSheetDialog.setContentView(dialogView);

        View btnCloseShare = dialogView.findViewById(R.id.btnCloseShare);
        View btnSendDishcision = dialogView.findViewById(R.id.btnSendDishcision);
        View btnCopyLink = dialogView.findViewById(R.id.btnCopyLink);

        if (btnCloseShare != null) {
            btnCloseShare.setOnClickListener(v -> bottomSheetDialog.dismiss());
        }

        if (btnSendDishcision != null) {
            btnSendDishcision.setOnClickListener(v -> {
                Toast.makeText(this, "Đã chia sẻ Story!", Toast.LENGTH_SHORT).show();
                bottomSheetDialog.dismiss();
            });
        }

        if (btnCopyLink != null) {
            btnCopyLink.setOnClickListener(v -> {
                Toast.makeText(this, "Đã sao chép liên kết Story!", Toast.LENGTH_SHORT).show();
                bottomSheetDialog.dismiss();
            });
        }

        bottomSheetDialog.show();
    }
}
