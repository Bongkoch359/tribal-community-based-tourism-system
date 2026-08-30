package com.example.miniproject.service.Member;

import com.example.miniproject.entity.*;
import com.example.miniproject.entity.enums.BookingStatus;
import com.example.miniproject.entity.enums.BookingType;
import com.example.miniproject.repository.Member.BookingroomdetailRepository;
import com.example.miniproject.repository.Member.BookingtourdetailRepository;
import com.example.miniproject.repository.Member.GuestRepository;
import com.example.miniproject.repository.Member.TourRepository;
import com.example.miniproject.repository.Tour.TourScheduleRepository;
import com.example.miniproject.repository.Homestay.RoomTypeRepository;
import com.example.miniproject.repository.Member.BookingRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Date;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.time.format.DateTimeFormatter;

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

    @Autowired
    private BookingIdGenerator bookingIdGenerator;

    @Autowired
private TourRepository tourRepository;

@Autowired
private TourScheduleRepository tourScheduleRepository;

@Autowired
private BookingtourdetailRepository bookingtourdetailRepository;

private static final double INSURANCE_PRICE_PER_PERSON = 100.0;

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
    //  VALIDATION HELPERS (ใช้ร่วมกันทั้ง create / edit)
    // ════════════════════════════════════════════════════════

    /**
     * ตรวจสอบจำนวนห้อง / ผู้ใหญ่ / เด็ก ให้เป็นค่าที่ถูกต้อง
     * แทนที่จะ silently default ค่าผิดๆ ให้กลายเป็น 1 เหมือนเดิม
     * ที่นี่จะ throw IllegalArgumentException ทันทีถ้าค่าไม่สมเหตุสมผล
     */
    private void validateGuestCounts(Integer numofrooms, Integer numofAdults, Integer numofChildren) {

        if (numofrooms == null || numofrooms < 1) {
            throw new IllegalArgumentException("จำนวนห้องต้องมีอย่างน้อย 1 ห้อง");
        }

        if (numofAdults == null || numofAdults < 1) {
            throw new IllegalArgumentException("จำนวนผู้ใหญ่ต้องมีอย่างน้อย 1 ท่าน");
        }

        if (numofChildren != null && numofChildren < 0) {
            throw new IllegalArgumentException("จำนวนเด็กต้องไม่ติดลบ");
        }
    }

    /**
     * ตรวจสอบว่าจำนวนผู้เข้าพัก (ผู้ใหญ่ + เด็ก) ไม่เกินความจุของห้อง
     * สมมติฐาน: roomtype.getMaxguest() คือความจุ "ต่อห้อง" ดังนั้นถ้าจองหลายห้อง
     * ความจุรวมจะคูณตามจำนวนห้อง (maxguest * rooms)
     * — ถ้าระบบจริงหมายถึงความจุรวมทั้งหมดไม่คูณตามห้อง ให้เอา " * rooms" ออก
     */
    private void validateCapacity(Roomtype roomtype, int rooms, int adults, int children) {
        if (roomtype.getMaxguest() == null) return; // ไม่ได้กำหนดความจุไว้ ข้ามการเช็ค

        int totalGuests = adults + children;
        int capacity = roomtype.getMaxguest() * rooms;

        if (totalGuests > capacity) {
            throw new IllegalArgumentException(
                    "จำนวนผู้เข้าพัก (" + totalGuests + " ท่าน) เกินความจุที่ห้องรองรับ (สูงสุด "
                            + capacity + " ท่าน สำหรับ " + rooms + " ห้อง)");
        }
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

        // ── 1.5 Validate จำนวนห้อง / ผู้ใหญ่ / เด็ก (ปฏิเสธค่าที่ไม่ถูกต้องแทนการ default เงียบๆ) ──
        validateGuestCounts(numofrooms, numofAdults, numofChildren);

        // ── 2. ดึง Roomtype ────────────────────────────────────
        Roomtype roomtype = roomtypeRepository.findById(roomtypeId)
                .orElseThrow(() -> new RuntimeException("ไม่พบประเภทห้องพัก: " + roomtypeId));

        int rooms    = numofrooms;
        int adults   = numofAdults;
        int children = (numofChildren != null) ? numofChildren : 0;

        // ── 2.5 เช็คความจุห้อง (ผู้ใหญ่ + เด็ก ต้องไม่เกิน maxguest * จำนวนห้อง) ──
        validateCapacity(roomtype, rooms, adults, children);

        if (roomtype.getTotalrooms() != null) {
            Integer bookedRooms = bookingroomdetailRepository.countBookedRoomsInRange(
                    roomtypeId, Date.valueOf(dateIn), Date.valueOf(dateOut));
            int availableRooms = roomtype.getTotalrooms() - (bookedRooms != null ? bookedRooms : 0);

            if (rooms > availableRooms) {
                throw new IllegalArgumentException(
                        "ห้องว่างไม่เพียงพอในช่วงวันที่เลือก (เหลือ " + Math.max(0, availableRooms) + " ห้อง)");
            }
        }

        long   nights   = ChronoUnit.DAYS.between(dateIn, dateOut);
        double subtotal = roomtype.getPricepernight() * nights * rooms;


        // ── 4. สร้าง Booking หลัก ──────────────────────────────
        Booking booking = new Booking();
        booking.setBookingid(bookingIdGenerator.generateBookingId());
        booking.setMember(member);
        booking.setBookingType(BookingType.ACCOMMODATION);
        booking.setBookingStatus(BookingStatus.PENDING);
       booking.setBookingdate(new Date(System.currentTimeMillis()));
booking.setPaymentDeadline(new java.sql.Timestamp(System.currentTimeMillis() + 30 * 60 * 1000)); // ★ เพิ่ม
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
            guest.setGuestid(bookingIdGenerator.generateGuestId());
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

        // ── 4.5 Validate จำนวนห้อง / ผู้ใหญ่ / เด็ก ─────────────
        validateGuestCounts(numofrooms, numofAdults, numofChildren);

        // ── 5. ดึง Bookingroomdetail ────────────────────────────
        if (booking.getRoomDetails() == null || booking.getRoomDetails().isEmpty())
            throw new RuntimeException("ไม่พบรายละเอียดห้องพักของการจองนี้");

        Bookingroomdetail detail   = booking.getRoomDetails().get(0);
        Roomtype          roomtype = detail.getRoomtype();

        int rooms    = numofrooms;
        int adults   = numofAdults;
        int children = (numofChildren != null) ? numofChildren : 0;

        // ── 5.5 เช็คความจุห้อง (ผู้ใหญ่ + เด็ก ต้องไม่เกิน maxguest * จำนวนห้อง) ──
        validateCapacity(roomtype, rooms, adults, children);

        // ⛔ เช็คห้องว่าง — นับรวมของ booking นี้เองด้วย (เพราะยังไม่ได้ save ค่าใหม่)
        //    ต้องหักจำนวนห้องเดิมของ booking นี้ออกก่อน ถ้าช่วงวันใหม่ยังทับกับช่วงวันเดิม
        if (roomtype.getTotalrooms() != null) {
            Integer bookedRooms = bookingroomdetailRepository.countBookedRoomsInRange(
                    roomtype.getRoomtypeid(), Date.valueOf(dateIn), Date.valueOf(dateOut));
            int totalBooked = (bookedRooms != null ? bookedRooms : 0);

            // ถ้าช่วงวันเดิมของ booking นี้ทับกับช่วงวันใหม่ที่กำลังจะเช็ค ต้องหักตัวเองออก
            // (เพราะ query countBookedRoomsInRange น่าจะนับรวม detail เดิมของ booking นี้ไปแล้ว
            //  ถ้าช่วงวันเดิม-ใหม่คาบเกี่ยวกัน)
            boolean oldOverlapsNewRange = detail.getCheckindate().toLocalDate().isBefore(dateOut)
                    && detail.getCheckoutdate().toLocalDate().isAfter(dateIn);
            if (oldOverlapsNewRange) {
                totalBooked -= detail.getNumofrooms();
            }

            int availableRooms = roomtype.getTotalrooms() - totalBooked;

            if (rooms > availableRooms) {
                throw new IllegalArgumentException(
                        "ห้องว่างไม่เพียงพอในช่วงวันที่เลือก (เหลือ " + Math.max(0, availableRooms) + " ห้อง)");
            }
        }

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
                g.setGuestid(bookingIdGenerator.generateGuestId());
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
public void cancelHomestayBooking(String bookingId, String memberId, String reason) {

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
    if (status == BookingStatus.COMPLETED)
        throw new IllegalStateException("ไม่สามารถยกเลิกการจองที่เสร็จสิ้นแล้วได้");

    // ── 4. อัปเดต status เป็น CANCEL ──────────────────────
    booking.setBookingStatus(BookingStatus.CANCEL);
    booking.setCancelReason(
        "ยกเลิกโดยผู้จอง" + (reason != null && !reason.isBlank() ? ": " + reason.trim() : "")
    );
    bookingRepository.save(booking);
}

//══════════════════════════════════════════════
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

    

public void autoCompleteIfPastEndDate(Booking booking) {
    if (booking == null || booking.getBookingStatus() != BookingStatus.CONFIRMED) {
        return; // เช็คเฉพาะ CONFIRMED เท่านั้น ไม่ยุ่งกับ status อื่น
    }

    LocalDate today = LocalDate.now();
    boolean isPastEnd = false;

    if (booking.getTourDetails() != null && !booking.getTourDetails().isEmpty()) {
        var schedule = booking.getTourDetails().get(0).getTourschedule();
        isPastEnd = schedule != null && schedule.getEnddate() != null
                && schedule.getEnddate().toLocalDate().isBefore(today);

    } else if (booking.getRoomDetails() != null && !booking.getRoomDetails().isEmpty()) {
        var checkout = booking.getRoomDetails().get(0).getCheckoutdate();
        isPastEnd = checkout != null && checkout.toLocalDate().isBefore(today);
    }

    if (isPastEnd) {
        booking.setBookingStatus(BookingStatus.COMPLETED);
        bookingRepository.save(booking);
    }
}


}