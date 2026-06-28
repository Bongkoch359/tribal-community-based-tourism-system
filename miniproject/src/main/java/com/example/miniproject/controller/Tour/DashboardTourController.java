package com.example.miniproject.controller.Tour;

import com.example.miniproject.service.Tour.DashboardService;
import com.example.miniproject.dto.Tour.MonthlyRevenueDTO;
import com.example.miniproject.dto.Tour.DashboardStatsDTO;
import com.example.miniproject.entity.Communitymanager;
import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

@Controller
@RequestMapping("/manager")
public class DashboardTourController {

    @Autowired
    private DashboardService dashboardService;

    @GetMapping("/dashboard")
    public String dashboard(Model model, HttpSession session) {

        Communitymanager manager =
                (Communitymanager) session.getAttribute("loggedInManager");

        if (manager == null) {
            return "redirect:/manager/login";
        }

        model.addAttribute("loggedInManager", manager);

        // ─── ตรวจสอบข้อมูลธนาคาร ───
        boolean bankInfoMissing =
                manager.getBankName()      == null || manager.getBankName().isBlank()      ||
                manager.getAccountName()   == null || manager.getAccountName().isBlank()   ||
                manager.getAccountNumber() == null || manager.getAccountNumber().isBlank();
        model.addAttribute("bankInfoMissing", bankInfoMissing);

        // ─── Stats, Bookings, Posts ───
        DashboardStatsDTO stats = dashboardService.getDashboardStats();
        model.addAttribute("stats", stats);
        model.addAttribute("recentBookings", dashboardService.getRecentBookings(5));
        model.addAttribute("recentPosts",    dashboardService.getPublishedPosts());

        // ─── Monthly Revenue → JSON string สำหรับ Chart.js ───
        List<MonthlyRevenueDTO> monthly = dashboardService.getMonthlyRevenue();
        try {
            ObjectMapper mapper = new ObjectMapper();
            // labels: ["ม.ค.","ก.พ.", ...]
            List<String> labels = monthly.stream()
                    .map(MonthlyRevenueDTO::getLabel).toList();
            // data: [0, 1200, 3400, ...]
            List<Double> data = monthly.stream()
                    .map(MonthlyRevenueDTO::getRevenue).toList();
            model.addAttribute("chartLabels", mapper.writeValueAsString(labels));
            model.addAttribute("chartData",   mapper.writeValueAsString(data));
        } catch (Exception e) {
            model.addAttribute("chartLabels", "[]");
            model.addAttribute("chartData",   "[]");
        }

        return "Tour/dashboardTour";
    }
}