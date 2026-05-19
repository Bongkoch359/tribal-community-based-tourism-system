package com.example.miniproject.controller.Member;

import com.example.miniproject.entity.Review;
import com.example.miniproject.entity.Tour;
import com.example.miniproject.repository.Member.ReviewRepository;
import com.example.miniproject.repository.Member.TourRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
public class ViewTourDetailController {

    @Autowired
    private TourRepository tourRepository;

    @Autowired
    private ReviewRepository reviewRepository;

    @GetMapping("/tour/{id}")
    public String viewTourDetail(@PathVariable String id, Model model) {

        Tour tour = tourRepository.findById(id).orElse(null);

        if (tour == null) {
            return "redirect:/search";
        }

        model.addAttribute("tour", tour);

        // ── รีวิวของทัวร์นี้ (ดึงจาก DB จริง) ──
        List<Review> reviews = reviewRepository.findByTourId(id);
        model.addAttribute("reviews", reviews);

        // ── จำนวนรีวิว ──
        model.addAttribute("reviewCount", reviews.size());

        // ── คะแนนเฉลี่ย ──
        Double avg = reviewRepository.avgRatingByTourId(id);
        model.addAttribute("avgRating", avg != null ? avg : 0.0);

        // ── นับจำนวนรีวิวแต่ละดาว (Map<Integer, Long>) ──
        Map<Integer, Long> ratingCounts = reviews.stream()
                .collect(Collectors.groupingBy(Review::getRating, Collectors.counting()));
        model.addAttribute("ratingCounts", ratingCounts);

        return "Member/tour_detail";
    }
}