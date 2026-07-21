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

import com.example.miniproject.repository.Member.BookingtourdetailRepository;
import com.example.miniproject.repository.Member.TourRepository;
import com.example.miniproject.repository.Tour.TourScheduleRepository;

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
    private TourService tourService;

    @Autowired
    private BookingroomdetailRepository bookingroomdetailRepository;

    @Autowired
    private RoomTypeRepository roomtypeRepository;

    @Autowired
    private GuestRepository guestRepository;

    @Autowired
    private TourRepository tourRepository;

    @Autowired
    private BookingtourdetailRepository bookingtourdetailRepository;
    
    @Autowired
private TourScheduleRepository tourScheduleRepository;

    public static final double INSURANCE_PRICE_PER_PERSON = 100.0;

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


    // ════════════════════════════════════════════════════════
    //  CREATE TOUR BOOKING
    // ════════════════════════════════════════════════════════

    // ════════════════════════════════════════════════════════
    //  CREATE TOUR BOOKING
    // ════════════════════════════════════════════════════════

    @Transactional
    public String createTourBooking(
            Member member,
            String tourId,
            String tourDate,
            Integer adult,
            Integer children,
            String note,
            Boolean isBookerGoing,
            String pickuptype,
            String pickuplocation,
            Boolean wantInsurance,
            List<String> guestFirstnames,
            List<String> guestLastnames,
            List<String> guestIdcards) {

        // ── 1. Validate date ──────────────────────────────────
        LocalDate startDate = LocalDate.parse(tourDate);
        if (startDate.isBefore(LocalDate.now())) {
            throw new IllegalArgumentException("ไม่สามารถเลือกวันย้อนหลังได้");
        }

        // ── 2. ดึง Tour (ไม่ต้องล็อก แค่ใช้ราคา/max seats) ──
        Tour tour = tourRepository.findById(tourId)
                .orElseThrow(() -> new RuntimeException("ไม่พบทัวร์"));

        // ── 2.5 ดึง schedule ของวันที่เลือก (ยังไม่ล็อก) ──
        Tourschedule scheduleRef = tourScheduleRepository
                .findByTourTouridAndOpendate(tourId, java.sql.Date.valueOf(startDate))
                .orElseThrow(() -> new IllegalArgumentException(
                        "ไม่พบรอบทัวร์ในวันที่เลือก กรุณาเลือกวันที่ที่เปิดรับจอง"));

        // 🔒 ล็อกแถว schedule นี้ไว้ทันที — คนอื่นที่จองรอบเดียวกันพร้อมกัน
        // จะต้องรอคิว จนกว่า transaction นี้ commit/rollback ก่อน
        // ถึงจะอ่านจำนวนที่นั่งที่อัปเดตแล้วได้ (กันที่นั่งเกิน)
        Tourschedule schedule = tourScheduleRepository
                .findByIdForUpdate(scheduleRef.getScheduleid())
                .orElseThrow(() -> new IllegalArgumentException("ไม่พบรอบทัวร์"));

        if (!"เปิดรับจอง".equals(schedule.getStatus())) {
            throw new IllegalArgumentException("รอบทัวร์วันที่เลือกไม่เปิดรับจองแล้ว");
        }

        // ── 3. คำนวณจำนวนคน ─────────────────────────────────
        int adults = (adult != null && adult > 0) ? adult : 1;
        int childs = (children != null) ? children : 0;
        int totalGuest = adults + childs;

        // ── 4. ตรวจที่นั่ง เฉพาะรอบนี้ (ไม่ใช่รวมทั้ง tour) ──
        if (tour.getMaxSeatstour() != null) {
            int bookedInSchedule = tourScheduleRepository
                    .countBookedSeatsBySchedule(schedule.getScheduleid());
            int availableSeats = tour.getMaxSeatstour() - bookedInSchedule;

            if (totalGuest > availableSeats) {
                throw new IllegalArgumentException(
                        "ที่นั่งคงเหลือไม่เพียงพอ (เหลือ " + Math.max(0, availableSeats) + " ที่นั่ง)");
            }
        }

        // ── 4.5 Validate เลขบัตรประชาชน ถ้าต้องการทำประกัน ──
        // หมายเหตุ: ถ้าผู้จองไปเองด้วย (isBookerGoing == true) ต้องกรอกเลขบัตรของ
        // ผู้จองเองมาเป็นตัวแรกใน guestIdcards ด้วย (ฝั่ง HTML วางช่องผู้จองไว้ก่อนแขกคนอื่นเสมอ)
        // ดังนั้นจำนวนเลขบัตรที่ต้องมี = totalGuest เท่ากันไม่ว่าจะไปเองหรือจองให้คนอื่น
        boolean insurance = true;
        if (insurance) {
            if (guestIdcards == null || guestIdcards.size() < totalGuest) {
                throw new IllegalArgumentException("กรุณากรอกเลขบัตรประชาชนให้ครบทุกท่านเพื่อทำประกัน");
            }
            for (String idcard : guestIdcards) {
                if (idcard == null || idcard.trim().length() != 13) {
                    throw new IllegalArgumentException("เลขบัตรประชาชนต้องมี 13 หลัก กรุณากรอกให้ครบทุกท่าน");
                }
            }
        }

        // ── 4.6 Validate จุดรับ (กรณีให้ทัวร์ไปรับที่โรงแรม ต้องอยู่ในเชียงใหม่เท่านั้น) ──
        if ("โรงแรม/ที่พัก".equals(pickuptype)) {
            if (pickuplocation == null || pickuplocation.trim().isEmpty()) {
                throw new IllegalArgumentException("กรุณาระบุชื่อโรงแรม/ที่พักสำหรับรับ");
            }
            if (!pickuplocation.contains("เชียงใหม่")) {
                throw new IllegalArgumentException("บริการรับที่พักรองรับเฉพาะในเขตจังหวัดเชียงใหม่เท่านั้น");
            }
        }

        // ── 5. คำนวณราคา ────────────────────────────────────
        double tourSubtotal = (adults * tour.getAdultprice()) + (childs * tour.getChildprice());

        double insuranceFeePerPerson = insurance ? INSURANCE_PRICE_PER_PERSON : 0.0;
        double subtotalInsurance = insurance ? (insuranceFeePerPerson * totalGuest) : 0.0;

        double grandTotal = tourSubtotal + subtotalInsurance;

        // ── 6. สร้าง Booking ─────────────────────────────────
        Booking booking = new Booking();
        booking.setBookingid(generateBookingId());
        booking.setMember(member);
        booking.setBookingType(BookingType.TOUR);
        booking.setBookingStatus(BookingStatus.PENDING);
        booking.setBookingdate(new Date(System.currentTimeMillis()));
        booking.setNumofguest(totalGuest);
        booking.setNote(note);
        booking.setIsBookerGoing(isBookerGoing != null ? isBookerGoing : true);
        booking.setTotalamount(grandTotal);
        booking.setPickuptype(pickuptype);
        booking.setPickuplocation(pickuplocation);
        booking.setWantInsurance(insurance);
        booking.setInsuranceFeePerPerson(insuranceFeePerPerson);
        booking.setSubtotalInsurance(subtotalInsurance);
        bookingRepository.save(booking);

        // ── 7. สร้าง Bookingtourdetail ──────────────────────
        Bookingtourdetailid detailId = new Bookingtourdetailid();
        detailId.setBookingid(booking.getBookingid());
        detailId.settourid(tour.getTourid());

        Bookingtourdetail detail = new Bookingtourdetail();
        detail.setId(detailId);
        detail.setBooking(booking);
        detail.setTour(tour);
        detail.setTourschedule(schedule);

        detail.setNumofadult(adults);
        detail.setNumofchild(childs);
        detail.setSubtotaltour(tourSubtotal);   // ค่าทัวร์ล้วนๆ ไม่รวมประกัน

        bookingtourdetailRepository.save(detail);

        // ── 8. Guest ─────────────────────────────────────────
        // ถ้าผู้จองไปเองและทำประกัน → ต้องเก็บเลขบัตรของผู้จองไว้ด้วย
        // เนื่องจากไม่มีที่เก็บเลขบัตรใน Member/Booking เราจึงสร้าง Guest
        // record แทนตัวผู้จองขึ้นมาเก็บชื่อ-นามสกุล-เลขบัตรของผู้จองเอง
        // (ฝั่ง HTML ส่ง guestIdcard ตัวแรกมาเป็นของผู้จองเสมอ เมื่อ isBookerGoing = true)
        int idcardOffset = 0;

        if (Boolean.TRUE.equals(isBookerGoing) && insurance
                && guestIdcards != null && !guestIdcards.isEmpty()) {

            Guest bookerGuest = new Guest();
            bookerGuest.setGuestid(generateGuestId());
            bookerGuest.setFirstname(member.getFirstname());
            bookerGuest.setLastname(member.getLastname());
            bookerGuest.setIdcardnumber(guestIdcards.get(0).trim());
            bookerGuest.setBooking(booking);
            guestRepository.save(bookerGuest);

            idcardOffset = 1; // เลขบัตรตัวถัดไปเป็นของแขกคนอื่น (ไม่ใช่ผู้จอง)
        }

        if (guestFirstnames != null && !guestFirstnames.isEmpty()) {
            for (int i = 0; i < guestFirstnames.size(); i++) {
                String fname = guestFirstnames.get(i);
                if (fname == null || fname.isBlank()) continue;

                String lname = (guestLastnames != null && i < guestLastnames.size())
                        ? guestLastnames.get(i) : "";

                Guest guest = new Guest();
                guest.setGuestid(generateGuestId());
                guest.setFirstname(fname.trim());
                guest.setLastname(lname.trim());

                // เพิ่ม idcard ถ้ามีการทำประกัน (offset เลื่อนเลขบัตรของผู้จองออกไปแล้ว)
                int idcardIndex = i + idcardOffset;
                if (insurance && guestIdcards != null && idcardIndex < guestIdcards.size()) {
                    guest.setIdcardnumber(guestIdcards.get(idcardIndex).trim());
                }

                guest.setBooking(booking);
                guestRepository.save(guest);
            }
        }

        return booking.getBookingid();
    }

// ════════════════════════════════════════════════════════
    //  EDIT TOUR BOOKING
    // ════════════════════════════════════════════════════════

    @Transactional
    public void editTourBooking(
            String bookingId,
            String memberId,
            String tourDate,
            Integer adult,
            Integer children,
            String note,
            String pickuptype,        // ➕ เพิ่ม
            String pickuplocation,
            String guestFirstname,
            String guestLastname) {

        // ── 1. ดึง Booking ──────────────────────────────────
        Booking booking = bookingRepository
                .findByIdWithDetails(bookingId)
                .orElseThrow(() ->
                        new RuntimeException("ไม่พบการจอง"));

        // ── 2. ตรวจสิทธิ์ ───────────────────────────────────
        if (!booking.getMember().getMemberid()
                .equals(memberId)) {

            throw new IllegalArgumentException(
                    "ไม่มีสิทธิ์แก้ไขการจองนี้");
        }

        // ── 3. ตรวจสถานะ ───────────────────────────────────
        BookingStatus status = booking.getBookingStatus();

        if (status != BookingStatus.PENDING
                && status != BookingStatus.WAITING_APPROVAL) {

            throw new IllegalStateException(
                    "ไม่สามารถแก้ไขการจองได้");
        }

        // ── 4. ดึง Tour Detail ─────────────────────────────
        if (booking.getTourDetails() == null
                || booking.getTourDetails().isEmpty()) {

            throw new RuntimeException(
                    "ไม่พบรายละเอียดทัวร์");
        }

        // ── 4.6 Validate จุดรับ (เหมือนตอน create) ──
    if ("โรงแรม/ที่พัก".equals(pickuptype)) {
        if (pickuplocation == null || pickuplocation.trim().isEmpty()) {
            throw new IllegalArgumentException("กรุณาระบุชื่อโรงแรม/ที่พักสำหรับรับ");
        }
        if (!pickuplocation.contains("เชียงใหม่")) {
            throw new IllegalArgumentException("บริการรับที่พักรองรับเฉพาะในเขตจังหวัดเชียงใหม่เท่านั้น");
        }
    }

        Bookingtourdetail detail =
                booking.getTourDetails().get(0);

        Tour tour = detail.getTour();

        // ── 5. Validate date ───────────────────────────────
        LocalDate startDate = LocalDate.parse(tourDate);

        if (startDate.isBefore(LocalDate.now())) {
            throw new IllegalArgumentException(
                    "ไม่สามารถเลือกวันย้อนหลังได้");
        }

        // ── 5.5 ดึง/ล็อกรอบทัวร์ของวันที่ใหม่ ─────────────
        // ถ้า user เปลี่ยนวันเดินทาง → ต้องหารอบ (schedule) ใหม่
        // ถ้าวันเดิม → ยังต้องล็อกรอบเดิมไว้ เพราะจำนวนคนอาจเปลี่ยน
        Tourschedule oldSchedule = detail.getTourschedule();
        boolean isChangingDate = oldSchedule == null
                || !oldSchedule.getOpendate().toLocalDate().equals(startDate);

        Tourschedule newSchedule;
        if (isChangingDate) {
            Tourschedule newScheduleRef = tourScheduleRepository
                    .findByTourTouridAndOpendate(tour.getTourid(), java.sql.Date.valueOf(startDate))
                    .orElseThrow(() -> new IllegalArgumentException(
                            "ไม่พบรอบทัวร์ในวันที่เลือก กรุณาเลือกวันที่ที่เปิดรับจอง"));

            // 🔒 ล็อกรอบใหม่ไว้ ก่อนเช็คที่นั่ง กันคนอื่นแย่งที่พร้อมกัน
            newSchedule = tourScheduleRepository
                    .findByIdForUpdate(newScheduleRef.getScheduleid())
                    .orElseThrow(() -> new IllegalArgumentException("ไม่พบรอบทัวร์"));

            if (!"เปิดรับจอง".equals(newSchedule.getStatus())) {
                throw new IllegalArgumentException("รอบทัวร์วันที่เลือกไม่เปิดรับจองแล้ว");
            }
        } else {
            // วันเดิม ไม่ได้เปลี่ยนรอบ — ยังล็อกไว้เพราะจำนวนคนอาจเปลี่ยน
            newSchedule = tourScheduleRepository
                    .findByIdForUpdate(oldSchedule.getScheduleid())
                    .orElseThrow(() -> new IllegalArgumentException("ไม่พบรอบทัวร์"));
        }

        // ── 6. คำนวณใหม่ ──────────────────────────────────
        int adults =
                (adult != null && adult > 0) ? adult : 1;

        int childs =
                (children != null) ? children : 0;

        int totalGuest = adults + childs;

        // ── 6.2 เช็คที่นั่งของรอบใหม่ ───────────────────────
        // ถ้าเปลี่ยนวัน: เช็คที่นั่งว่างของรอบใหม่ตรงๆ (ยังไม่มีคนของ booking นี้อยู่ในรอบนั้น)
        // ถ้าไม่เปลี่ยนวัน: booking นี้นับรวมอยู่ใน bookedInSchedule แล้ว ต้องหักจำนวนเดิมออกก่อน
        //                   ถึงจะได้ที่นั่งว่างที่แท้จริงสำหรับเทียบกับจำนวนคนใหม่
        if (tour.getMaxSeatstour() != null) {
            int bookedInSchedule = tourScheduleRepository
                    .countBookedSeatsBySchedule(newSchedule.getScheduleid());

            int currentGuestInThisBooking = isChangingDate ? 0 : booking.getNumofguest();
            int availableSeats = tour.getMaxSeatstour() - (bookedInSchedule - currentGuestInThisBooking);

            if (totalGuest > availableSeats) {
                throw new IllegalArgumentException(
                        "ที่นั่งคงเหลือไม่เพียงพอ (เหลือ " + Math.max(0, availableSeats) + " ที่นั่ง)");
            }
        }

        double subtotal =
                (adults * tour.getAdultprice())
                        + (childs * tour.getChildprice());

        // ── 6.5 ยอดประกันเดิม (ถ้ามี) — คงค่าตามที่จองไว้ตอนแรก
        //        แล้วปรับสัดส่วนตามจำนวนคนใหม่ ถ้าเคยติ๊กประกันไว้
        double subtotalInsurance = 0.0;
        if (Boolean.TRUE.equals(booking.getWantInsurance())
                && booking.getInsuranceFeePerPerson() != null) {
            subtotalInsurance = booking.getInsuranceFeePerPerson() * totalGuest;
        }
        double grandTotal = subtotal + subtotalInsurance;

        // ── 7. อัปเดต detail ──────────────────────────────
        detail.setTourschedule(newSchedule);   // ⬅️ ผูกกับรอบใหม่ (สำคัญ — เดิมไม่เคยอัปเดต)
        detail.setNumofadult(adults);
        detail.setNumofchild(childs);
        detail.setSubtotaltour(subtotal);

        bookingtourdetailRepository.save(detail);

        // ── 8. อัปเดต booking ─────────────────────────────
        booking.setNumofguest(totalGuest);
        booking.setNote(note);
        booking.setTotalamount(grandTotal);
        booking.setPickuptype(pickuptype);          // ➕ เพิ่ม
        booking.setPickuplocation(pickuplocation);
        booking.setSubtotalInsurance(subtotalInsurance);

        bookingRepository.save(booking);

        // ── 9. Guest ──────────────────────────────────────
        if (Boolean.FALSE.equals(booking.getIsBookerGoing())
                && guestFirstname != null
                && !guestFirstname.isBlank()) {

            Set<Guest> guests = booking.getGuests();

            if (guests != null && !guests.isEmpty()) {

                Guest g = guests.iterator().next();

                g.setFirstname(guestFirstname.trim());

                g.setLastname(
                        guestLastname != null
                                ? guestLastname.trim()
                                : "");

                guestRepository.save(g);

            } else {

                Guest g = new Guest();

                g.setGuestid(generateGuestId());

                g.setFirstname(guestFirstname.trim());

                g.setLastname(
                        guestLastname != null
                                ? guestLastname.trim()
                                : "");

                g.setBooking(booking);

                guestRepository.save(g);
            }
        }
    }
    // ════════════════════════════════════════════════════════
    //  CANCEL TOUR BOOKING
    // ════════════════════════════════════════════════════════

    @Transactional
    public void cancelTourBooking(
            String bookingId,
            String memberId) {

        Booking booking = bookingRepository
                .findByIdWithDetails(bookingId)
                .orElseThrow(() ->
                        new RuntimeException("ไม่พบการจอง"));

        // ตรวจสิทธิ์
        if (!booking.getMember().getMemberid()
                .equals(memberId)) {

            throw new IllegalArgumentException(
                    "ไม่มีสิทธิ์ยกเลิกการจองนี้");
        }

        // ตรวจสถานะ
        BookingStatus status =
                booking.getBookingStatus();

        if (status == BookingStatus.CONFIRMED) {

            throw new IllegalStateException(
                    "ไม่สามารถยกเลิกการจองที่ยืนยันแล้วได้");
        }

        if (status == BookingStatus.CANCEL) {

            throw new IllegalStateException(
                    "การจองนี้ถูกยกเลิกไปแล้ว");
        }

        booking.setBookingStatus(BookingStatus.CANCEL);

        bookingRepository.save(booking);
    }

}