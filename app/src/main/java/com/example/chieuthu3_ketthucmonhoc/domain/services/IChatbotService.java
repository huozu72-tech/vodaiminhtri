package com.example.chieuthu3_ketthucmonhoc.domain.services;

import com.example.chieuthu3_ketthucmonhoc.domain.models.ChatMessage;

import java.util.List;

public interface IChatbotService {
    ChatMessage sendMessage(String userText);
    ChatMessage analyzeFoodImage(String imageNameOrHint);
    ChatMessage analyzeFoodImage(String displayLabel, String recognitionHint, String imageUri);
    List<ChatMessage> getChatHistory();
    void clearChat();
}
