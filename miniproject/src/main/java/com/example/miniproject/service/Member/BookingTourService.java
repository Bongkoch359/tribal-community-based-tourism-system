package com.example.miniproject.service.Member;

import com.example.miniproject.entity.*;
import com.example.miniproject.entity.enums.BookingStatus;
import com.example.miniproject.entity.enums.BookingType;
import com.example.miniproject.repository.Member.BookingRepository;
import com.example.miniproject.repository.Member.BookingtourdetailRepository;
import com.example.miniproject.repository.Member.GuestRepository;
import com.example.miniproject.repository.Member.PaymentRepository;
import com.example.miniproject.repository.Member.TourRepository;
import com.example.miniproject.repository.Tour.TourScheduleRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Date;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

// ─── การจองทัวร์ทั้งหมด แยกออกมาจาก BookingService (ซึ่งเดิมรวมทั้งโฮมสเตย์และทัวร์ไว้ด้วยกัน) ───
@Service
public class BookingTourService {

    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private TourRepository tourRepository;

    @Autowired
    private BookingtourdetailRepository bookingtourdetailRepository;

    @Autowired
    private TourScheduleRepository tourScheduleRepository;

    @Autowired
    private GuestRepository guestRepository;

    @Autowired
    private PaymentRepository paymentRepository;

    @Autowired
    private BookingIdGenerator bookingIdGenerator;

    public static final double INSURANCE_PRICE_PER_PERSON = 100.0;

    // ════════════════════════════════════════════════════════
    // LIST TOUR BOOKINGS FOR COMMUNITY MANAGER
    // ════════════════════════════════════════════════════════

    /**
     * ดึงรายการจองทัวร์ทั้งหมดของ manager คนนั้น กรองตามสถานะได้ (status = null →
     * เอาทุกสถานะ)
     */
    public List<Booking> getTourBookingsByManager(String managerId, BookingStatus status) {
        if (status == null) {
            return bookingRepository.findTourBookingsByManagerId(managerId);
        }
        return bookingRepository.findTourBookingsByManagerIdAndStatus(managerId, status);
    }

    
    @Transactional(readOnly = true)
    public Booking getTourBookingDetailForManager(String bookingId, String managerId) {
        Booking booking = bookingRepository
                .findTourBookingDetailForManager(bookingId, managerId)
                .orElseThrow(() -> new RuntimeException("ไม่พบการจอง หรือไม่มีสิทธิ์เข้าถึงการจองนี้"));

        List<Guest> guests = guestRepository.findByBooking_BookingidOrderByGuestidAsc(bookingId);
        booking.setGuests(new LinkedHashSet<>(guests));

        Payment payment = paymentRepository.findByBooking_Bookingid(bookingId);
        booking.setPayment(payment);

        return booking;
    }

    /**
     * ยืนยันการจองทัวร์ (manager) — ทำได้เฉพาะการจองที่อยู่ในสถานะ "รอตรวจสอบ"
     */
    @Transactional
    public void confirmTourBookingByManager(String bookingId, String managerId) {
        Booking booking = bookingRepository
                .findTourBookingDetailForManager(bookingId, managerId)
                .orElseThrow(() -> new RuntimeException("ไม่พบการจอง หรือไม่มีสิทธิ์เข้าถึงการจองนี้"));

        if (booking.getBookingStatus() != BookingStatus.WAITING_APPROVAL) {
            throw new IllegalStateException("การจองนี้ไม่อยู่ในสถานะที่สามารถยืนยันได้");
        }

        booking.setBookingStatus(BookingStatus.CONFIRMED);
        bookingRepository.save(booking);
    }

    /**
     * ยกเลิกการจองทัวร์ (manager) — ยกเลิกซ้ำ, ยกเลิกรายการที่ยืนยันแล้ว
     * หรือยกเลิกรายการที่เสร็จสิ้นแล้วไม่ได้
     */
    @Transactional
    public void cancelTourBookingByManager(String bookingId, String managerId, String reason) {
        if (reason == null || reason.isBlank()) {
            throw new IllegalArgumentException("กรุณาระบุเหตุผลในการยกเลิก");
        }

        Booking booking = bookingRepository
                .findTourBookingDetailForManager(bookingId, managerId)
                .orElseThrow(() -> new RuntimeException("ไม่พบการจอง หรือไม่มีสิทธิ์เข้าถึงการจองนี้"));

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
        booking.setCancelReason("ยกเลิกโดยผู้ดูแลชุมชน: " + reason.trim());
        bookingRepository.save(booking);
    }

  
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

       
        LocalDate startDate = LocalDate.parse(tourDate);
        if (startDate.isBefore(LocalDate.now())) {
            throw new IllegalArgumentException("ไม่สามารถเลือกวันย้อนหลังได้");
        }

        Tour tour = tourRepository.findById(tourId)
                .orElseThrow(() -> new RuntimeException("ไม่พบทัวร์"));

        
        Tourschedule schedule = tourScheduleRepository
                .findByTourTouridAndOpendate(tourId, java.sql.Date.valueOf(startDate))
                .orElseThrow(() -> new IllegalArgumentException(
                        "ไม่พบรอบทัวร์ในวันที่เลือก กรุณาเลือกวันที่ที่เปิดรับจอง"));

        // lock แถว schedule นี้ไว้ก่อนเช็คที่นั่ง กันจองซ้อนตอนมีคนกดพร้อมกัน
        schedule = tourScheduleRepository.lockScheduleForUpdate(schedule.getScheduleid());

        if (!"เปิดรับจอง".equals(schedule.getStatus())) {
            throw new IllegalArgumentException("รอบทัวร์วันที่เลือกไม่เปิดรับจองแล้ว");
        }

        //  คำนวณจำนวนคน 
        int adults = (adult != null && adult > 0) ? adult : 1;
        int childs = (children != null) ? children : 0;
        if (childs < 0) {
            throw new IllegalArgumentException("จำนวนเด็กต้องไม่ติดลบ");
        }
        int totalGuest = adults + childs;

        // ตรวจที่นั่ง เฉพาะรอบนี้ (ไม่ใช่รวมทั้ง tour) 
        if (tour.getMaxSeatstour() != null) {
            int bookedInSchedule = tourScheduleRepository
                    .countBookedSeatsBySchedule(schedule.getScheduleid());
            int availableSeats = tour.getMaxSeatstour() - bookedInSchedule;

            if (totalGuest > availableSeats) {
                throw new IllegalArgumentException(
                        "ที่นั่งคงเหลือไม่เพียงพอ (เหลือ " + Math.max(0, availableSeats) + " ที่นั่ง)");
            }
        }

       
        // ถ้าผู้จองไปเองด้วย (isBookerGoing == true) เลขบัตรของผู้จองต้องเป็นตัวแรกใน
        // guestIdcards (ฝั่ง HTML วางช่องผู้จองไว้ก่อนแขกคนอื่นเสมอ)
        // ดังนั้นจำนวนเลขบัตรที่ต้องมี = totalGuest ไม่ว่าจะไปเองหรือจองให้คนอื่น
        boolean insurance = true;
        if (guestIdcards == null || guestIdcards.size() < totalGuest) {
            throw new IllegalArgumentException("กรุณากรอกเลขบัตรประชาชนให้ครบทุกท่านเพื่อทำประกัน");
        }
        for (String idcard : guestIdcards) {
            if (idcard == null || idcard.trim().length() != 13) {
                throw new IllegalArgumentException("เลขบัตรประชาชนต้องมี 13 หลัก กรุณากรอกให้ครบทุกท่าน");
            }
        }

        // จุดรับ (โรงแรม/ที่พัก ต้องอยู่ในเชียงใหม่เท่านั้น) 
        if ("โรงแรม/ที่พัก".equals(pickuptype)) {
            if (pickuplocation == null || pickuplocation.trim().isEmpty()) {
                throw new IllegalArgumentException("กรุณาระบุชื่อโรงแรม/ที่พักสำหรับรับ");
            }
            if (!pickuplocation.contains("เชียงใหม่")) {
                throw new IllegalArgumentException("บริการรับที่พักรองรับเฉพาะในเขตจังหวัดเชียงใหม่เท่านั้น");
            }
        }

        //  คำนวณราคา
        double tourSubtotal = (adults * tour.getAdultprice()) + (childs * tour.getChildprice());

        double insuranceFeePerPerson = INSURANCE_PRICE_PER_PERSON;
        double subtotalInsurance = insuranceFeePerPerson * totalGuest;

        double grandTotal = tourSubtotal + subtotalInsurance;

        //  สร้าง Booking 
        Booking booking = new Booking();
        booking.setBookingid(bookingIdGenerator.generateBookingId());
        booking.setMember(member);
        booking.setBookingType(BookingType.TOUR);
        booking.setBookingStatus(BookingStatus.PENDING);
        booking.setBookingdate(new Date(System.currentTimeMillis()));
        booking.setPaymentDeadline(new java.sql.Timestamp(System.currentTimeMillis() + 30 * 60 * 1000)); // deadline =
                                                                                                         // ตอนนี้ + 30
                                                                                                         // นาที
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

        //  สร้าง Bookingtourdetail 
        Bookingtourdetailid detailId = new Bookingtourdetailid();
        detailId.setBookingid(booking.getBookingid());
        detailId.setScheduleid(schedule.getScheduleid());

        Bookingtourdetail detail = new Bookingtourdetail();
        detail.setId(detailId);
        detail.setBooking(booking);
        detail.setTourschedule(schedule);

        detail.setNumofadult(adults);
        detail.setNumofchild(childs);
        detail.setSubtotaltour(tourSubtotal); // ค่าทัวร์ล้วนๆ ไม่รวมประกัน

        bookingtourdetailRepository.save(detail);

        
        // ถ้าผู้จองไปเอง → สร้าง Guest แทนตัวผู้จองเพื่อเก็บชื่อ-นามสกุล-เลขบัตร
        // (ฝั่ง HTML ส่ง guestIdcard ตัวแรกมาเป็นของผู้จองเสมอ เมื่อ isBookerGoing =
        // true)
        int idcardOffset = 0;

        if (Boolean.TRUE.equals(isBookerGoing) && !guestIdcards.isEmpty()) {

            Guest bookerGuest = new Guest();
            bookerGuest.setGuestid(bookingIdGenerator.generateGuestId());
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
                if (fname == null || fname.isBlank())
                    continue;

                String lname = (guestLastnames != null && i < guestLastnames.size())
                        ? guestLastnames.get(i)
                        : "";

                Guest guest = new Guest();
                guest.setGuestid(bookingIdGenerator.generateGuestId());
                guest.setFirstname(fname.trim());
                guest.setLastname(lname.trim());

                // offset เลื่อนเลขบัตรของผู้จองออกไปแล้ว
                int idcardIndex = i + idcardOffset;
                if (guestIdcards != null && idcardIndex < guestIdcards.size()) {
                    guest.setIdcardnumber(guestIdcards.get(idcardIndex).trim());
                }

                guest.setBooking(booking);
                guestRepository.save(guest);
            }
        }

        return booking.getBookingid();
    }

   
    @Transactional
    public void editTourBooking(
            String bookingId,
            String memberId,
            String tourDate,
            Integer adult,
            Integer children,
            String note,
            String pickuptype,
            String pickuplocation,
            List<String> guestIds,
            List<String> guestFirstnames,
            List<String> guestLastnames,
            List<String> guestIdcards) {

        // ── 1. ดึง Booking
        Booking booking = bookingRepository
                .findByIdWithDetails(bookingId)
                .orElseThrow(() -> new RuntimeException("ไม่พบการจอง"));

        // ── 2. ตรวจสิทธิ์
        if (!booking.getMember().getMemberid().equals(memberId)) {
            throw new IllegalArgumentException("ไม่มีสิทธิ์แก้ไขการจองนี้");
        }

        // ── 3. ตรวจสถานะ 
        BookingStatus status = booking.getBookingStatus();

        if (status != BookingStatus.PENDING
                && status != BookingStatus.WAITING_APPROVAL) {
            throw new IllegalStateException("ไม่สามารถแก้ไขการจองได้");
        }

        // ── 4. ดึง Tour Detail 
        if (booking.getTourDetails() == null
                || booking.getTourDetails().isEmpty()) {
            throw new RuntimeException("ไม่พบรายละเอียดทัวร์");
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

        Bookingtourdetail detail = booking.getTourDetails().get(0);

        Tour tour = detail.getTour();

        // ── 5. Validate date ───────────────────────────────
        LocalDate startDate = LocalDate.parse(tourDate);

        if (startDate.isBefore(LocalDate.now())) {
            throw new IllegalArgumentException("ไม่สามารถเลือกวันย้อนหลังได้");
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

            newSchedule = tourScheduleRepository.lockScheduleForUpdate(newScheduleRef.getScheduleid());

            if (!"เปิดรับจอง".equals(newSchedule.getStatus())) {
                throw new IllegalArgumentException("รอบทัวร์วันที่เลือกไม่เปิดรับจองแล้ว");
            }
        } else {
            newSchedule = tourScheduleRepository.lockScheduleForUpdate(oldSchedule.getScheduleid());
        }

        // ── 6. คำนวณใหม่ ──────────────────────────────────
        int adults = (adult != null && adult > 0) ? adult : 1;

        int childs = (children != null) ? children : 0;
        if (childs < 0) {
            throw new IllegalArgumentException("จำนวนเด็กต้องไม่ติดลบ");
        }

        int totalGuest = adults + childs;

        // ── 6.2 เช็คที่นั่งของรอบใหม่ ───────────────────────
        // ถ้าเปลี่ยนวัน: เช็คที่นั่งว่างของรอบใหม่ตรงๆ (ยังไม่มีคนของ booking
        // นี้อยู่ในรอบนั้น)
        // ถ้าไม่เปลี่ยนวัน: booking นี้นับรวมอยู่ใน bookedInSchedule แล้ว
        // ต้องหักจำนวนเดิมออกก่อน
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

        double subtotal = (adults * tour.getAdultprice())
                + (childs * tour.getChildprice());

        // ── 6.5 ยอดประกัน — ใช้ราคาต่อคนที่บันทึกไว้ตอนจอง คูณจำนวนคนใหม่ ──
        double subtotalInsurance = 0.0;
        if (Boolean.TRUE.equals(booking.getWantInsurance())
                && booking.getInsuranceFeePerPerson() != null) {
            subtotalInsurance = booking.getInsuranceFeePerPerson() * totalGuest;
        }
        double grandTotal = subtotal + subtotalInsurance;

        // ── 7. อัปเดต detail ──────────────────────────────
        // scheduleid เป็นส่วนหนึ่งของ primary key (bookingid + scheduleid) ของ
        // Bookingtourdetail
        // เปลี่ยนรอบของแถวที่ persist แล้วด้วย set + save ไม่ได้
        // ถ้าเปลี่ยนวันต้องลบแถวเดิม
        // แล้วสร้างแถวใหม่ด้วย id ชุดใหม่
        if (isChangingDate) {
            booking.getTourDetails().remove(detail);
            bookingtourdetailRepository.delete(detail);
            bookingtourdetailRepository.flush();

            Bookingtourdetailid newDetailId = new Bookingtourdetailid();
            newDetailId.setBookingid(booking.getBookingid());
            newDetailId.setScheduleid(newSchedule.getScheduleid());

            Bookingtourdetail newDetail = new Bookingtourdetail();
            newDetail.setId(newDetailId);
            newDetail.setBooking(booking);
            newDetail.setTourschedule(newSchedule);
            newDetail.setNumofadult(adults);
            newDetail.setNumofchild(childs);
            newDetail.setSubtotaltour(subtotal);

            Bookingtourdetail saved = bookingtourdetailRepository.save(newDetail);
            booking.getTourDetails().add(saved);
        } else {
            detail.setNumofadult(adults);
            detail.setNumofchild(childs);
            detail.setSubtotaltour(subtotal);
            bookingtourdetailRepository.save(detail);
        }

        // ── 8. อัปเดต booking ─────────────────────────────
        booking.setNumofguest(totalGuest);
        booking.setNote(note);
        booking.setTotalamount(grandTotal);
        booking.setPickuptype(pickuptype);
        booking.setPickuplocation(pickuplocation);
        booking.setSubtotalInsurance(subtotalInsurance);

        bookingRepository.save(booking);

        // ── 9. Guest — ซิงก์รายชื่อให้ตรงกับที่ส่งมา (แก้ / เพิ่ม / ลบ) ──
        syncGuests(booking, totalGuest, guestIds, guestFirstnames, guestLastnames, guestIdcards);
    }

    
    // SYNC GUESTS (ใช้ตอนแก้ไขการจอง)
    private void syncGuests(
            Booking booking,
            int totalGuest,
            List<String> guestIds,
            List<String> guestFirstnames,
            List<String> guestLastnames,
            List<String> guestIdcards) {

        List<String> ids = guestIds != null ? guestIds : List.of();
        List<String> fns = guestFirstnames != null ? guestFirstnames : List.of();
        List<String> lns = guestLastnames != null ? guestLastnames : List.of();
        List<String> cards = guestIdcards != null ? guestIdcards : List.of();

        // list ทั้ง 4 ต้องยาวเท่ากัน และเท่ากับจำนวนผู้เดินทางรวม
        int n = ids.size();
        if (fns.size() != n || lns.size() != n || cards.size() != n) {
            throw new IllegalArgumentException("ข้อมูลผู้เดินทางไม่ครบ กรุณาลองใหม่อีกครั้ง");
        }
        if (n != totalGuest) {
            throw new IllegalArgumentException("จำนวนรายชื่อผู้เดินทางไม่ตรงกับจำนวนผู้เดินทาง");
        }

        // guest ปัจจุบันของ booking นี้ ดึงตรงจาก repository ไม่พึ่ง lazy collection
        List<Guest> current = guestRepository
                .findByBooking_BookingidOrderByGuestidAsc(booking.getBookingid());
        Map<String, Guest> currentById = new HashMap<>();
        for (Guest g : current) {
            currentById.put(g.getGuestid(), g);
        }

        // หา guest ที่เป็นตัวผู้จอง จาก flag ก่อน
        String bookerGuestId = null;
        for (Guest g : current) {
            if (g.isBooker()) {
                bookerGuestId = g.getGuestid();
                break;
            }
        }

        Set<String> keepIds = new HashSet<>();

        for (int i = 0; i < n; i++) {
            String gid = ids.get(i) == null ? "" : ids.get(i).trim();
            String fname = fns.get(i) == null ? "" : fns.get(i).trim();
            String lname = lns.get(i) == null ? "" : lns.get(i).trim();
            String card = cards.get(i) == null ? "" : cards.get(i).trim();

            if (!card.matches("\\d{13}")) {
                throw new IllegalArgumentException("เลขบัตรประชาชนต้องเป็นตัวเลข 13 หลัก กรุณากรอกให้ครบทุกท่าน");
            }

            Guest g;
            if (gid.isEmpty()) {

                g = new Guest();
                g.setGuestid(bookingIdGenerator.generateGuestId());
                g.setBooking(booking);
            } else {
                g = currentById.get(gid);
                if (g == null) {
                    throw new IllegalArgumentException("ไม่พบผู้เดินทางในการจองนี้");
                }
                if (!keepIds.add(gid)) {
                    throw new IllegalArgumentException("ข้อมูลผู้เดินทางซ้ำกัน");
                }
            }

            // ตัวผู้จอง: ไม่แก้ชื่อจากหน้านี้ (แก้ที่โปรไฟล์) แก้ได้แค่เลขบัตร
            boolean isBookerRow = gid.equals(bookerGuestId);
            if (!isBookerRow) {
                if (fname.isEmpty() || lname.isEmpty()) {
                    throw new IllegalArgumentException("กรุณากรอกชื่อและนามสกุลผู้เดินทางให้ครบ");
                }
                g.setFirstname(fname);
                g.setLastname(lname);
            }
            g.setIdcardnumber(card);

            guestRepository.save(g);
        }

        if (bookerGuestId != null && !keepIds.contains(bookerGuestId)) {
            throw new IllegalArgumentException("ไม่สามารถลบผู้จองออกจากรายชื่อผู้เดินทางได้");
        }

        List<Guest> toRemove = new ArrayList<>();
        Set<String> removeIds = new HashSet<>();
        for (Guest g : current) {
            if (!keepIds.contains(g.getGuestid())) {
                toRemove.add(g);
                removeIds.add(g.getGuestid());
            }
        }
        if (!toRemove.isEmpty()) {

            if (booking.getGuests() != null) {
                booking.getGuests().removeIf(g -> removeIds.contains(g.getGuestid()));
            }
            guestRepository.deleteAll(toRemove);
        }
    }

    @Transactional
    public void cancelTourBooking(String bookingId, String memberId, String reason) {

        Booking booking = bookingRepository
                .findByIdWithDetails(bookingId)
                .orElseThrow(() -> new RuntimeException("ไม่พบการจอง"));

        // ตรวจสิทธิ์
        if (!booking.getMember().getMemberid().equals(memberId)) {
            throw new IllegalArgumentException("ไม่มีสิทธิ์ยกเลิกการจองนี้");
        }

        // ตรวจสถานะ
        BookingStatus status = booking.getBookingStatus();

        if (status == BookingStatus.CONFIRMED) {
            throw new IllegalStateException("ไม่สามารถยกเลิกการจองที่ยืนยันแล้วได้");
        }

        if (status == BookingStatus.CANCEL) {
            throw new IllegalStateException("การจองนี้ถูกยกเลิกไปแล้ว");
        }
        if (status == BookingStatus.COMPLETED) {
            throw new IllegalStateException("ไม่สามารถยกเลิกการจองที่เสร็จสิ้นแล้วได้");
        }

        booking.setBookingStatus(BookingStatus.CANCEL);
        booking.setCancelReason(
                "ยกเลิกโดยผู้จอง" + (reason != null && !reason.isBlank() ? ": " + reason.trim() : ""));
        bookingRepository.save(booking);
    }
}