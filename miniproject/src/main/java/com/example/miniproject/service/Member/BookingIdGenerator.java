package com.example.miniproject.service.Member;

import com.example.miniproject.repository.Member.BookingRepository;
import com.example.miniproject.repository.Member.GuestRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

// ─── ตัวสร้าง ID ที่ใช้ร่วมกันระหว่างการจองโฮมสเตย์ (BookingService)
//      และการจองทัวร์ (TourBookingService) เพื่อไม่ให้เลข booking/guest ชนกัน ───
@Component
public class BookingIdGenerator {

    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private GuestRepository guestRepository;

    public String generateBookingId() {
        String date  = LocalDate.now().format(DateTimeFormatter.ofPattern("MMdd"));
        long   count = bookingRepository.count() + 1;
        return "BK" + date + String.format("%04d", count);
    }

    public String generateGuestId() {
        long count = guestRepository.count() + 1;
        return "GS" + String.format("%08d", count);
    }
}