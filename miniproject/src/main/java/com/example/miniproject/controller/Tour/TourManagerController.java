package com.example.miniproject.controller.Tour;

import com.example.miniproject.entity.Communitymanager;
import com.example.miniproject.service.Tour.CommunityManagerService;

import jakarta.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class TourManagerController {

    @Autowired
    private CommunityManagerService managerService;

    // เปิดหน้า login
    @GetMapping("/manager/login")
    public String openLoginPage() {
        return "Tour/manager_login";
    }

    // ตรวจสอบ login
    @PostMapping("/manager/login")
    public String loginManager(@RequestParam String email,
                               @RequestParam String password,
                               HttpSession session,
                               Model model) {

        Communitymanager manager =
                managerService.login(email, password);

        // login สำเร็จ
        if (manager != null) {
            if (manager.getAccountstatus() != null && 
           ("INACTIVE".equals(manager.getAccountstatus().toString()) || "SUSPENDED".equals(manager.getAccountstatus().toString()))) {
            
            model.addAttribute("errorMessage", "บัญชีของคุณถูกระงับการใช้งาน กรุณาติดต่อผู้ดูแลระบบ");
            return "Tour/manager_login";
        }

            session.setAttribute("loggedInManager", manager);

           return "redirect:/manager/dashboard";
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

     // ─── แสดงหน้าแก้ไขโปรไฟล์ ───
    @GetMapping("manager/profile")
    public String profilePage(HttpSession session, Model model) {
        Communitymanager manager = (Communitymanager) session.getAttribute("loggedInManager");
        if (manager == null) {
            return "redirect:/manager/login";
        }
        // ดึงข้อมูลล่าสุดจาก DB
        Communitymanager fresh = managerService.getById(manager.getManagerid());
        model.addAttribute("loggedInManager", fresh);
        return "Tour/managerProfile";
    }
 
    // ─── บันทึกการแก้ไขโปรไฟล์ ───
    @PostMapping("/manager/profile/update")
    public String updateProfile(
            @RequestParam String firstname,
            @RequestParam String lastname,
            @RequestParam String email,
            @RequestParam(required = false) String phone,
            @RequestParam(required = false, defaultValue = "") String currentPassword,
            @RequestParam(required = false, defaultValue = "") String newPassword,
            @RequestParam(required = false, defaultValue = "") String confirmPassword,
            HttpSession session,
            RedirectAttributes redirectAttributes) {
 
        Communitymanager manager = (Communitymanager) session.getAttribute("loggedInManager");
        if (manager == null) {
            return "redirect:/manager/login";
        }
 
        try {
            boolean wantsChangePassword = !newPassword.isBlank()
                                       || !currentPassword.isBlank()
                                       || !confirmPassword.isBlank();
 
            if (wantsChangePassword) {
                // ตรวจ confirm ฝั่ง server อีกครั้ง
                if (!newPassword.equals(confirmPassword)) {
                    redirectAttributes.addFlashAttribute("errorMessage", "รหัสผ่านใหม่และยืนยันรหัสผ่านไม่ตรงกัน");
                    return "redirect:/manager/profile";
                }
                if (newPassword.length() < 6) {
                    redirectAttributes.addFlashAttribute("errorMessage", "รหัสผ่านใหม่ต้องมีอย่างน้อย 6 ตัวอักษร");
                    return "redirect:/manager/profile";
                }
                managerService.updateProfileWithPassword(
                        manager.getManagerid(), firstname, lastname, email, phone,
                        currentPassword, newPassword);
            } else {
                managerService.updateProfile(
                        manager.getManagerid(), firstname, lastname, email, phone);
            }
 
            // อัปเดต session ให้แสดงชื่อล่าสุดใน navbar
            Communitymanager updated = managerService.getById(manager.getManagerid());
            session.setAttribute("loggedInManager", updated);
 
            redirectAttributes.addFlashAttribute("successMessage", "บันทึกข้อมูลเรียบร้อยแล้ว");
 
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
 
        return "redirect:/manager/profile";
    }

    // ─── บันทึกข้อมูลธนาคาร ───
    @PostMapping("/manager/profile/update-bank")
    public String updateBankInfo(
            @RequestParam(required = false, defaultValue = "") String bankName,
            @RequestParam(required = false, defaultValue = "") String accountNumber,
            HttpSession session,
            RedirectAttributes redirectAttributes) {

        Communitymanager manager = (Communitymanager) session.getAttribute("loggedInManager");
        if (manager == null) {
            return "redirect:/manager/login";
        }

        try {
            managerService.updateBankInfo(manager.getManagerid(), bankName, accountNumber);

            Communitymanager updated = managerService.getById(manager.getManagerid());
            session.setAttribute("loggedInManager", updated);

            redirectAttributes.addFlashAttribute("successMessage", "บันทึกข้อมูลธนาคารเรียบร้อยแล้ว");
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }

        return "redirect:/manager/profile";
    }
}