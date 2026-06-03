package com.example.miniproject.controller.HomestayOwner;

import com.example.miniproject.entity.Booking;
import com.example.miniproject.entity.Homestay;
import com.example.miniproject.entity.Homestayowner;
import com.example.miniproject.entity.Roomtype;
import com.example.miniproject.entity.enums.BookingStatus;
import com.example.miniproject.service.Homestay.HomestayService;
import com.example.miniproject.service.Homestay.RoomTypeService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.example.miniproject.repository.Member.BookingRepository;

import java.util.List;

@Controller
public class DashboardController {

    @Autowired
    private RoomTypeService roomTypeService;

    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private HomestayService homestayService;

    @Autowired
    private com.example.miniproject.service.Homestay.HomestayOwnerService ownerService;

    @GetMapping("/owner/dashboard")
public String dashboard(
        @RequestParam(value = "homestayid", required = false) Integer homestayid,
        HttpSession session,
        Model model) {

    Integer ownerid = (Integer) session.getAttribute("ownerid");
    if (ownerid == null) return "redirect:/owner/login";

    // ดึงโฮมสเตย์ทั้งหมดของเจ้าของคนนี้
    List<Homestay> myHomestays = homestayService.getHomestaysByOwnerId(ownerid);

    // ถ้าไม่ได้ส่ง homestayid มา → ใช้อันแรกของรายการ
    if (homestayid == null) {
        if (!myHomestays.isEmpty()) {
            homestayid = myHomestays.get(0).getHomestayid();
        }
    }

// ดึงชื่อโฮมสเตย์ที่เลือก
        String homestayname = "";
        if (homestayid != null) {
        final Integer selectedId = homestayid;
        homestayname = myHomestays.stream()
            .filter(hs -> selectedId.equals(hs.getHomestayid()))
            .map(Homestay::getHomestayname)
            .findFirst()
            .orElse("");
        }

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

    model.addAttribute("ownername",       session.getAttribute("ownername"));
    model.addAttribute("homestayname",    homestayname);
    model.addAttribute("homestayid",      homestayid);
    model.addAttribute("myHomestays",     myHomestays);
    model.addAttribute("rooms",           rooms);
    model.addAttribute("totalRoomTypes",  totalRoomTypes);
    model.addAttribute("availableRooms",  availableRooms);
    model.addAttribute("pendingBookings", pendingBookings);
    model.addAttribute("totalRevenue",    totalRevenue);
    model.addAttribute("recentBookings",  recentBookings);

    // ── ตรวจสอบข้อมูลธนาคาร ──
    try {
        Homestayowner owner = ownerService.getProfile(ownerid);
        boolean bankInfoMissing = owner.getBankName()      == null || owner.getBankName().isBlank()
                               || owner.getAccountNumber() == null || owner.getAccountNumber().isBlank()
                               || owner.getAccountName()   == null || owner.getAccountName().isBlank();
        model.addAttribute("bankInfoMissing", bankInfoMissing);
        model.addAttribute("ownerEmail", owner.getEmail());
    } catch (Exception e) {
        model.addAttribute("bankInfoMissing", false);
    }

    return "Homestay/dashboard";
}
}