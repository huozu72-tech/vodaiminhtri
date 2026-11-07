package com.example.chieuthu3_ketthucmonhoc.services;

import com.example.chieuthu3_ketthucmonhoc.domain.services.SecurityHelper;

import org.junit.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;

/**
 * Contract test cho SecurityHelper.
 */
public class SecurityHelperTest {

    private final List<String> samplePasswords = Arrays.asList(
            "123456",
            "admin123",
            "P@ssw0rd!",
            "mật khẩu tiếng Việt"
    );

    @Test
    public void verifyPassword_acceptsThePasswordThatWasHashed() {
        for (String password : samplePasswords) {
            String hash = SecurityHelper.hashPassword(password);
            assertTrue(
                    "verifyPassword phải chấp nhận đúng mật khẩu đã băm: " + password,
                    SecurityHelper.verifyPassword(password, hash)
            );
        }
    }

    @Test
    public void verifyPassword_rejectsAWrongOrEmptyPassword() {
        String hash = SecurityHelper.hashPassword("matkhau123");
        assertFalse(SecurityHelper.verifyPassword("matkhau124", hash));
        assertFalse(SecurityHelper.verifyPassword("", hash));
    }

    @Test
    public void hashPassword_neverReturnsThePlaintextAndIsNeverBlank() {
        for (String password : samplePasswords) {
            String hash = SecurityHelper.hashPassword(password);
            assertNotEquals("Hash không được trả về plaintext", password, hash);
            assertFalse("Hash không được rỗng", hash == null || hash.trim().isEmpty());
        }
    }

    @Test
    public void hashPassword_producesDifferentHashesForDifferentPasswords() {
        String hashAbc123 = SecurityHelper.hashPassword("abc123");
        String hashAbc124 = SecurityHelper.hashPassword("abc124");
        assertNotEquals(hashAbc123, hashAbc124);
    }
}
