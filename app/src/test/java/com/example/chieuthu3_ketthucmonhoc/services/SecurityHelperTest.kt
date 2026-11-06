package com.example.chieuthu3_ketthucmonhoc.services

import com.example.chieuthu3_ketthucmonhoc.domain.services.SecurityHelper
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Contract test cho SecurityHelper (trước đây chưa có lớp test nào).
 *
 * Chỉ khóa các thuộc tính ổn định của hợp đồng băm/so mật khẩu:
 * round-trip verify, sai mật khẩu phải bị từ chối, hash không lộ plaintext,
 * hai mật khẩu khác nhau phải ra hash khác nhau.
 *
 * Cố tình KHÔNG khóa chi tiết triển khai (độ dài hex, tính tất định của cùng
 * một input, vector SHA-256 cụ thể) để không cản việc nâng cấp thuật toán
 * (ví dụ sang PBKDF2 có salt) mà vẫn giữ hợp đồng này đúng.
 */
class SecurityHelperTest {

    private val samplePasswords = listOf(
        "123456",
        "admin123",
        "P@ssw0rd!",
        "mật khẩu tiếng Việt"
    )

    @Test
    fun `verifyPassword accepts the password that was hashed`() {
        samplePasswords.forEach { password ->
            val hash = SecurityHelper.hashPassword(password)
            assertTrue(
                "verifyPassword phải chấp nhận đúng mật khẩu đã băm: $password",
                SecurityHelper.verifyPassword(password, hash)
            )
        }
    }

    @Test
    fun `verifyPassword rejects a wrong or empty password`() {
        val hash = SecurityHelper.hashPassword("matkhau123")
        assertFalse(SecurityHelper.verifyPassword("matkhau124", hash))
        assertFalse(SecurityHelper.verifyPassword("", hash))
    }

    @Test
    fun `hashPassword never returns the plaintext and is never blank`() {
        samplePasswords.forEach { password ->
            val hash = SecurityHelper.hashPassword(password)
            assertNotEquals("Hash không được trả về plaintext", password, hash)
            assertFalse("Hash không được rỗng", hash.isBlank())
        }
    }

    @Test
    fun `hashPassword produces different hashes for different passwords`() {
        val hashAbc123 = SecurityHelper.hashPassword("abc123")
        val hashAbc124 = SecurityHelper.hashPassword("abc124")
        assertNotEquals(hashAbc123, hashAbc124)
    }
}
