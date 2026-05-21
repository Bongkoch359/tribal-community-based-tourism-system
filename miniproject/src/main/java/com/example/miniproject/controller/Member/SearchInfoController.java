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

    // ── เพิ่มใหม่ ──────────────────────────────────────────
    @Autowired
    private ReviewRepository reviewRepository;

    @GetMapping
    public String searchPage(
            @RequestParam(defaultValue = "")         String  keyword,
            @RequestParam(defaultValue = "")         String  date,
            @RequestParam(defaultValue = "1")        Integer numGuest,
            @RequestParam(defaultValue = "activity") String  type,
            Model model) {

        if (numGuest < 1) {
            model.addAttribute("errorMessage", "กรุณากรอกข้อมูลให้ถูกต้อง");
            numGuest = 1;
        }

        List<Activitypost> activities = searchInfoService.searchActivity(keyword);
        List<Tour>         tours      = searchInfoService.searchTour(keyword, numGuest);
        List<Homestay>     homestays  = searchInfoService.searchHomestay(keyword);

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

        // ── เพิ่มใหม่: คำนวณ rating map ─────────────────────────────────

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

        model.addAttribute("actRating",       actRating);
        model.addAttribute("actReviewCount",  actReviewCount);

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