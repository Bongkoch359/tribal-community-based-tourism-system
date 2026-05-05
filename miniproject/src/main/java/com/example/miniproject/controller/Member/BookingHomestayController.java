package com.example.miniproject.controller.Member;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import com.example.miniproject.entity.Roomtype;
import com.example.miniproject.service.Homestay.RoomTypeService;

@Controller
public class BookingHomestayController {

    @Autowired
    private RoomTypeService roomtypeService;

    @GetMapping("/booking/homestay/{id}")
    public String bookingPage(@PathVariable("id") String id,
                              Model model){

        Roomtype room = roomtypeService.getRoomById(id);

        model.addAttribute("room", room);

        return "Member/booking_homestay";
    }

}