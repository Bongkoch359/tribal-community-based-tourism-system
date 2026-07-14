package com.example.miniproject.repository.Tour;

import java.sql.Date;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.example.miniproject.entity.Tourschedule;

public interface TourScheduleRepository extends JpaRepository<Tourschedule, String> {

    // ดึงรอบทั้งหมดของทัวร์หนึ่งๆ เรียงตามวันที่ (ใช้โชว์ list ในหน้า tourdetail ฝั่ง member)
    List<Tourschedule> findByTourTouridOrderByOpendateAsc(String tourid);

    // ดึงเฉพาะรอบที่ "เปิดรับจอง" และยังไม่ผ่านวันที่มาแล้ว (สำหรับหน้าจองของ member)
    @Query("""
        SELECT s FROM Tourschedule s
        WHERE s.tour.tourid = :tourid
        AND s.status = 'เปิดรับจอง'
        AND s.opendate >= :today
        ORDER BY s.opendate ASC
    """)
    List<Tourschedule> findBookableSchedules(@Param("tourid") String tourid, @Param("today") Date today);

    // จำนวนที่นั่งที่ถูกจองไปแล้วของแต่ละ schedule (นับเฉพาะ booking ที่ไม่ถูกยกเลิก)
    // คืนค่าเป็น [scheduleid, จำนวนที่นั่งที่จองแล้ว]
    @Query("""
        SELECT s.scheduleid,
               COALESCE(SUM(
                   CASE WHEN b.bookingStatus <> com.example.miniproject.entity.enums.BookingStatus.CANCEL
                        THEN d.numofadult + COALESCE(d.numofchild, 0)
                        ELSE 0 END
               ), 0)
        FROM Tourschedule s
        LEFT JOIN s.bookingtourdetails d
        LEFT JOIN d.booking b
        WHERE s.tour.tourid = :tourid
        GROUP BY s.scheduleid
    """)
    List<Object[]> findBookedSeatsByTour(@Param("tourid") String tourid);
}