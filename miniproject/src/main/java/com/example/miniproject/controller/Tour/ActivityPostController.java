package com.example.miniproject.controller.Tour;

import com.example.miniproject.entity.Activitypost;
import com.example.miniproject.entity.Communitymanager;
import com.example.miniproject.entity.Tour;
import com.example.miniproject.entity.Tourschedule;
import com.example.miniproject.repository.Tour.TourScheduleRepository;
import com.example.miniproject.service.Member.ActivityPostService;
import com.example.miniproject.service.Member.TourService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.sql.Date;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/manager/posts")
public class ActivityPostController {

    @Autowired
    private ActivityPostService activityPostService;

    @Autowired
    private TourService tourService;

    @Autowired
    private TourScheduleRepository tourScheduleRepository;

    // ─── ฟังก์ชันช่วยกรองเฉพาะทัวร์ที่มีรอบเปิดรับจองและยังไม่หมดอายุ ───
    private List<Tour> getActiveOpenTours(Communitymanager manager) {
        Date today = Date.valueOf(LocalDate.now());
        return tourService.getToursByManager(manager).stream().filter(tour -> {
            List<Tourschedule> bookableSchedules = 
                    tourScheduleRepository.findBookableSchedules(tour.getTourid(), today);
            return bookableSchedules != null && !bookableSchedules.isEmpty();
        }).collect(Collectors.toList());
    }

    // ─── แสดงรายการโพสต์ ───
    @GetMapping
    public String listPosts(Model model, HttpSession session) {
        Communitymanager manager = (Communitymanager) session.getAttribute("loggedInManager");

        if (manager == null) {
            return "redirect:/manager/login";
        }

        model.addAttribute("loggedInManager", manager);
        model.addAttribute("posts", activityPostService.getAllPosts());

        return "Tour/listPost";
    }

    // ─── แสดงรายละเอียดโพสต์ ───
    @GetMapping("/{id}")
    public String viewPost(@PathVariable("id") String activityId,
            Model model,
            HttpSession session) {
        Communitymanager manager = (Communitymanager) session.getAttribute("loggedInManager");
        if (session.getAttribute("loggedInManager") == null)
            return "redirect:/manager/login";

        Activitypost post = activityPostService.getPostById(activityId);
        if (post == null) {
            return "redirect:/manager/posts";
        }

        model.addAttribute("post", post);
        model.addAttribute("loggedInManager", manager);
        return "Tour/activityPostDetails";
    }

    // ─── แสดงฟอร์มสร้างโพสต์ ───
    @GetMapping("/create")
    public String showCreateForm(HttpSession session, Model model) {
        Communitymanager manager = (Communitymanager) session.getAttribute("loggedInManager");

        if (manager == null) {
            return "redirect:/manager/login";
        }

        model.addAttribute("loggedInManager", manager);
        model.addAttribute("tours", getActiveOpenTours(manager)); // ดึงเฉพาะทัวร์ที่เปิดจอง
        return "Tour/createPost";
    }

    // ─── บันทึกโพสต์ใหม่ ───
    @PostMapping("/create")
    public String createPost(
            @RequestParam("title") String title,
            @RequestParam("location") String location,
            @RequestParam("description") String description,
            @RequestParam(value = "images", required = false) String images,
            @RequestParam(value = "tourId", required = false) String tourId,
            HttpSession session,
            Model model) {

        Communitymanager manager = (Communitymanager) session.getAttribute("loggedInManager");
        if (manager == null)
            return "redirect:/manager/login";

        try {
            activityPostService.createPost(title, location, description, images, tourId, manager);
            model.addAttribute("loggedInManager", manager);
            model.addAttribute("tours", getActiveOpenTours(manager));
            model.addAttribute("successMessage", "บันทึกโพสต์สำเร็จ!");
            return "Tour/createPost";
        } catch (Exception e) {
            model.addAttribute("tours", getActiveOpenTours(manager));
            model.addAttribute("errorMessage", "เกิดข้อผิดพลาด: " + e.getMessage());
            return "Tour/listPost";
        }
    }

    // ─── แสดงฟอร์มแก้ไขโพสต์ ───
    @GetMapping("/{id}/edit")
    public String showEditForm(@PathVariable("id") String activityId,
            Model model,
            HttpSession session) {
        Communitymanager manager = (Communitymanager) session.getAttribute("loggedInManager");
        if (session.getAttribute("loggedInManager") == null)
            return "redirect:/manager/login";

        Activitypost post = activityPostService.getPostById(activityId);
        if (post == null)
            return "redirect:/manager/posts";

        model.addAttribute("post", post);
        model.addAttribute("loggedInManager", manager);
        model.addAttribute("tours", getActiveOpenTours(manager)); // ดึงเฉพาะทัวร์ที่เปิดจอง
        return "Tour/editPost";
    }

    // ─── บันทึกการแก้ไขโพสต์ ───
    @PostMapping("/{id}/edit")
    public String updatePost(
            @PathVariable("id") String activityId,
            @RequestParam("title") String title,
            @RequestParam("location") String location,
            @RequestParam("description") String description,
            @RequestParam(value = "images", required = false) String images,
            @RequestParam(value = "tourId", required = false) String tourId,
            HttpSession session,
            Model model) {

        Communitymanager manager = (Communitymanager) session.getAttribute("loggedInManager");
        if (manager == null)
            return "redirect:/manager/login";

        try {
            Activitypost updated = activityPostService.updatePost(
                    activityId, title, location, description, images, tourId);
            if (updated == null)
                return "redirect:/manager/posts";

            model.addAttribute("loggedInManager", manager);
            model.addAttribute("post", updated);
            model.addAttribute("tours", getActiveOpenTours(manager));
            model.addAttribute("successMessage", "แก้ไขโพสต์กิจกรรมสำเร็จ!");
            return "Tour/editPost";
        } catch (Exception e) {
            model.addAttribute("errorMessage", "เกิดข้อผิดพลาด: " + e.getMessage());
            model.addAttribute("post", activityPostService.getPostById(activityId));
            model.addAttribute("tours", getActiveOpenTours(manager));
            return "Tour/editPost";
        }
    }

    // ─── ลบโพสต์ ───
    @PostMapping("/{id}/delete")
    public String deletePost(@PathVariable("id") String activityId,
            HttpSession session) {
        if (session.getAttribute("loggedInManager") == null)
            return "redirect:/manager/login";

        activityPostService.deletePost(activityId);
        return "redirect:/manager/posts";
    }
}