package com.example.miniproject.controller.HomestayOwner;

import com.example.miniproject.entity.Roomtype;
import com.example.miniproject.service.Homestay.RoomTypeService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

@Controller  // ✅ ขาดไป
public class DashboardController {

    @Autowired
    private RoomTypeService roomTypeService;

    @GetMapping("/owner/dashboard")
public String dashboard(HttpSession session, Model model) {

    Integer ownerid = (Integer) session.getAttribute("ownerid");
    if (ownerid == null) return "redirect:/owner/login";

    // ✅ แก้จาก String เป็น Integer
    Integer homestayid = (Integer) session.getAttribute("homestayid");

    List<Roomtype> rooms = (homestayid != null)
            ? roomTypeService.getRoomTypesByHomestayId(homestayid)
            : new java.util.ArrayList<>();

    long total     = rooms.size();
    long available = rooms.stream()
            .filter(r -> "available".equals(r.getStatus())).count();

    model.addAttribute("ownername",       session.getAttribute("ownername"));
    model.addAttribute("homestayname",    session.getAttribute("homestayname"));
    model.addAttribute("homestayid",      homestayid);
    model.addAttribute("rooms",           rooms);
    model.addAttribute("totalRooms",      total);
    model.addAttribute("availableRooms",  available);
    model.addAttribute("pendingBookings", 0);
    model.addAttribute("lockedRooms",     0);

    return "Homestay/dashboard";
}
}