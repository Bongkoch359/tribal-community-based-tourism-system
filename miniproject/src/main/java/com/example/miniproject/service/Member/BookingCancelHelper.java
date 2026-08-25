package com.example.miniproject.service.Member;

import com.example.miniproject.entity.Booking;
import com.example.miniproject.entity.enums.BookingStatus;
import com.example.miniproject.repository.Member.BookingRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
public class BookingCancelHelper {

    @Autowired
    private BookingRepository bookingRepository;

    /**
     * ยกเลิก booking ทันทีใน transaction ใหม่แยกต่างหาก (REQUIRES_NEW)
     * เพื่อไม่ให้ถูก rollback ตาม exception ที่ throw ต่อจากนี้ใน caller
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void cancelNow(String bookingId, String reason) {
        Booking booking = bookingRepository.findById(bookingId).orElse(null);
        if (booking != null && booking.getBookingStatus() == BookingStatus.PENDING) {
            booking.setBookingStatus(BookingStatus.CANCEL);
            booking.setCancelReason(reason);
            bookingRepository.save(booking);
        }
    }
}