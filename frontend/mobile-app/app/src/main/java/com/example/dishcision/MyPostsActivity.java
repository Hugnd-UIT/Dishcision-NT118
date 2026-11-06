package com.example.dishcision;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.bottomsheet.BottomSheetDialog;

public class MyPostsActivity extends AppCompatActivity {

    private ImageView btnBack;
    private ImageView btnNotification;

    private LinearLayout btnLike1;
    private LinearLayout btnLike2;
    private LinearLayout btnComment1;
    private LinearLayout btnComment2;
    private TextView btnViewComments1;
    private TextView btnViewComments2;

    private ImageView btnShare1;
    private ImageView btnShare2;
    private ImageView btnBookmark1;
    private ImageView btnBookmark2;

    private boolean isLiked1 = false;
    private boolean isLiked2 = false;
    private boolean isBookmarked1 = false;
    private boolean isBookmarked2 = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_my_posts);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main_my_posts), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        initViews();
        setupListeners();
    }

    private void initViews() {
        btnBack = findViewById(R.id.btnBack);
        btnNotification = findViewById(R.id.btnNotification);

        btnLike1 = findViewById(R.id.btnLike1);
        btnLike2 = findViewById(R.id.btnLike2);
        btnComment1 = findViewById(R.id.btnComment1);
        btnComment2 = findViewById(R.id.btnComment2);
        btnViewComments1 = findViewById(R.id.btnViewComments1);
        btnViewComments2 = findViewById(R.id.btnViewComments2);

        btnShare1 = findViewById(R.id.btnShare1);
        btnShare2 = findViewById(R.id.btnShare2);
        btnBookmark1 = findViewById(R.id.btnBookmark1);
        btnBookmark2 = findViewById(R.id.btnBookmark2);
    }

    private void setupListeners() {
        if (btnBack != null) {
            btnBack.setOnClickListener(v -> finish());
        }

        if (btnNotification != null) {
            btnNotification.setOnClickListener(v -> {
                Intent intent = new Intent(MyPostsActivity.this, NotificationsActivity.class);
                startActivity(intent);
            });
        }

        // Heart Like buttons
        if (btnLike1 != null) {
            btnLike1.setOnClickListener(v -> {
                isLiked1 = !isLiked1;
                ImageView img = (ImageView) btnLike1.getChildAt(0);
                TextView txt = (TextView) btnLike1.getChildAt(1);
                if (isLiked1) {
                    img.setImageResource(R.drawable.ic_heart_filled_red);
                    txt.setText("1.2k Thích");
                    txt.setTextColor(Color.parseColor("#E53935"));
                } else {
                    img.setImageResource(R.drawable.ic_heart);
                    txt.setText("1.2k Thích");
                    txt.setTextColor(Color.parseColor("#1E232C"));
                }
            });
        }

        if (btnLike2 != null) {
            btnLike2.setOnClickListener(v -> {
                isLiked2 = !isLiked2;
                ImageView img = (ImageView) btnLike2.getChildAt(0);
                TextView txt = (TextView) btnLike2.getChildAt(1);
                if (isLiked2) {
                    img.setImageResource(R.drawable.ic_heart_filled_red);
                    txt.setText("1.2k Thích");
                    txt.setTextColor(Color.parseColor("#E53935"));
                } else {
                    img.setImageResource(R.drawable.ic_heart);
                    txt.setText("1.2k Thích");
                    txt.setTextColor(Color.parseColor("#1E232C"));
                }
            });
        }

        // Comments
        View.OnClickListener commentsListener = v -> openCommentsBottomSheet();
        if (btnComment1 != null) btnComment1.setOnClickListener(commentsListener);
        if (btnComment2 != null) btnComment2.setOnClickListener(commentsListener);
        if (btnViewComments1 != null) btnViewComments1.setOnClickListener(commentsListener);
        if (btnViewComments2 != null) btnViewComments2.setOnClickListener(commentsListener);

        // Share
        View.OnClickListener shareListener = v -> openShareBottomSheet();
        if (btnShare1 != null) btnShare1.setOnClickListener(shareListener);
        if (btnShare2 != null) btnShare2.setOnClickListener(shareListener);

        // Bookmark
        if (btnBookmark1 != null) {
            btnBookmark1.setOnClickListener(v -> {
                isBookmarked1 = !isBookmarked1;
                btnBookmark1.setImageResource(isBookmarked1 ? R.drawable.ic_bookmark_filled : R.drawable.ic_bookmark);
                Toast.makeText(this, isBookmarked1 ? "Đã lưu vào bộ sưu tập" : "Đã bỏ lưu", Toast.LENGTH_SHORT).show();
            });
        }

        if (btnBookmark2 != null) {
            btnBookmark2.setOnClickListener(v -> {
                isBookmarked2 = !isBookmarked2;
                btnBookmark2.setImageResource(isBookmarked2 ? R.drawable.ic_bookmark_filled : R.drawable.ic_bookmark);
                Toast.makeText(this, isBookmarked2 ? "Đã lưu vào bộ sưu tập" : "Đã bỏ lưu", Toast.LENGTH_SHORT).show();
            });
        }
    }

    private void openCommentsBottomSheet() {
        BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(this);
        View dialogView = getLayoutInflater().inflate(R.layout.layout_dialog_comments, null);
        bottomSheetDialog.setContentView(dialogView);

        View btnCloseComments = dialogView.findViewById(R.id.btnCloseComments);
        if (btnCloseComments != null) {
            btnCloseComments.setOnClickListener(v -> bottomSheetDialog.dismiss());
        }

        bottomSheetDialog.show();
    }

    private void openShareBottomSheet() {
        BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(this);
        View dialogView = getLayoutInflater().inflate(R.layout.layout_dialog_share_post, null);
        bottomSheetDialog.setContentView(dialogView);

        View btnCloseShare = dialogView.findViewById(R.id.btnCloseShare);
        if (btnCloseShare != null) {
            btnCloseShare.setOnClickListener(v -> bottomSheetDialog.dismiss());
        }

        bottomSheetDialog.show();
    }
}
