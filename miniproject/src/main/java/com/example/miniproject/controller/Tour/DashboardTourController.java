package com.example.miniproject.controller.Tour;

import com.example.miniproject.service.Tour.DashboardService;
import com.example.miniproject.dto.Tour.DashboardStatsDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
 
@Controller
@RequestMapping("/manager")
public class DashboardTourController {
 
    @Autowired
    private DashboardService dashboardService;
 
    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        DashboardStatsDTO stats = dashboardService.getDashboardStats();
        model.addAttribute("stats", stats);
        model.addAttribute("recentBookings", dashboardService.getRecentBookings(5));
        model.addAttribute("popularTours", dashboardService.getPopularTours(3));
        model.addAttribute("recentPosts", dashboardService.getRecentPosts(3));
        model.addAttribute("recentActivities", dashboardService.getRecentActivityLog(5));
        return "Tour/dashboardTour";
    }
}
 
