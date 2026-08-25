package com.example.miniproject.service.Homestay;

import com.example.miniproject.entity.Booking;
import com.example.miniproject.entity.enums.BookingStatus;
import com.example.miniproject.repository.Member.BookingRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class BookingOwnerService {

    @Autowired
    private BookingRepository bookingRepository;

    // ─── ดึงการจองทั้งหมดของ homestay ──────────────────────────────────────────
    public List<Booking> getBookingsByHomestayId(Integer homestayId) {
        return bookingRepository.findAllByHomestayId(homestayId);
    }

    // ─── ดึงการจองกรอง status ──────────────────────────────────────────────────
    public List<Booking> getBookingsByHomestayIdAndStatus(Integer homestayId, BookingStatus status) {
        if (status == null) {
            return bookingRepository.findAllByHomestayId(homestayId);
        }
        return bookingRepository.findAllByHomestayIdAndStatus(homestayId, status);
    }

    // ─── ยืนยันการจอง (WAITING_APPROVAL → CONFIRMED) ──────────────────────────
    @Transactional
    public void confirmBooking(String bookingId, Integer homestayId) {
        Booking booking = bookingRepository.findByIdWithDetails(bookingId)
                .orElseThrow(() -> new RuntimeException("ไม่พบการจอง: " + bookingId));

        validateHomestayOwnership(booking, homestayId);

        if (booking.getBookingStatus() != BookingStatus.WAITING_APPROVAL) {
            throw new IllegalStateException("สถานะการจองต้องเป็น 'รอตรวจสอบ' เท่านั้น");
        }

        booking.setBookingStatus(BookingStatus.CONFIRMED);
        bookingRepository.save(booking);
    }

    // ─── ยกเลิกการจอง (owner ยกเลิก) ─────────────────────────────────────────
    // ─── ยกเลิกการจอง (owner ยกเลิก) — ยกเลิกซ้ำ, ยกเลิกที่ยืนยันแล้ว
    //     หรือยกเลิกที่เสร็จสิ้นแล้วไม่ได้ ─────────────────────────────────────
    @Transactional
    public void cancelBookingByOwner(String bookingId, Integer homestayId, String reason) {
        if (reason == null || reason.isBlank()) {
            throw new IllegalArgumentException("กรุณาระบุเหตุผลในการยกเลิก");
        }

        Booking booking = bookingRepository.findByIdWithDetails(bookingId)
                .orElseThrow(() -> new RuntimeException("ไม่พบการจอง: " + bookingId));

        validateHomestayOwnership(booking, homestayId);

        if (booking.getBookingStatus() == BookingStatus.CANCEL) {
            throw new IllegalStateException("การจองนี้ถูกยกเลิกไปแล้ว");
        }
        if (booking.getBookingStatus() == BookingStatus.CONFIRMED) {
            throw new IllegalStateException("ไม่สามารถยกเลิกการจองที่ยืนยันแล้วได้");
        }
        if (booking.getBookingStatus() == BookingStatus.COMPLETED) {
            throw new IllegalStateException("ไม่สามารถยกเลิกการจองที่เสร็จสิ้นแล้วได้");
        }

        booking.setBookingStatus(BookingStatus.CANCEL);
        booking.setCancelReason("ยกเลิกโดยเจ้าของโฮมสเตย์: " + reason.trim());
        bookingRepository.save(booking);
    }

    // ─── ดึงรายละเอียดการจอง ──────────────────────────────────────────────────
    public Booking getBookingDetail(String bookingId) {
        return bookingRepository.findByIdWithDetails(bookingId)
                .orElseThrow(() -> new RuntimeException("ไม่พบการจอง: " + bookingId));
    }

    // ─── helper: ตรวจสอบว่าการจองนี้เป็นของ homestay นี้จริง ─────────────────
    private void validateHomestayOwnership(Booking booking, Integer homestayId) {
        boolean belongs = booking.getRoomDetails() != null &&
                booking.getRoomDetails().stream()
                        .anyMatch(rd -> rd.getRoomtype() != null &&
                                rd.getRoomtype().getHomestay() != null &&
                                homestayId.equals(rd.getRoomtype().getHomestay().getHomestayid()));
        if (!belongs) {
            throw new IllegalArgumentException("การจองนี้ไม่ได้เป็นของโฮมสเตย์นี้");
        }
    }
}