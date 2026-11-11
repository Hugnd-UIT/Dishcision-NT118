package com.example.dishcision;

import androidx.exifinterface.media.ExifInterface;

import com.example.dishcision.network.ImageOrientationHelper;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class ImageOrientationHelperTest {

    @Test
    public void calculatesPowerOfTwoSampleSizes() {
        assertEquals(2, ImageOrientationHelper.calculateInSampleSize(4032, 3024, 2048));
        assertEquals(8, ImageOrientationHelper.calculateInSampleSize(4032, 3024, 512));
        assertEquals(1, ImageOrientationHelper.calculateInSampleSize(1080, 1920, 2048));
        assertEquals(1, ImageOrientationHelper.calculateInSampleSize(2048, 1536, 2048));
        assertEquals(8, ImageOrientationHelper.calculateInSampleSize(10000, 7500, 2048));
        assertEquals(1, ImageOrientationHelper.calculateInSampleSize(0, 1000, 2048));
        assertEquals(1, ImageOrientationHelper.calculateInSampleSize(1000, -1, 2048));
    }

    @Test
    public void resolvesAllEightExifOrientations() {
        assertTrue(ImageOrientationHelper.resolveTransform(ExifInterface.ORIENTATION_UNDEFINED).isIdentity());
        assertTrue(ImageOrientationHelper.resolveTransform(ExifInterface.ORIENTATION_NORMAL).isIdentity());
        assertTrue(ImageOrientationHelper.resolveTransform(-1).isIdentity());

        ImageOrientationHelper.OrientationTransform flipH =
                ImageOrientationHelper.resolveTransform(ExifInterface.ORIENTATION_FLIP_HORIZONTAL);
        assertFalse(flipH.isIdentity());
        assertEquals(0, flipH.rotationDegrees);
        assertTrue(flipH.flipHorizontal);
        assertFalse(flipH.flipVertical);

        ImageOrientationHelper.OrientationTransform rot180 =
                ImageOrientationHelper.resolveTransform(ExifInterface.ORIENTATION_ROTATE_180);
        assertEquals(180, rot180.rotationDegrees);
        assertFalse(rot180.flipHorizontal);
        assertFalse(rot180.flipVertical);

        ImageOrientationHelper.OrientationTransform flipV =
                ImageOrientationHelper.resolveTransform(ExifInterface.ORIENTATION_FLIP_VERTICAL);
        assertEquals(0, flipV.rotationDegrees);
        assertFalse(flipV.flipHorizontal);
        assertTrue(flipV.flipVertical);

        ImageOrientationHelper.OrientationTransform transpose =
                ImageOrientationHelper.resolveTransform(ExifInterface.ORIENTATION_TRANSPOSE);
        assertEquals(90, transpose.rotationDegrees);
        assertTrue(transpose.flipHorizontal);
        assertFalse(transpose.flipVertical);

        ImageOrientationHelper.OrientationTransform rot90 =
                ImageOrientationHelper.resolveTransform(ExifInterface.ORIENTATION_ROTATE_90);
        assertEquals(90, rot90.rotationDegrees);
        assertFalse(rot90.flipHorizontal);
        assertFalse(rot90.flipVertical);

        ImageOrientationHelper.OrientationTransform transverse =
                ImageOrientationHelper.resolveTransform(ExifInterface.ORIENTATION_TRANSVERSE);
        assertEquals(270, transverse.rotationDegrees);
        assertTrue(transverse.flipHorizontal);
        assertFalse(transverse.flipVertical);

        ImageOrientationHelper.OrientationTransform rot270 =
                ImageOrientationHelper.resolveTransform(ExifInterface.ORIENTATION_ROTATE_270);
        assertEquals(270, rot270.rotationDegrees);
        assertFalse(rot270.flipHorizontal);
        assertFalse(rot270.flipVertical);
    }

    @Test
    public void matchesFilenameExtensionWithMimeType() {
        assertEquals("photo.jpg", ImageOrientationHelper.ensureExtensionMatchesMime("photo.heic", "image/jpeg"));
        assertEquals("photo.png", ImageOrientationHelper.ensureExtensionMatchesMime("photo.png", "image/png"));
        assertEquals("photo.JPEG", ImageOrientationHelper.ensureExtensionMatchesMime("photo.JPEG", "image/jpeg"));
        assertEquals("photo.jpg", ImageOrientationHelper.ensureExtensionMatchesMime("photo", "image/jpeg"));
        assertEquals("upload.jpg", ImageOrientationHelper.ensureExtensionMatchesMime("   ", "image/jpeg"));
        assertEquals("clip.gif", ImageOrientationHelper.ensureExtensionMatchesMime("clip.gif", "image/gif"));
    }
}

