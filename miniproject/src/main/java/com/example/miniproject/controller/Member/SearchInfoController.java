package com.example.miniproject.controller.Member;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import com.example.miniproject.model.TribeCode;

import com.example.miniproject.entity.Activitypost;
import com.example.miniproject.entity.Homestay;
import com.example.miniproject.entity.Tour;
import com.example.miniproject.repository.Member.ReviewRepository;
import com.example.miniproject.service.Member.BookingService;
import com.example.miniproject.service.Member.SearchInfoService;
import com.example.miniproject.service.Member.TourService;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/search")
public class SearchInfoController {

    private static final Logger log = LoggerFactory.getLogger(SearchInfoController.class);

    @Autowired
    private SearchInfoService searchInfoService;

    @Autowired
    private ReviewRepository reviewRepository;

    @Autowired
    private TourService tourService;

    @Autowired
    private BookingService bookingService;

    @GetMapping
    public String searchPage(
            @RequestParam(defaultValue = "")         String  keyword,
            @RequestParam(defaultValue = "")         String  date,
            @RequestParam(required = false)          String  startDate,
            @RequestParam(required = false)          String  endDate,
            @RequestParam(defaultValue = "1")         Integer numGuest,
            @RequestParam(defaultValue = "activity") String  type,
            @RequestParam(required = false)          String  managerId,
            @RequestParam(required = false)          String  tourTypeId,
            @RequestParam(required = false)          String tribeName, 
             @RequestParam(required = false)          Integer tribeId, 
            Model model) {

        if (numGuest < 1) {
            model.addAttribute("errorMessage", "กรุณากรอกข้อมูลให้ถูกต้อง");
            numGuest = 1;
        }

        if (managerId != null && !managerId.isEmpty()) {
            type = "tour";
        }

        List<Activitypost> activities = searchInfoService.searchActivity(keyword);

        List<Homestay> homestays;
        try {
            homestays = searchInfoService.searchHomestay(keyword, numGuest, startDate, endDate);
        } catch (IllegalArgumentException e) {
            model.addAttribute("errorMessage", e.getMessage());
            homestays = new ArrayList<>();
        }

        List<Tour> tours;
        if (managerId != null && !managerId.isEmpty()) {
            tours = searchInfoService.getToursByManagerId(managerId);
        } else {
            try {
                tours = searchInfoService.searchTour(keyword, numGuest, startDate, endDate, tourTypeId);
            } catch (IllegalArgumentException e) {
                if (model.getAttribute("errorMessage") == null) {
                    model.addAttribute("errorMessage", e.getMessage());
                }
                tours = new ArrayList<>();
            }
        }

        tourService.injectBookedSeats(tours);
        if (tribeId != null) {
            tours = tours.stream()
                    .filter(t -> t.getTribeid() != null && t.getTribeid().equals(tribeId))
                    .collect(Collectors.toList());
        }

        // ── ดึงทัวร์/โฮมสเตย์ยอดนิยม (rating สูงสุด) สำหรับ featured section ──
        List<Tour> featuredTours = searchInfoService.getTopRatedTours(4);
        List<Homestay> featuredHomestays = searchInfoService.getTopRatedHomestays(4);
        model.addAttribute("featuredTours", featuredTours);
        model.addAttribute("featuredHomestays", featuredHomestays);

        if (log.isDebugEnabled()) {
            log.debug("========== DEBUG SEARCH ==========");
            log.debug("keyword   : {}", keyword);
            log.debug("type      : {}", type);
            log.debug("numGuest  : {}", numGuest);
            log.debug("startDate : {}", startDate);
            log.debug("endDate   : {}", endDate);
            log.debug("tours     : {} รายการ", tours.size());
            tours.forEach(t -> log.debug("  -> {} | {}", t.getTourid(), t.getTourmname()));
            log.debug("===================================");
        }

        model.addAttribute("activities",    activities);
        model.addAttribute("tours",         tours);
        model.addAttribute("homestays",     homestays);
        model.addAttribute("keyword",       keyword);
        model.addAttribute("date",          date);
        model.addAttribute("startDate",     startDate);
        model.addAttribute("endDate",       endDate);
        model.addAttribute("numGuest",      numGuest);
        model.addAttribute("currentType",   type);
        model.addAttribute("tourTypeId",    tourTypeId);
        model.addAttribute("tribeId",       tribeId); 
        model.addAttribute("tribeName", tribeName);
        model.addAttribute("tribeOptions", TribeCode.values());
        model.addAttribute("heroTribeCount",    searchInfoService.countDistinctTribes());
        model.addAttribute("heroActivityCount", searchInfoService.countAllActivities());
        model.addAttribute("tourTypes",     tourService.getAllTourTypes());

        model.addAttribute("activityCount", activities.size());
        model.addAttribute("tourCount",     tours.size());
        model.addAttribute("homestayCount", homestays.size());
        model.addAttribute("totalCount",    activities.size() + tours.size() + homestays.size());

        // ── Tour rating (union กับ featuredTours เพื่อให้ template หา key เจอทั้ง 2 ส่วน) ──
        Set<String> allTourIds = tours.stream()
                .map(Tour::getTourid)
                .collect(Collectors.toCollection(LinkedHashSet::new));
        featuredTours.forEach(t -> allTourIds.add(t.getTourid()));

        Map<String, String> tourRating      = new HashMap<>();
        Map<String, Long>   tourReviewCount = new HashMap<>();
        for (String tid : allTourIds) {
            Double avg   = reviewRepository.avgRatingByTourId(tid);
            Long   count = reviewRepository.countByTourId(tid);
            tourRating.put(tid, avg != null ? String.format("%.1f", avg) : "-");
            tourReviewCount.put(tid, count != null ? count : 0L);
        }

        Map<String, String> actRating      = new HashMap<>();
        Map<String, Long>   actReviewCount = new HashMap<>();

        if (startDate != null && !startDate.isBlank()
                && endDate != null && !endDate.isBlank()
                && !homestays.isEmpty()) {
            try {
                java.time.LocalDate checkin  = java.time.LocalDate.parse(startDate);
                java.time.LocalDate checkout = java.time.LocalDate.parse(endDate);

                List<Integer> homestayIds = homestays.stream()
                        .map(Homestay::getHomestayid)
                        .collect(Collectors.toList());

                Map<Integer, Boolean> hsAvailability =
                        bookingService.checkAvailabilityForHomestays(homestayIds, checkin, checkout);

                model.addAttribute("hsAvailability", hsAvailability);
            } catch (java.time.format.DateTimeParseException e) {
                log.debug("Invalid date format for availability check: {}", e.getMessage());
            }
        }

        // ── Homestay rating (union กับ featuredHomestays) ──
        Set<Integer> allHsIds = homestays.stream()
                .map(Homestay::getHomestayid)
                .collect(Collectors.toCollection(LinkedHashSet::new));
        featuredHomestays.forEach(h -> allHsIds.add(h.getHomestayid()));

        Map<Integer, String> hsRating      = new HashMap<>();
        Map<Integer, Long>   hsReviewCount = new HashMap<>();
        for (Integer hid : allHsIds) {
            Double avg   = reviewRepository.avgRatingByHomestayId(hid);
            Long   count = reviewRepository.countByHomestayId(hid);
            hsRating.put(hid, avg != null ? String.format("%.1f", avg) : "-");
            hsReviewCount.put(hid, count != null ? count : 0L);
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