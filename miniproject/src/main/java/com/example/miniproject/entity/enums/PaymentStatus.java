package com.example.miniproject.entity.enums;


public enum PaymentStatus {
    UNPAID("ยังไม่ชำระเงิน"),
    PAID("ชำระเงินแล้ว");

    private final String label;

    PaymentStatus(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}