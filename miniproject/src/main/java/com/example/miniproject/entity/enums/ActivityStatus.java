package com.example.miniproject.entity.enums;

public enum ActivityStatus {
    DRAFT("ฉบับร่าง"),
    PUBLISHED("เผยแพร่แล้ว"),
    HIDDEN("ซ่อนโพส"),
    CANCELLED("ยกเลิกกิจกรรม");

    private String label;

    ActivityStatus(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
