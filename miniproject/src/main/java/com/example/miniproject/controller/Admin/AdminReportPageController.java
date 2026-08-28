package com.example.miniproject.controller.Admin;

import com.example.miniproject.entity.Report;
import com.example.miniproject.service.Admin.ReportService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Controller
public class AdminReportPageController {

    @Autowired
    private ReportService reportService;

    @GetMapping("/admin/reports")
    public String reportPage(@RequestParam(required = false) String status, Model model) {
        List<Report> reports = (status != null && !status.isBlank())
                ? reportService.getReportsByStatus(status)
                : reportService.getAllReports();

        model.addAttribute("reports", reports);
        model.addAttribute("currentStatus", status == null ? "" : status);
        return "admin/report_manage";
    }
}