package com.example.miniproject.entity.enums;

public enum HomestayownerStatus {
    ACTIVE("ใช้งานอยู่"),
    INACTIVE("ระงับการใช้งาน");
   

    private final String label;

    HomestayownerStatus(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
