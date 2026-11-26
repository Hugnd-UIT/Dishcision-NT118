package com.example.dishcision;

import android.app.Activity;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;

import com.example.dishcision.network.ImageLoader;

import java.util.ArrayList;
import java.util.List;

/** Hộp thoại chọn tài khoản (dùng cho Chặn / Hạn chế): tìm kiếm + phân trang "Xem thêm". */
final class AccountPickerDialog {

    static final int PAGE_SIZE = 10;

    static final class Item {
        final int userId;
        final String username;
        final String fullName;
        final String avatarUrl;

        Item(int userId, String username, String fullName, String avatarUrl) {
            this.userId = userId;
            this.username = username;
            this.fullName = fullName;
            this.avatarUrl = avatarUrl;
        }
    }

    static final class Page {
        final List<Item> items;
        final boolean hasMore;
        final boolean error;

        Page(List<Item> items, boolean hasMore, boolean error) {
            this.items = items;
            this.hasMore = hasMore;
            this.error = error;
        }

        static Page error() {
            return new Page(new ArrayList<>(), false, true);
        }
    }

    interface Done {
        void done(Page page);
    }

    /** page bắt đầu từ 1, mỗi trang PAGE_SIZE mục. */
    interface Loader {
        void load(String keyword, int page, Done done);
    }

    interface OnPick {
        void onPick(Item item, AlertDialog dialog);
    }

    private AccountPickerDialog() {}

    static void show(Activity activity, String title, String subtitle, String actionLabel,
                     String emptyText, Loader loader, OnPick onPick) {
        View content = activity.getLayoutInflater().inflate(R.layout.dialog_pick_account, null);
        ((TextView) content.findViewById(R.id.tvPickTitle)).setText(title);
        ((TextView) content.findViewById(R.id.tvPickSubtitle)).setText(subtitle);
        EditText edtQuery = content.findViewById(R.id.edtPickQuery);
        LinearLayout list = content.findViewById(R.id.listPick);

        final AlertDialog dialog = new AlertDialog.Builder(activity).setView(content).create();
        content.findViewById(R.id.btnPickClose).setOnClickListener(v -> dialog.dismiss());

        final Handler handler = new Handler(Looper.getMainLooper());
        final int[] token = {0};
        final int[] nextPage = {1};
        final boolean[] first = {true};
        final Runnable[] loadMore = new Runnable[1];

        float density = activity.getResources().getDisplayMetrics().density;

        Runnable restart = () -> {
            token[0]++;
            nextPage[0] = 1;
            first[0] = true;
            list.removeAllViews();
            loadMore[0].run();
        };

        loadMore[0] = () -> {
            final int myToken = token[0];
            final int page = nextPage[0];
            removeMoreButton(list);
            loader.load(edtQuery.getText().toString().trim(), page, result -> {
                if (myToken != token[0] || !dialog.isShowing()) return;
                if (result.error) {
                    list.addView(message(activity, "Không thể tải danh sách tài khoản", density));
                    return;
                }
                for (Item item : result.items) {
                    list.addView(row(activity, item, actionLabel, density, () -> onPick.onPick(item, dialog)));
                }
                if (first[0] && result.items.isEmpty()) {
                    list.addView(message(activity, emptyText, density));
                }
                first[0] = false;
                nextPage[0] = page + 1;
                if (result.hasMore) {
                    list.addView(moreButton(activity, density, () -> loadMore[0].run()));
                }
            });
        };

        edtQuery.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                handler.removeCallbacks(restart);
                handler.postDelayed(restart, 300);
            }
            @Override public void afterTextChanged(Editable s) {}
        });

        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
        dialog.show();
        dialog.getWindow().setLayout(
                (int) (activity.getResources().getDisplayMetrics().widthPixels * 0.9f),
                ViewGroup.LayoutParams.WRAP_CONTENT);
        restart.run();
    }

    private static final String MORE_TAG = "pick_more";

    private static void removeMoreButton(LinearLayout list) {
        View last = list.getChildCount() > 0 ? list.getChildAt(list.getChildCount() - 1) : null;
        if (last != null && MORE_TAG.equals(last.getTag())) list.removeView(last);
    }

    private static int dp(float density, int v) {
        return Math.round(v * density);
    }

    private static TextView message(Activity a, String text, float density) {
        TextView tv = new TextView(a);
        tv.setText(text);
        tv.setTextColor(Color.parseColor("#8E7570"));
        tv.setTextSize(13f);
        tv.setPadding(0, dp(density, 12), 0, dp(density, 12));
        return tv;
    }

    private static TextView moreButton(Activity a, float density, Runnable onClick) {
        TextView tv = new TextView(a);
        tv.setTag(MORE_TAG);
        tv.setText("Xem thêm");
        tv.setGravity(Gravity.CENTER);
        tv.setTextColor(Color.parseColor("#FF5722"));
        tv.setTextSize(14f);
        tv.setTypeface(null, Typeface.BOLD);
        tv.setPadding(0, dp(density, 12), 0, dp(density, 12));
        tv.setClickable(true);
        tv.setOnClickListener(v -> onClick.run());
        return tv;
    }

    private static View row(Activity a, Item item, String actionLabel, float density, Runnable onClick) {
        LinearLayout row = new LinearLayout(a);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(Gravity.CENTER_VERTICAL);
        row.setPadding(0, dp(density, 10), 0, dp(density, 10));
        row.setClickable(true);
        row.setOnClickListener(v -> onClick.run());

        ImageView avatar = new ImageView(a);
        avatar.setLayoutParams(new LinearLayout.LayoutParams(dp(density, 40), dp(density, 40)));
        avatar.setScaleType(ImageView.ScaleType.CENTER_CROP);
        avatar.setOutlineProvider(new android.view.ViewOutlineProvider() {
            @Override
            public void getOutline(View view, android.graphics.Outline outline) {
                outline.setOval(0, 0, view.getWidth(), view.getHeight());
            }
        });
        avatar.setClipToOutline(true);
        avatar.setImageResource(R.drawable.bg_avatar_circle);
        if (item.avatarUrl != null && !item.avatarUrl.trim().isEmpty()) {
            ImageLoader.loadImage(avatar, item.avatarUrl);
        }
        row.addView(avatar);

        LinearLayout info = new LinearLayout(a);
        info.setOrientation(LinearLayout.VERTICAL);
        LinearLayout.LayoutParams infoLp = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f);
        infoLp.setMarginStart(dp(density, 12));
        info.setLayoutParams(infoLp);

        TextView tvName = new TextView(a);
        tvName.setText(item.fullName != null && !item.fullName.isEmpty() ? item.fullName : item.username);
        tvName.setTextColor(Color.parseColor("#141B2B"));
        tvName.setTextSize(14f);
        tvName.setTypeface(null, Typeface.BOLD);
        tvName.setMaxLines(1);
        tvName.setEllipsize(android.text.TextUtils.TruncateAt.END);

        TextView tvUser = new TextView(a);
        tvUser.setText(item.username != null ? "@" + item.username : "");
        tvUser.setTextColor(Color.parseColor("#8E7570"));
        tvUser.setTextSize(12f);
        tvUser.setMaxLines(1);

        info.addView(tvName);
        info.addView(tvUser);
        row.addView(info);

        TextView btn = new TextView(a);
        btn.setText(actionLabel);
        btn.setTextColor(Color.parseColor("#C5221F"));
        btn.setTextSize(14f);
        btn.setTypeface(null, Typeface.BOLD);
        btn.setPadding(dp(density, 12), dp(density, 6), dp(density, 4), dp(density, 6));
        row.addView(btn);
        return row;
    }
}
