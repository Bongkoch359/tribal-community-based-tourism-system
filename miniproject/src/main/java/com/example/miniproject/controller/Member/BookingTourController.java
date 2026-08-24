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
import com.example.miniproject.service.Member.BookingTourService;
import com.example.miniproject.service.Member.TourService;
import com.example.miniproject.repository.Tour.TourScheduleRepository;
import com.example.miniproject.entity.Tourschedule;
import java.time.LocalDate;

import jakarta.servlet.http.HttpSession;

@Controller
public class BookingTourController {

    @Autowired
    private TourService tourService;

    @Autowired
    private BookingTourService tourBookingService;

    @Autowired
    private TourScheduleRepository tourScheduleRepository;

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

    int availableSeats = tourService.getAvailableSeats(tour);
    String seatLevel = tourService.getSeatStatusLevel(tour, availableSeats);


    List<Tourschedule> schedules = tourScheduleRepository
            .findBookableSchedules(id, java.sql.Date.valueOf(LocalDate.now()));

    model.addAttribute("tour", tour);
    model.addAttribute("availableSeats", availableSeats);
    model.addAttribute("seatLevel", seatLevel);
    model.addAttribute("insurancePrice", BookingTourService.INSURANCE_PRICE_PER_PERSON);
     model.addAttribute("schedules", schedules);

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
        @RequestParam(value = "wantinsurance", defaultValue = "false") Boolean wantInsurance,   // ✅ เพิ่ม
        @RequestParam(value = "guestFirstname", required = false) List<String> guestFirstnames,
        @RequestParam(value = "guestLastname",  required = false) List<String> guestLastnames,
        @RequestParam(value = "guestIdcard",    required = false) List<String> guestIdcards,   // ✅ เพิ่ม
        HttpSession session,
        RedirectAttributes redirectAttributes) {

    Member member = (Member) session.getAttribute("loggedInMember");

    if (member == null) {
        return "redirect:/member/login";
    }

    try {
        String bookingId = tourBookingService.createTourBooking(
            member, tourId, tourDate, adult, children, note,
            isBookerGoing, pickuptype, pickuplocation,
            wantInsurance, guestFirstnames, guestLastnames, guestIdcards);  

            redirectAttributes.addFlashAttribute("successMsg", "จองทัวร์สำเร็จแล้ว!");

        return "redirect:/member/bookings/detail/" + bookingId;

    } catch (IllegalArgumentException e) {
        redirectAttributes.addFlashAttribute("errorMsg", e.getMessage());
        return "redirect:/booking/tour/" + tourId;

    } catch (Exception e) {
        e.printStackTrace();
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
    @RequestParam(value = "pickuptype", required = false) String pickuptype,
    @RequestParam(value = "pickuplocation", required = false) String pickuplocation,
    @RequestParam(value = "guestId", required = false) List<String> guestIds,           // ✅ เพิ่ม
    @RequestParam(value = "guestFirstname", required = false) List<String> guestFirstnames,
    @RequestParam(value = "guestLastname", required = false) List<String> guestLastnames,
    @RequestParam(value = "guestIdcard", required = false) List<String> guestIdcards,
    HttpSession session,
    RedirectAttributes redirectAttributes) {

    Member member = (Member) session.getAttribute("loggedInMember");
    if (member == null) {
        return "redirect:/member/login";
    }

    try {
        tourBookingService.editTourBooking(
                bookingId, member.getMemberid(), tourDate, adult, children, note,
                pickuptype, pickuplocation,
                guestIds, guestFirstnames, guestLastnames, guestIdcards);

        redirectAttributes.addFlashAttribute("successMsg", "แก้ไขการจองเรียบร้อยแล้ว");
        return "redirect:/member/bookings/detail/" + bookingId;

    } catch (IllegalArgumentException | IllegalStateException e) {
        redirectAttributes.addFlashAttribute("errorMsg", e.getMessage());
        return "redirect:/member/bookings/detail/" + bookingId;
    } catch (Exception e) {
        redirectAttributes.addFlashAttribute("errorMsg", "ไม่สามารถแก้ไขข้อมูลการจองทัวร์ได้ กรุณาลองใหม่อีกครั้ง");
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
            tourBookingService.cancelTourBooking(bookingId, member.getMemberid());

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