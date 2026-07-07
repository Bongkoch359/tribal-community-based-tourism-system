package com.example.miniproject.controller.Member;

import com.example.miniproject.entity.Review;
import com.example.miniproject.entity.Tour;
import com.example.miniproject.repository.Member.ReviewRepository;
import com.example.miniproject.repository.Member.TourRepository;
import com.example.miniproject.service.Member.BookingService;
import com.example.miniproject.service.Member.TourService;
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

    @Autowired
    private TourService tourService;

@GetMapping("/tour/{id}")
public String viewTourDetail(@PathVariable String id, Model model) {

    Tour tour = tourRepository.findByIdWithBookings(id).orElse(null);

    if (tour == null) {
        return "redirect:/search";
    }

    // ✅ ใช้จุดคำนวณที่นั่งกลางจุดเดียว แทน injectBookedSeats + tour.full/remainingSeats เดิม
    // (ให้ตรงกับหน้า booking_tour.html ทุกที่)
    int availableSeats = tourService.getAvailableSeats(tour);
    String seatLevel = tourService.getSeatStatusLevel(tour, availableSeats);
    model.addAttribute("availableSeats", availableSeats);
    model.addAttribute("seatLevel", seatLevel);
    model.addAttribute("tour", tour);

    // ✅ ประเภททัวร์ — tourtype ตอนนี้เป็น object (TourType) ไม่ใช่ String แล้ว
    // ต้องดึง .getTypename() ออกมาก่อน แล้วค่อยกัน null/blank
    // fallback: ถ้าไม่มี tourtype แต่เป็นทัวร์ 1 วัน -> "ทัวร์รายวัน", นอกนั้น "ไม่ระบุประเภท"
    String typeName = (tour.getTourtype() != null) ? tour.getTourtype().getTypename() : null;
    String tourTypeDisplay = (typeName != null && !typeName.isBlank())
            ? typeName
            : (tour.getNumberOfDays() != null && tour.getNumberOfDays() == 1
                    ? "ทัวร์รายวัน" : "ไม่ระบุประเภท");
    model.addAttribute("tourTypeDisplay", tourTypeDisplay);

    // ── รีวิวของทัวร์นี้ ──
    List<Review> reviews = reviewRepository.findByTourId(id);
    model.addAttribute("reviews", reviews);
    model.addAttribute("reviewCount", reviews.size());

    Double avg = reviewRepository.avgRatingByTourId(id);
    model.addAttribute("avgRating", avg != null ? avg : 0.0);

    Map<Integer, Long> ratingCounts = reviews.stream()
            .collect(Collectors.groupingBy(Review::getRating, Collectors.counting()));
    model.addAttribute("ratingCounts", ratingCounts);

    return "Member/tour_detail";
}
}