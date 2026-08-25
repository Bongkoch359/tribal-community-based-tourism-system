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
import java.util.HashSet;
import java.util.List;
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

    // ════════════════════════════════════════════════════════
    // BOOKING DETAIL FOR COMMUNITY MANAGER
    // ════════════════════════════════════════════════════════

    /**
     * ดึงรายละเอียดการจองทัวร์ 1 รายการ สำหรับหน้า "รายละเอียดการจอง" ของ manager
     * ตรวจสิทธิ์ในตัว (managerId ต้องเป็นเจ้าของทัวร์ที่ถูกจองนี้) มิเช่นนั้นโยน
     * RuntimeException ("ไม่พบการจอง หรือไม่มีสิทธิ์เข้าถึง")
     *
     * Payment ไม่ได้ join fetch มาด้วย (เพราะเป็นความสัมพันธ์ mappedBy ฝั่ง
     * Payment)
     * จึงดึงแยกแล้ว set เข้า booking ก่อน return ให้ view ใช้ booking.payment
     * ได้ตามปกติ
     */
    @Transactional(readOnly = true)
    public Booking getTourBookingDetailForManager(String bookingId, String managerId) {
        Booking booking = bookingRepository
                .findTourBookingDetailForManager(bookingId, managerId)
                .orElseThrow(() -> new RuntimeException("ไม่พบการจอง หรือไม่มีสิทธิ์เข้าถึงการจองนี้"));

        // ดึง guests แยกต่างหาก กัน cartesian product กับ tourDetails
        List<Guest> guests = guestRepository.findByBooking_Bookingid(bookingId);
        booking.setGuests(new HashSet<>(guests));

        Payment payment = paymentRepository.findByBooking_Bookingid(bookingId);
        booking.setPayment(payment);

        return booking;
    }

    /**
     * ยืนยันการจองทัวร์ (manager) — ทำได้เฉพาะการจองที่อยู่ในสถานะ "รอตรวจสอบ"
     * เท่านั้น
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
    booking.setCancelReason("ยกเลิกโดยผู้ดูแลชุมชน: " + reason.trim());   // ← เติม prefix
    bookingRepository.save(booking);
}

    // ════════════════════════════════════════════════════════
    // CREATE TOUR BOOKING
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

        // ── 2. ดึง Tour ─────────────────────────────────────
        Tour tour = tourRepository.findById(tourId)
                .orElseThrow(() -> new RuntimeException("ไม่พบทัวร์"));

        // ── 2.5 ดึง schedule ของวันที่เลือก ───────────────
        Tourschedule schedule = tourScheduleRepository
                .findByTourTouridAndOpendate(tourId, java.sql.Date.valueOf(startDate))
                .orElseThrow(() -> new IllegalArgumentException(
                        "ไม่พบรอบทัวร์ในวันที่เลือก กรุณาเลือกวันที่ที่เปิดรับจอง"));

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
        // ผู้จองเองมาเป็นตัวแรกใน guestIdcards ด้วย (ฝั่ง HTML
        // วางช่องผู้จองไว้ก่อนแขกคนอื่นเสมอ)
        // ดังนั้นจำนวนเลขบัตรที่ต้องมี = totalGuest
        // เท่ากันไม่ว่าจะไปเองหรือจองให้คนอื่น
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

        // ── 4.6 Validate จุดรับ (กรณีให้ทัวร์ไปรับที่โรงแรม
        // ต้องอยู่ในเชียงใหม่เท่านั้น) ──
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
        booking.setBookingid(bookingIdGenerator.generateBookingId());
        booking.setMember(member);
        booking.setBookingType(BookingType.TOUR);
        booking.setBookingStatus(BookingStatus.PENDING);
        booking.setBookingdate(new Date(System.currentTimeMillis()));
booking.setPaymentDeadline(new java.sql.Timestamp(System.currentTimeMillis() + 30 * 60 * 1000)); // ★ เพิ่ม — deadline = ตอนนี้ + 30 นาที
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
        detail.setSubtotaltour(tourSubtotal); // ค่าทัวร์ล้วนๆ ไม่รวมประกัน

        bookingtourdetailRepository.save(detail);

        // ── 8. Guest ─────────────────────────────────────────
        // ถ้าผู้จองไปเองและทำประกัน → ต้องเก็บเลขบัตรของผู้จองไว้ด้วย
        // เนื่องจากไม่มีที่เก็บเลขบัตรใน Member/Booking เราจึงสร้าง Guest
        // record แทนตัวผู้จองขึ้นมาเก็บชื่อ-นามสกุล-เลขบัตรของผู้จองเอง
        // (ฝั่ง HTML ส่ง guestIdcard ตัวแรกมาเป็นของผู้จองเสมอ เมื่อ isBookerGoing =
        // true)
        int idcardOffset = 0;

        if (Boolean.TRUE.equals(isBookerGoing) && insurance
                && guestIdcards != null && !guestIdcards.isEmpty()) {

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
    // EDIT TOUR BOOKING
    // ════════════════════════════════════════════════════════

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

        // ── 1. ดึง Booking ──────────────────────────────────
        Booking booking = bookingRepository
                .findByIdWithDetails(bookingId)
                .orElseThrow(() -> new RuntimeException("ไม่พบการจอง"));

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

        Bookingtourdetail detail = booking.getTourDetails().get(0);

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

            // ล็อกรอบใหม่ไว้ ก่อนเช็คที่นั่ง กันคนอื่นแย่งที่พร้อมกัน
            newSchedule = newScheduleRef;

            if (!"เปิดรับจอง".equals(newSchedule.getStatus())) {
                throw new IllegalArgumentException("รอบทัวร์วันที่เลือกไม่เปิดรับจองแล้ว");
            }
        } else {
            // วันเดิม ไม่ได้เปลี่ยนรอบ — ยังล็อกไว้เพราะจำนวนคนอาจเปลี่ยน
            newSchedule = oldSchedule;
        }

        // ── 6. คำนวณใหม่ ──────────────────────────────────
        int adults = (adult != null && adult > 0) ? adult : 1;

        int childs = (children != null) ? children : 0;

        int totalGuest = adults + childs;

        // ── 6.2 เช็คที่นั่งของรอบใหม่ ───────────────────────
        // ถ้าเปลี่ยนวัน: เช็คที่นั่งว่างของรอบใหม่ตรงๆ (ยังไม่มีคนของ booking
        // นี้อยู่ในรอบนั้น)
        // ถ้าไม่เปลี่ยนวัน: booking นี้นับรวมอยู่ใน bookedInSchedule แล้ว
        // ต้องหักจำนวนเดิมออกก่อน
        // ถึงจะได้ที่นั่งว่างที่แท้จริงสำหรับเทียบกับจำนวนคนใหม่
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

        // ── 6.5 ยอดประกันเดิม (ถ้ามี) — คงค่าตามที่จองไว้ตอนแรก
        // แล้วปรับสัดส่วนตามจำนวนคนใหม่ ถ้าเคยติ๊กประกันไว้
        double subtotalInsurance = 0.0;
        if (Boolean.TRUE.equals(booking.getWantInsurance())
                && booking.getInsuranceFeePerPerson() != null) {
            subtotalInsurance = booking.getInsuranceFeePerPerson() * totalGuest;
        }
        double grandTotal = subtotal + subtotalInsurance;

        // ── 7. อัปเดต detail ──────────────────────────────
        detail.setTourschedule(newSchedule); // ⬅ผูกกับรอบใหม่ (สำคัญ — เดิมไม่เคยอัปเดต)
        detail.setNumofadult(adults);
        detail.setNumofchild(childs);
        detail.setSubtotaltour(subtotal);

        bookingtourdetailRepository.save(detail);

        // ── 8. อัปเดต booking ─────────────────────────────
        booking.setNumofguest(totalGuest);
        booking.setNote(note);
        booking.setTotalamount(grandTotal);
        booking.setPickuptype(pickuptype);
        booking.setPickuplocation(pickuplocation);
        booking.setSubtotalInsurance(subtotalInsurance);

        bookingRepository.save(booking);

        // ── 9. Guest — อัปเดตด้วย guestId แบบตรงๆ ไม่พึ่ง index/ลำดับ ──
        if (guestIds != null && !guestIds.isEmpty()) {

            // โหลด guest ของ booking นี้เข้า map ตาม guestId เพื่อ lookup เร็วและแม่นยำ
            Set<Guest> guests = booking.getGuests();
            java.util.Map<String, Guest> guestById = new java.util.HashMap<>();
            if (guests != null) {
                for (Guest g : guests) {
                    guestById.put(g.getGuestid(), g);
                }
            }

            for (int i = 0; i < guestIds.size(); i++) {
                String gId = guestIds.get(i);
                if (gId == null || gId.isBlank())
                    continue;

                Guest g = guestById.get(gId);
                if (g == null) {
                    // guestId ที่ส่งมาไม่ตรงกับ guest ของ booking นี้เลย — ข้าม ป้องกันแก้ guest
                    // คนอื่น
                    continue;
                }

                String fname = (guestFirstnames != null && i < guestFirstnames.size())
                        ? guestFirstnames.get(i)
                        : null;
                String lname = (guestLastnames != null && i < guestLastnames.size())
                        ? guestLastnames.get(i)
                        : null;
                String idcard = (guestIdcards != null && i < guestIdcards.size())
                        ? guestIdcards.get(i)
                        : null;

                if (fname != null && !fname.isBlank()) {
                    g.setFirstname(fname.trim());
                }
                if (lname != null) {
                    g.setLastname(lname.trim());
                }
                if (Boolean.TRUE.equals(booking.getWantInsurance())
                        && idcard != null && idcard.trim().length() == 13) {
                    g.setIdcardnumber(idcard.trim());
                }

                guestRepository.save(g);
            }
        }
    }

    // ════════════════════════════════════════════════════════
    // CANCEL TOUR BOOKING
    // ════════════════════════════════════════════════════════

    @Transactional
    public void cancelTourBooking(String bookingId, String memberId, String reason) {

        Booking booking = bookingRepository
                .findByIdWithDetails(bookingId)
                .orElseThrow(() -> new RuntimeException("ไม่พบการจอง"));

        // ตรวจสิทธิ์
        if (!booking.getMember().getMemberid()
                .equals(memberId)) {

            throw new IllegalArgumentException(
                    "ไม่มีสิทธิ์ยกเลิกการจองนี้");
        }

        // ตรวจสถานะ
        BookingStatus status = booking.getBookingStatus();

        if (status == BookingStatus.CONFIRMED) {

            throw new IllegalStateException(
                    "ไม่สามารถยกเลิกการจองที่ยืนยันแล้วได้");
        }

        if (status == BookingStatus.CANCEL) {

            throw new IllegalStateException(
                    "การจองนี้ถูกยกเลิกไปแล้ว");
        }
        if (booking.getBookingStatus() == BookingStatus.COMPLETED) {
            throw new IllegalStateException("ไม่สามารถยกเลิกการจองที่เสร็จสิ้นแล้วได้");
        }

          booking.setBookingStatus(BookingStatus.CANCEL);
     booking.setCancelReason(
        "ยกเลิกโดยผู้จอง" + (reason != null && !reason.isBlank() ? ": " + reason.trim() : "")
    );
    bookingRepository.save(booking);

      
    }
}