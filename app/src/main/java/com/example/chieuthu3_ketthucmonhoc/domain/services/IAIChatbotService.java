package com.example.chieuthu3_ketthucmonhoc.domain.services;

import com.example.chieuthu3_ketthucmonhoc.domain.models.ChatMessage;

import java.util.List;

public interface IAIChatbotService {
    ChatMessage sendMessage(String userText);
    ChatMessage analyzeFoodImage(String imageNameOrHint);
    List<ChatMessage> getChatHistory();
    void clearChat();
}
