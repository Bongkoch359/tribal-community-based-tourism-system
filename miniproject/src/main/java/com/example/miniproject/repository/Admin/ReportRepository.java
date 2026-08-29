package com.example.miniproject.repository.Admin;

import com.example.miniproject.entity.Report;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.Optional;

import java.util.List;

@Repository
public interface ReportRepository extends JpaRepository<Report, String> {

    // สำหรับหน้า Admin > จัดการรายงาน เรียงตามล่าสุดก่อน
    List<Report> findAllByOrderByCreatedAtDesc();

    // กรองตามสถานะ เช่น ดูเฉพาะที่ยังไม่ได้ดำเนินการ (PENDING)
    List<Report> findByStatusOrderByCreatedAtDesc(String status);

    // ดู report ทั้งหมดของทัวร์รายการหนึ่ง
    List<Report> findByTour_TouridOrderByCreatedAtDesc(String tourid);

    // ดู report ทั้งหมดของที่พักหลังหนึ่ง
    List<Report> findByHomestay_HomestayidOrderByCreatedAtDesc(int homestayid);

    // นับจำนวนครั้งที่ทัวร์รายการนี้ถูกรายงาน (ไม่จำกัดสถานะ)
    long countByTour_Tourid(String tourid);

    // นับจำนวนครั้งที่ที่พักหลังนี้ถูกรายงาน (ไม่จำกัดสถานะ)
    long countByHomestay_Homestayid(int homestayid);

    // หา Report ID ล่าสุด
    Optional<Report> findTopByOrderByReportidDesc();

    // นับ PENDING แยกตาม manager ทุกคน (group by ในทีเดียว ไม่ต้องโหลด report ทั้งหมดมานับเอง)
@Query("""
    SELECT r.tour.communitymanager.managerid, COUNT(r)
    FROM Report r
    WHERE r.status = 'PENDING' AND r.tour IS NOT NULL
    GROUP BY r.tour.communitymanager.managerid
""")
List<Object[]> countPendingGroupedByManager();

// นับ PENDING แยกตาม homestay owner ทุกคน
@Query("""
    SELECT r.homestay.owner.ownerid, COUNT(r)
    FROM Report r
    WHERE r.status = 'PENDING' AND r.homestay IS NOT NULL
    GROUP BY r.homestay.owner.ownerid
""")
List<Object[]> countPendingGroupedByHomestayOwner();

// ดึง report ทั้งหมด (ไม่จำกัดสถานะ) ของทัวร์ในความดูแลของ manager คนหนึ่ง — ใช้โชว์ใน modal
List<Report> findByTour_Communitymanager_ManageridOrderByCreatedAtDesc(String managerid);

// ดึง report ทั้งหมดของโฮมสเตย์ของเจ้าของคนหนึ่ง — ใช้โชว์ใน modal
List<Report> findByHomestay_Owner_OwneridOrderByCreatedAtDesc(String ownerid); // ปรับ type ให้ตรงกับ Homestayowner.ownerid จริง

// ดึงเฉพาะ report ที่ PENDING ของที่พักหลังหนึ่ง — ใช้ตอนระงับบัญชีเพื่อ resolve ทีเดียว
List<Report> findByHomestay_HomestayidAndStatus(int homestayid, String status);
List<Report> findByTour_Communitymanager_ManageridAndStatus(String managerId, String status);
}
