package com.example.miniproject.controller.Admin;

import java.util.Optional;
import java.util.List;
import org.springframework.ui.Model;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import com.example.miniproject.entity.Communitymanager;
import com.example.miniproject.entity.Homestayowner;
import com.example.miniproject.entity.Tour;
import com.example.miniproject.entity.enums.BookingStatus;
import com.example.miniproject.entity.enums.ManagerStatus;
import com.example.miniproject.repository.Homestay.HomestayOwnerRepository;
import com.example.miniproject.repository.Homestay.HomestayRepository;
import com.example.miniproject.repository.Member.BookingRepository;
import com.example.miniproject.repository.Member.TourRepository;
import com.example.miniproject.service.Admin.ManagerService;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.example.miniproject.entity.Admin;
import com.example.miniproject.service.Admin.AdminService;

import jakarta.servlet.http.HttpSession;

@Controller
public class LoginAdminController {

    @Autowired
    private AdminService adminService;

    @Autowired
    private HomestayOwnerRepository ownerRepository;

    @Autowired
    private ManagerService managerService;

    // ── เพิ่มใหม่ สำหรับสถิติ dashboard ──
    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private TourRepository tourRepository;

    @Autowired
    private HomestayRepository homestayRepository;

    @GetMapping("/")
    public String home() {
        return "redirect:/search";
    }

    @GetMapping("/admin/login")
    public String loginPage(HttpSession session) {
        if (session.getAttribute("loggedInAdmin") != null) {
            return "redirect:/admin/dashboard";
        }
        return "Admin/admin_login";
    }


    @GetMapping("/admin/dashboard")
    public String dashboard(HttpSession session, Model model) {
        if (session.getAttribute("loggedInAdmin") == null) {
            return "redirect:/admin/login";
        }

        List<Homestayowner> allOwners = ownerRepository.findAll();

        long homestayPending = allOwners.stream()
            .filter(o -> (o.getVerificationstatus() == null || !o.getVerificationstatus())
                      && !"REJECTED".equals(o.getAccountstatus()))
            .count();
        model.addAttribute("pendingCount", homestayPending);

        long homestayApproved = allOwners.stream()
            .filter(o -> Boolean.TRUE.equals(o.getVerificationstatus()))
            .count();

        long homestayRejected = allOwners.stream()
            .filter(o -> "REJECTED".equals(o.getAccountstatus()))
            .count();

        List<Communitymanager> managers = managerService.getAll();
        long managerTotal  = managers != null ? managers.size() : 0;
        long managerActive = managers != null ? managers.stream()
            .filter(m -> m.getAccountstatus() == ManagerStatus.ACTIVE)
            .count() : 0;

        double totalRevenue   = bookingRepository.sumTotalRevenue(BookingStatus.CONFIRMED);
        long bookingPending   = bookingRepository.countPendingBookings();
        long bookingConfirmed = bookingRepository.countByBookingStatus(BookingStatus.CONFIRMED);
        long bookingCancel    = bookingRepository.countByBookingStatus(BookingStatus.CANCEL);

  
        List<Tour> topTours = tourRepository.findTopToursByBookingCount(PageRequest.of(0, 5));

   
        List<Object[]> lowestRated = homestayRepository.findLowestRatedHomestays(PageRequest.of(0, 5));

       
        model.addAttribute("homestayTotal",    allOwners.size());
        model.addAttribute("homestayPending",  homestayPending);
        model.addAttribute("homestayApproved", homestayApproved);
        model.addAttribute("homestayRejected", homestayRejected);
        model.addAttribute("managerTotal",     managerTotal);
        model.addAttribute("managerActive",    managerActive);

        model.addAttribute("totalRevenue",     totalRevenue);
        model.addAttribute("bookingPending",   bookingPending);
        model.addAttribute("bookingConfirmed", bookingConfirmed);
        model.addAttribute("bookingCancel",    bookingCancel);

        model.addAttribute("topTours",    topTours);
        model.addAttribute("lowestRated", lowestRated);

        return "Admin/admin_dashboard";
    }

    @PostMapping("/admin/login")
    public String doLogin(@RequestParam String username,
                          @RequestParam String password,
                          HttpSession session,
                          RedirectAttributes redirectAttributes) {

    
        if (username == null || username.isBlank() ||
            password == null || password.isBlank()) {
            redirectAttributes.addFlashAttribute("message", "กรุณากรอกชื่อผู้ใช้และรหัสผ่าน");
            redirectAttributes.addFlashAttribute("alertType", "error");
            return "redirect:/admin/login";
        }

        Optional<Admin> adminOpt = adminService.login(username.trim(), password.trim());

        if (adminOpt.isEmpty()) {
            redirectAttributes.addFlashAttribute("message", "ชื่อผู้ใช้หรือรหัสผ่านไม่ถูกต้อง กรุณาลองใหม่อีกครั้ง");
            redirectAttributes.addFlashAttribute("alertType", "error");
            return "redirect:/admin/login";
        }


        session.setAttribute("loggedInAdmin", adminOpt.get());

        redirectAttributes.addFlashAttribute("message", "เข้าสู่ระบบสำเร็จ!");
        redirectAttributes.addFlashAttribute("alertType", "success");
        return "redirect:/admin/dashboard";
    }

    // logout
    @GetMapping("/admin/logout")
    public String logout(HttpSession session, RedirectAttributes redirectAttributes) {
        session.invalidate();
        redirectAttributes.addFlashAttribute("message", "ออกจากระบบเรียบร้อยแล้ว");
        redirectAttributes.addFlashAttribute("alertType", "error");
        return "redirect:/admin/login";
    }
}