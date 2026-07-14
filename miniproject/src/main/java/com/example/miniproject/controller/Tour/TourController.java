package com.example.miniproject.controller.Tour;

import com.example.miniproject.entity.Communitymanager;
import com.example.miniproject.entity.Review;
import com.example.miniproject.entity.Tour;
import com.example.miniproject.entity.Tourschedule;
import com.example.miniproject.service.Member.TourService;
import com.example.miniproject.service.Tour.TourScheduleService;
import com.example.miniproject.service.Member.ReviewService;
import jakarta.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;
import java.nio.file.*;
import java.sql.Date;
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
    private TourScheduleService tourScheduleService;

    @Autowired
    private ReviewService reviewService;

    private static final String UPLOAD_DIR = "uploads/tours/";

    // ─── คำนวณสถานะรวมของทัวร์จากสถานะของ "รอบทัวร์" (Tourschedule) ───────────────
    // ตัดฟิลด์ status ออกจาก Tour แล้ว ให้คำนวณสดจากรอบทัวร์แทน:
    // - มีอย่างน้อย 1 รอบ "เปิดรับจอง"  → เปิดรับจอง
    // - ไม่มีรอบเปิดรับจอง แต่มีรอบ "เต็ม" → เต็ม
    // - มีรอบแต่ทุกรอบถูกปิดหมด          → ปิด
    // - ยังไม่มีรอบทัวร์เลย               → ยังไม่เปิดรอบ
    private String computeOverallStatus(List<Tourschedule> schedules) {
        if (schedules == null || schedules.isEmpty()) {
            return "ยังไม่เปิดรอบ";
        }
        boolean hasOpen = schedules.stream()
                .anyMatch(s -> "เปิดรับจอง".equals(s.getStatus()));
        if (hasOpen) return "เปิดรับจอง";

        boolean hasFull = schedules.stream()
                .anyMatch(s -> "เต็ม".equals(s.getStatus()));
        if (hasFull) return "เต็ม";

        return "ปิด";
    }

    // ─── บันทึกรูปจาก imagesJson
    // ──────────────────────────────────────────────────
    // รับ 3 format:
    // 1. "__KEEP__" → ไม่เปลี่ยนรูป คืน null
    // 2. "__FILENAMES__:a.jpg||b.jpg" → รูปเดิมทั้งหมด คืน "a.jpg||b.jpg"
    // 3. JSON array → มีรูปใหม่ บันทึกไฟล์แล้วคืน filenames
    private String saveImagesFromBase64(String imagesJson, String tourid) {
        if (imagesJson == null || imagesJson.isBlank() || imagesJson.equals("__KEEP__"))
            return null;

        // ── format 2: รูปเดิมทั้งหมด ──
        if (imagesJson.startsWith("__FILENAMES__:")) {
            return imagesJson.substring("__FILENAMES__:".length());
        }

        // ── format 3: JSON array — ใช้ Jackson ที่มีใน Spring Boot ──
        if (!imagesJson.trim().startsWith("["))
            return null;

        try {
            Path dir = Paths.get(UPLOAD_DIR).toAbsolutePath();
            Files.createDirectories(dir);
            System.out.println("📁 บันทึกรูปที่: " + dir);

            com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
            com.fasterxml.jackson.databind.JsonNode arr = mapper.readTree(imagesJson);

            List<String> names = new ArrayList<>();
            int idx = 1;

            for (com.fasterxml.jackson.databind.JsonNode node : arr) {
                String b64 = node.has("base64") ? node.get("base64").asText() : "";
                if (b64.isBlank()) {
                    idx++;
                    continue;
                }

                // รูปเดิมที่ไม่ได้เปลี่ยน
                if (b64.startsWith("__OLD__:")) {
                    names.add(b64.substring("__OLD__:".length()));
                    idx++;
                    continue;
                }

                // รูปใหม่
                String ext = b64.contains("png") ? "png" : b64.contains("webp") ? "webp" : "jpg";
                String data = b64.contains(",") ? b64.split(",")[1] : b64;
                try {
                    byte[] bytes = Base64.getDecoder().decode(data);
                    String filename = tourid + "_" + idx + "." + ext;
                    Files.write(dir.resolve(filename), bytes,
                            StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
                    System.out.println("✅ บันทึกสำเร็จ: " + filename);
                    names.add(filename);
                    idx++;
                } catch (Exception ex) {
                    System.out.println("❌ decode ไม่ได้: " + ex.getMessage());
                }
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
        if (manager == null)
            return "redirect:/manager/login";

        List<Tour> tours = tourService.getToursByManager(manager);
        // ✅ ไม่มีฟิลด์ status ใน Tour แล้ว → คำนวณสถานะรวมจากรอบทัวร์ (Tourschedule) สดๆ ให้แต่ละทัวร์
        for (Tour t : tours) {
            t.setOverallStatus(computeOverallStatus(tourScheduleService.getSchedulesByTour(t.getTourid())));
        }

        model.addAttribute("tours", tours);
        model.addAttribute("loggedInManager", manager);
        return "Tour/listTour";
    }

    // ─── แสดงฟอร์มเพิ่มทัวร์ ────────────────────────────────────────────────────

    @GetMapping("/create")
    public String showCreateForm(HttpSession session, Model model) {
        Communitymanager manager = (Communitymanager) session.getAttribute("loggedInManager");
        if (manager == null)
            return "redirect:/manager/login";

        model.addAttribute("tour", new Tour());
        model.addAttribute("loggedInManager", manager);
        return "Tour/addTour";
    }

    // ─── บันทึกทัวร์ใหม่ ─────────────────────────────────────────────────────────

    @PostMapping("/create")
    public String createTour(
            @RequestParam("tourmname") String tourmname,
            @RequestParam("tourdetail") String tourdetail,
            @RequestParam(value = "conditiontour", required = false) String conditiontour,
            @RequestParam("minSeatstour") Integer minSeatstour,
            @RequestParam("maxSeatstour") Integer maxSeatstour,
            @RequestParam("adultprice") Double adultprice,
            @RequestParam("childprice") Double childprice,
            @RequestParam("numberOfDays") Integer numberOfDays,
            @RequestParam(value = "numberOfNights", required = false) Integer numberOfNights,
            @RequestParam(value = "tourtype", required = false) String tourtype,
            @RequestParam(value = "images", required = false) String imagesJson, 
            HttpSession session,
            Model model) {
        Communitymanager manager = (Communitymanager) session.getAttribute("loggedInManager");
        if (manager == null)
            return "/manager/login";

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
        if (numberOfDays == null || numberOfDays < 1) {
            model.addAttribute("errorMessage", "กรุณาระบุจำนวนวันให้ถูกต้อง (อย่างน้อย 1 วัน)");
            model.addAttribute("loggedInManager", manager);
            return "Tour/addTour";
        }

        // ✅ เพิ่มใหม่: ตรวจความสอดคล้องของจำนวนวัน/คืน (กันกรณี POST ตรงๆ ข้าม JS)
        if (numberOfDays == 1) {
            // ทัวร์รายวัน: คืนต้องเป็น 0 (หรือไม่ส่งมาเลยก็ยอมรับ แล้ว backend จะ set
            // ให้เป็น 0 อยู่แล้ว)
            if (numberOfNights != null && numberOfNights != 0) {
                model.addAttribute("errorMessage", "ทัวร์รายวัน (1 วัน) ต้องมีจำนวนคืนเป็น 0");
                model.addAttribute("loggedInManager", manager);
                return "Tour/addTour";
            }
        } else {
            // ทัวร์หลายวัน (numberOfDays > 1): ต้องระบุคืน และคืนต้องน้อยกว่าจำนวนวัน
            if (numberOfNights == null || numberOfNights < 1) {
                model.addAttribute("errorMessage", "ทัวร์หลายวันต้องระบุจำนวนคืนอย่างน้อย 1 คืน");
                model.addAttribute("loggedInManager", manager);
                return "Tour/addTour";
            }
            if (numberOfNights >= numberOfDays) {
                model.addAttribute("errorMessage", "จำนวนคืนต้องน้อยกว่าจำนวนวัน");
                model.addAttribute("loggedInManager", manager);
                return "Tour/addTour";
            }
            // ✅ ทัวร์ที่มากกว่า 1 วัน ต้องระบุ tourtype เอง (auto-calc ใน entity รองรับแค่ทัวร์รายวัน)
            if (tourtype == null || tourtype.isBlank()) {
                model.addAttribute("errorMessage", "กรุณาระบุประเภททัวร์สำหรับทัวร์ที่มากกว่า 1 วัน (เช่น ทัวร์วัฒนธรรมชนเผ่า, ทัวร์วิถีชีวิต)");
                model.addAttribute("loggedInManager", manager);
                return "Tour/addTour";
            }
            if (tourtype.trim().length() > 100) {
                model.addAttribute("errorMessage", "ชื่อประเภททัวร์ต้องไม่เกิน 100 ตัวอักษร");
                model.addAttribute("loggedInManager", manager);
                return "Tour/addTour";
            }
        }

        // ─── สร้าง entity แล้วบันทึก ───
        Tour tour = new Tour();
        tour.setTourmname(tourmname.trim());
        tour.setTourdetail(tourdetail.trim());
        tour.setConditiontour(conditiontour != null ? conditiontour.trim() : null);
        tour.setMinSeatstour(minSeatstour);
        tour.setMaxSeatstour(maxSeatstour);
        tour.setAdultprice(adultprice);
        tour.setChildprice(childprice);
        tour.setNumberOfDays(numberOfDays);
        tour.setNumberOfNights(numberOfDays == 1 ? 0 : numberOfNights); // ทัวร์รายวันบังคับ 0 คืน
        // ✅ tourtype ตอนนี้เป็น TourType (entity) — ส่งชื่อ (String) จากฟอร์มให้ service
        // ไปหา/สร้าง TourType เอง แล้วผูกเข้ากับ tour (ทัวร์รายวัน service จะบังคับเป็น "ทัวร์รายวัน" เองอยู่แล้ว)
        // ✅ ตัดฟิลด์ status ออกแล้ว — สถานะทัวร์ตอนนี้คำนวณจากสถานะรอบทัวร์ (Tourschedule) แทน
        //    ทัวร์ที่เพิ่งสร้างใหม่ยังไม่มีรอบทัวร์ จึงยังไม่มีสถานะจนกว่าจะไปเพิ่มวันที่เปิดทัวร์

        try {
            Tour saved = tourService.createTour(tour, manager,
                    tourtype != null && !tourtype.isBlank() ? tourtype.trim() : null);

            // ✅ บันทึกรูปจาก base64 JSON
            String imageNames = saveImagesFromBase64(imagesJson, saved.getTourid());
            if (imageNames != null) {
                tourService.updateImages(saved.getTourid(), imageNames);
            }

            return "redirect:/manager/tours/" + saved.getTourid() + "/schedules?success=created";
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
    Communitymanager manager = (Communitymanager)
    session.getAttribute("loggedInManager");
    if (manager == null) return "redirect:/manager/login";

    Tour tour = tourService.getTourByIdAny(tourid).orElse(null);
    if (tour == null) return "redirect:/manager/tours?error=notfound";

    List<Tourschedule> schedules = tourScheduleService.getSchedulesByTour(tourid);
    Map<String, Integer> bookedMap = tourScheduleService.getBookedSeatsMap(tourid);
    tour.setOverallStatus(computeOverallStatus(schedules));

    model.addAttribute("tour", tour);
    model.addAttribute("schedules", schedules);
    model.addAttribute("bookedSeatsMap", bookedMap);
    model.addAttribute("loggedInManager", manager);
    return "Tour/tourDetail";
    }

    // ─── แสดงหน้าจัดการรอบทัวร์ (แยกต่างหากจากหน้าแก้ไขข้อมูลทัวร์) ──────────────────

    @GetMapping("/{tourid}/schedules")
    public String manageSchedules(@PathVariable("tourid") String tourid,
            @RequestParam(value = "success", required = false) String success,
            HttpSession session, Model model) {
        Communitymanager manager = (Communitymanager) session.getAttribute("loggedInManager");
        if (manager == null)
            return "redirect:/manager/login";

        Tour tour = tourService.getTourByIdAny(tourid).orElse(null);
        if (tour == null)
            return "redirect:/manager/tours?error=notfound";

        if (!tour.getCommunitymanager().getManagerid().equals(manager.getManagerid())) {
            return "redirect:/manager/tours?error=forbidden";
        }

        List<Tourschedule> schedules = tourScheduleService.getSchedulesByTour(tourid);
        tour.setOverallStatus(computeOverallStatus(schedules));

        model.addAttribute("tour", tour);
        model.addAttribute("schedules", schedules);
        model.addAttribute("bookedSeatsMap", tourScheduleService.getBookedSeatsMap(tourid));
        model.addAttribute("loggedInManager", manager);
        if ("created".equals(success)) {
            model.addAttribute("successMessage", "สร้างทัวร์สำเร็จ! ตอนนี้เพิ่มวันที่เปิดทัวร์ได้เลย");
        }
        return "Tour/tourschedule";
    }

    // ─── เพิ่มวันที่เปิดทัวร์ใหม่ (Tourschedule) ─────────────────────────────────

    @PostMapping("/{tourid}/schedules")
    public String addSchedule(@PathVariable("tourid") String tourid,
            @RequestParam("opendate") @org.springframework.format.annotation.DateTimeFormat(iso = org.springframework.format.annotation.DateTimeFormat.ISO.DATE) java.time.LocalDate opendate,
            HttpSession session,
            RedirectAttributes redirectAttributes) {
        Communitymanager manager = (Communitymanager) session.getAttribute("loggedInManager");
        if (manager == null)
            return "redirect:/manager/login";

        Tour tour = tourService.getTourByIdAny(tourid).orElse(null);
        if (tour == null || !tour.getCommunitymanager().getManagerid().equals(manager.getManagerid())) {
            return "redirect:/manager/tours?error=forbidden";
        }

        tourScheduleService.createSchedule(tour, Date.valueOf(opendate));
        redirectAttributes.addFlashAttribute("successMessage", "เพิ่มวันที่เปิดทัวร์สำเร็จ");
        return "redirect:/manager/tours/" + tourid + "/schedules";
    }

    // ─── ปิด/เปิดรับจองรอบใดรอบหนึ่งด้วยมือ ─────────────────────────────────────

    @PostMapping("/{tourid}/schedules/{scheduleid}/status")
    public String updateScheduleStatus(@PathVariable("tourid") String tourid,
            @PathVariable("scheduleid") String scheduleid,
            @RequestParam("status") String status,
            HttpSession session,
            RedirectAttributes redirectAttributes) {
        Communitymanager manager = (Communitymanager) session.getAttribute("loggedInManager");
        if (manager == null)
            return "redirect:/manager/login";

        Tour tour = tourService.getTourByIdAny(tourid).orElse(null);
        if (tour == null || !tour.getCommunitymanager().getManagerid().equals(manager.getManagerid())) {
            return "redirect:/manager/tours?error=forbidden";
        }

        tourScheduleService.updateStatus(scheduleid, status);
        redirectAttributes.addFlashAttribute("successMessage", "อัปเดตสถานะรอบทัวร์สำเร็จ");
        return "redirect:/manager/tours/" + tourid + "/schedules";
    }

    // ─── ลบวันที่เปิดทัวร์ (เฉพาะรอบที่ยังไม่มีคนจอง) ───────────────────────────────

    @PostMapping("/{tourid}/schedules/{scheduleid}/delete")
    public String deleteSchedule(@PathVariable("tourid") String tourid,
            @PathVariable("scheduleid") String scheduleid,
            HttpSession session,
            RedirectAttributes redirectAttributes) {
        Communitymanager manager = (Communitymanager) session.getAttribute("loggedInManager");
        if (manager == null)
            return "redirect:/manager/login";

        Tour tour = tourService.getTourByIdAny(tourid).orElse(null);
        if (tour == null || !tour.getCommunitymanager().getManagerid().equals(manager.getManagerid())) {
            return "redirect:/manager/tours?error=forbidden";
        }

        Map<String, Integer> bookedMap = tourScheduleService.getBookedSeatsMap(tourid);
        if (bookedMap.getOrDefault(scheduleid, 0) > 0) {
            redirectAttributes.addFlashAttribute("errorMessage", "ไม่สามารถลบรอบนี้ได้ เพราะมีคนจองแล้ว");
            return "redirect:/manager/tours/" + tourid + "/schedules";
        }

        tourScheduleService.deleteSchedule(scheduleid);
        redirectAttributes.addFlashAttribute("successMessage", "ลบวันที่เปิดทัวร์สำเร็จ");
        return "redirect:/manager/tours/" + tourid + "/schedules";
    }

    // ─── แสดงฟอร์มแก้ไขทัวร์ ────────────────────────────────────────────────────

    @GetMapping("/{tourid}/edit")
    public String showEditForm(@PathVariable("tourid") String tourid,
            HttpSession session, Model model) {
        Communitymanager manager = (Communitymanager) session.getAttribute("loggedInManager");
        if (manager == null)
            return "redirect:/manager/login";

        Tour tour = tourService.getTourByIdAny(tourid).orElse(null);
        if (tour == null)
            return "redirect:/manager/tours?error=notfound";

        if (!tour.getCommunitymanager().getManagerid().equals(manager.getManagerid())) {
            return "redirect:/manager/tours?error=forbidden";
        }

        List<Tourschedule> schedules = tourScheduleService.getSchedulesByTour(tourid);
        tour.setOverallStatus(computeOverallStatus(schedules));

        model.addAttribute("tour", tour);
        model.addAttribute("schedules", schedules);
        model.addAttribute("bookedSeatsMap", tourScheduleService.getBookedSeatsMap(tourid));
        model.addAttribute("loggedInManager", manager);
        return "Tour/editTour";
    }

    // ─── บันทึกการแก้ไขทัวร์ ─────────────────────────────────────────────────────

    @PostMapping("/{tourid}/edit")
    public String updateTour(
            @PathVariable("tourid") String tourid,
            @RequestParam("tourmname") String tourmname,
            @RequestParam("tourdetail") String tourdetail,
            @RequestParam(value = "conditiontour", required = false) String conditiontour,
            @RequestParam("minSeatstour") Integer minSeatstour,
            @RequestParam("maxSeatstour") Integer maxSeatstour,
            @RequestParam("adultprice") Double adultprice,
            @RequestParam("childprice") Double childprice,
            @RequestParam("numberOfDays") Integer numberOfDays,
            @RequestParam(value = "numberOfNights", required = false) Integer numberOfNights,
            @RequestParam(value = "tourtype", required = false) String tourtype,
            @RequestParam(value = "images", required = false) String imagesJson, // ✅ รับ base64 หรือ __KEEP__
            HttpSession session,
            Model model,
            RedirectAttributes redirectAttributes) {
        Communitymanager manager = (Communitymanager) session.getAttribute("loggedInManager");
        if (manager == null)
            return "redirect:/manager/login";

        Tour existing = tourService.getTourByIdAny(tourid).orElse(null);
        if (existing == null)
            return "redirect:/manager/tours?error=notfound";

        if (!existing.getCommunitymanager().getManagerid().equals(manager.getManagerid())) {
            return "redirect:/manager/tours?error=forbidden";
        }

        // ─── Server-side validation ───
        if (tourmname == null || tourmname.isBlank()) {
            model.addAttribute("errorMessage", "กรุณากรอกชื่อทัวร์");
            model.addAttribute("tour", existing);
            model.addAttribute("schedules", tourScheduleService.getSchedulesByTour(tourid));
            model.addAttribute("bookedSeatsMap", tourScheduleService.getBookedSeatsMap(tourid));
            model.addAttribute("loggedInManager", manager);
            return "Tour/editTour";
        }
        if (minSeatstour != null && maxSeatstour != null && minSeatstour > maxSeatstour) {
            model.addAttribute("errorMessage", "จำนวนที่นั่งขั้นต่ำต้องน้อยกว่าหรือเท่ากับสูงสุด");
            model.addAttribute("tour", existing);
            model.addAttribute("schedules", tourScheduleService.getSchedulesByTour(tourid));
            model.addAttribute("bookedSeatsMap", tourScheduleService.getBookedSeatsMap(tourid));
            model.addAttribute("loggedInManager", manager);
            return "Tour/editTour";
        }
        // ✅ ทัวร์ที่มากกว่า 1 วัน ต้องระบุ tourtype เอง (auto-calc ใน entity รองรับแค่ทัวร์รายวัน)
        if (numberOfDays != null && numberOfDays > 1) {
            if (tourtype == null || tourtype.isBlank()) {
                model.addAttribute("errorMessage", "กรุณาระบุประเภททัวร์สำหรับทัวร์ที่มากกว่า 1 วัน (เช่น ทัวร์วัฒนธรรมชนเผ่า, ทัวร์วิถีชีวิต)");
                model.addAttribute("tour", existing);
                model.addAttribute("schedules", tourScheduleService.getSchedulesByTour(tourid));
                model.addAttribute("bookedSeatsMap", tourScheduleService.getBookedSeatsMap(tourid));
                model.addAttribute("loggedInManager", manager);
                return "Tour/editTour";
            }
            if (tourtype.trim().length() > 100) {
                model.addAttribute("errorMessage", "ชื่อประเภททัวร์ต้องไม่เกิน 100 ตัวอักษร");
                model.addAttribute("tour", existing);
                model.addAttribute("schedules", tourScheduleService.getSchedulesByTour(tourid));
                model.addAttribute("bookedSeatsMap", tourScheduleService.getBookedSeatsMap(tourid));
                model.addAttribute("loggedInManager", manager);
                return "Tour/editTour";
            }
        }

        // ─── อัปเดต fields ───
        Tour updated = new Tour();
        updated.setTourmname(tourmname.trim());
        updated.setTourdetail(tourdetail != null ? tourdetail.trim() : "");
        updated.setConditiontour(conditiontour != null ? conditiontour.trim() : null);
        updated.setMinSeatstour(minSeatstour);
        updated.setMaxSeatstour(maxSeatstour);
        updated.setAdultprice(adultprice);
        updated.setChildprice(childprice);
        updated.setNumberOfDays(numberOfDays);
        updated.setNumberOfNights(numberOfDays != null && numberOfDays == 1 ? 0 : numberOfNights);
        // ✅ ตัดฟิลด์ status ออกแล้ว — สถานะทัวร์คำนวณจากสถานะรอบทัวร์ (Tourschedule) เสมอ ไม่ต้องรับ/บันทึกจากฟอร์มนี้อีกต่อไป
       

        // ✅ ถ้าเป็น __KEEP__ หรือ null = ไม่ได้เปลี่ยนรูป ให้คงรูปเดิม
        if (imagesJson == null || imagesJson.isBlank() || imagesJson.equals("__KEEP__")) {
            updated.setImages(existing.getImages());
        } else {
            String newImageNames = saveImagesFromBase64(imagesJson, tourid);
            updated.setImages(newImageNames != null ? newImageNames : existing.getImages());
        }

        try {
            tourService.updateTour(tourid, updated,
                    tourtype != null && !tourtype.isBlank() ? tourtype.trim() : null);
            redirectAttributes.addFlashAttribute("successMessage", "แก้ไขทัวร์สำเร็จ");
            return "redirect:/manager/tours/" + tourid;
        } catch (Exception e) {
            model.addAttribute("errorMessage", "เกิดข้อผิดพลาด: " + e.getMessage());
            model.addAttribute("tour", existing);
            model.addAttribute("schedules", tourScheduleService.getSchedulesByTour(tourid));
            model.addAttribute("bookedSeatsMap", tourScheduleService.getBookedSeatsMap(tourid));
            model.addAttribute("loggedInManager", manager);
            return "Tour/editTour";
        }
    }

    // ─── แสดงรีวิวของทัวร์ ───────────────────────────────────────────────────────

    @GetMapping("/{tourid}/reviews")
    public String tourReviews(@PathVariable("tourid") String tourid,
            HttpSession session, Model model) {
        Communitymanager manager = (Communitymanager) session.getAttribute("loggedInManager");
        if (manager == null)
            return "redirect:/manager/login";

        Tour tour = tourService.getTourByIdAny(tourid).orElse(null);
        if (tour == null)
            return "redirect:/manager/tours?error=notfound";

        List<Review> reviews = reviewService.getReviewsByTourId(tourid);
        double avgRating = reviewService.getAvgRatingByTourId(tourid);
        Map<Integer, Long> ratingCounts = reviewService.getRatingCountsByTourId(tourid);

        model.addAttribute("tour", tour);
        model.addAttribute("reviews", reviews);
        model.addAttribute("avgRating", avgRating);
        model.addAttribute("ratingCounts", ratingCounts);
        model.addAttribute("loggedInManager", manager);
        return "Tour/tourReviews";
    }
}