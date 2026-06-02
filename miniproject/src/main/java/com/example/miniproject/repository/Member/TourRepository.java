package com.example.miniproject.repository.Member;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import com.example.miniproject.entity.Communitymanager;
import com.example.miniproject.entity.Tour;

public interface TourRepository extends JpaRepository<Tour, String> {

    // ค้นหาชื่อทัวร์
    List<Tour> findByTourmnameContainingIgnoreCase(String tourmname);

    List<Tour> findByCommunitymanagerManagerid(String managerId);
    

    // ดึงทัวร์ active
    List<Tour> findByStatus(String status);

    // ค้นหาชื่อ + status
    List<Tour> findByTourmnameContainingIgnoreCaseAndStatus(
            String tourmname,
            String status
    );

     // ─── ดึงตาม manager (ใหม่ — ใช้ใน listTour) ──────────
    List<Tour> findByCommunitymanager(Communitymanager communitymanager);

    // ค้นหาตามจำนวนที่นั่ง
    @Query("""
        SELECT t FROM Tour t
        WHERE t.status = 'active'
        AND t.minSeatstour <= :guests
        AND t.maxSeatstour >= :guests
    """)
    List<Tour> findByAvailableSeats(@Param("guests") int guests);

    // ค้นหาแบบรวม
    @Query("""
        SELECT t FROM Tour t
        WHERE (t.status IS NULL OR LOWER(t.status) = 'active')
        AND (:keyword IS NULL
             OR LOWER(t.tourmname)
             LIKE LOWER(CONCAT('%', :keyword, '%')))
        AND (:guests IS NULL 
             OR :guests <= 1 
             OR (t.minSeatstour <= :guests AND t.maxSeatstour >= :guests))
        ORDER BY t.tourmname ASC
    """)
    List<Tour> search(@Param("keyword") String keyword,
                      @Param("guests") Integer guests);

    //นับตามสถานะ
    long countByStatus(String status);
    // ดึงทัวร์ยอดนิยม (เรียงตามจำนวนการจอง)
    @Query("SELECT t FROM Tour t LEFT JOIN t.bookingTourDetails d GROUP BY t ORDER BY COUNT(d) DESC")
    List<Tour> findTopToursByBookingCount(@Param("limit") int limit);

    // ดึงทัวร์ของ manager คนนั้น
    @Query("SELECT t FROM Tour t WHERE t.communitymanager.managerid = :managerid ORDER BY t.tourmname ASC")
    List<Tour> findByManagerId(@Param("managerid") String managerid);

    

}