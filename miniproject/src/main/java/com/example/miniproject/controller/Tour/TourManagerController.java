package com.example.miniproject.controller.Tour;

import com.example.miniproject.entity.Communitymanager;
import com.example.miniproject.service.Tour.CommunityManagerService;

import jakarta.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import org.springframework.http.MediaType;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.util.Base64;

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

        Communitymanager manager = managerService.login(email, password);

        if (manager != null) {
            if (manager.getAccountstatus() != null &&
                    ("INACTIVE".equals(manager.getAccountstatus().toString()) ||
                            "SUSPENDED".equals(manager.getAccountstatus().toString()))) {
                
                // ดึงเหตุผลการระงับมาต่อท้ายข้อความ
                String reason = manager.getSuspensionReason();
                String errorMsg = "บัญชีของคุณถูกระงับการใช้งาน กรุณาติดต่อผู้ดูแลระบบ";
                
                if (reason != null && !reason.isBlank()) {
                    errorMsg += " (เหตุผล: " + reason + ")";
                }

                model.addAttribute("errorMessage", errorMsg);
                return "Tour/manager_login";
            }
            
            session.setAttribute("loggedInManager", manager);
            return "redirect:/manager/dashboard";
        }

        model.addAttribute("errorMessage", "อีเมลหรือรหัสผ่านไม่ถูกต้อง");
        return "Tour/manager_login";
    }

    // logout
    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/manager/login";
    }

    // ─── แสดงหน้าแก้ไขโปรไฟล์ ───
    @GetMapping("/manager/profile")
    public String profilePage(HttpSession session, Model model) {
        Communitymanager manager = (Communitymanager) session.getAttribute("loggedInManager");
        if (manager == null) {
            return "redirect:/manager/login";
        }
        model.addAttribute("loggedInManager", manager);
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
            @RequestParam(required = false, defaultValue = "") String accountName,
            @RequestParam(required = false, defaultValue = "") String accountNumber,
            @RequestParam(required = false, defaultValue = "") String bankBranch,
            HttpSession session,
            RedirectAttributes redirectAttributes) {

        Communitymanager manager = (Communitymanager) session.getAttribute("loggedInManager");
        if (manager == null) {
            return "redirect:/manager/login";
        }

        try {
            managerService.updateBankInfo(
                    manager.getManagerid(), bankName, accountName, accountNumber, bankBranch);

            Communitymanager updated = managerService.getById(manager.getManagerid());
            session.setAttribute("loggedInManager", updated);
            redirectAttributes.addFlashAttribute("successMessage", "บันทึกข้อมูลธนาคารเรียบร้อยแล้ว");

        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }

        return "redirect:/manager/profile";
    }

    // ─── บันทึกลายเซ็น ───
    @PostMapping(value = "/manager/profile/update-signature", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public String updateSignature(
            @RequestParam("signatureFile") MultipartFile signatureFile,
            HttpSession session,
            RedirectAttributes redirectAttributes) {

        Communitymanager manager = (Communitymanager) session.getAttribute("loggedInManager");
        if (manager == null) {
            return "redirect:/manager/login";
        }

        if (signatureFile == null || signatureFile.isEmpty()) {
            redirectAttributes.addFlashAttribute("errorMessage", "กรุณาเลือกไฟล์ลายเซ็น");
            return "redirect:/manager/profile";
        }

        String contentType = signatureFile.getContentType();
        boolean validType = contentType != null &&
                (contentType.equals("image/png") || contentType.equals("image/jpeg"));

        if (!validType) {
            redirectAttributes.addFlashAttribute("errorMessage", "รองรับเฉพาะไฟล์ PNG หรือ JPG เท่านั้น");
            return "redirect:/manager/profile";
        }

        if (signatureFile.getSize() > 2 * 1024 * 1024) { // 2MB
            redirectAttributes.addFlashAttribute("errorMessage", "ขนาดไฟล์ต้องไม่เกิน 2MB");
            return "redirect:/manager/profile";
        }

        try {
            String base64 = Base64.getEncoder().encodeToString(signatureFile.getBytes());
            String dataUrl = "data:" + contentType + ";base64," + base64;

            managerService.updateSignature(manager.getManagerid(), dataUrl);

            Communitymanager updated = managerService.getById(manager.getManagerid());
            session.setAttribute("loggedInManager", updated);
            redirectAttributes.addFlashAttribute("successMessage", "อัปโหลดลายเซ็นเรียบร้อยแล้ว");

        } catch (IOException e) {
            redirectAttributes.addFlashAttribute("errorMessage", "อัปโหลดลายเซ็นไม่สำเร็จ: " + e.getMessage());
        }

        return "redirect:/manager/profile";
    }
}