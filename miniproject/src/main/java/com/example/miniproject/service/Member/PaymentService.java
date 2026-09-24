package com.example.miniproject.service.Member;

import org.springframework.web.multipart.MultipartFile;

public interface PaymentService<T> {

   
    T getPaymentPageData(String bookingId);

    void confirmPayment(String bookingId, MultipartFile slipFile, String payNote);

    T getReceiptData(String bookingId);

    // สำหรับเช็ค/ยกเลิกเมื่อหมดกำหนดชำระเงิน ──
    boolean isPaymentExpired(String bookingId);

    void cancelIfExpired(String bookingId);
}