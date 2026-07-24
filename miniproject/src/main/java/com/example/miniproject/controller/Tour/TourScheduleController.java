package com.example.miniproject.controller.Tour;

import com.example.miniproject.entity.Communitymanager;
import com.example.miniproject.entity.Tour;
import com.example.miniproject.entity.Tourschedule;
import com.example.miniproject.service.Member.TourService;
import com.example.miniproject.service.Tour.TourScheduleService;
import jakarta.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.sql.Date;
import java.util.List;
import java.util.Map;

// ═══════════════════════════════════════════════════════════════
//  แยกออกมาจาก TourController: จัดการเฉพาะ "รอบทัวร์" (Tourschedule)
//    - แสดงหน้าจัดการรอบทัวร์แยก (Tour/tourschedule) — ยังเก็บไว้เผื่อใช้
//    - เพิ่ม / ลบ / เปลี่ยนสถานะ / bulk เปลี่ยนสถานะตามช่วงวันที่
@Controller
@RequestMapping("/manager/tours/{tourid}/schedules")
public class TourScheduleController {

    @Autowired
    private TourService tourService;

    @Autowired
    private TourScheduleService tourScheduleService;

    // computeOverallStatus() และ ALLOWED_MANUAL_STATUS อยู่ใน TourScheduleService
    // ที่เดียวแล้ว (ใช้ร่วมกับ TourController) — เรียกผ่าน
    // tourScheduleService.computeOverallStatus(...) และ TourScheduleService.ALLOWED_MANUAL_STATUS

    // ─── แสดงหน้าจัดการรอบทัวร์ (แยกต่างหากจากหน้าแก้ไขข้อมูลทัวร์) ─────────────────

    @GetMapping
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
        tour.setOverallStatus(tourScheduleService.computeOverallStatus(schedules));

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

    @PostMapping
    public String addSchedule(@PathVariable("tourid") String tourid,
            @RequestParam("opendate") @org.springframework.format.annotation.DateTimeFormat(iso = org.springframework.format.annotation.DateTimeFormat.ISO.DATE) java.time.LocalDate opendate,
            @RequestParam("enddate") @org.springframework.format.annotation.DateTimeFormat(iso = org.springframework.format.annotation.DateTimeFormat.ISO.DATE) java.time.LocalDate enddate,
            @RequestParam(value = "status", required = false) String status,
            HttpSession session,
            RedirectAttributes redirectAttributes) {

        Communitymanager manager = (Communitymanager) session.getAttribute("loggedInManager");
        if (manager == null)
            return "redirect:/manager/login";

        Tour tour = tourService.getTourByIdAny(tourid).orElse(null);
        if (tour == null || !tour.getCommunitymanager().getManagerid().equals(manager.getManagerid())) {
            return "redirect:/manager/tours?error=forbidden";
        }

        //  ทัวร์รายวัน (numberOfDays == 1) → วันที่เริ่มกับวันที่จบต้องเท่ากันเสมอ
        // บังคับที่ backend ด้วย ไม่พึ่ง JS ฝั่งหน้าเว็บอย่างเดียว
        java.time.LocalDate finalEnddate = enddate;
        if (tour.getNumberOfDays() != null && tour.getNumberOfDays() == 1) {
            finalEnddate = opendate;
        }

        // ตรวจสอบความถูกต้องของวันที่ (Validation)
        if (finalEnddate.isBefore(opendate)) {
            redirectAttributes.addFlashAttribute("errorMessage", "วันที่จบทัวร์ต้องไม่ก่อนหน้าวันที่เริ่มทัวร์");
            return "redirect:/manager/tours/" + tourid + "/schedules";
        }

        // ส่ง enddate เข้าไปใน Service
        Tourschedule createdSchedule = tourScheduleService.createSchedule(tour, Date.valueOf(opendate),
                Date.valueOf(finalEnddate));

        //  ตั้งสถานะเริ่มต้นตามที่เลือก (ถ้าไม่ระบุหรือค่าไม่ถูกต้อง
        // จะใช้ค่าเริ่มต้นจาก service คือ "เปิดรับจอง")
        if (createdSchedule != null && status != null && TourScheduleService.ALLOWED_MANUAL_STATUS.contains(status.trim())) {
            tourScheduleService.updateStatus(createdSchedule.getScheduleid(), status.trim());
        }

        redirectAttributes.addFlashAttribute("successMessage", "เพิ่มวันที่เปิดทัวร์สำเร็จ");
        return "redirect:/manager/tours/" + tourid + "/schedules";
    }

    // ─── ปิด/เปิดรับจองรอบใดรอบหนึ่งด้วยมือ ─────────────────────────────────────

    @PostMapping("/{scheduleid}/status")
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

    @PostMapping("/{scheduleid}/delete")
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

    // ─── อัปเดตสถานะรอบทัวร์หลายรอบพร้อมกัน ตามช่วงวันที่ (ใช้กับปฏิทินแบบลากเลือกช่วง) ───

    @PostMapping("/bulk-status")
    @ResponseBody
    public Map<String, Object> bulkUpdateStatus(
            @PathVariable("tourid") String tourid,
            @RequestParam("startDate") @org.springframework.format.annotation.DateTimeFormat(iso = org.springframework.format.annotation.DateTimeFormat.ISO.DATE) java.time.LocalDate startDate,
            @RequestParam("endDate") @org.springframework.format.annotation.DateTimeFormat(iso = org.springframework.format.annotation.DateTimeFormat.ISO.DATE) java.time.LocalDate endDate,
            @RequestParam("status") String status,
            HttpSession session) {

        Map<String, Object> result = new java.util.HashMap<>();
        Communitymanager manager = (Communitymanager) session.getAttribute("loggedInManager");
        if (manager == null) {
            result.put("ok", false);
            result.put("message", "กรุณาเข้าสู่ระบบ");
            return result;
        }

        Tour tour = tourService.getTourByIdAny(tourid).orElse(null);
        if (tour == null || !tour.getCommunitymanager().getManagerid().equals(manager.getManagerid())) {
            result.put("ok", false);
            result.put("message", "ไม่มีสิทธิ์แก้ไขทัวร์นี้");
            return result;
        }

        if (!TourScheduleService.ALLOWED_MANUAL_STATUS.contains(status)) {
            result.put("ok", false);
            result.put("message", "สถานะไม่ถูกต้อง");
            return result;
        }

        // ✅ ถ้าจะ "ปิด" รอบที่มีคนจองแล้ว ยังปิดได้ (ไม่รับจองเพิ่ม) — ตรงนี้แค่เปลี่ยน
        // สถานะ ไม่ใช่ลบ จึงอนุญาตแม้มีคนจองแล้ว (logic อยู่ใน Service แล้ว)
        int updated = tourScheduleService.bulkUpdateStatusByDateRange(tourid, startDate, endDate, status);

        result.put("ok", true);
        result.put("updated", updated);
        result.put("message", "อัปเดตสถานะ " + updated + " รอบทัวร์เรียบร้อย");
        return result;
    }
}