package com.example.miniproject.controller.Member;

import com.example.miniproject.entity.Booking;
import com.example.miniproject.entity.Bookingroomdetail;
import com.example.miniproject.entity.Homestay;
import com.example.miniproject.entity.Member;
import com.example.miniproject.entity.Review;
import com.example.miniproject.entity.Roomtype;
import com.example.miniproject.repository.Member.BookingRepository;
import com.example.miniproject.repository.Member.BookingroomdetailRepository;
import com.example.miniproject.service.Homestay.HomestayService;

import jakarta.servlet.http.HttpSession;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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

    @Autowired
    private BookingroomdetailRepository bookingroomdetailRepository;

    @Autowired
    private BookingRepository bookingRepository;

    @GetMapping("/{id}")
    public String homestayDetail(
            @PathVariable Integer id,
            // ✅ เปลี่ยนชื่อ param ให้ตรงกับ query string ที่หน้า search ส่งมา (checkin/checkout/guest)
            @RequestParam(required = false) String checkin,
            @RequestParam(required = false) String checkout,
            @RequestParam(required = false) Integer guest,
            HttpSession session,
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

        // ✅ แปลงเป็น LocalDate ก่อน เพื่อคำนวณ nights ได้ตรงๆ แล้วค่อยแปลงเป็น java.sql.Date สำหรับ query
        LocalDate checkinDate, checkoutDate;
        try {
            checkinDate = (checkin != null && !checkin.isBlank())
                    ? LocalDate.parse(checkin)
                    : LocalDate.now();
            checkoutDate = (checkout != null && !checkout.isBlank())
                    ? LocalDate.parse(checkout)
                    : checkinDate.plusDays(1);

            // กันกรณี checkout <= checkin ให้ fallback เป็น checkin + 1 คืน
            if (!checkoutDate.isAfter(checkinDate)) {
                checkoutDate = checkinDate.plusDays(1);
            }
        } catch (Exception e) {
            checkinDate  = LocalDate.now();
            checkoutDate = checkinDate.plusDays(1);
        }

        java.sql.Date sd = java.sql.Date.valueOf(checkinDate);
        java.sql.Date ed = java.sql.Date.valueOf(checkoutDate);
        long nights = ChronoUnit.DAYS.between(checkinDate, checkoutDate);

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
        model.addAttribute("availableRooms", availableRooms);

        // ✅ ส่ง attribute ที่หน้า view ต้องใช้จริง (ตรงชื่อกับ .html)
        model.addAttribute("checkinParam",  sd);
        model.addAttribute("checkoutParam", ed);
        model.addAttribute("guestParam",    guest != null ? guest : 1);
        model.addAttribute("nights",        nights);

        // ส่วนที่เพิ่ม: การจองที่เข้าพักเสร็จแล้วแต่ยังไม่ได้รีวิว
        Member member = (Member) session.getAttribute("loggedInMember");
        if (member != null) {
            List<Booking> pendingReviews = bookingRepository
                    .findCompletedBookingsWithoutReview(member.getMemberid(), id);

            Map<String, java.sql.Date> checkoutDateMap = pendingReviews.stream()
                .collect(Collectors.toMap(
                    Booking::getBookingid,
                    b -> b.getRoomDetails().stream()
                            .filter(rd -> rd.getRoomtype().getHomestay().getHomestayid() == id)
                            .map(Bookingroomdetail::getCheckoutdate)
                            .max(java.sql.Date::compareTo)
                            .orElse(null)
                ));

            model.addAttribute("pendingReviewBookings", pendingReviews);
            model.addAttribute("checkoutDateMap", checkoutDateMap);
        }

        return "Member/homestay_detail";
    }
}