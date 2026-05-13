package com.example.miniproject.service.Member;


import com.example.miniproject.dto.Member.PaymentDTO;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
 
@Service("tourPaymentService")
public class TourPaymentServiceImpl implements PaymentService {
 
    @Override
    public PaymentDTO getPaymentPageData(String bookingId) {
        // TODO: implement tour payment page data
        throw new UnsupportedOperationException("ยังไม่ได้ implement");
    }
 
    @Override
    public void confirmPayment(String bookingId, MultipartFile slipFile, String payNote) {
        // TODO: implement tour payment confirm
        throw new UnsupportedOperationException("ยังไม่ได้ implement");
    }
}
 
