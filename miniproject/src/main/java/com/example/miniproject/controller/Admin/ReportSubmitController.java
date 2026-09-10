package com.example.miniproject.controller.Admin;

import com.example.miniproject.dto.Admin.ReportResponseDto;
import com.example.miniproject.entity.HomestayReport;
import com.example.miniproject.entity.Member;
import com.example.miniproject.entity.TourReport;
import com.example.miniproject.service.Admin.HomestayReportService;
import com.example.miniproject.service.Admin.TourReportService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/reports")
public class ReportSubmitController {

    @Autowired
    private TourReportService tourReportService;

    @Autowired
    private HomestayReportService homestayReportService;

    @PostMapping
    public ResponseEntity<?> submitReport(
            @RequestParam(required = false) String tourId,
            @RequestParam(required = false) Integer homestayId,
            @RequestParam String reason,
            @RequestParam(required = false) String description,
            @RequestParam(required = false) String evidenceImage,
            HttpSession session) {

        // ✅ ต้องล็อกอินเป็นสมาชิกทั่วไปเท่านั้นถึงจะรายงานได้
        Member loggedInMember = (Member) session.getAttribute("loggedInMember");
        if (loggedInMember == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body("กรุณาเข้าสู่ระบบก่อนทำการรายงาน");
        }

        boolean hasTour = tourId != null && !tourId.isBlank();
        boolean hasHomestay = homestayId != null;

        if (hasTour == hasHomestay) {
            return ResponseEntity.badRequest().body("ต้องระบุ tourId หรือ homestayId อย่างใดอย่างหนึ่งเท่านั้น");
        }

        try {
            ReportResponseDto dto;
            if (hasTour) {
                TourReport saved = tourReportService.createReport(reason, description, evidenceImage, tourId);
                dto = new ReportResponseDto(saved.getTourreportid(), saved.getStatus(), saved.getReason(), "ส่งรายงานเรียบร้อยแล้ว");
            } else {
                HomestayReport saved = homestayReportService.createReport(reason, description, evidenceImage, homestayId);
                dto = new ReportResponseDto(saved.getHomestayreportid(), saved.getStatus(), saved.getReason(), "ส่งรายงานเรียบร้อยแล้ว");
            }
            return ResponseEntity.ok(dto);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}