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

        // รายได้รวมรายเดือน (เฉพาะยืนยันแล้ว/เสร็จสิ้น) ของ homestay
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

        @Query("SELECT YEAR(b.bookingdate) as yr, MONTH(b.bookingdate) as mo, " +
                        "COALESCE(SUM(b.totalamount), 0) as total FROM Booking b " +
                        "JOIN b.roomDetails rd " +
                        "JOIN rd.roomtype rt " +
                        "WHERE rt.homestay.homestayid = :homestayId " +
                        "AND b.bookingStatus IN (com.example.miniproject.entity.enums.BookingStatus.CONFIRMED, " +
                        "                         com.example.miniproject.entity.enums.BookingStatus.COMPLETED) " +
                        "AND b.bookingdate >= :startDate AND b.bookingdate <= :endDate " +
                        "GROUP BY YEAR(b.bookingdate), MONTH(b.bookingdate) " +
                        "ORDER BY YEAR(b.bookingdate) ASC, MONTH(b.bookingdate) ASC")
        List<Object[]> sumRevenueByMonthRangeByHomestayId(@Param("homestayId") Integer homestayId,
                        @Param("startDate") java.sql.Date startDate,
                        @Param("endDate") java.sql.Date endDate);

        // จำนวนการจองรายเดือน
        @Query("SELECT YEAR(b.bookingdate) as yr, MONTH(b.bookingdate) as mo, " +
                        "COUNT(DISTINCT b) as cnt FROM Booking b " +
                        "JOIN b.roomDetails rd " +
                        "JOIN rd.roomtype rt " +
                        "WHERE rt.homestay.homestayid = :homestayId " +
                        "AND b.bookingStatus IN (com.example.miniproject.entity.enums.BookingStatus.CONFIRMED, " +
                        "                         com.example.miniproject.entity.enums.BookingStatus.COMPLETED) " +
                        "AND b.bookingdate >= :startDate AND b.bookingdate <= :endDate " +
                        "GROUP BY YEAR(b.bookingdate), MONTH(b.bookingdate) " +
                        "ORDER BY YEAR(b.bookingdate) ASC, MONTH(b.bookingdate) ASC")
        List<Object[]> countBookingsByMonthRangeByHomestayId(@Param("homestayId") Integer homestayId,
                        @Param("startDate") java.sql.Date startDate,
                        @Param("endDate") java.sql.Date endDate);

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

      


        /** ดึงการจองทัวร์ทั้งหมดของ manager คนนั้น (ทุกสถานะ) */
        @Query("""
                            SELECT DISTINCT b FROM Booking b
                            LEFT JOIN FETCH b.member
                            LEFT JOIN FETCH b.tourDetails td
                            LEFT JOIN FETCH td.tourschedule ts
                            LEFT JOIN FETCH ts.tour t
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
                            LEFT JOIN FETCH td.tourschedule ts
                            LEFT JOIN FETCH ts.tour t
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
         */
        @Query("""
                            SELECT DISTINCT b FROM Booking b
                            LEFT JOIN FETCH b.member
                            LEFT JOIN FETCH b.tourDetails td
                            LEFT JOIN FETCH td.tourschedule ts
                            LEFT JOIN FETCH ts.tour t
                            LEFT JOIN FETCH t.tourtype
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

        // เช็ควันหมดอายุ
        @Query("""
                            SELECT DISTINCT b FROM Booking b
                            WHERE b.bookingStatus = com.example.miniproject.entity.enums.BookingStatus.PENDING
                            AND b.bookingType = com.example.miniproject.entity.enums.BookingType.TOUR
                            AND b.paymentDeadline <= CURRENT_TIMESTAMP
                        """)
        List<Booking> findExpiredPendingTourBookings();

        @Query("""
                            SELECT DISTINCT b FROM Booking b
                            WHERE b.bookingStatus = com.example.miniproject.entity.enums.BookingStatus.PENDING
                            AND b.bookingType = com.example.miniproject.entity.enums.BookingType.ACCOMMODATION
                            AND b.paymentDeadline <= CURRENT_TIMESTAMP
                        """)
        List<Booking> findExpiredPendingRoomBookings();

        // ─── ของทัวร์ (global ทุก manager) ──────────────────────
        @Query("""
                            SELECT COUNT(b) FROM Booking b
                            JOIN b.tourDetails td
                            JOIN td.tourschedule ts
                            JOIN ts.tour t
                            WHERE b.bookingType = com.example.miniproject.entity.enums.BookingType.TOUR
                            AND b.bookingStatus = com.example.miniproject.entity.enums.BookingStatus.WAITING_APPROVAL
                            AND t.communitymanager.managerid = :managerId
                        """)
        long countPendingTourBookingsByManagerId(@Param("managerId") String managerId);

        /** รายได้ทัวร์รายเดือน แบบเลือกช่วงได้ */
        @Query("SELECT YEAR(b.bookingdate) as yr, MONTH(b.bookingdate) as mo, " +
                        "COALESCE(SUM(b.totalamount), 0) as total FROM Booking b " +
                        "WHERE b.bookingType = com.example.miniproject.entity.enums.BookingType.TOUR " +
                        "AND b.bookingStatus IN (com.example.miniproject.entity.enums.BookingStatus.CONFIRMED, " +
                        "                         com.example.miniproject.entity.enums.BookingStatus.COMPLETED) " +
                        "AND b.bookingdate >= :startDate AND b.bookingdate <= :endDate " +
                        "GROUP BY YEAR(b.bookingdate), MONTH(b.bookingdate) " +
                        "ORDER BY YEAR(b.bookingdate) ASC, MONTH(b.bookingdate) ASC")
        List<Object[]> sumTourRevenueByMonthRange(@Param("startDate") java.sql.Date startDate,
                        @Param("endDate") java.sql.Date endDate);

        @Query("SELECT YEAR(b.bookingdate) as yr, MONTH(b.bookingdate) as mo, " +
                        "COALESCE(SUM(b.totalamount), 0) as total FROM Booking b " +
                        "JOIN b.tourDetails td JOIN td.tourschedule ts JOIN ts.tour t " +
                        "WHERE b.bookingType = com.example.miniproject.entity.enums.BookingType.TOUR " +
                        "AND b.bookingStatus IN (com.example.miniproject.entity.enums.BookingStatus.CONFIRMED, " +
                        "                         com.example.miniproject.entity.enums.BookingStatus.COMPLETED) " +
                        "AND t.communitymanager.managerid = :managerId " +
                        "AND b.bookingdate >= :startDate AND b.bookingdate <= :endDate " +
                        "GROUP BY YEAR(b.bookingdate), MONTH(b.bookingdate) " +
                        "ORDER BY YEAR(b.bookingdate) ASC, MONTH(b.bookingdate) ASC")
        List<Object[]> sumTourRevenueByMonthRangeAndManager(@Param("managerId") String managerId,
                        @Param("startDate") java.sql.Date startDate,
                        @Param("endDate") java.sql.Date endDate);

        /** จำนวนการจองทัวร์รายเดือน แบบเลือกช่วงได้ (เฉพาะของ manager คนนั้น) */
        @Query("SELECT YEAR(b.bookingdate) as yr, MONTH(b.bookingdate) as mo, " +
                        "COUNT(DISTINCT b) as cnt FROM Booking b " +
                        "JOIN b.tourDetails td JOIN td.tourschedule ts JOIN ts.tour t " +
                        "WHERE b.bookingType = com.example.miniproject.entity.enums.BookingType.TOUR " +
                        "AND b.bookingStatus IN (com.example.miniproject.entity.enums.BookingStatus.CONFIRMED, " +
                        "                         com.example.miniproject.entity.enums.BookingStatus.COMPLETED) " +
                        "AND t.communitymanager.managerid = :managerId " +
                        "AND b.bookingdate >= :startDate AND b.bookingdate <= :endDate " +
                        "GROUP BY YEAR(b.bookingdate), MONTH(b.bookingdate) " +
                        "ORDER BY YEAR(b.bookingdate) ASC, MONTH(b.bookingdate) ASC")
        List<Object[]> countTourBookingsByMonthRangeAndManager(@Param("managerId") String managerId,
                        @Param("startDate") java.sql.Date startDate,
                        @Param("endDate") java.sql.Date endDate);

        @Query("""
                            SELECT DISTINCT b FROM Booking b
                            LEFT JOIN FETCH b.member
                            LEFT JOIN FETCH b.review r
                            LEFT JOIN FETCH b.tourDetails td
                            LEFT JOIN FETCH td.tourschedule ts
                            LEFT JOIN FETCH ts.tour t
                            WHERE b.member.memberid = :memberId
                              AND b.bookingType = com.example.miniproject.entity.enums.BookingType.TOUR
                              AND (:status IS NULL OR b.bookingStatus = :status)
                            ORDER BY
                                CASE
                                    WHEN b.bookingStatus = com.example.miniproject.entity.enums.BookingStatus.PENDING THEN 0
                                    WHEN b.bookingStatus = com.example.miniproject.entity.enums.BookingStatus.WAITING_APPROVAL THEN 1
                                    WHEN b.bookingStatus = com.example.miniproject.entity.enums.BookingStatus.CONFIRMED THEN 2
                                    WHEN b.bookingStatus = com.example.miniproject.entity.enums.BookingStatus.COMPLETED AND r IS NULL THEN 3
                                    WHEN b.bookingStatus = com.example.miniproject.entity.enums.BookingStatus.COMPLETED AND r IS NOT NULL THEN 4
                                    WHEN b.bookingStatus = com.example.miniproject.entity.enums.BookingStatus.CANCEL THEN 5
                                    ELSE 6
                                END ASC,
                                b.bookingdate DESC
                        """)
        List<Booking> findTourBookingsByMemberSortedByPriority(
                        @Param("memberId") String memberId,
                        @Param("status") BookingStatus status);

        @Query("""
                            SELECT DISTINCT b FROM Booking b
                            LEFT JOIN FETCH b.member
                            LEFT JOIN FETCH b.review r
                            LEFT JOIN FETCH b.roomDetails rd
                            LEFT JOIN FETCH rd.roomtype rt
                            LEFT JOIN FETCH rt.homestay h
                            WHERE b.member.memberid = :memberId
                              AND b.bookingType = com.example.miniproject.entity.enums.BookingType.ACCOMMODATION
                              AND (:status IS NULL OR b.bookingStatus = :status)
                            ORDER BY
                                CASE
                                    WHEN b.bookingStatus = com.example.miniproject.entity.enums.BookingStatus.PENDING THEN 0
                                    WHEN b.bookingStatus = com.example.miniproject.entity.enums.BookingStatus.WAITING_APPROVAL THEN 1
                                    WHEN b.bookingStatus = com.example.miniproject.entity.enums.BookingStatus.CONFIRMED THEN 2
                                    WHEN b.bookingStatus = com.example.miniproject.entity.enums.BookingStatus.COMPLETED AND r IS NULL THEN 3
                                    WHEN b.bookingStatus = com.example.miniproject.entity.enums.BookingStatus.COMPLETED AND r IS NOT NULL THEN 4
                                    WHEN b.bookingStatus = com.example.miniproject.entity.enums.BookingStatus.CANCEL THEN 5
                                    ELSE 6
                                END ASC,
                                b.bookingdate DESC
                        """)
        List<Booking> findAccommodationBookingsByMemberSortedByPriority(
                        @Param("memberId") String memberId,
                        @Param("status") BookingStatus status);
}
