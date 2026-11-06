package com.example.chieuthu3_ketthucmonhoc.data.database.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.example.chieuthu3_ketthucmonhoc.domain.models.Customer

/**
 * Bảng khách hàng. Ràng buộc: SĐT UNIQUE NOT NULL; email UNIQUE (NULL khi chưa có —
 * SQLite cho phép nhiều NULL nên nhiều khách chưa nhập email vẫn hợp lệ).
 * Domain model dùng chuỗi rỗng cho "chưa có email"; mapper đổi "" <-> NULL.
 */
@Entity(
    tableName = "customers",
    indices = [
        Index(value = ["phone"], unique = true),
        Index(value = ["email"], unique = true)
    ]
)
data class CustomerEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val fullName: String,
    val email: String?,
    val phone: String,
    val passwordHash: String,
    val address: String?,
    val createdAt: Long
)

fun CustomerEntity.toDomain(): Customer = Customer(
    id = id,
    fullName = fullName,
    email = email.orEmpty(),
    phoneNumber = phone,
    passwordHash = passwordHash,
    address = address,
    createdAt = createdAt
)

fun Customer.toEntity(): CustomerEntity = CustomerEntity(
    id = id,
    fullName = fullName.trim(),
    email = email.trim().ifEmpty { null },
    phone = phoneNumber.trim(),
    passwordHash = passwordHash,
    address = address,
    createdAt = createdAt
)
