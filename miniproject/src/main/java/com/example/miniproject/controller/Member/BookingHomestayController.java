package com.example.miniproject.controller.Member;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.example.miniproject.dto.Member.PaymentDTO;
import com.example.miniproject.entity.Booking;
import com.example.miniproject.entity.Member;
import com.example.miniproject.entity.Roomtype;
import com.example.miniproject.entity.enums.BookingStatus;
import com.example.miniproject.service.Homestay.RoomTypeService;
import com.example.miniproject.service.Member.BookingService;
import com.example.miniproject.service.Member.PaymentService;

import jakarta.servlet.http.HttpSession;

@Controller
public class BookingHomestayController {

    @Autowired
    private RoomTypeService roomtypeService;

    @Autowired
    private BookingService bookingService;

    @Autowired
    @Qualifier("homestayPaymentService")
    private PaymentService paymentService;

    // ════════════════════════════════════════════════════════
    //  GET : หน้าจองโฮมสเตย์
    // ════════════════════════════════════════════════════════

    @GetMapping("/booking/homestay/{id}")
    public String bookingPage(@PathVariable("id") String id, Model model) {
        Roomtype room = roomtypeService.getRoomById(id);
        model.addAttribute("room", room);
        return "Member/booking_homestay";
    }

    // ════════════════════════════════════════════════════════
    //  POST : สร้างการจองใหม่
    // ════════════════════════════════════════════════════════

    @PostMapping("/booking/homestay/create")
    public String createBooking(
            @RequestParam("roomtypeid")                               String  roomtypeId,
            @RequestParam("checkin")                                  String  checkin,
            @RequestParam("checkout")                                 String  checkout,
            @RequestParam("numofrooms")                               Integer numofrooms,
            @RequestParam("guest")                                    Integer guest,
            @RequestParam(value = "children",      defaultValue = "0") Integer children,
            @RequestParam(value = "note",          required = false)   String  note,
            @RequestParam(value = "isBookerGoing", defaultValue = "true") Boolean isBookerGoing,
            @RequestParam(value = "guestFirstname", required = false)  String  guestFirstname,
            @RequestParam(value = "guestLastname",  required = false)  String  guestLastname,
            HttpSession session,
            RedirectAttributes redirectAttributes) {

        Member member = (Member) session.getAttribute("loggedInMember");
        if (member == null) return "redirect:/member/login";

        try {
            String bookingId = bookingService.createHomestayBooking(
                    member, roomtypeId, checkin, checkout,
                    numofrooms, guest, children,
                    note, isBookerGoing,
                    guestFirstname, guestLastname);

            return "redirect:/member/bookings/detail/" + bookingId;

        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("errorMsg", e.getMessage());
            return "redirect:/booking/homestay/" + roomtypeId;

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMsg", "เกิดข้อผิดพลาด กรุณาลองใหม่อีกครั้ง");
            return "redirect:/booking/homestay/" + roomtypeId;
        }
    }

    // ════════════════════════════════════════════════════════
    //  POST : แก้ไขการจอง
    // ════════════════════════════════════════════════════════

    @PostMapping("/booking/homestay/edit/{id}")
    public String editBooking(
            @PathVariable("id") String bookingId,
            @RequestParam("checkin")                                   String  checkin,
            @RequestParam("checkout")                                  String  checkout,
            @RequestParam(value = "numofrooms",    defaultValue = "1") Integer numofrooms,
            @RequestParam(value = "guest",         defaultValue = "1") Integer guest,
            @RequestParam(value = "children",      defaultValue = "0") Integer children,
            @RequestParam(value = "note",          required = false)   String  note,
            @RequestParam(value = "guestFirstname", required = false)  String  guestFirstname,  // ← เพิ่ม
            @RequestParam(value = "guestLastname",  required = false)  String  guestLastname,   // ← เพิ่ม
            HttpSession session,
            RedirectAttributes redirectAttributes) {

        Member member = (Member) session.getAttribute("loggedInMember");
        if (member == null) return "redirect:/member/login";

        try {
            bookingService.editHomestayBooking(
                    bookingId, member.getMemberid(),
                    checkin, checkout,
                    numofrooms, guest, children,
                    note,
                    guestFirstname, guestLastname);   // ← เพิ่ม

            redirectAttributes.addFlashAttribute("successMsg", "แก้ไขการจองเรียบร้อยแล้ว");
            return "redirect:/member/bookings/detail/" + bookingId;

        } catch (IllegalArgumentException | IllegalStateException e) {
            redirectAttributes.addFlashAttribute("errorMsg", e.getMessage());
            return "redirect:/member/bookings/detail/" + bookingId;

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMsg", "ไม่สามารถแก้ไขข้อมูลการจองห้องพักได้ กรุณาลองใหม่อีกครั้ง");
            return "redirect:/member/bookings/detail/" + bookingId;
        }
    }
    // ════════════════════════════════════════════════════════
    //  POST : ยกเลิกการจอง
    // ════════════════════════════════════════════════════════

    @PostMapping("/booking/homestay/cancel/{id}")
    public String cancelBooking(
            @PathVariable("id") String bookingId,
            HttpSession session,
            RedirectAttributes redirectAttributes) {

        Member member = (Member) session.getAttribute("loggedInMember");
        if (member == null) return "redirect:/member/login";

        try {
            bookingService.cancelHomestayBooking(bookingId, member.getMemberid());

            redirectAttributes.addFlashAttribute("successMsg", "ยกเลิกการจองเรียบร้อยแล้ว");
            return "redirect:/member/bookings/detail/" + bookingId;

        } catch (IllegalStateException e) {
            redirectAttributes.addFlashAttribute("errorMsg", e.getMessage());
            return "redirect:/member/bookings/detail/" + bookingId;

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMsg", "ไม่สามารถยกเลิกการจองได้ กรุณาลองใหม่อีกครั้ง");
            return "redirect:/member/bookings/detail/" + bookingId;
        }
    }


    // ════════════════════════════════════════════════════════
    //  GET : ใบเสร็จ
    // ════════════════════════════════════════════════════════

    @GetMapping("/member/receipt/{id}")
    public String viewReceipt(
            @PathVariable("id") String bookingId,
            HttpSession session,
            Model model,
            RedirectAttributes redirectAttributes) {

        Member member = (Member) session.getAttribute("loggedInMember");
        if (member == null) return "redirect:/member/login";

        Booking booking = bookingService.getBookingById(bookingId);
        if (booking == null) {
            redirectAttributes.addFlashAttribute("errorMsg", "ไม่พบข้อมูลการจอง");
            return "redirect:/member/bookings/list";
        }

        // กันคนอื่นเดา bookingId แล้วดูใบเสร็จของคนอื่น
        if (booking.getMember() == null
                || !booking.getMember().getMemberid().equals(member.getMemberid())) {
            redirectAttributes.addFlashAttribute("errorMsg", "ไม่มีสิทธิ์เข้าถึงใบเสร็จนี้");
            return "redirect:/member/bookings/list";
        }

        // ใบเสร็จควรดูได้เฉพาะที่จ่ายแล้ว/เสร็จสิ้นแล้ว
        if (booking.getBookingStatus() != BookingStatus.CONFIRMED
                && booking.getBookingStatus() != BookingStatus.COMPLETED) {
            redirectAttributes.addFlashAttribute("errorMsg", "การจองนี้ยังไม่สามารถออกใบเสร็จได้");
            return "redirect:/member/bookings/detail/" + bookingId;
        }

        try {
            PaymentDTO receipt = paymentService.getReceiptData(bookingId);
            model.addAttribute("receipt", receipt);
            model.addAttribute("booking", booking);
            return "member/view_receipt"; // templates/member/receipt.html
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMsg", "ไม่พบข้อมูลการชำระเงิน: " + e.getMessage());
            return "redirect:/member/bookings/detail/" + bookingId;
        }
    }
}