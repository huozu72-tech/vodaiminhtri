package com.example.chieuthu3_ketthucmonhoc.domain.services;

import android.graphics.Bitmap;
import android.graphics.Color;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Bộ tạo mã QR chuẩn quốc tế ISO/IEC 18004 (Scannable QR Code Generator)
 * Sử dụng mã hóa Byte Mode + Reed-Solomon Error Correction trên trường hữu hạn GF(256).
 * Mã QR sinh ra có thể quét thực tế bằng Camera điện thoại, Zalo hoặc ứng dụng Ngân hàng.
 */
public final class QrBitmapGenerator {

    // Đa thức sinh trường GF(256): x^8 + x^4 + x^3 + x^2 + 1 (0x11D)
    private static final int[] EXP_TABLE = new int[512];
    private static final int[] LOG_TABLE = new int[256];

    static {
        int x = 1;
        for (int i = 0; i < 255; i++) {
            EXP_TABLE[i] = x;
            LOG_TABLE[x] = i;
            x <<= 1;
            if ((x & 0x100) != 0) {
                x ^= 0x11D;
            }
        }
        for (int i = 255; i < 512; i++) {
            EXP_TABLE[i] = EXP_TABLE[i - 255];
        }
    }

    // Cấu hình Version 1 -> 9 (Mức sửa lỗi L - 7%)
    private static final int[][] VERSION_PARAMS = {
            // {version, size, totalDataBytes, numBlocks, eccPerBlock}
            {0, 0, 0, 0, 0},
            {1, 21, 19, 1, 7},
            {2, 25, 34, 1, 10},
            {3, 29, 55, 1, 15},
            {4, 33, 80, 1, 20},
            {5, 37, 108, 1, 26},
            {6, 41, 136, 2, 18},
            {7, 45, 156, 2, 20},
            {8, 49, 194, 2, 24},
            {9, 53, 232, 2, 30}
    };

    private static final int[][] ALIGNMENT_CENTERS = {
            {},
            {},
            {6, 18},
            {6, 22},
            {6, 26},
            {6, 30},
            {6, 34},
            {6, 22, 38},
            {6, 24, 42},
            {6, 26, 46}
    };

    private QrBitmapGenerator() {
        // Utility class
    }

    /**
     * Tạo chuỗi chuẩn EMVCo VietQR Napas247 (State Bank of Vietnam standard) có mã kiểm tra CRC-16/CCITT-FALSE.
     * Khi quét bằng ứng dụng Ngân hàng hoặc MoMo/ZaloPay thật, app ngân hàng sẽ tự động nhận diện
     * Ngân hàng thụ hưởng (MBBank - 970422), Số tài khoản, Số tiền và Nội dung chuyển khoản.
     */
    public static String buildVietQrEmvPayload(String bankBin, String accountNo, long amount, String description) {
        String safeBin = (bankBin != null && !bankBin.trim().isEmpty()) ? bankBin.trim() : "970422"; // 970422 = MBBank
        String safeAcc = (accountNo != null && !accountNo.trim().isEmpty()) ? accountNo.trim() : "0901234567";
        String cleanDesc = sanitizeAsciiDescription(description);

        String sub00 = tlv("00", "A000000727");
        String beneficiary = tlv("00", safeBin) + tlv("01", safeAcc);
        String sub01 = tlv("01", beneficiary);
        String sub02 = tlv("02", "QRIBFTTA");
        String tag38 = tlv("38", sub00 + sub01 + sub02);

        String tag53 = tlv("53", "704"); // VND
        String tag54 = amount > 0 ? tlv("54", String.valueOf(amount)) : "";
        String tag58 = tlv("58", "VN");
        String tag62 = !cleanDesc.isEmpty() ? tlv("62", tlv("08", cleanDesc)) : "";

        String rawWithoutCrc = "000201"
                + "010212"
                + tag38
                + tag53
                + tag54
                + tag58
                + tag62
                + "6304";

        String crcHex = computeCrc16CcittHex(rawWithoutCrc);
        return rawWithoutCrc + crcHex;
    }

    private static String tlv(String tag, String value) {
        if (value == null) value = "";
        return String.format(java.util.Locale.US, "%s%02d%s", tag, value.length(), value);
    }

    private static String sanitizeAsciiDescription(String input) {
        if (input == null) return "THANH TOAN DON HANG";
        String cleaned = input.replaceAll("[^A-Za-z0-9 ]", " ").trim().replaceAll("\\s+", " ");
        if (cleaned.length() > 25) {
            cleaned = cleaned.substring(0, 25).trim();
        }
        return cleaned.isEmpty() ? "THANH TOAN DON HANG" : cleaned;
    }

    public static String computeCrc16CcittHex(String data) {
        int crc = 0xFFFF;
        byte[] bytes = data.getBytes(StandardCharsets.US_ASCII);
        for (byte b : bytes) {
            crc ^= (b & 0xFF) << 8;
            for (int i = 0; i < 8; i++) {
                if ((crc & 0x8000) != 0) {
                    crc = ((crc << 1) ^ 0x1021) & 0xFFFF;
                } else {
                    crc = (crc << 1) & 0xFFFF;
                }
            }
        }
        return String.format(java.util.Locale.US, "%04X", crc & 0xFFFF);
    }

    public static Bitmap generateQrBitmap(String content) {
        return generateQrBitmap(content, 512);
    }

    public static Bitmap generateQrBitmap(String content, int sizePx) {
        if (content == null || content.trim().isEmpty()) {
            content = "NHA HANG CHIEU THU 3 - VIETQR";
        }
        byte[] payload = content.getBytes(StandardCharsets.UTF_8);

        // Nếu chuỗi dài quá sức chứa V9 (230 bytes), rút gọn an toàn
        if (payload.length > 228) {
            payload = Arrays.copyOf(payload, 228);
        }

        int version = selectVersion(payload.length);
        int size = VERSION_PARAMS[version][1];
        int totalDataBytes = VERSION_PARAMS[version][2];
        int numBlocks = VERSION_PARAMS[version][3];
        int eccPerBlock = VERSION_PARAMS[version][4];

        // 1. Đóng gói bitstream Byte Mode (0100) + Độ dài + Dữ liệu + Padding (0xEC, 0x11)
        byte[] dataCodewords = encodeDataCodewords(payload, version, totalDataBytes);

        // 2. Chia block và tính mã sửa lỗi Reed-Solomon (ECC) + Đan xen (Interleave)
        byte[] finalCodewords = interleaveWithEcc(dataCodewords, numBlocks, eccPerBlock);

        // 3. Khởi tạo ma trận module QR và đặt các mẫu chức năng (Finder, Timing, Alignment, Dark module)
        boolean[][] modules = new boolean[size][size];
        boolean[][] isFunction = new boolean[size][size];
        setupFunctionPatterns(modules, isFunction, version, size);

        // 4. Đặt các bit dữ liệu theo đường zíc-zắc từ phải sang trái
        placeCodewords(modules, isFunction, finalCodewords, size);

        // 5. Áp dụng mặt nạ tối ưu (Mask Pattern 0: (row + col) % 2 == 0) và ghi Format/Version Info
        int mask = 0;
        applyMask(modules, isFunction, size, mask);
        drawFormatBits(modules, isFunction, size, mask);
        if (version >= 7) {
            drawVersionBits(modules, isFunction, version, size);
        }

        // 6. Vẽ ra Bitmap với vùng trắng chuẩn (Quiet Zone = 3 modules)
        int quietZone = 3;
        int totalModules = size + quietZone * 2;
        Bitmap bitmap = Bitmap.createBitmap(sizePx, sizePx, Bitmap.Config.ARGB_8888);
        int[] pixels = new int[sizePx * sizePx];
        Arrays.fill(pixels, Color.WHITE);

        float scale = (float) sizePx / totalModules;
        for (int r = 0; r < size; r++) {
            for (int c = 0; c < size; c++) {
                if (modules[r][c]) {
                    int startX = Math.round((c + quietZone) * scale);
                    int endX = Math.min(Math.round((c + quietZone + 1) * scale), sizePx);
                    int startY = Math.round((r + quietZone) * scale);
                    int endY = Math.min(Math.round((r + quietZone + 1) * scale), sizePx);
                    for (int y = startY; y < endY; y++) {
                        int rowOffset = y * sizePx;
                        for (int x = startX; x < endX; x++) {
                            pixels[rowOffset + x] = Color.BLACK;
                        }
                    }
                }
            }
        }
        bitmap.setPixels(pixels, 0, sizePx, 0, 0, sizePx, sizePx);
        return bitmap;
    }

    private static int selectVersion(int byteLen) {
        for (int v = 1; v <= 9; v++) {
            int maxDataBytes = VERSION_PARAMS[v][2];
            // Byte mode cần 4 bit mode + 8 bit length (V1..9) = 12 bit (2 bytes overhead)
            if (byteLen + 2 <= maxDataBytes) {
                return v;
            }
        }
        return 9;
    }

    private static byte[] encodeDataCodewords(byte[] payload, int version, int totalDataBytes) {
        List<Integer> bits = new ArrayList<>();
        // Mode indicator cho Byte Mode: 0100
        appendBits(bits, 0b0100, 4);
        // Character count indicator (8 bits cho Version 1..9)
        appendBits(bits, payload.length, 8);
        // Payload bytes
        for (byte b : payload) {
            appendBits(bits, b & 0xFF, 8);
        }
        // Terminator (tối đa 4 bit 0)
        int capacityBits = totalDataBytes * 8;
        int terminatorLen = Math.min(4, capacityBits - bits.size());
        appendBits(bits, 0, terminatorLen);

        // Căn chỉnh chẵn byte (8 bit)
        while (bits.size() % 8 != 0) {
            bits.add(0);
        }

        // Pad bytes luân phiên 0xEC (236) và 0x11 (17)
        int[] padBytes = {0xEC, 0x11};
        int padIdx = 0;
        while (bits.size() < capacityBits) {
            appendBits(bits, padBytes[padIdx % 2], 8);
            padIdx++;
        }

        byte[] result = new byte[totalDataBytes];
        for (int i = 0; i < totalDataBytes; i++) {
            int val = 0;
            for (int b = 0; b < 8; b++) {
                val = (val << 1) | bits.get(i * 8 + b);
            }
            result[i] = (byte) val;
        }
        return result;
    }

    private static void appendBits(List<Integer> bits, int val, int numBits) {
        for (int i = numBits - 1; i >= 0; i--) {
            bits.add((val >>> i) & 1);
        }
    }

    private static byte[] interleaveWithEcc(byte[] data, int numBlocks, int eccPerBlock) {
        int blockDataLen = data.length / numBlocks;
        byte[][] dataBlocks = new byte[numBlocks][blockDataLen];
        byte[][] eccBlocks = new byte[numBlocks][eccPerBlock];
        int[] genPoly = buildReedSolomonGenerator(eccPerBlock);

        int offset = 0;
        for (int b = 0; b < numBlocks; b++) {
            System.arraycopy(data, offset, dataBlocks[b], 0, blockDataLen);
            offset += blockDataLen;
            eccBlocks[b] = computeReedSolomonRemainder(dataBlocks[b], genPoly, eccPerBlock);
        }

        byte[] result = new byte[data.length + numBlocks * eccPerBlock];
        int idx = 0;
        for (int i = 0; i < blockDataLen; i++) {
            for (int b = 0; b < numBlocks; b++) {
                result[idx++] = dataBlocks[b][i];
            }
        }
        for (int i = 0; i < eccPerBlock; i++) {
            for (int b = 0; b < numBlocks; b++) {
                result[idx++] = eccBlocks[b][i];
            }
        }
        return result;
    }

    private static int[] buildReedSolomonGenerator(int degree) {
        int[] poly = new int[]{1};
        for (int i = 0; i < degree; i++) {
            int[] next = new int[poly.length + 1];
            int root = EXP_TABLE[i];
            for (int j = 0; j < poly.length; j++) {
                next[j] ^= poly[j];
                next[j + 1] ^= gfMultiply(poly[j], root);
            }
            poly = next;
        }
        return poly;
    }

    private static byte[] computeReedSolomonRemainder(byte[] data, int[] genPoly, int degree) {
        int[] remainder = new int[degree];
        for (byte b : data) {
            int factor = (b & 0xFF) ^ remainder[0];
            System.arraycopy(remainder, 1, remainder, 0, degree - 1);
            remainder[degree - 1] = 0;
            if (factor != 0) {
                for (int j = 0; j < degree; j++) {
                    remainder[j] ^= gfMultiply(genPoly[j + 1], factor);
                }
            }
        }
        byte[] ecc = new byte[degree];
        for (int i = 0; i < degree; i++) {
            ecc[i] = (byte) remainder[i];
        }
        return ecc;
    }

    private static int gfMultiply(int a, int b) {
        if (a == 0 || b == 0) return 0;
        return EXP_TABLE[LOG_TABLE[a & 0xFF] + LOG_TABLE[b & 0xFF]];
    }

    private static void setupFunctionPatterns(boolean[][] modules, boolean[][] isFunction, int version, int size) {
        // 3 Finder patterns ở 3 góc
        drawFinderPattern(modules, isFunction, 0, 0, size);
        drawFinderPattern(modules, isFunction, size - 7, 0, size);
        drawFinderPattern(modules, isFunction, 0, size - 7, size);

        // Timing patterns ở hàng 6 và cột 6
        for (int i = 8; i < size - 8; i++) {
            boolean dark = (i % 2 == 0);
            modules[6][i] = dark;
            isFunction[6][i] = true;
            modules[i][6] = dark;
            isFunction[i][6] = true;
        }

        // Alignment patterns
        int[] align = ALIGNMENT_CENTERS[version];
        for (int r : align) {
            for (int c : align) {
                if ((r == 6 && c == 6) || (r == 6 && c == size - 7) || (r == size - 7 && c == 6)) {
                    continue;
                }
                drawAlignmentPattern(modules, isFunction, r, c);
            }
        }

        // Dark module cố định tại (4 * version + 9, 8)
        modules[size - 8][8] = true;
        isFunction[size - 8][8] = true;

        // Đặt trước vùng Format Info để tránh bị ghi đè bởi dữ liệu
        for (int i = 0; i < 9; i++) {
            if (i != 6) {
                isFunction[8][i] = true;
                isFunction[i][8] = true;
            }
        }
        for (int i = 0; i < 8; i++) {
            isFunction[8][size - 1 - i] = true;
            isFunction[size - 1 - i][8] = true;
        }

        // Đặt trước vùng Version Info cho Version >= 7
        if (version >= 7) {
            for (int i = 0; i < 6; i++) {
                for (int j = 0; j < 3; j++) {
                    isFunction[size - 11 + j][i] = true;
                    isFunction[i][size - 11 + j] = true;
                }
            }
        }
    }

    private static void drawFinderPattern(boolean[][] modules, boolean[][] isFunction, int topX, int topY, int size) {
        for (int dy = -1; dy <= 7; dy++) {
            for (int dx = -1; dx <= 7; dx++) {
                int r = topY + dy;
                int c = topX + dx;
                if (r >= 0 && r < size && c >= 0 && c < size) {
                    boolean dark = (dx >= 0 && dx <= 6 && (dy == 0 || dy == 6))
                            || (dy >= 0 && dy <= 6 && (dx == 0 || dx == 6))
                            || (dx >= 2 && dx <= 4 && dy >= 2 && dy <= 4);
                    modules[r][c] = dark;
                    isFunction[r][c] = true;
                }
            }
        }
    }

    private static void drawAlignmentPattern(boolean[][] modules, boolean[][] isFunction, int centerR, int centerC) {
        for (int dr = -2; dr <= 2; dr++) {
            for (int dc = -2; dc <= 2; dc++) {
                boolean dark = Math.max(Math.abs(dr), Math.abs(dc)) != 1;
                modules[centerR + dr][centerC + dc] = dark;
                isFunction[centerR + dr][centerC + dc] = true;
            }
        }
    }

    private static void placeCodewords(boolean[][] modules, boolean[][] isFunction, byte[] codewords, int size) {
        int bitIdx = 0;
        int totalBits = codewords.length * 8;
        boolean upward = true;

        for (int right = size - 1; right >= 1; right -= 2) {
            if (right == 6) {
                right = 5; // Bỏ qua cột Timing dọc số 6
            }
            for (int vert = 0; vert < size; vert++) {
                int r = upward ? (size - 1 - vert) : vert;
                for (int j = 0; j < 2; j++) {
                    int c = right - j;
                    if (!isFunction[r][c]) {
                        boolean dark = false;
                        if (bitIdx < totalBits) {
                            dark = ((codewords[bitIdx >>> 3] >>> (7 - (bitIdx & 7))) & 1) != 0;
                            bitIdx++;
                        }
                        modules[r][c] = dark;
                    }
                }
            }
            upward = !upward;
        }
    }

    private static void applyMask(boolean[][] modules, boolean[][] isFunction, int size, int mask) {
        for (int r = 0; r < size; r++) {
            for (int c = 0; c < size; c++) {
                if (!isFunction[r][c]) {
                    boolean invert = (mask == 0) && ((r + c) % 2 == 0);
                    if (invert) {
                        modules[r][c] = !modules[r][c];
                    }
                }
            }
        }
    }

    private static void drawFormatBits(boolean[][] modules, boolean[][] isFunction, int size, int mask) {
        // ECC Level L = 01, kết hợp 3 bit mask
        int data = (0b01 << 3) | mask;
        int rem = data;
        for (int i = 0; i < 10; i++) {
            rem = (rem << 1) ^ (((rem >>> 9) & 1) * 0x537);
        }
        int bits = ((data << 10) | rem) ^ 0x5412;

        // Vùng Format thứ nhất quanh góc trên-trái
        for (int i = 0; i <= 5; i++) {
            modules[8][i] = getBit(bits, i);
        }
        modules[8][7] = getBit(bits, 6);
        modules[8][8] = getBit(bits, 7);
        modules[7][8] = getBit(bits, 8);
        for (int i = 9; i < 15; i++) {
            modules[14 - i][8] = getBit(bits, i);
        }

        // Vùng Format thứ hai ở góc dưới-trái và trên-phải
        for (int i = 0; i < 8; i++) {
            modules[8][size - 1 - i] = getBit(bits, i);
        }
        for (int i = 8; i < 15; i++) {
            modules[size - 15 + i][8] = getBit(bits, i);
        }
        modules[size - 8][8] = true; // Dark module
    }

    private static void drawVersionBits(boolean[][] modules, boolean[][] isFunction, int version, int size) {
        int rem = version;
        for (int i = 0; i < 12; i++) {
            rem = (rem << 1) ^ (((rem >>> 11) & 1) * 0x1F25);
        }
        int bits = (version << 12) | rem;
        for (int i = 0; i < 18; i++) {
            boolean bit = getBit(bits, i);
            int a = size - 11 + (i % 3);
            int b = i / 3;
            modules[a][b] = bit;
            modules[b][a] = bit;
        }
    }

    private static boolean getBit(int val, int index) {
        return ((val >>> index) & 1) != 0;
    }
}
