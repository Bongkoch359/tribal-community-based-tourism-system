package com.example.miniproject.controller.Admin;

import com.example.miniproject.dto.Admin.ReportResponseDto;
import com.example.miniproject.entity.Report;
import com.example.miniproject.service.Admin.ReportService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/reports")
public class ReportSubmitController {

    @Autowired
    private ReportService reportService;

    @PostMapping
    public ResponseEntity<?> submitReport(
            @RequestParam(required = false) String tourId,
            @RequestParam(required = false) Integer homestayId,
            @RequestParam String reason,
            @RequestParam(required = false) String description,
            @RequestParam(required = false) String evidenceImage) {
        try {
            Report saved = reportService.createReport(reason, description, evidenceImage, tourId, homestayId);

            // ✅ เปลี่ยนตรงนี้ — ส่ง DTO แทน entity ทั้งก้อน
            ReportResponseDto dto = new ReportResponseDto(
                saved.getReportid(),
                saved.getStatus(),
                saved.getReason(),
                "ส่งรายงานเรียบร้อยแล้ว"
            );

            return ResponseEntity.ok(dto);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}