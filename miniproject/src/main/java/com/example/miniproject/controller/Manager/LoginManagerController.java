package com.example.miniproject.controller.Manager;

import com.example.miniproject.entity.Communitymanager;
import com.example.miniproject.service.Owner.CommunityManagerService;

import jakarta.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/manager/login")
public class LoginManagerController {

    @Autowired
    private CommunityManagerService managerService;

    // เปิดหน้า login
    @GetMapping
    public String openLoginPage() {
        return "Tour/manager_login";
    }

    // ตรวจสอบ login
    @PostMapping
    public String loginManager(@RequestParam String email,
                               @RequestParam String password,
                               HttpSession session,
                               Model model) {

        Communitymanager manager =
                managerService.login(email, password);

        // login สำเร็จ
        if (manager != null) {

            session.setAttribute("loggedInManager", manager);

            return "redirect:/manager/home";
        }

        // login ไม่สำเร็จ
        model.addAttribute("errorMessage",
                "อีเมลหรือรหัสผ่านไม่ถูกต้อง");

        return "Tour/manager_login";
    }

    // logout
    @GetMapping("/logout")
    public String logout(HttpSession session) {

        session.invalidate();

        return "redirect:/manager/login";
    }
}