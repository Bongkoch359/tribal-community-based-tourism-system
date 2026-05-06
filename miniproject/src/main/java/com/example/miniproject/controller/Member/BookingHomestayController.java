package com.example.miniproject.controller.Member;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.example.miniproject.entity.Member;
import com.example.miniproject.entity.Roomtype;
import com.example.miniproject.service.Homestay.RoomTypeService;
import com.example.miniproject.service.Member.BookingService; // Ensure this import is correct

import jakarta.servlet.http.HttpSession;

@Controller
public class BookingHomestayController {

    @Autowired
    private RoomTypeService roomtypeService;

    @Autowired
    private BookingService bookingService; // Added missing service

    @GetMapping("/booking/homestay/{id}")
    public String bookingPage(@PathVariable("id") String id, Model model) {
        Roomtype room = roomtypeService.getRoomById(id);
        model.addAttribute("room", room);
        return "Member/booking_homestay";
    }

    @PostMapping("/booking/homestay/create")
    public String createBooking(
            @RequestParam("roomtypeid") String roomtypeId,
            @RequestParam("checkin") String checkin,
            @RequestParam("checkout") String checkout,
            @RequestParam("guest") Integer guest,
            @RequestParam(value = "note", required = false) String note,
            HttpSession session) {

         Member member = (Member) session.getAttribute("loggedInMember");
        if (member == null) {
            return "redirect:/member/login";
        }

       String bookingId = bookingService.createHomestayBooking(
        member, roomtypeId, checkin, checkout, guest, note);

return "redirect:/member/bookings/detail/" + bookingId;
}
}