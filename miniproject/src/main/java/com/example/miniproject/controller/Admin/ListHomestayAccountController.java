package com.example.miniproject.controller.Admin;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import com.example.miniproject.entity.Homestayowner;
import com.example.miniproject.repository.Homestay.HomestayOwnerRepository;
import com.example.miniproject.service.Admin.EmailService;

import jakarta.servlet.http.HttpSession;

@Controller
@RequestMapping("/admin/homestay")
public class ListHomestayAccountController {

    @Autowired
    private HomestayOwnerRepository ownerrepository;

    @Autowired
    private EmailService emailService;


    // GET /admin/homestay → รายการคำขอสมัคร 
   @GetMapping
    public String listHomestay(Model model, HttpSession session) {

    if (session.getAttribute("loggedInAdmin") == null) {
        return "redirect:/admin/login";
    }

    List<Homestayowner> all = ownerrepository.findAll();

    // 1. ดึงรายการที่ รออนุมัติ (Pending)
    List<Homestayowner> pending = all.stream()
        .filter(o -> o.getVerificationstatus() == null || !o.getVerificationstatus())
        .filter(o -> !"REJECTED".equals(o.getAccountstatus()))
        .toList();

    // 2. ดึงรายการที่ ปฏิเสธแล้ว (Rejected)
    List<Homestayowner> rejected = all.stream()
        .filter(o -> "REJECTED".equals(o.getAccountstatus()))
        .toList();

    // 3. รวมเฉพาะข้อมูลที่จะแสดงในหน้านี้ (คำขอใหม่ + ปฏิเสธ) ไม่เอาพวกที่อนุมัติแล้วมารวม
    List<Homestayowner> homestaysForThisPage = new java.util.ArrayList<>();
    homestaysForThisPage.addAll(pending);
    homestaysForThisPage.addAll(rejected);

    // 🌟 แก้ตรงนี้: ส่งเฉพาะรายการคำขอสมัคร (ไม่เอาอนุมัติแล้ว) ไปแสดงในตาราง
    model.addAttribute("homestays",     homestaysForThisPage);
    
    // 🌟 แก้ไขตัวนับจำนวนให้ถูกต้องสัมพันธ์กับข้อมูลที่ส่งไป
    model.addAttribute("allCount",      homestaysForThisPage.size()); // จำนวนคำขอทั้งหมดในหน้านี้ (Pending + Rejected)
    model.addAttribute("pendingCount",  pending.size());              // จำนวนรออนุมัติ
    model.addAttribute("approvedCount", 0);                           // บังคับเป็น 0 เพราะคนที่อนุมัติแล้วถูกย้ายไปหน้า /all แล้ว
    model.addAttribute("rejectedCount", rejected.size());             // จำนวนที่ปฏิเสธแล้ว

    return "Admin/admin_homestaylist";
}

    // ============================================================
    // GET /admin/homestay/all → บัญชีที่อนุมัติแล้วทั้งหมด
    // ============================================================
    @GetMapping("/all")
    public String listAllHomestay(Model model, HttpSession session) {

        if (session.getAttribute("loggedInAdmin") == null) {
            return "redirect:/admin/login";
        }

        // แสดงเฉพาะที่ผ่านการตรวจสอบแล้ว
        List<Homestayowner> approved = ownerrepository.findAll().stream()
            .filter(o -> Boolean.TRUE.equals(o.getVerificationstatus()))
            .toList();

        long activeCount = approved.stream()
            .filter(o -> !"SUSPENDED".equals(o.getAccountstatus()))
            .count();

        long suspendCount = approved.stream()
            .filter(o -> "SUSPENDED".equals(o.getAccountstatus()))
            .count();

        model.addAttribute("homestays",    approved);
        model.addAttribute("allCount",     approved.size());
        model.addAttribute("activeCount",  activeCount);
        model.addAttribute("suspendCount", suspendCount);

        return "Admin/admin_homestayall";
    }

    // ============================================================
    // POST /admin/homestay/approve/{id}
    // ============================================================
   @PostMapping("/approve/{id}")
public String approveHomestay(@PathVariable Integer id, HttpSession session) {
    if (session.getAttribute("loggedInAdmin") == null)
        return "redirect:/admin/login";

    ownerrepository.findById(id).ifPresent(o -> {
        o.setVerificationstatus(true);
        o.setAccountstatus("ACTIVE");
        ownerrepository.save(o);

        // ส่งอีเมลแจ้งเตือน
        try {
            emailService.sendApprovalEmail(
                o.getEmail(),
                o.getFirstname() + " " + o.getLastname()
            );
        } catch (Exception e) {
            System.err.println("ส่งอีเมลไม่สำเร็จ: " + e.getMessage());
        }
    });

    return "redirect:/admin/homestay";
}

@PostMapping("/reject/{id}")
public String rejectHomestay(@PathVariable Integer id, HttpSession session) {
    if (session.getAttribute("loggedInAdmin") == null)
        return "redirect:/admin/login";

    ownerrepository.findById(id).ifPresent(o -> {
        o.setVerificationstatus(false);
        o.setAccountstatus("REJECTED");
        ownerrepository.save(o);

        //  ส่งอีเมลแจ้งเตือน
        try {
            emailService.sendRejectionEmail(
                o.getEmail(),
                o.getFirstname() + " " + o.getLastname()
            );
        } catch (Exception e) {
            System.err.println("ส่งอีเมลไม่สำเร็จ: " + e.getMessage());
        }
    });

    return "redirect:/admin/homestay";
}

    // POST /admin/homestay/suspend/{id}
    @PostMapping("/suspend/{id}")
    public String suspendHomestay(@PathVariable Integer id, HttpSession session) {

        if (session.getAttribute("loggedInAdmin") == null) {
            return "redirect:/admin/login";
        }

        ownerrepository.findById(id).ifPresent(o -> {
            o.setAccountstatus("SUSPENDED");
            ownerrepository.save(o);
        });

        return "redirect:/admin/homestay/all";
    }

    // POST /admin/homestay/activate/{id}
    @PostMapping("/activate/{id}")
    public String activateHomestay(@PathVariable Integer id, HttpSession session) {

        if (session.getAttribute("loggedInAdmin") == null) {
            return "redirect:/admin/login";
        }

        ownerrepository.findById(id).ifPresent(o -> {
            o.setAccountstatus("ACTIVE");
            ownerrepository.save(o);
        });

        return "redirect:/admin/homestay/all";
    }
}