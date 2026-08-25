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

/**
 * งานพื้นหลังที่คอยยกเลิกการจองทัวร์อัตโนมัติ เมื่อผู้จองไม่ชำระเงินภายใน
 * กำหนดเวลา (deadline = วันเดินทาง(opendate) - 1 วัน)
 *
 * นี่คือ "ชั้นป้องกันหลัก" ที่ทำให้ booking ที่ถูกลืมไว้ (ไม่มีใครเข้ามาดูหน้า
 * payment อีกเลย) ก็ยังถูกยกเลิกได้ โดยไม่ต้องพึ่งการที่ผู้ใช้ต้องเปิดหน้าใดหน้าหนึ่ง
 * ก่อนถึงจะ trigger การเช็ค
 *
 * หมายเหตุ: ต้องเปิดใช้งาน @EnableScheduling ในคลาส main ของ Spring Boot
 * (@SpringBootApplication) มิเช่นนั้น @Scheduled จะไม่ทำงานเลย
 */
@Service
public class BookingAutoCancelService {

    private static final Logger log = LoggerFactory.getLogger(BookingAutoCancelService.class);

    @Autowired
    private BookingRepository bookingRepository;

    /**
     * รันทุกต้นชั่วโมง — sweep booking ทัวร์ที่ยัง PENDING แต่ deadline ผ่านไปแล้ว
     * แล้วเปลี่ยนสถานะเป็น CANCEL พร้อมบันทึกเหตุผล
     */
    @Scheduled(fixedRate = 10000)
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