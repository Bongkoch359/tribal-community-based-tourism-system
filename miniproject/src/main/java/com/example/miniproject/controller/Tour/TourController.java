package com.example.miniproject.controller.Tour;

import com.example.miniproject.entity.Communitymanager;
import com.example.miniproject.entity.Tour;
import com.example.miniproject.service.Member.TourService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/manager/tours")
public class TourController {

    @Autowired
    private TourService tourService;

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

     // ─── ปิดรับการจอง (แทนการลบ เพื่อรักษาประวัติ) ──────────
    @PostMapping("/close/{tourid}")
    public String closeTour(@PathVariable String tourid, HttpSession session) {
        Communitymanager manager = (Communitymanager) session.getAttribute("loggedInManager");
        if (manager == null) return "redirect:/login";
        tourService.closeBooking(tourid);
        return "redirect:/manager/tours?success=closed";
    }
 
    // ─── เปลี่ยนสถานะทัวร์ ───────────────────────────────
 
    @PostMapping("/status/{tourid}")
    public String changeTourStatus(
            @PathVariable String tourid,
            @RequestParam("status") String status,
            HttpSession session
    ) {
        Communitymanager manager = (Communitymanager) session.getAttribute("loggedInManager");
        if (manager == null) return "redirect:/login";
        tourService.changeStatus(tourid, status);
        return "redirect:/manager/tours?success=updated";
    }
   
}
