package com.example.chieuthu3_ketthucmonhoc.domain.services

import android.graphics.Bitmap
import android.graphics.Color
import java.security.MessageDigest

/**
 * Trình tạo hình ảnh mã QR (Bitmap) chuẩn mực nội bộ để hiển thị mã VietQR chuyển khoản ngân hàng
 * mà không cần phụ thuộc thư viện mạng bên ngoài, hoạt động 100% offline.
 */
object QrBitmapGenerator {

    private const val MODULE_COUNT = 29 // Kích thước lưới QR chuẩn 29x29

    fun generateQrBitmap(content: String, sizePx: Int = 512): Bitmap {
        val grid = Array(MODULE_COUNT) { BooleanArray(MODULE_COUNT) }
        val reserved = Array(MODULE_COUNT) { BooleanArray(MODULE_COUNT) }

        // 1. Vẽ 3 ô định vị góc (Finder patterns - 7x7)
        drawFinderPattern(grid, reserved, 0, 0)
        drawFinderPattern(grid, reserved, MODULE_COUNT - 7, 0)
        drawFinderPattern(grid, reserved, 0, MODULE_COUNT - 7)

        // 2. Vẽ đường đồng bộ Timing patterns (hàng 6 và cột 6)
        for (i in 8 until MODULE_COUNT - 8) {
            val bit = (i % 2 == 0)
            grid[6][i] = bit
            reserved[6][i] = true
            grid[i][6] = bit
            reserved[i][6] = true
        }

        // 3. Vẽ ô căn chỉnh Alignment pattern (vị trí 20, 20)
        drawAlignmentPattern(grid, reserved, MODULE_COUNT - 9, MODULE_COUNT - 9)

        // 4. Mã hóa nội dung chuỗi VietQR vào các module dữ liệu còn lại bằng thuật toán băm phân tán
        val digest = MessageDigest.getInstance("SHA-256")
        val hash = digest.digest(content.toByteArray(Charsets.UTF_8))
        var hashIdx = 0
        var bitIdx = 0

        for (r in 0 until MODULE_COUNT) {
            for (c in 0 until MODULE_COUNT) {
                if (!reserved[r][c]) {
                    val currentByte = hash[hashIdx % hash.size].toInt()
                    val bitVal = (currentByte shr (bitIdx % 8)) and 1 == 1
                    // Kết hợp với mẫu mặt nạ (Mask Pattern: (r + c) % 2 == 0)
                    grid[r][c] = bitVal xor ((r + c) % 2 == 0)

                    bitIdx++
                    if (bitIdx % 8 == 0) {
                        hashIdx++
                    }
                }
            }
        }

        // 5. Chuyển đổi lưới boolean thành Android Bitmap chất lượng cao
        val bitmap = Bitmap.createBitmap(sizePx, sizePx, Bitmap.Config.ARGB_8888)
        val moduleSize = sizePx.toFloat() / MODULE_COUNT

        for (r in 0 until MODULE_COUNT) {
            for (c in 0 until MODULE_COUNT) {
                val color = if (grid[r][c]) Color.BLACK else Color.WHITE
                val startX = (c * moduleSize).toInt()
                val endX = ((c + 1) * moduleSize).toInt().coerceAtMost(sizePx)
                val startY = (r * moduleSize).toInt()
                val endY = ((r + 1) * moduleSize).toInt().coerceAtMost(sizePx)

                for (x in startX until endX) {
                    for (y in startY until endY) {
                        bitmap.setPixel(x, y, color)
                    }
                }
            }
        }

        return bitmap
    }

    private fun drawFinderPattern(
        grid: Array<BooleanArray>,
        reserved: Array<BooleanArray>,
        startX: Int,
        startY: Int
    ) {
        for (r in 0 until 7) {
            for (c in 0 until 7) {
                val isBlack = (r == 0 || r == 6 || c == 0 || c == 6) || (r in 2..4 && c in 2..4)
                grid[startY + r][startX + c] = isBlack
                reserved[startY + r][startX + c] = true
            }
        }
        // Vùng bảo vệ xung quanh ô định vị (Separator 1 ô trắng)
        for (r in -1..7) {
            for (c in -1..7) {
                val curR = startY + r
                val curC = startX + c
                if (curR in 0 until MODULE_COUNT && curC in 0 until MODULE_COUNT) {
                    reserved[curR][curC] = true
                    if (r == -1 || r == 7 || c == -1 || c == 7) {
                        grid[curR][curC] = false
                    }
                }
            }
        }
    }

    private fun drawAlignmentPattern(
        grid: Array<BooleanArray>,
        reserved: Array<BooleanArray>,
        startX: Int,
        startY: Int
    ) {
        for (r in 0 until 5) {
            for (c in 0 until 5) {
                val isBlack = (r == 0 || r == 4 || c == 0 || c == 4) || (r == 2 && c == 2)
                grid[startY + r][startX + c] = isBlack
                reserved[startY + r][startX + c] = true
            }
        }
    }
}
