package com.example.miniproject.entity.enums;

public enum BookingStatus {
    PENDING("รอชำระเงิน"),
    WAITING_APPROVAL("รอที่พักอนุมัติ"),
    CONFIRMED("อนุมัติแล้ว"),
    CANCEL("ยกเลิก");

    private final String label;

    BookingStatus(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
