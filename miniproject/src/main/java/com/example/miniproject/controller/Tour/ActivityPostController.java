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
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.sql.Date;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
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

    // ─── โฟลเดอร์เก็บรูปภาพโพสต์กิจกรรม ───
    private static final String UPLOAD_DIR = System.getProperty("user.dir") + "/uploads/posts/";

    // ─── ฟังก์ชันช่วยบันทึกไฟล์รูปภาพลงดิสก์ แล้วคืน path คั่นด้วย "||" ───
    private String saveImages(List<MultipartFile> images) throws IOException {
        if (images == null || images.isEmpty()) {
            return null; // ไม่มีไฟล์ใหม่ -> ไม่แตะรูปเดิม
        }

        File dir = new File(UPLOAD_DIR);
        if (!dir.exists()) {
            dir.mkdirs();
        }

        List<String> savedPaths = new ArrayList<>();
        for (MultipartFile file : images) {
            if (file == null || file.isEmpty()) {
                continue;
            }

            String original = file.getOriginalFilename();
            String ext = "";
            if (original != null && original.contains(".")) {
                ext = original.substring(original.lastIndexOf("."));
            }

            String fileName = UUID.randomUUID().toString() + ext;
            File dest = new File(dir, fileName);
            file.transferTo(dest);

            savedPaths.add("/uploads/posts/" + fileName);
        }

        if (savedPaths.isEmpty()) {
            return null;
        }

        return String.join("||", savedPaths);
    }

    // ─── ฟังก์ชันช่วยกรองเฉพาะทัวร์ที่มีรอบเปิดรับจองและยังไม่หมดอายุ ───
    private List<Tour> getActiveOpenTours(Communitymanager manager) {
        Date today = Date.valueOf(LocalDate.now());
        return tourService.getToursByManager(manager).stream().filter(tour -> {
            List<Tourschedule> bookableSchedules = tourScheduleRepository.findBookableSchedules(tour.getTourid(),
                    today);
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
        model.addAttribute("posts", activityPostService.getPostsByManager(manager.getManagerid()));

        return "Tour/listPost";
    }

    // ─── แสดงรายละเอียดโพสต์ ───
    // @GetMapping("/{id}")
    // public String viewPost(@PathVariable("id") String activityId,
    //         Model model,
    //         HttpSession session) {
    //     Communitymanager manager = (Communitymanager) session.getAttribute("loggedInManager");
    //     if (session.getAttribute("loggedInManager") == null)
    //         return "redirect:/manager/login";

    //     Activitypost post = activityPostService.getPostById(activityId);
    //     if (post == null) {
    //         return "redirect:/manager/posts";
    //     }

    //     model.addAttribute("post", post);
    //     model.addAttribute("loggedInManager", manager);
    //     return "Tour/activityPostDetails";
    // }

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
            @RequestParam(value = "images", required = false) List<MultipartFile> images,
            @RequestParam(value = "tourId", required = false) String tourId,
            HttpSession session,
            Model model) {

        Communitymanager manager = (Communitymanager) session.getAttribute("loggedInManager");
        if (manager == null)
            return "redirect:/manager/login";

        try {
            String imagePaths = saveImages(images);
            activityPostService.createPost(title, location, description, imagePaths, tourId, manager);
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
            @RequestParam(value = "images", required = false) List<MultipartFile> images,
            @RequestParam(value = "keepImages", required = false) String keepImages,
            @RequestParam(value = "tourId", required = false) String tourId,
            HttpSession session,
            Model model) {

        Communitymanager manager = (Communitymanager) session.getAttribute("loggedInManager");
        if (manager == null)
            return "redirect:/manager/login";

        try {
            // imagePaths สุดท้าย = รูปเดิมที่ผู้ใช้ยังเก็บไว้ (keepImages) + รูปใหม่ที่เพิ่งอัปโหลด
            // เพื่อรองรับการลบรูปเดิมทีละรูปจาก grid เดียวกันในหน้าแก้ไขโพสต์
            String imagePaths;
            if (keepImages != null) {
                List<String> finalPaths = new ArrayList<>();
                if (!keepImages.isBlank()) {
                    for (String p : keepImages.split("\\|\\|")) {
                        if (!p.isBlank()) {
                            finalPaths.add(p);
                        }
                    }
                }

                String newImagePaths = saveImages(images); // null = ไม่มีไฟล์ใหม่
                if (newImagePaths != null) {
                    finalPaths.addAll(List.of(newImagePaths.split("\\|\\|")));
                }
                imagePaths = String.join("||", finalPaths);
            } else {
                // ไม่มี keepImages ส่งมา (เผื่อเรียกจากที่อื่น) -> ใช้พฤติกรรมเดิม
                imagePaths = saveImages(images); // null = ไม่มีการอัปโหลดรูปใหม่ ให้ service คงรูปเดิมไว้
            }

            Activitypost updated = activityPostService.updatePost(
                    activityId, title, location, description, imagePaths, tourId);
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