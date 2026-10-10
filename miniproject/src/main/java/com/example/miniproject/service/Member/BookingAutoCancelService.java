package com.example.miniproject.service.Member;

import com.example.miniproject.entity.Booking;
import com.example.miniproject.entity.enums.BookingStatus;
import com.example.miniproject.repository.Member.BookingRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;


@Service
public class BookingAutoCancelService {

    private static final Logger log = LoggerFactory.getLogger(BookingAutoCancelService.class);

    @Autowired
    private BookingRepository bookingRepository;

    
    @Scheduled(fixedRate = 60000) 
@Transactional
public void cancelExpiredPendingBookings() {
    List<Booking> expiredTours = bookingRepository.findExpiredPendingTourBookings();
    List<Booking> expiredRooms = bookingRepository.findExpiredPendingRoomBookings();

     System.out.println("===== AUTO CANCEL CHECK =====");
    System.out.println("Expired tours: " + expiredTours.size());
    System.out.println("Expired rooms: " + expiredRooms.size());


    List<Booking> expired = new java.util.ArrayList<>();
    expired.addAll(expiredTours);
    expired.addAll(expiredRooms);

    if (expired.isEmpty()) {
        return;
    }

    for (Booking b : expired) {
        b.setBookingStatus(BookingStatus.CANCEL);
        b.setCancelReason("ยกเลิกอัตโนมัติ: ไม่ชำระเงินภายในกำหนดเวลา");
        bookingRepository.save(b);
    }

    log.info("Auto-cancelled {} expired pending booking(s) [{} tour, {} room]: {}",
            expired.size(), expiredTours.size(), expiredRooms.size(),
            expired.stream().map(Booking::getBookingid).toList());
}
}