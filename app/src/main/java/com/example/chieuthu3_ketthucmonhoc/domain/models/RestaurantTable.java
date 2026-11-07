package com.example.chieuthu3_ketthucmonhoc.domain.models;

import java.util.Objects;

/**
 * Mô hình Bàn ăn & Thông tin đặt bàn trong nhà hàng.
 */
public final class RestaurantTable {
    private final String tableNumber;
    private final String zone;
    private final int capacity;
    private final TableStatus status;
    private final String reservedByName;
    private final String reservedByPhone;
    private final String reservationTime;
    private final int guestCount;
    private final String note;

    public RestaurantTable(
            String tableNumber,
            String zone,
            int capacity,
            TableStatus status,
            String reservedByName,
            String reservedByPhone,
            String reservationTime,
            int guestCount,
            String note
    ) {
        this.tableNumber = Objects.requireNonNull(tableNumber, "tableNumber cannot be null");
        this.zone = Objects.requireNonNull(zone, "zone cannot be null");
        this.capacity = Math.max(1, capacity);
        this.status = Objects.requireNonNull(status, "status cannot be null");
        this.reservedByName = reservedByName != null ? reservedByName : "";
        this.reservedByPhone = reservedByPhone != null ? reservedByPhone : "";
        this.reservationTime = reservationTime != null ? reservationTime : "";
        this.guestCount = Math.max(0, guestCount);
        this.note = note != null ? note : "";
    }

    public RestaurantTable(String tableNumber, String zone, int capacity, TableStatus status) {
        this(tableNumber, zone, capacity, status, "", "", "", 0, "");
    }

    public String getTableNumber() {
        return tableNumber;
    }

    public String getZone() {
        return zone;
    }

    public int getCapacity() {
        return capacity;
    }

    public TableStatus getStatus() {
        return status;
    }

    public String getReservedByName() {
        return reservedByName;
    }

    public String getReservedByPhone() {
        return reservedByPhone;
    }

    public String getReservationTime() {
        return reservationTime;
    }

    public int getGuestCount() {
        return guestCount;
    }

    public String getNote() {
        return note;
    }

    public RestaurantTable withStatus(TableStatus newStatus) {
        if (newStatus == TableStatus.AVAILABLE) {
            return new RestaurantTable(tableNumber, zone, capacity, TableStatus.AVAILABLE, "", "", "", 0, "");
        }
        return new RestaurantTable(
                tableNumber, zone, capacity, newStatus,
                reservedByName, reservedByPhone, reservationTime, guestCount, note
        );
    }

    public RestaurantTable withReservation(
            String name,
            String phone,
            String time,
            int guests,
            String reservationNote
    ) {
        return new RestaurantTable(
                tableNumber,
                zone,
                capacity,
                TableStatus.RESERVED,
                name,
                phone,
                time,
                guests,
                reservationNote
        );
    }
}
