package com.example.chieuthu3_ketthucmonhoc.domain.models;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

public class ChatMessage {
    private final String id;
    private final String text;
    private final boolean isUser;
    private final List<MenuItem> suggestedItems;
    private final String imageUri;
    private final long timestamp;

    public ChatMessage(String text, boolean isUser) {
        this(UUID.randomUUID().toString(), text, isUser, Collections.emptyList(), null, System.currentTimeMillis());
    }

    public ChatMessage(String id, String text, boolean isUser, List<MenuItem> suggestedItems, String imageUri, long timestamp) {
        this.id = id != null ? id : UUID.randomUUID().toString();
        this.text = text;
        this.isUser = isUser;
        this.suggestedItems = suggestedItems != null ? Collections.unmodifiableList(new ArrayList<>(suggestedItems)) : Collections.emptyList();
        this.imageUri = imageUri;
        this.timestamp = timestamp;
    }

    public String getId() {
        return id;
    }

    public String getText() {
        return text;
    }

    public boolean isUser() {
        return isUser;
    }

    public List<MenuItem> getSuggestedItems() {
        return suggestedItems;
    }

    public String getImageUri() {
        return imageUri;
    }

    public long getTimestamp() {
        return timestamp;
    }
}
