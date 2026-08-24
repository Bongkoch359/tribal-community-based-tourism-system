package com.example.miniproject.controller.Tour;

import com.example.miniproject.entity.Booking;
import com.example.miniproject.entity.Communitymanager;
import com.example.miniproject.entity.enums.BookingStatus;
import com.example.miniproject.service.Member.BookingTourService;

import jakarta.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

// ─── หน้ารายการจองทัวร์ สำหรับผู้จัดการชุมชน (ตรวจสอบการจอง) ───
@Controller
@RequestMapping("/manager/bookings")
public class TourBookingManagerController {

    @Autowired
    private BookingTourService tourBookingService;

    // แท็บ: รอตรวจสอบ (default) / ยืนยันแล้ว / ยกเลิก
    @GetMapping
    public String listBookings(
            @RequestParam(value = "status", required = false, defaultValue = "WAITING_APPROVAL") String status,
            HttpSession session,
            Model model) {

        Communitymanager manager = (Communitymanager) session.getAttribute("loggedInManager");
        if (manager == null) {
            return "redirect:/manager/login";
        }

        BookingStatus filterStatus;
        String tabKey;

        if ("ALL".equalsIgnoreCase(status)) {
            filterStatus = null;
            tabKey = "ALL";
        } else {
            try {
                filterStatus = BookingStatus.valueOf(status.trim().toUpperCase());
            } catch (IllegalArgumentException e) {
                filterStatus = BookingStatus.WAITING_APPROVAL;
            }
            tabKey = filterStatus.name();
        }

        List<Booking> bookings = tourBookingService.getTourBookingsByManager(manager.getManagerid(), filterStatus);

        // ── นับจำนวนแต่ละสถานะ (ไม่ผูกกับ tab ที่กำลังดูอยู่ ใช้ทั้งหมดของ manager) ──
        List<Booking> allBookings = tourBookingService.getTourBookingsByManager(manager.getManagerid(), null);
        long countWaiting = allBookings.stream()
                .filter(b -> b.getBookingStatus() == BookingStatus.WAITING_APPROVAL).count();
        long countConfirmed = allBookings.stream()
                .filter(b -> b.getBookingStatus() == BookingStatus.CONFIRMED).count();
        long countCompleted = allBookings.stream()
                .filter(b -> b.getBookingStatus() == BookingStatus.COMPLETED).count();
        long countCancel = allBookings.stream()
                .filter(b -> b.getBookingStatus() == BookingStatus.CANCEL).count();

        model.addAttribute("bookings", bookings);
        model.addAttribute("currentStatus", tabKey);
        model.addAttribute("loggedInManager", manager);
        model.addAttribute("countWaiting", countWaiting);
        model.addAttribute("countConfirmed", countConfirmed);
        model.addAttribute("countCompleted", countCompleted);
        model.addAttribute("countCancel", countCancel);
        return "Tour/listTourBooking";
    }

    // ─── หน้ารายละเอียดการจองทัวร์ 1 รายการ ───
    @GetMapping("/{id}")
    public String bookingDetail(
            @PathVariable("id") String id,
            HttpSession session,
            Model model) {

        Communitymanager manager = (Communitymanager) session.getAttribute("loggedInManager");
        if (manager == null) {
            return "redirect:/manager/login";
        }

        try {
            Booking booking = tourBookingService.getTourBookingDetailForManager(id, manager.getManagerid());
            model.addAttribute("booking", booking);
            model.addAttribute("loggedInManager", manager);
            return "Tour/tourBookingDetail";
        } catch (RuntimeException e) {
            // ไม่พบการจอง หรือไม่มีสิทธิ์เข้าถึง -> เด้งกลับไปหน้ารายการ
            return "redirect:/manager/bookings?error=" + e.getMessage();
        }
    }

    // ─── ยืนยันการจองทัวร์ (เรียกจากปุ่มในหน้ารายละเอียด ผ่าน fetch/AJAX) ───
    @PostMapping("/confirm")
    @ResponseBody
    public Map<String, Object> confirmBooking(
            @RequestParam("bookingid") String bookingId,
            HttpSession session) {

        Map<String, Object> result = new HashMap<>();
        Communitymanager manager = (Communitymanager) session.getAttribute("loggedInManager");

        if (manager == null) {
            result.put("success", false);
            result.put("message", "กรุณาเข้าสู่ระบบใหม่อีกครั้ง");
            return result;
        }

        try {
            tourBookingService.confirmTourBookingByManager(bookingId, manager.getManagerid());
            result.put("success", true);
        } catch (RuntimeException e) {
            result.put("success", false);
            result.put("message", e.getMessage());
        }
        return result;
    }

    // ─── ยกเลิกการจองทัวร์ (เรียกจากปุ่มในหน้ารายละเอียด ผ่าน fetch/AJAX) ───
    @PostMapping("/cancel")
    @ResponseBody
    public Map<String, Object> cancelBooking(
            @RequestParam("bookingid") String bookingId,
            @RequestParam("reason") String reason,
            HttpSession session) {

        Map<String, Object> result = new HashMap<>();
        Communitymanager manager = (Communitymanager) session.getAttribute("loggedInManager");

        if (manager == null) {
            result.put("success", false);
            result.put("message", "กรุณาเข้าสู่ระบบใหม่อีกครั้ง");
            return result;
        }

        if (reason == null || reason.isBlank()) {
            result.put("success", false);
            result.put("message", "กรุณาระบุเหตุผลในการยกเลิก");
            return result;
        }

        try {
            tourBookingService.cancelTourBookingByManager(bookingId, manager.getManagerid(), reason);
            result.put("success", true);
        } catch (RuntimeException e) {
            result.put("success", false);
            result.put("message", e.getMessage());
        }
        return result;
    }
}