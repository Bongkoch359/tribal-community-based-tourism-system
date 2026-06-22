package com.example.miniproject.controller.Member;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import java.util.List;
import com.example.miniproject.entity.enums.BookingStatus;

import com.example.miniproject.entity.Member;
import com.example.miniproject.entity.Tour;
import com.example.miniproject.service.Member.BookingService;
import com.example.miniproject.service.Member.TourService;

import jakarta.servlet.http.HttpSession;

@Controller
public class BookingTourController {

    @Autowired
    private TourService tourService;

    @Autowired
    private BookingService bookingService;

    // ════════════════════════════════════════════════════════
    // GET : หน้าจองทัวร์
    // ════════════════════════════════════════════════════════
    @GetMapping("/booking/tour/{id}")
    public String bookingPage(
            @PathVariable("id") String id,
            Model model,
            RedirectAttributes redirectAttributes) {

        Optional<Tour> optionalTour = tourService.getTourByIdWithBookings(id);

        // 3.1.1 — ไม่พบทัวร์ → แสดง error ที่หน้า booking ไม่ใช่ redirect ไป /search
        if (optionalTour.isEmpty()) {
            redirectAttributes.addFlashAttribute("errorMsg", "ไม่พบข้อมูลรายการทัวร์");
            return "redirect:/search";
        }

        Tour tour = optionalTour.get();

        int bookedSeats = tour.getBookingTourDetails().stream()
            .filter(td -> td.getBooking() != null
                   && td.getBooking().getBookingStatus() != BookingStatus.CANCEL)
            .collect(java.util.stream.Collectors.toMap(
                td -> td.getBooking().getBookingid(),
                td -> {
                    int a = td.getNumofadult() != null ? td.getNumofadult() : 0;
                    int c = td.getNumofchild() != null ? td.getNumofchild() : 0;
                    return a + c;
                },
                (existing, duplicate) -> existing
            ))
            .values().stream()
            .mapToInt(Integer::intValue)
            .sum();

        int availableSeats = Math.max(0, tour.getMaxSeatstour() - bookedSeats);

        System.out.println("=== DEBUG ===");
        System.out.println("maxSeats: " + tour.getMaxSeatstour());
        System.out.println("bookingDetails size: " + tour.getBookingTourDetails().size());
        tour.getBookingTourDetails().forEach(td -> {
            String bid = td.getBooking() != null ? td.getBooking().getBookingid() : "NULL";
            String status = td.getBooking() != null ? td.getBooking().getBookingStatus().toString() : "NULL";
            int a = td.getNumofadult() != null ? td.getNumofadult() : 0;
            int c = td.getNumofchild() != null ? td.getNumofchild() : 0;
            System.out.println("  -> bookingId=" + bid + " status=" + status + " adult=" + a + " child=" + c);
        });
        System.out.println("bookedSeats: " + bookedSeats);
        System.out.println("availableSeats: " + availableSeats);

        model.addAttribute("tour", tour);
        model.addAttribute("availableSeats", availableSeats);

        return "Member/booking_tour";
    }

    // ════════════════════════════════════════════════════════
    // POST : สร้างการจองทัวร์
    // ════════════════════════════════════════════════════════
    @PostMapping("/booking/tour/create")
    public String createBooking(
            @RequestParam("tourid") String tourId,
            @RequestParam("tourdate") String tourDate,
            @RequestParam("adult") Integer adult,
            @RequestParam(value = "children", defaultValue = "0") Integer children,
            @RequestParam(value = "note", required = false) String note,
            @RequestParam(value = "isBookerGoing", defaultValue = "true") Boolean isBookerGoing,
            @RequestParam(value = "pickuptype", defaultValue = "จุดรับส่วนกลาง") String pickuptype,
            @RequestParam(value = "pickuplocation", required = false) String pickuplocation,
            @RequestParam(value = "guestFirstname", required = false) List<String> guestFirstnames,
            @RequestParam(value = "guestLastname",  required = false) List<String> guestLastnames,
            HttpSession session,
            RedirectAttributes redirectAttributes) {

        Member member = (Member) session.getAttribute("loggedInMember");

        if (member == null) {
            return "redirect:/member/login";
        }

        try {
            String bookingId = bookingService.createTourBooking(
                member, tourId, tourDate, adult, children, note,
                isBookerGoing, pickuptype, pickuplocation,
                guestFirstnames, guestLastnames);

            return "redirect:/member/bookings/detail/" + bookingId;

        } catch (IllegalArgumentException e) {
            // 6.1 — ข้อมูลไม่ถูกต้องจาก server (เช่น วันย้อนหลัง, ที่นั่งไม่พอ)
            redirectAttributes.addFlashAttribute("errorMsg", e.getMessage());
            return "redirect:/booking/tour/" + tourId;

        } catch (Exception e) {
            // 8.1.1 — บันทึกไม่สำเร็จ → ข้อความตรงตาม spec
            redirectAttributes.addFlashAttribute(
                    "errorMsg",
                    "ไม่สามารถบันทึกข้อมูลการจองได้ กรุณาลองใหม่อีกครั้ง");
            return "redirect:/booking/tour/" + tourId;
        }
    }

    // ════════════════════════════════════════════════════════
    // POST : แก้ไขการจองทัวร์
    // ════════════════════════════════════════════════════════
    @PostMapping("/booking/tour/edit/{id}")
    public String editBooking(
            @PathVariable("id") String bookingId,
            @RequestParam("tourdate") String tourDate,
            @RequestParam(value = "adult", defaultValue = "1") Integer adult,
            @RequestParam(value = "children", defaultValue = "0") Integer children,
            @RequestParam(value = "note", required = false) String note,
            @RequestParam(value = "guestFirstname", required = false) String guestFirstname,
            @RequestParam(value = "guestLastname", required = false) String guestLastname,
            HttpSession session,
            RedirectAttributes redirectAttributes) {

        Member member = (Member) session.getAttribute("loggedInMember");

        if (member == null) {
            return "redirect:/member/login";
        }

        try {
            bookingService.editTourBooking(
                    bookingId,
                    member.getMemberid(),
                    tourDate,
                    adult,
                    children,
                    note,
                    guestFirstname,
                    guestLastname);

            redirectAttributes.addFlashAttribute("successMsg", "แก้ไขการจองเรียบร้อยแล้ว");
            return "redirect:/member/bookings/detail/" + bookingId;

        } catch (IllegalArgumentException | IllegalStateException e) {
            redirectAttributes.addFlashAttribute("errorMsg", e.getMessage());
            return "redirect:/member/bookings/detail/" + bookingId;

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute(
                    "errorMsg",
                    "ไม่สามารถแก้ไขข้อมูลการจองทัวร์ได้ กรุณาลองใหม่อีกครั้ง");
            return "redirect:/member/bookings/detail/" + bookingId;
        }
    }

    // ════════════════════════════════════════════════════════
    // POST : ยกเลิกการจองทัวร์
    // ════════════════════════════════════════════════════════
    @PostMapping("/booking/tour/cancel/{id}")
    public String cancelBooking(
            @PathVariable("id") String bookingId,
            HttpSession session,
            RedirectAttributes redirectAttributes) {

        Member member = (Member) session.getAttribute("loggedInMember");

        if (member == null) {
            return "redirect:/member/login";
        }

        try {
            bookingService.cancelTourBooking(bookingId, member.getMemberid());

            redirectAttributes.addFlashAttribute("successMsg", "ยกเลิกการจองเรียบร้อยแล้ว");
            return "redirect:/member/bookings/detail/" + bookingId;

        } catch (IllegalStateException e) {
            redirectAttributes.addFlashAttribute("errorMsg", e.getMessage());
            return "redirect:/member/bookings/detail/" + bookingId;

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute(
                    "errorMsg",
                    "ไม่สามารถยกเลิกการจองได้ กรุณาลองใหม่อีกครั้ง");
            return "redirect:/member/bookings/detail/" + bookingId;
        }
    }
}