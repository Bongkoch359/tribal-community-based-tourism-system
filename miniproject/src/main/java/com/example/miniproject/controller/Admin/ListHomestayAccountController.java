package com.example.miniproject.controller.Admin;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.example.miniproject.entity.Homestay;
import com.example.miniproject.entity.Homestayowner;
import com.example.miniproject.repository.Homestay.HomestayOwnerRepository;
import com.example.miniproject.service.Admin.EmailService;
import com.example.miniproject.service.Admin.ReportService;

import jakarta.servlet.http.HttpSession;

@Controller
@RequestMapping("/admin/homestay")
public class ListHomestayAccountController {

    @Autowired
    private HomestayOwnerRepository ownerrepository;

    @Autowired
    private EmailService emailService;

    @Autowired
    private ReportService reportService;

    // GET /admin/homestay → รายการคำขอสมัคร
    // Basic Flow 1-6 ของ List Homestay Account
    @GetMapping
    public String listHomestay(Model model, HttpSession session) {

        if (session.getAttribute("loggedInAdmin") == null)
            return "redirect:/admin/login";

        List<Homestayowner> all = ownerrepository.findAll();

        List<Homestayowner> pending = all.stream()
            .filter(o -> o.getVerificationstatus() == null || !o.getVerificationstatus())
            .filter(o -> !"REJECTED".equals(o.getAccountstatus()))
            .toList();

        List<Homestayowner> rejected = all.stream()
            .filter(o -> "REJECTED".equals(o.getAccountstatus()))
            .toList();

        List<Homestayowner> homestaysForThisPage = new java.util.ArrayList<>();
        homestaysForThisPage.addAll(pending);
        homestaysForThisPage.addAll(rejected);

        model.addAttribute("homestays",     homestaysForThisPage);
        model.addAttribute("allCount",      homestaysForThisPage.size());
        model.addAttribute("pendingCount",  pending.size());
        model.addAttribute("approvedCount", 0);
        model.addAttribute("rejectedCount", rejected.size());

        return "Admin/admin_homestaylist";
    }

    // GET /admin/homestay/all → บัญชีที่อนุมัติแล้วทั้งหมด
    @GetMapping("/all")
    public String listAllHomestay(Model model, HttpSession session) {

        if (session.getAttribute("loggedInAdmin") == null)
            return "redirect:/admin/login";

        List<Homestayowner> allOwners = ownerrepository.findAll(); // <-- ดึงข้อมูลทั้งหมดมาก่อน

        // ── คำนวณจำนวนโฮมสเตย์ที่รออนุมัติสำหรับเอาไปโชว์ที่ Navbar ──
        long homestayPending = allOwners.stream()
            .filter(o -> (o.getVerificationstatus() == null || !o.getVerificationstatus())
                      && !"REJECTED".equals(o.getAccountstatus()))
            .count();

        // กรองเฉพาะรายการที่อนุมัติแล้วมาแสดงในตารางหน้านี้
        List<Homestayowner> approved = allOwners.stream()
            .filter(o -> Boolean.TRUE.equals(o.getVerificationstatus()))
            .toList();

        long activeCount  = approved.stream().filter(o -> !"SUSPENDED".equals(o.getAccountstatus())).count();
        long suspendCount = approved.stream().filter(o ->  "SUSPENDED".equals(o.getAccountstatus())).count();

        // ══════════════════════════════════════════
        // เพิ่มใหม่: คำนวณจำนวนรายงาน (PENDING) ของแต่ละ owner
        // แล้ว set ลงใน field @Transient pendingReportCount ก่อนส่งเข้า Thymeleaf
        // นับรวมจากทุกโฮมสเตย์ที่ owner คนนั้นมี ผ่าน reportService.countReportsForHomestay(homestayId)
        // หมายเหตุ: ถ้า owner.getHomestays() เป็น lazy collection และไม่มี @Transactional
        // ตรงนี้อาจเจอ LazyInitializationException — ถ้าเจอให้เพิ่ม @Transactional(readOnly = true)
        // ที่ method นี้ หรือเปลี่ยน fetch เป็น EAGER/JOIN FETCH ใน repository
        // ══════════════════════════════════════════
        approved.forEach(owner -> {
            List<Homestay> ownedHomestays = owner.getHomestays();
            long pendingReports = (ownedHomestays == null) ? 0 :
                ownedHomestays.stream()
                    .mapToLong(hs -> reportService.countReportsForHomestay(hs.getHomestayid()))
                    .sum();
            owner.setPendingReportCount((int) pendingReports);
        });

        model.addAttribute("homestays",    approved);
        model.addAttribute("allCount",     approved.size());
        model.addAttribute("activeCount",  activeCount);
        model.addAttribute("suspendCount", suspendCount);
        
        // ── เพิ่มบรรทัดนี้ เพื่อส่งตัวเลขไปแสดงที่ Navbar ──
        model.addAttribute("pendingCount", homestayPending);

        return "Admin/admin_homestayall";
    }

    // POST /admin/homestay/approve/{id}
    // Basic Flow 5 / Alternate Flow 5.1.1 — error update message
    @PostMapping("/approve/{id}")
    public String approveHomestay(@PathVariable String id,
                                  HttpSession session,
                                  RedirectAttributes redirectAttrs) {

        if (session.getAttribute("loggedInAdmin") == null)
            return "redirect:/admin/login";

        try {
            Homestayowner owner = ownerrepository.findById(id)
                .orElseThrow(() -> new RuntimeException("ไม่พบข้อมูลเจ้าของโฮมสเตย์"));

            // Basic Flow 5.1 — update status
            owner.setVerificationstatus(true);
            owner.setAccountstatus("ACTIVE");
            ownerrepository.save(owner);

            // Basic Flow 5.2 — ส่งอีเมลแจ้งเตือน
            try {
                emailService.sendApprovalEmail(
                    owner.getEmail(),
                    owner.getFirstname() + " " + owner.getLastname()
                );
            } catch (Exception mailEx) {
                System.err.println("ส่งอีเมลไม่สำเร็จ: " + mailEx.getMessage());
            }

             redirectAttrs.addFlashAttribute("successMessage",
            "อนุมัติการสมัครสมาชิกเจ้าของโฮมสเตย์เรียบร้อยแล้ว");

        } catch (Exception e) {
            // Alternate Flow 5.1.1 — error update message → กลับไปแสดงที่ Page
            redirectAttrs.addFlashAttribute("errorMessage",
                "ไม่สามารถอนุมัติการสมัครสมาชิกเจ้าของโฮมสเตย์ได้ กรุณาลองใหม่อีกครั้ง");
            return "redirect:/admin/homestay";
        }

        return "redirect:/admin/homestay";
    }
@PostMapping("/reject/{id}")
public String rejectHomestay(@PathVariable String id,
                             @RequestParam("reason") String reason,
                             HttpSession session,
                             RedirectAttributes redirectAttrs) {

    if (session.getAttribute("loggedInAdmin") == null)
        return "redirect:/admin/login";

    try {
        Homestayowner owner = ownerrepository.findById(id)
            .orElseThrow(() -> new RuntimeException("ไม่พบข้อมูลเจ้าของโฮมสเตย์"));

        owner.setVerificationstatus(false);
        owner.setAccountstatus("REJECTED");
        owner.setRejectionReason(reason);
        ownerrepository.save(owner);

        try {
            emailService.sendRejectionEmail(
                owner.getEmail(),
                owner.getFirstname() + " " + owner.getLastname(),
                reason
            );
        } catch (Exception mailEx) {
            System.err.println("ส่งอีเมลไม่สำเร็จ: " + mailEx.getMessage());
        }

         redirectAttrs.addFlashAttribute("successMessage",
            "ปฏิเสธคำขอสมัครสมาชิกเจ้าของโฮมสเตย์เรียบร้อยแล้ว");

    } catch (Exception e) {
        redirectAttrs.addFlashAttribute("errorMessage",
            "ไม่สามารถปฏิเสธการสมัครสมาชิกเจ้าของโฮมสเตย์ได้ กรุณาลองใหม่อีกครั้ง");
        return "redirect:/admin/homestay";
    }

    return "redirect:/admin/homestay";
}
@PostMapping("/suspend/{id}")
public String suspendHomestay(@PathVariable String id,
                            @RequestParam("reason") String reason,
                            HttpSession session,
                            RedirectAttributes redirectAttrs) {

    if (session.getAttribute("loggedInAdmin") == null)
        return "redirect:/admin/login";

    try {
        Homestayowner owner = ownerrepository.findById(id)
            .orElseThrow(() -> new RuntimeException("ไม่พบข้อมูล"));

        // Basic Flow 5.1 — update status & reason
               owner.setAccountstatus("SUSPENDED");
        owner.setSuspensionReason(reason);
        ownerrepository.save(owner);

        // resolve report ที่ PENDING ทั้งหมดของทุกโฮมสเตย์ที่ owner คนนี้เป็นเจ้าของ
        List<Homestay> ownedHomestays = owner.getHomestays();
        if (ownedHomestays != null) {
            for (Homestay hs : ownedHomestays) {
                reportService.resolveReportsForHomestay(hs.getHomestayid());
            }
        }

    } catch (Exception e) {
        redirectAttrs.addFlashAttribute("errorMessage",
            "ไม่สามารถระงับบัญชีโฮมสเตย์นี้ได้ กรุณาลองใหม่อีกครั้ง");
    }

    return "redirect:/admin/homestay/all";
}

@PostMapping("/activate/{id}")
public String activateHomestay(@PathVariable String id,
                               HttpSession session,
                               RedirectAttributes redirectAttrs) {

    if (session.getAttribute("loggedInAdmin") == null)
        return "redirect:/admin/login";

    try {
        Homestayowner owner = ownerrepository.findById(id)
            .orElseThrow(() -> new RuntimeException("ไม่พบข้อมูล"));

        owner.setAccountstatus("ACTIVE");
        ownerrepository.save(owner);

    } catch (Exception e) {
        // Alternate Flow 5.1.1 — error query message
        redirectAttrs.addFlashAttribute("errorMessage",
            "ไม่สามารถเปิดใช้งานบัญชีโฮมสเตย์นี้ได้ กรุณาลองใหม่อีกครั้ง");
    }

    return "redirect:/admin/homestay/all";
}

// GET /admin/homestay/detail/{id} → ดูรายละเอียดคำขอโฮมสเตย์ (read-only)
@GetMapping("/detail/{id}")
public String homestayDetail(@PathVariable("id") String id,
                              Model model,
                              HttpSession session,
                              RedirectAttributes redirectAttrs) {

    if (session.getAttribute("loggedInAdmin") == null)
        return "redirect:/admin/login";

    Homestayowner owner = ownerrepository.findById(id).orElse(null);

    if (owner == null) {
        redirectAttrs.addFlashAttribute("errorMessage", "ไม่พบข้อมูลเจ้าของโฮมสเตย์");
        return "redirect:/admin/homestay";
    }

    model.addAttribute("h", owner);
    return "Admin/admin_homestay_detail";
}
}