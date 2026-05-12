package com.example.miniproject.controller.Member;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
public class PaymentHomestayController {

    @GetMapping("/payment/homestay/{id}")
    public String paymentHomestay(
            @PathVariable("id") String bookingId,
            Model model) {

        model.addAttribute("bookingId", bookingId);

        return "Member/payment_homestay";
    }
}