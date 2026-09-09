package com.example.miniproject.repository.Admin;

import com.example.miniproject.entity.TourReport;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TourReportRepository extends JpaRepository<TourReport, String> {

    // สำหรับหน้า Admin > จัดการรายงาน เรียงตามล่าสุดก่อน
    List<TourReport> findAllByOrderByCreatedAtDesc();

    // กรองตามสถานะ เช่น ดูเฉพาะที่ยังไม่ได้ดำเนินการ (PENDING)
    List<TourReport> findByStatusOrderByCreatedAtDesc(String status);

    // ดู report ทั้งหมดของทัวร์รายการหนึ่ง
    List<TourReport> findByTour_TouridOrderByCreatedAtDesc(String tourid);

    // นับจำนวนครั้งที่ทัวร์รายการนี้ถูกรายงาน (ไม่จำกัดสถานะ)
    long countByTour_Tourid(String tourid);

    // หา Report ID ล่าสุด
    Optional<TourReport> findTopByOrderByTourreportidDesc();

    // นับ PENDING แยกตาม manager ทุกคน (group by ในทีเดียว ไม่ต้องโหลด report ทั้งหมดมานับเอง)
    @Query("""
        SELECT r.tour.communitymanager.managerid, COUNT(r)
        FROM TourReport r
        WHERE r.status = 'PENDING'
        GROUP BY r.tour.communitymanager.managerid
    """)
    List<Object[]> countPendingGroupedByManager();

    // ดึง report ทั้งหมด (ไม่จำกัดสถานะ) ของทัวร์ในความดูแลของ manager คนหนึ่ง — ใช้โชว์ใน modal
    List<TourReport> findByTour_Communitymanager_ManageridOrderByCreatedAtDesc(String managerid);

    // ดึงเฉพาะ report ที่ PENDING ของทัวร์ในความดูแลของ manager คนหนึ่ง — ใช้ตอนระงับบัญชีเพื่อ resolve ทีเดียว
    List<TourReport> findByTour_Communitymanager_ManageridAndStatus(String managerId, String status);
}