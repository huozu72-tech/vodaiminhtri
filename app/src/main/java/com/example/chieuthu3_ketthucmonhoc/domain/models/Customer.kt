package com.example.chieuthu3_ketthucmonhoc.domain.models

data class Customer(
    val id: Int,
    val fullName: String,
    val email: String,
    val phoneNumber: String,
    val passwordHash: String,
    val address: String? = null,
    val createdAt: Long = System.currentTimeMillis()
)
