package com.example.miniproject.controller.Admin;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

import com.example.miniproject.service.Admin.TourReportService;
import com.example.miniproject.service.Admin.HomestayReportService;

@ControllerAdvice(basePackages = "com.example.miniproject.controller.Admin")
public class AdminGlobalModelAdvice {

    @Autowired
    private TourReportService tourReportService;

    @Autowired
    private HomestayReportService homestayReportService;

    @ModelAttribute("pendingReportCount")
    public long pendingReportCount() {
        long tourPending = tourReportService.getReportsByStatus("PENDING").size();
        long homestayPending = homestayReportService.getReportsByStatus("PENDING").size();
        return tourPending + homestayPending;
    }
}