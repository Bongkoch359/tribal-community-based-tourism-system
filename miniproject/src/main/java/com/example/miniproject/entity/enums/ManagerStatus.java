package com.example.miniproject.entity.enums;

public enum ManagerStatus {
    ACTIVE("ใช้งานอยู่"),
    INACTIVE("ระงับการใช้งาน");
   

    private final String label;

    ManagerStatus(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}