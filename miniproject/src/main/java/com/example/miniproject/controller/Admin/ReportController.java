package com.example.miniproject.controller.Admin;


import com.example.miniproject.dto.Admin.ReportListItemDto;
import com.example.miniproject.entity.Report;
import com.example.miniproject.service.Admin.ReportService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

// ใช้จากหน้า Admin > จัดการรายงาน
@RestController
@RequestMapping("/api/admin/reports")
public class ReportController {

    @Autowired
    private ReportService reportService;

    // ดูลิสต์ทั้งหมด หรือกรองตามสถานะ เช่น /api/admin/reports?status=PENDING
    @GetMapping
    public List<Report> getAll(@RequestParam(required = false) String status) {
        if (status != null && !status.isBlank()) {
            return reportService.getReportsByStatus(status);
        }
        return reportService.getAllReports();
    }

    // ดูรายละเอียด report เดียว พร้อมรูปหลักฐาน0
    @GetMapping("/{id}")
    public Report getOne(@PathVariable String id) {
        return reportService.getReportById(id);
    }

    // ตัดสินใจดำเนินการ: REJECT / SUSPEND_LISTING / SUSPEND_ACCOUNT
    @PutMapping("/{id}/action")
    public ResponseEntity<?> takeAction(@PathVariable String id, @RequestParam String action) {
        try {
            Report updated = reportService.resolveReport(id, action);
            return ResponseEntity.ok(updated);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // นับจำนวนครั้งที่ถูกรายงาน ใช้โชว์ข้าง ๆ แต่ละรายการในลิสต์
    @GetMapping("/count")
    public long count(@RequestParam(required = false) String tourId,
            @RequestParam(required = false) Integer homestayId) {
        if (tourId != null && !tourId.isBlank()) {
            return reportService.countReportsForTour(tourId);
        }
        if (homestayId != null) {
            return reportService.countReportsForHomestay(homestayId);
        }
        return 0;
    }

    // ดู report ทั้งหมดของ manager คนหนึ่ง (ใช้ตอนกด "ดู" ในหน้าจัดการผู้จัดการ)
    // คืนเป็น DTO ที่ flatten แล้ว แทน entity ตรง ๆ เพราะ Report -> tour -> communitymanager -> tours[]
    // วนกลับมาหา entity เดิมโดยไม่มี @JsonIgnore กัน ถ้าส่ง entity ตรง ๆ จะ StackOverflowError
    @GetMapping("/by-manager/{managerId}")
    public List<ReportListItemDto> getByManager(@PathVariable String managerId) {
        return reportService.getReportsByManager(managerId).stream()
                .map(ReportController::toListItemDto)
                .collect(Collectors.toList());
    }

    // ดู report ทั้งหมดของ homestay owner คนหนึ่ง (ใช้ตอนกด "ดู" ในหน้าจัดการโฮมสเตย์)
    // เหตุผลเดียวกับด้านบน: Report -> homestay -> owner -> homestays[] วนกลับมาหา entity เดิม
    // หมายเหตุ: Homestayowner ใช้ String เป็น PK (เช่น "OW001") ไม่ใช่ int จึงต้องรับเป็น String
    @GetMapping("/by-owner/{ownerId}")
    public List<ReportListItemDto> getByOwner(@PathVariable String ownerId) {
        return reportService.getReportsByHomestayOwner(ownerId).stream()
                .map(ReportController::toListItemDto)
                .collect(Collectors.toList());
    }

    // แปลง Report entity -> ReportListItemDto โดยดึงเฉพาะชื่อ/รหัสของทัวร์หรือโฮมสเตย์ที่ถูกรายงาน
    // (ไม่ดึง entity ทัวร์/โฮมสเตย์ทั้งก้อนออกไป ตัดปัญหา lazy-loading และ circular reference)
    private static ReportListItemDto toListItemDto(Report r) {
        String targetType = null;
        String targetId = null;
        String targetName = null;

        if (r.getTour() != null) {
            targetType = "TOUR";
            targetId = r.getTour().getTourid();
            targetName = r.getTour().getTourmname();
        } else if (r.getHomestay() != null) {
            targetType = "HOMESTAY";
            targetId = String.valueOf(r.getHomestay().getHomestayid());
            targetName = r.getHomestay().getHomestayname();
        }

        return new ReportListItemDto(
                r.getReportid(),
                r.getReason(),
                r.getDescription(),
                r.getStatus(),
                r.getCreatedAt(),
                r.getEvidenceImage(),
                targetType,
                targetId,
                targetName
        );
    }
}