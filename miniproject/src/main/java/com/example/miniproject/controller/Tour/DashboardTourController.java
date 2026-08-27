package com.example.miniproject.controller.Tour;

import com.example.miniproject.service.Tour.DashboardService;
import com.example.miniproject.dto.Tour.DashboardStatsDTO;
import com.example.miniproject.entity.Communitymanager;

import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.YearMonth;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/manager")
public class DashboardTourController {

    @Autowired
    private DashboardService dashboardService;

    @GetMapping("/dashboard")
    public String dashboard(
            @RequestParam(value = "range", required = false, defaultValue = "6") String range,
            @RequestParam(value = "startMonth", required = false) String startMonthParam,
            @RequestParam(value = "endMonth", required = false) String endMonthParam,
            Model model, HttpSession session) {

        Communitymanager manager = (Communitymanager) session.getAttribute("loggedInManager");

        if (manager == null) {
            return "redirect:/manager/login";
        }

        model.addAttribute("loggedInManager", manager);

        // ─── ตรวจสอบข้อมูลธนาคาร ───
        boolean bankInfoMissing = manager.getBankName() == null || manager.getBankName().isBlank() ||
                manager.getAccountName() == null || manager.getAccountName().isBlank() ||
                manager.getAccountNumber() == null || manager.getAccountNumber().isBlank();
        model.addAttribute("bankInfoMissing", bankInfoMissing);

        // ─── ตรวจสอบลายเซ็น ───
        boolean signatureMissing = manager.getSignatureImageUrl() == null || manager.getSignatureImageUrl().isBlank();
        model.addAttribute("signatureMissing", signatureMissing);

        // ─── Stats + จำนวนโพสต์ทั้งหมด ───
        DashboardStatsDTO stats = dashboardService.getDashboardStats();
        model.addAttribute("stats", stats);
        model.addAttribute("totalPosts", dashboardService.getTotalPostCount());

        // ─── คำนวณช่วงเดือนที่จะแสดงกราฟ (preset 3/6/12 หรือ custom) ───
        YearMonth currentYM = YearMonth.now();
        YearMonth startYM;
        YearMonth endYM;

        if ("custom".equals(range) && startMonthParam != null && !startMonthParam.isBlank()
                && endMonthParam != null && !endMonthParam.isBlank()) {
            try {
                startYM = YearMonth.parse(startMonthParam);
                endYM = YearMonth.parse(endMonthParam);
            } catch (Exception e) {
                startYM = currentYM.minusMonths(5);
                endYM = currentYM;
                range = "6";
            }
        } else {
            int months;
            try {
                months = Integer.parseInt(range);
            } catch (NumberFormatException e) {
                months = 6;
                range = "6";
            }
            endYM = currentYM;
            startYM = currentYM.minusMonths(months - 1);
        }

        if (startYM.isAfter(endYM)) {
            YearMonth tmp = startYM;
            startYM = endYM;
            endYM = tmp;
        }
        if (ChronoUnit.MONTHS.between(startYM, endYM) > 23) {
            endYM = startYM.plusMonths(23);
        }

        model.addAttribute("selectedRange", range);
        model.addAttribute("selectedStartMonth", startYM.toString());
        model.addAttribute("selectedEndMonth", endYM.toString());

        // ─── กราฟรายได้ / ยอดจองทัวร์รายเดือน ───
        List<Map<String, Object>> revenueTrend = dashboardService.getTourRevenueTrend(startYM, endYM);
        List<Map<String, Object>> bookingCountTrend = dashboardService.getTourBookingCountTrend(startYM, endYM);
        model.addAttribute("revenueTrend", revenueTrend);
        model.addAttribute("bookingCountTrend", bookingCountTrend);

        // ─── ทัวร์ยอดจองสูงสุด ───
        List<Map<String, Object>> topTours = dashboardService.getTopToursByBookingCount();
        model.addAttribute("topTours", topTours);

        return "Tour/dashboardTour";
    }
}