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
    // เพิ่ม DISTINCT ป้องกัน review ซ้ำ ถ้า booking มีหลาย roomDetail
    @Query("""
        SELECT DISTINCT r FROM Review r
        JOIN r.booking b
        JOIN b.roomDetails rd
        JOIN rd.roomtype rt
        WHERE rt.homestay.homestayid = :homestayId
        ORDER BY r.reviewdate DESC
    """)
    List<Review> findByHomestayId(@Param("homestayId") Integer homestayId);

    Optional<Review> findByBookingBookingid(String bookingId);

    // นับ/เฉลี่ยจาก reviewid ที่ไม่ซ้ำ แทนการ AVG/COUNT(r) ตรงๆ บน join ที่มีโอกาสซ้ำแถว
    @Query("""
        SELECT AVG(r.rating) FROM Review r
        WHERE r.reviewid IN (
            SELECT DISTINCT r2.reviewid FROM Review r2
            JOIN r2.booking b
            JOIN b.roomDetails rd
            JOIN rd.roomtype rt
            WHERE rt.homestay.homestayid = :homestayId
        )
    """)
    Double avgRatingByHomestayId(@Param("homestayId") Integer homestayId);

    @Query("""
        SELECT COUNT(DISTINCT r) FROM Review r
        JOIN r.booking b
        JOIN b.roomDetails rd
        JOIN rd.roomtype rt
        WHERE rt.homestay.homestayid = :homestayId
    """)
    Long countByHomestayId(@Param("homestayId") Integer homestayId);

    // ─── ทัวร์ ────────────────────────────────────────────
    @Query("""
        SELECT DISTINCT r FROM Review r
        JOIN r.booking b
        JOIN b.tourDetails btd
        WHERE btd.tour.tourid = :tourid
        ORDER BY r.reviewdate DESC
    """)
    List<Review> findByTourId(@Param("tourid") String tourid);

    @Query("""
        SELECT AVG(r.rating) FROM Review r
        WHERE r.reviewid IN (
            SELECT DISTINCT r2.reviewid FROM Review r2
            JOIN r2.booking b
            JOIN b.tourDetails btd
            WHERE btd.tour.tourid = :tourid
        )
    """)
    Double avgRatingByTourId(@Param("tourid") String tourid);

    @Query("""
        SELECT COUNT(DISTINCT r) FROM Review r
        JOIN r.booking b
        JOIN b.tourDetails btd
        WHERE btd.tour.tourid = :tourid
    """)
    Long countByTourId(@Param("tourid") String tourid);

    @Query("SELECT COUNT(r) FROM Review r")
    long countAll();
}