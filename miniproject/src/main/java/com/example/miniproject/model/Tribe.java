package com.example.miniproject.model;

public class Tribe {
    private int id;
    private String name;
    private String description;
    private String history;
    private String language;
    private String culture;
    private String lifestyle;
    private String image;

    // ต้องมีพารามิเตอร์ 8 ตัวตามลำดับนี้เท่านั้น
    public Tribe(int id, String name, String description, String history, 
                 String language, String culture, String lifestyle, String image) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.history = history;
        this.language = language;
        this.culture = culture;
        this.lifestyle = lifestyle;
        this.image = image;
    }

    // Getters สำหรับเรียกใช้ใน Thymeleaf
    public int getId() { return id; }
    public String getName() { return name; }
    public String getDescription() { return description; }
    public String getHistory() { return history; }
    public String getLanguage() { return language; }
    public String getCulture() { return culture; }
    public String getLifestyle() { return lifestyle; }
    public String getImage() { return image; }
}