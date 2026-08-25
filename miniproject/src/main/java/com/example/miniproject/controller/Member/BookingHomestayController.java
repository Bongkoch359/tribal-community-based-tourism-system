package com.example.miniproject.controller.Member;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.example.miniproject.entity.Member;
import com.example.miniproject.entity.Roomtype;
import com.example.miniproject.service.Homestay.RoomTypeService;
import com.example.miniproject.service.Member.BookingService;

import jakarta.servlet.http.HttpSession;

@Controller
public class BookingHomestayController {

    @Autowired
    private RoomTypeService roomtypeService;

    @Autowired
    private BookingService bookingService;

    // ════════════════════════════════════════════════════════
    //  GET : หน้าจองโฮมสเตย์
    // ════════════════════════════════════════════════════════

 @GetMapping("/booking/homestay/{id}")
public String bookingPage(
        @PathVariable("id") String id,
        @RequestParam(value = "checkin", required = false) String checkin,
        @RequestParam(value = "checkout", required = false) String checkout,
        @RequestParam(value = "guest", required = false) Integer guest,
        Model model) {

    Roomtype room = roomtypeService.getRoomById(id);
    model.addAttribute("room", room);
    model.addAttribute("checkinParam", checkin);
    model.addAttribute("checkoutParam", checkout);
    model.addAttribute("guestParam", guest != null ? guest : 1);
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
            @RequestParam(value = "guestFirstname", required = false)  String  guestFirstname,
            @RequestParam(value = "guestLastname",  required = false)  String  guestLastname,
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
                    guestFirstname, guestLastname);

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
        @RequestParam(value = "cancelReason", required = false) String cancelReason,
        HttpSession session,
        RedirectAttributes redirectAttributes) {

    Member member = (Member) session.getAttribute("loggedInMember");
    if (member == null) return "redirect:/member/login";

    try {
        bookingService.cancelHomestayBooking(bookingId, member.getMemberid(), cancelReason);

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
}