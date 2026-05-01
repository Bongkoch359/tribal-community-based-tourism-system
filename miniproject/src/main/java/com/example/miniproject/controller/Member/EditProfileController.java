package com.example.miniproject.controller.Member;

import com.example.miniproject.entity.Member;
import com.example.miniproject.service.Member.MemberService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import java.util.Date;

@Controller
@RequestMapping("/member/profile")
public class EditProfileController {

    @Autowired
    private MemberService memberService;

    // ══════════════════════════════════════════════════════
    //  GET /member/profile/edit
    //  ซีเคว้น Step 1-4: open page → getProfile() → display result
    // ══════════════════════════════════════════════════════
    @GetMapping("/edit")
    public String openEditPage(HttpSession session, Model model) {

        // Guard: ต้องล็อกอินก่อน
        Member loggedIn = (Member) session.getAttribute("loggedInMember");
        if (loggedIn == null) {
            return "redirect:/member/login";
        }

        // getProfile(): ดึงข้อมูลล่าสุดจาก DB (memberid เป็น String)
        Member member = memberService.getMemberById(loggedIn.getMemberid())
                                     .orElse(loggedIn); // fallback ใช้จาก session

        // display result: ส่งไปหน้า edit_profile.html
        model.addAttribute("member", member);
        return "member_editprofile";
    }

    // ══════════════════════════════════════════════════════
    //  POST /member/profile/edit
    //  ซีเคว้น Step 5-9: รับข้อมูล → validate → doEditProfile() → update
    // ══════════════════════════════════════════════════════
    @PostMapping("/edit")
    public String doEditProfile(
            // ชื่อ @RequestParam ต้องตรงกับ name="" ใน HTML form
            @RequestParam("firstname")                          String firstname,
            @RequestParam("lastname")                           String lastname,
            @RequestParam(value = "phone",      required = false) String phone,
            @RequestParam(value = "birthdate", required = false)
            @org.springframework.format.annotation.DateTimeFormat(pattern = "yyyy-MM-dd")
            Date birthdate,
            @RequestParam(value = "address",    required = false) String address,
            @RequestParam(value = "newPassword",     required = false) String newPassword,
            @RequestParam(value = "confirmPassword", required = false) String confirmPassword,
            HttpSession session,
            Model model,
            RedirectAttributes ra) {

        // Guard
        Member loggedIn = (Member) session.getAttribute("loggedInMember");
        if (loggedIn == null) {
            return "redirect:/member/login";
        }

        // ══ Alternate 6.1: Server-side validate ══════════
        if (firstname == null || firstname.isBlank() ||
            lastname  == null || lastname.isBlank()) {
            model.addAttribute("errorMessage", "กรุณากรอกชื่อและนามสกุลให้ครบถ้วน");
            model.addAttribute("member", loggedIn);
            return "member_editprofile";
        }

        if (newPassword != null && !newPassword.isBlank()) {
            if (newPassword.length() < 6) {
                model.addAttribute("errorMessage", "รหัสผ่านต้องมีอย่างน้อย 6 ตัวอักษร");
                model.addAttribute("member", loggedIn);
                return "edit_profile";
            }
            if (!newPassword.equals(confirmPassword)) {
                model.addAttribute("errorMessage", "รหัสผ่านและยืนยันรหัสผ่านไม่ตรงกัน");
                model.addAttribute("member", loggedIn);
                return "member_editprofile";
            }
        }

        // ══ Step 7: เตรียม object ที่จะ update ══════════
        Member toUpdate = new Member();
        toUpdate.setMemberid(loggedIn.getMemberid()); // String ID
        toUpdate.setFirstname(firstname.trim());
        toUpdate.setLastname(lastname.trim());
        toUpdate.setEmail(loggedIn.getEmail());        // email ไม่เปลี่ยน
        toUpdate.setPhone(phone != null ? phone.trim() : "");
        toUpdate.setBirthdate(birthdate);
        toUpdate.setAddress(address != null ? address.trim() : "");

        // รหัสผ่าน: ส่งเฉพาะเมื่อกรอก
        if (newPassword != null && !newPassword.isBlank()) {
            toUpdate.setPassword(newPassword);
        } else {
            // ถ้าไม่กรอก → ส่ง null เพื่อให้ Service ข้ามการอัปเดตรหัสผ่าน
            toUpdate.setPassword(null);
        }

        // ══ Step 8: doEditProfile() ══════════════════════
        boolean saved = memberService.updateProfile(toUpdate);

        // ══ Alternate 8.1.1: บันทึกไม่ได้ ════════════════
        if (!saved) {
            model.addAttribute("errorMessage",
                    "ไม่สามารถบันทึกข้อมูลได้ กรุณาลองใหม่อีกครั้ง");
            model.addAttribute("member", loggedIn);
            return "member_editprofile";
        }

        // ══ Step 9: สำเร็จ → อัปเดต session ══════════════
        session.setAttribute("loggedInMember", toUpdate);

        // display result พร้อม flash message
        ra.addFlashAttribute("successMessage", "แก้ไขข้อมูลสำเร็จแล้ว!");
        return "redirect:/member/profile/edit";
    }

    // ── View Profile (read-only) ─────────────────────────
    @GetMapping
    public String viewProfile(HttpSession session, Model model) {
        Member loggedIn = (Member) session.getAttribute("loggedInMember");
        if (loggedIn == null) return "redirect:/member/login";

        Member member = memberService.getMemberById(loggedIn.getMemberid())
                                     .orElse(loggedIn);
        model.addAttribute("member", member);
        return "member_editprofile";
    }
}