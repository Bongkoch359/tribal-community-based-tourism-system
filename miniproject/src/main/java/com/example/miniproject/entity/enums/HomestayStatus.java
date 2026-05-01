package com.example.miniproject.entity.enums;

public enum HomestayStatus {
    ACTIVE("เปิดให้บริการ"),
    INACTIVE("ปิดให้บริการ"),
    PENDING("รอการอนุมัติ");
 
    private final String label;
 
    HomestayStatus(String label) {
        this.label = label;
    }
 
    public String getLabel() {
        return label;
    }
}
