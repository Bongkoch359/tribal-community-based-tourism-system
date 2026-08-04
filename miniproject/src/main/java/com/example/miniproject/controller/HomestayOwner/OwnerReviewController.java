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

        // เช็ค login เหมือนหน้าอื่นๆ ของ owner (เช่น addroom, homestays)
        Integer ownerid = (Integer) session.getAttribute("ownerid");
        if (ownerid == null) return "redirect:/owner/login";

        // กันเจ้าของคนอื่นมาดูรีวิวโฮมสเตย์ที่ไม่ใช่ของตัวเอง
        if (!homestayService.isOwnedBy(homestayId, ownerid)) {
            return "redirect:/owner/homestays";
        }

        // ดึงข้อมูลโฮมสเตย์จริง (ชื่อ, ที่อยู่, รูป) — ใช้ตัวเดียวกับหน้า viewHomestay/editHomestay
        HomestayDetailDto detail = homestayService.getHomestayDetail(homestayId);
        if (detail == null) return "redirect:/owner/homestays";

        // ดึงรีวิวทั้งหมดของโฮมสเตย์นี้ (เรียงล่าสุดก่อน อยู่ใน repository แล้ว)
        List<Review> allReviews = reviewService.getReviewsByHomestayId(homestayId);

        // ค่าเฉลี่ย + จำนวนรีวิวรวม — ใช้ค่าที่ HomestayDetailDto คำนวณมาให้แล้ว
        // (กันไม่ให้เรียกคำนวณซ้ำสองที่แล้วได้ค่าไม่ตรงกัน)
        double avgRating = detail.getAvgRating();
        long totalCount = detail.getReviewCount();

        // นับจำนวนรีวิวแยกตามดาว 1-5 (สำหรับกราฟแท่งสรุปคะแนน)
        Map<Integer, Long> starCounts = allReviews.stream()
                .filter(r -> r.getRating() != null)
                .collect(Collectors.groupingBy(Review::getRating, Collectors.counting()));

        // ถ้าเลือก filter ดาว ให้กรองเฉพาะรีวิวดาวนั้น ไม่งั้นแสดงทั้งหมด
        List<Review> filteredReviews = (star != null)
                ? allReviews.stream()
                    .filter(r -> star.equals(r.getRating()))
                    .collect(Collectors.toList())
                : allReviews;

        // ── navbar เหมือน addroom: ใช้ ownername จาก session ──
        model.addAttribute("ownername", session.getAttribute("ownername"));

        model.addAttribute("homestayId", homestayId);
        model.addAttribute("homestay", detail); // ใช้ใน HTML: homestay.homestayname / homestay.address / homestay.firstImage
        model.addAttribute("reviews", filteredReviews);
        model.addAttribute("avgRating", avgRating);
        model.addAttribute("totalCount", totalCount);
        model.addAttribute("starCounts", starCounts); // Map<Integer, Long> คีย์ 1-5
        model.addAttribute("selectedStar", star);      // null = "ทั้งหมด"

        return "Homestay/homestayReviews";
    }
}