package com.example.chieuthu3_ketthucmonhoc.utils;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.Uri;
import android.provider.OpenableColumns;
import android.util.LruCache;
import android.widget.ImageView;

import androidx.annotation.DrawableRes;
import androidx.annotation.Nullable;

import com.example.chieuthu3_ketthucmonhoc.domain.models.FoodImageResolver;
import com.example.chieuthu3_ketthucmonhoc.domain.models.MenuItem;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Locale;
import java.util.UUID;

/**
 * Tiện ích lưu trữ và nạp ảnh món ăn từ bộ nhớ thiết bị (chọn từ thư viện/máy ảnh),
 * tự động thu gọn kích thước, chỉnh hướng EXIF, trích xuất đặc trưng màu sắc món ăn
 * và fallback về drawable mặc định của nhà hàng.
 */
public final class FoodImageLoader {

    private static final String FOOD_IMAGE_DIR = "food_images";
    private static final String FOOD_SCAN_DIR = "food_scans";
    private static final String CAMERA_CACHE_DIR = "camera_photos";
    private static final int MAX_SAVED_DIMENSION_PX = 800;
    private static final int DISPLAY_DECODE_MAX_PX = 600;
    private static final int JPEG_QUALITY = 85;

    private static final LruCache<String, Bitmap> BITMAP_CACHE;

    static {
        long maxMemoryKb = Runtime.getRuntime().maxMemory() / 1024L;
        int cacheSizeKb = (int) Math.min(16 * 1024L, Math.max(4 * 1024L, maxMemoryKb / 8L));
        BITMAP_CACHE = new LruCache<String, Bitmap>(cacheSizeKb) {
            @Override
            protected int sizeOf(String key, Bitmap bitmap) {
                return bitmap.getByteCount() / 1024;
            }
        };
    }

    private FoodImageLoader() {
    }

    /**
     * Nạp ảnh món ăn vào ImageView: ưu tiên ảnh tùy chỉnh đã lưu từ điện thoại nếu tồn tại,
     * nếu không có thì fallback về ảnh drawable chuẩn theo {@link FoodImageResolver}.
     */
    public static void loadFoodImage(ImageView imageView, @Nullable MenuItem item) {
        if (imageView == null) {
            return;
        }
        int fallbackResId = FoodImageResolver.resolveImage(item);
        if (item == null || !item.hasCustomImage()) {
            imageView.setImageResource(fallbackResId);
            return;
        }
        loadImagePathOrFallback(imageView, item.getImagePath(), fallbackResId);
    }

    /**
     * Nạp ảnh từ đường dẫn cục bộ hoặc content URI vào ImageView; dùng fallback drawable nếu trống/lỗi.
     */
    public static void loadImagePathOrFallback(ImageView imageView, @Nullable String imagePath, @DrawableRes int fallbackResId) {
        if (imageView == null) {
            return;
        }
        String trimmed = imagePath != null ? imagePath.trim() : "";
        if (trimmed.isEmpty()) {
            imageView.setImageResource(fallbackResId);
            return;
        }

        Bitmap cached = BITMAP_CACHE.get(trimmed);
        if (cached != null && !cached.isRecycled()) {
            imageView.setImageBitmap(cached);
            return;
        }

        Bitmap decoded = decodeFromPathOrUri(imageView.getContext(), trimmed, DISPLAY_DECODE_MAX_PX);
        if (decoded != null) {
            BITMAP_CACHE.put(trimmed, decoded);
            imageView.setImageBitmap(decoded);
        } else {
            imageView.setImageResource(fallbackResId);
        }
    }

    /**
     * Sao chép và tối ưu kích thước ảnh người dùng chọn từ điện thoại vào thư mục riêng của ứng dụng
     * ({@code filesDir/food_images}) để lưu trữ lâu dài trong SQLite không phụ thuộc quyền URI tạm thời.
     *
     * @return Đường dẫn tuyệt đối của tệp ảnh đã lưu, hoặc {@code null} nếu không thể đọc/lưu ảnh.
     */
    @Nullable
    public static String savePickedImageToInternalStorage(Context context, @Nullable Uri sourceUri) {
        return saveImageToSubdir(context, sourceUri, FOOD_IMAGE_DIR, "food_");
    }

    /**
     * Lưu ảnh tải lên/chụp để nhận diện món ăn vào {@code filesDir/food_scans} giúp hiển thị ổn định trong khung chat.
     */
    @Nullable
    public static String saveScanImageToInternalStorage(Context context, @Nullable Uri sourceUri) {
        return saveImageToSubdir(context, sourceUri, FOOD_SCAN_DIR, "scan_");
    }

    /**
     * Tạo tệp tạm trong {@code cacheDir/camera_photos} để ứng dụng Camera ghi ảnh chụp thông qua FileProvider.
     */
    @Nullable
    public static File createTempCameraPhotoFile(Context context) {
        if (context == null) {
            return null;
        }
        File dir = new File(context.getCacheDir(), CAMERA_CACHE_DIR);
        if (!dir.exists() && !dir.mkdirs()) {
            return null;
        }
        return new File(dir, "capture_" + System.currentTimeMillis() + ".jpg");
    }

    /**
     * Lấy tên hiển thị của tệp từ Content URI để gắn nhãn thân thiện và trích xuất từ khóa món ăn nếu có.
     */
    public static String getDisplayName(Context context, @Nullable Uri uri) {
        if (context == null || uri == null) {
            return "Ảnh món ăn";
        }
        if ("content".equalsIgnoreCase(uri.getScheme())) {
            try (Cursor cursor = context.getContentResolver().query(
                    uri, new String[]{OpenableColumns.DISPLAY_NAME}, null, null, null)) {
                if (cursor != null && cursor.moveToFirst()) {
                    int idx = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
                    if (idx >= 0) {
                        String name = cursor.getString(idx);
                        if (name != null && !name.trim().isEmpty()) {
                            return name.trim();
                        }
                    }
                }
            } catch (Exception ignored) {
            }
        }
        String last = uri.getLastPathSegment();
        if (last != null && !last.trim().isEmpty()) {
            int slash = last.lastIndexOf('/');
            return slash >= 0 ? last.substring(slash + 1) : last;
        }
        return "Ảnh món ăn";
    }

    /**
     * Phân tích tên tệp gốc + đặc trưng màu sắc HSV thực tế của ảnh tải lên/chụp để sinh chuỗi đặc trưng
     * giúp {@code ChatbotMLModel.classifyFoodImage(...)} khớp món phù hợp ngay cả khi tên ảnh là {@code IMG_...jpg}.
     */
    public static String buildFoodRecognitionHint(Context context, @Nullable String imagePath, @Nullable String originalName) {
        StringBuilder hints = new StringBuilder();
        String normalizedName = (originalName != null ? originalName : "")
                .toLowerCase(Locale.ROOT)
                .replace('_', ' ')
                .replace('-', ' ')
                .replace('.', ' ');

        if (normalizedName.contains("chan gio") || normalizedName.contains("chân giò") || normalizedName.contains("gio heo")) {
            hints.append(" chân giò chiên giòn ");
        }
        if (normalizedName.contains("ech") || normalizedName.contains("ếch")) {
            hints.append(" ếch nướng ");
        }
        if (normalizedName.contains("ga ") || normalizedName.startsWith("ga") || normalizedName.contains("gà")) {
            if (normalizedName.contains("chan ga") || normalizedName.contains("chân gà")) {
                hints.append(" chân gà sả tắc sốt thái chua cay ");
            } else {
                hints.append(" gà nướng mật ong ");
            }
        }
        if (normalizedName.contains("banh bao") || normalizedName.contains("bánh bao")) {
            hints.append(" bánh bao ");
        }
        if (normalizedName.contains("tom") || normalizedName.contains("tôm") || normalizedName.contains("hai san")) {
            hints.append(" tôm chiên nướng ");
        }
        if (normalizedName.contains("lau") || normalizedName.contains("lẩu") || normalizedName.contains("la giang")) {
            hints.append(" lẩu lá giang chua cay ");
        }
        if (normalizedName.contains("goi") || normalizedName.contains("gỏi") || normalizedName.contains("salad")) {
            hints.append(" gỏi ngó sen xoài ");
        }
        if (normalizedName.contains("7up") || normalizedName.contains("coca")
                || normalizedName.contains("pepsi") || normalizedName.contains("sting")
                || normalizedName.contains("tra") || normalizedName.contains("nuoc")
                || normalizedName.contains("drink") || normalizedName.contains("soda")) {
            hints.append(" nước ngọt giải khát coca 7up pepsi sting ");
        }

        // Phân tích đặc trưng màu sắc thực tế trên điểm ảnh Bitmap (kích thước nhỏ 48x48)
        if (imagePath != null && !imagePath.trim().isEmpty()) {
            Bitmap sample = decodeFromPathOrUri(context, imagePath, 48);
            if (sample != null) {
                try {
                    hints.append(' ').append(extractHsvVisualCues(sample));
                } finally {
                    sample.recycle();
                }
            }
        }

        String result = hints.toString().trim();
        return result.isEmpty() ? "món nướng chiên giòn" : result;
    }

    private static String extractHsvVisualCues(Bitmap bitmap) {
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        int total = width * height;
        if (total <= 0) {
            return "";
        }

        int goldenCrispyCount = 0;
        int spicyRedCount = 0;
        int freshGreenCount = 0;
        int brightPaleCount = 0;
        float[] hsv = new float[3];

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                Color.colorToHSV(bitmap.getPixel(x, y), hsv);
                float hue = hsv[0];
                float sat = hsv[1];
                float val = hsv[2];

                if (sat >= 0.22f && val >= 0.28f && hue >= 16f && hue <= 52f) {
                    goldenCrispyCount++;
                } else if (sat >= 0.32f && val >= 0.25f && (hue < 16f || hue > 335f)) {
                    spicyRedCount++;
                } else if (sat >= 0.20f && val >= 0.20f && hue >= 65f && hue <= 165f) {
                    freshGreenCount++;
                } else if (sat < 0.16f && val >= 0.80f) {
                    brightPaleCount++;
                }
            }
        }

        StringBuilder cues = new StringBuilder();
        double goldenRatio = (double) goldenCrispyCount / total;
        double redRatio = (double) spicyRedCount / total;
        double greenRatio = (double) freshGreenCount / total;
        double brightRatio = (double) brightPaleCount / total;

        if (goldenRatio >= 0.18) {
            cues.append("món chiên giòn nướng vàng ");
        }
        if (redRatio >= 0.14) {
            cues.append("chua cay sốt thái lẩu nướng ");
        }
        if (greenRatio >= 0.14) {
            cues.append("gỏi thanh mát rau ");
        }
        if (brightRatio >= 0.45 && goldenRatio < 0.12 && redRatio < 0.12) {
            cues.append("nước giải khát ");
        }

        if (cues.length() == 0) {
            if (goldenRatio >= redRatio && goldenRatio >= greenRatio) {
                cues.append("món nướng chiên ");
            } else if (redRatio >= greenRatio) {
                cues.append("món chua cay lẩu ");
            } else {
                cues.append("gỏi sả tắc ");
            }
        }
        return cues.toString().trim();
    }

    @Nullable
    private static String saveImageToSubdir(Context context, @Nullable Uri sourceUri, String subDir, String prefix) {
        if (context == null || sourceUri == null) {
            return null;
        }

        Bitmap decoded = decodeUriScaled(context, sourceUri, MAX_SAVED_DIMENSION_PX);
        if (decoded == null) {
            return null;
        }

        Bitmap oriented = applyExifRotationIfNeeded(context, sourceUri, decoded);
        Bitmap resized = scaleDownIfNeeded(oriented, MAX_SAVED_DIMENSION_PX);

        File dir = new File(context.getFilesDir(), subDir);
        if (!dir.exists() && !dir.mkdirs()) {
            if (resized != oriented) resized.recycle();
            if (oriented != decoded) oriented.recycle();
            decoded.recycle();
            return null;
        }

        String fileName = prefix + System.currentTimeMillis() + "_"
                + UUID.randomUUID().toString().substring(0, 6) + ".jpg";
        File outFile = new File(dir, fileName);

        try (FileOutputStream fos = new FileOutputStream(outFile)) {
            boolean compressed = resized.compress(Bitmap.CompressFormat.JPEG, JPEG_QUALITY, fos);
            fos.flush();
            if (!compressed) {
                return null;
            }
            BITMAP_CACHE.put(outFile.getAbsolutePath(), resized);
            return outFile.getAbsolutePath();
        } catch (IOException e) {
            return null;
        } finally {
            if (oriented != decoded && oriented != resized) {
                oriented.recycle();
            }
            if (decoded != resized) {
                decoded.recycle();
            }
        }
    }

    @Nullable
    private static Bitmap decodeFromPathOrUri(Context context, String pathOrUri, int maxDimPx) {
        File localFile = new File(pathOrUri);
        if (localFile.exists() && localFile.isFile()) {
            BitmapFactory.Options bounds = new BitmapFactory.Options();
            bounds.inJustDecodeBounds = true;
            BitmapFactory.decodeFile(localFile.getAbsolutePath(), bounds);
            if (bounds.outWidth <= 0 || bounds.outHeight <= 0) {
                return null;
            }
            BitmapFactory.Options opts = new BitmapFactory.Options();
            opts.inSampleSize = calculateInSampleSize(bounds.outWidth, bounds.outHeight, maxDimPx);
            return BitmapFactory.decodeFile(localFile.getAbsolutePath(), opts);
        }

        if (pathOrUri.startsWith("content://") || pathOrUri.startsWith("file://")) {
            return decodeUriScaled(context, Uri.parse(pathOrUri), maxDimPx);
        }
        return null;
    }

    @Nullable
    private static Bitmap decodeUriScaled(Context context, Uri uri, int maxDimPx) {
        ContentResolver resolver = context.getContentResolver();
        BitmapFactory.Options bounds = new BitmapFactory.Options();
        bounds.inJustDecodeBounds = true;
        try (InputStream boundsStream = resolver.openInputStream(uri)) {
            if (boundsStream == null) {
                return null;
            }
            BitmapFactory.decodeStream(boundsStream, null, bounds);
        } catch (IOException | SecurityException e) {
            return null;
        }

        if (bounds.outWidth <= 0 || bounds.outHeight <= 0) {
            return null;
        }

        BitmapFactory.Options decodeOpts = new BitmapFactory.Options();
        decodeOpts.inSampleSize = calculateInSampleSize(bounds.outWidth, bounds.outHeight, maxDimPx);
        try (InputStream dataStream = resolver.openInputStream(uri)) {
            if (dataStream == null) {
                return null;
            }
            return BitmapFactory.decodeStream(dataStream, null, decodeOpts);
        } catch (IOException | SecurityException e) {
            return null;
        }
    }

    private static Bitmap applyExifRotationIfNeeded(Context context, Uri sourceUri, Bitmap bitmap) {
        int rotationDegrees = 0;
        try (InputStream exifStream = context.getContentResolver().openInputStream(sourceUri)) {
            if (exifStream != null) {
                ExifInterface exif = new ExifInterface(exifStream);
                int orientation = exif.getAttributeInt(
                        ExifInterface.TAG_ORIENTATION,
                        ExifInterface.ORIENTATION_NORMAL
                );
                if (orientation == ExifInterface.ORIENTATION_ROTATE_90) {
                    rotationDegrees = 90;
                } else if (orientation == ExifInterface.ORIENTATION_ROTATE_180) {
                    rotationDegrees = 180;
                } else if (orientation == ExifInterface.ORIENTATION_ROTATE_270) {
                    rotationDegrees = 270;
                }
            }
        } catch (Exception ignored) {
            return bitmap;
        }

        if (rotationDegrees == 0) {
            return bitmap;
        }

        Matrix matrix = new Matrix();
        matrix.postRotate(rotationDegrees);
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
    }

    private static Bitmap scaleDownIfNeeded(Bitmap bitmap, int maxDimPx) {
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        if (width <= maxDimPx && height <= maxDimPx) {
            return bitmap;
        }
        float ratio = Math.min((float) maxDimPx / width, (float) maxDimPx / height);
        int targetW = Math.max(1, Math.round(width * ratio));
        int targetH = Math.max(1, Math.round(height * ratio));
        return Bitmap.createScaledBitmap(bitmap, targetW, targetH, true);
    }

    private static int calculateInSampleSize(int width, int height, int maxDimPx) {
        int inSampleSize = 1;
        while ((width / inSampleSize) > maxDimPx * 2 || (height / inSampleSize) > maxDimPx * 2) {
            inSampleSize *= 2;
        }
        return inSampleSize;
    }
}
