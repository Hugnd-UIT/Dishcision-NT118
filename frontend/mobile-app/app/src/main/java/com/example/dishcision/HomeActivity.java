package com.example.dishcision;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.PopupMenu;
import androidx.core.graphics.Insets;
import androidx.core.view.GravityCompat;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.example.dishcision.network.ApiClient;
import com.example.dishcision.network.FeedPostsResponse;
import com.example.dishcision.network.UserProfileResponse;
import com.google.android.material.bottomsheet.BottomSheetDialog;

import java.util.List;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class HomeActivity extends AppCompatActivity {

    private DrawerLayout drawerLayout;
    private LinearLayout btnOpenSidebar;
    private ImageView btnNotification;
    private EditText edtSearch;
    private ImageView imgSearchIcon;
    private RelativeLayout btnFilter;

    // Bottom Navigation Bar Items
    private LinearLayout navHome;
    private LinearLayout navDecide;
    private LinearLayout navCreate;
    private LinearLayout navMessages;
    private LinearLayout navProfile;

    // Sidebar Items
    private LinearLayout menuForYou;
    private LinearLayout menuFavorites;
    private LinearLayout menuFollowing;
    private LinearLayout menuReviews;
    private LinearLayout menuLogout;
    private TextView tvSidebarUserName;
    private TextView tvSidebarUserHandle;

    // Feed Post Interactive Elements
    private RelativeLayout containerAvatar1;
    private RelativeLayout containerAvatar2;
    private RelativeLayout containerAvatar3;

    private RelativeLayout containerPostImage1;
    private RelativeLayout containerPostImage2;
    private RelativeLayout containerPostImage3;

    private TextView tvAuthorName1, tvUsername1, tvLocation1;
    private TextView tvAuthorName2, tvLocation2;
    private TextView tvAuthorName3, tvUsername3, tvLocation3;
    private TextView tvPostText1, tvPostText2, tvPostText3;

    private TextView tvHashtags1;
    private TextView tvHashtags2;
    private TextView tvHashtags3;

    private LinearLayout btnLike1;
    private LinearLayout btnLike2;
    private LinearLayout btnLike3;

    private LinearLayout btnComment1;
    private LinearLayout btnComment2;
    private LinearLayout btnComment3;

    private ImageView btnShare1;
    private ImageView btnShare2;
    private ImageView btnShare3;

    private ImageView btnBookmark1;
    private ImageView btnBookmark2;
    private ImageView btnBookmark3;

    private boolean isLiked1 = false;
    private boolean isLiked2 = false;
    private boolean isLiked3 = false;

    private boolean isBookmarked1 = false;
    private boolean isBookmarked2 = false;
    private boolean isBookmarked3 = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_home);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main_home), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        initViews();
        setupListeners();
        fetchFeedFromBackend();
    }

    private void initViews() {
        drawerLayout = findViewById(R.id.drawerLayout);
        btnOpenSidebar = findViewById(R.id.btnOpenSidebar);
        btnNotification = findViewById(R.id.btnNotification);
        edtSearch = findViewById(R.id.edtSearch);
        imgSearchIcon = findViewById(R.id.imgSearchIcon);
        btnFilter = findViewById(R.id.btnFilter);

        navHome = findViewById(R.id.navHome);
        navDecide = findViewById(R.id.navDecide);
        navCreate = findViewById(R.id.navCreate);
        navMessages = findViewById(R.id.navMessages);
        navProfile = findViewById(R.id.navProfile);

        menuForYou = findViewById(R.id.menuForYou);
        menuFavorites = findViewById(R.id.menuFavorites);
        menuFollowing = findViewById(R.id.menuFollowing);
        menuReviews = findViewById(R.id.menuReviews);
        menuLogout = findViewById(R.id.menuLogout);
        tvSidebarUserName = findViewById(R.id.tvSidebarUserName);
        tvSidebarUserHandle = findViewById(R.id.tvSidebarUserHandle);

        containerAvatar1 = findViewById(R.id.containerAvatar1);
        containerAvatar2 = findViewById(R.id.containerAvatar2);
        containerAvatar3 = findViewById(R.id.containerAvatar3);

        containerPostImage1 = findViewById(R.id.containerPostImage1);
        containerPostImage2 = findViewById(R.id.containerPostImage2);
        containerPostImage3 = findViewById(R.id.containerPostImage3);

        tvAuthorName1 = findViewById(R.id.tvAuthorName1);
        tvUsername1 = findViewById(R.id.tvUsername1);
        tvLocation1 = findViewById(R.id.tvLocation1);

        tvAuthorName2 = findViewById(R.id.tvAuthorName2);
        tvLocation2 = findViewById(R.id.tvLocation2);

        tvAuthorName3 = findViewById(R.id.tvAuthorName3);
        tvUsername3 = findViewById(R.id.tvUsername3);
        tvLocation3 = findViewById(R.id.tvLocation3);

        tvPostText1 = findViewById(R.id.tvPostText1);
        tvPostText2 = findViewById(R.id.tvPostText2);
        tvPostText3 = findViewById(R.id.tvPostText3);

        tvHashtags1 = findViewById(R.id.tvHashtags1);
        tvHashtags2 = findViewById(R.id.tvHashtags2);
        tvHashtags3 = findViewById(R.id.tvHashtags3);

        btnLike1 = findViewById(R.id.btnLike1);
        btnLike2 = findViewById(R.id.btnLike2);
        btnLike3 = findViewById(R.id.btnLike3);

        btnComment1 = findViewById(R.id.btnComment1);
        btnComment2 = findViewById(R.id.btnComment2);
        btnComment3 = findViewById(R.id.btnComment3);

        btnShare1 = findViewById(R.id.btnShare1);
        btnShare2 = findViewById(R.id.btnShare2);
        btnShare3 = findViewById(R.id.btnShare3);

        btnBookmark1 = findViewById(R.id.btnBookmark1);
        btnBookmark2 = findViewById(R.id.btnBookmark2);
        btnBookmark3 = findViewById(R.id.btnBookmark3);
    }

    private void setupListeners() {
        // Top Header
        if (btnOpenSidebar != null && drawerLayout != null) {
            btnOpenSidebar.setOnClickListener(v -> drawerLayout.openDrawer(GravityCompat.START));
        }

        if (btnNotification != null) {
            btnNotification.setOnClickListener(v -> {
                Intent intent = new Intent(HomeActivity.this, NotificationsActivity.class);
                startActivity(intent);
            });
        }

        // Realtime Feed Search
        if (edtSearch != null) {
            edtSearch.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    filterFeed(s.toString().toLowerCase().trim());
                }

                @Override
                public void afterTextChanged(Editable s) {}
            });
        }

        if (imgSearchIcon != null) {
            imgSearchIcon.setOnClickListener(v -> {
                Intent intent = new Intent(HomeActivity.this, SearchActivity.class);
                startActivity(intent);
            });
        }

        if (btnFilter != null) {
            btnFilter.setOnClickListener(v -> {
                Intent intent = new Intent(HomeActivity.this, SearchActivity.class);
                startActivity(intent);
            });
        }

        // Bottom Navigation Bar
        if (navHome != null) {
            navHome.setOnClickListener(v -> {
                android.widget.ScrollView scrollFeed = findViewById(R.id.scrollFeed);
                if (scrollFeed != null) scrollFeed.smoothScrollTo(0, 0);
            });
        }

        if (navDecide != null) {
            navDecide.setOnClickListener(v -> {
                Intent intent = new Intent(HomeActivity.this, DecideActivity.class);
                startActivity(intent);
            });
        }

        if (navCreate != null) {
            navCreate.setOnClickListener(v -> {
                Intent intent = new Intent(HomeActivity.this, CreatePostActivity.class);
                startActivity(intent);
            });
        }

        if (navMessages != null) {
            navMessages.setOnClickListener(v -> {
                Intent intent = new Intent(HomeActivity.this, MessagesInboxActivity.class);
                startActivity(intent);
            });
        }

        if (navProfile != null) {
            navProfile.setOnClickListener(v -> {
                Intent intent = new Intent(HomeActivity.this, ProfileActivity.class);
                startActivity(intent);
            });
        }

        // Sidebar Menu
        if (menuForYou != null) {
            menuForYou.setOnClickListener(v -> {
                if (drawerLayout != null) drawerLayout.closeDrawer(GravityCompat.START);
                Intent intent = new Intent(HomeActivity.this, DecideActivity.class);
                startActivity(intent);
            });
        }

        if (menuFavorites != null) {
            menuFavorites.setOnClickListener(v -> {
                if (drawerLayout != null) drawerLayout.closeDrawer(GravityCompat.START);
                Intent intent = new Intent(HomeActivity.this, FavoriteDishesActivity.class);
                startActivity(intent);
            });
        }

        if (menuFollowing != null) {
            menuFollowing.setOnClickListener(v -> {
                if (drawerLayout != null) drawerLayout.closeDrawer(GravityCompat.START);
                Intent intent = new Intent(HomeActivity.this, FavoriteRestaurantsActivity.class);
                startActivity(intent);
            });
        }

        if (menuReviews != null) {
            menuReviews.setOnClickListener(v -> {
                if (drawerLayout != null) drawerLayout.closeDrawer(GravityCompat.START);
                Intent intent = new Intent(HomeActivity.this, MyReviewsActivity.class);
                startActivity(intent);
            });
        }

        LinearLayout menuMyPosts = findViewById(R.id.menuMyPosts);
        if (menuMyPosts != null) {
            menuMyPosts.setOnClickListener(v -> {
                if (drawerLayout != null) drawerLayout.closeDrawer(GravityCompat.START);
                Intent intent = new Intent(HomeActivity.this, MyPostsActivity.class);
                startActivity(intent);
            });
        }

        if (menuLogout != null) {
            menuLogout.setOnClickListener(v -> {
                Intent intent = new Intent(HomeActivity.this, LoginActivity.class);
                startActivity(intent);
                finish();
            });
        }

        // User Avatars in Feed
        View.OnClickListener openUserProfile = v -> {
            Intent intent = new Intent(HomeActivity.this, UserProfileActivity.class);
            startActivity(intent);
        };
        if (containerAvatar1 != null) containerAvatar1.setOnClickListener(openUserProfile);
        if (containerAvatar2 != null) containerAvatar2.setOnClickListener(openUserProfile);
        if (containerAvatar3 != null) containerAvatar3.setOnClickListener(openUserProfile);

        // Fullscreen Facebook-style Photo Viewer on Post Image Click
        if (containerPostImage1 != null) {
            containerPostImage1.setOnClickListener(v -> {
                String author = (tvAuthorName1 != null ? tvAuthorName1.getText() : "Maya Lin") + " • " + (tvUsername1 != null ? tvUsername1.getText() : "@mayafoodie");
                String caption = tvPostText1 != null ? tvPostText1.getText().toString() : "Tìm thấy tô salad ngũ sắc tươi ngon nhất phố! 🥗✨ #MonChay";
                openFullscreenImage(author, caption);
            });
        }
        if (containerPostImage2 != null) {
            containerPostImage2.setOnClickListener(v -> {
                String author = tvAuthorName2 != null ? tvAuthorName2.getText().toString() : "Julian Cole";
                String caption = tvPostText2 != null ? tvPostText2.getText().toString() : "Món pasta thủ công tươi mới vừa ra lò chiều nay! 🍝🇮🇹 #ItalianFood";
                openFullscreenImage(author, caption);
            });
        }
        if (containerPostImage3 != null) {
            containerPostImage3.setOnClickListener(v -> {
                String author = (tvAuthorName3 != null ? tvAuthorName3.getText() : "An Nhiên") + " • " + (tvUsername3 != null ? tvUsername3.getText() : "@annhien_food");
                String caption = tvPostText3 != null ? tvPostText3.getText().toString() : "Bánh mousse dâu tây mọng nước ngọt thanh nhẹ dịu... 🍰🍓 #BanhNgot";
                openFullscreenImage(author, caption);
            });
        }

        // Hashtags Clickable
        View.OnClickListener hashtagListener = v -> {
            Intent intent = new Intent(HomeActivity.this, SearchResultsActivity.class);
            startActivity(intent);
        };
        if (tvHashtags1 != null) tvHashtags1.setOnClickListener(hashtagListener);
        if (tvHashtags2 != null) tvHashtags2.setOnClickListener(hashtagListener);
        if (tvHashtags3 != null) tvHashtags3.setOnClickListener(hashtagListener);

        // Heart / Like Buttons
        if (btnLike1 != null) {
            btnLike1.setOnClickListener(v -> {
                isLiked1 = !isLiked1;
                ImageView img = (ImageView) btnLike1.getChildAt(0);
                TextView txt = (TextView) btnLike1.getChildAt(1);
                if (isLiked1) {
                    img.setImageResource(R.drawable.ic_heart_red);
                    txt.setText("1.2k Thích");
                    txt.setTextColor(Color.parseColor("#E53935"));
                    Toast.makeText(this, "Đã thích bài viết của Maya Lin", Toast.LENGTH_SHORT).show();
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
                    img.setImageResource(R.drawable.ic_heart_red);
                    txt.setText("2.5k Thích");
                    txt.setTextColor(Color.parseColor("#E53935"));
                    Toast.makeText(this, "Đã thích bài viết của Julian Cole", Toast.LENGTH_SHORT).show();
                } else {
                    img.setImageResource(R.drawable.ic_heart);
                    txt.setText("2.5k Thích");
                    txt.setTextColor(Color.parseColor("#1E232C"));
                }
            });
        }

        if (btnLike3 != null) {
            btnLike3.setOnClickListener(v -> {
                isLiked3 = !isLiked3;
                ImageView img = (ImageView) btnLike3.getChildAt(0);
                TextView txt = (TextView) btnLike3.getChildAt(1);
                if (isLiked3) {
                    img.setImageResource(R.drawable.ic_heart_red);
                    txt.setText("981 Thích");
                    txt.setTextColor(Color.parseColor("#E53935"));
                    Toast.makeText(this, "Đã thích bài viết của An Nhiên", Toast.LENGTH_SHORT).show();
                } else {
                    img.setImageResource(R.drawable.ic_heart);
                    txt.setText("980 Thích");
                    txt.setTextColor(Color.parseColor("#1E232C"));
                }
            });
        }

        // Comments
        View.OnClickListener openComments = v -> openCommentsBottomSheet();
        if (btnComment1 != null) btnComment1.setOnClickListener(openComments);
        if (btnComment2 != null) btnComment2.setOnClickListener(openComments);
        if (btnComment3 != null) btnComment3.setOnClickListener(openComments);

        // Share
        View.OnClickListener openShare = v -> openShareBottomSheet();
        if (btnShare1 != null) btnShare1.setOnClickListener(openShare);
        if (btnShare2 != null) btnShare2.setOnClickListener(openShare);
        if (btnShare3 != null) btnShare3.setOnClickListener(openShare);

        // Yellow Bookmarks
        if (btnBookmark1 != null) {
            btnBookmark1.setOnClickListener(v -> {
                isBookmarked1 = !isBookmarked1;
                if (isBookmarked1) {
                    btnBookmark1.setImageResource(R.drawable.ic_bookmark_filled);
                    Toast.makeText(this, "Đã lưu bài viết của Maya Lin vào bộ sưu tập", Toast.LENGTH_SHORT).show();
                } else {
                    btnBookmark1.setImageResource(R.drawable.ic_bookmark);
                }
            });
        }

        if (btnBookmark2 != null) {
            btnBookmark2.setOnClickListener(v -> {
                isBookmarked2 = !isBookmarked2;
                if (isBookmarked2) {
                    btnBookmark2.setImageResource(R.drawable.ic_bookmark_filled);
                    Toast.makeText(this, "Đã lưu bài viết của Julian Cole vào bộ sưu tập", Toast.LENGTH_SHORT).show();
                } else {
                    btnBookmark2.setImageResource(R.drawable.ic_bookmark);
                }
            });
        }

        if (btnBookmark3 != null) {
            btnBookmark3.setOnClickListener(v -> {
                isBookmarked3 = !isBookmarked3;
                if (isBookmarked3) {
                    btnBookmark3.setImageResource(R.drawable.ic_bookmark_filled);
                    Toast.makeText(this, "Đã lưu bài viết của An Nhiên vào bộ sưu tập", Toast.LENGTH_SHORT).show();
                } else {
                    btnBookmark3.setImageResource(R.drawable.ic_bookmark);
                }
            });
        }

        // Options (3 dots)
        ImageView btnOptions1 = findViewById(R.id.btnOptions1);
        if (btnOptions1 != null) {
            btnOptions1.setOnClickListener(v -> openPostOptionsBottomSheet());
        }
    }

    private void filterFeed(String query) {
        View card1 = (View) containerAvatar1.getParent().getParent();
        View card2 = (View) containerAvatar2.getParent().getParent();
        View card3 = (View) containerAvatar3.getParent().getParent();

        if (query.isEmpty()) {
            if (card1 != null) card1.setVisibility(View.VISIBLE);
            if (card2 != null) card2.setVisibility(View.VISIBLE);
            if (card3 != null) card3.setVisibility(View.VISIBLE);
            return;
        }

        String text1 = ((tvAuthorName1 != null ? tvAuthorName1.getText() : "") + " "
                + (tvPostText1 != null ? tvPostText1.getText() : "") + " "
                + (tvHashtags1 != null ? tvHashtags1.getText() : "")).toLowerCase();
        String text2 = ((tvAuthorName2 != null ? tvAuthorName2.getText() : "") + " "
                + (tvPostText2 != null ? tvPostText2.getText() : "") + " "
                + (tvHashtags2 != null ? tvHashtags2.getText() : "")).toLowerCase();
        String text3 = ((tvAuthorName3 != null ? tvAuthorName3.getText() : "") + " "
                + (tvPostText3 != null ? tvPostText3.getText() : "") + " "
                + (tvHashtags3 != null ? tvHashtags3.getText() : "")).toLowerCase();

        if (card1 != null) {
            card1.setVisibility(text1.contains(query) ? View.VISIBLE : View.GONE);
        }
        if (card2 != null) {
            card2.setVisibility(text2.contains(query) ? View.VISIBLE : View.GONE);
        }
        if (card3 != null) {
            card3.setVisibility(text3.contains(query) ? View.VISIBLE : View.GONE);
        }
    }

    private void openFullscreenImage(String author, String caption) {
        Dialog dialog = new Dialog(this, android.R.style.Theme_Black_NoTitleBar_Fullscreen);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.layout_dialog_full_image);
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.parseColor("#EE000000")));
            dialog.getWindow().setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.MATCH_PARENT);
            dialog.getWindow().setFlags(
                    WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
                    WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
            );
        }

        TextView tvFullAuthor = dialog.findViewById(R.id.tvFullAuthor);
        TextView tvFullCaption = dialog.findViewById(R.id.tvFullCaption);
        ImageView imgFullPhoto = dialog.findViewById(R.id.imgFullPhoto);
        View btnClose = dialog.findViewById(R.id.btnCloseFullImage);

        if (imgFullPhoto != null) {
            imgFullPhoto.setScaleType(ImageView.ScaleType.FIT_CENTER);
        }

        if (tvFullAuthor != null) tvFullAuthor.setText(author);
        if (tvFullCaption != null) tvFullCaption.setText(caption);
        if (btnClose != null) btnClose.setOnClickListener(v -> dialog.dismiss());

        dialog.show();
    }

    private void openCommentsBottomSheet() {
        BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(HomeActivity.this);
        View dialogView = getLayoutInflater().inflate(R.layout.layout_dialog_comments, null);
        bottomSheetDialog.setContentView(dialogView);

        View btnCloseComments = dialogView.findViewById(R.id.btnCloseComments);
        TextView btnSortComments = dialogView.findViewById(R.id.btnSortComments);
        View btnReply1 = dialogView.findViewById(R.id.btnReply1);
        View btnReplyReply1 = dialogView.findViewById(R.id.btnReplyReply1);
        View btnReply2 = dialogView.findViewById(R.id.btnReply2);
        EditText edtCommentInput = dialogView.findViewById(R.id.edtCommentInput);
        View btnSendComment = dialogView.findViewById(R.id.btnSendComment);

        View btnEmojiPicker = dialogView.findViewById(R.id.btnEmojiPicker);
        View btnAttachFileComment = dialogView.findViewById(R.id.btnAttachFileComment);
        LinearLayout layoutEmojiBar = dialogView.findViewById(R.id.layoutEmojiBar);

        TextView emojiHeart = dialogView.findViewById(R.id.emojiHeart);
        TextView emojiLaugh = dialogView.findViewById(R.id.emojiLaugh);
        TextView emojiLove = dialogView.findViewById(R.id.emojiLove);
        TextView emojiFire = dialogView.findViewById(R.id.emojiFire);
        TextView emojiClap = dialogView.findViewById(R.id.emojiClap);

        if (btnCloseComments != null) {
            btnCloseComments.setOnClickListener(v -> bottomSheetDialog.dismiss());
        }

        if (btnSortComments != null) {
            btnSortComments.setOnClickListener(v -> {
                PopupMenu popupMenu = new PopupMenu(HomeActivity.this, btnSortComments);
                popupMenu.getMenu().add("Mới nhất");
                popupMenu.getMenu().add("Cũ nhất");
                popupMenu.getMenu().add("Nhiều lượt thích nhất");
                popupMenu.setOnMenuItemClickListener(item -> {
                    btnSortComments.setText(item.getTitle() + " ▼");
                    Toast.makeText(HomeActivity.this, "Sắp xếp: " + item.getTitle(), Toast.LENGTH_SHORT).show();
                    return true;
                });
                popupMenu.show();
            });
        }

        if (btnEmojiPicker != null && layoutEmojiBar != null) {
            btnEmojiPicker.setOnClickListener(v -> {
                int vis = layoutEmojiBar.getVisibility();
                layoutEmojiBar.setVisibility(vis == View.VISIBLE ? View.GONE : View.VISIBLE);
            });
        }

        View.OnClickListener appendEmoji = v -> {
            if (v instanceof TextView && edtCommentInput != null) {
                String emoji = ((TextView) v).getText().toString();
                edtCommentInput.append(emoji);
                edtCommentInput.requestFocus();
            }
        };

        if (emojiHeart != null) emojiHeart.setOnClickListener(appendEmoji);
        if (emojiLaugh != null) emojiLaugh.setOnClickListener(appendEmoji);
        if (emojiLove != null) emojiLove.setOnClickListener(appendEmoji);
        if (emojiFire != null) emojiFire.setOnClickListener(appendEmoji);
        if (emojiClap != null) emojiClap.setOnClickListener(appendEmoji);

        if (btnAttachFileComment != null) {
            btnAttachFileComment.setOnClickListener(v ->
                Toast.makeText(HomeActivity.this, "Chọn ảnh/tệp đính kèm bình luận", Toast.LENGTH_SHORT).show()
            );
        }

        View.OnClickListener replyMinhAnh = v -> {
            if (edtCommentInput != null) {
                edtCommentInput.setText("@Minh Anh ");
                edtCommentInput.setSelection(edtCommentInput.getText().length());
                edtCommentInput.requestFocus();
                Toast.makeText(HomeActivity.this, "Đang trả lời Minh Anh", Toast.LENGTH_SHORT).show();
            }
        };

        View.OnClickListener replyLinhHa = v -> {
            if (edtCommentInput != null) {
                edtCommentInput.setText("@Linh Hà ");
                edtCommentInput.setSelection(edtCommentInput.getText().length());
                edtCommentInput.requestFocus();
                Toast.makeText(HomeActivity.this, "Đang trả lời Linh Hà", Toast.LENGTH_SHORT).show();
            }
        };

        View.OnClickListener replyQuangTran = v -> {
            if (edtCommentInput != null) {
                edtCommentInput.setText("@Quang Trần ");
                edtCommentInput.setSelection(edtCommentInput.getText().length());
                edtCommentInput.requestFocus();
                Toast.makeText(HomeActivity.this, "Đang trả lời Quang Trần", Toast.LENGTH_SHORT).show();
            }
        };

        if (btnReply1 != null) btnReply1.setOnClickListener(replyMinhAnh);
        if (btnReplyReply1 != null) btnReplyReply1.setOnClickListener(replyLinhHa);
        if (btnReply2 != null) btnReply2.setOnClickListener(replyQuangTran);

        if (btnSendComment != null) {
            btnSendComment.setOnClickListener(v -> {
                String text = edtCommentInput != null ? edtCommentInput.getText().toString().trim() : "";
                if (text.isEmpty()) {
                    Toast.makeText(HomeActivity.this, "Vui lòng nhập nội dung bình luận!", Toast.LENGTH_SHORT).show();
                    return;
                }
                Toast.makeText(HomeActivity.this, "Đã đăng bình luận: " + text, Toast.LENGTH_SHORT).show();
                if (edtCommentInput != null) {
                    edtCommentInput.setText("");
                }
                if (layoutEmojiBar != null) {
                    layoutEmojiBar.setVisibility(View.GONE);
                }
            });
        }

        bottomSheetDialog.show();
    }

    private void openShareBottomSheet() {
        BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(HomeActivity.this);
        View dialogView = getLayoutInflater().inflate(R.layout.layout_dialog_share_post, null);
        bottomSheetDialog.setContentView(dialogView);

        View btnCloseShare = dialogView.findViewById(R.id.btnCloseShare);
        View btnSendDishcision = dialogView.findViewById(R.id.btnSendDishcision);
        View btnCopyLink = dialogView.findViewById(R.id.btnCopyLink);

        View contactMaya = dialogView.findViewById(R.id.contactMaya);
        View contactFriends = dialogView.findViewById(R.id.contactFriends);
        View contactGroup = dialogView.findViewById(R.id.contactGroup);

        View tickMaya = dialogView.findViewById(R.id.tickMaya);
        View tickFriends = dialogView.findViewById(R.id.tickFriends);
        View tickGroup = dialogView.findViewById(R.id.tickGroup);

        EditText edtSearchShare = dialogView.findViewById(R.id.edtSearchShare);

        if (btnCloseShare != null) {
            btnCloseShare.setOnClickListener(v -> bottomSheetDialog.dismiss());
        }

        if (contactMaya != null && tickMaya != null) {
            contactMaya.setOnClickListener(v -> {
                int currentVis = tickMaya.getVisibility();
                if (currentVis == View.VISIBLE) {
                    tickMaya.setVisibility(View.GONE);
                } else {
                    tickMaya.setVisibility(View.VISIBLE);
                    Toast.makeText(HomeActivity.this, "Đã chọn: Maya Lin", Toast.LENGTH_SHORT).show();
                }
            });
        }

        if (contactFriends != null && tickFriends != null) {
            contactFriends.setOnClickListener(v -> {
                int currentVis = tickFriends.getVisibility();
                if (currentVis == View.VISIBLE) {
                    tickFriends.setVisibility(View.GONE);
                } else {
                    tickFriends.setVisibility(View.VISIBLE);
                    Toast.makeText(HomeActivity.this, "Đã chọn: Linh Đan", Toast.LENGTH_SHORT).show();
                }
            });
        }

        if (contactGroup != null && tickGroup != null) {
            contactGroup.setOnClickListener(v -> {
                int currentVis = tickGroup.getVisibility();
                if (currentVis == View.VISIBLE) {
                    tickGroup.setVisibility(View.GONE);
                } else {
                    tickGroup.setVisibility(View.VISIBLE);
                    Toast.makeText(HomeActivity.this, "Đã chọn: Quang Huy", Toast.LENGTH_SHORT).show();
                }
            });
        }

        if (edtSearchShare != null) {
            edtSearchShare.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    String query = s.toString().toLowerCase().trim();
                    if (contactMaya != null) {
                        contactMaya.setVisibility("maya lin".contains(query) || query.isEmpty() ? View.VISIBLE : View.GONE);
                    }
                    if (contactFriends != null) {
                        contactFriends.setVisibility("linh đan".contains(query) || query.isEmpty() ? View.VISIBLE : View.GONE);
                    }
                    if (contactGroup != null) {
                        contactGroup.setVisibility("quang huy".contains(query) || query.isEmpty() ? View.VISIBLE : View.GONE);
                    }
                }

                @Override
                public void afterTextChanged(Editable s) {}
            });
        }

        if (btnSendDishcision != null) {
            btnSendDishcision.setOnClickListener(v -> {
                Toast.makeText(HomeActivity.this, "Đã chia sẻ bài viết!", Toast.LENGTH_SHORT).show();
                bottomSheetDialog.dismiss();
            });
        }

        if (btnCopyLink != null) {
            btnCopyLink.setOnClickListener(v -> {
                Toast.makeText(HomeActivity.this, "Đã sao chép liên kết bài viết!", Toast.LENGTH_SHORT).show();
                bottomSheetDialog.dismiss();
            });
        }

        bottomSheetDialog.show();
    }

    private void openPostOptionsBottomSheet() {
        View dialogView = getLayoutInflater().inflate(R.layout.layout_dialog_post_options, null);
        BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(HomeActivity.this);
        bottomSheetDialog.setContentView(dialogView);

        View btnReportPost = dialogView.findViewById(R.id.btnReportPost);
        if (btnReportPost != null) {
            btnReportPost.setOnClickListener(v -> {
                bottomSheetDialog.dismiss();
                Intent intent = new Intent(HomeActivity.this, ReportContentActivity.class);
                startActivity(intent);
            });
        }

        bottomSheetDialog.show();
    }

    private void fetchFeedFromBackend() {
        String authHeader = ApiClient.getAuthHeader(this);
        ApiClient.getApiService().getFeedPosts(authHeader).enqueue(new Callback<FeedPostsResponse>() {
            @Override
            public void onResponse(Call<FeedPostsResponse> call, Response<FeedPostsResponse> response) {
                if (response.isSuccessful() && response.body() != null && response.body().getData() != null) {
                    List<FeedPostsResponse.PostItem> posts = response.body().getData();
                    bindFeedData(posts);
                }
            }

            @Override
            public void onFailure(Call<FeedPostsResponse> call, Throwable t) {
                // Fallback gracefully to default pre-rendered mock content
            }
        });

        // Check if signed in via Google
        android.content.SharedPreferences prefs = getSharedPreferences("DishcisionPrefs", MODE_PRIVATE);
        String googleName = prefs.getString("user_name", null);
        String googleEmail = prefs.getString("user_email", null);
        if (googleName != null && tvSidebarUserName != null) {
            tvSidebarUserName.setText(googleName);
        }
        if (googleEmail != null && tvSidebarUserHandle != null) {
            tvSidebarUserHandle.setText(googleEmail);
        }

        // Fetch User Profile for Sidebar
        ApiClient.getApiService().getMyProfile(authHeader).enqueue(new Callback<UserProfileResponse>() {
            @Override
            public void onResponse(Call<UserProfileResponse> call, Response<UserProfileResponse> response) {
                if (googleName != null) return; // Keep Google user name if logged in with Google
                if (response.isSuccessful() && response.body() != null && response.body().getData() != null) {
                    UserProfileResponse.UserData user = response.body().getData();
                    if (tvSidebarUserName != null && user.fullName != null) {
                        tvSidebarUserName.setText(user.fullName);
                    }
                    if (tvSidebarUserHandle != null && user.username != null) {
                        tvSidebarUserHandle.setText(user.username.startsWith("@") ? user.username : "@" + user.username);
                    }
                }
            }

            @Override
            public void onFailure(Call<UserProfileResponse> call, Throwable t) {}
        });
    }

    private void bindFeedData(List<FeedPostsResponse.PostItem> posts) {
        if (posts == null || posts.isEmpty()) return;

        // Bind Card 1
        if (posts.size() > 0) {
            FeedPostsResponse.PostItem p1 = posts.get(0);
            if (p1.author != null && tvAuthorName1 != null && p1.author.fullName != null) {
                tvAuthorName1.setText(p1.author.fullName);
            }
            if (p1.author != null && tvUsername1 != null && p1.author.username != null) {
                tvUsername1.setText(p1.author.username);
            }
            if (tvPostText1 != null && p1.content != null) {
                tvPostText1.setText(p1.content);
            }
            if (tvHashtags1 != null && p1.hashtags != null && !p1.hashtags.isEmpty()) {
                StringBuilder sb = new StringBuilder();
                for (String tag : p1.hashtags) {
                    sb.append("#").append(tag).append(" ");
                }
                tvHashtags1.setText(sb.toString().trim());
            }
            if (btnLike1 != null && p1.likesCount > 0) {
                TextView txtLike = (TextView) btnLike1.getChildAt(1);
                if (txtLike != null) txtLike.setText(p1.likesCount + " Thích");
            }
            if (btnComment1 != null && p1.commentsCount > 0) {
                TextView txtComment = (TextView) btnComment1.getChildAt(1);
                if (txtComment != null) txtComment.setText(p1.commentsCount + " Bình luận");
            }
        }

        // Bind Card 2
        if (posts.size() > 1) {
            FeedPostsResponse.PostItem p2 = posts.get(1);
            if (p2.author != null && tvAuthorName2 != null && p2.author.fullName != null) {
                tvAuthorName2.setText(p2.author.fullName);
            }
            if (tvPostText2 != null && p2.content != null) {
                tvPostText2.setText(p2.content);
            }
            if (tvHashtags2 != null && p2.hashtags != null && !p2.hashtags.isEmpty()) {
                StringBuilder sb = new StringBuilder();
                for (String tag : p2.hashtags) {
                    sb.append("#").append(tag).append(" ");
                }
                tvHashtags2.setText(sb.toString().trim());
            }
            if (btnLike2 != null && p2.likesCount > 0) {
                TextView txtLike = (TextView) btnLike2.getChildAt(1);
                if (txtLike != null) txtLike.setText(p2.likesCount + " Thích");
            }
            if (btnComment2 != null && p2.commentsCount > 0) {
                TextView txtComment = (TextView) btnComment2.getChildAt(1);
                if (txtComment != null) txtComment.setText(p2.commentsCount + " Bình luận");
            }
        }

        // Bind Card 3
        if (posts.size() > 2) {
            FeedPostsResponse.PostItem p3 = posts.get(2);
            if (p3.author != null && tvAuthorName3 != null && p3.author.fullName != null) {
                tvAuthorName3.setText(p3.author.fullName);
            }
            if (p3.author != null && tvUsername3 != null && p3.author.username != null) {
                tvUsername3.setText(p3.author.username);
            }
            if (tvPostText3 != null && p3.content != null) {
                tvPostText3.setText(p3.content);
            }
            if (tvHashtags3 != null && p3.hashtags != null && !p3.hashtags.isEmpty()) {
                StringBuilder sb = new StringBuilder();
                for (String tag : p3.hashtags) {
                    sb.append("#").append(tag).append(" ");
                }
                tvHashtags3.setText(sb.toString().trim());
            }
            if (btnLike3 != null && p3.likesCount > 0) {
                TextView txtLike = (TextView) btnLike3.getChildAt(1);
                if (txtLike != null) txtLike.setText(p3.likesCount + " Thích");
            }
            if (btnComment3 != null && p3.commentsCount > 0) {
                TextView txtComment = (TextView) btnComment3.getChildAt(1);
                if (txtComment != null) txtComment.setText(p3.commentsCount + " Bình luận");
            }
        }
    }
}
