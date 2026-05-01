package com.example.miniproject.entity.enums;

public enum RoomtypeStatus {
    AVAILABLE("ว่าง"),
    UNAVAILABLE("ไม่ว่าง"),
    MAINTENANCE("อยู่ระหว่างซ่อมบำรุง");
 
    private final String label;
 
    RoomtypeStatus(String label) {
        this.label = label;
    }
 
    public String getLabel() {
        return label;
    }
}
 
