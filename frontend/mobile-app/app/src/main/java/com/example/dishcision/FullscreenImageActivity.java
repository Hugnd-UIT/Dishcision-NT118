package com.example.dishcision;

import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.activity.SystemBarStyle;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.view.WindowInsetsControllerCompat;
import androidx.viewpager.widget.PagerAdapter;
import androidx.viewpager.widget.ViewPager;

import com.example.dishcision.network.ImageLoader;

import java.util.ArrayList;
import java.util.List;

public class FullscreenImageActivity extends AppCompatActivity {

    public static final String EXTRA_AUTHOR = "extra_author";
    public static final String EXTRA_CAPTION = "extra_caption";
    public static final String EXTRA_IMAGE_URL = "extra_image_url";
    public static final String EXTRA_IMAGE_URLS = "extra_image_urls";
    public static final String EXTRA_INITIAL_INDEX = "extra_initial_index";

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
        View scrollFullCaption = findViewById(R.id.scrollFullCaption);
        ViewPager vpFullPhotos = findViewById(R.id.vpFullPhotos);
        TextView tvFullImageCounter = findViewById(R.id.tvFullImageCounter);
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
        List<String> imageUrls = new ArrayList<>();
        ArrayList<String> passedList = getIntent().getStringArrayListExtra(EXTRA_IMAGE_URLS);
        if (passedList != null) {
            for (String u : passedList) {
                if (!TextUtils.isEmpty(u) && !TextUtils.isEmpty(u.trim())) {
                    imageUrls.add(u.trim());
                }
            }
        }
        if (imageUrls.isEmpty()) {
            String singleUrl = getIntent().getStringExtra(EXTRA_IMAGE_URL);
            if (!TextUtils.isEmpty(singleUrl) && !TextUtils.isEmpty(singleUrl.trim())) {
                imageUrls.add(singleUrl.trim());
            }
        }

        int initialIndex = getIntent().getIntExtra(EXTRA_INITIAL_INDEX, 0);
        if (initialIndex < 0 || initialIndex >= imageUrls.size()) {
            initialIndex = 0;
        }

        if (tvFullImageCounter != null) {
            GradientDrawable badgeBg = new GradientDrawable();
            badgeBg.setColor(Color.parseColor("#80000000"));
            badgeBg.setCornerRadius(dpToPx(16));
            tvFullImageCounter.setBackground(badgeBg);
            if (imageUrls.size() > 1) {
                tvFullImageCounter.setText((initialIndex + 1) + " / " + imageUrls.size());
                tvFullImageCounter.setVisibility(View.VISIBLE);
            } else {
                tvFullImageCounter.setVisibility(View.GONE);
            }
        }

        if (vpFullPhotos != null) {
            if (!imageUrls.isEmpty()) {
                vpFullPhotos.setVisibility(View.VISIBLE);
                vpFullPhotos.setOffscreenPageLimit(Math.max(1, imageUrls.size() - 1));
                vpFullPhotos.setAdapter(new PagerAdapter() {
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
                        iv.setScaleType(ImageView.ScaleType.FIT_CENTER);
                        iv.setOnClickListener(v -> finish());
                        ImageLoader.loadImage(iv, imageUrls.get(position));
                        container.addView(iv);
                        return iv;
                    }

                    @Override
                    public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
                        container.removeView((View) object);
                    }
                });
                vpFullPhotos.setCurrentItem(initialIndex, false);
                vpFullPhotos.addOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
                    @Override
                    public void onPageSelected(int position) {
                        if (tvFullImageCounter != null && imageUrls.size() > 1) {
                            tvFullImageCounter.setText((position + 1) + " / " + imageUrls.size());
                        }
                    }
                });
            } else {
                vpFullPhotos.setVisibility(View.GONE);
            }
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
                if (scrollFullCaption != null) {
                    scrollFullCaption.setVisibility(View.VISIBLE);
                    scrollFullCaption.post(() -> {
                        int rootH = rootView != null && rootView.getHeight() > 0
                                ? rootView.getHeight()
                                : getResources().getDisplayMetrics().heightPixels;
                        int topH = topBar != null ? topBar.getHeight() : dpToPx(56);
                        int maxRatioH = (int) (rootH * (imageUrls.isEmpty() ? 0.68f : 0.34f));
                        int availableH = Math.max(dpToPx(80), rootH - topH - dpToPx(96));
                        int maxAllowed = Math.min(maxRatioH, availableH);
                        if (scrollFullCaption.getHeight() > maxAllowed) {
                            ViewGroup.LayoutParams svLp = scrollFullCaption.getLayoutParams();
                            svLp.height = maxAllowed;
                            scrollFullCaption.setLayoutParams(svLp);
                        }
                    });
                }
            } else {
                tvFullCaption.setVisibility(View.GONE);
                if (scrollFullCaption != null) {
                    scrollFullCaption.setVisibility(View.GONE);
                }
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

