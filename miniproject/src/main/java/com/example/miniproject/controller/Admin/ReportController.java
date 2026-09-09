package com.example.miniproject.controller.Admin;

import com.example.miniproject.dto.Admin.ReportListItemDto;
import com.example.miniproject.entity.HomestayReport;
import com.example.miniproject.entity.TourReport;
import com.example.miniproject.service.Admin.HomestayReportService;
import com.example.miniproject.service.Admin.TourReportService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

// ใช้จากหน้า Admin > จัดการรายงาน (รวม Tour + Homestay ไว้ด้วยกัน)
@RestController
@RequestMapping("/api/admin/reports")
public class ReportController {

    @Autowired
    private TourReportService tourReportService;

    @Autowired
    private HomestayReportService homestayReportService;

    // ดูลิสต์ทั้งหมด หรือกรองตามสถานะ เช่น /api/admin/reports?status=PENDING
    // รวมของทั้งสองฝั่งแล้วเรียงตามวันที่ล่าสุดก่อน
    @GetMapping
    public List<ReportListItemDto> getAll(@RequestParam(required = false) String status) {
        List<TourReport> tourReports = (status != null && !status.isBlank())
                ? tourReportService.getReportsByStatus(status)
                : tourReportService.getAllReports();

        List<HomestayReport> homestayReports = (status != null && !status.isBlank())
                ? homestayReportService.getReportsByStatus(status)
                : homestayReportService.getAllReports();

        List<ReportListItemDto> combined = new ArrayList<>();
        tourReports.forEach(r -> combined.add(toDto(r)));
        homestayReports.forEach(r -> combined.add(toDto(r)));

        combined.sort(Comparator.comparing(ReportListItemDto::getCreatedAt).reversed());
        return combined;
    }

    // ดูรายละเอียด report เดียว — เช็ค prefix id ว่าเป็น TR (tour) หรือ HR (homestay)
    @GetMapping("/{id}")
    public ReportListItemDto getOne(@PathVariable String id) {
        if (id.startsWith("TR")) {
            return toDto(tourReportService.getReportById(id));
        } else if (id.startsWith("HR")) {
            return toDto(homestayReportService.getReportById(id));
        }
        throw new IllegalArgumentException("ไม่รู้จักรูปแบบ report id นี้: " + id);
    }

    // ตัดสินใจดำเนินการ: REJECT / SUSPEND_LISTING / SUSPEND_ACCOUNT
    @PutMapping("/{id}/action")
    public ResponseEntity<?> takeAction(@PathVariable String id, @RequestParam String action) {
        try {
            if (id.startsWith("TR")) {
                return ResponseEntity.ok(toDto(tourReportService.resolveReport(id, action)));
            } else if (id.startsWith("HR")) {
                return ResponseEntity.ok(toDto(homestayReportService.resolveReport(id, action)));
            }
            return ResponseEntity.badRequest().body("ไม่รู้จักรูปแบบ report id นี้: " + id);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // นับจำนวนครั้งที่ถูกรายงาน ใช้โชว์ข้าง ๆ แต่ละรายการในลิสต์
    @GetMapping("/count")
    public long count(@RequestParam(required = false) String tourId,
            @RequestParam(required = false) Integer homestayId) {
        if (tourId != null && !tourId.isBlank()) {
            return tourReportService.countReportsForTour(tourId);
        }
        if (homestayId != null) {
            return homestayReportService.countReportsForHomestay(homestayId);
        }
        return 0;
    }

    // ดู report ทั้งหมดของ manager คนหนึ่ง (ใช้ตอนกด "ดู" ในหน้าจัดการผู้จัดการ)
    @GetMapping("/by-manager/{managerId}")
    public List<ReportListItemDto> getByManager(@PathVariable String managerId) {
        return tourReportService.getReportsByManager(managerId).stream()
                .map(ReportController::toDto)
                .collect(Collectors.toList());
    }

    // ดู report ทั้งหมดของ homestay owner คนหนึ่ง (ใช้ตอนกด "ดู" ในหน้าจัดการโฮมสเตย์)
    @GetMapping("/by-owner/{ownerId}")
    public List<ReportListItemDto> getByOwner(@PathVariable String ownerId) {
        return homestayReportService.getReportsByHomestayOwner(ownerId).stream()
                .map(ReportController::toDto)
                .collect(Collectors.toList());
    }

    // แปลง TourReport -> DTO
    private static ReportListItemDto toDto(TourReport r) {
        return new ReportListItemDto(
                r.getTourreportid(),
                r.getReason(),
                r.getDescription(),
                r.getStatus(),
                r.getCreatedAt(),
                r.getEvidenceImage(),
                "TOUR",
                r.getTour().getTourid(),
                r.getTour().getTourmname()
        );
    }

    // แปลง HomestayReport -> DTO
    private static ReportListItemDto toDto(HomestayReport r) {
        return new ReportListItemDto(
                r.getHomestayreportid(),
                r.getReason(),
                r.getDescription(),
                r.getStatus(),
                r.getCreatedAt(),
                r.getEvidenceImage(),
                "HOMESTAY",
                String.valueOf(r.getHomestay().getHomestayid()),
                r.getHomestay().getHomestayname()
        );
    }
}