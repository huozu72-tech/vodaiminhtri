package com.example.chieuthu3_ketthucmonhoc.domain.models;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Objects;

/**
 * Yêu cầu gọi nhân viên / hỗ trợ tại bàn (Smart Waiter Call).
 */
public final class TableServiceRequest {
    private final int id;
    private final String tableNumber;
    private final String requestType;
    private final String customNote;
    private final long createdAt;
    private final boolean resolved;

    public TableServiceRequest(
            int id,
            String tableNumber,
            String requestType,
            String customNote,
            long createdAt,
            boolean resolved
    ) {
        this.id = id;
        this.tableNumber = Objects.requireNonNull(tableNumber, "tableNumber cannot be null");
        this.requestType = Objects.requireNonNull(requestType, "requestType cannot be null");
        this.customNote = customNote != null ? customNote.trim() : "";
        this.createdAt = createdAt;
        this.resolved = resolved;
    }

    public int getId() {
        return id;
    }

    public String getTableNumber() {
        return tableNumber;
    }

    public String getRequestType() {
        return requestType;
    }

    public String getCustomNote() {
        return customNote;
    }

    public long getCreatedAt() {
        return createdAt;
    }

    public String getFormattedTime() {
        return new SimpleDateFormat("HH:mm", Locale.getDefault()).format(new Date(createdAt));
    }

    public boolean isResolved() {
        return resolved;
    }

    public TableServiceRequest withResolved(boolean isResolved) {
        return new TableServiceRequest(id, tableNumber, requestType, customNote, createdAt, isResolved);
    }
}
