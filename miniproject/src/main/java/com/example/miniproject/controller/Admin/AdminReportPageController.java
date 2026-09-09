package com.example.miniproject.controller.Admin;

import com.example.miniproject.dto.Admin.ReportListItemDto;
import com.example.miniproject.entity.HomestayReport;
import com.example.miniproject.entity.TourReport;
import com.example.miniproject.service.Admin.HomestayReportService;
import com.example.miniproject.service.Admin.TourReportService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Controller
public class AdminReportPageController {

    @Autowired
    private TourReportService tourReportService;

    @Autowired
    private HomestayReportService homestayReportService;

    @GetMapping("/admin/reports")
    public String reportPage(@RequestParam(required = false) String status, Model model) {
        List<TourReport> tourReports = (status != null && !status.isBlank())
                ? tourReportService.getReportsByStatus(status)
                : tourReportService.getAllReports();

        List<HomestayReport> homestayReports = (status != null && !status.isBlank())
                ? homestayReportService.getReportsByStatus(status)
                : homestayReportService.getAllReports();

        List<ReportListItemDto> combined = new ArrayList<>();
        tourReports.forEach(r -> combined.add(new ReportListItemDto(
                r.getTourreportid(), r.getReason(), r.getDescription(), r.getStatus(),
                r.getCreatedAt(), r.getEvidenceImage(), "TOUR",
                r.getTour().getTourid(), r.getTour().getTourmname()
        )));
        homestayReports.forEach(r -> combined.add(new ReportListItemDto(
                r.getHomestayreportid(), r.getReason(), r.getDescription(), r.getStatus(),
                r.getCreatedAt(), r.getEvidenceImage(), "HOMESTAY",
                String.valueOf(r.getHomestay().getHomestayid()), r.getHomestay().getHomestayname()
        )));

        combined.sort(Comparator.comparing(ReportListItemDto::getCreatedAt).reversed());

        model.addAttribute("reports", combined);
        model.addAttribute("currentStatus", status == null ? "" : status);
        return "admin/report_manage";
    }
}