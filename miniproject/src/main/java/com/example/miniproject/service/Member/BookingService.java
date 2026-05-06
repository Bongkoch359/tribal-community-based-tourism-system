package com.example.miniproject.service.Member;

import com.example.miniproject.entity.*;
import com.example.miniproject.entity.enums.BookingStatus;
import com.example.miniproject.entity.enums.BookingType;
import com.example.miniproject.repository.Member.BookingroomdetailRepository;
import com.example.miniproject.repository.Homestay.RoomTypeRepository;
import com.example.miniproject.repository.Member.BookingRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.sql.Date;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class BookingService {

    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private BookingroomdetailRepository bookingroomdetailRepository;

    @Autowired
    private RoomTypeRepository roomtypeRepository;

    public Booking getBookingById(String bookingId) {
        return bookingRepository.findById(bookingId).orElse(null);
    }

    // ════════════════════════════════════════════════════════
    //  LIST / COUNT
    // ════════════════════════════════════════════════════════

    /** ดึงการจองทั้งหมดของ member */
    public List<Booking> getBookingsByMember(String memberId) {
        return bookingRepository.findByMemberMemberidOrderByBookingdateDesc(memberId);
    }

    /** ดึงการจองของ member กรองตาม type + status (status = null → ทั้งหมด) */
    public List<Booking> getBookingsByMemberTypeAndStatus(
            String memberId, BookingType type, BookingStatus status) {
        return bookingRepository
                .findByMemberMemberidOrderByBookingdateDesc(memberId)
                .stream()
                .filter(b -> b.getBookingType() == type)
                .filter(b -> status == null || b.getBookingStatus() == status)
                .collect(Collectors.toList());
    }

    /** นับจำนวนการจองตาม type */
    public long countByMemberAndType(String memberId, BookingType type) {
        return bookingRepository
                .findByMemberMemberidOrderByBookingdateDesc(memberId)
                .stream()
                .filter(b -> b.getBookingType() == type)
                .count();
    }

    /** นับจำนวนตาม type + status (status = null → ทั้งหมด) */
    public long countByMemberTypeAndStatus(String memberId, BookingType type, BookingStatus status) {
        return bookingRepository
                .findByMemberMemberidOrderByBookingdateDesc(memberId)
                .stream()
                .filter(b -> b.getBookingType() == type)
                .filter(b -> status == null || b.getBookingStatus() == status)
                .count();
    }

    // ════════════════════════════════════════════════════════
    //  CREATE HOMESTAY BOOKING
    // ════════════════════════════════════════════════════════

    public String createHomestayBooking(
            Member member,
            String roomtypeId,
            String checkin,
            String checkout,
            Integer guest,
            String note) {

        // ── 1. สร้าง Booking หลัก ──────────────────────────────
        Booking booking = new Booking();
        booking.setBookingid(generateBookingId());
        booking.setMember(member);
        booking.setBookingType(BookingType.ACCOMMODATION);
        booking.setBookingStatus(BookingStatus.PENDING);
        booking.setBookingdate(new Date(System.currentTimeMillis()));
        booking.setNumofguest(guest);
        booking.setNote(note);
        bookingRepository.save(booking);
       

        // ── 2. ดึง Roomtype ────────────────────────────────────
        Roomtype roomtype = roomtypeRepository.findById(roomtypeId).orElse(null);
        if (roomtype == null) {
    throw new RuntimeException("Roomtype not found");
}

        // ── 3. คำนวณราคา ───────────────────────────────────────
        LocalDate dateIn  = LocalDate.parse(checkin);
        LocalDate dateOut = LocalDate.parse(checkout);
        long nights       = ChronoUnit.DAYS.between(dateIn, dateOut);
        double subtotal   = roomtype.getPricepernight() * nights;

        // ── 4. สร้าง Bookingroomdetail ─────────────────────────
        Bookingroomdetailid detailId = new Bookingroomdetailid();
        detailId.setBookingid(booking.getBookingid());
        detailId.setRoomtypeid(roomtypeId);

        Bookingroomdetail detail = new Bookingroomdetail();
        detail.setId(detailId);
        detail.setBooking(booking);
        detail.setRoomtype(roomtype);
        detail.setCheckindate(Date.valueOf(dateIn));
        detail.setCheckoutdate(Date.valueOf(dateOut));
        detail.setNumofadults(guest);
        detail.setNumofChcldren(0);
        detail.setNumofrooms(1);
        detail.setSubtotalroom(subtotal);
        bookingroomdetailRepository.save(detail);

        // ── 5. อัปเดต totalamount ───────────────────────────────
        booking.setTotalamount(subtotal);
        bookingRepository.save(booking);

        return booking.getBookingid();
    }

    // ════════════════════════════════════════════════════════
    //  HELPER
    // ════════════════════════════════════════════════════════

    private String generateBookingId() {

    String date = LocalDate.now().format(DateTimeFormatter.ofPattern("MMdd"));

    long count = bookingRepository.count() + 1;

    return "BK" + date + String.format("%04d", count);
}
}