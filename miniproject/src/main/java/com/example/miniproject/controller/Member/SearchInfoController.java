package com.example.miniproject.controller.Member;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import com.example.miniproject.entity.Activitypost;
import com.example.miniproject.entity.Homestay;
import com.example.miniproject.entity.Tour;
import com.example.miniproject.repository.Member.ReviewRepository;
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

        // ถ้ามีการส่ง managerId มา ให้เปลี่ยนแท็บเริ่มต้นเป็นหน้าทัวร์อัตโนมัติ
        if (managerId != null && !managerId.isEmpty()) {
            type = "tour";
        }

        // 1. ดึงข้อมูลของทุกแท็บ — ห่อ try-catch แยกแต่ละส่วน
        //    เพื่อกัน IllegalArgumentException จาก validation วันที่ (searchTour/searchHomestay)
        //    ไม่ให้พุ่งขึ้นมาจน controller error ทั้งหน้า (whitelabel 500)
        List<Activitypost> activities = searchInfoService.searchActivity(keyword);

        List<Homestay> homestays;
        try {
            homestays = searchInfoService.searchHomestay(keyword, numGuest, startDate, endDate);
        } catch (IllegalArgumentException e) {
            model.addAttribute("errorMessage", e.getMessage());
            homestays = new ArrayList<>();
        }

        // 2. แยกเฉพาะตรรกะของ Tour ที่มีเงื่อนไข managerId เพิ่มเติมเท่านั้น
        List<Tour> tours;
        if (managerId != null && !managerId.isEmpty()) {
            tours = searchInfoService.getToursByManagerId(managerId);
        } else {
            try {
                tours = searchInfoService.searchTour(keyword, numGuest, startDate, endDate, tourTypeId);
            } catch (IllegalArgumentException e) {
                // ถ้า homestay error ไปก่อนหน้าแล้ว ไม่ต้อง overwrite ข้อความเดิม
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

        // เปลี่ยนจาก System.out.println เป็น logger — debug log จะไม่ไปโผล่ปนกับ log จริงบน production
        // และควบคุมเปิด/ปิดได้ผ่าน log level (DEBUG) โดยไม่ต้องแก้โค้ด
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

        // 3. ส่งข้อมูลและสเตททั้งหมดเข้าสู่ Model เพื่อแสดงผลและคงค่าไว้บนฟอร์มหน้าเว็บ
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
        model.addAttribute("tourTypes",     tourService.getAllTourTypes());

        // นับจำนวนนับตามกลุ่มข้อมูลที่ดึงได้จริงของแท็บนั้นๆ
        model.addAttribute("activityCount", activities.size());
        model.addAttribute("tourCount",     tours.size());
        model.addAttribute("homestayCount", homestays.size());
        model.addAttribute("totalCount",    activities.size() + tours.size() + homestays.size());

        // ── คำนวณ rating map (ระบบจะวนลูปทำงานเฉพาะแท็บที่มีข้อมูลส่งกลับไปเท่านั้น) ──

        // Tour rating
        Map<String, String> tourRating      = new HashMap<>();
        Map<String, Long>   tourReviewCount = new HashMap<>();
        for (Tour t : tours) {
            Double avg   = reviewRepository.avgRatingByTourId(t.getTourid());
            Long   count = reviewRepository.countByTourId(t.getTourid());
            tourRating.put(t.getTourid(), avg != null ? String.format("%.1f", avg) : "-");
            tourReviewCount.put(t.getTourid(), count != null ? count : 0L);
        }

        Map<String, String> actRating      = new HashMap<>();
        Map<String, Long>   actReviewCount = new HashMap<>();

        // Homestay rating
        Map<Integer, String> hsRating      = new HashMap<>();
        Map<Integer, Long>   hsReviewCount = new HashMap<>();
        for (Homestay h : homestays) {
            Double avg   = reviewRepository.avgRatingByHomestayId(h.getHomestayid());
            Long   count = reviewRepository.countByHomestayId(h.getHomestayid());
            hsRating.put(h.getHomestayid(), avg != null ? String.format("%.1f", avg) : "-");
            hsReviewCount.put(h.getHomestayid(), count != null ? count : 0L);
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