package com.example.miniproject.controller.Member;

import com.example.miniproject.dto.Member.RoomReceiptDTO;
import com.example.miniproject.service.Member.PaymentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/payment/homestay")
public class HomestayPaymentController {

    @Autowired
    @Qualifier("homestayPaymentService")
    private PaymentService<RoomReceiptDTO> paymentService;

    // GET: แสดงหน้าชำระเงินโฮมสเตย์
   @GetMapping("/{bookingId}")
public String showPaymentPage(@PathVariable String bookingId, Model model,
                               RedirectAttributes redirectAttributes) {
    try {
        paymentService.cancelIfExpired(bookingId);

        if (paymentService.isPaymentExpired(bookingId)) {
            redirectAttributes.addFlashAttribute("errorMsg",
                    "เลยกำหนดชำระเงินแล้ว การจองนี้ถูกยกเลิกอัตโนมัติ กรุณาจองใหม่อีกครั้ง");
            return "redirect:/member/bookings/detail/" + bookingId;
        }

        RoomReceiptDTO payment = paymentService.getPaymentPageData(bookingId);
        model.addAttribute("payment", payment);
    } catch (Exception e) {
        model.addAttribute("errorMsg", "ไม่พบข้อมูลการจอง: " + e.getMessage());
        model.addAttribute("payment", new RoomReceiptDTO());
    }
    return "Member/homestay-payment";
}

    // POST: รับสลิปและยืนยันการชำระเงิน
    @PostMapping("/{bookingId}/confirm")
    public String confirmPayment(
            @PathVariable String bookingId,
            @RequestParam(value = "slipFile", required = false) MultipartFile slipFile,
            @RequestParam(value = "payNote", required = false) String payNote,
            RedirectAttributes redirectAttributes) {

        try {
            paymentService.confirmPayment(bookingId, slipFile, payNote);
            redirectAttributes.addFlashAttribute("successMsg",
                    "ส่งหลักฐานการชำระเงินสำเร็จ! กรุณารอการยืนยันจากโฮมสเตย์");
            return "redirect:/member/bookings/detail/" + bookingId;
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("errorMsg", e.getMessage());
            return "redirect:/payment/homestay/" + bookingId;
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMsg", "เกิดข้อผิดพลาด กรุณาลองใหม่อีกครั้ง");
            return "redirect:/payment/homestay/" + bookingId;
        }
    }
}