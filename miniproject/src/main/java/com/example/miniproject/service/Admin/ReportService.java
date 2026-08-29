package com.example.miniproject.service.Admin;

import com.example.miniproject.entity.Homestay;
import com.example.miniproject.entity.Report;
import com.example.miniproject.entity.Tour;
import com.example.miniproject.entity.Tourschedule;

import com.example.miniproject.repository.Admin.ReportRepository;
import com.example.miniproject.repository.Member.TourRepository;
import com.example.miniproject.repository.Homestay.HomestayRepository;
import com.example.miniproject.repository.Admin.CommunitymanagerRepository;
import com.example.miniproject.repository.Homestay.HomestayOwnerRepository;
import com.example.miniproject.repository.Tour.TourScheduleRepository;
import com.example.miniproject.service.Homestay.HomestayOwnerService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy; // <-- เพิ่ม Import ตัวนี้
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.List;
import java.util.Optional;

@Service
public class ReportService {

    @Autowired
    private ReportRepository reportRepository;

    @Autowired
    private TourRepository tourRepository;

    @Autowired
    private HomestayRepository homestayRepository;

    @Autowired
    private CommunitymanagerRepository communitymanagerRepository;

    @Autowired
    private HomestayOwnerRepository homestayownerRepository;

    @Autowired
    private TourScheduleRepository tourscheduleRepository;

    @Autowired
    @Lazy // <-- เพิ่ม @Lazy ตรงนี้ เพื่อตัดวงจร Circular Reference กับ ManagerService
    private ManagerService managerService;            

    @Autowired
    private HomestayOwnerService homestayOwnerService;   

    private String generateReportId() {
        Optional<Report> latestReport = reportRepository.findTopByOrderByReportidDesc();

        if (latestReport.isEmpty()) {
            return "RP000001";
        }

        String lastId = latestReport.get().getReportid();
        int lastNumber = Integer.parseInt(lastId.substring(2));
        int newNumber = lastNumber + 1;

        return String.format("RP%06d", newNumber);
    }

    // ===================== ฝั่งสมาชิก: สร้าง report =====================
    public Report createReport(String reason, String description, String evidenceImage,
                                String tourId, Integer homestayId) {

        boolean hasTour = tourId != null && !tourId.isBlank();
        boolean hasHomestay = homestayId != null;

        if (hasTour == hasHomestay) {
            throw new IllegalArgumentException("ต้องระบุ tourId หรือ homestayId อย่างใดอย่างหนึ่งเท่านั้น");
        }
        
        Report report = new Report();
        report.setReportid(generateReportId());
        report.setReason(reason);
        report.setDescription(description);
        report.setEvidenceImage(evidenceImage);
        report.setStatus("PENDING");

        if (hasTour) {
            Tour tour = tourRepository.findById(tourId)
                    .orElseThrow(() -> new IllegalArgumentException("ไม่พบทัวร์รายการนี้"));
            report.setTour(tour);
        } else {
            Homestay homestay = homestayRepository.findById(homestayId)
                    .orElseThrow(() -> new IllegalArgumentException("ไม่พบที่พักหลังนี้"));
            report.setHomestay(homestay);
        }

        return reportRepository.save(report);
    }

    // ===================== ฝั่ง Admin: ดูรายการ =====================
    public List<Report> getAllReports() {
        return reportRepository.findAllByOrderByCreatedAtDesc();
    }

    public List<Report> getReportsByStatus(String status) {
        return reportRepository.findByStatusOrderByCreatedAtDesc(status);
    }

    public Report getReportById(String reportId) {
        return reportRepository.findById(reportId)
                .orElseThrow(() -> new IllegalArgumentException("ไม่พบ report นี้"));
    }

    public long countReportsForTour(String tourId) {
        return reportRepository.countByTour_Tourid(tourId);
    }

    public long countReportsForHomestay(int homestayId) {
        return reportRepository.countByHomestay_Homestayid(homestayId);
    }

    // ===================== ฝั่ง Admin: ตัดสินใจดำเนินการ =====================
    public Report resolveReport(String reportId, String action) {
        Report report = getReportById(reportId);

        switch (action) {
            case "REJECT" -> report.setStatus("REJECTED");

            case "SUSPEND_LISTING" -> {
                report.setStatus("RESOLVED");
                if (report.getTour() != null) {
                    closeAllSchedules(report.getTour());
                }
                if (report.getHomestay() != null) {
                    report.getHomestay().setStatus("SUSPENDED");
                    homestayRepository.save(report.getHomestay());
                }
            }

            case "SUSPEND_ACCOUNT" -> {
                report.setStatus("RESOLVED");
                if (report.getTour() != null && report.getTour().getCommunitymanager() != null) {
                    managerService.suspend(report.getTour().getCommunitymanager().getManagerid(), report.getReason());
                }
                if (report.getHomestay() != null && report.getHomestay().getOwner() != null) {
                    homestayOwnerService.suspend(report.getHomestay().getOwner().getOwnerid(), report.getReason());
                }
            }

            default -> throw new IllegalArgumentException("ไม่รู้จัก action นี้: " + action);
        }

        return reportRepository.save(report);
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
        for (Object[] row : reportRepository.countPendingGroupedByManager()) {
            map.put((String) row[0], (Long) row[1]);
        }
        return map;
    }

    public Map<String, Long> getPendingCountByHomestayOwner() {
        Map<String, Long> map = new HashMap<>();
        for (Object[] row : reportRepository.countPendingGroupedByHomestayOwner()) {
            map.put((String) row[0], (Long) row[1]);
        }
        return map;
    }

    public List<Report> getReportsByManager(String managerId) {
        return reportRepository.findByTour_Communitymanager_ManageridOrderByCreatedAtDesc(managerId);
    }

    public List<Report> getReportsByHomestayOwner(String ownerId) {
        return reportRepository.findByHomestay_Owner_OwneridOrderByCreatedAtDesc(ownerId);
    }

    @org.springframework.transaction.annotation.Transactional
    public void resolveReportsForHomestay(int homestayId) {
        List<Report> pendingReports = reportRepository
                .findByHomestay_HomestayidAndStatus(homestayId, "PENDING");

        for (Report r : pendingReports) {
            r.setStatus("RESOLVED");
        }
        reportRepository.saveAll(pendingReports);
    }

    @org.springframework.transaction.annotation.Transactional
    public void resolveReportsForManager(String managerId) {
        List<Report> pendingReports = reportRepository
                .findByTour_Communitymanager_ManageridAndStatus(managerId, "PENDING");

        for (Report r : pendingReports) {
            r.setStatus("RESOLVED");
        }
        reportRepository.saveAll(pendingReports);
    }
}