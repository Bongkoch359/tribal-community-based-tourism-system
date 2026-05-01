package com.example.miniproject.controller.Owner;

import com.example.miniproject.entity.Communitymanager;
import com.example.miniproject.service.Owner.CommunityManagerService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
// ลบ @RequestMapping จากหัวคลาสออก เพื่อให้จัดการ Path ได้อิสระเหมือน Member
public class LoginManagerController {

    @Autowired
    private CommunityManagerService managerService;

    // ─── แสดงหน้า Login ───
    @GetMapping("/manager/login")
    public String openLoginPage(Model model) {
        return "manager_login";
    }

    // ─── ประมวลผลการ Login ───
    @PostMapping("/manager/login")
    public String loginManager(@RequestParam String email,
                               @RequestParam String password,
                               HttpSession session,
                               Model model) {

        Communitymanager manager = managerService.login(email, password);

        if (manager != null) {
            session.setAttribute("loggedInManager", manager);
            session.removeAttribute("loggedInMember");
            session.removeAttribute("loggedInOwner");

            // ตรงกับ @GetMapping("/manager/home") ด้านล่าง
            return "redirect:/manager/home"; 
        }

        model.addAttribute("errorMessage", "อีเมลหรือรหัสผ่านไม่ถูกต้อง");
        return "manager_login";
    }

    // ─── Logout ───
    @GetMapping("/manager/logout")
    public String logout(HttpSession session) {
        session.invalidate(); 
        return "redirect:/search"; 
    }

    // ─── หน้าหลักผู้จัดการ (หน้าเปล่าสำหรับทดสอบ) ───
    @GetMapping("/manager/home")
    public String showManagerHome() {
        return "manager_home"; 
    }
}