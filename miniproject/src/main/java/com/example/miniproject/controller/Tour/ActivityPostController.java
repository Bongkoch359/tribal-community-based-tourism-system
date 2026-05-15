package com.example.miniproject.controller.Tour;

import com.example.miniproject.entity.Communitymanager;
import com.example.miniproject.service.Member.ActivityPostService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
 
@Controller
@RequestMapping("/manager/posts")
public class ActivityPostController {
 
    @Autowired
    private ActivityPostService activityPostService;
 
    // ─── แสดงรายการโพสต์ ───
    @GetMapping
    public String listPosts(Model model, HttpSession session) {
        if (session.getAttribute("loggedInManager") == null)
            return "redirect:/manager/login";
 
        model.addAttribute("posts", activityPostService.getAllPosts());
        return "Tour/listPost"; 
    }
 
    // ─── แสดงฟอร์มสร้างโพสต์ ───
    @GetMapping("/create")
    public String showCreateForm(HttpSession session) {
        if (session.getAttribute("loggedInManager") == null)
            return "redirect:/manager/login";
        return "Tour/createPost";
    }
 
    // ─── บันทึกโพสต์ใหม่ ───
    @PostMapping("/create")
    public String createPost(
            @RequestParam("title")       String title,
            @RequestParam("location")    String location,
            @RequestParam("description") String description,
            @RequestParam("status")      String status,
            @RequestParam(value = "images", required = false) String images,
            HttpSession session,
            Model model) {
 
        // ตรวจ session
        Communitymanager manager = (Communitymanager) session.getAttribute("loggedInManager");
        if (manager == null) return "redirect:/manager/login";
 
        try {
            activityPostService.createPost(title, location, description, status, images, manager);
            model.addAttribute("successMessage", "บันทึกโพสต์เรียบร้อยแล้ว");
            return "redirect:/manager/posts";
 
        } catch (Exception e) {
            model.addAttribute("errorMessage", "เกิดข้อผิดพลาด: " + e.getMessage());
            return "Tour/createPost";
        }
    }
}
