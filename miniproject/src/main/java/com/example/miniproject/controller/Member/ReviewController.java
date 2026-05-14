package com.example.miniproject.controller.Member;


import com.example.miniproject.entity.Member;
import com.example.miniproject.service.Member.ReviewService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/member/review")
public class ReviewController {

    @Autowired
    private ReviewService reviewService;

    @PostMapping("/submit")
    public String submitReview(
            @RequestParam("bookingId")                          String bookingId,
            @RequestParam("rating")                             Integer rating,
            @RequestParam(value = "comment",   required = false) String comment,
            @RequestParam(value = "reviewimage", required = false) MultipartFile imageFile,
            HttpSession session,
            RedirectAttributes redirectAttributes) {

        Member member = (Member) session.getAttribute("loggedInMember");
        if (member == null) return "redirect:/member/login";

        try {
            reviewService.submitReview(
                bookingId, member.getMemberid(),
                rating, comment, imageFile);

            redirectAttributes.addFlashAttribute("successMsg",
                "ขอบคุณสำหรับรีวิวของคุณ! 🌟");

        } catch (IllegalArgumentException | IllegalStateException e) {
            redirectAttributes.addFlashAttribute("errorMsg", e.getMessage());

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMsg",
                "ไม่สามารถส่งรีวิวได้ กรุณาลองใหม่");
        }

        return "redirect:/member/bookings/detail/" + bookingId;
    }
}