package com.example.miniproject.service.Member;

import com.example.miniproject.dto.Member.PaymentDTO;
import org.springframework.web.multipart.MultipartFile;

public interface PaymentService {

    //กลาง
    PaymentDTO getPaymentPageData(String bookingId);

    void confirmPayment(String bookingId, MultipartFile slipFile, String payNote);

    PaymentDTO getReceiptData(String bookingId);
}