package com.example.miniproject.model;

public enum TribeCode {
    KAREN(1, "กะเหรี่ยง (ปกาเกอะญอ)"),
    HMONG(2, "ม้ง (แม้ว)"),
    AKHA(3, "อาข่า (อีก้อ)"),
    LAHU(4, "ลาหู่ (มูเซอ)"),
    LISU(5, "ลีซู (ลีซอ)"),
    PADONG(6, "กะเหรี่ยงคอยาว (ปะด่อง)"),
    YAO(7, "เย้า (เมี่ยน)"),
    LUA(8, "ลัวะ (ละว้า)");

    private final int id;
    private final String label;

    TribeCode(int id, String label) {
        this.id = id;
        this.label = label;
    }

    public int getId() { return id; }
    public String getLabel() { return label; }

    public static Integer resolveId(String label) {
        if (label == null) return null;
        String trimmed = label.trim();
        for (TribeCode t : values()) {
            if (t.label.equals(trimmed)) return t.id;
        }
        return null;
    }
}