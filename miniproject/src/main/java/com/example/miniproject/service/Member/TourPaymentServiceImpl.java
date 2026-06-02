package com.example.miniproject.service.Member;

import com.example.miniproject.dto.Member.PaymentDTO;
import com.example.miniproject.entity.Booking;
import com.example.miniproject.entity.Bookingtourdetail;
import com.example.miniproject.entity.Communitymanager;
import com.example.miniproject.entity.Payment;
import com.example.miniproject.entity.enums.BookingStatus;
import com.example.miniproject.entity.enums.PaymentStatus;
import com.example.miniproject.repository.Member.BookingRepository;
import com.example.miniproject.repository.Member.PaymentRepository;
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
public class TourPaymentServiceImpl implements PaymentService {

    @Autowired
    private PaymentRepository paymentRepository;

    @Autowired
    private BookingRepository bookingRepository;

    
    // ✅ แก้ใหม่ — ลบ @Value ออก แล้วใช้ user.dir แทน
    private final String slipUploadDir = System.getProperty("user.dir") + "/uploads/slips/";

    // ─────────────────────────────────────────────────────────────
    // ดึงข้อมูลสำหรับแสดงหน้าชำระเงิน (ทัวร์)
    // ─────────────────────────────────────────────────────────────
    @Override
    public PaymentDTO getPaymentPageData(String bookingId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new RuntimeException("ไม่พบการจอง: " + bookingId));

        PaymentDTO dto = new PaymentDTO();
        dto.setBookingId(booking.getBookingid());
        dto.setBookingDate(booking.getBookingdate());
        dto.setTotalAmount(booking.getTotalamount());
        dto.setNumOfGuests(booking.getNumofguest());

        // ดึงข้อมูลจาก tourDetails (Bookingtourdetail)
        List<Bookingtourdetail> tourDetails = booking.getTourDetails();
        if (tourDetails != null && !tourDetails.isEmpty()) {
            Bookingtourdetail detail = tourDetails.get(0);

            // วันเริ่มทัวร์  →  startdate (ทัวร์ไม่มี checkout ใช้ checkIn อย่างเดียว)
        dto.setCheckIn(detail.getStartdate());
        if (detail.getStartdate() != null) {
        Date deadline = Date.valueOf(
        detail.getStartdate().toLocalDate().minusDays(1)
    );
    dto.setPaymentDeadline(deadline);
}
            dto.setCheckOut(null);

            // จำนวนผู้ใหญ่ / เด็ก
            dto.setNumOfAdults(detail.getNumofadult());
            dto.setNumOfChildren(detail.getNumofchild());
            dto.setNumOfRooms(null); // ทัวร์ไม่มีห้อง

            // ข้อมูล Tour
            if (detail.getTour() != null) {
                dto.setRoomTypeName(detail.getTour().getTourmname()); // ใช้ field เดิมเก็บชื่อทัวร์

                // รูปทัวร์
            if (detail.getTour().getImages() != null&& !detail.getTour().getImages().isEmpty()) {

            String images = detail.getTour().getImages();

    // แยกรูปด้วย ||
            String[] imageArray = images.split("\\|\\|");

    // เอารูปแรก
            String firstImage = imageArray[0].trim();

            dto.setRoomImageUrl("/uploads/tours/" + firstImage);
}

               
        // ✅ แก้ใหม่ให้ตรงกับ Communitymanager entity
        if (detail.getTour().getCommunitymanager() != null) {
        Communitymanager mgr = detail.getTour().getCommunitymanager();
    
        // ชื่อเต็ม = firstname + lastname
        String fullName = mgr.getFirstname() + " " + mgr.getLastname();
        dto.setHomestayName(fullName);
    
        // Communitymanager ไม่มี address → ใช้ tribe แทน (หรือ set null ถ้าไม่ต้องการแสดง)
        dto.setHomestayAddress(mgr.getTribe());

         dto.setBankName(mgr.getBankName());
        dto.setBankAccount(mgr.getAccountNumber());
       dto.setAccountName(mgr.getAccountName());
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

        String savedFileName = saveSlipFile(slipFile, bookingId);

        // หา Payment เดิม ถ้าไม่มีสร้างใหม่
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

        // ✅ อัปเดตสถานะ Booking หลังบันทึก Payment
        booking.setBookingStatus(BookingStatus.WAITING_APPROVAL);
        bookingRepository.save(booking);
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
}