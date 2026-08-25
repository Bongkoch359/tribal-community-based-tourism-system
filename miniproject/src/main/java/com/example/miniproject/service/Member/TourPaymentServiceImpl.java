package com.example.miniproject.service.Member;

import com.example.miniproject.dto.Member.TourReceiptDTO;
import com.example.miniproject.entity.Booking;
import com.example.miniproject.entity.Bookingtourdetail;
import com.example.miniproject.entity.Communitymanager;
import com.example.miniproject.entity.Payment;
import com.example.miniproject.entity.Tour;
import com.example.miniproject.entity.Tourschedule;
import com.example.miniproject.entity.enums.BookingStatus;
import com.example.miniproject.entity.enums.PaymentStatus;
import com.example.miniproject.repository.Member.BookingRepository;
import com.example.miniproject.repository.Member.PaymentRepository;
import com.example.miniproject.repository.Tour.TourScheduleRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.sql.Date;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Service("tourPaymentService")
public class TourPaymentServiceImpl implements PaymentService<TourReceiptDTO> {

    @Autowired
    private PaymentRepository paymentRepository;

    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
private BookingCancelHelper bookingCancelHelper;

@Autowired
private TourScheduleRepository tourScheduleRepository;

    private final String slipUploadDir = System.getProperty("user.dir") + "/uploads/slips/";

    // ─────────────────────────────────────────────────────────────
    // ดึงข้อมูลสำหรับแสดงหน้าชำระเงิน (ทัวร์)
    // ─────────────────────────────────────────────────────────────
    @Override
    public TourReceiptDTO getPaymentPageData(String bookingId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new RuntimeException("ไม่พบการจอง: " + bookingId));

        TourReceiptDTO dto = new TourReceiptDTO();
        dto.setBookingId(booking.getBookingid());
        dto.setBookingDate(booking.getBookingdate());
        dto.setTotalAmount(booking.getTotalamount());

        // ── ผู้จ่ายเงิน ──
        if (booking.getMember() != null) {
            dto.setMemberFirstname(booking.getMember().getFirstname());
            dto.setMemberLastname(booking.getMember().getLastname());
            dto.setMemberPhone(booking.getMember().getPhone());
        }

        // ดึงข้อมูลจาก tourDetails (Bookingtourdetail)
        List<Bookingtourdetail> tourDetails = booking.getTourDetails();
        if (tourDetails != null && !tourDetails.isEmpty()) {
            Bookingtourdetail detail = tourDetails.get(0);

            // วันเริ่มทัวร์ → อ่านจาก tourschedule.opendate
            Date openDate = detail.getTourschedule() != null
                    ? detail.getTourschedule().getOpendate()
                    : null;

            dto.setScheduleOpenDate(openDate);

// ★ deadline ดึงจาก booking ตรงๆ ไม่คำนวณจาก opendate อีกต่อไป
dto.setPaymentDeadline(booking.getPaymentDeadline());

            // จำนวนผู้ใหญ่ / เด็ก
            dto.setNumOfAdults(detail.getNumofadult());
            dto.setNumOfChildren(detail.getNumofchild());
            dto.setSubtotalTour(detail.getSubtotaltour());

            // ── ประกันภัย ──
            dto.setWantInsurance(booking.getWantInsurance());
            dto.setSubtotalInsurance(booking.getSubtotalInsurance());

            // ข้อมูล Tour
            if (detail.getTour() != null) {
                dto.setTourName(detail.getTour().getTourmname());
                dto.setTourDuration(detail.getTour().getTourDuration());
                dto.setAdultPrice(detail.getTour().getAdultprice());
                dto.setChildPrice(detail.getTour().getChildprice());

                // ── รูปทัวร์ ──
                String images = detail.getTour().getImages();
                if (images != null && !images.isEmpty()) {
                    String[] imageArray = images.split("\\|\\|");
                    String firstImage = imageArray[0].trim();
                    dto.setTourImageUrl("/uploads/tours/" + firstImage);
                }

                // ข้อมูล Communitymanager
                if (detail.getTour().getCommunitymanager() != null) {
                    Communitymanager mgr = detail.getTour().getCommunitymanager();

                    String fullName = mgr.getFirstname() + " " + mgr.getLastname();
                    dto.setCommunityManagerName(fullName);
                    dto.setCommunityManagerAddress(mgr.getTribe());

                    dto.setBankName(mgr.getBankName());
                    dto.setBankAccount(mgr.getAccountNumber());
                    dto.setAccountName(mgr.getAccountName());
                    dto.setSignatureImageUrl(mgr.getSignatureImageUrl());
                }
            }
        }

        // สถานะ payment ปัจจุบัน
        Payment existing = paymentRepository.findByBooking_Bookingid(bookingId);
        if (existing != null) {
            dto.setPaymentStatus(existing.getPaymentStatus().name());
        }

        return dto;
    }

    // ─────────────────────────────────────────────────────────────
    // ยืนยันการชำระเงิน (ทัวร์)
    // ─────────────────────────────────────────────────────────────
    @Override
    @Transactional
   public void confirmPayment(String bookingId, MultipartFile slipFile, String payNote) {
    Booking booking = bookingRepository.findById(bookingId)
            .orElseThrow(() -> new RuntimeException("ไม่พบการจอง: " + bookingId));

             if (isExpired(booking)) {
        bookingCancelHelper.cancelNow(bookingId, "ยกเลิกอัตโนมัติ: ไม่ชำระเงินภายในกำหนดเวลา");
        throw new IllegalStateException("เลยกำหนดชำระเงินแล้ว การจองนี้ถูกยกเลิกอัตโนมัติ กรุณาจองใหม่อีกครั้ง");
    }
    Bookingtourdetail detail = booking.getTourDetails().get(0);
    Tourschedule schedule = detail.getTourschedule();
    Tour tour = detail.getTour();

if (tour.getMaxSeatstour() != null) {
    int totalBooked = tourScheduleRepository.countBookedSeatsBySchedule(schedule.getScheduleid());
    if (totalBooked > tour.getMaxSeatstour()) {
        bookingCancelHelper.cancelNow(bookingId, "ยกเลิกอัตโนมัติ: ที่นั่งเต็มก่อนชำระเงิน");
        throw new IllegalStateException("ขออภัย ที่นั่งเต็มแล้วก่อนที่คุณจะชำระเงิน การจองถูกยกเลิก กรุณาจองใหม่อีกครั้ง");
    }
}

        String savedFileName = saveSlipFile(slipFile, bookingId);

        Payment payment = paymentRepository.findByBooking_Bookingid(bookingId);
        if (payment == null) {
            payment = new Payment();
            payment.setPaymentid("PAY-" + UUID.randomUUID().toString().substring(0, 6).toUpperCase());
            payment.setBooking(booking);
        }

        payment.setPaymentdate(Date.valueOf(LocalDate.now()));
        payment.setAmount(booking.getTotalamount());
        payment.setPaymentslip(savedFileName);
        payment.setPaymentStatus(PaymentStatus.PAID);

        paymentRepository.save(payment);

        booking.setBookingStatus(BookingStatus.WAITING_APPROVAL);
        bookingRepository.save(booking);
    }

    // ─────────────────────────────────────────────────────────────
    // เช็คว่าการจองนี้เลยกำหนดชำระเงินแล้วหรือยัง (public — ให้ controller เรียกใช้)
    // ─────────────────────────────────────────────────────────────
    @Override
    public boolean isPaymentExpired(String bookingId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new RuntimeException("ไม่พบการจอง: " + bookingId));
        return isExpired(booking);
    }

    // ─────────────────────────────────────────────────────────────
    // ยกเลิก booking ทันทีถ้าหมดเวลาแล้ว — เรียกจาก controller ตอนเปิดหน้า/ก่อน confirm
    // เพื่อไม่ต้องรอ scheduled job รอบถัดไป (สูงสุด 1 ชั่วโมง)
    // ─────────────────────────────────────────────────────────────
    @Override
    @Transactional
    public void cancelIfExpired(String bookingId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new RuntimeException("ไม่พบการจอง: " + bookingId));

        if (isExpired(booking) && booking.getBookingStatus() == BookingStatus.PENDING) {
            booking.setBookingStatus(BookingStatus.CANCEL);
            booking.setCancelReason("ยกเลิกอัตโนมัติ: ไม่ชำระเงินภายในกำหนดเวลา");
            bookingRepository.save(booking);
        }
    }

    /**
     * ตรรกะกลางในการเช็ค deadline — ใช้ทั้งใน isPaymentExpired / cancelIfExpired / confirmPayment
     * deadline = วันเดินทาง(opendate) - 1 วัน (23:59:59)
     * ถือว่าหมดเวลาเมื่อ "วันนี้" มาหลังจากวัน deadline นั้นแล้ว
     */
  private boolean isExpired(Booking booking) {
    if (booking.getPaymentDeadline() == null) {
        return false;
    }
    return new java.sql.Timestamp(System.currentTimeMillis()).after(booking.getPaymentDeadline());
}

    // ─────────────────────────────────────────────────────────────
    // Helper: บันทึกไฟล์สลิปลง disk
    // ─────────────────────────────────────────────────────────────
    private String saveSlipFile(MultipartFile file, String bookingId) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("กรุณาอัปโหลดสลิปการโอนเงิน");
        }
        try {
            Path uploadPath = Paths.get(slipUploadDir);
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }
            String original = file.getOriginalFilename();
            String ext = (original != null && original.contains("."))
                    ? original.substring(original.lastIndexOf('.'))
                    : ".jpg";
            String fileName = bookingId + "_" + System.currentTimeMillis() + ext;
            Files.copy(file.getInputStream(), uploadPath.resolve(fileName),
                    StandardCopyOption.REPLACE_EXISTING);
            return fileName;
        } catch (IOException e) {
            throw new RuntimeException("ไม่สามารถบันทึกสลิปได้: " + e.getMessage(), e);
        }
    }

    // ─────────────────────────────────────────────────────────────
    // ดึงข้อมูลสำหรับแสดงหน้าใบเสร็จ (ทัวร์)
    // ─────────────────────────────────────────────────────────────
    @Override
    public TourReceiptDTO getReceiptData(String bookingId) {
        TourReceiptDTO dto = getPaymentPageData(bookingId);

        Payment payment = paymentRepository.findByBooking_Bookingid(bookingId);
        if (payment == null) {
            throw new RuntimeException("ไม่พบข้อมูลการชำระเงินของการจองนี้: " + bookingId);
        }

        dto.setPaymentId(payment.getPaymentid());
        dto.setPaymentDate(payment.getPaymentdate());
        dto.setAmount(payment.getAmount());
        dto.setPaymentSlip(payment.getPaymentslip());
        dto.setPaymentStatus(payment.getPaymentStatus().name());

        return dto;
    }
}