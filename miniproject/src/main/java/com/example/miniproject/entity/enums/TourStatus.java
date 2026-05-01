package com.example.miniproject.entity.enums;

public enum TourStatus {
    OPEN("เปิดรับจอง"),
    CLOSED("ปิดรับจอง"),
    CANCELLED("ยกเลิก"),
    COMPLETED("เสร็จสิ้น");
 
    private final String label;
 
    TourStatus(String label) {
        this.label = label;
    }
 
    public String getLabel() {
        return label;
    }
}
 
