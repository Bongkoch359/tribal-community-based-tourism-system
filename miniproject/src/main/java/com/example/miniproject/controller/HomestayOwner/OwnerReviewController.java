package com.example.miniproject.controller.HomestayOwner;

import com.example.miniproject.dto.Homestay.HomestayDetailDto;
import com.example.miniproject.entity.Review;
import com.example.miniproject.service.Homestay.HomestayService;
import com.example.miniproject.service.Member.ReviewService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


@Controller
public class OwnerReviewController {

    @Autowired
    private ReviewService reviewService;

    @Autowired
    private HomestayService homestayService;

    @GetMapping("/owner/homestay/{homestayId}/reviews")
    public String viewHomestayReviews(@PathVariable Integer homestayId,
                                       @RequestParam(value = "star", required = false) Integer star,
                                       HttpSession session,
                                       Model model) {

        String ownerid = (String) session.getAttribute("ownerid");
        if (ownerid == null) return "redirect:/owner/login";

        // กันเจ้าของคนอื่นมาดูรีวิวโฮมสเตย์ที่ไม่ใช่ของตัวเอง
        if (!homestayService.isOwnedBy(homestayId, ownerid)) {
            return "redirect:/owner/homestays";
        }

        HomestayDetailDto detail = homestayService.getHomestayDetail(homestayId);
        if (detail == null) return "redirect:/owner/homestays";

        // ดึงรีวิวทั้งหมดของโฮมสเตย์นี้ 
        List<Review> allReviews = reviewService.getReviewsByHomestayId(homestayId);

        // ค่าเฉลี่ย + จำนวนรีวิวรวม 
        double avgRating = detail.getAvgRating();
        long totalCount = detail.getReviewCount();

        // นับจำนวนรีวิวแยกตามดาว 1-5 
        Map<Integer, Long> starCounts = allReviews.stream()
                .filter(r -> r.getRating() != null)
                .collect(Collectors.groupingBy(Review::getRating, Collectors.counting()));

        // ถ้าเลือก filter ดาว ให้กรองเฉพาะรีวิวดาวนั้น 
        List<Review> filteredReviews = (star != null)
                ? allReviews.stream()
                    .filter(r -> star.equals(r.getRating()))
                    .collect(Collectors.toList())
                : allReviews;
        model.addAttribute("ownername", session.getAttribute("ownername"));

        model.addAttribute("homestayId", homestayId);
        model.addAttribute("homestay", detail); 
        model.addAttribute("reviews", filteredReviews);
        model.addAttribute("avgRating", avgRating);
        model.addAttribute("totalCount", totalCount);
        model.addAttribute("starCounts", starCounts); 
        model.addAttribute("selectedStar", star);      

        return "Homestay/homestayReviews";
    }
}