package com.example.miniproject.repository.Admin;

import com.example.miniproject.entity.Report;
import org.springframework.data.jpa.repository.JpaRepository;
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
} 
