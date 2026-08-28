package com.example.miniproject.controller.Admin;

import com.example.miniproject.service.Admin.ReportService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

@ControllerAdvice(basePackages = "com.example.miniproject.controller.Admin")
public class AdminGlobalModelAdvice {

    @Autowired
    private ReportService reportService;

    @ModelAttribute("pendingReportCount")
    public long pendingReportCount() {
        return reportService.getReportsByStatus("PENDING").size();
    }
}