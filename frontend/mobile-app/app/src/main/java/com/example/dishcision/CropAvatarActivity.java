package com.example.dishcision;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.net.Uri;
import android.os.Bundle;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.dishcision.network.ImageOrientationHelper;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/** Cho người dùng kéo/phóng ảnh trong khung tròn rồi trả về ảnh vuông đã cắt (Uri qua FileProvider). */
public class CropAvatarActivity extends AppCompatActivity {

    private static final int OUTPUT_SIZE = 512;
    private static final int DECODE_MAX_DIM = 2048;

    private CropView cropView;
    private TextView btnDone;
    private final ExecutorService io = Executors.newSingleThreadExecutor();

    public static Intent newIntent(Context context, Uri source) {
        Intent intent = new Intent(context, CropAvatarActivity.class);
        intent.setData(source);
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        return intent;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);

        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setBackgroundColor(Color.BLACK);

        TextView hint = new TextView(this);
        hint.setText("Kéo và chụm hai ngón để căn ảnh đại diện");
        hint.setTextColor(Color.WHITE);
        hint.setTextSize(14);
        hint.setGravity(Gravity.CENTER);
        hint.setPadding(dp(16), dp(16), dp(16), dp(8));
        root.addView(hint, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

        cropView = new CropView(this);
        root.addView(cropView, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, 0, 1f));

        LinearLayout bar = new LinearLayout(this);
        bar.setOrientation(LinearLayout.HORIZONTAL);
        bar.setPadding(dp(16), dp(8), dp(16), dp(16));
        TextView btnCancel = barButton("Hủy", Color.WHITE);
        btnCancel.setOnClickListener(v -> finish());
        btnDone = barButton("Xong", Color.parseColor("#FF5A36"));
        btnDone.setOnClickListener(v -> finishCrop());
        bar.addView(btnCancel, new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));
        bar.addView(btnDone, new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));
        root.addView(bar);

        setContentView(root);
        ViewCompat.setOnApplyWindowInsetsListener(root, (v, insets) -> {
            Insets bars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(bars.left, bars.top, bars.right, bars.bottom);
            return insets;
        });

        Uri source = getIntent().getData();
        if (source == null) {
            finish();
            return;
        }
        btnDone.setEnabled(false);
        io.execute(() -> {
            Bitmap bmp = ImageOrientationHelper.decodeSampledAndOrientedBitmap(this, source, DECODE_MAX_DIM);
            runOnUiThread(() -> {
                if (isFinishing() || isDestroyed()) return;
                if (bmp == null) {
                    Toast.makeText(this, "Không đọc được ảnh", Toast.LENGTH_SHORT).show();
                    finish();
                    return;
                }
                cropView.setBitmap(bmp);
                btnDone.setEnabled(true);
            });
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        io.shutdown();
    }

    private void finishCrop() {
        btnDone.setEnabled(false);
        Bitmap out = cropView.crop(OUTPUT_SIZE);
        io.execute(() -> {
            try {
                File dir = new File(getCacheDir(), "camera");
                if (!dir.exists() && !dir.mkdirs()) throw new IOException("mkdirs failed");
                File file = File.createTempFile("avatar_crop_", ".jpg", dir);
                try (FileOutputStream fos = new FileOutputStream(file)) {
                    out.compress(Bitmap.CompressFormat.JPEG, 92, fos);
                }
                Uri uri = FileProvider.getUriForFile(this, getPackageName() + ".fileprovider", file);
                runOnUiThread(() -> {
                    Intent result = new Intent().setData(uri).addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                    setResult(RESULT_OK, result);
                    finish();
                });
            } catch (IOException e) {
                runOnUiThread(() -> {
                    Toast.makeText(this, "Không thể cắt ảnh", Toast.LENGTH_SHORT).show();
                    btnDone.setEnabled(true);
                });
            }
        });
    }

    private TextView barButton(String text, int color) {
        TextView tv = new TextView(this);
        tv.setText(text);
        tv.setTextColor(color);
        tv.setTextSize(16);
        tv.setGravity(Gravity.CENTER);
        tv.setPadding(0, dp(12), 0, dp(12));
        return tv;
    }

    private int dp(int v) {
        return (int) (v * getResources().getDisplayMetrics().density);
    }

    /** View vẽ ảnh dưới lớp phủ tối có lỗ tròn; kéo + chụm để chỉnh. */
    private static final class CropView extends View {
        private Bitmap bmp;
        private final Matrix matrix = new Matrix();
        private final RectF crop = new RectF();
        private final RectF tmp = new RectF();
        private final Path overlay = new Path();
        private final Paint bmpPaint = new Paint(Paint.FILTER_BITMAP_FLAG);
        private final Paint dimPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        private final Paint ringPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        private final float[] vals = new float[9];
        private final ScaleGestureDetector scaleDetector;
        private float minScale = 1f;
        private float lastX, lastY;

        CropView(Context context) {
            super(context);
            dimPaint.setColor(Color.argb(160, 0, 0, 0));
            ringPaint.setColor(Color.WHITE);
            ringPaint.setStyle(Paint.Style.STROKE);
            ringPaint.setStrokeWidth(2f * context.getResources().getDisplayMetrics().density);
            scaleDetector = new ScaleGestureDetector(context, new ScaleGestureDetector.SimpleOnScaleGestureListener() {
                @Override
                public boolean onScale(ScaleGestureDetector d) {
                    float cur = currentScale();
                    float target = Math.max(minScale, Math.min(cur * d.getScaleFactor(), minScale * 6f));
                    matrix.postScale(target / cur, target / cur, d.getFocusX(), d.getFocusY());
                    clamp();
                    invalidate();
                    return true;
                }
            });
        }

        void setBitmap(Bitmap b) {
            bmp = b;
            fit();
        }

        Bitmap crop(int size) {
            Bitmap out = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888);
            Canvas c = new Canvas(out);
            c.drawColor(Color.WHITE);
            Matrix t = new Matrix(matrix);
            t.postTranslate(-crop.left, -crop.top);
            t.postScale(size / crop.width(), size / crop.height());
            c.drawBitmap(bmp, t, bmpPaint);
            return out;
        }

        @Override
        protected void onSizeChanged(int w, int h, int oldw, int oldh) {
            float side = Math.min(w, h) - 2f * getResources().getDisplayMetrics().density * 16f;
            crop.set((w - side) / 2f, (h - side) / 2f, (w + side) / 2f, (h + side) / 2f);
            overlay.reset();
            overlay.setFillType(Path.FillType.EVEN_ODD);
            overlay.addRect(0, 0, w, h, Path.Direction.CW);
            overlay.addCircle(crop.centerX(), crop.centerY(), side / 2f, Path.Direction.CW);
            fit();
        }

        private void fit() {
            if (bmp == null || crop.isEmpty()) return;
            minScale = Math.max(crop.width() / bmp.getWidth(), crop.height() / bmp.getHeight());
            matrix.setScale(minScale, minScale);
            matrix.postTranslate(crop.centerX() - bmp.getWidth() * minScale / 2f,
                    crop.centerY() - bmp.getHeight() * minScale / 2f);
            invalidate();
        }

        private float currentScale() {
            matrix.getValues(vals);
            return vals[Matrix.MSCALE_X];
        }

        /** Giữ ảnh luôn phủ kín khung cắt. */
        private void clamp() {
            tmp.set(0, 0, bmp.getWidth(), bmp.getHeight());
            matrix.mapRect(tmp);
            float dx = 0, dy = 0;
            if (tmp.left > crop.left) dx = crop.left - tmp.left;
            else if (tmp.right < crop.right) dx = crop.right - tmp.right;
            if (tmp.top > crop.top) dy = crop.top - tmp.top;
            else if (tmp.bottom < crop.bottom) dy = crop.bottom - tmp.bottom;
            matrix.postTranslate(dx, dy);
        }

        @Override
        public boolean onTouchEvent(MotionEvent e) {
            if (bmp == null) return false;
            scaleDetector.onTouchEvent(e);
            int action = e.getActionMasked();
            int skip = action == MotionEvent.ACTION_POINTER_UP ? e.getActionIndex() : -1;
            float fx = 0, fy = 0;
            int n = 0;
            for (int i = 0; i < e.getPointerCount(); i++) {
                if (i == skip) continue;
                fx += e.getX(i);
                fy += e.getY(i);
                n++;
            }
            if (n == 0) return true;
            fx /= n;
            fy /= n;
            if (action == MotionEvent.ACTION_MOVE) {
                matrix.postTranslate(fx - lastX, fy - lastY);
                clamp();
                invalidate();
            }
            lastX = fx;
            lastY = fy;
            return true;
        }

        @Override
        protected void onDraw(Canvas canvas) {
            if (bmp == null) return;
            canvas.drawBitmap(bmp, matrix, bmpPaint);
            canvas.drawPath(overlay, dimPaint);
            canvas.drawCircle(crop.centerX(), crop.centerY(), crop.width() / 2f, ringPaint);
        }
    }
}
