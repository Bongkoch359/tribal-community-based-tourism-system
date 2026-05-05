package com.example.miniproject.controller.Member;

import com.example.miniproject.entity.Tour;
import com.example.miniproject.repository.Member.TourRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;

@Controller
public class ViewTourDetailController {

    @Autowired
    private TourRepository tourRepository;

    @GetMapping("/tour/{id}")
    public String viewTourDetail(@PathVariable String id, Model model) {

        Tour tour = tourRepository.findById(id).orElse(null);

        if (tour == null) {
            return "redirect:/search"; // ถ้าไม่เจอ tour ให้กลับหน้าแรก
        }

        model.addAttribute("tour", tour);
        model.addAttribute("reviews", List.of()); // ยังไม่มี review ส่ง list ว่างไปก่อน

        return "Tour/tour_detail";
    }
}