package com.example.miniproject.controller.Tour;

import com.example.miniproject.dto.Tour.ReviewTourView;
import com.example.miniproject.entity.Communitymanager;
import com.example.miniproject.entity.Review;
import com.example.miniproject.entity.Tour;
import com.example.miniproject.repository.Member.TourRepository;
import com.example.miniproject.service.Member.ReviewService;

import jakarta.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/manager/tours")
public class ManagerTourReviewController {

    @Autowired
    private ReviewService reviewService;

    @Autowired
    private TourRepository tourRepository;

    // ══════════════════════════════════════════════════════
    // GET /manager/tours/{tourid}/reviews
    // หน้าแสดงรีวิวทั้งหมดของทัวร์ที่เลือก
    // ══════════════════════════════════════════════════════
    @GetMapping("/{tourid}/reviews")
    public String viewTourReviews(@PathVariable String tourid,
            HttpSession session,
            Model model) {

        // ── ตรวจสอบสิทธิ์ผู้จัดการ (เหมือนหน้าอื่น ๆ ของ manager) ──
        Communitymanager loggedInManager = (Communitymanager) session.getAttribute("loggedInManager");
        if (loggedInManager == null) {
            return "redirect:/manager/login";
        }

        // ── ดึงข้อมูลทัวร์ ──
        Tour tour = tourRepository.findById(tourid)
                .orElseThrow(() -> new IllegalArgumentException("ไม่พบทัวร์นี้"));

        // ── ตรวจสิทธิ์: ต้องเป็นทัวร์ของ manager ที่ล็อกอินอยู่เท่านั้น ──
        if (tour.getCommunitymanager() == null
                || !tour.getCommunitymanager().getManagerid().equals(loggedInManager.getManagerid())) {
            throw new IllegalStateException("ไม่มีสิทธิ์ดูรีวิวของทัวร์นี้");
        }

        // ── ดึงรีวิว + สรุปคะแนน (ใช้ ReviewService ที่มีอยู่แล้ว ไม่ต้องแก้) ──
        List<Review> reviews = reviewService.getReviewsByTourId(tourid);
        double avgRating = reviewService.getAvgRatingByTourId(tourid);
        Map<Integer, Long> ratingCounts = reviewService.getRatingCountsByTourId(tourid);

        model.addAttribute("loggedInManager", loggedInManager);
        model.addAttribute("tour", tour);
        model.addAttribute("reviews", reviews);
        model.addAttribute("avgRating", avgRating);
        model.addAttribute("ratingCounts", ratingCounts);

        return "Tour/tourReviews";
    }

    // ══════════════════════════════════════════════════════
    // GET /manager/tours/reviews
    // หน้ารีวิว "รวม" ทุกทัวร์ของ manager คนที่ล็อกอินอยู่
    // ไม่แยกทีละทัวร์ แต่ละรีวิวจะบอกในตัวว่าเป็นของทัวร์ไหน
    // แล้วกรองดูได้ตามประเภททัวร์
    // ══════════════════════════════════════════════════════
    @GetMapping("/reviews")
    public String viewAllTourReviews(HttpSession session, Model model) {

        Communitymanager loggedInManager = (Communitymanager) session.getAttribute("loggedInManager");
        if (loggedInManager == null) {
            return "redirect:/manager/login";
        }

        List<ReviewTourView> reviews = reviewService.getReviewsByManagerId(loggedInManager.getManagerid());
        double avgRating = reviewService.getAvgRatingForViews(reviews);
        Map<Integer, Long> ratingCounts = reviewService.getRatingCountsForViews(reviews);
        Map<String, Long> tourTypeCounts = reviewService.getReviewCountByTourType(reviews);

        // ✅ เพิ่มบรรทัดนี้ — ดึงประเภททัวร์ "ทั้งหมด" ของ manager
        // ไม่ใช่แค่ที่มีรีวิวแล้ว
        List<String> allTourTypes = tourRepository.findDistinctTourTypeNamesByManagerId(loggedInManager.getManagerid());
        model.addAttribute("allTourTypes", allTourTypes);

        long reviewedTourCount = reviews.stream()
                .filter(v -> v.getTour() != null)
                .map(v -> v.getTour().getTourid())
                .distinct()
                .count();

        model.addAttribute("loggedInManager", loggedInManager);
        model.addAttribute("reviews", reviews);
        model.addAttribute("avgRating", avgRating);
        model.addAttribute("ratingCounts", ratingCounts);
        model.addAttribute("tourTypeCounts", tourTypeCounts);
        model.addAttribute("reviewedTourCount", reviewedTourCount);

        return "Tour/tourReviews";
    }
}