package com.example.miniproject.controller.Member;

import com.example.miniproject.dto.Member.PaymentDTO;
import com.example.miniproject.entity.Booking;
import com.example.miniproject.entity.Member;
import com.example.miniproject.entity.Tourschedule;
import com.example.miniproject.entity.enums.BookingStatus;
import com.example.miniproject.entity.enums.BookingType;
import com.example.miniproject.repository.Tour.TourScheduleRepository;
import com.example.miniproject.service.Member.BookingService;
import com.example.miniproject.service.Member.PaymentService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.util.List;

@Controller
@RequestMapping("/member/bookings")
public class BookingController {

    @Autowired
    private BookingService bookingService;

    // ⬇️ NEW: ใช้ดึงรอบทัวร์ที่เปิดรับจอง สำหรับ dropdown เลือกวันในหน้าแก้ไขการจอง
    @Autowired
    private TourScheduleRepository tourScheduleRepository;

    // ⬇️ NEW: ใช้ดึงข้อมูลใบเสร็จ แยกตามประเภทการจอง (โฮมสเตย์ / ทัวร์)
    @Autowired
    @Qualifier("homestayPaymentService")
    private PaymentService homestayPaymentService;

    @Autowired
    @Qualifier("tourPaymentService")
    private PaymentService tourPaymentService;

    /**
     * GET /member/booking/list?type=TOUR&status=PENDING
     * type   : TOUR | HOMESTAY          (default = TOUR)
     * status : PENDING | WAITING_APPROVAL | CONFIRMED | CANCEL | null = ทั้งหมด
     */
    @GetMapping("/list")
    public String listBookings(
            @RequestParam(value = "type",   defaultValue = "TOUR") String typeStr,
            @RequestParam(value = "status", required = false)       String statusStr,
            HttpSession session,
            Model model) {

        // ── ดึง member จาก session ──────────────────────────────────
        Member member = (Member) session.getAttribute("loggedInMember");
        if (member == null) {
            return "redirect:/member/login";
        }
        String memberId = member.getMemberid();

        // ── แปลง parameter ──────────────────────────────────────────
        BookingType   activeType   = parseType(typeStr);
        BookingStatus activeStatus = parseStatus(statusStr);

        // ── ดึงรายการจอง ────────────────────────────────────────────
        List<Booking> bookings = bookingService
                .getBookingsByMemberTypeAndStatus(memberId, activeType, activeStatus);

        // ── นับจำนวน badge บน type tab ──────────────────────────────
        // ⚠️ หาก BookingType enum ของคุณใช้ชื่อต่างออกไป ให้แก้ตรงนี้
        long tourCount     = bookingService.countByMemberAndType(memberId, BookingType.TOUR);
        long homestayCount = bookingService.countByMemberAndType(memberId, BookingType.ACCOMMODATION);

        // ── นับจำนวนตาม status (ของ type ที่กำลังดูอยู่) ─────────────
        long countAll      = bookingService.countByMemberTypeAndStatus(memberId, activeType, null);
        long countPending  = bookingService.countByMemberTypeAndStatus(memberId, activeType, BookingStatus.PENDING);
        long countWaiting  = bookingService.countByMemberTypeAndStatus(memberId, activeType, BookingStatus.WAITING_APPROVAL);
        long countConfirmed= bookingService.countByMemberTypeAndStatus(memberId, activeType, BookingStatus.CONFIRMED);
        long countCancel   = bookingService.countByMemberTypeAndStatus(memberId, activeType, BookingStatus.CANCEL);
        long countCompleted = bookingService.countByMemberTypeAndStatus(memberId, activeType, BookingStatus.COMPLETED);
        model.addAttribute("countCompleted", countCompleted);

        // ── ส่งข้อมูลไปยัง view ──────────────────────────────────────
        model.addAttribute("member",         member);
        model.addAttribute("bookings",        bookings);
        model.addAttribute("activeType",      activeType.name());
        model.addAttribute("activeStatus",    statusStr);           // null = ทั้งหมด

        model.addAttribute("tourCount",       tourCount);
        model.addAttribute("homestayCount",   homestayCount);

        model.addAttribute("countAll",        countAll);
        model.addAttribute("countPending",    countPending);
        model.addAttribute("countWaiting",    countWaiting);
        model.addAttribute("countConfirmed",  countConfirmed);
        model.addAttribute("countCancel",     countCancel);

        return "member/list_booking";   // → templates/member/booking-list.html
    }

    // ── helpers ──────────────────────────────────────────────────────
    private BookingType parseType(String s) {
        try { return BookingType.valueOf(s.toUpperCase()); }
        catch (Exception e) { return BookingType.TOUR; }
    }

    private BookingStatus parseStatus(String s) {
        if (s == null || s.isBlank()) return null;
        try { return BookingStatus.valueOf(s.toUpperCase()); }
        catch (Exception e) { return null; }
    }



    @GetMapping("/detail/{bookingId}")
    public String bookingDetail(
            @PathVariable String bookingId,
            Model model) {

        Booking booking = bookingService.getBookingById(bookingId);

        if (booking == null) {
            return "redirect:/member/bookings";
        }

        bookingService.autoCompleteIfPastEndDate(booking); 

        model.addAttribute("booking", booking);

        // จองที่พัก
        if (booking.getBookingType() == BookingType.ACCOMMODATION) {

            return "Member/detail_bookinghomestay";
        }

        // จองทัวร์
        else if (booking.getBookingType() == BookingType.TOUR) {

            // ⬇️ NEW: ดึงรอบทัวร์ (schedule) ที่เปิดรับจองอยู่ตอนนี้ ของทัวร์ตัวนี้
            //         ส่งเข้า view เป็น "schedules" เพื่อให้ dropdown วันออกเดินทาง
            //         ในหน้าแก้ไขการจอง (detail_bookingtour.html) มีตัวเลือกให้เลือก
            //         เหมือนกับที่หน้าจองใหม่ (BookingTourController.bookingPage) ทำไว้
            if (booking.getTourDetails() != null && !booking.getTourDetails().isEmpty()
                    && booking.getTourDetails().get(0).getTour() != null) {

                String tourId = booking.getTourDetails().get(0).getTour().getTourid();

                List<Tourschedule> schedules = tourScheduleRepository
                        .findBookableSchedules(tourId, java.sql.Date.valueOf(LocalDate.now()));

                model.addAttribute("schedules", schedules);
            }

            return "Member/detail_bookingtour";
        }

       return "redirect:/member/bookings/list";
    }

    // ════════════════════════════════════════════════════════
    //  GET : หน้าใบเสร็จ (ใช้ได้ทั้งโฮมสเตย์และทัวร์)
    // ════════════════════════════════════════════════════════

    @GetMapping("/receipt/{bookingId}")
    public String viewReceipt(
            @PathVariable String bookingId,
            HttpSession session,
            Model model,
            RedirectAttributes redirectAttributes) {

        Member member = (Member) session.getAttribute("loggedInMember");
        if (member == null) return "redirect:/member/login";

        Booking booking = bookingService.getBookingById(bookingId);
        if (booking == null) {
            return "redirect:/member/bookings/list";
        }

        try {
            PaymentDTO receipt = (booking.getBookingType() == BookingType.ACCOMMODATION)
                    ? homestayPaymentService.getReceiptData(bookingId)
                    : tourPaymentService.getReceiptData(bookingId);

            model.addAttribute("booking", booking);
            model.addAttribute("receipt", receipt);
            return "Member/view_receipt";

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMsg", "ไม่พบข้อมูลใบเสร็จของการจองนี้");
            return "redirect:/member/bookings/detail/" + bookingId;
        }
    }
}