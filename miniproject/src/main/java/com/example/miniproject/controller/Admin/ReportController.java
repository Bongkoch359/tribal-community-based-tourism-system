package com.example.miniproject.controller.Admin;


import com.example.miniproject.entity.Report;
import com.example.miniproject.service.Admin.ReportService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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
} 
