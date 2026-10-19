package com.example.chieuthu3_ketthucmonhoc.domain.services

import com.example.chieuthu3_ketthucmonhoc.domain.models.ChatMessage

interface IAIChatbotService {
    fun sendMessage(userText: String): ChatMessage
    fun analyzeFoodImage(imageNameOrHint: String): ChatMessage
    fun getChatHistory(): List<ChatMessage>
    fun clearChat()
}
