package com.example.chieuthu3_ketthucmonhoc.domain.models

data class ChatMessage(
    val id: String = java.util.UUID.randomUUID().toString(),
    val text: String,
    val isUser: Boolean,
    val suggestedItems: List<MenuItem> = emptyList(),
    val imageUri: String? = null,
    val timestamp: Long = System.currentTimeMillis()
)
