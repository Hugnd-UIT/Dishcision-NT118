package com.example.dishcision;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.View;

import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;

public class WheelView extends View {

    private final List<String> items = new ArrayList<>();
    private final RectF wheelRect = new RectF();

    private Paint slicePaint;
    private Paint dividerPaint;
    private Paint textPaint;
    private Paint borderPaint;
    private Paint hubPaint;

    // Palette of vibrant, appetizing slice colors
    private static final int[] SLICE_COLORS = {
            Color.parseColor("#00B894"), // Mint Green
            Color.parseColor("#FF7675"), // Coral Salmon
            Color.parseColor("#0984E3"), // Royal Blue
            Color.parseColor("#FDCB6E"), // Warm Yellow
            Color.parseColor("#E17055"), // Deep Orange
            Color.parseColor("#6C5CE7"), // Purple
            Color.parseColor("#00CEC9"), // Robin Egg Blue
            Color.parseColor("#FD79A8"), // Pink
            Color.parseColor("#2ECC71"), // Emerald Green
            Color.parseColor("#E67E22"), // Carrot Orange
            Color.parseColor("#9B59B6"), // Amethyst
            Color.parseColor("#3498DB")  // Dodger Blue
    };

    public WheelView(Context context) {
        super(context);
        init();
    }

    public WheelView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public WheelView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        slicePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        slicePaint.setStyle(Paint.Style.FILL);

        dividerPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        dividerPaint.setStyle(Paint.Style.STROKE);
        dividerPaint.setColor(Color.WHITE);
        dividerPaint.setStrokeWidth(dpToPx(2.5f));

        borderPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        borderPaint.setStyle(Paint.Style.STROKE);
        borderPaint.setColor(Color.WHITE);
        borderPaint.setStrokeWidth(dpToPx(3.5f));

        hubPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        hubPaint.setStyle(Paint.Style.FILL);
        hubPaint.setColor(Color.WHITE);

        textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        textPaint.setColor(Color.WHITE);
        textPaint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));
        textPaint.setTextAlign(Paint.Align.CENTER);
        textPaint.setShadowLayer(dpToPx(2f), 0, 1.5f, Color.argb(160, 0, 0, 0));
    }

    public void setItems(List<String> newItems) {
        items.clear();
        if (newItems != null) {
            items.addAll(newItems);
        }
        invalidate();
    }

    public List<String> getItems() {
        return new ArrayList<>(items);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        int count = items.size();
        if (count == 0) return;

        float width = getWidth();
        float height = getHeight();
        float diameter = Math.min(width, height);
        float radius = (diameter / 2f) - dpToPx(3f);
        float cx = width / 2f;
        float cy = height / 2f;
        float rHub = dpToPx(33f);

        wheelRect.set(cx - radius, cy - radius, cx + radius, cy + radius);

        float sweepAngle = 360f / count;

        // Dynamic text size based on item count
        float textSizeSp;
        if (count <= 4) {
            textSizeSp = 12.5f;
        } else if (count <= 6) {
            textSizeSp = 11.5f;
        } else if (count <= 8) {
            textSizeSp = 10.5f;
        } else if (count <= 10) {
            textSizeSp = 9.5f;
        } else {
            textSizeSp = 8.8f;
        }
        textPaint.setTextSize(spToPx(textSizeSp));

        // 1. Draw pie slices and dividers
        for (int i = 0; i < count; i++) {
            slicePaint.setColor(SLICE_COLORS[i % SLICE_COLORS.length]);
            float startAngle = i * sweepAngle;
            canvas.drawArc(wheelRect, startAngle, sweepAngle, true, slicePaint);
            canvas.drawArc(wheelRect, startAngle, sweepAngle, true, dividerPaint);
        }

        // 2. Draw outer border
        canvas.drawCircle(cx, cy, radius, borderPaint);

        // 3. Draw center hub base
        canvas.drawCircle(cx, cy, rHub, hubPaint);

        // 4. Draw dish labels with ellipsis ".." truncation if too long
        float maxTextWidth = (radius - rHub) * 0.82f;
        float textRadius = (rHub + radius) / 2f;

        for (int i = 0; i < count; i++) {
            String originalDish = items.get(i);
            String displayDish = formatDishName(originalDish, textPaint, maxTextWidth);

            float midAngle = i * sweepAngle + (sweepAngle / 2f);
            float normAngle = (midAngle % 360f + 360f) % 360f;

            Paint.FontMetrics fm = textPaint.getFontMetrics();
            float textY = cy - (fm.ascent + fm.descent) / 2f;

            canvas.save();
            if (normAngle > 90f && normAngle < 270f) {
                // Invert angle so text on left side is always right-side up
                canvas.rotate(normAngle + 180f, cx, cy);
                canvas.drawText(displayDish, cx - textRadius, textY, textPaint);
            } else {
                canvas.rotate(normAngle, cx, cy);
                canvas.drawText(displayDish, cx + textRadius, textY, textPaint);
            }
            canvas.restore();
        }
    }

    private String formatDishName(String dishName, Paint paint, float maxWidth) {
        if (dishName == null || dishName.trim().isEmpty()) return "";
        dishName = dishName.trim();
        if (paint.measureText(dishName) <= maxWidth) {
            return dishName;
        }

        // Truncate with ".." if text is too long to prevent overflowing
        String suffix = "..";
        float suffixWidth = paint.measureText(suffix);
        float availableWidth = maxWidth - suffixWidth;
        if (availableWidth <= 0) return suffix;

        int len = dishName.length();
        int charCount = paint.breakText(dishName, 0, len, true, availableWidth, null);
        if (charCount <= 0) return suffix;
        return dishName.substring(0, charCount).trim() + suffix;
    }

    private float dpToPx(float dp) {
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, getResources().getDisplayMetrics());
    }

    private float spToPx(float sp) {
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, sp, getResources().getDisplayMetrics());
    }
}

