package com.example.miniproject.controller.Admin;

import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpSession;

@Controller
@RequestMapping("/admin/homestay")
public class ListHomestayAccountController {

    // ============================================================
    // GET /admin/homestay
    // หน้า รายการคำขอสมัครโฮมสเตย์
    // ============================================================
    @GetMapping
    public String listHomestay(Model model, HttpSession session) {

        if (session.getAttribute("loggedInAdmin") == null) {
            return "redirect:/admin/login";
        }

        // ตอนนี้ยังไม่มีระบบสมัคร → list ว่าง
        model.addAttribute("homestays", List.of());

        model.addAttribute("allCount", 0);
        model.addAttribute("pendingCount", 0);
        model.addAttribute("approvedCount", 0);
        model.addAttribute("rejectedCount", 0);

        return "Admin/admin_homestaylist";
    }

    // ============================================================
    // GET /admin/homestay/all
    // หน้า รายการบัญชีโฮมสเตย์ทั้งหมด
    // ============================================================
    @GetMapping("/all")
    public String listAllHomestay(Model model, HttpSession session) {

        if (session.getAttribute("loggedInAdmin") == null) {
            return "redirect:/admin/login";
        }

        model.addAttribute("homestays", List.of());

        model.addAttribute("allCount", 0);
        model.addAttribute("activeCount", 0);
        model.addAttribute("suspendCount", 0);

        return "admin_homestayall";
    }

    // ============================================================
    // POST /admin/homestay/approve/{id}
    // อนุมัติคำขอ
    // ============================================================
    @PostMapping("/approve/{id}")
    public String approveHomestay(@PathVariable String id,
                                  HttpSession session) {

        if (session.getAttribute("loggedInAdmin") == null) {
            return "redirect:/admin/login";
        }


        return "redirect:/admin/homestay";
    }

    // ============================================================
    // POST /admin/homestay/reject/{id}
    // ปฏิเสธคำขอ
    // ============================================================
    @PostMapping("/reject/{id}")
    public String rejectHomestay(@PathVariable String id,
                                 HttpSession session) {

        if (session.getAttribute("loggedInAdmin") == null) {
            return "redirect:/admin/login";
        }

        

        return "redirect:/admin/homestay";
    }

    // ============================================================
    // POST /admin/homestay/suspend/{id}
    // ระงับบัญชี
    // ============================================================
    @PostMapping("/suspend/{id}")
    public String suspendHomestay(@PathVariable String id,
                                  HttpSession session) {

        if (session.getAttribute("loggedInAdmin") == null) {
            return "redirect:/admin/login";
        }

        // TODO: update status → SUSPENDED

        return "redirect:/admin/homestay/all";
    }

    // ============================================================
    // POST /admin/homestay/activate/{id}
    // เปิดใช้งานบัญชี
    // ============================================================
    @PostMapping("/activate/{id}")
    public String activateHomestay(@PathVariable String id,
                                   HttpSession session) {

        if (session.getAttribute("loggedInAdmin") == null) {
            return "redirect:/admin/login";
        }

       

        return "redirect:/admin/homestay/all";
    }

}