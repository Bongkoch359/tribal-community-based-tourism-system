package com.example.miniproject.repository.Member;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.example.miniproject.entity.Booking;
import com.example.miniproject.entity.Bookingroomdetail;
import com.example.miniproject.entity.enums.BookingStatus;
import com.example.miniproject.entity.enums.BookingType;

@Repository
public interface BookingRepository extends JpaRepository<Booking, String> {

    List<Booking> findTop5ByOrderByBookingdateDesc();

    @Query("SELECT COUNT(b) FROM Booking b WHERE b.bookingStatus = 'PENDING'")
    long countPendingBookings();

    /** ดึงการจองทั้งหมดของ Member คนนั้น */
    List<Booking> findByMemberMemberidOrderByBookingdateDesc(String memberid);

    /** ดึงการจองตาม type */
    List<Booking> findByBookingType(BookingType bookingType);

    /** ดึงตาม status */
    List<Booking> findByBookingStatus(BookingStatus bookingStatus);

    /** รายได้รวมทั้งระบบ */
    @Query("SELECT COALESCE(SUM(b.totalamount), 0) FROM Booking b WHERE b.bookingStatus = :status")
    double sumTotalRevenue(@Param("status") BookingStatus status);

    @Query("SELECT COUNT(b) FROM Booking b WHERE b.bookingStatus = :status")
    long countByBookingStatus(@Param("status") BookingStatus status);

    @Query("""
                SELECT DISTINCT b FROM Booking b
                LEFT JOIN FETCH b.member
                LEFT JOIN FETCH b.roomDetails rd
                LEFT JOIN FETCH rd.roomtype rt
                LEFT JOIN FETCH rt.homestay
                LEFT JOIN FETCH b.guests
                WHERE b.bookingid = :id
            """)
    Optional<Booking> findByIdWithDetails(@Param("id") String id);

    /** นับการจองรอตรวจสอบของ homestay */
    @Query("SELECT COUNT(b) FROM Booking b " +
            "JOIN b.roomDetails rd " +
            "JOIN rd.roomtype rt " +
            "WHERE rt.homestay.homestayid = :homestayId " +
            "AND b.bookingStatus = :status")
    long countByRoomHomestayIdAndStatus(
            @Param("homestayId") Integer homestayId,
            @Param("status") BookingStatus status);
// ================== ของโฮมสเตย์ ==================
    /**
     * รายได้รวมของ homestay (นับทั้ง CONFIRMED และ COMPLETED
     * เพราะถือว่าจ่ายเงินแล้วทั้งคู่)
     */
    @Query("SELECT COALESCE(SUM(b.totalamount), 0) FROM Booking b " +
            "JOIN b.roomDetails rd " +
            "JOIN rd.roomtype rt " +
            "WHERE rt.homestay.homestayid = :homestayId " +
            "AND b.bookingStatus IN (com.example.miniproject.entity.enums.BookingStatus.CONFIRMED, " +
            "                         com.example.miniproject.entity.enums.BookingStatus.COMPLETED)")
    double sumConfirmedRevenueByHomestayId(@Param("homestayId") Integer homestayId);

    /** การจองที่ "รอตรวจสอบ" ล่าสุด 5 รายการของ homestay */
    @Query("SELECT b FROM Booking b " +
            "JOIN b.roomDetails rd " +
            "JOIN rd.roomtype rt " +
            "WHERE rt.homestay.homestayid = :homestayId " +
            "AND b.bookingStatus = :status " +
            "ORDER BY b.bookingdate DESC " +
            "LIMIT 5")
    List<Booking> findTop5ByHomestayIdAndStatus(@Param("homestayId") Integer homestayId,
            @Param("status") BookingStatus status);

    // // ─── กิจกรรมวันนี้: เช็คอิน / เช็คเอาท์ ───────────────────────────

    // /**
    // * จำนวนห้องที่มีแขก "เช็คอิน" วันนี้ (นับตามรายละเอียดห้อง ไม่ใช่นับ booking)
    // */
    // @Query("SELECT COUNT(rd) FROM Bookingroomdetail rd " +
    // "JOIN rd.roomtype rt " +
    // "JOIN rd.booking b " +
    // "WHERE rt.homestay.homestayid = :homestayId " +
    // "AND rd.checkindate = :today " +
    // "AND b.bookingStatus IN
    // (com.example.miniproject.entity.enums.BookingStatus.CONFIRMED, " +
    // " com.example.miniproject.entity.enums.BookingStatus.COMPLETED)")
    // long countCheckinsTodayByHomestayId(@Param("homestayId") Integer homestayId,
    // @Param("today") java.sql.Date today);

    // /** จำนวนห้องที่มีแขก "เช็คเอาท์" วันนี้ (= ห้องที่ต้องทำความสะอาดวันนี้ด้วย)
    // */
    // @Query("SELECT COUNT(rd) FROM Bookingroomdetail rd " +
    // "JOIN rd.roomtype rt " +
    // "JOIN rd.booking b " +
    // "WHERE rt.homestay.homestayid = :homestayId " +
    // "AND rd.checkoutdate = :today " +
    // "AND b.bookingStatus IN
    // (com.example.miniproject.entity.enums.BookingStatus.CONFIRMED, " +
    // " com.example.miniproject.entity.enums.BookingStatus.COMPLETED)")
    // long countCheckoutsTodayByHomestayId(@Param("homestayId") Integer homestayId,
    // @Param("today") java.sql.Date today);

    // ─── รายได้รวมรายเดือน (เฉพาะยืนยันแล้ว/เสร็จสิ้น) ของ homestay ────────────
    /**
     * รายได้รวมรายเดือน (เฉพาะยืนยันแล้ว/เสร็จสิ้น) ของ homestay
     * ตั้งแต่ :startDate จนถึงวันนี้ — คืนค่าเป็น [Integer year, Integer month,
     * Double total]
     */
    @Query("SELECT YEAR(b.bookingdate) as yr, MONTH(b.bookingdate) as mo, " +
            "COALESCE(SUM(b.totalamount), 0) as total FROM Booking b " +
            "JOIN b.roomDetails rd " +
            "JOIN rd.roomtype rt " +
            "WHERE rt.homestay.homestayid = :homestayId " +
            "AND b.bookingStatus IN (com.example.miniproject.entity.enums.BookingStatus.CONFIRMED, " +
            "                         com.example.miniproject.entity.enums.BookingStatus.COMPLETED) " +
            "AND b.bookingdate >= :startDate " +
            "GROUP BY YEAR(b.bookingdate), MONTH(b.bookingdate) " +
            "ORDER BY YEAR(b.bookingdate) ASC, MONTH(b.bookingdate) ASC")
    List<Object[]> sumRevenueByMonthByHomestayId(@Param("homestayId") Integer homestayId,
            @Param("startDate") java.sql.Date startDate);

    // ─── ดึงการจองทั้งหมดของ homestay ───────────
    /** ดึงการจองทั้งหมดของ homestay เรียงวันที่ล่าสุดก่อน */
    @Query("SELECT DISTINCT b FROM Booking b " +
            "LEFT JOIN FETCH b.member " +
            "LEFT JOIN FETCH b.roomDetails rd " +
            "LEFT JOIN FETCH rd.roomtype rt " +
            "WHERE rt.homestay.homestayid = :homestayId " +
            "ORDER BY b.bookingdate DESC")
    List<Booking> findAllByHomestayId(@Param("homestayId") Integer homestayId);

    /** ดึงการจองของ homestay กรอง status */
    @Query("SELECT DISTINCT b FROM Booking b " +
            "LEFT JOIN FETCH b.member " +
            "LEFT JOIN FETCH b.roomDetails rd " +
            "LEFT JOIN FETCH rd.roomtype rt " +
            "WHERE rt.homestay.homestayid = :homestayId " +
            "AND b.bookingStatus = :status " +
            "ORDER BY b.bookingdate DESC")
    List<Booking> findAllByHomestayIdAndStatus(
            @Param("homestayId") Integer homestayId,
            @Param("status") BookingStatus status);

    // =====================================================================================================================================

    // ─── การจองทัวร์ของ manager (สำหรับหน้ารายการจองทัวร์) ───

    /** ดึงการจองทัวร์ทั้งหมดของ manager คนนั้น (ทุกสถานะ) */
    @Query("""
                SELECT DISTINCT b FROM Booking b
                LEFT JOIN FETCH b.member
                LEFT JOIN FETCH b.tourDetails td
                LEFT JOIN FETCH td.tour t
                LEFT JOIN FETCH td.tourschedule
                WHERE t.communitymanager.managerid = :managerId
                AND b.bookingType = com.example.miniproject.entity.enums.BookingType.TOUR
                ORDER BY b.bookingdate DESC
            """)
    List<Booking> findTourBookingsByManagerId(@Param("managerId") String managerId);

    /** ดึงการจองทัวร์ของ manager กรองตามสถานะ */
    @Query("""
                SELECT DISTINCT b FROM Booking b
                LEFT JOIN FETCH b.member
                LEFT JOIN FETCH b.tourDetails td
                LEFT JOIN FETCH td.tour t
                LEFT JOIN FETCH td.tourschedule
                WHERE t.communitymanager.managerid = :managerId
                AND b.bookingType = com.example.miniproject.entity.enums.BookingType.TOUR
                AND b.bookingStatus = :status
                ORDER BY b.bookingdate DESC
            """)
    List<Booking> findTourBookingsByManagerIdAndStatus(
            @Param("managerId") String managerId,
            @Param("status") BookingStatus status);

    /**
     * ดึงการจองทัวร์ "รายการเดียว" พร้อมรายละเอียด สำหรับหน้า "รายละเอียดการจอง"
     * ของ manager
     * — ผูก managerId ไว้ใน WHERE เลย เพื่อกันไม่ให้ manager คนอื่นเปิดดู/แก้ไข
     * การจองทัวร์ที่ไม่ใช่ของชุมชนตัวเอง (ป้องกัน IDOR)
     */
    @Query("""
                SELECT DISTINCT b FROM Booking b
                LEFT JOIN FETCH b.member
                LEFT JOIN FETCH b.tourDetails td
                LEFT JOIN FETCH td.tour t
                LEFT JOIN FETCH t.tourtype
                LEFT JOIN FETCH td.tourschedule
                WHERE b.bookingid = :bookingId
                AND b.bookingType = com.example.miniproject.entity.enums.BookingType.TOUR
                AND t.communitymanager.managerid = :managerId
            """)
    Optional<Booking> findTourBookingDetailForManager(
            @Param("bookingId") String bookingId,
            @Param("managerId") String managerId);

    @Query("SELECT DISTINCT b FROM Booking b " +
            "JOIN b.roomDetails rd " +
            "WHERE b.member.memberid = :memberId " +
            "AND rd.roomtype.homestay.homestayid = :homestayId " +
            "AND b.bookingStatus = com.example.miniproject.entity.enums.BookingStatus.COMPLETED " +
            "AND b.review IS NULL")
    List<Booking> findCompletedBookingsWithoutReview(@Param("memberId") String memberId,
            @Param("homestayId") Integer homestayId);

            //เช็ควันหมดอายุ
@Query("""
            SELECT DISTINCT b FROM Booking b
            JOIN b.tourDetails td
            JOIN td.tourschedule ts
            WHERE b.bookingStatus = com.example.miniproject.entity.enums.BookingStatus.PENDING
            AND b.bookingType = com.example.miniproject.entity.enums.BookingType.TOUR
            AND ts.opendate <= CURRENT_DATE
        """)
List<Booking> findExpiredPendingTourBookings();

@Query("""
    SELECT DISTINCT b FROM Booking b
    JOIN b.roomDetails rd
    WHERE b.bookingStatus = com.example.miniproject.entity.enums.BookingStatus.PENDING
    AND b.bookingType = com.example.miniproject.entity.enums.BookingType.ACCOMMODATION
    AND rd.checkindate <= CURRENT_DATE
""")
List<Booking> findExpiredPendingRoomBookings();

}