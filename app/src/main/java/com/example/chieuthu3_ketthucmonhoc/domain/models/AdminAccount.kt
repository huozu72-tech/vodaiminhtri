package com.example.chieuthu3_ketthucmonhoc.domain.models

data class AdminAccount(
    val id: Int,
    val username: String,
    val passwordHash: String,
    val fullName: String,
    val role: UserRole = UserRole.MANAGER,
    val createdAt: Long = System.currentTimeMillis()
)
