package com.example.miniproject.dto.Homestay;

import com.example.miniproject.entity.Homestay;

public class HomestayDto {

    private Homestay homestay;
    private Double avgRating;
    private Long reviewCount;
    private String firstImage; 

    public HomestayDto(Homestay homestay, Double avgRating, Long reviewCount) {
        this.homestay    = homestay;
        this.avgRating   = avgRating  != null ? avgRating  : 0.0;
        this.reviewCount = reviewCount != null ? reviewCount : 0L;

        // images field เก็บเป็น CSV ของ file path เช่น
        // "/uploads/homestays/1/abc.jpg,/uploads/homestays/1/def.jpg"
        String raw = homestay.getImages();
        if (raw != null && !raw.isBlank()) {
            String first = raw.split(",")[0].trim();
            this.firstImage = first.isEmpty() ? null : first;
        } else {
            this.firstImage = null;
        }
    }

    public Homestay getHomestay()     { return homestay; }
    public Double getAvgRating()      { return avgRating; }
    public Long getReviewCount()      { return reviewCount; }
    public String getFirstImage()     { return firstImage; } // ✅ เพิ่ม
}