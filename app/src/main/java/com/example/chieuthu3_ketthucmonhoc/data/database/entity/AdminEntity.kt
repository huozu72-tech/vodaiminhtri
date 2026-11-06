package com.example.chieuthu3_ketthucmonhoc.data.database.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.example.chieuthu3_ketthucmonhoc.domain.models.AdminAccount
import com.example.chieuthu3_ketthucmonhoc.domain.models.UserRole

/** Bảng tài khoản quản trị. Ràng buộc: username UNIQUE NOT NULL. */
@Entity(
    tableName = "admins",
    indices = [Index(value = ["username"], unique = true)]
)
data class AdminEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val username: String,
    val passwordHash: String,
    val fullName: String,
    val role: String,
    val createdAt: Long
)

fun AdminEntity.toDomain(): AdminAccount = AdminAccount(
    id = id,
    username = username,
    passwordHash = passwordHash,
    fullName = fullName,
    role = UserRole.valueOf(role),
    createdAt = createdAt
)

fun AdminAccount.toEntity(): AdminEntity = AdminEntity(
    id = id,
    username = username.trim(),
    passwordHash = passwordHash,
    fullName = fullName,
    role = role.name,
    createdAt = createdAt
)
