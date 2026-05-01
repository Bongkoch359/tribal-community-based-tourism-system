package com.example.miniproject.controller.Member;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import com.example.miniproject.entity.Activitypost;
import com.example.miniproject.entity.Homestay;
import com.example.miniproject.entity.Tour;
import com.example.miniproject.service.Member.SearchInfoService;

@Controller
@RequestMapping("/search")
public class SearchInfoController {

    @Autowired
    private SearchInfoService searchInfoService;

    @GetMapping
    public String searchPage(
            @RequestParam(defaultValue = "")         String  keyword,
            @RequestParam(defaultValue = "")         String  date,
            @RequestParam(defaultValue = "1")        Integer numGuest,
            @RequestParam(defaultValue = "activity") String  type,
            Model model) {

        if (numGuest < 1) {
            model.addAttribute("errorMessage", "กรุณากรอกข้อมูลให้ถูกต้อง");
            numGuest = 1;
        }

        List<Activitypost> activities = searchInfoService.searchActivity(keyword);
        List<Tour>         tours      = searchInfoService.searchTour(keyword, numGuest);
        List<Homestay>     homestays  = searchInfoService.searchHomestay(keyword);

        model.addAttribute("activities",    activities);
        model.addAttribute("tours",         tours);
        model.addAttribute("homestays",     homestays);
        model.addAttribute("keyword",       keyword);
        model.addAttribute("date",          date);
        model.addAttribute("numGuest",      numGuest);
        model.addAttribute("currentType",   type);  // ← ส่ง type กลับไปให้ HTML
        model.addAttribute("activityCount", activities.size());
        model.addAttribute("tourCount",     tours.size());
        model.addAttribute("homestayCount", homestays.size());
        model.addAttribute("totalCount",    activities.size() + tours.size() + homestays.size());

        return "member_search";
    }
}