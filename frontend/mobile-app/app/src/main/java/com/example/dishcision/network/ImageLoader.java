package com.example.dishcision.network;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.util.LruCache;
import android.widget.ImageView;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ImageLoader {

    private static final int MAX_CACHE_SIZE = (int) (Runtime.getRuntime().maxMemory() / 1024) / 8;
    private static final LruCache<String, Bitmap> memoryCache = new LruCache<String, Bitmap>(MAX_CACHE_SIZE) {
        @Override
        protected int sizeOf(String key, Bitmap bitmap) {
            return bitmap.getByteCount() / 1024;
        }
    };

    private static final ExecutorService executor = Executors.newFixedThreadPool(4);

    public static final String DEFAULT_AVATAR_URL = "/uploads/images/default-avatar.jpg";

    /** Drops every cached bitmap; returns the approximate size freed in KB. */
    public static int clearCache() {
        int sizeKb = memoryCache.size();
        memoryCache.evictAll();
        return sizeKb;
    }

    public static void loadAvatar(ImageView imageView, String imageUrl) {
        if (imageView == null) {
            return;
        }

        String resolved = (imageUrl == null || imageUrl.trim().isEmpty()) ? DEFAULT_AVATAR_URL : imageUrl.trim();
        String finalUrl = ApiClient.normalizeMediaUrl(resolved);
        if (finalUrl.isEmpty()) {
            finalUrl = ApiClient.normalizeMediaUrl(DEFAULT_AVATAR_URL);
        }
        if (finalUrl.isEmpty()) {
            return;
        }

        Bitmap cached = memoryCache.get(finalUrl + "_circle");
        if (cached != null) {
            imageView.setImageBitmap(cached);
            return;
        }

        final String targetUrl = finalUrl;
        imageView.setTag(targetUrl);

        executor.execute(() -> {
            Bitmap bitmap = downloadBitmap(targetUrl);
            if (bitmap != null) {
                Bitmap circular = getCircularBitmap(bitmap);
                memoryCache.put(targetUrl + "_circle", circular);
                imageView.post(() -> {
                    Object tag = imageView.getTag();
                    if (tag != null && tag.equals(targetUrl)) {
                        imageView.setImageBitmap(circular);
                    }
                });
            }
        });
    }

    public static void loadImage(ImageView imageView, String imageUrl) {
        if (imageView == null || imageUrl == null || imageUrl.trim().isEmpty()) {
            return;
        }

        String finalUrl = ApiClient.normalizeMediaUrl(imageUrl);
        if (finalUrl.isEmpty()) {
            return;
        }

        Bitmap cached = memoryCache.get(finalUrl);
        if (cached != null) {
            imageView.setImageBitmap(cached);
            return;
        }

        final String targetUrl = finalUrl;
        imageView.setTag(targetUrl);

        executor.execute(() -> {
            Bitmap bitmap = downloadBitmap(targetUrl);
            if (bitmap != null) {
                memoryCache.put(targetUrl, bitmap);
                imageView.post(() -> {
                    Object tag = imageView.getTag();
                    if (tag != null && tag.equals(targetUrl)) {
                        imageView.setImageBitmap(bitmap);
                    }
                });
            }
        });
    }

    private static Bitmap downloadBitmap(String src) {
        try {
            URL url = new URL(src);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setDoInput(true);
            connection.setConnectTimeout(10000);
            connection.setReadTimeout(10000);
            connection.connect();
            InputStream input = connection.getInputStream();
            Bitmap bitmap = BitmapFactory.decodeStream(input);
            input.close();
            connection.disconnect();
            return bitmap;
        } catch (Exception e) {
            return null;
        }
    }

    private static Bitmap getCircularBitmap(Bitmap bitmap) {
        int size = Math.min(bitmap.getWidth(), bitmap.getHeight());
        Bitmap output = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888);

        Canvas canvas = new Canvas(output);
        Paint paint = new Paint();
        Rect rect = new Rect(0, 0, size, size);

        paint.setAntiAlias(true);
        canvas.drawARGB(0, 0, 0, 0);
        paint.setColor(0xff424242);
        canvas.drawCircle(size / 2f, size / 2f, size / 2f, paint);

        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));

        int x = (bitmap.getWidth() - size) / 2;
        int y = (bitmap.getHeight() - size) / 2;
        Rect srcRect = new Rect(x, y, x + size, y + size);

        canvas.drawBitmap(bitmap, srcRect, rect, paint);
        return output;
    }
}
