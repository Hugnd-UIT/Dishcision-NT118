package com.example.dishcision;

import android.app.Activity;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Typeface;
import android.net.Uri;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.PopupMenu;

import com.google.android.material.card.MaterialCardView;

import com.example.dishcision.network.ApiClient;
import com.example.dishcision.network.AuthHelper;
import com.example.dishcision.network.CommentsListResponse;
import com.example.dishcision.network.CreateCommentRequest;
import com.example.dishcision.network.CreateCommentResponse;
import com.example.dishcision.network.CreatePostResponse;
import com.example.dishcision.network.FeedPostsResponse;
import com.example.dishcision.network.FollowersListResponse;
import com.example.dishcision.network.ImageLoader;
import com.example.dishcision.network.SavePostButton;
import com.example.dishcision.network.SearchAccountsResponse;
import com.example.dishcision.network.SendChatMessageRequest;
import com.example.dishcision.network.SendChatMessageResponse;
import com.example.dishcision.network.TokenManager;
import com.example.dishcision.network.UpdatePostRequest;
import com.example.dishcision.network.UserProfileResponse;
import com.google.android.material.bottomsheet.BottomSheetDialog;

import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public final class PostFeedHelper {

    public interface PostCardListener {
        void onPostDeleted(int postId);
        void onPostArchived(int postId);
    }

    public interface PostGridClickListener {
        void onPostClick(FeedPostsResponse.PostItem post, int position);
    }

    private PostFeedHelper() {}

    public static void renderPostGrid(
            Activity activity,
            LinearLayout gridContainer,
            TextView tvEmpty,
            List<FeedPostsResponse.PostItem> posts,
            String emptyText,
            PostGridClickListener clickListener
    ) {
        if (activity == null || gridContainer == null) return;
        gridContainer.removeAllViews();

        if (posts == null || posts.isEmpty()) {
            if (tvEmpty != null) {
                if (!isEmpty(emptyText)) tvEmpty.setText(emptyText);
                tvEmpty.setVisibility(View.VISIBLE);
            }
            return;
        }

        if (tvEmpty != null) tvEmpty.setVisibility(View.GONE);

        int tileHeight = dpToPx(activity, 120);
        int gap = dpToPx(activity, 4);
        int rowGap = dpToPx(activity, 8);
        int corner = dpToPx(activity, 8);

        for (int i = 0; i < posts.size(); i += 3) {
            LinearLayout row = new LinearLayout(activity);
            row.setOrientation(LinearLayout.HORIZONTAL);
            LinearLayout.LayoutParams rowLp = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT, tileHeight);
            rowLp.bottomMargin = rowGap;
            row.setLayoutParams(rowLp);

            for (int col = 0; col < 3; col++) {
                int idx = i + col;
                LinearLayout.LayoutParams colLp = new LinearLayout.LayoutParams(
                        0, LinearLayout.LayoutParams.MATCH_PARENT, 1f);
                if (col == 0) colLp.rightMargin = gap;
                else if (col == 1) {
                    colLp.leftMargin = gap / 2;
                    colLp.rightMargin = gap / 2;
                } else {
                    colLp.leftMargin = gap;
                }

                if (idx >= posts.size()) {
                    View spacer = new View(activity);
                    spacer.setVisibility(View.INVISIBLE);
                    spacer.setLayoutParams(colLp);
                    row.addView(spacer);
                    continue;
                }

                FeedPostsResponse.PostItem post = posts.get(idx);
                MaterialCardView card = new MaterialCardView(activity);
                card.setLayoutParams(colLp);
                card.setRadius(corner);
                card.setCardElevation(0f);
                card.setStrokeWidth(0);
                card.setClickable(true);
                card.setFocusable(true);
                card.setCardBackgroundColor(Color.parseColor("#E3E8FA"));

                FrameLayout frame = new FrameLayout(activity);
                frame.setLayoutParams(new FrameLayout.LayoutParams(
                        FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT));

                if (post.images != null && !post.images.isEmpty() && !isEmpty(post.images.get(0).imageUrl)) {
                    ImageView img = new ImageView(activity);
                    img.setLayoutParams(new FrameLayout.LayoutParams(
                            FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT));
                    img.setScaleType(ImageView.ScaleType.CENTER_CROP);
                    frame.addView(img);
                    ImageLoader.loadImage(img, post.images.get(0).imageUrl);
                } else {
                    card.setCardBackgroundColor(Color.parseColor("#FFF4EF"));
                    TextView tvSnippet = new TextView(activity);
                    tvSnippet.setLayoutParams(new FrameLayout.LayoutParams(
                            FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT));
                    tvSnippet.setPadding(dpToPx(activity, 8), dpToPx(activity, 8), dpToPx(activity, 8), dpToPx(activity, 24));
                    tvSnippet.setText(post.content != null ? post.content.trim() : "");
                    tvSnippet.setTextColor(Color.parseColor("#3E2723"));
                    tvSnippet.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12);
                    tvSnippet.setMaxLines(4);
                    tvSnippet.setEllipsize(android.text.TextUtils.TruncateAt.END);
                    frame.addView(tvSnippet);
                }

                if (post.likesCount > 0) {
                    TextView tvLikes = new TextView(activity);
                    FrameLayout.LayoutParams badgeLp = new FrameLayout.LayoutParams(
                            FrameLayout.LayoutParams.WRAP_CONTENT, FrameLayout.LayoutParams.WRAP_CONTENT);
                    badgeLp.gravity = android.view.Gravity.BOTTOM | android.view.Gravity.START;
                    int margin = dpToPx(activity, 6);
                    badgeLp.setMargins(margin, margin, margin, margin);
                    tvLikes.setLayoutParams(badgeLp);
                    tvLikes.setBackgroundResource(R.drawable.bg_badge_green_rating);
                    tvLikes.setPadding(dpToPx(activity, 6), dpToPx(activity, 2), dpToPx(activity, 6), dpToPx(activity, 2));
                    tvLikes.setText("👍 " + formatCount(post.likesCount));
                    tvLikes.setTextColor(Color.WHITE);
                    tvLikes.setTextSize(TypedValue.COMPLEX_UNIT_SP, 11);
                    tvLikes.setTypeface(null, Typeface.BOLD);
                    frame.addView(tvLikes);
                }

                card.addView(frame);
                final int clickedPos = idx;
                card.setOnClickListener(v -> {
                    if (clickListener != null) clickListener.onPostClick(post, clickedPos);
                });
                row.addView(card);
            }

            gridContainer.addView(row);
        }
    }

    public static String decodeLocationDisplay(String locationUrl) {
        if (locationUrl == null || locationUrl.trim().isEmpty()) {
            return "";
        }
        String trimmed = locationUrl.trim();
        if (trimmed.startsWith("http://") || trimmed.startsWith("https://")) {
            try {
                Uri uri = Uri.parse(trimmed);
                String q = uri.getQueryParameter("q");
                if (q != null && !q.trim().isEmpty()) {
                    return q.trim();
                }
            } catch (Exception ignored) {}
            try {
                int qIndex = trimmed.indexOf("?q=");
                if (qIndex == -1) qIndex = trimmed.indexOf("&q=");
                if (qIndex != -1) {
                    String rest = trimmed.substring(qIndex + 3);
                    int amp = rest.indexOf('&');
                    if (amp != -1) rest = rest.substring(0, amp);
                    return URLDecoder.decode(rest, StandardCharsets.UTF_8.name()).trim();
                }
            } catch (Exception ignored) {}
        }
        return trimmed;
    }

    public static String normalizeLocationUrl(String rawInput) {
        if (rawInput == null || rawInput.trim().isEmpty()) {
            return "";
        }
        String trimmed = rawInput.trim();
        if (trimmed.startsWith("http://") || trimmed.startsWith("https://")) {
            return trimmed;
        }
        try {
            return "https://maps.google.com/?q=" + URLEncoder.encode(trimmed, StandardCharsets.UTF_8.name());
        } catch (Exception e) {
            return "https://maps.google.com/?q=" + trimmed.replace(" ", "+");
        }
    }

    public static String formatHashtags(List<String> hashtags) {
        if (hashtags == null || hashtags.isEmpty()) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        for (String tag : hashtags) {
            if (tag == null) continue;
            String clean = tag.replace("#", "").trim();
            if (!clean.isEmpty()) {
                if (sb.length() > 0) sb.append(" ");
                sb.append("#").append(clean);
            }
        }
        return sb.toString();
    }

    public static String formatCount(int count) {
        int safe = Math.max(0, count);
        if (safe >= 1000) {
            String formatted = String.format(Locale.US, "%.1fk", safe / 1000.0);
            return formatted.endsWith(".0k") ? (safe / 1000) + "k" : formatted;
        }
        return String.valueOf(safe);
    }

    public static void bindPostCard(
            Activity activity,
            View cardView,
            FeedPostsResponse.PostItem post,
            ActivityResultLauncher<Intent> editPostLauncher,
            PostCardListener listener
    ) {
        if (activity == null || cardView == null || post == null) return;

        View containerAvatar = cardView.findViewById(R.id.containerAvatar);
        ImageView imgAvatar = cardView.findViewById(R.id.imgAvatar);
        View layoutAuthorMeta = cardView.findViewById(R.id.layoutAuthorMeta);
        TextView tvAuthorName = cardView.findViewById(R.id.tvAuthorName);
        TextView tvUsername = cardView.findViewById(R.id.tvUsername);
        ImageView imgVerified = cardView.findViewById(R.id.imgVerified);
        TextView tvLocation = cardView.findViewById(R.id.tvLocation);
        ImageView btnOptions = cardView.findViewById(R.id.btnOptions);

        View containerPostImage = cardView.findViewById(R.id.containerPostImage);
        androidx.viewpager.widget.ViewPager vpPostImages = cardView.findViewById(R.id.vpPostImages);
        TextView tvImageCountBadge = cardView.findViewById(R.id.tvImageCountBadge);
        TextView tvPostText = cardView.findViewById(R.id.tvPostText);
        TextView tvVoucherBadge = cardView.findViewById(R.id.tvVoucherBadge);
        TextView tvHashtags = cardView.findViewById(R.id.tvHashtags);

        LinearLayout btnLike = cardView.findViewById(R.id.btnLike);
        ImageView imgLike = cardView.findViewById(R.id.imgLike);
        TextView tvLikeCount = cardView.findViewById(R.id.tvLikeCount);
        LinearLayout btnComment = cardView.findViewById(R.id.btnComment);
        TextView tvCommentCount = cardView.findViewById(R.id.tvCommentCount);
        ImageView btnBookmark = cardView.findViewById(R.id.btnBookmark);
        ImageView btnShare = cardView.findViewById(R.id.btnShare);

        String authorName = "Người dùng";
        String usernameRaw = "";
        int authorId = 0;
        if (post.author != null) {
            authorId = post.author.userId;
            if (!isEmpty(post.author.fullName)) {
                authorName = post.author.fullName.trim();
            } else if (!isEmpty(post.author.username)) {
                authorName = post.author.username.replace("@", "").trim();
            }
            if (!isEmpty(post.author.username)) {
                usernameRaw = post.author.username.replace("@", "").trim();
            }
        }

        if (tvAuthorName != null) tvAuthorName.setText(authorName);
        if (tvUsername != null) {
            if (!usernameRaw.isEmpty()) {
                tvUsername.setText("@" + usernameRaw);
                tvUsername.setVisibility(View.VISIBLE);
            } else {
                tvUsername.setVisibility(View.GONE);
            }
        }
        if (imgVerified != null) {
            boolean verified = post.author != null && ("RestaurantAccount".equals(post.author.role) || "Admin".equals(post.author.role));
            imgVerified.setVisibility(verified ? View.VISIBLE : View.GONE);
        }
        if (imgAvatar != null) {
            imgAvatar.setImageResource(R.drawable.logo);
            imgAvatar.setTag(null);
            if (post.author != null && !isEmpty(post.author.avatarUrl)) {
                ImageLoader.loadAvatar(imgAvatar, post.author.avatarUrl);
            }
        }

        final int targetAuthorId = authorId;
        View.OnClickListener authorClickListener = v -> {
            if (targetAuthorId <= 0) return;
            int currentUserId = AuthHelper.getCurrentUserId(activity);
            if (currentUserId > 0 && targetAuthorId == currentUserId) {
                if (!(activity instanceof ProfileActivity)) {
                    activity.startActivity(new Intent(activity, ProfileActivity.class));
                }
            } else {
                Intent intent = new Intent(activity, UserProfileActivity.class);
                intent.putExtra("USER_ID", targetAuthorId);
                activity.startActivity(intent);
            }
        };
        if (containerAvatar != null) containerAvatar.setOnClickListener(authorClickListener);
        if (layoutAuthorMeta != null) layoutAuthorMeta.setOnClickListener(authorClickListener);

        if (tvLocation != null) {
            String locDisplay = decodeLocationDisplay(post.locationUrl);
            String timeDisplay = MyPostsActivity.formatTime(post.createdAt);
            tvLocation.setText((!locDisplay.isEmpty() ? locDisplay : "Dishcision") + " • " + timeDisplay);
        }

        if (containerPostImage != null) {
            ArrayList<String> imageUrls = new ArrayList<>();
            if (post.images != null) {
                for (FeedPostsResponse.PostImage img : post.images) {
                    if (img != null && !isEmpty(img.imageUrl)) {
                        imageUrls.add(img.imageUrl.trim());
                    }
                }
            }
            if (!imageUrls.isEmpty()) {
                containerPostImage.setVisibility(View.VISIBLE);
                final String fullAuthorHeader = usernameRaw.isEmpty() ? authorName : authorName + " • @" + usernameRaw;

                if (vpPostImages != null) {
                    vpPostImages.clearOnPageChangeListeners();
                    vpPostImages.setOffscreenPageLimit(Math.max(1, imageUrls.size() - 1));
                    vpPostImages.setAdapter(new androidx.viewpager.widget.PagerAdapter() {
                        @Override
                        public int getCount() {
                            return imageUrls.size();
                        }

                        @Override
                        public boolean isViewFromObject(@NonNull View view, @NonNull Object object) {
                            return view == object;
                        }

                        @NonNull
                        @Override
                        public Object instantiateItem(@NonNull ViewGroup container, int position) {
                            ImageView iv = new ImageView(container.getContext());
                            iv.setLayoutParams(new ViewGroup.LayoutParams(
                                    ViewGroup.LayoutParams.MATCH_PARENT,
                                    ViewGroup.LayoutParams.MATCH_PARENT
                            ));
                            iv.setScaleType(ImageView.ScaleType.CENTER_CROP);
                            iv.setOnClickListener(v ->
                                    openFullscreenImages(activity, fullAuthorHeader, post.content, imageUrls, position));
                            ImageLoader.loadImage(iv, imageUrls.get(position));
                            container.addView(iv);
                            return iv;
                        }

                        @Override
                        public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
                            container.removeView((View) object);
                        }
                    });
                    vpPostImages.setCurrentItem(0, false);

                    if (tvImageCountBadge != null) {
                        if (imageUrls.size() > 1) {
                            tvImageCountBadge.setText("1 / " + imageUrls.size());
                            tvImageCountBadge.setVisibility(View.VISIBLE);
                            vpPostImages.addOnPageChangeListener(new androidx.viewpager.widget.ViewPager.SimpleOnPageChangeListener() {
                                @Override
                                public void onPageSelected(int position) {
                                    tvImageCountBadge.setText((position + 1) + " / " + imageUrls.size());
                                }
                            });
                        } else {
                            tvImageCountBadge.setVisibility(View.GONE);
                        }
                    }
                }
            } else {
                containerPostImage.setVisibility(View.GONE);
            }
        }

        if (tvPostText != null) {
            tvPostText.setText(post.content != null ? post.content : "");
        }

        if (tvVoucherBadge != null) {
            if (!isEmpty(post.voucherCode)) {
                String code = post.voucherCode.trim();
                tvVoucherBadge.setText("🎟 Mã ưu đãi: " + code);
                tvVoucherBadge.setVisibility(View.VISIBLE);
                tvVoucherBadge.setOnClickListener(v -> {
                    copyToClipboard(activity, "Voucher Code", code);
                    Toast.makeText(activity, "Đã sao chép mã ưu đãi: " + code, Toast.LENGTH_SHORT).show();
                });
            } else {
                tvVoucherBadge.setVisibility(View.GONE);
            }
        }

        if (tvHashtags != null) {
            String hashtagsText = formatHashtags(post.hashtags);
            if (!hashtagsText.isEmpty()) {
                tvHashtags.setText(hashtagsText);
                tvHashtags.setVisibility(View.VISIBLE);
                tvHashtags.setOnClickListener(v -> {
                    Intent intent = new Intent(activity, SearchResultsActivity.class);
                    intent.putExtra("SEARCH_QUERY", post.hashtags.get(0).replace("#", "").trim());
                    activity.startActivity(intent);
                });
            } else {
                tvHashtags.setVisibility(View.GONE);
            }
        }

        SavePostButton saveButton = new SavePostButton(activity, btnBookmark);
        saveButton.bind(post.postId, post.isSaved, authorName);

        renderLikeState(imgLike, tvLikeCount, post);
        final boolean[] likeInFlight = {false};
        if (btnLike != null) {
            btnLike.setOnClickListener(v -> {
                if (likeInFlight[0] || post.postId <= 0) return;
                String auth = ApiClient.getAuthHeader(activity);
                if (auth.isEmpty()) {
                    Toast.makeText(activity, "Vui lòng đăng nhập lại để thích bài viết", Toast.LENGTH_SHORT).show();
                    return;
                }

                boolean targetLiked = !post.isLiked;
                post.isLiked = targetLiked;
                post.likesCount = Math.max(0, post.likesCount + (targetLiked ? 1 : -1));
                renderLikeState(imgLike, tvLikeCount, post);
                likeInFlight[0] = true;

                Call<Object> call = targetLiked
                        ? ApiClient.getApiService().likePost(auth, post.postId)
                        : ApiClient.getApiService().unlikePost(auth, post.postId);

                call.enqueue(new Callback<Object>() {
                    @Override
                    public void onResponse(@NonNull Call<Object> c, @NonNull Response<Object> r) {
                        likeInFlight[0] = false;
                        if (activity.isFinishing()) return;
                        if (r.isSuccessful()) return;
                        if (targetLiked && r.code() == 409) {
                            post.isLiked = true;
                            renderLikeState(imgLike, tvLikeCount, post);
                            return;
                        }
                        post.isLiked = !targetLiked;
                        post.likesCount = Math.max(0, post.likesCount + (targetLiked ? -1 : 1));
                        renderLikeState(imgLike, tvLikeCount, post);
                        Toast.makeText(activity, "Không thể cập nhật lượt thích", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onFailure(@NonNull Call<Object> c, @NonNull Throwable t) {
                        likeInFlight[0] = false;
                        if (activity.isFinishing()) return;
                        post.isLiked = !targetLiked;
                        post.likesCount = Math.max(0, post.likesCount + (targetLiked ? -1 : 1));
                        renderLikeState(imgLike, tvLikeCount, post);
                        Toast.makeText(activity, "Lỗi kết nối máy chủ", Toast.LENGTH_SHORT).show();
                    }
                });
            });
        }

        if (tvCommentCount != null) {
            tvCommentCount.setText(formatCount(post.commentsCount) + " Bình luận");
        }
        if (btnComment != null) {
            btnComment.setOnClickListener(v -> openCommentsBottomSheet(activity, post, tvCommentCount));
        }

        if (btnShare != null) {
            btnShare.setOnClickListener(v -> openShareBottomSheet(activity, post));
        }

        if (btnOptions != null) {
            btnOptions.setOnClickListener(v -> openPostOptionsBottomSheet(activity, post, editPostLauncher, listener));
        }
    }

    private static void renderLikeState(ImageView imgLike, TextView tvLikeCount, FeedPostsResponse.PostItem post) {
        if (imgLike != null) {
            imgLike.setImageResource(post.isLiked ? R.drawable.ic_heart_red : R.drawable.ic_heart);
        }
        if (tvLikeCount != null) {
            tvLikeCount.setText(formatCount(post.likesCount) + " Thích");
            tvLikeCount.setTextColor(Color.parseColor(post.isLiked ? "#E53935" : "#1E232C"));
        }
    }

    public static void openFullscreenImage(Activity activity, String author, String caption, String imageUrl) {
        if (isEmpty(imageUrl)) return;
        ArrayList<String> urls = new ArrayList<>();
        urls.add(imageUrl.trim());
        openFullscreenImages(activity, author, caption, urls, 0);
    }

    public static void openFullscreenImages(Activity activity, String author, String caption, List<String> imageUrls, int initialIndex) {
        if (activity == null || activity.isFinishing() || imageUrls == null || imageUrls.isEmpty()) return;
        Intent intent = new Intent(activity, FullscreenImageActivity.class);
        intent.putExtra(FullscreenImageActivity.EXTRA_AUTHOR, author);
        intent.putExtra(FullscreenImageActivity.EXTRA_CAPTION, caption);
        intent.putStringArrayListExtra(FullscreenImageActivity.EXTRA_IMAGE_URLS, new ArrayList<>(imageUrls));
        intent.putExtra(FullscreenImageActivity.EXTRA_INITIAL_INDEX, Math.max(0, initialIndex));
        activity.startActivity(intent);
        activity.overridePendingTransition(0, 0);
    }

    public static void openCommentsBottomSheet(Activity activity, FeedPostsResponse.PostItem post, TextView tvCommentCount) {
        if (activity == null || post == null || activity.isFinishing()) return;

        BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(activity);
        View dialogView = activity.getLayoutInflater().inflate(R.layout.layout_dialog_comments, null);
        bottomSheetDialog.setContentView(dialogView);

        TextView tvCommentsTitle = dialogView.findViewById(R.id.tvCommentsTitle);
        TextView tvCommentInputHeader = dialogView.findViewById(R.id.tvCommentInputHeader);
        View btnCloseComments = dialogView.findViewById(R.id.btnCloseComments);
        TextView btnSortComments = dialogView.findViewById(R.id.btnSortComments);
        LinearLayout containerCommentsList = dialogView.findViewById(R.id.containerCommentsList);
        EditText edtCommentInput = dialogView.findViewById(R.id.edtCommentInput);
        View btnSendComment = dialogView.findViewById(R.id.btnSendComment);
        View btnEmojiPicker = dialogView.findViewById(R.id.btnEmojiPicker);
        View btnAttachFileComment = dialogView.findViewById(R.id.btnAttachFileComment);
        LinearLayout layoutEmojiBar = dialogView.findViewById(R.id.layoutEmojiBar);

        if (btnAttachFileComment != null) {
            btnAttachFileComment.setVisibility(View.GONE);
        }

        if (tvCommentsTitle != null) {
            tvCommentsTitle.setText("Bình luận (" + post.commentsCount + ")");
        }

        final String[] currentUserLabel = {"Người dùng"};
        SharedPreferences prefs = activity.getSharedPreferences("DishcisionPrefs", Context.MODE_PRIVATE);
        TokenManager tm = new TokenManager(activity);
        String cachedName = prefs.getString("user_name", tm.getUserName());
        if (!isEmpty(cachedName)) {
            currentUserLabel[0] = cachedName.trim();
        }

        final Integer[] replyParentId = {null};
        final Runnable updateInputHeader = () -> {
            if (tvCommentInputHeader == null) return;
            if (replyParentId[0] != null) {
                Object tagName = tvCommentInputHeader.getTag();
                String target = tagName != null ? tagName.toString() : "bình luận";
                tvCommentInputHeader.setText("Đang trả lời " + target + " • Nhấn để hủy");
                tvCommentInputHeader.setTextColor(Color.parseColor("#FF5A36"));
            } else {
                String init = HomeActivity.getInitials(currentUserLabel[0]);
                tvCommentInputHeader.setText(init + "  Bình luận với tên " + currentUserLabel[0]);
                tvCommentInputHeader.setTextColor(Color.parseColor("#6B7280"));
            }
        };
        updateInputHeader.run();

        if (tvCommentInputHeader != null) {
            tvCommentInputHeader.setOnClickListener(v -> {
                if (replyParentId[0] != null) {
                    replyParentId[0] = null;
                    tvCommentInputHeader.setTag(null);
                    updateInputHeader.run();
                }
            });
        }

        String authHeader = ApiClient.getAuthHeader(activity);
        if (!authHeader.isEmpty()) {
            ApiClient.getApiService().getMyProfile(authHeader).enqueue(new Callback<UserProfileResponse>() {
                @Override
                public void onResponse(@NonNull Call<UserProfileResponse> call, @NonNull Response<UserProfileResponse> response) {
                    if (activity.isFinishing() || !response.isSuccessful() || response.body() == null || response.body().getData() == null) {
                        return;
                    }
                    UserProfileResponse.UserData user = response.body().getData();
                    if (!isEmpty(user.fullName)) {
                        currentUserLabel[0] = user.fullName.trim();
                        updateInputHeader.run();
                    }
                }

                @Override
                public void onFailure(@NonNull Call<UserProfileResponse> call, @NonNull Throwable t) {}
            });
        }

        final List<CommentsListResponse.CommentItem> loadedComments = new ArrayList<>();
        final boolean[] newestFirst = {true};

        final Runnable renderList = () -> renderCommentsList(
                activity,
                post,
                loadedComments,
                newestFirst[0],
                containerCommentsList,
                tvCommentsTitle,
                tvCommentCount,
                (parentCommentId, authorDisplay) -> {
                    replyParentId[0] = parentCommentId;
                    if (tvCommentInputHeader != null) {
                        tvCommentInputHeader.setTag(authorDisplay);
                    }
                    updateInputHeader.run();
                    if (edtCommentInput != null) edtCommentInput.requestFocus();
                },
                () -> loadPostComments(activity, post, loadedComments, () -> {
                    if (containerCommentsList != null && tvCommentsTitle != null) {
                        containerCommentsList.post(() -> {});
                    }
                }, containerCommentsList, tvCommentsTitle, tvCommentCount, newestFirst, replyParentId, tvCommentInputHeader, updateInputHeader, edtCommentInput)
        );

        loadPostComments(activity, post, loadedComments, renderList, containerCommentsList, tvCommentsTitle, tvCommentCount, newestFirst, replyParentId, tvCommentInputHeader, updateInputHeader, edtCommentInput);

        if (btnCloseComments != null) {
            btnCloseComments.setOnClickListener(v -> bottomSheetDialog.dismiss());
        }

        if (btnSortComments != null) {
            btnSortComments.setOnClickListener(v -> {
                PopupMenu popupMenu = new PopupMenu(activity, btnSortComments);
                popupMenu.getMenu().add("Mới nhất");
                popupMenu.getMenu().add("Cũ nhất");
                popupMenu.setOnMenuItemClickListener(item -> {
                    newestFirst[0] = "Mới nhất".equals(item.getTitle().toString());
                    btnSortComments.setText(item.getTitle() + " ▼");
                    renderList.run();
                    return true;
                });
                popupMenu.show();
            });
        }

        if (btnEmojiPicker != null && layoutEmojiBar != null) {
            btnEmojiPicker.setOnClickListener(v ->
                    layoutEmojiBar.setVisibility(layoutEmojiBar.getVisibility() == View.VISIBLE ? View.GONE : View.VISIBLE));
        }

        View.OnClickListener appendEmoji = v -> {
            if (v instanceof TextView && edtCommentInput != null) {
                edtCommentInput.append(((TextView) v).getText().toString());
                edtCommentInput.requestFocus();
            }
        };
        int[] emojiIds = {R.id.emojiHeart, R.id.emojiLaugh, R.id.emojiLove, R.id.emojiFire, R.id.emojiClap};
        for (int id : emojiIds) {
            View emojiView = dialogView.findViewById(id);
            if (emojiView != null) emojiView.setOnClickListener(appendEmoji);
        }

        final boolean[] sending = {false};
        if (btnSendComment != null) {
            btnSendComment.setOnClickListener(v -> {
                if (sending[0]) return;
                String text = edtCommentInput != null ? edtCommentInput.getText().toString().trim() : "";
                if (text.isEmpty()) {
                    Toast.makeText(activity, "Vui lòng nhập nội dung bình luận!", Toast.LENGTH_SHORT).show();
                    return;
                }
                String auth = ApiClient.getAuthHeader(activity);
                if (auth.isEmpty()) {
                    Toast.makeText(activity, "Vui lòng đăng nhập lại để bình luận", Toast.LENGTH_SHORT).show();
                    return;
                }

                sending[0] = true;
                btnSendComment.setEnabled(false);
                ApiClient.getApiService().createPostComment(auth, post.postId, new CreateCommentRequest(text, replyParentId[0]))
                        .enqueue(new Callback<CreateCommentResponse>() {
                            @Override
                            public void onResponse(@NonNull Call<CreateCommentResponse> call, @NonNull Response<CreateCommentResponse> response) {
                                sending[0] = false;
                                btnSendComment.setEnabled(true);
                                if (activity.isFinishing()) return;
                                if (response.isSuccessful() && response.body() != null && response.body().success) {
                                    if (edtCommentInput != null) edtCommentInput.setText("");
                                    if (layoutEmojiBar != null) layoutEmojiBar.setVisibility(View.GONE);
                                    replyParentId[0] = null;
                                    if (tvCommentInputHeader != null) tvCommentInputHeader.setTag(null);
                                    updateInputHeader.run();
                                    loadPostComments(activity, post, loadedComments, renderList, containerCommentsList, tvCommentsTitle, tvCommentCount, newestFirst, replyParentId, tvCommentInputHeader, updateInputHeader, edtCommentInput);
                                } else {
                                    Toast.makeText(activity, "Gửi bình luận thất bại (mã " + response.code() + ")", Toast.LENGTH_SHORT).show();
                                }
                            }

                            @Override
                            public void onFailure(@NonNull Call<CreateCommentResponse> call, @NonNull Throwable t) {
                                sending[0] = false;
                                btnSendComment.setEnabled(true);
                                if (!activity.isFinishing()) {
                                    Toast.makeText(activity, "Lỗi kết nối khi gửi bình luận", Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
            });
        }

        bottomSheetDialog.show();
    }

    private interface ReplyCallback {
        void onReply(int parentCommentId, String authorDisplay);
    }

    private static void loadPostComments(
            Activity activity,
            FeedPostsResponse.PostItem post,
            List<CommentsListResponse.CommentItem> loadedComments,
            Runnable renderCallback,
            LinearLayout container,
            TextView tvTitle,
            TextView tvCommentCount,
            boolean[] newestFirst,
            Integer[] replyParentId,
            TextView tvInputHeader,
            Runnable updateInputHeader,
            EditText edtInput
    ) {
        if (container == null || post == null) return;
        container.removeAllViews();

        TextView tvLoading = new TextView(activity);
        tvLoading.setText("Đang tải bình luận...");
        tvLoading.setTextColor(Color.parseColor("#9E8A83"));
        tvLoading.setPadding(0, dpToPx(activity, 8), 0, dpToPx(activity, 8));
        container.addView(tvLoading);

        ApiClient.getApiService().getPostComments(post.postId).enqueue(new Callback<CommentsListResponse>() {
            @Override
            public void onResponse(@NonNull Call<CommentsListResponse> call, @NonNull Response<CommentsListResponse> response) {
                if (activity.isFinishing()) return;
                loadedComments.clear();
                if (response.isSuccessful() && response.body() != null && response.body().data != null) {
                    loadedComments.addAll(response.body().data);
                    int totalComments = 0;
                    for (CommentsListResponse.CommentItem c : loadedComments) {
                        totalComments += 1 + (c.replies != null ? c.replies.size() : 0);
                    }
                    post.commentsCount = totalComments;
                    if (tvTitle != null) tvTitle.setText("Bình luận (" + totalComments + ")");
                    if (tvCommentCount != null) tvCommentCount.setText(formatCount(totalComments) + " Bình luận");

                    renderCommentsList(
                            activity,
                            post,
                            loadedComments,
                            newestFirst[0],
                            container,
                            tvTitle,
                            tvCommentCount,
                            (parentId, authorDisplay) -> {
                                replyParentId[0] = parentId;
                                if (tvInputHeader != null) tvInputHeader.setTag(authorDisplay);
                                updateInputHeader.run();
                                if (edtInput != null) edtInput.requestFocus();
                            },
                            () -> loadPostComments(activity, post, loadedComments, renderCallback, container, tvTitle, tvCommentCount, newestFirst, replyParentId, tvInputHeader, updateInputHeader, edtInput)
                    );
                } else {
                    container.removeAllViews();
                    TextView tvErr = new TextView(activity);
                    tvErr.setText("Không thể tải bình luận.");
                    tvErr.setTextColor(Color.parseColor("#E53935"));
                    container.addView(tvErr);
                }
            }

            @Override
            public void onFailure(@NonNull Call<CommentsListResponse> call, @NonNull Throwable t) {
                if (activity.isFinishing()) return;
                container.removeAllViews();
                TextView tvErr = new TextView(activity);
                tvErr.setText("Lỗi kết nối khi tải bình luận.");
                tvErr.setTextColor(Color.parseColor("#E53935"));
                container.addView(tvErr);
            }
        });
    }

    private static void renderCommentsList(
            Activity activity,
            FeedPostsResponse.PostItem post,
            List<CommentsListResponse.CommentItem> sourceComments,
            boolean newestFirst,
            LinearLayout container,
            TextView tvTitle,
            TextView tvCommentCount,
            ReplyCallback replyCallback,
            Runnable reloadCallback
    ) {
        if (container == null) return;
        container.removeAllViews();

        if (sourceComments == null || sourceComments.isEmpty()) {
            TextView tvEmpty = new TextView(activity);
            tvEmpty.setText("Chưa có bình luận nào. Hãy là người đầu tiên bình luận!");
            tvEmpty.setTextColor(Color.parseColor("#9E8A83"));
            tvEmpty.setPadding(0, dpToPx(activity, 14), 0, dpToPx(activity, 14));
            container.addView(tvEmpty);
            return;
        }

        int currentUserId = AuthHelper.getCurrentUserId(activity);
        List<CommentsListResponse.CommentItem> ordered = new ArrayList<>(sourceComments);
        if (newestFirst) {
            java.util.Collections.reverse(ordered);
        }

        for (CommentsListResponse.CommentItem comment : ordered) {
            View row = buildCommentRow(activity, comment, comment.commentId, false, currentUserId, replyCallback, reloadCallback);
            container.addView(row);

            if (comment.replies != null && !comment.replies.isEmpty()) {
                for (CommentsListResponse.CommentItem reply : comment.replies) {
                    View replyRow = buildCommentRow(activity, reply, comment.commentId, true, currentUserId, replyCallback, reloadCallback);
                    container.addView(replyRow);
                }
            }
        }
    }

    private static View buildCommentRow(
            Activity activity,
            CommentsListResponse.CommentItem comment,
            int rootCommentId,
            boolean isReply,
            int currentUserId,
            ReplyCallback replyCallback,
            Runnable reloadCallback
    ) {
        LinearLayout row = new LinearLayout(activity);
        row.setOrientation(LinearLayout.VERTICAL);
        row.setPadding(isReply ? dpToPx(activity, 32) : 0, dpToPx(activity, 8), 0, dpToPx(activity, 8));

        RelativeLayout headerRow = new RelativeLayout(activity);

        String authorName = "Người dùng";
        int commentAuthorId = 0;
        if (comment.author != null) {
            commentAuthorId = comment.author.userId;
            if (!isEmpty(comment.author.fullName)) authorName = comment.author.fullName.trim();
            else if (!isEmpty(comment.author.username)) authorName = comment.author.username.replace("@", "").trim();
        }

        TextView tvAuthor = new TextView(activity);
        tvAuthor.setText(authorName);
        tvAuthor.setTextSize(isReply ? 12 : 14);
        tvAuthor.setTypeface(null, Typeface.BOLD);
        tvAuthor.setTextColor(Color.parseColor("#1E232C"));
        final int targetUserId = commentAuthorId;
        tvAuthor.setOnClickListener(v -> {
            if (targetUserId <= 0) return;
            if (currentUserId > 0 && targetUserId == currentUserId) {
                activity.startActivity(new Intent(activity, ProfileActivity.class));
            } else {
                Intent i = new Intent(activity, UserProfileActivity.class);
                i.putExtra("USER_ID", targetUserId);
                activity.startActivity(i);
            }
        });

        RelativeLayout.LayoutParams lpAuthor = new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.WRAP_CONTENT,
                RelativeLayout.LayoutParams.WRAP_CONTENT
        );
        lpAuthor.addRule(RelativeLayout.ALIGN_PARENT_START);
        lpAuthor.addRule(RelativeLayout.CENTER_VERTICAL);
        headerRow.addView(tvAuthor, lpAuthor);

        boolean canDelete = currentUserId > 0 && commentAuthorId == currentUserId;
        if (canDelete) {
            TextView tvDelete = new TextView(activity);
            tvDelete.setText("Xóa");
            tvDelete.setTextSize(12);
            tvDelete.setTextColor(Color.parseColor("#E53935"));
            tvDelete.setPadding(dpToPx(activity, 8), dpToPx(activity, 2), dpToPx(activity, 4), dpToPx(activity, 2));
            RelativeLayout.LayoutParams lpDelete = new RelativeLayout.LayoutParams(
                    RelativeLayout.LayoutParams.WRAP_CONTENT,
                    RelativeLayout.LayoutParams.WRAP_CONTENT
            );
            lpDelete.addRule(RelativeLayout.ALIGN_PARENT_END);
            lpDelete.addRule(RelativeLayout.CENTER_VERTICAL);
            headerRow.addView(tvDelete, lpDelete);

            tvDelete.setOnClickListener(v -> new AlertDialog.Builder(activity)
                    .setTitle("Xóa bình luận")
                    .setMessage("Bạn có chắc chắn muốn xóa bình luận này?")
                    .setPositiveButton("Xóa", (dialog, which) -> {
                        String authHeader = ApiClient.getAuthHeader(activity);
                        ApiClient.getApiService().deleteComment(authHeader, comment.commentId).enqueue(new Callback<Object>() {
                            @Override
                            public void onResponse(@NonNull Call<Object> c, @NonNull Response<Object> r) {
                                if (activity.isFinishing()) return;
                                if (r.isSuccessful()) {
                                    Toast.makeText(activity, "Đã xóa bình luận!", Toast.LENGTH_SHORT).show();
                                    reloadCallback.run();
                                } else {
                                    Toast.makeText(activity, "Không thể xóa (mã " + r.code() + ")", Toast.LENGTH_SHORT).show();
                                }
                            }

                            @Override
                            public void onFailure(@NonNull Call<Object> c, @NonNull Throwable t) {
                                if (!activity.isFinishing()) {
                                    Toast.makeText(activity, "Lỗi kết nối khi xóa", Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
                    })
                    .setNegativeButton("Hủy", null)
                    .show());
        }

        TextView tvContent = new TextView(activity);
        tvContent.setText(comment.content != null ? comment.content : "");
        tvContent.setTextSize(isReply ? 12 : 13);
        tvContent.setTextColor(Color.parseColor("#3E2723"));
        tvContent.setPadding(0, dpToPx(activity, 3), 0, 0);

        LinearLayout metaRow = new LinearLayout(activity);
        metaRow.setOrientation(LinearLayout.HORIZONTAL);
        metaRow.setPadding(0, dpToPx(activity, 4), 0, 0);

        TextView tvTime = new TextView(activity);
        tvTime.setText(MyPostsActivity.formatTime(comment.createdAt));
        tvTime.setTextSize(11);
        tvTime.setTextColor(Color.parseColor("#9E8A83"));
        metaRow.addView(tvTime);

        final String replyName = authorName;
        TextView tvReply = new TextView(activity);
        tvReply.setText("Trả lời");
        tvReply.setTextSize(12);
        tvReply.setTypeface(null, Typeface.BOLD);
        tvReply.setTextColor(Color.parseColor("#FF5A36"));
        tvReply.setPadding(dpToPx(activity, 16), 0, dpToPx(activity, 8), 0);
        tvReply.setOnClickListener(v -> replyCallback.onReply(rootCommentId, replyName));
        metaRow.addView(tvReply);

        row.addView(headerRow);
        row.addView(tvContent);
        row.addView(metaRow);
        return row;
    }

    public static void openShareBottomSheet(Activity activity, FeedPostsResponse.PostItem post) {
        if (activity == null || post == null || activity.isFinishing()) return;
        ApiClient.init(activity);

        BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(activity);
        View dialogView = activity.getLayoutInflater().inflate(R.layout.layout_dialog_share_post, null);
        bottomSheetDialog.setContentView(dialogView);

        ImageView imgSharePreview = dialogView.findViewById(R.id.imgSharePreview);
        TextView tvShareAuthor = dialogView.findViewById(R.id.tvShareAuthor);
        TextView tvShareLocation = dialogView.findViewById(R.id.tvShareLocation);
        TextView tvShareSnippet = dialogView.findViewById(R.id.tvShareSnippet);

        String authorName = post.author != null && !isEmpty(post.author.fullName) ? post.author.fullName.trim() : "Người dùng";
        String handle = post.author != null && !isEmpty(post.author.username) ? "@" + post.author.username.replace("@", "").trim() : "";
        if (tvShareAuthor != null) tvShareAuthor.setText(handle.isEmpty() ? authorName : authorName + " • " + handle);

        String locDisplay = decodeLocationDisplay(post.locationUrl);
        if (tvShareLocation != null) tvShareLocation.setText(!locDisplay.isEmpty() ? locDisplay : "Dishcision");
        if (tvShareSnippet != null) tvShareSnippet.setText(post.content != null ? post.content : "");
        final String primaryImgUrl = (post.images != null && !post.images.isEmpty() && !isEmpty(post.images.get(0).imageUrl))
                ? post.images.get(0).imageUrl : null;
        if (imgSharePreview != null && primaryImgUrl != null) {
            ImageLoader.loadImage(imgSharePreview, primaryImgUrl);
        }

        View btnCloseShare = dialogView.findViewById(R.id.btnCloseShare);
        View btnSendDishcision = dialogView.findViewById(R.id.btnSendDishcision);
        View btnCopyLink = dialogView.findViewById(R.id.btnCopyLink);
        EditText edtSearchShare = dialogView.findViewById(R.id.edtSearchShare);
        LinearLayout containerContacts = dialogView.findViewById(R.id.containerShareContacts);
        TextView tvContactsStatus = dialogView.findViewById(R.id.tvShareContactsStatus);

        final List<FollowersListResponse.FollowItem> baseContacts = new ArrayList<>();
        final List<FollowersListResponse.FollowItem> visibleContacts = new ArrayList<>();
        final java.util.Set<Integer> selectedRecipientIds = new java.util.LinkedHashSet<>();
        final int currentUserId = AuthHelper.getCurrentUserId(activity);

        final Runnable renderContacts = () -> {
            if (containerContacts == null) return;
            containerContacts.removeAllViews();
            if (visibleContacts.isEmpty()) {
                if (tvContactsStatus != null) {
                    tvContactsStatus.setText("Chưa tìm thấy người nhận phù hợp.");
                    tvContactsStatus.setVisibility(View.VISIBLE);
                }
                return;
            }
            if (tvContactsStatus != null) tvContactsStatus.setVisibility(View.GONE);
            for (FollowersListResponse.FollowItem item : visibleContacts) {
                if (item.userId <= 0 || item.userId == currentUserId) continue;

                LinearLayout pill = new LinearLayout(activity);
                pill.setOrientation(LinearLayout.VERTICAL);
                pill.setGravity(android.view.Gravity.CENTER_HORIZONTAL);
                LinearLayout.LayoutParams pillLp = new LinearLayout.LayoutParams(dpToPx(activity, 72), LinearLayout.LayoutParams.WRAP_CONTENT);
                pillLp.setMarginEnd(dpToPx(activity, 8));
                pill.setLayoutParams(pillLp);
                pill.setClickable(true);
                pill.setFocusable(true);

                FrameLayout avatarFrame = new FrameLayout(activity);
                avatarFrame.setLayoutParams(new LinearLayout.LayoutParams(dpToPx(activity, 52), dpToPx(activity, 52)));

                ImageView imgAvatar = new ImageView(activity);
                FrameLayout.LayoutParams imgLp = new FrameLayout.LayoutParams(dpToPx(activity, 48), dpToPx(activity, 48));
                imgLp.gravity = android.view.Gravity.CENTER;
                imgAvatar.setLayoutParams(imgLp);
                imgAvatar.setBackgroundResource(R.drawable.bg_logo_circle);
                imgAvatar.setScaleType(ImageView.ScaleType.CENTER_CROP);
                imgAvatar.setImageResource(R.drawable.logo);
                if (!isEmpty(item.avatarUrl)) {
                    ImageLoader.loadAvatar(imgAvatar, item.avatarUrl);
                }
                avatarFrame.addView(imgAvatar);

                TextView tick = new TextView(activity);
                FrameLayout.LayoutParams tickLp = new FrameLayout.LayoutParams(dpToPx(activity, 18), dpToPx(activity, 18));
                tickLp.gravity = android.view.Gravity.TOP | android.view.Gravity.END;
                tick.setLayoutParams(tickLp);
                tick.setBackgroundResource(R.drawable.bg_badge_verified);
                tick.setGravity(android.view.Gravity.CENTER);
                tick.setText("✓");
                tick.setTextColor(Color.parseColor("#0A8050"));
                tick.setTextSize(TypedValue.COMPLEX_UNIT_SP, 11);
                tick.setTypeface(null, Typeface.BOLD);
                tick.setVisibility(selectedRecipientIds.contains(item.userId) ? View.VISIBLE : View.GONE);
                avatarFrame.addView(tick);

                TextView tvName = new TextView(activity);
                LinearLayout.LayoutParams nameLp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                nameLp.topMargin = dpToPx(activity, 4);
                tvName.setLayoutParams(nameLp);
                tvName.setGravity(android.view.Gravity.CENTER);
                tvName.setMaxLines(1);
                tvName.setEllipsize(android.text.TextUtils.TruncateAt.END);
                String displayName = !isEmpty(item.fullName) ? item.fullName : item.username;
                tvName.setText(displayName != null ? displayName : "Thành viên");
                tvName.setTextColor(Color.parseColor("#141B2B"));
                tvName.setTextSize(TypedValue.COMPLEX_UNIT_SP, 11);

                pill.addView(avatarFrame);
                pill.addView(tvName);

                pill.setOnClickListener(v -> {
                    if (selectedRecipientIds.contains(item.userId)) {
                        selectedRecipientIds.remove(item.userId);
                        tick.setVisibility(View.GONE);
                    } else {
                        selectedRecipientIds.add(item.userId);
                        tick.setVisibility(View.VISIBLE);
                    }
                });
                containerContacts.addView(pill);
            }
        };

        String auth = ApiClient.getAuthHeader(activity);
        if (!auth.isEmpty()) {
            if (tvContactsStatus != null) {
                tvContactsStatus.setText("Đang tải danh sách bạn bè...");
                tvContactsStatus.setVisibility(View.VISIBLE);
            }
            ApiClient.getApiService().getFollowing(auth, "me").enqueue(new Callback<FollowersListResponse>() {
                @Override
                public void onResponse(@NonNull Call<FollowersListResponse> call, @NonNull Response<FollowersListResponse> response) {
                    if (activity.isFinishing()) return;
                    baseContacts.clear();
                    visibleContacts.clear();
                    if (response.isSuccessful() && response.body() != null && response.body().data != null) {
                        for (FollowersListResponse.FollowItem item : response.body().data) {
                            if (item.userId > 0 && item.userId != currentUserId) {
                                baseContacts.add(item);
                            }
                        }
                        visibleContacts.addAll(baseContacts);
                    }
                    renderContacts.run();
                }

                @Override
                public void onFailure(@NonNull Call<FollowersListResponse> call, @NonNull Throwable t) {
                    if (activity.isFinishing()) return;
                    renderContacts.run();
                }
            });
        } else if (tvContactsStatus != null) {
            tvContactsStatus.setText("Đăng nhập để gửi bài viết cho bạn bè.");
            tvContactsStatus.setVisibility(View.VISIBLE);
        }

        if (edtSearchShare != null) {
            edtSearchShare.addTextChangedListener(new TextWatcher() {
                @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    String q = s.toString().toLowerCase(Locale.ROOT).trim();
                    visibleContacts.clear();
                    if (q.isEmpty()) {
                        visibleContacts.addAll(baseContacts);
                        renderContacts.run();
                        return;
                    }
                    for (FollowersListResponse.FollowItem item : baseContacts) {
                        String n = (item.fullName != null ? item.fullName : "").toLowerCase(Locale.ROOT);
                        String u = (item.username != null ? item.username : "").toLowerCase(Locale.ROOT);
                        if (n.contains(q) || u.contains(q)) {
                            visibleContacts.add(item);
                        }
                    }
                    renderContacts.run();
                    if (q.length() >= 2) {
                        ApiClient.getApiService().searchAccounts(q, 15).enqueue(new Callback<SearchAccountsResponse>() {
                            @Override
                            public void onResponse(@NonNull Call<SearchAccountsResponse> call, @NonNull Response<SearchAccountsResponse> response) {
                                if (activity.isFinishing() || !response.isSuccessful() || response.body() == null || response.body().data == null) {
                                    return;
                                }
                                String curQ = edtSearchShare.getText() != null ? edtSearchShare.getText().toString().toLowerCase(Locale.ROOT).trim() : "";
                                if (!curQ.equals(q)) return;
                                java.util.Set<Integer> seenIds = new java.util.LinkedHashSet<>();
                                for (FollowersListResponse.FollowItem existing : visibleContacts) {
                                    seenIds.add(existing.userId);
                                }
                                for (FeedPostsResponse.Author acc : response.body().data) {
                                    if (acc != null && acc.userId > 0 && acc.userId != currentUserId && !seenIds.contains(acc.userId)) {
                                        FollowersListResponse.FollowItem mapped = new FollowersListResponse.FollowItem();
                                        mapped.userId = acc.userId;
                                        mapped.fullName = acc.fullName;
                                        mapped.username = acc.username;
                                        mapped.avatarUrl = acc.avatarUrl;
                                        visibleContacts.add(mapped);
                                        seenIds.add(acc.userId);
                                    }
                                }
                                renderContacts.run();
                            }

                            @Override
                            public void onFailure(@NonNull Call<SearchAccountsResponse> call, @NonNull Throwable t) {}
                        });
                    }
                }

                @Override public void afterTextChanged(Editable s) {}
            });
        }

        if (btnCloseShare != null) {
            btnCloseShare.setOnClickListener(v -> bottomSheetDialog.dismiss());
        }

        if (btnCopyLink != null) {
            btnCopyLink.setOnClickListener(v -> {
                copyToClipboard(activity, "Post Link", ApiClient.getServerRootUrl() + "/posts/" + post.postId);
                Toast.makeText(activity, "Đã sao chép liên kết bài viết!", Toast.LENGTH_SHORT).show();
                bottomSheetDialog.dismiss();
            });
        }

        if (btnSendDishcision != null) {
            btnSendDishcision.setOnClickListener(v -> {
                String currentAuth = ApiClient.getAuthHeader(activity);
                if (currentAuth.isEmpty()) {
                    Toast.makeText(activity, "Vui lòng đăng nhập lại để chia sẻ", Toast.LENGTH_SHORT).show();
                    return;
                }
                btnSendDishcision.setEnabled(false);

                String rawSnippet = post.content != null ? post.content.trim() : "";
                if (rawSnippet.length() > 100) rawSnippet = rawSnippet.substring(0, 100) + "...";
                String shareMsg = "Đã chia sẻ bài viết của " + authorName
                        + (rawSnippet.isEmpty() ? "" : ": \"" + rawSnippet + "\"")
                        + "\n" + ApiClient.getServerRootUrl() + "/posts/" + post.postId;

                for (Integer recipientId : selectedRecipientIds) {
                    ApiClient.getApiService().sendChatMessage(
                            currentAuth,
                            recipientId,
                            new SendChatMessageRequest(shareMsg, primaryImgUrl)
                    ).enqueue(new Callback<SendChatMessageResponse>() {
                        @Override public void onResponse(@NonNull Call<SendChatMessageResponse> c, @NonNull Response<SendChatMessageResponse> r) {}
                        @Override public void onFailure(@NonNull Call<SendChatMessageResponse> c, @NonNull Throwable t) {}
                    });
                }

                ApiClient.getApiService().sharePost(currentAuth, post.postId).enqueue(new Callback<Object>() {
                    @Override
                    public void onResponse(@NonNull Call<Object> call, @NonNull Response<Object> response) {
                        btnSendDishcision.setEnabled(true);
                        if (activity.isFinishing()) return;
                        if (response.isSuccessful()) {
                            post.sharesCount++;
                            String msg = selectedRecipientIds.isEmpty()
                                    ? "Đã ghi nhận chia sẻ bài viết!"
                                    : "Đã gửi bài viết cho " + selectedRecipientIds.size() + " bạn bè!";
                            Toast.makeText(activity, msg, Toast.LENGTH_SHORT).show();
                            bottomSheetDialog.dismiss();
                        } else {
                            Toast.makeText(activity, "Chia sẻ thất bại (mã " + response.code() + ")", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(@NonNull Call<Object> call, @NonNull Throwable t) {
                        btnSendDishcision.setEnabled(true);
                        if (!activity.isFinishing()) {
                            Toast.makeText(activity, "Lỗi kết nối máy chủ", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            });
        }

        bottomSheetDialog.show();
    }

    public static void openPostOptionsBottomSheet(
            Activity activity,
            FeedPostsResponse.PostItem post,
            ActivityResultLauncher<Intent> editPostLauncher,
            PostCardListener listener
    ) {
        if (activity == null || post == null || activity.isFinishing()) return;

        View dialogView = activity.getLayoutInflater().inflate(R.layout.layout_dialog_post_options, null);
        BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(activity);
        bottomSheetDialog.setContentView(dialogView);

        View btnClose = dialogView.findViewById(R.id.btnCloseOptions);
        View layoutEditPost = dialogView.findViewById(R.id.layoutEditPost);
        View layoutCopyLink = dialogView.findViewById(R.id.layoutCopyLink);
        View layoutSharePost = dialogView.findViewById(R.id.layoutSharePost);
        View layoutArchivePost = dialogView.findViewById(R.id.layoutArchivePost);
        View btnReportPost = dialogView.findViewById(R.id.btnReportPost);
        View layoutDeletePost = dialogView.findViewById(R.id.layoutDeletePost);
        View dividerDelete = dialogView.findViewById(R.id.dividerDelete);

        int currentUserId = AuthHelper.getCurrentUserId(activity);
        boolean isOwner = post.author != null && post.author.userId > 0 && post.author.userId == currentUserId;

        if (isOwner) {
            if (layoutEditPost != null) layoutEditPost.setVisibility(View.VISIBLE);
            if (layoutArchivePost != null) layoutArchivePost.setVisibility(View.VISIBLE);
            if (layoutDeletePost != null) layoutDeletePost.setVisibility(View.VISIBLE);
            if (dividerDelete != null) dividerDelete.setVisibility(View.VISIBLE);
            if (btnReportPost != null) btnReportPost.setVisibility(View.GONE);
        } else {
            if (layoutEditPost != null) layoutEditPost.setVisibility(View.GONE);
            if (layoutArchivePost != null) layoutArchivePost.setVisibility(View.GONE);
            if (layoutDeletePost != null) layoutDeletePost.setVisibility(View.GONE);
            if (dividerDelete != null) dividerDelete.setVisibility(View.GONE);
            if (btnReportPost != null) btnReportPost.setVisibility(View.VISIBLE);
        }

        if (btnClose != null) {
            btnClose.setOnClickListener(v -> bottomSheetDialog.dismiss());
        }

        if (layoutCopyLink != null) {
            layoutCopyLink.setOnClickListener(v -> {
                bottomSheetDialog.dismiss();
                copyToClipboard(activity, "Post Link", ApiClient.getServerRootUrl() + "/posts/" + post.postId);
                Toast.makeText(activity, "Đã sao chép liên kết bài viết", Toast.LENGTH_SHORT).show();
            });
        }

        if (layoutSharePost != null) {
            layoutSharePost.setOnClickListener(v -> {
                bottomSheetDialog.dismiss();
                openShareBottomSheet(activity, post);
            });
        }

        if (layoutEditPost != null && isOwner) {
            layoutEditPost.setOnClickListener(v -> {
                bottomSheetDialog.dismiss();
                Intent intent = new Intent(activity, EditPostActivity.class);
                intent.putExtra("POST_ID", post.postId);
                intent.putExtra("POST_CONTENT", post.content != null ? post.content : "");
                intent.putExtra("POST_LOCATION", post.locationUrl != null ? post.locationUrl : "");
                intent.putExtra("POST_VOUCHER", post.voucherCode != null ? post.voucherCode : "");
                intent.putExtra("POST_STATUS", post.status != null ? post.status : "Active");
                ArrayList<String> hashtagsList = post.hashtags != null ? new ArrayList<>(post.hashtags) : new ArrayList<>();
                intent.putStringArrayListExtra("POST_HASHTAGS", hashtagsList);
                ArrayList<String> imgUrls = new ArrayList<>();
                if (post.images != null) {
                    for (FeedPostsResponse.PostImage img : post.images) {
                        if (img != null && !isEmpty(img.imageUrl)) {
                            imgUrls.add(img.imageUrl);
                        }
                    }
                }
                intent.putStringArrayListExtra("POST_IMAGES", imgUrls);
                if (editPostLauncher != null) {
                    editPostLauncher.launch(intent);
                } else {
                    activity.startActivity(intent);
                }
            });
        }

        if (layoutArchivePost != null && isOwner) {
            layoutArchivePost.setOnClickListener(v -> {
                bottomSheetDialog.dismiss();
                String auth = ApiClient.getAuthHeader(activity);
                if (auth.isEmpty()) return;
                UpdatePostRequest req = new UpdatePostRequest();
                req.status = "Hidden";
                ApiClient.getApiService().updatePost(auth, post.postId, req).enqueue(new Callback<CreatePostResponse>() {
                    @Override
                    public void onResponse(@NonNull Call<CreatePostResponse> call, @NonNull Response<CreatePostResponse> response) {
                        if (activity.isFinishing()) return;
                        if (response.isSuccessful()) {
                            Toast.makeText(activity, "Đã lưu trữ bài viết", Toast.LENGTH_SHORT).show();
                            if (listener != null) listener.onPostArchived(post.postId);
                        } else {
                            Toast.makeText(activity, "Không thể lưu trữ (mã " + response.code() + ")", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(@NonNull Call<CreatePostResponse> call, @NonNull Throwable t) {
                        if (!activity.isFinishing()) {
                            Toast.makeText(activity, "Lỗi kết nối máy chủ", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            });
        }

        if (layoutDeletePost != null && isOwner) {
            layoutDeletePost.setOnClickListener(v -> {
                bottomSheetDialog.dismiss();
                new AlertDialog.Builder(activity)
                        .setTitle("Xóa bài viết?")
                        .setMessage("Bạn có chắc chắn muốn xóa bài viết này không? Hành động này không thể hoàn tác.")
                        .setPositiveButton("Xóa", (d, w) -> {
                            String auth = ApiClient.getAuthHeader(activity);
                            if (auth.isEmpty()) return;
                            ApiClient.getApiService().deletePost(auth, post.postId).enqueue(new Callback<Object>() {
                                @Override
                                public void onResponse(@NonNull Call<Object> call, @NonNull Response<Object> response) {
                                    if (activity.isFinishing()) return;
                                    if (response.isSuccessful()) {
                                        Toast.makeText(activity, "Đã xóa bài viết thành công", Toast.LENGTH_SHORT).show();
                                        if (listener != null) listener.onPostDeleted(post.postId);
                                    } else {
                                        Toast.makeText(activity, "Không thể xóa bài viết (mã " + response.code() + ")", Toast.LENGTH_SHORT).show();
                                    }
                                }

                                @Override
                                public void onFailure(@NonNull Call<Object> call, @NonNull Throwable t) {
                                    if (!activity.isFinishing()) {
                                        Toast.makeText(activity, "Lỗi kết nối máy chủ", Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });
                        })
                        .setNegativeButton("Hủy", null)
                        .show();
            });
        }

        if (btnReportPost != null && !isOwner) {
            btnReportPost.setOnClickListener(v -> {
                bottomSheetDialog.dismiss();
                Intent intent = new Intent(activity, ReportContentActivity.class);
                intent.putExtra("POST_ID", post.postId);
                if (post.author != null && post.author.fullName != null) {
                    intent.putExtra("TARGET_SUBTITLE", "Tác giả: " + post.author.fullName);
                }
                if (post.content != null && !post.content.trim().isEmpty()) {
                    String preview = post.content.trim();
                    if (preview.length() > 140) preview = preview.substring(0, 140) + "...";
                    intent.putExtra("TARGET_EXCERPT", preview);
                }
                activity.startActivity(intent);
            });
        }

        bottomSheetDialog.show();
    }

    private static void copyToClipboard(Context context, String label, String text) {
        ClipboardManager clipboard = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
        if (clipboard != null) {
            clipboard.setPrimaryClip(ClipData.newPlainText(label, text));
        }
    }

    private static boolean isEmpty(String s) {
        return s == null || s.trim().isEmpty();
    }

    private static int dpToPx(Context context, int dp) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, context.getResources().getDisplayMetrics());
    }
}
