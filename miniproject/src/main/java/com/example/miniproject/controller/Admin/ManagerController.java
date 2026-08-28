package com.example.miniproject.controller.Admin;

import java.util.List;
import java.util.UUID;
import java.util.Arrays;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.example.miniproject.entity.Communitymanager;
import com.example.miniproject.entity.enums.ManagerStatus;
import com.example.miniproject.service.Admin.EmailService;
import com.example.miniproject.service.Admin.ManagerService;
import com.example.miniproject.entity.Homestayowner;
import com.example.miniproject.repository.Homestay.HomestayOwnerRepository;

import jakarta.servlet.http.HttpSession;

@Controller
@RequestMapping("/admin/manager")
public class ManagerController {

    @Autowired
    private ManagerService managerService;

    @Autowired
    private EmailService emailService;

    @Autowired
    private HomestayOwnerRepository ownerRepository;

    // ============================================================
    // GET /admin/manager
    // List Manager Account — step 2-5 (Sequence: List Manager)
    // ============================================================
    @GetMapping
    public String listManager(Model model, HttpSession session,
                              RedirectAttributes redirectAttributes) {

        // ตรวจ session
        if (session.getAttribute("loggedInAdmin") == null) {
            return "redirect:/admin/login";
        }

        addPendingHomestayCount(model);

        // step 4.1: query data
        List<Communitymanager> managers = managerService.getAll();

        // Alternate Flow 4.1.1 — ไม่พบข้อมูล
        if (managers == null || managers.isEmpty()) {
            model.addAttribute("managers", List.of());
            model.addAttribute("allCount",      0);
            model.addAttribute("activeCount",   0);
            model.addAttribute("inactiveCount", 0);
            model.addAttribute("message",   "ไม่พบรายการบัญชีผู้จัดการท่องเที่ยวชุมชน");
            model.addAttribute("alertType", "error");
            return "Admin/admin_managerlist";
        }

        // step 4.2 / step 5: display list
        long activeCount   = managers.stream().filter(m -> m.getAccountstatus() == ManagerStatus.ACTIVE).count();
        long inactiveCount = managers.stream().filter(m -> m.getAccountstatus() == ManagerStatus.INACTIVE).count();

        model.addAttribute("managers",      managers);
        model.addAttribute("allCount",      managers.size());
        model.addAttribute("activeCount",   activeCount);
        model.addAttribute("inactiveCount", inactiveCount);

        return "Admin/admin_managerlist";
    }

  // ============================================================
    // GET /admin/manager/create
    // เปิดหน้าฟอร์ม Create Manager — step 1 open page
    // ============================================================
    @GetMapping("/create")
    public String createPage(HttpSession session, Model model) {
        if (session.getAttribute("loggedInAdmin") == null) {
            return "redirect:/admin/login";
        }
        
        addPendingHomestayCount(model);

        // เพิ่มรายชื่อชุมชนชนเผ่าทั้ง 8 เผ่า เพื่อส่งไปแสดงผลใน Dropdown ของหน้า HTML
        List<String> tribeNames = Arrays.asList(
            "กะเหรี่ยง (ปกาเกอะญอ)",
            "ม้ง (แม้ว)",
            "อาข่า (อีก้อ)",
            "ลาหู่ (มูเซอ)",
            "ลีซู (ลีซอ)",
            "กะเหรี่ยงคอยาว (ปะด่อง)",
            "เย้า (เมี่ยน)",
            "ลัวะ (ละว้า)"
        );
        model.addAttribute("tribeNames", tribeNames);

        return "Admin/admin_managercreate";
    }

    // ============================================================
    // POST /admin/manager/create
    // Create Manager Account — step 3-6 (Sequence: Create Manager)
    // ============================================================
    @PostMapping("/create")
    public String createManager(@RequestParam String firstname,
                                @RequestParam String lastname,
                                @RequestParam String phone,
                                @RequestParam String tribe,
                                @RequestParam String email,
                                @RequestParam String password,
                                @RequestParam String confirmPassword,
                                HttpSession session,
                                RedirectAttributes redirectAttributes) {

        if (session.getAttribute("loggedInAdmin") == null) {
            return "redirect:/admin/login";
        }

        // step 3: validate (Alternate Flow 3.1)
        if (firstname.isBlank() || lastname.isBlank() || phone.isBlank() ||
            tribe.isBlank()     || email.isBlank()    || password.isBlank()) {
            redirectAttributes.addFlashAttribute("message",   "กรุณากรอกข้อมูลให้ถูกต้อง");
            redirectAttributes.addFlashAttribute("alertType", "error");
            return "redirect:/admin/manager/create";
        }

        if (!password.equals(confirmPassword)) {
            redirectAttributes.addFlashAttribute("message",   "รหัสผ่านและยืนยันรหัสผ่านไม่ตรงกัน");
            redirectAttributes.addFlashAttribute("alertType", "error");
            return "redirect:/admin/manager/create";
        }

        // ตรวจ email ซ้ำ
        if (managerService.emailExists(email)) {
            redirectAttributes.addFlashAttribute("message",   "อีเมลนี้มีในระบบแล้ว");
            redirectAttributes.addFlashAttribute("alertType", "error");
            return "redirect:/admin/manager/create";
        }

        // step 5: createManagerAccount() → insert data
        Communitymanager manager = new Communitymanager();
        manager.setManagerid("MG" + UUID.randomUUID().toString().substring(0, 6).toUpperCase());
        manager.setFirstname(firstname);
        manager.setLastname(lastname);
        manager.setPhone(phone);
        manager.setTribe(tribe);
        manager.setEmail(email);
        manager.setPassword(password);

        boolean success = managerService.createManager(manager);

        // step 5.1.1: return F (Alternate Flow)
        if (!success) {
            redirectAttributes.addFlashAttribute("message",   "ไม่สามารถบันทึกข้อมูลได้ กรุณาลองใหม่อีกครั้ง");
            redirectAttributes.addFlashAttribute("alertType", "error");
            return "redirect:/admin/manager/create";
        }

        // step 6: display result — success
        try {
        emailService.sendManagerCreatedEmail(
        email,
        firstname + " " + lastname,
        password
        );
        } catch (Exception e) {
        System.err.println("ส่งอีเมลไม่สำเร็จ: " + e.getMessage());
        }

        redirectAttributes.addFlashAttribute("message", "สร้างบัญชีผู้จัดการเรียบร้อยแล้ว");
        redirectAttributes.addFlashAttribute("alertType", "success");
        return "redirect:/admin/manager";
        
    }

    // ============================================================
    // POST /admin/manager/suspend/{id}
    // Suspend Manager Account
    // ============================================================
    // ============================================================
    // POST /admin/manager/suspend/{id}
    // Suspend Manager Account
    // ============================================================
    @PostMapping("/suspend/{id}")
    public String suspendManager(@PathVariable String id,
                                   @RequestParam("reason") String reason, // <-- เพิ่มรับค่าเหตุผลจากฟอร์ม
                                   HttpSession session,
                                   RedirectAttributes redirectAttributes) {

        if (session.getAttribute("loggedInAdmin") == null) {
            return "redirect:/admin/login";
        }

        // ส่ง id และ reason ไปให้ Service จัดการบันทึก
        boolean success = managerService.suspend(id, reason); // <-- ปรับให้ Service รับเหตุผลด้วย
        
        if (success) {
            redirectAttributes.addFlashAttribute("message", "ระงับบัญชีเรียบร้อยแล้ว");
            redirectAttributes.addFlashAttribute("alertType", "success");
        } else {
            redirectAttributes.addFlashAttribute("message", "ไม่พบบัญชีที่ต้องการระงับ");
            redirectAttributes.addFlashAttribute("alertType", "error");
        }
        return "redirect:/admin/manager";
    }

    // ============================================================
    // POST /admin/manager/activate/{id}
    // เปิดใช้งานบัญชีอีกครั้ง
    // ============================================================
    @PostMapping("/activate/{id}")
    public String activateManager(@PathVariable String id,
                                  HttpSession session,
                                  RedirectAttributes redirectAttributes) {

        if (session.getAttribute("loggedInAdmin") == null) {
            return "redirect:/admin/login";
        }

        boolean success = managerService.activate(id);
        if (success) {
            redirectAttributes.addFlashAttribute("message",   "เปิดใช้งานบัญชีเรียบร้อยแล้ว");
            redirectAttributes.addFlashAttribute("alertType", "success");
        } else {
             redirectAttributes.addFlashAttribute("message",   "ไม่สามารถระงับบัญชีผู้จัดการท่องเที่ยวชุมชนนี้ได้ กรุณาลองใหม่อีกครั้ง");
            redirectAttributes.addFlashAttribute("alertType", "error");
        }
        return "redirect:/admin/manager";
    }

    private void addPendingHomestayCount(Model model) {
        List<Homestayowner> allOwners = ownerRepository.findAll();
        long homestayPending = allOwners.stream()
            .filter(o -> (o.getVerificationstatus() == null || !o.getVerificationstatus())
                      && !"REJECTED".equals(o.getAccountstatus()))
            .count();
        model.addAttribute("pendingCount", homestayPending);
    }

    // ============================================================
    // GET /admin/manager/api/check-email
    // API สำหรับเช็คอีเมลซ้ำแบบ Real-time (AJAX)
    // ============================================================
    @GetMapping("/api/check-email")
    @ResponseBody
    public boolean checkEmailDuplicate(@RequestParam("email") String email) {
        if (email == null || email.trim().isEmpty()) {
            return false;
        }
        return managerService.emailExists(email);
    }
}