package com.example.dishcision;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.provider.MediaStore;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.BaseAdapter;
import android.widget.FrameLayout;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.core.content.ContextCompat;

import com.example.dishcision.network.ImageOrientationHelper;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/** Bảng ảnh dưới thanh nhập chat (kiểu Zalo): ô "Chụp ảnh" + lưới ảnh thư viện, chọn nhiều ảnh rồi gửi. */
final class ChatMediaPanel {

    interface Listener {
        void onCameraClick();

        void onSend(List<Uri> images);
    }

    private static final int COLUMNS = 3;
    private static final int MAX_SELECTED = 10;
    private static final int MAX_LOADED = 300;
    private static final int THUMB_MAX_DIM = 320;

    private final Context context;
    private final View root;
    private final GridView grid;
    private final TextView btnSend;
    private final Listener listener;
    private final List<Uri> images = new ArrayList<>();
    private final List<Uri> selected = new ArrayList<>();
    private final ExecutorService io = Executors.newSingleThreadExecutor();
    private final int gapPx;
    private final int cellPx;
    private boolean loaded = false;

    private final BaseAdapter adapter = new BaseAdapter() {
        @Override public int getCount() { return images.size() + 1; }
        @Override public Object getItem(int position) { return position == 0 ? null : images.get(position - 1); }
        @Override public long getItemId(int position) { return position; }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (position == 0) return cameraTile();
            FrameLayout cell = convertView instanceof FrameLayout && convertView.getTag() != null
                    ? (FrameLayout) convertView : photoCell();
            Uri uri = images.get(position - 1);
            ImageView img = (ImageView) cell.getChildAt(0);
            TextView badge = (TextView) cell.getChildAt(1);
            View dim = cell.getChildAt(2);
            img.setImageDrawable(null);
            ImageOrientationHelper.loadLocalUri(context, img, uri, THUMB_MAX_DIM);
            int order = selected.indexOf(uri);
            boolean on = order >= 0;
            badge.setText(on ? String.valueOf(order + 1) : "");
            badge.setBackgroundResource(on ? R.drawable.bg_media_select_on : R.drawable.bg_media_select_off);
            dim.setVisibility(on ? View.VISIBLE : View.GONE);
            return cell;
        }
    };

    ChatMediaPanel(Context context, View root, Listener listener) {
        this.context = context;
        this.root = root;
        this.listener = listener;
        this.grid = root.findViewById(R.id.gridMedia);
        this.btnSend = root.findViewById(R.id.btnSendMedia);
        this.gapPx = dp(2);
        int width = context.getResources().getDisplayMetrics().widthPixels;
        this.cellPx = (width - gapPx * (COLUMNS - 1)) / COLUMNS;

        grid.setNumColumns(COLUMNS);
        grid.setColumnWidth(cellPx);
        grid.setStretchMode(GridView.STRETCH_COLUMN_WIDTH);
        grid.setHorizontalSpacing(gapPx);
        grid.setVerticalSpacing(gapPx);
        grid.setAdapter(adapter);
        grid.setOnItemClickListener((p, v, position, id) -> {
            if (position == 0) {
                listener.onCameraClick();
                return;
            }
            Uri uri = images.get(position - 1);
            if (!selected.remove(uri)) {
                if (selected.size() >= MAX_SELECTED) {
                    Toast.makeText(context, "Chọn tối đa " + MAX_SELECTED + " ảnh", Toast.LENGTH_SHORT).show();
                    return;
                }
                selected.add(uri);
            }
            adapter.notifyDataSetChanged();
            refreshSendButton();
        });
        btnSend.setOnClickListener(v -> {
            List<Uri> out = new ArrayList<>(selected);
            if (out.isEmpty()) return;
            clearSelection();
            listener.onSend(out);
        });
    }

    static String requiredPermission() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU
                ? Manifest.permission.READ_MEDIA_IMAGES
                : Manifest.permission.READ_EXTERNAL_STORAGE;
    }

    static boolean hasPermission(Context context) {
        return ContextCompat.checkSelfPermission(context, requiredPermission()) == PackageManager.PERMISSION_GRANTED;
    }

    boolean isShowing() {
        return root.getVisibility() == View.VISIBLE;
    }

    void show() {
        root.setVisibility(View.VISIBLE);
        loadImages();
    }

    void hide() {
        root.setVisibility(View.GONE);
        clearSelection();
    }

    void destroy() {
        io.shutdownNow();
    }

    private void clearSelection() {
        selected.clear();
        adapter.notifyDataSetChanged();
        refreshSendButton();
    }

    private void refreshSendButton() {
        btnSend.setVisibility(selected.isEmpty() ? View.GONE : View.VISIBLE);
        btnSend.setText("Gửi (" + selected.size() + ")");
    }

    private void loadImages() {
        if (loaded) return;
        loaded = true;
        io.execute(() -> {
            List<Uri> found = new ArrayList<>();
            String[] projection = {MediaStore.Images.Media._ID};
            try (Cursor c = context.getContentResolver().query(
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI, projection, null, null,
                    MediaStore.Images.Media.DATE_ADDED + " DESC")) {
                if (c != null) {
                    int idCol = c.getColumnIndexOrThrow(MediaStore.Images.Media._ID);
                    while (c.moveToNext() && found.size() < MAX_LOADED) {
                        found.add(android.content.ContentUris.withAppendedId(
                                MediaStore.Images.Media.EXTERNAL_CONTENT_URI, c.getLong(idCol)));
                    }
                }
            } catch (Exception ignored) {
                loaded = false;
            }
            root.post(() -> {
                images.clear();
                images.addAll(found);
                adapter.notifyDataSetChanged();
            });
        });
    }

    private View cameraTile() {
        LinearLayout tile = new LinearLayout(context);
        tile.setOrientation(LinearLayout.VERTICAL);
        tile.setGravity(Gravity.CENTER);
        tile.setBackgroundColor(0xFFF1F3F9);
        tile.setLayoutParams(new AbsListView.LayoutParams(cellPx, cellPx));
        ImageView icon = new ImageView(context);
        icon.setImageResource(R.drawable.ic_camera);
        icon.setColorFilter(0xFF505568);
        tile.addView(icon, new LinearLayout.LayoutParams(dp(32), dp(32)));
        TextView label = new TextView(context);
        label.setText("Chụp ảnh");
        label.setGravity(Gravity.CENTER);
        label.setTextColor(0xFF505568);
        label.setTextSize(TypedValue.COMPLEX_UNIT_SP, 13);
        label.setPadding(0, dp(6), 0, 0);
        tile.addView(label);
        return tile;
    }

    private FrameLayout photoCell() {
        FrameLayout cell = new FrameLayout(context);
        cell.setTag("photo");
        cell.setLayoutParams(new AbsListView.LayoutParams(cellPx, cellPx));
        ImageView img = new ImageView(context);
        img.setScaleType(ImageView.ScaleType.CENTER_CROP);
        img.setBackgroundColor(0xFFE5E7EF);
        cell.addView(img, new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        TextView badge = new TextView(context);
        badge.setGravity(Gravity.CENTER);
        badge.setTextColor(Color.WHITE);
        badge.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12);
        FrameLayout.LayoutParams badgeLp = new FrameLayout.LayoutParams(dp(24), dp(24), Gravity.TOP | Gravity.END);
        badgeLp.setMargins(0, dp(6), dp(6), 0);
        cell.addView(badge, badgeLp);
        View dim = new View(context);
        dim.setBackgroundColor(0x33FFFFFF);
        dim.setVisibility(View.GONE);
        cell.addView(dim, 2, new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        return cell;
    }

    private int dp(int v) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, v, context.getResources().getDisplayMetrics());
    }
}
