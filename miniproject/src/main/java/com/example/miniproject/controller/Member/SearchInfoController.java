package com.example.miniproject.controller.Member;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import com.example.miniproject.entity.Activitypost;
import com.example.miniproject.entity.Homestay;
import com.example.miniproject.entity.Tour;
import com.example.miniproject.repository.Member.ReviewRepository;
import com.example.miniproject.service.Member.SearchInfoService;

@Controller
@RequestMapping("/search")
public class SearchInfoController {

    @Autowired
    private SearchInfoService searchInfoService;

    @Autowired
    private ReviewRepository reviewRepository;

    @GetMapping
    public String searchPage(
            @RequestParam(defaultValue = "")         String  keyword,
            @RequestParam(defaultValue = "")         String  date,
            @RequestParam(defaultValue = "1")         Integer numGuest,
            @RequestParam(defaultValue = "activity") String  type,
            @RequestParam(required = false)          String  managerId, 
            Model model) {

        if (numGuest < 1) {
            model.addAttribute("errorMessage", "กรุณากรอกข้อมูลให้ถูกต้อง");
            numGuest = 1;
        }

        // ถ้ามีการส่ง managerId มา ให้เปลี่ยนแท็บเริ่มต้นเป็นหน้าทัวร์อัตโนมัติ
        if (managerId != null && !managerId.isEmpty()) {
            type = "tour";
        }

        // ดึงข้อมูลกิจกรรมและโฮมสเตย์ปกติ
        List<Activitypost> activities = searchInfoService.searchActivity(keyword);
        List<Homestay>     homestays  = searchInfoService.searchHomestay(keyword);
        
        // ดึงข้อมูลทัวร์ตามเงื่อนไข (ดึงแยกตาม managerId หรือดึงตาม keyword ปกติ)
        List<Tour> tours;
        if (managerId != null && !managerId.isEmpty()) {
            tours = searchInfoService.getToursByManagerId(managerId); 
        } else {
            tours = searchInfoService.searchTour(keyword, numGuest);
        }

        // ส่งข้อมูลเข้าสู่ Model เพื่อแสดงผลบนหน้าเว็บ
        model.addAttribute("activities",    activities);
        model.addAttribute("tours",         tours);
        model.addAttribute("homestays",     homestays);
        model.addAttribute("keyword",       keyword);
        model.addAttribute("date",          date);
        model.addAttribute("numGuest",      numGuest);
        model.addAttribute("currentType",   type);
        model.addAttribute("activityCount", activities.size());
        model.addAttribute("tourCount",     tours.size());
        model.addAttribute("homestayCount", homestays.size());
        model.addAttribute("totalCount",    activities.size() + tours.size() + homestays.size());

        // ── คำนวณ rating map ─────────────────────────────────

        // Tour rating
        Map<String, String> tourRating      = new HashMap<>();
        Map<String, Long>   tourReviewCount = new HashMap<>();
        for (Tour t : tours) {
            Double avg   = reviewRepository.avgRatingByTourId(t.getTourid());
            Long   count = reviewRepository.countByTourId(t.getTourid());
            tourRating.put(t.getTourid(),
                    avg != null ? String.format("%.1f", avg) : "-");
            tourReviewCount.put(t.getTourid(),
                    count != null ? count : 0L);
        }

        Map<String, String> actRating      = new HashMap<>();
        Map<String, Long>   actReviewCount = new HashMap<>();

        // Homestay rating
        Map<Integer, String> hsRating      = new HashMap<>();
        Map<Integer, Long>   hsReviewCount = new HashMap<>();
        for (Homestay h : homestays) {
            Double avg   = reviewRepository.avgRatingByHomestayId(h.getHomestayid());
            Long   count = reviewRepository.countByHomestayId(h.getHomestayid());
            hsRating.put(h.getHomestayid(),
                    avg != null ? String.format("%.1f", avg) : "-");
            hsReviewCount.put(h.getHomestayid(),
                    count != null ? count : 0L);
        }

        model.addAttribute("tourRating",      tourRating);
        model.addAttribute("tourReviewCount", tourReviewCount);
        model.addAttribute("actRating",       actRating);
        model.addAttribute("actReviewCount",  actReviewCount);
        model.addAttribute("hsRating",        hsRating);
        model.addAttribute("hsReviewCount",   hsReviewCount);

        return "Member/member_search";
    }
}