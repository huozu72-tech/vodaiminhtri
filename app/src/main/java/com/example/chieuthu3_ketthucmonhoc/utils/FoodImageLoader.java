package com.example.chieuthu3_ketthucmonhoc.utils;

import android.content.ContentResolver;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.Uri;
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
import java.util.UUID;

/**
 * Tiện ích lưu trữ và nạp ảnh món ăn từ bộ nhớ thiết bị (chọn từ thư viện/máy ảnh),
 * tự động thu gọn kích thước, chỉnh hướng EXIF và fallback về drawable mặc định của nhà hàng.
 */
public final class FoodImageLoader {

    private static final String FOOD_IMAGE_DIR = "food_images";
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
        if (context == null || sourceUri == null) {
            return null;
        }

        Bitmap decoded = decodeUriScaled(context, sourceUri, MAX_SAVED_DIMENSION_PX);
        if (decoded == null) {
            return null;
        }

        Bitmap oriented = applyExifRotationIfNeeded(context, sourceUri, decoded);
        Bitmap resized = scaleDownIfNeeded(oriented, MAX_SAVED_DIMENSION_PX);

        File dir = new File(context.getFilesDir(), FOOD_IMAGE_DIR);
        if (!dir.exists() && !dir.mkdirs()) {
            if (resized != oriented) resized.recycle();
            if (oriented != decoded) oriented.recycle();
            decoded.recycle();
            return null;
        }

        String fileName = "food_" + System.currentTimeMillis() + "_"
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
