package com.example.miniproject.controller.Member;

import com.example.miniproject.dto.Homestay.HomestayDetailDto;
import com.example.miniproject.service.Homestay.HomestayService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/homestay")
public class ViewHomestayDetailController {

    @Autowired
    private HomestayService homestayService;

    @GetMapping("/{id}")
    public String homestayDetail(@PathVariable Integer id, Model model) {

        HomestayDetailDto detail = homestayService.getHomestayDetail(id);

        if (detail == null) {
            return "redirect:/search";
        }

        model.addAttribute("homestay", detail.getHomestay());
        model.addAttribute("firstImage", detail.getFirstImage());
        model.addAttribute("rooms", detail.getHomestay().getRoomtypes());

        return "Member/homestay_detail";
    }
}