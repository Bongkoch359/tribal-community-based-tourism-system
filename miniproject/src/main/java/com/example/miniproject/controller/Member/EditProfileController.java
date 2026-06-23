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

        // getProfile(): ดึงข้อมูลล่าสุดจาก DB
        Member member = memberService.getMemberById(loggedIn.getMemberid())
                                     .orElse(loggedIn);

        // display result: ส่งไปหน้าฟอร์มแก้ไขข้อมูล
        model.addAttribute("member", member);
        return "Member/member_editprofile";
    }

    // ══════════════════════════════════════════════════════
    //  POST /member/profile/edit
    //  ซีเคว้น Step 5-9: รับข้อมูล → validate → doEditProfile() → update
    // ══════════════════════════════════════════════════════
    @PostMapping("/edit")
    public String doEditProfile(
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

        // ══ Step 7 (Pre-build): เตรียม Object จากข้อมูลที่กรอกเข้ามา เพื่อใช้ดัก Alternate Flows ════
        Member currentInput = new Member();
        currentInput.setMemberid(loggedIn.getMemberid());
        currentInput.setFirstname(firstname != null ? firstname.trim() : "");
        currentInput.setLastname(lastname != null ? lastname.trim() : "");
        currentInput.setEmail(loggedIn.getEmail()); // อีเมลดึงจากระบบเดิม ห้ามแก้ไข
        currentInput.setPhone(phone != null ? phone.trim() : "");
        currentInput.setBirthdate(birthdate);
        currentInput.setAddress(address != null ? address.trim() : "");

        // ══ Alternate Flow 6.1 — ข้อมูลไม่ครบ (หรือกรอกไม่ถูกต้อง) ══════════
        if (firstname == null || firstname.isBlank() ||
            lastname  == null || lastname.isBlank()) {
            // ดัก Alternate Flow: แสดงกล่องข้อความเตือนให้ตรงตามเอกสารสเปก
            model.addAttribute("errorMessage", "กรุณากรอกข้อมูลให้ถูกต้องและครบถ้วน");
            model.addAttribute("member", currentInput); // คงค่าที่กรอกไว้หน้าจอ
            return "Member/member_editprofile";
        }

        // ✅ เพิ่มใหม่: ตรวจสอบเบอร์โทรศัพท์ต้องเป็นตัวเลข 10 หลัก
        if (phone != null && !phone.isBlank()) {
        String cleanPhone = phone.trim();
        // ใช้ Regular Expression เช็กว่าเป็นตัวเลขล้วน [0-9] และมีความยาว 10 ตัวพอดี
        if (!cleanPhone.matches("^[0-9]{10}$")) {
        model.addAttribute("errorMessage", "กรุณากรอกข้อมูลให้ถูกต้องและครบถ้วน");
        model.addAttribute("member", currentInput);
        return "Member/member_editprofile";
        }
        }

        // ══ Alternate Flow เพิ่มเติม: ตรวจสอบความถูกต้องของรหัสผ่านใหม่ ══════════
        if (newPassword != null && !newPassword.isBlank()) {
            if (newPassword.length() < 6) {
                model.addAttribute("errorMessage", "รหัสผ่านต้องมีอย่างน้อย 6 ตัวอักษร");
                model.addAttribute("member", currentInput);
                return "Member/member_editprofile"; 
            }
            if (!newPassword.equals(confirmPassword)) {
                model.addAttribute("errorMessage", "รหัสผ่านไม่ตรงกัน");
                model.addAttribute("member", currentInput);
                return "Member/member_editprofile";
            }
            currentInput.setPassword(newPassword);
        } else {
            currentInput.setPassword(null); // Service จะข้ามการอัปเดตรหัสผ่านถ้าเป็น null
        }

        // ══ Step 8: doEditProfile() (ส่งวัตถุไปบันทึกที่ระบบ) ══════════════════════
        boolean saved = memberService.updateProfile(currentInput);

        // ══ Alternate Flow 8.1.1 — เกิดข้อผิดพลาด/บันทึกไม่ได้ ════════════════
        if (!saved) {
            // แจ้งเตือนข้อความความผิดพลาดตามสเปกของระบบที่กำหนดไว้
            model.addAttribute("errorMessage", "ไม่สามารถบันทึกข้อมูลได้ กรุณาลองใหม่อีกครั้ง");
            model.addAttribute("member", currentInput); // คงค่าที่กรอกล่าสุดไว้ ไม่ให้พิมพ์ใหม่หมด
            return "Member/member_editprofile";
        }

        // ══ Step 9: สำเร็จ → อัปเดตข้อมูลใน Session ของผู้ใช้ ══════════════
        session.setAttribute("loggedInMember", currentInput);

        // แสดงผลสำเร็จด้วย Flash Attribute และโหลดหน้าเว็บใหม่ป้องกันการกดส่งซ้ำ (F5)
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
        return "Member/member_editprofile";
    }
}