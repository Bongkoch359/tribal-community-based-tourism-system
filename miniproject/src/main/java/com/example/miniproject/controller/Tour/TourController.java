package com.example.miniproject.controller.Tour;

import com.example.miniproject.entity.Communitymanager;
import com.example.miniproject.entity.Review;
import com.example.miniproject.entity.Tour;
import com.example.miniproject.service.Member.TourService;
import com.example.miniproject.service.Member.ReviewService;
import jakarta.servlet.http.HttpSession;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/manager/tours")
public class TourController {

    @Autowired
    private TourService tourService;

    @Autowired
    private ReviewService reviewService;

    // ─── แสดงรายการทัวร์ทั้งหมดของ manager ───────────────

    @GetMapping
    public String listTours(HttpSession session, Model model) {
        Communitymanager manager = (Communitymanager) session.getAttribute("loggedInManager");
        if (manager == null) return "redirect:/login";

        model.addAttribute("tours", tourService.getAllTours());
        model.addAttribute("loggedInManager", manager);
        return "Tour/listTour";  // templates/manager/tours/list.html
    }

    // ─── แสดงฟอร์มเพิ่มทัวร์ ─────────────────────────────

    @GetMapping("/create")
    public String showCreateForm(HttpSession session, Model model) {
        Communitymanager manager = (Communitymanager) session.getAttribute("loggedInManager");
        if (manager == null) return "redirect:/login";

        model.addAttribute("tour", new Tour());
        model.addAttribute("loggedInManager", manager);
        return "Tour/addTour"; 
    }

    // ─── บันทึกทัวร์ใหม่ ──────────────────────────────────

    @PostMapping("/create")
    public String createTour(
            @RequestParam("tourmname")    String tourmname,
            @RequestParam("status")       String status,
            @RequestParam("tourdetail")   String tourdetail,
            @RequestParam(value = "conditiontour", required = false) String conditiontour,
            @RequestParam("minSeatstour") Integer minSeatstour,
            @RequestParam("maxSeatstour") Integer maxSeatstour,
            @RequestParam("adultprice")   Double adultprice,
            @RequestParam("childprice")   Double childprice,
            @RequestParam(value = "images", required = false) String images,
            HttpSession session,
            Model model
    ) {
        Communitymanager manager = (Communitymanager) session.getAttribute("loggedInManager");
        if (manager == null) return "redirect:/login";

        // ─── Server-side validation ───
        if (tourmname == null || tourmname.isBlank()) {
            model.addAttribute("errorMessage", "กรุณากรอกชื่อทัวร์");
            model.addAttribute("loggedInManager", manager);
            return "manager/tours/add-tour";
        }
        if (tourdetail == null || tourdetail.isBlank()) {
            model.addAttribute("errorMessage", "กรุณากรอกรายละเอียดทัวร์");
            model.addAttribute("loggedInManager", manager);
            return "manager/tours/add-tour";
        }
        if (minSeatstour == null || maxSeatstour == null) {
            model.addAttribute("errorMessage", "กรุณาระบุจำนวนที่นั่ง");
            model.addAttribute("loggedInManager", manager);
            return "manager/tours/add-tour";
        }
        if (minSeatstour > maxSeatstour) {
            model.addAttribute("errorMessage", "จำนวนที่นั่งขั้นต่ำต้องน้อยกว่าสูงสุด");
            model.addAttribute("loggedInManager", manager);
            return "manager/tours/add-tour";
        }
        if (adultprice == null || childprice == null) {
            model.addAttribute("errorMessage", "กรุณาระบุราคาทัวร์");
            model.addAttribute("loggedInManager", manager);
            return "manager/tours/add-tour";
        }

        // ─── สร้าง entity แล้วบันทึก ───
        Tour tour = new Tour();
        tour.setTourmname(tourmname.trim());
        tour.setStatus(status);
        tour.setTourdetail(tourdetail.trim());
        tour.setConditiontour(conditiontour != null ? conditiontour.trim() : null);
        tour.setMinSeatstour(minSeatstour);
        tour.setMaxSeatstour(maxSeatstour);
        tour.setAdultprice(adultprice);
        tour.setChildprice(childprice);
        tour.setImages(images != null && !images.isBlank() ? images : null);

        try {
            tourService.createTour(tour, manager);
            return "redirect:/manager/tours?success=created";
        } catch (Exception e) {
            model.addAttribute("errorMessage", "เกิดข้อผิดพลาด: " + e.getMessage());
            model.addAttribute("loggedInManager", manager);
            return "manager/tours/add-tour";
        }
    }

     // ─── แสดงรายละเอียดทัวร์ ─────────────────────────────

@GetMapping("/{tourid}")
public String tourDetail(@PathVariable("tourid") String tourid, HttpSession session, Model model) {
    Communitymanager manager = (Communitymanager) session.getAttribute("loggedInManager");
    if (manager == null) return "redirect:/login";

    Tour tour = tourService.getTourByIdAny(tourid).orElse(null);
    if (tour == null) return "redirect:/manager/tours?error=notfound";

    model.addAttribute("tour", tour);
    model.addAttribute("loggedInManager", manager); 
    return "Tour/tourDetail"; 
}

    // ─── แสดงฟอร์มแก้ไขทัวร์ ─────────────────────────────
 
    @GetMapping("/{tourid}/edit")
    public String showEditForm(@PathVariable("tourid") String tourid,
                               HttpSession session, Model model) {
        Communitymanager manager = (Communitymanager) session.getAttribute("loggedInManager");
        if (manager == null) return "redirect:/login";
 
        Tour tour = tourService.getTourByIdAny(tourid).orElse(null);
        if (tour == null) return "redirect:/manager/tours?error=notfound";
 
        // ตรวจสิทธิ์: manager ต้องเป็นเจ้าของทัวร์
        if (!tour.getCommunitymanager().getManagerid().equals(manager.getManagerid())) {
            return "redirect:/manager/tours?error=forbidden";
        }
 
        model.addAttribute("tour", tour);
        model.addAttribute("loggedInManager", manager);
        return "Tour/editTour";
    }
 
    // ─── บันทึกการแก้ไขทัวร์ ─────────────────────────────
 
    @PostMapping("/{tourid}/edit")
    public String updateTour(
            @PathVariable("tourid") String tourid,
            @RequestParam("tourmname")    String tourmname,
            @RequestParam("status")       String status,
            @RequestParam("tourdetail")   String tourdetail,
            @RequestParam(value = "conditiontour", required = false) String conditiontour,
            @RequestParam("minSeatstour") Integer minSeatstour,
            @RequestParam("maxSeatstour") Integer maxSeatstour,
            @RequestParam("adultprice")   Double adultprice,
            @RequestParam("childprice")   Double childprice,
            @RequestParam(value = "images", required = false) String images,
            HttpSession session,
            Model model,
            RedirectAttributes redirectAttributes
    ) {
        Communitymanager manager = (Communitymanager) session.getAttribute("loggedInManager");
        if (manager == null) return "redirect:/login";
 
        // โหลดทัวร์เดิมมาแสดงหากเกิด error
        Tour existing = tourService.getTourByIdAny(tourid).orElse(null);
        if (existing == null) return "redirect:/manager/tours?error=notfound";
 
        // ตรวจสิทธิ์
        if (!existing.getCommunitymanager().getManagerid().equals(manager.getManagerid())) {
            return "redirect:/manager/tours?error=forbidden";
        }
 
        // ─── Server-side validation ───
        if (tourmname == null || tourmname.isBlank()) {
            model.addAttribute("errorMessage", "กรุณากรอกชื่อทัวร์");
            model.addAttribute("tour", existing);
            model.addAttribute("loggedInManager", manager);
            return "Tour/editTour";
        }
        if (minSeatstour != null && maxSeatstour != null && minSeatstour > maxSeatstour) {
            model.addAttribute("errorMessage", "จำนวนที่นั่งขั้นต่ำต้องน้อยกว่าหรือเท่ากับสูงสุด");
            model.addAttribute("tour", existing);
            model.addAttribute("loggedInManager", manager);
            return "Tour/editTour";
        }
 
        // ─── สร้าง updated object ───
        Tour updated = new Tour();
        updated.setTourmname(tourmname.trim());
        updated.setStatus(status);
        updated.setTourdetail(tourdetail != null ? tourdetail.trim() : "");
        updated.setConditiontour(conditiontour != null ? conditiontour.trim() : null);
        updated.setMinSeatstour(minSeatstour);
        updated.setMaxSeatstour(maxSeatstour);
        updated.setAdultprice(adultprice);
        updated.setChildprice(childprice);
        updated.setImages(images != null && !images.isBlank() ? images : null);
 
        try {
            tourService.updateTour(tourid, updated);
            redirectAttributes.addFlashAttribute("successMessage", "แก้ไขทัวร์สำเร็จ");
            return "redirect:/manager/tours/" + tourid;
        } catch (Exception e) {
            model.addAttribute("errorMessage", "เกิดข้อผิดพลาด: " + e.getMessage());
            model.addAttribute("tour", existing);
            model.addAttribute("loggedInManager", manager);
            return "Tour/editTour";
        }
    }

    // ─── แสดงรีวิวของทัวร์ ───────────────────────────────
 
    @GetMapping("/{tourid}/reviews")
    public String tourReviews(@PathVariable("tourid") String tourid,
                              HttpSession session, Model model) {
        Communitymanager manager = (Communitymanager) session.getAttribute("loggedInManager");
        if (manager == null) return "redirect:/login";
 
        Tour tour = tourService.getTourByIdAny(tourid).orElse(null);
        if (tour == null) return "redirect:/manager/tours?error=notfound";
 
        List<Review> reviews             = reviewService.getReviewsByTourId(tourid);
        double avgRating                 = reviewService.getAvgRatingByTourId(tourid);
        Map<Integer, Long> ratingCounts  = reviewService.getRatingCountsByTourId(tourid);
 
        model.addAttribute("tour", tour);
        model.addAttribute("reviews", reviews);
        model.addAttribute("avgRating", avgRating);
        model.addAttribute("ratingCounts", ratingCounts);
        model.addAttribute("loggedInManager", manager);
        return "Tour/tourReviews";
    }
}
 
