package com.example.miniproject.controller.Member;

import com.example.miniproject.service.Member.MemberService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/member/login")
public class LoginMemberController {

    @Autowired
    private MemberService memberService;

    @GetMapping
    public String openLoginPage(@RequestParam(required = false) String registered,
                                Model model) {
        if ("true".equals(registered)) {
            model.addAttribute("successMessage", "สมัครสมาชิกสำเร็จ! กรุณาเข้าสู่ระบบ");
        }
        return "Member/member_login";
    }

    @PostMapping
    public String loginMember(@RequestParam String email,
                              @RequestParam String password,
                              HttpSession session,
                              Model model) {

        String result = memberService.loginMember(email, password);

        if ("SUCCESS".equals(result)) {
            memberService.getMemberByEmail(email)
                         .ifPresent(m -> session.setAttribute("loggedInMember", m));
            //  ✅ ชื่อ session key ต้องตรงกับ Thymeleaf: session.loggedInMember
            return "redirect:/search";
        }

        model.addAttribute("errorMessage", result);
        return "Member/member_login";
    }

    // ─── Logout (เรียกจาก href="/member/login/logout") ───
    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/search";
    }
}