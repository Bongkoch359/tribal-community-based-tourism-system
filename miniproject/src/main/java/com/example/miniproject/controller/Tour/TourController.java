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

import java.io.IOException;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/manager/tours")
public class TourController {

    @Autowired
    private TourService tourService;

    @Autowired
    private ReviewService reviewService;

    private static final String UPLOAD_DIR = "uploads/tours/";

    // ─── บันทึกรูปจาก base64 JSON ที่ส่งมาจาก hidden input ─────────────────────
    // รับ JSON array string เช่น [{"base64":"data:image/jpeg;base64,...","primary":true}, ...]
    // หรือ base64 string เดี่ยว (จาก editTour) เช่น "/9j/4AAQ..."
    // คืนค่าเป็น "filename1.jpg||filename2.jpg" หรือ null ถ้าไม่มีรูป
    private String saveImagesFromBase64(String imagesJson, String tourid) {
        if (imagesJson == null || imagesJson.isBlank() || imagesJson.equals("__KEEP__")) return null;

        try {
            Path dir = Paths.get(UPLOAD_DIR).toAbsolutePath();
            Files.createDirectories(dir);
            System.out.println("📁 บันทึกรูปที่: " + dir);

            List<String> names = new ArrayList<>();

            // ── กรณี editTour ส่งมาเป็น base64 เดี่ยว (ไม่ใช่ JSON array) ──
            if (!imagesJson.trim().startsWith("[")) {
                String b64 = imagesJson.trim();
                String ext = "jpg";
                String data = b64;
                if (b64.startsWith("data:")) {
                    ext  = b64.contains("png") ? "png" : b64.contains("webp") ? "webp" : "jpg";
                    data = b64.split(",")[1];
                }
                byte[] bytes = Base64.getDecoder().decode(data);
                String filename = tourid + "_1." + ext;
                Files.write(dir.resolve(filename), bytes, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
                System.out.println("✅ บันทึกสำเร็จ: " + filename);
                names.add(filename);
                return String.join("||", names);
            }

            // ── กรณี addTour ส่งมาเป็น JSON array ──
            // parse ด้วย substring แทน library เพื่อไม่ต้อง import org.json
            String json = imagesJson.trim();
            // ดึง base64 แต่ละรูปออกมา
            int idx = 1;
            int pos = 0;
            while (pos < json.length()) {
                int start = json.indexOf("\"base64\"", pos);
                if (start < 0) break;
                start = json.indexOf("\"", start + 8); // เปิด "
                if (start < 0) break;
                start++; // ข้าม "
                // หา " ปิด (ต้องข้าม escaped quote)
                int end = start;
                while (end < json.length()) {
                    if (json.charAt(end) == '"' && json.charAt(end - 1) != '\\') break;
                    end++;
                }
                String b64 = json.substring(start, end);
                String ext  = "jpg";
                String data = b64;
                if (b64.startsWith("data:")) {
                    ext  = b64.contains("png") ? "png" : b64.contains("webp") ? "webp" : "jpg";
                    data = b64.split(",")[1];
                }
                try {
                    byte[] bytes = Base64.getDecoder().decode(data);
                    String filename = tourid + "_" + idx + "." + ext;
                    Files.write(dir.resolve(filename), bytes, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
                    System.out.println("✅ บันทึกสำเร็จ: " + filename);
                    names.add(filename);
                    idx++;
                } catch (Exception ex) {
                    System.out.println("❌ decode ไม่ได้: " + ex.getMessage());
                }
                pos = end + 1;
            }

            return names.isEmpty() ? null : String.join("||", names);

        } catch (IOException e) {
            System.out.println("❌ บันทึกรูปไม่สำเร็จ: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    // ─── แสดงรายการทัวร์ทั้งหมดของ manager ─────────────────────────────────────

    @GetMapping
    public String listTours(HttpSession session, Model model) {
        Communitymanager manager = (Communitymanager) session.getAttribute("loggedInManager");
        if (manager == null) return "redirect:/login";

        model.addAttribute("tours", tourService.getAllTours());
        model.addAttribute("loggedInManager", manager);
        return "Tour/listTour";
    }

    // ─── แสดงฟอร์มเพิ่มทัวร์ ────────────────────────────────────────────────────

    @GetMapping("/create")
    public String showCreateForm(HttpSession session, Model model) {
        Communitymanager manager = (Communitymanager) session.getAttribute("loggedInManager");
        if (manager == null) return "redirect:/login";

        model.addAttribute("tour", new Tour());
        model.addAttribute("loggedInManager", manager);
        return "Tour/addTour";
    }

    // ─── บันทึกทัวร์ใหม่ ─────────────────────────────────────────────────────────

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
            @RequestParam(value = "images", required = false) String imagesJson, // ✅ รับ base64 JSON
            HttpSession session,
            Model model
    ) {
        Communitymanager manager = (Communitymanager) session.getAttribute("loggedInManager");
        if (manager == null) return "redirect:/login";

        // ─── Server-side validation ───
        if (tourmname == null || tourmname.isBlank()) {
            model.addAttribute("errorMessage", "กรุณากรอกชื่อทัวร์");
            model.addAttribute("loggedInManager", manager);
            return "Tour/addTour";
        }
        if (tourdetail == null || tourdetail.isBlank()) {
            model.addAttribute("errorMessage", "กรุณากรอกรายละเอียดทัวร์");
            model.addAttribute("loggedInManager", manager);
            return "Tour/addTour";
        }
        if (minSeatstour == null || maxSeatstour == null) {
            model.addAttribute("errorMessage", "กรุณาระบุจำนวนที่นั่ง");
            model.addAttribute("loggedInManager", manager);
            return "Tour/addTour";
        }
        if (minSeatstour > maxSeatstour) {
            model.addAttribute("errorMessage", "จำนวนที่นั่งขั้นต่ำต้องน้อยกว่าสูงสุด");
            model.addAttribute("loggedInManager", manager);
            return "Tour/addTour";
        }
        if (adultprice == null || childprice == null) {
            model.addAttribute("errorMessage", "กรุณาระบุราคาทัวร์");
            model.addAttribute("loggedInManager", manager);
            return "Tour/addTour";
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

        try {
            Tour saved = tourService.createTour(tour, manager);

            // ✅ บันทึกรูปจาก base64 JSON
            String imageNames = saveImagesFromBase64(imagesJson, saved.getTourid());
            if (imageNames != null) {
                saved.setImages(imageNames);
                tourService.updateTour(saved.getTourid(), saved);
            }

            return "redirect:/manager/tours?success=created";
        } catch (Exception e) {
            model.addAttribute("errorMessage", "เกิดข้อผิดพลาด: " + e.getMessage());
            model.addAttribute("loggedInManager", manager);
            return "Tour/addTour";
        }
    }

    // ─── แสดงรายละเอียดทัวร์ ─────────────────────────────────────────────────────

    @GetMapping("/{tourid}")
    public String tourDetail(@PathVariable("tourid") String tourid,
                             HttpSession session, Model model) {
        Communitymanager manager = (Communitymanager) session.getAttribute("loggedInManager");
        if (manager == null) return "redirect:/login";

        Tour tour = tourService.getTourByIdAny(tourid).orElse(null);
        if (tour == null) return "redirect:/manager/tours?error=notfound";

        model.addAttribute("tour", tour);
        model.addAttribute("loggedInManager", manager);
        return "Tour/tourDetail";
    }

    // ─── แสดงฟอร์มแก้ไขทัวร์ ────────────────────────────────────────────────────

    @GetMapping("/{tourid}/edit")
    public String showEditForm(@PathVariable("tourid") String tourid,
                               HttpSession session, Model model) {
        Communitymanager manager = (Communitymanager) session.getAttribute("loggedInManager");
        if (manager == null) return "redirect:/login";

        Tour tour = tourService.getTourByIdAny(tourid).orElse(null);
        if (tour == null) return "redirect:/manager/tours?error=notfound";

        if (!tour.getCommunitymanager().getManagerid().equals(manager.getManagerid())) {
            return "redirect:/manager/tours?error=forbidden";
        }

        model.addAttribute("tour", tour);
        model.addAttribute("loggedInManager", manager);
        return "Tour/editTour";
    }

    // ─── บันทึกการแก้ไขทัวร์ ─────────────────────────────────────────────────────

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
            @RequestParam(value = "images", required = false) String imagesJson, // ✅ รับ base64 หรือ __KEEP__
            HttpSession session,
            Model model,
            RedirectAttributes redirectAttributes
    ) {
        Communitymanager manager = (Communitymanager) session.getAttribute("loggedInManager");
        if (manager == null) return "redirect:/login";

        Tour existing = tourService.getTourByIdAny(tourid).orElse(null);
        if (existing == null) return "redirect:/manager/tours?error=notfound";

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

        // ─── อัปเดต fields ───
        Tour updated = new Tour();
        updated.setTourmname(tourmname.trim());
        updated.setStatus(status);
        updated.setTourdetail(tourdetail != null ? tourdetail.trim() : "");
        updated.setConditiontour(conditiontour != null ? conditiontour.trim() : null);
        updated.setMinSeatstour(minSeatstour);
        updated.setMaxSeatstour(maxSeatstour);
        updated.setAdultprice(adultprice);
        updated.setChildprice(childprice);

        // ✅ ถ้าเป็น __KEEP__ หรือ null = ไม่ได้เปลี่ยนรูป ให้คงรูปเดิม
        if (imagesJson == null || imagesJson.isBlank() || imagesJson.equals("__KEEP__")) {
            updated.setImages(existing.getImages());
        } else {
            String newImageNames = saveImagesFromBase64(imagesJson, tourid);
            updated.setImages(newImageNames != null ? newImageNames : existing.getImages());
        }

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

    // ─── แสดงรีวิวของทัวร์ ───────────────────────────────────────────────────────

    @GetMapping("/{tourid}/reviews")
    public String tourReviews(@PathVariable("tourid") String tourid,
                              HttpSession session, Model model) {
        Communitymanager manager = (Communitymanager) session.getAttribute("loggedInManager");
        if (manager == null) return "redirect:/login";

        Tour tour = tourService.getTourByIdAny(tourid).orElse(null);
        if (tour == null) return "redirect:/manager/tours?error=notfound";

        List<Review> reviews            = reviewService.getReviewsByTourId(tourid);
        double avgRating                = reviewService.getAvgRatingByTourId(tourid);
        Map<Integer, Long> ratingCounts = reviewService.getRatingCountsByTourId(tourid);

        model.addAttribute("tour", tour);
        model.addAttribute("reviews", reviews);
        model.addAttribute("avgRating", avgRating);
        model.addAttribute("ratingCounts", ratingCounts);
        model.addAttribute("loggedInManager", manager);
        return "Tour/tourReviews";
    }
}