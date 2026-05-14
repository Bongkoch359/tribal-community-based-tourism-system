package com.example.miniproject.service.Member;

import com.example.miniproject.entity.*;
import com.example.miniproject.entity.enums.BookingStatus;
import com.example.miniproject.entity.enums.BookingType;
import com.example.miniproject.repository.Member.BookingroomdetailRepository;
import com.example.miniproject.repository.Member.GuestRepository;
import com.example.miniproject.repository.Homestay.RoomTypeRepository;
import com.example.miniproject.repository.Member.BookingRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Date;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class BookingService {

    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private BookingroomdetailRepository bookingroomdetailRepository;

    @Autowired
    private RoomTypeRepository roomtypeRepository;

    @Autowired
    private GuestRepository guestRepository;

    // ════════════════════════════════════════════════════════
    //  GET / FIND
    // ════════════════════════════════════════════════════════

    public Booking getBookingById(String bookingId) {
        return bookingRepository.findByIdWithDetails(bookingId).orElse(null);
    }

    // ════════════════════════════════════════════════════════
    //  LIST / COUNT
    // ════════════════════════════════════════════════════════

    public List<Booking> getBookingsByMember(String memberId) {
        return bookingRepository.findByMemberMemberidOrderByBookingdateDesc(memberId);
    }

    public List<Booking> getBookingsByMemberTypeAndStatus(
            String memberId, BookingType type, BookingStatus status) {
        return bookingRepository
                .findByMemberMemberidOrderByBookingdateDesc(memberId)
                .stream()
                .filter(b -> b.getBookingType() == type)
                .filter(b -> status == null || b.getBookingStatus() == status)
                .collect(Collectors.toList());
    }

    public long countByMemberAndType(String memberId, BookingType type) {
        return bookingRepository
                .findByMemberMemberidOrderByBookingdateDesc(memberId)
                .stream()
                .filter(b -> b.getBookingType() == type)
                .count();
    }

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

    @Transactional
    public String createHomestayBooking(
            Member member,
            String roomtypeId,
            String checkin,
            String checkout,
            Integer numofrooms,
            Integer numofAdults,
            Integer numofChildren,
            String note,
            Boolean isBookerGoing,
            String guestFirstname,
            String guestLastname) {

        // ── 1. Validate dates ──────────────────────────────────
        LocalDate dateIn  = LocalDate.parse(checkin);
        LocalDate dateOut = LocalDate.parse(checkout);

        if (!dateOut.isAfter(dateIn)) {
            throw new IllegalArgumentException("วันที่เช็คเอาท์ต้องมากกว่าวันเช็คอิน");
        }

        // ── 2. ดึง Roomtype ────────────────────────────────────
        Roomtype roomtype = roomtypeRepository.findById(roomtypeId)
                .orElseThrow(() -> new RuntimeException("ไม่พบประเภทห้องพัก: " + roomtypeId));

        // ── 3. คำนวณราคา ───────────────────────────────────────
        long   nights   = ChronoUnit.DAYS.between(dateIn, dateOut);
        int    rooms    = (numofrooms  != null && numofrooms  > 0) ? numofrooms  : 1;
        int    adults   = (numofAdults != null && numofAdults > 0) ? numofAdults : 1;
        int    children = (numofChildren != null)                  ? numofChildren : 0;
        double subtotal = roomtype.getPricepernight() * nights * rooms;

        // ── 4. สร้าง Booking หลัก ──────────────────────────────
        Booking booking = new Booking();
        booking.setBookingid(generateBookingId());
        booking.setMember(member);
        booking.setBookingType(BookingType.ACCOMMODATION);
        booking.setBookingStatus(BookingStatus.PENDING);
        booking.setBookingdate(new Date(System.currentTimeMillis()));
        booking.setNumofguest(adults + children);
        booking.setNote(note);
        booking.setIsBookerGoing(isBookerGoing != null ? isBookerGoing : true);
        booking.setTotalamount(subtotal);
        bookingRepository.save(booking);

        // ── 5. สร้าง Bookingroomdetail ─────────────────────────
        Bookingroomdetailid detailId = new Bookingroomdetailid();
        detailId.setBookingid(booking.getBookingid());
        detailId.setRoomtypeid(roomtypeId);

        Bookingroomdetail detail = new Bookingroomdetail();
        detail.setId(detailId);
        detail.setBooking(booking);
        detail.setRoomtype(roomtype);
        detail.setCheckindate(Date.valueOf(dateIn));
        detail.setCheckoutdate(Date.valueOf(dateOut));
        detail.setNumofadults(adults);
        detail.setNumofChcldren(children);
        detail.setNumofrooms(rooms);
        detail.setSubtotalroom(subtotal);
        bookingroomdetailRepository.save(detail);

        // ── 6. สร้าง Guest (กรณีจองให้ผู้อื่น) ────────────────
        if (Boolean.FALSE.equals(isBookerGoing)
                && guestFirstname != null && !guestFirstname.isBlank()) {

            Guest guest = new Guest();
            guest.setGuestid(generateGuestId());
            guest.setFirstname(guestFirstname.trim());
            guest.setLastname(guestLastname != null ? guestLastname.trim() : "");
            guest.setBooking(booking);
            guestRepository.save(guest);
        }

        return booking.getBookingid();
    }

    // ════════════════════════════════════════════════════════
    //  EDIT HOMESTAY BOOKING
    // ════════════════════════════════════════════════════════

    
    @Transactional
    public void editHomestayBooking(
            String bookingId,
            String memberId,
            String checkin,
            String checkout,
            Integer numofrooms,
            Integer numofAdults,
            Integer numofChildren,
            String note,
            String guestFirstname,   // ← เพิ่ม
            String guestLastname) {  // ← เพิ่ม
 
        // ── 1. ดึง Booking ─────────────────────────────────────
        Booking booking = bookingRepository.findByIdWithDetails(bookingId)
                .orElseThrow(() -> new RuntimeException("ไม่พบการจอง: " + bookingId));
 
        // ── 2. ตรวจสิทธิ์ ──────────────────────────────────────
        if (!booking.getMember().getMemberid().equals(memberId))
            throw new IllegalArgumentException("ไม่มีสิทธิ์แก้ไขการจองนี้");
 
        // ── 3. ตรวจสถานะ (แก้ได้เฉพาะ PENDING / WAITING_APPROVAL) ──
        BookingStatus status = booking.getBookingStatus();
        if (status != BookingStatus.PENDING && status != BookingStatus.WAITING_APPROVAL)
            throw new IllegalStateException("ไม่สามารถแก้ไขข้อมูลการจองห้องพักได้ กรุณาลองใหม่อีกครั้ง");
 
        // ── 4. Validate dates ───────────────────────────────────
        LocalDate dateIn  = LocalDate.parse(checkin);
        LocalDate dateOut = LocalDate.parse(checkout);
        if (!dateOut.isAfter(dateIn))
            throw new IllegalArgumentException("วันที่เช็คเอาท์ต้องมากกว่าวันเช็คอิน");
 
        // ── 5. ดึง Bookingroomdetail ────────────────────────────
        if (booking.getRoomDetails() == null || booking.getRoomDetails().isEmpty())
            throw new RuntimeException("ไม่พบรายละเอียดห้องพักของการจองนี้");
 
        Bookingroomdetail detail   = booking.getRoomDetails().get(0);
        Roomtype          roomtype = detail.getRoomtype();
 
        // ── 6. คำนวณราคาใหม่ ───────────────────────────────────
        int    rooms    = (numofrooms  != null && numofrooms  > 0) ? numofrooms  : 1;
        int    adults   = (numofAdults != null && numofAdults > 0) ? numofAdults : 1;
        int    children = (numofChildren != null)                  ? numofChildren : 0;
        long   nights   = ChronoUnit.DAYS.between(dateIn, dateOut);
        double subtotal = roomtype.getPricepernight() * nights * rooms;
 
        // ── 7. อัปเดต Bookingroomdetail ────────────────────────
        detail.setCheckindate(Date.valueOf(dateIn));
        detail.setCheckoutdate(Date.valueOf(dateOut));
        detail.setNumofrooms(rooms);
        detail.setNumofadults(adults);
        detail.setNumofChcldren(children);
        detail.setSubtotalroom(subtotal);
        bookingroomdetailRepository.save(detail);
 
        // ── 8. อัปเดต Booking หลัก ─────────────────────────────
        booking.setNumofguest(adults + children);
        booking.setNote(note);
        booking.setTotalamount(subtotal);
        bookingRepository.save(booking);
 
        // ── 9. อัปเดตชื่อ Guest (กรณีจองให้ผู้อื่น) ───────────
        if (Boolean.FALSE.equals(booking.getIsBookerGoing())
                && guestFirstname != null && !guestFirstname.isBlank()) {
 
            Set<Guest> guests = booking.getGuests();
            if (guests != null && !guests.isEmpty()) {
                // แก้ guest รายแรก
                Guest g = guests.iterator().next();
                g.setFirstname(guestFirstname.trim());
                g.setLastname(guestLastname != null ? guestLastname.trim() : "");
                guestRepository.save(g);
            } else {
                // ไม่มี guest เลย → สร้างใหม่
                Guest g = new Guest();
                g.setGuestid(generateGuestId());
                g.setFirstname(guestFirstname.trim());
                g.setLastname(guestLastname != null ? guestLastname.trim() : "");
                g.setBooking(booking);
                guestRepository.save(g);
            }
        }
    }
    // ════════════════════════════════════════════════════════
    //  CANCEL HOMESTAY BOOKING
    // ════════════════════════════════════════════════════════

    @Transactional
    public void cancelHomestayBooking(String bookingId, String memberId) {

        // ── 1. ดึง Booking ─────────────────────────────────────
        Booking booking = bookingRepository.findByIdWithDetails(bookingId)
                .orElseThrow(() -> new RuntimeException("ไม่พบการจอง: " + bookingId));

        // ── 2. ตรวจสิทธิ์ ──────────────────────────────────────
        if (!booking.getMember().getMemberid().equals(memberId))
            throw new IllegalArgumentException("ไม่มีสิทธิ์ยกเลิกการจองนี้");

        // ── 3. ตรวจสถานะ ───────────────────────────────────────
        BookingStatus status = booking.getBookingStatus();
        if (status == BookingStatus.CONFIRMED)
            throw new IllegalStateException("ไม่สามารถยกเลิกการจองที่ยืนยันแล้วได้ กรุณาติดต่อเจ้าหน้าที่");
        if (status == BookingStatus.CANCEL)
            throw new IllegalStateException("การจองนี้ถูกยกเลิกไปแล้ว");

        // ── 4. อัปเดต status เป็น CANCEL ──────────────────────
        booking.setBookingStatus(BookingStatus.CANCEL);
        bookingRepository.save(booking);
    }

    // ════════════════════════════════════════════════════════
    //  HELPERS
    // ════════════════════════════════════════════════════════

    private String generateBookingId() {
        String date  = LocalDate.now().format(DateTimeFormatter.ofPattern("MMdd"));
        long   count = bookingRepository.count() + 1;
        return "BK" + date + String.format("%04d", count);
    }

    private String generateGuestId() {
        long count = guestRepository.count() + 1;
        return "GS" + String.format("%08d", count);
    }
}