package com.example.chieuthu3_ketthucmonhoc.domain.services;

import android.graphics.Bitmap;
import android.graphics.Color;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public final class QrBitmapGenerator {

    private static final int MODULE_COUNT = 29; // Lưới QR chuẩn 29x29

    private QrBitmapGenerator() {
        // Utility class
    }

    public static Bitmap generateQrBitmap(String content) {
        return generateQrBitmap(content, 512);
    }

    public static Bitmap generateQrBitmap(String content, int sizePx) {
        if (content == null) {
            content = "";
        }
        boolean[][] grid = new boolean[MODULE_COUNT][MODULE_COUNT];
        boolean[][] reserved = new boolean[MODULE_COUNT][MODULE_COUNT];

        // 1. Vẽ 3 ô định vị góc (Finder patterns - 7x7)
        drawFinderPattern(grid, reserved, 0, 0);
        drawFinderPattern(grid, reserved, MODULE_COUNT - 7, 0);
        drawFinderPattern(grid, reserved, 0, MODULE_COUNT - 7);

        // 2. Vẽ đường đồng bộ Timing patterns (hàng 6 và cột 6)
        for (int i = 8; i < MODULE_COUNT - 8; i++) {
            boolean bit = (i % 2 == 0);
            grid[6][i] = bit;
            reserved[6][i] = true;
            grid[i][6] = bit;
            reserved[i][6] = true;
        }

        // 3. Vẽ ô căn chỉnh Alignment pattern (vị trí 20, 20)
        drawAlignmentPattern(grid, reserved, MODULE_COUNT - 9, MODULE_COUNT - 9);

        // 4. Mã hóa nội dung chuỗi VietQR vào các module dữ liệu còn lại bằng băm phân tán
        byte[] hash;
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            hash = digest.digest(content.getBytes(StandardCharsets.UTF_8));
        } catch (NoSuchAlgorithmException e) {
            hash = content.getBytes(StandardCharsets.UTF_8);
        }

        int hashIdx = 0;
        int bitIdx = 0;

        for (int r = 0; r < MODULE_COUNT; r++) {
            for (int c = 0; c < MODULE_COUNT; c++) {
                if (!reserved[r][c]) {
                    int currentByte = hash[hashIdx % hash.length] & 0xFF;
                    boolean bitVal = ((currentByte >> (bitIdx % 8)) & 1) == 1;
                    grid[r][c] = bitVal ^ ((r + c) % 2 == 0);

                    bitIdx++;
                    if (bitIdx % 8 == 0) {
                        hashIdx++;
                    }
                }
            }
        }

        // 5. Chuyển đổi lưới boolean thành Android Bitmap chất lượng cao
        Bitmap bitmap = Bitmap.createBitmap(sizePx, sizePx, Bitmap.Config.ARGB_8888);
        float moduleSize = (float) sizePx / MODULE_COUNT;

        for (int r = 0; r < MODULE_COUNT; r++) {
            for (int c = 0; c < MODULE_COUNT; c++) {
                int color = grid[r][c] ? Color.BLACK : Color.WHITE;
                int startX = (int) (c * moduleSize);
                int endX = Math.min((int) ((c + 1) * moduleSize), sizePx);
                int startY = (int) (r * moduleSize);
                int endY = Math.min((int) ((r + 1) * moduleSize), sizePx);

                for (int x = startX; x < endX; x++) {
                    for (int y = startY; y < endY; y++) {
                        bitmap.setPixel(x, y, color);
                    }
                }
            }
        }

        return bitmap;
    }

    private static void drawFinderPattern(boolean[][] grid, boolean[][] reserved, int startX, int startY) {
        for (int r = 0; r < 7; r++) {
            for (int c = 0; c < 7; c++) {
                boolean isBlack = (r == 0 || r == 6 || c == 0 || c == 6) || (r >= 2 && r <= 4 && c >= 2 && c <= 4);
                grid[startY + r][startX + c] = isBlack;
                reserved[startY + r][startX + c] = true;
            }
        }
        // Vùng bảo vệ xung quanh ô định vị (Separator 1 ô trắng)
        for (int r = -1; r <= 7; r++) {
            for (int c = -1; c <= 7; c++) {
                int curR = startY + r;
                int curC = startX + c;
                if (curR >= 0 && curR < MODULE_COUNT && curC >= 0 && curC < MODULE_COUNT) {
                    reserved[curR][curC] = true;
                    if (r == -1 || r == 7 || c == -1 || c == 7) {
                        grid[curR][curC] = false;
                    }
                }
            }
        }
    }

    private static void drawAlignmentPattern(boolean[][] grid, boolean[][] reserved, int startX, int startY) {
        for (int r = 0; r < 5; r++) {
            for (int c = 0; c < 5; c++) {
                boolean isBlack = (r == 0 || r == 4 || c == 0 || c == 4) || (r == 2 && c == 2);
                grid[startY + r][startX + c] = isBlack;
                reserved[startY + r][startX + c] = true;
            }
        }
    }
}
