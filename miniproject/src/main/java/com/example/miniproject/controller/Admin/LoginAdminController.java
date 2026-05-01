package com.example.miniproject.controller.Admin;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.example.miniproject.entity.Admin;
import com.example.miniproject.service.Admin.AdminService;

import jakarta.servlet.http.HttpSession;

@Controller
public class LoginAdminController {

    @Autowired
    private AdminService adminService;

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

    @GetMapping("/admin/dashboard")
    public String dashboard(HttpSession session) {
        if (session.getAttribute("loggedInAdmin") == null) {
            return "redirect:/admin/login";
        }
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