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

    String result = memberService.registerUser(member, confirmPassword);

    if ("SUCCESS".equals(result)) {
        return "redirect:/member/login?registered=true";
    }

    // ส่ง errorMessage ที่ได้จาก Service ไปแสดงใน HTML ตรงๆ
    model.addAttribute("errorMessage", result);
    model.addAttribute("member", member);
    return "Member/member_register";
}
}