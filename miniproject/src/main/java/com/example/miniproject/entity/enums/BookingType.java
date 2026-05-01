package com.example.miniproject.entity.enums;

public enum BookingType {
    TOUR("จองทัวร์"),
    ACCOMMODATION("จองที่พัก");

    private final String label;

    BookingType(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
