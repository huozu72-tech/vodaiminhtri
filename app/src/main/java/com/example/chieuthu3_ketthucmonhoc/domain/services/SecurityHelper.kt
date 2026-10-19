package com.example.chieuthu3_ketthucmonhoc.domain.services

import java.security.MessageDigest

object SecurityHelper {
    fun hashPassword(password: String): String {
        val digest = MessageDigest.getInstance("SHA-256")
        val hashBytes = digest.digest(password.toByteArray(Charsets.UTF_8))
        return hashBytes.joinToString("") { "%02x".format(it) }
    }

    fun verifyPassword(password: String, expectedHash: String): Boolean {
        return hashPassword(password).equals(expectedHash, ignoreCase = true)
    }
}
