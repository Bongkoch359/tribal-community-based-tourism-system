package com.example.miniproject.repository.Admin;

import com.example.miniproject.entity.HomestayReport;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface HomestayReportRepository extends JpaRepository<HomestayReport, String> {

    // สำหรับหน้า Admin > จัดการรายงาน เรียงตามล่าสุดก่อน
    List<HomestayReport> findAllByOrderByCreatedAtDesc();

    // กรองตามสถานะ เช่น ดูเฉพาะที่ยังไม่ได้ดำเนินการ (PENDING)
    List<HomestayReport> findByStatusOrderByCreatedAtDesc(String status);

    // ดู report ทั้งหมดของที่พักหลังหนึ่ง
    List<HomestayReport> findByHomestay_HomestayidOrderByCreatedAtDesc(int homestayid);

    // นับจำนวนครั้งที่ที่พักหลังนี้ถูกรายงาน (ไม่จำกัดสถานะ)
    long countByHomestay_Homestayid(int homestayid);

    // หา Report ID ล่าสุด
    Optional<HomestayReport> findTopByOrderByHomestayreportidDesc();

    // นับ PENDING แยกตาม homestay owner ทุกคน
    @Query("""
        SELECT r.homestay.owner.ownerid, COUNT(r)
        FROM HomestayReport r
        WHERE r.status = 'PENDING'
        GROUP BY r.homestay.owner.ownerid
    """)
    List<Object[]> countPendingGroupedByHomestayOwner();

    // ดึง report ทั้งหมดของโฮมสเตย์ของเจ้าของคนหนึ่ง — ใช้โชว์ใน modal
    List<HomestayReport> findByHomestay_Owner_OwneridOrderByCreatedAtDesc(String ownerid);

    // ดึงเฉพาะ report ที่ PENDING ของที่พักหลังหนึ่ง — ใช้ตอนระงับบัญชีเพื่อ resolve ทีเดียว
    List<HomestayReport> findByHomestay_HomestayidAndStatus(int homestayid, String status);
}