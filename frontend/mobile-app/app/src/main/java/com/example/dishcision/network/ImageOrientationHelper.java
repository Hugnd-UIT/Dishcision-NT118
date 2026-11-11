package com.example.dishcision.network;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.widget.ImageView;

import androidx.exifinterface.media.ExifInterface;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Normalizes EXIF image orientation and downsamples large photos for display and upload.
 */
public final class ImageOrientationHelper {

    public static final int DEFAULT_DISPLAY_MAX_DIM = 2048;
    public static final int DEFAULT_AVATAR_MAX_DIM = 512;
    public static final int DEFAULT_UPLOAD_MAX_DIM = 2048;
    private static final int JPEG_UPLOAD_QUALITY = 90;

    private static final ExecutorService IO_EXECUTOR = Executors.newFixedThreadPool(2);
    private static volatile Handler mainHandler;

    private ImageOrientationHelper() {}

    private static Handler getMainHandler() {
        Handler handler = mainHandler;
        if (handler == null) {
            handler = new Handler(Looper.getMainLooper());
            mainHandler = handler;
        }
        return handler;
    }

    public static final class OrientationTransform {
        public final int rotationDegrees;
        public final boolean flipHorizontal;
        public final boolean flipVertical;

        public OrientationTransform(int rotationDegrees, boolean flipHorizontal, boolean flipVertical) {
            this.rotationDegrees = rotationDegrees;
            this.flipHorizontal = flipHorizontal;
            this.flipVertical = flipVertical;
        }

        public boolean isIdentity() {
            return rotationDegrees == 0 && !flipHorizontal && !flipVertical;
        }
    }

    public static final class PreparedImage {
        public final File file;
        public final String mimeType;

        public PreparedImage(File file, String mimeType) {
            this.file = file;
            this.mimeType = mimeType;
        }
    }

    public interface PrepareUploadCallback {
        void onReady(PreparedImage prepared, Bitmap previewBitmap);
        void onError(Exception error);
    }

    /** Computes a power-of-two subsampling factor so neither dimension exceeds maxDimension. */
    public static int calculateInSampleSize(int rawWidth, int rawHeight, int maxDimension) {
        if (rawWidth <= 0 || rawHeight <= 0 || maxDimension <= 0) {
            return 1;
        }
        int inSampleSize = 1;
        while ((rawWidth / inSampleSize) > maxDimension || (rawHeight / inSampleSize) > maxDimension) {
            inSampleSize *= 2;
        }
        return inSampleSize;
    }

    /** Maps standard EXIF orientation codes (1..8) into rotation and reflection steps. */
    public static OrientationTransform resolveTransform(int exifOrientation) {
        switch (exifOrientation) {
            case ExifInterface.ORIENTATION_FLIP_HORIZONTAL:
                return new OrientationTransform(0, true, false);
            case ExifInterface.ORIENTATION_ROTATE_180:
                return new OrientationTransform(180, false, false);
            case ExifInterface.ORIENTATION_FLIP_VERTICAL:
                return new OrientationTransform(0, false, true);
            case ExifInterface.ORIENTATION_TRANSPOSE:
                return new OrientationTransform(90, true, false);
            case ExifInterface.ORIENTATION_ROTATE_90:
                return new OrientationTransform(90, false, false);
            case ExifInterface.ORIENTATION_TRANSVERSE:
                return new OrientationTransform(270, true, false);
            case ExifInterface.ORIENTATION_ROTATE_270:
                return new OrientationTransform(270, false, false);
            default:
                return new OrientationTransform(0, false, false);
        }
    }

    /** Updates filename extension so uploaded multipart name matches re-encoded MIME type. */
    public static String ensureExtensionMatchesMime(String rawFileName, String mimeType) {
        String desiredExt = extensionForMime(mimeType);
        String name = rawFileName != null ? rawFileName.trim() : "";
        if (name.isEmpty()) {
            return "upload" + desiredExt;
        }
        int dotIdx = name.lastIndexOf('.');
        String base = dotIdx > 0 ? name.substring(0, dotIdx) : name;
        String currentExt = dotIdx >= 0 ? name.substring(dotIdx).toLowerCase(Locale.US) : "";
        if (".jpeg".equals(currentExt) && ".jpg".equals(desiredExt)) {
            return name;
        }
        if (currentExt.equalsIgnoreCase(desiredExt)) {
            return name;
        }
        return base + desiredExt;
    }

    public static String extensionForMime(String mimeType) {
        if (mimeType == null) return ".jpg";
        String lower = mimeType.trim().toLowerCase(Locale.US);
        if (lower.equals("image/png")) return ".png";
        if (lower.equals("image/webp")) return ".webp";
        if (lower.equals("image/gif")) return ".gif";
        if (lower.equals("image/svg+xml")) return ".svg";
        if (lower.startsWith("video/")) return ".mp4";
        return ".jpg";
    }

    /** Decodes raw image bytes with subsampling and EXIF orientation correction. */
    public static Bitmap decodeSampledAndOrientedBitmap(byte[] bytes, int maxDimension) {
        if (bytes == null || bytes.length == 0) {
            return null;
        }
        int orientation = readExifOrientation(bytes);

        BitmapFactory.Options bounds = new BitmapFactory.Options();
        bounds.inJustDecodeBounds = true;
        BitmapFactory.decodeByteArray(bytes, 0, bytes.length, bounds);
        if (bounds.outWidth <= 0 || bounds.outHeight <= 0) {
            return null;
        }

        int sampleSize = calculateInSampleSize(bounds.outWidth, bounds.outHeight, maxDimension);
        Bitmap decoded = decodeBytesWithRetry(bytes, sampleSize);
        if (decoded == null) {
            return null;
        }
        return applyOrientationTransform(decoded, orientation);
    }

    /** Decodes a local image file with subsampling and EXIF orientation correction. */
    public static Bitmap decodeSampledAndOrientedBitmap(File file, int maxDimension) {
        if (file == null || !file.exists()) {
            return null;
        }
        String path = file.getAbsolutePath();
        int orientation = readExifOrientation(path);

        BitmapFactory.Options bounds = new BitmapFactory.Options();
        bounds.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(path, bounds);
        if (bounds.outWidth <= 0 || bounds.outHeight <= 0) {
            return null;
        }

        int sampleSize = calculateInSampleSize(bounds.outWidth, bounds.outHeight, maxDimension);
        Bitmap decoded = decodeFileWithRetry(path, sampleSize);
        if (decoded == null) {
            return null;
        }
        return applyOrientationTransform(decoded, orientation);
    }

    /** Decodes a content URI with EXIF orientation applied for upright local ImageView previews. */
    public static void loadLocalUri(Context context, ImageView target, Uri uri, int maxDimension) {
        if (context == null || target == null || uri == null) {
            return;
        }
        Context appContext = context.getApplicationContext();
        target.setTag(uri);
        IO_EXECUTOR.execute(() -> {
            Bitmap bitmap = decodeSampledAndOrientedBitmap(appContext, uri, maxDimension);
            getMainHandler().post(() -> {
                if (uri.equals(target.getTag()) && bitmap != null) {
                    target.setImageBitmap(bitmap);
                }
            });
        });
    }

    public static Bitmap decodeSampledAndOrientedBitmap(Context context, Uri uri, int maxDimension) {
        if (context == null || uri == null) {
            return null;
        }
        int orientation = readExifOrientation(context, uri);

        BitmapFactory.Options bounds = new BitmapFactory.Options();
        bounds.inJustDecodeBounds = true;
        try (InputStream boundsIn = context.getContentResolver().openInputStream(uri)) {
            if (boundsIn == null) return null;
            BitmapFactory.decodeStream(boundsIn, null, bounds);
        } catch (Exception ignored) {
            return null;
        }
        if (bounds.outWidth <= 0 || bounds.outHeight <= 0) {
            return null;
        }

        int sampleSize = calculateInSampleSize(bounds.outWidth, bounds.outHeight, maxDimension);
        for (int attempt = 0; attempt < 3; attempt++) {
            BitmapFactory.Options opts = new BitmapFactory.Options();
            opts.inSampleSize = sampleSize;
            try (InputStream imgIn = context.getContentResolver().openInputStream(uri)) {
                if (imgIn == null) return null;
                Bitmap decoded = BitmapFactory.decodeStream(imgIn, null, opts);
                if (decoded != null) {
                    return applyOrientationTransform(decoded, orientation);
                }
                return null;
            } catch (OutOfMemoryError oom) {
                sampleSize *= 2;
            } catch (Exception ignored) {
                return null;
            }
        }
        return null;
    }

    /**
     * Copies a picked URI into cache on a background thread, applying EXIF orientation and max
     * dimension normalization when needed, and optionally returning a scaled preview Bitmap.
     */
    public static void prepareUploadAsync(
            Context context,
            Uri uri,
            String tempPrefix,
            int previewMaxDim,
            PrepareUploadCallback callback
    ) {
        if (context == null || uri == null || callback == null) {
            return;
        }
        Context appContext = context.getApplicationContext();
        IO_EXECUTOR.execute(() -> {
            try {
                PreparedImage prepared = prepareUploadImage(appContext, uri, tempPrefix);
                Bitmap preview = null;
                if (previewMaxDim > 0 && prepared.mimeType.startsWith("image/")
                        && !prepared.mimeType.equalsIgnoreCase("image/gif")
                        && !prepared.mimeType.equalsIgnoreCase("image/svg+xml")) {
                    preview = decodeSampledAndOrientedBitmap(prepared.file, previewMaxDim);
                }
                final Bitmap finalPreview = preview;
                getMainHandler().post(() -> callback.onReady(prepared, finalPreview));
            } catch (Exception e) {
                getMainHandler().post(() -> callback.onError(e));
            }
        });
    }

    /**
     * Produces an upload-ready file in cache whose pixels are already oriented upright and within
     * DEFAULT_UPLOAD_MAX_DIM. Non-raster / animated assets are passed through unchanged.
     */
    public static PreparedImage prepareUploadImage(Context context, Uri uri, String tempPrefix) throws IOException {
        String sourceMime = context.getContentResolver().getType(uri);
        String rawExt = extensionForMime(sourceMime);
        File rawFile = File.createTempFile(tempPrefix + "raw_", rawExt, context.getCacheDir());

        try (InputStream in = context.getContentResolver().openInputStream(uri)) {
            if (in == null) {
                deleteQuietly(rawFile);
                throw new IOException("Không thể mở luồng dữ liệu ảnh");
            }
            try (FileOutputStream out = new FileOutputStream(rawFile)) {
                byte[] buffer = new byte[8192];
                int read;
                while ((read = in.read(buffer)) != -1) {
                    out.write(buffer, 0, read);
                }
                out.flush();
            }
        } catch (IOException | RuntimeException e) {
            deleteQuietly(rawFile);
            throw e instanceof IOException ? (IOException) e : new IOException(e);
        }

        if (isPassThroughMime(sourceMime)) {
            return new PreparedImage(rawFile, sourceMime);
        }

        int orientation = readExifOrientation(rawFile.getAbsolutePath());
        BitmapFactory.Options bounds = new BitmapFactory.Options();
        bounds.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(rawFile.getAbsolutePath(), bounds);
        if (bounds.outWidth <= 0 || bounds.outHeight <= 0) {
            return new PreparedImage(rawFile, fallbackImageMime(sourceMime));
        }

        boolean needsReencode = !resolveTransform(orientation).isIdentity()
                || bounds.outWidth > DEFAULT_UPLOAD_MAX_DIM
                || bounds.outHeight > DEFAULT_UPLOAD_MAX_DIM
                || isHeifMime(sourceMime);

        if (!needsReencode) {
            return new PreparedImage(rawFile, fallbackImageMime(sourceMime));
        }

        Bitmap normalized = decodeSampledAndOrientedBitmap(rawFile, DEFAULT_UPLOAD_MAX_DIM);
        if (normalized == null) {
            return new PreparedImage(rawFile, fallbackImageMime(sourceMime));
        }

        boolean keepPng = "image/png".equalsIgnoreCase(sourceMime) && normalized.hasAlpha();
        String outMime = keepPng ? "image/png" : "image/jpeg";
        String outExt = keepPng ? ".png" : ".jpg";
        File cleanFile = File.createTempFile(tempPrefix, outExt, context.getCacheDir());

        try (FileOutputStream out = new FileOutputStream(cleanFile)) {
            Bitmap.CompressFormat format = keepPng ? Bitmap.CompressFormat.PNG : Bitmap.CompressFormat.JPEG;
            int quality = keepPng ? 100 : JPEG_UPLOAD_QUALITY;
            if (!normalized.compress(format, quality, out)) {
                deleteQuietly(cleanFile);
                return new PreparedImage(rawFile, fallbackImageMime(sourceMime));
            }
            out.flush();
        } catch (IOException | RuntimeException e) {
            deleteQuietly(cleanFile);
            return new PreparedImage(rawFile, fallbackImageMime(sourceMime));
        } finally {
            if (!normalized.isRecycled()) {
                normalized.recycle();
            }
        }

        deleteQuietly(rawFile);
        return new PreparedImage(cleanFile, outMime);
    }

    public static void deleteQuietly(File file) {
        if (file != null && file.exists()) {
            //noinspection ResultOfMethodCallIgnored
            file.delete();
        }
    }

    private static Bitmap decodeBytesWithRetry(byte[] bytes, int initialSampleSize) {
        int sampleSize = Math.max(1, initialSampleSize);
        for (int attempt = 0; attempt < 3; attempt++) {
            BitmapFactory.Options opts = new BitmapFactory.Options();
            opts.inSampleSize = sampleSize;
            try {
                Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length, opts);
                if (bitmap != null) {
                    return bitmap;
                }
                return null;
            } catch (OutOfMemoryError oom) {
                sampleSize *= 2;
            }
        }
        return null;
    }

    private static Bitmap decodeFileWithRetry(String path, int initialSampleSize) {
        int sampleSize = Math.max(1, initialSampleSize);
        for (int attempt = 0; attempt < 3; attempt++) {
            BitmapFactory.Options opts = new BitmapFactory.Options();
            opts.inSampleSize = sampleSize;
            try {
                Bitmap bitmap = BitmapFactory.decodeFile(path, opts);
                if (bitmap != null) {
                    return bitmap;
                }
                return null;
            } catch (OutOfMemoryError oom) {
                sampleSize *= 2;
            }
        }
        return null;
    }

    private static Bitmap applyOrientationTransform(Bitmap bitmap, int exifOrientation) {
        OrientationTransform transform = resolveTransform(exifOrientation);
        if (transform.isIdentity()) {
            return bitmap;
        }
        Matrix matrix = new Matrix();
        if (transform.rotationDegrees != 0) {
            matrix.postRotate(transform.rotationDegrees);
        }
        if (transform.flipHorizontal || transform.flipVertical) {
            matrix.postScale(
                    transform.flipHorizontal ? -1f : 1f,
                    transform.flipVertical ? -1f : 1f
            );
        }
        try {
            Bitmap transformed = Bitmap.createBitmap(
                    bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true
            );
            if (transformed != bitmap && !bitmap.isRecycled()) {
                bitmap.recycle();
            }
            return transformed;
        } catch (OutOfMemoryError oom) {
            return bitmap;
        }
    }

    private static int readExifOrientation(byte[] bytes) {
        try {
            ExifInterface exif = new ExifInterface(new ByteArrayInputStream(bytes));
            return exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);
        } catch (Exception ignored) {
            return ExifInterface.ORIENTATION_NORMAL;
        }
    }

    private static int readExifOrientation(String filePath) {
        try {
            ExifInterface exif = new ExifInterface(filePath);
            return exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);
        } catch (Exception ignored) {
            return ExifInterface.ORIENTATION_NORMAL;
        }
    }

    private static int readExifOrientation(Context context, Uri uri) {
        try (InputStream in = context.getContentResolver().openInputStream(uri)) {
            if (in == null) return ExifInterface.ORIENTATION_NORMAL;
            ExifInterface exif = new ExifInterface(in);
            return exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);
        } catch (Exception ignored) {
            return ExifInterface.ORIENTATION_NORMAL;
        }
    }

    private static boolean isPassThroughMime(String mimeType) {
        if (mimeType == null) return false;
        String lower = mimeType.trim().toLowerCase(Locale.US);
        return lower.equals("image/gif")
                || lower.equals("image/svg+xml")
                || lower.startsWith("video/");
    }

    private static boolean isHeifMime(String mimeType) {
        if (mimeType == null) return false;
        String lower = mimeType.trim().toLowerCase(Locale.US);
        return lower.equals("image/heic") || lower.equals("image/heif");
    }

    private static String fallbackImageMime(String mimeType) {
        if (mimeType == null || mimeType.trim().isEmpty() || "image/*".equals(mimeType.trim())) {
            return "image/jpeg";
        }
        return mimeType.trim();
    }
}

