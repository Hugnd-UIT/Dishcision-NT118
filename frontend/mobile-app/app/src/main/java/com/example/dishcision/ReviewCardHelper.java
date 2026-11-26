package com.example.dishcision;

import android.content.Context;
import android.graphics.Typeface;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.core.content.ContextCompat;

import com.example.dishcision.network.ImageLoader;
import com.example.dishcision.network.ReviewsResponse;

import java.util.Locale;

/** Dựng thẻ đánh giá dùng chung cho trang quán và hồ sơ quán. */
public final class ReviewCardHelper {
    private ReviewCardHelper() {}

    private static int dp(Context ctx, int v) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, v, ctx.getResources().getDisplayMetrics());
    }

    public static View build(Context ctx, ReviewsResponse.ReviewItem rev) {
        LinearLayout card = new LinearLayout(ctx);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        params.topMargin = dp(ctx, 8);
        card.setLayoutParams(params);
        card.setBackgroundResource(R.drawable.bg_info_box);
        card.setOrientation(LinearLayout.VERTICAL);
        int pad = dp(ctx, 12);
        card.setPadding(pad, pad, pad, pad);

        LinearLayout header = new LinearLayout(ctx);
        header.setOrientation(LinearLayout.HORIZONTAL);
        header.setGravity(Gravity.CENTER_VERTICAL);

        String authorName = "Người dùng";
        String avatarUrl = null;
        if (rev.author != null) {
            if (rev.author.fullName != null && !rev.author.fullName.isEmpty()) authorName = rev.author.fullName;
            else if (rev.author.username != null) authorName = rev.author.username;
            avatarUrl = rev.author.avatarUrl;
        }

        if (avatarUrl != null && !avatarUrl.isEmpty()) {
            ImageView imgAvatar = new ImageView(ctx);
            imgAvatar.setLayoutParams(new LinearLayout.LayoutParams(dp(ctx, 32), dp(ctx, 32)));
            imgAvatar.setScaleType(ImageView.ScaleType.CENTER_CROP);
            ImageLoader.loadAvatar(imgAvatar, avatarUrl);
            header.addView(imgAvatar);
        } else {
            TextView tvInitial = new TextView(ctx);
            tvInitial.setLayoutParams(new LinearLayout.LayoutParams(dp(ctx, 32), dp(ctx, 32)));
            tvInitial.setBackgroundResource(R.drawable.bg_logo_circle);
            tvInitial.setGravity(Gravity.CENTER);
            tvInitial.setText(authorName.substring(0, 1).toUpperCase(Locale.ROOT));
            tvInitial.setTextColor(ContextCompat.getColor(ctx, R.color.primary));
            tvInitial.setTextSize(13f);
            tvInitial.setTypeface(null, Typeface.BOLD);
            header.addView(tvInitial);
        }

        LinearLayout metaCol = new LinearLayout(ctx);
        LinearLayout.LayoutParams mcParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        mcParams.setMarginStart(dp(ctx, 10));
        metaCol.setLayoutParams(mcParams);
        metaCol.setOrientation(LinearLayout.VERTICAL);

        TextView tvAuthor = new TextView(ctx);
        tvAuthor.setText(authorName);
        tvAuthor.setTextColor(ContextCompat.getColor(ctx, R.color.text_dark));
        tvAuthor.setTextSize(13f);
        tvAuthor.setTypeface(null, Typeface.BOLD);

        TextView tvMeta = new TextView(ctx);
        String timeStr = MyPostsActivity.formatTime(rev.createdAt);
        tvMeta.setText(rev.rating + " sao" + (!timeStr.isEmpty() ? " • " + timeStr : ""));
        tvMeta.setTextColor(ContextCompat.getColor(ctx, R.color.text_gray));
        tvMeta.setTextSize(11f);

        metaCol.addView(tvAuthor);
        metaCol.addView(tvMeta);
        header.addView(metaCol);
        card.addView(header);

        if (rev.content != null && !rev.content.trim().isEmpty()) {
            TextView tvBody = new TextView(ctx);
            LinearLayout.LayoutParams bParams = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            bParams.topMargin = dp(ctx, 8);
            tvBody.setLayoutParams(bParams);
            tvBody.setText(rev.content.trim());
            tvBody.setTextColor(ContextCompat.getColor(ctx, R.color.text_dark));
            tvBody.setTextSize(13f);
            card.addView(tvBody);
        }

        return card;
    }
}
