package com.example.miniproject.model;

public class Vocabulary {
    private String word;       // คำศัพท์ประจำเผ่า
    private String karaoke;    // คำอ่านออกเสียงภาษาไทย
    private String meaning;    // ความหมายภาษาไทย
    private String category;   // หมวดหมู่

    public Vocabulary(String word, String karaoke, String meaning, String category) {
        this.word = word;
        this.karaoke = karaoke;
        this.meaning = meaning;
        this.category = category;
    }

    public String getWord() { return word; }
    public String getKaraoke() { return karaoke; }
    public String getMeaning() { return meaning; }
    public String getCategory() { return category; }
}