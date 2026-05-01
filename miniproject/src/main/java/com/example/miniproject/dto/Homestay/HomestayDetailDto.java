package com.example.miniproject.dto.Homestay;

import com.example.miniproject.entity.Homestay;
import java.util.List;

public class HomestayDetailDto {

    private Homestay homestay;
    private Double avgRating;
    private Long reviewCount;
    private Long roomTypeCount;
    private Long bookingCount;
    private String firstImage;
    private List<String> allImages;

   public HomestayDetailDto(Homestay homestay, Double avgRating, Long reviewCount,
                          Long roomTypeCount, Long bookingCount) {
    this.homestay      = homestay;
    this.avgRating     = avgRating     != null ? avgRating     : 0.0;
    this.reviewCount   = reviewCount   != null ? reviewCount   : 0L;
    this.roomTypeCount = roomTypeCount != null ? roomTypeCount : 0L;
    this.bookingCount  = bookingCount  != null ? bookingCount  : 0L;

    // ✅ null-safe
    String raw = homestay.getImages();
    if (raw != null && !raw.isBlank()) {
        String[] parts = raw.split("(?=,data:)");
        this.firstImage = parts[0];
        this.allImages  = java.util.Arrays.asList(parts);
    } else {
        this.firstImage = null;
        this.allImages  = new java.util.ArrayList<>();
    }
}

    public Homestay getHomestay()       { return homestay; }
    public Double getAvgRating()        { return avgRating; }
    public Long getReviewCount()        { return reviewCount; }
    public Long getRoomTypeCount()      { return roomTypeCount; }
    public Long getBookingCount()       { return bookingCount; }
    public String getFirstImage()       { return firstImage; }
    public List<String> getAllImages()  { return allImages; }
}
