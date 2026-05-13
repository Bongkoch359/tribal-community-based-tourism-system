package com.example.miniproject.controller.Member;


import com.example.miniproject.service.Member.PaymentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/payment/tour")
public class TourPaymentController {

    @Autowired
    @Qualifier("tourPaymentService")
    private PaymentService paymentService;

    // TODO: implement tour payment endpoints
}