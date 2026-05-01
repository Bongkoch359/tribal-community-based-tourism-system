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

        // ✅ ดึงรูปแรกจาก Base64 string
        if (homestay.getImages() != null && !homestay.getImages().isEmpty()) {
            String[] parts = homestay.getImages().split(",data:");
            this.firstImage = parts[0]; // รูปแรก
        }
    }

    public Homestay getHomestay()     { return homestay; }
    public Double getAvgRating()      { return avgRating; }
    public Long getReviewCount()      { return reviewCount; }
    public String getFirstImage()     { return firstImage; } // ✅ เพิ่ม
}