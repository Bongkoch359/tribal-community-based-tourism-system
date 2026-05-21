package com.example.miniproject.controller.HomestayOwner;

import com.example.miniproject.entity.Booking;
import com.example.miniproject.entity.Roomtype;
import com.example.miniproject.entity.enums.BookingStatus;
import com.example.miniproject.service.Homestay.RoomTypeService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import com.example.miniproject.repository.Member.BookingRepository;

import java.util.List;

@Controller
public class DashboardController {

    @Autowired
    private RoomTypeService roomTypeService;

    @Autowired
    private BookingRepository bookingRepository;

    @GetMapping("/owner/dashboard")
    public String dashboard(HttpSession session, Model model) {

        Integer ownerid = (Integer) session.getAttribute("ownerid");
        if (ownerid == null) return "redirect:/owner/login";

        Integer homestayid = (Integer) session.getAttribute("homestayid");

        // ─── ห้องพัก ───
        List<Roomtype> rooms = (homestayid != null)
                ? roomTypeService.getRoomTypesByHomestayId(homestayid)
                : new java.util.ArrayList<>();

        long totalRoomTypes = rooms.size();
       long availableRooms = rooms.stream()
        .filter(r -> "เปิดจอง".equals(r.getStatus()))
        .mapToLong(r -> r.getTotalrooms() != null ? r.getTotalrooms() : 0)
        .sum();

        // ─── การจองรอตรวจสอบ ───
        long pendingBookings = (homestayid != null)
                ? bookingRepository.countByRoomHomestayIdAndStatus(homestayid, BookingStatus.WAITING_APPROVAL)
                : 0;

        // ─── รายได้รวม (CONFIRMED) ───
        double totalRevenue = (homestayid != null)
                ? bookingRepository.sumRevenueByHomestayId(homestayid, BookingStatus.CONFIRMED)
                : 0.0;

        // ─── การจองล่าสุด 5 รายการ ───
        List<Booking> recentBookings = (homestayid != null)
                ? bookingRepository.findTop5ByHomestayId(homestayid)
                : new java.util.ArrayList<>();

        model.addAttribute("ownername",      session.getAttribute("ownername"));
        model.addAttribute("homestayname",   session.getAttribute("homestayname"));
        model.addAttribute("homestayid",     homestayid);
        model.addAttribute("rooms",          rooms);
        model.addAttribute("totalRoomTypes", totalRoomTypes);
        model.addAttribute("availableRooms", availableRooms);
        model.addAttribute("pendingBookings",pendingBookings);
        model.addAttribute("totalRevenue",   totalRevenue);
        model.addAttribute("recentBookings", recentBookings);

        return "Homestay/dashboard";
    }
}