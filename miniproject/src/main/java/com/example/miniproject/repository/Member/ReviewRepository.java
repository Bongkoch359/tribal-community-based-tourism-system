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

    // ─── โฮมสเตย์ ─────────────────────────────────────────

    @Query("""
        SELECT r FROM Review r
        JOIN r.booking b
        JOIN b.roomDetails rd
        JOIN rd.roomtype rt
        WHERE rt.homestay.homestayid = :homestayId
        ORDER BY r.reviewdate DESC
    """)
    List<Review> findByHomestayId(@Param("homestayId") Integer homestayId);

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
    Double avgRatingByHomestayId(@Param("homestayId") Integer homestayId);

    // ─── ทัวร์ ────────────────────────────────────────────

    // ดึงรีวิวทั้งหมดของทัวร์ ผ่าน booking → tourDetails → tour
    @Query("""
        SELECT r FROM Review r
        JOIN r.booking b
        JOIN b.tourDetails btd
        WHERE btd.tour.tourid = :tourid
        ORDER BY r.reviewdate DESC
    """)
    List<Review> findByTourId(@Param("tourid") String tourid);

    // ค่าเฉลี่ย rating ของทัวร์
    @Query("""
        SELECT AVG(r.rating) FROM Review r
        JOIN r.booking b
        JOIN b.tourDetails btd
        WHERE btd.tour.tourid = :tourid
    """)
    Double avgRatingByTourId(@Param("tourid") String tourid);

    // นับจำนวน review ทั้งหมดเพื่อ generate ID ใหม่
    @Query("SELECT COUNT(r) FROM Review r")
    long countAll();
}