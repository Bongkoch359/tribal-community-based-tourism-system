package com.example.miniproject.controller.Member;


import com.example.miniproject.entity.Homestay;
import com.example.miniproject.entity.Review;
import com.example.miniproject.service.Homestay.HomestayService;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/homestay")
public class ViewHomestayDetailController {

    @Autowired
    private HomestayService homestayService;

   @GetMapping("/{id}")
public String homestayDetail(@PathVariable Integer id, Model model) {

    Homestay homestay = homestayService.getHomestayDetailForMember(id);
    if (homestay == null) return "redirect:/search";

    // images เป็น Base64 ให้ใส่ prefix data:image/jpeg;base64,
    String firstImage = null;
    if (homestay.getImages() != null && !homestay.getImages().isBlank()) {
        String raw = homestay.getImages().trim();
        // ถ้ายังไม่มี prefix ให้ใส่เอง
        if (raw.startsWith("data:")) {
            firstImage = raw;
        } else {
            firstImage = "data:image/jpeg;base64," + raw;
        }
    }

    List<Review> reviews = homestayService.getReviewsByHomestay(id);
    Double avgRating     = homestayService.getAvgRating(id);
    Long reviewCount     = homestayService.getReviewCount(id);

    model.addAttribute("homestay", homestay);
    model.addAttribute("firstImage", firstImage);
    model.addAttribute("rooms", homestay.getRoomtypes());
    model.addAttribute("reviews", reviews);
    model.addAttribute("avgRating", avgRating);
    model.addAttribute("reviewCount", reviewCount);

    return "Member/homestay_detail";
}

}