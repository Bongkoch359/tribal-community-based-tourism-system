package com.example.miniproject.controller.HomestayOwner;

import com.example.miniproject.entity.Booking;
import com.example.miniproject.entity.Bookingroomdetail;
import com.example.miniproject.entity.Homestay;
import com.example.miniproject.entity.enums.BookingStatus;
import com.example.miniproject.service.Homestay.BookingOwnerService;
import com.example.miniproject.service.Homestay.HomestayService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import java.util.*;
import java.util.stream.Collectors;

@Controller
public class BookingOwnerController {

    @Autowired
    private BookingOwnerService bookingOwnerService;

    @Autowired
    private HomestayService homestayService;


    // ─── GET: รายการจองทั้งหมด ────────────────────────────────────────────────
    @GetMapping("/owner/bookings")
    public String listBookings(
            @RequestParam(value = "homestayid",  required = false) Integer homestayid,
            @RequestParam(value = "status",      required = false) String status,
            HttpSession session,
            Model model) {

        if (session.getAttribute("ownerid") == null) return "redirect:/owner/login";

        Integer ownerid   = (Integer) session.getAttribute("ownerid");
        String  ownername = (String)  session.getAttribute("ownername");

        // ดึงโฮมสเตย์ทั้งหมดของเจ้าของ
        List<Homestay> myHomestays = homestayService.getHomestaysByOwnerId(ownerid);

        // fallback → อันแรก
        if (homestayid == null && !myHomestays.isEmpty()) {
            homestayid = myHomestays.get(0).getHomestayid();
        }

        // ชื่อโฮมสเตย์ที่เลือก
        String homestayname = "";
        if (homestayid != null) {
            final Integer selectedId = homestayid;
            homestayname = myHomestays.stream()
                    .filter(hs -> selectedId.equals(hs.getHomestayid()))
                    .map(Homestay::getHomestayname)
                    .findFirst().orElse("");
        }

        // แปลง status string → enum
        BookingStatus statusEnum = null;
        if (status != null && !status.isBlank()) {
            try { statusEnum = BookingStatus.valueOf(status); }
            catch (IllegalArgumentException ignored) {}
        }

        // ดึงการจอง
        List<Booking> bookings = (homestayid != null)
                ? bookingOwnerService.getBookingsByHomestayIdAndStatus(homestayid, statusEnum)
                : Collections.emptyList();

        // แปลงเป็น view model
        List<Map<String, Object>> bookingViews = buildBookingViews(bookings);

        // นับสถิติ
        long countAll      = bookings.size();
        long countWaiting  = bookings.stream().filter(b -> b.getBookingStatus() == BookingStatus.WAITING_APPROVAL).count();
        long countConfirmed = bookings.stream()
        .filter(b -> b.getBookingStatus() == BookingStatus.CONFIRMED
                  || b.getBookingStatus() == BookingStatus.COMPLETED)
        .count();
        long countCancel   = bookings.stream().filter(b -> b.getBookingStatus() == BookingStatus.CANCEL).count();

        model.addAttribute("ownername",      ownername != null ? ownername : "Owner");
        model.addAttribute("homestayid",     homestayid);
        model.addAttribute("homestayname",   homestayname);
        model.addAttribute("myHomestays",    myHomestays);
        model.addAttribute("bookings",       bookingViews);
        model.addAttribute("activeStatus",   status != null ? status : "");
        model.addAttribute("countAll",       countAll);
        model.addAttribute("countWaiting",   countWaiting);
        model.addAttribute("countConfirmed", countConfirmed);
        model.addAttribute("countCancel",    countCancel);

        return "Homestay/listBooking";
    }

    // ─── GET: รายละเอียดการจอง ────────────────────────────────────────────────
    @GetMapping("/owner/bookings/detail")
    public String bookingDetail(
            @RequestParam("bookingid") String bookingid,
            HttpSession session,
            Model model) {

        if (session.getAttribute("ownerid") == null) return "redirect:/owner/login";

        Booking booking = bookingOwnerService.getBookingDetail(bookingid);
        if (booking == null) return "redirect:/owner/bookings";

        String ownername = (String) session.getAttribute("ownername");

        model.addAttribute("ownername", ownername != null ? ownername : "Owner");
        model.addAttribute("booking",   booking);

        return "Homestay/bookingDetail";
    }

    // ─── POST: ยืนยันการจอง ──────────────────────────────────────────────────
    @PostMapping("/owner/bookings/confirm")
    @ResponseBody
    public ResponseEntity<?> confirmBooking(
            @RequestParam("bookingid")  String  bookingid,
            @RequestParam("homestayid") Integer homestayid,
            HttpSession session) {

        if (session.getAttribute("ownerid") == null)
            return ResponseEntity.status(401).body(Map.of("success", false, "message", "กรุณาเข้าสู่ระบบ"));

        try {
            bookingOwnerService.confirmBooking(bookingid, homestayid);
            return ResponseEntity.ok(Map.of("success", true));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", e.getMessage()));
        }
    }

    // ─── POST: ยกเลิกการจอง (โดย owner) ────────────────────────────────────
    @PostMapping("/owner/bookings/cancel")
    @ResponseBody
    public ResponseEntity<?> cancelBooking(
            @RequestParam("bookingid")  String  bookingid,
            @RequestParam("homestayid") Integer homestayid,
            HttpSession session) {

        if (session.getAttribute("ownerid") == null)
            return ResponseEntity.status(401).body(Map.of("success", false, "message", "กรุณาเข้าสู่ระบบ"));

        try {
            bookingOwnerService.cancelBookingByOwner(bookingid, homestayid);
            return ResponseEntity.ok(Map.of("success", true));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", e.getMessage()));
        }
    }

    // ─── helper: แปลง Booking → Map สำหรับ Thymeleaf ────────────────────────
    private List<Map<String, Object>> buildBookingViews(List<Booking> bookings) {
        return bookings.stream().map(b -> {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("bookingid",     b.getBookingid());
            m.put("bookingStatus", b.getBookingStatus() != null ? b.getBookingStatus().name() : "");
            m.put("bookingdate",   b.getBookingdate());
            m.put("totalamount",   b.getTotalamount() != null ? b.getTotalamount() : 0.0);
            m.put("numofguest",    b.getNumofguest() != null   ? b.getNumofguest()  : 0);

            // ชื่อผู้จอง
            if (b.getMember() != null) {
                m.put("membername", b.getMember().getFirstname() + " " + b.getMember().getLastname());
                m.put("memberphone", b.getMember().getPhone() != null ? b.getMember().getPhone() : "");
            } else {
                m.put("membername",  "-");
                m.put("memberphone", "");
            }

            // ข้อมูลห้องพักจาก roomDetails
            if (b.getRoomDetails() != null && !b.getRoomDetails().isEmpty()) {
                Bookingroomdetail rd = b.getRoomDetails().get(0);
                m.put("roomtypename", rd.getRoomtype() != null ? rd.getRoomtype().getTypename() : "-");
                m.put("checkindate",  rd.getCheckindate());
                m.put("checkoutdate", rd.getCheckoutdate());
                m.put("numofrooms",   rd.getNumofrooms() != null ? rd.getNumofrooms() : 1);

                // คำนวณจำนวนคืน
                if (rd.getCheckindate() != null && rd.getCheckoutdate() != null) {
                    long nights = rd.getCheckoutdate().toLocalDate()
                            .toEpochDay() - rd.getCheckindate().toLocalDate().toEpochDay();
                    m.put("nights", nights);
                } else {
                    m.put("nights", 0);
                }
            } else {
                m.put("roomtypename", "-");
                m.put("checkindate",  null);
                m.put("checkoutdate", null);
                m.put("numofrooms",   1);
                m.put("nights",       0);
            }

            return m;
        }).collect(Collectors.toList());
    }
}