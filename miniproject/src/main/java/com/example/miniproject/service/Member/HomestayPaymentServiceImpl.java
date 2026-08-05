package com.example.miniproject.service.Member;

import com.example.miniproject.dto.Member.RoomReceiptDTO;
import com.example.miniproject.entity.Booking;
import com.example.miniproject.entity.Bookingroomdetail;
import com.example.miniproject.entity.Payment;
import com.example.miniproject.entity.enums.PaymentStatus;
import com.example.miniproject.repository.Member.BookingRepository;
import com.example.miniproject.repository.Member.PaymentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import com.example.miniproject.entity.enums.BookingStatus;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.sql.Date;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Service("homestayPaymentService")
public class HomestayPaymentServiceImpl implements PaymentService<RoomReceiptDTO> {

    @Autowired
    private PaymentRepository paymentRepository;

    @Autowired
    private BookingRepository bookingRepository;

    private final String slipUploadDir = System.getProperty("user.dir") + "/uploads/slips/";

    // ─────────────────────────────────────────────────────────────
    // ดึงข้อมูลสำหรับแสดงหน้าชำระเงิน
    // ─────────────────────────────────────────────────────────────
    @Override
    public RoomReceiptDTO getPaymentPageData(String bookingId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new RuntimeException("ไม่พบการจอง: " + bookingId));

        RoomReceiptDTO dto = new RoomReceiptDTO();
        dto.setBookingId(booking.getBookingid());
        dto.setBookingDate(booking.getBookingdate());
        dto.setTotalAmount(booking.getTotalamount());

        // ── ผู้จ่ายเงิน ──
        if (booking.getMember() != null) {
            dto.setMemberFirstname(booking.getMember().getFirstname());
            dto.setMemberLastname(booking.getMember().getLastname());
            dto.setMemberPhone(booking.getMember().getPhone());
        }

        // ดึงข้อมูลจาก roomDetails (Bookingroomdetail)
        List<Bookingroomdetail> roomDetails = booking.getRoomDetails();

        if (roomDetails != null && !roomDetails.isEmpty()) {

            Bookingroomdetail detail = roomDetails.get(0);

            // วันเช็คอิน / เช็คเอาท์
            dto.setCheckIn(detail.getCheckindate());
            if (detail.getCheckindate() != null) {
                Date deadline = Date.valueOf(
                        detail.getCheckindate().toLocalDate().minusDays(1)
                );
                dto.setPaymentDeadline(deadline);
            }
            dto.setCheckOut(detail.getCheckoutdate());

            // จำนวนห้อง / ผู้ใหญ่ / เด็ก
            dto.setNumOfRooms(detail.getNumofrooms());
            dto.setNumOfAdults(detail.getNumofadults());
            dto.setNumOfChildren(detail.getNumofChcldren());

            // ── ใบเสร็จ: ค่าห้อง + ประกัน ──
            dto.setRoomSubtotal(detail.getSubtotalroom());
            dto.setWantInsurance(booking.getWantInsurance());
            dto.setSubtotalInsurance(booking.getSubtotalInsurance());

            // ข้อมูล Roomtype
            if (detail.getRoomtype() != null) {

                dto.setRoomTypeName(detail.getRoomtype().getTypename());

                // รูปห้อง
                String images = detail.getRoomtype().getImages();
                if (images != null && !images.isEmpty()) {
                    String[] imageArray = images.split(",");
                    String firstImage = imageArray[0].trim();
                    dto.setRoomImageUrl(firstImage);
                }

                // ข้อมูล Homestay
                if (detail.getRoomtype().getHomestay() != null) {

                    dto.setHomestayName(detail.getRoomtype().getHomestay().getHomestayname());
                    dto.setHomestayAddress(detail.getRoomtype().getHomestay().getAddress());

                    // ข้อมูลบัญชีธนาคาร
                    if (detail.getRoomtype().getHomestay().getOwner() != null) {
                        dto.setBankName(detail.getRoomtype().getHomestay().getOwner().getBankName());
                        dto.setBankAccount(detail.getRoomtype().getHomestay().getOwner().getAccountNumber());
                        dto.setAccountName(detail.getRoomtype().getHomestay().getOwner().getAccountName());
                    }
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
    // ยืนยันการชำระเงิน
    // ─────────────────────────────────────────────────────────────
    @Override
    @Transactional
    public void confirmPayment(String bookingId, MultipartFile slipFile, String payNote) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new RuntimeException("ไม่พบการจอง: " + bookingId));

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
    // ดึงข้อมูลสำหรับแสดงหน้าใบเสร็จ
    // ─────────────────────────────────────────────────────────────
    @Override
    public RoomReceiptDTO getReceiptData(String bookingId) {
        RoomReceiptDTO dto = getPaymentPageData(bookingId);

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