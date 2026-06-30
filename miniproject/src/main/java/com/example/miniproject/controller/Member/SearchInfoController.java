package com.example.miniproject.controller.Member;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

@Controller
@RequestMapping("/search")
public class SearchInfoController {

    @Autowired
    private SearchInfoService searchInfoService;

    @Autowired
    private ReviewRepository reviewRepository;

    @Autowired
    private TourService tourService; // ← เพิ่มตรงนี้ (ไม่ใช่ searchInfoService)

    @GetMapping
    public String searchPage(
            @RequestParam(defaultValue = "")         String  keyword,
            @RequestParam(defaultValue = "")         String  date,
            @RequestParam(required = false)          String  startDate,  
            @RequestParam(required = false)          String  endDate,
            @RequestParam(defaultValue = "1")         Integer numGuest,
            @RequestParam(defaultValue = "activity") String  type,
            @RequestParam(required = false)          String  managerId, 
            Model model) {

        if (numGuest < 1) {
            model.addAttribute("errorMessage", "กรุณากรอกข้อมูลให้ถูกต้อง");
            numGuest = 1;
        }

        // ถ้ามีการส่ง managerId มา ให้เปลี่ยนแท็บเริ่มต้นเป็นหน้าทัวร์อัตโนมัติ
        if (managerId != null && !managerId.isEmpty()) {
            type = "tour";
        }

       
        // 1. สั่งดึงข้อมูลของทุกแท็บมารอไว้พร้อมกันเลย (ไม่ต้องใช้ if-else บีบแล้ว)
        List<Activitypost> activities = searchInfoService.searchActivity(keyword);
        List<Tour>         tours;
        List<Homestay>     homestays  = searchInfoService.searchHomestay(keyword, numGuest, startDate, endDate);

        // 2. แยกเฉพาะตรรกะของ Tour ที่มีเงื่อนไข managerId เพิ่มเติมเท่านั้น
        if (managerId != null && !managerId.isEmpty()) {
            tours = searchInfoService.getToursByManagerId(managerId); 
        } else {
            tours = searchInfoService.searchTour(keyword, numGuest, startDate, endDate);
        }

        tourService.injectBookedSeats(tours); // ← เพิ่มบรรทัดนี้

    // ===== DEBUG LOG เพิ่มตรงนี้ =====
    System.out.println("========== DEBUG SEARCH ==========");
    System.out.println("keyword   : " + keyword);
    System.out.println("type      : " + type);
    System.out.println("numGuest  : " + numGuest);
    System.out.println("startDate : " + startDate);
    System.out.println("endDate   : " + endDate);
    System.out.println("tours     : " + tours.size() + " รายการ");
    tours.forEach(t -> System.out.println("  -> " + t.getTourid() 
        + " | " + t.getTourmname() 
        + " | status=" + t.getStatus()));
    System.out.println("===================================");
    // ===================================
    

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