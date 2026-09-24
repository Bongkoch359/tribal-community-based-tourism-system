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

    
    @GetMapping("/edit")
    public String openEditPage(HttpSession session, Model model) {

      
        Member loggedIn = (Member) session.getAttribute("loggedInMember");
        if (loggedIn == null) {
            return "redirect:/member/login";
        }

      
        Member member = memberService.getMemberById(loggedIn.getMemberid())
                                     .orElse(loggedIn);

        model.addAttribute("member", member);
        return "Member/member_editprofile";
    }

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

     
        Member loggedIn = (Member) session.getAttribute("loggedInMember");
        if (loggedIn == null) {
            return "redirect:/member/login";
        }

      
        Member currentInput = new Member();
        currentInput.setMemberid(loggedIn.getMemberid());
        currentInput.setFirstname(firstname != null ? firstname.trim() : "");
        currentInput.setLastname(lastname != null ? lastname.trim() : "");
        currentInput.setEmail(loggedIn.getEmail()); // อีเมลดึงจากระบบเดิม ห้ามแก้ไข
        currentInput.setPhone(phone != null ? phone.trim() : "");
        currentInput.setBirthdate(birthdate);
        currentInput.setAddress(address != null ? address.trim() : "");

       
        if (firstname == null || firstname.isBlank() ||
            lastname  == null || lastname.isBlank()) {
          
            model.addAttribute("errorMessage", "กรุณากรอกข้อมูลให้ถูกต้องและครบถ้วน");
            model.addAttribute("member", currentInput); // คงค่าที่กรอกไว้หน้าจอ
            return "Member/member_editprofile";
        }

        
        if (phone != null && !phone.isBlank()) {
        String cleanPhone = phone.trim();
        
        if (!cleanPhone.matches("^[0-9]{10}$")) {
        model.addAttribute("errorMessage", "กรุณากรอกข้อมูลให้ถูกต้องและครบถ้วน");
        model.addAttribute("member", currentInput);
        return "Member/member_editprofile";
        }
        }

       
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
            currentInput.setPassword(null); 
        }

       
        boolean saved = memberService.updateProfile(currentInput);

       
        if (!saved) {
            
            model.addAttribute("errorMessage", "ไม่สามารถบันทึกข้อมูลได้ กรุณาลองใหม่อีกครั้ง");
            model.addAttribute("member", currentInput); // คงค่าที่กรอกล่าสุดไว้ ไม่ให้พิมพ์ใหม่หมด
            return "Member/member_editprofile";
        }

        // อัปเดตข้อมูลใน Session
        session.setAttribute("loggedInMember", currentInput);
        ra.addFlashAttribute("successMessage", "แก้ไขข้อมูลสำเร็จแล้ว!");
        return "redirect:/member/profile/edit";
    }

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