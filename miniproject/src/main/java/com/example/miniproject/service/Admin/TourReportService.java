package com.example.miniproject.service.Admin;

import com.example.miniproject.entity.Tour;
import com.example.miniproject.entity.TourReport;
import com.example.miniproject.entity.Tourschedule;

import com.example.miniproject.repository.Admin.TourReportRepository;
import com.example.miniproject.repository.Member.TourRepository;
import com.example.miniproject.repository.Tour.TourScheduleRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class TourReportService {

    @Autowired
    private TourReportRepository tourReportRepository;

    @Autowired
    private TourRepository tourRepository;

    @Autowired
    private TourScheduleRepository tourscheduleRepository;

    @Autowired
    @Lazy // ตัดวงจร Circular Reference กับ ManagerService
    private ManagerService managerService;

    private String generateReportId() {
        Optional<TourReport> latestReport = tourReportRepository.findTopByOrderByTourreportidDesc();

        if (latestReport.isEmpty()) {
            return "TR000001";
        }

        String lastId = latestReport.get().getTourreportid();
        int lastNumber = Integer.parseInt(lastId.substring(2));
        int newNumber = lastNumber + 1;

        return String.format("TR%06d", newNumber);
    }

    // ===================== ฝั่งสมาชิก: สร้าง report =====================
    public TourReport createReport(String reason, String description, String evidenceImage, String tourId) {

        Tour tour = tourRepository.findById(tourId)
                .orElseThrow(() -> new IllegalArgumentException("ไม่พบทัวร์รายการนี้"));

        TourReport report = new TourReport();
        report.setTourreportid(generateReportId());
        report.setReason(reason);
        report.setDescription(description);
        report.setEvidenceImage(evidenceImage);
        report.setStatus("PENDING");
        report.setTour(tour);

        return tourReportRepository.save(report);
    }

    // ===================== ฝั่ง Admin: ดูรายการ =====================
    public List<TourReport> getAllReports() {
        return tourReportRepository.findAllByOrderByCreatedAtDesc();
    }

    public List<TourReport> getReportsByStatus(String status) {
        return tourReportRepository.findByStatusOrderByCreatedAtDesc(status);
    }

    public TourReport getReportById(String reportId) {
        return tourReportRepository.findById(reportId)
                .orElseThrow(() -> new IllegalArgumentException("ไม่พบ report นี้"));
    }

    public long countReportsForTour(String tourId) {
        return tourReportRepository.countByTour_Tourid(tourId);
    }

    // ===================== ฝั่ง Admin: ตัดสินใจดำเนินการ =====================
    public TourReport resolveReport(String reportId, String action) {
        TourReport report = getReportById(reportId);

        switch (action) {
            case "REJECT" -> report.setStatus("REJECTED");

            case "SUSPEND_LISTING" -> {
                report.setStatus("RESOLVED");
                closeAllSchedules(report.getTour());
            }

            case "SUSPEND_ACCOUNT" -> {
                report.setStatus("RESOLVED");
                if (report.getTour().getCommunitymanager() != null) {
                    managerService.suspend(report.getTour().getCommunitymanager().getManagerid(), report.getReason());
                }
            }

            default -> throw new IllegalArgumentException("ไม่รู้จัก action นี้: " + action);
        }

        return tourReportRepository.save(report);
    }

    private void closeAllSchedules(Tour tour) {
        List<Tourschedule> schedules = tour.getTourSchedules();
        if (schedules == null || schedules.isEmpty()) {
            return;
        }
        for (Tourschedule schedule : schedules) {
            schedule.setStatus("ปิด");
        }
        tourscheduleRepository.saveAll(schedules);
    }

    public Map<String, Long> getPendingCountByManager() {
        Map<String, Long> map = new HashMap<>();
        for (Object[] row : tourReportRepository.countPendingGroupedByManager()) {
            map.put((String) row[0], (Long) row[1]);
        }
        return map;
    }

    public List<TourReport> getReportsByManager(String managerId) {
        return tourReportRepository.findByTour_Communitymanager_ManageridOrderByCreatedAtDesc(managerId);
    }

    @org.springframework.transaction.annotation.Transactional
    public void resolveReportsForManager(String managerId) {
        List<TourReport> pendingReports = tourReportRepository
                .findByTour_Communitymanager_ManageridAndStatus(managerId, "PENDING");

        for (TourReport r : pendingReports) {
            r.setStatus("RESOLVED");
        }
        tourReportRepository.saveAll(pendingReports);
    }
}