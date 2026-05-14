package com.example.miniproject.repository.Member;

import com.example.miniproject.entity.Review;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface ReviewRepository extends JpaRepository<Review, String> {

    @Query("SELECT r FROM Review r " +
           "JOIN r.booking b JOIN b.roomDetails rd JOIN rd.roomtype rt " +
           "WHERE rt.homestay.homestayid = :homestayid")
    List<Review> findByHomestayId(@Param("homestayid") Integer homestayid);

    
    // ดึงรีวิวทั้งหมดของโฮมสเตย์ ผ่าน booking → roomDetails → roomtype → homestay
    @Query("""
        SELECT r FROM Review r
        JOIN r.booking b
        JOIN b.roomDetails rd
        JOIN rd.roomtype rt
        WHERE rt.homestay.homestayid = :homestayId
        ORDER BY r.reviewdate DESC
    """)
    List<Review> findByHomestayId(@Param("homestayId") String homestayId);

    // เช็คว่า booking นี้ review ไปแล้วหรือยัง
    Optional<Review> findByBookingBookingid(String bookingId);

    // ค่าเฉลี่ย rating ของโฮมสเตย์
    @Query("""
        SELECT AVG(r.rating) FROM Review r
        JOIN r.booking b
        JOIN b.roomDetails rd
        JOIN rd.roomtype rt
        WHERE rt.homestay.homestayid = :homestayId
    """)
    Double avgRatingByHomestayId(@Param("homestayId") String homestayId);

    // นับ ID สูงสุดเพื่อ generate ID ใหม่
    @Query("SELECT COUNT(r) FROM Review r")
    long countAll();
}