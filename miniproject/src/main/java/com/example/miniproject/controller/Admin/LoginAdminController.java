package com.example.miniproject.controller.Admin;

import java.util.Optional;
import java.util.List;
import org.springframework.ui.Model;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import com.example.miniproject.entity.Communitymanager;
import com.example.miniproject.entity.Homestayowner;
import com.example.miniproject.entity.enums.ManagerStatus;
import com.example.miniproject.repository.Homestay.HomestayOwnerRepository;
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

    @GetMapping("/")
public String home() {
    return "redirect:/search";
}

    // เปิดหน้า login (step 1 open page)
    @GetMapping("/admin/login")
    public String loginPage(HttpSession session) {
        if (session.getAttribute("loggedInAdmin") != null) {
            return "redirect:/admin/dashboard";
        }
        return "Admin/admin_login";
    }

// ── แก้ตรงนี้: เพิ่ม Model และดึงข้อมูลจริงจาก DB ──
    @GetMapping("/admin/dashboard")
    public String dashboard(HttpSession session, Model model) {
        if (session.getAttribute("loggedInAdmin") == null) {
            return "redirect:/admin/login";
        }
 
        // ข้อมูล Homestay
        List<Homestayowner> allOwners = ownerRepository.findAll();
 
        long homestayPending = allOwners.stream()
            .filter(o -> (o.getVerificationstatus() == null || !o.getVerificationstatus())
                      && !"REJECTED".equals(o.getAccountstatus()))
            .count();
 
        long homestayApproved = allOwners.stream()
            .filter(o -> Boolean.TRUE.equals(o.getVerificationstatus()))
            .count();
 
        long homestayRejected = allOwners.stream()
            .filter(o -> "REJECTED".equals(o.getAccountstatus()))
            .count();
 
        // ข้อมูล Manager
        List<Communitymanager> managers = managerService.getAll();
        long managerTotal  = managers != null ? managers.size() : 0;
        long managerActive = managers != null ? managers.stream()
            .filter(m -> m.getAccountstatus() == ManagerStatus.ACTIVE)
            .count() : 0;
 
        model.addAttribute("homestayTotal",    allOwners.size());
        model.addAttribute("homestayPending",  homestayPending);
        model.addAttribute("homestayApproved", homestayApproved);
        model.addAttribute("homestayRejected", homestayRejected);
        model.addAttribute("managerTotal",     managerTotal);
        model.addAttribute("managerActive",    managerActive);
 
        return "Admin/admin_dashboard";
    }
 

    // step 3-6: validate → loginAdmin() → return T/F
    @PostMapping("/admin/login")
    public String doLogin(@RequestParam String username,
                          @RequestParam String password,
                          HttpSession session,
                          RedirectAttributes redirectAttributes) {

        // step 3: validate (Alternate Flow 3.1)
        if (username == null || username.isBlank() ||
            password == null || password.isBlank()) {
            redirectAttributes.addFlashAttribute("message", "กรุณากรอกชื่อผู้ใช้และรหัสผ่าน");
            redirectAttributes.addFlashAttribute("alertType", "error");
            return "redirect:/admin/login";
        }

        // step 5: loginAdmin() → query DB
        Optional<Admin> adminOpt = adminService.login(username.trim(), password.trim());

        // step 5.1.1: return F → error message
        if (adminOpt.isEmpty()) {
            redirectAttributes.addFlashAttribute("message", "ชื่อผู้ใช้หรือรหัสผ่านไม่ถูกต้อง กรุณาลองใหม่อีกครั้ง");
            redirectAttributes.addFlashAttribute("alertType", "error");
            return "redirect:/admin/login";
        }

        // step 6: return T → เก็บ session แล้ว open homepage
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