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
import androidx.appcompat.widget.PopupMenu;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.dishcision.network.ApiClient;
import com.example.dishcision.network.SavePostButton;
import com.example.dishcision.network.ImageLoader;
import com.example.dishcision.network.FeedPostsResponse;
import com.google.android.material.bottomsheet.BottomSheetDialog;

import java.util.List;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MyPostsActivity extends AppCompatActivity {

    private ImageView btnBack;
    private ImageView btnNotification;

    private TextView tvAuthorName1;
    private TextView tvAuthorName2;
    private TextView tvCaption1;
    private TextView tvCaption2;
    private TextView tvHashtags1;
    private TextView tvHashtags2;

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
    private ImageView btnOptions1;
    private ImageView btnOptions2;

    private TextView tvLocation1;
    private TextView tvLocation2;
    private TextView tvPostTime1;
    private TextView tvPostTime2;
    private ImageView imgAvatar1;
    private ImageView imgAvatar2;
    private ImageView imgPost1;
    private ImageView imgPost2;

    private boolean isLiked1 = false;
    private boolean isLiked2 = false;
    private SavePostButton saveButton1;
    private SavePostButton saveButton2;

    public static final String EXTRA_POST_INDEX = "POST_INDEX";

    // Single source of truth for comment counts so the button, "Xem tất cả" and the sheet agree
    private int commentCount1 = 84;
    private int commentCount2 = 84;

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
        updateCommentCountViews();
        scrollToRequestedPost();
        fetchMyPostsFromBackend();
    }

    /** Scrolls so the post tapped in the profile grid sits at the top; earlier posts stay above it. */
    private void scrollToRequestedPost() {
        int index = getIntent().getIntExtra(EXTRA_POST_INDEX, 1);
        if (index <= 1) return;

        android.widget.ScrollView scrollFeed = findViewById(R.id.scrollFeed);
        LinearLayout feedContent = findViewById(R.id.feedContent);
        View target = findViewById(R.id.cardPost2); // only 2 post cards exist; later grid items map to the last one
        if (scrollFeed == null || feedContent == null || target == null) return;

        scrollFeed.post(() -> {
            int targetTop = target.getTop() - feedContent.getPaddingTop();
            // Make sure there is enough room below so the target can actually reach the top edge
            int spaceBelow = feedContent.getHeight() - targetTop;
            int missing = scrollFeed.getHeight() - spaceBelow;
            if (missing > 0) {
                feedContent.setPadding(feedContent.getPaddingLeft(), feedContent.getPaddingTop(),
                        feedContent.getPaddingRight(), feedContent.getPaddingBottom() + missing);
                scrollFeed.post(() -> scrollFeed.scrollTo(0, targetTop));
            } else {
                scrollFeed.scrollTo(0, targetTop);
            }
        });
    }

    private void updateCommentCountViews() {
        setCommentTexts(btnComment1, btnViewComments1, commentCount1);
        setCommentTexts(btnComment2, btnViewComments2, commentCount2);
    }

    private void setCommentTexts(LinearLayout btnComment, TextView btnViewAll, int count) {
        if (btnComment != null) {
            TextView txt = (TextView) btnComment.getChildAt(1);
            if (txt != null) txt.setText(count + " Bình luận");
        }
        if (btnViewAll != null) {
            btnViewAll.setText("Xem tất cả " + count + " bình luận");
        }
    }

    private void initViews() {
        btnBack = findViewById(R.id.btnBack);
        btnNotification = findViewById(R.id.btnNotification);

        tvAuthorName1 = findViewById(R.id.tvAuthorName1);
        tvAuthorName2 = findViewById(R.id.tvAuthorName2);
        tvCaption1 = findViewById(R.id.tvCaption1);
        tvCaption2 = findViewById(R.id.tvCaption2);
        tvHashtags1 = findViewById(R.id.tvHashtags1);
        tvHashtags2 = findViewById(R.id.tvHashtags2);

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
        btnOptions1 = findViewById(R.id.btnOptions1);
        btnOptions2 = findViewById(R.id.btnOptions2);

        tvLocation1 = findViewById(R.id.tvLocation1);
        tvLocation2 = findViewById(R.id.tvLocation2);
        tvPostTime1 = findViewById(R.id.tvPostTime1);
        tvPostTime2 = findViewById(R.id.tvPostTime2);
        imgAvatar1 = findViewById(R.id.imgAvatar1);
        imgAvatar2 = findViewById(R.id.imgAvatar2);
        imgPost1 = findViewById(R.id.imgPost1);
        imgPost2 = findViewById(R.id.imgPost2);

        // Preload default high quality food images
        if (imgPost1 != null) {
            ImageLoader.loadImage(imgPost1, "https://images.unsplash.com/photo-1582878826629-29b7ad1cdc43?w=800");
        }
        if (imgPost2 != null) {
            ImageLoader.loadImage(imgPost2, "https://images.unsplash.com/photo-1626804475297-41608ea09aeb?w=800");
        }
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
        View.OnClickListener comments1 = v -> openCommentsBottomSheet(commentCount1);
        View.OnClickListener comments2 = v -> openCommentsBottomSheet(commentCount2);
        if (btnComment1 != null) btnComment1.setOnClickListener(comments1);
        if (btnComment2 != null) btnComment2.setOnClickListener(comments2);
        if (btnViewComments1 != null) btnViewComments1.setOnClickListener(comments1);
        if (btnViewComments2 != null) btnViewComments2.setOnClickListener(comments2);

        // Share
        View.OnClickListener shareListener = v -> openShareBottomSheet();
        if (btnShare1 != null) btnShare1.setOnClickListener(shareListener);
        if (btnShare2 != null) btnShare2.setOnClickListener(shareListener);

        // Bookmark: save the post to "Bài viết đã lưu"
        saveButton1 = new SavePostButton(this, btnBookmark1);
        saveButton2 = new SavePostButton(this, btnBookmark2);

        if (btnOptions1 != null) {
            btnOptions1.setOnClickListener(v -> openPostOptionsBottomSheet(1));
        }
        if (btnOptions2 != null) {
            btnOptions2.setOnClickListener(v -> openPostOptionsBottomSheet(2));
        }
    }

    private void openPostOptionsBottomSheet(int postIndex) {
        BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(this);
        View dialogView = getLayoutInflater().inflate(R.layout.layout_dialog_post_options, null);
        bottomSheetDialog.setContentView(dialogView);

        View btnClose = dialogView.findViewById(R.id.btnCloseOptions);
        View layoutEditPost = dialogView.findViewById(R.id.layoutEditPost);
        View layoutCopyLink = dialogView.findViewById(R.id.layoutCopyLink);
        View layoutSharePost = dialogView.findViewById(R.id.layoutSharePost);
        View layoutArchivePost = dialogView.findViewById(R.id.layoutArchivePost);
        View layoutDeletePost = dialogView.findViewById(R.id.layoutDeletePost);

        if (btnClose != null) {
            btnClose.setOnClickListener(v -> bottomSheetDialog.dismiss());
        }

        if (layoutEditPost != null) {
            layoutEditPost.setOnClickListener(v -> {
                bottomSheetDialog.dismiss();
                showEditPostDialog(postIndex);
            });
        }

        if (layoutCopyLink != null) {
            layoutCopyLink.setOnClickListener(v -> {
                bottomSheetDialog.dismiss();
                android.content.ClipboardManager clipboard = (android.content.ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
                android.content.ClipData clip = android.content.ClipData.newPlainText("Post Link", "https://dishcision.app/posts/" + postIndex);
                if (clipboard != null) clipboard.setPrimaryClip(clip);
                Toast.makeText(this, "Đã sao chép liên kết bài viết", Toast.LENGTH_SHORT).show();
            });
        }

        if (layoutSharePost != null) {
            layoutSharePost.setOnClickListener(v -> {
                bottomSheetDialog.dismiss();
                openShareBottomSheet();
            });
        }

        if (layoutArchivePost != null) {
            layoutArchivePost.setOnClickListener(v -> {
                bottomSheetDialog.dismiss();
                Toast.makeText(this, "Đã lưu trữ bài viết", Toast.LENGTH_SHORT).show();
            });
        }

        if (layoutDeletePost != null) {
            layoutDeletePost.setOnClickListener(v -> {
                bottomSheetDialog.dismiss();
                showDeleteConfirmDialog(postIndex);
            });
        }

        bottomSheetDialog.show();
    }

    private void showEditPostDialog(int postIndex) {
        TextView tvCaption = postIndex == 1 ? tvCaption1 : tvCaption2;
        String currentCaption = tvCaption != null ? tvCaption.getText().toString() : "";

        android.widget.EditText input = new android.widget.EditText(this);
        input.setText(currentCaption);
        input.setPadding(40, 30, 40, 30);

        new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("Chỉnh sửa bài viết")
                .setView(input)
                .setPositiveButton("Lưu", (dialog, which) -> {
                    String updated = input.getText().toString().trim();
                    if (!updated.isEmpty() && tvCaption != null) {
                        tvCaption.setText(updated);
                        Toast.makeText(this, "Đã cập nhật bài viết thành công", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Hủy", null)
                .show();
    }

    private void showDeleteConfirmDialog(int postIndex) {
        new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("Xóa bài viết?")
                .setMessage("Bạn có chắc chắn muốn xóa bài viết này không? Hành động này không thể hoàn tác.")
                .setPositiveButton("Xóa", (dialog, which) -> {
                    View card = postIndex == 1
                            ? findViewById(R.id.cardPost1)
                            : findViewById(R.id.cardPost2);
                    if (card != null) {
                        card.setVisibility(View.GONE);
                    }
                    Toast.makeText(this, "Đã xóa bài viết thành công", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("Hủy", null)
                .show();
    }

    private void openCommentsBottomSheet(int commentCount) {
        BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(this);
        View dialogView = getLayoutInflater().inflate(R.layout.layout_dialog_comments, null);
        bottomSheetDialog.setContentView(dialogView);

        TextView tvCommentsTitle = dialogView.findViewById(R.id.tvCommentsTitle);
        if (tvCommentsTitle != null) {
            tvCommentsTitle.setText("Bình luận  " + commentCount);
        }

        View btnCloseComments = dialogView.findViewById(R.id.btnCloseComments);
        if (btnCloseComments != null) {
            btnCloseComments.setOnClickListener(v -> bottomSheetDialog.dismiss());
        }

        TextView btnSortComments = dialogView.findViewById(R.id.btnSortComments);
        if (btnSortComments != null) {
            btnSortComments.setOnClickListener(v -> {
                PopupMenu popupMenu = new PopupMenu(MyPostsActivity.this, btnSortComments);
                popupMenu.getMenu().add("Phù hợp nhất");
                popupMenu.getMenu().add("Mới nhất");
                popupMenu.getMenu().add("Tất cả bình luận");
                popupMenu.setOnMenuItemClickListener(item -> {
                    btnSortComments.setText(item.getTitle() + " ▼");
                    Toast.makeText(MyPostsActivity.this, "Sắp xếp: " + item.getTitle(), Toast.LENGTH_SHORT).show();
                    return true;
                });
                popupMenu.show();
            });
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

    private void fetchMyPostsFromBackend() {
        String authHeader = ApiClient.getAuthHeader(this);
        ApiClient.getApiService().getMyPosts(authHeader).enqueue(new Callback<FeedPostsResponse>() {
            @Override
            public void onResponse(Call<FeedPostsResponse> call, Response<FeedPostsResponse> response) {
                if (response.isSuccessful() && response.body() != null && response.body().getData() != null) {
                    List<FeedPostsResponse.PostItem> posts = response.body().getData();
                    bindMyPosts(posts);
                }
            }

            @Override
            public void onFailure(Call<FeedPostsResponse> call, Throwable t) {}
        });
    }

    private void bindMyPosts(List<FeedPostsResponse.PostItem> posts) {
        if (posts == null || posts.isEmpty()) return;

        if (posts.size() > 0) {
            FeedPostsResponse.PostItem p1 = posts.get(0);
            if (saveButton1 != null) saveButton1.bind(p1.postId, p1.isSaved, p1.author != null ? p1.author.fullName : null);
            if (p1.author != null && tvAuthorName1 != null && p1.author.fullName != null) {
                tvAuthorName1.setText(p1.author.fullName);
            }
            if (p1.locationUrl != null && tvLocation1 != null) {
                tvLocation1.setText(p1.locationUrl);
            }
            if (p1.author != null && p1.author.avatarUrl != null && imgAvatar1 != null) {
                ImageLoader.loadAvatar(imgAvatar1, p1.author.avatarUrl);
            }
            if (p1.images != null && !p1.images.isEmpty() && imgPost1 != null) {
                ImageLoader.loadImage(imgPost1, p1.images.get(0).imageUrl);
            }
            if (tvCaption1 != null && p1.content != null) {
                String authorPrefix = (p1.author != null && p1.author.username != null) ? p1.author.username + " " : "";
                tvCaption1.setText(authorPrefix + p1.content);
            }
            if (tvHashtags1 != null && p1.hashtags != null && !p1.hashtags.isEmpty()) {
                StringBuilder sb = new StringBuilder();
                for (String h : p1.hashtags) {
                    sb.append("#").append(h).append(" ");
                }
                tvHashtags1.setText(sb.toString().trim());
            }
            if (btnLike1 != null && p1.likesCount > 0) {
                TextView txt = (TextView) btnLike1.getChildAt(1);
                if (txt != null) txt.setText(p1.likesCount + " Thích");
            }
            if (p1.commentsCount > 0) {
                commentCount1 = p1.commentsCount;
                setCommentTexts(btnComment1, btnViewComments1, commentCount1);
            }
            if (tvPostTime1 != null && p1.createdAt != null) {
                tvPostTime1.setText(formatTime(p1.createdAt) + " • 🌐");
            }
        }

        if (posts.size() > 1) {
            FeedPostsResponse.PostItem p2 = posts.get(1);
            if (saveButton2 != null) saveButton2.bind(p2.postId, p2.isSaved, p2.author != null ? p2.author.fullName : null);
            if (p2.author != null && tvAuthorName2 != null && p2.author.fullName != null) {
                tvAuthorName2.setText(p2.author.fullName);
            }
            if (p2.locationUrl != null && tvLocation2 != null) {
                tvLocation2.setText(p2.locationUrl);
            }
            if (p2.author != null && p2.author.avatarUrl != null && imgAvatar2 != null) {
                ImageLoader.loadAvatar(imgAvatar2, p2.author.avatarUrl);
            }
            if (p2.images != null && !p2.images.isEmpty() && imgPost2 != null) {
                ImageLoader.loadImage(imgPost2, p2.images.get(0).imageUrl);
            }
            if (tvCaption2 != null && p2.content != null) {
                String authorPrefix = (p2.author != null && p2.author.username != null) ? p2.author.username + " " : "";
                tvCaption2.setText(authorPrefix + p2.content);
            }
            if (tvHashtags2 != null && p2.hashtags != null && !p2.hashtags.isEmpty()) {
                StringBuilder sb = new StringBuilder();
                for (String h : p2.hashtags) {
                    sb.append("#").append(h).append(" ");
                }
                tvHashtags2.setText(sb.toString().trim());
            }
            if (btnLike2 != null && p2.likesCount > 0) {
                TextView txt = (TextView) btnLike2.getChildAt(1);
                if (txt != null) txt.setText(p2.likesCount + " Thích");
            }
            if (p2.commentsCount > 0) {
                commentCount2 = p2.commentsCount;
                setCommentTexts(btnComment2, btnViewComments2, commentCount2);
            }
            if (tvPostTime2 != null && p2.createdAt != null) {
                tvPostTime2.setText(formatTime(p2.createdAt) + " • 🌐");
            }
        }
    }

    public static String formatTime(String isoDateString) {
        if (isoDateString == null || isoDateString.trim().isEmpty()) {
            return "Vừa xong";
        }
        try {
            java.text.SimpleDateFormat sdf;
            if (isoDateString.contains(".")) {
                sdf = new java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", java.util.Locale.US);
            } else {
                sdf = new java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", java.util.Locale.US);
            }
            sdf.setTimeZone(java.util.TimeZone.getTimeZone("UTC"));
            java.util.Date postDate = sdf.parse(isoDateString);
            if (postDate == null) return "Vừa xong";

            long now = System.currentTimeMillis();
            long diff = now - postDate.getTime();
            if (diff < 0) diff = 0;

            long diffSeconds = diff / 1000;
            long diffMinutes = diffSeconds / 60;
            long diffHours = diffMinutes / 60;
            long diffDays = diffHours / 24;

            if (diffSeconds < 60) {
                return "Vừa xong";
            } else if (diffMinutes < 60) {
                return diffMinutes + " phút trước";
            } else if (diffHours < 24) {
                return diffHours + " giờ trước";
            } else if (diffDays < 7) {
                return diffDays + " ngày trước";
            } else {
                java.util.Calendar calNow = java.util.Calendar.getInstance();
                java.util.Calendar calPost = java.util.Calendar.getInstance();
                calPost.setTime(postDate);
                if (calNow.get(java.util.Calendar.YEAR) == calPost.get(java.util.Calendar.YEAR)) {
                    return new java.text.SimpleDateFormat("d 'tháng' M", new java.util.Locale("vi", "VN")).format(postDate);
                } else {
                    return new java.text.SimpleDateFormat("d 'tháng' M, yyyy", new java.util.Locale("vi", "VN")).format(postDate);
                }
            }
        } catch (Exception e) {
            return "Vừa xong";
        }
    }
}
