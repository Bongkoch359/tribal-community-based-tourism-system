package com.example.miniproject.controller.Member;

import com.example.miniproject.entity.Review;
import com.example.miniproject.entity.Tour;
import com.example.miniproject.entity.Tourschedule;
import com.example.miniproject.entity.enums.ManagerStatus;
import com.example.miniproject.repository.Member.ReviewRepository;
import com.example.miniproject.repository.Member.TourRepository;
import com.example.miniproject.repository.Tour.TourScheduleRepository;
import com.example.miniproject.service.Member.BookingService;
import com.example.miniproject.service.Member.TourService;
import com.example.miniproject.service.Tour.TourScheduleService;
import com.example.miniproject.entity.enums.ManagerStatus;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;

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

    @Autowired
    private TourScheduleService tourScheduleService;

    @GetMapping("/tour/{id}")
    public String viewTourDetail(@PathVariable String id, Model model) {

        Tour tour = tourRepository.findByIdWithBookings(id).orElse(null);
        if (tour == null)
            return "redirect:/search";
        if (tour.getCommunitymanager() == null
                || tour.getCommunitymanager().getAccountstatus() != ManagerStatus.ACTIVE) {
            return "redirect:/search";
        }

        // ดึงรอบทัวร์ทั้งหมด
        List<Tourschedule> schedules = tourScheduleService.getSchedulesByTour(id);

        // จำนวนคนจองแยกตามรอบ
        Map<String, Integer> bookedSeatsMap = tourScheduleService.getBookedSeatsMap(id);

        int maxSeatsTour = tour.getMaxSeatstour() != null
                ? tour.getMaxSeatstour()
                : 0;

        LocalDate today = LocalDate.now();

        // หาจำนวนที่นั่งว่างของเฉพาะรอบที่ยังเปิดจอง
        int availableSeats = 0;

        for (Tourschedule schedule : schedules) {

            LocalDate start = schedule.getOpendate().toLocalDate();

            boolean isPast = start.isBefore(today);
            boolean isOpen = "เปิดรับจอง".equals(schedule.getStatus());

            // ข้ามรอบที่ผ่านไปแล้ว หรือไม่ได้เปิดรับจอง
            if (isPast || !isOpen) {
                continue;
            }

            Integer booked = bookedSeatsMap.get(schedule.getScheduleid());
            int bookedCount = booked != null ? booked : 0;

            int available = Math.max(
                    maxSeatsTour - bookedCount,
                    0);

            // เอาจำนวนที่ว่างมากที่สุดจากรอบที่ยังจองได้
            availableSeats = Math.max(availableSeats, available);
        }

        String seatLevel = availableSeats <= 0
                ? "full"
                : tourService.getSeatStatusLevel(tour, availableSeats);

        model.addAttribute("availableSeats", availableSeats);
        model.addAttribute("seatLevel", seatLevel);
        model.addAttribute("tour", tour);

        model.addAttribute("schedules", schedules);
        model.addAttribute("bookedSeatsMap", bookedSeatsMap);

        List<Map<String, Object>> calendarData = new ArrayList<>();

        for (Tourschedule s : schedules) {
            LocalDate start = s.getOpendate().toLocalDate();
            LocalDate end = (s.getEnddate() != null) ? s.getEnddate().toLocalDate() : start;

            Integer booked = bookedSeatsMap.get(s.getScheduleid()); // ✅ key เป็น String ตรงกับ scheduleid
            int bookedCount = booked != null ? booked : 0;
            int avail = Math.max(maxSeatsTour - bookedCount, 0);

            boolean isPast = start.isBefore(today);
            boolean isOpen = "เปิดรับจอง".equals(s.getStatus());

            String status;
            if (isPast)
                status = "past";
            else if (!isOpen)
                status = "closed";
            else if (avail <= 0)
                status = "full";
            else if (avail <= maxSeatsTour * 0.2)
                status = "low";
            else
                status = "available";

            for (LocalDate d = start; !d.isAfter(end); d = d.plusDays(1)) {
                Map<String, Object> entry = new HashMap<>();
                entry.put("date", d.toString()); // yyyy-MM-dd
                entry.put("status", status);
                entry.put("availableSeats", avail);
                entry.put("scheduleid", s.getScheduleid()); 
                calendarData.add(entry);
            }
        }
        model.addAttribute("scheduleCalendarJson", calendarData);

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