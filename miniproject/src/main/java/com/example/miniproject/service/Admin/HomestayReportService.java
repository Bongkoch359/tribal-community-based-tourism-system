package com.example.miniproject.service.Admin;

import com.example.miniproject.entity.Homestay;
import com.example.miniproject.entity.HomestayReport;

import com.example.miniproject.repository.Admin.HomestayReportRepository;
import com.example.miniproject.repository.Homestay.HomestayRepository;
import com.example.miniproject.service.Homestay.HomestayOwnerService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class HomestayReportService {

    @Autowired
    private HomestayReportRepository homestayReportRepository;

    @Autowired
    private HomestayRepository homestayRepository;

    @Autowired
    private HomestayOwnerService homestayOwnerService;

    private String generateReportId() {
        Optional<HomestayReport> latestReport = homestayReportRepository.findTopByOrderByHomestayreportidDesc();

        if (latestReport.isEmpty()) {
            return "HR000001";
        }

        String lastId = latestReport.get().getHomestayreportid();
        int lastNumber = Integer.parseInt(lastId.substring(2));
        int newNumber = lastNumber + 1;

        return String.format("HR%06d", newNumber);
    }

    // ===================== ฝั่งสมาชิก: สร้าง report =====================
    public HomestayReport createReport(String reason, String description, String evidenceImage, Integer homestayId) {

        Homestay homestay = homestayRepository.findById(homestayId)
                .orElseThrow(() -> new IllegalArgumentException("ไม่พบที่พักหลังนี้"));

        HomestayReport report = new HomestayReport();
        report.setHomestayreportid(generateReportId());
        report.setReason(reason);
        report.setDescription(description);
        report.setEvidenceImage(evidenceImage);
        report.setStatus("PENDING");
        report.setHomestay(homestay);

        return homestayReportRepository.save(report);
    }

    // ===================== ฝั่ง Admin: ดูรายการ =====================
    public List<HomestayReport> getAllReports() {
        return homestayReportRepository.findAllByOrderByCreatedAtDesc();
    }

    public List<HomestayReport> getReportsByStatus(String status) {
        return homestayReportRepository.findByStatusOrderByCreatedAtDesc(status);
    }

    public HomestayReport getReportById(String reportId) {
        return homestayReportRepository.findById(reportId)
                .orElseThrow(() -> new IllegalArgumentException("ไม่พบ report นี้"));
    }

    public long countReportsForHomestay(int homestayId) {
        return homestayReportRepository.countByHomestay_Homestayid(homestayId);
    }

    // ===================== ฝั่ง Admin: ตัดสินใจดำเนินการ =====================
    public HomestayReport resolveReport(String reportId, String action) {
        HomestayReport report = getReportById(reportId);

        switch (action) {
            case "REJECT" -> report.setStatus("REJECTED");

            case "SUSPEND_LISTING" -> {
                report.setStatus("RESOLVED");
                report.getHomestay().setStatus("SUSPENDED");
                homestayRepository.save(report.getHomestay());
            }

            case "SUSPEND_ACCOUNT" -> {
                report.setStatus("RESOLVED");
                if (report.getHomestay().getOwner() != null) {
                    homestayOwnerService.suspend(report.getHomestay().getOwner().getOwnerid(), report.getReason());
                }
            }

            default -> throw new IllegalArgumentException("ไม่รู้จัก action นี้: " + action);
        }

        return homestayReportRepository.save(report);
    }

    public Map<String, Long> getPendingCountByHomestayOwner() {
        Map<String, Long> map = new HashMap<>();
        for (Object[] row : homestayReportRepository.countPendingGroupedByHomestayOwner()) {
            map.put((String) row[0], (Long) row[1]);
        }
        return map;
    }

    public List<HomestayReport> getReportsByHomestayOwner(String ownerId) {
        return homestayReportRepository.findByHomestay_Owner_OwneridOrderByCreatedAtDesc(ownerId);
    }

    @org.springframework.transaction.annotation.Transactional
    public void resolveReportsForHomestay(int homestayId) {
        List<HomestayReport> pendingReports = homestayReportRepository
                .findByHomestay_HomestayidAndStatus(homestayId, "PENDING");

        for (HomestayReport r : pendingReports) {
            r.setStatus("RESOLVED");
        }
        homestayReportRepository.saveAll(pendingReports);
    }
}