package com.example.miniproject.controller.Member;

import com.example.miniproject.entity.Activitypost;
import com.example.miniproject.service.Member.ActivityPostService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/member/activity")
public class ActivityMemberController {

    @Autowired
    private ActivityPostService activityPostService;

    // ─── ดูรายละเอียดกิจกรรม ───
    @GetMapping("/{id}")
    public String viewActivity(@PathVariable("id") String activityId, Model model) {
        Activitypost post = activityPostService.getPostById(activityId);
        if (post == null) return "redirect:/search";
        model.addAttribute("post", post);
        return "Member/activity_post";
    }
}