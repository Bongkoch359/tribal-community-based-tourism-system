package com.example.miniproject.service.Admin;

import com.example.miniproject.entity.Homestay;
import com.example.miniproject.entity.Report;
import com.example.miniproject.entity.Tour;
import com.example.miniproject.entity.Tourschedule;
import com.example.miniproject.entity.Communitymanager;
import com.example.miniproject.entity.Homestayowner;
import com.example.miniproject.entity.enums.ManagerStatus;

import com.example.miniproject.repository.Admin.ReportRepository;
// TODO: แก้ path ให้ตรงกับตำแหน่งจริงของ repository เหล่านี้ในโปรเจกต์เธอ
import com.example.miniproject.repository.Member.TourRepository;
import com.example.miniproject.repository.Homestay.HomestayRepository;
import com.example.miniproject.repository.Admin.CommunitymanagerRepository;
import com.example.miniproject.repository.Homestay.HomestayOwnerRepository;
import com.example.miniproject.repository.Tour.TourScheduleRepository;
import com.example.miniproject.service.Homestay.HomestayOwnerService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

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
private ManagerService managerService;              // package service.Admin

@Autowired
private HomestayOwnerService homestayOwnerService;   // package service.Homestay — import ให้ตรง

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
    // เรียกจากปุ่ม 🚩 ที่หน้า tour_detail (ส่ง tourId) หรือหน้า homestay_detail (ส่ง homestayId)
    // ต้องส่งมาแค่ตัวเดียว ห้ามส่งทั้งคู่หรือไม่ส่งเลย
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
    // action: "REJECT" | "SUSPEND_LISTING" | "SUSPEND_ACCOUNT"
    public Report resolveReport(String reportId, String action) {
        Report report = getReportById(reportId);

        switch (action) {
            case "REJECT" -> report.setStatus("REJECTED");

            case "SUSPEND_LISTING" -> {
                report.setStatus("RESOLVED");
                if (report.getTour() != null) {
                    // Tour ไม่มี field สถานะของตัวเอง — "ระงับทัวร์" ทำโดยปิดรับจองทุกรอบ
                    // (Tourschedule.status) แทน ใช้ค่าเดียวกับที่ระบบใช้อยู่แล้วคือ "ปิด"
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

    // ปิดรับจองทุกรอบของทัวร์นี้ (ใช้ตอน SUSPEND_LISTING)
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
}