package com.example.miniproject.controller.Member;

import com.example.miniproject.entity.Homestay;
import com.example.miniproject.entity.Review;
import com.example.miniproject.entity.Roomtype;
import com.example.miniproject.repository.Member.BookingroomdetailRepository;
import com.example.miniproject.service.Homestay.HomestayService;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping("/homestay")
public class ViewHomestayDetailController {

    @Autowired
    private HomestayService homestayService;

    // ✅ เพิ่ม inject
    @Autowired
    private BookingroomdetailRepository bookingroomdetailRepository;

    @GetMapping("/{id}")
    public String homestayDetail(
            @PathVariable Integer id,
            @RequestParam(required = false) String startDate,  // ✅ เพิ่ม
            @RequestParam(required = false) String endDate,    // ✅ เพิ่ม
            Model model) {

        Homestay homestay = homestayService.getHomestayDetailForMember(id);
        if (homestay == null) return "redirect:/search";

        List<String> homestayImages = new ArrayList<>();
        if (homestay.getImages() != null && !homestay.getImages().isBlank()) {
            for (String img : homestay.getImages().split(",")) {
                String trimmed = img.trim();
                if (!trimmed.isEmpty()) homestayImages.add(trimmed);
            }
        }

        List<Review>  reviews     = homestayService.getReviewsByHomestay(id);
        Double        avgRating   = homestayService.getAvgRating(id);
        Long          reviewCount = homestayService.getReviewCount(id);

        // ✅ คำนวณห้องที่เหลือจริงๆ
        java.sql.Date sd, ed;
        try {
            sd = (startDate != null && !startDate.isBlank())
                    ? java.sql.Date.valueOf(startDate)
                    : java.sql.Date.valueOf(java.time.LocalDate.now());
            ed = (endDate != null && !endDate.isBlank())
                    ? java.sql.Date.valueOf(endDate)
                    : java.sql.Date.valueOf(java.time.LocalDate.now().plusDays(1));
        } catch (Exception e) {
            sd = java.sql.Date.valueOf(java.time.LocalDate.now());
            ed = java.sql.Date.valueOf(java.time.LocalDate.now().plusDays(1));
        }

        Map<String, Integer> availableRooms = new HashMap<>();
        for (Roomtype room : homestay.getRoomtypes()) {
            Integer booked = bookingroomdetailRepository
                    .countBookedRooms(room.getRoomtypeid(), sd, ed);
            int remaining = room.getTotalrooms() - (booked != null ? booked : 0);
            availableRooms.put(room.getRoomtypeid(), Math.max(0, remaining));
        }

        model.addAttribute("homestay",       homestay);
        model.addAttribute("homestayImages", homestayImages);
        model.addAttribute("rooms",          homestay.getRoomtypes());
        model.addAttribute("reviews",        reviews);
        model.addAttribute("avgRating",      avgRating);
        model.addAttribute("reviewCount",    reviewCount);
        model.addAttribute("availableRooms", availableRooms); // ✅ เพิ่ม

        return "Member/homestay_detail";
    }
}