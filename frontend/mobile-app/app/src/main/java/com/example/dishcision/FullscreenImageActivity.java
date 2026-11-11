package com.example.dishcision;

import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.activity.SystemBarStyle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.view.WindowInsetsControllerCompat;

import com.example.dishcision.network.ImageLoader;

public class FullscreenImageActivity extends AppCompatActivity {

    public static final String EXTRA_AUTHOR = "extra_author";
    public static final String EXTRA_CAPTION = "extra_caption";
    public static final String EXTRA_IMAGE_URL = "extra_image_url";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        EdgeToEdge.enable(
                this,
                SystemBarStyle.dark(Color.BLACK),
                SystemBarStyle.dark(Color.BLACK)
        );
        super.onCreate(savedInstanceState);

        Window window = getWindow();
        if (window != null) {
            WindowCompat.setDecorFitsSystemWindows(window, false);
            WindowManager.LayoutParams lp = window.getAttributes();
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                lp.layoutInDisplayCutoutMode = WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_ALWAYS;
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                lp.layoutInDisplayCutoutMode = WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES;
            }
            window.setAttributes(lp);

            WindowInsetsControllerCompat controller = WindowCompat.getInsetsController(window, window.getDecorView());
            if (controller != null) {
                controller.setAppearanceLightStatusBars(false);
                controller.setAppearanceLightNavigationBars(false);
                controller.hide(WindowInsetsCompat.Type.systemBars());
                controller.setSystemBarsBehavior(
                        WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
                );
            }
        }

        setContentView(R.layout.layout_dialog_full_image);

        View rootView = findViewById(R.id.main_full_image);
        View topBar = findViewById(R.id.topBar);
        View bottomCaption = findViewById(R.id.bottomCaption);
        ImageView imgFullPhoto = findViewById(R.id.imgFullPhoto);
        ImageView btnClose = findViewById(R.id.btnCloseFullImage);
        TextView tvFullAuthor = findViewById(R.id.tvFullAuthor);
        TextView tvFullCaption = findViewById(R.id.tvFullCaption);

        if (rootView != null) {
            ViewCompat.setOnApplyWindowInsetsListener(rootView, (v, insets) -> {
                Insets safe = insets.getInsetsIgnoringVisibility(
                        WindowInsetsCompat.Type.systemBars() | WindowInsetsCompat.Type.displayCutout()
                );
                if (topBar != null) {
                    topBar.setPadding(
                            dpToPx(16) + safe.left,
                            safe.top + dpToPx(8),
                            dpToPx(16) + safe.right,
                            dpToPx(8)
                    );
                }
                if (bottomCaption != null) {
                    bottomCaption.setPadding(
                            dpToPx(16) + safe.left,
                            dpToPx(12),
                            dpToPx(16) + safe.right,
                            dpToPx(16) + safe.bottom
                    );
                }
                return WindowInsetsCompat.CONSUMED;
            });
        }

        String author = getIntent().getStringExtra(EXTRA_AUTHOR);
        String caption = getIntent().getStringExtra(EXTRA_CAPTION);
        String imageUrl = getIntent().getStringExtra(EXTRA_IMAGE_URL);

        if (imgFullPhoto != null && !TextUtils.isEmpty(imageUrl)) {
            ImageLoader.loadImage(imgFullPhoto, imageUrl);
        }

        boolean hasAuthor = !TextUtils.isEmpty(author) && !TextUtils.isEmpty(author.trim());
        boolean hasCaption = !TextUtils.isEmpty(caption) && !TextUtils.isEmpty(caption.trim());

        if (bottomCaption != null) {
            bottomCaption.setVisibility(hasAuthor || hasCaption ? View.VISIBLE : View.GONE);
        }
        if (tvFullAuthor != null) {
            if (hasAuthor) {
                tvFullAuthor.setText(author.trim());
                tvFullAuthor.setVisibility(View.VISIBLE);
            } else {
                tvFullAuthor.setVisibility(View.GONE);
            }
        }
        if (tvFullCaption != null) {
            if (hasCaption) {
                tvFullCaption.setText(caption.trim());
                tvFullCaption.setVisibility(View.VISIBLE);
            } else {
                tvFullCaption.setVisibility(View.GONE);
            }
        }

        if (btnClose != null) {
            btnClose.setOnClickListener(v -> finish());
        }
        if (rootView != null) {
            rootView.setOnClickListener(v -> finish());
        }
    }

    private int dpToPx(int dp) {
        return (int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP, dp, getResources().getDisplayMetrics()
        );
    }
}
