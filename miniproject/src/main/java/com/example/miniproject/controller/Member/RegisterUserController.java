package com.example.miniproject.controller.Member;

import com.example.miniproject.entity.Member;
import com.example.miniproject.service.Member.MemberService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/member/register")
public class RegisterUserController {

    @Autowired
    private MemberService memberService;

    @GetMapping
    public String openRegisterPage(Model model) {
        model.addAttribute("member", new Member());
        return "Member/member_register";
    }
@PostMapping
public String registerUser(@ModelAttribute Member member,
                           @RequestParam String confirmPassword,
                           Model model) {

    // เช็คอีเมลซ้ำแยกก่อน เพื่อโชว์ error เฉพาะช่องอีเมล ไม่ล้างข้อมูลในฟอร์ม
    if (memberService.existsByEmail(member.getEmail())) {
        model.addAttribute("emailError", "อีเมลนี้ถูกใช้งานแล้ว");
        model.addAttribute("member", member);
        return "Member/member_register";
    }

    String result = memberService.registerUser(member, confirmPassword);

    if ("SUCCESS".equals(result)) {
        return "redirect:/member/login?registered=true";
    }

  
    model.addAttribute("errorMessage", result);
    model.addAttribute("member", member);
    return "Member/member_register";
}


}